package com.teachingeval.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {
    private static final String USERNAME = "teacher";
    private static final String PASSWORD = "123456";

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        if (USERNAME.equals(username) && PASSWORD.equals(password)) {
            return "redirect:/students";
        }

        model.addAttribute("error", "账号或密码错误，请使用 teacher / 123456");
        return "login";
    }

    @GetMapping("/students")
    public String students() {
        return "students";
    }

    @GetMapping("/works")
    public String works() {
        return "works";
    }

    @GetMapping("/evaluation")
    public String evaluation() {
        return "evaluation";
    }

    @GetMapping("/export")
    public String export() {
        return "export";
    }
}
