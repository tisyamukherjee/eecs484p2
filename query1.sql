-- Public: 6 points • Private: 6 points

-- Query 1 asks you to identify information about Fakebook users’ first names.

-- We’d like to know the longest and shortest first names by length. If there are ties 
-- between multiple names, report all tied names in alphabetical order.
-- We’d also like to know what first name(s) are the most common and how many users have 
-- that first name. If there are ties, report all tied names in alphabetical order.
-- Hint: You may consider using the LENGTH() operation in SQL. Remember that you are 
-- allowed to execute multiple SQL statements in one query.

SELECT 'Min_Name' AS Type, u.First_Name as Name, null
FROM project2.Public_Users u 
WHERE LENGTH(u.First_Name) = (
    SELECT MIN(LENGTH(First_Name))
    FROM project2.Public_Users
)
UNION  
SELECT 'Max_Name' AS Type, u2.First_Name as Name, null
FROM project2.Public_Users u2
WHERE (LENGTH(u2.First_Name)) = (
    SELECT MAX(LENGTH(First_Name))
    FROM project2.Public_Users
)
UNION
SELECT 'Common_Name' AS Type, u3.First_Name as Name, COUNT(u3.First_Name) as Count
FROM project2.Public_Users u3
GROUP BY u3.First_Name
HAVING COUNT(*) = (
    SELECT MAX(name_count)
    FROM (
        SELECT COUNT(*) AS name_count
        FROM project2.Public_Users u4
        GROUP BY u4.First_Name
    )
)
ORDER BY Name;