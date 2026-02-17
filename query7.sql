-- Query 7 asks you to identify the states in which the most Fakebook events are held. 
-- If more than one state is tied for hosting the most Fakebook events, 
-- all states involved in the tie should be returned, listed in ascending order by state name. 
-- You also need to report how many events are held in those state(s).
--  You can assume that there is at least 1 Fakebook event.

SELECT c.state_name, COUNT(*) AS num_events
FROM project2.Public_User_Events e
JOIN project2.Public_Cities c ON e.event_city_id = c.city_id
GROUP BY c.state_name
HAVING COUNT(*) = (
    SELECT MAX(event_count)
    FROM (
        SELECT COUNT(*) AS event_count
        FROM project2.Public_User_Events e2
        JOIN project2.Public_Cities c2 ON e2.event_city_id = c2.city_id
        GROUP BY c2.state_name
    )
)
ORDER BY c.state_name ASC;