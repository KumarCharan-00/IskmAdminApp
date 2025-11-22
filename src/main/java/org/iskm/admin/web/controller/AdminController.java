package org.iskm.admin.web.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.ContentDTO;
import org.iskm.admin.web.dto.res.ContentDTO.ImageDTO;
import org.iskm.admin.web.dto.res.FetchContentResponse;
import org.iskm.admin.web.dto.res.Response;
import org.iskm.admin.web.model.AuthenticationRequest;
import org.iskm.admin.web.model.AuthenticationResponse;
import org.iskm.admin.web.model.ContentRequest;
import org.iskm.admin.web.model.ContentUpdateRequest;
import org.iskm.admin.web.model.PasswordUpdateRequest;
import org.iskm.admin.web.service.UserService;
import org.iskm.admin.web.util.JwtUtil;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AdminController {

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final UserService userService;

    public AdminController(JwtUtil jwtutil, AuthenticationManager authenticationManager, UserService userService) {
        this.jwtUtil = jwtutil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping("/user/authenticate")
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
    public ResponseEntity<String> addContent(@ModelAttribute ContentRequest contentRequest, @RequestHeader("Channel") String channel) {
        try {
            userService.saveContent(contentRequest, channel);
            return ResponseEntity.ok("Content saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/content")
    public ResponseEntity<FetchContentResponse> getAllContent(@RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (from != null && !from.trim().isEmpty()) {
            fromDateTime = LocalDateTime.parse(from + "T00:00:00");
        }
        if (to != null && !to.trim().isEmpty()) {
            toDateTime = LocalDateTime.parse(to + "T23:59:59");
        }

        var response = new FetchContentResponse();
        var dtoList = userService.getContent(status, fromDateTime, toDateTime).stream()
                .map(userService::toContentDTO)
                .toList();
        var count = dtoList.size();
        response.setContent(dtoList);
        response.setCount(count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/content/{id}")
    public ResponseEntity<ContentDTO> patchContentById(@PathVariable String id,
            @RequestBody ContentUpdateRequest request) {
        ContentDTO content = userService.partialUpdateById(id, request);
        return ResponseEntity.ok(content);
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable String id) {
        userService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/webImages/{contentId}")
    public ResponseEntity<List<ImageDTO>> getWebImagesByContentId(@PathVariable String contentId) {
        List<ImageDTO> images = userService.getWebImagesByContentId(contentId);
        if (images.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(images);
    }

    @GetMapping("/mobileImages/{contentId}")
    public ResponseEntity<List<ImageDTO>> getMobileImagesByContentId(@PathVariable String contentId) {
        List<ImageDTO> images = userService.getMobileImagesByContentId(contentId);
        if (images.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(images);
    }

}
