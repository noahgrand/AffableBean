/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */

package controller;

import entity.Customer;
import entity.CustomerOrder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.http.HttpSession;
import session.CategoryFacade;
import session.CustomerFacade;
import session.CustomerOrderFacade;
import session.OrderManager;

/**
 *
 * @author tgiunipero
 */
@WebServlet(name = "AdminServlet",
            urlPatterns = {"/admin/",
                           "/admin/viewOrders",
                           "/admin/viewCustomers",
                           "/admin/customerRecord",
                           "/admin/orderRecord",
                           "/admin/bestandesverwaltung",
                           "/admin/changekategorie",
                           "/admin/kategorienverwaltung",
                           "/admin/artikelverwaltung",
                           "/admin/changeartikel",
                           "/admin/logout"})
/*@ServletSecurity(
    @HttpConstraint(transportGuarantee = TransportGuarantee.CONFIDENTIAL,
                    rolesAllowed = {"affableBeanAdmin"})
)*/
public class AdminServlet extends HttpServlet {

    @EJB
    private OrderManager orderManager;
    @EJB
    private CustomerFacade customerFacade;
    @EJB
    private CustomerOrderFacade customerOrderFacade;
    @EJB
    private CategoryFacade categoryFacade;


    private String userPath;
    private Customer customer;
    private CustomerOrder order;
    private List orderList = new ArrayList();
    private List customerList = new ArrayList();
    private List categoryList =new ArrayList();


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        userPath = request.getServletPath();

        // if viewCustomers is requested
        if (userPath.equals("/admin/viewCustomers")) {
            customerList = customerFacade.findAll();
            request.setAttribute("customerList", customerList);
        }

	// if kategorienVerwaltung is requested
        if (userPath.equals("/admin/kategorienverwaltung")) {
	    categoryList = categoryFacade.findAll();
            request.setAttribute("categoryList", categoryList);
        }

	// if changekategorie is requested
        if (userPath.equals("/admin/changekategorie")) {
	    categoryList = categoryFacade.findAll();
            String categoryId = request.getQueryString();
	    for(int i=0;i<categoryList.size();i++){
		    if(!((entity.Category)categoryList.get(i)).getName().equals(request.getParameter(Integer.toString(i+1)))){
			  //System.out.println(((entity.Category)categoryList.get(i)).getName()+" ::: "+request.getParameter(Integer.toString(i+1)) );
((entity.Category)categoryList.get(i)).setName(request.getParameter(Integer.toString(i+1)));
categoryFacade.edit((entity.Category)categoryList.get(i));

		    }	    }
		
	    //checkt if eine neue kategorie soll hinzugefügt werden
	    if(!request.getParameter("addthis").equals("Neue Kategorie")){
		    System.out.println(request.getParameter("addthis"));
		    Short newId = 0;
		    newId=Short.parseShort(Integer.toString(((entity.Category)categoryList.get(categoryList.size()-1)).getId()+1));
		    categoryFacade.create(new entity.Category(null,request.getParameter("addthis")));
	    }
	    categoryList = categoryFacade.findAll();
            request.setAttribute("categoryList", categoryList);
        }

	
	


       // if viewOrders is requested
        if (userPath.equals("/admin/viewOrders")) {
            orderList = customerOrderFacade.findAll();
            request.setAttribute("orderList", orderList);
        }

        // if customerRecord is requested
        if (userPath.equals("/admin/customerRecord")) {

            // get customer ID from request
            String customerId = request.getQueryString();

            // get customer details
            customer = customerFacade.find(Integer.parseInt(customerId));
            request.setAttribute("customerRecord", customer);

            // get customer order details
            order = customerOrderFacade.findByCustomer(customer);
            request.setAttribute("order", order);
        }

        // if orderRecord is requested
        if (userPath.equals("/admin/orderRecord")) {

            // get customer ID from request
            String orderId = request.getQueryString();

            // get order details
            Map orderMap = orderManager.getOrderDetails(Integer.parseInt(orderId));

            // place order details in request scope
            request.setAttribute("customer", orderMap.get("customer"));
            request.setAttribute("products", orderMap.get("products"));
            request.setAttribute("orderRecord", orderMap.get("orderRecord"));
            request.setAttribute("orderedProducts", orderMap.get("orderedProducts"));
        }

        // if logout is requested
        if (userPath.equals("/admin/logout")) {
            session = request.getSession();
            session.invalidate();   // terminate session
            response.sendRedirect("/AffableBean/admin/");
            return;
        }

        // use RequestDispatcher to forward request internally
        userPath = "/admin/index.jsp";
        try {
            request.getRequestDispatcher(userPath).forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}