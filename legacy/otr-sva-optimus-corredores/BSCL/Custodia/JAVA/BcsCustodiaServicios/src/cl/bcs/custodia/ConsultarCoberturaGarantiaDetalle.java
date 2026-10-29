package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCoberturaGarantiaDetalle implements JavaCall{
	
	private final Logger logger = LogManager.getLogger(ConsultarCoberturaGarantiaDetalle.class.getName());
	private final String SQL_SELECT_COBERTURA_DETALLE = loadResourceToString("cl/bcs/custodia/sql/ConsultarCoberturaGarantiaDetalle_SelectCoberturaDetalle.sql");
	
	public static class EntradaConsultarCoberturaGarantiaDetalle{
		//Parametros Entrada Genericas
		public Long p_IdEmpresa;						
		public Long p_IdUsuario; 						
		public String p_IdModulo;						
		//Parametros Entrada Servicio
		public Long p_IdCliente;						
		public Long p_IdPortafolio;
		public String p_IdTipoInstrumentoGarantizado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCoberturaGarantiaDetalle{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<GarantiaDetalle> P_OUT_GARANTIASDETALLE;
		
		public SalidaConsultarCoberturaGarantiaDetalle(){
			this.P_OUT_GARANTIASDETALLE = new ArrayList<GarantiaDetalle>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GarantiaDetalle{	
		public Long IdCliente;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String PropositoPortafolio;
		public String NombreCliente;
		public String DocumentoIdentidad;
		public String IdTipoInstrumentoGarantizado;
		public Long IdInstrumentoCarteraGarantizada;
		public String NemoGarantizado;
		public Long IdContraparteGarantia;
		public String RazonSocialContraparte;		
		public String EsEspecifica;
		public Long IdOperacionOrigenGarantia;
		public String NombreOrigenGarantia;
		public String IdBolsaOperacionOrigen;
		public BigDecimal MontoValorizado;
		public BigDecimal MontoCoberturaEntidad;
		public BigDecimal MontoCoberturaCorredora;
		public BigDecimal MontoExigibilidadEntidad;
		public BigDecimal MontoExigibilidadCorredora;
		public BigDecimal PorcentajeCoberturaEntidad;
		public BigDecimal PorcentajeCoberturaCorredora;
		public BigDecimal PorcentajeExigibilidadEntidad;
		public BigDecimal PorcentajeExigibilidadCorredora;
		public BigDecimal PrecioGarantia;
		public BigDecimal PrecioMercado;
		public String EstadoCoberturaEntidad;
		public String EstadoCoberturaCorredora;
		public String IdMonedaLiquidacionOperacion;
		public Date FechaLiquidacion;
		public String IdConcepto;
		public Long FolioCarteraGarantizada;
		public Long FolioGarantia;
		public String NombreTipoInstrumentoGarantizado;
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public BigDecimal CantidadGarantia;
		public String NombreConcepto;
		public String IdMonedaInstrumento;
		public String IdTipoReajuste;
		
		public GarantiaDetalle () {
			this.MontoCoberturaEntidad=BigDecimal.ZERO;
			this.MontoCoberturaCorredora=BigDecimal.ZERO;
			this.MontoExigibilidadEntidad=BigDecimal.ZERO;
			this.MontoExigibilidadCorredora=BigDecimal.ZERO;
			this.EstadoCoberturaCorredora = "";
			this.EstadoCoberturaEntidad = "";	
		}
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarCoberturaGarantiaDetalle jin = gson.fromJson(in.toString(), EntradaConsultarCoberturaGarantiaDetalle.class);
		SalidaConsultarCoberturaGarantiaDetalle out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarCoberturaGarantiaDetalle logica(Connection con, EntradaConsultarCoberturaGarantiaDetalle entrada) throws Exception {
		logger.debug("INICIO::ConsultarCoberturaGarantiaResumen");
		Date fechaSistema = Fecha.toDate(ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"));
		SQL sql=new SQL();
		SalidaConsultarCoberturaGarantiaDetalle out = new SalidaConsultarCoberturaGarantiaDetalle();
	
		HashMap<String,Object> parametrosConsulta = new HashMap<>();
		parametrosConsulta.put("p_IdCliente", entrada.p_IdCliente);
		parametrosConsulta.put("p_IdPortafolio", entrada.p_IdPortafolio);
		parametrosConsulta.put("p_IdTipoInstrumentoGarantizado", entrada.p_IdTipoInstrumentoGarantizado);
		
		List<GarantiaDetalle> listaCoberturaDetalle = sql.query(con,SQL_SELECT_COBERTURA_DETALLE, parametrosConsulta ,GarantiaDetalle.class);
		
		//EntregarPrecioTasa
		EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		paramsPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsPrecioTasa.p_IdModulo = entrada.p_IdModulo;
		paramsPrecioTasa.p_IdPais = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		paramsPrecioTasa.p_IdUsuario = entrada.p_IdUsuario;
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas",new Long(listaCoberturaDetalle.size()));
		AtomicInteger i = new AtomicInteger(0);
		paramsPrecioTasa.p_DatosInstrumentos = listaCoberturaDetalle.parallelStream()
		.filter(dato -> !dato.IdClasificacionTipoInstrumento.equals(""))
		.map(dato->{
			EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
			paramsRegIn.FechaConsulta 					= fechaSistema;
			paramsRegIn.IdClasificacionTipoInstrumento 	= dato.IdClasificacionTipoInstrumento;
			paramsRegIn.IdInstrumento 					= dato.IdInstrumento;
			paramsRegIn.IdMoneda 						= dato.IdMonedaLiquidacionOperacion;
			paramsRegIn.IdRegistro 						= secuencias.get(i.getAndIncrement());
			paramsRegIn.IdTipoCriterioTasa 				= "0";
			paramsRegIn.IdTipoInstrumento 				= dato.IdTipoInstrumento;
			paramsRegIn.IdUso 							= "GARANTIA";
			paramsRegIn.Nemotecnico 					= dato.Nemotecnico;
			paramsRegIn.PlazoResidual 					= 0L;
			paramsRegIn.TipoCartera 					= "0";
			return paramsRegIn;
		})
		.collect(Collectors.toList());
		
		HashMap<String, EntregarPreciosTasas_Salida_PreciosTasas> hashSalidaPrecioTasa = new HashMap<String,EntregarPreciosTasas_Salida_PreciosTasas>();
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);
		for(EntregarPreciosTasas_Salida_PreciosTasas dato : salidaPrecioTasa.P_OUT_PRECIOSTASAS){
			hashSalidaPrecioTasa.putIfAbsent(dato.Nemotecnico, dato);
		}
		
		listaCoberturaDetalle.stream().forEach( cobertura -> {
			String key = cobertura.Nemotecnico;
			if(hashSalidaPrecioTasa.get(key) != null) {
				cobertura.PrecioMercado = hashSalidaPrecioTasa.get(key).Precio;
			}
			out.P_OUT_GARANTIASDETALLE.add(cobertura);
		});
		
		logger.debug("FIN::ConsultarCoberturaGarantiaDetalle size() : " + out.P_OUT_GARANTIASDETALLE.size());
		logger.debug("FIN::ConsultarCoberturaGarantiaDetalle");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":123569,\"p_IdPortafolio\":27499,\"p_IdTipoInstrumentoGarantizado\":\"SIM\"}";
		EntradaConsultarCoberturaGarantiaDetalle Objin = gson.fromJson(json, EntradaConsultarCoberturaGarantiaDetalle.class);
		ConsultarCoberturaGarantiaDetalle  demo = new ConsultarCoberturaGarantiaDetalle();				
		try {
			SalidaConsultarCoberturaGarantiaDetalle ObjOut = demo.logica(con, Objin);
			System.out.println(ObjOut.P_OUT_GARANTIASDETALLE);
			con.rollback();
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