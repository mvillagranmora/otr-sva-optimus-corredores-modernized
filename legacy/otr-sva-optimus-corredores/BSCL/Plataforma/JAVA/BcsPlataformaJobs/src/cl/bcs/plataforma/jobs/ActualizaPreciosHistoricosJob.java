package cl.bcs.plataforma.jobs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
/**
 * Actualizar instrumentos ruebas -> optimus
 * IdPrograma: ACTUALIZAR_INSTRUMENTOS
 * @author chernandez
 *
 */
public class ActualizaPreciosHistoricosJob extends BcsJob implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ActualizaPreciosHistoricosJob.class);
	//Futuros
	private final String SQL_SELECT_TBRVPCDI = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaPreciosHistoricosJob_SelectTBRVPCDI.sql");
	private final String SQL_INSERT_PRECIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaPreciosHistoricosJob_Insert_Precios.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaPreciosHistoricosJob_Select_Instrumentos.sql");
	
	
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
	}
	
	public static class InstrumentoRuebas {
		public String FECHA;
		public String NEMO;
		public BigDecimal PRECIO_CIERRE;
		public BigDecimal MONTO_TRANSADO;
		public BigDecimal VOLUMEN_TRANSADO;
		
	}
	
	public static class BolsaInstrumentoPrecio {
		public Long IdInstrumento;
		public String IdBolsa;
		public String Fecha;
		public String Nemotecnico;
		public BigDecimal PrecioMayor;
		public BigDecimal PrecioMenor;
		public BigDecimal PrecioApertura;
		public BigDecimal PrecioCierre;
		public BigDecimal PrecioMedio;
		public BigDecimal UnidadesTransadas;
		public BigDecimal MontoTransado;
		public BigDecimal Variacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection optimusConn,boolean debug) throws InterruptedException, Throwable {
		logger.info("Inicio job");
		String fechaDesde = (debug) ? "2019-10-01" : this.getParam("FechaDesde");
		String fechaHasta = (debug) ? "2019-10-31" : this.getParam("FechaHasta");
		
		Properties prop = new Properties();
        try(InputStream input = new FileInputStream("ruebas.properties");){
        	prop.load(input);	
        }
        catch(Exception e) {
        	throw e;
        }

        String DBdriver=prop.getProperty("DBdriver");
        String DBuser=prop.getProperty("DBuser");
        String DBpass=prop.getProperty("DBpass");
        String DBurl=prop.getProperty("DBurl");
        String DBprop=prop.getProperty("DBprop");
        
            
        BasicDataSource dsTarget = new BasicDataSource();
    	try {
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);           
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
            logger.debug("Conectado a RUEBAS");
            
            SQL sql = new SQL();
            HashMap<String,Object> params = new HashMap<>();
            params.put("FechaDesde",fechaDesde);
            params.put("FechaHasta",fechaHasta);
			List<InstrumentoRuebas> preciosRuebas = sql.query(dsTarget.getConnection(), SQL_SELECT_TBRVPCDI, params , InstrumentoRuebas.class);
            List<Instrumento> instrumentos = sql.query(optimusConn, SQL_SELECT_INSTRUMENTOS, new HashMap<>(),Instrumento.class);
            HashMap<String, Object> instrumentosHash = Utils.List2Hash(instrumentos, "Nemotecnico");
            
            List<BolsaInstrumentoPrecio> precios = new ArrayList<>();
            for (InstrumentoRuebas precio : preciosRuebas) {
            	Instrumento i = (Instrumento)instrumentosHash.get(precio.NEMO.trim());
            	if (i == null) {
            		logger.debug("falta nemo " + precio.NEMO.trim());
            		continue;
            	}
            	BolsaInstrumentoPrecio b = new BolsaInstrumentoPrecio();
            	b.Fecha = precio.FECHA;
            	b.IdBolsa = "BCS";
            	b.IdInstrumento = i.IdInstrumento;
            	b.MontoTransado = precio.MONTO_TRANSADO;
            	b.PrecioMayor = BigDecimal.ZERO;
            	b.PrecioMenor = BigDecimal.ZERO;
            	b.PrecioMedio = BigDecimal.ZERO;
            	b.PrecioApertura = BigDecimal.ZERO;
            	b.PrecioCierre = precio.PRECIO_CIERRE;
            	b.UnidadesTransadas = precio.VOLUMEN_TRANSADO;
            	b.Variacion = BigDecimal.ZERO;
            	
            	precios.add(b);
            }
            sql.batch(optimusConn, SQL_INSERT_PRECIOS, precios, BolsaInstrumentoPrecio.class);           
            
    	}catch(Exception e){
        	e.printStackTrace();
        	throw e;
        }finally {
			if(dsTarget!=null && !dsTarget.isClosed()) {
				dsTarget.close();
			}
		}
    	logger.info("Fin job");
        return true;
		
	}

	
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String [] args) throws InterruptedException, Throwable {
		
		Connection c = PlataformaUtils.getDESAConnection();
		ActualizaPreciosHistoricosJob a = new ActualizaPreciosHistoricosJob();
		a.logica(c, true);
		c.commit();
		
	}
	
	

}
