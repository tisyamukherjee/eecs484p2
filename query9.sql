-- Query 9 asks you to identify pairs of Fakebook users that might be siblings. Two users 
-- might be siblings if they meet each of the following criteria:

-- The two users have the same last name
-- The two users have the same hometown
-- The two users are friends
-- The difference in the two users’ birth years is strictly less than 10 years

-- Each pair should be reported with the smaller user ID first and the larger user ID second. 
-- The smaller ID should be used to order pairs relative to one another (smaller smaller ID first); 
-- the larger ID should be used to break ties (smaller larger ID first).

SELECT 
    CASE 
        WHEN f.user1_id < f.user2_id THEN f.user1_id
        ELSE f.user2_id 
    END AS user1_id,
    CASE 
        WHEN f.user1_id < f.user2_id THEN f.user2_id 
        ELSE f.user1_id 
    END AS user2_id,
    u.first_name AS user1_first, u.last_name AS user1_last, u2.first_name AS user2_first, u2.last_name AS user2_last
FROM project2.Public_Friends f
JOIN project2.Public_Users u ON f.user1_id = u.user_id
JOIN project2.Public_User_Hometown_Cities c on u.user_id = c.user_id
JOIN project2.Public_Users u2 on f.user2_id = u2.user_id
JOIN project2.Public_User_Hometown_Cities c2 on u2.user_id = c2.user_id
WHERE u.last_name = u2.last_name AND c.hometown_city_id = c2.hometown_city_id AND ABS(u.year_of_birth - u2.year_of_birth) < 10;