package test.billy.jee.tc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet0 extends HttpServlet {
    private static final long serialVersionUID = 1L;

	@Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
	    service0(req, resp);
    }

	private void service0(HttpServletRequest req, HttpServletResponse resp) {
	    String id = req.getParameter("id");
	    System.out.println("id=" + id);
    }
    

}
