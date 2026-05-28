package com.qrify;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRRepository qrRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                return ResponseEntity.ok(users.get(0));
            }
            return ResponseEntity.status(401).body("No account records found.");
        }
        
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QRCodeRecord>> getUserHistory() {
        List<QRCodeRecord> history = qrRepository.findAll();
        return ResponseEntity.ok(history);
    }
}