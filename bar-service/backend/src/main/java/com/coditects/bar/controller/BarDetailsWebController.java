// package com.coditects.bar.controller;

// import java.util.AbstractMap;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.UUID;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.coditects.bar.model.dto.BarDto;
// import com.coditects.bar.model.dto.BarStockItemDto;
// import com.coditects.bar.model.dto.SupplyItemDto;
// import com.coditects.bar.model.dto.SupplyRequestDto;
// import com.coditects.bar.model.dto.UsageLogDto;
// import com.coditects.bar.service.BarService;
// import com.coditects.bar.service.BarStockService;
// import com.coditects.bar.service.SupplyRequestService;
// import com.coditects.bar.service.UsageLogService;

// import lombok.RequiredArgsConstructor;

// @Controller
// @RequestMapping("/bars")
// @RequiredArgsConstructor
// public class BarDetailsWebController {

//     private final BarService barService;
//     private final BarStockService stockService;
//     private final UsageLogService usageLogService;
//     private final SupplyRequestService supplyRequestService;

//     // Display details of a specific bar
//     @GetMapping("/{barId}")
//     public String getBarDetails(@PathVariable UUID barId, Model model) {
//         BarDto bar = barService.getBar(barId);
//         if (bar == null) {
//             return "redirect:/bars";
//         }

//         List<BarStockItemDto> stock = stockService.getStock(barId);
//         List<UsageLogDto> serveLog = usageLogService.getLogsForBar(barId);

//         // Create a map of productId to product name from stock
//         Map<UUID, String> productNameMap = stock.stream()
//                 .collect(Collectors.toMap(
//                         BarStockItemDto::id,
//                         BarStockItemDto::name,
//                         (existing, replacement) -> existing // In case of duplicate product IDs, keep the first name
//                 ));

//         // Map serveLog to include product names
//         List<Map<String, Object>> serveLogWithNames = serveLog.stream().map(log -> {
//             Map<String, Object> logEntry = new HashMap<>();
//             String productName = productNameMap.getOrDefault(log.productId(), "Unknown Product");
//             logEntry.put("name", productName);
//             logEntry.put("quantity", log.quantity());
//             logEntry.put("timestamp", log.timestamp());
//             return logEntry;
//         }).collect(Collectors.toList());

//         // Calculate Total Served using product names from stock
//         Map<UUID, Integer> totalServedById = serveLog.stream()
//                 .collect(Collectors.groupingBy(
//                         UsageLogDto::productId,
//                         Collectors.summingInt(UsageLogDto::quantity)
//                 ));

//         List<Map.Entry<String, Integer>> totalServed = totalServedById.entrySet().stream()
//                 .map(entry -> {
//                     String productName = productNameMap.getOrDefault(entry.getKey(), "Unknown Product");
//                     return new AbstractMap.SimpleEntry<>(productName, entry.getValue());
//                 })
//                 .collect(Collectors.toList());

//         List<SupplyRequestDto> supplyRequests = supplyRequestService.getRequestsByBar(barId);

//         model.addAttribute("bar", bar);
//         model.addAttribute("stock", stock);
//         model.addAttribute("serveLog", serveLogWithNames);
//         model.addAttribute("totalServed", totalServed);
//         model.addAttribute("supplyRequests", supplyRequests);

//         return "bar_details";
//     }


//     @PostMapping("/{barId}/sell")
//     public String sellProduct(@PathVariable UUID barId, @RequestParam UUID productId, @RequestParam int quantity, Model model) {
//         try {
//             stockService.reduceStock(barId, productId, quantity);
//             usageLogService.logDrinkServed(barId, productId, quantity);
//         } catch (Exception e) {
//             model.addAttribute("error", "Failed to sell product: " + e.getMessage());
//         }
//         return "redirect:/bars/" + barId;
//     }

//     @PostMapping("/{barId}/request-supply")
//     public String requestSupply(@PathVariable UUID barId, @RequestParam UUID productId, @RequestParam String productName, Model model) {
//         try {
//             List<SupplyItemDto> items = List.of(new SupplyItemDto(productId, productName, 36));
//             supplyRequestService.createRequest(barId, items);
//         } catch (Exception e) {
//             model.addAttribute("error", "Failed to request supply: " + e.getMessage());
//         }
//         return "redirect:/bars/" + barId;
//     }

//     @PostMapping("/{barId}/cancel-supply")
//     public String cancelSupplyRequest(@PathVariable UUID barId, @RequestParam UUID requestId, Model model) {
//         try {
//             SupplyRequestDto request = supplyRequestService.getRequest(requestId);
//             model.addAttribute("error", "Cannot cancel supply request: Invalid request or status.");
//         } catch (Exception e) {
//             model.addAttribute("error", "Failed to cancel supply request: " + e.getMessage());
//         }
//         return "redirect:/bars/" + barId;
//     }
// }