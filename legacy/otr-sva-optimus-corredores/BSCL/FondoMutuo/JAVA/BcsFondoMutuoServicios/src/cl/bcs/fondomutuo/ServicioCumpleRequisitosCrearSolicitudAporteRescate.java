package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
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

import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ServicioCumpleRequisitosCrearSolicitudAporteRescate implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ServicioCumpleRequisitosCrearSolicitudAporteRescate.class);
	
	private final String SQL_SELECT_EMPLEADO_USUARIO 	= loadResourceToString("cl/bcs/fondomutuo/sql/ServicioCumpleRequisitosCrearSolicitudAporteRescate_SelectEmpleadoUsuario.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdEmpleado;
		public List<InSolicitudes> p_SolicitudesAporteRescate;
		public List<InPortafolio> p_Portafolios;
		public String p_PermiteOperarProspectos;
		public EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate() {
			this.p_SolicitudesAporteRescate = new ArrayList<InSolicitudes>();
			this.p_Portafolios = new ArrayList<InPortafolio>();
			this.p_PermiteOperarProspectos = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudes{
		public Long IdInterno;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String IdTipoMovimientoFondoMutuo;
    	public String IdEstadoSerie;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String EstadoFondo;
		public String EstadoBloqueo;
		public String EsFondoValido;
		public String EsFechaValidaAporte;
		public String EsFechaValidaRescate;
		public String EsDiferido;
		public String EsMontoMaximo;
		public String EsMontoMinimo;
		public BigDecimal ValorCuota;
		public String Overnight;
		public String EstadoLiquidez;
		public String EsMoneyMarket;
		public String EsFamiliaEquivalente;
		public String EsPortafolioRegimenValido;
		public String EsRegimenValido;
		public String PermiteAporte;
		public String PermiteTraspaso;
		public String PermiteRescate;
		public String RestringeAporte;
		public String RestringeRescate;
		//REINVERSIONES
		public String PermiteRescateMandato;
		public String PermiteCambioGrupo;
		public String EsRegimenDestinoValido;
		public String PermiteCambioFondo;
		//TRASPASOS y TRANFERENCIAS
		public String EsPortafolioValido;
		//CUENTA INVERSION
		public String EsCuentaInversionValido;	
		//Validacion Cliente-Producto
    	public String IdTipoOperacion;
    	public String IdTipoProducto;
    	public String Glosa;
    	public Long FolioCartera;
    	public String ProcedenciaCanal;
    	public InSolicitudes() {
    		this.PermiteRescate = "S";
    		this.PermiteTraspaso = "S";
    		this.RestringeAporte  ="N";
    		this.RestringeRescate = "N";
    		this.PermiteCambioFondo="S";
    		this.Glosa ="";
    		this.FolioCartera = 0L;
    		this.ProcedenciaCanal ="";
    	}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InPortafolio{
		public Long IdInterno;
		public Long IdPortafolio;
		public Long IdPortafolioReal;
		public String IdEstadoPortafolio;
		public Long IdCliente;
		public Long IdClienteReal;
		public String IdEstadoCliente;
		public Long IdClienteDestino;
		public Long IdClienteRealDestino;
		public Long IdPortafolioDestino;
		public Long IdPortafolioRealDestino;
		public String IdEstadoClienteDestino;
		public String IdEstadoPortafolioDestino;
		public String DocumentoIdentidad;
		public Long IdEmpleadoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate{
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class ObjValidaProducto{
		public String ValidaProducto;
		public String IdTipoOperacion;
		public String IdTipoOperacionTodos;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate.class);
		SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate logica(Connection con, EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entrada) throws Exception{
		SQL sql=new SQL();
		SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate out = new SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate();
		logger.info("INICIO::ServicioCumpleRequisitosCrearSolicitudAporteRescate");
		
		String reinversionDistintasFamilias = "";
		try {
			reinversionDistintasFamilias = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "VALIDAR_REINVERSION_DISTINTAS_FAMILIAS");
		}
		catch(Exception e) {
			reinversionDistintasFamilias = "S";
		}
		
		
		HashMap<Long, Object> portafolios = Utils.List2Hash(entrada.p_Portafolios, "IdInterno");
		HashMap<String,String> params = new HashMap<String,String>();
		HashMap<String,String> params2 = new HashMap<String,String>();
		HashMap<String,Object> params3 = new HashMap<String,Object>();
		
		List<String> listaTipoProducto  = new ArrayList<String>();
		List<String> listaTipoOperacion = new ArrayList<String>();
		cl.bcs.plataforma.com.ConsultarProductosPortafolio serviceConsultarProductosPortafolio = new cl.bcs.plataforma.com.ConsultarProductosPortafolio();
		cl.bcs.plataforma.com.ConsultarProductosPortafolio.ConsultarProductosPortafolio_Entrada entradaConsultarProductosPortafolio = new cl.bcs.plataforma.com.ConsultarProductosPortafolio.ConsultarProductosPortafolio_Entrada();
		
		List<Long> idsPortafolios = new ArrayList<Long>();
		for(InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate){
			InPortafolio portafolio = (InPortafolio) portafolios.get(solicitud.IdInterno);
			if (!idsPortafolios.contains(portafolio.IdPortafolio)) {
				idsPortafolios.add(portafolio.IdPortafolio);
			}
		}
		
		params3.put("IdPortafolio", idsPortafolios);
		logger.trace("INICIO::SQL_SELECT_PORTAFOLIO");
		List<ClientePortafolioSolicitudes> clientesSolicitudes = sql.query(con, SQL_SELECT_PORTAFOLIO, params3, ClientePortafolioSolicitudes.class);
		HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");

		logger.trace("FIN::SQL_SELECT_PORTAFOLIO");

		for(InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate){
			
			InPortafolio portafolio = (InPortafolio) portafolios.get(solicitud.IdInterno);
			//Valida Producto Portafolio - Empleado
			if (!listaTipoProducto.contains(solicitud.IdTipoProducto)) listaTipoProducto.add(solicitud.IdTipoProducto);
			if (!listaTipoOperacion.contains(solicitud.IdTipoOperacion)) listaTipoOperacion.add(solicitud.IdTipoOperacion);
			cl.bcs.plataforma.com.ConsultarProductosPortafolio.PortafolioEntrada port = new cl.bcs.plataforma.com.ConsultarProductosPortafolio.PortafolioEntrada();
	        port.IdPortafolio = portafolio.IdPortafolio;
	        entradaConsultarProductosPortafolio.p_portafolios.add(port);

			ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
			params3.clear();
			
			//Valida Cliente			
			if(portafolio == null || portafolio.IdClienteReal == null){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_NO_EXISTE",params,con,entrada.p_IdEmpresa));
			}
			//Valida Cliente Habilitado
			if(portafolio != null && !_c.EstadoActivo.equalsIgnoreCase(portafolio.IdEstadoCliente) && !((_c.SI).equals(entrada.p_PermiteOperarProspectos) && _c.SI.equals(cliente.EsClienteNuevoProspecto)) ){
				params.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_BLOQUEADO",params,con,entrada.p_IdEmpresa));
				params.clear();
			}
			//Valida Portafolio Existe
			if(portafolio == null || portafolio.IdPortafolioReal == null){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_NO_EXISTE",params,con,entrada.p_IdEmpresa));
			}
			//Valida Portafolio habilitado
			if(portafolio != null && !_c.EstadoActivo.equalsIgnoreCase(portafolio.IdEstadoPortafolio)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_NO_HABILITADO",params,con,entrada.p_IdEmpresa));
			}
			if(portafolio != null && (portafolio.IdClienteRealDestino == null || portafolio.IdClienteRealDestino.intValue() != 0)){
				//Cliente destino no existe
				if(portafolio.IdClienteRealDestino == null){
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_DESTINO_NO_EXISTE",params,con,entrada.p_IdEmpresa));
				}
				//Valida Cliente Destino Habilitado
				if(!_c.EstadoActivo.equalsIgnoreCase(portafolio.IdEstadoClienteDestino)){
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_DESTINO_BLOQUEADO",params,con,entrada.p_IdEmpresa));
				}
				//Cliente destino valido
				if(portafolio.IdClienteRealDestino != null && portafolio.IdClienteReal != null && portafolio.IdClienteRealDestino.intValue() == portafolio.IdClienteReal.intValue()){
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_DESTINO_NO_VALIDO",params,con,entrada.p_IdEmpresa));
				}
				
				if(portafolio.IdPortafolioRealDestino == null || portafolio.IdPortafolioRealDestino.intValue() != 0){
					//Valida Portafolio Destino Existe
					if(portafolio.IdPortafolioRealDestino == null){
						out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_DESTINO_NO_EXISTE",params,con,entrada.p_IdEmpresa));
					}
					//Valida Portafolio Destino habilitado
					if(!_c.EstadoActivo.equalsIgnoreCase(portafolio.IdEstadoPortafolio)){
						out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_DESTINO_NO_HABILITADO",params,con,entrada.p_IdEmpresa));
					}
					//Portafolio destino valido
					if(portafolio.IdPortafolioRealDestino != null && portafolio.IdPortafolioReal != null && portafolio.IdPortafolioRealDestino.intValue() == portafolio.IdPortafolioReal.intValue()){
						out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_DESTINO_NO_VALIDO",params,con,entrada.p_IdEmpresa));
					}
				}
			}
			
			if("".equalsIgnoreCase(solicitud.IdSerieFondo)){
				params.put("Nemotecnico", solicitud.IdSerieFondo);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"INSTRUMENTO_NO_EXISTE",params,con,entrada.p_IdEmpresa));
				params.clear();
			}
			
			if(!_c.EstadoActivo.equalsIgnoreCase(solicitud.IdEstadoSerie)){
				params.put("Nemotecnico", solicitud.IdSerieFondo);
				params.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"INSTRUMENTO_NO_VIGENTE_RUT_CLIENTE",params,con,entrada.p_IdEmpresa));
				params.clear();
			}
			
			if("".equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"TIPO_OPERACION_NO_VALIDA_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.Abierto.equalsIgnoreCase(solicitud.EstadoFondo)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,"FONDO_CERRADO_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.NO.equalsIgnoreCase(solicitud.EstadoBloqueo)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FONDO_BLOQUEADO_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsFondoValido)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FONDO_INVALIDO_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.NO.equalsIgnoreCase(solicitud.RestringeAporte) && _c.TipoOperacionAporteFondoMutuo.equalsIgnoreCase(solicitud.IdTipoOperacion)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FONDO_SERIE_RESTRINGIDO_APORTE_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.NO.equalsIgnoreCase(solicitud.RestringeRescate)&& _c.TipoOperacionRescateFondoMutuo.equalsIgnoreCase(solicitud.IdTipoOperacion)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FONDO_SERIE_RESTRINGIDO_RESCATE_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsFechaValidaAporte)){
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FECHA_INVALIDA_APORTE_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsFechaValidaRescate)){
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FECHA_INVALIDA_RESCATE_REQ_APO_RES",params,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsDiferido) && (!_c.RescateTraspaso.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) && !"DESINVERSION APV".equalsIgnoreCase(solicitud.Glosa))){
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FONDO_NO_DIFERIDO_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
				
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.PermiteCambioFondo) && (_c.Reinversion.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo)||_c.AporteReinversion.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo)) ){
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"FONDO_NO_PERMITE_CAMBIO_FONDO",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsMontoMaximo)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("CodigoPortafolio",cliente.CodigoPortafolio);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"MONTO_MAXIMO_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.OrigenPAC.equalsIgnoreCase(solicitud.ProcedenciaCanal)) {
			    if(!_c.SI.equalsIgnoreCase(solicitud.EsMontoMinimo)){
				     params2.put("IdInstrumento",solicitud.IdSerieFondo);
				     params2.put("CodigoPortafolio",cliente.CodigoPortafolio);
				     params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				     out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"MONTO_MINIMO",params2,con,entrada.p_IdEmpresa));
				     params2.clear();
			    }
			}
			
			//Validacion de indicador (Cliente bloqueado por cumplimiento) para ingreso de titulos
			if(_c.IngresoTitulo.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) && cliente.BloqueadoPorCumplimiento.equalsIgnoreCase("S"))
			{
				params2.clear();
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				params2.put("NombreCliente",cliente.NombreCompleto);
										
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			
			
			
			if(BigDecimal.ZERO.compareTo(solicitud.ValorCuota) == 1 || BigDecimal.ZERO.compareTo(solicitud.ValorCuota) == 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"VALOR_CUOTA_INCORRECTO",params,con,entrada.p_IdEmpresa));
			}
			
			if(_c.SI.equalsIgnoreCase(solicitud.Overnight) && !_c.Abierto.equalsIgnoreCase(solicitud.EstadoLiquidez)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,"FECHA_LIQUIDEZ_CERRADO",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsMoneyMarket)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"NO_MONEY_MARKET",params,con,entrada.p_IdEmpresa));
			}
			
			if(BigDecimal.ZERO.compareTo(solicitud.Monto) == 1 || BigDecimal.ZERO.compareTo(solicitud.Monto) == 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"MONTO_VALIDO",params,con,entrada.p_IdEmpresa));
			}
			
			if(BigDecimal.ZERO.compareTo(solicitud.NumeroCuotas) == 1 || BigDecimal.ZERO.compareTo(solicitud.NumeroCuotas) == 0){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				params2.put("NombreCliente",cliente.NombreCompleto);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"NUMERO_CUOTA_VALIDO_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(_c.SI.equals(reinversionDistintasFamilias)) {
				if(_c.Reinversion.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo) && !_c.SI.equalsIgnoreCase(solicitud.EsFamiliaEquivalente)){
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"NO_ES_FAMILIA_EQUIVALENTE",params,con,entrada.p_IdEmpresa));
				}
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsPortafolioRegimenValido)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_REGIMEN_INVALIDO",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsRegimenValido)){
				params2.put("IdInstrumento",solicitud.IdSerieFondo);
				params2.put("DocumentoIdentidad",cliente.DocumentoIdentidad);
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"REGIMEN_NO_EXISTE_REQ_APO_RES",params2,con,entrada.p_IdEmpresa));
				params2.clear();
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.PermiteRescateMandato)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"RESCATE_MANDATO_NO_PERMITIDO",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.PermiteCambioGrupo)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"CAMBIO_GRUPO_REGIMEN_NO_PERMITIDO",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsPortafolioValido)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_DESTINO_NO_COINCIDE_PROPOSITO",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equalsIgnoreCase(solicitud.EsCuentaInversionValido)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_NO_OPERA_CUENTA_INVERSION_FFMM",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equals(solicitud.PermiteAporte)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"REGIMEN_NO_PERMITE_APORTE",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equals(solicitud.PermiteTraspaso)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"REGIMEN_NO_PERMITE_TRASPASO",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equals(solicitud.PermiteRescate)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"REGIMEN_NO_PERMITE_RESCATE",params,con,entrada.p_IdEmpresa));
			}
			
			if(!_c.SI.equals(solicitud.EsRegimenDestinoValido)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"COMBINACION_REGIMEN_REINVERSION_NO_CONFIGURADO",params,con,entrada.p_IdEmpresa));
			}
			
		}
		// En caso de descripcion duplicada, mantiene el primera descripcion
		out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream()		
			    .collect(Collectors.toMap(
			        mensaje -> mensaje.descripcion,
			        mensaje -> mensaje,
			        (mensaje1, mensaje2) -> mensaje1
			    ))
			    .values()
			    .stream()
			    .collect(Collectors.toCollection(ArrayList::new));
					
		entradaConsultarProductosPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarProductosPortafolio.p_IdModulo = "FMM";
		entradaConsultarProductosPortafolio.p_IdUsuario = entrada.p_IdUsuario;
		
		cl.bcs.plataforma.com.ConsultarProductosPortafolio.ConsultarProductosPortafolio_Salida salidaConsultarProductosPortafolio = serviceConsultarProductosPortafolio.logica(con, entradaConsultarProductosPortafolio);
		boolean validaProducto = false;
		for (InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate) {
			validaProducto = false;
			for (cl.bcs.plataforma.com.ConsultarProductosPortafolio.DetallePerfilProducto detalle : salidaConsultarProductosPortafolio.p_out_PortafolioProductos) {				
				if (detalle.IdPortafolio.equals(solicitud.IdPortafolio)
						&& detalle.IdTipoProducto.equalsIgnoreCase(solicitud.IdTipoProducto)
						&& (detalle.IdTipoOperacion.equalsIgnoreCase("0") || detalle.IdTipoOperacion.equalsIgnoreCase(solicitud.IdTipoOperacion))
						&& (detalle.IdEstadoPerfilProducto.equalsIgnoreCase("ACT"))
						) {
					validaProducto = true;
				}
			}
			if (!validaProducto) out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_NO_OPERA_PRODUCTO_SELECCIONADO",params,con,entrada.p_IdEmpresa));
		}
		
		HashMap<String,Object> paramsProductos = new HashMap<String,Object>();
		paramsProductos.clear();
		paramsProductos.put("p_IdUsuario", entrada.p_IdUsuario);
		logger.debug("INICIO::SQL_SELECT_EMPLEADO_USUARIO");
		List<ObjValidaProducto> resEmpleadoUsuario = sql.query(con, SQL_SELECT_EMPLEADO_USUARIO, paramsProductos, ObjValidaProducto.class);
		logger.debug("FIN::SQL_SELECT_EMPLEADO_USUARIO");	
		paramsProductos.clear();
		paramsProductos.put("p_IdEmpresa",entrada.p_IdEmpresa);
		paramsProductos.put("p_IdEmpleado", entrada.p_IdEmpleado);
		paramsProductos.put("p_IdTipoOperacion", listaTipoOperacion);
		for(ObjValidaProducto res : resEmpleadoUsuario){
			if(res.ValidaProducto.equals("N")) {
				for (InSolicitudes solicitud : entrada.p_SolicitudesAporteRescate) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"ERROR_AL_ENCONTRAR_ID_USUARIO",params, con, entrada.p_IdUsuario));
				}
			}
		}
		logger.info("FIN::ServicioCumpleRequisitosCrearSolicitudAporteRescate");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		//		String json = "{\"p_IdEmpresa\":1,\"p_SolicitudesAporteRescate\":[{\"IdInterno\":1,\"IdCliente\":130826,\"IdPortafolio\":34993,\"IdSerieFondo\":\"ASDASASADA\",\"IdTipoMovimientoFondoMutuo\":\"APONORM\",\"IdEstadoSerie\":\"ACT\",\"Monto\":5121,\"NumeroCuotas\":5121,\"EstadoFondo\":\"C\",\"EstadoBloqueo\":\"N\",\"EsFondoValido\":\"S\",\"EsFechaValidaAporte\":\"S\",\"EsFechaValidaRescate\":\"S\",\"EsDiferido\":\"S\",\"EsMontoMaximo\":\"S\",\"EsMontoMinimo\":\"S\",\"ValorCuota\":1.00000000,\"Overnight\":\"N\",\"EstadoLiquidez\":\"C\",\"EsMoneyMarket\":\"S\",\"EsFamiliaEquivalente\":\"S\",\"EsPortafolioRegimenValido\":\"N\",\"EsRegimenValido\":\"S\",\"PermiteAporte\":\"S\",\"PermiteRescateMandato\":\"S\",\"PermiteCambioGrupo\":\"S\",\"EsRegimenDestinoValido\":\"S\",\"EsPortafolioValido\":\"S\",\"EsCuentaInversionValido\":\"S\",\"IdTipoOperacion\":\"APORFM\",\"IdTipoProducto\":\"INFM\"}],\"p_Portafolios\":[{\"IdInterno\":1,\"IdPortafolio\":34993,\"IdPortafolioReal\":34993,\"IdEstadoPortafolio\":\"ACT\",\"IdCliente\":130826,\"IdClienteReal\":130826,\"IdEstadoCliente\":\"ACT\",\"IdClienteDestino\":0,\"IdClienteRealDestino\":0,\"IdPortafolioDestino\":34993,\"IdPortafolioRealDestino\":0,\"IdEstadoClienteDestino\":\"\",\"IdEstadoPortafolioDestino\":\"\",\"DocumentoIdentidad\":\"15772424-K\",\"IdEmpleadoPortafolio\":7298}]}";
		String json = "{\"p_IdEmpresa\":1,\"p_SolicitudesAporteRescate\":[{\"IdInterno\":1,\"IdCliente\":130826,\"IdPortafolio\":34993,\"IdSerieFondo\":\"ASDASASADA\",\"IdTipoMovimientoFondoMutuo\":\"APONORM\",\"IdEstadoSerie\":\"ACT\",\"Monto\":5121,\"NumeroCuotas\":5121,\"EstadoFondo\":\"C\",\"EstadoBloqueo\":\"N\",\"EsFondoValido\":\"S\",\"EsFechaValidaAporte\":\"S\",\"EsFechaValidaRescate\":\"S\",\"EsDiferido\":\"S\",\"EsMontoMaximo\":\"S\",\"EsMontoMinimo\":\"S\",\"ValorCuota\":1.00000000,\"Overnight\":\"N\",\"EstadoLiquidez\":\"C\",\"EsMoneyMarket\":\"S\",\"EsFamiliaEquivalente\":\"S\",\"EsPortafolioRegimenValido\":\"N\",\"EsRegimenValido\":\"S\",\"PermiteAporte\":\"S\",\"PermiteRescateMandato\":\"S\",\"PermiteCambioGrupo\":\"S\",\"EsRegimenDestinoValido\":\"S\",\"EsPortafolioValido\":\"S\",\"EsCuentaInversionValido\":\"S\",\"IdTipoOperacion\":\"APORFM\",\"IdTipoProducto\":\"INFM\"}],\"p_Portafolios\":[{\"IdInterno\":1,\"IdPortafolio\":34993,\"IdPortafolioReal\":34993,\"IdEstadoPortafolio\":\"ACT\",\"IdCliente\":130826,\"IdClienteReal\":130826,\"IdEstadoCliente\":\"ACT\",\"IdClienteDestino\":0,\"IdClienteRealDestino\":0,\"IdPortafolioDestino\":34993,\"IdPortafolioRealDestino\":0,\"IdEstadoClienteDestino\":\"\",\"IdEstadoPortafolioDestino\":\"\",\"DocumentoIdentidad\":\"15772424-K\",\"IdEmpleadoPortafolio\":7298}]}";
		
		EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate Objin = gson.fromJson(json, EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate.class);
		ServicioCumpleRequisitosCrearSolicitudAporteRescate demo=new ServicioCumpleRequisitosCrearSolicitudAporteRescate();			
		try {
			SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate ObjOut=demo.logica(con, Objin);
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