package com.qrify;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public String handleSignUp(@RequestParam("name") String name,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password) {
        try {
            if (userRepository.findByEmail(email).isPresent()) {
                return "redirect:/index.html?error=exists";
            }
            
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(password);
            userRepository.save(newUser);
            
            return "redirect:/index.html?signup=success";
        } catch (Exception e) {
            return "redirect:/index.html?error=invalid";
        }
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              HttpSession session) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("currentUser", userOpt.get());
            return "redirect:/dashboard.html";
        }
        
        return "redirect:/index.html?error=invalid";
    }
}