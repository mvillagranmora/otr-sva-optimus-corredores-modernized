package cl.bcs.tesoreria.webservices.recaudacion;

import javax.servlet.annotation.WebServlet;

import org.apache.cxf.transport.servlet.CXFServlet;

@WebServlet(urlPatterns="/services/*",loadOnStartup=1)
public class CXFStartServlet extends CXFServlet {
	public CXFStartServlet(){
		super();
	}
	
}
