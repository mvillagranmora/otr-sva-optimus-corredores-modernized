package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CambiarEstadoContratosSAAPV.ConsultarDatosPersona;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosEntradaServicio;
import cl.bcs.APV.GrabarContratoSAAPV.PortafolioContratoSAAPV;
import cl.bcs.APV.ModificarPortafolioTraspaso.vistaInfoPort;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GrabarTraspasos implements JavaCall   {

	/*
	 * El log
	 */	
	//private final Log log = new Log("Afiliacion.GrabarContraroSAAPV");
	private final static Logger logger = LogManager.getLogger(GrabarTraspasos.class);
	public String SQL_CREAR_DETALLETRASPASO = loadResourceToString("cl/bcs/APV/sql/DetalleTraspaso_Insert.sql");
	public String SQL_MODIFICA_TRASPASO = loadResourceToString("cl/bcs/APV/sql/Traspaso_Update.sql");
	public String SQL_CREAR_PORTAFOLIO_APV_Call = loadResourceToString("cl/bcs/APV/sql/Crear_Portafolio_APV_Call.sql");
	public String SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV = loadResourceToString("cl/bcs/APV/sql/Crear_PortafolioContratoSAAPV.sql");
	public String SQL_VISTA_PORTAFOLIOS_CLIENTE = "SELECT \"IdPortafolio\", \"CodigoPortafolio\", \"Proposito\", \"IdCliente\", \"IdEstado\" FROM BSCL.\"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio\"  WHERE \"IdCliente\" = :IdCliente AND \"Proposito\" LIKE '%PROPOSITOAPV%';";
	private final String SQL_SELECT_ESTADO_CLIENTE = loadResourceToString("cl/bcs/APV/sql/GrabarTraspasos_SelectEstadoCliente.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	public static class Traspaso{
		public Long 		IdTraspaso;
		public Long 		IdContratoSAAPV;
		public String		FolioContratoSAAPV;
		public String 		FechaIngreso;
		public String 		FechaModificacion;
		public Long			IdPersonaCliente;
		public Long 		IdCliente;
		public String 		DocumentoIdentidad;
		public String       IdPerfilComercial;
		
		//Datos Detalle Traspaso
		public Long			IdDetalleTraspaso;
		public Long 		IdPortafolio;
		public String		IdPropositoPortafolio;
		public Long 		IdPlanTipoInversion;
		public String		NombrePlan;
		public Long 		IdProductoAgrupador;
		public String		ProductoAgrupador;
		public Long 		IdRegimenTributario;
		public String		RegimenTributario;
		public String 		CodigoAgrupador;
		public BigDecimal 	MontoPeso;
		public BigDecimal 	MontoUF;
		public BigDecimal 	Porcentaje;
	}
	
	public static class DetalleTraspaso{
		public Long 		IdTraspaso;
		public Long			IdDetalleTraspaso;
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public String 		FechaIngreso;
		public String 		FechaModificacion;
		public Long 		IdPlanTipoInversion;
		public Long 		IdProductoAgrupador;
		public Long 		IdRegimenTributario;
		public BigDecimal 	MontoPeso;
		public BigDecimal 	MontoUF;
		public BigDecimal 	Porcentaje;
		public String		IdEstado;
		public String		EsReapertura;
	}
	
	public static class TraspasoModificar{
		public Long			IdTraspaso;
		public String		IdEstado;
	}
	
	public static class FormaDePago{
		public String OrigenDestino;
		public Long IdBanco;
		public Long IdSocioDeNegocio;
		public Long IdPersona;
	}
	
	public static class EstadoCliente extends BcsPojo {
		public Long		IdCliente;
		public String	IdEstado;
		public String	DocumentoIdentidad;
	}

	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public String					p_Opcion;
		public String					p_Fecha;
		public List<Traspaso>			p_Traspasos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			 = new ParametrosSalidaServicio ();
		MensajeSalida               msg              = new MensajeSalida();
		SalidaServicioCrearCliente	salidaPortafolio = new SalidaServicioCrearCliente();
		
		int contador = 0;
		Long IdPortafolioCreado = 0L;
		Long IdTraspaso = 0L;
		Long IdCliente = 0L;
		String FechaIngresotraspaso = "";
		List<DetalleTraspaso> lstDetalleTraspaso = new ArrayList<DetalleTraspaso>();
		List<PortafolioContratoSAAPV> lstPortCont = new ArrayList<PortafolioContratoSAAPV>();
		List<Long> IdDetalleTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdDetalleTraspaso", (long)in.p_Traspasos.size());
		String ApruebaTraspasoAutomatico = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "APV","APV.ApruebaTraspasoAutomatico");
		
		java.sql.Date FechaIngreso = new java.sql.Date(System.currentTimeMillis());	
		DateFormat PeriodoFecha = new SimpleDateFormat("yyyy-MM");			
		String FechaPeriodo = PeriodoFecha.format(FechaIngreso);	
		List<Long> listaIdCliente = new ArrayList<>();
		List<EstadoCliente> listaEstadoCliente = new ArrayList<>();
		HashMap<Long,Object> hashCliente = new HashMap<>();
		
		if(in.p_Traspasos.size() > 0) {
			listaIdCliente = in.p_Traspasos.stream()
					.map(traspaso -> traspaso.IdCliente != null ? traspaso.IdCliente :0L)
					.distinct()
					.collect(Collectors.toList());
			
			if(listaIdCliente.size() > 0) {
				listaEstadoCliente = obtenerEstadoCliente(con, sql, listaIdCliente);
				
				if(listaEstadoCliente.size() > 0) {
					hashCliente = Utils.List2Hash(listaEstadoCliente, "IdCliente");
				}
			}
		}
		Long ClienteAnterior = 0L;
		for(Traspaso Fila:in.p_Traspasos){
			
			String DocumentoIdentidad = "";
			
			//Validar estado Cliente
			if(hashCliente.size() > 0) {
				EstadoCliente estadoCliente = (EstadoCliente) hashCliente.get(Fila.IdCliente);
				
				if(estadoCliente != null) {
					DocumentoIdentidad = estadoCliente.DocumentoIdentidad;
						if("BLO".equalsIgnoreCase(estadoCliente.IdEstado)) {
							if(ClienteAnterior.compareTo(estadoCliente.IdCliente) != 0) {
								String codMsg = "CLIENTE_BLOQUEADO";
								String descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa,codMsg)
										  .add("DocumentoIdentidad", DocumentoIdentidad)
										  .msg();
								salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,descripcion));
							}
							ClienteAnterior = estadoCliente.IdCliente;
							continue;
						}
				}
			}
			
			//Estado portafolios cliente
			ModificarPortafolioTraspaso modificarEstadoPort = new ModificarPortafolioTraspaso();
			HashMap<String,Object> portPrevio = new HashMap<String,Object>();
			portPrevio.put("IdCliente", Fila.IdCliente);
			List<vistaInfoPort> portPrevioSelect = sql.query(con, SQL_VISTA_PORTAFOLIOS_CLIENTE, portPrevio, vistaInfoPort.class);
			
			//Valido si el Portafolio Existe
			ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada inValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada();
			ValidaContratoSAAPV.ValidaContratoSAAPV_Salida outValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Salida();
			List<ValidaContratoSAAPV.ContratosSAAPV> tablaValidaContrato = new ArrayList<ValidaContratoSAAPV.ContratosSAAPV>();
			IdPortafolioCreado = 0L;
			ValidaContratoSAAPV.ContratosSAAPV ContratoValidaContrato = new ValidaContratoSAAPV.ContratosSAAPV();
			
			ContratoValidaContrato.IdPersona = null;
			ContratoValidaContrato.IdCliente = null;
			ContratoValidaContrato.IdEmpleador = null;
			ContratoValidaContrato.IdPortafolio = null;
			ContratoValidaContrato.DocumentoIdentidad = Fila.DocumentoIdentidad;
			ContratoValidaContrato.DocumentoIdentidadEmpleador = "";
			ContratoValidaContrato.FolioContratoSAAPV = "";
			ContratoValidaContrato.Codigo = Fila.RegimenTributario;
			ContratoValidaContrato.NombrePlan = Fila.NombrePlan;
			ContratoValidaContrato.CodigoAgrupador = Fila.CodigoAgrupador;
			tablaValidaContrato.add(ContratoValidaContrato);	
		
			ValidaContratoSAAPV ValidaContratoSAAPVService = new ValidaContratoSAAPV();
			inValidaContrato.p_IdEmpresa = in.p_IdEmpresa;
			inValidaContrato.p_IdUsuario = in.p_IdUsuario;
			inValidaContrato.p_IdModulo = in.p_IdModulo;
			inValidaContrato.p_Opcion = "I";
			inValidaContrato.p_ContratosSAAPV = tablaValidaContrato;
			outValidaContrato = ValidaContratoSAAPVService.logica(con, inValidaContrato);

			if(outValidaContrato.P_OUT_ERRORESCONTADOR == null){// && outValidaContrato.P_OUT_ERRORESCONTADOR == 0 ){
				for (ValidaContratoSAAPV.ModificaContratoSAAPV filaValidaContrato : outValidaContrato.P_IDSCONTRATO) {
					if(filaValidaContrato.Codigo != null){
						if(filaValidaContrato.IdPortafolio != null)
							IdPortafolioCreado	= filaValidaContrato.IdPortafolio;
					}	
				}
			}else{					
				msg.id = IdDetalleTraspaso.get(contador);
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_VALIDA_PORTAFOLIO";
				msg.descripcion = "Problema con la valdiacion de Portafolio.";
			}	
			
			if(IdPortafolioCreado > 0) {
				if(portPrevioSelect.size() > 0) {
					HashMap<Long,Object> hashIdportafolio = Utils.List2Hash(portPrevioSelect, "IdPortafolio");
					if(hashIdportafolio.size() >0) {
						vistaInfoPort estadoPortafolio = (vistaInfoPort) hashIdportafolio.get(IdPortafolioCreado);
						if(estadoPortafolio != null) {
							if("BLO".equalsIgnoreCase(estadoPortafolio.IdEstado)) {
								String codMsg = "PORTAFOLIO_BLOQUEADO";
								String descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa,codMsg)
										.add("CodigoPortafolio", Fila.RegimenTributario)
										.add("DocumentoIdentidad", DocumentoIdentidad != "" ? DocumentoIdentidad:Fila.DocumentoIdentidad)
										.msg();
								salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,descripcion));
								continue;
							}
						}
					}
				}
			}
			
			if(IdPortafolioCreado == 0) {
				//Creo Portafolio segun Regimen
				HashMap<String,Object> paramsIn = new HashMap<String,Object>();
				paramsIn.put("IdEmpresa", in.p_IdEmpresa);
				paramsIn.put("IdUsuario", in.p_IdUsuario);
				paramsIn.put("IdModulo", in.p_IdModulo);
				paramsIn.put("PropositoPortafolio", Fila.IdPropositoPortafolio);
				paramsIn.put("IdCliente", Fila.IdCliente);
				paramsIn.put("IdPerfilComercial", Fila.IdPerfilComercial);
				paramsIn.put("CodigoPortafolio", Fila.RegimenTributario);
				paramsIn.put("CodigoRegimen", Fila.RegimenTributario);
				paramsIn.put("NombrePlan", Fila.NombrePlan);
				paramsIn.put("CodigoAgrupador", Fila.CodigoAgrupador);	
					
				//Salida HashMap
				HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
				paramsOut.put("p_out_RefId", Long.class);
				paramsOut.put("p_out_AdvertenciasContador", Long.class);
				paramsOut.put("p_out_ErroresContador", Long.class);
				paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
				paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);

				HashMap<String,Object> resultSP = sql.call(con, SQL_CREAR_PORTAFOLIO_APV_Call, paramsIn, paramsOut);
				
				HashMap<String,Object> portPost = new HashMap<String,Object>();
				portPost.put("IdCliente", in.p_Traspasos.get(0).IdCliente);	
				List<vistaInfoPort> portPostSelect = sql.query(con, SQL_VISTA_PORTAFOLIOS_CLIENTE, portPost, vistaInfoPort.class);
				
				//Validacion estado portafolios cliente
				for(vistaInfoPort portafolioPrev : portPrevioSelect) {	
					for(vistaInfoPort portafolioPost : portPostSelect) {
						if(portafolioPrev.IdPortafolio.compareTo(portafolioPost.IdPortafolio) == 0) {
							if(portafolioPrev.IdEstado.compareTo(portafolioPost.IdEstado) != 0) {
								//Caso en el que un portafolio ageno al proceso haya sido afectado
								modificarEstadoPort.ModificarEstadoPortafolioTraspaso(portafolioPrev.IdPortafolio, portafolioPrev.IdEstado, sql, con);
							}
						}
					}
				}
				
				if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
					IdPortafolioCreado = (Long)resultSP.get("p_out_RefId");
				}else{
					for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
						MensajeSalida msg1 = new MensajeSalida();
						msg1.id = IdDetalleTraspaso.get(contador);
						msg1.tipo = "ERROR";
						msg1.codigo = errorDetalle.Codigo;
						msg1.descripcion = errorDetalle.Descripcion;
						salida.P_OUT_MENSAJES.add(msg1);
					}
					for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){	
						MensajeSalida msg1 = new MensajeSalida();
						msg1.id = IdDetalleTraspaso.get(contador);
						msg1.tipo = "ADVERTENCIA";
						msg1.codigo = advertenciaDetalle.Codigo;
						msg1.descripcion = advertenciaDetalle.Descripcion;
						salida.P_OUT_MENSAJES.add(msg1);
					}
				}
				if(IdPortafolioCreado != 0)
				{
					MensajeSalida msg1 = new MensajeSalida();
					msg1.id = IdDetalleTraspaso.get(contador);
					msg1.tipo = "OK";
					msg1.codigo = "CREACION_PORTAFOLIO_OK";
					msg1.descripcion = "Creacion de portafolio exitosa N: " + IdPortafolioCreado.toString();
					salida.P_OUT_MENSAJES.add(msg1);
				}
			}
			if(IdDetalleTraspaso.get(contador) > 0 && IdPortafolioCreado > 0){
				
				if(!portPrevioSelect.isEmpty()) {
					for(vistaInfoPort portafolioPrev : portPrevioSelect) {
						if(IdPortafolioCreado.compareTo(portafolioPrev.IdPortafolio) == 0) {
							if(portafolioPrev.IdEstado.compareTo("INA") == 0) {
								modificarEstadoPort.ModificarEstadoPortafolioTraspaso(portafolioPrev.IdPortafolio, "ACT", sql, con);
							}
						}
					}
				}
				
				IdTraspaso = Fila.IdTraspaso;
				FechaIngresotraspaso = Fila.FechaIngreso;
				IdCliente = Fila.IdCliente;
				DetalleTraspaso dtTraspaso = new DetalleTraspaso();
				dtTraspaso.IdTraspaso = Fila.IdTraspaso;
				dtTraspaso.IdDetalleTraspaso = IdDetalleTraspaso.get(contador);
				dtTraspaso.IdCliente = Fila.IdCliente;
				dtTraspaso.IdPortafolio = IdPortafolioCreado;
				dtTraspaso.FechaIngreso = Fila.FechaIngreso;
				dtTraspaso.FechaModificacion = Fila.FechaModificacion;
				dtTraspaso.IdPlanTipoInversion = Fila.IdPlanTipoInversion;
				dtTraspaso.IdProductoAgrupador = Fila.IdProductoAgrupador;
				dtTraspaso.IdRegimenTributario = Fila.IdRegimenTributario;
				dtTraspaso.MontoPeso = Fila.MontoPeso;
				dtTraspaso.MontoUF = Fila.MontoUF;
				dtTraspaso.Porcentaje = Fila.Porcentaje;
				dtTraspaso.IdEstado = "VIG";
				dtTraspaso.EsReapertura = "N";
				lstDetalleTraspaso.add(dtTraspaso);	
				
				if(Fila.IdContratoSAAPV > 0){
					/*Una vez que se crea Portafolios, se asignan al Contrato SAAPV */
					String OrigenAhorro = ""; 
					if(Fila.RegimenTributario.equals("BF")){
						OrigenAhorro = "BF";
					} else{
						if(Fila.RegimenTributario.substring(0, 3).equals("APV")) OrigenAhorro = "APV";
						if(Fila.RegimenTributario.substring(0, 2).equals("DC"))OrigenAhorro = "DC";
					}
						
					PortafolioContratoSAAPV inPortCont = new PortafolioContratoSAAPV();
					inPortCont.IdContratoSAAPV = Fila.IdContratoSAAPV;
					inPortCont.IdPortafolio = IdPortafolioCreado;
					inPortCont.OrigenAhorro = OrigenAhorro;
					inPortCont.MontoPesos = Double.valueOf(Fila.MontoPeso.toString());
					inPortCont.MontoUF = Double.valueOf(Fila.MontoUF.toString());
					inPortCont.PorcentajeRegimenImponible = Double.valueOf(Fila.Porcentaje.toString());
					inPortCont.Pago = "UNI";
					inPortCont.PeriodoInicio = FechaPeriodo;
					inPortCont.PeriodoTermino = FechaPeriodo;
					inPortCont.IdEstado = "VIG";
					lstPortCont.add(inPortCont);
				}
			}
			contador++;
		}
		
		if(lstDetalleTraspaso.size() > 0 && salida.P_OUT_MENSAJES.stream().filter(mensaje -> "ERROR".equalsIgnoreCase(mensaje.tipo)).count() == 0){
			int[] result = sql.batch(con, SQL_CREAR_DETALLETRASPASO, lstDetalleTraspaso, DetalleTraspaso.class);
			contador--;
			if(result != null && result.length > 0 && result[0] == 1){
				MensajeSalida msg1 = new MensajeSalida();
				msg1.id = IdDetalleTraspaso.get(contador);
				msg1.tipo = "OK";
				msg1.codigo = "CREAR_DETALLE_TRASPASO_OK";
				msg1.descripcion = "Se crea detalle del traspaso correctamente.";
				salida.P_OUT_MENSAJES.add(msg1);
				
				int[] resultado = sql.batch(con, SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV, lstPortCont, PortafolioContratoSAAPV.class);
				
				if(resultado != null && resultado.length > 0 && resultado[0] == 1){
					MensajeSalida msg2 = new MensajeSalida();
					msg2.id = IdDetalleTraspaso.get(contador);
					msg2.tipo = "OK";
					msg2.codigo = "ASIGNA_PORTAFOLIO_CONTRATO_OK";
					msg2.descripcion = "Se asigna Portafolio al detalle traspaso correctamente.";
					salida.P_OUT_MENSAJES.add(msg2);
				}else{
					MensajeSalida msg2 = new MensajeSalida();
					msg2.id = IdDetalleTraspaso.get(contador);
					msg2.tipo = "ERROR";
					msg2.codigo = "ERROR_CREAR_PORTAFOLIO_CONTRATO";
					msg2.descripcion = "Problema al asignar portafolio al detalle traspaso.";
					salida.P_OUT_MENSAJES.add(msg2);
				}
				
				if(ApruebaTraspasoAutomatico.equals("N")) {
					//Modifico estado de traspaso a Pendiente de Autorizacion
					List<TraspasoModificar> lstTraspasoMod = new ArrayList<TraspasoModificar>();
					TraspasoModificar tm = new TraspasoModificar();
					tm.IdEstado = "PDA";
					tm.IdTraspaso = IdTraspaso;
					lstTraspasoMod.add(tm);
					
					int[] resultadoMod = sql.batch(con, SQL_MODIFICA_TRASPASO, lstTraspasoMod, TraspasoModificar.class);				
					if(resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1){
						MensajeSalida msg2 = new MensajeSalida();
						msg2.id = IdDetalleTraspaso.get(contador);
						msg2.tipo = "OK";
						msg2.codigo = "MODIFICA_ESTADO_OK";
						msg2.descripcion = "Modifica estado del Traspaso Folio: " + IdTraspaso.toString();
						salida.P_OUT_MENSAJES.add(msg2);
					}else{
						MensajeSalida msg2 = new MensajeSalida();
						msg2.id = IdDetalleTraspaso.get(contador);
						msg2.tipo = "ERROR";
						msg2.codigo = "ERROR_MODIFICA_ESTADO";
						msg2.descripcion = "Problema al modificar estado del Traspaso.";
						salida.P_OUT_MENSAJES.add(msg2);
					}					
					
					CrearEventoAutorizacionMasivo.ServiceEntrada inCrearAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
					List<ObjTABLAEVENTOS> tablaEventos = new ArrayList<ObjTABLAEVENTOS>();
					ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();
					evento.IdTipoEvento = "INGTRA";
					evento.IdModuloSolicita = "APV";
					evento.IdUsuarioSolicita = in.p_IdUsuario;
					evento.Fecha = FechaIngresotraspaso;
					evento.IdTipoOperacion = null;
					evento.IdConcepto = "INGTRA";
					evento.NumeroOperacionConcepto = IdTraspaso.toString();
					evento.FechaOperacionConcepto = FechaIngresotraspaso;
					evento.IdCliente = IdCliente;
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
					evento.IdMoneda = "CLP";
					evento.Glosa = "INGRESO DE TRASPASO APV Folio: " + IdTraspaso.toString();
					tablaEventos.add(evento);
					
					if (tablaEventos.size() > 0) {
						CrearEventoAutorizacionMasivo crearAutorizacionService = new CrearEventoAutorizacionMasivo();
						inCrearAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
						inCrearAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
						inCrearAutorizacion.P_IDMODULO = in.p_IdModulo;
						inCrearAutorizacion.P_SIMULAROPERACION = "N";
						inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";
						inCrearAutorizacion.P_TABLAEVENTOS = tablaEventos;
						CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = crearAutorizacionService.logica(con, inCrearAutorizacion);
						for(CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE ad : salidaAut.P_OUT_ADVERTENCIASDETALLE) {
							MensajeSalida msg2 = new MensajeSalida();
							msg2.id = IdDetalleTraspaso.get(contador);
							msg2.tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
							msg2.codigo = ad.Codigo;
							msg2.descripcion = ad.Descripcion;
							salida.P_OUT_MENSAJES.add(msg2);
						}
						for(CrearEventoAutorizacionMasivo.ObjERRORESDETALLE ad : salidaAut.P_OUT_ERRORESDETALLE) {
							MensajeSalida msg2 = new MensajeSalida();
							msg2.id = IdDetalleTraspaso.get(contador);
							msg2.tipo = MensajeSalida.MENSAJE_ERROR;
							msg2.codigo = ad.Codigo;
							msg2.descripcion = ad.Descripcion;
							salida.P_OUT_MENSAJES.add(msg2);
						}
					}
				}else {
					//Modifico estado de traspaso
					List<TraspasoModificar> lstTraspasoMod = new ArrayList<TraspasoModificar>();
					TraspasoModificar tm = new TraspasoModificar();
					tm.IdEstado = "ING";
					tm.IdTraspaso = IdTraspaso;
					lstTraspasoMod.add(tm);
					
					int[] resultadoMod = sql.batch(con, SQL_MODIFICA_TRASPASO, lstTraspasoMod, TraspasoModificar.class);				
					if(resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1){
						MensajeSalida msg2 = new MensajeSalida();
						msg2.id = IdDetalleTraspaso.get(contador);
						msg2.tipo = "OK";
						msg2.codigo = "MODIFICA_ESTADO_OK";
						msg2.descripcion = "Modifica estado del Traspaso Folio: " + IdTraspaso.toString();
						salida.P_OUT_MENSAJES.add(msg2);
					}else{
						MensajeSalida msg2 = new MensajeSalida();
						msg2.id = IdDetalleTraspaso.get(contador);
						msg2.tipo = "ERROR";
						msg2.codigo = "ERROR_MODIFICA_ESTADO";
						msg2.descripcion = "Problema al modificar estado del Traspaso.";
						salida.P_OUT_MENSAJES.add(msg2);
					}					
				}
			}else{
				MensajeSalida msg2 = new MensajeSalida();
				msg2.id = 1L;
				msg2.tipo = "ERROR";
				msg2.codigo = "ERROR_CREAR_DETALLE_TRASPASO";
				msg2.descripcion = "Problema al crear detalle del traspaso.";
				salida.P_OUT_MENSAJES.add(msg2);
			}	
		}
		return salida;
	}
	
	private List<EstadoCliente> obtenerEstadoCliente (Connection con, SQL sql, List<Long> listaIdCliente) throws SQLException{
		logger.debug("INICIO::GrabarTraspasos.obtenerEstadoCliente");
		long inicio = System.currentTimeMillis();
		
		List<EstadoCliente> listaEstadoCliente = new ArrayList<>();
		HashMap<String, Object> params = new HashMap<>();
		params.put("idCliente", listaIdCliente);
		listaEstadoCliente = sql.query(con, SQL_SELECT_ESTADO_CLIENTE, params,EstadoCliente.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GrabarTraspasos.obtenerEstadoCliente", fin - inicio);
		
		return listaEstadoCliente;
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
			try {
				ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
				GrabarTraspasos demo = new GrabarTraspasos();	
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"I\",\"p_Fecha\":\"2023-09-14\",\"p_Traspasos\":[{\"IdTraspaso\":2991747,\"IdContratoSAAPV\":1013908,\"FechaIngreso\":\"2023-09-14\",\"FechaModificacion\":\"0001-01-01\",\"IdCliente\":85053,\"IdPersonaCliente\":117943,\"DocumentoIdentidad\":\"19263099-1\",\"IdPerfilComercial\":\"APV INV ACTIVA_ARRIESGADO\",\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":2,\"RegimenTributario\":\"APV-A\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":200000,\"MontoUF\":0,\"Porcentaje\":0,\"MontoReapertura\":0},{\"IdTraspaso\":2991747,\"IdContratoSAAPV\":1013908,\"FechaIngreso\":\"2023-09-14\",\"FechaModificacion\":\"0001-01-01\",\"IdCliente\":85053,\"IdPersonaCliente\":117943,\"DocumentoIdentidad\":\"19263099-1\",\"IdPerfilComercial\":\"APV INV ACTIVA_ARRIESGADO\",\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":5,\"RegimenTributario\":\"DC-DL\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":235000,\"MontoUF\":0,\"Porcentaje\":0,\"MontoReapertura\":0}]}";
				ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
	 				//con.commit();
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}