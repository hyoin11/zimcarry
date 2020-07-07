package com.zimcarry.book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.simple.JSONObject;

import com.zimcarry.db.DBConn;

public class BookDAO {
	Connection conn;
	PreparedStatement pstmt;
	ResultSet rs;
	
	@SuppressWarnings("unchecked")
	public String checkBook(String bHp, String reBookidx) {
		JSONObject jobj = new JSONObject();
		jobj.put("isData", "no");
		try {
			conn = DBConn.getConnection();
			String sql = "SELECT b_idx, b_name, b_hp, b_start, b_end, b_startdate, b_enddate, b_isreview FROM tb_book WHERE b_idx = ? AND b_hp = ? ORDER BY b_idx DESC";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, reBookidx);
			pstmt.setString(2, bHp);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				jobj.clear();
				jobj.put("isData", "yes");
				jobj.put("bIdx", rs.getString("b_idx"));
				jobj.put("bName", rs.getString("b_name"));
				jobj.put("bHp", rs.getString("b_hp"));
				jobj.put("bStart", rs.getString("b_start"));
				jobj.put("bEnd", rs.getString("b_end"));
				jobj.put("bStartdate", rs.getString("b_startdate"));
				jobj.put("bEnddate", rs.getString("b_enddate"));
				jobj.put("bIsreview", rs.getString("b_isreview"));
				return jobj.toString();
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jobj.toString();
	}
}
