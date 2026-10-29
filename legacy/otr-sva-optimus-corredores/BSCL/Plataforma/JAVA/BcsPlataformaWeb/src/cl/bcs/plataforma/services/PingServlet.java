package cl.bcs.plataforma.services;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Ping
 * Solamente utilizada para medir el throughtput del servidor.
 */
@WebServlet("/Ping")
public class PingServlet extends HttpServlet {
	 
	private static final long serialVersionUID = -4429105610015300803L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public PingServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long sleep_seconds = Long.parseLong(Optional.ofNullable(request.getParameter("sleep")).orElse("0"));
		if(sleep_seconds>0) {
			try {
				Thread.sleep(sleep_seconds*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
