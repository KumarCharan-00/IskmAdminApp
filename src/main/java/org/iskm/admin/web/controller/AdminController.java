package org.iskm.admin.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.ContentDTO;
import org.iskm.admin.web.dto.res.Response;
import org.iskm.admin.web.model.*;
import org.iskm.admin.web.service.UserService;
import org.iskm.admin.web.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    private final UserService userService;
    
    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/authenticate/user")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getUserName());

        response.addCookie(jwtUtil.generateHttpOnlyCookie(token));
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }
    
    @PostMapping("/user/create")
    public Response registerUser(@RequestBody AddUserDTO addUserDTO) {
        return userService.saveUser(addUserDTO);
    }
    
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request.getUserName(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully.");
    }
    
    @PostMapping("/content")
    public ResponseEntity<String> addContent(@ModelAttribute ContentRequest contentRequest) {
    	userService.saveContent(contentRequest);
        return ResponseEntity.ok("Content saved successfully");
    }
    
    @GetMapping("/content")
    public ResponseEntity<List<ContentDTO>> getAllContent() {
        List<ContentDTO> dtos = userService.getAllContent().stream()
            .map(userService::toContentDTO)
            .toList();
        return ResponseEntity.ok(dtos);
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContentDTO>> getContentByStatus(@PathVariable String status) {
        List<ContentDTO> dtos = userService.getContentByStatus(status).stream()
                .map(userService::toContentDTO)
                .toList();
            return ResponseEntity.ok(dtos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<String> updateContent(@PathVariable Long id,
                                                    @RequestBody ContentUpdateRequest request) {
        userService.updateContent(id, request);
        return ResponseEntity.ok("Content updated successfully");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
    	userService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}