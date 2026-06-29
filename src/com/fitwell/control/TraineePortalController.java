package com.fitwell.control;

import static com.fitwell.entity.DBConst.CONN_STR;
import static com.fitwell.entity.DBConst.DB_DRIVER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fitwell.entity.TrainingClass;

public class TraineePortalController {

    private static TraineePortalController instance;
    private Map<Integer, String> classTypeMap;

    private TraineePortalController() {
        classTypeMap = new HashMap<>();
        loadClassType();
    }

    private void loadClassType(){
        String sql = "SELECT * FROM ClassType";
        try {
            Class.forName(DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                try (ResultSet rs = ps.executeQuery()) {
                    while(rs.next()){
                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        classTypeMap.put(id, name);
                    }
                    
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public String getClassTypeName(int classTypeId){
        return classTypeMap.get(classTypeId);
    }
    public static TraineePortalController getInstance() {
        if (instance == null) instance = new TraineePortalController();
        return instance;
    }


    public static class PlanInfo {
        private final String planId;
        private final String planType;

        public PlanInfo(String planId, String planType) {
            this.planId = planId;
            this.planType = planType;
        }
        public String getPlanId() { return planId; }
        public String getPlanType() { return planType; }
    }

    public boolean traineeExists(String traineeId) {
        String sql = "SELECT COUNT(*) FROM Trainee WHERE traineeId = ?";
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, traineeId); 
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public String getTraineeFullName(String traineeId) {
        String sql = "SELECT firstName, lastName FROM Trainee WHERE traineeID = ?";
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, traineeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return (rs.getString(1) + " " + rs.getString(2));
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }


    public boolean unsubscribeTrainee(String traineeId) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(CONN_STR)) {
                
                String sqlDeactivate = "UPDATE Trainee SET isActive = FALSE WHERE traineeId = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlDeactivate)) {
                    ps.setString(1, traineeId);
                    ps.executeUpdate();
                }

                String sqlDeleteRegs = "DELETE FROM ClassRegistration WHERE traineeId = ? " +
                                       "AND classID IN (SELECT classId FROM TrainingClass WHERE startDateTime > NOW())";
                try (PreparedStatement ps = conn.prepareStatement(sqlDeleteRegs)) {
                    ps.setString(1, traineeId);
                    ps.executeUpdate();
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 
    }

    public void registerToClass(String traineeIdStr, int classId) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(CONN_STR)) {
                
                if (isTraineeRegistered(classId, traineeIdStr, conn)) {
                    throw new IllegalStateException("You are already registered to this class.");
                }

                if (hasTimeConflict(conn, traineeIdStr, classId)) {
                    throw new IllegalStateException("You are already registered for another class at this time!");
                }

                String getClassSql = "SELECT C.startDateTime, C.maxParticipants, C.planId, F.Type, " +
                                     "(SELECT COUNT(*) FROM ClassRegistration R WHERE R.classID = C.classId) AS currentRegs " +
                                     "FROM TrainingClass C INNER JOIN FitnessPlan F ON C.planId = F.planId " +
                                     "WHERE C.classId = ?";
                                     
                Timestamp startTs = null;
                int maxCapacity = 0;
                int currentRegs = 0;
                String planId = null;
                String planType = null;
                
                try (PreparedStatement ps = conn.prepareStatement(getClassSql)) {
                    ps.setInt(1, classId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Class not found.");
                        }
                        startTs = rs.getTimestamp("startDateTime");
                        maxCapacity = rs.getInt("maxParticipants");
                        currentRegs = rs.getInt("currentRegs");
                        planId = rs.getString("planId");
                        planType = rs.getString("Type");
                    }
                }

                if (!isTraineeInPlan(conn, traineeIdStr, planId, planType)) {
                    throw new IllegalStateException("Error: You are not enrolled in the training plan associated with this class.");
                }

                int seatsLeft = maxCapacity - currentRegs;
                if (seatsLeft <= 0) {
                    throw new IllegalStateException("Class is full.");
                }

                LocalDateTime start = startTs.toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                if (now.plusHours(24).isAfter(start)) {
                    throw new IllegalStateException("Registration closed (Must register at least 24h in advance).");
                }
                
                if (now.isAfter(start)) {
                    throw new IllegalStateException("Class has already started.");
                }

                String insertRegSql = "INSERT INTO ClassRegistration (classID, traineeId, registrationTime) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertRegSql)) {
                    ps.setInt(1, classId);
                    ps.setString(2, traineeIdStr);
                    ps.setTimestamp(3, Timestamp.valueOf(now));
                    ps.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public void cancelRegistration(String traineeIdStr, int classId) {
        String deleteSql = "DELETE FROM ClassRegistration WHERE classID = ? AND traineeId = ?";
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(CONN_STR)) {
                int rows = 0;
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, classId);
                    ps.setString(2, traineeIdStr);
                    rows = ps.executeUpdate();
                }
                if (rows == 0) {
                    throw new IllegalStateException("Cancellation failed. Registration not found.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Cancellation failed: " + e.getMessage(), e);
        }
    }

