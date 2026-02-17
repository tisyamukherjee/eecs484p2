-- Query 8 asks you to identify the oldest and youngest friend of a particular Fakebook user. 
-- We will pass a single integer argument, userID, to the query function; you should return 
-- the ID, first name, and last name of the oldest and youngest friend of the Fakebook user 
-- with that ID. Notice that you should not type convert the date, month and year fields 
-- using TO_DATE; instead, order them just as they are (numbers). If two friends of the user
-- passed as the argument are born on the exact same date, report the one with the larger 
-- user ID. You can assume that the user with the specified ID has at least 1 Fakebook friend.

WITH user_friends AS (
    SELECT 
        CASE
            WHEN f.user1_id = 215 THEN f.user2_id
            WHEN f.user2_id = 215 THEN f.user1_id
        END AS user_id
    FROM project2.Public_Friends f
    WHERE f.user1_id = 215 OR f.user2_id = 215
) 
SELECT u.user_id, u.first_name, u.last_name, u.year_of_birth, 'MIN' as type
FROM user_friends uf
JOIN project2.Public_Users u ON uf.user_id = u.user_id
WHERE (u.year_of_birth, u.month_of_birth, u.day_of_birth) = (
    SELECT u2.year_of_birth, u2.month_of_birth, u2.day_of_birth
    FROM project2.Public_Users u2
    JOIN user_friends uf2 ON u2.user_id = uf2.user_id 
    WHERE u2.year_of_birth <> 0 AND u2.month_of_birth <> 0 AND u2.day_of_birth <> 0
    ORDER BY u2.year_of_birth DESC, u2.month_of_birth DESC, u2.day_of_birth DESC, u2.user_id ASC
    FETCH FIRST 1 ROW ONLY
)
UNION ALL
SELECT u.user_id, u.first_name, u.last_name, u.year_of_birth, 'MAX' as type
FROM user_friends uf
JOIN project2.Public_Users u ON uf.user_id = u.user_id
WHERE (u.year_of_birth, u.month_of_birth, u.day_of_birth) = (
    SELECT u2.year_of_birth, u2.month_of_birth, u2.day_of_birth
    FROM project2.Public_Users u2
    JOIN user_friends uf2 ON u2.user_id = uf2.user_id
    WHERE u2.year_of_birth <> 0 AND u2.month_of_birth <> 0 AND u2.day_of_birth <> 0
    ORDER BY u2.year_of_birth ASC, u2.month_of_birth ASC, u2.day_of_birth ASC, u2.user_id ASC
    FETCH FIRST 1 ROW ONLY
);