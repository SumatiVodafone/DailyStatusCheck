package com.example.status;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api/status")
@CrossOrigin(origins = "*") // needed for UI
public class StatusApplication {

    private static final List<String> SCENARIOS = List.of(
            "Scenario1","Scenario2","Scenario3","Scenario4",
            "Scenario5","Scenario6","Scenario7","Scenario8",
            "Scenario9","Scenario10","Scenario11","Scenario12"
    );

    // date -> scenario -> value
    private final Map<String, Map<String, Object>> data = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(StatusApplication.class, args);
    }

    // Initialize date if not present
    private Map<String, Object> initDate(String date) {
        return data.computeIfAbsent(date, d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            SCENARIOS.forEach(s -> map.put(s, "No Run"));
            return map;
        });
    }

    // GET by date
    @GetMapping
    public Map<String, Object> getByDate(
            @RequestParam(required = false) String date) {

        String finalDate = (date == null)
                ? LocalDate.now().toString()
                : date;

        return initDate(finalDate);
    }

    // UPDATE scenario count
    @PostMapping
    public String updateScenario(
            @RequestParam String date,
            @RequestParam String scenario,
            @RequestParam int count) {

        Map<String, Object> scenarios = initDate(date);
        scenarios.put(scenario, count);

        return "Updated successfully";
    }
}
