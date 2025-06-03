// package com.coditects.bar.controller;

// import java.util.List;

// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.coditects.bar.model.dto.BarDto;
// import com.coditects.bar.service.BarService;

// import lombok.RequiredArgsConstructor;

// @Controller
// @RequestMapping("/bars")
// @RequiredArgsConstructor
// public class BarController {

//     private final BarService barService;

//     // Display the list of all bars
//     @GetMapping
//     public String getAllBars(Model model) {
//         List<BarDto> bars = barService.getAllBars();
//         model.addAttribute("bars", bars);
//         return "index"; // Refers to index.html in src/main/resources/templates
//     }

// //    // Display details of a specific bar
// //    @GetMapping("/{barId}")
// //    public String getBarDetails(@PathVariable UUID barId, Model model) {
// //        BarDto bar = barService.getBar(barId);
// //        if (bar == null) {
// //            return "redirect:/bars"; // Redirect to bars list if bar not found
// //        }
// //        model.addAttribute("bar", bar);
// //        return "bar_details"; // Refers to bar_details.html in src/main/resources/templates
// //    }

//     // Handle form submission for adding a new bar
//     @PostMapping("/add")
//     public String addBar(@RequestParam String name, @RequestParam String location, @RequestParam int maxCapacity, Model model) {
//         barService.registerBar(name, location, maxCapacity);
//         return "redirect:/bars"; // Redirect to the bars list after successful creation
//     }
// }