    // =========================
    // HELPERS & CALENDAR... (שאר הפונקציות ללא שינוי)
    // =========================
    private boolean isTraineeInPlan(Connection conn, String traineeId, String planId, String planType) throws SQLException {
        String sql = "";
        if ("Group".equalsIgnoreCase(planType)) {
            sql = "SELECT COUNT(*) FROM GroupPlanMembers WHERE planId = ? AND traineeId = ?";
        } else if ("Personal".equalsIgnoreCase(planType)) {
            sql = "SELECT COUNT(*) FROM PersonalPlan WHERE planId = ? AND traineeId = ?";
        } else {
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, planId);
            ps.setString(2, traineeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private boolean isTraineeRegistered(int classId, String traineeId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ClassRegistration WHERE classID = ? AND traineeId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setString(2, traineeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private boolean hasTimeConflict(Connection conn, String traineeId, int newClassId) throws SQLException {
        Timestamp newStart = null, newEnd = null;
        String sqlTimes = "SELECT startDateTime, endDateTime FROM TrainingClass WHERE classId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlTimes)) {
            ps.setInt(1, newClassId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    newStart = rs.getTimestamp("startDateTime");
                    newEnd = rs.getTimestamp("endDateTime");
                }
            }
        }
        if (newStart == null) return false;

        String sqlCheck = "SELECT COUNT(*) FROM ClassRegistration R " +
                          "JOIN TrainingClass C ON R.classID = C.classId " +
                          "WHERE R.traineeId = ? AND C.startDateTime < ? AND C.endDateTime > ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, traineeId);
            ps.setTimestamp(2, newEnd);
            ps.setTimestamp(3, newStart);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<TrainingClass> getClassesForMonth(int month, int year, String traineeId) {
        List<TrainingClass> classes = new ArrayList<>();
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        String sql = "SELECT C.ClassID, C.Name, C.StartDateTime, C.EndDateTime, C.ClassTypeId, C.MaxParticipants, " +
                     "(C.MaxParticipants - (SELECT COUNT(*) FROM ClassRegistration R WHERE R.classID = C.classId)) AS seatsLeft " +
                     "FROM TrainingClass C " +
                     "WHERE C.status != 'Cancelled' " +
                     "AND C.startDateTime >= ? AND C.startDateTime < ? " +
                     "AND (C.planId IN (SELECT planId FROM PersonalFitnessPlan WHERE traineeId = ?) " +
                     "OR C.planId IN (SELECT planId FROM GroupPlanTrainee WHERE traineeId = ?)) " +
                     "ORDER BY C.startDateTime ASC";

        try {
            Class.forName(DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(startOfMonth));
                ps.setTimestamp(2, Timestamp.valueOf(endOfMonth));
                ps.setString(3, traineeId);
                ps.setString(4, traineeId);
                TrainingClass trainingClass = null;
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        
                            int classId = rs.getInt("ClassID");
                            String name = rs.getString("Name");
                            LocalDateTime start = rs.getTimestamp("startDateTime").toLocalDateTime();
                            LocalDateTime end = rs.getTimestamp("endDateTime").toLocalDateTime();
                            int classType = rs.getInt("ClassTypeId");
                            int maxParticipants = rs.getInt("MaxParticipants");
                            int seatsLeft = rs.getInt("seatsLeft");
                            trainingClass = new TrainingClass(classId, name, start, end, classType,maxParticipants, seatsLeft);
                            classes.add(trainingClass);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return classes;
    }

    public boolean isTraineeRegistered(int classId, String traineeId) {
        try {
            Class.forName(DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(CONN_STR)) {
                return isTraineeRegistered(classId, traineeId, conn);
            }
        } catch (Exception e) { return false; }
    }

    public int[] getMonthlyStats(String traineeIdStr) {
        int[] stats = {0, 0}; 
        String sql = "SELECT C.startDateTime, C.endDateTime FROM ClassRegistration R " +
                     "INNER JOIN TrainingClass C ON R.classID = C.classId " +
                     "WHERE R.traineeId = ? AND MONTH(C.startDateTime) = MONTH(NOW()) AND YEAR(C.startDateTime) = YEAR(NOW())";
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) { // <--- התיקון הקטן נמצא ממש כאן (הוספנו conn.)
                ps.setString(1, traineeIdStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        stats[0]++;
                        Timestamp start = rs.getTimestamp("startDateTime");
                        Timestamp end = rs.getTimestamp("endDateTime");
                        if (start != null && end != null) {
                            long diff = end.getTime() - start.getTime();
                            stats[1] += (int) (diff / (1000 * 60));
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return stats;
    }

    public List<String> getTipsForClass(int classId) {
        List<String> tips = new ArrayList<>();
        String sql = "SELECT content FROM Tip WHERE classID = ?";
        try {
            Class.forName(DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tips.add(rs.getString("content"));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tips;
    }
}