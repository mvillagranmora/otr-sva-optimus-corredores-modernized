package cl.bcs.plataforma.ins;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarDatosInstrumentos implements JavaCall {
	private final static Logger logger = LogManager.getLogger(ConsultarDatosInstrumentos.class);
	private final String SELECT_CONSULTAR_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarDatosInstrumentos_SelectInstrumento.sql");
	private final String SELECT_BOLSA = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarDatosInstrumentos_SelectBolsa.sql");
	private final String INSERT_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarDatosInstrumentos_InsertInstrumento.sql");
	private final String INSERT_BOLSA_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarDatosInstrumentos_InsertBolsaInstrumento.sql");
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarDatosInstrumento_Entrada jin = gson.fromJson(in.toString(), ConsultarDatosInstrumento_Entrada.class);
		ConsultarDatosInstrumento_Salida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class ConsultaInstrumento {
		public Long Correlativo;
		public Long IdInstrumento;	//busca por idinstrumento y luego por nemo
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FiltroTipoInstrumento {
		public Long Correlativo;
		public String IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class FiltroClasificacionTipoInstrumento {
		public Long Correlativo;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ConsultarDatosInstrumento_Entrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		
		public String						p_CreaOpcionAutomatica = "N";
		public List<FiltroTipoInstrumento>	p_IdTipoInstrumentos;
		public List<FiltroClasificacionTipoInstrumento>	p_IdClasificacionTipoInstrumentos;
		public List<ConsultaInstrumento>	p_Instrumentos;
		public List<String> p_filtroEstado;
		public int 							p_filtroInstrumentoNegociacion;
		
		public ConsultarDatosInstrumento_Entrada() {
			p_Instrumentos = new ArrayList<>();
			p_IdTipoInstrumentos = new ArrayList<>();
			p_IdClasificacionTipoInstrumentos = new ArrayList<>();
			p_filtroEstado = new ArrayList<>();
			p_filtroInstrumentoNegociacion = 0; // por defecto, no se filtra por instrumento raiz
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarDatosInstrumento_Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<DatosInstrumento>	P_OUT_INSTRUMENTOS;
		
		public ConsultarDatosInstrumento_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_INSTRUMENTOS = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public ConsultarDatosInstrumento_Salida logica(Connection conn, ConsultarDatosInstrumento_Entrada entrada) throws Exception {
		ConsultarDatosInstrumento_Salida salida = new ConsultarDatosInstrumento_Salida();
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		{
			List<Long> IdInstrumento = new ArrayList<>();
			List<String> Nemotecnico = new ArrayList<>();
			List<String> idsTipoInstrumento = new ArrayList<>();
			List<String> idsClasificacionTipoInstrumento = new ArrayList<>();
			List<String> estadoInstrumento = new ArrayList<>();
			
			params.put("IdInstrumento", IdInstrumento);
			params.put("Nemotecnico", Nemotecnico);
			params.put("idsTipoInstrumento", idsTipoInstrumento);
			params.put("idsClasificacionTipoInstrumento", idsClasificacionTipoInstrumento);
			params.put("filtroPorTipo", 1);
			//params.put("filtroPorSubyacente", 1);
			params.put("filtroPorClasificacion", 1);
			params.put("estadoInstrumento", estadoInstrumento);
			
			// Si el filtro esta encendido (valor 1), entonces el valor del instrumento raiz es 0
			// De lo contrario, si el filtro esta apagado (valor 0) el valor del instrumento raiz es 1
			if(entrada.p_filtroInstrumentoNegociacion == 0) {
				params.put("filtroInstrumentoNegociacion", 0L);
			} else if (entrada.p_filtroInstrumentoNegociacion != 0) {
				params.put("filtroInstrumentoNegociacion", 1L);
			}

			if (entrada.p_CreaOpcionAutomatica.equalsIgnoreCase("S")) {
				idsTipoInstrumento.add("OPC");
				params.put("filtroPorTipo", 0);
			}
			for (ConsultaInstrumento asdf : entrada.p_Instrumentos) {
				if (asdf.IdInstrumento != null) {
					IdInstrumento.add(asdf.IdInstrumento);
				}
				else if (asdf.Nemotecnico != null && asdf.Nemotecnico.length() > 0) {
					Nemotecnico.add(asdf.Nemotecnico);
				}
			}
			
			for (FiltroTipoInstrumento ff: entrada.p_IdTipoInstrumentos) {
				if (ff.IdTipoInstrumento != null) {
					
					idsTipoInstrumento.add(ff.IdTipoInstrumento);
					params.put("filtroPorTipo", 0);
					
				}
			}
			
			for (FiltroClasificacionTipoInstrumento ff: entrada.p_IdClasificacionTipoInstrumentos) {
				if (ff.IdClasificacionTipoInstrumento != null) {
					idsClasificacionTipoInstrumento.add(ff.IdClasificacionTipoInstrumento);
					params.put("filtroPorClasificacion", 0);
				}
			}
			
			if(entrada.p_filtroEstado != null) {
				if(entrada.p_filtroEstado.size() == 0) {
					estadoInstrumento.add("ACT");
				}else {
					for (String fe : entrada.p_filtroEstado) {
						estadoInstrumento.add(fe);
				}
			}
		}else {
			estadoInstrumento.add("INA");
		}
			if (IdInstrumento.size() == 0) {
				IdInstrumento.add(-1L);
			}
			
			if (Nemotecnico.size() == 0) {
				Nemotecnico.add("");
			}
		}
		logger.debug("INICIO::SELECT_CONSULTAR_INSTRUMENTO");
		List<DatosInstrumento> instrumentos = _sql.query(conn, SELECT_CONSULTAR_INSTRUMENTO, params, DatosInstrumento.class);
		logger.debug("FIN::SELECT_CONSULTAR_INSTRUMENTO");
		for (ConsultaInstrumento asdf : entrada.p_Instrumentos) {
			boolean found = false;
			for (DatosInstrumento instrumento : instrumentos) {
				if(entrada.p_filtroInstrumentoNegociacion == 0){
					if (((asdf.IdInstrumento != null && instrumento.IdInstrumento.equals(asdf.IdInstrumento)) || (asdf.Nemotecnico != null && instrumento.Nemotecnico.equals(asdf.Nemotecnico))) && instrumento.IdInstrumentoRaiz.equals(0L)) {
						instrumento.Correlativo = asdf.Correlativo;
						salida.P_OUT_INSTRUMENTOS.add(instrumento);
						found = true;
					}
				}
				else if(entrada.p_filtroInstrumentoNegociacion != 0){
					if ((asdf.IdInstrumento != null && instrumento.IdInstrumento.equals(asdf.IdInstrumento)) || (asdf.Nemotecnico != null && instrumento.Nemotecnico.equals(asdf.Nemotecnico))) {
						instrumento.Correlativo = asdf.Correlativo;
						salida.P_OUT_INSTRUMENTOS.add(instrumento);
						found = true;
					}
				}
			}
			
			//CREA NEMO OPCION DE FORMA AUTOMATICA
			if (!found && entrada.p_CreaOpcionAutomatica.equalsIgnoreCase("S")) {
				
				String nemoSubyacenteOpcion;
				Date fechaVencimientoString;
				BigDecimal precioStrike;
				String callPut;
				try {
					
					int i = 0;
					while (i < asdf.Nemotecnico.length() && !Character.isDigit(asdf.Nemotecnico.charAt(i))) {
						i++;
					}
					
					nemoSubyacenteOpcion = asdf.Nemotecnico.substring(0, i);
					
					String agno = asdf.Nemotecnico.substring(i, i+2);
					String mes = asdf.Nemotecnico.substring(i+2, i+4);
					String dia = asdf.Nemotecnico.substring(i+4, i+6);
					callPut = asdf.Nemotecnico.substring(i+6, i+7);
					String precioEntero = asdf.Nemotecnico.substring(i+7, i+12);
					String precioDecimal = asdf.Nemotecnico.substring(i+13, asdf.Nemotecnico.length()-1);

					
					precioStrike = new BigDecimal(precioEntero + "." + precioDecimal);
					fechaVencimientoString = Fecha.toDate("20" + agno + "-" + mes + "-" + dia);
					
				}
				catch (Exception e) {
					//error de parseo
					System.out.println(e.toString());
					continue;
				}
				
				if (!callPut.equalsIgnoreCase("C") && !callPut.equalsIgnoreCase("P") ) {
					//ERROR, CALL PUT DEBE SER C o P
					continue;
				}
				
				HashMap<String,Object> params1 = new HashMap<>();
				{
					List<Long> IdInstrumento = new ArrayList<>();
					List<String> Nemotecnico = new ArrayList<>();
					List<String> idsTipoInstrumento = new ArrayList<>();
					params1.put("idsTipoInstrumento", idsTipoInstrumento);
					params1.put("IdInstrumento", IdInstrumento);
					params1.put("filtroPorTipo", 0);
					
					List<String> idsClasificacionTipoInstrumento = new ArrayList<>();
					params1.put("idsClasificacionTipoInstrumento", idsClasificacionTipoInstrumento);
					params1.put("filtroPorClasificacion", 1);
					IdInstrumento.add(-1L);
					idsTipoInstrumento.add("ES");
					idsTipoInstrumento.add("E1");
					idsTipoInstrumento.add("EP");
					params1.put("Nemotecnico", Nemotecnico);
					Nemotecnico.add(nemoSubyacenteOpcion);
				}
				logger.debug("INICIO::SELECT_CONSULTAR_INSTRUMENTO");
				List<DatosInstrumento> instrumentos1 = _sql.query(conn, SELECT_CONSULTAR_INSTRUMENTO, params1, DatosInstrumento.class);
				logger.debug("FIN::SELECT_CONSULTAR_INSTRUMENTO");
				if (instrumentos1.size() == 0) {
					//NO EXISTE SUBYANCENTE
					continue;
				}
				Long idInstrumentoSubyacente = instrumentos1.get(0).IdInstrumento;
				
				Instrumento inst = new Instrumento();
				List<Long> ids = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::INSInstrumento", 1L);
				inst.IdInstrumento = ids.get(0);
				inst.Nemotecnico = asdf.Nemotecnico;
				inst.RazonSocial = instrumentos1.get(0).RazonSocial;
				inst.IdInstrumentoRelacionado = idInstrumentoSubyacente;
				inst.FechaVencimiento = fechaVencimientoString;
				inst.PrecioStrike = precioStrike;
				inst.EsPutEsCall = callPut.equalsIgnoreCase("C") ? "CALL" : "PUT";
				inst.IdMoneda = "USD";
				inst.ISIN = instrumentos1.get(0).ISIN;
				inst.EstaInscrito = "S";
				inst.OrigenInstrumento = "E";
				inst.TipoRenta = "A";
				inst.IdEstado = "ACT";
				inst.Tamano = new BigDecimal(100);
				inst.PrecioReferencia = BigDecimal.ZERO;
				inst.IdTipoInstrumento = "OPC";
				inst.FechaEmision = Fecha.toDate("0001-01-01");
				inst.FechaReferencia = Fecha.toDate("0001-01-01");
				inst.CorteMinimo = BigDecimal.ZERO;
				inst.FrecuenciaCupon = BigDecimal.ZERO;
				inst.NumeroCupones = 0;
				inst.ValorizaPor = "P";
				inst.TipoBonoIRF = "";
				inst.RelacionadoGrupo = "N";
				inst.FormadorMercado = "N";
				inst.PactoAndino = "N";
				inst.LargoPlazo = "N";
				inst.IdEmisor = instrumentos1.get(0).IdEmisor;
				inst.FactorExposicion = BigDecimal.ZERO;
				inst.ModalidadLiquidacion = "C";
				inst.ModalidadEjercicio = "M";
				inst.EstiloOpcion = "E";
				List<Instrumento> instrumentosNuevos = new ArrayList<>();
				instrumentosNuevos.add(inst);
				_sql.batch(conn, INSERT_INSTRUMENTO, instrumentosNuevos, Instrumento.class);
				
				HashMap<String,Object> params3 = new HashMap<>();
				params3.put("idTipoInstrumento", "OPC");
				logger.debug("INICIO::SELECT_BOLSA");
				List <BolsaTipoInstrumento> bolsas = _sql.query(conn, SELECT_BOLSA, params3 , BolsaTipoInstrumento.class);
				logger.debug("FIN::SELECT_BOLSA");
				List<BolsaInstrumento> params4 = new ArrayList<>();
				for (BolsaTipoInstrumento bol : bolsas){ 
					BolsaInstrumento b = new BolsaInstrumento();
					b.IdInstrumento = inst.IdInstrumento;
					b.IdBolsa = bol.IdBolsa;
					params4.add(b);
				}
				
				if (params4.size() > 0) {
					_sql.batch(conn, INSERT_BOLSA_INSTRUMENTO, params4 , BolsaInstrumento.class);	
				}
				
				
				{
					HashMap<String,Object> params2 = new HashMap<>();
					List<Long> IdInstrumento2 = new ArrayList<>();
					List<String> Nemotecnico2 = new ArrayList<>();
					params2.put("IdInstrumento", IdInstrumento2);
					IdInstrumento2.add(inst.IdInstrumento);
					params2.put("Nemotecnico", Nemotecnico2);
					Nemotecnico2.add("");
					List<String> idsTipoInstrumento = new ArrayList<>();
					params2.put("idsTipoInstrumento", idsTipoInstrumento);
					idsTipoInstrumento.add("OPC");
					
					List<String> idsClasificacionTipoInstrumento = new ArrayList<>();
					params2.put("idsClasificacionTipoInstrumento", idsClasificacionTipoInstrumento);
					params2.put("filtroPorClasificacion", 1);
					
					params2.put("filtroPorTipo", 0);
					logger.debug("INICIO::SELECT_CONSULTAR_INSTRUMENTO");
					List<DatosInstrumento> instrumentos2 = _sql.query(conn, SELECT_CONSULTAR_INSTRUMENTO, params2, DatosInstrumento.class);
					logger.debug("FIN::SELECT_CONSULTAR_INSTRUMENTO");
					instrumentos2.get(0).Correlativo = asdf.Correlativo;
					salida.P_OUT_INSTRUMENTOS.add(instrumentos2.get(0));
				}
			}
			
			
		}
		
		return salida;
	}
	
	public static class BolsaTipoInstrumento {
		public String IdBolsa;
		public String IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BolsaInstrumento {
		public String IdBolsa;
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosInstrumento {
		public Long Correlativo;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String ISIN;
		public BigDecimal CorteMinimo;
		public String EstaInscrito;
		public String EsDivisible;
		public String RazonSocial;
		public Date FechaEmision;
		public Date FechaVencimiento;
		public Long IdInstrumentoRelacionado;
		public String IdEmisor;
		public String DocumentoIdentidad;
		public String IdTipoReajuste;
		public String Reajustable;
		public String IdMoneda;
		public String IdMonedaEmision;
		public String IdTipoInstrumento;
		public String IdEstado;
		public int DecimalesPrecio;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String NombreCompletoEmisor;
		public Long Tamano;
		public String TipoValorizacion;
		public String IndicadorBolson;
		public BigDecimal Tasa;
		public BigDecimal TasaEmision;
		public BigDecimal Precio;
		public String ValorizaPor;
		public String TieneCupon;
		public BigDecimal PorcentajeCastigo;
		public BigDecimal PrecioStrike;
		public String EsPutEsCall;
		public BigDecimal FrecuenciaCupon;
		public String InstrumentoAcreditado;
		public Long IdInstrumentoRaiz;
		public String EsInstrumentoCalificado;
		public Long ConteoNemotecnico;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Instrumento {
		
		public Long IdInstrumento;
		public String Nemotecnico;
		public String ISIN;
		public BigDecimal CorteMinimo;
		public String EstaInscrito;
		public String EsDivisible;
		public String RazonSocial;
		public Date FechaEmision;
		public Date FechaVencimiento;
		public Long IdInstrumentoRelacionado;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String Reajustable;
		public String IdMoneda;
		public String IdMonedaEmision;
		public String IdTipoInstrumento;
		public String IdEstado;
		public String DecimalesPrecio;
		public BigDecimal Tamano;
		public String FechaInicioVigencia;
		public String IndArt104;
		public String TasaFiscal;
		public String TipoValorizacion;
		public String ValorizaPor;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public String TieneCupon;
		public String NumeroCuotasCirculacionMesActual;
		public String NumeroAccionesCirculacion;
		public String IndicadorLiquidaDCV;
		public String IdPaisEmisor;
		public BigDecimal FrecuenciaCupon;
		public String Serie;
		public BigDecimal PrecioReferencia;
		public String IndicadorBolson;
		public Date FechaReferencia;
		public Integer NumeroCupones;
		public String CodigoRubro;
		public BigDecimal PrecioStrike;
		public String EsPutEsCall;
		public String PactoAndino;
		public String FormadorMercado;
		public String LargoPlazo;
		public String InstrumentoAcreditado;
		public String RelacionadoGrupo;
		public BigDecimal FactorExposicion;
		public String TipoRenta;
		public String OrigenInstrumento;
		public String TipoBonoIRF;
		public String ModalidadLiquidacion;
		public String ModalidadEjercicio;
		public String EstiloOpcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[] args) throws Exception  {
		
		Connection con = PlataformaUtils.getPropertiesConnection("qa2", "connections.properties");
			ConsultarDatosInstrumentos consultar = new ConsultarDatosInstrumentos();
			Gson gson = consultar.getGsonBuilder();
			
			ConsultarDatosInstrumento_Entrada entrada = gson.fromJson("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_CreaOpcionAutomatica\":\"S\",\"p_IdTipoInstrumentos\":[{\"Correlativo\":0,\"IdTipoInstrumento\":\"ACN\"}],\"p_IdClasificacionTipoInstrumentos\":[{\"Correlativo\":0,\"IdClasificacionTipoInstrumento\":\"RV\"}],\"p_Instrumentos\":[{\"Correlativo\":0,\"Nemotecnico\":\"COPEC\"}]}",
					ConsultarDatosInstrumento_Entrada.class);
			
			System.out.println(consultar.logica(con, entrada));
	 			
			
		System.out.println("== END ==");
	}
	
}




