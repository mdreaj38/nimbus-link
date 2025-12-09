package com.nimbuslink.web;

import com.nimbuslink.server.SubscriptionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final SubscriptionManager subscriptionManager;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("clientCount", subscriptionManager.getClientCount());
        model.addAttribute("rooms", subscriptionManager.getActiveRooms());
        return "dashboard";
    }
}
