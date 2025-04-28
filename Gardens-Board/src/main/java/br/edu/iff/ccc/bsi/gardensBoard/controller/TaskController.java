package br.edu.iff.ccc.bsi.gardensBoard.controller;

import br.edu.iff.ccc.bsi.gardensBoard.model.TaskModel;
import br.edu.iff.ccc.bsi.gardensBoard.service.TaskService;
import br.edu.iff.ccc.bsi.gardensBoard.exception.TaskNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.InvalidTaskDateException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UnauthorizedOperationException;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Operations related to tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/")
    @Operation(summary = "Create new task", description = "Creates a new task for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> create(
            @Parameter(description = "Input data for creating a new task") 
            @Valid @RequestBody TaskModel taskModel,
            HttpServletRequest request) {

        // Verify if the user is authenticated
        var idUser = request.getAttribute("idUser");
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }
        
        // Define the owner of the task
        taskModel.setIdUserOwner((UUID) idUser);
        
        try {
            // Chamar o service para criar a tarefa
            TaskModel createdTask = taskService.create(taskModel, (UUID) idUser);
            // Usar status 201 (Created) para criação de recursos
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (InvalidTaskDateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Deletes a task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<?> delete(
            @PathVariable UUID id,
            HttpServletRequest request) {
        
        var idUser = request.getAttribute("idUser");
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }
        
        try {
            taskService.delete(id, (UUID) idUser);
            return ResponseEntity.noContent().build(); // HTTP 204
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates a task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<?> patch(
            @Parameter(description = "Task ID") 
            @PathVariable UUID id,
            @Parameter(description = "Input data for updating the task") 
            @Valid @RequestBody TaskModel taskModel,
            HttpServletRequest request) {
        
        var idUser = request.getAttribute("idUser");
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }
        
        try {
            TaskModel updated = taskService.patch(id, taskModel, (UUID) idUser);
            return ResponseEntity.ok(updated);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (InvalidTaskDateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    @Operation(summary = "List tasks", description = "Retrieves all tasks for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "No tasks found for this user")
    })
    public ResponseEntity<?> getTasksByAuthenticatedUser(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("idUser");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }
        
        try {
            List<TaskModel> tasks = taskService.findByIdUserOwner(userId);

            if (tasks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tasks found for this user");
            }

            List<EntityModel<TaskModel>> taskResources = tasks.stream().map(task -> {
                EntityModel<TaskModel> resource = EntityModel.of(task);

                // Link para o próprio recurso
                resource.add(linkTo(methodOn(TaskController.class).getTasksByAuthenticatedUser(request)).withSelfRel());

                // Link para a lista de todas as tarefas do usuário
                resource.add(linkTo(methodOn(TaskController.class).getTasksByAuthenticatedUser(request)).withRel("tasks"));

                return resource;
            }).toList();

            CollectionModel<EntityModel<TaskModel>> collection = CollectionModel.of(taskResources);

            // Adiciona um link geral para esta coleção
            collection.add(linkTo(methodOn(TaskController.class).getTasksByAuthenticatedUser(request)).withSelfRel());

            return ResponseEntity.ok(collection);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{taskId}/collaborators/{userId}")
    @Operation(summary = "Add a collaborator to a task", description = "Adds a user as a collaborator to a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Collaborator added successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Task or user not found")
    })
    public ResponseEntity<?> addCollaborator(
        @Parameter(description = "ID of the task") @PathVariable UUID taskId,
        @Parameter(description = "ID of the user to add as collaborator") @PathVariable UUID userId,
        HttpServletRequest request) {
        
        UUID requestUserId = (UUID) request.getAttribute("idUser");
        if (requestUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }
        
        try {
            TaskModel updatedTask = taskService.addCollaborator(taskId, userId, requestUserId);
            return ResponseEntity.ok(updatedTask);
        } catch (TaskNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}