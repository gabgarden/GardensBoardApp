package br.edu.iff.ccc.bsi.gardensBoard.service;

import br.edu.iff.ccc.bsi.gardensBoard.exception.InvalidTaskDateException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.TaskNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UnauthorizedOperationException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.model.TaskModel;
import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.repository.TaskRepository;
import br.edu.iff.ccc.bsi.gardensBoard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID userId;
    private UUID taskId;
    private UUID collaboratorId;
    private TaskModel taskModel;
    private UserModel userModel;
    private UserModel collaboratorModel;
    private LocalDateTime futureDateTime;
    private LocalDateTime pastDateTime;

    @BeforeEach
    void setUp() {
        // Inicializar UUIDs
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        collaboratorId = UUID.randomUUID();

        // Configurar datas
        futureDateTime = LocalDateTime.now().plusDays(1);
        pastDateTime = LocalDateTime.now().minusDays(1);

        // Inicializar modelos
        userModel = new UserModel();
        userModel.setId(userId);
        userModel.setName("Test User");

        collaboratorModel = new UserModel();
        collaboratorModel.setId(collaboratorId);
        collaboratorModel.setName("Collaborator");

        taskModel = new TaskModel();
        taskModel.setId(taskId);
        taskModel.setTitle("Test Task");
        taskModel.setDescription("Test Description");
        taskModel.setStartAt(futureDateTime);
        taskModel.setEndAt(futureDateTime.plusDays(1));
        taskModel.setIdUserOwner(userId);
        taskModel.setCollaborators(new ArrayList<>());
    }

    @Test
    @DisplayName("Deve criar uma tarefa com sucesso quando a data de início for no futuro")
    void shouldCreateTaskWhenStartDateIsInFuture() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));
        when(taskRepository.save(any(TaskModel.class))).thenReturn(taskModel);

        // Act
        TaskModel result = taskService.create(taskModel, userId);

        // Assert
        assertNotNull(result);
        assertEquals(taskModel.getTitle(), result.getTitle());
        assertEquals(taskModel.getDescription(), result.getDescription());
        verify(userRepository, times(1)).findById(userId);
        verify(taskRepository, times(1)).save(taskModel);
    }

    @Test
    @DisplayName("Deve lançar InvalidTaskDateException quando a data de início for no passado")
    void shouldThrowInvalidTaskDateExceptionWhenStartDateIsInPast() {
        // Arrange
        taskModel.setStartAt(pastDateTime);

        // Act & Assert
        InvalidTaskDateException exception = assertThrows(InvalidTaskDateException.class, () -> {
            taskService.create(taskModel, userId);
        });

        assertEquals("Start date must be in the future.", exception.getMessage());
        verify(taskRepository, never()).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando o usuário não existe ao criar tarefa")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistOnCreate() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            taskService.create(taskModel, userId);
        });

        assertTrue(exception.getMessage().contains("User with ID " + userId + " not found"));
        verify(taskRepository, never()).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve excluir uma tarefa com sucesso quando o usuário for o dono")
    void shouldDeleteTaskSuccessfullyWhenUserIsOwner() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));

        // Act
        taskService.delete(taskId, userId);

        // Assert
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).delete(taskModel);
    }

    @Test
    @DisplayName("Deve lançar TaskNotFoundException quando a tarefa não existe ao tentar excluir")
    void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExistOnDelete() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.delete(taskId, userId);
        });

        assertTrue(exception.getMessage().contains("Task with ID " + taskId + " not found"));
        verify(taskRepository, never()).delete(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedOperationException ao tentar excluir tarefa de outro usuário")
    void shouldThrowUnauthorizedOperationExceptionWhenUserIsNotOwnerOnDelete() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));

        // Act & Assert
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () -> {
            taskService.delete(taskId, differentUserId);
        });

        assertEquals("Only the task owner can delete this task", exception.getMessage());
        verify(taskRepository, never()).delete(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve atualizar uma tarefa parcialmente com sucesso")
    void shouldPatchTaskSuccessfully() {
        // Arrange
        TaskModel partialTask = new TaskModel();
        partialTask.setTitle("Updated Title");
        partialTask.setDescription("Updated Description");
        partialTask.setStartAt(futureDateTime.plusDays(2));
        
        TaskModel expectedUpdatedTask = new TaskModel();
        expectedUpdatedTask.setId(taskId);
        expectedUpdatedTask.setTitle("Updated Title");
        expectedUpdatedTask.setDescription("Updated Description");
        expectedUpdatedTask.setStartAt(futureDateTime.plusDays(2));
        expectedUpdatedTask.setEndAt(futureDateTime.plusDays(1));
        expectedUpdatedTask.setIdUserOwner(userId);
        expectedUpdatedTask.setCollaborators(new ArrayList<>());
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));
        when(taskRepository.save(any(TaskModel.class))).thenReturn(expectedUpdatedTask);

        // Act
        TaskModel result = taskService.patch(taskId, partialTask, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(futureDateTime.plusDays(2), result.getStartAt());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidTaskDateException quando a data de início atualizada for no passado")
    void shouldThrowInvalidTaskDateExceptionWhenUpdatedStartDateIsInPast() {
        // Arrange
        TaskModel partialTask = new TaskModel();
        partialTask.setStartAt(pastDateTime);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));

        // Act & Assert
        InvalidTaskDateException exception = assertThrows(InvalidTaskDateException.class, () -> {
            taskService.patch(taskId, partialTask, userId);
        });

        assertEquals("Start date must be in the future.", exception.getMessage());
        verify(taskRepository, never()).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedOperationException ao tentar atualizar tarefa de outro usuário")
    void shouldThrowUnauthorizedOperationExceptionWhenUserIsNotOwnerOnPatch() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        TaskModel partialTask = new TaskModel();
        partialTask.setTitle("Updated Title");
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));

        // Act & Assert
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () -> {
            taskService.patch(taskId, partialTask, differentUserId);
        });

        assertEquals("Only the task owner can update this task", exception.getMessage());
        verify(taskRepository, never()).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve adicionar um colaborador com sucesso")
    void shouldAddCollaboratorSuccessfully() {
        // Arrange
        TaskModel taskWithCollaborator = new TaskModel();
        taskWithCollaborator.setId(taskId);
        taskWithCollaborator.setTitle("Test Task");
        taskWithCollaborator.setIdUserOwner(userId);
        taskWithCollaborator.setCollaborators(new ArrayList<>());
        taskWithCollaborator.getCollaborators().add(collaboratorModel);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));
        when(userRepository.findById(collaboratorId)).thenReturn(Optional.of(collaboratorModel));
        when(taskRepository.save(any(TaskModel.class))).thenReturn(taskWithCollaborator);

        // Act
        TaskModel result = taskService.addCollaborator(taskId, collaboratorId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCollaborators().size());
        assertEquals(collaboratorId, result.getCollaborators().get(0).getId());
        verify(taskRepository, times(1)).findById(taskId);
        verify(userRepository, times(1)).findById(collaboratorId);
        verify(taskRepository, times(1)).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve retornar a tarefa quando o colaborador já estiver adicionado")
    void shouldReturnTaskWhenCollaboratorAlreadyAdded() {
        // Arrange
        taskModel.getCollaborators().add(collaboratorModel);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));
        when(userRepository.findById(collaboratorId)).thenReturn(Optional.of(collaboratorModel));
        when(taskRepository.save(any(TaskModel.class))).thenReturn(taskModel);

        // Act
        TaskModel result = taskService.addCollaborator(taskId, collaboratorId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCollaborators().size());
        assertEquals(collaboratorId, result.getCollaborators().get(0).getId());
        verify(taskRepository, times(1)).findById(taskId);
        verify(userRepository, times(1)).findById(collaboratorId);
        verify(taskRepository, times(1)).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedOperationException ao tentar adicionar colaborador em tarefa de outro usuário")
    void shouldThrowUnauthorizedOperationExceptionWhenUserIsNotOwnerOnAddCollaborator() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskModel));

        // Act & Assert
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () -> {
            taskService.addCollaborator(taskId, collaboratorId, differentUserId);
        });

        assertEquals("Only the task owner can add collaborators", exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(taskRepository, never()).save(any(TaskModel.class));
    }

    @Test
    @DisplayName("Deve buscar tarefas por ID do usuário dono com sucesso")
    void shouldFindTasksByUserIdSuccessfully() {
        // Arrange
        List<TaskModel> tasks = Arrays.asList(taskModel);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));
        when(taskRepository.findByIdUserOwner(userId)).thenReturn(tasks);

        // Act
        List<TaskModel> result = taskService.findByIdUserOwner(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).getId());
        verify(userRepository, times(1)).findById(userId);
        verify(taskRepository, times(1)).findByIdUserOwner(userId);
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando usuário não existe ao buscar tarefas")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistOnFindTasks() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            taskService.findByIdUserOwner(userId);
        });

        assertTrue(exception.getMessage().contains("User with ID " + userId + " not found"));
        verify(taskRepository, never()).findByIdUserOwner(any(UUID.class));
    }
}