package com.benchmark.server.rest;

import com.benchmark.server.model.*;
import com.benchmark.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API endpoints - mirrors the gRPC service for fair comparison.
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) {
        try {
            User user = userService.getUser(id);
            return ResponseEntity.ok(user);
        } catch (UserService.UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getAge(),
                request.getDepartment()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    public ResponseEntity<UserListResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<User> users = userService.listUsers(page, size);
        UserListResponse response = UserListResponse.builder()
                .users(users)
                .total(userService.getTotalCount())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkCreateResponse> bulkCreateUsers(@RequestBody List<CreateUserRequest> requests) {
        List<UserService.UserInput> inputs = requests.stream()
                .map(r -> new UserService.UserInput(r.getName(), r.getEmail(), r.getAge(), r.getDepartment()))
                .collect(Collectors.toList());

        List<User> created = userService.bulkCreateUsers(inputs);
        BulkCreateResponse response = BulkCreateResponse.builder()
                .users(created)
                .createdCount(created.size())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(UserService.UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(UserService.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}
