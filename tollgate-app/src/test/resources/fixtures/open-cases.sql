-- Three open cases for repository and dashboard tests: one Desk, one Engineer, one not yet assessed.
INSERT INTO movement_application (id, reference, haulier_id, hazardous, status, created_by, created_at)
VALUES (9001, 'HX-A-2023-009001', 1, false, 'SUBMITTED', 'officer1', '2023-03-13 09:00:00'),
       (9002, 'HX-A-2023-009002', 2, false, 'SUBMITTED', 'officer1', '2023-03-13 09:00:00'),
       (9003, 'HX-A-2023-009003', 3, false, 'SUBMITTED', 'officer1', '2023-03-13 09:00:00');

INSERT INTO case_file (id, reference, application_id, haulier_id, state, track, assigned_officer, lodged_at, updated_at)
VALUES (9001, 'HX-A-2023-009001', 9001, 1, 'IN_REVIEW', 'DESK', 'officer1', '2023-03-13 10:00:00', '2023-03-13 10:00:00'),
       (9002, 'HX-A-2023-009002', 9002, 2, 'AWAITING_HAULIER', 'ENGINEER', 'officer2', '2023-03-13 10:00:00', '2023-03-13 10:00:00'),
       (9003, 'HX-A-2023-009003', 9003, 3, 'LODGED', NULL, NULL, '2023-03-13 10:00:00', '2023-03-13 10:00:00');
