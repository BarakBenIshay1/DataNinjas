package com.fitwell.control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.fitwell.entity.DBConst;


public class AuthenticationController {

	private static AuthenticationController instance;
	private AuthenticationController() {}
	
    public static AuthenticationController getInstance() {
        if (instance == null)
            instance = new AuthenticationController();
        return instance;
    }
    
    public boolean traineeExists(int traineeId) {
        String sql = "SELECT (*) FROM Trainee WHERE traineeID = ? AND isActive = true";
        return exists(sql,traineeId
        );
    }
    private boolean exists(String sql, int id) {
        try (
            Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean consultantExists(int consultantId) {
        String sql = "SELECT COUNT(*) FROM FitnessConsultant WHERE consultantID = ?";
        return exists(sql,consultantId);
    }
    public String getConsultantFirstName(int consultantId) {
        String sql = "SELECT firstName FROM FitnessConsultant WHERE consultantID = ?";
        try (
            Connection conn = DriverManager.getConnection(DBConst.CONN_STR);
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, consultantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("firstName");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Consultant";
    }
}
