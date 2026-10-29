package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;


public class CalcularFechaSolicitudAporteRescateJava implements JavaCall{
	
	private final Log log = new Log("FM.CalcularFechaSolicitudAporteRescateJava");
	private final String SQL_SELECT_CONFIGURACION_FECHAS_FFMM = loadResourceToString("cl/bcs/fondomutuo/sql/CalcularFechaSolicitudAporteRescate_SelectConfiguracionFechasFFMM.sql");
	
	public static class EntradaCalcularFechaSolicitudAporteRescate{
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public List<InFechas> p_SolicitudAporteRescate;
		
		public EntradaCalcularFechaSolicitudAporteRescate() {
			this.p_SolicitudAporteRescate = new ArrayList<InFechas>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InFechas{
		public Long IdInterno;
		public String IdSerieFondo;
		public String FechaEmision;
		public String EstadoFondo;
		public String EstadoLiquidacion;
		public Long NumeroDiasRetencion;
		public Long NumeroValutaValorCuota;
		public Long NumeroValutaCertificacion;
		public Long NumeroValutaCurse;
		public String SolicitudBatch;
		public String TipoSolicitud;
		public String FondoExtranjero;
		public Long IdPaisFondo;
		public Long IdPaisEmisor;
		public Long IdPaisMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ProcesamientoFechas{
		public Long IdInterno;
		public String KeyConfig;
		public String IdSerieFondo;
		public String FechaInicial;
		public String FechaEmision;
		public String FechaDigitacion;
		public String FechaCurse;
		public String FechaValorCuota;
		public String FechaCertificacion;
		public String FechaLiquidacion;
		public String EstadoFondo;
		public String EstadoLiquidacion;
		public Long NumeroDiasRetencion;
		public Long NumeroValutaValorCuota;
		public Long NumeroValutaCertificacion;
		public Long NumeroValutaCurse;
		public String SolicitudBatch;
		public String TipoSolicitud;
		public String FondoExtranjero;
		public Long IdPaisFondo;
		public Long IdPaisEmisor;
		public Long IdPaisMoneda;
		public Long IdPaisLocal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConfiguracionFechaFFMM {
		public String IdModalidad;
		public String IdTipoFecha;
		public String TipoSolicitud;
		public String EstadoFondo;
		public String FondoExtranjero;
		public String Descripcion;
		public Long CantidadDias;
		public String EsHabil;
		public String IdTipoFechaOrigen;
		public String DebeSerHabilFechaOrigen;
		public String ValutaCurse;
		public String ValutaValorCuota;
		public String ValutaCertificacion;
		public String ValutaRetencion;
		public String CalendarioLocal;
		public String CalendarioEmisor;
		public String CalendarioFondo;
		public String CalendarioMoneda;
		public String IdEstado;
	}
	
	public static class PECO {

		public long CantidadDiasValuta;
		public String KeyConfig;
		public String EsHabil;
		public String IdTipoFechaOrigen;
		public String DebeSerHabilFechaOrigen;
		public long IdPaisFondo;
		public long IdPaisEmisor;
		public long IdPaisMoneda;
		public long IdPaisLocal;
		public long paisCalculo;
		
	}
	
	public static class FechasPivote{
		public String KeyConfig;
		public long IdInternoProceso;
		public long IdInterno;
		public String IdSerieFondo;
		public Date FechaEmision;
		public Date FechaInicial;
		
		public String FechaInicialEsHabil;
		public Date FechaFinal;
		public String FechaFinalEsHabil;
		
		public long CantidadDiasValuta;
		public long IdPaisLocal;
		public long IdPaisEmisor;
		public long IdPaisFondo;
		public long IdPaisMoneda;
		public long IdConfiguracionFechasFondoMutuo;
		public String IdTipoFecha;
		public String EsHabil;
		public String IdTipoFechaOrigen;
		public String DebeSerHabilFechaOrigen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularFechaSolicitudAporteRescate{
		public List <ResultadoFecha> P_FECHASAPORTERESCATE;
		
		public SalidaCalcularFechaSolicitudAporteRescate(){
			this.P_FECHASAPORTERESCATE = new ArrayList<ResultadoFecha>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoFecha{
		public Long IdInterno;
		public String IdSerieFondo;
		public String FechaEmision;
		public Date FechaDigitacion;        
		public Date FechaAceptacion;
		public Date FechaValorCuota;
		public Date FechaCertificacion;
		public Date FechaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCalcularFechaSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaCalcularFechaSolicitudAporteRescate.class);
		SalidaCalcularFechaSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SalidaCalcularFechaSolicitudAporteRescate logica(Connection con, EntradaCalcularFechaSolicitudAporteRescate entrada) throws Exception{
		log.write("INICIO CalcularFechaSolicitudAporteRescate");
		SalidaCalcularFechaSolicitudAporteRescate out = new SalidaCalcularFechaSolicitudAporteRescate();
		SQL sql=new SQL();
		try{
			
			String modalidadSolicitud = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE");
			HashMap<String, Object>  paramsIn = new HashMap<String, Object>();
			HashMap<String, ConfiguracionFechaFFMM>  pFechas = new HashMap<String, ConfiguracionFechaFFMM>();
			HashMap<String, PECO>  hashPECO = new HashMap<String, PECO>();
			
			List<ProcesamientoFechas> listProcFechas = new ArrayList<ProcesamientoFechas>();
			paramsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
			List<ConfiguracionFechaFFMM> configuracionFechaFFMM = sql.query(con, SQL_SELECT_CONFIGURACION_FECHAS_FFMM, paramsIn, ConfiguracionFechaFFMM.class);
			
			for (InFechas sol : entrada.p_SolicitudAporteRescate) {
				ProcesamientoFechas pf = new ProcesamientoFechas();
				pf.IdInterno = sol.IdInterno;
				pf.IdSerieFondo = sol.IdSerieFondo;
				pf.FechaEmision = sol.FechaEmision;
				pf.EstadoFondo = sol.EstadoFondo;
				pf.EstadoLiquidacion = sol.EstadoLiquidacion;
				pf.NumeroDiasRetencion = sol.NumeroDiasRetencion;
				pf.NumeroValutaValorCuota = sol.NumeroValutaValorCuota;
				pf.NumeroValutaCertificacion = sol.NumeroValutaCertificacion;
				pf.NumeroValutaCurse = sol.NumeroValutaCurse;
				pf.SolicitudBatch = sol.SolicitudBatch;
				pf.TipoSolicitud = sol.TipoSolicitud;
				pf.FondoExtranjero = sol.FondoExtranjero;
				listProcFechas.add(pf);
			}
			
			for (ProcesamientoFechas sol : listProcFechas) {
				for (ConfiguracionFechaFFMM confFechaFFMM : configuracionFechaFFMM){
					if (((confFechaFFMM.TipoSolicitud == null || confFechaFFMM.TipoSolicitud.equalsIgnoreCase(sol.TipoSolicitud)) &&
						(confFechaFFMM.EstadoFondo == null || confFechaFFMM.EstadoFondo.equalsIgnoreCase(sol.EstadoFondo)) &&
						(confFechaFFMM.FondoExtranjero == null || confFechaFFMM.FondoExtranjero.equalsIgnoreCase(sol.FondoExtranjero)))&&
						((modalidadSolicitud == null && ((confFechaFFMM.IdModalidad.equalsIgnoreCase("UNITARIO") && sol.SolicitudBatch.equalsIgnoreCase("N")) || (confFechaFFMM.IdModalidad.equalsIgnoreCase("BATCH") && sol.SolicitudBatch.equalsIgnoreCase("S")))) 
						|| confFechaFFMM.IdModalidad.equalsIgnoreCase(modalidadSolicitud))){
						sol.KeyConfig = sol.IdInterno + confFechaFFMM.IdTipoFecha;
						pFechas.put(sol.KeyConfig, confFechaFFMM);
						PECO p = new PECO();
						p.CantidadDiasValuta = (confFechaFFMM.ValutaCurse.equalsIgnoreCase("S") ? sol.NumeroValutaCurse : 0) + 
								               (confFechaFFMM.ValutaCertificacion.equalsIgnoreCase("S") ? sol.NumeroValutaCertificacion : 0) + 
								               (confFechaFFMM.ValutaRetencion.equalsIgnoreCase("S") ? sol.NumeroDiasRetencion : 0) + 
								               (confFechaFFMM.ValutaValorCuota.equalsIgnoreCase("S") ? sol.NumeroValutaValorCuota : 0) +
								                confFechaFFMM.CantidadDias;
						p.KeyConfig = sol.IdInterno + confFechaFFMM.IdTipoFecha;
						p.EsHabil = confFechaFFMM.EsHabil;
						p.IdTipoFechaOrigen = confFechaFFMM.IdTipoFechaOrigen;
						p.DebeSerHabilFechaOrigen = confFechaFFMM.DebeSerHabilFechaOrigen;
						p.IdPaisFondo = (confFechaFFMM.CalendarioFondo.equalsIgnoreCase("S") ? sol.IdPaisFondo : -1);
						p.IdPaisEmisor = (confFechaFFMM.CalendarioEmisor.equalsIgnoreCase("S") ? sol.IdPaisEmisor : -1);
						p.IdPaisMoneda = (confFechaFFMM.CalendarioMoneda.equalsIgnoreCase("S") ? sol.IdPaisMoneda : -1);
						p.IdPaisLocal = (confFechaFFMM.CalendarioLocal.equalsIgnoreCase("S") ? entrada.p_IdPais : -1);
						hashPECO.put(sol.KeyConfig, p);
					}
				}
			}
			
			ObtenerDiaSiguiente odb = new ObtenerDiaSiguiente();
			ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
			ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
			inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			//Calculo fecha digitacion
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "DIGITA");
				peco.paisCalculo = (peco.IdPaisEmisor > 0 ? peco.IdPaisEmisor : (peco.IdPaisFondo > 0 ? peco.IdPaisFondo : (peco.IdPaisLocal > 0 ? peco.IdPaisLocal : (peco.IdPaisMoneda > 0 ? peco.IdPaisFondo : -1)))); 
				fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
				fecha.fechaInicial = sol.FechaEmision;
				fecha.numeroDias = peco.CantidadDiasValuta;
				fecha.pais = peco.paisCalculo;
				inObtenerDiaSiguiente.p_fechas.add(fecha);
			}
			fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
			ObtenerDiaSiguiente.OutObtenerDiaSiguiente outDiaSiguiente = odb.logica(con, inObtenerDiaSiguiente);
			inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> hashFechasSalida = new HashMap<>();
			for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
				hashFechasSalida.put(fechaSalida.fechaInicial + fechaSalida.numeroDias + fechaSalida.idPais, fechaSalida);
			}
			
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.KeyConfig);
				FechasOut fechaDigitacionSalida = hashFechasSalida.get(sol.FechaEmision + peco.CantidadDiasValuta + peco.paisCalculo);
				sol.FechaDigitacion = fechaDigitacionSalida.fechaCalculada;
				sol.FechaInicial = fechaDigitacionSalida.fechaCalculada;
			}
			
			//Calculo fecha curse
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "CURSE");
				peco.paisCalculo = (peco.IdPaisEmisor > 0 ? peco.IdPaisEmisor : (peco.IdPaisFondo > 0 ? peco.IdPaisFondo : (peco.IdPaisLocal > 0 ? peco.IdPaisLocal : (peco.IdPaisMoneda > 0 ? peco.IdPaisFondo : -1)))); 
				fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
				fecha.fechaInicial = sol.FechaInicial;
				fecha.numeroDias = peco.CantidadDiasValuta;
				fecha.pais = peco.paisCalculo;
				inObtenerDiaSiguiente.p_fechas.add(fecha);
			}
			fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
		    outDiaSiguiente = odb.logica(con, inObtenerDiaSiguiente);
		    inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			hashFechasSalida = new HashMap<>();
			for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
				hashFechasSalida.put(fechaSalida.fechaInicial + fechaSalida.numeroDias + fechaSalida.idPais, fechaSalida);
			}
			
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "CURSE");
				FechasOut fechaDigitacionSalida = hashFechasSalida.get(sol.FechaInicial + peco.CantidadDiasValuta + peco.paisCalculo);
				sol.FechaCurse = fechaDigitacionSalida.fechaCalculada;
				sol.FechaInicial = fechaDigitacionSalida.fechaCalculada;
			}
			
			//Calculo fecha valor cuota
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "VCUOTA");
				peco.paisCalculo = (peco.IdPaisEmisor > 0 ? peco.IdPaisEmisor : (peco.IdPaisFondo > 0 ? peco.IdPaisFondo : (peco.IdPaisLocal > 0 ? peco.IdPaisLocal : (peco.IdPaisMoneda > 0 ? peco.IdPaisFondo : -1)))); 
				fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
				fecha.fechaInicial = sol.FechaInicial;
				fecha.numeroDias = peco.CantidadDiasValuta;
				fecha.pais = peco.paisCalculo;
				inObtenerDiaSiguiente.p_fechas.add(fecha);
			}
			fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
		    outDiaSiguiente = odb.logica(con, inObtenerDiaSiguiente);
		    inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			hashFechasSalida = new HashMap<>();
			for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
				hashFechasSalida.put(fechaSalida.fechaInicial + fechaSalida.numeroDias + fechaSalida.idPais, fechaSalida);
			}
			
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "VCUOTA");
				FechasOut fechaDigitacionSalida = hashFechasSalida.get(sol.FechaInicial + peco.CantidadDiasValuta + peco.paisCalculo);
				sol.FechaValorCuota = fechaDigitacionSalida.fechaCalculada;
				sol.FechaInicial = fechaDigitacionSalida.fechaCalculada;
			}
			
			//Calculo fecha certificacion
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "CERTIF");
				peco.paisCalculo = (peco.IdPaisEmisor > 0 ? peco.IdPaisEmisor : (peco.IdPaisFondo > 0 ? peco.IdPaisFondo : (peco.IdPaisLocal > 0 ? peco.IdPaisLocal : (peco.IdPaisMoneda > 0 ? peco.IdPaisFondo : -1)))); 
				fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
				fecha.fechaInicial = sol.FechaInicial;
				fecha.numeroDias = peco.CantidadDiasValuta;
				fecha.pais = peco.paisCalculo;
				inObtenerDiaSiguiente.p_fechas.add(fecha);
			}
			fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
		    outDiaSiguiente = odb.logica(con, inObtenerDiaSiguiente);
		    inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			hashFechasSalida = new HashMap<>();
			for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
				hashFechasSalida.put(fechaSalida.fechaInicial + fechaSalida.numeroDias + fechaSalida.idPais, fechaSalida);
			}
			
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "CERTIF");
				FechasOut fechaDigitacionSalida = hashFechasSalida.get(sol.FechaInicial + peco.CantidadDiasValuta + peco.paisCalculo);
				sol.FechaCertificacion = fechaDigitacionSalida.fechaCalculada;
				sol.FechaInicial = fechaDigitacionSalida.fechaCalculada;
			}
			
			//Calculo fecha liquidacion
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "LIQUID");
				peco.paisCalculo = (peco.IdPaisEmisor > 0 ? peco.IdPaisEmisor : (peco.IdPaisFondo > 0 ? peco.IdPaisFondo : (peco.IdPaisLocal > 0 ? peco.IdPaisLocal : (peco.IdPaisMoneda > 0 ? peco.IdPaisFondo : -1)))); 
				fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
				fecha.fechaInicial = sol.FechaInicial;
				fecha.numeroDias = peco.CantidadDiasValuta;
				fecha.pais = peco.paisCalculo;
				inObtenerDiaSiguiente.p_fechas.add(fecha);
			}
			fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
		    outDiaSiguiente = odb.logica(con, inObtenerDiaSiguiente);
		    inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			hashFechasSalida = new HashMap<>();
			for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
				hashFechasSalida.put(fechaSalida.fechaInicial + fechaSalida.numeroDias + fechaSalida.idPais, fechaSalida);
			}
			
			for (ProcesamientoFechas sol : listProcFechas) {
				PECO peco = hashPECO.get(sol.IdInterno + "LIQUID");
				FechasOut fechaDigitacionSalida = hashFechasSalida.get(sol.FechaInicial + peco.CantidadDiasValuta + peco.paisCalculo);
				sol.FechaLiquidacion = fechaDigitacionSalida.fechaCalculada;
				sol.FechaInicial = fechaDigitacionSalida.fechaCalculada;
			}
			
			for (ProcesamientoFechas sol : listProcFechas){
				ResultadoFecha rf = new ResultadoFecha();
				rf.IdInterno = sol.IdInterno;
				rf.IdSerieFondo = sol.IdSerieFondo;
				rf.FechaEmision = sol.FechaEmision;
				rf.FechaAceptacion = Fecha.toDate(sol.FechaCurse);
				rf.FechaDigitacion = Fecha.toDate(sol.FechaDigitacion);
				rf.FechaValorCuota = Fecha.toDate(sol.FechaValorCuota);
				rf.FechaCertificacion = Fecha.toDate(sol.FechaCertificacion);
				rf.FechaLiquidacion = Fecha.toDate(sol.FechaLiquidacion);
				out.P_FECHASAPORTERESCATE.add(rf);
			}
			
		}catch(Exception e){
			log.write("Error::CalcularFechaSolicitudAporteRescate",e);
			throw e;
		}
		log.write("FIN CalcularFechaSolicitudAporteRescate");
		
		return out;
	}

	public static void main(String[] args){
		
        //prueba de la clase
		try {
			Connection con = PlataformaUtils.getUATConnection();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_SolicitudAporteRescate\":[{\"IdInterno\":1,\"IdSerieFondo\":\"DEUDA360_A\",\"FechaEmision\":\"2019-02-04\",\"EstadoFondo\":\"C\",\"EstadoLiquidacion\":\"C\",\"NumeroDiasRetencion\":0,\"NumeroValutaValorCuota\":0,\"NumeroValutaCertificacion\":0,\"NumeroValutaCurse\":0,\"SolicitudBatch\":\"N\",\"TipoSolicitud\":\"APO\",\"FondoExtranjero\":\"N\",\"IdPaisFondo\":44,\"IdPaisEmisor\":44,\"IdPaisMoneda\":1},{\"IdInterno\":2,\"IdSerieFondo\":\"DEUDA360_B\",\"FechaEmision\":\"2019-02-05\",\"EstadoFondo\":\"C\",\"EstadoLiquidacion\":\"C\",\"NumeroDiasRetencion\":0,\"NumeroValutaValorCuota\":0,\"NumeroValutaCertificacion\":0,\"NumeroValutaCurse\":0,\"SolicitudBatch\":\"N\",\"TipoSolicitud\":\"RES\",\"FondoExtranjero\":\"N\",\"IdPaisFondo\":44,\"IdPaisEmisor\":44,\"IdPaisMoneda\":1}]}";
			Gson gson = Utils.getGsonBuilder();
			EntradaCalcularFechaSolicitudAporteRescate entrada = gson.fromJson(json, EntradaCalcularFechaSolicitudAporteRescate.class);
			CalcularFechaSolicitudAporteRescateJava calcularFechaSolicitudAporteRescateJava = new CalcularFechaSolicitudAporteRescateJava();
			SalidaCalcularFechaSolicitudAporteRescate objOut = calcularFechaSolicitudAporteRescateJava.logica(con, entrada);
			System.out.println(objOut.toString());
			con.rollback();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}

}
