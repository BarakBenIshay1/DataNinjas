package com.fitwell.control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.fitwell.entity.DBConst;
import com.fitwell.entity.Trainee;

public class ManagerController {

    private static ManagerController instance;
    private List<PairData> updateMethods;
    private List<PairData> planStatusList;

    public static final int PERSONAL_PLAN = -1;

    private ManagerController() {
        updateMethods = new ArrayList<>();
        planStatusList = new ArrayList<>();
        loadUpdateMethods();
        loadPlanStatus();
    }

    public static ManagerController getInstance() {
        if (instance == null)
            instance = new ManagerController();
        return instance;
    }

    private void loadUpdateMethods() {
        String sql = "SELECT * FROM UpdateMethod";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        PairData pairData = new PairData(id, name);
                        updateMethods.add(pairData);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlanStatus() {
        String sql = "SELECT * FROM PlanStatus";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        PairData pairData = new PairData(id, name);
                        planStatusList.add(pairData);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PairData {
        private int id;
        private String name;

        public PairData(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PairData))
                return false;
            PairData other = (PairData) o;
            return this.id == other.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }

    public List<PairData> getUpdateMethods() {
        return updateMethods;
    }
    public PairData getUpdateMethod(int id){
        for(PairData pd : updateMethods){
            if(pd.getId() == id) return pd;
        }
        return null;
    }

    public List<PairData> getPlanStatusList() {
        return planStatusList;
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

    public List<PairData> getAllPlans() {
        List<PairData> plans = new ArrayList<>();
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT planID, GeneralGuidelines FROM GroupFitnessPlan")) {
                    while (rs.next())
                        plans.add(new PairData(rs.getInt("planID"), rs.getString("GeneralGuidelines")));
                } catch (Exception e) {
                }
                plans.add(new PairData(PERSONAL_PLAN, "Create New Personal Plan"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (date == null)
            return null;
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
                    if (rs.next())
                        return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerTrainee(int id, String fName, String lName, String email,
            String phone, java.sql.Date birthDate, int updateMethod,
            int planId,
            String dietaryRestrictions, String goals, int dietitianId) {

        if (isTraineeExists(id)) {
            return false;
        }

        boolean isGroup = planId != PERSONAL_PLAN;

        java.sql.Date cleanBirthDate = getCleanDate(birthDate);

        Connection conn = null;
        try {
            Class.forName(DBConst.DB_DRIVER);
            conn = DriverManager.getConnection(DBConst.CONN_STR);
            conn.setAutoCommit(true);

            String sqlTrainee = "INSERT INTO TRAINEE (traineeId, firstName, lastName, email, phone, birthDate, preferredUpdateMethodID) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlTrainee)) {
                ps.setInt(1, id);
                ps.setString(2, fName);
                ps.setString(3, lName);
                ps.setString(4, email);
                ps.setString(5, phone);
                ps.setDate(6, cleanBirthDate);
                ps.setInt(7, updateMethod);
                ps.executeUpdate();
            }

            if (isGroup) {
                String sqlGroup = "INSERT INTO GroupPlanTrainee (planId, traineeId) VALUES (?, ?)";
                try (PreparedStatement psLink = conn.prepareStatement(sqlGroup)) {
                    psLink.setInt(1, planId);
                    psLink.setInt(2, id);
                    psLink.executeUpdate();
                    conn.commit();
                }
            } else {
                String newPlanId = generateNextPlanId(conn);
                java.sql.Date cleanToday = getCleanDate(new java.util.Date());

                String sqlFitnessParent = "INSERT INTO FitnessPlan (planId, startDate, durationWeeks, statusID, PlanType) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psParent = conn.prepareStatement(sqlFitnessParent)) {
                    psParent.setString(1, newPlanId);
                    psParent.setDate(2, cleanToday);
                    psParent.setInt(3, 12);
                    psParent.setInt(4, 1); // Active status
                    psParent.setString(5, "Personal");
                    psParent.executeUpdate();
                }

                String sqlPersonal = "INSERT INTO PersonalFitnessPlan (planId, traineeId, dietaryRestrictions, individualGoals, dietitianId) VALUES (?, ?, ?, ?, ?)";
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
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<Object[]> getAllTrainees() {
        List<Object[]> trainees = new ArrayList<>();
        String sql = "SELECT traineeId, firstName, lastName FROM Trainee ORDER BY firstName";
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    trainees.add(new Object[] {
                            rs.getInt("traineeId"),
                            rs.getString("firstName"),
                            rs.getString("lastName")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainees;
    }

    public Trainee getTraineeDetails(int traineeId) {
        String sql = "SELECT firstName, lastName, email, phone, birthDate, preferredUpdateMethodid, isActive FROM Trainee WHERE traineeId = ?";
        Trainee trainee = null;
        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, traineeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String firstName = rs.getString("firstName");
                        String lastName = rs.getString("lastName");
                        String email = rs.getString("email");
                        String phone = rs.getString("phone");
                        LocalDateTime birthDate = rs.getTimestamp("birthDate").toLocalDateTime();
                        int updateMethod = rs.getInt("preferredUpdateMethodid");
                        boolean isActive = rs.getBoolean("isActive");
                        trainee = new Trainee(traineeId, firstName, lastName, birthDate, phone, email, updateMethod,
                                isActive);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainee;
    }

    public boolean updateTraineeDetails(int id, String fName, String lName, String email, String phone,
            java.sql.Date birthDate, int updateMethod, boolean isActive) {
        java.sql.Date BirthDateMidnight = getCleanDate(birthDate);
        String sql = "UPDATE Trainee SET firstName = ?, lastName = ?, email = ?, phone = ?, birthDate = ?, preferredUpdateMethodId = ?, isActive = ? WHERE traineeId = ?";

        try {
            Class.forName(DBConst.DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fName);
                ps.setString(2, lName);
                ps.setString(3, email);
                ps.setString(4, phone);
                ps.setDate(5, BirthDateMidnight);
                ps.setInt(6, updateMethod);
                ps.setBoolean(7, isActive);
                ps.setInt(8, id);

                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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
                    if (rs.next())
                        return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}