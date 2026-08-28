import React, { useState, useEffect } from 'react';
import {
  Plane,
  Plus,
  Sparkles,
  Calendar,
  MapPin,
  Clock,
  DollarSign,
  Trash2,
  ChevronRight,
  Compass,
  ArrowRight,
  CheckCircle,
} from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';
import { travelApi } from '../../lib/api';
import { Trip, TripSummary, TripStatus, ItineraryItem } from '../../types';
import { formatCurrency, formatDate, cn } from '../../lib/utils';
import { StatCard } from '../../components/common/StatCard';
import { Badge } from '../../components/common/Badge';
import { Button } from '../../components/common/Button';
import { Modal } from '../../components/common/Modal';
import { EmptyState } from '../../components/common/EmptyState';

export const TravelPage: React.FC = () => {
  const { user } = useAuthStore();
  const currency = user?.currency || 'INR';

  const [trips, setTrips] = useState<Trip[]>([]);
  const [summary, setSummary] = useState<TripSummary | null>(null);
  const [selectedTrip, setSelectedTrip] = useState<Trip | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Modals
  const [isCreateTripOpen, setIsCreateTripOpen] = useState(false);
  const [isAiPlanOpen, setIsAiPlanOpen] = useState(false);
  const [isAddItineraryOpen, setIsAddItineraryOpen] = useState(false);
  const [isAiGenerating, setIsAiGenerating] = useState(false);

  // Form States
  const [tripForm, setTripForm] = useState({
    name: '',
    destination: '',
    startDate: '',
    endDate: '',
    budget: '',
    description: '',
  });

  const [aiForm, setAiForm] = useState({
    destination: '',
    days: 3,
    budget: 15000,
    travelers: 1,
    interests: 'Sightseeing, Local Food, Culture',
  });

  const [itineraryForm, setItineraryForm] = useState({
    dayNumber: 1,
    title: '',
    description: '',
    location: '',
    startTime: '',
    endTime: '',
    estimatedCost: '',
    notes: '',
  });

  const loadTrips = async () => {
    setIsLoading(true);
    try {
      const [allTrips, sum] = await Promise.all([
        travelApi.getTrips(),
        travelApi.getSummary(),
      ]);
      const safeTrips = Array.isArray(allTrips) ? allTrips : [];
      setTrips(safeTrips);
      setSummary(sum);

      if (safeTrips.length > 0) {
        // Keep selected trip or select first
        setSelectedTrip((prev) => (prev ? safeTrips.find((t) => t.id === prev.id) || safeTrips[0] : safeTrips[0]));
      } else {
        setSelectedTrip(null);
      }
    } catch (err) {
      console.error('Failed to load trips:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadTrips();
  }, []);

  const handleCreateTrip = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!tripForm.destination || !tripForm.startDate || !tripForm.endDate) return;

    try {
      const created = await travelApi.createTrip({
        name: tripForm.name || `Trip to ${tripForm.destination}`,
        destination: tripForm.destination,
        startDate: tripForm.startDate,
        endDate: tripForm.endDate,
        budget: tripForm.budget ? parseFloat(tripForm.budget) : 0,
        currency,
        description: tripForm.description,
      });

      setIsCreateTripOpen(false);
      setTripForm({
        name: '',
        destination: '',
        startDate: '',
        endDate: '',
        budget: '',
        description: '',
      });
      await loadTrips();
      setSelectedTrip(created);
    } catch (err) {
      console.error('Failed to create trip:', err);
    }
  };

  const handleAiPlanGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!aiForm.destination) return;

    setIsAiGenerating(true);
    try {
      const plan = await travelApi.generateTripPlan(aiForm);

      // Now create a trip with this plan
      const today = new Date();
      const nextMonth = new Date(today.getTime() + 14 * 24 * 60 * 60 * 1000);
      const startStr = nextMonth.toISOString().split('T')[0];
      const endObj = new Date(nextMonth.getTime() + (aiForm.days - 1) * 24 * 60 * 60 * 1000);
      const endStr = endObj.toISOString().split('T')[0];

      const newTrip = await travelApi.createTrip({
        name: plan.tripName || `${aiForm.days}-Day ${aiForm.destination} Tour`,
        destination: aiForm.destination,
        startDate: startStr,
        endDate: endStr,
        budget: aiForm.budget,
        currency,
        description: `AI Generated ${aiForm.interests} itinerary for ${aiForm.travelers} traveler(s).`,
      });

      // Add itinerary items from AI plan
      if (plan.itinerary && Array.isArray(plan.itinerary)) {
        for (const item of plan.itinerary) {
          await travelApi.addItineraryItem(newTrip.id, {
            dayNumber: item.dayNumber || 1,
            title: item.title || 'Sightseeing',
            description: item.description || '',
            location: item.location || aiForm.destination,
            startTime: item.startTime || '09:00:00',
            endTime: item.endTime || '12:00:00',
            estimatedCost: item.estimatedCost || 0,
            notes: item.notes || '',
          });
        }
      }

      setIsAiPlanOpen(false);
      await loadTrips();
      const updated = await travelApi.getTripById(newTrip.id);
      setSelectedTrip(updated);
    } catch (err) {
      console.error('AI trip planning failed:', err);
    } finally {
      setIsAiGenerating(false);
    }
  };

  const handleAddItineraryItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTrip || !itineraryForm.title) return;

    try {
      await travelApi.addItineraryItem(selectedTrip.id, {
        dayNumber: Number(itineraryForm.dayNumber),
        title: itineraryForm.title,
        description: itineraryForm.description,
        location: itineraryForm.location,
        startTime: itineraryForm.startTime ? `${itineraryForm.startTime}:00` : undefined,
        endTime: itineraryForm.endTime ? `${itineraryForm.endTime}:00` : undefined,
        estimatedCost: itineraryForm.estimatedCost ? parseFloat(itineraryForm.estimatedCost) : 0,
        notes: itineraryForm.notes,
      });

      setIsAddItineraryOpen(false);
      setItineraryForm({
        dayNumber: 1,
        title: '',
        description: '',
        location: '',
        startTime: '',
        endTime: '',
        estimatedCost: '',
        notes: '',
      });

      const updated = await travelApi.getTripById(selectedTrip.id);
      setSelectedTrip(updated);
      loadTrips();
    } catch (err) {
      console.error('Failed to add itinerary item:', err);
    }
  };

  const handleDeleteItineraryItem = async (itemId: string) => {
    if (!selectedTrip) return;
    try {
      await travelApi.deleteItineraryItem(selectedTrip.id, itemId);
      const updated = await travelApi.getTripById(selectedTrip.id);
      setSelectedTrip(updated);
      loadTrips();
    } catch (err) {
      console.error('Failed to delete itinerary item:', err);
    }
  };

  const handleDeleteTrip = async (id: string) => {
    try {
      await travelApi.deleteTrip(id);
      setSelectedTrip(null);
      loadTrips();
    } catch (err) {
      console.error('Failed to delete trip:', err);
    }
  };

  // Group itinerary items by day
  const groupedItinerary: Record<number, ItineraryItem[]> = {};
  if (selectedTrip?.itineraryItems) {
    selectedTrip.itineraryItems.forEach((item) => {
      if (!groupedItinerary[item.dayNumber]) {
        groupedItinerary[item.dayNumber] = [];
      }
      groupedItinerary[item.dayNumber].push(item);
    });
  }

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-foreground tracking-tight">Travel & Itinerary Planner</h1>
          <p className="text-sm text-muted">Plan vacations, build daily itineraries, and generate AI schedules.</p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsAiPlanOpen(true)}
            leftIcon={<Sparkles className="w-4 h-4 text-pink-300" />}
          >
            AI Trip Planner
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setIsCreateTripOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            New Trip
          </Button>
        </div>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard
          title="Total Trips"
          value={summary?.totalTrips ?? 0}
          subtitle="Lifetime adventures"
          icon={<Plane className="w-6 h-6" />}
          glowColor="cyan"
        />
        <StatCard
          title="Planned"
          value={summary?.plannedTrips ?? 0}
          subtitle="Upcoming journeys"
          icon={<Calendar className="w-6 h-6" />}
          glowColor="indigo"
        />
        <StatCard
          title="Active"
          value={summary?.activeTrips ?? 0}
          subtitle="Currently traveling"
          icon={<Compass className="w-6 h-6" />}
          glowColor="emerald"
        />
        <StatCard
          title="Completed"
          value={summary?.completedTrips ?? 0}
          subtitle="Memories logged"
          icon={<CheckCircle className="w-6 h-6" />}
          glowColor="rose"
        />
      </div>

      {/* Main Two-Column Layout: Trips List & Active Itinerary Explorer */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Trips Directory */}
        <div className="glass-card rounded-2xl p-6 border border-border space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-border">
            <h3 className="text-base font-semibold text-foreground">Your Journeys</h3>
            <span className="text-xs text-muted">{trips.length} Total</span>
          </div>

          <div className="space-y-3 max-h-[600px] overflow-y-auto pr-1">
            {trips.length === 0 ? (
              <EmptyState
                icon={<Plane className="w-8 h-8" />}
                title="No trips planned"
                description="Use AI Trip Planner to create a customized itinerary in seconds!"
                actionLabel="Generate with AI"
                onAction={() => setIsAiPlanOpen(true)}
              />
            ) : (
              trips.map((trip) => {
                const isSelected = selectedTrip?.id === trip.id;
                return (
                  <div
                    key={trip.id}
                    onClick={() => setSelectedTrip(trip)}
                    className={cn(
                      'p-4 rounded-xl border transition-all cursor-pointer text-left space-y-2',
                      isSelected
                        ? 'bg-indigo-950/40 border-brand-500/50 shadow-lg shadow-indigo-500/10'
                        : 'bg-surface/40 border-border hover:border-hover'
                    )}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <h4 className="font-semibold text-sm text-foreground truncate">{trip.name}</h4>
                      <Badge status={trip.status} size="sm" />
                    </div>

                    <div className="flex items-center gap-2 text-xs text-muted">
                      <MapPin className="w-3.5 h-3.5 text-teal-400 shrink-0" />
                      <span className="truncate">{trip.destination}</span>
                    </div>

                    <div className="flex items-center justify-between text-xs text-muted pt-1 border-t border-border/60">
                      <span>{formatDate(trip.startDate, 'MMM dd')} - {formatDate(trip.endDate, 'MMM dd')}</span>
                      <span className="font-semibold text-foreground">{formatCurrency(trip.budget, trip.currency)}</span>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Right: Selected Trip Itinerary Timeline */}
        <div className="lg:col-span-2 glass-card rounded-2xl p-6 border border-border space-y-6">
          {selectedTrip ? (
            <>
              {/* Trip Header & Budget Overview */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
                <div>
                  <div className="flex items-center gap-2.5">
                    <h2 className="text-xl font-bold text-foreground tracking-tight">{selectedTrip.name}</h2>
                    <Badge status={selectedTrip.status} />
                  </div>
                  <p className="text-xs text-muted mt-1 flex items-center gap-2">
                    <MapPin className="w-3.5 h-3.5 text-teal-400" /> {selectedTrip.destination} • {selectedTrip.totalDays} Days ({formatDate(selectedTrip.startDate)} to {formatDate(selectedTrip.endDate)})
                  </p>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    variant="primary"
                    onClick={() => {
                      setItineraryForm({ ...itineraryForm, dayNumber: 1 });
                      setIsAddItineraryOpen(true);
                    }}
                    leftIcon={<Plus className="w-4 h-4" />}
                  >
                    Add Schedule
                  </Button>
                  <button
                    onClick={() => handleDeleteTrip(selectedTrip.id)}
                    className="p-2 text-muted hover:text-rose-400 rounded-xl hover:bg-rose-500/10 transition-colors border border-border"
                    title="Delete Trip"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {/* Trip Financial Snapshot */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-4 rounded-xl bg-surface/60 border border-border">
                <div>
                  <p className="text-[11px] text-muted">Total Budget</p>
                  <p className="text-base font-bold text-foreground">{formatCurrency(selectedTrip.budget, selectedTrip.currency)}</p>
                </div>
                <div>
                  <p className="text-[11px] text-muted">Estimated Cost</p>
                  <p className="text-base font-bold text-brand-600">{formatCurrency(selectedTrip.totalEstimatedCost, selectedTrip.currency)}</p>
                </div>
                <div>
                  <p className="text-[11px] text-muted">Remaining Budget</p>
                  <p className={cn('text-base font-bold', selectedTrip.remainingBudget < 0 ? 'text-rose-400' : 'text-emerald-400')}>
                    {formatCurrency(selectedTrip.remainingBudget, selectedTrip.currency)}
                  </p>
                </div>
              </div>

              {/* Itinerary Timeline */}
              <div className="space-y-6">
                <h3 className="text-sm font-semibold uppercase tracking-wider text-muted">
                  Day-by-Day Timeline
                </h3>

                {Object.keys(groupedItinerary).length === 0 ? (
                  <div className="p-8 text-center border border-dashed border-border rounded-2xl text-muted text-xs">
                    No itinerary items added for this trip yet. Click "+ Add Schedule" or plan automatically with AI.
                  </div>
                ) : (
                  Array.from({ length: selectedTrip.totalDays || 1 }).map((_, idx) => {
                    const dayNum = idx + 1;
                    const items = groupedItinerary[dayNum] || [];
                    return (
                      <div key={dayNum} className="space-y-3">
                        <div className="flex items-center gap-2">
                          <span className="px-2.5 py-1 rounded-lg bg-brand-600/20 text-indigo-300 font-bold text-xs border border-brand-500/30">
                            Day {dayNum}
                          </span>
                          <div className="flex-1 h-px bg-surface-hover" />
                        </div>

                        <div className="space-y-2.5 pl-3 border-l-2 border-border">
                          {items.length === 0 ? (
                            <p className="text-xs text-muted py-1">No activities scheduled for this day.</p>
                          ) : (
                            items.map((item) => (
                              <div
                                key={item.id}
                                className="p-3.5 rounded-xl bg-surface/40 border border-border/80 hover:border-hover transition-colors flex items-start justify-between gap-3 group"
                              >
                                <div className="space-y-1 min-w-0">
                                  <div className="flex items-center gap-2">
                                    <h5 className="font-semibold text-sm text-foreground">{item.title}</h5>
                                    {item.estimatedCost > 0 && (
                                      <span className="text-[11px] px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium">
                                        ~{formatCurrency(item.estimatedCost, selectedTrip.currency)}
                                      </span>
                                    )}
                                  </div>

                                  {item.description && (
                                    <p className="text-xs text-foreground leading-relaxed">{item.description}</p>
                                  )}

                                  <div className="flex flex-wrap items-center gap-3 text-[11px] text-muted pt-1">
                                    {item.location && (
                                      <span className="flex items-center gap-1">
                                        <MapPin className="w-3 h-3 text-teal-400" /> {item.location}
                                      </span>
                                    )}
                                    {item.startTime && (
                                      <span className="flex items-center gap-1">
                                        <Clock className="w-3 h-3 text-brand-600" /> {item.startTime.slice(0, 5)} {item.endTime ? `- ${item.endTime.slice(0, 5)}` : ''}
                                      </span>
                                    )}
                                    {item.notes && (
                                      <span className="text-muted italic">Note: {item.notes}</span>
                                    )}
                                  </div>
                                </div>

                                <button
                                  onClick={() => handleDeleteItineraryItem(item.id)}
                                  className="opacity-0 group-hover:opacity-100 p-1 text-muted hover:text-rose-400 transition-opacity"
                                  title="Delete item"
                                >
                                  <Trash2 className="w-3.5 h-3.5" />
                                </button>
                              </div>
                            ))
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </>
          ) : (
            <div className="h-96 flex flex-col items-center justify-center text-center text-muted">
              <Plane className="w-12 h-12 text-teal-400/50 mb-3" />
              <h3 className="text-base font-semibold text-foreground">Select a Trip</h3>
              <p className="text-xs text-muted mt-1 max-w-sm">
                Choose a trip from the left sidebar or create a new vacation to view and customize its schedule.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Create Manual Trip Modal */}
      <Modal
        isOpen={isCreateTripOpen}
        onClose={() => setIsCreateTripOpen(false)}
        title="Plan New Trip"
        subtitle="Create a new trip itinerary card"
      >
        <form onSubmit={handleCreateTrip} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Destination City / Country
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Kyoto, Japan or Goa, India"
              value={tripForm.destination}
              onChange={(e) => setTripForm({ ...tripForm, destination: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Trip Name (Optional)
            </label>
            <input
              type="text"
              placeholder="e.g. Summer Kyoto Getaway"
              value={tripForm.name}
              onChange={(e) => setTripForm({ ...tripForm, name: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Start Date
              </label>
              <input
                type="date"
                required
                value={tripForm.startDate}
                onChange={(e) => setTripForm({ ...tripForm, startDate: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                End Date
              </label>
              <input
                type="date"
                required
                value={tripForm.endDate}
                onChange={(e) => setTripForm({ ...tripForm, endDate: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Budget ({currency})
            </label>
            <input
              type="number"
              step="0.01"
              placeholder="e.g. 25000"
              value={tripForm.budget}
              onChange={(e) => setTripForm({ ...tripForm, budget: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div className="pt-3 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => setIsCreateTripOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary">
              Create Trip
            </Button>
          </div>
        </form>
      </Modal>

      {/* AI Trip Planner Wizard Modal */}
      <Modal
        isOpen={isAiPlanOpen}
        onClose={() => setIsAiPlanOpen(false)}
        title="AI Vacation Planner"
        subtitle="Let AI craft an end-to-end customized travel itinerary with budget breakdown"
        maxWidth="lg"
      >
        <form onSubmit={handleAiPlanGenerate} className="space-y-4">
          <div className="p-4 rounded-xl bg-gradient-to-r from-blue-50 via-purple-50 to-pink-50 border border-brand-500/20 flex items-center gap-3 text-xs text-brand-700">
            <Sparkles className="w-5 h-5 text-brand-600 shrink-0" />
            <span>AI will automatically organize sightseeing schedules, meal suggestions, and budget estimations.</span>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Where would you like to go?
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Bangalore, Manali, Paris, Tokyo, Dubai"
              value={aiForm.destination}
              onChange={(e) => setAiForm({ ...aiForm, destination: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Duration (Days)
              </label>
              <input
                type="number"
                min="1"
                max="14"
                required
                value={aiForm.days}
                onChange={(e) => setAiForm({ ...aiForm, days: parseInt(e.target.value) || 1 })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Travelers
              </label>
              <input
                type="number"
                min="1"
                required
                value={aiForm.travelers}
                onChange={(e) => setAiForm({ ...aiForm, travelers: parseInt(e.target.value) || 1 })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Budget ({currency})
              </label>
              <input
                type="number"
                required
                value={aiForm.budget}
                onChange={(e) => setAiForm({ ...aiForm, budget: parseFloat(e.target.value) || 0 })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Interests & Travel Style
            </label>
            <input
              type="text"
              placeholder="e.g. Adventure, Food Tours, Nature, Historic Sites"
              value={aiForm.interests}
              onChange={(e) => setAiForm({ ...aiForm, interests: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div className="pt-3 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => setIsAiPlanOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              isLoading={isAiGenerating}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Generate Itinerary
            </Button>
          </div>
        </form>
      </Modal>

      {/* Add Itinerary Item Modal */}
      <Modal
        isOpen={isAddItineraryOpen}
        onClose={() => setIsAddItineraryOpen(false)}
        title="Add Activity to Itinerary"
        subtitle={`Scheduling for ${selectedTrip?.name}`}
      >
        <form onSubmit={handleAddItineraryItem} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Day Number
              </label>
              <input
                type="number"
                min="1"
                max={selectedTrip?.totalDays || 30}
                required
                value={itineraryForm.dayNumber}
                onChange={(e) => setItineraryForm({ ...itineraryForm, dayNumber: parseInt(e.target.value) || 1 })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Estimated Cost ({currency})
              </label>
              <input
                type="number"
                step="0.01"
                placeholder="e.g. 500"
                value={itineraryForm.estimatedCost}
                onChange={(e) => setItineraryForm({ ...itineraryForm, estimatedCost: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Activity Title
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Visit Fushimi Inari Shrine"
              value={itineraryForm.title}
              onChange={(e) => setItineraryForm({ ...itineraryForm, title: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Location
            </label>
            <input
              type="text"
              placeholder="e.g. Kyoto Station area"
              value={itineraryForm.location}
              onChange={(e) => setItineraryForm({ ...itineraryForm, location: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Start Time
              </label>
              <input
                type="time"
                value={itineraryForm.startTime}
                onChange={(e) => setItineraryForm({ ...itineraryForm, startTime: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                End Time
              </label>
              <input
                type="time"
                value={itineraryForm.endTime}
                onChange={(e) => setItineraryForm({ ...itineraryForm, endTime: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Description / Notes
            </label>
            <textarea
              rows={2}
              placeholder="Add tips, ticket notes, or directions..."
              value={itineraryForm.description}
              onChange={(e) => setItineraryForm({ ...itineraryForm, description: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground"
            />
          </div>

          <div className="pt-3 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => setIsAddItineraryOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary">
              Save Schedule
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
