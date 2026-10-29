package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarFondo implements JavaCall {
	
	private final static Logger logger = LogManager.getLogger(ConsultarFondo.class);
	
	private final String SQL_SELECT_CONSULTAR_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarFondo_Select_Fondo.sql");

	public static class EntradaConsultarFondo {
		public Long p_IdEmpresa;
		public String p_IdSerieFondo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Serie{
		public Long IdInterno;
		public String Fecha;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public String IdMonedaFondo;
	 	public String IdMonedaInversion;
	 	public String TipoSolicitud;
	 	public String TipoFactorAjuste;
	 	public Long IdEmpresa;
	 	public BigDecimal Porcentaje;
	 	
	 	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularFondo{
		public List<Obj_Vista> P_OUT_RESULTADO;
		
		public SalidaCalcularFondo(){
			this.P_OUT_RESULTADO = new ArrayList<Obj_Vista>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
	
	public static class Obj_Vista{
		public String IdFondo;
		public String IdEmisor;
		public Long IdPaisEmisor;
		public String NombrePaisEmisor;
		public String Nemotecnico;
		public String NombreFondo;
		public String IdTipoFondo;
		public Long IdPaisOrigen;
		public String CodigoSVS;
		public String MoneyMarket;
		public String IdCategoriaPerfilFondoMutuo;
		public Date FechaCreacion;
		public Date FechaConstitucion;
		public Date FechaCierre;
		public String Vigencia;
		public String IdTipoInstrumento;
		public String IdMoneda;
		public String IdFamiliaFondo;
		public String FondoExtranjero;
		public String NombreCorto;
		public String Descripcion;
		public String IdSerieFondo;
		public String IdSerieFondoNombre;
		public String IdSerieFondoNombreNombreSerieFondo;
		public Long IdInstrumento;
		public String NombreSerieFondo;
		public String CodigoSerie;
		public BigDecimal MontoMinimo;
		public BigDecimal MontoMaximo;
		public String Omnibus;
		public String OmnibusUI;
		public String CobraComision;
		public String CobraComisionUI;
		public String IdEstado;
		public Long DecimalesValorCuota;
		public Long DecimalesCuota;
		public BigDecimal PatrimonioMinimo;
		public Long DiasRetencionesAporte;
		public Long DiasRetencionesRescate;
		public String ExcluyeGarantia;
		public String ExcluyeGarantiaUI;
		public String Diferido;
		public String CambioFondo;
		public String EsDiferidoUI;
		public String CambioFondoUI;
		public String InstrumentoDCV;
		public String EstadoAporteComercial;
		public String EstadoRescateComercial;
		public String EstadoAporteComercialUI;
		public String EstadoRescateComercialUI;
		public String EstadoReinversionComercialUI;
		public String EstadoReinversionComercial;
		public String EstadoLiquidezComercial;
		public String EstadoAporteBackoffice;
		public String EstadoRescateBackoffice;
		public String EstadoAporteBackOfficeUI;
		public String EstadoRescateBackOfficeUI;
		public String EstadoReinversionBackofficeUI;
		public String EstadoReinversionBackoffice;
		public String EstadoLiquidezBackoffice;
		public String UsaHorarioAporteDefault;
		public String UsaHorarioRescateDefault;
		public String UsaHorarioReinversionDefault;
		public String UsaHorarioLiquidezDefault;
		public String UsaHorarioAporteDefaultUI;
		public String UsaHorarioRescateDefaultUI;
		public String UsaHorarioReinversionDefaultUI;
		public String UsaHorarioLiquidezDefaultUI;
		public Time HoraCierreAporteComercial;
		public Time HoraCierreRescateComercial;
		public Time HoraCierreReinversionComercial;
		public Time HoraCierreLiquidezComercial;
		public Time HoraCierreAporteBackoffice;
		public Time HoraCierreRescateBackoffice;
		public Time HoraCierreReinversionBackoffice;
		public Time HoraCierreLiquidezBackoffice;
		public String BloqueoAporte;
		public String BloqueoAporteUI;
		public String BloqueoRescate;
		public String BloqueoRescateUI;
		public String EsPatrimonioMinimoUI;
		public String EsPatrimonioMinimo;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdSocioDeNegocio;
		public Long IdPersona;
		public String IdMedioDePagoAbono;
		public String IdMedioDePagoCargo;
		public String NombreMedioPagoCargo;
		public String NombreMedioPagoAbono;
		public Long DiasValutaCurseAporte;
		public Long DiasValutaCurseRescate;
		public Long DiasValutaValorCuotaAporte;
		public Long DiasValutaValorCuotaRescate;
		public Long DiasValutaCertificacionAporte;
		public Long DiasValutaCertificacionRescate;
		public Long DiasComision;
		public BigDecimal PorcentajeComision;
		public BigDecimal PorcentajeRemuneracion;
		public BigDecimal PorcentajeGastoOpera;
		public BigDecimal PorcentajeCostoAdm;
		public String NombreFamilia;
		public String RazonSocialEmisor;
		public BigDecimal ValorCuota;
		public Date FechaValorCuota;
		public BigDecimal ValorDolarAporte;
		public BigDecimal ValorDolarRescate;
		public Long IdEmpresa;
		public String NombreEstado;
		public String NombreTipoInstrumento;
		public String EsComisionSalidaUI;
		public String EsComisionSalida;
		public String EsComisionEntradaUI;
		public String EsComisionEntrada;
		public String EsFondoNacional;
		public String EsFondoExtranjeroUI;
		public Long Jerarquia;
		public String NombreCategoriaPerfilFondoMutuo;
		public String NombrePais;
		public String NombreClienteAGF;
		public String CodigoPortafolioAGF;
		public Date FechaFondo;
		public String AperturaAutomaticaFondo;
		public Timestamp UltimoFechaHoraCierreAporteComercial;
		public Timestamp UltimoFechaHoraCierreRescateComercial;
		public Timestamp UltimoFechaHoraCierreReinversionComercial;
		public Timestamp UltimoFechaHoraCierreLiquidezComercial;
		public Timestamp UltimoFechaHoraCierreAporteBackOffice;
		public Timestamp UltimoFechaHoraCierreRescateBackOffice;
		public Timestamp UltimoFechaHoraCierreReinversionBackOffice;
		public Timestamp UltimoFechaHoraCierreLiquidezBackOffice;
		public String AperturaAutomaticaFondoUI;
		public String FondoNormativo;
		public String FondoNormativoUI;
		public String IdTipoDocumentoIdentidadFondo;
		public String NombreTipoDocumentoIdentidadFondo;
		public String DocumentoIdentidadFondo;
		public BigDecimal MontoMinimoInvertir;
		public BigDecimal LimiteAltoPatrimonio;
		public String AportesAltos;
		public String PatrimonioAltos;
		public BigDecimal MontoPatrimonioEmisor;
		public String AportesAltosUI;
		public String PatrimonioAltosUI;
		public String InstrumentoOWP;
		public String EsAPV;
		public String InstrumentoOWPUI;
		public String EsAPVUI;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado{
		public Long IdInterno;
		public String TipoSolicitud;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal ValorParidad;
	 	public BigDecimal ValorFactorAjuste;
	 	public BigDecimal CostoEnFactorAjuste;
	 	public String IdMonedaSistema;
	 	public BigDecimal MontoMonedaSistema;
	 	public String Resultado;
	 	public String CodigoMensaje;
	 	public BigDecimal Porcentaje;
	 	public Long DecimalesValorCuota;
	 	public Long DecimalesNumeroCuotas;
	 	public String FechaValorCuota;
	 	
	 	public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarFondo jin = gson.fromJson(in.toString(), EntradaConsultarFondo.class);
		SalidaCalcularFondo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace	
	public SalidaCalcularFondo logica(Connection con, EntradaConsultarFondo entrada) throws Exception {
		logger.info("INICIO CalcularFondo");
		long inicioConsultarFondo = System.currentTimeMillis();
		SalidaCalcularFondo out = new SalidaCalcularFondo();
		
		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSerieFondo", entrada.p_IdSerieFondo);
		List<Obj_Vista> fondos = sql.query(con, SQL_SELECT_CONSULTAR_FONDO, params, Obj_Vista.class);
		
		params.clear();
		

		List<String> series = new ArrayList<String>();
		series.add(entrada.p_IdSerieFondo);
		
		List<EntregarPreciosTasas_Salida_PreciosTasas> listaPrecios = obtenerPrecios(con, entrada, fondos);
		HashMap<String,Object> HashPrecio = Utils.List2Hash(listaPrecios, "Nemotecnico");
		
		List<Obj_Vista> listaFondos = actualizarValorCuota(HashPrecio, fondos);
	
		/*falta gestionar las validaciones*/
		out.P_OUT_RESULTADO = listaFondos;
		
		if(listaFondos == null || listaFondos.size() == 0) {
			/*for(String serie : series){
				Resultado resultadoErroneo = new Resultado();
				resultadoErroneo.CodigoMensaje = "ERROR_VALORCUOTA";
				resultadoErroneo.Resultado = "Error al obtener Valor Cuota: " + serie;
				out.P_OUT_RESULTADO.add(resultadoErroneo);
			}*/
			return out;
		}
		
		long finConsultarFondo = System.currentTimeMillis();
		logger.debug("Tiempo CalcularMontoNumeroCuota: " + (finConsultarFondo - inicioConsultarFondo));
		logger.info("FIN CalcularMontoNumeroCuota");
		
		return out;
	}
	
	private List<EntregarPreciosTasas_Salida_PreciosTasas>  obtenerPrecios(Connection con, EntradaConsultarFondo entrada, List<Obj_Vista> fondos) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.obtenerPrecios");
		//Obtiene datos Fondos
		
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");

		List<String> series = new ArrayList<String>();
		series.add(entrada.p_IdSerieFondo);
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		
		params.clear();
		if(fondos.size() == 0)
			return null;
		EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		paramsPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsPrecioTasa.p_IdModulo = null;
		paramsPrecioTasa.p_IdPais = obtenerPaisSistema(con,entrada.p_IdEmpresa);
		paramsPrecioTasa.p_IdUsuario = null;
		
		paramsPrecioTasa.p_DatosInstrumentos = fondos.parallelStream()
		.map(dato->{
			EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
			paramsRegIn.FechaConsulta = Fecha.utilDateToSqlDate(Fecha.toDate(FechaSistema));
			paramsRegIn.IdClasificacionTipoInstrumento = "FM";
			paramsRegIn.IdInstrumento = dato.IdInstrumento;
			paramsRegIn.IdMoneda = dato.IdMoneda;
			paramsRegIn.IdRegistro = dato.IdInstrumento;
			paramsRegIn.IdTipoCriterioTasa = "0";
			paramsRegIn.IdTipoInstrumento = dato.IdTipoInstrumento;
			paramsRegIn.IdUso = "CARTERA";
			paramsRegIn.Nemotecnico = dato.IdSerieFondo;
			paramsRegIn.PlazoResidual = 0L;
			paramsRegIn.TipoCartera = "0";
			return paramsRegIn;
		})
		.collect(Collectors.toList());
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);	
		logger.info("FIN::ActualizarCarterasFondosMutuos.obtenerPrecios");
		return salidaPrecioTasa.P_OUT_PRECIOSTASAS;
	}
	
	private List<Obj_Vista> actualizarValorCuota(HashMap<String,Object> HashPrecio, List<Obj_Vista> fondos){
		
		List<Obj_Vista> fondosValorCuota = fondos.parallelStream().map(dato->{
			EntregarPreciosTasas_Salida_PreciosTasas precio = (EntregarPreciosTasas_Salida_PreciosTasas) HashPrecio.get(dato.IdSerieFondo);
			dato.ValorCuota = precio != null && precio.Precio != null ? precio.Precio : BigDecimal.ZERO;
			return dato;
		}).collect(Collectors.toList());
		
		return fondosValorCuota;
	} 
	
	private Long obtenerPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.obtenerPaisSistema");
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		logger.info("FIN::ActualizarCarterasFondosMutuos.obtenerPaisSistema");
		return paisSistema;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_IdSerieFondo\":\"FMLIQUIDEZ_C\"}";
		EntradaConsultarFondo Objin = gson.fromJson(jsonRF, EntradaConsultarFondo.class);
		ConsultarFondo demo=new ConsultarFondo();				
		try {
			SalidaCalcularFondo ObjOut=demo.logica(con, Objin);
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
