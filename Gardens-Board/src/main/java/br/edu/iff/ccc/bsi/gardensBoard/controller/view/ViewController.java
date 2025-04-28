package br.edu.iff.ccc.bsi.gardensBoard.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/home")
    public String index() {
        return "homepage"; 
    }

   
    

}
