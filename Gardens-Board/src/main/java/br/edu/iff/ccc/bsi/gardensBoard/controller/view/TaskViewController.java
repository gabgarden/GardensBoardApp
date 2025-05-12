package br.edu.iff.ccc.bsi.gardensBoard.controller.view;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.iff.ccc.bsi.gardensBoard.model.TaskModel;
import br.edu.iff.ccc.bsi.gardensBoard.service.TaskService;
import br.edu.iff.ccc.bsi.gardensBoard.service.UserService;
import br.edu.iff.ccc.bsi.gardensBoard.exception.TaskNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.InvalidTaskDateException;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UnauthorizedOperationException;

@Controller
@RequestMapping("/tasks")
public class TaskViewController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;

    @GetMapping("")
    public String listTasks(@RequestParam UUID userId, Model model) {
        try {
            // Search for user's tasks by userId
            List<TaskModel> tasks = taskService.findByIdUserOwner(userId);
            
            // add model attributes
            model.addAttribute("tasks", tasks);
            model.addAttribute("userId", userId);
            
            return "tasksPerUser";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/new")
    public String newTask(@RequestParam UUID userId, Model model) {
        try {
            // create a new task for the user
            TaskModel task = new TaskModel();
            task.setIdUserOwner(userId);
            
            
            // Adicionar atributos ao modelo
            model.addAttribute("task", task);
            model.addAttribute("userId", userId);
            
            return "newTask";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/save")
    public String saveTask(@ModelAttribute TaskModel task, 
                          @RequestParam UUID userId,
                          RedirectAttributes redirectAttributes) {
        try {
            // set the owner of the task
            task.setIdUserOwner(userId);
            
            // if doesn't have id, create a new task
            if (task.getId() == null) {
                taskService.create(task, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Tarefa criada com sucesso!");
            } else {
                // if edit, than patch
                taskService.patch(task.getId(), task, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Tarefa atualizada com sucesso!");
            }
            
            return "redirect:/tasks?userId=" + userId;
        } catch (InvalidTaskDateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Data inválida: " + e.getMessage());
            return "redirect:/tasks/new?userId=" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tasks?userId=" + userId;
        }
    }
    
    @GetMapping("/{id}/edit")
    public String editTask(@PathVariable UUID id, 
                          @RequestParam UUID userId,
                          Model model) {
        try {
            // search for the task by id
            TaskModel task = taskService.findById(id);
            
            // verify if the task belongs to the user
            if (!task.getIdUserOwner().equals(userId)) {
                model.addAttribute("errorMessage", "Você não tem permissão para editar esta tarefa");
                return "error";
            }
            
            // add attributes to the model
            model.addAttribute("task", task);
            model.addAttribute("userId", userId);
            
            return "newTask";
        } catch (TaskNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/{id}/delete")
    public String deleteTask(@PathVariable UUID id, 
                            @RequestParam UUID userId,
                            RedirectAttributes redirectAttributes) {
        try {
            // delete the task by id
            taskService.delete(id, userId);
            
            redirectAttributes.addFlashAttribute("successMessage", "Tarefa excluída com sucesso!");
            
            return "redirect:/tasks?userId=" + userId;
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Permissão negada: " + e.getMessage());
            return "redirect:/tasks?userId=" + userId;
        } catch (TaskNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tasks?userId=" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tasks?userId=" + userId;
        }
    }
    
    @GetMapping("/{id}/add-collaborator")
    public String showAddCollaboratorForm(@PathVariable UUID id,
                                         @RequestParam UUID userId,
                                         Model model) {
        try {
            // search for the task by id
            TaskModel task = taskService.findById(id);
            
            // verify if the task belongs to the user
            if (!task.getIdUserOwner().equals(userId)) {
                model.addAttribute("errorMessage", "Você não tem permissão para adicionar colaboradores a esta tarefa");
                return "error";
            }
            
            // add attributes to the model
            model.addAttribute("task", task);
            model.addAttribute("userId", userId);
            // list all users except the owner of the task
            model.addAttribute("availableUsers", userService.findAllExcept(userId));
            
            return "add-collaborator";
        } catch (TaskNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/{id}/add-collaborator")
    public String addCollaborator(@PathVariable UUID id,
                                 @RequestParam UUID collaboratorId,
                                 @RequestParam UUID userId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // add the collaborator to the task
            taskService.addCollaborator(id, collaboratorId, userId);
            
            redirectAttributes.addFlashAttribute("successMessage", "Colaborador adicionado com sucesso!");
            
            return "redirect:/tasks?userId=" + userId;
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Permissão negada: " + e.getMessage());
            return "redirect:/tasks?userId=" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tasks?userId=" + userId;
        }
    }
    

    @GetMapping("/{id}/seeTask")
    public String seeTask(@PathVariable UUID id, 
                          @RequestParam UUID userId,
                          Model model) {
        try {
            // search for the task by id
            TaskModel task = taskService.findById(id);
            
            // verify if the task belongs to the user
            if (!task.getIdUserOwner().equals(userId)) {
                model.addAttribute("errorMessage", "Você não tem permissão para editar esta tarefa");
                return "error";
            }
            
            // add attributes to the model
            model.addAttribute("task", task);
            model.addAttribute("userId", userId);

            return "seeTask";
        } catch (TaskNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
   
}

}