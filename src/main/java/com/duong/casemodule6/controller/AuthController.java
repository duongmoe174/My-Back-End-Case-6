package com.duong.casemodule6.controller;

import com.duong.casemodule6.model.AppERole;
import com.duong.casemodule6.model.AppRole;
import com.duong.casemodule6.model.AppUser;
import com.duong.casemodule6.model.dto.JwtResponse;
import com.duong.casemodule6.model.dto.MessageResponse;
import com.duong.casemodule6.model.dto.UserPrinciple;
import com.duong.casemodule6.model.payload.LoginRequest;
import com.duong.casemodule6.model.payload.SignupRequest;
import com.duong.casemodule6.service.approle.IAppRoleService;
import com.duong.casemodule6.service.appuser.IAppUserService;
import com.duong.casemodule6.service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class AuthController {
    @Autowired
    private IAppRoleService appRoleService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private IAppUserService userService;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateTokenLogin(authentication);

        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        AppUser currentUser = userService.findByName(loginRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt, currentUser.getId(), userDetails.getUsername(), userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userService.existsByName(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: username is already taken!"));
        }
        AppUser appUser = new AppUser(signupRequest.getUsername(), signupRequest.getPassword());
        Set<String> strRoles = signupRequest.getRole();
        Set<AppRole> roles = new HashSet<>();
        if (strRoles == null) {
            AppRole userRole = appRoleService.findByName(String.valueOf(AppERole.ROLE_USER)).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        AppRole adminRole = appRoleService.findByName(String.valueOf(AppERole.ROLE_ADMIN)).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        AppRole modRole = appRoleService.findByName(String.valueOf(AppERole.ROLE_MODERATOR)).orElseThrow(()-> new RuntimeException("Error: Role is not found"));
                        roles.add(modRole);
                        break;
                    default:
                        AppRole userRole = appRoleService.findByName(String.valueOf(AppERole.ROLE_USER)).orElseThrow(()-> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        appUser.setRoleSet(roles);
        userService.save(appUser);
        return  ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}