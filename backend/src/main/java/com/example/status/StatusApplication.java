package com.example.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class StatusApplication {

    private static final String TOKEN = "witbe-secret-token";

    @Autowired
    private MongoTemplate mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(StatusApplication.class, args);
    }

    /* -------------------- AUTH -------------------- */

    private void checkAuth(String token) {
        if (!TOKEN.equals(token)) {
            throw new RuntimeException("Unauthorized");
        }
    }

    /* -------------------- BOX APIs -------------------- */

    /**
     * Optional manual box registration
     * (Auto-registration also happens via /status/run)
     */
    @PostMapping("/boxes/register")
    public String registerBox(
            @RequestHeader("X-API-TOKEN") String token,
            @RequestBody Map<String, String> payload) {

        checkAuth(token);

        String box = payload.get("box");

        Query q = new Query(Criteria.where("box").is(box));
        if (!mongoTemplate.exists(q, BoxRecord.class)) {
            BoxRecord br = new BoxRecord();
            br.box = box;
            br.firstSeen = LocalDate.now().toString();
            mongoTemplate.save(br);
        }

        return "Box registered";
    }

    @GetMapping("/boxes")
    public List<String> getBoxes() {
        return mongoTemplate.findAll(BoxRecord.class)
                .stream()
                .map(b -> b.box)
                .sorted()
                .toList();
    }

    /* -------------------- STATUS APIs -------------------- */

    @PostMapping("/status/run")
    public Map<String, Object> addRun(
            @RequestHeader("X-API-TOKEN") String token,
            @RequestBody Map<String, Object> payload) {

        checkAuth(token);

        String date = payload.getOrDefault(
                "date", LocalDate.now().toString()).toString();

        String box = payload.get("box").toString();
        String release = payload.get("release").toString();
        String scenario = payload.get("scenario").toString();
        Object value = payload.get("value");

        /* ---- auto-register box ---- */
        Query boxQuery = new Query(Criteria.where("box").is(box));
        if (!mongoTemplate.exists(boxQuery, BoxRecord.class)) {
            BoxRecord br = new BoxRecord();
            br.box = box;
            br.firstSeen = date;
            mongoTemplate.save(br);
        }

        /* ---- save run ---- */
        RunRecord run = new RunRecord();
        run.date = date;
        run.box = box;
        run.release = release;
        run.scenario = scenario;
        run.value = value;
        run.timestamp = Instant.now();

        mongoTemplate.save(run);

        return Map.of("status", "Run recorded");
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus(
            @RequestParam(required = false) String date) {

        String finalDate = (date == null)
                ? LocalDate.now().toString()
                : date;

        Query query = new Query(Criteria.where("date").is(finalDate));
        List<RunRecord> runs = mongoTemplate.find(query, RunRecord.class);

        return Map.of(
                "date", finalDate,
                "runs", runs
        );
    }
}

/* ===================== MONGO DOCUMENTS ===================== */

@Document(collection = "runs")
class RunRecord {
    @Id
    public String id;

    public String date;
    public String box;
    public String release;
    public String scenario;
    public Object value;
    public Instant timestamp;
}

@Document(collection = "boxes")
class BoxRecord {
    @Id
    public String id;

    public String box;
    public String firstSeen;
}
