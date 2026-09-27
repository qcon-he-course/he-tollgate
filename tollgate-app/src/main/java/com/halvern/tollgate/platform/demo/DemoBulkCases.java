package com.halvern.tollgate.platform.demo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * CR-160 background cases for demos and training: two years of history and a realistic open workload.
 * Inserted straight into the tables for speed. The anchor cases are made through the services instead,
 * see DemoDataLoader.
 */
@Component
public class DemoBulkCases {

    private static final Logger log = LoggerFactory.getLogger(DemoBulkCases.class);

    static final int TERMINAL_CASES = 2700;
    static final int OPEN_CASES = 300;
    static final int AMENDMENTS = 60;

    private static final String[] OFFICERS = { "officer1", "officer2", "officer3", "officer4", "officer5", "officer6",
            "officer7", "officer8", "officer9", "officer10", "officer11", "officer12" };
    private static final String[] ENGINEERS = { "engineer1", "engineer2", "engineer3", "engineer4", "engineer5", "engineer6" };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random(20240315L);
    private Set<LocalDate> holidays = new HashSet<>();
    private final Map<String, Long> loadTypes = new HashMap<>();
    private final Map<String, Long> routes = new HashMap<>();
    private final Map<Long, Long> vehicles = new HashMap<>();
    private final List<Long> hauliers = new ArrayList<>();
    private final Map<Integer, Integer> permitCounters = new HashMap<>();
    private int applicationNo = 0;
    private Long panelSittingId;

    /** One kind of load per track, so each case's track follows from its load. */
    private static class Profile {
        final String track;
        final String loadType;
        final int massKg;
        final String width;
        final String length;
        final String height;
        final int score;
        final String[] routeCodes;
        final String[] descriptions;

        Profile(String track, String loadType, int massKg, String width, String length, String height, int score,
                String[] routeCodes, String[] descriptions) {
            this.track = track;
            this.loadType = loadType;
            this.massKg = massKg;
            this.width = width;
            this.length = length;
            this.height = height;
            this.score = score;
            this.routeCodes = routeCodes;
            this.descriptions = descriptions;
        }
    }

    private static final Profile EXPRESS = new Profile("EXPRESS", "CONSTRUCTION_PLANT", 30000, "2.50", "16.00", "4.00", 0,
            new String[] { "R4", "R1", "R5" },
            new String[] { "Tracked excavator on low loader", "Crawler crane base section", "Piling rig, mast lowered",
                    "Wheeled loader and attachments" });
    private static final Profile DESK = new Profile("DESK", "TRANSFORMER", 60000, "3.50", "22.00", "4.00", 20,
            new String[] { "R4", "R1", "R5" },
            new String[] { "Distribution transformer for substation upgrade", "Transformer core, oil drained",
                    "Replacement transformer for the Kelder works" });
    private static final Profile ENGINEER = new Profile("ENGINEER", "MOBILE_CRANE", 120000, "4.50", "25.00", "5.00", 40,
            new String[] { "R4", "R6", "R3" },
            new String[] { "All-terrain mobile crane, 7 axles", "Mobile crane with counterweight carried separately",
                    "Crane for wind farm maintenance" });
    private static final Profile PANEL = new Profile("PANEL", "TRANSFORMER", 160000, "4.00", "20.00", "4.50", 65,
            new String[] { "R2", "R8" },
            new String[] { "Grid transformer, 160 t, girder trailer", "Supergrid transformer for Salt Marsh substation" });

    public void load(LocalDate today) {
        prepare();
        int decided = 0;
        for (int i = 0; i < TERMINAL_CASES; i++) {
            decided += terminalCase(today) ? 1 : 0;
        }
        for (int i = 0; i < OPEN_CASES; i++) {
            openCase(today, i);
        }
        amendments(today);
        for (Map.Entry<Integer, Integer> c : permitCounters.entrySet()) {
            jdbcTemplate.update("delete from permit_ref_counter where ref_year = ?", c.getKey());
            jdbcTemplate.update("insert into permit_ref_counter (ref_year, last_value) values (?, ?)", c.getKey(), c.getValue());
        }
        // keep new application references clear of the demo ones
        jdbcTemplate.queryForObject("select setval('application_ref_seq', ?)", Long.class, (long) applicationNo + 100);
        log.info("Demo cases: {} terminal ({} decided), {} open", TERMINAL_CASES, decided, OPEN_CASES);
    }

    public Set<LocalDate> holidays() {
        return holidays;
    }

