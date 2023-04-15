package com.gpt.chatproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wxPay")
public class WXPayController {

    @GetMapping("getPay")
    public String person(Model model) {
        model.addAttribute("name", "小C");
        return "/view/pay";
    }
}
