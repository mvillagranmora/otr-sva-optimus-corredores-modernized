package cl.bcs.APV;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CambiarAPlanActivoContratoSAAPV.CambiarAPlanActivoEntrada;
import cl.bcs.APV.CambiarAPlanActivoContratoSAAPV.CambiarAPlanActivoSalida;
import cl.bcs.APV.CambiarAPlanActivoContratoSAAPV.ContratoCambioPlanIn;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjERRORESDETALLE;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjIDEVENTO;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLADETALLE;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class CambiarAPlanActivoContratoSAAPVWrapper implements JavaCall {
	private Logger log = LogManager.getLogger(CambiarAPlanActivoContratoSAAPVWrapper.class);
	
	public static class ContratoCambioPlanInWrapper {
		public Long IdPersona;
		public Long IdCliente;
		public String RutCliente;
		public Long IdPortafolio;
		public String RegimenTributario;
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CambiarAPlanActivoEntradaWrapper {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<ContratoCambioPlanInWrapper> p_ContratoCambioPlanIn;

		public CambiarAPlanActivoEntradaWrapper() {
			this.p_ContratoCambioPlanIn = new ArrayList<ContratoCambioPlanInWrapper>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CambiarAPlanActivoSalidaWrapper {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		
		public CambiarAPlanActivoSalidaWrapper() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		CambiarAPlanActivoEntradaWrapper jin = gson.fromJson(in.toString(), CambiarAPlanActivoEntradaWrapper.class);
		CambiarAPlanActivoSalidaWrapper out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public CambiarAPlanActivoSalidaWrapper logica(Connection con, CambiarAPlanActivoEntradaWrapper entrada) throws Exception {
		CambiarAPlanActivoSalidaWrapper out = new CambiarAPlanActivoSalidaWrapper();
		
		String cambioPlanRequiereAutorizacion = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "APV","APV.CambioPlanRequiereAutorizacion");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		if("S".equalsIgnoreCase(cambioPlanRequiereAutorizacion)) {
			//EVENTOS DE AUTORIZACION
			CrearEventoAutorizacionMasivo.ServiceEntrada inCrearAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
			List<ObjTABLAEVENTOS> tablaEventos = new ArrayList<ObjTABLAEVENTOS>();
			List<ObjTABLADETALLE> tablaEventoDetalle = new ArrayList<ObjTABLADETALLE>();
			Long lngEvento = 0L;
			
			Map<Long, List<ContratoCambioPlanInWrapper>> groupByIdCliente = entrada.p_ContratoCambioPlanIn.stream().collect(Collectors.groupingBy(x -> x.IdCliente));
			
			Iterator<Long> it = groupByIdCliente.keySet().iterator();
			
			while(it.hasNext()){
				Long key = (Long) it.next();
				
			  	ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();
			  	++lngEvento;
				evento.IdUnion=lngEvento;
				evento.IdTipoEvento = "MOPLAC";
				evento.IdModuloSolicita = "APV";
				evento.IdUsuarioSolicita = entrada.p_IdUsuario;
				evento.Fecha = fechaSistema;
				evento.IdTipoOperacion = null;
				evento.IdConcepto = "MOPLAC";
				evento.NumeroOperacionConcepto = null;
				evento.FechaOperacionConcepto = null;
				evento.IdCliente = key;
				evento.IdFuncionario = 0L;
				evento.IdProveedor = 0L;
				evento.IdTipoProducto = null;
				evento.IdClasificacionTipoInstrumento = null;
				evento.IdTipoInstrumento = null;
				evento.IdInstrumento = null;
				evento.Nemo = null;
				evento.Cantidad = null;
				evento.Monto = null;
				evento.Precio = null;
				evento.IdMoneda = "";
				
				tablaEventos.add(evento);
				
				int contador = 1;
				StringBuilder glosa = new StringBuilder();
				glosa.append("RutCliente: " + groupByIdCliente.get(key).get(0).RutCliente + " Portafolios APV: ");
				for(ContratoCambioPlanInWrapper contratoIn: groupByIdCliente.get(key)) {
					//agrega elementos de objeto que iran a nivel de detalle de las autorizaciones 
					ObjTABLADETALLE ItemEventoDetalle = new ObjTABLADETALLE();
					ItemEventoDetalle.IdUnion	= evento.IdUnion; 
					ItemEventoDetalle.Clasificacion = "1"; //indica que este valor ira a nivel de cabecera
					ItemEventoDetalle.NombreCampo="Datos";
					ItemEventoDetalle.TipoDato = "STRING";
					ItemEventoDetalle.Valor=contratoIn.IdPersona.toString() + ";" + contratoIn.IdCliente.toString() + ";" + contratoIn.IdPortafolio.toString() + ";" + contratoIn.IdContratoSAAPV.toString()+ ";" + contratoIn.FolioContratoSAAPV;
					tablaEventoDetalle.add(ItemEventoDetalle);	
					glosa.append(contratoIn.RegimenTributario.toString());
					
					if(contador != groupByIdCliente.get(key).size())
						glosa.append(", ");
					
					contador++;
				}
				
				evento.Glosa = glosa.toString();
			}
				
			if (tablaEventos.size() > 0) {
				CrearEventoAutorizacionMasivo crearAutorizacionService = new CrearEventoAutorizacionMasivo();
				inCrearAutorizacion.P_IDEMPRESA = entrada.p_IdEmpresa;
				inCrearAutorizacion.P_IDUSUARIO = entrada.p_IdUsuario;
				inCrearAutorizacion.P_IDMODULO = entrada.p_IdModulo;
				inCrearAutorizacion.P_SIMULAROPERACION = "N";
				inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";
				inCrearAutorizacion.P_TABLAEVENTOS = tablaEventos; 
				inCrearAutorizacion.P_DETALLE = tablaEventoDetalle;
					
				//enviar arreglo de campos nuevos
				ServiceSalida salida = crearAutorizacionService.logica(con, inCrearAutorizacion);
				
				if(salida.P_OUT_ERRORESCONTADOR > 0) {
					out.P_OUT_ERRORESCONTADOR =	salida.P_OUT_ERRORESCONTADOR;
					for(ObjERRORESDETALLE error : salida.P_OUT_ERRORESDETALLE) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR,MensajeSalida.MENSAJE_ERROR,error.Codigo,error.Descripcion));
					}
				}else {				
					out.P_OUT_ADVERTENCIASCONTADOR = lngEvento;
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"EVENTO_AUTORIZACION_CREADO","Evento de autorizacion creado."));
				}
			}
			
		}else {
			CambiarAPlanActivoContratoSAAPV cambiarAPlanActivoContratoSAAPV = new CambiarAPlanActivoContratoSAAPV();
			CambiarAPlanActivoSalida salida = new CambiarAPlanActivoSalida();
			List<ContratoCambioPlanIn> lstContratos = new ArrayList<ContratoCambioPlanIn>();
			CambiarAPlanActivoEntrada entradaConfirmar = new CambiarAPlanActivoEntrada();
			entradaConfirmar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConfirmar.p_IdModulo = entrada.p_IdModulo;
			entradaConfirmar.p_IdUsuario = entrada.p_IdUsuario;
			for(ContratoCambioPlanInWrapper in : entrada.p_ContratoCambioPlanIn) {
				ContratoCambioPlanIn contrato = new ContratoCambioPlanIn();
				contrato.IdCliente = in.IdCliente;
				contrato.IdPersona = in.IdPersona;
				contrato.IdPortafolio = in.IdPortafolio;
				contrato.IdContratoSAAPV = in.IdContratoSAAPV;
				contrato.FolioContratoSAAPV = in.FolioContratoSAAPV;
				lstContratos.add(contrato);
			}
			entradaConfirmar.p_ContratoCambioPlanIn = lstContratos;
			salida = cambiarAPlanActivoContratoSAAPV.logica(con, entradaConfirmar);
			
			out.P_OUT_ADVERTENCIASCONTADOR = 0L;
			out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
 			out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
		}
		
		return out;	
	}
	
	public static void main (String [] args) throws SQLException {
		System.out.println("== TEST ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String jsonDESA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"APV\",\"p_ContratoCambioPlanIn\":[{\"IdPersona\":1750936,\"IdCliente\":1419564,\"RutCliente\":\"115103595-7\",\"IdPortafolio\":1450489,\"RegimenTributario\":\"APV-A\",\"IdContratoSAAPV\":167,\"FolioContratoSAAPV\":\"113433\"}]}";
			CambiarAPlanActivoEntradaWrapper Objin = gson.fromJson(jsonDESA, CambiarAPlanActivoEntradaWrapper.class);

			CambiarAPlanActivoContratoSAAPVWrapper demo = new CambiarAPlanActivoContratoSAAPVWrapper();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			try {
				CambiarAPlanActivoSalidaWrapper ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
