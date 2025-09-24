package org.iskm.admin.web.controller;

import java.time.LocalDateTime;

import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.ContentDTO;
import org.iskm.admin.web.dto.res.FetchContentResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

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
    	try {
    		userService.saveContent(contentRequest);
            return ResponseEntity.ok("Content saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }
    
    @GetMapping("/content")
    public ResponseEntity<FetchContentResponse> getAllContent(@RequestParam(required = false) String status,
                                                          @RequestParam(required = false) LocalDateTime from,
                                                          @RequestParam(required = false) LocalDateTime to) {
        var response = new FetchContentResponse();
        var dtoList = userService.getContent(status, from, to).stream()
            .map(userService::toContentDTO)
            .toList();
        var count = dtoList.size();
        response.setContent(dtoList);
        response.setCount(count);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("content/{id}")
    public ResponseEntity<ContentDTO> patchContentById(@PathVariable String id,
                                                    @RequestBody ContentUpdateRequest request) {
    	ContentDTO content = userService.partialUpdateById(id, request);
        return ResponseEntity.ok(content);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable String id) {
    	userService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}