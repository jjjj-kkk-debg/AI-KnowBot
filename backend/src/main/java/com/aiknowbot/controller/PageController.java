package com.aiknowbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/article")
    public String article() {
        return "article";
    }

    @GetMapping("/image")
    public String image() {
        return "image";
    }

    @GetMapping("/analysis")
    public String analysis() {
        return "analysis";
    }

    @GetMapping("/knowledge")
    public String knowledge() {
        return "knowledge";
    }
}
