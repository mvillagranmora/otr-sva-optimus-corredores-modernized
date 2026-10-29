package cl.bcs.plataforma.jobs;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/***************************************
 * @version 	1.0
 * @author 		CSI
 *
**************************************/

public class ExtraerPreciosRuebasDB extends BcsJob implements JavaCall {
	private final Log log = new Log("PLA.ExtraerPreciosRuebasDB");
	
	private final String SQL_PRECIOS_EXTRANJEROS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ExtraerPreciosRuebasDB_PreciosExtranjeros.sql");;

	private final String SQL_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ExtraerPreciosRuebasDB_Instrumentos.sql");
	
	private final String SQL_PRECIO_UPDATE = loadResourceToString("cl/bcs/plataforma/jobs/sql/ExtraerPreciosRuebasDB_PrecioUpdate.sql");
	
	public static class InstrumentosExtranjero {
		public String		TS;
		public String		BOLSA_EX;
		public String		MERCADO;
		public String		NEMO;
		public BigDecimal	PRE_APE_DI_AC;
		public BigDecimal	PRE_ULT_DI_AC;
		public BigDecimal	PRE_CIE_DI_AC;
		public BigDecimal	PRE_CIE_OFI;
		public BigDecimal	VAR_PRE;
		public BigDecimal	PRE_MEN_DI_AC;
		public BigDecimal	PRE_MAY_DI_AC;
		public BigDecimal	PRE_MED_DI_AC;
		public BigDecimal	MONTO_DI_AC;
		public BigDecimal	UNI_TRA_DI_AC;
		public BigDecimal	NUM_NEG_DI_AC;
		public String		TENDEN_DI_AC;
		public BigDecimal	LIM_MIN;
		public BigDecimal	LIM_MAX;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}		
	}
	
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}		
	
	public static class BolsaInstrumentoPrecio {
		public Long			IdInstrumento;
		public String		IdBolsa;
		public String		Nemotecnico;
		public Date			Fecha;
		public Timestamp	FechaActualizacionArchivo;
		public Timestamp	FechaActualizacionMD;
		public BigDecimal	PrecioMayor;
		public BigDecimal	PrecioMenor;
		public BigDecimal	PrecioApertura;
		public BigDecimal	PrecioCierre;
		public BigDecimal	PrecioMedio;
		public BigDecimal	UnidadesTransadas;
		public BigDecimal	MontoTransado;
		public BigDecimal	Variacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}	
	
    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");
        String DBdriver,DBuser,DBpass,DBurl,DBprop;
        SQL _sql=new SQL();
        List<BolsaInstrumentoPrecio> ListaNuevosPrecios=new ArrayList<>();
        
        //Demonio se duerme 
        try {
        	Integer sleep=this.getParamInteger("CicloDemonioSegs");
        	if (sleep==null) sleep=900;
        	Thread.sleep(sleep*1000);
        }catch(Exception er) { }

        //Obtiene propiedades a la BD RUEBAS
        try{
        	Properties prop = new Properties();
            InputStream input = new FileInputStream("ruebas.properties");
            prop.load(input);
            DBdriver=prop.getProperty("DBdriver");
            DBuser=prop.getProperty("DBuser");
            DBpass=prop.getProperty("DBpass");
            DBurl=prop.getProperty("DBurl");
            DBprop=prop.getProperty("DBprop");
            input.close();
            log.write("Configuracion cargada");
        }catch(Exception e){
        	log.write(e);
        	throw new InterruptedException("ERROR NO SE PUEDE LEER ruebas.properties");
        }  	
    	
        //Obtiene DS conexion a la BD RUEBAS
    	BasicDataSource dsTarget = new BasicDataSource();    	 
    	try {
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);           
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
            log.write("Conectado a RUEBAS");
    	}catch(Exception e){
        	log.write(e);
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        }  	

        //Obtiene los Instrumentos de Optimus
    	log.write("Obteniendo Instrumentos Optimus");
    	List<Instrumento> instrumentos = null;
    	 HashMap<String, Instrumento> hashinstrumentos = null;
        try {
        	instrumentos=_sql.query(con, SQL_INSTRUMENTOS,new HashMap<String,Object>(), Instrumento.class);
            hashinstrumentos = (HashMap) Utils.List2Hash(instrumentos, "Nemotecnico");
            log.write("Instrumentos leidos:" + hashinstrumentos.size());
        }
        catch(Exception e) {
        	log.write("Error al leer instrumentos optimus");
        	log.write(e);
        }
        
        
    	//Obtiene los Precios Extrajeros de Ruebas
    	log.write("Obteniendo Precios Extranjeros Ruebas");
    	List<InstrumentosExtranjero> preciosExtranjeroRuebas;
        try (Connection conRU = dsTarget.getConnection()) {        	
        	preciosExtranjeroRuebas=_sql.query(conRU, SQL_PRECIOS_EXTRANJEROS,new HashMap<String,Object>(), InstrumentosExtranjero.class);
        }catch(Exception er) {
        	log.write(er);
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER PRECIOS EXTRANJEROS");        	
        }
        log.write(preciosExtranjeroRuebas.size() + " leidos.");
        
        //Procesamiento
        log.write("Procesando Instrumentos");
        for (InstrumentosExtranjero precioExt:preciosExtranjeroRuebas) {
        	BolsaInstrumentoPrecio nPrecio=new BolsaInstrumentoPrecio(); 
        	String nemo=precioExt.NEMO.trim().toUpperCase();
        	String bolsa=precioExt.BOLSA_EX.trim().toUpperCase();
        	if (!hashinstrumentos.containsKey(nemo)) {
        		log.write("Warning: Instrumento no cargado en optimus :"+bolsa+":"+nemo);
        		continue;
        	}
        	if (!(bolsa.equals("XLIM") || bolsa.equals("XBOG"))) {
        		log.write("Warning: Bolsa no reconocida :"+bolsa+":"+nemo);
        		continue;
        	}
        	nPrecio.IdInstrumento=hashinstrumentos.get(nemo).IdInstrumento;        	
        	if (bolsa.equals("XLIM")) nPrecio.IdBolsa="BVL";
        	if (bolsa.equals("XBOG")) nPrecio.IdBolsa="BVC";        	
        	nPrecio.Nemotecnico=nemo;
        	nPrecio.Fecha=Fecha.toDate(precioExt.TS.substring(0,10));
        	nPrecio.PrecioMayor=precioExt.PRE_MAY_DI_AC;
        	nPrecio.PrecioMenor=precioExt.PRE_MEN_DI_AC;
        	nPrecio.PrecioApertura=precioExt.PRE_APE_DI_AC;
        	nPrecio.PrecioCierre=precioExt.PRE_CIE_DI_AC;
        	nPrecio.PrecioMedio=precioExt.PRE_MED_DI_AC;
        	nPrecio.UnidadesTransadas=precioExt.NUM_NEG_DI_AC;
        	nPrecio.MontoTransado=precioExt.MONTO_DI_AC;
        	if (precioExt.VAR_PRE == null) nPrecio.Variacion=BigDecimal.ZERO;
        	else nPrecio.Variacion=precioExt.VAR_PRE;
        	ListaNuevosPrecios.add(nPrecio);
        }
        
        ///////////////////////////////////////////
        //Envia Precios a BD
        ///////////////////////////////////////////
        if (ListaNuevosPrecios.size()>0) {
        	log.write("Precios extranjeros encontrados n="+ListaNuevosPrecios.size());
        	_sql.batch(con, SQL_PRECIO_UPDATE, ListaNuevosPrecios, BolsaInstrumentoPrecio.class);        	
        }else {
        	log.write("No hay precios extranjeros que ingresar");
        }
        //Proceso Terminado correctamente
        return true;    	    	
    }

    @Override
    public void onError(Throwable t) {
    	log.write(t.getMessage());
        log.write("****** ERROR TAREA  *******");
        t.printStackTrace();
    }

	@Override
	public void pause() {
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}	
	
    public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
            // ---------------------------------
            try {
            	ExtraerPreciosRuebasDB demo=new ExtraerPreciosRuebasDB();
            	demo.transaction(con);
                con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
        

    	
	
}