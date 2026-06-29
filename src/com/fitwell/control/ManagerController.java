package com.fitwell.control;

import com.fitwell.entity.DBConst;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManagerController {

    private static ManagerController instance;

    private ManagerController() {}

    public static ManagerController getInstance() {
        if (instance == null) instance = new ManagerController();
        return instance;
    }

    public boolean isManagerExists(int managerId) {
        String sql = "SELECT managerID FROM StudioManager WHERE managerID = ?";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, managerId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllPlans() {
        List<String> plans = new ArrayList<>();
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT planID FROM GroupPlan")) {
                    while (rs.next()) plans.add(rs.getString("planID") + " (Group)");
                } catch (Exception e) {}
                plans.add("Create New Personal Plan");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return plans;
    }

    public List<String> getAllDietitians() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT dietitianId, firstName, lastName FROM Dietitian";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(rs.getInt("dietitianId") + " - " + 
                             rs.getString("firstName") + " " + rs.getString("lastName"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private String generateNextPlanId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(VAL(planId)) FROM FitnessPlan";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.valueOf(max + 1);
            }
        }
        return "1000";
    }
    
    private java.sql.Date getCleanDate(java.util.Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(cal.getTimeInMillis());
    }


    public boolean isTraineeExists(int id) {
        String sql = "SELECT COUNT(*) FROM Trainee WHERE traineeId = ?";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean registerTrainee(int id, String fName, String lName, String email, 
                                   String phone, java.sql.Date birthDate, String updateMethod, 
                                   String fullPlanString, 
                                   String dietaryRestrictions, String goals, int dietitianId) {
        
        if (isTraineeExists(id)) {
            return false;
        }

        boolean isGroup = fullPlanString.toLowerCase().contains("group");
        String existingPlanId = fullPlanString.split(" ")[0];
        java.sql.Date cleanBirthDate = getCleanDate(birthDate);

        Connection conn = null;
        try {
            Class.forName(DBConst.DB_DRIVER);
            conn = DriverManager.getConnection(DBConst.CONN_STR);
            conn.setAutoCommit(false); 

            String sqlTrainee = "INSERT INTO TRAINEE (traineeId, firstName, lastName, email, phoneNumber, birthDat, preferredUpdateMethod) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlTrainee)) {
                ps.setInt(1, id);
                ps.setString(2, fName);
                ps.setString(3, lName);
                ps.setString(4, email);
                ps.setString(5, phone);
                ps.setDate(6, cleanBirthDate); 
                ps.setString(7, updateMethod); 
                ps.executeUpdate();
            }

            if (isGroup) {
                String sqlGroup = "INSERT INTO GroupPlanMembers (planId, traineeId) VALUES (?, ?)";
                try (PreparedStatement psLink = conn.prepareStatement(sqlGroup)) {
                    psLink.setString(1, existingPlanId);
                    psLink.setInt(2, id);
                    psLink.executeUpdate();
                }
            } else {
                String newPlanId = generateNextPlanId(conn);
                java.sql.Date cleanToday = getCleanDate(new java.util.Date());
                
                String sqlFitnessParent = "INSERT INTO FitnessPlan (planId, startDate, durationInWeeks, status, Type) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psParent = conn.prepareStatement(sqlFitnessParent)) {
                    psParent.setString(1, newPlanId);
                    psParent.setDate(2, cleanToday); 
                    psParent.setInt(3, 12); 
                    psParent.setString(4, "Active");
                    psParent.setString(5, "Personal");
                    psParent.executeUpdate();
                }

                String sqlPersonal = "INSERT INTO PersonalPlan (planId, traineeId, dietaryRestrictions, individualGoals, dietitianId) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psChild = conn.prepareStatement(sqlPersonal)) {
                    psChild.setString(1, newPlanId);
                    psChild.setInt(2, id);
                    psChild.setString(3, dietaryRestrictions);
                    psChild.setString(4, goals);
                    psChild.setInt(5, dietitianId);
                    psChild.executeUpdate();
                }
            }

            conn.commit(); 
            return true;

        } catch (Exception e) {
            e.printStackTrace(); 
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // --- שליפת רשימת כל המתאמנים לטבלה ---
    public List<Object[]> getAllTrainees() {
        List<Object[]> trainees = new ArrayList<>();
        String sql = "SELECT traineeId, firstName, lastName FROM Trainee ORDER BY firstName";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    trainees.add(new Object[]{
                        rs.getInt("traineeId"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return trainees;
    }

    // --- שליפת פרטים מלאים של מתאמן ספציפי (כולל סטטוס פעילות isActive) ---
    public Object[] getTraineeDetails(int traineeId) {
        String sql = "SELECT firstName, lastName, email, phoneNumber, birthDat, preferredUpdateMethod, isActive FROM Trainee WHERE traineeId = ?";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, traineeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Object[]{
                            rs.getString("firstName"), 
                            rs.getString("lastName"),
                            rs.getString("email"), 
                            rs.getString("phoneNumber"),
                            rs.getDate("birthDat"), 
                            rs.getString("preferredUpdateMethod"),
                            rs.getBoolean("isActive") // אינדקס 6
                        };
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // --- עדכון פרטי מתאמן במסד (כולל סטטוס פעילות isActive) ---
    public boolean updateTraineeDetails(int id, String fName, String lName, String email, String phone, java.sql.Date birthDate, String updateMethod, boolean isActive) {
        java.sql.Date BirthDateMidnight = getCleanDate(birthDate);
        String sql = "UPDATE Trainee SET firstName = ?, lastName = ?, email = ?, phoneNumber = ?, birthDat = ?, preferredUpdateMethod = ?, isActive = ? WHERE traineeId = ?";
        
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fName); 
                ps.setString(2, lName);
                ps.setString(3, email); 
                ps.setString(4, phone);
                ps.setDate(5, BirthDateMidnight); 
                ps.setString(6, updateMethod);
                ps.setBoolean(7, isActive); 
                ps.setInt(8, id);
                
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- הוספה חדשה למנהל: בדיקה אם יש למתאמן שיעורים עתידיים פתוחים ---
    public boolean hasFutureRegistrations(int traineeId) {
        String sql = "SELECT COUNT(*) FROM ClassRegistration R " +
                     "INNER JOIN TrainingClass C ON R.classID = C.classId " +
                     "WHERE R.traineeId = ? AND C.startTime > NOW()";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, String.valueOf(traineeId)); // שימוש ב-String כמו במסד
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}