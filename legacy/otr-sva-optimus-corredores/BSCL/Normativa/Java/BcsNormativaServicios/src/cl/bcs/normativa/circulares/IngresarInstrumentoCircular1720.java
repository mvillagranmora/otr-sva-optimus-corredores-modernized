package cl.bcs.normativa.circulares;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.custodia.GenerarCuadraturaCustodiaCircular1518;
import cl.bcs.custodia.GenerarCuadraturaCustodiaCircular1518.DatoCuentaPropia;
import cl.bcs.normativa.circulares.ProcesarCuadraturaCustodia.Custodia;
import cl.bcs.normativa.circulares.ProcesarCuadraturaCustodia.FechaProceso;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class IngresarInstrumentoCircular1720 implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(IngresarInstrumentoCircular1720.class);
	
	private final String SQL_INSERT_INSTRUMENTO_CIR_1720 		= loadResourceToString("cl/bcs/normativa/circulares/sql/IngresarInstrumentoCircular1720_InsertDatosCIR1720.sql");
	private final String SQL_INSERT_CUSTODIA_CIRCULAR_1720 		= loadResourceToString("cl/bcs/normativa/circulares/sql/IngresarInstrumentoCircular1720_InsertCuadraturaCustodiaCircular1720.sql");
	private final String SQL_INSERT_CUADRATURA_CIRCULAR1720 	= loadResourceToString("cl/bcs/normativa/circulares/sql/IngresarInstrumentoCircular1720_InsertCuadraturaCircular1720.sql");
	private final String SQL_SELECT_NEMOTECNICO 				= loadResourceToString("cl/bcs/normativa/circulares/sql/IngresarInstrumentoCircular1720_SelectNemotecnicoCuadraturaCircular1720.sql");
	
	private final Log log = new Log("NOR.IngresarInstrumentoCircular1720");
	
	public static class EntradaIngresarInstrumentoCircular1720{
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public Long					p_IdCuadratura; 
		public java.sql.Timestamp	p_FechaCuadratura;
		public java.sql.Timestamp	p_FechaHoraSistema;
		public String				p_Nemotecnico; 
		public String  				p_NombreEmisor;
		public String     	  		p_TipoInstrumento;
		public BigDecimal   		p_CantidadUnidadesNominal;
		public String       		p_TipoUnidades; 
		public BigDecimal   		p_Valorizacion;
		public BigDecimal   		p_CarteraPropia;
		public BigDecimal   		p_CustodiaTerceros;
		public BigDecimal   		p_CustodiaPropiaDCV;
		public BigDecimal   		p_CustodiaTercerosDCV;
		public BigDecimal   		p_CustodiaSociedadEmisora;
		public BigDecimal   		p_CustodiaIntermediario;
		public BigDecimal   		p_CustodiaBolsas;
		public BigDecimal   		p_CustodiaOtros;
		public String				p_Habilitado; 
		
		public EntradaIngresarInstrumentoCircular1720() {
			this.p_Habilitado = "S";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		
	};
	
	public static class Custodia {
		public Long 					IdEmpresa;
		public java.sql.Timestamp		FechaCuadratura;
		public Long						IdCuadratura;
		public Long						IdInstrumento; 
		public String					IdTipoInstrumento; 
		public String					IdClasificacionTipoInstrumento; 
		public String					Nemotecnico; 
		public BigDecimal   			CantidadCustodia;
		public BigDecimal   			CantidadSimultaneaCompra;
		public BigDecimal   			CantidadSimultaneaVenta;
		public BigDecimal   			CantidadPactoCompra;
		public BigDecimal   			CantidadPactoVenta;
		public BigDecimal   			CantidadPrestamo;
		public BigDecimal   			CantidadEmprestito;
		public BigDecimal   			CantidadGarantia;
		public BigDecimal   			CantidadTransitoIngreso;
		public BigDecimal   			CantidadTransitoEgreso;
		public BigDecimal   			CantidadTotal;
		public BigDecimal   			MontoATasaMercadoActual;
		public BigDecimal   			Diferencia;
		public String  	 				ExplicacionDescuadratura;
		public BigDecimal   			CantidadRelacionado;
		public BigDecimal   			CantidadTerceros;
		public BigDecimal   			CantidadCuentaPropia;
		public BigDecimal   			CantidadIntermediario;
		public BigDecimal   			CantidadBCS;
		public BigDecimal   			CantidadBEC;
		public BigDecimal   			CantidadBOVALPO;
		public BigDecimal   			CantidadDCV;
		public BigDecimal   			CantidadEmisores;
		public BigDecimal   			CantidadOtros;
		public BigDecimal   			CantidadGarantiaBCS;
		public BigDecimal   			CantidadGarantiaBEC;
		public BigDecimal   			CantidadGarantiaBOVALPO;
		public BigDecimal   			CantidadGarantiaDCV;
		public BigDecimal   			CantidadGarantiaEmisores;
		public BigDecimal   			CantidadGarantiaOtros;
		public BigDecimal   			CantidadDCVRelacionado;
		public BigDecimal   			CantidadDCVTerceros;
		public BigDecimal   			CantidadDCVCuentaPropia;
		
		public Custodia() {
			this.IdInstrumento 					= 0L;
			this.IdTipoInstrumento 				= "";
			this.IdClasificacionTipoInstrumento = ""; 
			this.CantidadCustodia 				= BigDecimal.ZERO;
			this.CantidadSimultaneaCompra 		= BigDecimal.ZERO;
			this.CantidadSimultaneaVenta 		= BigDecimal.ZERO;
			this.CantidadPactoCompra 			= BigDecimal.ZERO;
			this.CantidadPactoVenta 			= BigDecimal.ZERO;
			this.CantidadPrestamo				= BigDecimal.ZERO;
			this.CantidadEmprestito				= BigDecimal.ZERO;
			this.CantidadGarantia				= BigDecimal.ZERO;
			this.CantidadTransitoIngreso		= BigDecimal.ZERO;
			this.CantidadTransitoEgreso			= BigDecimal.ZERO;
			this.CantidadTotal					= BigDecimal.ZERO;
			this.MontoATasaMercadoActual 		= BigDecimal.ZERO;
			this.Diferencia						= BigDecimal.ZERO;
			this.ExplicacionDescuadratura 		= "";
			this.CantidadRelacionado			= BigDecimal.ZERO;
			this.CantidadTerceros				= BigDecimal.ZERO;
			this.CantidadCuentaPropia			= BigDecimal.ZERO;
			this.CantidadIntermediario			= BigDecimal.ZERO;
			this.CantidadBCS					= BigDecimal.ZERO;
			this.CantidadBEC					= BigDecimal.ZERO;
			this.CantidadBOVALPO				= BigDecimal.ZERO;
			this.CantidadDCV					= BigDecimal.ZERO;
			this.CantidadEmisores				= BigDecimal.ZERO;
			this.CantidadOtros					= BigDecimal.ZERO;
			this.CantidadGarantiaBCS			= BigDecimal.ZERO;
			this.CantidadGarantiaBEC			= BigDecimal.ZERO;
			this.CantidadGarantiaBOVALPO		= BigDecimal.ZERO;
			this.CantidadGarantiaDCV			= BigDecimal.ZERO;
			this.CantidadGarantiaEmisores		= BigDecimal.ZERO;
			this.CantidadGarantiaOtros 			= BigDecimal.ZERO;
			this.CantidadDCVRelacionado			= BigDecimal.ZERO;
			this.CantidadDCVTerceros			= BigDecimal.ZERO;
			this.CantidadDCVCuentaPropia		= BigDecimal.ZERO;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	
	public static class Cuadratura extends BcsPojo {
		public Long IdCarga;
		public String FechaCuadratura;
		public String Nemotecnico;
		public BigDecimal CantidadCustodia;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadVentaPlazo;
		public BigDecimal CantidadCompraPlazo;
		public BigDecimal TotalCustodia;
		public BigDecimal CompraEnTransito;
		public BigDecimal VentaEnTransito;
		public BigDecimal TransitoIngreso;
		public BigDecimal TransitoEgreso;
		public BigDecimal Diferencia;
		public BigDecimal Precio;
		public BigDecimal Valorizacion;
		public BigDecimal DiferenciaValorizada;
		public BigDecimal TotalCustodioEmisores;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;							
		public String IdEmisor;
		public BigDecimal TotalCustodiaCuentaPropia;
		
		public Cuadratura() {
			this.CantidadCustodia = BigDecimal.ZERO;
			this.CantidadGarantia = BigDecimal.ZERO;
			this.CantidadVentaPlazo = BigDecimal.ZERO;
			this.CantidadCompraPlazo = BigDecimal.ZERO;
			this.TotalCustodia = BigDecimal.ZERO;
			this.CompraEnTransito = BigDecimal.ZERO;
			this.VentaEnTransito = BigDecimal.ZERO;
			this.TransitoIngreso = BigDecimal.ZERO;
			this.TransitoEgreso = BigDecimal.ZERO;
			this.Diferencia = BigDecimal.ZERO;
			this.Precio = BigDecimal.ZERO;
			this.Valorizacion = BigDecimal.ZERO;
			this.DiferenciaValorizada = BigDecimal.ZERO;
			this.TotalCustodioEmisores = BigDecimal.ZERO;
			this.IdClasificacionTipoInstrumento ="";
			this.IdTipoInstrumento = "";
			this.IdInstrumento = 0L;						
			this.IdEmisor = "";
			this.TotalCustodiaCuentaPropia = BigDecimal.ZERO;
		}
	}
	
	public static class SalidaIngresarInstrumentoCircular1720{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		public SalidaIngresarInstrumentoCircular1720(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		};
	};
	
	public static class ExisteNemotecnico extends BcsPojo {
		public Long IdCuadratura;
		
		public ExisteNemotecnico() {
			this.IdCuadratura = 0L; 
		}
	}
	
	private void ingresarDatosCIR1720 (Connection con,EntradaIngresarInstrumentoCircular1720 in) throws Exception {
		logger.debug("INICIO::IngresarInstrumentoCircular1720.ingresarDatosCIR1720");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<EntradaIngresarInstrumentoCircular1720> lstParametros = new ArrayList<EntradaIngresarInstrumentoCircular1720>();
		lstParametros.add(in);
		sql.batch(con, SQL_INSERT_INSTRUMENTO_CIR_1720,lstParametros, EntradaIngresarInstrumentoCircular1720.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::IngresarInstrumentoCircular1720.ingresarDatosCIR1720", fin - inicio);
	};
	
	private void ingresarCuadraturaCustodiaCircular1720(Connection con,Custodia custodia1720 ) throws Exception {
		logger.debug("INICIO::IngresarInstrumentoCircular1720.ingresarCuadraturaCustodiaCircular1720");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<Custodia> lstParametrosCustodia = new ArrayList<Custodia>();
		lstParametrosCustodia.add(custodia1720);
		sql.batch(con, SQL_INSERT_CUSTODIA_CIRCULAR_1720,lstParametrosCustodia, Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::IngresarInstrumentoCircular1720.ingresarCuadraturaCustodiaCircular1720", fin - inicio);
	};
	
	private void ingresarCuadraturaCircular1720(Connection con, Cuadratura cuadratura1720) throws Exception {
		logger.debug("INICIO::IngresarInstrumentoCircular1720.ingresarCuadraturaCircular1720");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<Cuadratura> lstParametrosCuadratura = new ArrayList<Cuadratura>();
		lstParametrosCuadratura.add(cuadratura1720);
		sql.batch(con, SQL_INSERT_CUADRATURA_CIRCULAR1720,lstParametrosCuadratura, Cuadratura.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::IngresarInstrumentoCircular1720.ingresarCuadraturaCircular1720", fin - inicio);
	};
	
	private boolean existeNemotecnico(Connection con,EntradaIngresarInstrumentoCircular1720 in) throws Exception {
		logger.debug("INICIO::IngresarInstrumentoCircular1720.existeNemotecnico");
		long inicio = System.currentTimeMillis();
		
		boolean existe= false;
		
		SQL _sql = new SQL();
		HashMap<String, Object> parametro = new HashMap<String, Object>();
		parametro.put("Nemotecnico", in.p_Nemotecnico);
		parametro.put("FechaCuadraturaCustodia", in.p_FechaCuadratura);
		List<ExisteNemotecnico> datosNemotecnico = _sql.query(con, SQL_SELECT_NEMOTECNICO, parametro, ExisteNemotecnico.class);
		if(!datosNemotecnico.isEmpty()) {
			existe = true;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::IngresarInstrumentoCircular1720.existeNemotecnico", fin - inicio);
		
		return existe;
	}
	
	@Trace
	public SalidaIngresarInstrumentoCircular1720 logica (Connection con,EntradaIngresarInstrumentoCircular1720 in) throws Exception {
		logger.debug("INICIO::IngresarInstrumentoCircular1720");
		log.write(in.toString());
		
		SalidaIngresarInstrumentoCircular1720 out  	= new SalidaIngresarInstrumentoCircular1720();
		Custodia custodia1720 						= new Custodia();
		Cuadratura cuadratura1720					= new Cuadratura();
		
		boolean existeNemo = existeNemotecnico(con,in);
		
		java.sql.Timestamp fechaSistemaLocal = Fecha.toTimeStamp("0001-01-01 00:00:00");
		
		if(!existeNemo) {
		
			try {
				fechaSistemaLocal= Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaLocal(con,in.p_IdEmpresa).toString());
				
				List<Long> idsCuadratura = Utils.getSecuencia(con, "BSCL.Normativa.BD.Secuencias::PRO.IdCuadraturaCircular1720", (Long) 1L);
				Long idCuadratura = idsCuadratura.get(0);
				
				in.p_FechaHoraSistema	= fechaSistemaLocal;
				in.p_IdCuadratura 		= idCuadratura;
				
				custodia1720.FechaCuadratura		= in.p_FechaCuadratura;
				custodia1720.Nemotecnico			= in.p_Nemotecnico;
				custodia1720.IdEmpresa 				= in.p_IdEmpresa;
				custodia1720.IdCuadratura			= idCuadratura;
				
				List<Long> idsCarga = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::CargaCuadraturaCircular1720", (Long) 1L);
				Long idCarga = idsCarga.get(0);
				
				cuadratura1720.FechaCuadratura		= in.p_FechaCuadratura.toString().split(" ")[0];
				cuadratura1720.Nemotecnico			= in.p_Nemotecnico;
				cuadratura1720.IdCarga				= idCarga;
				
				ingresarDatosCIR1720(con, in);
				ingresarCuadraturaCustodiaCircular1720(con,custodia1720);
				ingresarCuadraturaCircular1720(con, cuadratura1720);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESAR_INSTRUMENTO_CIRCULAR_1720",new HashMap<>(), con, in.p_IdEmpresa));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_AL_INGRESAR_INSTRUMENTO_CIRCULAR_1720",new HashMap<>(), con, in.p_IdEmpresa));
				log.write(e);
				e.printStackTrace();
			}
			log.write("Fin IngresarInstrumentoCircular1720");
		}else {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_YA_EXISTE_NEMOTECNICO",new HashMap<>(), con, in.p_IdEmpresa));
		}
		logger.debug("FIN::IngresarInstrumentoCircular1720");
		return out;
	};
	
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaIngresarInstrumentoCircular1720 jin=gson.fromJson(in.toString() ,EntradaIngresarInstrumentoCircular1720.class);
		SalidaIngresarInstrumentoCircular1720 out = logica(conn, jin);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

}
