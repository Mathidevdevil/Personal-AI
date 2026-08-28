package com.personalassistant.finance;

import com.personalassistant.common.PagedResponse;
import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserService userService;

    @Transactional
    public IncomeResponse createIncome(String userId, IncomeRequest request) {
        User user = userService.getUserById(userId);

        Income income = Income.builder()
                .user(user)
                .amount(request.getAmount())
                .source(request.getSource().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .incomeDate(request.getIncomeDate() != null ? request.getIncomeDate() : Instant.now())
                .build();

        Income saved = incomeRepository.save(income);
        return IncomeResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<IncomeResponse> getIncome(String userId, int page, int size) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Income> incomePage = incomeRepository.findByUserOrderByIncomeDateDesc(user, pageable);

        List<IncomeResponse> content = incomePage.getContent().stream()
                .map(IncomeResponse::fromEntity)
                .toList();

        return PagedResponse.<IncomeResponse>builder()
                .content(content)
                .page(incomePage.getNumber())
                .size(incomePage.getSize())
                .totalElements(incomePage.getTotalElements())
                .totalPages(incomePage.getTotalPages())
                .last(incomePage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(String userId, String incomeId) {
        Income income = incomeRepository.findById(incomeId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", incomeId));
        return IncomeResponse.fromEntity(income);
    }

    @Transactional
    public IncomeResponse updateIncome(String userId, String incomeId, IncomeRequest request) {
        Income income = incomeRepository.findById(incomeId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", incomeId));

        income.setAmount(request.getAmount());
        income.setSource(request.getSource().trim());
        income.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getIncomeDate() != null) {
            income.setIncomeDate(request.getIncomeDate());
        }

        Income updated = incomeRepository.save(income);
        return IncomeResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteIncome(String userId, String incomeId) {
        Income income = incomeRepository.findById(incomeId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", incomeId));
        incomeRepository.delete(income);
    }
}
