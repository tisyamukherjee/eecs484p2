-- Public: 6 points • Private: 6 points

-- Query 1 asks you to identify information about Fakebook users’ first names.

-- We’d like to know the longest and shortest first names by length. If there are ties 
-- between multiple names, report all tied names in alphabetical order.
-- We’d also like to know what first name(s) are the most common and how many users have 
-- that first name. If there are ties, report all tied names in alphabetical order.
-- Hint: You may consider using the LENGTH() operation in SQL. Remember that you are 
-- allowed to execute multiple SQL statements in one query.


SELECT MIN(LENGTH(u.First_Name)), MAX(LENGTH(u.First_Name))
FROM project2.Public_Users u 
GROUP BY u.First_Name
ORDER BY u.First_Name;