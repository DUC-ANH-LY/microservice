package com.benchmark.server.grpc;

import com.benchmark.proto.*;
import com.benchmark.server.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * gRPC service implementation - mirrors the REST controller for fair comparison.
 */
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    public UserGrpcService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        try {
            com.benchmark.server.model.User user = userService.getUser(request.getId());
            GetUserResponse response = GetUserResponse.newBuilder()
                    .setUser(toProtoUser(user))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserService.UserNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        com.benchmark.server.model.User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getAge(),
                request.getDepartment()
        );
        CreateUserResponse response = CreateUserResponse.newBuilder()
                .setUser(toProtoUser(user))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        List<com.benchmark.server.model.User> users = userService.listUsers(request.getPage(), request.getSize());
        ListUsersResponse response = ListUsersResponse.newBuilder()
                .addAllUsers(users.stream().map(this::toProtoUser).collect(Collectors.toList()))
                .setTotal(userService.getTotalCount())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void bulkCreateUsers(BulkCreateUsersRequest request, StreamObserver<BulkCreateUsersResponse> responseObserver) {
        List<UserService.UserInput> inputs = request.getUsersList().stream()
                .map(r -> new UserService.UserInput(r.getName(), r.getEmail(), r.getAge(), r.getDepartment()))
                .collect(Collectors.toList());

        List<com.benchmark.server.model.User> created = userService.bulkCreateUsers(inputs);
        BulkCreateUsersResponse response = BulkCreateUsersResponse.newBuilder()
                .addAllUsers(created.stream().map(this::toProtoUser).collect(Collectors.toList()))
                .setCreatedCount(created.size())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private User toProtoUser(com.benchmark.server.model.User user) {
        return User.newBuilder()
                .setId(user.getId())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .setAge(user.getAge())
                .setDepartment(user.getDepartment())
                .build();
    }
}
