package com.zenfulcode.commercify.commercify.controller;


import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserManagementService userManagementService;
    private final PagedResourcesAssembler<UserDTO> pagedResourcesAssembler;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserDTO> users = userManagementService.getAllUsers(pageRequest);

        return ResponseEntity.ok(pagedResourcesAssembler.toModel(users));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userManagementService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/addresses")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<UserDTO> addAddress(@PathVariable Long id, @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(userManagementService.addAddress(id, addressDTO));
    }

    @PutMapping("/{userId}/addresses/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<UserDTO> updateAddress(@PathVariable Long userId, @PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(userManagementService.updateAddress(userId, addressId, addressDTO));
    }

    @DeleteMapping("/{userId}/addresses/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<UserDTO> removeAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        return ResponseEntity.ok(userManagementService.removeAddress(userId, addressId));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> addRoleToUser(@PathVariable Long id, @RequestBody String role) {
        return ResponseEntity.ok(userManagementService.addRoleToUser(id, role));
    }

    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> removeRoleFromUser(@PathVariable Long id, @RequestBody String role) {
        return ResponseEntity.ok(userManagementService.removeRoleFromUser(id, role));
    }
}
