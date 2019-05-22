package la.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import la.bean.CartBean;
import la.bean.CustomerBean;
import la.bean.ItemBean;

public class OrderDAO {
	private Connection con;

	public OrderDAO() throws DAOException {
		getConnection();
	}

	public int saveOrder(CustomerBean customer, CartBean cart)
			throws DAOException {
		if (con == null)
			getConnection();

		PreparedStatement st = null;
		ResultSet rs = null;

		int lastInsertId = 0;

		try {
			// 顧客情報の追加SQL文
			String sql = "INSERT INTO customer(name, address, tel, email) VALUES(?, ?, ?, ?)";
			// PreparedStatementオブジェクトの取得
			st = con.prepareStatement(sql);
			// プレースホルダーの設定
			st.setString(1, customer.getName());
			st.setString(2, customer.getAddress());
			st.setString(3, customer.getTel());
			st.setString(4, customer.getEmail());
			// SQLの実行
			st.executeUpdate();
			rs = st.executeQuery("SELECT LAST_INSERT_ID() AS LAST");
			if (rs != null && rs.next()) {
				lastInsertId = rs.getInt("LAST");
			}
			st.close();

			// 注文情報のOrderedテーブルへの追加
			sql = "INSERT INTO ordered(customer_code, ordered_date, total_price) VALUES(?, ?, ?)";
			st = con.prepareStatement(sql);
			// プレースホルダーの設定
			st.setInt(1, lastInsertId);
			Date today = new Date(System.currentTimeMillis());
			st.setDate(2, today);
			st.setInt(3, cart.getTotal());
			// SQLの実行
			st.executeUpdate();
			rs = st.executeQuery("SELECT LAST_INSERT_ID() AS LAST");
			if (rs != null && rs.next()) {
				lastInsertId = rs.getInt("LAST");
			}
			st.close();

			// 注文明細情報のOrderedDetailテーブルへの追加
			// 商品ごとに複数レコード追加
			sql = "INSERT INTO ordered_detail(order_code, item_code, num) VALUES(?, ?, ?)";
			st = con.prepareStatement(sql);
			Map<Integer, ItemBean> items = cart.getItems();
			Collection<ItemBean> list = items.values();
			for (ItemBean item : list) {
				st.setInt(1, lastInsertId); // XXXXXXX
				st.setInt(2, item.getCode());
				st.setInt(3, item.getQuantity());
				st.executeUpdate();
			}
			st.close();
			return lastInsertId;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException("レコードの操作に失敗しました。");
		} finally {
			try {
				// リソースの開放
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				close();
			} catch (Exception e) {
				throw new DAOException("リソースの開放に失敗しました。");
			}
		}
	}

	private void getConnection() throws DAOException {
		try {
			// JDBCドライバの登録
//			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("com.mysql.cj.jdbc.Driver");
			// URL、ユーザ名、パスワードの設定
			String url = "jdbc:mysql://localhost/shopping";
			String user = "jojo";
			String pass = "jojo";
			// データベースへの接続
			con = DriverManager.getConnection(url, user, pass);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException("接続に失敗しました。");
		}
	}

	private void close() throws SQLException {
		if (con != null) {
			con.close();
			con = null;
		}
	}
}
