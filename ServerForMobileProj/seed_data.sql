-- Найдите ID пользователя test
SELECT id FROM users WHERE username = 'test3';

-- Заполните расписание для понедельника (полностью занятого дня)
INSERT INTO schedule (user_id, date, description) VALUES
(1, '2025-01-20', 'Meeting at 9:00'),
(1, '2025-01-20', 'Lunch at 12:00'),
(1, '2025-01-20', 'Project review at 15:00');

-- Заполните расписание для вторника (частично занятого дня)
INSERT INTO schedule (user_id, date, description) VALUES
(1, '2025-01-21', 'Team standup at 10:00');

-- Среда и четверг оставлены пустыми

-- Заполните пятницу (частично занятого дня)
INSERT INTO schedule (user_id, date, description) VALUES
(1, '2025-01-24', 'Weekly sync at 11:00');