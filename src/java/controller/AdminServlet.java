/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package controller;

import entity.Category;
import entity.Customer;
import entity.CustomerOrder;
import entity.Product;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.http.HttpSession;
import session.CategoryFacade;
import session.CustomerFacade;
import session.CustomerOrderFacade;
import session.OrderManager;
import session.ProductFacade;

/**
 *
 * @author 
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
	@EJB

	private ProductFacade productFacade;

	private String userPath;
	private Customer customer;
	private CustomerOrder order;
	private List orderList = new ArrayList();
	private List customerList = new ArrayList();
	private List categoryList = new ArrayList();
	private List artikelList = new ArrayList();

	/**
	 * Processes requests for both HTTP <code>GET</code> and
	 * <code>POST</code> methods.
	 *
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

		// if artikelVerwaltung is requested
		if (userPath.equals("/admin/artikelverwaltung")) {
			artikelList = productFacade.findAll();
			request.setAttribute("artikelList", artikelList);
		}

		// if changeartikel is requested
		if (userPath.equals("/admin/changeartikel")) {
			artikelList = productFacade.findAll();

			for (int i = 0; i < artikelList.size(); i++) {
//		    if(!((entity.Product)artikelList.get(i)).getName().equals(request.getParameter(Integer.toString(i+1)))){
				//System.out.println(((entity.Category)categoryList.get(i)).getName()+" ::: "+request.getParameter(Integer.toString(i+1)) );
				((entity.Product) artikelList.get(i)).setName(request.getParameter(Integer.toString(i + 1) + ".1"));
				((entity.Product) artikelList.get(i)).setPrice(new BigDecimal(request.getParameter(Integer.toString(i + 1) + ".2")));
//setStock				((entity.Product) artikelList.get(i)).setName(request.getParameter(Integer.toString(i + 1) + ".1"));
				Category requestedOne= null;
				for(Category c: categoryFacade.findAll()){
					if(c.getName().equals(request.getParameter(Integer.toString(i+1)+".4"))){
						requestedOne=c;
					}
				}
				((entity.Product) artikelList.get(i)).setCategory(
					requestedOne
				);
				productFacade.edit((entity.Product) artikelList.get(i));

//				}
			}

			//checkt if ein neues artikel soll hinzugefügt werden
			if (!(request.getParameter("addthis.1").equals("Neuer Artikel")&&request.getParameter("addthis.2").equals("Price")&&request.getParameter("addthis.3").equals("Stock")&&request.getParameter("addthis.4").equals("Category"))) {
				for(int j=1;j<5;j++){
					System.out.println(request.getParameter("addthis."+j));
				}
				Short newId = 0;
				newId = Short.parseShort(Integer.toString(((entity.Product) artikelList.get(artikelList.size() - 1)).getId() + 1));
				Product tempProd=new Product();
//				tempProd.setId(Integer.parseInt(newId.toString()));
				tempProd.setName(request.getParameter("addthis.1"));
				tempProd.setPrice(new BigDecimal(request.getParameter("addthis.2")));
//When stock is implemented	tempProd.setStock(request.getParameter("addthis.3"));
				Category tempCat=null;
				for(Category c: categoryFacade.findAll()){
				if(c.getName().equals(request.getParameter("addthis.4"))){
					tempCat=c;
				}	
				}
				tempProd.setCategory(tempCat);
				productFacade.create(tempProd);
			}

			artikelList = productFacade.findAll();
			request.setAttribute("artikelList", artikelList);
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
			for (int i = 0; i < categoryList.size(); i++) {
				if (!((entity.Category) categoryList.get(i)).getName().equals(request.getParameter(Integer.toString(i + 1)))) {
					//System.out.println(((entity.Category)categoryList.get(i)).getName()+" ::: "+request.getParameter(Integer.toString(i+1)) );
					((entity.Category) categoryList.get(i)).setName(request.getParameter(Integer.toString(i + 1)));
					categoryFacade.edit((entity.Category) categoryList.get(i));

				}
			}

			//checkt if eine neue kategorie soll hinzugefügt werden
			if (!request.getParameter("addthis").equals("Neue Kategorie")) {
				System.out.println(request.getParameter("addthis"));
				Short newId = 0;
				newId = Short.parseShort(Integer.toString(((entity.Category) categoryList.get(categoryList.size() - 1)).getId() + 1));
				categoryFacade.create(new entity.Category(null, request.getParameter("addthis")));
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
	 *
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
	 *
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