    private void prepare() {
        for (Date d : jdbcTemplate.queryForList("select holiday_date from holiday", Date.class)) {
            holidays.add(d.toLocalDate());
        }
        jdbcTemplate.query("select id, code from load_type", rs -> {
            loadTypes.put(rs.getString("code"), rs.getLong("id"));
        });
        jdbcTemplate.query("select id, code from route", rs -> {
            routes.put(rs.getString("code"), rs.getLong("id"));
        });
        jdbcTemplate.query("select haulier_id, min(id) as id from vehicle group by haulier_id", rs -> {
            vehicles.put(rs.getLong("haulier_id"), rs.getLong("id"));
        });
        hauliers.addAll(jdbcTemplate.queryForList("select distinct haulier_id from vehicle order by haulier_id", Long.class));
    }

    private boolean terminalCase(LocalDate today) {
        LocalDate lodged = businessDayOnOrBefore(today.minusDays(10 + random.nextInt(720)));
        Profile p = pickProfile();
        long haulier = hauliers.get(random.nextInt(hauliers.size()));
        LocalDate first = lodged.plusDays(20 + random.nextInt(25));
        LocalDate last = first.plusDays(random.nextInt(3));
        int roll = random.nextInt(100);
        String state;
        String outcome = null;
        if (roll < 50) {
            state = "DECIDED";
            outcome = "EXPRESS".equals(p.track) ? "ISSUED" : (random.nextInt(4) == 0 ? "ISSUED_WITH_CONDITIONS" : "ISSUED");
        } else if (roll < 60) {
            state = "DECIDED";
            outcome = "REFUSED";
        } else if (roll < 77) {
            state = "WITHDRAWN";
        } else if (roll < 86) {
            state = "CANCELLED";
        } else {
            state = "LAPSED";
        }
        if ("LAPSED".equals(state) && "EXPRESS".equals(p.track)) {
            state = "WITHDRAWN";
        }
        String legacy = "DECIDED".equals(state) ? ("REFUSED".equals(outcome) ? "REJECTED" : "APPROVED") : "SUBMITTED";
        long appId = application(p, haulier, lodged, first, last, legacy);
        String officer = OFFICERS[random.nextInt(OFFICERS.length)];
        String engineer = needsEngineer(p.track) ? ENGINEERS[random.nextInt(ENGINEERS.length)] : null;
        LocalDateTime lodgedAt = lodged.atTime(9 + random.nextInt(7), random.nextInt(60));
        LocalDateTime closedAt = lodgedAt.plusDays(1 + random.nextInt(targetDays(p.track) + 5));
        if (!closedAt.toLocalDate().isBefore(today)) {
            closedAt = today.minusDays(1).atTime(15, 0);
        }
        String reference = reference(appId);
        String closedReason = "WITHDRAWN".equals(state) ? "Withdrawn by the haulier"
                : "CANCELLED".equals(state) ? "Duplicate application" : null;
        long caseId = caseFile(reference, appId, haulier, state, outcome, p, officer, engineer, lodgedAt,
                "DECIDED".equals(state) ? closedAt : null, closedReason, closedAt);
        assessment(caseId, p, lodgedAt);
        audit(reference, null, "LODGED", officer, lodgedAt, null);
        if ("DECIDED".equals(state)) {
            String decidedBy = "EXPRESS".equals(p.track) ? "SYSTEM" : officer;
            if ("EXPRESS".equals(p.track)) {
                closedAt = lodgedAt.plusMinutes(1);
                jdbcTemplate.update("update case_file set decided_at = ?, updated_at = ? where id = ?",
                        Timestamp.valueOf(closedAt), Timestamp.valueOf(closedAt), caseId);
            }
            jdbcTemplate.update("insert into decision (case_id, outcome, reason, decided_by, decided_at, panel) values (?, ?, ?, ?, ?, ?)",
                    caseId, outcome, "REFUSED".equals(outcome) ? "Route cannot take the load at the dates requested" : null,
                    decidedBy, Timestamp.valueOf(closedAt), "PANEL".equals(p.track));
            if (!"REFUSED".equals(outcome)) {
                permit(caseId, appId, p, first, last, today, closedAt, decidedBy, outcome);
            }
            audit(reference, "IN_REVIEW", "DECIDED", decidedBy, closedAt, outcome);
        } else {
            if ("LAPSED".equals(state)) {
                LocalDate issued = lodged.plusDays(3);
                long roundId = jdbcTemplate.queryForObject("insert into query_round (case_id, round_no, issued_by, issued_on, deadline, status, reminder_sent) "
                        + "values (?, 1, ?, ?, ?, 'LAPSED', true) returning id", Long.class, caseId, officer, Date.valueOf(issued),
                        Date.valueOf(issued.plusDays(7)));
                jdbcTemplate.update("insert into query_question (query_round_id, seq, question_text) values (?, 1, ?)", roundId,
                        "Please send the axle load schedule.");
            }
            audit(reference, "IN_REVIEW", state, "LAPSED".equals(state) ? "SYSTEM" : officer, closedAt, closedReason);
        }
        return "DECIDED".equals(state);
    }

