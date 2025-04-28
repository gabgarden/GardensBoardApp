package br.edu.iff.ccc.bsi.gardensBoard.service;

import br.edu.iff.ccc.bsi.gardensBoard.exception.UserAlreadyExistsException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserIdNullException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.InvalidUserDataException;
import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.repository.UserRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Checks if a user exists by ID
     * @param idUser The user ID to check
     * @return True if the user exists, false otherwise
     */
    public boolean userExists(UUID idUser) {
        if (idUser == null) {
            throw new UserIdNullException("User ID cannot be null");
        }
        return userRepository.findById(idUser).isPresent();
    }

    /**
     * Finds a user by ID
     * @param idUser The user ID to find
     * @return The user model
     * @throws UserIdNullException if the ID is null
     * @throws UserNotFoundException if the user is not found
     */
    public UserModel findById(UUID idUser) {
        if (idUser == null) {
            throw new UserIdNullException("User ID cannot be null");
        }
        
        return userRepository.findById(idUser)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + idUser));
    }

    /**
     * Creates a new user
     * @param userModel The user data to create
     * @return The created user
     * @throws InvalidUserDataException if the user data is invalid
     * @throws UserAlreadyExistsException if a user with the same username already exists
     */
    public UserModel createUser(UserModel userModel) {
        validateUserData(userModel);
        
        // Check if username already exists - modificado para lidar com UserModel diretamente
        UserModel existingUser = userRepository.findByUsername(userModel.getUsername());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User with username " + userModel.getUsername() + " already exists");
        }
        
        // Hash the password
        String hashedPassword = BCrypt.withDefaults()
                                  .hashToString(12, userModel.getPassword().toCharArray());
        userModel.setPassword(hashedPassword);
        
        return userRepository.save(userModel);
    }

    /**
     * Updates a user partially
     * @param idUser The user ID to update
     * @param partialUser The partial user data to update
     * @return The updated user
     * @throws UserIdNullException if the ID is null
     * @throws UserNotFoundException if the user is not found
     * @throws InvalidUserDataException if the update data is invalid
     */
    public UserModel patch(UUID idUser, UserModel partialUser) {
        if (idUser == null) {
            throw new UserIdNullException("User ID cannot be null");
        }
        
        UserModel existingUser = userRepository.findById(idUser)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + idUser));
        
        // Validate and update username if provided - modificado para lidar com UserModel diretamente
        if (partialUser.getUsername() != null) {
            // Check if new username is already taken by another user
            UserModel userWithSameUsername = userRepository.findByUsername(partialUser.getUsername());
            if (userWithSameUsername != null && !userWithSameUsername.getId().equals(idUser)) {
                throw new UserAlreadyExistsException("Username " + partialUser.getUsername() + " is already taken");
            }
            existingUser.setUsername(partialUser.getUsername());
        }
        
        // Update name if provided
        if (partialUser.getName() != null) {
            existingUser.setName(partialUser.getName());
        }
        
        // Update password if provided
        if (partialUser.getPassword() != null && !partialUser.getPassword().isBlank()) {
            String hashedPassword = BCrypt.withDefaults()
                                      .hashToString(12, partialUser.getPassword().toCharArray());
            existingUser.setPassword(hashedPassword);
        }
        
        return userRepository.save(existingUser);
    }
    
    /**
     * Deletes a user
     * @param idUser The user ID to delete
     * @throws UserIdNullException if the ID is null
     * @throws UserNotFoundException if the user is not found
     */
    public void delete(UUID idUser) {
        if (idUser == null) {
            throw new UserIdNullException("User ID cannot be null");
        }
        
        if (!userRepository.existsById(idUser)) {
            throw new UserNotFoundException("User not found with ID: " + idUser);
        }
        
        userRepository.deleteById(idUser);
    }
    
    /**
     * Validates user data
     * @param userModel The user data to validate
     * @throws InvalidUserDataException if the user data is invalid
     */
    private void validateUserData(UserModel userModel) {
        if (userModel == null) {
            throw new InvalidUserDataException("User data cannot be null");
        }
        
        if (userModel.getUsername() == null || userModel.getUsername().isBlank()) {
            throw new InvalidUserDataException("Username cannot be null or blank");
        }
        
        if (userModel.getPassword() == null || userModel.getPassword().isBlank()) {
            throw new InvalidUserDataException("Password cannot be null or blank");
        }
        
        if (userModel.getName() == null || userModel.getName().isBlank()) {
            throw new InvalidUserDataException("Name cannot be null or blank");
        }
        
        // Additional validation could be added here (password strength, email format, etc.)
    }
}