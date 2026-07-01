package com.fitwell.entity;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class DBConst {
    private DBConst() { throw new AssertionError(); }
    
    public static final String DB_DRIVER = "net.ucanaccess.jdbc.UcanaccessDriver";

    private static final String DB_NAME = "FitWell_EX3.accdb";
    protected static final String DB_FILEPATH = "/Volumes/disk1_2/macbkp/"+DB_NAME;//getDBPath();

    public static final String CONN_STR =
            "jdbc:ucanaccess://" + DB_FILEPATH + ";COLUMNORDER=DISPLAY";

    private static String getDBPath() {
        try {
            URL location = DBConst.class.getProtectionDomain().getCodeSource().getLocation();
            String decoded = URLDecoder.decode(location.getPath(), StandardCharsets.UTF_8);

            File codeLocation = new File(decoded);
            File baseDir = codeLocation.isFile() ? codeLocation.getParentFile() : codeLocation;

            // We'll search in both: data/ and db/
            String[] folders = {"data", "db"};

            // 1) <runFolder>/data|db/DB_NAME   (works when running from IDE output or from a JAR folder)
            for (String f : folders) {
                File candidate = new File(baseDir, f + File.separator + DB_NAME);
                if (candidate.exists()) return candidate.getAbsolutePath();
            }

            // 2) parent of runFolder (common in IDE: target/classes -> project root)
            File parent = baseDir.getParentFile();
            if (parent != null) {
                for (String f : folders) {
                    File candidate = new File(parent, f + File.separator + DB_NAME);
                    if (candidate.exists()) return candidate.getAbsolutePath();
                }
            }

            // 3) relative to current working dir
            for (String f : folders) {
                File candidate = new File(f + File.separator + DB_NAME);
                if (candidate.exists()) return candidate.getAbsolutePath();
            }

            throw new RuntimeException(
                    "DB file not found. Expected: data/" + DB_NAME + " or db/" + DB_NAME + "\n" +
                    "Tip: keep the folder (data/ or db/) in the project root, and next to the JAR when exporting."
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve DB path: " + e.getMessage(), e);
        }
    }

    // ===== EquipmentItem SQL =====
    public static final String SQL_SEL_ITEM_FUNCTIONAL =
            "SELECT isfunctional FROM EquipmentItem WHERE Serialnumber = ?";

    public static final String SQL_UPD_ITEM_FUNCTIONAL =
            "UPDATE EquipmentItem SET isfunctional = ? WHERE Serialnumber = ?";

    public static final String SQL_DEL_ITEM =
            "DELETE FROM EquipmentItem WHERE Serialnumber = ?";

    public static final String SQL_UPD_ITEM_NEEDS_REVIEW =
            "UPDATE EquipmentItem SET needsReview = ? WHERE Serialnumber = ?";

    public static final String SQL_UPD_ITEM_LOCATION =
            "UPDATE EquipmentItem SET locationX = ?, locationY = ?, shelfNumber = ? WHERE Serialnumber = ?";

    public static final String SQL_SEL_ITEMS_FOR_REVIEW =
            "SELECT Serialnumber, typeID, shelfNumber, locationX, locationY, needsReview, isfunctional " +
            "FROM EquipmentItem WHERE needsReview = TRUE " +
            "ORDER BY Serialnumber";

    // ===== Import SQL (ONLY ADD) =====
    public static final String SQL_COUNT_ITEMS_BY_TYPE =
            "SELECT COUNT(*) AS cnt FROM EquipmentItem WHERE equipmentTypeID = ?";

    public static final String SQL_MAX_SERIAL =
            "SELECT MAX(Serialnumber) AS m FROM EquipmentItem";

    public static final String SQL_INS_EQUIPMENT_ITEM =
            "INSERT INTO EquipmentItem (Serialnumber, equipmentTypeID, shelfNumber, locationX, locationY, needsReview, isfunctional) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
 // ===== EquipmentType SQL =====
    public static final String SQL_SEL_TYPE_EXISTS =
            "SELECT COUNT(*) AS cnt FROM EquipmentType WHERE equipmentTypeID = ?";

    public static final String SQL_INS_EQUIPMENT_TYPE =
            "INSERT INTO EquipmentType (equipmentTypeID, name, category, description) VALUES (?, ?, ?, ?)";

 // ===== Usage Report (UR) SQL =====

    public static final String SQL_SEL_UNREGISTERED_TRAINEES =
            "SELECT t.traineeId, t.firstName, t.lastName, t.phoneNumber, t.email, " +
            "COUNT(cr.classID) AS regCount " +
            "FROM Trainee t " +
            "LEFT JOIN ClassRegistration cr ON t.traineeId = cr.traineeId " +
            "LEFT JOIN TrainingClass tc ON cr.classID = tc.classId " +
            "AND tc.startTime BETWEEN ? AND ? " +
            "GROUP BY t.traineeId, t.firstName, t.lastName, t.phoneNumber, t.email " +
            "HAVING COUNT(cr.classID) < 7";

}


