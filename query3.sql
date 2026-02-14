-- Query 3 asks you to identify all of the Fakebook users that no longer live in their 
-- hometown. For each such user, report their ID, first name, and last name. Results should 
-- be sorted in ascending order by the users’ ID. If a user does not have a current city or 
-- a hometown listed, they should not be included in the results. If every Fakebook user 
-- still lives in his/her hometown, you should return an empty FakebookArrayList.

SELECT u.user_id, u.First_name, u.Last_Name
FROM project2.Public_Users u
LEFT JOIN project2.Public_User_Hometown_Cities h ON h.user_id = u.user_id
LEFT JOIN project2.Public_User_Current_Cities c ON c.user_id = u.user_id
WHERE EXISTS (
    SELECT 1
    FROM project2.Public_Users u
    WHERE c.current_city_id <> h.hometown_city_id
)
ORDER BY u.user_ID ASC;