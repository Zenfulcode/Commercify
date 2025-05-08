package com.zenfulcode.commercify.api.users;

import com.zenfulcode.commercify.api.product.dto.response.PageInfo;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import com.zenfulcode.commercify.user.application.dto.request.PagedUserResponse;
import com.zenfulcode.commercify.user.application.dto.response.UserProfileResponse;
import com.zenfulcode.commercify.user.application.service.UserApplicationService;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userApplicationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<User> usersPage = userApplicationService.getAllUsers(pageRequest);
        PagedUserResponse response = toPagedResponse(usersPage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String userId) {
        // Fetch user by ID from the database
        User user = userApplicationService.getUser(UserId.of(userId));

        // Map to response DTO
        UserProfileResponse response = UserProfileResponse.fromUser(user);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private PagedUserResponse toPagedResponse(Page<User> userPage) {
        List<UserProfileResponse> items = userPage.getContent()
                .stream()
                .map(UserProfileResponse::fromUser)
                .toList();

        PageInfo pageInfo = new PageInfo(
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );

        return new PagedUserResponse(items, pageInfo);
    }
}
