import React, { useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Sparkles,
  Send,
  Plus,
  Trash2,
  Bot,
  User as UserIcon,
  CheckCircle2,
  Clock,
  Wallet,
  Plane,
  AlertCircle,
  MessageSquare,
  ChevronRight,
} from 'lucide-react';
import { aiApi } from '../../lib/api';
import { Conversation, ChatMessage, ToolResult } from '../../types';
import { formatTimeAgo, cn } from '../../lib/utils';
import { Button } from '../../components/common/Button';

export const AssistantPage: React.FC = () => {
  const location = useLocation();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [toolResults, setToolResults] = useState<Record<string, ToolResult[]>>({});

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  // Load conversations on mount
  const loadConversations = async () => {
    try {
      const list = await aiApi.getConversations();
      setConversations(list);
      if (list.length > 0 && !activeConversationId) {
        selectConversation(list[0].id);
      }
    } catch (err) {
      console.error('Failed to load conversations:', err);
    }
  };

  useEffect(() => {
    loadConversations();
  }, []);

  // Handle initialPrompt from other pages/topbar
  useEffect(() => {
    if (location.state?.initialPrompt) {
      const prompt = location.state.initialPrompt;
      window.history.replaceState({}, document.title);
      handleSendMessage(prompt);
    }
  }, [location.state]);

  const selectConversation = async (id: string) => {
    setActiveConversationId(id);
    try {
      const conv = await aiApi.getConversation(id);
      setMessages(conv.messages || []);
    } catch (err) {
      console.error('Failed to load conversation messages:', err);
    }
  };

  const handleNewChat = () => {
    setActiveConversationId(null);
    setMessages([]);
    setToolResults({});
  };

  const handleDeleteConversation = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await aiApi.deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (activeConversationId === id) {
        handleNewChat();
      }
    } catch (err) {
      console.error('Failed to delete conversation:', err);
    }
  };

  const handleSendMessage = async (promptToSend?: string) => {
    const prompt = (promptToSend || inputPrompt).trim();
    if (!prompt || isLoading) return;

    // Optimistic user message
    const tempUserMsg: ChatMessage = {
      id: `temp-${Date.now()}`,
      role: 'USER',
      content: prompt,
      createdAt: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, tempUserMsg]);
    setInputPrompt('');
    setIsLoading(true);

    try {
      const response = await aiApi.chat(prompt, activeConversationId || undefined);

      if (!activeConversationId) {
        setActiveConversationId(response.conversationId);
      }

      setMessages((prev) => [...prev, response.message]);

      if (response.toolResults && response.toolResults.length > 0) {
        setToolResults((prev) => ({
          ...prev,
          [response.message.id]: response.toolResults,
        }));
      }

      // Refresh conversations list to update title
      const updatedList = await aiApi.getConversations();
      setConversations(updatedList);
    } catch (err) {
      console.error('AI chat failed:', err);
      const errorMsg: ChatMessage = {
        id: `err-${Date.now()}`,
        role: 'ASSISTANT',
        content: 'I encountered an issue processing your request. Please try again or verify your settings.',
        createdAt: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const suggestedPrompts = [
    'Add ₹500 for dinner via UPI',
    'How much did I spend this month?',
    'Create high priority task: Finish project pitch by tomorrow',
    'Plan a 3-day Bangalore trip under ₹15,000',
    'Show my overdue tasks',
  ];

  return (
    <div className="h-[calc(100vh-8.5rem)] flex flex-col md:flex-row gap-6 animate-fade-in">
      {/* Conversations Sidebar */}
      <div className="hidden lg:flex flex-col w-72 glass-card rounded-2xl p-4 border border-border shrink-0 justify-between">
        <div className="space-y-3">
          <Button
            onClick={handleNewChat}
            variant="primary"
            size="sm"
            className="w-full justify-center"
            leftIcon={<Plus className="w-4 h-4" />}
          >
            New AI Session
          </Button>

          <div className="pt-2">
            <p className="text-[11px] font-semibold uppercase tracking-wider text-muted mb-2 px-2">
              Recent Conversations
            </p>
            <div className="space-y-1 max-h-[480px] overflow-y-auto pr-1">
              {conversations.length === 0 ? (
                <p className="text-xs text-muted px-2 py-4 text-center">No conversation history</p>
              ) : (
                conversations.map((conv) => {
                  const isActive = activeConversationId === conv.id;
                  return (
                    <div
                      key={conv.id}
                      onClick={() => selectConversation(conv.id)}
                      className={cn(
                        'flex items-center justify-between p-2.5 rounded-xl text-xs font-medium cursor-pointer transition-all group',
                        isActive
                          ? 'bg-brand-600/20 text-foreground border border-brand-500/40'
                          : 'text-muted hover:text-foreground hover:bg-surface/60'
                      )}
                    >
                      <div className="flex items-center gap-2 truncate">
                        <MessageSquare className="w-3.5 h-3.5 shrink-0 text-brand-600" />
                        <span className="truncate">{conv.title || 'Personal AI Chat'}</span>
                      </div>
                      <button
                        onClick={(e) => handleDeleteConversation(conv.id, e)}
                        className="opacity-0 group-hover:opacity-100 p-1 text-muted hover:text-rose-400 transition-opacity"
                        title="Delete"
                      >
                        <Trash2 className="w-3 h-3" />
                      </button>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>

        <div className="p-3 rounded-xl bg-surface/60 border border-border text-[11px] text-muted flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-brand-600 shrink-0" />
          <span>Multi-tool engine equipped for finance, tasks, and travel.</span>
        </div>
      </div>

      {/* Main Chat Interface */}
      <div className="flex-1 glass-card rounded-2xl border border-border flex flex-col overflow-hidden">
        {/* Chat Top Header */}
        <div className="p-4 border-b border-border flex items-center justify-between bg-surface/40">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-pink-500 p-0.5 flex items-center justify-center">
              <div className="w-full h-full bg-background rounded-[10px] flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-brand-600" />
              </div>
            </div>
            <div>
              <h2 className="text-sm font-bold text-foreground flex items-center gap-2">
                Personal AI Assistant
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              </h2>
              <p className="text-[11px] text-muted">Deterministic Multi-Tool Engine & LLM Pipeline</p>
            </div>
          </div>

          <Button variant="ghost" size="sm" onClick={handleNewChat} className="lg:hidden">
            <Plus className="w-4 h-4" />
          </Button>
        </div>

        {/* Messages Scroll Area */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4">
          {messages.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-center p-6 space-y-4 max-w-lg mx-auto my-auto">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-indigo-500/20 via-purple-500/20 to-pink-500/20 p-3 text-brand-600 border border-brand-500/30 flex items-center justify-center">
                <Bot className="w-8 h-8 text-brand-600" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-foreground">How can I assist your day?</h3>
                <p className="text-xs text-muted mt-1 leading-relaxed">
                  I can track expenses, add income, schedule tasks, check your monthly budgets, or plan full travel itineraries with automatic tool invocation.
                </p>
              </div>

              <div className="w-full space-y-2 pt-2">
                <p className="text-[11px] font-semibold text-muted uppercase tracking-wider">
                  Suggested Prompts
                </p>
                <div className="grid grid-cols-1 gap-2">
                  {suggestedPrompts.slice(0, 3).map((sp) => (
                    <button
                      key={sp}
                      onClick={() => handleSendMessage(sp)}
                      className="p-3 rounded-xl bg-surface/80 hover:bg-indigo-950/40 border border-border hover:border-brand-500/40 text-xs text-foreground text-left transition-all flex items-center justify-between group"
                    >
                      <span>💬 {sp}</span>
                      <ChevronRight className="w-3.5 h-3.5 text-muted group-hover:text-brand-600" />
                    </button>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            messages.map((msg) => {
              const isUser = msg.role === 'USER';
              const results = toolResults[msg.id];

              return (
                <div
                  key={msg.id}
                  className={cn(
                    'flex gap-3 max-w-2xl',
                    isUser ? 'ml-auto flex-row-reverse' : 'mr-auto'
                  )}
                >
                  {/* Avatar */}
                  <div
                    className={cn(
                      'w-8 h-8 rounded-xl shrink-0 flex items-center justify-center text-xs font-bold',
                      isUser
                        ? 'bg-brand-600 text-foreground'
                        : 'bg-gradient-to-tr from-indigo-500 to-pink-500 text-foreground'
                    )}
                  >
                    {isUser ? <UserIcon className="w-4 h-4" /> : <Sparkles className="w-4 h-4" />}
                  </div>

                  {/* Bubble Content */}
                  <div className="space-y-2 min-w-0">
                    <div
                      className={cn(
                        'p-4 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap',
                        isUser
                          ? 'bg-brand-600 text-foreground rounded-tr-none shadow-lg shadow-indigo-600/15'
                          : 'bg-surface/90 text-foreground rounded-tl-none border border-border shadow-sm'
                      )}
                    >
                      {msg.content}
                    </div>

                    {/* Tool Execution Badges / Structured Result Cards */}
                    {results && results.length > 0 && (
                      <div className="space-y-2">
                        {results.map((tool, idx) => (
                          <div
                            key={idx}
                            className="p-3 rounded-xl bg-background/80 border border-brand-500/30 text-xs text-foreground space-y-1.5"
                          >
                            <div className="flex items-center justify-between">
                              <span className="font-semibold text-indigo-300 flex items-center gap-1.5">
                                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                                Action Executed: {tool.toolName}
                              </span>
                              <span className="text-[10px] uppercase font-bold text-muted px-1.5 py-0.5 rounded bg-surface-hover">
                                {tool.module}
                              </span>
                            </div>
                            <p className="text-foreground text-[11px]">{tool.message}</p>
                          </div>
                        ))}
                      </div>
                    )}

                    <div className={cn('text-[10px] text-muted px-1', isUser ? 'text-right' : 'text-left')}>
                      {formatTimeAgo(msg.createdAt)}
                    </div>
                  </div>
                </div>
              );
            })
          )}

          {/* Typing Indicator */}
          {isLoading && (
            <div className="flex gap-3 max-w-2xl mr-auto">
              <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-500 to-pink-500 text-foreground shrink-0 flex items-center justify-center">
                <Sparkles className="w-4 h-4" />
              </div>
              <div className="p-4 rounded-2xl bg-surface/90 rounded-tl-none border border-border flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-indigo-400 animate-bounce" />
                <span className="w-2 h-2 rounded-full bg-indigo-400 animate-bounce [animation-delay:0.2s]" />
                <span className="w-2 h-2 rounded-full bg-indigo-400 animate-bounce [animation-delay:0.4s]" />
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Suggested Prompts Pills (Quick Taps) */}
        {messages.length > 0 && (
          <div className="px-4 py-2 border-t border-border/60 bg-surface/30 flex items-center gap-2 overflow-x-auto text-[11px] no-scrollbar">
            <span className="text-muted shrink-0 font-medium">Quick ask:</span>
            {suggestedPrompts.slice(0, 4).map((sp) => (
              <button
                key={sp}
                onClick={() => handleSendMessage(sp)}
                className="px-2.5 py-1 rounded-lg bg-surface-hover hover:bg-indigo-950 text-foreground hover:text-indigo-300 border border-hover/60 whitespace-nowrap shrink-0 transition-colors"
              >
                {sp}
              </button>
            ))}
          </div>
        )}

        {/* Input Bar */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSendMessage();
          }}
          className="p-4 border-t border-border bg-surface/60 flex items-center gap-3"
        >
          <div className="relative flex-1">
            <input
              type="text"
              value={inputPrompt}
              onChange={(e) => setInputPrompt(e.target.value)}
              placeholder="Ask anything or invoke tools (e.g. 'Spent ₹800 on groceries', 'Plan a 2-day trip to Jaipur')..."
              className="w-full glass-input rounded-xl pl-4 pr-10 py-3 text-sm text-foreground placeholder:text-muted focus:border-brand-500"
            />
          </div>
          <Button
            type="submit"
            variant="primary"
            size="md"
            disabled={!inputPrompt.trim() || isLoading}
            className="shrink-0 px-4 py-3"
          >
            <Send className="w-4 h-4" />
          </Button>
        </form>
      </div>
    </div>
  );
};
