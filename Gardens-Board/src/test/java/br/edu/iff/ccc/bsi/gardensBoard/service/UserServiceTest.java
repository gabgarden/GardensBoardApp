package br.edu.iff.ccc.bsi.gardensBoard.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.edu.iff.ccc.bsi.gardensBoard.exception.InvalidUserDataException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserAlreadyExistsException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserIdNullException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;
    
    @Captor
    private ArgumentCaptor<UserModel> userCaptor;

    private UUID userId;
    private UserModel userModel;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        userModel = new UserModel();
        userModel.setId(userId);
        userModel.setName("Test User");
        userModel.setUsername("testuser");
        userModel.setPassword("password123");
    }

    @Test
    @DisplayName("Deve verificar se o usuário existe")
    void shouldCheckIfUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));

        // Act
        boolean result = userService.userExists(userId);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve verificar se o usuário não existe")
    void shouldCheckIfUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.userExists(userId);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar UserIdNullException quando ID é null ao verificar existência")
    void shouldThrowUserIdNullExceptionWhenIdIsNullOnExists() {
        // Act & Assert
        UserIdNullException exception = assertThrows(UserIdNullException.class, () -> {
            userService.userExists(null);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve encontrar um usuário por ID")
    void shouldFindUserById() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));

        // Act
        UserModel result = userService.findById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("testuser", result.getUsername());
        assertEquals("password123", result.getPassword());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando usuário não é encontrado por ID")
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundById() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.findById(userId);
        });

        assertTrue(exception.getMessage().contains("User not found with ID: " + userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar UserIdNullException quando ID é null ao buscar usuário")
    void shouldThrowUserIdNullExceptionWhenIdIsNullOnFindById() {
        // Act & Assert
        UserIdNullException exception = assertThrows(UserIdNullException.class, () -> {
            userService.findById(null);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserModel result = userService.createUser(userModel);

        // Assert
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("testuser", result.getUsername());
        // Verifique se a senha foi hash
        assertTrue(BCrypt.verifyer().verify("password123".toCharArray(), result.getPassword()).verified);
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve lançar UserAlreadyExistsException quando o username já existir")
    void shouldThrowUserAlreadyExistsExceptionWhenUsernameExists() {
        // Arrange
        UserModel existingUser = new UserModel();
        existingUser.setId(UUID.randomUUID());
        existingUser.setUsername("testuser");
        
        when(userRepository.findByUsername("testuser")).thenReturn(existingUser);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(userModel);
        });

        assertEquals("User with username testuser already exists", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidUserDataException quando os dados do usuário são inválidos")
    void shouldThrowInvalidUserDataExceptionWhenUserDataIsInvalid() {
        // Arrange
        userModel.setUsername("");

        // Act & Assert
        InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
            userService.createUser(userModel);
        });

        assertEquals("Username cannot be null or blank", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidUserDataException quando o usuário é null")
    void shouldThrowInvalidUserDataExceptionWhenUserIsNull() {
        // Act & Assert
        InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
            userService.createUser(null);
        });

        assertEquals("User data cannot be null", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve atualizar parcialmente um usuário com sucesso")
    void shouldPatchUserSuccessfully() {
        // Arrange
        UserModel existingUser = new UserModel();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setUsername("oldusername");
        existingUser.setPassword(BCrypt.withDefaults().hashToString(12, "oldpassword".toCharArray()));
        
        UserModel partialUser = new UserModel();
        partialUser.setName("New Name");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserModel result = userService.patch(userId, partialUser);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("oldusername", result.getUsername()); // não alterado
        assertEquals(existingUser.getPassword(), result.getPassword()); // não alterado
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve atualizar username e verificar duplicidade")
    void shouldUpdateUsernameAndCheckDuplication() {
        // Arrange
        UserModel existingUser = new UserModel();
        existingUser.setId(userId);
        existingUser.setName("Test User");
        existingUser.setUsername("oldusername");
        
        UserModel partialUser = new UserModel();
        partialUser.setUsername("newusername");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newusername")).thenReturn(null);
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserModel result = userService.patch(userId, partialUser);

        // Assert
        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByUsername("newusername");
        verify(userRepository, times(1)).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve atualizar senha e fazer hash")
    void shouldUpdatePasswordAndHashIt() {
        // Arrange
        UserModel existingUser = new UserModel();
        existingUser.setId(userId);
        existingUser.setUsername("testuser");
        existingUser.setPassword("oldhash");
        
        UserModel partialUser = new UserModel();
        partialUser.setPassword("newpassword");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserModel result = userService.patch(userId, partialUser);

        // Assert
        assertNotNull(result);
        UserModel savedUser = userCaptor.getValue();
        assertTrue(BCrypt.verifyer().verify("newpassword".toCharArray(), savedUser.getPassword()).verified);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve lançar UserAlreadyExistsException quando username já está em uso")
    void shouldThrowUserAlreadyExistsExceptionWhenUsernameAlreadyInUse() {
        // Arrange
        UserModel existingUser = new UserModel();
        existingUser.setId(userId);
        existingUser.setUsername("oldusername");
        
        UserModel anotherUser = new UserModel();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("takenusername");
        
        UserModel partialUser = new UserModel();
        partialUser.setUsername("takenusername");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("takenusername")).thenReturn(anotherUser);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.patch(userId, partialUser);
        });

        assertEquals("Username takenusername is already taken", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByUsername("takenusername");
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando o usuário não existe ao atualizar")
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundOnPatch() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.patch(userId, new UserModel());
        });

        assertTrue(exception.getMessage().contains("User not found with ID: " + userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve lançar UserIdNullException quando ID é null ao atualizar")
    void shouldThrowUserIdNullExceptionWhenIdIsNullOnPatch() {
        // Act & Assert
        UserIdNullException exception = assertThrows(UserIdNullException.class, () -> {
            userService.patch(null, new UserModel());
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    @DisplayName("Deve excluir um usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.delete(userId);

        // Assert
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando o usuário não existe ao excluir")
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundOnDelete() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.delete(userId);
        });

        assertTrue(exception.getMessage().contains("User not found with ID: " + userId));
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar UserIdNullException quando ID é null ao excluir")
    void shouldThrowUserIdNullExceptionWhenIdIsNullOnDelete() {
        // Act & Assert
        UserIdNullException exception = assertThrows(UserIdNullException.class, () -> {
            userService.delete(null);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userRepository, never()).existsById(any());
        verify(userRepository, never()).deleteById(any());
    }
}