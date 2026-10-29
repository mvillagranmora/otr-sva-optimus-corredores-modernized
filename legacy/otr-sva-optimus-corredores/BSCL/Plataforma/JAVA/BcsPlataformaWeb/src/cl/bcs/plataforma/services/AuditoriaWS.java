package cl.bcs.plataforma.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import cl.bcs.utils.Fecha;

@WebServlet("/AuditoriaWS")
public class AuditoriaWS extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//private static final Logger logger = LogManager.getLogger(AuditoriaWS.class.getName());
	@Resource(name = "jdbc/hanaDB")
	DataSource dataSource;
	
	public AuditoriaWS() {
		super();
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			performTask(request, response);
		} catch (SQLException e) {
			e.printStackTrace();			
		}
	}
	
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		StringBuilder sb = new StringBuilder();
        String s;
		try(InputStream inputStream = request.getInputStream()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream , StandardCharsets.UTF_8));
            while ((s = reader.readLine()) != null) {
                sb.append(s);
            }
        }
        String bodyParms=sb.toString();
		String[] params = bodyParms.split("@@@");
		if (params.length != 7) {
			response.setStatus(400);
			response.getOutputStream().write("ERROR".getBytes("UTF-8"));	
			response.getOutputStream().flush();
			return;
		}
		try (Connection conn = dataSource.getConnection()) {
			
			final String insert = "INSERT INTO \"BSCL.Plataforma.BD::PLA.AuditoriaWebServices\"(\"Servicio\",\"Entrada\",\"Salida\",\"FechaInicio\",\"FechaFin\",\"IpOrigen\",\"HTTPCode\") VALUES(?,?,?,?,?,?,?)";
			try (PreparedStatement p = conn.prepareStatement(insert)) {
				String servicio = params[0];
				String entrada = params[1];
				String salida = params[2];
				Timestamp fechainicio = Fecha.toTimeStamp(params[3]);
				
				Timestamp fechafin = Fecha.toTimeStamp(params[4]);
				
				String iporigen = params[5];
				p.setString(1,servicio);
				
				p.setString(2, entrada);
				
				p.setString(3, salida);
				p.setTimestamp(4, fechainicio);
				p.setTimestamp(5, fechafin);
				p.setString(6, iporigen);
				p.setInt(7, Integer.parseInt(params[6]));
				p.execute();
				conn.commit();
			}
		}
		
		finally {
		}
	}

}
