package br.edu.iff.ccc.bsi.gardensBoard.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import br.edu.iff.ccc.bsi.gardensBoard.model.UserModel;
import br.edu.iff.ccc.bsi.gardensBoard.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping("/user")
public class UserViewController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String register() {
        return "register"; 
    }

    
    @GetMapping("/home")
    public String home(@ModelAttribute UserModel user) {
        System.out.println("User: " + user.getUsername());
        return "home"; 
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody String username, @RequestBody String name, @RequestBody String password , Model model) {
        UserModel user = new UserModel();
        user.setUsername(username);
        user.setName(name);
        user.setPassword(password);

        model.addAttribute("user", user);
       
        // userService.createUser(user);

        return "redirect:/user/home"; 
    }
    
}
