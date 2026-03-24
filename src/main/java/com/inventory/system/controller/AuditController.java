package com.inventory.system.controller;

import com.inventory.system.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping
    public String viewAuditLogs(Model model) {
        model.addAttribute("logs", auditService.getAllLogs());
        model.addAttribute("title", "Audit Logs");
        return "audit/list";
    }
}