    private void openCase(LocalDate today, int i) {
        Profile p;
        String state;
        if (i < 30) {
            p = DESK;
            state = "LODGED";
        } else if (i < 50) {
            p = EXPRESS;
            state = "IN_REVIEW";
        } else if (i < 170) {
            p = i < 120 ? DESK : ENGINEER;
            state = i % 4 == 0 ? "AWAITING_HAULIER" : "IN_REVIEW";
        } else if (i < 260) {
            p = i < 230 ? ENGINEER : PANEL;
            state = i % 3 == 0 ? "AWAITING_HAULIER" : "IN_REVIEW";
        } else {
            p = PANEL;
            state = "AWAITING_PANEL";
        }
        int target = "LODGED".equals(state) ? 5 : targetDays(p.track);
        int age = random.nextInt(target + 2);
        if (random.nextInt(10) == 0) {
            age = target + 1 + random.nextInt(4);
        }
        LocalDate lodged = businessDaysBefore(today, age);
        LocalDate first = today.plusDays(20 + random.nextInt(40));
        LocalDate last = first.plusDays(random.nextInt(3));
        long haulier = hauliers.get(random.nextInt(hauliers.size()));
        long appId = application(p, haulier, lodged, first, last, "SUBMITTED");
        String officer = OFFICERS[random.nextInt(OFFICERS.length)];
        String engineer = needsEngineer(p.track) && random.nextBoolean() ? ENGINEERS[random.nextInt(ENGINEERS.length)] : null;
        LocalDateTime lodgedAt = lodged.atTime(9 + random.nextInt(7), random.nextInt(60));
        String warnings = "EXPRESS".equals(p.track) ? "SHORT_NOTICE" : null;
        String reference = reference(appId);
        long caseId = caseFile(reference, appId, haulier, state, null, "LODGED".equals(state) ? null : p,
                "LODGED".equals(state) ? null : officer, engineer, lodgedAt, null, null, lodgedAt);
        if (warnings != null) {
            jdbcTemplate.update("update case_file set warnings = ? where id = ?", warnings, caseId);
        }
        audit(reference, null, "LODGED", officer, lodgedAt, null);
        if (!"LODGED".equals(state)) {
            assessment(caseId, p, lodgedAt);
            audit(reference, "LODGED", "IN_REVIEW", "SYSTEM", lodgedAt.plusHours(2), "Assessed: score " + p.score + ", track " + p.track);
        }
        if ("AWAITING_HAULIER".equals(state)) {
            LocalDate issued = businessDaysBefore(today, random.nextInt(2));
            if (issued.isBefore(lodged)) {
                issued = lodged;
            }
            long roundId = jdbcTemplate.queryForObject("insert into query_round (case_id, round_no, issued_by, issued_on, deadline, status, reminder_sent) "
                    + "values (?, 1, ?, ?, ?, 'OPEN', false) returning id", Long.class, caseId, officer, Date.valueOf(issued),
                    Date.valueOf(businessDaysAfter(issued, 4)));
            jdbcTemplate.update("insert into query_question (query_round_id, seq, question_text) values (?, 1, ?)", roundId,
                    "Please confirm the escort arrangements and send the axle load schedule.");
            jdbcTemplate.update("insert into sla_pause (case_id, cause, started_on, created_at) values (?, 'QUERY_ROUND', ?, now())",
                    caseId, Date.valueOf(issued));
            audit(reference, "IN_REVIEW", "AWAITING_HAULIER", officer, issued.atTime(11, 0), "Query round 1 issued");
        }
        if ("AWAITING_PANEL".equals(state)) {
            jdbcTemplate.update("insert into engineer_assessment (case_id, engineer, recommendation, notes, recorded_at) values (?, ?, 'REFER_TO_PANEL', ?, ?)",
                    caseId, engineer == null ? "engineer1" : engineer, "Bridge loading close to the rating, panel to decide",
                    Timestamp.valueOf(lodgedAt.plusDays(3)));
            jdbcTemplate.update("insert into panel_listing (panel_sitting_id, case_id, listed_at) values (?, ?, ?)",
                    panelSitting(today), caseId, Timestamp.valueOf(lodgedAt.plusDays(3)));
        }
    }

