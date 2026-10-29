package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CertificarCargoAbono.Detalle;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;
import cl.bcs.socionegocio.por.ModificarIdEjecutivoComercial.PortafolioModificarIdEmpleado;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPortafolio implements JavaCall {
	
	// Log
	private static final Logger logger = LogManager.getLogger(CrearPortafolio.class);
	
	public final String SQL_CONSULTAR_SOCIO_NEGOCIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectConfiguracionSocioNegocio.sql");
	public final String SQL_CONSULTAR_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectDatosPortafolio.sql");
	public final String SQL_CONSULTAR_PROPOSITO_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectPropositoPortafolio.sql");
	public final String SQL_CONSULTAR_MATRIZ_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectMatriz.sql");
	public final String SQL_CONSULTAR_MATRIZ_AGRUPADOR = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectMatrizAgrupador.sql");
	public final String SQL_CONSULTAR_PLAN_CONFIGURACION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectConfiguracion.sql");
	public final String SQL_CONSULTAR_REGIMEN_TRIBUTARIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectRegimenTributario.sql");
	public final String SQL_CONSULTAR_PERFIL_COMERCIAL_BASE = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectPerfilComercialBase.sql");
	public final String SQL_CONSULTAR_EMPLEADO_EJECUTIVO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectEmpleadoEjecutivo.sql");
	public final String SQL_CONSULTAR_CUENTA_BANCO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectIdCuentaBanco.sql");
	public final String SQL_CONSULTAR_TIPO_CLIENTE = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectIdTipoCliente.sql");
	public final String SQL_CONSULTAR_GLOSA_AUTORIZACION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectGlosaAutorizacion.sql");
	public final String SQL_CONSULTAR_PORTAFOLIO_LOG_AUDITORIA = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectPortafolioLogAuditoria.sql");
	
	public final String SQL_INSERT_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertPortafolio.sql");
	public final String SQL_INSERT_PORTAFOLIO_INDICADOR_PREFERENCIAS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertIndicadorPreferenciasPortafolio.sql");
	public final String SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_FISICA = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertIndicadorDireccionFisica.sql");
	public final String SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_NOTIFICACION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertIndicadorDireccionElectronicaNotificacion.sql");
	public final String SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_FACTURACION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertIndicadorDireccionElectronicaFacturacion.sql");
	public final String SQL_INSERT_PORTAFOLIO_INDICADOR_OPERA_CUENTA_INVERSION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertIndicadorOperaCuentaInversion.sql");
	public final String SQL_INSERT_PORTAFOLIO_PERFIL_COBROS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertPerfilCobros.sql");
	public final String SQL_INSERT_PORTAFOLIO_PERFIL_COBROS_COM_2 = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertPerfilCobrosCOM_2.sql");
	public final String SQL_INSERT_PORTAFOLIO_PERFIL_PRODUCTOS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertPerfilProductos.sql");
	public final String SQL_INSERT_PORTAFOLIO_PLANILLA_LIMITE = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertPlanillaLimite.sql");
	public final String SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertPreferenciaPago.sql");
	public final String SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO_TIPO_CLIENTE = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertPreferenciaPagoTipoCliente.sql");
	public final String SQL_INSERT_PORTAFOLIO_LOG_GENERAR_CONTRATOS = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertLogGenerarContratos.sql");
	public final String SQL_INSERT_PORTAFOLIO_GLOB_CTA_INVERSION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertTempCuentaInversion.sql");	
	public final String SQL_INSERT_PORTAFOLIO_GLOB_DETALLE_LOG = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertTempDetalleLog.sql");	
	
	public final String SQL_CALL_CREAR_CUENTA_INVERSION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_CallCrearCuentaInversion.sql");
	public final String SQL_CALL_CREAR_EVENTO_AUTORIZACION = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_CallCrearEventoAutorizacion.sql");
	public final String SQL_CALL_CONFIRMAR_CREAR_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_CallConfirmarCrearPortafolio.sql");
	public final String SQL_CALL_REPLICA_PORTAFOLIO_APV = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_CallReplicaPortafolioAPV.sql");
	public final String SQL_CALL_AGREGAR_LOG = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_CallAgregarLog.sql");
	
	public final String SQL_UPDATE_PORTAFOLIO_DEFECTO_CLIENTE = "UPDATE \"BSCL.SocioNegocio.BD::CLI.Cliente\" SET \"IdPortafolioDefecto\" = :IdPortafolio WHERE \"IdCliente\" = :IdCliente;";
	public final String SQL_SELECT_COUNT_PORTAFOLIO = "SELECT (\"IdPortafolio\") FROM \"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"CodigoPortafolio\" = :CodigoPortafolio AND \"IdCliente\" = :IdCliente;";
	public final String SQL_SELECT_COUNT_PORTAFOLIOS_VIGENTES = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"IdCliente\" = :IdCliente;";
	
	private final String SQL_SELECT_EJECUTIVO_COMERCIAL = loadResourceToString("cl/bcs/portafolio/sql/ModificarIdEjecutivoComercial_SelectEmpleado.sql");
	private final String SQL_SELECT_EJECUTIVO_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectEjecutivoPortafolio.sql");

	public static class EntradaCrearPortafolio {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		public String 	p_CodigoPortafolio;
		public String 	p_DescripcionPortafolio;
		public String 	p_PropositoPortafolio;
		public Long 	p_IdCuentaOrigen;
		public String 	p_CodigoRegimen;
		public String 	p_NombrePlan;
		public String 	p_CodigoAgrupador;
		public Long 	p_IdCliente;
		public String 	p_IdPerfilComercialBase;
		public Long 	p_IdPersonaBrokerExtranjero;
		public String 	p_Estado;
		public String 	p_AreaCliente;
		public String 	p_DesdeEnrolamiento;
		
		public String  p_IdPlantillaProducto;
	    public String  p_IdPlantillaCobro;
	    public Long    p_IdEjecutivoPortafolio = 0L;
		public Long    p_IdEjecutivoComercial = 0L;

		public EntradaCrearPortafolio() {
			this.p_CodigoPortafolio = "";
			this.p_DescripcionPortafolio = "Portafolio Default";
			this.p_PropositoPortafolio = "PROPGENERALES";
			this.p_IdCuentaOrigen = null;
			this.p_CodigoRegimen = "";
			this.p_NombrePlan = "";
			this.p_CodigoAgrupador = "";
			this.p_IdPersonaBrokerExtranjero = 0L;
			this.p_Estado = "ACT";
			this.p_AreaCliente = "";
			this.p_DesdeEnrolamiento = "";
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearPortafolio_Proceso{
		public String CodigoPortafolio;             		// NVARCHAR(100):= p_CodigoPortafolio;
        public Long IdPortafolio;                  			// BIGINT:=0;
        public java.sql.Date FechaIngreso;          		// SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
        public String IdConcepto;                    		// NVARCHAR(6):='NUEPOR';
        public String RequiereAutorizacion;         		// NVARCHAR(1):='';
        public String RequiereAutOrigen;            		// NVARCHAR(1):='';
        public String IdEstadoAutorizacion;         		// NVARCHAR(3):='PEN';
        
        public Long p_out_RefId;                     		// BIGINT:=0;
        
        public Long countPortafolio;               			// INT:=0;
        public Long countPerfilComercial;         			// INT:=0;
        public Long countPortafoliosVigentes;      			// INT:=0;
        public Long IdEmpleadoEjecutivo;           			// BIGINT;
        public Long countPlanAhorro;               			// INT:=0;
        
        public Long countIdCuentaBanco;            			// INT:=0;
        public Long IdCuentaBanco;            	   			// BIGINT;
        
        // CUENTA DE INVERSION PESO
        public String MonedaSistema;                 		// NVARCHAR(3);
        public String CreaCuentaInversionEUR;        		// NVARCHAR(520);
        public Long refCtaInvPesoId;           				// BIGINT; -- CUENTA INVERSION PORTAFOLIO
        
        public String IdPerfilProductos;                 	//NVARCHAR(40);
        public String IdPerfilCobros;                    	//NVARCHAR(40);   
        public String IdPerfilCobrosPeru;                	//NVARCHAR(40);   
        public String IdModeloFacturacion;               	//NVARCHAR(40);
        public String IdPerfilLimites;                   	//NVARCHAR(40);
        public String IdPerfilCondicionesOperativas;     	//NVARCHAR(40);
        
        public String IdPlantillaExtra;                  	//NVARCHAR(40);
        public String ValCreaPrefPagoVenComTer; 			//NVARCHAR(5);
        public Long CountMatrizAgrupadorAPV;           		//INT:=0;
        public Long IdMatrizAgrupadorAPV;              		//BIGINT:=0;
        public String NombreAgrupador;                   	//NVARCHAR(100);
        public Long IdProductoAgrupador;               		//INT:=0;
        public String TablaMatrizAgrupador;               	//TABLE ("IdMatrizAgrupadorAPV" BIGINT);
        public Long CountEstadosMatrizAgrupador;        	//INT:=0;
        public Long IdPortafolioGrupoAPV;               	//INT:=0;
        public Long ExistePortafolio;                   	//INT:=0;
        public String ProductoAgrupador;                 	//NVARCHAR(100);
        public String TipoEntidadReplica;                	//NVARCHAR(50):= 'PORTAFOLIO';
        public String TipoOperacion;                     	//NVARCHAR(1):= 'U';
        public String ImpideDoblePlanDeAhorro;           	//NVARCHAR(1):= 'S';
		//public Long NroRegistros;           				//INT:=0;
        public String Alias;                             	//NVARCHAR(100):= '';
       
       //Variables Logs
        public String TipoEntidad;							//NVARCHAR(40):= 'PORTAFOLIO';
		public String subregistro;							//NVARCHAR(40):= '';
		public String NombreProceso;						//NVARCHAR(100):='Ingreso Portafolio';
		public String nombreCliente; 						//NVARCHAR(200):='';
		public String documentoIdentidad; 					//NVARCHAR(40):='';
		public String tipoDocumentoIdentidad; 				//NVARCHAR(40):='';
		public String DescripcionPortafolio;				//NVARCHAR(100):='';
		public String proposito;							//NVARCHAR(100):='';
		public String perfilComercialBase;					//NVARCHAR(200):='';
		public String estadoAutorizacion;					//NVARCHAR(40):='';
		public String estadoAutorizacionPortafolioLargo;	//NVARCHAR(40):='';
		public String glosa;
		
		//Para validaciones
		public long 				errores 		= 0;
		public long 				advertencias 	= 0;
		public long 				autorizaciones 	= 0;
		public long 				ok 				= 0;
		
		public CrearPortafolio_Proceso() {
			this.IdPortafolio = 0L;
			this.IdConcepto = "NUEPOR";
			this.RequiereAutorizacion = "";
			this.RequiereAutOrigen = "";
			this.IdEstadoAutorizacion = "PEN";
			this.p_out_RefId = 0L;
			this.countPortafolio = 0L;
			this.countPerfilComercial = 0L;
			this.countPortafoliosVigentes = 0L;
			this.countPlanAhorro = 0L;
			this.countIdCuentaBanco = 0L;
			this.CountMatrizAgrupadorAPV = 0L;
			this.IdMatrizAgrupadorAPV = 0L;
			this.IdProductoAgrupador = 0L;
			this.CountEstadosMatrizAgrupador = 0L;
			this.IdPortafolioGrupoAPV = 0L;
			this.ExistePortafolio = 0L;
			this.TipoEntidadReplica = "PORTAFOLIO";
			this.TipoOperacion = "U";
			this.ImpideDoblePlanDeAhorro = "S";
			this.Alias = "";
			this.TipoEntidad = "PORTAFOLIO";
			this.subregistro = "";
			this.NombreProceso = "Ingreso Portafolio";
			this.nombreCliente = "";
			this.documentoIdentidad = "";
			this.tipoDocumentoIdentidad = "";
			this.DescripcionPortafolio = "";
			this.proposito = "";
			this.perfilComercialBase = "";
			this.estadoAutorizacion = "";
			this.estadoAutorizacionPortafolioLargo = "";
			this.glosa = "";
			
		}
	}
	
	public static class SalidaCrearPortafolio extends SalidaServicios{
		public Long p_out_RefId;
		
		public SalidaCrearPortafolio(){
			super();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoCrearPortafolio {
		public Long      IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	
	
	public static class Configuracion {
		public String      Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	
	
	public static class Portafolio {
		public Long 	IdPortafolio;
		public String	CodigoPortafolio;
		public String	DescripcionPortafolio;
		public String	PropositoPortafolio;
		public Long 	IdCliente;
		public String  	Estado;
		public String	IdPerfilComercialBase;
		public Long		IdPersonaBrokerExtranjero;
		public String	IdPerfilProductos;
		public String	IdPlantillaExtra;
		public String	IdPerfilCobros;
		public String	IdPerfilCobrosPeru;
		public String	IdPerfilLimites;
		public String  	IdPerfilCondicionesOperativas;
		public Long		IdEmpleadoEjecutivo;
		public Long		IdEmpleadoEjecutivoComercial;
		public String	FechaIngreso;
		public String	Alias;
		public Long		IdMatrizAgrupadorAPV;
		public Long		IdCuentaOrigen;
		public String	AreaCliente;
		public Long 	IdEmpresa;
		public Long		IdUsuario;
        
        public Portafolio() {
        	this.IdPortafolio = 0L;
        	this.CodigoPortafolio = "";
        	this.DescripcionPortafolio = "Portafolio Default";
        	this.PropositoPortafolio = "PROPGENERALES";
        	this.IdCliente = null;
        	this.Estado = "ACT";
        	this.IdPerfilComercialBase = null;
        	this.IdPersonaBrokerExtranjero = 0L;
        	this.IdPerfilProductos = null;
        	this.IdPlantillaExtra = null;
        	this.IdPerfilCobros = null;
        	this.IdPerfilCobrosPeru = null;
        	this.IdPerfilLimites = null;
        	this.IdPerfilCondicionesOperativas = null;
        	this.IdEmpleadoEjecutivo = null;
        	this.FechaIngreso = null;
        	this.Alias = "";
        	this.IdMatrizAgrupadorAPV = 0L;
        	this.IdCuentaOrigen = null;
        	this.AreaCliente = "";
        	this.IdEmpresa = 0L;
        	this.IdUsuario = 0L;
        }
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	
	
	public static class CodigoPortafolio {
		public String   CodigoPortafolio;
		public Long		IdPortafolio;

		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	

	public static class Matriz {
		public Long   	IdMatrizAgrupadorAPV;
		public String   CodigoPortafolio;
		public int   	IdProductoAgrupador;
		public String   ProductoAgrupador;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	
	
	public static class MatrizAgrupador {
		public String  IdEstado;
		public Long    IdPortafolio;

		public MatrizAgrupador(){
			this.IdEstado = "";
			this.IdPortafolio = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}		
	
	public static class PerfilComercialBase {
		public String IdPerfilProductos;
		public String IdPerfilCobros;
		public String IdPerfilCobrosPeru;
		public String IdPerfilLimites;
		public String IdPerfilCondicionesOperativas;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}
	
	public static class EmpleadoEjecutivo {
		public Long     IdEmpleadoEjecutivo;
		public String	NombreCompleto;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	
	
	public static class CuentaInversion {
		public Long     IdCliente;
		public Long		IdPortafolio;
		public String	IdMoneda;
		public Long		IdCuentaInversion;
		public String	Nombre;
		public String	CuentaInversionPrincipal;
		public String	Accion;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	
	
	public static class PreferenciaDePago {
		public Long 	IdPortafolio;
		public Long 	IdCuentaBanco;
		public Long 	IdUsuario;
		public Long 	IdEmpresa;
		public String 	FechaIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}
	
	public static class LogGenerarContratos {
		public Long 	IdEmpresa;
		public Long 	IdCliente;
		public Long 	IdUsuario;
		public String 	FechaIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}
	
	public static class PortafolioLogAuditoria {
		public String 	NombreCompleto;
		public String 	DocumentoIdentidad;
		public String 	TipoDocumentoIdentidad;
		public String 	CodigoPortafolio;
		public String 	DescripcionPortafolio;
		public String 	PropositoPortafolio;
		public String 	PerfilComercialBase;
		public String 	EstadoPortafolioAutorizacion;
		public Long     IdEjecutivoComercial;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}
	
	public static class DetalleLog {
		public String 	SubRegistro;
		public String 	CampoRegistro;
		public String 	ValorAntiguo;
		public String 	ValorNuevo;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}
	
	public static class GestionCtaInversion {
		public String 	Campo;
		public Long	 	Id;
		public String 	Servicio;
		public String 	Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}
	
	public static class GlosaAutorizacion {
		public String 	Tipo;
		public String 	DocumentoIdentidad;
		public String 	RazonSocial;
		public String 	Nombres;
		public String 	ApellidoPaterno;
		public String 	ApellidoMaterno;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}
	
	public static class CategoriaInvOperacion {
		public String IdPerfilProducto;
		public String IdPerfilCobro;
		public String CategoriaInvComercial;
		public String CategoriaInvProducto;
		public String CategoriaInvCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
			}		
	}	
	
	@Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaCrearPortafolio jin = gson.fromJson(in.toString(), EntradaCrearPortafolio.class);
        SalidaCrearPortafolio out = logica(conn, jin);

        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(out)).getAsJsonObject();
    }	
	
	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS PARA PROCESAMIENTO EN LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------	
	 
	public SalidaCrearPortafolio LimpiarSalida(SalidaCrearPortafolio out) {
		
		if (out.P_OUT_ERRORESCONTADOR > 0){
			for ( int i = 0; i < out.P_OUT_ADVERTENCIASDETALLE.size() && i >= 0  ; i++ ){
				if (!out.P_OUT_ADVERTENCIASDETALLE.get(i).Codigo.isEmpty()){
					out.P_OUT_ADVERTENCIASDETALLE.remove( i ) ;
					i--;
				}
			}
		}
		return out;
	}
	
	//Procesa salida de validaciones
	private void ProcesaRequisitoCrearPortafolio(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, CrearPortafolio_Proceso cpp, SalidaCrearPortafolio salida)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("countPortafolio", cpp.countPortafolio)
					.add("countPlanAhorro", cpp.countPlanAhorro)
					.add("countPerfilComercial", cpp.countPerfilComercial)
				    .msg();			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				salida.P_OUT_ERRORESDETALLE.add( new ErrorDetalle(CodRequisito,msg));
				salida.P_OUT_ERRORESCONTADOR++;
				cpp.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA))
			{
				salida.P_OUT_ADVERTENCIASDETALLE.add( new AdvertenciaDetalle(CodRequisito,MensajeSalida.MENSAJE_ADVERTENCIA,msg));
				salida.P_OUT_ADVERTENCIASCONTADOR++;
				cpp.advertencias++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				salida.P_OUT_ADVERTENCIASDETALLE.add( new AdvertenciaDetalle("REQUIERE_AUTORIZACION_PORTAFOLIO","S",msg));
				salida.P_OUT_ADVERTENCIASCONTADOR++;
				cpp.advertencias++;
			}
			else 
			{
				cpp.ok++;
			}
		}
		else 
		{
			cpp.ok++;
		}		
	}
	
	
	
	public SalidaCrearPortafolio logica(Connection con, EntradaCrearPortafolio in) throws Exception {
		logger.info("== INICIO CREAR PORTAFOLIO ==");
		SQL sql = new SQL();
		SalidaCrearPortafolio salida = new SalidaCrearPortafolio();
		List<MatrizAgrupador> matrizAgrupador = new ArrayList<MatrizAgrupador>();
		List<Matriz> matriz = new ArrayList<Matriz>();
		List<Portafolio> portafolio = new ArrayList<Portafolio>();
		CrearPortafolio_Proceso crearPortVar = new CrearPortafolio_Proceso();

		crearPortVar.p_out_RefId = -1L;
		//Long countRegimen = 0L;
		crearPortVar.IdConcepto = "NUEPOR";
		crearPortVar.IdEstadoAutorizacion = "PEN";
		crearPortVar.TipoEntidadReplica = "PORTAFOLIO";
		crearPortVar.TipoOperacion = "U";
		crearPortVar.TipoEntidad = "PORTAFOLIO";
		crearPortVar.NombreProceso = "Ingreso Portafolio";
		Long contadorMatriz = 0L;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		//VALIDA SI CUMPLE REQUISITOS BASICOS
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			logger.debug("NO CUMPLE REQUISITOS BASICOS");
			salida.P_OUT_ERRORESDETALLE.add((ErrorDetalle) salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR++;
			return LimpiarSalida(salida);
		}
		
		crearPortVar.CodigoPortafolio = in.p_CodigoPortafolio;
		HashMap<String,Object> params = new HashMap<>();		
		if (in.p_CodigoPortafolio.isEmpty()) {			
			crearPortVar.CodigoPortafolio = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "SOCIODENEGOCIO", "POR.CodigoPortafolioDefault");
		}
		
		if (!in.p_PropositoPortafolio.equalsIgnoreCase("PROPOSITOAPV")) {
			String maximoCodigoPortafolio = "";
			
			try {
				maximoCodigoPortafolio = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "SOCIODENEGOCIO", "POR.MaximoCodigoPortafolio");
			} catch (Exception e) {
				logger.warn("No se pudo obtener el par�metro POR.MaximoCodigoPortafolio. Usando valor por defecto vac�o.", e);
			}
			
			if (!maximoCodigoPortafolio.isEmpty()) {
			    Pattern regexCodigoPortafolio = Pattern.compile(maximoCodigoPortafolio);
			    Matcher matchCodigoPortafolio = regexCodigoPortafolio.matcher(crearPortVar.CodigoPortafolio);
			    if (!matchCodigoPortafolio.find()) {
				    logger.debug("CODIGO PORTAFOLIO NO CUMPLE CON PARAMETRO POR.MaximoCodigoPortafolio");
				    String codigoError = "CODIGO_PORTAFOLIO_NO_VALIDO";
				    ErrorDetalle error = new ErrorDetalle(codigoError,Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()); 
				    salida.P_OUT_ERRORESDETALLE.add(error);
				    salida.P_OUT_ERRORESCONTADOR++;
				    return LimpiarSalida(salida);
			    }
			}

			params.put("IdCliente", in.p_IdCliente);
			params.put("CodigoPortafolio", crearPortVar.CodigoPortafolio);
			crearPortVar.countPortafolio = sql.countQuery(con, SQL_SELECT_COUNT_PORTAFOLIO, params);
			
			params.clear();
			params.put("IdCliente", in.p_IdCliente);
			crearPortVar.countPortafoliosVigentes = sql.countQuery(con, SQL_SELECT_COUNT_PORTAFOLIOS_VIGENTES, params);	  
		}
	
		else {
			params.clear();			
			params.put("IdCliente", in.p_IdCliente);
			params.put("PropositoPortafolio", in.p_PropositoPortafolio);
			params.put("CodigoRegimen", in.p_CodigoRegimen);
			params.put("NombrePlan", in.p_NombrePlan);
			params.put("CodigoAgrupador", in.p_CodigoAgrupador);				
			
			portafolio = sql.query(con, SQL_CONSULTAR_PROPOSITO_PORTAFOLIO, params, Portafolio.class);
			crearPortVar.countPortafolio = (long) portafolio.size();

			params.clear();
			params.put("CodigoRegimen", in.p_CodigoRegimen);
			params.put("PropositoPortafolio", in.p_PropositoPortafolio);
			params.put("NombrePlan", in.p_NombrePlan);			
			params.put("CodigoAgrupador", in.p_CodigoAgrupador);		
			matriz = sql.query(con, SQL_CONSULTAR_MATRIZ_PORTAFOLIO, params, Matriz.class);
			
			if (matriz.size() > 0) {
				crearPortVar.CountMatrizAgrupadorAPV =  (long) matriz.size();				
				crearPortVar.IdMatrizAgrupadorAPV = matriz.get(0).IdMatrizAgrupadorAPV;
				crearPortVar.CodigoPortafolio = matriz.get(0).CodigoPortafolio;
				crearPortVar.IdProductoAgrupador = (long) matriz.get(0).IdProductoAgrupador;
				crearPortVar.ProductoAgrupador = matriz.get(0).ProductoAgrupador;		

				if (matriz.get(0).IdMatrizAgrupadorAPV != null && matriz.get(0).IdMatrizAgrupadorAPV > 0) {	
					params.clear();
					params.put("IdCliente", in.p_IdCliente);
					params.put("IdProductoAgrupador", crearPortVar.IdProductoAgrupador);
					matrizAgrupador = sql.query(con, SQL_CONSULTAR_MATRIZ_AGRUPADOR, params, MatrizAgrupador.class);
						
				}				
			}
			
			crearPortVar.ImpideDoblePlanDeAhorro = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "APV.ImpideDoblePlanAhorro");
			if(crearPortVar.ImpideDoblePlanDeAhorro == null) crearPortVar.ImpideDoblePlanDeAhorro = "S";
			
			if (crearPortVar.ImpideDoblePlanDeAhorro.equals("S")){

				params.clear();
				params.put("IdCliente", in.p_IdCliente);
				params.put("PropositoPortafolio", in.p_PropositoPortafolio);
				params.put("NombrePlan", in.p_NombrePlan);			
				crearPortVar.countPlanAhorro = sql.countQuery(con, SQL_CONSULTAR_REGIMEN_TRIBUTARIO, params);
				
			}
		}

		params.clear();
		params.put("p_IdPerfilComercialBase", in.p_IdPerfilComercialBase);			
		List<PerfilComercialBase> perfilComercial = sql.query(con, SQL_CONSULTAR_PERFIL_COMERCIAL_BASE, params, PerfilComercialBase.class);
		
		List<SocioNegocioUtils.ConfiguracionPortafolioDefecto> configuracionPortafolios = SocioNegocioUtils.obtenerConfiguracionPortafolioDefecto(con);
		
		List<PortafolioModificarIdEmpleado> ExisteEjecutivoComercial=new ArrayList<PortafolioModificarIdEmpleado>();		 
		if (in.p_IdEjecutivoComercial > 0) {
			params.clear();
			params.put("idEmpleado", in.p_IdEjecutivoComercial);			
			ExisteEjecutivoComercial= sql.query(con, SQL_SELECT_EJECUTIVO_COMERCIAL, params, PortafolioModificarIdEmpleado.class);
			 
			if (ExisteEjecutivoComercial.isEmpty() ){
				String codigoError = "UI_ERROR_CARGAR_EJECUTIVO_COMERCIAL";	   	
				salida.P_OUT_ADVERTENCIASCONTADOR++;
				salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(codigoError, "N", Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()));
			}
		
		}
		
		if (in.p_IdEjecutivoPortafolio > 0) {
			params.clear();
			params.put("IdEmpleadoEjecutivo", in.p_IdEjecutivoPortafolio);			
			List<EmpleadoEjecutivo> ejecutivoPortafolio = sql.query(con, SQL_SELECT_EJECUTIVO_PORTAFOLIO, params, EmpleadoEjecutivo.class);
			
			if (ejecutivoPortafolio.isEmpty()){
				String codigoError = "UI_ERROR_CARGAR_EJECUTIVO_PORTAFOLIO";	   	
				salida.P_OUT_ADVERTENCIASCONTADOR++;
				salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(codigoError, "N", Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg()));
				in.p_IdEjecutivoPortafolio = 0L;
			}
		}
		/*--------------------
        --Logica Negocio
        --------------------*/		
		if (perfilComercial == null || perfilComercial.isEmpty()) {
			String codigoError = "PERFIL_COMERCIAL_NO_EXISTE";
			ErrorDetalle error = new ErrorDetalle(codigoError, Diccionario.getMensaje(con, in.p_IdEmpresa, codigoError).msg());
			salida.P_OUT_ERRORESDETALLE.add(error);
			salida.P_OUT_ERRORESCONTADOR++;
			return LimpiarSalida(salida);
		}

		if ("PROPOSITOAPV".equalsIgnoreCase(in.p_PropositoPortafolio)) {
			String idPerfilCobros = perfilComercial.get(0).IdPerfilCobros != null ? perfilComercial.get(0).IdPerfilCobros.trim() : "";
			if (!"APV_EXENTO".equalsIgnoreCase(idPerfilCobros)) {
				String codigoError = "PERFIL_COMERCIAL_APV_EXENTO_INVALIDO";
				ErrorDetalle error = new ErrorDetalle(codigoError, Diccionario.getMensaje(con, in.p_IdEmpresa, codigoError).msg());
				salida.P_OUT_ERRORESDETALLE.add(error);
				salida.P_OUT_ERRORESCONTADOR++;
				return LimpiarSalida(salida);
			}
		}
		
		
		if(crearPortVar.countPortafolio > 0) {
			ProcesaRequisitoCrearPortafolio(con, in.p_IdEmpresa, (crearPortVar.countPortafolio == null || crearPortVar.countPortafolio > 0L),"PORTAFOLIO_EXISTE",MensajeSalida.MENSAJE_ERROR,crearPortVar,salida);
		}else {
			List<Long> IdPortafolio = Utils.getSecuencia(con, "BSCL.Portafolio.BD.Secuencias::Portafolio", 1L);
			crearPortVar.p_out_RefId = IdPortafolio.get(0);
			crearPortVar.IdPlantillaExtra = "EXT_".concat(crearPortVar.p_out_RefId.toString());

			params.clear();
			params.put("p_IdCliente", in.p_IdCliente);			
			List<EmpleadoEjecutivo> empleadoEjecutivo = sql.query(con, SQL_CONSULTAR_EMPLEADO_EJECUTIVO, params, EmpleadoEjecutivo.class);
			
			Portafolio inPortafolio = new Portafolio();
			List<Portafolio> lstInPortafolio = new ArrayList<Portafolio>();
			Long IdempleadoEjecutivo = 0L;
			inPortafolio.IdPortafolio = crearPortVar.p_out_RefId;
			inPortafolio.CodigoPortafolio = crearPortVar.CodigoPortafolio;	
			inPortafolio.DescripcionPortafolio = in.p_DescripcionPortafolio;	
			inPortafolio.PropositoPortafolio = in.p_PropositoPortafolio;	
			inPortafolio.IdCliente = in.p_IdCliente;	
			inPortafolio.IdPerfilComercialBase = in.p_IdPerfilComercialBase;
			inPortafolio.IdPlantillaExtra = crearPortVar.IdPlantillaExtra;	
			inPortafolio.IdPerfilCobrosPeru = perfilComercial.get(0).IdPerfilCobrosPeru;	
			inPortafolio.IdPerfilLimites = perfilComercial.get(0).IdPerfilLimites;		
			inPortafolio.IdPerfilCondicionesOperativas = perfilComercial.get(0).IdPerfilCondicionesOperativas;	
			inPortafolio.FechaIngreso = fechaSistema;
			inPortafolio.IdCuentaOrigen = in.p_IdCuentaOrigen;
			inPortafolio.AreaCliente = in.p_AreaCliente;
			inPortafolio.IdEmpresa = in.p_IdEmpresa;
			
			if ( (in.p_IdEjecutivoComercial != null)) {
				if (! ExisteEjecutivoComercial.isEmpty() ) {
						inPortafolio.IdEmpleadoEjecutivoComercial =  in.p_IdEjecutivoComercial;					
				}else if(empleadoEjecutivo.get(0).IdEmpleadoEjecutivo != null){
						inPortafolio.IdEmpleadoEjecutivoComercial = empleadoEjecutivo.get(0).IdEmpleadoEjecutivo;
				}else {
					inPortafolio.IdEmpleadoEjecutivoComercial = 0L;
				}
			}else {
					inPortafolio.IdEmpleadoEjecutivoComercial = IdempleadoEjecutivo;
			}
			
			if((!empleadoEjecutivo.isEmpty()) && (empleadoEjecutivo.get(0).IdEmpleadoEjecutivo!= null )) {IdempleadoEjecutivo = empleadoEjecutivo.get(0).IdEmpleadoEjecutivo; } else {IdempleadoEjecutivo =0L;} ;
			
			if (in.p_IdEjecutivoPortafolio > 0 && in.p_IdEjecutivoPortafolio != null) {
				inPortafolio.IdEmpleadoEjecutivo = in.p_IdEjecutivoPortafolio;
			}else if (empleadoEjecutivo.get(0).IdEmpleadoEjecutivo != null) {
				inPortafolio.IdEmpleadoEjecutivo = empleadoEjecutivo.get(0).IdEmpleadoEjecutivo;
			}else {
				inPortafolio.IdEmpleadoEjecutivo = 0L;
			}
			
			if (in.p_IdPlantillaCobro != null && !in.p_IdPlantillaCobro.trim().isEmpty()) inPortafolio.IdPerfilCobros = in.p_IdPlantillaCobro;	
			else inPortafolio.IdPerfilCobros = perfilComercial.get(0).IdPerfilCobros;
			
			if(in.p_IdPlantillaProducto != null && !in.p_IdPlantillaProducto.trim().isEmpty()) inPortafolio.IdPerfilProductos = in.p_IdPlantillaProducto;
			else inPortafolio.IdPerfilProductos = perfilComercial.get(0).IdPerfilProductos;
			
			if(in.p_IdPersonaBrokerExtranjero == null) inPortafolio.IdPersonaBrokerExtranjero = 0L;
			else inPortafolio.IdPersonaBrokerExtranjero = in.p_IdPersonaBrokerExtranjero;	
			
			if(in.p_Estado == null || in.p_Estado.isEmpty()) inPortafolio.Estado = "ACT";
			else inPortafolio.Estado = in.p_Estado;	
			
			if(!empleadoEjecutivo.isEmpty()) inPortafolio.Alias = empleadoEjecutivo.get(0).NombreCompleto;
			
			if(!matriz.isEmpty()) inPortafolio.IdMatrizAgrupadorAPV = matriz.get(0).IdMatrizAgrupadorAPV;
			
			lstInPortafolio.add(inPortafolio);
			
			{
				ProcesaRequisitoCrearPortafolio(con, in.p_IdEmpresa, (crearPortVar.countPortafolio == null || crearPortVar.countPortafolio > 0L),"PORTAFOLIO_EXISTE",MensajeSalida.MENSAJE_ERROR,crearPortVar,salida);
				ProcesaRequisitoCrearPortafolio(con, in.p_IdEmpresa, (crearPortVar.countPlanAhorro == null || crearPortVar.countPlanAhorro > 0L),"REGLA_CLIENTE_PLAN_AHORRO",MensajeSalida.MENSAJE_ERROR,crearPortVar,salida);
				ProcesaRequisitoCrearPortafolio(con, in.p_IdEmpresa, (perfilComercial.isEmpty() || perfilComercial == null),"PERFIL_COMERCIAL_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,crearPortVar,salida);			
			}

			logger.debug("INICIO::SQL_INSERT_PORTAFOLIO");
			int[] resultInsertPort = sql.batch(con, SQL_INSERT_PORTAFOLIO, lstInPortafolio, Portafolio.class);
			logger.debug("FIN::SQL_INSERT_PORTAFOLIO");
			
			if(resultInsertPort != null && resultInsertPort.length > 0L) {
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_INDICADOR_PREFERENCIAS");
				//Heredad las preferencias del portafolio desde el modelo tipo cliente
				int[] resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_INDICADOR_PREFERENCIAS, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_INDICADOR_PREFERENCIAS");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_FISICA ");
				//direccion fisica
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_FISICA, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_FISICA ");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_NOTIFICACION ");
				//direccion electronica notificacion
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_NOTIFICACION, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_NOTIFICACION ");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_FACTURACION ");
				//direccion electronica de facturacion
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_FACTURACION, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_INDICADOR_DIRECCION_ELECTRONICA_FACTURACION ");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_INDICADOR_OPERA_CUENTA_INVERSION ");
				//Indicador Opera contra Cuenta de Inversion (GEN_OCCINV) queda marcado para PORTAFOLIO APV
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_INDICADOR_OPERA_CUENTA_INVERSION, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_INDICADOR_OPERA_CUENTA_INVERSION ");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_PERFIL_COBROS ");
				//INSERT CABECERA DE PLANTILLAS POR DEFECTO
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_PERFIL_COBROS, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_PERFIL_COBROS ");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_PERFIL_COBROS_COM_2 ");
				//INSERT CABECERA DE PLANTILLAS POR DEFECTO
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_PERFIL_COBROS_COM_2, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_PERFIL_COBROS_COM_2 ");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_PERFIL_PRODUCTOS ");
				//INSERT Perfil productos
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_PERFIL_PRODUCTOS, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_PERFIL_PRODUCTOS ");
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_PLANILLA_LIMITE ");
				//INSERT Perfil productos
				resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_PLANILLA_LIMITE, lstInPortafolio, Portafolio.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_PLANILLA_LIMITE ");
			}

			if(crearPortVar.p_out_RefId > 0) {
				crearPortVar.MonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
				
				CuentaInversion ctaInversion = new CuentaInversion();
				List<CuentaInversion> lstCtaInversion = new ArrayList<CuentaInversion>();
				ctaInversion.IdCliente = in.p_IdCliente;
				ctaInversion.IdPortafolio = crearPortVar.p_out_RefId;
				ctaInversion.IdMoneda = crearPortVar.MonedaSistema;
				ctaInversion.IdCuentaInversion = null;
				ctaInversion.Nombre = null;
				ctaInversion.CuentaInversionPrincipal = null;
				ctaInversion.Accion = "I";
				lstCtaInversion.add(ctaInversion);
				
				ctaInversion = new CuentaInversion();
				ctaInversion.IdCliente = in.p_IdCliente;
				ctaInversion.IdPortafolio = crearPortVar.p_out_RefId;
				ctaInversion.IdMoneda = "USD";
				ctaInversion.IdCuentaInversion = null;
				ctaInversion.Nombre = null;
				ctaInversion.CuentaInversionPrincipal = null;
				ctaInversion.Accion = "I";
				lstCtaInversion.add(ctaInversion);
				
				String creaCuentaInversionEUR = "S";
				creaCuentaInversionEUR = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "SOCIODENEGOCIO", "CLI.CrearCuentaInversionEUR");
				if(creaCuentaInversionEUR.compareTo("S") == 0) {
					ctaInversion = new CuentaInversion();
					ctaInversion.IdCliente = in.p_IdCliente;
					ctaInversion.IdPortafolio = crearPortVar.p_out_RefId;
					ctaInversion.IdMoneda = "EUR";
					ctaInversion.IdCuentaInversion = null;
					ctaInversion.Nombre = null;
					ctaInversion.CuentaInversionPrincipal = null;
					ctaInversion.Accion = "I";
					lstCtaInversion.add(ctaInversion);
				}
				
				sql.batch(con,SQL_INSERT_PORTAFOLIO_GLOB_CTA_INVERSION,lstCtaInversion,CuentaInversion.class);
				
				HashMap<String,Object> paramsInCtaInversion = new HashMap<>();	
				paramsInCtaInversion.put("p_IdEmpresa", in.p_IdEmpresa);
				paramsInCtaInversion.put("p_IdUsuario", in.p_IdUsuario);
				paramsInCtaInversion.put("p_IdModulo", in.p_IdModulo);
				//paramsInCtaInversion.put("p_UsaReglasNegocio", "S");
				paramsInCtaInversion.put("p_CuentaInversion", lstCtaInversion);
				
				HashMap<String,Class>  paramsOutCtaInversion = new HashMap<String,Class>();
				paramsOutCtaInversion.put("p_out_ResultadoProceso", GestionCtaInversion.class);
				
				paramsOutCtaInversion.put("p_out_InformacionContador", Long.class);
				paramsOutCtaInversion.put("p_out_InformacionDetalle", AdvertenciaDetalle.class);
				paramsOutCtaInversion.put("p_out_AdvertenciasContador", Long.class);
				paramsOutCtaInversion.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
				paramsOutCtaInversion.put("p_out_ErroresContador", Long.class);
				paramsOutCtaInversion.put("p_out_ErroresDetalle", ErrorDetalle.class);
				
				
				HashMap<String,Object> resultSP = sql.call(con, SQL_CALL_CREAR_CUENTA_INVERSION, paramsInCtaInversion, paramsOutCtaInversion);	
				
				if (!resultSP.isEmpty()) {
				    
				    // Manejar ErroresContador
				    Object erroresContador = resultSP.get("p_out_ErroresContador");
				    if (erroresContador instanceof Long) {
				    	salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR.sum(salida.P_OUT_ERRORESCONTADOR, (long) erroresContador);
				    }
				    // Manejar ErroresDetalle
				    Object erroresDetalle = resultSP.get("p_out_ErroresDetalle");
				    if (erroresDetalle instanceof List<?>) {
				        salida.P_OUT_ERRORESDETALLE.addAll((Collection<? extends ErrorDetalle>) erroresDetalle);
				    }
				}

			}
	
			if(crearPortVar.countPortafolio == 0L) {
				//Primer Portafolio se crea cuenta por defecto
				HashMap<String,Object> paramsCtaBancaria = new HashMap<>();
				paramsCtaBancaria.put("monedaSistema", crearPortVar.MonedaSistema);			
				List<Long> lstCtaBanco = sql.query(con, SQL_CONSULTAR_CUENTA_BANCO, paramsCtaBancaria, Long.class);
				
				if(!lstCtaBanco.isEmpty()){
					
					PreferenciaDePago prefPagoIn = new PreferenciaDePago();
					prefPagoIn.IdPortafolio = crearPortVar.p_out_RefId;
					prefPagoIn.IdUsuario = in.p_IdUsuario;
					prefPagoIn.IdEmpresa = in.p_IdEmpresa;
					prefPagoIn.FechaIngreso = fechaSistema;
					prefPagoIn.IdCuentaBanco = lstCtaBanco.get(0);
					
					List<PreferenciaDePago> lstInPrefPago = new ArrayList<PreferenciaDePago>();
					lstInPrefPago.add(prefPagoIn);
					
					logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO ");
					sql.batch(con, SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO, lstInPrefPago, PreferenciaDePago.class);
					logger.debug("FIN::SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO ");
				}
			}
			HashMap<String,Object> paramsInTipoCliente = new HashMap<>();
			paramsInTipoCliente.put("IdPortafolio", crearPortVar.p_out_RefId);		
			paramsInTipoCliente.put("IdEmpresa", in.p_IdEmpresa);
			List<String> IdTipoCliente = sql.query(con, SQL_CONSULTAR_TIPO_CLIENTE, paramsInTipoCliente, String.class);

			if(IdTipoCliente.get(0).compareTo("S") == 0) {
				PreferenciaDePago prefPagoIn = new PreferenciaDePago();
				prefPagoIn.IdPortafolio = crearPortVar.p_out_RefId;
				prefPagoIn.IdUsuario = in.p_IdUsuario;
				prefPagoIn.IdEmpresa = in.p_IdEmpresa;
				prefPagoIn.FechaIngreso = fechaSistema;
				prefPagoIn.IdCuentaBanco = 0L;
				
				List<PreferenciaDePago> lstInPrefPago = new ArrayList<PreferenciaDePago>();
				lstInPrefPago.add(prefPagoIn);
				
				logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO_TIPO_CLIENTE ");
				sql.batch(con, SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO_TIPO_CLIENTE, lstInPrefPago, PreferenciaDePago.class);
				logger.debug("FIN::SQL_INSERT_PORTAFOLIO_PREFERENCIA_PAGO_TIPO_CLIENTE ");
			}
			
			crearPortVar.RequiereAutOrigen = Utils.getRequiereEventoAutorizacion(con, in.p_IdEmpresa, crearPortVar.IdConcepto);
			crearPortVar.RequiereAutorizacion = crearPortVar.RequiereAutOrigen;
			
			if(matrizAgrupador.size() > 0L) crearPortVar.RequiereAutorizacion = "N";	
			if(in.p_UsaReglasNegocio.compareTo("N") != 0 && matrizAgrupador.size() == 0 && in.p_PropositoPortafolio.compareTo("PROPOSITOAPV") == 0) crearPortVar.RequiereAutorizacion = "S";
			else crearPortVar.RequiereAutOrigen = "N";

			if(crearPortVar.RequiereAutorizacion.compareToIgnoreCase("S") == 0 && in.p_CodigoRegimen.compareToIgnoreCase("BF") != 0) {
				//Se genera evento de autorizacion

				
				if(!matriz.isEmpty() && matriz.get(0).ProductoAgrupador.compareTo("") != 0) crearPortVar.CodigoPortafolio = matriz.get(0).ProductoAgrupador;
				
				HashMap<String,Object> paramsGlosa = new HashMap<>();
				//paramsGlosa.put("CodigoPortafolio", crearPortVar.CodigoPortafolio);		
				paramsGlosa.put("IdCliente", in.p_IdCliente);
				List<GlosaAutorizacion> glosaAutorizacion = new ArrayList<GlosaAutorizacion>();
				glosaAutorizacion = sql.query(con, SQL_CONSULTAR_GLOSA_AUTORIZACION, paramsGlosa, GlosaAutorizacion.class);
				
				HashMap<String,Object> paramsInEventoAut = new HashMap<>();	
				paramsInEventoAut.put("IdEmpresa", in.p_IdEmpresa);
				paramsInEventoAut.put("IdUsuario", in.p_IdUsuario);
				paramsInEventoAut.put("IdModulo", in.p_IdModulo);
				paramsInEventoAut.put("IdConcepto", crearPortVar.IdConcepto);
				//paramsInEventoAut.put("IdModulo", in.p_IdModulo);
				paramsInEventoAut.put("FechaIngreso", fechaSistema);
				paramsInEventoAut.put("IdPortafolio", crearPortVar.p_out_RefId.toString());
				paramsInEventoAut.put("IdCliente", in.p_IdCliente);
				if(glosaAutorizacion.get(0).Tipo.compareToIgnoreCase("J") == 0) crearPortVar.glosa = glosaAutorizacion.get(0).DocumentoIdentidad.concat(" / ").concat(glosaAutorizacion.get(0).RazonSocial).concat(" / CodPort:").concat(crearPortVar.CodigoPortafolio);
				else crearPortVar.glosa = glosaAutorizacion.get(0).DocumentoIdentidad.concat(" / ").concat(glosaAutorizacion.get(0).Nombres).concat(" ").concat(glosaAutorizacion.get(0).ApellidoPaterno).concat(" ").concat(glosaAutorizacion.get(0).ApellidoMaterno).concat(" / CodPort:").concat(crearPortVar.CodigoPortafolio);
				paramsInEventoAut.put("Glosa", crearPortVar.glosa);
				
				HashMap<String, Class> paramsOutEventoAut = new HashMap<String, Class>();
				paramsOutEventoAut.put("p_out_IdEvento", Long.class);
				paramsOutEventoAut.put("p_out_AdvertenciasContador", Long.class);
				paramsOutEventoAut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
				paramsOutEventoAut.put("p_out_ErroresContador", Long.class);
				paramsOutEventoAut.put("p_out_ErroresDetalle", ErrorDetalle.class);

				HashMap<String, Object> resultSP = new HashMap<String, Object>();
				resultSP = sql.call(con, SQL_CALL_CREAR_EVENTO_AUTORIZACION, paramsInEventoAut, paramsOutEventoAut);
				
				if (!resultSP.isEmpty()) {
										
				    // Manejar AdvertenciasContador
				    Object advertenciasContador = resultSP.get("p_out_AdvertenciasContador");
				    if (advertenciasContador instanceof Long) {
				    	salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASCONTADOR.sum(salida.P_OUT_ADVERTENCIASCONTADOR, (long) advertenciasContador);
				    }		 
				    // Manejar AdvertenciasDetalle
				    Object advertenciasDetalle = resultSP.get("p_out_AdvertenciasDetalle");
				    if (advertenciasDetalle instanceof List<?>) {
				        salida.P_OUT_ADVERTENCIASDETALLE.addAll((Collection<? extends AdvertenciaDetalle>) advertenciasDetalle);
				    } 
				    // Manejar ErroresContador
				    Object erroresContador = resultSP.get("p_out_ErroresContador");
				    if (erroresContador instanceof Long) {
				    	salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR.sum(salida.P_OUT_ERRORESCONTADOR, (long) erroresContador);
				    }
				    // Manejar ErroresDetalle
				    Object erroresDetalle = resultSP.get("p_out_ErroresDetalle");
				    if (erroresDetalle instanceof List<?>) {
				        salida.P_OUT_ERRORESDETALLE.addAll((Collection<? extends ErrorDetalle>) erroresDetalle);
				    }				    
					ProcesaRequisitoCrearPortafolio(con, in.p_IdEmpresa, (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0),"REQUIERE_AUTORIZACION_PORTAFOLIO",MensajeSalida.MENSAJE_AUTORIZACION,crearPortVar,salida);	    
				}		

			}
			
			else{
				//Confirma la creacion y cambia el estado del documento
				
				 /*---------------------------------------------------------------------------------------------------------------------
                 --+ Pronet IT 
                 --+ Valida si es un Portafolio APV el que cambiara de estado, debe cumplir una de estas condiciones:
                 --+ 1.- Si ya existe un portafolio del mismo grupo con una solicitud de autorizacion => No debe cambiar de estado 
                 --+ 2.- Si exite un portafolio del mismo grupo en cualquier estado => No debe cambiar de estado   
                 --+ 3.- Si y solo si es el primer portafolio el que viene ingresando => Debe entrar a cambiar el estado
                 --+ 4.- En caso de que no sea APV el vCountEstadosMatrizAgrupador = 0
                 ---------------------------------------------------------------------------------------------------------------------*/
				
				if(crearPortVar.RequiereAutOrigen.compareToIgnoreCase("N") == 0 && matrizAgrupador.size() == 0){
					logger.info("INICIO::InicioEventoAutorizacion");
					
					if(in.p_DesdeEnrolamiento.compareToIgnoreCase("N") == 0) {
						
						LogGenerarContratos logGenerarContratos = new LogGenerarContratos();						
						List<LogGenerarContratos> lstLogGenerarContratos = new ArrayList<LogGenerarContratos>();
						logGenerarContratos.IdEmpresa = in.p_IdEmpresa;
						logGenerarContratos.IdCliente = in.p_IdCliente;
						logGenerarContratos.IdUsuario = in.p_IdUsuario;
						logGenerarContratos.FechaIngreso = fechaSistema;
						lstLogGenerarContratos.add(logGenerarContratos);
						
						logger.debug("INICIO::SQL_INSERT_PORTAFOLIO_LOG_GENERAR_CONTRATOS");
						sql.batch(con, SQL_INSERT_PORTAFOLIO_LOG_GENERAR_CONTRATOS, lstLogGenerarContratos, LogGenerarContratos.class);
						logger.debug("FIN::SQL_INSERT_PORTAFOLIO_LOG_GENERAR_CONTRATOS");
					}
	
					crearPortVar.IdEstadoAutorizacion = "ING";
					HashMap<String,Object> paramsInEventoAut = new HashMap<>();	
					paramsInEventoAut.put("IdEmpresa", in.p_IdEmpresa);
					paramsInEventoAut.put("IdUsuario", in.p_IdUsuario);
					paramsInEventoAut.put("IdModulo", in.p_IdModulo);
					paramsInEventoAut.put("UsaReglasNegocio", in.p_UsaReglasNegocio);
					paramsInEventoAut.put("IdPortafolio", crearPortVar.p_out_RefId);
					paramsInEventoAut.put("IdEstadoAutorizacion", crearPortVar.IdEstadoAutorizacion);
					
					HashMap<String,Class>  paramsOutEventoAut = new HashMap<String,Class>();
					paramsOutEventoAut.put("p_out_RefId", Long.class);
					paramsOutEventoAut.put("p_out_AdvertenciasContador", Long.class);
					paramsOutEventoAut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
					paramsOutEventoAut.put("p_out_ErroresContador", Long.class);
					paramsOutEventoAut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);

					HashMap<String, Object> resultSP = new HashMap<String, Object>();
					resultSP = sql.call(con, SQL_CALL_CONFIRMAR_CREAR_PORTAFOLIO, paramsInEventoAut, paramsOutEventoAut);
					
					if (!resultSP.isEmpty()) {
					    // Manejar AdvertenciasContador
					    Object advertenciasContador = resultSP.get("p_out_AdvertenciasContador");
					    if (advertenciasContador instanceof Long) {
					    	salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASCONTADOR.sum(salida.P_OUT_ADVERTENCIASCONTADOR, (long) advertenciasContador);
					    }		 
					    // Manejar AdvertenciasDetalle
					    Object advertenciasDetalle = resultSP.get("p_out_AdvertenciasDetalle");
					    if (advertenciasDetalle instanceof List<?>) {
					        salida.P_OUT_ADVERTENCIASDETALLE.addAll((Collection<? extends AdvertenciaDetalle>) advertenciasDetalle);
					    } 
					    // Manejar ErroresContador
					    Object erroresContador = resultSP.get("p_out_ErroresContador");
					    if (erroresContador instanceof Long) {
					    	salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR.sum(salida.P_OUT_ERRORESCONTADOR, (long) erroresContador);
					    }
					    // Manejar ErroresDetalle
					    Object erroresDetalle = resultSP.get("p_out_ErroresDetalle");
					    if (erroresDetalle instanceof List<?>) {
					        salida.P_OUT_ERRORESDETALLE.addAll((Collection<? extends ErrorDetalle>) erroresDetalle);
					    }
					}
				}
					
				if(matrizAgrupador.size() > 0) {
					
					HashMap<String,Object> paramsInReplicaPortAPV = new HashMap<>();	
					paramsInReplicaPortAPV.put("IdEmpresa", in.p_IdEmpresa);
					paramsInReplicaPortAPV.put("IdUsuario", in.p_IdUsuario);
					paramsInReplicaPortAPV.put("IdModulo", in.p_IdModulo);
					paramsInReplicaPortAPV.put("UsaReglasNegocio", in.p_UsaReglasNegocio);
					paramsInReplicaPortAPV.put("IdPortafolioGrupoAPV", matrizAgrupador.get(0).IdPortafolio);
					paramsInReplicaPortAPV.put("TipoEntidadReplica", crearPortVar.TipoEntidadReplica);
					paramsInReplicaPortAPV.put("TipoOperacion", crearPortVar.TipoOperacion);
					
					HashMap<String,Class>  paramsOutReplicaPortAPV = new HashMap<String,Class>();
					paramsOutReplicaPortAPV.put("p_out_AdvertenciasContador", Long.class);
					paramsOutReplicaPortAPV.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
					paramsOutReplicaPortAPV.put("p_out_ErroresContador", Long.class);
					paramsOutReplicaPortAPV.put("p_out_ErroresDetalle", Long.class);

					HashMap<String, Object> resultSPReplica = new HashMap<String, Object>();
					resultSPReplica = sql.call(con, SQL_CALL_REPLICA_PORTAFOLIO_APV, paramsInReplicaPortAPV, paramsOutReplicaPortAPV);
					
					if (!resultSPReplica.isEmpty()) {
					    // Manejar AdvertenciasContador
					    Object advertenciasContador = resultSPReplica.get("p_out_AdvertenciasContador");
					    if (advertenciasContador instanceof Long) {
					    	salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASCONTADOR.sum(salida.P_OUT_ADVERTENCIASCONTADOR, (long) advertenciasContador);
					    }		 
					    // Manejar AdvertenciasDetalle
					    Object advertenciasDetalle = resultSPReplica.get("p_out_AdvertenciasDetalle");
					    if (advertenciasDetalle instanceof List<?>) {
					        salida.P_OUT_ADVERTENCIASDETALLE.addAll((Collection<? extends AdvertenciaDetalle>) advertenciasDetalle);
					    } 
					    // Manejar ErroresContador
					    Object erroresContador = resultSPReplica.get("p_out_ErroresContador");
					    if (erroresContador instanceof Long) {
					    	salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR.sum(salida.P_OUT_ERRORESCONTADOR, (long) erroresContador);
					    }
					    // Manejar ErroresDetalle
					    Object erroresDetalle = resultSPReplica.get("p_out_ErroresDetalle");
					    if (erroresDetalle instanceof List<?>) {
					        salida.P_OUT_ERRORESDETALLE.addAll((Collection<? extends ErrorDetalle>) erroresDetalle);
					    }
					}					
				}
				logger.info("FIN::EventoAutorizacion");
			
			}
				// actualizar idPortafolioDefecto del cliente
			if(crearPortVar.p_out_RefId > 0) {
				
				Portafolio portDefecto = new Portafolio();
				List<Portafolio> lstPortDefecto = new ArrayList<Portafolio>();
				portDefecto.IdCliente = in.p_IdCliente;
				portDefecto.IdPortafolio = crearPortVar.p_out_RefId;
				lstPortDefecto.add(portDefecto);
				
				logger.debug("INICIO::SQL_UPDATE_PORTAFOLIO_DEFECTO_CLIENTE ");
				sql.batch(con, SQL_UPDATE_PORTAFOLIO_DEFECTO_CLIENTE, lstPortDefecto, Portafolio.class);
				logger.debug("FIN::SQL_UPDATE_PORTAFOLIO_DEFECTO_CLIENTE ");
			
				logger.info("INICIO::Log de Auditoria");
			
				HashMap<String,Object> paramsPortLogAud = new HashMap<>();		
				paramsPortLogAud.put("IdPortafolio", crearPortVar.p_out_RefId);
				
				List<PortafolioLogAuditoria> portLogAuditoria = sql.query(con, SQL_CONSULTAR_PORTAFOLIO_LOG_AUDITORIA, paramsPortLogAud, PortafolioLogAuditoria.class);
				
				String subregistro = crearPortVar.TipoEntidad;
				
				if(crearPortVar.IdEstadoAutorizacion.compareTo("ING") != 0) {
					crearPortVar.NombreProceso = portLogAuditoria.get(0).EstadoPortafolioAutorizacion.concat(" ").concat(crearPortVar.NombreProceso);
				}

				DetalleLog detalleLog = new DetalleLog();
				List<DetalleLog> lstDetalleLog = new ArrayList<DetalleLog>();
				detalleLog.SubRegistro = subregistro;
				detalleLog.CampoRegistro = "CodigoPortafolio";
				detalleLog.ValorAntiguo = "";
				detalleLog.ValorNuevo = portLogAuditoria.get(0).CodigoPortafolio;
				lstDetalleLog.add(detalleLog);
				detalleLog = new DetalleLog();
				detalleLog.SubRegistro = subregistro;
				detalleLog.CampoRegistro = "DescripcionPortafolio";
				detalleLog.ValorAntiguo = "";
				detalleLog.ValorNuevo = portLogAuditoria.get(0).DescripcionPortafolio;
				lstDetalleLog.add(detalleLog);
				detalleLog = new DetalleLog();
				detalleLog.SubRegistro = subregistro;
				detalleLog.CampoRegistro = "Proposito";
				detalleLog.ValorAntiguo = "";
				detalleLog.ValorNuevo = portLogAuditoria.get(0).PropositoPortafolio;
				lstDetalleLog.add(detalleLog);
				detalleLog = new DetalleLog();
				detalleLog.SubRegistro = subregistro;
				detalleLog.CampoRegistro = "IdPerfilComercialBaseOrigen";
				detalleLog.ValorAntiguo = "";
				detalleLog.ValorNuevo = portLogAuditoria.get(0).PerfilComercialBase;
				lstDetalleLog.add(detalleLog);
				detalleLog.SubRegistro = subregistro;
				detalleLog.CampoRegistro = "IdEjecutivoComercial";
				detalleLog.ValorAntiguo = "";
				detalleLog.ValorNuevo =  portLogAuditoria.get(0).IdEjecutivoComercial.toString();
				lstDetalleLog.add(detalleLog);
			
				int[] resultInsert = sql.batch(con, SQL_INSERT_PORTAFOLIO_GLOB_DETALLE_LOG, lstDetalleLog, DetalleLog.class);
				
				HashMap<String,Object> paramsInAgregarLog = new HashMap<>();	
				paramsInAgregarLog.put("IdEmpresa", in.p_IdEmpresa);
				paramsInAgregarLog.put("IdUsuario", in.p_IdUsuario);
				paramsInAgregarLog.put("IdCliente", in.p_IdCliente);
				paramsInAgregarLog.put("FechaHoraSistema", fechaSistema);
				paramsInAgregarLog.put("NombreCliente", portLogAuditoria.get(0).NombreCompleto);
				paramsInAgregarLog.put("DocumentoIdentidad", portLogAuditoria.get(0).DocumentoIdentidad);
				paramsInAgregarLog.put("TipoDocumentoIdentidad", portLogAuditoria.get(0).TipoDocumentoIdentidad);
				paramsInAgregarLog.put("TipoEntidad", crearPortVar.TipoEntidad);
				paramsInAgregarLog.put("DetalleLog", lstDetalleLog);
				paramsInAgregarLog.put("p_out_RefId", crearPortVar.p_out_RefId);
				paramsInAgregarLog.put("NombreProceso", crearPortVar.NombreProceso);
				paramsInAgregarLog.put("idEstadoAutorizacion", crearPortVar.IdEstadoAutorizacion);
				
				HashMap<String, Class> out = new HashMap<>();
				sql.call(con, SQL_CALL_AGREGAR_LOG, paramsInAgregarLog, out);
			}
			logger.info("FIN::Log de Auditoria");
		}
		
		if(salida.P_OUT_ERRORESCONTADOR == 0) {
			salida.p_out_RefId = crearPortVar.p_out_RefId;
		}
		
		logger.info("== FIN CREAR PORTAFOLIO ==");
		return salida;	
	}
		
	public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");
        //String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoPortafolio\":\"\",\"p_DescripcionPortafolio\":\"\",\"p_PropositoPortafolio\":\"PROPOSITOAPV\",\"p_IdCuentaOrigen\":0,\"p_CodigoRegimen\":\"APV-A\",\"p_NombrePlan\":\"A. ABIERTO\",\"p_CodigoAgrupador\":\"GRUPO 4\",\"p_IdCliente\":84944,\"p_IdPerfilComercialBase\":\"ARRIESGADO\",\"p_IdPersonaBrokerExtranjero\":0,\"p_Estado\":\"\",\"p_AreaCliente\":\"\",\"p_DesdeEnrolamiento\":\"\"}";
        //String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoPortafolio\":\"98\",\"p_DescripcionPortafolio\":\"PORTAFOLIO_PRUEBA_2\",\"p_PropositoPortafolio\":\"PROPGENERALES\",\"p_DesdeEnrolamiento\":\"N\",\"p_CodigoRegimen\":\"\",\"p_NombrePlan\":\"\",\"p_CodigoAgrupador\":\"\",\"p_IdCliente\":84953,\"p_IdPerfilComercialBase\":\"<ARRIESGADO\"}";
        String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoPortafolio\":\"96\",\"p_DescripcionPortafolio\":\"PRUE NUEPOR\",\"p_PropositoPortafolio\":\"PROPGENERALES\",\"p_DesdeEnrolamiento\":\"N\",\"p_CodigoRegimen\":\"\",\"p_NombrePlan\":\"\",\"p_CodigoAgrupador\":\"\",\"p_IdCliente\":85137,\"p_IdPerfilComercialBase\":\"ARRIESGADO\"}";
        
        try (Connection conn = PlataformaUtils.getMASTER01Connection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaCrearPortafolio entrada = gson.fromJson(requestString, EntradaCrearPortafolio.class);
            CrearPortafolio crearPortafolio = new CrearPortafolio();
            try {
            	SalidaCrearPortafolio out = crearPortafolio.logica(conn, entrada);
                System.out.println(out);
                conn.rollback();
                //conn.commit();
            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();             
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }

}

