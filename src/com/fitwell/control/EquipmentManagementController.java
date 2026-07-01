package com.fitwell.control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fitwell.entity.DBConst;
import com.fitwell.entity.EquipmentItem;

public class EquipmentManagementController {

    private static EquipmentManagementController instance;

    private EquipmentManagementController() { }

    public static EquipmentManagementController getInstance() {
        if (instance == null) {
            instance = new EquipmentManagementController();
        }
        return instance;
    }

    private void loadDriver() throws ClassNotFoundException {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
    }

    public Boolean getIsFunctional(int serialNumber) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(DBConst.SQL_SEL_ITEM_FUNCTIONAL)) {

                stmt.setInt(1, serialNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) return null;
                    return rs.getBoolean("isfunctional");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean markItemAsNonFunctional(String serialNumber) {
        return setFunctional(serialNumber, false);
    }

    public boolean markItemAsFunctional(String serialNumber) {
        return setFunctional(serialNumber, true);
    }

    private boolean setFunctional(String serialNumber, boolean isFunctional) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(DBConst.SQL_UPD_ITEM_FUNCTIONAL)) {

                stmt.setBoolean(1, isFunctional);
                stmt.setString(2, serialNumber);

                int rows = stmt.executeUpdate();
                return rows == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeItem(String serialNumber) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(DBConst.SQL_DEL_ITEM)) {

                stmt.setString(1, serialNumber);
                int rows = stmt.executeUpdate();
                return rows == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setNeedsReview(String serialNumber, boolean needsReview) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(DBConst.SQL_UPD_ITEM_NEEDS_REVIEW)) {

                stmt.setBoolean(1, needsReview);
                stmt.setString(2, serialNumber);
                return stmt.executeUpdate() == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLocation(int serialNumber, int newX, int newY, int newShelfNumber) {
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(DBConst.SQL_UPD_ITEM_LOCATION)) {

                stmt.setInt(1, newX);
                stmt.setInt(2, newY);
                stmt.setInt(3, newShelfNumber);
                stmt.setInt(4, serialNumber);
                return stmt.executeUpdate() == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<EquipmentItem> getEquipmentForReview() {
        List<EquipmentItem> items = new ArrayList<>();
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(DBConst.SQL_SEL_ITEMS_FOR_REVIEW);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    items.add(new EquipmentItem(
                            rs.getInt("itemserialnumber"), 
                            rs.getInt("typeID"), 
                            rs.getInt("shelfNumber"), 
                            rs.getInt("x"), 
                            rs.getInt("y"), 
                            rs.getBoolean("needsReview"), 
                            rs.getBoolean("isfunctional")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public boolean markItemAsReviewed(String serialNumber) {
        return setNeedsReview(serialNumber, false);
    }

    public List<EquipmentItemView> getAllEquipmentTableData() {
        List<EquipmentItemView> data = new ArrayList<>();
        String sql = 
            "SELECT EI.SerialNumber, ET.name, EI.equipmentTypeID, EI.shelfNumber, EI.LocationX, EI.LocationY, EI.needsReview, EI.isFunctional " +
            "FROM EquipmentItem EI " +
            "INNER JOIN EquipmentType ET ON EI.equipmentTypeID = ET.equipmentTypeID ";

        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    data.add(new EquipmentItemView(
                        rs.getString("SerialNumber"),
                        rs.getString("name"),
                        rs.getInt("equipmentTypeID"),
                        rs.getInt("shelfNumber"),
                        rs.getInt("LocationX"),
                        rs.getInt("LocationY"),
                        rs.getBoolean("needsReview"),
                        rs.getBoolean("isFunctional")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static class EquipmentItemView{
        public final String SerialNumber;
        public final String name;
        public final int equipomentTypeID;
        public final int shelfNumber;
        public final int LocationX;
        public final int LocationY;
        public final boolean needsReview;
        public final boolean isFunctional;
        public EquipmentItemView(String serialNumber, String name, int equipomentTypeID, int shelfNumber, int locationX,
                int locationY, boolean needsReview, boolean isFunctional) {
            SerialNumber = serialNumber;
            this.name = name;
            this.equipomentTypeID = equipomentTypeID;
            this.shelfNumber = shelfNumber;
            LocationX = locationX;
            LocationY = locationY;
            this.needsReview = needsReview;
            this.isFunctional = isFunctional;
        }
        
    }
    public static class ShortageInfo {
        public final int classId;
        public final String className;
        public final int typeId;
        public final int requestedQty;
        public final int availableAfterBreak;
        public final String startTimeStr;

        public ShortageInfo(int classId, String className, int typeId, int requestedQty, int availableAfterBreak, String startTimeStr) {
            this.classId = classId;
            this.className = className;
            this.typeId = typeId;
            this.requestedQty = requestedQty;
            this.availableAfterBreak = availableAfterBreak;
            this.startTimeStr = startTimeStr;
        }
    }

    public List<ShortageInfo> getAffectedClassesIfItemBreaks(String serialNumber) {
        List<ShortageInfo> affectedClasses = new ArrayList<>();
        int typeId = -1;

        String sqlGetType = "SELECT equipmentTypeID FROM EquipmentItem WHERE SerialNumber = ?";
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sqlGetType)) {
                ps.setString(1, serialNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) typeId = rs.getInt("equipmentTypeID");
                }
            }
        } catch (Exception e) { e.printStackTrace(); return affectedClasses; }

        if (typeId == -1) return affectedClasses;

        String sqlFutureClasses =
            "SELECT tc.classId, tc.name, tc.startDateTime, tc.endDateTime, cea.requestedQuantity " +
            "FROM TrainingClass tc " +
            "INNER JOIN ClassEquipmentAssignment cea ON tc.classId = cea.classId " +
            "WHERE cea.equipmentTypeID = ? AND tc.startDateTime > ? AND tc.status NOT IN ('Cancelled', 'Completed')";

        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sqlFutureClasses)) {

                ps.setInt(1, typeId);
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int classId = rs.getInt("classId");
                        String className = rs.getString("name");
                        java.sql.Timestamp startTs = rs.getTimestamp("startDateTime");
                        java.sql.Timestamp endTs = rs.getTimestamp("endDateTime"); 
                        int requestedQty = rs.getInt("requestedQuantity");

                        int totalFunctionalInGym = getTotalFunctionalByType(typeId);
                        int totalAfterBreak = totalFunctionalInGym - 1;

                        int totalRequestedDuringOverlap = getOverlappingRequestedQuantity(typeId, startTs, endTs);

                        if (totalRequestedDuringOverlap > totalAfterBreak) {
                            affectedClasses.add(new ShortageInfo(
                                    classId, className, typeId, requestedQty, totalAfterBreak, 
                                    startTs.toLocalDateTime().toString()
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return affectedClasses;
    }

    private int getOverlappingRequestedQuantity(int typeId, java.sql.Timestamp start, java.sql.Timestamp end) {
        String sql = "SELECT SUM(A.requestedQuantity) " +
                     "FROM ClassEquipmentAssignment A " +
                     "INNER JOIN TrainingClass C ON A.classId = C.classId " +
                     "WHERE A.equipmentTypeID = ? " +
                     "AND C.status NOT IN ('Cancelled', 'Completed') " +
                     "AND C.startDateTime < ? AND C.endDateTime > ?"; 
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, typeId);
                ps.setTimestamp(2, end);
                ps.setTimestamp(3, start);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private int getTotalFunctionalByType(int typeId) {
        String sql = "SELECT COUNT(*) FROM EquipmentItem WHERE equipmentTypeID = ? AND isFunctional = true";
        try {
            loadDriver();
            try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, typeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}