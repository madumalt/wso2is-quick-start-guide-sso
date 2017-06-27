package org.wso2.is.sso.quickStartGuide;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thilina on 6/22/17.
 */
public class ForwardingServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Forwarding Servelet....................!!!!!!!!!!!!!!!!!! ");
//        if((Boolean)req.getAttribute("shouldDirectToLoginPage") || req.getRequestURI().endsWith("/logout")){
//            req.getRequestDispatcher("index.jsp").forward(req, resp);
//        } else {
//            req.getRequestDispatcher("home.jsp").forward(req, resp);
//        }
//        if(req.getRequestURI().endsWith("home.jsp")){
//            req.getRequestDispatcher("home.jsp").forward(req, resp);
//        }


    }
}
