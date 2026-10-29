package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCargaMasivaRecaudacion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ProcesarCargaMasivaRecaudacion.class);
	
	private final String SQL_SELECT_CARGA_DETALLE = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_CargaMasivaDetalle_Select.sql");
	private final String SQL_SELECT_EMPLEADOR     = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_ConsultarEmpleador_Select.sql");
	private final String SQL_SELECT_CLIENTE       = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_ConsultarIdCliente_Select.sql");
	private final String SQL_SELECT_CONTRATOS     = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_ConsultarContratos_Select.sql");
	private final String SQL_SELECT_BANCOS        = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_ConsultarBancos_Select.sql");
	private final String SQL_SELECT_DEPOSITO      = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_ConsultarDepositos_Select.sql");
	private final String SQL_SELECT_RECAUDACION      = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_ConsultarRecaudacion_Select.sql");	
	private final String SQL_UPDATE_ESTADO_RENDICION = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_DetalleRecaudacion_Update.sql");
	private final String SQL_UPDATE_CARGA_MASIVA = loadResourceToString("cl/bcs/APV/sql/ProcesarCargaMasivaRecaudacion_CargaMasiva_Update.sql");
	

	public static class EntradaProcesarCargaMasivaRecaudacion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_accion;
		public Long p_IdCargaMasiva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaProcesarCargaMasivaRecaudacion extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaProcesarCargaMasivaRecaudacion() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class DetalleRecaudacion extends BcsPojo{
		public Long IdCargaMasiva;
		public String Fecha ;
		public String TipoDeposito ;
		public Long FolioCartola ;
		public Long FolioLineaCartola;
		public String Banco ;
		public String Cuenta;
		public BigDecimal Monto ;
		public String PeriodoPago;
		public String TipoPago;
		public String RUTEmpleador;
		public String RUTCliente;
		public String TipoDeAfiliado;
		public String Regimen;
		public String MensajeValidacion;
		public String EstadoRendicion;
		public String EstadoProceso;
		public Long IdDeposito;
				
		public DetalleRecaudacion() {
			this.IdCargaMasiva=0L;
			this.IdDeposito=0L;
			this.MensajeValidacion="";
			this.EstadoProceso="";
			this.EstadoRendicion="";
		}
		
	}
	
	public static class Empleador extends BcsPojo {
		public String DocumentoIdentidad;
		public Long IdPersona;		
		public String PersonaBusqueda;
		
	};
	
	public static class Cliente {
		public String DocumentoIdentidad;
		public Long IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	
	public static class Contrato extends BcsPojo{
		 public String RutCliente;
		 public String RegimenTributario; 
		 public Long   IdContratoSAAPV; 
		 public String   FolioContratoSAAPV; 
		
	}
	
	public static class Banco extends BcsPojo {
		 public Long   IdBanco;
		 public String NombreBanco; 
		 public String NumeroCuentaBancaria; 
		
	}
	

	public static class Deposito extends BcsPojo{
		public Long IdRecaudacion;                  
		public Long IdEmpresa;                      
		public String DetalleEmpleador;               
		public String DetalleCliente;                 
		public Long FolioCartola;                   
		public Long LineaCartola;                   
		public String Sucursal;                       
		public Date FechaDeposito;                  
		public String CuentaCorriente;                
		public BigDecimal MontoDeposito;                  
		public String ConceptoRecaudacion;            
		public String Origen;                         
		public String Observacion;                    
		public Long NumeroDocumento;                
		public String IdTipoDeposito;                 
		public String NombreTipoDeposito;             
		public Long IdBanco;                        
		public String NombreBanco;                    
		public Long IdRegimenTributario;            
		public Long IdCarga;                        
		public String IdEstado;                       
		public String Estado;                         
		
		
	}
	
	public static class EmpleadorRecaudacion extends BcsPojo{		
		public Long IdEmpleador;		
		public Long IdRecaudacion;	
		public String PeriodoPago;
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaProcesarCargaMasivaRecaudacion jin = gson.fromJson(in.toString(), EntradaProcesarCargaMasivaRecaudacion.class);
		SalidaProcesarCargaMasivaRecaudacion out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private SalidaProcesarCargaMasivaRecaudacion logica(Connection conn, EntradaProcesarCargaMasivaRecaudacion in) throws Exception {
		logger.info("INICIO::ProcesarCargaMasivaRecaudacion ");
		long inicioServicio = System.currentTimeMillis();
		SalidaProcesarCargaMasivaRecaudacion salida = new SalidaProcesarCargaMasivaRecaudacion();
		
		List<DetalleRecaudacion> listaDetalleRecaudacion = obtenerDetalleRecaudacion(conn,in);
		
		List<String> documentosIdentidad = listaDetalleRecaudacion.stream()
				.map(docI -> docI.RUTCliente)
				.collect(Collectors.toList());
		
		if("PRE".equals(in.p_accion)) {
			
			Map<String,Empleador> mapEmpleadores = this.obtenerEmpleadores(conn,listaDetalleRecaudacion);
			Map<String,Cliente>   mapCliente = this.obtenerClientes(conn, documentosIdentidad);
			Map<String,Contrato>  mapContrato = this.obtenerContratos(conn, documentosIdentidad);			
			
			List<DetalleRecaudacion> listaDetalleActualizada = listaDetalleRecaudacion.stream().map(detalle->{
				Empleador empleador = mapEmpleadores.get(detalle.RUTEmpleador);
				Cliente cliente = mapCliente.get(detalle.RUTCliente);
				Contrato contrato = mapContrato.get(detalle.RUTCliente+detalle.Regimen);
								
				detalle = this.preProceso(salida, empleador, cliente, contrato, detalle);
				
				return detalle;
				
			}).collect(Collectors.toList());
			
			this.actualizaDetalles(conn, listaDetalleActualizada);
			
			if(salida.P_OUT_MENSAJES.isEmpty()) {
				MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_OK, "PROCESA_CARGA_RECAUDACION_OK","Preproceso de carga exitoso");					
				salida.P_OUT_MENSAJES.add(mensajeSalida);
			}
			
			return salida;
			//QUIZAS HAY QUE ACTUALIZAR LA CARGA GENERAL. Y PONER UN ESTADO QUE SE PUEDE PROCESAR
			//TERMIAN PREPROCESO
			
		}else {
			// si accion es PRO  
			
			List<DetalleRecaudacion> listaIngreso = listaDetalleRecaudacion.stream().filter(detalle->detalle.EstadoProceso.equals("CRT")).collect(Collectors.toList());	
			Map<String,DetalleRecaudacion> mapDetalleDeposito = new HashMap<String,DetalleRecaudacion>();
			Map<String,Banco> mapBancos = this.obtenerBancos(conn, listaIngreso);
			Map<String,Cliente> mapCliente = this.obtenerClientes(conn, documentosIdentidad);
			
			
			for ( DetalleRecaudacion  detalle: listaIngreso) {
				String key = detalle.FolioCartola.toString()+detalle.FolioLineaCartola.toString();
				DetalleRecaudacion detMapDeposito = new DetalleRecaudacion();
				detMapDeposito.IdCargaMasiva	 =detalle.IdCargaMasiva;
				detMapDeposito.Fecha 			 =detalle.Fecha ;
				detMapDeposito.TipoDeposito		 =detalle.TipoDeposito ;
				detMapDeposito.FolioCartola      =detalle.FolioCartola ;
				detMapDeposito.FolioLineaCartola =detalle.FolioLineaCartola;
				detMapDeposito.Banco 		     =detalle.Banco ;
				detMapDeposito.Cuenta		     =detalle.Cuenta;
				detMapDeposito.Monto 		     =detalle.Monto ;
				detMapDeposito.PeriodoPago       =detalle.PeriodoPago;
				detMapDeposito.TipoPago		     =detalle.TipoPago;
				detMapDeposito.RUTEmpleador 	 =detalle.RUTEmpleador;
				detMapDeposito.RUTCliente	 	 =detalle.RUTCliente;
				detMapDeposito.TipoDeAfiliado	 =detalle.TipoDeAfiliado;
				detMapDeposito.Regimen			 =detalle.Regimen;
				detMapDeposito.MensajeValidacion =detalle.MensajeValidacion;
				detMapDeposito.EstadoRendicion   =detalle.EstadoRendicion;
				detMapDeposito.EstadoProceso 	 =detalle.EstadoProceso;
				detMapDeposito.IdDeposito	 	 =detalle.IdDeposito;
				
				if(mapDetalleDeposito.get(key)==null) {									
					mapDetalleDeposito.put(key, detMapDeposito);
				}else {
					BigDecimal montoSumado = mapDetalleDeposito.get(key).Monto.add(detMapDeposito.Monto);
					mapDetalleDeposito.get(key).Monto=montoSumado;
				}
			}
			
			List<Long> listaIdDepositos = this.ingresarDeposito(conn, in, salida, mapDetalleDeposito, mapBancos);
			
			Map<String,Empleador> mapEmpleadores =this.obtenerEmpleadores(conn, listaIngreso);
			Map<String,Deposito> mapRecaudacion = this.obtenerDepositos(conn, listaIdDepositos);
			
			List<Long> listaIdRecaudacionEmpleador= new ArrayList<Long>();
			if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L)==0 && mapEmpleadores.size()>0) {
				//CrearRecaudacionEmpleador
				listaIdRecaudacionEmpleador=  this.ingresarEmpleador(conn, in, salida, mapRecaudacion,listaDetalleRecaudacion, mapEmpleadores);				
			}
			
			if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L)==0) {
				Map<String,EmpleadorRecaudacion> mapEmpleadorRecaudacion = this.obtenerEmpleadorRecaudacion(conn,listaIdRecaudacionEmpleador);
				//CrearReacudacionCliente
				this.ingresarClientes(conn, in, salida, listaIngreso, mapCliente, mapRecaudacion, mapEmpleadorRecaudacion);					
			}
			
			if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L)==0) {
				this.actualizaCargaMasiva(conn, in.p_IdCargaMasiva);
			}
			
			
		}
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaMasivaRecaudacion ", finServicio - inicioServicio);
		return salida;
	}
	

	public List<Long> ingresarEmpleador (Connection conn, EntradaProcesarCargaMasivaRecaudacion in ,SalidaProcesarCargaMasivaRecaudacion out,Map<String,Deposito> mapRecaudacion,List<DetalleRecaudacion> listaDetalleRecaudacion,Map<String,Empleador> mapEmpleadores) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaRecaudacion.ingresarEmpleador ");
		long inicioServicio = System.currentTimeMillis();
		CrearRecaudacionEmpleador recaudacionEmpleador = new CrearRecaudacionEmpleador();
		CrearRecaudacionEmpleador.ParametrosEntradaServicio entradaEmpleador = new CrearRecaudacionEmpleador.ParametrosEntradaServicio();
		List<Long> listaIdRecaudacionEmpleador = new ArrayList<>();
		Map<String,CrearRecaudacionEmpleador.Empleador> hashEmpleador = new HashMap<String,CrearRecaudacionEmpleador.Empleador>();
		
		for( DetalleRecaudacion obj : listaDetalleRecaudacion){
		   
			if("DEP".equals(obj.TipoDeAfiliado)) {
				String key = obj.FolioCartola.toString()+obj.FolioLineaCartola.toString();
				CrearRecaudacionEmpleador.Empleador empleador = new CrearRecaudacionEmpleador.Empleador();
				
					empleador.IdRecaudacionEmpleador  =0L;         
					empleador.IdRecaudacion           =mapRecaudacion.get(key).IdRecaudacion;         
					empleador.IdEmpresa               =in.p_IdEmpresa;         
					empleador.IdPersona               =mapEmpleadores.get(obj.RUTEmpleador).IdPersona;     
					empleador.FechaIngreso            =obj.Fecha;         
					empleador.PeriodoPago             =obj.PeriodoPago;         
					empleador.NumeroHojas             =0L;         
					empleador.TipoAfiliado            =obj.TipoDeAfiliado;         
					empleador.TipoPago                =obj.TipoPago;         
					empleador.Insistencias            ="";         
					empleador.MontoDepositoConvenido  =obj.Regimen.contains("APV")?0L:obj.Monto.longValue();         
					empleador.MontoDepositoAPV        =obj.Regimen.contains("APV")?obj.Monto.longValue():0L;         
					empleador.Reajustes               =0L;         
					empleador.Intereses               =0L;         
					empleador.Origen                  ="CARGA";         
					empleador.Observaciones           ="";         
					empleador.IdEstado                ="ING";  
				if(hashEmpleador.get(key)==null) {
					hashEmpleador.put(key, empleador);
				}else {
					if(hashEmpleador.get(key).IdRecaudacion == mapRecaudacion.get(key).IdRecaudacion){
						if(obj.PeriodoPago.equals(hashEmpleador.get(key).PeriodoPago)) {
							if(obj.Regimen.contains("APV")) {
								hashEmpleador.get(key).MontoDepositoAPV =hashEmpleador.get(key).MontoDepositoAPV+obj.Monto.longValue();
							}
							else {
								hashEmpleador.get(key).MontoDepositoConvenido =hashEmpleador.get(key).MontoDepositoConvenido+obj.Monto.longValue();
							}
						}else {
							hashEmpleador.put(key+obj.PeriodoPago,empleador);
						}
				}				
			}			
		}
		}
		
		for(Map.Entry<String, CrearRecaudacionEmpleador.Empleador> obj : hashEmpleador.entrySet()){			
			entradaEmpleador.p_Parametros.add(obj.getValue());
		}
		
		entradaEmpleador.p_IdEmpresa= in.p_IdEmpresa;
		entradaEmpleador.p_IdModulo= in.p_IdModulo;
		entradaEmpleador.p_IdUsuario= in.p_IdUsuario;
		entradaEmpleador.p_Origen = "CARGA";
		entradaEmpleador.p_Tipo="I";
		
		CrearRecaudacionEmpleador.ParametrosSalidaServicio salidaEmpleador = recaudacionEmpleador.logica(conn, entradaEmpleador);
		out.P_OUT_MENSAJES.addAll(salidaEmpleador.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR=salidaEmpleador.P_OUT_ERRORESCONTADOR!= null?salidaEmpleador.P_OUT_ERRORESCONTADOR:0L;
		
						
				
		salidaEmpleador.P_OUT_RESULTADO.forEach(reca-> {
			listaIdRecaudacionEmpleador.add(reca.IdRecaudacionEmpleador);
		});
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaRecaudacion.ingresarEmpleador ", finServicio - inicioServicio);
		
		return listaIdRecaudacionEmpleador;
	}
	
	public void ingresarClientes (Connection conn, EntradaProcesarCargaMasivaRecaudacion in ,SalidaProcesarCargaMasivaRecaudacion out,List<DetalleRecaudacion> listaDetalles,Map<String,Cliente> mapCliente
			,Map<String,Deposito> mapRecaudacion ,Map<String,EmpleadorRecaudacion> mapEmpleadorRecaudacion) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaRecaudacion.ingresarClientes ");
		long inicioServicio = System.currentTimeMillis();
		
		CrearRecaudacionCliente recaudacionClientes = new CrearRecaudacionCliente();
		CrearRecaudacionCliente.ParametrosEntradaServicio entradaClientes= new CrearRecaudacionCliente.ParametrosEntradaServicio();
		
		listaDetalles.stream().forEach(detalle->{
			CrearRecaudacionCliente.Cliente cliente = new CrearRecaudacionCliente.Cliente();
			String key = detalle.FolioCartola.toString()+detalle.FolioLineaCartola.toString();	
			String Periodo = detalle.PeriodoPago;
			
			cliente.IdRecaudacionEmpleador=0L;
			cliente.IdRecaudacionCliente       =0L; 
			cliente.IdRecaudacion              =mapRecaudacion.get(key).IdRecaudacion; 			
			if(!mapEmpleadorRecaudacion.isEmpty() && "DEP".equals(detalle.TipoDeAfiliado)) {
				cliente.IdRecaudacionEmpleador     =mapEmpleadorRecaudacion.get(cliente.IdRecaudacion.toString()+Periodo).IdEmpleador; 
			}			
			cliente.IdEmpresa                  =in.p_IdEmpresa;
			cliente.IdCliente                  =mapCliente.get(detalle.RUTCliente).IdCliente;
			cliente.FechaIngreso               =detalle.Fecha;
			cliente.TipoAfiliado               =detalle.TipoDeAfiliado;
			cliente.IdRegimenTriburario        =detalle.Regimen;
			cliente.MontoDepositoConvenido     =detalle.Regimen.contains("APV")?BigDecimal.ZERO:detalle.Monto;
			cliente.MontoDepositoAPV           =detalle.Regimen.contains("APV")?detalle.Monto:BigDecimal.ZERO;
			cliente.Origen                     ="CARGA";
			cliente.IdEstado                   ="ING";		
			
			entradaClientes.p_Parametros.add(cliente);
			
		});	
		
		
		entradaClientes.p_IdEmpresa= in.p_IdEmpresa;
		entradaClientes.p_IdModulo= in.p_IdModulo;
		entradaClientes.p_IdUsuario= in.p_IdUsuario;
		entradaClientes.p_Origen = "CARGA";
		entradaClientes.p_Tipo="I";
		
		

		CrearRecaudacionCliente.ParametrosSalidaServicio salidaCliente = recaudacionClientes.logica(conn, entradaClientes);
		out.P_OUT_MENSAJES.addAll(salidaCliente.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR=salidaCliente.P_OUT_ERRORESCONTADOR!= null?salidaCliente.P_OUT_ERRORESCONTADOR:0L;
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaRecaudacion.ingresarClientes ", finServicio - inicioServicio);
	}
	

	
	public List<Long> ingresarDeposito (Connection conn, EntradaProcesarCargaMasivaRecaudacion in ,SalidaProcesarCargaMasivaRecaudacion out, Map<String,DetalleRecaudacion> mapIngreso ,Map<String,Banco> mapBancos) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaRecaudacion.ingresarDeposito ");
		long inicioServicio = System.currentTimeMillis();
		
		List<Long> listaIdDepositos = new ArrayList<>();
		CrearRecaudacionDeposito depositos = new CrearRecaudacionDeposito();
		CrearRecaudacionDeposito.ParametrosEntradaServicio entradaDepositos = new CrearRecaudacionDeposito.ParametrosEntradaServicio();
		
		for(Map.Entry<String, DetalleRecaudacion> obj : mapIngreso.entrySet()){
			CrearRecaudacionDeposito.Recaudacion deposito = new CrearRecaudacionDeposito.Recaudacion();
			
			Banco banco = mapBancos.get(obj.getValue().Banco+obj.getValue().Cuenta);
			
			deposito.IdRecaudacion   = 0L;                     
			deposito.IdEmpresa       = in.p_IdEmpresa;                        
			deposito.FolioCartola    = obj.getValue().FolioCartola;                       
			deposito.LineaCartola    = obj.getValue().FolioLineaCartola;                       
			deposito.Sucursal        = "";                       
			deposito.FechaDeposito   = obj.getValue().Fecha;                      
			deposito.CuentaCorriente = obj.getValue().Cuenta;                     
			deposito.MontoDeposito   = obj.getValue().Monto;                      
			deposito.ConceptoRecaudacion = "";                  
			deposito.Origen          = "";                       
			deposito.Observacion     = "";                    
			deposito.NumeroDocumento = 0L;                     
			deposito.IdTipoDeposito  = obj.getValue().TipoDeposito;                    
			deposito.IdBanco         = banco !=null? banco.IdBanco:0L; 
			deposito.IdCarga         = 0L;                      
			deposito.IdRegimenTributario = 0L;                 
			deposito.IdEstado        = "ING";	
			
			entradaDepositos.p_Parametros.add(deposito);
		}
		
		entradaDepositos.p_IdEmpresa=in.p_IdEmpresa;
		entradaDepositos.p_IdModulo=in.p_IdModulo;
		entradaDepositos.p_IdUsuario= in.p_IdUsuario;
		entradaDepositos.p_Tipo= "I";
		entradaDepositos.p_Origen="";
		
		
		CrearRecaudacionDeposito.ParametrosSalidaServicio salidaDepositos = depositos.logica(conn, entradaDepositos); 
		out.P_OUT_MENSAJES.addAll(salidaDepositos.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR=salidaDepositos.P_OUT_ERRORESCONTADOR;
		
		if(out.P_OUT_ERRORESCONTADOR.compareTo(0L)==0) {
			out.P_OUT_MENSAJES.forEach(mensaje->{			
				if("UI_CREACION_DEPOSITO_OK".equals(mensaje.codigo)) {
					listaIdDepositos.add(mensaje.id);
				}
			});
		}
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaRecaudacion.ingresarDeposito ", finServicio - inicioServicio);
		return listaIdDepositos;
	}
	
	
	public boolean validaEmpleador(String empleadorArchivo, Empleador empleadorValidado, String clienteArchivo , String tipoAfiliado) {
		boolean validado= false;
		
		if("IND".equals(tipoAfiliado)) {
			if(empleadorArchivo.equals(clienteArchivo)) {
				validado=true;
			}
		}else {
			if(empleadorValidado!= null && empleadorArchivo.equals(empleadorValidado.DocumentoIdentidad)) {
				validado=true;
			}			
		}		
		return validado;
	}

	public boolean validaCliente (String clienteArchivo , Cliente clienteValidado ) {		
		boolean validado= false;
		
		if(clienteValidado!= null && clienteArchivo.equals(clienteValidado.DocumentoIdentidad)) {
			validado=true;
		}	
		
		return validado;
	}

	public DetalleRecaudacion preProceso (SalidaProcesarCargaMasivaRecaudacion salida ,Empleador empleador, Cliente cliente , Contrato contrato, DetalleRecaudacion detalle) {
		logger.trace("INICIO::ProcesarCargaMasivaRecaudacion.preProceso ");
		long inicioServicio = System.currentTimeMillis();
		boolean certificado = true;
		
		if(!validaCliente(detalle.RUTCliente,cliente)) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "PROCESA_CARGA_RECAUDACION_CLIENTE_NO_VALIDO","Cliente rut "+detalle.RUTCliente+" no valido ");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);							
			//buscar contratos del cliente 
			detalle.EstadoRendicion="NO OK";
			detalle.MensajeValidacion+= " Cliente Informado no existe, ";	
			certificado= false;
			detalle.EstadoProceso="ADV";
			
		}

		if(!validaEmpleador(detalle.RUTEmpleador,empleador, detalle.RUTCliente,detalle.TipoDeAfiliado)) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "PROCESA_CARGA_RECAUDACION_EMPLEADOR_NO_VALIDO","Empleador rut "+detalle.RUTEmpleador+" no valido ");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
			detalle.EstadoRendicion="NO OK";
			detalle.MensajeValidacion+= " Empleador Informado no existe ";
			certificado= false;
			detalle.EstadoProceso="NOK";
		}				
		
		if(!("NORMAL".equalsIgnoreCase(detalle.TipoPago) || "ATRASADO".equalsIgnoreCase(detalle.TipoPago))) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "PROCESA_CARGA_RECAUDACION_TIPO_PAGO_NO_VALIDO","Cliente rut "+detalle.TipoPago+" no valido ");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
			certificado= false;
		}
		
		//Quizas Validar cuenta Bancaria 
		detalle.EstadoRendicion=certificado ==true ?"OK":detalle.EstadoProceso;	
		detalle.EstadoProceso=certificado ==true ?"CRT":detalle.EstadoProceso;	
		
		long finServicio = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaRecaudacion.actualizaDetalles ", finServicio - inicioServicio);
		
		return detalle;
	}
	
	public void actualizaDetalles ( Connection conn,List<DetalleRecaudacion> listaDetalleActualizada ) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaRecaudacion.actualizaDetalles ");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		
		sql.batch(conn, SQL_UPDATE_ESTADO_RENDICION,listaDetalleActualizada,DetalleRecaudacion.class);		
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaRecaudacion.actualizaDetalles ", finServicio - inicioServicio);
	}
	
	public void actualizaCargaMasiva( Connection conn,Long idCargaMasiva  ) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaRecaudacion.actualizaCargaMasiva ");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();	
		List<DetalleRecaudacion> listaIdCarga = new ArrayList<DetalleRecaudacion>();
		DetalleRecaudacion carga = new DetalleRecaudacion();
		carga.IdCargaMasiva= idCargaMasiva;
		listaIdCarga.add(carga);
		
		sql.batch(conn, SQL_UPDATE_CARGA_MASIVA,listaIdCarga ,DetalleRecaudacion.class);
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaRecaudacion.actualizaCargaMasiva ", finServicio - inicioServicio);
	}
	
	public Map<String,Banco> obtenerBancos (Connection conn , List<DetalleRecaudacion> listaIngreso) throws Exception{
		logger.debug("INICIO::ProcesarCargaMasivaRecaudacion.obtenerBancos ");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		Map<String,Banco> mapBanco = new HashMap<String,Banco>();
		List<String> nombresBanco  = new ArrayList<String>();
		List<String> cuentaBanco   = new ArrayList<String>();
		listaIngreso.forEach(detalle->{
			nombresBanco.add(detalle.Banco);
			cuentaBanco.add(detalle.Cuenta);
		});
		
		
		HashMap<String, Object> params = new HashMap<String, Object>();		
		params.put("NombreBanco", nombresBanco);		
		params.put("NumeroCuenta", cuentaBanco);		
		
		List<Banco> listaBancos= sql.query(conn, SQL_SELECT_BANCOS,params,Banco.class);
		
		listaBancos.forEach(banco->{
			String key = banco.NombreBanco+banco.NumeroCuentaBancaria;
			mapBanco.put(key, banco);
		});
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaRecaudacion.obtenerBancos ", finServicio - inicioServicio);
		return mapBanco;
	}
	
	
	public List<DetalleRecaudacion> obtenerDetalleRecaudacion ( Connection conn,EntradaProcesarCargaMasivaRecaudacion in ) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaRecaudacion.obtenerDetalleRecaudacion ");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();		
		params.put("idCargaMasiva", in.p_IdCargaMasiva);		
		
		List<DetalleRecaudacion> listaDetalle = sql.query(conn, SQL_SELECT_CARGA_DETALLE,params,DetalleRecaudacion.class);
		
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaRecaudacion.obtenerDetalleRecaudacion ", finServicio - inicioServicio);
		return listaDetalle;
	}
	
	public Map<String,Empleador> obtenerEmpleadores(Connection conn,List<DetalleRecaudacion> archivoRecaudaciones ) throws Exception {		
		logger.info("INICIO::ProcesarCargaMasivaRecaudacion.obtenerEmpleadores");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<String> documentosIdentidad = archivoRecaudaciones.stream()
				.map(docI -> docI.RUTEmpleador)
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
	    parametros.put("DocumentoIdentidad", documentosIdentidad);		
		
		Map<String,Empleador> empleadores = sql.query(conn, SQL_SELECT_EMPLEADOR,parametros,Empleador.class)
				.stream().collect(Collectors.toMap(emple -> emple.DocumentoIdentidad, emple -> emple));
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaMasivaRecaudacion.obtenerEmpleadores {}", finServicio - inicioServicio);
		return empleadores;		
	}
	public Map<String,Cliente> obtenerClientes(Connection conn,List<String> documentosIdentidad ) throws Exception {
		logger.info("INICIO::ProcesarCargaMasivaRecaudacion.obtenerClientes");
		long inicioServicio = System.currentTimeMillis();
		
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
	    parametros.put("DocumentoIdentidad", documentosIdentidad);
		SQL sql = new SQL();
		
		
		Map<String, Cliente> clientes = sql.query(conn, SQL_SELECT_CLIENTE,parametros,Cliente.class)
				.stream().collect(Collectors.toMap(cliente -> cliente.DocumentoIdentidad,cliente -> cliente ));
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaMasivaRecaudacion.obtenerClientes {}", finServicio - inicioServicio);
		return clientes;		
	}
	
	public Map<String,Deposito> obtenerDepositos(Connection conn,List<Long> listaIdDepositos ) throws Exception {
		logger.info("INICIO::ProcesarCargaMasivaRecaudacion.obtenerDepositos");
		long inicioServicio = System.currentTimeMillis();
		
		Map<String,Deposito> mapDepositos = new HashMap<String,Deposito>();
		HashMap<String, Object> parametros = new HashMap<String, Object>();
	    parametros.put("IdRecaudacion", listaIdDepositos);
		SQL sql = new SQL();
		
		List <Deposito> listaDepositosIngresados = sql.query(conn, SQL_SELECT_DEPOSITO,parametros,Deposito.class);
		
		listaDepositosIngresados.forEach(deposito -> {
			String key = deposito.FolioCartola.toString()+deposito.LineaCartola.toString();			
			mapDepositos.put(key, deposito);
		});
				
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaMasivaRecaudacion.obtenerDepositos {}", finServicio - inicioServicio);
		return mapDepositos;		
	}
	
	public Map<String,EmpleadorRecaudacion> obtenerEmpleadorRecaudacion(Connection conn,List<Long> listaIdRecaudacionEmpleador) throws Exception {
		logger.info("INICIO::ProcesarCargaMasivaRecaudacion.obtenerEmpleadorRecaudacion");
		long inicioServicio = System.currentTimeMillis();
		
		Map<String,EmpleadorRecaudacion> mapEmpleadorRecaudacion = new HashMap<String,EmpleadorRecaudacion>();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
	    parametros.put("IdRecaudacionEmpleador", listaIdRecaudacionEmpleador);
		SQL sql = new SQL();
		
		List <EmpleadorRecaudacion> listaEmpleadorRecaudacion = sql.query(conn, SQL_SELECT_RECAUDACION,parametros,EmpleadorRecaudacion.class);
		
		listaEmpleadorRecaudacion.forEach(EmpReca -> {
			String key = EmpReca.IdRecaudacion.toString()+EmpReca.PeriodoPago;
			mapEmpleadorRecaudacion.put(key, EmpReca);
		});
				
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaMasivaRecaudacion.obtenerEmpleadorRecaudacion {}", finServicio - inicioServicio);
		return mapEmpleadorRecaudacion;		
	}
	
	
	public Map<String,Contrato> obtenerContratos(Connection conn,List<String> documentosIdentidad ) throws Exception {
		logger.info("INICIO::ProcesarCargaMasivaRecaudacion.obtenerContratos");
		long inicioServicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		Map<String, Contrato> contratos = new HashMap<String, Contrato>();
	    parametros.put("RutCliente", documentosIdentidad);
		SQL sql = new SQL();
		
		List<Contrato> listaContratos = sql.query(conn, SQL_SELECT_CONTRATOS,parametros,Contrato.class);
		
		listaContratos.forEach(contrato->{
			String key = contrato.RutCliente.toString()+contrato.RegimenTributario;
			contratos.put(key, contrato);
		});
	    
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaMasivaRecaudacion.obtenerContratos {}", finServicio - inicioServicio);
		return contratos;		
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			ProcesarCargaMasivaRecaudacion CMR = new ProcesarCargaMasivaRecaudacion();
			try {
				EntradaProcesarCargaMasivaRecaudacion Objin = new EntradaProcesarCargaMasivaRecaudacion();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":70830,\"p_IdModulo\":\"APV\",\"p_accion\":\"PRO\",\"p_IdCargaMasiva\":1528}"; 
				
				Objin = gson.fromJson(json, EntradaProcesarCargaMasivaRecaudacion.class);
				

				try {

					SalidaProcesarCargaMasivaRecaudacion ObjOut = CMR.logica(con, Objin);
					System.out.println(ObjOut);
					con.rollback(); 
					//con.commit();
				} catch (Exception er) {
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
