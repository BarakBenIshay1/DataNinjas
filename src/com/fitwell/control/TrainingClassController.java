package com.fitwell.control;

import static com.fitwell.entity.DBConst.CONN_STR;
import static com.fitwell.entity.DBConst.DB_DRIVER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fitwell.entity.DBConst;
import com.fitwell.entity.Tip;

public class TrainingClassController {

	public static final String CLASS_STATUS_ACTIVE = "Active";
	private static TrainingClassController instance;
	private Map<Integer, ClassType> classTypeMap;

	private TrainingClassController() {
		classTypeMap = new HashMap<>();
		loadClassTypeList();
	}

	public static TrainingClassController getInstance() {
		if (instance == null)
			instance = new TrainingClassController();
		return instance;
	}

	public List<ClassType> getClassTypeList() {
		return new ArrayList<>(classTypeMap.values());
	}

	public ClassType getClassTypeById(int id) {
		return classTypeMap.get(id);
	}

	private void loadClassTypeList() {

		try {
			loadDriver();
			String sql = "SELECT ClassTypeId, Name FROM ClassType";
			try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					int classTypeId = rs.getInt(1);
					String name = rs.getString(2);

					ClassType ct = new ClassType(classTypeId, name);
					classTypeMap.put(ct.getClassTypeId(), ct);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadDriver() throws ClassNotFoundException {
		Class.forName(DB_DRIVER);
	}

	private boolean isPlanActive(int planId) {
		try {
			loadDriver();
			try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
				String sql = "SELECT statusId FROM FitnessPlan WHERE planId = ?";
				try (PreparedStatement ps = conn.prepareStatement(sql)) {
					ps.setInt(1, planId);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							int status = rs.getInt("statusId");
							return status == 1;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void createTrainingClass(String name, LocalDateTime start, LocalDateTime end, int type,
			int maxParticipants, List<Tip> selectedTips, int planId, Map<Integer, Integer> equipmentRequirements) {

		if (name == null || name.isBlank())
			throw new IllegalArgumentException("Class name is required");

		if (maxParticipants < 1 || maxParticipants > 30)
			throw new IllegalArgumentException("Max participants must be between 1 and 30");

		if (start == null || end == null)
			throw new IllegalArgumentException("Start and end times are required");
		if (!end.isAfter(start)) {
			throw new IllegalArgumentException("End time must be after start time");
		}

		if (start.isBefore(LocalDateTime.now().plusHours(24))) {
			throw new IllegalArgumentException("Error: A class must be scheduled at least 24 hours.");
		}
		// =================================================================

		if (!end.isAfter(start))
			throw new IllegalArgumentException("End time must be after start time");

		if (!start.toLocalDate().equals(end.toLocalDate()))
			throw new IllegalArgumentException("Training class must start and end on the same day");

		if (!isPlanActive(planId)) {
			throw new IllegalArgumentException("Error: You can only add classes to an 'Active' plan!");
		}

		if (equipmentRequirements != null && !equipmentRequirements.isEmpty()) {
			for (Map.Entry<Integer, Integer> entry : equipmentRequirements.entrySet()) {
				int typeId = entry.getKey();
				int qtyNeeded = entry.getValue();

				if (!checkEquipmentAvailability(typeId, qtyNeeded, start, end)) {
					throw new IllegalArgumentException(
							"Not enough equipment available for TypeID: " + typeId + ". requested: " + qtyNeeded);
				}
			}
		}

		String classSql = "INSERT INTO TrainingClass "
				+ "(name, startDateTime, endDateTime, classTypeId, maxParticipants, planId, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try {
			loadDriver();
			try (Connection conn = DriverManager.getConnection(CONN_STR)) {
				int generatedClassId = -1;
				try (PreparedStatement ps = conn.prepareStatement(classSql, Statement.RETURN_GENERATED_KEYS)) {
					ps.setString(1, name.trim());
					ps.setTimestamp(2, Timestamp.valueOf(start));
					ps.setTimestamp(3, Timestamp.valueOf(end));
					ps.setInt(4, type);
					ps.setInt(5, maxParticipants);
					ps.setInt(6, planId);
					ps.setString(7, CLASS_STATUS_ACTIVE);
					ps.executeUpdate();
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next()) {
						generatedClassId = rs.getInt(1);
					}else{
						throw new IllegalArgumentException("Error creating class with id " + generatedClassId);
					}
				}

				if (selectedTips != null && !selectedTips.isEmpty()) {
					String tipSql = "INSERT INTO Tip ( classID, text, linkUrl) VALUES ( ?, ?, ?)";
					try (PreparedStatement psTip = conn.prepareStatement(tipSql)) {
						for (Tip tip : selectedTips) {
							psTip.setInt(1, generatedClassId);
							psTip.setString(2, tip.getContent());
							psTip.setString(3, "https://fitwell.com/tips");
							psTip.executeUpdate();
						}
					}
				}

				if (equipmentRequirements != null && !equipmentRequirements.isEmpty()) {
					String eqSql = "INSERT INTO ClassEquipmentAssignment (classID, typeID, requestedQuantity) VALUES (?, ?, ?)";
					try (PreparedStatement psEq = conn.prepareStatement(eqSql)) {
						for (Map.Entry<Integer, Integer> entry : equipmentRequirements.entrySet()) {
							psEq.setInt(1, generatedClassId);
							psEq.setInt(2, entry.getKey());
							psEq.setInt(3, entry.getValue());
							psEq.executeUpdate();
						}
					}
				}

			}
		} catch (SQLException | ClassNotFoundException e) {
			throw new RuntimeException("Failed to create training class", e);
		}
	}

	public boolean checkEquipmentAvailability(int typeId, int requiredAmount, LocalDateTime start, LocalDateTime end) {
		String sqlTotal = "SELECT COUNT(*) AS total FROM EquipmentItem WHERE typeID = ? AND isFunctional = TRUE";

		String sqlUsed = "SELECT SUM(cea.requestedQuantity) AS usedQty " + "FROM ClassEquipmentAssignment cea "
				+ "INNER JOIN TrainingClass tc ON cea.classID = tc.classId " + "WHERE cea.typeID = ? "
				+ "AND tc.status <> 'Cancelled' " + "AND (tc.startDateTime > ? OR tc.endDateTime < ?)";

		try (Connection conn = DriverManager.getConnection(CONN_STR)) {
			int totalFunctional = 0;
			try (PreparedStatement ps = conn.prepareStatement(sqlTotal)) {
				ps.setInt(1, typeId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						totalFunctional = rs.getInt("total");
				}
			}

			int usedInOverlap = 0;
			try (PreparedStatement ps = conn.prepareStatement(sqlUsed)) {
				ps.setInt(1, typeId);
				ps.setTimestamp(2, Timestamp.valueOf(end));
				ps.setTimestamp(3, Timestamp.valueOf(start));
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						usedInOverlap = rs.getInt("usedQty");
					}
				}
			}

			int available = totalFunctional - usedInOverlap;
			return available >= requiredAmount;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// ***** HELPER METHODS ***** //

	public List<EquipmentTypeView> getAllEquipmentTypes() {
		List<EquipmentTypeView> types = new ArrayList<>();
		String sql = "SELECT equipmentTypeID, name FROM EquipmentType";
		try (Connection conn = DriverManager.getConnection(CONN_STR);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				types.add(new EquipmentTypeView(rs.getInt("equipmentTypeID"), rs.getString("name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return types;
	}


	public List<Tip> getAllTips() {
		return List.of(new Tip("Warm up properly"), new Tip("Focus on breathing"), new Tip("Maintain correct posture"),
				new Tip("Hydrate before training"), new Tip("Quality over quantity"), new Tip("Stretch after workout"),
				new Tip("Listen to your body"));
	}

	// מעודכן: שאילתות שמבצעות JOIN עם FitnessPlan כדי לסנן רק תוכניות פעילות
	public List<PlanItem> getAllGroupPlanIds() {
		return fetchPlanList(
				"SELECT G.planId, G.GeneralGuidelines FROM GroupFitnessPlan G INNER JOIN FitnessPlan F ON G.planId = F.planId WHERE F.statusid = 1");
	}

	public List<PlanItem> getAllPersonalPlanIds() {
		return fetchPlanList(
				"SELECT P.planId, P.DietaryRestrictions FROM PersonalFitnessPlan P INNER JOIN FitnessPlan F ON P.planId = F.planId WHERE F.statusid = 1");
	}

	private List<PlanItem> fetchPlanList(String sql) {
		List<PlanItem> plans = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(CONN_STR);
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) {
				plans.add(new PlanItem(rs.getInt(1), rs.getString(2)));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to load plans", e);
		}
		return plans;
	}

	public List<Object[]> getAllTrainingClasses() {
		List<Object[]> rows = new ArrayList<>();
		String sql = "SELECT classId, name, startDateTime, endDateTime, classTypeId, maxParticipants, status FROM TrainingClass";
		try (Connection conn = DriverManager.getConnection(CONN_STR);
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) {
				rows.add(new Object[] { rs.getString("classId"), rs.getString("name"),
						rs.getTimestamp("startDateTime"), rs.getTimestamp("endDateTime"), rs.getInt("classTypeId"),
						rs.getInt("maxParticipants"), rs.getString("status") });
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to load training classes", e);
		}
		return rows;
	}

	public void updateTrainingClass(String classId, String name, LocalDateTime start, LocalDateTime end, String type,
			Integer maxParticipants) {
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private java.util.Timer emergencyTimer;
	private boolean isEmergencyActive = false;
	private LocalDateTime emergencyTargetTime;

	public void startEmergencyMode() {
		if (isEmergencyActive)
			return;

		System.out.println("⚠️ EMERGENCY MODE ACTIVATED");
		isEmergencyActive = true;

		emergencyTargetTime = LocalDateTime.now().plusMinutes(30);

		String sql = "UPDATE TrainingClass SET status = 'Paused' WHERE status IN ('Active', 'InSession')";
		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to activate emergency mode", e);
		}

		emergencyTimer = new java.util.Timer();
		emergencyTimer.schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				endEmergencyMode();
			}
		}, 30 * 60 * 1000);
	}

	public void endEmergencyMode() {
		if (emergencyTimer != null) {
			emergencyTimer.cancel();
			emergencyTimer = null;
		}
		isEmergencyActive = false;
		emergencyTargetTime = null;

		String sql = "UPDATE TrainingClass SET status = IIF(NOW() < startDateTime, 'Active', 'InSession') WHERE status = 'Paused' AND NOW() < endDateTime";
		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to end emergency mode", e);
		}
	}

	public boolean isEmergencyActive() {
		return isEmergencyActive;
	}

	public LocalDateTime getEmergencyTargetTime() {
		return emergencyTargetTime;
	}

	public List<EquipmentTypeView> getAvailabilityReportForTime(LocalDateTime start, LocalDateTime end) {
		List<EquipmentTypeView> allTypes = getAllEquipmentTypes();
		List<EquipmentTypeView> availableTypes = new ArrayList<EquipmentTypeView>();

		for (EquipmentTypeView type : allTypes) {
			int available = getAvailableQuantity(type.getEquipmentTypeID(), start, end);

			if (available < 0)
				available = 0;
			availableTypes.add(type);
		}
		return availableTypes;
	}

	private int getAvailableQuantity(int typeId, LocalDateTime start, LocalDateTime end) {
		String sqlTotal = "SELECT COUNT(*) AS total FROM EquipmentItem WHERE equipmentTypeID = ? AND isFunctional = TRUE";

		String sqlUsed = "SELECT SUM(cea.requestedQuantity) AS usedQty " + "FROM ClassEquipmentAssignment cea "
				+ "INNER JOIN TrainingClass tc ON cea.classID = tc.classId " + "WHERE cea.equipmentTypeID = ? "
				+ "AND tc.status <> 'Cancelled' " + "AND (tc.startDateTime < ? AND tc.endDateTime > ?)";

		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
			int total = 0;
			try (PreparedStatement ps = conn.prepareStatement(sqlTotal)) {
				ps.setInt(1, typeId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						total = rs.getInt("total");
				}
			}

			int used = 0;
			try (PreparedStatement ps = conn.prepareStatement(sqlUsed)) {
				ps.setInt(1, typeId);
				ps.setTimestamp(2, Timestamp.valueOf(end));
				ps.setTimestamp(3, Timestamp.valueOf(start));
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						used = rs.getInt("usedQty");
				}
			}
			return total - used;

		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public boolean cancelTrainingClass(int classId) {
		String sql = "UPDATE TrainingClass SET status = 'Cancelled' WHERE classId = ?";
		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, classId);
			int rows = ps.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateClassReschedule(int classId, LocalDateTime newStart, LocalDateTime newEnd) {
		String validationError = validateEquipmentForReschedule(classId, newStart, newEnd);
		if (validationError != null) {
			throw new RuntimeException(validationError);
		}

		String sql = "UPDATE TrainingClass SET startDateTime = ?, endDateTime = ? WHERE classId = ?";
		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setTimestamp(1, Timestamp.valueOf(newStart));
			ps.setTimestamp(2, Timestamp.valueOf(newEnd));
			ps.setInt(3, classId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String validateEquipmentForReschedule(int classId, LocalDateTime newStart, LocalDateTime newEnd) {
		Map<Integer, Integer> currentEquipment = getClassEquipment(classId);
		StringBuilder errors = new StringBuilder();

		for (Map.Entry<Integer, Integer> entry : currentEquipment.entrySet()) {
			int typeId = entry.getKey();
			int qtyNeeded = entry.getValue();

			int availableAtNewTime = getAvailableQuantityExcludingClass(typeId, newStart, newEnd, classId);

			if (qtyNeeded > availableAtNewTime) {
				errors.append("- Type ID ").append(typeId).append(": Need ").append(qtyNeeded).append(", but only ")
						.append(availableAtNewTime).append(" available.\n");
			}
		}

		return errors.length() > 0 ? errors.toString() : null;
	}

	public int getAvailableQuantityExcludingClass(int typeId, LocalDateTime start, LocalDateTime end,
			int excludeClassId) {
		String sqlTotal = "SELECT COUNT(*) AS total FROM EquipmentItem WHERE equipmentTypeID = ? AND isFunctional = TRUE";

		String sqlUsed = "SELECT SUM(cea.requestedQuantity) AS usedQty " + "FROM ClassEquipmentAssignment cea "
				+ "INNER JOIN TrainingClass tc ON cea.classID = tc.classId " + "WHERE cea.equipmentTypeID = ? "
				+ "AND tc.status <> 'Cancelled' " + "AND tc.classId <> ? "
				+ "AND (tc.startDateTime < ? AND tc.endDateTime > ?)";

		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
			int total = 0;
			try (PreparedStatement ps = conn.prepareStatement(sqlTotal)) {
				ps.setInt(1, typeId);
				ResultSet rs = ps.executeQuery();
				if (rs.next())
					total = rs.getInt("total");
			}

			int used = 0;
			try (PreparedStatement ps = conn.prepareStatement(sqlUsed)) {
				ps.setInt(1, typeId);
				ps.setInt(2, excludeClassId);
				ps.setTimestamp(3, Timestamp.valueOf(end));
				ps.setTimestamp(4, Timestamp.valueOf(start));
				ResultSet rs = ps.executeQuery();
				if (rs.next())
					used = rs.getInt("usedQty");
			}
			return total - used;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Map<Integer, Integer> getClassEquipment(int classId) {
		Map<Integer, Integer> map = new HashMap<>();
		String sql = "SELECT equipmentTypeID, requestedQuantity FROM ClassEquipmentAssignment WHERE classID = ?";
		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, classId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getInt("equipmentTypeID"), rs.getInt("requestedQuantity"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	public boolean reduceClassEquipmentRequirement(int classId, int typeId, int newQuantity) {
		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
			if (newQuantity <= 0) {
				String sqlDel = "DELETE FROM ClassEquipmentAssignment WHERE classID = ? AND typeID = ?";
				try (PreparedStatement ps = conn.prepareStatement(sqlDel)) {
					ps.setInt(1, classId);
					ps.setInt(2, typeId);
					return ps.executeUpdate() > 0;
				}
			} else {
				String sqlUpd = "UPDATE ClassEquipmentAssignment SET requestedQuantity = ? WHERE classID = ? AND typeID = ?";
				try (PreparedStatement ps = conn.prepareStatement(sqlUpd)) {
					ps.setInt(1, newQuantity);
					ps.setInt(2, classId);
					ps.setInt(3, typeId);
					return ps.executeUpdate() > 0;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateClassDetails(int classId, String newName, LocalDateTime newStart, LocalDateTime newEnd,
			int newType, int newMax, Map<Integer, Integer> newEquipment) {
		for (Map.Entry<Integer, Integer> entry : newEquipment.entrySet()) {
			int typeId = entry.getKey();
			int qty = entry.getValue();
			int available = getAvailableQuantityExcludingClass(typeId, newStart, newEnd, classId);
			if (qty > available) {
				throw new RuntimeException("Not enough equipment for Type ID " + typeId + ". Need " + qty + " but only "
						+ available + " available.");
			}
		}

		String sqlClass = "UPDATE TrainingClass SET name=?, startDateTime=?, endDateTime=?, classTypeId=?, maxParticipants=? WHERE classId=?";
		try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
			conn.setAutoCommit(false);

			try (PreparedStatement ps = conn.prepareStatement(sqlClass)) {
				ps.setString(1, newName);
				ps.setTimestamp(2, Timestamp.valueOf(newStart));
				ps.setTimestamp(3, Timestamp.valueOf(newEnd));
				ps.setInt(4, newType);
				ps.setInt(5, newMax);
				ps.setInt(6, classId);
				ps.executeUpdate();
			}

			try (PreparedStatement psDel = conn
					.prepareStatement("DELETE FROM ClassEquipmentAssignment WHERE classID = ?")) {
				psDel.setInt(1, classId);
				psDel.executeUpdate();
			}

			if (newEquipment != null && !newEquipment.isEmpty()) {
				String insSql = "INSERT INTO ClassEquipmentAssignment (classID, equipmentTypeID, requestedQuantity) VALUES (?, ?, ?)";
				try (PreparedStatement psIns = conn.prepareStatement(insSql)) {
					for (Map.Entry<Integer, Integer> entry : newEquipment.entrySet()) {
						psIns.setInt(1, classId);
						psIns.setInt(2, entry.getKey());
						psIns.setInt(3, entry.getValue());
						psIns.executeUpdate();
					}
				}
			}

			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<EquipmentTypeView> getAvailabilityReportForTimeExcludingClass(LocalDateTime start, LocalDateTime end,
			int excludeClassId) {
		List<EquipmentTypeView> result = new ArrayList<>();
		List<EquipmentTypeView> allTypes = getAllEquipmentTypes();

		for (EquipmentTypeView type : allTypes) {
			int typeId = type.getEquipmentTypeID();
			String name = type.getName();
			int available = getAvailableQuantityExcludingClass(typeId, start, end, excludeClassId);
			if (available < 0)
				available = 0;

			result.add(new EquipmentTypeView(typeId, name, available));
		}
		return result;
	}

	public void autoUpdateClassStatuses() {
		String sqlStart = "UPDATE TrainingClass SET status = 'InSession' WHERE status = 'Active' AND NOW() >= startDateTime AND NOW() < endDateTime";
		String sqlEnd = "UPDATE TrainingClass SET status = 'Completed' WHERE status IN ('InSession', 'Paused', 'Active') AND NOW() >= endDateTime";

		try {
			Class.forName(DBConst.DB_DRIVER);
			try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
					java.sql.Statement st = conn.createStatement()) {
				st.executeUpdate(sqlStart);
				st.executeUpdate(sqlEnd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class PlanItem {
		private int planId;
		private String description;

		public PlanItem(int planId, String description) {
			this.planId = planId;
			this.description = description;
		}

		public int getPlanId() {
			return planId;
		}

		public String getDescription() {
			return description;
		}

		public String toString() {
			return description;
		}
	}

	public static class ClassType {
		private int classTypeId;
		private String name;

		public ClassType(int classTypeId, String name) {
			this.classTypeId = classTypeId;
			this.name = name;
		}

		public int getClassTypeId() {
			return classTypeId;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ClassType))
				return false;
			return this.classTypeId == ((ClassType) obj).classTypeId;
		}

		@Override
		public int hashCode() {
			return this.classTypeId;
		}
	}

	public static class EquipmentTypeView {
		private int equipmentTypeID;
		private String name;
		private int available;

		public EquipmentTypeView(int equipmentTypeID, String name) {
			this(equipmentTypeID, name, 0);
		}

		public EquipmentTypeView(int equipmentTypeID, String name, int available) {
			this.equipmentTypeID = equipmentTypeID;
			this.name = name;
			this.available = available;
		}

		public int getEquipmentTypeID() {
			return equipmentTypeID;
		}

		public String getName() {
			return name;
		}

		public int getAvailable() {
			return available;
		}

	}

	public static class TrainingClassView {
		private int classId;
		private String name;
		private LocalDateTime startDateTime;
		private LocalDateTime endDateTime;
		private String classTypeId;
		private int maxParticipants;
		private String status;
	}
}