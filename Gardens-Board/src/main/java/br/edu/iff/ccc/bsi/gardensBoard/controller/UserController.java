package br.edu.iff.ccc.bsi.gardensBoard.controller;

import br.edu.iff.ccc.bsi.gardensBoard.exception.UserAlreadyExistsException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserIdNullException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.InvalidUserDataException;
import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Operations related to user management")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/")
    @Operation(summary = "Create new user", description = "Registers a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "User already exists or invalid data") 
    })
    public ResponseEntity<?> create(
            @Parameter(description = "New user data")
            @Valid @RequestBody UserModel userModel) {
        try {
            UserModel userCreated = userService.createUser(userModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (InvalidUserDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates user data by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid user data or ID")
    })
    public ResponseEntity<?> patch(
            @Parameter(description = "User ID") 
            @PathVariable UUID id,
            @Parameter(description = "Input data for updating the user") 
            @Valid @RequestBody UserModel partialUser) {
        try {
            UserModel updatedUser = userService.patch(id, partialUser);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserIdNullException | UserAlreadyExistsException | InvalidUserDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "User ID") 
            @PathVariable UUID id) {
        try {
            userService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserIdNullException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user data by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "User ID cannot be null or blank")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "User ID") 
            @PathVariable UUID id) {
        try {
            UserModel user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserIdNullException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}