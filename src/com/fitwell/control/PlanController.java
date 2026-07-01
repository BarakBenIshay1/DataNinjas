package com.fitwell.control;

import static com.fitwell.entity.DBConst.DB_DRIVER;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fitwell.entity.DBConst;
import com.fitwell.entity.Trainee;

public class PlanController {

    private static PlanController instance;

    public static final int PLAN_STATUS_ACTIVE = 1;
    public static final int PLAN_STATUS_CANCELLED = 4;
    public static final int PLAN_STATUS_COMPLETED = 3;
    public static final String PLAN_TYPE_PERSONAL = "Personal";
    public static final String PLAN_TYPE_GROUP = "Group";

    private List<PlanStatus> planStatusList;
    private Map<Integer, PlanStatus> planStatusMap;

    private PlanController() {
        planStatusList = new ArrayList<>();
        planStatusMap = new HashMap<>();
        LoadPlanStatus();
    }

    public static PlanController getInstance() {
        if (instance == null)
            instance = new PlanController();
        return instance;
    }

    private void LoadPlanStatus() {
        String sql = "SELECT planStatusId, Name from PlanStatus";
        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                PlanStatus planStatus = new PlanStatus(id, name);
                planStatusList.add(planStatus);
                planStatusMap.put(id, planStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<PlanStatus> getPlanStatusList() {
        return planStatusList;
    }

    // ---------- PlanId generation (safe-ish for Access TEXT ids) ----------
    // private String generateNextPlanId(Connection conn) throws SQLException {
    // String sql = "SELECT MAX(VAL(planId)) FROM FitnessPlan";
    // try (Statement stmt = conn.createStatement();
    // ResultSet rs = stmt.executeQuery(sql)) {
    // if (rs.next()) {
    // int max = rs.getInt(1);
    // if (max > 0)
    // return String.valueOf(max + 1);
    // }
    // }
    // return "1000";
    // }

    // ---------- Dropdowns ----------

    // ===== תיקון 1: רק מתאמנים פעילים יכולים לקבל תוכנית אישית חדשה =====
    public List<String> getAllTraineesForDropdown() {
        List<String> list = new ArrayList<>();

        String sql = "SELECT traineeId, firstName, lastName FROM Trainee WHERE isActive = true ORDER BY traineeId";
        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getInt("traineeId") + " - " + rs.getString("firstName") + " " + rs.getString("lastName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getAllDietitiansForDropdown() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT dietitianId, firstName, lastName FROM Dietitian ORDER BY dietitianId";
        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getInt("dietitianId") + " - " + rs.getString("firstName") + " " + rs.getString("lastName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getAllGroupPlansForDropdown() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT planId FROM GroupFitnessPlan ORDER BY VAL(planId)";
        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String planId = rs.getString("planId");
                list.add(planId + " (Group)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PlanView> getAllPlansForDropdown() {
        List<PlanView> list = new ArrayList<>();
        String sql = "SELECT planId, planType, statusId FROM FitnessPlan ORDER BY VAL(planId)";
        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int planId = rs.getInt("planId");
                String type = rs.getString("planType");
                int statusId = rs.getInt("statusId");
                if (type == null)
                    type = "Unknown";
                String status = "N/A";
                if(planStatusMap.containsKey(statusId)) {
                	status =  planStatusMap.get(statusId).getName();
                }
                list.add(new PlanView(planId, type, status));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public int createPersonalPlan(int traineeId, Date startDate, int durationWeeks,
            String goals, String dietaryRestrictions, int dietitianId) throws Exception {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DBConst.CONN_STR);
            conn.setAutoCommit(false);
            int generatedClassId = -1;
            String sqlFitness = "INSERT INTO FitnessPlan (startDate, durationWeeks, statusid, planType) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlFitness, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, startDate);
                ps.setInt(2, durationWeeks);
                ps.setInt(3, PLAN_STATUS_ACTIVE);
                ps.setString(4, PLAN_TYPE_PERSONAL);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedClassId = rs.getInt(1);
                } else {
                    throw new IllegalArgumentException("Error creating class with id " + generatedClassId);
                }
            }

            String sqlPersonal = "INSERT INTO PersonalFitnessPlan (planId, traineeId, dietaryRestrictions, individualGoals, dietitianId) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlPersonal)) {
                ps.setInt(1, generatedClassId);
                ps.setInt(2, traineeId);
                ps.setString(3, dietaryRestrictions);
                ps.setString(4, goals);
                ps.setInt(5, dietitianId);
                ps.executeUpdate();
            }

            conn.commit();
            return generatedClassId;

        } catch (Exception e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            throw e;
        } finally {
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
        }
    }

    public int createGroupPlan(int minAge, int maxAge, String preferredClassTypes, String guidelines,
            int durationInWeeks) throws Exception {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DBConst.CONN_STR);
            conn.setAutoCommit(false);

            int generatedClassId = -1;

            String sqlFitness = "INSERT INTO FitnessPlan (startDate, durationWeeks, status, planType) VALUES ( DATE(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlFitness, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, durationInWeeks);
                ps.setInt(2, PLAN_STATUS_ACTIVE);
                ps.setString(3, PLAN_TYPE_GROUP);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedClassId = rs.getInt(1);
                } else {
                    throw new IllegalArgumentException("Error creating class with id " + generatedClassId);
                }
            }

            String sqlGroup = "INSERT INTO GroupFitnessPlan (planId, minAge, maxAge, preferredClassTypes, generalGuidelines) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlGroup)) {
                ps.setInt(1, generatedClassId);
                ps.setInt(2, minAge);
                ps.setInt(3, maxAge);
                ps.setString(4, preferredClassTypes);
                ps.setString(5, guidelines);
                ps.executeUpdate();
            }

            conn.commit();
            return generatedClassId;

        } catch (Exception e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            throw e;
        } finally {
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
        }
    }

    // ---------- Status ----------
    public boolean updatePlanStatus(int planId, int newStatus) {
        String updatePlanSql = "UPDATE FitnessPlan SET statusID = ? WHERE planId = ?";

        String cancelClassesSql = "UPDATE TrainingClass SET status = 'Cancelled' " +
                "WHERE planId = ? AND startDateTime > NOW() AND status != 'Cancelled'";

        try {
            Class.forName(DB_DRIVER);
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
                conn.setAutoCommit(false);

                try {
                    int rows;
                    try (PreparedStatement ps = conn.prepareStatement(updatePlanSql)) {
                        ps.setInt(1, newStatus);
                        ps.setInt(2, planId);
                        rows = ps.executeUpdate();
                    }

                    if (rows > 0
                            && (newStatus == PLAN_STATUS_CANCELLED || newStatus == PLAN_STATUS_COMPLETED)) {
                        try (PreparedStatement ps2 = conn.prepareStatement(cancelClassesSql)) {
                            ps2.setInt(1, planId);
                            int cancelledClassesCount = ps2.executeUpdate();
                            System.out.println(
                                    "Auto-cancelled " + cancelledClassesCount + " future classes for plan " + planId);
                        }
                    }

                    conn.commit();
                    return rows > 0;

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- Group members ----------

    public boolean addTraineeToGroupPlan(int traineeId, int planId) {
        String check = "SELECT COUNT(*) FROM GroupPlanTrainee WHERE planId = ? AND traineeId = ?";
        String insert = "INSERT INTO GroupPlanTrainee (planId, traineeId) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {

            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, planId);
                ps.setInt(2, traineeId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0)
                        return false;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, planId);
                ps.setInt(2, traineeId);
                ps.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeTraineeFromGroupPlan(int traineeId, int planId) {
        String sql = "DELETE FROM GroupPlanTrainee WHERE planId = ? AND traineeId = ?";
        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.setInt(2, traineeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Trainee> getGroupMembers(int planId) {
        List<Trainee> list = new ArrayList<>();

        String sql = "SELECT t.traineeId, t.firstName, t.lastName, " +
                " t.birthDate, t.phone, t.email " +
                "FROM GroupPlanTrainee g " +
                "INNER JOIN [Trainee] t ON g.[traineeId] = t.[traineeId] " +
                "WHERE g.[planId] = ? AND t.isActive = true " +
                "ORDER BY t.[traineeId]";

        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, planId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(createFromRs(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Trainee> getEligibleTrainees(int minAge, int maxAge) {
        List<Trainee> list = new ArrayList<>();

        String ageExpr = "(DateDiff('yyyy',[birthDate], Date()) - " +
                "IIf(Format(Date(),'mmdd') < Format([birthDate],'mmdd'), 1, 0))";

        String sql = "SELECT traineeId, firstName, lastName,birthDate, phone, email " +
                "FROM [Trainee] " +
                "WHERE " + ageExpr + " BETWEEN ? AND ? " +
                "AND isActive = true " +
                "ORDER BY [traineeId]";

        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minAge);
            ps.setInt(2, maxAge);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(createFromRs(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // הושאר כפי שהוא, עם התיקון הקודם (isActive = TRUE)
    public List<Trainee> getEligibleTraineesForPlan(int planId, int minAge, int maxAge) {
        List<Trainee> list = new ArrayList<>();

        String ageExpr = "(DateDiff('yyyy',[birthDate], Date()) - " +
                "IIf(Format(Date(),'mmdd') < Format([birthDate],'mmdd'), 1, 0))";

        String sql = "SELECT traineeId, firstName, lastName, birthDate, phone, email " +
                "FROM [Trainee] " +
                "WHERE " + ageExpr + " BETWEEN ? AND ? " +
                "AND isActive = true " +
                "AND [traineeId] NOT IN (SELECT [traineeId] FROM [GroupPlanTrainee] WHERE [planId] = ?) " +
                "ORDER BY [traineeId]";

        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minAge);
            ps.setInt(2, maxAge);
            ps.setInt(3, planId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(createFromRs(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static class PlanStatus {
        private final int planStatusId;
        private final String name;

        public PlanStatus(int planStatusId, String name) {
            this.planStatusId = planStatusId;
            this.name = name;
        }

        public int getPlanStatusId() {
            return planStatusId;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof PlanStatus))
                return false;
            return planStatusId == ((PlanStatus) obj).planStatusId;
        }
    }

    public static class PlanView {
	private int planId;
	private String type;
	private String status;

	public  PlanView(int planId, String type, String status) {
		this.planId = planId;
		this.type = type;
		this.status = status;
	}
	public int getPlanId() {
		return planId;
	}
	public String getType() {
		return type;
	}
	public String getStatus() {
		return status;
	}
    public String toString(){
        return planId + " - " + type + " - " + status;
    }
}
    private Trainee createFromRs(ResultSet rs) throws SQLException {
        return new Trainee(
                rs.getInt("traineeId"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getTimestamp("birthDate").toLocalDateTime(),
                rs.getString("phone"),
                rs.getString("email"));

    }
}
