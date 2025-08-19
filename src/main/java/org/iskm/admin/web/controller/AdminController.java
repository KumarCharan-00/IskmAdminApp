package org.iskm.admin.web.controller;

import java.util.List;

import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.ContentDTO;
import org.iskm.admin.web.dto.res.Response;
import org.iskm.admin.web.model.AuthenticationRequest;
import org.iskm.admin.web.model.AuthenticationResponse;
import org.iskm.admin.web.model.ContentRequest;
import org.iskm.admin.web.model.ContentUpdateRequest;
import org.iskm.admin.web.model.PasswordUpdateRequest;
import org.iskm.admin.web.service.UserService;
import org.iskm.admin.web.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AdminController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    
    private final UserService userService;
    
    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody AuthenticationRequest request) {
    	authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getUserName());
        return new AuthenticationResponse(token);
    }
    
    @PostMapping("/save/user")
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