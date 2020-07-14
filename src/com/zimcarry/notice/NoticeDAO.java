package com.zimcarry.notice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.zimcarry.db.DBConn;
import com.zimcarry.util.FileService;

public class NoticeDAO {
	private Connection conn;
	private PreparedStatement pstmt;
	private ResultSet rs;
	
	public NoticeDTO insertFileNotice(NoticeDTO noticeDTO) {
		String[] generatedColumns = {"no_idx"};
		try {
			conn = DBConn.getConnection();
			String sql = "INSERT INTO tb_notice (no_title, no_content, no_writer, no_filename, no_hidden) VALUES (?, ?, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql, generatedColumns);
			pstmt.setString(1, noticeDTO.getNoTitle());
			pstmt.setString(2, noticeDTO.getNoContent());
			pstmt.setString(3, noticeDTO.getNoWriter());
			pstmt.setString(4, noticeDTO.getNoFilename());
			pstmt.setString(5, noticeDTO.getNoHidden());
			int result = pstmt.executeUpdate();
			
			try (ResultSet geneResultKey = pstmt.getGeneratedKeys()){
				if(geneResultKey.next()) {
					noticeDTO.setNoIdx(geneResultKey.getLong("noIdx"));
				}
			}
			if (result > 0) {
				noticeDTO = this.updateFilepath(noticeDTO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt, rs);
		}
		return noticeDTO;
	}
	
	public boolean insertNotice(String noTitle, String noWriter, String noContent, String noHidden) {
		try {
			conn = DBConn.getConnection();
			String sql = "INSERT INTO tb_notice (no_title, no_content, no_writer, no_hidden) VALUES (?, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, noTitle);
			pstmt.setString(2, noContent);
			pstmt.setString(3, noWriter);
			pstmt.setString(4, noHidden);
			int result = pstmt.executeUpdate();
			if (result > 0) {
				System.out.println("파일없는 인서트");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt);
		}
		return true;
	}
	
	public NoticeDTO updateFilepath(NoticeDTO noticeDTO) {
		try {
			String sql = "UPDATE tb_notice SET no_filepath = ? WHERE no_idx = ?";
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql);
			String tmp = "/" + FileService.getToday() + "/" + noticeDTO.getNoIdx() + ".zim";
			noticeDTO.setNoFilepath(tmp);
			pstmt.setString(1, tmp);
			pstmt.setLong(2, noticeDTO.getNoIdx());
			pstmt.executeUpdate();
			System.out.println("tmp = " + tmp);
			System.out.println(noticeDTO);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt);
		}
		return noticeDTO;
	}
	public boolean editNoice(String noIdx, String noTitle, String noWriter, String noContent, String noHidden) {
		try {
			String sql = "UPDATE tb_notice SET no_title = ?, no_writer = ?, no_content = ?, no_hidden = ? WHERE no_idx = ?";
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, noTitle);
			pstmt.setString(2, noWriter);
			pstmt.setString(3, noContent);
			pstmt.setString(4, noHidden);
			pstmt.setString(5, noIdx);
			int result = pstmt.executeUpdate();
			if (result > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public NoticeDTO editFileNotice(NoticeDTO noticeDTO) {
		try {
			conn = DBConn.getConnection();
			String sql = "UPDATE tb_notice SET no_title = ?, no_content = ?, no_writer = ?, no_filename = ?, no_filepath = ?, no_hidden = ? WHERE no_idx = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, noticeDTO.getNoTitle());
			pstmt.setString(2, noticeDTO.getNoContent());
			pstmt.setString(3, noticeDTO.getNoWriter());
			pstmt.setString(4, noticeDTO.getNoFilename());
			pstmt.setString(5, noticeDTO.getNoFilepath());
			pstmt.setString(6, noticeDTO.getNoHidden());
			pstmt.setLong(7, noticeDTO.getNoIdx());
			int result = pstmt.executeUpdate();
			if (result > 0) {
				return noticeDTO;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return noticeDTO;
	}
	
	public List<NoticeDTO> getNoticeList(String allList, String limit) {
		List<NoticeDTO> noticeList = new ArrayList<NoticeDTO>();
		String sql = "";
		try {
			conn = DBConn.getConnection();
			if (allList.equals("y")) {
				sql = "SELECT no_idx, no_title, no_writer, no_writedate, no_hit, no_hidden FROM tb_notice ORDER BY no_idx DESC LIMIT " + limit;
			} else if (allList.equals("n")) {
				sql = "SELECT no_idx, no_title, no_writer, no_writedate, no_hit, no_hidden FROM tb_notice WHERE no_hidden = 'n' ORDER BY no_idx DESC LIMIT " + limit;
			}
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				NoticeDTO notice = new NoticeDTO();
				notice.setNoIdx(rs.getLong("no_idx"));
				notice.setNoTitle(rs.getString("no_title"));
				notice.setNoWriter(rs.getString("no_writer"));
				notice.setNoWritedate(rs.getDate("no_writedate"));
				notice.setNoHit(rs.getLong("no_hit"));
				notice.setNoHidden(rs.getString("no_hidden"));
				noticeList.add(notice);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt, rs);
		}
		return noticeList;
	}
	
	//숨겨진 글 빼고 개수 카운트
	public int noticeListSize(String hidden) {
		int size = 0;
		try {
			conn = DBConn.getConnection();
			String sql = "SELECT COUNT(no_idx) AS total FROM tb_notice WHERE no_hidden = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, hidden);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				size = rs.getInt("total");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt, rs);
		}
		return size;
	}
	
	//파라미터가 없으면 숨김 여부 상관없이 모든 글 개수 카운트
	public int noticeListSize() {
		int size = 0;
		try {
			conn = DBConn.getConnection();
			String sql = "SELECT COUNT(no_idx) AS total FROM tb_notice";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				size = rs.getInt("total");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt, rs);
		}
		return size;
	}
	
	@SuppressWarnings("unchecked")
	public String noticeDataToJSON(String no_idx) {
		String no_title = "", no_content = "", no_writer = "", no_writedate = "", no_hit = "", no_filename = "", no_hidden = "";
		try {
			conn = DBConn.getConnection();
			String sql = "SELECT no_title, no_content, no_writer, no_writedate, no_hit, no_filename, no_hidden FROM tb_notice WHERE no_idx = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, no_idx);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				no_title = rs.getString("no_title");
				no_content = rs.getString("no_content");
				no_writer = rs.getString("no_writer");
				no_writedate = rs.getString("no_writedate");
				no_hit = rs.getString("no_hit");
				no_filename = rs.getString("no_filename");
				no_hidden = rs.getString("no_hidden");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt, rs);
		}
		JSONObject jobj = new JSONObject();
		jobj.put("no_title", no_title);
		jobj.put("no_content", no_content);
		jobj.put("no_writer", no_writer);
		jobj.put("no_writedate", no_writedate);
		jobj.put("no_hit", no_hit);
		jobj.put("no_filename", no_filename);
		jobj.put("no_hidden", no_hidden);
		
		return jobj.toString();
	}
	
	public NoticeDTO viewNotice(String noIdx) {
		NoticeDTO noticeDTO = null;
		
		try {
			conn = DBConn.getConnection();
			String sql = "UPDATE tb_notice SET no_hit = no_hit + 1 WHERE no_idx = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, noIdx);
			rs = pstmt.executeQuery();
			
			sql = "SELECT no_idx, no_title, no_content, no_writer, no_writedate, no_hit FROM tb_notice WHERE no_idx = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, noIdx);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				noticeDTO = new NoticeDTO();
				noticeDTO.setNoIdx(Long.parseLong(noIdx));
				noticeDTO.setNoTitle(rs.getString("no_title"));
				noticeDTO.setNoContent(rs.getString("no_content"));
				noticeDTO.setNoWriter(rs.getString("no_writer"));
				noticeDTO.setNoWritedate(rs.getDate("no_writedate"));
				noticeDTO.setNoHit(rs.getLong("no_hit"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConn.close(conn, pstmt, rs);
		}
		return noticeDTO;
	}
}
