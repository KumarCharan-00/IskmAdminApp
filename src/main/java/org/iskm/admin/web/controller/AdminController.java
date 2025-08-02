package org.iskm.admin.web.controller;

import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.Response;
import org.iskm.admin.web.model.AuthenticationRequest;
import org.iskm.admin.web.model.AuthenticationResponse;
import org.iskm.admin.web.service.UserService;
import org.iskm.admin.web.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
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
}