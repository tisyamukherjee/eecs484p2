-- Query 2 asks you to identify all of the Fakebook users with no Fakebook friends. 
-- For each user without any friends, report their ID, first name, and last name. 
-- The users should be reported in ascending order by ID. If every Fakebook user has at 
-- least one Fakebook friend, you should return an empty FakebookArrayList.

SELECT u.user_id, u.First_name, u.Last_Name
FROM project2.Public_Users u
WHERE NOT EXISTS (
    SELECT 1
    FROM project2.Public_Friends f 
    WHERE f.user1_id = u.user_id OR f.user2_id = u.user_id
)
ORDER BY u.user_id ASC;