package org.iskm.admin.web.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import org.springframework.lang.NonNull;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );
        log.info("User authenticated completed :: {}", authentication);
        String token = jwtUtil.generateToken(request.getUserName());
        log.info("Generated token for user: {}", token.substring(0, 5));
        response.addCookie(jwtUtil.generateHttpOnlyCookie(token));
        log.info("Added cookie for user");
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

    @PostMapping("/user/create")
    public Response registerUser(@RequestBody @NonNull AddUserDTO addUserDTO) {
        log.info("Registering user: {}", addUserDTO.getUserName());
        return userService.saveUser(addUserDTO);
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody @NonNull PasswordUpdateRequest request) {
        log.info("Updating password for user: {}", request.getUserName());
        userService.updatePassword(request.getUserName(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PostMapping("/content")
    public ResponseEntity<String> addContent(@ModelAttribute @NonNull ContentRequest contentRequest) {
        contentRequest.setType(contentRequest.getType().toUpperCase());
        log.info("Adding content: {} of type {}", contentRequest.getTitle(), contentRequest.getType());
        try {
            var response = userService.saveContent(contentRequest);
            if (response instanceof ContentDTO) {
                return ResponseEntity.ok("Content saved successfully");
            } else {
                return ResponseEntity.status(500).body(response.toString());
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/public/content")
    public ResponseEntity<FetchContentResponse> getPublishedContent(
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Optional<Integer> k) {
        log.info("Fetching published content from: {} to: {}", from, to);
        return getAllContent(type, List.of("published"), from, to, k.or(() -> Optional.of(0)));
    }

    @GetMapping("/content")
    public ResponseEntity<FetchContentResponse> getAllContent(
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Optional<Integer> k) {
        log.info("Fetching {} {} content from: {} to: {} limit to {}", type, status, from, to, k.orElse(0));
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (from != null && !from.trim().isEmpty()) {
            log.info("From date: {}", from);
            fromDateTime = LocalDateTime.parse(from + "T00:00:00");
        }
        if (to != null && !to.trim().isEmpty()) {
            log.info("To date: {}", to);
            toDateTime = LocalDateTime.parse(to + "T23:59:59");
        }

        var response = new FetchContentResponse();
        var dtoList = userService.getContent(type, status, fromDateTime, toDateTime, k.orElse(0)).stream()
                .map(userService::toContentDTO)
                .toList();
        var count = dtoList.size();
        response.setContent(dtoList);
        response.setCount(count);
        log.info("Response count: {}", count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/content/{id}")
    public ResponseEntity<ContentDTO> patchContentById(@PathVariable String id,
            @RequestBody ContentUpdateRequest request) {
        log.info("Patching content: {}", id);
        ContentDTO content = userService.partialUpdateById(id, request);
        return ResponseEntity.ok(content);
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable @NonNull String id) {
        userService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/images/{contentId}")
    public ResponseEntity<List<ImageDTO>> getImagesByContentId(@PathVariable @NonNull String contentId) {
        List<ImageDTO> images = userService.getImagesByContentId(contentId);
        if (images.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(images);
    }

}
