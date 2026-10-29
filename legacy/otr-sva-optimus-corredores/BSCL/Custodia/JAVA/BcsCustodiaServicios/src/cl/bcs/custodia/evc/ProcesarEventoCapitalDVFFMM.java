package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.custodia.pojo.EventoCapital;

public class ProcesarEventoCapitalDVFFMM implements JavaCall {
	
	private final Log log = new Log("CUS.ProcesarEventoCapitalDVFFMM");
	
	private final String SQL_SELECT_CANTIDAD_TOTAL_FONDO = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDVFFMM_SelectCantidadTotalFondo.sql");
	private final String SQL_SELECT_CAMPOS_CALCULO = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDVFFMM_SelectValoresCalculo.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDVFFMM_InsertEventoCapitalCliente.sql");
	private final String SQL_UPDATE_ESTADO_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDVFFMM_UpdateEstadoEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDVFFMM_SelectEventoCapital.sql");
	
	public static class Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<EventoCapital> p_IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

    public static class Salida{
		public List<MensajeSalida> P_OUT_MENSAJES;
    	public Long P_OUT_ERRORESCONTADOR;
    	public List<Long> P_OUT_SECUENCIA;
    	
    	public Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}

    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
    public static class DetalleValoresParaCalculo{
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdBoveda;
    	public Long IdEventoCapital;
    	public String IdClasificacionTipoInstrumento;
    	public String IdTipoInstrumento;
    	public Long IdInstrumento;
    	public String Nemo;
    	public String IdMoneda;
    	public BigDecimal Monto;
    	public BigDecimal CantidadCuotas;
    	public BigDecimal Cantidad;
    	public BigDecimal CantidadTotalFondo;
    	public BigDecimal CantidadDerecho;
    	public BigDecimal MontoCapital;
    	public String IdTipoEventoCapital;
    	public Long Secuencia;
    	public Long IdSocioDeNegocio;
    	public String DocumentoIdentidad;
    	public String NombreCompleto;
    	public Long NumeroDividendo;
    	public String TipoCarteraNormativa;
    	public String TipoDocumentoIdentidad;
    	public String IdRegimenFondoMutuo;
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }
    
    public static class DetalleEventoCapitalCliente{
    	public Long IdEventoCapitalCliente;
    	public Long IdEventoCapital;
    	public String IdTipoEventoCapital;
    	public Long Secuencia;
    	public Long IdSocioNegocio;
    	public String TipoSocioNegocio;
    	public Long IdPortafolio;
    	public String IdTipoDocumentoIdentidad;
    	public String DocumentoIdentidad;
    	public String NombreCliente;
    	public String IdClasificacionTipoInstrumento;
    	public String IdTipoInstrumento;
    	public Long IdInstrumento;
    	public String Nemo;
    	public Long IdInstrumentoNuevo;
    	public String NemoNuevo;
    	public Date FechaCierre;
    	public Date FechaPago;
    	public BigDecimal CantidadDerecho;
    	public String TipoCartera;
    	public BigDecimal CantidadNueva;
    	public BigDecimal MontoCapital;
    	public BigDecimal MontoInteres;
    	public BigDecimal Total;
    	public String IdMoneda;
    	public Long SecuenciaCupon;
    	public String CodigoUsuarioAgente;
    	public String NombreAgente;
    	public String CodigoSucursal;
    	public Date FechaHoraIngresoRegistro;
    	public Date FechaHoraUltimaActualizacion;
    	public Long IdUsuario;
    	public String IdEstado;
    	public String IdBoveda;
    	public String IdBolsa;
    	public BigDecimal Precio;
    	public BigDecimal PrecioNuevo;
    	public Long NumeroDividendo;
    	public String Origen;
    	public Long IdEmpleadoEjecutivoPortafolio;
    	public String TipoCarteraNormativa;
    	public String IdTipoOperacionGarantia;
    	public String IdConceptoGarantia;
    	public String IdTipoReajuste;
    	public String CodigoCorredorContraparte;
    	public String TipoOperacion;
    	public String IdRegimenFondoMutuo;
    	public BigDecimal MontoRetencion;
    	
