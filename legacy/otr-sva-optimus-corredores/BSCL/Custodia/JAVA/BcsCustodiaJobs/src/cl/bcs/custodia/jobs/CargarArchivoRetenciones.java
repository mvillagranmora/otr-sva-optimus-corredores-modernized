package cl.bcs.custodia.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarArchivoRetenciones extends BcsJob implements JavaCall{
	
	private final Log log = new Log("CUS.CargarArchivoRetenciones");
	private final String SQL_UPDATE_ID_PROGRAMA 									= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_update.sql");
	private final String SQL_SELECT_CONSULTAR_CLIENTE								= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_SelectConsultarCliente.sql");
	private final String SQL_SELECT_CONSULTAR_EVENTO_CLIENTE						= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_SelectConsultarEventoCliente.sql");
	private final String SQL_SELECT_CONSULTAR_EVENTO_CLIENTE_PORTAFOLIO				= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_SelectConsultarEventoClientePortafolio.sql");
	private final String SQL_SELECT_CONSULTAR_EVENTO_CLIENTE_GARANTIA				= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_SelectConsultarEventoClienteGarantia.sql");
	private final String SQL_SELECT_CONSULTAR_EVENTO_CLIENTE_PORTAFOLIO_GARANTIA	= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_SelectConsultarEventoClientePortafolioGarantia.sql");	
	private final String SQL_UPDATE_EVENTO_CAPITAL									= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_ActualizaEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_GARANTIA							= loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoRetenciones_ActualizaEventoCapitalGarantia.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class ArchivoRetenciones{
		public String 			Mercado;
		public String			Nemotecnico;
		public String			ISIN;
		public String			TipoEvento;
		public String			TipoDocumentoIdentidad;
		public String			DocumentoIdentidad;
		public String			RutCavali;
		public Long 			IdCliente;
		public Long				IdSocioDeNegocio;
		public Long				Cantidad;
		public Long				CantidadGarantia;
		public BigDecimal		MontoNominal;
		public BigDecimal		MontoNominalGarantia;
		public BigDecimal		MontoRetenido;
		public BigDecimal		MontoRetenidoGarantia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientesAgrupados {
		public Long			IdSocioNegocio;
		public BigDecimal	CantidadDerecho;
		public BigDecimal	Total;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ClientesPortafolioAgrupados {
		public Long			IdSocioNegocio;
		public Long			IdPortafolio;
		public BigDecimal	Total;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ConsultaCli {
		public String		RutCavali;
		public Long			IdCliente;
		public Long 		IdSocioDeNegocio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ActualizaEventoCapitalCliente{
		public Long			IdSocioNegocio;
		public Long			IdPortafolio;
		public BigDecimal	Retencion;
		public Long			IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	@Override
	public boolean transaction(Connection con) throws Throwable  {
		return logica(con,false);
	}
	//TODO: Logica
	public boolean logica(Connection con,boolean debug) throws Throwable  {
		
		final long idEventoCapital;
		final String contenidoArchivo;
		String archivoDecoded;
		
		if (debug) {
	        //contenidoArchivo = "RV;ALICORC1;PEP214001005;DV;IUP;02150838270000;15083827;69;690;35";
			//contenidoArchivo = "RV;ALICORC1;PEP214001005;DV;IUP;02150838270000;15083827;24183;5817;3627.45;872.55;107.52;22.81";
			contenidoArchivo = "RV;ALICORC1;PEP214001005;DV;IUP;02150838270000;15083827;24183;0;3627.45;0;107.55;0\r\n" + 
					"RV;ALICORC1;PEP214001005;DV;IUP;02150838270000;15083827;0;5817;0;872.55;0;22.88\r\n" + 
					"";
	        idEventoCapital	= 153L;
			archivoDecoded = contenidoArchivo;
		}
		else {
			contenidoArchivo = this.getParam("Archivo");
	        idEventoCapital	= Long.parseLong(this.getParam("IdEventoCapital"));
	        archivoDecoded = new String(Base64.getDecoder().decode(contenidoArchivo));
		}
		
        SQL sql=new SQL();
//        String contenidoArchivo = this.getParam("Archivo");
//        Long idEventoCapital	= Long.parseLong(this.getParam("IdEventoCapital"));
        String rutaLog = "";
        int numeroRegistros = 0;
        int numeroRegistrosErroneos = 0;
        try{
        	//INICIO

			String[] registros = archivoDecoded.split(System.getProperty("line.separator"));
			List<ArchivoRetenciones> registrosRetenciones = new ArrayList<ArchivoRetenciones>();
			List<String> tipoDocumentoIdentidad = new ArrayList<String>();
			List<String> documentoIdentidad = new ArrayList<String>();
			for(String registro : registros){
				registro = registro.replace("\n", "").replace("\r", "");
				if(!registro.isEmpty()){
					numeroRegistros++;
					ArchivoRetenciones registroRetencion = new ArchivoRetenciones();
					String[] elemento = registro.split(";");
					try{
						registroRetencion.Mercado					= elemento[0];
						registroRetencion.Nemotecnico				= elemento[1];
						registroRetencion.ISIN						= elemento[2];
						registroRetencion.TipoEvento				= elemento[3];
						registroRetencion.TipoDocumentoIdentidad	= elemento[4];
						registroRetencion.DocumentoIdentidad		= elemento[5];
						registroRetencion.RutCavali					= elemento[6];
						registroRetencion.Cantidad					= elemento[7].isEmpty() ? 0L : new Long(elemento[7]);
						registroRetencion.CantidadGarantia			= elemento[8].isEmpty() ? 0L : new Long(elemento[8]);
						registroRetencion.MontoNominal				= elemento[9].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[9]);
						registroRetencion.MontoNominalGarantia		= elemento[10].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[10]);
						registroRetencion.MontoRetenido				= elemento[11].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[11]);
						registroRetencion.MontoRetenidoGarantia		= elemento[12].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[12]);
						
						tipoDocumentoIdentidad.add(registroRetencion.TipoDocumentoIdentidad);
						documentoIdentidad.add(registroRetencion.DocumentoIdentidad);
						registrosRetenciones.add(registroRetencion);
					}catch(Exception e){
						log.write("ERROR",e);
						numeroRegistrosErroneos++;
					}
				}
			}

			//AGREGO ID DE CLIENTE AL DETALLE DEL ARCHIVO DE ENTRADA
			HashMap<String,Object> paramsConsCli = new HashMap<>();
			paramsConsCli.put("TipoDocumentoIdentidad", tipoDocumentoIdentidad);
			paramsConsCli.put("DocumentoIdentidad", documentoIdentidad);
			List<ConsultaCli> infoClienteSocio = sql.query(con, SQL_SELECT_CONSULTAR_CLIENTE, paramsConsCli, ConsultaCli.class);
			
			for(ArchivoRetenciones recorreRetenciones: registrosRetenciones){
				for(ConsultaCli recorreInfCli: infoClienteSocio){
					if (recorreRetenciones.DocumentoIdentidad.equals(recorreInfCli.RutCavali)){
						recorreRetenciones.IdCliente			= recorreInfCli.IdCliente;
						recorreRetenciones.IdSocioDeNegocio		= recorreInfCli.IdSocioDeNegocio;
					}
				}
			}
			
			//TODO: OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA CARTERAS, NO PARA GARANTÍAS.
			HashMap<String,Object> paramsEvCap = new HashMap<>();
			paramsEvCap.put("IdEventoCapital", idEventoCapital);
			List<ClientesAgrupados> TablaEventoClienteAgrupado = sql.query(con, SQL_SELECT_CONSULTAR_EVENTO_CLIENTE, paramsEvCap, ClientesAgrupados.class);
		
			//OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
			HashMap<String,Object> paramsEvCapCliPor = new HashMap<>();
			paramsEvCapCliPor.put("IdEventoCapital", idEventoCapital);
			List<ClientesPortafolioAgrupados> TablaEventoCliente = sql.query(con, SQL_SELECT_CONSULTAR_EVENTO_CLIENTE_PORTAFOLIO, paramsEvCapCliPor, ClientesPortafolioAgrupados.class);
			
			List<ActualizaEventoCapitalCliente> actualizaEvCapCli = new ArrayList<ActualizaEventoCapitalCliente>(); 
			for(ClientesPortafolioAgrupados recorreTablaEventoCliente :TablaEventoCliente){
				for(ClientesAgrupados recorreTablaEventoClienteAgrupado : TablaEventoClienteAgrupado){
					for(ArchivoRetenciones recorreregistrosRetenciones: registrosRetenciones){
						ActualizaEventoCapitalCliente registroActualizar = new ActualizaEventoCapitalCliente();
						if((recorreTablaEventoCliente.IdSocioNegocio.equals(recorreTablaEventoClienteAgrupado.IdSocioNegocio)) && 
								(recorreTablaEventoClienteAgrupado.IdSocioNegocio.equals(recorreregistrosRetenciones.IdSocioDeNegocio)) &&
								(recorreregistrosRetenciones.MontoRetenido.compareTo(BigDecimal.ZERO)== 1)){
							BigDecimal prorrateo = (((((recorreTablaEventoCliente.Total.divide(recorreTablaEventoClienteAgrupado.Total,20,RoundingMode.HALF_UP))).multiply(new BigDecimal(100))).multiply(recorreregistrosRetenciones.MontoRetenido)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
							registroActualizar.IdSocioNegocio 	= recorreTablaEventoCliente.IdSocioNegocio;
							registroActualizar.IdPortafolio 	= recorreTablaEventoCliente.IdPortafolio;
							registroActualizar.Retencion 		= prorrateo;
							registroActualizar.IdEventoCapital	= idEventoCapital;
							actualizaEvCapCli.add(registroActualizar);
						}
					}
				}
			}
			
			if(actualizaEvCapCli.size() > 0){
				//ACTUALIZO LOS CLIENTES EN SU DISPONIBLE.
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, actualizaEvCapCli, ActualizaEventoCapitalCliente.class);
			}
			
			//TODO: OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA GARANTÍAS.
			HashMap<String,Object> paramsEvCapGar = new HashMap<>();
			paramsEvCapGar.put("IdEventoCapital", idEventoCapital);
			List<ClientesAgrupados> TablaEventoClienteAgrupadoGar = sql.query(con, SQL_SELECT_CONSULTAR_EVENTO_CLIENTE_GARANTIA, paramsEvCapGar, ClientesAgrupados.class);
		
			//OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
			HashMap<String,Object> paramsEvCapCliPorGar = new HashMap<>();
			paramsEvCapCliPorGar.put("IdEventoCapital", idEventoCapital);
			List<ClientesPortafolioAgrupados> TablaEventoClienteGar = sql.query(con, SQL_SELECT_CONSULTAR_EVENTO_CLIENTE_PORTAFOLIO_GARANTIA, paramsEvCapCliPorGar, ClientesPortafolioAgrupados.class);
			
			List<ActualizaEventoCapitalCliente> actualizaEvCapCliGar = new ArrayList<ActualizaEventoCapitalCliente>(); 
			for(ClientesPortafolioAgrupados recorreTablaEventoCliente :TablaEventoClienteGar){
				for(ClientesAgrupados recorreTablaEventoClienteAgrupado : TablaEventoClienteAgrupadoGar){
					for(ArchivoRetenciones recorreregistrosRetenciones: registrosRetenciones){
						ActualizaEventoCapitalCliente registroActualizar = new ActualizaEventoCapitalCliente();
						if((recorreTablaEventoCliente.IdSocioNegocio.equals(recorreTablaEventoClienteAgrupado.IdSocioNegocio)) && 
								(recorreTablaEventoClienteAgrupado.IdSocioNegocio.equals(recorreregistrosRetenciones.IdSocioDeNegocio))  &&
								(recorreregistrosRetenciones.MontoRetenidoGarantia.compareTo(BigDecimal.ZERO)== 1)){
							BigDecimal prorrateo = (((((recorreTablaEventoCliente.Total.divide(recorreTablaEventoClienteAgrupado.Total,20,RoundingMode.HALF_UP))).multiply(new BigDecimal(100))).multiply(recorreregistrosRetenciones.MontoRetenidoGarantia)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
							registroActualizar.IdSocioNegocio 	= recorreTablaEventoCliente.IdSocioNegocio;
							registroActualizar.IdPortafolio 	= recorreTablaEventoCliente.IdPortafolio;
							registroActualizar.Retencion 		= prorrateo;
							registroActualizar.IdEventoCapital	= idEventoCapital;
							actualizaEvCapCliGar.add(registroActualizar);
						}
					}
				}
			}
			
			if(actualizaEvCapCli.size() > 0){
				//ACTUALIZO LOS CLIENTES EN SU GARANTÍA
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_GARANTIA, actualizaEvCapCliGar, ActualizaEventoCapitalCliente.class);
			}
			
	        con.commit();
	        return true;
	        
	        // FIN
        }catch (Exception e) {
			log.write(e);
			con.rollback();
			throw new Throwable(e);
		}
	}

	@Override
	public void onError(Throwable t) {
		
	}

	@Override
	public void pause() {
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	//TODO: Main
	public static void main(String[] args) throws Throwable {
		Connection conn = PlataformaUtils.getMBPEConnection();
		//Connection conn = PlataformaUtils.getMBPEConnection();
		CargarArchivoRetenciones cb = new CargarArchivoRetenciones();
		cb.logica(conn, true);
	}
	
}
