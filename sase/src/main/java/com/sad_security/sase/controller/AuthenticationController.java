package com.sad_security.sase.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sad_security.sase.model.AuthenticationRequest;
import com.sad_security.sase.model.AuthenticationResponse;
import com.sad_security.sase.service.CustomStudentDetailsService;
import com.sad_security.sase.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


// Controller che viene usato per autenticare tutte le richieste fatte dagli utenti

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    CustomStudentDetailsService customStudentDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/authenticate")
    public ResponseEntity <?> authenticate(@RequestBody AuthenticationRequest auth) {
       try {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(auth.getUsername(), auth.getPassword()));
        final UserDetails userDetails = customStudentDetailsService.loadUserByUsername(auth.getUsername());
        String jwt = jwtUtil.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new AuthenticationResponse(jwt, null));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new AuthenticationResponse(null,"Invalid username o password"+e.getMessage()));
       }
    }
    
}
