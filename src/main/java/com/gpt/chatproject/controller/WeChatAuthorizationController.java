package com.gpt.chatproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class WeChatAuthorizationController {

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/error")
    public String error() {
        return "";
    }

    @GetMapping("/MP_verify_5d5F6p98IKpM9HGi.txt")
    public ResponseEntity<Resource> websiteAuthorization() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:wxResources/MP_verify_5d5F6p98IKpM9HGi.txt");
        System.out.println(resource.getFile().toPath());
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