    	public DetalleEventoCapitalCliente() {
    		IdEventoCapital = 0L;
    		IdEventoCapitalCliente = 0L;
    		IdTipoEventoCapital = "";
    		Secuencia = 0L;
    		NumeroDividendo = 0L;
    		IdSocioNegocio = 0L;
    		TipoSocioNegocio = "";
    		IdPortafolio = 0L;
    		IdTipoDocumentoIdentidad = "";
    		DocumentoIdentidad = "";
    		NombreCliente = "";
    		IdClasificacionTipoInstrumento = "";
    		IdTipoInstrumento = "";
    		IdInstrumento = 0L;
    		Nemo = "";
    		IdInstrumentoNuevo = 0L;
    		NemoNuevo = "";
    		FechaCierre = Fecha.toDate("0001-01-01");
    		FechaPago = Fecha.toDate("0001-01-01");
    		CantidadDerecho = BigDecimal.ZERO;
    		TipoCartera = "";
    		CantidadNueva = BigDecimal.ZERO;
    		MontoCapital = BigDecimal.ZERO;
    		MontoInteres = BigDecimal.ZERO;
    		Total = BigDecimal.ZERO;
    		IdMoneda = "";
    		SecuenciaCupon = 0L;
    		IdEmpleadoEjecutivoPortafolio = 0L;
    		CodigoUsuarioAgente = "";
    		NombreAgente = "";
    		CodigoSucursal = "";
    		FechaHoraIngresoRegistro = Fecha.toDate("0001-01-01");
    		FechaHoraUltimaActualizacion = Fecha.toDate("0001-01-01");
    		IdUsuario = 0L;
    		IdEstado = "";
    		IdBoveda = "";
    		IdBolsa = "";
    		Origen = "";
    		IdTipoOperacionGarantia = "";
    		IdTipoReajuste = "";
    		CodigoCorredorContraparte = "";
    		TipoOperacion="";
    		IdRegimenFondoMutuo = "";
    		Precio = BigDecimal.ZERO;
    		PrecioNuevo = BigDecimal.ZERO;
    		MontoRetencion = BigDecimal.ZERO;
    	}
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(conn,in);
		JsonParser parser = new JsonParser();
			JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	public Salida logica(Connection conn, Entrada entrada) throws Exception {
		Salida salida = new Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		log.write("Inicio");
		
		try {
			List<Long> idEventos = new ArrayList<>();
			idEventos.addAll(entrada.p_IdEventoCapital.stream().map(p -> p.IdEventoCapital).collect(Collectors.toList()));
			HashMap<String,Object> params =new HashMap<String,Object>();
			params.put("IdEventoCapital", idEventos);
			List<EventoCapital> eventosCapitales = sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);
			
			if(eventosCapitales.size() == 0){
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_EVENTO_NO_EXISTE",null,conn,entrada.p_IdEmpresa));
				//salida.P_OUT_MENSAJE = "UI_ERROR_EVENTO_NO_EXISTE";
				return salida;
			}
			
			EventoCapital evento = eventosCapitales.get(0);
			
			if(!"VIG".equalsIgnoreCase(evento.IdEstado)){
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_EVENTO_NO_VIGENTE",null,conn,entrada.p_IdEmpresa));
				//salida.P_OUT_MENSAJE = "UI_ERROR_EVENTO_NO_VIGENTE";
				return salida;
			}
			
			HashMap<String,Object> paramsValoresCalculos =new HashMap<String,Object>();
			paramsValoresCalculos.put("IdEventoCapital", evento.IdEventoCapital);
			paramsValoresCalculos.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsValoresCalculos.put("Nemotecnico", evento.Nemo);
			paramsValoresCalculos.put("IdTipoInstrumento", evento.IdTipoInstrumento);
			
			List<DetalleValoresParaCalculo> valoresCalculo = sql.query(conn, SQL_SELECT_CAMPOS_CALCULO, 
					paramsValoresCalculos, DetalleValoresParaCalculo.class);
			
			List<DetalleValoresParaCalculo> cantidadTotalFondo = sql.query(conn, SQL_SELECT_CANTIDAD_TOTAL_FONDO, 
					paramsValoresCalculos, DetalleValoresParaCalculo.class);

			List<DetalleEventoCapitalCliente> insertList = new ArrayList<DetalleEventoCapitalCliente>();
			DetalleEventoCapitalCliente eventoCapitalCliente;
			BigDecimal tempPorcentaje = BigDecimal.ZERO;
			
			for (DetalleValoresParaCalculo detalleValoresParaCalculo : valoresCalculo) {
				for(EventoCapital ec : entrada.p_IdEventoCapital) {
					if(ec.IdEventoCapital.compareTo(detalleValoresParaCalculo.IdEventoCapital) == 0) {
						eventoCapitalCliente = new DetalleEventoCapitalCliente();
						tempPorcentaje = detalleValoresParaCalculo.Cantidad.multiply(new BigDecimal(100), MathContext.DECIMAL128)
								.divide(cantidadTotalFondo.get(0).CantidadTotalFondo, MathContext.DECIMAL128);
						
						tempPorcentaje = tempPorcentaje.divide(new BigDecimal(100), MathContext.DECIMAL128);
						
						eventoCapitalCliente.IdEventoCapital = ec.IdEventoCapital;
						eventoCapitalCliente.IdTipoEventoCapital = detalleValoresParaCalculo.IdTipoEventoCapital;
						eventoCapitalCliente.Secuencia = detalleValoresParaCalculo.Secuencia;
						eventoCapitalCliente.IdSocioNegocio = detalleValoresParaCalculo.IdSocioDeNegocio;
						eventoCapitalCliente.DocumentoIdentidad = detalleValoresParaCalculo.DocumentoIdentidad;
						eventoCapitalCliente.NombreCliente = detalleValoresParaCalculo.NombreCompleto;
						eventoCapitalCliente.TipoCartera = "";
						eventoCapitalCliente.SecuenciaCupon = detalleValoresParaCalculo.Secuencia;
						eventoCapitalCliente.FechaHoraIngresoRegistro = new Date(System.currentTimeMillis());
						eventoCapitalCliente.FechaHoraUltimaActualizacion = new Date(System.currentTimeMillis());
						eventoCapitalCliente.IdUsuario = entrada.p_IdUsuario;
						eventoCapitalCliente.IdEstado = "VIG";
						eventoCapitalCliente.IdPortafolio = detalleValoresParaCalculo.IdPortafolio;
						eventoCapitalCliente.IdBoveda = detalleValoresParaCalculo.IdBoveda;
						eventoCapitalCliente.IdClasificacionTipoInstrumento = detalleValoresParaCalculo.IdClasificacionTipoInstrumento;
						eventoCapitalCliente.IdTipoInstrumento = detalleValoresParaCalculo.IdTipoInstrumento;
						eventoCapitalCliente.IdInstrumento = detalleValoresParaCalculo.IdInstrumento;
						eventoCapitalCliente.Nemo = detalleValoresParaCalculo.Nemo;
						eventoCapitalCliente.IdMoneda = detalleValoresParaCalculo.IdMoneda;
						eventoCapitalCliente.MontoCapital = detalleValoresParaCalculo.Monto.multiply(tempPorcentaje, MathContext.DECIMAL128);
						eventoCapitalCliente.CantidadDerecho = evento.CantidadCuotas
								.multiply(tempPorcentaje, MathContext.DECIMAL128);
						eventoCapitalCliente.NumeroDividendo = detalleValoresParaCalculo.NumeroDividendo;
						eventoCapitalCliente.TipoCarteraNormativa = detalleValoresParaCalculo.TipoCarteraNormativa;
						eventoCapitalCliente.IdTipoDocumentoIdentidad = detalleValoresParaCalculo.TipoDocumentoIdentidad;
						eventoCapitalCliente.FechaCierre = eventosCapitales.get(0).FechaLimite;
						eventoCapitalCliente.FechaPago = eventosCapitales.get(0).FechaPago;
						eventoCapitalCliente.TipoSocioNegocio = "";
						eventoCapitalCliente.NemoNuevo = eventosCapitales.get(0).NemoNuevo != null ? eventosCapitales.get(0).NemoNuevo : "";
						eventoCapitalCliente.CodigoUsuarioAgente = "";
						eventoCapitalCliente.NombreAgente = "";
						eventoCapitalCliente.CodigoSucursal = "";
						eventoCapitalCliente.IdBolsa = "";
						eventoCapitalCliente.IdTipoOperacionGarantia = "";
						eventoCapitalCliente.IdConceptoGarantia = "";
						eventoCapitalCliente.IdTipoReajuste = "";
						eventoCapitalCliente.CodigoCorredorContraparte = "";
						eventoCapitalCliente.TipoOperacion = "";
						eventoCapitalCliente.IdRegimenFondoMutuo = detalleValoresParaCalculo.IdRegimenFondoMutuo;
						insertList.add(eventoCapitalCliente);
						break;
					}
				}
			}			
			sql.batch(conn, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, insertList, DetalleEventoCapitalCliente.class);
			ProcesarEventoCapital.agregarLogProcesarEventoCapital(conn, evento.IdEventoCapital, entrada.p_IdModulo, entrada.p_IdUsuario, entrada.p_IdEmpresa,
															"PRO", evento.IdTipoEventoCapital, insertList.size(), evento.Nemo);

			List<EventoCapital> updateList = new ArrayList<EventoCapital>();
			evento.IdEstado = "PRO";
			updateList.add(evento);
			sql.batch(conn, SQL_UPDATE_ESTADO_EVENTO_CAPITAL, updateList, EventoCapital.class);
			
			salida.P_OUT_SECUENCIA = insertList.stream().map(p -> p.IdEventoCapital).distinct().collect(Collectors.toList());
			salida.P_OUT_ERRORESCONTADOR = 0L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_EXITO",null,conn,entrada.p_IdEmpresa));
		} catch(Exception e){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR",null,conn,entrada.p_IdEmpresa));
			log.write("Error", e);
		}
		log.write("Termino");
		return salida;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
					.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":115,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":262,\"p_IdTipoEventoCapital\":\"DF\",\"p_Secuencia\":227,\"p_NumeroDividendo\":41,\"p_IdClasificacionTipoInstrumento\":\"FM\",\"p_IdTipoInstrumento\":\"FFM\",\"p_Nemo\":\"SURRAFFMMSEIRE A\",\"p_Factor\":1,\"p_Paridad\":0,\"p_FechaLimite\":\"2018-06-12\",\"p_FechaPago\":\"2018-06-17\",\"p_IdMoneda\":\"CLP\",\"p_IdInstrumento\":0,\"p_IdInstrumentoNuevo\":0,\"p_NemoNuevo\":\"\",\"p_FactorNuevo\":205,\"p_IdEstado\":\"VIG\",\"p_Indice\":0}";
			Entrada Objin = gson.fromJson(json, Entrada.class);
			
			ProcesarEventoCapitalDVFFMM demo = new ProcesarEventoCapitalDVFFMM();				
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				Salida ObjOut= demo.logica(con, Objin);
				con.commit();
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
		}	
		System.out.println("== END ==");
    }

}
