-- Reference data and a few test users and hauliers for the embedded test database.

INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (1, 'NEB', 'Northgate Estuary Bridge', 'CARRYING', 150, NULL, TRUE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (10, 'S08', 'Quarry Lane Viaduct', 'CARRYING', 95, NULL, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (11, 'S09', 'Brindle Street', 'OVERBRIDGE', NULL, 5.05, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (12, 'S10', 'Salt Marsh Viaduct', 'CARRYING', 140, NULL, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (13, 'S11', 'Orchard Way', 'OVERBRIDGE', NULL, 5.90, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (14, 'S12', 'Fenwick Culvert Bridge', 'CARRYING', 250, NULL, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (15, 'S13', 'Gallows Hill', 'OVERBRIDGE', NULL, 5.25, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (16, 'S14', 'Westmere Canal Bridge', 'CARRYING', 80, NULL, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (2, 'SEB', 'Southreach Estuary Bridge', 'CARRYING', 110, NULL, TRUE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (3, 'S01', 'Kelder Lane', 'OVERBRIDGE', NULL, 5.30, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (4, 'S02', 'Mill Reach Viaduct', 'CARRYING', 180, NULL, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (5, 'S03', 'Ashby Road', 'OVERBRIDGE', NULL, 5.10, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (6, 'S04', 'Tarrow Brook Bridge', 'CARRYING', 120, NULL, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (7, 'S05', 'Coldharbour Rail Bridge', 'OVERBRIDGE', NULL, 5.45, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (8, 'S06', 'Penrose Underpass', 'CARRYING', 200, NULL, FALSE);
INSERT INTO structure (id, code, name, structure_type, mass_rating_t, clearance_m, estuary) VALUES (9, 'S07', 'Hatch End Footbridge', 'OVERBRIDGE', NULL, 5.70, FALSE);

INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (1, 'R1', 'J1', 'J4', 'J1 to J4');
INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (2, 'R2', 'J1', 'J9', 'J1 to J9');
INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (3, 'R3', 'J4', 'J9', 'J4 to J9');
INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (4, 'R4', 'J2', 'J6', 'J2 to J6');
INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (5, 'R5', 'J6', 'J9', 'J6 to J9');
INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (6, 'R6', 'J3', 'J8', 'J3 to J8');
INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (7, 'R7', 'J5', 'J8', 'J5 to J8');
INSERT INTO route (id, code, entry_junction, exit_junction, description) VALUES (8, 'R8', 'J1', 'J9', 'J1 to J9');

INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (1, 1, 1, 3);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (10, 3, 3, 13);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (11, 4, 1, 7);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (12, 4, 2, 8);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (13, 4, 3, 9);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (14, 5, 1, 10);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (15, 5, 2, 11);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (16, 5, 3, 12);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (17, 6, 1, 8);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (18, 6, 2, 1);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (19, 6, 3, 14);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (2, 1, 2, 4);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (20, 7, 1, 15);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (21, 7, 2, 2);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (22, 7, 3, 16);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (23, 8, 1, 3);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (24, 8, 2, 4);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (25, 8, 3, 6);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (26, 8, 4, 8);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (27, 8, 5, 10);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (28, 8, 6, 1);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (29, 8, 7, 12);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (3, 1, 3, 5);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (30, 8, 8, 14);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (4, 2, 1, 3);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (5, 2, 2, 4);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (6, 2, 3, 1);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (7, 2, 4, 12);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (8, 3, 1, 6);
INSERT INTO route_segment (id, route_id, seq, structure_id) VALUES (9, 3, 2, 2);

INSERT INTO load_type (id, code, description) VALUES (1, 'WIND_TURBINE_BLADE', 'Wind turbine blade');
INSERT INTO load_type (id, code, description) VALUES (2, 'TRANSFORMER', 'Electrical transformer');
INSERT INTO load_type (id, code, description) VALUES (3, 'MOBILE_CRANE', 'Mobile crane');
INSERT INTO load_type (id, code, description) VALUES (4, 'CONSTRUCTION_PLANT', 'Construction plant');
INSERT INTO load_type (id, code, description) VALUES (5, 'MODULAR_BUILDING', 'Modular building unit');
INSERT INTO load_type (id, code, description) VALUES (6, 'HAZARDOUS_BULK', 'Hazardous bulk load');
INSERT INTO load_type (id, code, description) VALUES (7, 'OTHER', 'Other abnormal load');

INSERT INTO question_set (id, load_type_id, version, active) VALUES (1, 1, 1, TRUE);
INSERT INTO question_set (id, load_type_id, version, active) VALUES (2, 2, 1, TRUE);
INSERT INTO question_set (id, load_type_id, version, active) VALUES (3, 3, 1, TRUE);
INSERT INTO question_set (id, load_type_id, version, active) VALUES (4, 4, 1, TRUE);
INSERT INTO question_set (id, load_type_id, version, active) VALUES (5, 5, 1, TRUE);
INSERT INTO question_set (id, load_type_id, version, active) VALUES (6, 6, 1, TRUE);
INSERT INTO question_set (id, load_type_id, version, active) VALUES (7, 7, 1, TRUE);

INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (1, 1, 1, 'Give the blade length and the rear overhang beyond the trailer in metres.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (10, 3, 2, 'Is the counterweight carried on the crane or on a separate vehicle?', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (11, 3, 3, 'Give the axle loads for each axle.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (12, 3, 4, 'Describe any ballast or attachments carried.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (13, 4, 1, 'Give the make and model of the plant.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (14, 4, 2, 'Describe any attachments removed for transport.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (15, 4, 3, 'Give the name of the site contact at the destination.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (16, 5, 1, 'Give the number of modules moving under this application.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (17, 5, 2, 'Describe the sheeting or weather protection used on the move.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (18, 5, 3, 'Describe any overhanging fixtures such as eaves or balconies.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (19, 6, 1, 'Give the proper shipping name of the substance.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (2, 1, 2, 'Describe how the blade is secured against wind loading while crossing the estuary bridges.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (20, 6, 2, 'Give the name and contact number of the dangerous goods safety adviser.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (21, 6, 3, 'Describe the emergency response arrangements for the move.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (22, 6, 4, 'Describe any segregation from other goods on the vehicle.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (23, 7, 1, 'Describe the load and why it cannot be broken down into smaller loads.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (24, 7, 2, 'Describe the securing arrangements.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (25, 7, 3, 'Give any other information the permit officer should know.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (3, 1, 3, 'Will a steerable rear bogie be used? If so, give the operator''s name.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (4, 1, 4, 'Describe any planned stops on the Halvern network.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (5, 2, 1, 'Give the axle loads for each axle of the vehicle combination.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (6, 2, 2, 'Describe the trailer type and number of axle lines.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (7, 2, 3, 'Is the transformer shipped with oil? State the volume in litres.', TRUE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (8, 2, 4, 'Give the name of the site contact at the destination.', FALSE);
INSERT INTO question (id, question_set_id, seq, question_text, mandatory) VALUES (9, 3, 1, 'Give the crane make and model.', TRUE);

INSERT INTO holiday (id, holiday_date, description) VALUES (73, '2025-01-01', 'New Year''s Day');
INSERT INTO holiday (id, holiday_date, description) VALUES (74, '2025-04-18', 'Good Friday');
INSERT INTO holiday (id, holiday_date, description) VALUES (75, '2025-04-21', 'Easter Monday');
INSERT INTO holiday (id, holiday_date, description) VALUES (76, '2025-05-05', 'Early May bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (77, '2025-05-26', 'Spring bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (78, '2025-08-25', 'Summer bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (79, '2025-12-24', 'Christmas Eve (Halvern company day)');
INSERT INTO holiday (id, holiday_date, description) VALUES (80, '2025-12-25', 'Christmas Day');
INSERT INTO holiday (id, holiday_date, description) VALUES (81, '2025-12-26', 'Boxing Day');
INSERT INTO holiday (id, holiday_date, description) VALUES (82, '2026-01-01', 'New Year''s Day');
INSERT INTO holiday (id, holiday_date, description) VALUES (83, '2026-04-03', 'Good Friday');
INSERT INTO holiday (id, holiday_date, description) VALUES (84, '2026-04-06', 'Easter Monday');
INSERT INTO holiday (id, holiday_date, description) VALUES (85, '2026-05-04', 'Early May bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (86, '2026-05-25', 'Spring bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (87, '2026-08-31', 'Summer bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (88, '2026-12-24', 'Christmas Eve (Halvern company day)');
INSERT INTO holiday (id, holiday_date, description) VALUES (89, '2026-12-25', 'Christmas Day');
INSERT INTO holiday (id, holiday_date, description) VALUES (90, '2026-12-28', 'Boxing Day (substitute day)');
INSERT INTO holiday (id, holiday_date, description) VALUES (91, '2027-01-01', 'New Year''s Day');
INSERT INTO holiday (id, holiday_date, description) VALUES (92, '2027-03-26', 'Good Friday');
INSERT INTO holiday (id, holiday_date, description) VALUES (93, '2027-03-29', 'Easter Monday');
INSERT INTO holiday (id, holiday_date, description) VALUES (94, '2027-05-03', 'Early May bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (95, '2027-05-31', 'Spring bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (96, '2027-08-30', 'Summer bank holiday');
INSERT INTO holiday (id, holiday_date, description) VALUES (97, '2027-12-24', 'Christmas Eve (Halvern company day)');
INSERT INTO holiday (id, holiday_date, description) VALUES (98, '2027-12-27', 'Christmas Day (substitute day)');
INSERT INTO holiday (id, holiday_date, description) VALUES (99, '2027-12-28', 'Boxing Day (substitute day)');

INSERT INTO haulier (id, name, contact_name, contact_email, phone, address_line1, town, postcode, created_at) VALUES (1, 'Marram Heavy Haul', 'Gemma Oakes', 'permits@marram.example', '01632 359710', 'Unit 21, Estuary Park', 'Kelder', 'HV3 9TX', '2017-10-02 09:00:00');
INSERT INTO haulier (id, name, contact_name, contact_email, phone, address_line1, town, postcode, created_at) VALUES (2, 'Tern Marine Logistics', 'Daniel Holcroft', 'permits@tern.example', '01632 926284', 'Unit 39, Quay Street', 'Penrose', 'HV8 1DL', '2017-10-02 09:00:00');
INSERT INTO haulier (id, name, contact_name, contact_email, phone, address_line1, town, postcode, created_at) VALUES (3, 'Brackwater Abnormal Loads', 'Harriet Cardew', 'permits@brackwater.example', '01632 149286', 'Unit 13, Station Yard', 'Millbrook', 'HV8 9WR', '2017-10-02 09:00:00');

INSERT INTO operator_licence (id, haulier_id, licence_number, valid_from, expiry_date) VALUES (1, 1, 'OH5573490', '2015-05-01', '2031-06-25');
INSERT INTO operator_licence (id, haulier_id, licence_number, valid_from, expiry_date) VALUES (2, 2, 'OH4550033', '2015-08-01', '2032-07-05');
INSERT INTO operator_licence (id, haulier_id, licence_number, valid_from, expiry_date) VALUES (3, 3, 'OH3031868', '2015-03-01', '2031-09-21');

INSERT INTO insurance_policy (id, haulier_id, insurer, policy_number, start_date, end_date) VALUES (1, 1, 'Harrow Mutual Insurance', 'HMV-754495', '2017-01-01', '2030-12-31');
INSERT INTO insurance_policy (id, haulier_id, insurer, policy_number, start_date, end_date) VALUES (2, 2, 'Beacon Fleet Cover', 'HMV-548421', '2017-01-01', '2029-12-31');
INSERT INTO insurance_policy (id, haulier_id, insurer, policy_number, start_date, end_date) VALUES (3, 3, 'Stanegate Commercial', 'HMV-969272', '2017-01-01', '2030-12-31');

INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (1, 1, 'VS15 NDX', 'MOBILE_CRANE');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (121, 1, 'KV66 CZV', 'BALLAST_TRACTOR');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (122, 2, 'KB65 TCU', 'RIGID');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (123, 3, 'BX15 ZTH', 'ARTIC');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (2, 2, 'EE65 KTO', 'ARTIC');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (3, 3, 'RY65 TOC', 'RIGID');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (41, 1, 'RC15 NWN', 'MODULAR_TRAILER');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (42, 2, 'KR16 CXC', 'ARTIC');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (43, 3, 'VO17 NPT', 'BALLAST_TRACTOR');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (81, 1, 'LH15 GWC', 'ARTIC');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (82, 2, 'KD65 TAU', 'MOBILE_CRANE');
INSERT INTO vehicle (id, haulier_id, registration, category) VALUES (83, 3, 'NU65 NLP', 'BALLAST_TRACTOR');

INSERT INTO app_user (id, username, password_hash, full_name, email, enabled, created_at, is_panel_chair) VALUES (1, 'officer1', '$2a$10$Ba0KNKPQjIuUK9COOUXM2.Zqbq6I/VzukjVC9m2k0xG44qmC5ceIu', 'Alan Ashworth', 'alan.ashworth@halvern-crossings.example', TRUE, '2017-09-04 09:00:00', FALSE);
INSERT INTO app_user (id, username, password_hash, full_name, email, enabled, created_at, is_panel_chair) VALUES (13, 'engineer1', '$2a$10$Ba0KNKPQjIuUK9COOUXM2.Zqbq6I/VzukjVC9m2k0xG44qmC5ceIu', 'Megan Marwood', 'megan.marwood@halvern-crossings.example', TRUE, '2017-09-04 09:00:00', FALSE);
INSERT INTO app_user (id, username, password_hash, full_name, email, enabled, created_at, is_panel_chair) VALUES (19, 'chair1', '$2a$10$Ba0KNKPQjIuUK9COOUXM2.Zqbq6I/VzukjVC9m2k0xG44qmC5ceIu', 'Tobias Stannard', 'tobias.stannard@halvern-crossings.example', TRUE, '2017-09-04 09:00:00', TRUE);
INSERT INTO app_user (id, username, password_hash, full_name, email, enabled, created_at, is_panel_chair) VALUES (21, 'admin1', '$2a$10$Ba0KNKPQjIuUK9COOUXM2.Zqbq6I/VzukjVC9m2k0xG44qmC5ceIu', 'Victor Upton', 'victor.upton@halvern-crossings.example', TRUE, '2017-09-04 09:00:00', FALSE);

INSERT INTO user_role (user_id, role) VALUES (1, 'OFFICER');
INSERT INTO user_role (user_id, role) VALUES (13, 'ENGINEER');
INSERT INTO user_role (user_id, role) VALUES (19, 'OFFICER');
INSERT INTO user_role (user_id, role) VALUES (21, 'ADMIN');

-- Identity columns continue after the ids above
ALTER TABLE structure ALTER COLUMN id RESTART WITH 1016;
ALTER TABLE route ALTER COLUMN id RESTART WITH 1008;
ALTER TABLE route_segment ALTER COLUMN id RESTART WITH 1030;
ALTER TABLE load_type ALTER COLUMN id RESTART WITH 1007;
ALTER TABLE question_set ALTER COLUMN id RESTART WITH 1007;
ALTER TABLE question ALTER COLUMN id RESTART WITH 1025;
ALTER TABLE holiday ALTER COLUMN id RESTART WITH 1099;
ALTER TABLE haulier ALTER COLUMN id RESTART WITH 1003;
ALTER TABLE operator_licence ALTER COLUMN id RESTART WITH 1003;
ALTER TABLE insurance_policy ALTER COLUMN id RESTART WITH 1003;
ALTER TABLE vehicle ALTER COLUMN id RESTART WITH 1123;
ALTER TABLE app_user ALTER COLUMN id RESTART WITH 1021;

-- Partner API test keys (PI-16)
INSERT INTO api_key (haulier_id, api_key, label, active, created_at) VALUES (1, 'HXPK-test0000000000000000000000000001', 'Test key 1', TRUE, '2021-04-12 09:00:00');
INSERT INTO api_key (haulier_id, api_key, label, active, created_at) VALUES (2, 'HXPK-test0000000000000000000000000002', 'Test key 2', TRUE, '2021-04-12 09:00:00');

-- The rows above set their own ids, so move each identity past them
SELECT setval(pg_get_serial_sequence('structure', 'id'), (SELECT MAX(id) FROM structure));
SELECT setval(pg_get_serial_sequence('route', 'id'), (SELECT MAX(id) FROM route));
SELECT setval(pg_get_serial_sequence('route_segment', 'id'), (SELECT MAX(id) FROM route_segment));
SELECT setval(pg_get_serial_sequence('load_type', 'id'), (SELECT MAX(id) FROM load_type));
SELECT setval(pg_get_serial_sequence('holiday', 'id'), (SELECT MAX(id) FROM holiday));
SELECT setval(pg_get_serial_sequence('haulier', 'id'), (SELECT MAX(id) FROM haulier));
SELECT setval(pg_get_serial_sequence('operator_licence', 'id'), (SELECT MAX(id) FROM operator_licence));
SELECT setval(pg_get_serial_sequence('insurance_policy', 'id'), (SELECT MAX(id) FROM insurance_policy));
SELECT setval(pg_get_serial_sequence('vehicle', 'id'), (SELECT MAX(id) FROM vehicle));
SELECT setval(pg_get_serial_sequence('app_user', 'id'), (SELECT MAX(id) FROM app_user));
SELECT setval(pg_get_serial_sequence('question_set', 'id'), (SELECT MAX(id) FROM question_set));
SELECT setval(pg_get_serial_sequence('question', 'id'), (SELECT MAX(id) FROM question));
