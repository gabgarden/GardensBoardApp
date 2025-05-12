package br.edu.iff.ccc.bsi.gardensBoard.controller.view;

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

import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.service.UserService;

@Controller
@RequestMapping("/user")
public class UserViewController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String register(Model model) {
        model.addAttribute("user", new UserModel());
        return "register"; 
    }
    
    @GetMapping("/home/{userId}")
    public String home(@PathVariable UUID userId, Model model) {
        UserModel user = userService.findById(userId);
        
        model.addAttribute("user", user.getName());
        model.addAttribute("userId", userId);
        
        return "home"; 
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserModel user, RedirectAttributes redirectAttributes) {
        userService.createUser(user);
        
        UUID userId = user.getId();
        
        return "redirect:/user/home/" + userId; 
    }

 @GetMapping("/my-account")
public String viewAccount(@RequestParam UUID userId, Model model) {
    UserModel user = userService.findById(userId);
    
    if (user == null) {
        return "redirect:/login";
    }
    
    model.addAttribute("user", user);
    model.addAttribute("userId", userId);
    
    return "my-account";
}
    
    @GetMapping("/my-account/edit/{userId}")
    public String editAccount(@PathVariable UUID userId, Model model) {
        UserModel user = userService.findById(userId);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        
        return "edit-account";
    }
    
    @PostMapping("/my-account/update")
    public String updateAccount(@ModelAttribute UserModel updatedUser, 
                                @RequestParam UUID userId,
                                RedirectAttributes redirectAttributes) {
        try {
            UserModel currentUser = userService.findById(userId);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (updatedUser.getPassword() == null || updatedUser.getPassword().trim().isEmpty()) {
                updatedUser.setPassword(currentUser.getPassword());
            }
            
            userService.patch(userId , updatedUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Your account has been updated successfully.");
            return "redirect:/user/my-account?userId=" + userId;

            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating account: " + e.getMessage());
            return "redirect:/user/my-account/edit/" + userId;
        }
    }
    
    @PostMapping("/my-account/delete")
    public String deleteAccount(@RequestParam UUID userId, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Your account has been deleted successfully.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account: " + e.getMessage());
            return "redirect:/user/my-account/" + userId;
        }
    }



}