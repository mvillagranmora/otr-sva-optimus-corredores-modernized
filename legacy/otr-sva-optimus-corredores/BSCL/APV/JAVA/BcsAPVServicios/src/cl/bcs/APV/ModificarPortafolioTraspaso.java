package cl.bcs.APV;

import java.math.BigDecimal;
import java.math.BigInteger;
//import java.math.BigDecimal; HMR
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.GrabarContratoSAAPV.PortafolioContratoSAAPV;
import cl.bcs.APV.GrabarTraspasos.DetalleTraspaso;
//import cl.bcs.APV.GrabarTraspasos.DetalleTraspasoModificar;
//import cl.bcs.APV.GrabarTraspasos.TabDetalleTraspaso;
import cl.bcs.APV.GrabarTraspasos.TraspasoModificar;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPortafolioTraspaso implements JavaCall   {
	/*==================================================================================================*/
	/* Queries que van a ser utilizadas por el servicio. */ 
	/*==================================================================================================*/
	public String SQL_CREAR_PORTAFOLIO_APV_Call 			= loadResourceToString("cl/bcs/APV/sql/Crear_Portafolio_APV_Call.sql"); //Para los nuevos portafolios
	public String SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV	 	= loadResourceToString("cl/bcs/APV/sql/Crear_PortafolioContratoSAAPV.sql"); // crea relacion entre portafolio con contrato SAAPV
	public String SQL_ELIMINAR_PORTAFOLIO_CONTRATO 			= loadResourceToString("cl/bcs/APV/sql/PortafolioContratoSAAPV_Eliminar.sql");
	public String SQL_MODIFICAR_ESTADO_PORTAFOLIO_TRASPASO_CALL = loadResourceToString("cl/bcs/APV/sql/ModificarEstadoPortafolioTraspaso_Call.sql");
	public String SQL_INHABILITAR_ESTADO_PORTAFOLIO 		= loadResourceToString("cl/bcs/APV/sql/InhabilitarPortafolioTraspaso.sql");
	/*TRASPASOS*/
	public String SQL_CONSULTAR_DETALLE_TRASPASO_A_ELIMINAR = loadResourceToString("cl/bcs/APV/sql/ConsultarPortafolioDetalleTraspaso.sql");
	public String SQL_CONSULTAR_MATRIZ_AGRUPADOR_APV 		= loadResourceToString( "cl/bcs/APV/sql/ModificarPortafolioTraspaso_SelectMatrizAgrupadorApv.sql");
	public String SQL_CONSULTAR_DETALLE_TRASPASO 			= loadResourceToString("cl/bcs/APV/sql/ConsultarDetalleTraspasoIngreso.sql");
	public String SQL_MODIFICAR_DETALLE_TRASPASO_PORTAFOLIO = loadResourceToString("cl/bcs/APV/sql/ModificarPortafolioTraspasoModificado.sql");
	public String SQL_MODIFICAR_DETALLE_TRASPASO 			= loadResourceToString("cl/bcs/APV/sql/ModificarDetalleTraspaso.sql");
	public String SQL_CONSULTAR_CLIENTE_CONTRATO 			= loadResourceToString( "cl/bcs/APV/sql/ModificarPortafolioTraspaso_SelectClienteContrato.sql");
	public String SQL_MODIFICAR_DETALLE_TRASPASO_CONTRATOSAAPV = loadResourceToString("cl/bcs/APV/sql/ModificarMontoContratoSAAPV.sql");
	public String SQL_INACTIVAR_DETALLE_TRASPASO_REPETIDO 	= loadResourceToString("cl/bcs/APV/sql/ModificarPortafolioTraspaso_UpdateInactivarDetalleTraspRepetido.sql");
	public String SQL_MODIFICA_TRASPASO 					= loadResourceToString("cl/bcs/APV/sql/Traspaso_Update.sql");
	public String SQL_ELIMINAR_DETALLE_TRASPASO				= loadResourceToString("cl/bcs/APV/sql/DetalleTraspaso_Delete.sql"); // Elimina detalle traspaso	
	public String SQL_CREAR_DETALLE_TRASPASO 				= loadResourceToString("cl/bcs/APV/sql/DetalleTraspaso_Insert.sql"); // Inserta detalle traspaso
	public String SQL_CANTIDAD_PORTAFOLIO_CONTRATOSAAPV 	= loadResourceToString("cl/bcs/APV/sql/ConsultarIdPortafolioContratoSAAPV.sql");
	public String SQL_CONSULTAR_MATRIZ_AGRUPADOR_APV_PORTAFOLIO_NUEVO = loadResourceToString("cl/bcs/APV/sql/ConsultarMatrizAgrupadorAPVPortafolio.sql");
	public String SQL_DATOS_PORTAFOLIO_CREADO 				= loadResourceToString( "cl/bcs/APV/sql/ModificarPortafolioTraspaso_SelectPortafoliosCreados.sql");
	public String SQL_VISTA_PORTAFOLIOS_CLIENTE 			= loadResourceToString( "cl/bcs/APV/sql/ModificarPortafolioTraspaso_SelectPortafoliosClientes.sql");
	public String SQL_VISTA_PORTAFOLIOS_CLIENTE_MATRIZ_APV 	= loadResourceToString( "cl/bcs/APV/sql/ModificarPortafolioTraspaso_SelectPlanInversion.sql");
	
	
	/********************/
	/* Clases IN/OUT	*/
	/********************/	
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
		public String		EsReapertura;
		
		public Traspaso () {
			this.IdTraspaso = 0L;
			this.IdContratoSAAPV = 0L;
			this.FolioContratoSAAPV = "";
			this.FechaIngreso = "";
			this.FechaModificacion = "";
			this.IdPersonaCliente = 0L;
			this.IdCliente = 0L;
			this.DocumentoIdentidad = "";
			this.IdPerfilComercial = "";
			this.IdDetalleTraspaso = 0L;
			this.IdPortafolio = 0L;
			this.IdPropositoPortafolio = "";
			this.IdPlanTipoInversion = 0L;
			this.NombrePlan = "";
			this.IdProductoAgrupador = 0L;
			this.ProductoAgrupador = "";
			this.IdRegimenTributario = 0L;
			this.RegimenTributario = "";
			this.CodigoAgrupador = "";
			this.MontoPeso = BigDecimal.ZERO;
			this.MontoUF = BigDecimal.ZERO;
			this.Porcentaje = BigDecimal.ZERO;
			this.EsReapertura = "N";
		}
	}
	
	public static class DetalleTraspaso{
		public Long 	IdTraspaso;
		public Long		IdDetalleTraspaso;
		public Long 	IdPortafolio;
		public String 	FechaIngreso;
		public String 	FechaModificacion;
		public Long 	IdPlanTipoInversion;
		public Long 	IdProductoAgrupador;
		public Long 	IdRegimenTributario;
		public double 	MontoPeso;
		public double 	MontoUF;
		public double 	Porcentaje;
		public String	IdEstado;
		public String	EsReapertura;
		public Long		IdMatrizAgrupadorAPV;
	}
	
	public static class TabDetalleTraspaso{
		public Long 		IdTraspaso;
		public Long			IdDetalleTraspaso;
		//public Long 		IdCliente;
		public Long 		IdPortafolio;
		public Date 		FechaIngreso;
		public Date 		FechaModificacion;
		public Long 		IdPlanTipoInversion;
		public Long 		IdProductoAgrupador;
		public Long 		IdRegimenTributario;
		public BigDecimal 	MontoPeso;
		public BigDecimal 	MontoUF;
		public BigDecimal 	Porcentaje;
		public String		IdEstado;
		public String		EsReapertura;
	}
	
	public static class DetalleTraspasoModificar{
		public Long			IdDetalleTraspaso;
		public Long 		IdPortafolio;
		public Long 		IdTraspaso;
		public Long 		IdProductoAgrupador;
		public Long 		IdRegimenTributario;
		public String		FechaIngreso;
		public BigDecimal 	MontoPeso;
		public BigDecimal 	MontoUF;
	}
	
	public static class DetalleTraspasoElimino{
		public Long 	IdDetalleTraspaso;
		//public Long     IdTraspaso;
		public Long		IdContratoSAAPV;
	}
	
	public static class PortafolioContratoElimino{
		public Long 	IdContratoSAAPV;
		public Long 	IdPortafolio;
	}
	
	public static class DetalleTraspasoAEliminar{
		public Long 	IdDetalleTraspaso;
		public Long		IdPortafolio;
		public Long		IdContratoSAAPV;
		public String	FolioContratoSAAPV;
		public String	CodigoPortafolio;
		public Long		IdPlanTipoInversion;
		public Long		IdRegimenTributario;
		public Long		IdProductoAgrupador;
		public BigDecimal	MontoPeso;
		public BigDecimal	MontoUF;
		
		public DetalleTraspasoAEliminar(){
			this.CodigoPortafolio = "";
		}
	}
	
	public static class PortafoliosModificados {
		public String 	CodigoAgrupador;
		public Long 	IdMatrizAgrupador;
		public String 	OrigenAhorro;
		public Long 	IdPortafolio;
		public String	IdPropositoPortafolio;
		public String 	MesInicio;
		public String 	MesTermino;
		public double 	MontoPesos;
		public double 	MontoUF;
		public String	Pago;
		public String	PagoNombre;
		public double 	Porcentaje;
		public String 	Producto;
		public String 	Regimen;
	}
	
	public static class PortafolioModificar {
		public String 	p_CodigoPortafolio;
		public Long 	p_IdMatrizAgrupadorAPV;
		public Long		p_IdPortafolio;
		
	}
	
	public static class PortafolioContratoSAAPVModificar {
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;
		public Long IdPortafolioNuevo;
		public Long IdPortafolio;
		public Long IdContratoSAAPV;
	}
	
	public static class MatrizAgrupadorAPVTraspaso {
		public Long IdMatrizAgrupadorAPV;
	}
	
	public static class DetalleTraspasoInactivo {
		public Long IdDetalleTraspaso;
		public String IdEstado;
	}
	
	public static class PortafolioEliminar {
		public Long IdPortafolio;
	}
	
	public static class ClienteContratoSAAPV {
		public Long IdCliente;
		public String IdPerfilComercial;
	}
	
	public static class CantidadPortafolios {
		public Long ContPort;
		public CantidadPortafolios() {
			this.ContPort = 0L;
		}
	}
	
	public static class EstadoPortafolio{
		public Long IdPortafolio;
		public String IdEstado;
		public String IdEstadoAutorizacion;
	}
	
	public static class infoPort{
		public Long IdPortafolio;
		public Long IdCliente;
		public String IdEstado;
		public String IdEstadoAutorizacion;
	}
	
	public static class vistaInfoPort{
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String Proposito;
		public Long IdCliente;
		public String IdEstado;
		
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_Tipo;
		public List<Traspaso>				p_Traspasos;
		public List<PortafoliosModificados> p_PortafoliosModificados;
		
		public ParametrosEntradaServicio(){
			p_Traspasos = new ArrayList<Traspaso>();	
			p_PortafoliosModificados = new ArrayList<PortafoliosModificados>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class validaPlanApv{
				
		public Long IdPortafolio;
		public String CodigoPortafolio; 
		public String Proposito; 
		public Long IdCliente; 
		public String IdEstado;
		public Long IdMatrizAgrupadorAPV;
		public Integer IdPlanTipoInversion;
		
	}
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_IDRESULTADO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_IDRESULTADO = new ArrayList<Long>();
		}		
	};
	
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
	 * Metodos Basicos	*	
	 ************************/
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();
		SalidaServicioCrearCliente  SalidaCliente   = new SalidaServicioCrearCliente();
		SQL sql = new SQL();
		Long IdPortafolioCreado = 0L;
		Long IdTraspaso = 0L;
		String estadoPortafolio;
		List<DetalleTraspaso> lstDetalleTraspaso = new ArrayList<DetalleTraspaso>();
		
		Long IdPortafolioMatriz = 0L;
		java.sql.Date FechaIngreso = new java.sql.Date(System.currentTimeMillis());	
		DateFormat PeriodoFecha = new SimpleDateFormat("yyyy-MM");			
		String FechaPeriodo = PeriodoFecha.format(FechaIngreso);
			
		//Detalles Traspaso eliminados
		HashMap<String,Object> paramsInEl = new HashMap<String,Object>();
		paramsInEl.put("IdTraspaso", in.p_Traspasos.get(0).IdTraspaso);
		paramsInEl.put("IdContratoSAAPV", in.p_Traspasos.get(0).IdContratoSAAPV);
		List<DetalleTraspasoAEliminar> detalleTraspasoElim = sql.query(con, SQL_CONSULTAR_DETALLE_TRASPASO_A_ELIMINAR, paramsInEl, DetalleTraspasoAEliminar.class);
		
 		if(!detalleTraspasoElim.isEmpty() && salida.P_OUT_ERRORESCONTADOR == 0) {
			for(DetalleTraspasoAEliminar detTraspaso : detalleTraspasoElim){
				List<DetalleTraspasoElimino> lstDetalleTraspasoElimino = new ArrayList<DetalleTraspasoElimino>();
				boolean eliminarTraspaso = true;
				for(Traspaso fila : in.p_Traspasos) {
					if(fila.IdDetalleTraspaso == null || fila.IdPortafolio == null) continue;
					if(detTraspaso.IdDetalleTraspaso.compareTo(fila.IdDetalleTraspaso) == 0 && detTraspaso.IdPortafolio.compareTo(fila.IdPortafolio) == 0 || fila.EsReapertura.compareTo("S") == 0) {
						eliminarTraspaso = false;
						//continue;
					}
				}
				if(eliminarTraspaso == true) {
					//Elimino Detalle Traspaso	
					int contTraspaso = 0;
					for(Traspaso filaDT : in.p_Traspasos) {	
						if(filaDT.IdDetalleTraspaso.compareTo(detTraspaso.IdDetalleTraspaso) == 0) {
							if(filaDT.IdPlanTipoInversion.compareTo(detTraspaso.IdPlanTipoInversion) != 0
									|| filaDT.IdProductoAgrupador.compareTo(detTraspaso.IdProductoAgrupador) != 0
									|| filaDT.IdRegimenTributario.compareTo(detTraspaso.IdRegimenTributario) != 0
									|| filaDT.MontoPeso.compareTo(detTraspaso.MontoPeso) != 0
									|| filaDT.MontoUF.compareTo(detTraspaso.MontoUF) != 0) {
								in.p_Traspasos.get(contTraspaso).IdDetalleTraspaso = 0L;
								in.p_Traspasos.get(contTraspaso).IdPortafolio = 0L;
							}
						}
						contTraspaso++;
					}				
			 		DetalleTraspasoElimino detalleTraspasoElimino = new DetalleTraspasoElimino();
			 		detalleTraspasoElimino.IdDetalleTraspaso = detTraspaso.IdDetalleTraspaso;
			 		detalleTraspasoElimino.IdContratoSAAPV = in.p_Traspasos.get(0).IdContratoSAAPV;
			 		
			 		lstDetalleTraspasoElimino.add(detalleTraspasoElimino);
			 		int[] deleteDetTraspaso = sql.batch(con, SQL_ELIMINAR_DETALLE_TRASPASO, lstDetalleTraspasoElimino, DetalleTraspasoElimino.class);
		 			if(deleteDetTraspaso != null && deleteDetTraspaso.length > 0 && deleteDetTraspaso[0] == 1){		
		 				
		 				//Elimino Contrato Portafolio Detalle Traspaso
			 			List<PortafolioContratoElimino> lstPortafolioContratoElimino = new ArrayList<PortafolioContratoElimino>();
			 			PortafolioContratoElimino porPortafolioContratoElimino = new PortafolioContratoElimino();
			 			porPortafolioContratoElimino.IdPortafolio = detTraspaso.IdPortafolio;
			 			porPortafolioContratoElimino.IdContratoSAAPV = detTraspaso.IdContratoSAAPV;
			 			lstPortafolioContratoElimino.add(porPortafolioContratoElimino);
				 		int[] deletePortafolioContrato = sql.batch(con, SQL_ELIMINAR_PORTAFOLIO_CONTRATO, lstPortafolioContratoElimino, PortafolioContratoElimino.class);

				 		if(deletePortafolioContrato != null && deletePortafolioContrato.length > 0 && deletePortafolioContrato[0] == 1){
				 			//Se verifica si el portafolio debe ser eliminado
							int deletePortafolio = EliminarPortafolioTraspaso(detTraspaso.IdPortafolio, detTraspaso.IdContratoSAAPV, sql, con);
							
							if(deletePortafolio < 2){
			 				//if(deletePortafolio == true){
								HashMap<String,String> valores = new HashMap<>();
								String codMsg = "";
								if(deletePortafolio == 0) {
									codMsg = "MOFIFICACION_DETALLE_CONTRATO";
									valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio != null ? detTraspaso.CodigoPortafolio : "");
									valores.put("FolioContrato", detTraspaso.FolioContratoSAAPV != null ? detTraspaso.FolioContratoSAAPV : "");
								}
								if(deletePortafolio == 1) {
									codMsg = "DESACTIVAR_PORTAFOLIO";
									valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio != null ? detTraspaso.CodigoPortafolio : "");
								}
								msg = new MensajeSalida(detTraspaso.IdPortafolio, MensajeSalida.MENSAJE_INFORMACION, codMsg, valores, con, in.p_IdEmpresa);
			 					salida.P_OUT_MENSAJES.add(msg);
			 				}else{
			 					HashMap<String,String> valores = new HashMap<>();
			 					valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio != null ? detTraspaso.CodigoPortafolio : "");
								valores.put("FolioContrato", detTraspaso.FolioContratoSAAPV != null ? detTraspaso.FolioContratoSAAPV : "");
								msg = new MensajeSalida(detTraspaso.IdPortafolio, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_MOFIFICAR_DETALLE_CONTRATO", valores, con, in.p_IdEmpresa);
		 					   	salida.P_OUT_MENSAJES.add(msg);
		 					   	salida.P_OUT_ERRORESCONTADOR++;
		 				   }
			 						 			
			 			}else {
			 				HashMap<String,String> valores = new HashMap<>();
		 					valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio != null ? detTraspaso.CodigoPortafolio : "");
							valores.put("FolioContrato", detTraspaso.FolioContratoSAAPV != null ? detTraspaso.FolioContratoSAAPV : "");
							msg = new MensajeSalida(detTraspaso.IdPortafolio, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_MOFIFICAR_DETALLE_CONTRATO", valores, con, in.p_IdEmpresa);
							salida.P_OUT_MENSAJES.add(msg);
							salida.P_OUT_ERRORESCONTADOR++;
			 			}
					}else{
						HashMap<String,String> valores = new HashMap<>();
	 					valores.put("CodigoPortafolio", detTraspaso.CodigoPortafolio != null ? detTraspaso.CodigoPortafolio : "");
						valores.put("FolioContrato", detTraspaso.FolioContratoSAAPV != null ? detTraspaso.FolioContratoSAAPV : "");
						msg = new MensajeSalida(detTraspaso.IdPortafolio, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_MOFIFICAR_DETALLE_CONTRATO", valores, con, in.p_IdEmpresa);
						salida.P_OUT_MENSAJES.add(msg);
						salida.P_OUT_ERRORESCONTADOR++;
					}	 			
				}
			}
			 
		}
		
 		if(!in.p_Traspasos.isEmpty() && salida.P_OUT_ERRORESCONTADOR == 0) {
			for(Traspaso fila : in.p_Traspasos){
				//*** VALIDACION CONTRATOS SAAPV ***
				ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada inValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada();
				ValidaContratoSAAPV.ValidaContratoSAAPV_Salida outValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Salida();
				List<ValidaContratoSAAPV.ContratosSAAPV> tablaValidaContrato = new ArrayList<ValidaContratoSAAPV.ContratosSAAPV>();
				IdTraspaso = fila.IdTraspaso;
				IdPortafolioCreado = 0L;
				boolean crearPortafolio = false;
				boolean portafolioEncontrado = false;
				
				//Se envia solo IdPortafolio NULL para que SOLO valide el portafolio
				ValidaContratoSAAPV.ContratosSAAPV ContratoValidaContrato = new ValidaContratoSAAPV.ContratosSAAPV();
				ContratoValidaContrato.IdPersona = fila.IdPersonaCliente;
				ContratoValidaContrato.IdCliente = null;
				ContratoValidaContrato.IdEmpleador = null;
				ContratoValidaContrato.IdPortafolio = null;
				ContratoValidaContrato.DocumentoIdentidad = fila.DocumentoIdentidad;
				ContratoValidaContrato.DocumentoIdentidadEmpleador = "";
				ContratoValidaContrato.FolioContratoSAAPV = null;
				ContratoValidaContrato.Codigo = fila.RegimenTributario;
				ContratoValidaContrato.NombrePlan = fila.NombrePlan;
				ContratoValidaContrato.CodigoAgrupador = fila.CodigoAgrupador;
				tablaValidaContrato.add(ContratoValidaContrato);	
	
				ValidaContratoSAAPV ValidaContratoSAAPVService = new ValidaContratoSAAPV();
				inValidaContrato.p_IdEmpresa = in.p_IdEmpresa;
				inValidaContrato.p_IdUsuario = in.p_IdUsuario;
				inValidaContrato.p_IdModulo = in.p_IdModulo;
				inValidaContrato.p_Opcion = "M";
				inValidaContrato.p_ContratosSAAPV = tablaValidaContrato;
				outValidaContrato = ValidaContratoSAAPVService.logica(con, inValidaContrato);
				
				if(outValidaContrato.P_OUT_ERRORESCONTADOR == null){// && outValidaContrato.P_OUT_ERRORESCONTADOR == 0 ){
	
					for (ValidaContratoSAAPV.ModificaContratoSAAPV filaValidaContrato : outValidaContrato.P_IDSCONTRATO) {
						if(filaValidaContrato.IdPersona != null)
							fila.IdPersonaCliente = filaValidaContrato.IdPersona;
						
						if(filaValidaContrato.IdCliente != null)
							fila.IdCliente = filaValidaContrato.IdCliente;
						
						if(filaValidaContrato.Codigo != null){
							IdPortafolioCreado = filaValidaContrato.IdPortafolio;
						}	
					}
					System.out.println("ValidaContratoSAAPV OK");
				}
				
				// Verificacion si el detalle traspaso existe, no es una reapertura y si ha sido modificado
				if(fila.IdTraspaso > 0 && fila.IdPortafolio > 0 && fila.IdDetalleTraspaso > 0 && fila.IdDetalleTraspaso != null && fila.EsReapertura.compareTo("N") == 0) {
					HashMap<String,Object> paramsInt = new HashMap<String,Object>();
					paramsInt.put("IdTraspaso", fila.IdTraspaso);
					paramsInt.put("IdPortafolio", fila.IdPortafolio);
					paramsInt.put("IdDetalleTraspaso", fila.IdDetalleTraspaso);
					
					List<TabDetalleTraspaso> detalleTraspaso = sql.query(con, SQL_CONSULTAR_DETALLE_TRASPASO, paramsInt, TabDetalleTraspaso.class);
					
					if(fila.IdCliente.compareTo(0L) == 0) {
						HashMap<String,Object> paramsInCli = new HashMap<String,Object>();
						paramsInCli.put("IdContratoSAAPV", fila.IdContratoSAAPV);	
						List<ClienteContratoSAAPV> clienteContrato = sql.query(con, SQL_CONSULTAR_CLIENTE_CONTRATO, paramsInCli, ClienteContratoSAAPV.class);
						fila.IdCliente = clienteContrato.get(0).IdCliente;
						fila.IdPerfilComercial = clienteContrato.get(0).IdPerfilComercial;
					}
					
					//Cambio de estado a Inactivo para detalles traspasos antiguos
					int contDetalleTraspaso = 0;
					for(TabDetalleTraspaso dTrasp : detalleTraspaso){
						if(dTrasp.IdDetalleTraspaso.compareTo(fila.IdDetalleTraspaso) == 0 && dTrasp.IdEstado.equals("VIG")) contDetalleTraspaso++;
					}
					if(contDetalleTraspaso >= 2) {
						List<DetalleTraspasoInactivo> listDetalleTraspasoInactivo = new ArrayList <DetalleTraspasoInactivo>();
						DetalleTraspasoInactivo detalleTraspasoInactivo = new DetalleTraspasoInactivo();
						detalleTraspasoInactivo.IdDetalleTraspaso = fila.IdDetalleTraspaso;
						detalleTraspasoInactivo.IdEstado = "INA";
						listDetalleTraspasoInactivo.add(detalleTraspasoInactivo);
						int[] inaDT = sql.batch(con, SQL_INACTIVAR_DETALLE_TRASPASO_REPETIDO, listDetalleTraspasoInactivo, DetalleTraspasoInactivo.class);
						
						//Ingreso detalle traspaso vigente
						IngresarDetalleTraspaso (fila, IdPortafolioCreado, sql, con);
						continue;
					}
		
					if(detalleTraspaso.get(0).IdProductoAgrupador.compareTo(fila.IdProductoAgrupador) != 0 || detalleTraspaso.get(0).IdRegimenTributario.compareTo(fila.IdRegimenTributario) != 0 ){
						
						//Se elimina relacion portafolio-contrato
						List<PortafolioContratoElimino> lstPortafolioContratoElimino = new ArrayList<PortafolioContratoElimino>();
			 			PortafolioContratoElimino porPortafolioContratoElimino = new PortafolioContratoElimino();
			 			porPortafolioContratoElimino.IdPortafolio = detalleTraspaso.get(0).IdPortafolio;
			 			porPortafolioContratoElimino.IdContratoSAAPV = fila.IdContratoSAAPV;
			 			lstPortafolioContratoElimino.add(porPortafolioContratoElimino);
				 		int[] deletePortafolioContrato = sql.batch(con, SQL_ELIMINAR_PORTAFOLIO_CONTRATO, lstPortafolioContratoElimino, PortafolioContratoElimino.class);
			 			
						
						//Se verifica si matrizAPV ingresada tiene portafolio para el cliente
						List<EstadoPortafolio> matrizAgrupadorTAPVPort = matrizAgrupadorTraspasoPortafolio (fila,sql,con);
											
						if(matrizAgrupadorTAPVPort != null && matrizAgrupadorTAPVPort.size() > 0) {
							//Se asigna a portafolio preexistente con misma matrizAPV
							boolean resultadoEstado = true;
							if(matrizAgrupadorTAPVPort.get(0).IdEstado.compareTo("INA") == 0) {
								resultadoEstado = ModificarEstadoPortafolioTraspaso (matrizAgrupadorTAPVPort.get(0).IdPortafolio, "ACT", sql, con);
							}
							IdPortafolioMatriz = matrizAgrupadorTAPVPort.get(0).IdPortafolio;
							
							//Se crea nueva relacion entre portafolio encontrado y contrato
							int[] result = RelacionarPortafolioContratoSAAPV(fila, IdPortafolioMatriz, FechaPeriodo, sql, con);
	
							if(result.length > 0 && resultadoEstado == true) {	
								//Se verifica si el portafolio antiguo debe ser eliminado
								int deletePortafolio = EliminarPortafolioTraspaso(detalleTraspaso.get(0).IdPortafolio, fila.IdContratoSAAPV, sql, con);		
				 				if(deletePortafolio == 0 || deletePortafolio == 1)	{
				 					msg = new MensajeSalida();
				 					msg.id = detalleTraspaso.get(0).IdPortafolio;
				 					msg.tipo = "OK";
				 					msg.codigo = "ELIMINO_PORTAFOLIO_CONTRATO_OK";
				 					if(deletePortafolio == 0) msg.descripcion = "Se ha Modificado el contrato folio N: " + fila.IdContratoSAAPV.toString();
				 					if(deletePortafolio == 1) msg.descripcion = "Se ha Desactivado el portafolio N: " + detalleTraspaso.get(0).IdPortafolio.toString();
				 					salida.P_OUT_MENSAJES.add(msg);
			 				   }else{
			 					   	msg = new MensajeSalida();
			 					   	msg.id = 6L;
			 					   	msg.tipo = "ERROR";
			 					   	msg.codigo = "ERROR_ELIMINO_PORTAFOLIO_CONTRATO";
			 					   	msg.descripcion = "Problema al eliminar el Portafolio Contrato N: " + detalleTraspaso.get(0).IdPortafolio.toString();
			 					   	salida.P_OUT_MENSAJES.add(msg);
			 					   	salida.P_OUT_ERRORESCONTADOR++;
			 				   }
			 				}
				 				
							//Update detalle traspaso
							int[] modDT = ActualizarDetalleTraspaso (fila, IdPortafolioMatriz, FechaPeriodo, sql, con);							
							if(modDT.length > 0){						
								System.out.println("Detalle Traspaso modificado");	
								msg = new MensajeSalida();
								msg.id = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
								msg.tipo = "OK";
								msg.codigo = "MODIFICAR_PORTAFOLIO_CONTRATO_OK";
								msg.descripcion = "Se ha modificado el portafolio N: " + matrizAgrupadorTAPVPort.get(0).IdPortafolio.toString() + " y el contrato traspaso N: " + fila.IdContratoSAAPV.toString();
								salida.P_OUT_MENSAJES.add(msg);
								System.out.println("Contrato modificado");
								continue;
							}else{
								msg = new MensajeSalida();
								msg.id = 6L;
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_ASIGNAR_PORTAFOLIO_CONTRATO";
								msg.descripcion = "Problema al asignar relacion entre portafolio y contrato traspaso.";
								salida.P_OUT_MENSAJES.add(msg);
								salida.P_OUT_ERRORESCONTADOR++;
								continue;
							}
	
						}
						//Caso en que no exista un portafolio con la matriz agrupadorAPV nueva
						else {
							
			 				//Se crea nuevo portafolio
			 				HashMap<String,Object> resultSP = CrearPortafolioAPV(in, fila, sql, con);						
							if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
								SalidaCliente.P_OUT_REFIDPORTAFOLIO = (Long)resultSP.get("p_out_RefId");
							}else{
								for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
									SalidaCliente.addError(errorDetalle.Codigo, errorDetalle.Descripcion);
								}
								for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
									SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
								}
							}
			 					
							//Se crea nueva relacion entre portafolio nuevo y contrato
							int[] relCon = RelacionarPortafolioContratoSAAPV(fila, SalidaCliente.P_OUT_REFIDPORTAFOLIO, FechaPeriodo, sql, con);					
							
							HashMap<String,Object> param = new HashMap<String,Object>();
							param.put("IdPortafolio", SalidaCliente.P_OUT_REFIDPORTAFOLIO);
							List<infoPort> infoPor = sql.query(con, SQL_CONSULTAR_DETALLE_TRASPASO, paramsInt, infoPort.class);
							
							
							//verificar si el portafolio antiguo tiene otros traspasos asignados
							int deletePortafolio = EliminarPortafolioTraspaso(detalleTraspaso.get(0).IdPortafolio, fila.IdContratoSAAPV, sql, con); 
							
							if(relCon != null && relCon.length > 0) {						
								//Update detalle traspaso
								int[] modDT = ActualizarDetalleTraspaso (fila, SalidaCliente.P_OUT_REFIDPORTAFOLIO, FechaPeriodo, sql, con);
								
								if(modDT != null && modDT.length > 0) {
									System.out.println("Detalle Traspaso modificado");	
									msg = new MensajeSalida();
									msg.id = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
									msg.tipo = "OK";
									msg.codigo = "MODIFICAR_PORTAFOLIO_CONTRATO_OK";
									if(deletePortafolio == 0) msg.descripcion = "Se ha Modificado el contrato folio N: " + fila.IdContratoSAAPV.toString();
				 					if(deletePortafolio == 1) msg.descripcion = "Se ha Modificado el contrato folio N: " + fila.IdContratoSAAPV.toString() + " y se ha Desactivado el portafolio N: " + detalleTraspaso.get(0).IdPortafolio.toString();		
									//msg.descripcion = "Se ha modificado el portafolio N: " + SalidaCliente.P_OUT_REFIDPORTAFOLIO + " y el contrato traspaso N: " + SalidaCliente.P_OUT_REFIDPORTAFOLIO;
									salida.P_OUT_MENSAJES.add(msg);
									System.out.println("Contrato modificado");
									continue;
								}else {
									msg = new MensajeSalida();
									msg.id = 6L;
									msg.tipo = "ERROR";
									msg.codigo = "ERROR_MODIFICAR_DETALLE_TRASPASO";
									msg.descripcion = "Problema al modificar el portafolio.";
									salida.P_OUT_MENSAJES.add(msg);
									salida.P_OUT_ERRORESCONTADOR++;
									continue;
								}
							}else {
								msg = new MensajeSalida();
								msg.id = 6L;
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_ASIGNAR_PORTAFOLIO_CONTRATO";
								msg.descripcion = "Problema al asignar relacion entre portafolio y contrato traspaso.";
								salida.P_OUT_MENSAJES.add(msg);
								salida.P_OUT_ERRORESCONTADOR++;
								continue;
							}
						}
						
					}else {
						
						if(detalleTraspaso.get(0).MontoPeso.compareTo(fila.MontoPeso) != 0 || detalleTraspaso.get(0).MontoUF.compareTo(fila.MontoUF) != 0) {
							//Solo se modifican los montos del traspaso
							int[] actDT = ActualizarDetalleTraspaso (fila, fila.IdPortafolio, FechaPeriodo, sql, con);
							if(actDT.length > 0 && actDT != null) {
								List<PortafolioContratoSAAPVModificar> listModPortContrato = new ArrayList <PortafolioContratoSAAPVModificar>();
								PortafolioContratoSAAPVModificar portContratoMod = new PortafolioContratoSAAPVModificar();
								portContratoMod.IdContratoSAAPV = fila.IdContratoSAAPV;
								portContratoMod.IdPortafolio = fila.IdPortafolio;
								portContratoMod.IdPortafolioNuevo = fila.IdPortafolio;
								portContratoMod.MontoPesos = fila.MontoPeso;
								portContratoMod.MontoUF = fila.MontoUF;
								listModPortContrato.add(portContratoMod);
								
								int[] modTC = sql.batch(con, SQL_MODIFICAR_DETALLE_TRASPASO_CONTRATOSAAPV, listModPortContrato, PortafolioContratoSAAPVModificar.class);
								if(modTC.length > 0 && modTC != null){
									System.out.println("Detalle Traspaso modificado");	
									msg = new MensajeSalida();
									msg.id = fila.IdPortafolio;
									msg.tipo = "OK";
									msg.codigo = "MODIFICAR_PORTAFOLIO_CONTRATO_OK";
									msg.descripcion = "Se ha modificado el portafolio N: " + fila.IdPortafolio.toString() + " y el contrato traspaso N: " + fila.IdContratoSAAPV.toString();
									salida.P_OUT_MENSAJES.add(msg);
									System.out.println("Contrato modificado");
									continue;
								}else {
									msg = new MensajeSalida();
									msg.id = 6L;
									msg.tipo = "ERROR";
									msg.codigo = "ERROR_MODIFICAR_DETALLE_TRASPASO";
									msg.descripcion = "Problema al modificar el portafolio.";
									salida.P_OUT_MENSAJES.add(msg);
									salida.P_OUT_ERRORESCONTADOR++;
									continue;
								}
							}
						}
						else {
							crearPortafolio = true;
							System.out.println("Detalle Traspaso no ha sido modificado");
							continue;
						} 
					}
				
				}else {
					//Se agrega Detalle Traspaso nuevo o reapertura ingresada
					System.out.println("Se agrega nuevo Detalle Traspaso");
					
					//Verificar portafolio preexistente para la matrizAgrupadorAPV a ingresar	
					List<EstadoPortafolio> matrizAgrupadorTAPVPort = matrizAgrupadorTraspasoPortafolio (fila,sql,con);
									
					if(matrizAgrupadorTAPVPort != null && matrizAgrupadorTAPVPort.size() > 0) {	
						//Se asigna a portafolio preexistente con misma matrizAPV
						boolean modEstado = true;
						if(matrizAgrupadorTAPVPort.get(0).IdEstado.compareTo("INA") == 0) {
							modEstado = ModificarEstadoPortafolioTraspaso (matrizAgrupadorTAPVPort.get(0).IdPortafolio, "ACT", sql, con);
						}
						
						int[] result = RelacionarPortafolioContratoSAAPV(fila, matrizAgrupadorTAPVPort.get(0).IdPortafolio, FechaPeriodo, sql, con);
						
						if(result != null && result.length > 0 && result[0] == 1 && modEstado == true){
							portafolioEncontrado = true;
							msg = new MensajeSalida();
							msg.id = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
							msg.tipo = "OK";
							msg.codigo = "ASIGNAR_PORTAFOLIO_CONTRATO_OK";
							msg.descripcion = "Se crea relacion entre portafolio y contrato traspaso.";
							salida.P_OUT_MENSAJES.add(msg);
						}else{
							msg = new MensajeSalida();
							msg.id = 6L;
							msg.tipo = "ERROR";
							msg.codigo = "ERROR_ASIGNAR_PORTAFOLIO_CONTRATO";
							msg.descripcion = "Problema al asignar relacion entre portafolio y contrato traspaso.";
							salida.P_OUT_MENSAJES.add(msg);
							salida.P_OUT_ERRORESCONTADOR++;
						}
					
					}else {
						
						//validacion tipo de plan
					    Boolean valido =validaPlanInversion(fila,con);
						if (!valido ) {	
							HashMap<String,String> valores = new HashMap<>();
							valores.put("NombrePlanAhorro", fila.NombrePlan);
							msg = new MensajeSalida(fila.IdContratoSAAPV, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_PLAN_TIPO_INVERSION", valores, con, in.p_IdEmpresa);
							salida.P_OUT_MENSAJES.add(msg);
							salida.P_OUT_ERRORESCONTADOR++;
							continue;
						}
						
						//Nuevo Portafolio, Crear y vincular.				
						HashMap<String,Object> resultSP = CrearPortafolioAPV(in, fila, sql, con);
								
						if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
							SalidaCliente.P_OUT_REFIDPORTAFOLIO = (Long)resultSP.get("p_out_RefId");
						}else{
							for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
								SalidaCliente.addError(errorDetalle.Codigo, errorDetalle.Descripcion);
							}
							for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
								SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
							}
						}
						
						
						
						if(SalidaCliente.P_OUT_REFIDPORTAFOLIO > 0) {
							IdPortafolioCreado = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
							msg = new MensajeSalida();
							msg.id = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
							msg.tipo = "OK";
							msg.codigo = "CREACION_PORTAFOLIO_OK";
							msg.descripcion = "Creacion de portafolio exitosa";
							salida.P_OUT_MENSAJES.add(msg);
						}
				 			//==============================================================================================
							//Se crea relacion entre el Contrato SAAPV y el Portafolio
				 			//==============================================================================================
						int[] result = RelacionarPortafolioContratoSAAPV(fila, IdPortafolioCreado, FechaPeriodo, sql, con);
						
						if(result != null && result.length > 0 && result[0] == 1){
							msg = new MensajeSalida();
							msg.id = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
							msg.tipo = "OK";
							msg.codigo = "CREAR_PORTAFOLIO_CONTRATO_OK";
							msg.descripcion = "Se crea relacion entre portafolio y contrato traspaso.";
							salida.P_OUT_MENSAJES.add(msg);
						}else{
							msg = new MensajeSalida();
							msg.id = 6L;
							msg.tipo = "ERROR";
							msg.codigo = "ERROR_ASIGNAR_PORTAFOLIO_CONTRATO";
							msg.descripcion = "Problema al asignar relacion entre portafolio y contrato traspaso.";
							salida.P_OUT_MENSAJES.add(msg);
							salida.P_OUT_ERRORESCONTADOR++;
						}
					}
				
					/*Ingreso Detalle Traspaso*/
					Long IdPortafolioIngreso;
					if(portafolioEncontrado == true) IdPortafolioIngreso = matrizAgrupadorTAPVPort.get(0).IdPortafolio;
					else IdPortafolioIngreso = IdPortafolioCreado;
					Long IdInsertDetTraspaso = IngresarDetalleTraspaso (fila,IdPortafolioIngreso, sql, con);
		
					if(IdInsertDetTraspaso != null && IdInsertDetTraspaso > 0){
						salida.P_OUT_IDRESULTADO.add(IdInsertDetTraspaso);
						
						msg = new MensajeSalida();
						msg.id = IdPortafolioIngreso;
						msg.tipo = "OK";
						msg.codigo = "CREAR_DETALLE_TRASPASO_OK";
						msg.descripcion = "Se crea detalle del traspaso nuevo.";
						salida.P_OUT_MENSAJES.add(msg);
					}else{
						msg = new MensajeSalida();
						msg.id = 6L;
						msg.tipo = "ERROR";
						msg.codigo = "ERROR_CREAR_DETALLE_TRASPASO";
						msg.descripcion = "Problema al ingresar el Detalle Traspaso.";
						salida.P_OUT_MENSAJES.add(msg);
						salida.P_OUT_ERRORESCONTADOR++;
					}
					
					HashMap<String,Object> portNuevo = new HashMap<String,Object>();
					portNuevo.put("IdPortafolio", SalidaCliente.P_OUT_REFIDPORTAFOLIO);	
					List<infoPort> portNuevoSelect = sql.query(con, SQL_DATOS_PORTAFOLIO_CREADO, portNuevo, infoPort.class);
					
					HashMap<String,Object> portPrevio = new HashMap<String,Object>();
					portPrevio.put("IdCliente", in.p_Traspasos.get(0).IdCliente);	
					List<vistaInfoPort> portPrevioSelect = sql.query(con, SQL_VISTA_PORTAFOLIOS_CLIENTE, portPrevio, vistaInfoPort.class);
					
					System.out.println("Portafolios Validados");
				}
			}
 		}	
		return salida;
	}
	
	private HashMap<String,Object> CrearPortafolioAPV (ParametrosEntradaServicio in, Traspaso fila, SQL sql,Connection con) throws Exception{
		HashMap<String,Object> paramsIn = new HashMap<String,Object>();
		HashMap<String,Object> resultSP = new HashMap<String,Object>();
		
		paramsIn.put("IdEmpresa", in.p_IdEmpresa);
		paramsIn.put("IdUsuario", in.p_IdUsuario);
		paramsIn.put("IdModulo", in.p_IdModulo);			
		paramsIn.put("PropositoPortafolio", fila.IdPropositoPortafolio);
		//paramsIn.put("IdCliente", fila.IdCliente);
		//paramsIn.put("IdPerfilComercial", fila.IdPerfilComercial);
		if(fila.IdCliente.compareTo(0L) == 0) {
			HashMap<String,Object> paramsInCli = new HashMap<String,Object>();
			paramsInCli.put("IdContratoSAAPV", fila.IdContratoSAAPV);	
			List<ClienteContratoSAAPV> clienteContrato = sql.query(con, SQL_CONSULTAR_CLIENTE_CONTRATO, paramsInCli, ClienteContratoSAAPV.class);
			paramsIn.put("IdCliente", clienteContrato.get(0).IdCliente);
			paramsIn.put("IdPerfilComercial", clienteContrato.get(0).IdPerfilComercial);
		}else {
			paramsIn.put("IdCliente", fila.IdCliente);
			paramsIn.put("IdPerfilComercial", fila.IdPerfilComercial);
		}
		paramsIn.put("CodigoPortafolio", fila.RegimenTributario);
		paramsIn.put("CodigoRegimen", fila.RegimenTributario);
		paramsIn.put("NombrePlan", fila.NombrePlan);
		paramsIn.put("CodigoAgrupador", fila.CodigoAgrupador);
		
		//Salida HashMap
		HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
		paramsOut.put("p_out_RefId", Long.class);
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
		paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
		
		HashMap<String,Object> portPrevio = new HashMap<String,Object>();
		portPrevio.put("IdCliente", in.p_Traspasos.get(0).IdCliente);	
		List<vistaInfoPort> portPrevioSelect = sql.query(con, SQL_VISTA_PORTAFOLIOS_CLIENTE, portPrevio, vistaInfoPort.class);
		
		resultSP = sql.call(con, SQL_CREAR_PORTAFOLIO_APV_Call, paramsIn, paramsOut);
		Long IdPortCreado = (Long)resultSP.get("p_out_RefId");
		
		HashMap<String,Object> portNuevo = new HashMap<String,Object>();
		portNuevo.put("IdPortafolio", IdPortCreado);	
		List<infoPort> portNuevoSelect = sql.query(con, SQL_DATOS_PORTAFOLIO_CREADO, portNuevo, infoPort.class);
		
		HashMap<String,Object> portPost = new HashMap<String,Object>();
		portPost.put("IdCliente", in.p_Traspasos.get(0).IdCliente);	
		List<vistaInfoPort> portPostSelect = sql.query(con, SQL_VISTA_PORTAFOLIOS_CLIENTE, portPost, vistaInfoPort.class);
		
		for(vistaInfoPort portafolioPrev : portPrevioSelect) {	
			for(vistaInfoPort portafolioPost : portPostSelect) {
				if(portafolioPrev.IdPortafolio.compareTo(portafolioPost.IdPortafolio) == 0) {
					if(portafolioPrev.IdEstado.compareTo(portafolioPost.IdEstado) != 0) {
						ModificarEstadoPortafolioTraspaso(portafolioPrev.IdPortafolio, portafolioPrev.IdEstado, sql, con);
					}
				}
			}
		}

		if(portNuevoSelect.get(0).IdEstadoAutorizacion.compareTo("AUT") != 0 || portNuevoSelect.get(0).IdEstado.compareTo("ACT") != 0) {
			ModificarEstadoPortafolioTraspaso(IdPortCreado, "ACT", sql, con);
		}

		HashMap<String,Object> portPostMod = new HashMap<String,Object>();
		portPostMod.put("IdCliente", in.p_Traspasos.get(0).IdCliente);	
		List<vistaInfoPort> portPostSelectMod = sql.query(con, SQL_VISTA_PORTAFOLIOS_CLIENTE, portPostMod, vistaInfoPort.class);
		
		return resultSP;
	}
	
	public boolean ModificarEstadoPortafolioTraspaso (Long IdPortafolio, String estado, SQL sql,Connection con) throws Exception{
		
		List<EstadoPortafolio> listPortafolioContrato = new ArrayList <EstadoPortafolio>();
		EstadoPortafolio portafolioContrato = new EstadoPortafolio();
		int[] modCon;
		
		if(estado.compareTo("ACT") == 0) {		
			portafolioContrato.IdPortafolio = IdPortafolio;
			portafolioContrato.IdEstado = estado;
			portafolioContrato.IdEstadoAutorizacion = "AUT";
			listPortafolioContrato.add(portafolioContrato);
			modCon = sql.batch(con, SQL_INHABILITAR_ESTADO_PORTAFOLIO, listPortafolioContrato, EstadoPortafolio.class);
		}
		else {		
			portafolioContrato.IdPortafolio = IdPortafolio;
			portafolioContrato.IdEstado = "INA";
			portafolioContrato.IdEstadoAutorizacion = "AUT";
			listPortafolioContrato.add(portafolioContrato);
			modCon = sql.batch(con, SQL_INHABILITAR_ESTADO_PORTAFOLIO, listPortafolioContrato, EstadoPortafolio.class);	
		}	
		
		if(modCon.length > 0 && modCon != null) return true;
		else return false;
	}
	
	private int EliminarPortafolioTraspaso (Long IdPortafolio, Long IdContratoSAAPV, SQL sql,Connection con) throws Exception {
		
		HashMap<String,Object> cantPort = new HashMap<String,Object>();
		cantPort.put("IdPortafolio", IdPortafolio);
		cantPort.put("IdContratoSAAPV", IdContratoSAAPV);
		Long cantPortafolios = sql.countQuery(con, SQL_CANTIDAD_PORTAFOLIO_CONTRATOSAAPV, cantPort);
		
		if(cantPortafolios == 0) {
			
			List<EstadoPortafolio> listPortafolioContrato = new ArrayList <EstadoPortafolio>();
			EstadoPortafolio portafolioContrato = new EstadoPortafolio();
			portafolioContrato.IdPortafolio = IdPortafolio;
			portafolioContrato.IdEstado = "INA";
			portafolioContrato.IdEstadoAutorizacion = "AUT";
			listPortafolioContrato.add(portafolioContrato);
			int[] modCon = sql.batch(con, SQL_INHABILITAR_ESTADO_PORTAFOLIO, listPortafolioContrato, EstadoPortafolio.class);			
			
			if(modCon.length > 0 && modCon != null) return 1;
			else return 2;
			
		}else return 0;
	}
	
	private List<EstadoPortafolio> matrizAgrupadorTraspasoPortafolio (Traspaso fila, SQL sql,Connection con) throws Exception{
		//Se verifica si matrizAPV ingresada tiene portafolio
		HashMap<String,Object> paramsInMAPV = new HashMap<String,Object>();
		paramsInMAPV.put("p_IdPlanTipoInversion", fila.IdPlanTipoInversion);
		paramsInMAPV.put("p_IdProductoAgrupador", fila.IdProductoAgrupador);
		paramsInMAPV.put("p_IdRegimenTributario", fila.IdRegimenTributario);	
		List<MatrizAgrupadorAPVTraspaso> matrizAgrupadorTAPV = sql.query(con, SQL_CONSULTAR_MATRIZ_AGRUPADOR_APV, paramsInMAPV, MatrizAgrupadorAPVTraspaso.class);
		
		HashMap<String,Object> paramsInMAPVPort = new HashMap<String,Object>();
		paramsInMAPVPort.put("IdMatrizAgrupadorAPV", matrizAgrupadorTAPV.get(0).IdMatrizAgrupadorAPV);
		paramsInMAPVPort.put("IdCliente", fila.IdCliente);
		//paramsInMAPVPort.put("IdEstado", "ACT");	
		List<EstadoPortafolio> matrizAgrupadorPort = sql.query(con, SQL_CONSULTAR_MATRIZ_AGRUPADOR_APV_PORTAFOLIO_NUEVO, paramsInMAPVPort, EstadoPortafolio.class);
		return matrizAgrupadorPort;
	}
	
	private Long IngresarDetalleTraspaso (Traspaso fila, Long IdPortafolio, SQL sql,Connection con) throws Exception {
		List<Long> IdDetalleTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdDetalleTraspaso", 1L);
		List<DetalleTraspaso> lstDetalleTraspasoVig = new ArrayList<DetalleTraspaso>();
		DetalleTraspaso dtTraspasoVig = new DetalleTraspaso();
		dtTraspasoVig.IdTraspaso = fila.IdTraspaso;
		dtTraspasoVig.IdDetalleTraspaso = IdDetalleTraspaso.get(0);
		dtTraspasoVig.IdPortafolio = IdPortafolio;
		dtTraspasoVig.FechaIngreso = fila.FechaIngreso;
		dtTraspasoVig.FechaModificacion = "0001-01-01";
		dtTraspasoVig.IdPlanTipoInversion = fila.IdPlanTipoInversion;
		dtTraspasoVig.IdProductoAgrupador = fila.IdProductoAgrupador;
		dtTraspasoVig.IdRegimenTributario = fila.IdRegimenTributario;
		dtTraspasoVig.MontoPeso = Double.valueOf(fila.MontoPeso.toString());
		dtTraspasoVig.MontoUF = Double.valueOf(fila.MontoUF.toString());
		dtTraspasoVig.Porcentaje = Double.valueOf(fila.Porcentaje.toString());
		dtTraspasoVig.IdEstado = "VIG";
		dtTraspasoVig.EsReapertura = fila.EsReapertura;
		lstDetalleTraspasoVig.add(dtTraspasoVig);	
		int[] resultadoIngreso = sql.batch(con, SQL_CREAR_DETALLE_TRASPASO, lstDetalleTraspasoVig, DetalleTraspaso.class);
		if(resultadoIngreso.length > 0 && resultadoIngreso != null) return IdDetalleTraspaso.get(0);
		else return 0L;
	}
	
	private int[] AsignarTraspasoAPortafolio (Traspaso fila, Long IdPortafolio, SQL sql,Connection con) throws Exception {
		List<PortafolioContratoSAAPVModificar> listPortafolioContrato = new ArrayList <PortafolioContratoSAAPVModificar>();
		PortafolioContratoSAAPVModificar portafolioContrato = new PortafolioContratoSAAPVModificar();
		portafolioContrato.IdContratoSAAPV = fila.IdContratoSAAPV;
		portafolioContrato.IdPortafolio = fila.IdPortafolio;
		portafolioContrato.IdPortafolioNuevo = IdPortafolio;
		portafolioContrato.MontoPesos = fila.MontoPeso;
		portafolioContrato.MontoUF = fila.MontoUF;
		listPortafolioContrato.add(portafolioContrato);

		int[] modCon = sql.batch(con, SQL_MODIFICAR_DETALLE_TRASPASO_CONTRATOSAAPV, listPortafolioContrato, PortafolioContratoSAAPVModificar.class);		
		System.out.println("Se asigna traspaso a portafolio");
		return modCon;
	}
	
	private int[] RelacionarPortafolioContratoSAAPV(Traspaso fila, Long IdPortafolio, String FechaPeriodo, SQL sql,Connection con) throws Exception {
		String OrigenAhorro = ""; 
		if(fila.RegimenTributario.equals("BF")){
			OrigenAhorro = "BF";
		} else{
			if(fila.RegimenTributario.substring(0, 3).equals("APV")) OrigenAhorro = "APV";
			if(fila.RegimenTributario.substring(0, 2).equals("DC")) OrigenAhorro = "DC";
		}
		
		PortafolioContratoSAAPV inPortCont = new PortafolioContratoSAAPV();
		inPortCont.IdPortafolio = IdPortafolio;
		List<PortafolioContratoSAAPV> ListPortCont = new ArrayList<PortafolioContratoSAAPV>();
		inPortCont.IdContratoSAAPV = fila.IdContratoSAAPV;
		inPortCont.OrigenAhorro = OrigenAhorro;
		inPortCont.MontoPesos = Double.valueOf(fila.MontoPeso.toString());
		inPortCont.MontoUF = Double.valueOf(fila.MontoUF.toString());
		inPortCont.PorcentajeRegimenImponible = Double.valueOf(fila.Porcentaje.toString());
		inPortCont.Pago = "UNI";
		inPortCont.PeriodoInicio = FechaPeriodo;
		inPortCont.PeriodoTermino = FechaPeriodo;
		inPortCont.IdEstado = "VIG";
		ListPortCont.add(inPortCont);

		int[] result = sql.batch(con, SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV, ListPortCont, PortafolioContratoSAAPV.class);
		return result;
	}
	
	private int[] ActualizarDetalleTraspaso (Traspaso fila, Long IdPortafolio, String FechaPeriodo, SQL sql,Connection con) throws Exception {
		List<DetalleTraspasoModificar> listModificarDetalleTraspaso = new ArrayList <DetalleTraspasoModificar>();
		DetalleTraspasoModificar detalleTraspasoModificar = new DetalleTraspasoModificar();
		detalleTraspasoModificar.IdDetalleTraspaso = fila.IdDetalleTraspaso;
		detalleTraspasoModificar.IdTraspaso = fila.IdTraspaso;
		detalleTraspasoModificar.IdPortafolio = IdPortafolio;
		detalleTraspasoModificar.IdProductoAgrupador = fila.IdProductoAgrupador;
		detalleTraspasoModificar.IdRegimenTributario = fila.IdRegimenTributario;
		detalleTraspasoModificar.MontoPeso = fila.MontoPeso;
		detalleTraspasoModificar.MontoUF = fila.MontoUF;
		detalleTraspasoModificar.FechaIngreso = fila.FechaIngreso;
		listModificarDetalleTraspaso.add(detalleTraspasoModificar);
		
		int[] modDT = sql.batch(con, SQL_MODIFICAR_DETALLE_TRASPASO, listModificarDetalleTraspaso, DetalleTraspasoModificar.class);
		return modDT;
	}
	
	private Boolean validaPlanInversion (Traspaso fila,Connection con)  throws Exception {
		SQL sql = new SQL();
		Boolean respuesta =false;
		
		HashMap<String,Object> validaPlan = new HashMap<String,Object>();
		validaPlan.put("IdCliente", fila.IdCliente);
		List<validaPlanApv>validaTipoPlan = sql.query(con, SQL_VISTA_PORTAFOLIOS_CLIENTE_MATRIZ_APV, validaPlan, validaPlanApv.class);						
		//valida si existen portafolios 
		if (!validaTipoPlan.isEmpty() ) {
			//valida si existen del mismo tipo de plan de inversion
		   for(validaPlanApv valPlan : validaTipoPlan) {
			    Long IdPlanTipoInversion = valPlan.IdPlanTipoInversion != null ? valPlan.IdPlanTipoInversion.longValue(): 0L;
			    IdPlanTipoInversion = IdPlanTipoInversion != null ? IdPlanTipoInversion : 0L;
			   if(IdPlanTipoInversion.compareTo(fila.IdPlanTipoInversion) == 0) {
				   respuesta= true;
			   }
		    }
		}else {
			respuesta= true;
		}
		
		return respuesta;
	}	
	
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
			//SimpleDateFormat FormatoFechaSistema = new SimpleDateFormat("yyyy-MM-dd hh:mmm:ss.SSS");
				//Long time = System.currentTimeMillis();
				//java.sql.Date FechaSistema = Fecha.toDate("1978-01-01T03:00:00.000Z");
				//Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"I\",\"p_Fecha\":\"2023-09-14\",\"p_Traspasos\":[{\"IdTraspaso\":2991733,\"IdContratoSAAPV\":1013894,\"FechaIngreso\":\"2023-09-14\",\"FechaModificacion\":\"0001-01-01\",\"IdCliente\":85050,\"IdPersonaCliente\":117940,\"DocumentoIdentidad\":\"15571571-5\",\"IdPerfilComercial\":\"APV INV ACTIVA_ARRIESGADO\",\"IdDetalleTraspaso\":1930047,\"IdPortafolio\":487451,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":4,\"RegimenTributario\":\"DC-AL\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":2000000,\"MontoUF\":0,\"Porcentaje\":0},{\"IdTraspaso\":2991733,\"IdContratoSAAPV\":1013894,\"FechaIngreso\":\"2023-09-14\",\"FechaModificacion\":\"0001-01-01\",\"IdCliente\":85050,\"IdPersonaCliente\":117940,\"DocumentoIdentidad\":\"15571571-5\",\"IdPerfilComercial\":\"APV INV ACTIVA_ARRIESGADO\",\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":2,\"RegimenTributario\":\"APV-A\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":1000000,\"MontoUF\":0,\"Porcentaje\":0}]}";
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"I\",\"p_Fecha\":\"2023-07-18\",\"p_Traspasos\":[{\"IdTraspaso\":448,\"IdContratoSAAPV\":2846,\"FechaIngreso\":\"2023-07-18\",\"FechaModificacion\":\"0001-01-01\",\"IdDetalleTraspaso\":680,\"IdPortafolio\":1458937,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":3,\"RegimenTributario\":\"APV-B\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":300000,\"MontoUF\":0,\"Porcentaje\":0},{\"IdTraspaso\":448,\"IdContratoSAAPV\":2846,\"FechaIngreso\":\"2023-07-18\",\"FechaModificacion\":\"0001-01-01\",\"IdDetalleTraspaso\":563,\"IdPortafolio\":1458609,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":2,\"RegimenTributario\":\"APV-A\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":1500000,\"MontoUF\":0,\"Porcentaje\":0}]}";
				
				ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				ModificarPortafolioTraspaso demo = new ModificarPortafolioTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
	 				con.rollback();
	 				//con.commit();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					SQL.exec(con, "DROP TABLE #RegistroAtributo");
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}