/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Category;
import model.Order;
import model.Product;
import model.Role;
import model.SubCategory;
import model.User;

/**
 *
 * @author hoan
 */
public class OrderDBContext extends DBContext {

    public ArrayList<Order> getUserOrders(int uid, String startDate, String endDate) {
        try {
            String sql = "WITH \n"
                    + "t as\n"
                    + "(SELECT [Order].id as OrderID, [Order].[date], [Order].totalPrice, [Product].[name] as ProductName, [Order].[status] as OrderStatus\n"
                    + "FROM [Order] inner join OrderDetail ON [Order].id = OrderDetail.orderId\n"
                    + "inner join Product ON OrderDetail.productId = Product.id\n"
                    + "WHERE [Order].userId = ?),\n"
                    + "b as\n"
                    + "(SELECT [Order].id as OrderID, COUNT(Product.id) as NumberOfProducts\n"
                    + "FROM [Order] inner join OrderDetail ON [Order].id = OrderDetail.orderId\n"
                    + "inner join Product ON OrderDetail.productId = Product.id\n"
                    + "WHERE [Order].userId = ? group by [Order].id),\n"
                    + "c as\n"
                    + "(\n"
                    + "SELECT  a.*\n"
                    + "FROM    (\n"
                    + "        SELECT  DISTINCT t.OrderID\n"
                    + "        FROM t\n"
                    + "        ) mo\n"
                    + "CROSS APPLY\n"
                    + "        (\n"
                    + "        SELECT  TOP 1 *\n"
                    + "        FROM    t mi\n"
                    + "        WHERE   mi.OrderID = mo.OrderID\n"
                    + "        ) a\n"
                    + "		)\n"
                    + "Select c.OrderID, c.[date], c.totalPrice, c.ProductName, c.OrderStatus, b.NumberOfProducts from c inner join b on c.OrderID = b.OrderID\n"
                    + "WHERE c.[date] between ? and ?";
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setInt(1, uid);
            stm.setInt(2, uid);
            stm.setString(3, startDate);
            stm.setString(4, endDate);

            ResultSet rs = stm.executeQuery();
            ArrayList<Order> orders = new ArrayList<>();
            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("OrderID"));
                o.setDate(rs.getDate("date"));
                o.setTotalcost(rs.getDouble("totalPrice"));
                o.setStatus(rs.getInt("OrderStatus"));
                o.setNumproducts(rs.getInt("NumberOfProducts"));

                ArrayList<Product> products = new ArrayList<>();
                Product p = new Product();
                p.setName(rs.getString("ProductName"));
                products.add(p);

                o.setProducts(products);
                orders.add(o);
            }
            return orders;
        } catch (SQLException ex) {
            Logger.getLogger(UserDBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public int addOrder(Product[] productsOrder, long total, int idCustomer,
            String email, String shipFullName, String shipAddress, String shipPhone,
            String shipNote, int idPayment, int idSeller) {
        int idOrder = 0;
        try {
            connection.setAutoCommit(false);
            String sqlInsertShip = "INSERT INTO [dbo].[ShipInfo]\n"
                    + "           ([fullname]\n"
                    + "           ,[address]\n"
                    + "           ,[phone]\n"
                    + "           ,[email])\n"
                    + "     VALUES\n"
                    + "           (?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?)";
            PreparedStatement stmInsertShip = connection.prepareStatement(sqlInsertShip);
            stmInsertShip.setString(1, shipFullName);
            stmInsertShip.setString(2, shipAddress);
            stmInsertShip.setString(3, shipPhone);
            stmInsertShip.setString(4, email);
            stmInsertShip.executeUpdate();

            String getIdShipInfo = "SELECT @@IDENTITY AS idShip";
            PreparedStatement stmGetIdShip = connection.prepareStatement(getIdShipInfo);
            ResultSet rs = stmGetIdShip.executeQuery();
            int idShip = 0;
            while (rs.next()) {
                idShip = rs.getInt(1);
            }

            String sqlInsertOrder = "INSERT INTO [dbo].[Order]\n"
                    + "           ([userId]\n"
                    + "           ,[totalPrice]\n"
                    + "           ,[note]\n"
                    + "           ,[status]\n"
                    + "           ,[date]\n"
                    + "           ,[idShip]\n"
                    + "           ,[payment]\n"
                    + "           ,[sellerid])\n"
                    + "     VALUES\n"
                    + "           (?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?)";
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd");
            LocalDate localDate = LocalDate.now();
            Date dateNow = Date.valueOf(dtf.format(localDate).replaceAll("/", "-"));
            PreparedStatement stmInsertOrder = connection.prepareStatement(sqlInsertOrder);
            stmInsertOrder.setInt(1, idCustomer);
            stmInsertOrder.setFloat(2, total);
            stmInsertOrder.setString(3, shipNote);
            stmInsertOrder.setInt(4, 1);
            stmInsertOrder.setDate(5, dateNow);
            stmInsertOrder.setInt(6, idShip);
            stmInsertOrder.setInt(7, idPayment);
            stmInsertOrder.setInt(8, idSeller);
            stmInsertOrder.executeUpdate();

            String getIdOrder = "SELECT @@IDENTITY AS id";
            PreparedStatement stmGetIdOrder = connection.prepareStatement(getIdOrder);
            ResultSet rs2 = stmGetIdOrder.executeQuery();

            while (rs2.next()) {
                idOrder = rs2.getInt(1);
            }
            String sqlInsertProductOrder = "INSERT INTO [dbo].[OrderDetail]\n"
                    + "           ([orderId]\n"
                    + "           ,[productId]\n"
                    + "           ,[discount]\n"
                    + "           ,[quantity]\n"
                    + "           ,[price])\n"
                    + "     VALUES\n"
                    + "           (?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?\n"
                    + "           ,?)";
            for (Product product : productsOrder) {
                PreparedStatement stmInsertProductsOrder = connection.prepareStatement(sqlInsertProductOrder);
                stmInsertProductsOrder.setInt(1, idOrder);
                stmInsertProductsOrder.setInt(2, product.getId());
                stmInsertProductsOrder.setInt(3, product.getDiscount());
                stmInsertProductsOrder.setLong(4, product.getQuantity());
                stmInsertProductsOrder.setLong(5, product.getPrice());
                stmInsertProductsOrder.executeUpdate();
            }
            connection.commit();
        } catch (SQLException ex) {
            idOrder = 0;
            try {
                connection.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(OrderDBContext.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(OrderDBContext.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(OrderDBContext.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(OrderDBContext.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return idOrder;
    }

    public ArrayList<Order> getOrders(String startDate, String endDate) {
        try {
            String sql = "WITH \n"
                    + "t as\n"
                    + "(SELECT [Order].id as OrderID,[User].fullname as CustomerName, [Order].[date], [Order].totalPrice, [Product].[name] as ProductName, [Order].[status] as OrderStatus\n"
                    + "FROM \n"
                    + "[User] inner join [Order] on [User].id = [Order].userId\n"
                    + "inner join OrderDetail ON [Order].id = OrderDetail.orderId\n"
                    + "inner join Product ON OrderDetail.productId = Product.id\n"
                    + "),\n"
                    + "b as\n"
                    + "(SELECT [Order].id as OrderID, COUNT(Product.id) as NumberOfProducts\n"
                    + "FROM [Order] inner join OrderDetail ON [Order].id = OrderDetail.orderId\n"
                    + "inner join Product ON OrderDetail.productId = Product.id\n"
                    + "group by [Order].id),\n"
                    + "c as\n"
                    + "(\n"
                    + "SELECT  a.*\n"
                    + "FROM    (\n"
                    + "        SELECT  DISTINCT t.OrderID\n"
                    + "        FROM t\n"
                    + "        ) mo\n"
                    + "CROSS APPLY\n"
                    + "        (\n"
                    + "        SELECT  TOP 1 *\n"
                    + "        FROM    t mi\n"
                    + "        WHERE   mi.OrderID = mo.OrderID\n"
                    + "        ) a\n"
                    + "		)\n"
                    + "Select c.OrderID,c.CustomerName, c.[date], c.totalPrice, c.ProductName, c.OrderStatus, b.NumberOfProducts from c inner join b on c.OrderID = b.OrderID\n"
                    + "\n"
                    + "WHERE c.[date] between ? and ?";
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1, startDate);
            stm.setString(2, endDate);

            ResultSet rs = stm.executeQuery();
            ArrayList<Order> orders = new ArrayList<>();
            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("OrderID"));
                o.setDate(rs.getDate("date"));
                o.setTotalcost(rs.getDouble("totalPrice"));
                o.setStatus(rs.getInt("OrderStatus"));
                o.setNumproducts(rs.getInt("NumberOfProducts"));
                o.setBuyer(rs.getString("CustomerName"));
                ArrayList<Product> products = new ArrayList<>();
                Product p = new Product();
                p.setName(rs.getString("ProductName"));
                products.add(p);

                o.setProducts(products);
                orders.add(o);
            }
            return orders;
        } catch (SQLException ex) {
            Logger.getLogger(UserDBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Order getInformationOfOrderByID(int orderID) {
        try {
            String sql = " select DISTINCT  od.id, od.date, od.status, od.totalPrice\n"
                    + "from\n"
                    + "[Order] od WHERE od.id = ? ";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setDate(rs.getDate("date"));
                order.setStatus(rs.getInt("status"));
                order.setTotalcost(rs.getDouble("totalPrice"));
                return order;
            }
        } catch (SQLException ex) {
            Logger.getLogger(OrderDBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public User getUserOrderInformation(int orderID) {
        try {
            String sql = " select DISTINCT  s.fullname, u.gender, s.email, s.phone\n"
                    + "from\n"
                    + "[Order] od\n"
                    + " INNER JOIN  ShipInfo s ON od.idShip = s.id\n"
                    + " INNER JOIN [User] u on od.userId = u.id\n"
                    + "WHERE od.id = ? ";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setFullname(rs.getString("fullname"));
                user.setGender(rs.getBoolean("gender"));
                user.setEmail(rs.getString("email"));
                user.setMobile(rs.getString("phone"));
                return user;
            }
        } catch (SQLException ex) {
            Logger.getLogger(OrderDBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ArrayList<Product> getListOrderProductOfUser(int orderID) {
        ArrayList<Product> listProduct = new ArrayList<>();
        String sql = " select p.thumbnail, p.name as pname, c.name as categoryName, o.quantity, o.discount, o.price\n"
                + "from\n"
                + "Product p\n"
                + " INNER JOIN OrderDetail o ON o.productId = p.id\n"
                + " INNER JOIN SubCategory sub ON p.subCategoryId = sub.id\n"
                + " INNER JOIN Category c ON sub.categoryId = c.id\n"
                + "WHERE o.orderId = ? ";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setThumbnail(rs.getString("thumbnail"));
                product.setName(rs.getString("pname"));
                product.setQuantity(rs.getLong("quantity"));
                product.setDiscount(rs.getInt("discount"));
                product.setPrice(rs.getLong("price"));
                
                Category category = new Category();
                category.setName(rs.getString("categoryName"));
                
                SubCategory subCategory = new SubCategory();
                subCategory.setCategory(category);
                
                product.setSubCategory(subCategory);
                
                listProduct.add(product);
            }
        } catch (SQLException ex) {
            Logger.getLogger(OrderDBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listProduct;
    }

}
