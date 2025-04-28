package br.edu.iff.ccc.bsi.gardensBoard.service;

import br.edu.iff.ccc.bsi.gardensBoard.model.TaskModel;
import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.repository.TaskRepository;
import br.edu.iff.ccc.bsi.gardensBoard.repository.UserRepository;
import br.edu.iff.ccc.bsi.gardensBoard.exception.TaskNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.InvalidTaskDateException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UnauthorizedOperationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a new task after validating the start date
     * @param taskModel the task to be created
     * @param idUser the ID of the user creating the task
     * @return the created task
     * @throws InvalidTaskDateException if the start date is in the past
     */
    public TaskModel create(TaskModel taskModel, UUID idUser) {
        // Validate that the start date is in the future
        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt())) {
            throw new InvalidTaskDateException("Start date must be in the future.");
        }
        
        // Validate that the user exists
        userRepository.findById(idUser)
            .orElseThrow(() -> new UserNotFoundException("User with ID " + idUser + " not found"));
            
        // Save and return the task
        return taskRepository.save(taskModel);
    }

    /**
     * Deletes a task by its ID
     * @param idTask the ID of the task to delete
     * @param idUser the ID of the user attempting to delete the task
     * @throws TaskNotFoundException if the task doesn't exist
     * @throws UnauthorizedOperationException if the user is not the owner of the task
     */
    public void delete(UUID idTask, UUID idUser) {
        TaskModel task = taskRepository.findById(idTask)
            .orElseThrow(() -> new TaskNotFoundException("Task with ID " + idTask + " not found"));
        
        // Check if the user is the owner of the task
        if (!task.getIdUserOwner().equals(idUser)) {
            throw new UnauthorizedOperationException("Only the task owner can delete this task");
        }
    
        taskRepository.delete(task);
    }
    
    /**
     * Updates a task with partial data
     * @param idTask the ID of the task to update
     * @param partialTask the partial task data to update
     * @param idUser the ID of the user attempting to update the task
     * @return the updated task
     * @throws TaskNotFoundException if the task doesn't exist
     * @throws UnauthorizedOperationException if the user is not the owner of the task
     * @throws InvalidTaskDateException if the start date is in the past
     */
    public TaskModel patch(UUID idTask, TaskModel partialTask, UUID idUser) {
        TaskModel task = taskRepository.findById(idTask)
            .orElseThrow(() -> new TaskNotFoundException("Task with ID " + idTask + " not found"));
            
        // Check if the user is the owner of the task
        if (!task.getIdUserOwner().equals(idUser)) {
            throw new UnauthorizedOperationException("Only the task owner can update this task");
        }
        
        // Validate start date if it's being updated
        if (partialTask.getStartAt() != null) {
            var currentDate = LocalDateTime.now();
            if (currentDate.isAfter(partialTask.getStartAt())) {
                throw new InvalidTaskDateException("Start date must be in the future.");
            }
            task.setStartAt(partialTask.getStartAt());
        }
    
        // Update fields if they are present in the partial task
        if (partialTask.getTitle() != null) task.setTitle(partialTask.getTitle());
        if (partialTask.getDescription() != null) task.setDescription(partialTask.getDescription());
        if (partialTask.getPriority() != null) task.setPriority(partialTask.getPriority());
        if (partialTask.getEndAt() != null) task.setEndAt(partialTask.getEndAt());
    
        return taskRepository.save(task);
    }
    
    /**
     * Adds a collaborator to a task
     * @param taskId the ID of the task
     * @param userId the ID of the user to add as a collaborator
     * @param requestUserId the ID of the user making the request
     * @return the updated task
     * @throws TaskNotFoundException if the task doesn't exist
     * @throws UserNotFoundException if the user doesn't exist
     * @throws UnauthorizedOperationException if the requesting user is not the owner
     */
    public TaskModel addCollaborator(UUID taskId, UUID userId, UUID requestUserId) {
        TaskModel task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskId + " not found"));

        // Check if the requesting user is the owner of the task
        if (!task.getIdUserOwner().equals(requestUserId)) {
            throw new UnauthorizedOperationException("Only the task owner can add collaborators");
        }

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        // Check if the user is already a collaborator
        if (task.getCollaborators().contains(user)) {
            return task; // User is already a collaborator, no need to add again
        }

        task.getCollaborators().add(user);
        return taskRepository.save(task);
    }

    /**
     * Finds tasks owned by a specific user
     * @param idUserOwner the ID of the task owner
     * @return list of tasks owned by the user
     */
    public List<TaskModel> findByIdUserOwner(UUID idUserOwner) {
        // Validate that the user exists
        userRepository.findById(idUserOwner)
            .orElseThrow(() -> new UserNotFoundException("User with ID " + idUserOwner + " not found"));
            
        return taskRepository.findByIdUserOwner(idUserOwner);
    }
}