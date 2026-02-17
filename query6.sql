-- Query 6 asks you to suggest possible unrealized Fakebook friendships in a different way. 
-- We will pass a single integer argument, num, to the query function; you should return the 
-- top num pairs of Fakebook users with the most mutual friends who are not friends themselves. 
-- If there are fewer than num pairs, then you should return only those available pairs.

-- A mutual friend is one such that A is friends with B and B is friends with C, in which case 
-- B is a mutual friend of A and C. The IDs, first names, and last names of the two users who 
-- share a mutual friend should be returned; list the user with the smaller ID first and larger 
-- ID second within the pair and rank the pairs in descending order by the number of mutual 
-- friends. In the event of a tie between pairs, list the pair with the smaller first ID 
-- before the pair with the larger first ID; if pairs are still tied, list the pair with the 
-- smaller second ID before the pair with the larger second ID.

-- For each pair of users you report, you should also list the IDs, first names, and last names 
-- of all their mutual friends. List the mutual friends in ascending order by ID.

-- Hint: Remember that the friends table contains one direction of user IDs for each friendship. 
-- Consider creating a bidirectional friendship view.

CREATE VIEW bidirectional_friends AS
SELECT f.user1_id AS user1_id, f.user2_id AS user2_id
FROM project2.Public_Friends f
UNION
SELECT f2.user2_id AS user1_id, f2.user1_id AS user2_id
FROM project2.Public_Friends f2;

WITH mutual_pairs AS (
    SELECT b1.user1_id AS user1_id, b2.user1_id AS user2_id, COUNT(*) AS mutual_friend_count
    FROM bidirectional_friends b1
    JOIN bidirectional_friends b2 ON b1.user2_id = b2.user2_id AND b1.user1_id < b2.user1_id
    WHERE NOT EXISTS (
        SELECT 1 FROM bidirectional_friends b3
        WHERE b3.user1_id = b1.user1_id AND b3.user2_id = b2.user1_id
    )
    GROUP BY b1.user1_id, b2.user1_id         
    ORDER BY mutual_friend_count DESC, b1.user1_id ASC, b2.user1_id ASC
    FETCH FIRST 5 ROWS ONLY
) 
SELECT mp.user1_id, u1.first_name AS user1_first, u1.last_name AS user1_last, mp.user2_id, u2.first_name AS user2_first, u2.last_name AS user2_last, u3.user_id AS mutualfriend_id, u3.first_name AS mutualfriend_first, u3.last_name AS mutualfriend_last, mp.mutual_friend_count
FROM mutual_pairs mp 
JOIN bidirectional_friends b ON mp.user1_id = b.user1_id
JOIN project2.Public_Users u1 ON mp.user1_id = u1.user_id
JOIN project2.Public_Users u2 ON mp.user2_id = u2.user_id
JOIN project2.Public_Users u3 ON b.user2_id = u3.user_id
WHERE EXISTS (
    SELECT 1 FROM bidirectional_friends b2
    WHERE b2.user1_id = mp.user2_id AND b2.user2_id = b.user2_id
)
ORDER BY mp.mutual_friend_count DESC, mp.user1_id ASC, mp.user2_id ASC, u3.user_id ASC;

DROP VIEW bidirectional_friends;