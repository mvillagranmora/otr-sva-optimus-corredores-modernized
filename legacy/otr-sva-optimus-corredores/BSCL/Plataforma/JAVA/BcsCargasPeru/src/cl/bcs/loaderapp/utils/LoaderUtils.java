package cl.bcs.loaderapp.utils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

public class LoaderUtils {
	
    public static String loadResourceToString(final String path){         
        final InputStream stream = LoaderUtils.class.getClassLoader().getResourceAsStream(path);
        if (stream==null) {
            throw new IllegalStateException("can't load resource:"+path);
        }
        try{
            return IOUtils.toString(stream);
        } catch(final Exception e){
            throw new IllegalStateException(e);
        } finally{
            IOUtils.closeQuietly(stream);
        }
    }
    
    public static BigDecimal analizarNumero(String valor) throws ParseException {
		BigDecimal resultado = null;
		
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator(',');
		
		DecimalFormat format= (DecimalFormat) DecimalFormat.getInstance();
		format.setDecimalFormatSymbols(otherSymbols);
		
		if(valor!=null && !valor.equalsIgnoreCase("null")) {
			resultado = new BigDecimal(format.parse(valor).toString());
		}else {
			resultado = BigDecimal.valueOf(0.0);
		}
		return resultado;
	}
    
    public void log(Connection conn, String fechaSistema, String codigoPais, String entidad, int linea, String mensaje, String estado, String data) {
//		new Thread(() ->{
			try {
				String INSERT = "INSERT INTO \"BSCL.Plataforma.BD::PLA.LogCargaMasiva\" VALUES(?, ?, ?, ?, ?, ?, ?, CURRENT_UTCTIMESTAMP);";
			    try (PreparedStatement pStatement = conn.prepareStatement(INSERT)) {
			        pStatement.setString(1, fechaSistema);
			        pStatement.setString(2, entidad);
			        pStatement.setInt(3, linea);
			        pStatement.setString(4, codigoPais);
			        pStatement.setString(5, (mensaje!=null && mensaje.length()>2000) ? mensaje.substring(0, 2000) : mensaje);
			        pStatement.setString(6, estado);
			        pStatement.setString(7, data);
			        pStatement.executeUpdate();
			    }
				System.out.println(mensaje);
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
//		});
    }
}
