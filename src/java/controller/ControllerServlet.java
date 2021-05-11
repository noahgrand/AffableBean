/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package controller;

import cart.ShoppingCart;
import cart.ShoppingCartItem;
import entity.Category;
import entity.Customer;
import entity.Product;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import session.CategoryFacade;
import session.CustomerFacade;
import session.OrderManager;
import session.ProductFacade;
import validate.Validator;

/**
 *
 * @author tgiunipero
 */
@WebServlet(name = "Controller",
        loadOnStartup = 1,
        urlPatterns = {"/category",
            "/addToCart",
            "/viewCart",
            "/updateCart",
            "/checkout",
            "/purchase",
            "/loginpage",
            "/registrationpage",
            "/LoginBean",
            "/Register",
            "/chooseLanguage"})
public class ControllerServlet extends HttpServlet {

    private String surcharge;
    private String password;

	
    @EJB
    private CategoryFacade categoryFacade;
    @EJB
    private ProductFacade productFacade;
    @EJB
    private OrderManager orderManager;
    @EJB
    private CustomerFacade customerFacade;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);

        // initialize servlet with configuration information
        surcharge = servletConfig.getServletContext().getInitParameter("deliverySurcharge");

        // store category list in servlet context
        getServletContext().setAttribute("categories", categoryFacade.findAll());
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

        String userPath = request.getServletPath();
        HttpSession session = request.getSession();
        Category selectedCategory;
        Collection<Product> categoryProducts;

        // if category page is requested
        if (userPath.equals("/category")) {

            // get categoryId from request
            String categoryId = request.getQueryString();

            if (categoryId != null) {

                // get selected category
                selectedCategory = categoryFacade.find(Short.parseShort(categoryId));

                // place selected category in session scope
                session.setAttribute("selectedCategory", selectedCategory);

                // get all products for selected category
                categoryProducts = selectedCategory.getProductCollection();

                // place category products in session scope
                session.setAttribute("categoryProducts", categoryProducts);
            }

            // if cart page is requested
        } else if (userPath.equals("/viewCart")) {

            String clear = request.getParameter("clear");

            if ((clear != null) && clear.equals("true")) {
                ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
				resetReservations(cart);
                cart.clear();
            }

            userPath = "/cart";

            // if checkout page is requested
        } else if (userPath.equals("/checkout")) {
			
            ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

            // calculate total
            cart.calculateTotal(surcharge);

            // forward to checkout page and switch to a secure channel
            // if user switches language
        } else if (userPath.equals("/chooseLanguage")) {

            // get language choice
            String language = request.getParameter("language");

            // place in request scope
            request.setAttribute("language", language);

            String userView = (String) session.getAttribute("view");

            if ((userView != null)
                    && (!userView.equals("/index"))) {     // index.jsp exists outside 'view' folder
                // so must be forwarded separately
                userPath = userView;
            } else {

                // if previous view is index or cannot be determined, send user to welcome page
                try {
                    request.getRequestDispatcher("/index.jsp").forward(request, response);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }
//        else if(userPath.equals("/login")){
//            System.out.println("Test");
//        }

        // use RequestDispatcher to forward request internally
        String url = "/WEB-INF/view" + userPath + ".jsp";

        try {
            request.getRequestDispatcher(url).forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

        request.setCharacterEncoding("UTF-8");  // ensures that user input is interpreted as
        // 8-bit Unicode (e.g., for Czech characters)

        String userPath = request.getServletPath();
        HttpSession session = request.getSession();
        ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
        Validator validator = new Validator();

        // if addToCart action is called
        if (userPath.equals("/addToCart")) {

            // if user is adding item to cart for first time
            // create cart object and attach it to user session
            if (cart == null) {

                cart = new ShoppingCart();
                session.setAttribute("cart", cart);
            }

            // get user input from request
            String productId = request.getParameter("productId");

            if (!productId.isEmpty()) {
                Product product = productFacade.find(Integer.parseInt(productId));
				
				if (product.getStock() > 0) {
					updateStock(product, 1);
					cart.addItem(product);
				}
				else{
					// kein Bestand
					System.out.println("Kein Bestand mehr.");
				}
            }
			
            userPath = "/category";

            // if updateCart action is called
        // if updateCart action is called
        } else if (userPath.equals("/updateCart")) {

            // get input from request
            String productId = request.getParameter("productId");
            String quantity = request.getParameter("quantity");
            boolean invalidEntry = validator.validateQuantity(productId, quantity);
			Product product = productFacade.find(Integer.parseInt(productId));

            if (!invalidEntry) {

				int newQuantity = (-quantityOfProductInCart(product, cart) + Integer.parseInt(quantity));

				if (product.getStock() > newQuantity) {
					updateStock(product, newQuantity);
					cart.update(product, quantity);
				}
				else{
					// Menge zu gross
						System.out.println("KEIN BESTAND");
				}
            }

            userPath = "/cart";

            // if purchase action is called
        } else if (userPath.equals("/LoginBean")) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            List<Customer> customerlist;
            customerlist = customerFacade.findAll();
            if (email.isEmpty() || password.isEmpty()) {
                userPath = "/loginpage";
            } else {
                for (Customer c : customerlist) {
                    if (c.getEmail().equals(email) && c.getPassword().equals(password)) {
                        userPath = "/checkout";
                        this.password = password;
                        break;
                    } else {
                        userPath = "/loginpage";
                    }
                }
            }

        } else if (userPath.equals("/Register")) {
            String email = request.getParameter("email");
            String password = request.getParameter("newpassword");

            if (email.isEmpty() || password.isEmpty()) {
                userPath = "/registrationpage";
            } else {
                customerFacade.addCustomer(" ", email, " ", " ", " ", " ", password);
                userPath = "/loginpage";
            }
        } else if (userPath.equals("/purchase")) {

            if (cart != null) {

                // extract user data from request
                String name = request.getParameter("name");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String address = request.getParameter("address");
                String cityRegion = request.getParameter("cityRegion");
                String ccNumber = request.getParameter("creditcard");

                // validate user data
                boolean validationErrorFlag = false;
                validationErrorFlag = validator.validateForm(name, email, phone, address, cityRegion, ccNumber, request);

                // if validation error found, return user to checkout
                if (validationErrorFlag == true) {
                    request.setAttribute("validationErrorFlag", validationErrorFlag);
                    userPath = "/checkout";

                    // otherwise, save order to database
                } else {

                    int orderId = orderManager.placeOrder(name, email, phone, address, cityRegion, ccNumber, this.password,cart);

                    // if order processed successfully send user to confirmation page
                    if (orderId != 0) {

                        // in case language was set using toggle, get language choice before destroying session
                        Locale locale = (Locale) session.getAttribute("javax.servlet.jsp.jstl.fmt.locale.session");
                        String language = "";

                        if (locale != null) {

                            language = (String) locale.getLanguage();
                        }

                        // dissociate shopping cart from session
                        cart = null;

                        // end session
                        session.invalidate();

                        if (!language.isEmpty()) {                       // if user changed language using the toggle,
                            // reset the language attribute - otherwise
                            request.setAttribute("language", language);  // language will be switched on confirmation page!
                        }

                        // get order details
                        Map orderMap = orderManager.getOrderDetails(orderId);

                        // place order details in request scope
                        request.setAttribute("customer", orderMap.get("customer"));
                        request.setAttribute("products", orderMap.get("products"));
                        request.setAttribute("orderRecord", orderMap.get("orderRecord"));
                        request.setAttribute("orderedProducts", orderMap.get("orderedProducts"));

                        userPath = "/confirmation";

                        // otherwise, send back to checkout page and display error
                    } else {
                        userPath = "/checkout";
                        request.setAttribute("orderFailureFlag", true);
                    }
                }
            }
        }

        // use RequestDispatcher to forward request internally
        String url = "/WEB-INF/view" + userPath + ".jsp";

        try {
            request.getRequestDispatcher(url).forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	private void updateStock(Product product, int number){
		int newStock = product.getStock() - number;
		product.setStock(newStock);
		productFacade.edit(product);
	}
	
	public void resetReservations(ShoppingCart cart){
		ArrayList<ShoppingCartItem> items = (ArrayList<ShoppingCartItem>) cart.getItems();
		
		for(ShoppingCartItem sci : items){
			sci.getProduct().setStock((sci.getProduct().getStock() + sci.getQuantity()) - (sci.getQuantity() - 1));
			productFacade.edit(sci.getProduct());
		}
	}
	
	private int quantityOfProductInCart(Product product, ShoppingCart cart){
		ArrayList<ShoppingCartItem> items = (ArrayList<ShoppingCartItem>) cart.getItems();
		
		for(ShoppingCartItem sci : items){
			if (sci.getProduct().equals(product)) {
				return sci.getQuantity();
			}
		}
			
		return 0;
	}

}
