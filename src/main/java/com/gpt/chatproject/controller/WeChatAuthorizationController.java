package com.gpt.chatproject.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class WeChatAuthorizationController {

    @GetMapping("/error")
    public String error() {
        return "";
    }

    @GetMapping("/MP_verify_5d5F6p98IKpM9HGi.txt")
    public ResponseEntity<InputStreamResource> websiteAuthorization() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("wxResources/MP_verify_5d5F6p98IKpM9HGi.txt");
            InputStreamResource inputStreamResource = new InputStreamResource(classPathResource.getInputStream());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + classPathResource.getFilename() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(inputStreamResource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}