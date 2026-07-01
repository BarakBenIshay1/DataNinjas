package com.fitwell.control;


import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import com.fitwell.entity.DBConst;
import com.fitwell.entity.EquipmentReportItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fitwell.entity.DBConst.CONN_STR;

public class ReportController {

    private static ReportController instance;

    private ReportController() {}

    public static ReportController getInstance() {
        if (instance == null)
            instance = new ReportController();
        return instance;
    }

    public void generateLowParticipationReport(java.util.Date from, java.util.Date to) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Dates must not be null");

        if (to.before(from))
            throw new IllegalArgumentException("End date must be after start date");

        try (Connection conn = DriverManager.getConnection(CONN_STR)) {

            InputStream jasperStream = getClass().getResourceAsStream("/com/fitwell/boundary/LowParticipationReport.jasper");

            if (jasperStream == null) {
                throw new RuntimeException("LowParticipationReport.jasper not found in classpath (under boundary)");
            } 
            

            Map<String, Object> params = new HashMap<>();
            params.put("fromDate", new java.sql.Date(from.getTime()));
            params.put("toDate", new java.sql.Date(to.getTime()));

            JasperPrint print = JasperFillManager.fillReport(jasperStream, params, conn);
            JasperViewer.viewReport(print, false);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Low Participation Report", e);
        }
    }


    public List<EquipmentReportItem> getEquipmentInventoryReportData() {
        List<EquipmentReportItem> reportData = new ArrayList<>();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Timestamp startOfYear = Timestamp.valueOf(now.withDayOfYear(1).toLocalDate().atStartOfDay());
        Timestamp endOfYear = Timestamp.valueOf(now.withDayOfYear(1).plusYears(1).toLocalDate().atStartOfDay());


        String sql = 
            "SELECT et.name, et.categoryId, COUNT(filtered_cea.classID) as usageCount " +
            "FROM EquipmentType et " +
            "LEFT JOIN (" +
            "    SELECT cea.equipmentTypeID, cea.classID " +
            "    FROM ClassEquipmentAssignment cea " +
            "    INNER JOIN TrainingClass tc ON cea.classID = tc.classId " +
            "    WHERE tc.startDateTime >= ? AND tc.startDateTime < ? " +
            ") filtered_cea ON et.equipmentTypeID = filtered_cea.equipmentTypeID " +
            "GROUP BY et.equipmentTypeID, et.name, et.categoryId " +
            "ORDER BY usageCount DESC";

        try (Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, startOfYear);
            ps.setTimestamp(2, endOfYear);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reportData.add(new EquipmentReportItem(
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("usageCount")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reportData;
    }

    
    public boolean exportReportToXML(String filePath) {
        List<EquipmentReportItem> data = getEquipmentInventoryReportData();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("SwiftFitInventoryReport");
            doc.appendChild(rootElement);
            rootElement.setAttribute("generatedAt", new java.util.Date().toString());

            for (EquipmentReportItem item : data) {
                Element itemElement = doc.createElement("EquipmentItem");
                
                Element name = doc.createElement("Name");
                name.appendChild(doc.createTextNode(item.getTypeName()));
                itemElement.appendChild(name);

                Element category = doc.createElement("Category");
                category.appendChild(doc.createTextNode(item.getCategory() != null ? item.getCategory() : "General"));
                itemElement.appendChild(category);

                Element usage = doc.createElement("TimesUsed");
                usage.appendChild(doc.createTextNode(String.valueOf(item.getTimesUsed())));
                itemElement.appendChild(usage);

                rootElement.appendChild(itemElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));

            transformer.transform(source, result);
            System.out.println("XML Exported successfully to: " + filePath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}