    private void amendments(LocalDate today) {
        List<Map<String, Object>> permits = jdbcTemplate.queryForList(
                "select id, reference, case_id, application_id, window_start, window_end, issued_by, issued_at, status from permit "
                + "where version = 1 and status in ('ACTIVE', 'EXPIRED') order by id limit ?", AMENDMENTS * 3);
        int made = 0;
        for (int i = 0; i < permits.size() && made < AMENDMENTS; i += 3) {
            Map<String, Object> p = permits.get(i);
            LocalDate start = ((Date) p.get("window_start")).toLocalDate().plusDays(7);
            LocalDate end = ((Date) p.get("window_end")).toLocalDate().plusDays(7);
            LocalDateTime requested = ((Timestamp) p.get("issued_at")).toLocalDateTime().plusDays(2);
            String status = end.isBefore(today) ? "EXPIRED" : "ACTIVE";
            jdbcTemplate.update("update permit set status = 'SUPERSEDED', status_changed_at = ? where id = ?",
                    Timestamp.valueOf(requested.plusHours(3)), p.get("id"));
            Long v2 = jdbcTemplate.queryForObject("insert into permit (reference, application_id, status, issued_by, issued_at, version, window_start, window_end, case_id) "
                    + "values (?, ?, ?, ?, ?, 2, ?, ?, ?) returning id", Long.class, p.get("reference"), p.get("application_id"), status,
                    "officer" + (1 + random.nextInt(12)), Timestamp.valueOf(requested.plusHours(3)), Date.valueOf(start), Date.valueOf(end), p.get("case_id"));
            jdbcTemplate.update("insert into amendment_request (case_id, permit_id, new_first_move_date, new_last_move_date, status, requested_by, requested_at, decided_by, decided_at) "
                    + "values (?, ?, ?, ?, 'APPROVED', 'officer1', ?, 'officer2', ?)", p.get("case_id"), p.get("id"), Date.valueOf(start),
                    Date.valueOf(end), Timestamp.valueOf(requested), Timestamp.valueOf(requested.plusHours(3)));
            made++;
            if (v2 == null) {
                break;
            }
        }
    }

    private long application(Profile p, long haulier, LocalDate lodged, LocalDate first, LocalDate last, String status) {
        String route = p.routeCodes[random.nextInt(p.routeCodes.length)];
        String description = p.descriptions[random.nextInt(p.descriptions.length)];
        LocalDateTime created = lodged.atTime(8, 30).minusDays(random.nextInt(3));
        applicationNo++;
        String reference = "HX-A-" + lodged.getYear() + "-" + String.format("%06d", applicationNo);
        long appId = jdbcTemplate.queryForObject("insert into movement_application (reference, haulier_id, vehicle_id, load_type_id, load_description, "
                + "gross_mass_kg, width_m, length_m, height_m, hazardous, route_id, first_move_date, last_move_date, status, created_by, created_at, lodged_by, lodged_at) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, false, ?, ?, ?, ?, ?, ?, ?, ?) returning id", Long.class,
                reference, haulier, vehicles.get(haulier), loadTypes.get(p.loadType), description, p.massKg, new BigDecimal(p.width),
                new BigDecimal(p.length), new BigDecimal(p.height), routes.get(route), Date.valueOf(first), Date.valueOf(last), status,
                "officer1", Timestamp.valueOf(created), "officer1", Timestamp.valueOf(lodged.atTime(9, 0)));
        boolean estuary = route.equals("R2") || route.equals("R3") || route.equals("R6") || route.equals("R7") || route.equals("R8");
        jdbcTemplate.update("insert into application_trip (application_id, trip_no, departure_at, crossing_at) values (?, 1, ?, ?)",
                appId, Timestamp.valueOf(first.atTime(9, 0)), estuary ? Timestamp.valueOf(first.atTime(10, 30)) : null);
        return appId;
    }

    private String reference(long appId) {
        return jdbcTemplate.queryForObject("select reference from movement_application where id = ?", String.class, appId);
    }

    private long caseFile(String reference, long appId, long haulier, String state, String outcome, Profile p, String officer,
            String engineer, LocalDateTime lodgedAt, LocalDateTime decidedAt, String closedReason, LocalDateTime updatedAt) {
        return jdbcTemplate.queryForObject("insert into case_file (reference, application_id, haulier_id, state, outcome, track, risk_score, "
                + "assigned_officer, assigned_engineer, lodged_at, decided_at, closed_reason, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id",
                Long.class, reference, appId, haulier, state, outcome, p == null ? null : p.track, p == null ? null : p.score, officer, engineer,
                Timestamp.valueOf(lodgedAt), decidedAt == null ? null : Timestamp.valueOf(decidedAt), closedReason, Timestamp.valueOf(updatedAt));
    }

