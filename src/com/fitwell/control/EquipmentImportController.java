package com.fitwell.control;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.fitwell.entity.DBConst;

public class EquipmentImportController {

	private static EquipmentImportController instance;

	private final SwiftFitInterface swiftFit = new SwiftFitInterface();

	private EquipmentImportController() {
	}

	public static EquipmentImportController getInstance() {
		if (instance == null)
			instance = new EquipmentImportController();
		return instance;
	}

	private void loadDriver() throws ClassNotFoundException {
		Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
	}

	public static class EquipmentImportData {
		private String monthlyUpdateDate;
		private List<EquipmentItem> equipment;

		public String getMonthlyUpdateDate() {
			return monthlyUpdateDate;
		}

		public List<EquipmentItem> getEquipment() {
			return equipment;
		}

	}

	public static class EquipmentItem {
		private String serialNumber;
		private int equipmentTypeId;
		private int quantity;
		private String photoLinks;

		public String getSerialNumber() {
			return serialNumber;
		}

		public int getEquipmentTypeId() {
			return equipmentTypeId;
		}

		public int getQuantity() {
			return quantity;
		}

		public String getPhotoLinks() {
			return photoLinks;
		}

	}

	public ImportResult processMonthlyUpdates(Path jsonPath) {
		int insertedTotal = 0;
		int firstInsertedSerial = -1;
		int lastInsertedSerial = -1;

		try {
			loadDriver();

			EquipmentImportData importedData = swiftFit.fetchUpdates(jsonPath);

			try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR)) {
				conn.setAutoCommit(false);

				int itemsPerShelf = 8;
				int currentShelf = getStartingShelf(conn);
				int itemsInCurrentShelf = getItemCountInShelf(conn, currentShelf);

				for (EquipmentItem item : importedData.getEquipment()) {
					int typeId = item.getEquipmentTypeId();
					int quantity = item.getQuantity();
					ensureTypeExists(conn, typeId);
					for (int i = 0; i < quantity; i++) {
						String serialToInsert = UUID.randomUUID().toString();
						if (itemsInCurrentShelf >= itemsPerShelf) {
							currentShelf++;
							itemsInCurrentShelf = 0;
						}
						int shelf = currentShelf;
						int quantityE = itemsInCurrentShelf + 1;
						int y = currentShelf;
						boolean needsReview = Math.random() < 0.5;

						boolean isFunctional = !needsReview;						
						insertItem(conn, serialToInsert, typeId, shelf, quantityE, y, needsReview, isFunctional);
						itemsInCurrentShelf++;
						insertedTotal++;
					}
				}
				
				conn.commit();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ImportResult(insertedTotal, firstInsertedSerial, lastInsertedSerial);
	}

	// ---------- Result DTO ----------

	public static class ImportResult {
		public final int totalInserted;
		public final int firstSerial;
		public final int lastSerial;

		public ImportResult(int totalInserted, int firstSerial, int lastSerial) {
			this.totalInserted = totalInserted;
			this.firstSerial = firstSerial;
			this.lastSerial = lastSerial;
		}

		@Override
		public String toString() {
			if (totalInserted <= 0)
				return "No items added.";
			return "Success! Added " + totalInserted + " items.\nSerials: " + firstSerial + " - " + lastSerial;
		}
	}


	private int getStartingShelf(Connection conn) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement("SELECT MAX(shelfNumber) AS maxShelf FROM EquipmentItem");
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				int max = rs.getInt("maxShelf");
				return rs.wasNull() || max < 10 ? 10 : max;
			}
			return 10;
		}
	}

	private int getItemCountInShelf(Connection conn, int shelf) throws SQLException {
		try (PreparedStatement ps = conn
				.prepareStatement("SELECT COUNT(*) AS cnt FROM EquipmentItem WHERE shelfNumber = ?")) {
			ps.setInt(1, shelf);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt("cnt");
			}
		}
	}

	private void ensureTypeExists(Connection conn, int typeId) throws SQLException {
		if (typeExists(conn, typeId))
			return;
		String name = "Imported Type " + typeId;
		try (PreparedStatement ps = conn.prepareStatement(DBConst.SQL_INS_EQUIPMENT_TYPE)) {
			ps.setInt(1, typeId);
			ps.setString(2, name);
			ps.setString(3, "UNKNOWN");
			ps.setString(4, "Imported from monthly SwiftFit report");
			ps.executeUpdate();
		}
	}

	private boolean typeExists(Connection conn, int typeId) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(DBConst.SQL_SEL_TYPE_EXISTS)) {
			ps.setInt(1, typeId);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt("cnt") > 0;
			}
		}
	}

	// === התיקון הקריטי נמצא כאן ===
	private void insertItem(Connection conn, String serial, int typeId, int shelf, int quantityE, int y,
			boolean needsReview,
			boolean isFunctional) throws SQLException {

		// במקום להשתמש ב-Consts.SQL_INS_EQUIPMENT_ITEM שאולי הסדר שם הפוך,
		// אנחנו כותבים את ה-SQL במפורש כאן כדי להיות בטוחים ב-100% בסדר הפרמטרים.
		String sql = "INSERT INTO EquipmentItem (itemSerialNumber, typeID, shelfNumber, x, y, needsReview, isFunctional) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, serial);
			ps.setInt(2, typeId);
			ps.setInt(3, shelf);
			ps.setInt(4, quantityE);
			ps.setInt(5, y);

			// עכשיו זה בטוח הולך לעמודה הנכונה
			ps.setBoolean(6, needsReview);
			ps.setBoolean(7, isFunctional);

			ps.executeUpdate();
		}
	}
}