package project2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
 */
public final class StudentFakebookOracle extends FakebookOracle {

    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth "
                    + // select birth months and number of uses with that birth month
                    "FROM " + UsersTable + " "
                    + // from all users
                    "WHERE Month_of_Birth IS NOT NULL "
                    + // for which a birth month is available
                    "GROUP BY Month_of_Birth "
                    + // group into buckets by birth month
                    "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name "
                    + // select ID, first name, and last name
                    "FROM " + UsersTable + " "
                    + // from all users
                    "WHERE Month_of_Birth = " + mostMonth + " "
                    + // born in the most popular birth month
                    "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name "
                    + // select ID, first name, and last name
                    "FROM " + UsersTable + " "
                    + // from all users
                    "WHERE Month_of_Birth = " + leastMonth + " "
                    + // born in the least popular birth month
                    "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
             */
            ResultSet rst = stmt.executeQuery(
                    "SELECT 'Min_Name' AS Type, u.First_Name as Name, null "
                    + "FROM " + UsersTable + " u "
                    + "WHERE LENGTH(u.First_Name) = ("
                    + "SELECT MIN(LENGTH(First_Name)) "
                    + "FROM " + UsersTable
                    + ") "
                    + "UNION "
                    + "SELECT 'Max_Name' AS Type, u2.First_Name as Name, null "
                    + "FROM " + UsersTable + " u2 "
                    + "WHERE (LENGTH(u2.First_Name)) = ( "
                    + "SELECT MAX(LENGTH(First_Name)) "
                    + "FROM " + UsersTable
                    + ") "
                    + "UNION "
                    + "SELECT 'Common_Name' AS Type, u3.First_Name as Name, COUNT(u3.First_Name) as Count "
                    + "FROM " + UsersTable + " u3 "
                    + "GROUP BY u3.First_Name "
                    + "HAVING COUNT(*) = ( "
                    + "SELECT MAX(name_count) "
                    + "FROM ("
                    + "SELECT COUNT(*) AS name_count "
                    + "FROM " + UsersTable + " u4 "
                    + "GROUP BY u4.First_Name "
                    + ") inner_query "
                    + ") "
                    + "ORDER BY Name"
            );
 
            ArrayList<String> longNames = new ArrayList<>();
            ArrayList<String> shortNames = new ArrayList<>();
            ArrayList<String[]> commonNames = new ArrayList<>();

            while (rst.next()) {
                String colName = rst.getString(1);  
                String name = rst.getString(2);   
                if (colName.equals("Min_Name")) {
                    shortNames.add(name);
                } else if (colName.equals("Max_Name")) {
                    longNames.add(name);
                } else { 
                    int count = rst.getInt(3);
                    String string_count = Integer.toString(count);
                    commonNames.add(new String[]{name, string_count});
                }
            }
            FirstNameInfo info = new FirstNameInfo();
            for (String name : longNames) {
                info.addLongName(name);
            }
            for (String name : shortNames) {
                info.addShortName(name);
            }
            for (int i = 0; i < commonNames.size(); ++i) {
                if (i == 0) {
                    int count = Integer.parseInt(commonNames.get(0)[1]);
                    info.setCommonNameCount(count); 
                }
                info.addCommonName(commonNames.get(i)[0]);
            }
            return info; // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
             */
            ResultSet rst = stmt.executeQuery(
                "SELECT u.user_id, u.First_name, u.Last_Name " +
                "FROM " + UsersTable + " u " +
                " WHERE NOT EXISTS (" +
                    "SELECT 1 " +
                    "FROM " + FriendsTable + " f " +
                    " WHERE f.user1_id = u.user_id OR f.user2_id = u.user_id " +
                ") " + 
                " ORDER BY u.user_id ASC"
            );

