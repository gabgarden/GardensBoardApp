package br.edu.iff.ccc.bsi.gardensBoard.controller.view;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.edu.iff.ccc.bsi.gardensBoard.exception.UserNotFoundException;
import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/login")
public class LoginViewController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String login(Model model) {
        return "login";
    }
    @PostMapping("/authenticate")
public String authenticate(@RequestParam String username, @RequestParam String password, 
                          RedirectAttributes redirectAttributes) {
    try {
        UserModel user = userService.findByUsername(username);
        
        boolean passwordMatch = BCrypt.verifyer()
            .verify(password.toCharArray(), user.getPassword())
            .verified;
            
        if (passwordMatch) {
            return "redirect:/user/home/" + user.getId();
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login?error";
        }
    } catch (UserNotFoundException e) {
        redirectAttributes.addFlashAttribute("error", "Invalid username or password");
        return "redirect:/login?error";
    }
}

    
}
                              
                            