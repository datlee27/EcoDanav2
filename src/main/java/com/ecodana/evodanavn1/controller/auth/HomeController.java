package com.ecodana.evodanavn1.controller.auth;

import com.ecodana.evodanavn1.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController{

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("featuredVehicles", vehicleService.getAllVehicles().subList(0, 3));  // Featured 3 vehicles
        
        
        return "auth/home";
    }





}