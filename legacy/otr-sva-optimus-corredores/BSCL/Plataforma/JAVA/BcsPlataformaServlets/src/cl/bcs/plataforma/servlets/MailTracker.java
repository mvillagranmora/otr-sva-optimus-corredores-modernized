package cl.bcs.plataforma.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;

import cl.bcs.plataforma.evt.pojo.Evento;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

@WebServlet("/mail/*")

public class MailTracker extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Resource(name = "jdbc/hanaDB")
    private DataSource dataSource;

    @Override
    public void doGet(final HttpServletRequest request,final HttpServletResponse response) throws IOException {
    	
    	try {
    		Connection conn = dataSource.getConnection();
    		
    		String update = "UPDATE \"BSCL.Normativa.BD::CIR.Nomina\" SET \"IdEstadoNotificacion\" = 'ENT' WHERE \"IdEvento\" = :IdEvento AND \"IdEstadoNotificacion\" != 'ENT'";
    		String idEvento = request.getParameter("r");
    		if (idEvento == null) {
    			throw new NullPointerException();
    		}
    		SQL _sql = new SQL();
    		Evento evt = new Evento();
    		List<Evento> eventos = new ArrayList<>();
    		
    		String idEventoDecodificado = Utils.decodificarEvento(idEvento);
    		evt.IdEvento = Long.parseLong(idEventoDecodificado);
    		eventos.add(evt);
    		_sql.batch(conn, update, eventos, Evento.class);
    		System.out.println("Evento actualizado:" + idEventoDecodificado);
    		conn.commit();
    	}
    	catch(NumberFormatException e) {
    		System.out.println("Evento incorrecto");
    		e.printStackTrace();
    	}
    	catch(NullPointerException e) {
    		System.out.println("No se envio evento");
    		e.printStackTrace();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	byte[] trackingGif = { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x1, 0x0, 0x1, 0x0, (byte) 0x80, 0x0, 0x0,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x0, 0x0, 0x0, 0x2c, 0x0, 0x0, 0x0, 0x0, 0x1, 0x0, 0x1, 0x0,
                0x0, 0x2, 0x2, 0x44, 0x1, 0x0, 0x3b };

        response.setContentType("image/gif");
        response.setContentLength(trackingGif.length);

        OutputStream out;
        out = response.getOutputStream();
		out.write(trackingGif);
        out.close();
    	
    }
    
    public static void main(String[] args) {
    	String idEventoDecodificado = Utils.decodificarEvento("2323");
		
    }
        
}