            while (rst.next()) {
                String first = rst.getString(2);
                String last = rst.getString(3);
                int id = rst.getInt(1); 
                UserInfo u = new UserInfo(id, first, last);
                results.add(u);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
             */
            ResultSet rst = stmt.executeQuery(
                "SELECT u.user_id, u.First_name, u.Last_Name " +
                "FROM " + UsersTable + " u " +
                "LEFT JOIN " + HometownCitiesTable + " h ON h.user_id = u.user_id " +
                "LEFT JOIN " + CurrentCitiesTable + " c ON c.user_id = u.user_id " + 
                "WHERE EXISTS ( " +
                    "SELECT 1 " + 
                    "FROM " + UsersTable + " u " + 
                    "WHERE c.current_city_id <> h.hometown_city_id " +
                ") " +
                "ORDER BY u.user_ID ASC"
            );

            while (rst.next()) {
                String first = rst.getString(2);
                String last = rst.getString(3);
                int id = rst.getInt(1); 
                UserInfo u = new UserInfo(id, first, last);
                results.add(u);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
             */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
             */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
             */

            // create view
            stmt.executeUpdate(
                "CREATE VIEW bidirectional_friends AS " +
                "SELECT f.user1_id AS user1_id, f.user2_id AS user2_id " +
                "FROM " + FriendsTable + " f " +
                "UNION " +
                "SELECT f2.user2_id AS user1_id, f2.user1_id AS user2_id " +
                "FROM " + FriendsTable + " f2"
            );

            ResultSet rst = stmt.executeQuery(
                "WITH mutual_pairs AS ( " +
                "    SELECT b1.user1_id AS user1_id, b2.user1_id AS user2_id, COUNT(*) AS mutual_friend_count " +
                "    FROM bidirectional_friends b1 " +
                "    JOIN bidirectional_friends b2 ON b1.user2_id = b2.user2_id AND b1.user1_id < b2.user1_id " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT 1 FROM bidirectional_friends bf " +
                "        WHERE bf.user1_id = b1.user1_id AND bf.user2_id = b2.user1_id " +
                "    ) " +
                "    GROUP BY b1.user1_id, b2.user1_id " +
                "    ORDER BY mutual_friend_count DESC, b1.user1_id ASC, b2.user1_id ASC " +
                "    FETCH FIRST " + num + " ROWS ONLY " +
                ") " +
                "SELECT mp.user1_id, u1.first_name AS user1_first, u1.last_name AS user1_last, " +
                "       mp.user2_id, u2.first_name AS user2_first, u2.last_name AS user2_last, " +
                "       u3.user_id AS mutualfriend_id, u3.first_name AS mutualfriend_first, u3.last_name AS mutualfriend_last, mp.mutual_friend_count " +
                "FROM mutual_pairs mp " +
                "JOIN bidirectional_friends b ON mp.user1_id = b.user1_id " +
                "JOIN " + UsersTable + " u1 ON mp.user1_id = u1.user_id " +
                "JOIN " + UsersTable + " u2 ON mp.user2_id = u2.user_id " +
                "JOIN " + UsersTable + " u3 ON b.user2_id = u3.user_id " +
                "WHERE EXISTS ( " +
                "    SELECT 1 FROM bidirectional_friends b2 " +
                "    WHERE b2.user1_id = mp.user2_id AND b2.user2_id = b.user2_id " +
                ") " +
                "ORDER BY mp.mutual_friend_count DESC, mp.user1_id ASC, mp.user2_id ASC, u3.user_id ASC"
            );

            long prevU1Id = -1;
            long prevU2Id = -1;

            while (rst.next()) {
                long newU1Id = rst.getLong(1);
                long newU2Id = rst.getLong(4);

                if (!results.isEmpty() && prevU1Id == newU1Id && prevU2Id == newU2Id) {
                    UserInfo mutualFriend = new UserInfo(rst.getLong(7), rst.getString(8), rst.getString(9));
                    UsersPair lastPair = results.get(results.size() - 1);
                    lastPair.addSharedFriend(mutualFriend);
                } else {
                    UserInfo u1 = new UserInfo(newU1Id, rst.getString(2), rst.getString(3));
                    UserInfo u2 = new UserInfo(newU2Id, rst.getString(5), rst.getString(6));
                    UserInfo mutualFriend = new UserInfo(rst.getLong(7), rst.getString(8), rst.getString(9));
                    UsersPair up = new UsersPair(u1, u2);
                    up.addSharedFriend(mutualFriend);
                    results.add(up);
                    prevU1Id = newU1Id;
                    prevU2Id = newU2Id;
                }                
            }
            stmt.executeUpdate("DROP VIEW bidirectional_friends");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
             */
            return new EventStateInfo(-1); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
             */
            ResultSet rst = stmt.executeQuery(
            "WITH user_friends AS ( " +
                "SELECT " +
                    "CASE " +
                        "WHEN f.user1_id = " + userID + " THEN f.user2_id " +
                        "WHEN f.user2_id = " + userID + " THEN f.user1_id " +
                    "END AS user_id " +
                "FROM " + FriendsTable + " f " +
                "WHERE f.user1_id = " + userID + " OR f.user2_id = " + userID + " " +
            ") " +
            "SELECT u.user_id, u.first_name, u.last_name, 'MIN' as type " +
            "FROM user_friends uf " +
            "JOIN " + UsersTable + " u ON uf.user_id = u.user_id " +
            "WHERE (u.year_of_birth, u.month_of_birth, u.day_of_birth) = ( " +
                "SELECT u2.year_of_birth, u2.month_of_birth, u2.day_of_birth " +
                "FROM " + UsersTable + " u2 " +
                "JOIN user_friends uf2 ON u2.user_id = uf2.user_id " +
                "WHERE u2.year_of_birth <> 0 AND u2.month_of_birth <> 0 AND u2.day_of_birth <> 0 " +
                "ORDER BY u2.year_of_birth DESC, u2.month_of_birth DESC, u2.day_of_birth DESC, u2.user_id DESC " +
                "FETCH FIRST 1 ROW ONLY " +
            ") " +
            "UNION ALL " +
            "SELECT u.user_id, u.first_name, u.last_name, 'MAX' as type " +
            "FROM user_friends uf " +
            "JOIN " + UsersTable + " u ON uf.user_id = u.user_id " +
            "WHERE (u.year_of_birth, u.month_of_birth, u.day_of_birth) = ( " +
                "SELECT u2.year_of_birth, u2.month_of_birth, u2.day_of_birth " +
                "FROM " + UsersTable + " u2 " +
                "JOIN user_friends uf2 ON u2.user_id = uf2.user_id " +
                "WHERE u2.year_of_birth <> 0 AND u2.month_of_birth <> 0 AND u2.day_of_birth <> 0 " +
                "ORDER BY u2.year_of_birth ASC, u2.month_of_birth ASC, u2.day_of_birth ASC, u2.user_id ASC " +
                "FETCH FIRST 1 ROW ONLY " +
            ")"
            );

            UserInfo old = null;
            UserInfo young = null;

            while (rst.next()) {
                long id = rst.getLong(1);
                String first = rst.getString(2);
                String last = rst.getString(3);
                String category = rst.getString(4);

                if (category.equals("MAX")) {
                    old = new UserInfo(id, first, last);
                } else {
                    young = new UserInfo(id, first, last);
                }
            }
            return new AgeInfo(old, young); 
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
             */
            ResultSet rst = stmt.executeQuery(
                "SELECT " +
                    "CASE " +
                        "WHEN f.user1_id < f.user2_id THEN f.user1_id " +
                        "ELSE f.user2_id " +
                    "END AS user1_id, " +
                    "CASE " +
                        "WHEN f.user1_id < f.user2_id THEN f.user2_id " +
                        "ELSE f.user1_id " +
                    "END AS user2_id, " +
                    "u.first_name AS user1_first, u.last_name AS user1_last, " +
                    "u2.first_name AS user2_first, u2.last_name AS user2_last " +
                "FROM " + FriendsTable + " f " +
                "JOIN " + UsersTable + " u ON f.user1_id = u.user_id " +
                "JOIN " + HometownCitiesTable + " c ON u.user_id = c.user_id " +
                "JOIN " + UsersTable + " u2 ON f.user2_id = u2.user_id " +
                "JOIN " + HometownCitiesTable + " c2 ON u2.user_id = c2.user_id " +
                "WHERE u.last_name = u2.last_name " +
                    "AND c.hometown_city_id = c2.hometown_city_id " +
                    "AND ABS(u.year_of_birth - u2.year_of_birth) < 10"
            );

            while (rst.next()) {
                long firstId = rst.getLong(1);
                long secondId = rst.getLong(2);
                String first_fname = rst.getString(3);
                String first_lname = rst.getString(4);
                String second_fname = rst.getString(5);
                String second_lname = rst.getString(6); 

                UserInfo u1 = new UserInfo(firstId, first_fname, first_lname);
                UserInfo u2 = new UserInfo(secondId, second_fname, second_lname);
                SiblingInfo s = new SiblingInfo(u1, u2);
                results.add(s);
            }
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
