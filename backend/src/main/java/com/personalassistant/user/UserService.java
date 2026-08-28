package com.personalassistant.user;

import com.personalassistant.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public UserDto getUserProfile(String userId) {
        User user = getUserById(userId);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto updateProfile(String userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        user.setName(request.getName());
        user.setTimezone(request.getTimezone());
        User updated = userRepository.save(user);
        return UserDto.fromEntity(updated);
    }

    @Transactional
    public UserDto updateCurrency(String userId, String currency) {
        User user = getUserById(userId);
        user.setCurrency(currency.toUpperCase());
        User updated = userRepository.save(user);
        return UserDto.fromEntity(updated);
    }
}
