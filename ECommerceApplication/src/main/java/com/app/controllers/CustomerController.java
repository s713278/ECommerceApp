package com.app.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.config.AppConstants;
import com.app.payloads.CustomerDTO;
import com.app.payloads.response.APIResponse;
import com.app.payloads.response.UserResponse;
import com.app.services.SubscriptionService;
import com.app.services.impl.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;


@SecurityRequirement(name = AppConstants.SECURITY_CONTEXT_PARAM)
@RestController
@Tag(name = "2. User Management")
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class CustomerController {
        
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @PreAuthorize("#userId == authentication.principal and (hasAuthority('ADMIN'))")
    @GetMapping("/admin")
    public ResponseEntity<UserResponse> getUsers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        UserResponse userResponse = userService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<UserResponse>(userResponse, HttpStatus.FOUND);
    }

    @PreAuthorize("#userId == authentication.principal and (hasAuthority('ADMIN') or hasAuthority('USER'))")
    @Operation(summary = "User Information")
    @GetMapping("/{userId}")
    public ResponseEntity<APIResponse<?>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(APIResponse.success(userService.getUserInfo(userId)));
    }


    @PreAuthorize("#userId == authentication.principal and (hasAuthority('ADMIN') or hasAuthority('USER'))")
    @Operation(summary = "Update User Information")
    @PutMapping("/{userId}")
    public ResponseEntity<CustomerDTO> updateUser(@RequestBody CustomerDTO userDTO, @PathVariable Long userId) {
        CustomerDTO updatedUser = userService.updateUser(userId, userDTO);
        return new ResponseEntity<CustomerDTO>(updatedUser, HttpStatus.OK);
    }

    @PreAuthorize("#userId == authentication.principal and (hasAuthority('ADMIN') or hasAuthority('USER'))")
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        String status = userService.deleteUser(userId);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }

    @Operation(summary = "Update Delivery Address", description = "Updates the delivery address for a specific user. Only valid keys (address1, address2, city, state, zipCode, country) are accepted.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Address updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input keys or validation error", content = @Content(schema = @Schema(example = "{\"error\": \"Invalid keys found: [invalidKey]\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found") })

    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A map containing valid delivery address fields. Valid keys: address1, address2, city, state, zipCode, country.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class), examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
            {
                "address1": "123 Main St",
                "address2": "Apt 4B",
                "city": "Hyderabad",
                "state": "Telangana",
                "zipCode": "500001",
                "country": "India"
            }
            """)))
    @PatchMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal and (hasAuthority('ADMIN') or hasAuthority('USER'))")
    public ResponseEntity<APIResponse<?>> updateAddress(@PathVariable Long userId,
            @RequestBody Map<String, String> address) {
        userService.updateUserAddress(userId, address);
        return ResponseEntity.ok(APIResponse.success("Address updated succssfully."));
    }

    @Operation(summary = "All Subscriptions By Vendor")
    @GetMapping("/{userId}/vendor/{vendorId}")
    @PreAuthorize("#userId == authentication.principal and (hasAuthority('ADMIN') or hasAuthority('USER'))")
    public ResponseEntity<APIResponse<?>> fetchSubsByUserAndVendor( @PathVariable Long vendorId,
            @PathVariable Long userId) {
        var subscriptions = subscriptionService.fetchSubsByUserAndVendor(userId, vendorId);
        return ResponseEntity.ok(APIResponse.success(subscriptions));
    }

}
