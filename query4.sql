-- Query 4 asks you to identify the most highly-tagged photos. We will pass an integer argument 
-- num to the query function; you should return the top num photos with the most tagged users 
-- sorted in descending order by the number of tagged users (most tagged users first). If 
-- there are fewer than num photos with at least 1 tag, then you should return only those 
-- available photos. If more than one photo has the same number of tagged users, list the 
-- photo with the smaller ID first.

-- For each photo, you should report the photo’s ID, the ID of the album containing the photo, 
-- the photo’s Fakebook link, and the name of the album containing the photo. For each reported 
-- photo, you should list the ID, first name, and last name of the users tagged in that photo. 
-- Tagged users should be listed in ascending order by ID.

-- SELECT p.photo_id, p.album_id, p.photo_link, a.album_name
-- FROM project2.Public_Photos p 
-- JOIN project2.Public_Albums a ON a.album_id = p.album_id
-- WHERE EXISTS (
--     SELECT 1
--     FROM project2.Public_Tags t
--     WHERE p.photo_id = t.tag_photo_id
-- )
-- GROUP BY p.photo_id
-- ORDER BY COUNT(p.photo_id) DESC;


SELECT p2.photo_id, p2.album_id, p2.photo_link, a.album_name, u.user_id, u.first_name, u.last_name
FROM (
    SELECT p.photo_id
    FROM project2.Public_Photos p 
    JOIN project2.Public_Tags t on p.photo_id = t.tag_photo_id
    GROUP BY p.photo_id
    ORDER BY COUNT(*) DESC
) tagged_photos
JOIN project2.Public_Photos p2 on tagged_photos.photo_id = p2.photo_id
JOIN project2.Public_Albums a ON a.album_id = p2.album_id
JOIN project2.Public_Tags t2 ON t2.tag_photo_id = p2.photo_id
JOIN project2.Public_Users u ON t2.tag_subject_id = u.user_id
FETCH FIRST 5 ROWS ONLY; 
