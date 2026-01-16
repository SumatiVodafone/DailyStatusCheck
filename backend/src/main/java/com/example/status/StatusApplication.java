package com.example.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class StatusApplication {

    private static final String TOKEN = "witbe-secret-token";
    private static final String DATA_FILE = "data/store.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Set<String> boxRegistry = new LinkedHashSet<>();
    private final Map<String, Map<String, Object>> dailyRuns = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(StatusApplication.class, args);
    }

    /* -------------------- INIT & SAVE -------------------- */

    @PostConstruct
    public void loadData() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) return;

            Map<String, Object> root = mapper.readValue(file, Map.class);

            boxRegistry.addAll((List<String>) root.getOrDefault("boxes", List.of()));
            dailyRuns.putAll((Map<String, Map<String, Object>>) root.getOrDefault("runs", Map.of()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            new File("data").mkdirs();

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("boxes", boxRegistry);
            root.put("runs", dailyRuns);

            mapper.writeValue(new File(DATA_FILE), root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* -------------------- HELPERS -------------------- */

    private Map<String, Object> initDate(String date) {
        return dailyRuns.computeIfAbsent(date, d -> {
            Map<String, Object> obj = new HashMap<>();
            obj.put("runs", new ArrayList<>());
            return obj;
        });
    }

    private void checkAuth(String token) {
        if (!TOKEN.equals(token)) {
            throw new RuntimeException("Unauthorized");
        }
    }

    /* -------------------- BOX APIs -------------------- */

    @PostMapping("/boxes/register")
    public String registerBox(
            @RequestHeader("X-API-TOKEN") String token,
            @RequestBody Map<String, String> payload) {

        checkAuth(token);
        boxRegistry.add(payload.get("box"));
        saveData();
        return "Box registered";
    }

    @GetMapping("/boxes")
    public Set<String> getBoxes() {
        return boxRegistry;
    }

    /* -------------------- STATUS APIs -------------------- */

    @PostMapping("/status/run")
    public String addRun(
            @RequestHeader("X-API-TOKEN") String token,
            @RequestBody Map<String, Object> payload) {

        checkAuth(token);

        String date = (String) payload.getOrDefault(
                "date", LocalDate.now().toString());

        Map<String, Object> dateObj = initDate(date);
        List<Map<String, Object>> runs =
                (List<Map<String, Object>>) dateObj.get("runs");

        runs.add(new LinkedHashMap<>(payload));
        saveData();
        return "Run recorded";
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus(
            @RequestParam(required = false) String date) {

        String finalDate = (date == null)
                ? LocalDate.now().toString()
                : date;

        return initDate(finalDate);
    }
}
