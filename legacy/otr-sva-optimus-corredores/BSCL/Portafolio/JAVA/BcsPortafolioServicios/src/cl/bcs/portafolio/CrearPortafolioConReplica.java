package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.portafolio.CrearPortafolio.CategoriaInvOperacion;
import cl.bcs.portafolio.CrearPortafolio.EmpleadoEjecutivo;
import cl.bcs.portafolio.CrearPortafolio.EntradaCrearPortafolio;
import cl.bcs.portafolio.CrearPortafolio.PortafolioLogAuditoria;
import cl.bcs.portafolio.CrearPortafolio.SalidaCrearPortafolio;
import cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo;
import cl.bcs.socionegocio.por.CrearPreferenciaPagoPortafolioMasivo;
import cl.bcs.socionegocio.por.pojo.CuentaCustodio;
import cl.bcs.socionegocio.por.pojo.PreferenciaPagoPortafolio;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPortafolioConReplica implements JavaCall {
	
	//Log
	private static final Logger Logger = LogManager.getLogger(CrearPortafolioConReplica.class);
	private static final Gson GSON = new Gson();
	
	public final String SQL_CONSULTAR_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPortafolio.sql");
	public final String SQL_CONSULTAR_PREFERENCIA_DE_NEGOCIOS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPreferenciasDeNegocio.sql");
	public final String SQL_CONSULTAR_INDICADORES_DESTINO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarIndicadoresDestino.sql");
	public final String SQL_CONSULTAR_PREFERENCIA_DE_PAGOS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPreferenciasDePago.sql");
	public final String SQL_CONSULTAR_CUENTA_CUSTODIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarCuentaCustodio.sql");
	public final String SQL_SELECT_CATEGORIA_INVERSIONISTA_OPERACION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarCategoriaInversionistaOperacion.sql");
	public final String SQL_SELECT_CATEGORIA_INVERSIONISTA_OPERACION_SIN_PRODUCTO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarCategoriaInversionistaOperacionSinProducto.sql");
	public final String SQL_SELECT_PERFIL_PRODUCTOS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPerfilProductos.sql");
	public final String SQL_SELECT_PERFIL_COBROS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_ConsultarPerfilCobros.sql");
	public final String SQL_SELECT_PROPOSITO_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolioConReplica_SelectPropositoPortafolio.sql");

	public final String SQL_CONSULTAR_PORTAFOLIO_LOG_AUDITORIA = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectPortafolioLogAuditoria.sql");
	private final String SQL_SELECT_EJECUTIVO_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectEjecutivoPortafolio.sql");

	public static class EntradaCrearPortafolioConReplica {
	    public Long    p_IdEmpresa;
	    public Long    p_IdUsuario;
	    public String  p_IdModulo;
	    public String  p_UsaReglasNegocio;
	    public String  p_CodigoPortafolio;
	    public String  p_DescripcionPortafolio;
	    
	    public String  p_PropositoPortafolio;
	    
	    public Long    p_IdCuentaOrigen;
	    public String  p_CodigoRegimen;
	    public String  p_NombrePlan;
	    public String  p_CodigoAgrupador;
	    public Long    p_IdCliente;
	    public String  p_IdPerfilComercialBase;
	    public Long    p_IdPersonaBrokerExtranjero;
	    public String  p_Estado;
	    public String  p_AreaCliente;
	    public String  p_DesdeEnrolamiento;

	    public String  p_IdPortafolio;
	    public String  p_ReplicaPortafolio;
	    
	    public String  p_IdPlantillaProductos = "";
	    public String  p_IdPlantillaCobros = "";
	    public Long    p_IdEjecutivoPortafolio = 0L;
	    public Long    p_IdEjecutivoComercial = 0L;

	    public EntradaCrearPortafolioConReplica() {
	        this.p_CodigoPortafolio          = "";
	        this.p_DescripcionPortafolio     = "Portafolio Default";
	        this.p_PropositoPortafolio       = "PROPGENERALES";
	        this.p_IdCuentaOrigen            = null;
	        this.p_CodigoRegimen             = "";
	        this.p_NombrePlan                = "";
	        this.p_CodigoAgrupador           = "";
	        this.p_IdPersonaBrokerExtranjero = 0L;
	        this.p_Estado                    = "ACT";
	        this.p_AreaCliente               = "";
	        this.p_DesdeEnrolamiento         = "";
	        this.p_ReplicaPortafolio     = "";
	    }

	    @Override
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	public static class DatosPortafolio {
		public Long IdPortafolio;
	    public Long IdDireccionEnvioCorrespondencia;
	    public Long IdTelefonoContacto;
	    public String IdAreaCliente;
	    public Long IdCliente;
        public String IdPlantillaProductos;
        public String IdPlantillaCobros;
        public String PropositoPortafolio;
        public Long IdEjecutivoPortafolio = 0L;
        public Long IdEjecutivoComercial = 0L;

	    
	    @Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosPreferenciaNegocio {
	    public Long   IdIndicador;
	    public Long   IdTipoIndicador;
	    public String ValorIndicador;
	    
	    @Override
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	public static class RegistroCambio {
	    public String CampoModificado;
	    public String ValorNuevo;
	    
	    public String toString() {
			return Utils.printJson(this, false);
	    }
	}

	public static class DatosPreferenciaPago {
	    public String IdPreferenciaPago;
	    public Long   IdCuentaBancaria;
	    public Long   IdTarjetaBancaria;
	    public String IdEstado;
	    public String PorDefecto;
	    public String RecibePagaPortafolio;
	    public Long   IdPersonaRelacionada;
	    public Long   IdCarga;
	    
	    @Override
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	public static class CuentaCustodioIn {
	    public String IdBoveda;
	    public String Codigo;
	    public String IdTipoCuentaCustodia;
	    public String IdTipoCustodia;
	    public String IdModeloCustodia;
	    public Long   IdPersonaBancoCustodio;
	    public Long   IdPersonaBancoLiquidador;
	    public String IdEstado;
	    public String LiquidaVencimientos;
	    public String LiquidaCupones;
	    public String LiquidaPrepagos;
	    public String LiquidaDividendos;

	    @Override
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
    
	public static class CrearPortafolioConReplica_Proceso{
        public List<DatosPreferenciaNegocio> preferenciasNegocioNuevas;
        public List <DatosPreferenciaNegocio> preferenciasNegociosAntiguas;
        public List<DatosPreferenciaPago>   preferenciasPago;
        public List<DatosPortafolio>              portafolioOrigen;
        public List<CuentaCustodioIn> cuentaCustodio;
	}
	
	public static class SalidaCrearPortafolioConReplica extends SalidaServicios {
		public Long p_out_RefId;
		public String p_out_RefMessage;
		public SalidaCrearPortafolioConReplica(){
			super();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		EntradaCrearPortafolioConReplica jin = GSON.fromJson(in.toString(), EntradaCrearPortafolioConReplica.class);
		SalidaCrearPortafolioConReplica out = logica(conn, jin);
		JsonParser jsonParser = new JsonParser();
		return jsonParser.parse(GSON.toJson(out)).getAsJsonObject();
	}
		
	private ModificarIndicador.RegistroAtributo crearRegistroAtributo(Long idIndicador, Long idTipoIndicador, String valorIndicador) {
	    ModificarIndicador.RegistroAtributo reg = new ModificarIndicador.RegistroAtributo();
	    reg.IdIndicador      = idIndicador;
	    reg.IdTipoIndicador  = idTipoIndicador;
	    reg.ValorIndicador   = valorIndicador != null ? valorIndicador : "";
	    reg.FechaVencimiento = java.sql.Timestamp.valueOf("0001-01-01 00:00:00");
	    return reg;
	}
	
    public SalidaCrearPortafolioConReplica logica(Connection con, EntradaCrearPortafolioConReplica in) throws Exception {
        Logger.info("== INICIO CREACION PORTAFOLIO CON REPLICA ==");
 
        SalidaCrearPortafolioConReplica salida = new SalidaCrearPortafolioConReplica();
        
        salida = crearPortafolio(con, in, salida);
        if (salida.P_OUT_ERRORESCONTADOR > 0) return salida;
 
        if (in.p_ReplicaPortafolio == null || in.p_ReplicaPortafolio.trim().isEmpty()) {
            Logger.info("Sin configuracion para importar, finalizando");
            salida.p_out_RefMessage = resolverMensajeFinal(con, in.p_IdEmpresa, salida);
            return salida;
        }

        CrearPortafolioConReplica_Proceso proceso = obtenerDatosOrigen(con, in, salida);
        if (proceso == null) return salida;
 
        replicarConfiguracion(con, in, proceso, salida);
        replicarPrefNegocio(con, in, proceso, salida);
        replicarCuentaCustodio(con, in, proceso, salida);
        replicarPrefPago(con, in, proceso, salida);
 
        registrarLogAuditoria(con, in, salida);
 
        salida.p_out_RefMessage = resolverMensajeFinal(con, in.p_IdEmpresa, salida);
        
        Logger.info("== FIN CREACION PORTAFOLIO CON REPLICA ==");
        return salida;
    }

	private SalidaCrearPortafolioConReplica crearPortafolio(Connection con, EntradaCrearPortafolioConReplica in, SalidaCrearPortafolioConReplica salida) throws Exception {
		Logger.info(":: Creando portafolio base ::");
		SQL sql = new SQL();

        EntradaCrearPortafolio entrada = new EntradaCrearPortafolio();
        entrada.p_IdEmpresa               = in.p_IdEmpresa;
        entrada.p_IdUsuario               = in.p_IdUsuario;
        entrada.p_IdModulo                = in.p_IdModulo;
        entrada.p_UsaReglasNegocio        = in.p_UsaReglasNegocio;
        entrada.p_CodigoPortafolio        = in.p_CodigoPortafolio;
        entrada.p_DescripcionPortafolio   = in.p_DescripcionPortafolio;
        entrada.p_IdCuentaOrigen          = in.p_IdCuentaOrigen;
        entrada.p_CodigoRegimen           = in.p_CodigoRegimen;
        entrada.p_NombrePlan              = in.p_NombrePlan;
        entrada.p_CodigoAgrupador         = in.p_CodigoAgrupador;
        entrada.p_IdCliente               = in.p_IdCliente;
        entrada.p_IdPerfilComercialBase   = in.p_IdPerfilComercialBase;
        entrada.p_IdPersonaBrokerExtranjero = in.p_IdPersonaBrokerExtranjero;
        entrada.p_Estado                  = in.p_Estado;
        entrada.p_AreaCliente             = in.p_AreaCliente;
        entrada.p_DesdeEnrolamiento       = in.p_DesdeEnrolamiento;
        
        HashMap<String,Object> params = new HashMap<String,Object> ();
        params.put("PropositoPortafolio", in.p_PropositoPortafolio);
        List<String> propositos = sql.query(con, SQL_SELECT_PROPOSITO_PORTAFOLIO, params, String.class);
        if(propositos.size() == 0) {
        	String codigoError = "PROPOSITO_PORTAFOLIO_INVALIDO";	   	
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()));
			return salida;
        }else{
			entrada.p_PropositoPortafolio = in.p_PropositoPortafolio;
		}
        
        if (in.p_IdPlantillaCobros != null && !in.p_IdPlantillaCobros.trim().isEmpty()) {
			params.clear();
			params.put("IdPerfilCobro", in.p_IdPlantillaCobros);
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<CategoriaInvOperacion> perfilCobro = sql.query(con, SQL_SELECT_PERFIL_COBROS, params, CategoriaInvOperacion.class);
			
			if(perfilCobro.size() == 0) {
				String codigoError = "PERFIL_COBROS_REPLICA_NO_EXISTE";	   	
				salida.P_OUT_ADVERTENCIASCONTADOR++;
				salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg(), Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()));
				in.p_IdPlantillaCobros = "";
			}else {
				entrada.p_IdPlantillaCobro = in.p_IdPlantillaCobros;
			}
		}
        
        //Si las categorias de inversion del perfil comercial base y la plantilla de productos coinciden puede continuar la operacion
        List<CategoriaInvOperacion> catInvOperacion = new ArrayList<CategoriaInvOperacion>();
		HashMap<String, Object> sqlInOperacion = new HashMap<String,Object>();                        
		sqlInOperacion.put("IdPerfilComercialBase", in.p_IdPerfilComercialBase);
		sqlInOperacion.put("IdCliente", in.p_IdCliente);
		sqlInOperacion.put("IdEmpresa", in.p_IdEmpresa);

		if (in.p_IdPlantillaProductos != null && !in.p_IdPlantillaProductos.trim().isEmpty()) {
			params.clear();
			params.put("IdPerfilProducto", in.p_IdPlantillaProductos);
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<CategoriaInvOperacion> perfilProducto = sql.query(con, SQL_SELECT_PERFIL_PRODUCTOS, params, CategoriaInvOperacion.class);
			
			if(perfilProducto.size() == 0 ) {
				String codigoError = "PERFIL_PRODUCTOS_REPLICA_NO_EXISTE";	   	
				salida.P_OUT_ADVERTENCIASCONTADOR++;
				salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg(), Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()));
				in.p_IdPlantillaProductos = "";
			}else {
				sqlInOperacion.put("IdPlantillaProductos", in.p_IdPlantillaProductos);
				catInvOperacion = sql.query(con, SQL_SELECT_CATEGORIA_INVERSIONISTA_OPERACION , sqlInOperacion, CategoriaInvOperacion.class);
				CategoriaInvOperacion categorias = catInvOperacion.get(0);
				
				if ((categorias != null) && !(categorias.CategoriaInvCliente.equals(categorias.CategoriaInvComercial) && categorias.CategoriaInvComercial.equals(categorias.CategoriaInvProducto)) ) {
				    String codigoError = "CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN";	   	
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()));
				    return salida;
			    }
				entrada.p_IdPlantillaProducto = in.p_IdPlantillaProductos;
			}
	    }else {
	    	catInvOperacion = sql.query(con, SQL_SELECT_CATEGORIA_INVERSIONISTA_OPERACION_SIN_PRODUCTO , sqlInOperacion, CategoriaInvOperacion.class);
	    	CategoriaInvOperacion categorias = catInvOperacion.get(0);
			
			if ((categorias != null) && !(categorias.CategoriaInvCliente.equals(categorias.CategoriaInvComercial)) ) {
			    String codigoError = "CATEGORIAS_INVERSIONISTA_REPLICA_NO_COINCIDEN";	   	
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()));
			    return salida;
		    }
	    }

        
        if (in.p_IdEjecutivoPortafolio > 0L) entrada.p_IdEjecutivoPortafolio = in.p_IdEjecutivoPortafolio;
        
        if (in.p_IdEjecutivoComercial > 0L ) entrada.p_IdEjecutivoComercial = in.p_IdEjecutivoComercial;

		SalidaCrearPortafolio salidaCrear = new CrearPortafolio().logica(con, entrada);
		
		Logger.info("Resultado crear portafolio: {}", salidaCrear);
		 if (salidaCrear.P_OUT_ERRORESCONTADOR > 0) {
            Logger.error("[ReplicarPortafolio] Fallo la creacion del portafolio");
            salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
            salida.P_OUT_ERRORESDETALLE.addAll(salidaCrear.P_OUT_ERRORESDETALLE);
            return salida;

        }
		if (salidaCrear.P_OUT_ADVERTENCIASCONTADOR > 0) {
            salida.P_OUT_ADVERTENCIASCONTADOR += salidaCrear.P_OUT_ADVERTENCIASCONTADOR;
            salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaCrear.P_OUT_ADVERTENCIASDETALLE);
		}
	 
        salida.p_out_RefId      = salidaCrear.p_out_RefId;
        salida.p_out_RefMessage = "Portafolio Creado";
        in.p_IdPortafolio       = String.valueOf(salidaCrear.p_out_RefId);
        Logger.info("Portafolio nuevo creado con ID: {}", in.p_IdPortafolio);
		return salida;
	}
	
	private CrearPortafolioConReplica_Proceso obtenerDatosOrigen(Connection con, EntradaCrearPortafolioConReplica in, SalidaCrearPortafolioConReplica salida) throws Exception {
	    Logger.info(":: INICIO OBTENCION DE DATOS PORTAFOLIO A COPIAR ::");
	    SQL sql = new SQL();
	    CrearPortafolioConReplica_Proceso proceso = new CrearPortafolioConReplica_Proceso();

	    HashMap<String, Object> paramsPortafolio = new HashMap<>();
	    paramsPortafolio.put("CodigoPortafolio", in.p_ReplicaPortafolio.trim());
	    paramsPortafolio.put("IdCliente",        in.p_IdCliente);
	    proceso.portafolioOrigen = sql.query(con, SQL_CONSULTAR_PORTAFOLIO, paramsPortafolio, DatosPortafolio.class);
	    Logger.info("Portafolio origen: {}", proceso.portafolioOrigen);

	    if (proceso.portafolioOrigen == null || proceso.portafolioOrigen.isEmpty()) {
	        Logger.warn("No se encontro portafolio con codigo {} para cliente {}", in.p_ReplicaPortafolio, in.p_IdCliente);
	        addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "PORTAFOLIO_NO_ENCONTRADO", null);
	        return null;
	    }

	    Long idOrigen = proceso.portafolioOrigen.get(0).IdPortafolio;
	    Logger.info("IdPortafolio origen resuelto: {}", idOrigen);

	    HashMap<String, Object> paramsNegocioOld = new HashMap<>();
	    paramsNegocioOld.put("IdEntidad", idOrigen);
	    paramsNegocioOld.put("IdEmpresa", in.p_IdEmpresa);
	    proceso.preferenciasNegociosAntiguas = sql.query(con, SQL_CONSULTAR_PREFERENCIA_DE_NEGOCIOS, paramsNegocioOld, DatosPreferenciaNegocio.class);
	    Logger.info("Preferencias negocio antiguas: {}", proceso.preferenciasNegociosAntiguas);

	    HashMap<String, Object> paramsNegocioNew = new HashMap<>();
	    paramsNegocioNew.put("IdEntidad", in.p_IdPortafolio);
	    paramsNegocioNew.put("IdEmpresa", in.p_IdEmpresa);
	    proceso.preferenciasNegocioNuevas = sql.query(con, SQL_CONSULTAR_INDICADORES_DESTINO, paramsNegocioNew, DatosPreferenciaNegocio.class);
	    Logger.info("Preferencias negocio nuevas: {}", proceso.preferenciasNegocioNuevas);

	    HashMap<String, Object> paramsPago = new HashMap<>();
	    paramsPago.put("IdPortafolio", idOrigen);
	    proceso.preferenciasPago = sql.query(con, SQL_CONSULTAR_PREFERENCIA_DE_PAGOS, paramsPago, DatosPreferenciaPago.class);
	    Logger.info("Preferencias pago: {}", proceso.preferenciasPago);

	    HashMap<String, Object> paramsCustodio = new HashMap<>();
	    paramsCustodio.put("IdPortafolio", idOrigen);
	    proceso.cuentaCustodio = sql.query(con, SQL_CONSULTAR_CUENTA_CUSTODIO, paramsCustodio, CuentaCustodioIn.class);
	    Logger.info("Cuentas Custodio: {}", proceso.cuentaCustodio);

	    Logger.info(":: FIN OBTENCION DE DATOS PORTAFOLIO A COPIAR ::");
	    return proceso;
	}
	
	private void replicarConfiguracion(Connection con, EntradaCrearPortafolioConReplica in, CrearPortafolioConReplica_Proceso proceso, SalidaCrearPortafolioConReplica salida) throws SQLException {
	    Logger.info(":: Replicando configuracion general ::");
	    DatosPortafolio portafolio = proceso.portafolioOrigen.get(0);

	    List<ModificarPortafolio.RegistroCambio> cambios = new ArrayList<>();
	    cambios.add(new ModificarPortafolio.RegistroCambio("IdDireccionEnvioCorrespondencia", portafolio.IdDireccionEnvioCorrespondencia != null ? portafolio.IdDireccionEnvioCorrespondencia.toString() : ""));
	    cambios.add(new ModificarPortafolio.RegistroCambio("IdTelefonoContacto",              portafolio.IdTelefonoContacto              != null ? portafolio.IdTelefonoContacto.toString()              : ""));
	    cambios.add(new ModificarPortafolio.RegistroCambio("AreaCliente",                     portafolio.IdAreaCliente                   != null ? portafolio.IdAreaCliente                              : ""));
	    
	    //Si llegan campos entonces los toma como prioridad sino toma los datos del portafolio a replicar
	    if (in.p_IdPlantillaProductos != null && !in.p_IdPlantillaProductos.trim().isEmpty()) cambios.add(new ModificarPortafolio.RegistroCambio("IdPerfilProductos",in.p_IdPlantillaProductos));
	    else cambios.add(new ModificarPortafolio.RegistroCambio("IdPerfilProductos",portafolio.IdPlantillaProductos)); 
	    
	    if (in.p_IdPlantillaCobros != null && !in.p_IdPlantillaCobros.trim().isEmpty()) cambios.add(new ModificarPortafolio.RegistroCambio("IdPerfilCobros",in.p_IdPlantillaCobros));
	    else cambios.add(new ModificarPortafolio.RegistroCambio("IdPerfilCobros",portafolio.IdPlantillaCobros)); 
	    
	    HashMap<String,Object> params = new HashMap<String,Object>();
	    params.put("IdEmpleadoEjecutivo", in.p_IdEjecutivoComercial);
	    SQL sql = new SQL();
	    List<String> ejecutivoComercial = sql.query(con, SQL_SELECT_EJECUTIVO_PORTAFOLIO, params, String.class);
	    if (in.p_IdEjecutivoComercial != null && !ejecutivoComercial.isEmpty()) cambios.add(new ModificarPortafolio.RegistroCambio("IdEmpleadoEjecutivoComercial",in.p_IdEjecutivoComercial.toString()));
	    else cambios.add(new ModificarPortafolio.RegistroCambio("IdEjecutivoComercial",portafolio.IdEjecutivoComercial.toString())); 
	   
	    params.clear();
	    params.put("IdEmpleadoEjecutivo", in.p_IdEjecutivoPortafolio);
	    List<String> ejecutivoPortafolio = sql.query(con, SQL_SELECT_EJECUTIVO_PORTAFOLIO, params, String.class);
	    if (in.p_IdEjecutivoPortafolio != null && !ejecutivoPortafolio.isEmpty()) cambios.add(new ModificarPortafolio.RegistroCambio("IdEmpleadoEjecutivo",in.p_IdEjecutivoPortafolio.toString()));
	    else cambios.add(new ModificarPortafolio.RegistroCambio("IdEjecutivoPortafolio",portafolio.IdEjecutivoPortafolio.toString())); 
	    
	    if (in.p_PropositoPortafolio != null && !in.p_PropositoPortafolio.trim().isEmpty()) cambios.add(new ModificarPortafolio.RegistroCambio("Proposito",in.p_PropositoPortafolio));
	    else cambios.add(new ModificarPortafolio.RegistroCambio("Proposito",portafolio.PropositoPortafolio)); 
	    
	    ModificarPortafolio modificarPortafolio = new ModificarPortafolio();
	    ModificarPortafolio.Entrada entrada = new ModificarPortafolio.Entrada();
	    entrada.p_IdEmpresa        = in.p_IdEmpresa;
	    entrada.p_IdUsuario        = in.p_IdUsuario;
	    entrada.p_IdCliente        = in.p_IdCliente;
	    entrada.p_IdModulo         = in.p_IdModulo;
	    entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
	    entrada.p_IdPortafolio     = Long.parseLong(in.p_IdPortafolio);
	    
	    entrada.p_RegistroCambio   = cambios;

	    try {
	        ModificarPortafolio.Salida resultado = modificarPortafolio.logica(con, entrada);
	        if (resultado.P_OUT_ERRORESCONTADOR > 0) {
	            Logger.error("[ReplicarPortafolio] Fallo configuracion general: {}", resultado.P_OUT_ERRORESDETALLE);
	            addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "configuracion general");
	        }
	    } catch (Exception e) {
	        Logger.error("[ReplicarPortafolio] Fallo configuracion general", e);
	        addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "configuracion general");
	    }
	}
	 
	private void replicarPrefNegocio(Connection con, EntradaCrearPortafolioConReplica in, CrearPortafolioConReplica_Proceso proceso, SalidaCrearPortafolioConReplica salida) {
        if (proceso.preferenciasNegociosAntiguas == null || proceso.preferenciasNegociosAntiguas.isEmpty()) {
            Logger.info("Sin preferencias de negocio que replicar");
            return;
        }
        Logger.info(":: Replicando preferencias de negocio ::");
 
        ModificarIndicador modificarIndicador = new ModificarIndicador();
        ModificarIndicador.EntradaModificarIndicador entrada = new ModificarIndicador.EntradaModificarIndicador();
        entrada.p_IdEmpresa        = in.p_IdEmpresa;
        entrada.p_IdUsuario        = in.p_IdUsuario;
        entrada.p_IdModulo         = in.p_IdModulo;
        entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
        entrada.p_IdTipoEntidad    = "PORTAFOLIO";
        entrada.p_IdEntidad        = Long.parseLong(in.p_IdPortafolio);
        entrada.p_ModeloCliente    = "";
 
        Map<Long, Long> mapaIndicadoresDestino = new HashMap<>();
        if (proceso.preferenciasNegocioNuevas != null) {
            for (DatosPreferenciaNegocio ind : proceso.preferenciasNegocioNuevas) {
                mapaIndicadoresDestino.put(ind.IdTipoIndicador, ind.IdIndicador);
            }
        }
 
        for (DatosPreferenciaNegocio ref : proceso.preferenciasNegociosAntiguas) {
            entrada.p_RegistroAtributo.add(
                crearRegistroAtributo(
                    mapaIndicadoresDestino.getOrDefault(ref.IdTipoIndicador, 0L),
                    ref.IdTipoIndicador,
                    ref.ValorIndicador
                )
            );
        }
 
        try {
            ModificarIndicador.SalidaModificarIndicador salida2 = modificarIndicador.logica(con, entrada);
            if (salida2.P_OUT_ERRORESCONTADOR > 0) {
                Logger.error("[ReplicarPortafolio] Errores en preferencias de negocio: {}", salida2.P_OUT_MENSAJES);
                addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "preferencias de negocio");
            }
        } catch (Exception e) {
            Logger.error("[ReplicarPortafolio] Fallo preferencias de negocio", e);
            addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "preferencias de negocio");
        }
    }
	 
	 private void replicarCuentaCustodio(Connection con, EntradaCrearPortafolioConReplica in,
		        CrearPortafolioConReplica_Proceso proceso, SalidaCrearPortafolioConReplica salida) throws Exception {
		    if (proceso.cuentaCustodio == null || proceso.cuentaCustodio.isEmpty()) {
		        Logger.info("Sin cuentas custodio que replicar");
		        return;
		    }
		    Logger.info(":: Replicando cuentas custodio ::");

		    CrearCuentaCustodioMasivo.EntradaCrearCuentaCustodioMasivo entrada =
		            new CrearCuentaCustodioMasivo.EntradaCrearCuentaCustodioMasivo();
		    entrada.p_IdEmpresa        = in.p_IdEmpresa;
		    entrada.p_IdUsuario        = in.p_IdUsuario;
		    entrada.p_IdModulo         = in.p_IdModulo;
		    entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;

		    for (CuentaCustodioIn custodio : proceso.cuentaCustodio) {
		        CuentaCustodio cc = new CuentaCustodio();
		        cc.IdPortafolio            = Long.parseLong(in.p_IdPortafolio);
		        cc.IdBoveda                = custodio.IdBoveda;
		        cc.Codigo                  = custodio.Codigo;
		        cc.IdTipoCuentaCustodia    = custodio.IdTipoCuentaCustodia;
		        cc.IdTipoCustodia          = custodio.IdTipoCustodia          != null ? custodio.IdTipoCustodia          : "";
		        cc.IdModeloCustodia        = custodio.IdModeloCustodia         != null ? custodio.IdModeloCustodia         : "";
		        cc.IdPersonaBancoCustodio  = custodio.IdPersonaBancoCustodio  != null ? custodio.IdPersonaBancoCustodio  : 0L;
		        cc.IdPersonaBancoLiquidador= custodio.IdPersonaBancoLiquidador != null ? custodio.IdPersonaBancoLiquidador : 0L;
		        cc.IdEstado                = custodio.IdEstado;
		        cc.LiquidaDividendos       = custodio.LiquidaDividendos;
		        cc.LiquidaCupones          = custodio.LiquidaCupones;
		        cc.LiquidaVencimientos     = custodio.LiquidaVencimientos;
		        cc.LiquidaPrepagos         = custodio.LiquidaPrepagos;
		        entrada.p_CuentasCustodio.add(cc);
		    }

		    try {
		        CrearCuentaCustodioMasivo crearCustodio = new CrearCuentaCustodioMasivo();
		        CrearCuentaCustodioMasivo.SalidaCrearCuentaCustodioMasivo resultado =
		                crearCustodio.logica(con, entrada);

		        if (resultado.P_OUT_ERRORESCONTADOR > 0) {
		            Logger.error("[ReplicarPortafolio] Fallo cuentas custodio: {}", resultado.P_OUT_MENSAJES);
		            addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "cuentas custodio");
		        }
		    } catch (Exception e) {
		        Logger.error("[ReplicarPortafolio] Fallo cuentas custodio", e);
		        addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "cuentas custodio");
		    }
		}
	
	private void replicarPrefPago(Connection con, EntradaCrearPortafolioConReplica in, CrearPortafolioConReplica_Proceso proceso, SalidaCrearPortafolioConReplica salida) throws Exception {
	    if (proceso.preferenciasPago == null || proceso.preferenciasPago.isEmpty()) {
	        Logger.info("Sin preferencias de pago que replicar");
	        return;
	    }
	    Logger.info(":: Replicando preferencias de pago ::");

	    CrearPreferenciaPagoPortafolioMasivo.EntradaCrearPreferenciaPagoPortafolioMasivo entrada =
	            new CrearPreferenciaPagoPortafolioMasivo.EntradaCrearPreferenciaPagoPortafolioMasivo();
	    entrada.p_IdEmpresa        = in.p_IdEmpresa;
	    entrada.p_IdUsuario        = in.p_IdUsuario;
	    entrada.p_IdModulo         = in.p_IdModulo;
	    entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;

	    for (DatosPreferenciaPago pago : proceso.preferenciasPago) {
	        PreferenciaPagoPortafolio pref = new PreferenciaPagoPortafolio();
	        pref.IdPortafolio         = Long.parseLong(in.p_IdPortafolio);
	        pref.IdPreferenciaPago    = pago.IdPreferenciaPago;
	        pref.IdCuentaBancaria     = pago.IdCuentaBancaria  != null ? pago.IdCuentaBancaria  : 0L;
	        pref.IdTarjetaBancaria    = pago.IdTarjetaBancaria != null ? pago.IdTarjetaBancaria : 0L;
	        pref.IdEstado             = pago.IdEstado;
	        pref.PorDefecto           = pago.PorDefecto;
	        pref.IdPersonaRelacionada = pago.IdPersonaRelacionada != null ? pago.IdPersonaRelacionada : 0L;
	        pref.IdCarga              = pago.IdCarga != null ? pago.IdCarga : 0L;
	        entrada.p_PreferenciasPagoPortafolio.add(pref);
	    }

	    try {
	        CrearPreferenciaPagoPortafolioMasivo crearPrefPago = new CrearPreferenciaPagoPortafolioMasivo();
	        CrearPreferenciaPagoPortafolioMasivo.SalidaCrearPreferenciaPagoPortafolioMasivo resultado =
	                crearPrefPago.logica(con, entrada);

	        if (resultado.P_OUT_ERRORESCONTADOR > 0) {
	            Logger.error("[ReplicarPortafolio] Fallo preferencias de pago: {}", resultado.P_OUT_MENSAJES);
	            addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "preferencias de pago");
	        }
	    } catch (Exception e) {
	        Logger.error("[ReplicarPortafolio] Fallo preferencias de pago", e);
	        addAdvertenciaDiccionario(con, salida, in.p_IdEmpresa, "REPLICAR_PORTAFOLIO_PARCIAL", "preferencias de pago");
	    }
	}
	
	 private void registrarLogAuditoria(Connection con, EntradaCrearPortafolioConReplica in, SalidaCrearPortafolioConReplica salida) throws Exception {
        Logger.info(":: Registrando log de auditoria ::");
        SQL sql = new SQL();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", in.p_IdPortafolio);
 
        List<PortafolioLogAuditoria> portLogAuditoria;
        try {
            portLogAuditoria = sql.query(con, SQL_CONSULTAR_PORTAFOLIO_LOG_AUDITORIA, params, PortafolioLogAuditoria.class);
        } catch (Exception e) {
            Logger.error("[ReplicarPortafolio] Fallo al consultar datos de auditoria", e);
            return;
        }
 
        if (portLogAuditoria == null || portLogAuditoria.isEmpty()) {
            Logger.warn("[ReplicarPortafolio] No se encontraron datos de auditoria para el portafolio {}", in.p_IdPortafolio);
            return;
        }
 
        InAgregarLog agregarLogEntrada = new InAgregarLog();
 
        Log logReplicar = new Log();
        logReplicar.IdCabeceraLog  = Long.parseLong(in.p_IdPortafolio);
        logReplicar.IdTipoLog      = "CREACION";
        logReplicar.IdModulo       = in.p_IdModulo;
        logReplicar.IdUsuario      = in.p_IdUsuario;
        logReplicar.IdEmpresa      = in.p_IdEmpresa;
        logReplicar.IdCliente      = in.p_IdCliente;
        logReplicar.NombreCliente  = portLogAuditoria.get(0).NombreCompleto;
        logReplicar.IdTipoRegistro = "REPLICA_PORTAFOLIO";
        logReplicar.NombreProceso  = "Replica Portafolio: Prefencia Pago, Negocio, Datos Generales y Custodio";
        logReplicar.FechaSistemaLog = fechaSistema;
        logReplicar.IdUsuarioAtendido = in.p_IdUsuario;
        agregarLogEntrada.p_Log.add(logReplicar);
        agregarLogEntrada.agregarDetalleLogBasico(
            Long.parseLong(in.p_IdPortafolio),
            0L,
            "IdPortafolioReplicado",
            in.p_ReplicaPortafolio,
            in.p_IdPortafolio,
            "PORTAFOLIO"
        );
 
        try {
            OutAgregarLog agregarLogSalida = new AgregarLog().logica(con, agregarLogEntrada);
            if (agregarLogSalida != null && agregarLogSalida.P_OUT_ERRORESCONTADOR > 0) {
                Logger.warn("[ReplicarPortafolio] El log no se registro correctamente");
            }
        } catch (Exception e) {
            Logger.error("[ReplicarPortafolio] Fallo al registrar log", e);
        }
    }
	
	 private String resolverMensajeFinal(Connection con, Long idEmpresa, SalidaCrearPortafolioConReplica salida) {
		    try {
		        if (salida.P_OUT_ERRORESCONTADOR > 0) {
		            return Diccionario.getMensaje(con, idEmpresa, "PORTAFOLIO_CREADO_CON_ERRORES", new HashMap<>());
		        }
		        if (salida.P_OUT_ADVERTENCIASCONTADOR > 0) {
		            return Diccionario.getMensaje(con, idEmpresa, "PORTAFOLIO_CREADO_CON_ADVERTENCIAS", new HashMap<>());
		        }
		        return Diccionario.getMensaje(con, idEmpresa, "PORTAFOLIO_CREADO_OK", new HashMap<>());
		    } catch (Exception e) {
		        Logger.error("Error al obtener mensaje final del diccionario", e);
		        if (salida.P_OUT_ERRORESCONTADOR > 0) return "Portafolio creado con errores en la replicacion";
		        if (salida.P_OUT_ADVERTENCIASCONTADOR > 0) return "Portafolio creado con advertencias en la replicacion";
		        return "Portafolio creado correctamente con replicacion completa";
		    }
	}
	
	private void addAdvertenciaDiccionario(Connection con, SalidaCrearPortafolioConReplica salida, Long idEmpresa, String codigo, String seccion) {
	    try {
	        HashMap<String, String> params = new HashMap<>();
	        if (seccion != null) params.put("Seccion", seccion);
	        List<PlataformaUtils.MensajeSalida> mensajes = new ArrayList<>();
	        mensajes.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, codigo, params, con, idEmpresa));
	        salida.parseMensajeSalida(mensajes);
	    } catch (Exception e) {
	        Logger.error("Error al obtener mensaje de diccionario para codigo: {}", codigo, e);
	        salida.addAdvertencia(codigo, codigo);
	    }
	}

	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		 try (Connection conn = PlataformaUtils.getDESAConnection()) {
	            Gson gson = Utils.getGsonBuilder();
				System.out.println("== BEGIN ==");
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":96,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoPortafolio\":\"0-Duplicado-47\",\"p_DescripcionPortafolio\":\"Tercera prueba\",\"p_PropositoPortafolio\":\"PROPGENERALES\",\"p_DesdeEnrolamiento\":\"N\",\"p_CodigoRegimen\":\"\",\"p_NombrePlan\":\"\",\"p_CodigoAgrupador\":\"\",\"p_IdCliente\":7086,\"p_IdPerfilComercialBase\":\"MC ARRIESGADO\", \"p_IdPlantillaCobros\":\"EXT_7059\", \"p_IdPlantillaProductos\":\"ROBOT PRODUCTOS ARRIESGADO\" , \"p_IdEjecutivoPortafolio\":\"1\" ,\"p_IdEjecutivoComercial\":\"1\",\"p_ReplicaPortafolio\":\"0\"}";
				EntradaCrearPortafolioConReplica objIn = GSON.fromJson(json, EntradaCrearPortafolioConReplica.class);
				CrearPortafolioConReplica replicarPortafolio = new CrearPortafolioConReplica();
				SalidaCrearPortafolioConReplica objOut = replicarPortafolio.logica(conn, objIn);
				System.out.println(objOut);
				//conn.rollback();
				conn.commit();
	        } catch (SQLException throwables) {
	            throwables.printStackTrace();
	        }

		System.out.println("== END ==");
	}

}