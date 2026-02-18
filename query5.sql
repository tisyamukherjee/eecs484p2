-- SELECT u1.user_id, u1.First_Name, u1.Last_Name, p.photo_id, p.photo_link, p.album_id, p.album_name
-- -- when outputting the pair, u1.user_id < u2.user_id, 
-- FROM project2.Public_Users u1
-- WHERE EXISTS (
--     SELECT u2.user_id
--     FROM project2.Public_Users u2
--     JOIN project2.Public_Tags t1 ON t1.tag_subject_id = u1.user_id
--     JOIN project2.Public_Tags t2 ON t2.tag_subject_id = u2.user_id
--     JOIN project2.Public_Photo p1 ON p1.photo_id = t1.photo_ID AND p1.photo_id = t2.photo_ID
--     WHERE u1.gender = u2.gender
--     AND NOT EXISTS (
--         SELECT(*)
--         FROM project2.Public_Friends f
--         WHERE (u1.user_id = f.user1_id AND u2.user_id = f.user2_id) OR (u1.user_id = f.user2_id AND u2.user_id = f.user1_id)
--     )
--     AND 
-- -- The difference in the two users’ birth years is less than or equal to yearDiff
-- )
-- ORDER BY 
SELECT u1.user_id, u1.First_Name, u1.Last_Name, u2.user_id, u2.First_Name, u2.Last_Name, p.photo_id, p.photo_link, a.album_id, a.album_name
FROM project2.Public_Users u1
JOIN project2.Public_Users u2 ON u1.user_id < u2.user_id
JOIN project2.Public_Tags t1 ON t1.tag_subject_id = u1.user_id 
JOIN project2.Public_Tags t2 ON t2.tag_subject_id = u2.user_id
JOIN project2.Public_Photos p ON p.photo_id = t1.tag_photo_id AND p.photo_id = t2.tag_photo_id
JOIN project2.Public_Albums a ON a.album_id = p.album_id -- join should make sure they're tagged in the same photo
WHERE u1.gender = u2.gender -- users are same gender
AND NOT EXISTS (  -- making sure users are not friends 
    SELECT 1
    FROM project2.Public_Friends f
    WHERE (f.user1_id = u1.user_id AND f.user2_id = u2.user_id) OR (f.user1_id = u2.user_id AND f.user2_id = u1.user_id)
) 
AND (ABS(u1.year_of_birth - u2.year_of_birth) <= 1)
ORDER BY u1.user_id ASC, u2.user_id ASC, p.photo_id ASC;


SELECT p.photo_id, p.photo_link, a.album_id, a.album_name
FROM project2.Public_Tags t1
JOIN project2.Public_Tags t2 ON t2.photo_id = t1.photo_id
JOIN project2.Public_Photos p ON p.photo_id = t1.tag_photo_id
JOIN project2.Public_Albums a ON a.album_id = p.album_id
WHERE u1.user_id = ? AND u2.user_id = ?