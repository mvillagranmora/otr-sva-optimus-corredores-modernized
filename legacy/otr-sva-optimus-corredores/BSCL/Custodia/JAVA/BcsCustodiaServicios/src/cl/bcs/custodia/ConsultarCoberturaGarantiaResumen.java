package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.custodia.ConsultarCoberturaGarantiaDetalle.EntradaConsultarCoberturaGarantiaDetalle;
import cl.bcs.custodia.ConsultarCoberturaGarantiaDetalle.SalidaConsultarCoberturaGarantiaDetalle;
import cl.bcs.custodia.ConsultarCoberturaGarantiaDetalle.GarantiaDetalle;

public class ConsultarCoberturaGarantiaResumen implements JavaCall{
	
	private final Logger logger = LogManager.getLogger(ConsultarCoberturaGarantiaResumen.class.getName());
	private final String SQL_SELECT_EXIGIBILIDAD_GARANTIA_OPERACIONES = loadResourceToString("cl/bcs/custodia/sql/ConsultarCoberturaGarantiaResumen_SelectExigibilidadGarantiaOperaciones.sql");
	
	public class EntradaConsultarCoberturaGarantiaResumen{
		//Parametros Entrada Genericas
		public Long p_IdEmpresa;						
		public Long p_IdUsuario; 						
		public String p_IdModulo;						
		//Parametros Entrada Servicio
		public Long p_IdCliente;						
		public Long p_IdPortafolio;						
		public String p_IdTipoInstrumentoGarantizado;	
		public Long p_IdInstrumentoCarteraGarantizada;  
		public Long p_IdContraparteGarantia;			
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaConsultarCoberturaGarantiaResumen{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<GarantiaResumen> P_OUT_GARANTIASRESUMEN;
		
		public SalidaConsultarCoberturaGarantiaResumen(){
			this.P_OUT_GARANTIASRESUMEN = new ArrayList<GarantiaResumen>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GarantiaResumen{	
		public Long IdCliente;
		public Long IdInstrumentoCarteraGarantizada;
		public Long IdContraparteGarantia;
		public String IdConcepto;
		public String NombreCliente;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String IdTipoInstrumentoGarantizado;
		public String NombreTipoInstrumentoGarantizado;
		public String EstadoCoberturaEntidad;
		public String EstadoCoberturaCorredora;
		public String EsEspecifica;
		public Long IdOperacionOrigenGarantia;
		public String IdMonedaLiquidacionOperacion;
		public String RazonSocialContraparte;
		public BigDecimal MontoCoberturaEntidad;
		public BigDecimal MontoCoberturaCorredora;
		public BigDecimal MontoExigibilidadEntidad;
		public BigDecimal MontoExigibilidadCorredora;
		public BigDecimal PorcentajeCoberturaEntidad;
		public BigDecimal PorcentajeCoberturaCorredora;
		public BigDecimal PorcentajeExigibilidadEntidad;
		public BigDecimal PorcentajeExigibilidadCorredora;
		
		public GarantiaResumen () {
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
	
	public static class ObjOrden {
		public Long IdOrden;
		public String IdOperacionOrigenGarantia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarCoberturaGarantiaResumen jin = gson.fromJson(in.toString(), EntradaConsultarCoberturaGarantiaResumen.class);
		SalidaConsultarCoberturaGarantiaResumen out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarCoberturaGarantiaResumen logica(Connection con, EntradaConsultarCoberturaGarantiaResumen entrada) throws Exception {
		
		logger.debug("INICIO::ConsultarCoberturaGarantiaResumen");
		SalidaConsultarCoberturaGarantiaResumen out = new SalidaConsultarCoberturaGarantiaResumen();
		
		EntradaConsultarCoberturaGarantiaDetalle entradaCoberturaDetalle = new EntradaConsultarCoberturaGarantiaDetalle();
		entradaCoberturaDetalle.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCoberturaDetalle.p_IdModulo = entrada.p_IdModulo;
		entradaCoberturaDetalle.p_IdUsuario = entrada.p_IdUsuario;
		entradaCoberturaDetalle.p_IdCliente = entrada.p_IdCliente;
		entradaCoberturaDetalle.p_IdPortafolio = entrada.p_IdPortafolio;
		entradaCoberturaDetalle.p_IdTipoInstrumentoGarantizado = entrada.p_IdTipoInstrumentoGarantizado;
		
		ConsultarCoberturaGarantiaDetalle consultarCoberturaGarantiaDetalle = new ConsultarCoberturaGarantiaDetalle();
		SalidaConsultarCoberturaGarantiaDetalle salidaCoberturaDetalle = consultarCoberturaGarantiaDetalle.logica(con, entradaCoberturaDetalle);
		
		List<GarantiaDetalle> listaCoberturaDetalleFiltrada = new ArrayList<>();
		if(!salidaCoberturaDetalle.P_OUT_GARANTIASDETALLE.isEmpty()) {
		    listaCoberturaDetalleFiltrada = salidaCoberturaDetalle.P_OUT_GARANTIASDETALLE.stream()
		    .filter(cob -> (entrada.p_IdInstrumentoCarteraGarantizada.compareTo(0L)!=0) ? cob.IdInstrumentoCarteraGarantizada.equals(entrada.p_IdInstrumentoCarteraGarantizada) : true)
		    .filter(cob -> (entrada.p_IdContraparteGarantia.compareTo(0L)!=0) ? cob.IdContraparteGarantia.equals(entrada.p_IdContraparteGarantia) : true)
		    .filter(cob -> {
		        if (!entrada.p_IdTipoInstrumentoGarantizado.equals("")) {
		            if (entrada.p_IdTipoInstrumentoGarantizado.equals("DER") && cob.IdConcepto.equals("GARDVA")) {
		                return true;
		            } else {
		                return cob.IdTipoInstrumentoGarantizado.equals(entrada.p_IdTipoInstrumentoGarantizado);
		            }
		        }
		        return true;
		    }).collect(Collectors.toList());
		}
		
		List<GarantiaResumen> listaCoberturaResumen = agruparCoberturaResumen(listaCoberturaDetalleFiltrada);
		HashMap<String,GarantiaResumen>  hashExigibilidad = obtenerExigibilidadGarantia(con, entrada);
		
		listaCoberturaResumen.stream().forEach(coberturaAgrupada-> {
			coberturaAgrupada.EstadoCoberturaEntidad = "CUBIERTO";
			coberturaAgrupada.EstadoCoberturaCorredora = "CUBIERTO";
			
			String key = crearKey(coberturaAgrupada);
			if(hashExigibilidad.get(key) != null) {
				coberturaAgrupada.MontoExigibilidadCorredora = hashExigibilidad.get(key).MontoExigibilidadCorredora ;
				coberturaAgrupada.MontoExigibilidadEntidad = hashExigibilidad.get(key).MontoExigibilidadEntidad;
				coberturaAgrupada.PorcentajeExigibilidadCorredora = hashExigibilidad.get(key).PorcentajeExigibilidadCorredora;
				coberturaAgrupada.PorcentajeExigibilidadEntidad = hashExigibilidad.get(key).PorcentajeExigibilidadEntidad;
				BigDecimal entidadCalculo = coberturaAgrupada.MontoCoberturaEntidad.subtract(hashExigibilidad.get(key).MontoExigibilidadEntidad);
				BigDecimal corredoraCalculo = coberturaAgrupada.MontoCoberturaCorredora.subtract(hashExigibilidad.get(key).MontoExigibilidadEntidad);
				coberturaAgrupada.EstadoCoberturaCorredora = corredoraCalculo.compareTo(BigDecimal.ZERO) <= 0 ? "DESCUBIERTO" : "CUBIERTO";
				coberturaAgrupada.EstadoCoberturaEntidad = entidadCalculo.compareTo(BigDecimal.ZERO) <= 0 ? "DESCUBIERTO" : "CUBIERTO";
			}
			out.P_OUT_GARANTIASRESUMEN.add(coberturaAgrupada);
		});
		
		logger.debug("FIN::ConsultarCoberturaGarantiaResumen size() : " + out.P_OUT_GARANTIASRESUMEN.size());
		logger.debug("FIN::ConsultarCoberturaGarantiaResumen");
		return out;
	}
	
	private  HashMap<String,GarantiaResumen> obtenerExigibilidadGarantia (Connection con, EntradaConsultarCoberturaGarantiaResumen in) throws Exception{
		SQL sql=new SQL();
		HashMap<String,GarantiaResumen> hashSalida = new HashMap<String,GarantiaResumen>();
		HashMap<String,Object> parametrosConsulta = new HashMap<>();
		parametrosConsulta.put("p_IdCliente", in.p_IdCliente);
		parametrosConsulta.put("p_IdPortafolio", in.p_IdPortafolio);
		parametrosConsulta.put("p_IdTipoInstrumentoGarantizado", in.p_IdTipoInstrumentoGarantizado);
	    parametrosConsulta.put("p_IdInstrumentoCarteraGarantizada", in.p_IdInstrumentoCarteraGarantizada);
		parametrosConsulta.put("p_IdContraparteGarantia", in.p_IdContraparteGarantia);
		
		List<GarantiaResumen> listaExigibilidadGarantiaOperaciones= sql.query(con,SQL_SELECT_EXIGIBILIDAD_GARANTIA_OPERACIONES, parametrosConsulta ,GarantiaResumen.class);
		
		listaExigibilidadGarantiaOperaciones.stream().forEach(exigibilidadGarantia -> hashSalida.put(crearKey(exigibilidadGarantia), exigibilidadGarantia));
		return hashSalida;
	}
	
	private String crearKey(GarantiaResumen obj) {
		return obj.IdCliente+obj.IdPortafolio+obj.IdTipoInstrumentoGarantizado;
	}
	private List<GarantiaResumen> agruparCoberturaResumen(List<GarantiaDetalle> listaCoberturaDetalleFiltrada ){	
		List<GarantiaResumen> listaCoberturaAgrupada = new ArrayList<>();
		HashMap<String,GarantiaResumen> hashCoberturaResumen = new HashMap<String,GarantiaResumen>();
		for(GarantiaDetalle coberturaDetalle : listaCoberturaDetalleFiltrada) {
			GarantiaResumen coberturaResumen = new GarantiaResumen();
			String key = coberturaDetalle.IdCliente+coberturaDetalle.IdPortafolio+coberturaDetalle.IdTipoInstrumentoGarantizado;
			if(hashCoberturaResumen.get(key)!=null) {
				hashCoberturaResumen.get(key).MontoCoberturaEntidad   = hashCoberturaResumen.get(key).MontoCoberturaEntidad.add(coberturaDetalle.MontoCoberturaEntidad);
				hashCoberturaResumen.get(key).MontoCoberturaCorredora = hashCoberturaResumen.get(key).MontoCoberturaCorredora.add(coberturaDetalle.MontoCoberturaCorredora);
			}else {
				coberturaResumen.IdCliente = coberturaDetalle.IdCliente;
				coberturaResumen.IdInstrumentoCarteraGarantizada = coberturaDetalle.IdInstrumentoCarteraGarantizada;
				coberturaResumen.IdContraparteGarantia = coberturaDetalle.IdContraparteGarantia;
				coberturaResumen.IdConcepto = coberturaDetalle.IdConcepto;
				coberturaResumen.NombreCliente = coberturaDetalle.NombreCliente;
				coberturaResumen.DocumentoIdentidad = coberturaDetalle.DocumentoIdentidad;
				coberturaResumen.IdPortafolio = coberturaDetalle.IdPortafolio;
				coberturaResumen.CodigoPortafolio = coberturaDetalle.CodigoPortafolio;
				coberturaResumen.IdTipoInstrumentoGarantizado = coberturaDetalle.IdTipoInstrumentoGarantizado;
				coberturaResumen.NombreTipoInstrumentoGarantizado = coberturaDetalle.NombreTipoInstrumentoGarantizado;
				coberturaResumen.EstadoCoberturaEntidad = coberturaDetalle.EstadoCoberturaEntidad;
				coberturaResumen.EstadoCoberturaCorredora = coberturaDetalle.EstadoCoberturaCorredora;
				coberturaResumen.EsEspecifica = coberturaDetalle.EsEspecifica;
				coberturaResumen.IdOperacionOrigenGarantia = coberturaDetalle.IdOperacionOrigenGarantia;
				coberturaResumen.IdMonedaLiquidacionOperacion = coberturaDetalle.IdMonedaLiquidacionOperacion;
				coberturaResumen.RazonSocialContraparte = coberturaDetalle.RazonSocialContraparte;
				coberturaResumen.MontoCoberturaEntidad = coberturaDetalle.MontoCoberturaEntidad;
				coberturaResumen.MontoCoberturaCorredora = coberturaDetalle.MontoCoberturaCorredora;
				coberturaResumen.MontoExigibilidadEntidad = coberturaDetalle.MontoExigibilidadEntidad;
				coberturaResumen.MontoExigibilidadCorredora = coberturaDetalle.MontoExigibilidadCorredora;
				coberturaResumen.PorcentajeCoberturaEntidad = coberturaDetalle.PorcentajeCoberturaEntidad;
				coberturaResumen.PorcentajeCoberturaCorredora = coberturaDetalle.PorcentajeCoberturaCorredora;
				coberturaResumen.PorcentajeExigibilidadEntidad = coberturaDetalle.PorcentajeExigibilidadEntidad;
				coberturaResumen.PorcentajeExigibilidadCorredora = coberturaDetalle.PorcentajeExigibilidadCorredora;
				hashCoberturaResumen.put(key, coberturaResumen);
			}
		}
		
		for(Map.Entry<String, GarantiaResumen> obj : hashCoberturaResumen.entrySet()){
			listaCoberturaAgrupada.add(obj.getValue());
        }
		
		return listaCoberturaAgrupada;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_IdTipoInstrumentoGarantizado\":\"DER\",\"p_IdInstrumentoCarteraGarantizada\":0,\"p_IdContraparteGarantia\":0}";
		EntradaConsultarCoberturaGarantiaResumen Objin = gson.fromJson(json, EntradaConsultarCoberturaGarantiaResumen.class);
		ConsultarCoberturaGarantiaResumen  demo = new ConsultarCoberturaGarantiaResumen();				
		try {
			SalidaConsultarCoberturaGarantiaResumen ObjOut = demo.logica(con, Objin);
			System.out.println(ObjOut.P_OUT_GARANTIASRESUMEN);
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
