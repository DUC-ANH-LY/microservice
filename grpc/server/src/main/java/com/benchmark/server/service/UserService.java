package com.benchmark.server.service;

import com.benchmark.server.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Shared service layer used by both REST and gRPC endpoints.
 * Uses in-memory storage for benchmark consistency.
 */
@Service
public class UserService {

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserService() {
        // Pre-populate with sample data for read benchmarks
        for (int i = 1; i <= 100; i++) {
            User user = User.builder()
                    .id((long) i)
                    .name("User " + i)
                    .email("user" + i + "@benchmark.com")
                    .age(20 + (i % 50))
                    .department("Dept-" + (i % 10))
                    .build();
            userStore.put(user.getId(), user);
        }
        idGenerator.set(101);
    }

    public User getUser(long id) {
        User user = userStore.get(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return user;
    }

    public User createUser(String name, String email, int age, String department) {
        long id = idGenerator.getAndIncrement();
        User user = User.builder()
                .id(id)
                .name(name)
                .email(email)
                .age(age)
                .department(department)
                .build();
        userStore.put(id, user);
        return user;
    }

    public List<User> listUsers(int page, int size) {
        return userStore.values().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    public int getTotalCount() {
        return userStore.size();
    }

    public List<User> bulkCreateUsers(List<UserInput> inputs) {
        List<User> created = new ArrayList<>();
        for (UserInput input : inputs) {
            User user = createUser(input.name(), input.email(), input.age(), input.department());
            created.add(user);
        }
        return created;
    }

    public record UserInput(String name, String email, int age, String department) {}

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
