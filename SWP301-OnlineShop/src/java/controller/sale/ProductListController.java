/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.sale;

import dal.CategoryDBContext;
import dal.ProductDBContext;
import filter.BaseAuthController;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Category;
import model.Product;
import model.SubCategory;
import model.User;

/**
 *
 * @author Admin
 */
@WebServlet(name = "SaleProductListController", urlPatterns = {"/sale/productlist"})
public class ProductListController extends BaseAuthController {

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void processGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        int sellerId = user.getId();

        String tempCategoryId = request.getParameter("categoryId");
        String tempSubCategoryId = request.getParameter("subCategoryId");
        String tempStatus = request.getParameter("status");
        String tempFeatured = request.getParameter("featured");
        String tempSearch = request.getParameter("search");
        String tempOrderBy = request.getParameter("orderBy");
        String tempSort = request.getParameter("sort");
        int categoryId, subCategoryId;
        String status, featured, search, orderBy, sort;
        if (tempCategoryId == null) {
            categoryId = 0;
        } else {
            categoryId = Integer.parseInt(request.getParameter("categoryId"));
        }

        if (tempSubCategoryId == null) {
            subCategoryId = 0;
        } else {
            subCategoryId = Integer.parseInt(request.getParameter("subCategoryId"));
        }

        if (tempStatus == null) {
            featured = "all";
        } else {
            featured = request.getParameter("featured");
        }

        if (tempFeatured == null) {
            status = "all";
        } else {
            status = request.getParameter("status");
        }

        if (tempSearch == null) {
            search = "";
        } else {
            search = request.getParameter("search");
        }

        if (tempOrderBy == null) {
            orderBy = "id";
        } else {
            orderBy = request.getParameter("orderBy");
        }

        if (tempSort == null) {
            sort = "asc";
        } else {
            sort = request.getParameter("sort");
        }

        String alter = request.getParameter("alter");
        request.setAttribute("alter", alter);

        CategoryDBContext categoryDB = new CategoryDBContext();
        ArrayList<Category> categorys = categoryDB.getAllCategory();
        ArrayList<SubCategory> subCategorys = new ArrayList<>();
        if (tempCategoryId == null) {
            subCategorys = categoryDB.getSubCatgory(categorys.get(0).getId());
        } else {
            subCategorys = categoryDB.getSubCatgory(categoryId);
        }
        ProductDBContext productDB = new ProductDBContext();

        ArrayList<Product> listAllProductFilter = new ArrayList<>();
        if (user.getRole().getName().equals("SaleManager")) {
            listAllProductFilter = productDB.getListProductFilterBySaleManage(categoryId, subCategoryId, status, featured, search, orderBy, sort);
        } else {
            listAllProductFilter = productDB.getListProductFilterBySaleId(categoryId, subCategoryId, status, featured, search, orderBy, sort, sellerId);

        }
//        int totalRecord = productDB.getTotalRecord();
        int totalRecord = listAllProductFilter.size();
        int page, numberRecordPerPage = 5;
        int totalPage = totalRecord % numberRecordPerPage == 0
                ? totalRecord / numberRecordPerPage : totalRecord / numberRecordPerPage + 1;
        String currentPage = request.getParameter("xpage");
        if (currentPage == null) {
            page = 1;
        } else {
            page = Integer.parseInt(currentPage);
        }
        int startRecord = (page - 1) * numberRecordPerPage;
        int endRecord = Math.min(page * numberRecordPerPage, totalRecord);
        ArrayList<Product> listPaging = productDB.getListProductByPage(listAllProductFilter, startRecord, endRecord);

        request.setAttribute("categorys", categorys);
        request.setAttribute("subCategorys", subCategorys);
        request.setAttribute("categoryId", categoryId);
        request.setAttribute("subCategoryId", subCategoryId);
        request.setAttribute("status", status);
        request.setAttribute("featured", featured);
        request.setAttribute("search", search);
        request.setAttribute("orderBy", orderBy);
        request.setAttribute("sort", sort);
        request.setAttribute("page", page);
        request.setAttribute("totalPage", totalPage);
        request.setAttribute("products", listPaging);

        request.getRequestDispatcher("../view/sale/productList.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void processPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
