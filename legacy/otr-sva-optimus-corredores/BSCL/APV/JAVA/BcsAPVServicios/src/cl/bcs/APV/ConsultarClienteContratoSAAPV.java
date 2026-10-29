package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarClienteContratoSAAPV implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ConsultarClienteContratoSAAPV.class);
	
	//TODO revisar performance de las querys poniendo los filtros en el join o en el where
	public final String SQL_SELECT_CLIENTE_CONTRATO = loadResourceToString("cl/bcs/APV/sql/ConsultarClienteContratoSAAPV_SelectClienteContrato.sql");
	public final String SQL_SELECT_CONTRATO = loadResourceToString("cl/bcs/APV/sql/ConsultarClienteContratoSAAPV_SelectContrato.sql");
	public final String SQL_SELECT_CONTRATO_MODAL = loadResourceToString("cl/bcs/APV/sql/ConsultarClienteContratoSAAPV_SelectContratoModal.sql");
	
	
	public static class PersonaPortafolio {
		public Long IdPersona;
		public Long IdPortafolio;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarClienteContratoSAAPVEntrada {
	    public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_Consulta;
	    public List<PersonaPortafolio> p_PersonaPortafolio;
	    
	    public ConsultarClienteContratoSAAPVEntrada() {
	    	this.p_PersonaPortafolio = new ArrayList<PersonaPortafolio>();
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ConsultarClienteContratoSAAPVSalida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ClienteContratoSAAPV> P_OUT_CLIENTECONTRATOSAAPV;
		public List<ContratoSAAPV> P_OUT_CONTRATOSAAPV;
		public List<ContratoSAAPVModal> P_OUT_CONTRATOSAAPVMODAL;
		
		public ConsultarClienteContratoSAAPVSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CLIENTECONTRATOSAAPV = new ArrayList<ClienteContratoSAAPV>();
			this.P_OUT_CONTRATOSAAPV = new ArrayList<ContratoSAAPV>();
			this.P_OUT_CONTRATOSAAPVMODAL = new ArrayList<ContratoSAAPVModal>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ClienteContratoSAAPV {
		public Long IdPersona;
		public String RutCliente;
		public String NombrePersona;
		public Long IdPortafolio;
		public String Producto;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    @Override
		public boolean equals(Object o) {
	    	ClienteContratoSAAPV cc = (ClienteContratoSAAPV) o;
			return this.IdPersona.compareTo(cc.IdPersona) == 0 && this.RutCliente.equals(cc.RutCliente) &&
					this.NombrePersona.equals(cc.NombrePersona) && this.IdPortafolio.compareTo(cc.IdPortafolio) == 0 &&
					this.Producto.equals(cc.Producto);
		}
	}
	
	public static class ContratoSAAPV {
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
		public String Folio;
		public String NombrePlan;
		public String RutEmpleador;
		public String NombreEmpleador;
		public String NombreEncargado;
		public Date FechaSuscripcion;
		public Date FechaCreacion;
		public Date FechaAprobacion;
		public Date FechaNotificacion;
		public Date FechaRecepcion;
		public String OrigenAhorro;
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;
		public BigDecimal PorcentajeRegimenImponible;
		public String Pago;
		public String PeriodoInicio;
		public String PeriodoTermino;
		public String RegimenTributario;
		public String FormaDePago;
		public String NombreMedioRecaudacion;
		public Long DiaDescuento;
		public String Institucion;
		public BigDecimal ComisionTransferencia;
		public String CodigoTipoSolicitud;
		public String RutEntidadOrigen;
		public String NombreEntidadOrigen;
		public String TipoDeAhorro;
		public String FondoDeOrigen;
		public String TipoTraspaso;
		public BigDecimal MontoTraspaso;
		public BigDecimal UFTraspaso;
		public BigDecimal PorcentajeTraspaso;
		public String EstadoNotificacion;
		public String IdEstadoEvtAut;
		public String NombreEstadoAPV;
		public String EstadoNuevo;
		public String EstadoPAC;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    public Long getIdContratoSAAPV() {
	    	return IdContratoSAAPV;
	    }
	}
	
	public static class ContratoSAAPVModal {
		public Long IdPersona;
		public String RutCliente;
		public String NombrePersona;
		public Long IdPortafolio;
		public String Producto;
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    public Long getIdContratoSAAPV() {
	    	return IdContratoSAAPV;
	    }
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ConsultarClienteContratoSAAPVEntrada jin = gson.fromJson(in.toString(), ConsultarClienteContratoSAAPVEntrada.class);
		ConsultarClienteContratoSAAPVSalida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
			
	public ConsultarClienteContratoSAAPVSalida logica(Connection con, ConsultarClienteContratoSAAPVEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = 0L;
		logger.info("Inicio servicio ConsultarClienteContratoSAAPV");
		
		SQL sql = new SQL();
		ConsultarClienteContratoSAAPVSalida out = new ConsultarClienteContratoSAAPVSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		try {
			List<Long> idPersonaList = new ArrayList<Long>();
			int filtroIdPersona = 0;
			
			//Obtiene Clientes con Contratos SAAPV con Plan Abierto(1)
			if (in.p_PersonaPortafolio.size() > 0) {
				idPersonaList = in.p_PersonaPortafolio.stream().filter(pp -> pp.IdPersona != null).map(pp -> pp.IdPersona).distinct().collect(Collectors.toList());
				filtroIdPersona = idPersonaList.size();
			}
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdPlan", 1);
			params.put("FiltroIdPersona", filtroIdPersona);
			params.put("IdPersona", idPersonaList);
			
			if ("GRILLA".equals(in.p_Consulta)) {
				logger.debug("Inicio SQL_SELECT_CLIENTE_CONTRATO");
				startTime = System.nanoTime();
				List<ClienteContratoSAAPV> clienteContratoSAAPVList = sql.query(con, SQL_SELECT_CLIENTE_CONTRATO, params, ClienteContratoSAAPV.class);
				logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_CLIENTE_CONTRATO");
				
				HashMap<String, Boolean> clienteContratoSAAPVHash = new HashMap<String, Boolean>();
				for (ClienteContratoSAAPV clienteContratoSAAPV : clienteContratoSAAPVList) {
					if (!clienteContratoSAAPVHash.containsKey(clienteContratoSAAPV.IdPersona+"_"+clienteContratoSAAPV.IdPortafolio)) {
						clienteContratoSAAPVHash.put(clienteContratoSAAPV.IdPersona+"_"+clienteContratoSAAPV.IdPortafolio, true);
						out.P_OUT_CLIENTECONTRATOSAAPV.add(clienteContratoSAAPV);
					}
				}
			}
			//Obtiene Contratos SAAPV con Plan Abierto(1)
			else if ("SUBGRILLA".equals(in.p_Consulta) || "MODAL".equals(in.p_Consulta)) {
				List<Long> idPortafolioList = new ArrayList<Long>();
				int filtroIdPortafolio = 0;
				
				if (in.p_PersonaPortafolio.size() > 0) {
					idPortafolioList = in.p_PersonaPortafolio.stream().filter(pp -> pp.IdPortafolio != null).map(pp -> pp.IdPortafolio).distinct().collect(Collectors.toList());
					filtroIdPortafolio = idPortafolioList.size();
				}
				
				params.put("FiltroIdPortafolio", filtroIdPortafolio);
				params.put("IdPortafolio", idPortafolioList);
				
				if ("SUBGRILLA".equals(in.p_Consulta)) {
					out.P_OUT_CONTRATOSAAPV = sql.query(con, SQL_SELECT_CONTRATO, params, ContratoSAAPV.class);
					
					for (ContratoSAAPV contrato : out.P_OUT_CONTRATOSAAPV) {
						contrato.NombreMedioRecaudacion = "DIRECTA".equals(contrato.FormaDePago) ? contrato.NombreMedioRecaudacion : null;
						contrato.DiaDescuento = "DIRECTA".equals(contrato.FormaDePago) ? "PAC".equals(contrato.NombreMedioRecaudacion) ? contrato.DiaDescuento : null : null;
						contrato.Institucion = "INDIRECTA".equals(contrato.FormaDePago) ? contrato.Institucion : null;
						contrato.ComisionTransferencia = "INDIRECTA".equals(contrato.FormaDePago) ? contrato.ComisionTransferencia : null;
						contrato.EstadoNuevo = "VCD".equals(contrato.IdEstadoEvtAut) || "VIG".equals(contrato.IdEstadoEvtAut) ? "PENDIENTE DE APROBACION" : "RZD".equals(contrato.IdEstadoEvtAut) ? "RECHAZADO" : contrato.NombreEstadoAPV;
					}
					
					out.P_OUT_CONTRATOSAAPV.sort(Comparator.comparing((ContratoSAAPV contratoSAAPV) -> contratoSAAPV.getIdContratoSAAPV()));
				}
				else {
					out.P_OUT_CONTRATOSAAPVMODAL = sql.query(con, SQL_SELECT_CONTRATO_MODAL, params, ContratoSAAPVModal.class);
					out.P_OUT_CONTRATOSAAPVMODAL.sort(Comparator.comparing((ContratoSAAPVModal contratoSAAPVModal) -> contratoSAAPVModal.getIdContratoSAAPV()));
				}
			} 
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		logger.info("Termino del servicio ConsultarClienteContratoSAAPV, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String jsonDE = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"APV\",\"p_Consulta\":\"GRILLA\",\"p_PersonaPortafolio\":[{\"IdPersona\":null,\"IdPortafolio\":null}]}";
			//String jsonDECLI = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"APV\",\"p_Consulta\":\"GRILLA\",\"p_PersonaPortafolio\":[{\"IdPersona\":41162,\"IdPortafolio\":null}]}";
			String jsonDEDetalle = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"APV\",\"p_Consulta\":\"SUBGRILLA\",\"p_PersonaPortafolio\":[{\"IdPersona\":41162,\"IdPortafolio\":1451540}]}";
			String jsonDEModal = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"APV\",\"p_Consulta\":\"MODAL\",\"p_PersonaPortafolio\":[{\"IdPersona\":41162,\"IdPortafolio\":1451540}]}";
			ConsultarClienteContratoSAAPVEntrada Objin = gson.fromJson(jsonDEDetalle, ConsultarClienteContratoSAAPVEntrada.class);
			
			ConsultarClienteContratoSAAPV demo = new ConsultarClienteContratoSAAPV();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				ConsultarClienteContratoSAAPVSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}