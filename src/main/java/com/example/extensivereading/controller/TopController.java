package com.example.extensivereading.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {
    @GetMapping("/")
    public String showTop() {
        return "top";
    }

    @GetMapping("/top")
    public String showTopAlias() {
        return "top";
    }
}