INSERT INTO app.users (username, full_name, email)
VALUES ('ivanova', 'Иванова Анна', 'ivanova@museum.ru'),
       ('petrov', 'Петров Олег', 'petrov@museum.ru'),
       ('smirnova', 'Смирнова Мария', NULL);

INSERT INTO app.exhibition_halls (name, area, available)
VALUES ('Большой зал', 120, true),
       ('Малый зал', 40, true),
       ('Зал 3', 60, false);

INSERT INTO app.exhibits (name, exhibit_condition, required_area)
VALUES ('Амфора', 'GOOD', 4),
       ('Меч викингов', 'GOOD', 2.5),
       ('Икона XVI века', 'NEEDS_RESTORATION', 3),
       ('Скелет мамонта', 'GOOD', 35),
       ('Картина Айвазовского', 'GOOD', 6);

INSERT INTO app.exhibitions (name, start_date, end_date, status, curator_id)
VALUES ('Древний мир', '2026-11-01', '2026-12-31', 'PLANNED', 1),
       ('Морской пейзаж', '2026-10-01', '2026-11-30', 'OPEN', 2),
       ('Средневековье', '2026-12-01', '2027-01-31', 'PLANNED', NULL);

INSERT INTO app.exhibit_placements (exhibit_id, exhibition_id, hall_id, mounting_status)
VALUES (1, 1, 1, 'PLANNED'),
       (4, 1, 1, 'PLANNED'),
       (5, 2, 2, 'MOUNTED');
