/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class DBContext {

    Connection connection;

    public DBContext() {
        try {
            String user = "se1617-g1";
            String pass = "passmon123!@";
            String url = "jdbc:sqlserver://103.9.158.241:1433;databaseName=OnlineShop2";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void main(String[] args) {
        DBContext bContext = new DBContext();
        if (bContext.connection != null) {
            System.out.println("OK");
        }
        System.out.println(bContext.connection.toString());

    }
}