    private void assessment(long caseId, Profile p, LocalDateTime lodgedAt) {
        jdbcTemplate.update("insert into assessment (case_id, score, score_track, final_track, assessed_at, superseded) values (?, ?, ?, ?, ?, false)",
                caseId, p.score, p.track, p.track, Timestamp.valueOf(lodgedAt.plusHours(2)));
    }

    private void permit(long caseId, long appId, Profile p, LocalDate first, LocalDate last, LocalDate today, LocalDateTime issuedAt,
            String issuedBy, String outcome) {
        int year = issuedAt.getYear();
        int next = permitCounters.merge(year, 1, Integer::sum);
        String reference = "HX-P-" + year + "-" + String.format("%05d", next);
        String status = last.isBefore(today) ? "EXPIRED" : "ACTIVE";
        if (random.nextInt(100) == 0) {
            status = "REVOKED";
        }
        long permitId = jdbcTemplate.queryForObject("insert into permit (reference, application_id, status, issued_by, issued_at, version, window_start, window_end, case_id, status_reason) "
                + "values (?, ?, ?, ?, ?, 1, ?, ?, ?, ?) returning id", Long.class, reference, appId, status, issuedBy, Timestamp.valueOf(issuedAt),
                Date.valueOf(first), Date.valueOf(last), caseId, "REVOKED".equals(status) ? "Load details were wrong" : null);
        if (new BigDecimal(p.width).compareTo(new BigDecimal("4.00")) > 0) {
            jdbcTemplate.update("insert into permit_condition (case_id, permit_id, condition_type, detail, escort_vehicles, source) values (?, ?, 'ESCORT', ?, 1, 'MANDATORY')",
                    caseId, permitId, "One escort vehicle");
        }
        if ("ISSUED_WITH_CONDITIONS".equals(outcome)) {
            jdbcTemplate.update("insert into permit_condition (case_id, permit_id, condition_type, detail, source) values (?, ?, 'TIME_WINDOW', ?, 'OFFICER')",
                    caseId, permitId, "Cross between 10:00 and 15:00");
        }
    }

    private void audit(String reference, String from, String to, String actor, LocalDateTime at, String reason) {
        jdbcTemplate.update("insert into audit_entry (reference, entity_type, old_state, new_state, actor, created_at, reason) values (?, 'CASE', ?, ?, ?, ?, ?)",
                reference, from, to, actor, Timestamp.valueOf(at), reason);
    }

    private long panelSitting(LocalDate today) {
        if (panelSittingId == null) {
            LocalDate thursday = today.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.THURSDAY));
            panelSittingId = jdbcTemplate.queryForObject("insert into panel_sitting (sitting_date, status) values (?, 'SCHEDULED') returning id",
                    Long.class, Date.valueOf(thursday));
        }
        return panelSittingId;
    }

    private Profile pickProfile() {
        int roll = random.nextInt(100);
        return roll < 45 ? EXPRESS : roll < 75 ? DESK : roll < 93 ? ENGINEER : PANEL;
    }

    private static boolean needsEngineer(String track) {
        return "ENGINEER".equals(track) || "PANEL".equals(track);
    }

    private static int targetDays(String track) {
        switch (track) {
            case "EXPRESS":
                return 2;
            case "ENGINEER":
                return 15;
            case "PANEL":
                return 25;
            default:
                return 5;
        }
    }

    boolean isBusinessDay(LocalDate d) {
        return d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(d);
    }

    LocalDate businessDayOnOrBefore(LocalDate d) {
        while (!isBusinessDay(d)) {
            d = d.minusDays(1);
        }
        return d;
    }

    /** The date n business days before today, counting today if it is one (n = 0 gives the latest business day). */
    LocalDate businessDaysBefore(LocalDate today, int n) {
        LocalDate d = businessDayOnOrBefore(today);
        for (int i = 0; i < n; i++) {
            d = businessDayOnOrBefore(d.minusDays(1));
        }
        return d;
    }

    LocalDate businessDaysAfter(LocalDate from, int n) {
        LocalDate d = from;
        int counted = 0;
        while (counted < n) {
            d = d.plusDays(1);
            if (isBusinessDay(d)) {
                counted++;
            }
        }
        return d;
    }

    static LocalDateTime at(LocalDate d, int hour) {
        return LocalDateTime.of(d, LocalTime.of(hour, 0));
    }

    List<String> officers() {
        return new ArrayList<>(List.of(OFFICERS));
    }
}
