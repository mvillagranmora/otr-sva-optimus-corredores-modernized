package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoEntrada;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.CuentaInversion;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDEVENTO;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ServiceEntrada;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ServiceSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CertificarCargoAbono implements JavaCall{

	
	private final String SQL_SELECT_CARGO_ABONO_A_CERTIFICAR = loadResourceToString("cl/bcs/cuentaInversion/sql/CertificarCargoAbono_Select_Cargo_Abono_A_Certificar.sql");
	private final String SQL_UPDATE_MOVIMIENTOS_CERTIFICADOS = loadResourceToString("cl/bcs/cuentaInversion/sql/CertificarCargoAbono_Update_Movimientos_Certificados.sql");
	private final String SQL_INSERT_ID_CARGO_ABONO_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/CertificarCargoAbono_Insert_IdCargoAbono_Tabla_Paso.sql");
	private final String SQL_INFO_EXTRA_NOTIFICACION = loadResourceToString("cl/bcs/cuentaInversion/sql/CertificarCargoAbono_Select_Info_Extra_Notificacion.sql");
	//private final String SQL_DELETE_ID_CARGO_ABONO_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/CertificarCargoAbono_Delete_IdCargoAbono_Tabla_Paso.sql");
	private final static Logger logger = LogManager.getLogger(CertificarCargoAbono.class);
	
	public static class CertificarCargoAbonoEntrada {
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento;
		public Long p_IdPais;
		public List<InCargosAbonosACertificar> p_CargosAbonosACertificar;
		
		public CertificarCargoAbonoEntrada(){
			this.p_CargosAbonosACertificar = new ArrayList<InCargosAbonosACertificar>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InCargosAbonosACertificar{
		public Long IdCargoAbonoMovimiento;
	}
	
	public static class CargoAbonoACertificar{
		public Integer IdSesion;
        public Long IdInterno;
        public Long IdSecuencia;
        public Long IdCuentaInversion;
        public Long IdPortafolio;
        public String IdMoneda;
        public Long IdCargoAbonoMovimiento;           
        public String IdCondicionDeLiquidacion;
        public int PlazoEnDias;
        public Date FechaEfectivaMovimiento;
        public Date FechaSistema;
        public Timestamp FechaIngresoRegistro;
        public String FechaCalculada;
        public String IdTipoMovimiento;
        public String EsCargoAbonoPorTraspaso;
        public Long IdCargoAbonoRelacionadoTraspaso;
        public BigDecimal Monto;
        public String IdEstado;
        public String Informado;
        

        
        public String CodigoPortafolio;
		public String NombreCompleto;
		public Long IdPersona;
		public String DocumentoIdentidad;
		public Long	IdDireccionElectronicaPrincipal;
		public String IdTipoCliente;
		public String TelCliente;	
		public String TipoDocumento;
		public String NombreOperacion;
		public String NombreConcepto;
		public String Correo;
		public String Agente;
        /* VALIDACIONES */
		public Long Errores = 0L;
		public Long Ok = 0L;
		

	    //Para validacion flujo caja
		public BigDecimal	TotalMontoPortafolio = BigDecimal.ZERO;
		public BigDecimal 	SaldoCajaPortafolio = BigDecimal.ZERO;
		
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//jvillagran ... cambio 111
	public static class FlujoCajaElem {
		String IdMoneda;
		String Fecha;
		Long IdPortafolio;
		
		public FlujoCajaElem(String moneda,String fecha,Long idPortafolio) {
			this.IdMoneda = moneda;
			this.Fecha = fecha;
			this.IdPortafolio = idPortafolio;
		}
		
		@Override
		public boolean equals(Object o) {
			FlujoCajaElem e = (FlujoCajaElem)o;
			return this.IdMoneda.equalsIgnoreCase(e.IdMoneda) && this.Fecha.equalsIgnoreCase(e.Fecha) && this.IdPortafolio.equals(e.IdPortafolio);
		}
		
		@Override
		public int hashCode() {
			return (this.IdMoneda + this.Fecha + this.IdPortafolio).hashCode();
		}
		
	}
	
	public static class InfoExtraNotificacion {
		public Long IdPersona; 
	    public Long IdDireccionElectronica;
	    public String RazonCorredora;
	    public String DireccionCorredora;
	    public String RutCorredora;
	    public String TelefonoCorredora;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CertificarCargoAbonoSalida {
		public Long	P_OUT_ERRORESCONTADOR;
		public List<Detalle> P_OUT_ERRORESDETALLE;
		public Long	P_OUT_INFORMACIONCONTADOR;
		public List<Detalle> P_OUT_INFORMACIONDETALLE;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<Detalle> P_OUT_ADVERTENCIASDETALLE;

		public CertificarCargoAbonoSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<Detalle>();
			this.P_OUT_INFORMACIONCONTADOR = 0L;
			this.P_OUT_INFORMACIONDETALLE = new ArrayList<Detalle>();
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<Detalle>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Detalle {
		public Long IdCargoAbonoMovimiento;
		public String Tipo;
		public String Codigo;
		public String Descripcion;
		
		public Detalle() {
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Detalle(Long id,String tipo,String codigo,String descripcion) {
			this.IdCargoAbonoMovimiento = id;
			this.Tipo = tipo;
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}
	
	
	public static class ParamsCargoAbono_Update{
		public Long p_IdUsuario;
		public String p_FechaCalculada;
		public String p_FechaHoraSistema;
		public BigDecimal p_Monto;   
		public Long p_IdCargoAbonoMovimiento;
		public Long p_IdEmpresa;
	}
	
	public static class ParamsCuentaInversion_Insert{
		public Long IdEjecucion;
		public Long IdCuentaInversion;
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String codValidacion,CargoAbonoACertificar ca,CertificarCargoAbonoSalida out) 
			throws Exception {
		if (esError) {
			String msg= Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdCargoAbonoMovimiento", ca.IdCargoAbonoMovimiento)
				  

				  .add("DocumentoIdentidad", ca.DocumentoIdentidad)
				  
				  
				  .msg();
			if (codValidacion.equals(MensajeSalida.MENSAJE_ERROR)) {
				out.P_OUT_ERRORESDETALLE.add(new Detalle(ca.IdInterno,codValidacion,codMsg,msg));
				out.P_OUT_ERRORESCONTADOR++;
				ca.Errores++;
			}else{
				ca.Ok++;
			}
		}		
	}
	
	
	public void limpiarCargosAbonos(List<CargoAbonoACertificar> cargosAbonos, List<CargoAbonoACertificar> cargosAbonosConErrores)  throws Exception
	{
				
		for (CargoAbonoACertificar ca : cargosAbonos) {
			if (ca.Errores > 0) {
				cargosAbonosConErrores.add(ca);
			}
		}
		
		
		HashMap<Long, Object> hashCargosAbonos = Utils.List2Hash(cargosAbonos, "IdCargoAbonoMovimiento");
		
		for (CargoAbonoACertificar ca : cargosAbonosConErrores) {
			if (cargosAbonos.contains(ca))
				cargosAbonos.remove(ca);			
			
			//Tambien remueve movimiento relacionado cuando es un traspaso
			if (ca.EsCargoAbonoPorTraspaso.equalsIgnoreCase("S") && ca.IdCargoAbonoRelacionadoTraspaso.compareTo(0L) > 0)
			{
				CargoAbonoACertificar cargoAbonoRelacionado = (CargoAbonoACertificar) hashCargosAbonos.get(ca.IdCargoAbonoRelacionadoTraspaso);
				
				if (cargosAbonos.contains(cargoAbonoRelacionado))
					cargosAbonos.remove(cargoAbonoRelacionado);
				
			}			
		}
	}
	
	public CertificarCargoAbonoSalida logica(Connection con, CertificarCargoAbonoEntrada entrada) throws Exception {

		logger.info("<<INICIO::CertificarCargoAbono>>");
		SQL sql = new SQL();
		CertificarCargoAbonoSalida out = new CertificarCargoAbonoSalida();
		
		String pValidaFlujoCaja = "N";
		try 
		{			
			pValidaFlujoCaja = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUENTAINVERSION", "VALIDAFLUJOCAJAENCERTIFICACION");
		}
		catch(Exception er) 
		{
			pValidaFlujoCaja  = "N";
		}
		
		try {				
			logger.debug("INICIO:: Insert IdCargoAbonoMovimiento Tabla de paso");
			sql.batch(con, SQL_INSERT_ID_CARGO_ABONO_TABLA_PASO, entrada.p_CargosAbonosACertificar, InCargosAbonosACertificar.class);
			logger.debug("FIN:: Insert IdCargoAbonoMovimiento Tabla de paso");
			
			List<CargoAbonoACertificar> listaCargoAbono = new ArrayList<>();
			HashMap<String,Object> parmsConsulta = new HashMap<>();
			parmsConsulta.put("p_IdEmpresa", entrada.p_IdEmpresa);
			
			logger.debug("INICIO:: Select Cargo Abono a Certificar");
			listaCargoAbono = sql.query(con, SQL_SELECT_CARGO_ABONO_A_CERTIFICAR, parmsConsulta, CargoAbonoACertificar.class);
			logger.debug("FIN:: Select Cargo Abono a Certificar");
			
			if(listaCargoAbono.isEmpty()){
				out.P_OUT_ERRORESDETALLE.add(new Detalle(0L, MensajeSalida.MENSAJE_ERROR, "NO_HAY_MOVIMIENTOS", "No se encontraron Movimientos"));
				return out;
			}
			
			//1) Parametros para ajustar fecha de Liquidacion
			ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
		    InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = entrada.p_IdPais;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
			
			List<CargoAbonoACertificar> cargosAbonosConErrores = new ArrayList<>();
			
			if(("S").equalsIgnoreCase(entrada.p_UsaReglasNegocio)){
				
				RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
				logger.debug("INICIO:: Revisar Requisitos Basicos");
				RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
				logger.debug("FIN:: Revisar Requisitos Basicos");
				
				if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
					for(MensajeSalida salida : salidaBasicos.p_out_Mensajes){
						out.P_OUT_ERRORESCONTADOR++;
						out.P_OUT_ERRORESDETALLE.add(new Detalle(0L, salida.tipo, salida.codigo, salida.descripcion));
					}
					return out;
				}
				
				// ------------------------------------------------------------------
				// VALIDACIONES BASICAS
				logger.debug("INICIO::Validacion otros requisitos");
				

				for(CargoAbonoACertificar cargoAbono : listaCargoAbono) 
				{
					ProcesaRequisito(con,entrada.p_IdEmpresa,(cargoAbono.FechaSistema == null),"FECHA_SISTEMA_INVALIDA",MensajeSalida.MENSAJE_ERROR,cargoAbono,out);
					ProcesaRequisito(con,entrada.p_IdEmpresa,(cargoAbono.IdCargoAbonoMovimiento == null),"MOVIMIENTO_CARGO_ABONO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,cargoAbono,out);
					ProcesaRequisito(con,entrada.p_IdEmpresa,(cargoAbono.IdEstado.equals("CRT")),"MOVIMIENTO_CARGO_ABONO_CERTIFICADO",MensajeSalida.MENSAJE_ERROR,cargoAbono,out);
					ProcesaRequisito(con,entrada.p_IdEmpresa,(cargoAbono.IdEstado.equals("ANU")),"MOVIMIENTO_CARGO_ABONO_ANULADO",MensajeSalida.MENSAJE_ERROR,cargoAbono,out);
					ProcesaRequisito(con,entrada.p_IdEmpresa,(cargoAbono.IdTipoMovimiento.equals("MOVABN") && cargoAbono.EsCargoAbonoPorTraspaso.equals("S") && cargoAbono.Informado.equals("S")),"MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA",MensajeSalida.MENSAJE_ERROR,cargoAbono,out);				
					
					FechasIn f = new FechasIn();
					f.fechaInicial = entrada.p_FechaMovimiento;
					f.numeroDias = (long) cargoAbono.PlazoEnDias;
					inObtenerDiaSiguiente.p_fechas.add(f);
			
					OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(con, inObtenerDiaSiguiente);				
					cargoAbono.FechaCalculada = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;
					inObtenerDiaSiguiente.p_fechas.clear();					
				}
				
				//  jvillagran ... cambio 111
				logger.debug("FIN::Validacion otros requisitos");	
				
				
				// ------------------------------------------------------------------
				// VALIDACION FLUJO CAJA (por total de cargos)
				
				if (pValidaFlujoCaja.equalsIgnoreCase("S"))
				{
				
					logger.debug("INICIO::Validacion Flujo Caja para Cargos");				
					
					HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
					HashMap<FlujoCajaElem,BigDecimal> totalCargosPortafolio = new HashMap<>();
					
		
					HashMap<FlujoCajaElem,BigDecimal> totalFlujoCajaPortafolio = new HashMap<>();
					
					
					java.sql.Date fechaCalculadaMinima = Fecha.toDate("2099-09-09");
					java.sql.Date fechaCalculadaMaxima = Fecha.toDate("0001-01-01");
					
					for(CargoAbonoACertificar cargoAbono : listaCargoAbono) {					
						if(cargoAbono.IdTipoMovimiento.equalsIgnoreCase("MOVCAR"))
						{
							java.sql.Date fechaCalculada =  Fecha.toDate(cargoAbono.FechaCalculada);
							
							if (fechaCalculada.before(fechaCalculadaMinima)) { // Antes
								fechaCalculadaMinima = fechaCalculada;
							}
							
							if (fechaCalculada.after(fechaCalculadaMaxima)) {  // Despues
								fechaCalculadaMaxima = fechaCalculada;
							}
							
							FlujoCajaElem llave = new FlujoCajaElem(cargoAbono.IdMoneda, cargoAbono.FechaCalculada,cargoAbono.IdPortafolio);
							if (!totalCargosPortafolio.containsKey(llave)) {
								totalCargosPortafolio.put(llave, BigDecimal.ZERO);
							}
							BigDecimal totalCargos = totalCargosPortafolio.get(llave);
							totalCargos = totalCargos.add(cargoAbono.Monto);
							totalCargosPortafolio.put(llave, totalCargos);
							
							SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
							port.IdMoneda = cargoAbono.IdMoneda;
							port.IdPortafolio = cargoAbono.IdPortafolio;
							portafoliosFlujoCaja.add(port);	
						}				
					}	
					

				    

			        

				    
				    if (portafoliosFlujoCaja.size() > 0)
				    {					
						ServiceEntrada entradaFlujoCaja = new ServiceEntrada();
						entradaFlujoCaja.P_IDEMPRESA 	= entrada.p_IdEmpresa;
						entradaFlujoCaja.P_FECHADESDE 	=  Fecha.toStringDate(fechaCalculadaMinima);
						entradaFlujoCaja.P_FECHAHASTA 	=  Fecha.toStringDate(fechaCalculadaMaxima);					
						entradaFlujoCaja.P_USO 			= "CUENTA_INVERSION";					
						entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(portafoliosFlujoCaja);
	
						SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
						ServiceSalida salidaFlujoCaja = saldoFlujoCaja.logica(con, entradaFlujoCaja);
						
						
						List<ObjFLUJOCAJA> asdf = salidaFlujoCaja.P_OUT_FLUJOCAJA;
						for (ObjFLUJOCAJA flj : asdf) {				

						
							FlujoCajaElem llave = new FlujoCajaElem(flj.IdMoneda, flj.FechaSaldo,flj.IdPortafolio);
							if (!totalFlujoCajaPortafolio.containsKey(llave)) {
								totalFlujoCajaPortafolio.put(llave, BigDecimal.ZERO);
							}
							BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);
							totalFlujoCaja = totalFlujoCaja.add(flj.Saldo);
							totalFlujoCajaPortafolio.put(llave, totalFlujoCaja);
				        	
						}					
					}
					
				    
				    
				    //Actualizamos totales por grupo (portafolio,moneda,fecha)
			        for(CargoAbonoACertificar cargoAbono : listaCargoAbono) {	
			        	
			        	FlujoCajaElem llave = new FlujoCajaElem(cargoAbono.IdMoneda, cargoAbono.FechaCalculada,cargoAbono.IdPortafolio);
			        	BigDecimal totalCargos = totalCargosPortafolio.get(llave);
			        	
			        	BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);
			        	
			        	if (totalCargos != null)			        	
			        		cargoAbono.TotalMontoPortafolio  = totalCargos;
			        	
			        	if (totalFlujoCaja != null)
			        		cargoAbono.SaldoCajaPortafolio = totalFlujoCaja;
			        }
			        
				    
				    for (CargoAbonoACertificar cargoAbono : listaCargoAbono)
					{
						//Valida Saldo flujo caja solo para cargos
				    	ProcesaRequisito(con, entrada.p_IdEmpresa,( cargoAbono.IdTipoMovimiento.equalsIgnoreCase("MOVCAR") &&  cargoAbono.TotalMontoPortafolio.doubleValue() > cargoAbono.SaldoCajaPortafolio.doubleValue()),"FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO",MensajeSalida.MENSAJE_ERROR,cargoAbono,out);
					}
					
				    logger.debug("FIN::Validacion Flujo Caja para Cargos");
				} // if (pValidaFlujoCaja.equalsIgnoreCase("S"))
				
		
			}
			
			
			limpiarCargosAbonos(listaCargoAbono, cargosAbonosConErrores);
			if (listaCargoAbono.size() == 0) {
				return out;
			}
			
			//3) Actualizar Movimientos Certificados
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con,entrada.p_IdEmpresa);
			
			List<ParamsCargoAbono_Update> update_CargoAbonoMovimiento = new ArrayList<ParamsCargoAbono_Update>();
		
			for (CargoAbonoACertificar cargoAbono : listaCargoAbono){
				ParamsCargoAbono_Update parms_saldo_movimiento = new ParamsCargoAbono_Update();
				parms_saldo_movimiento.p_FechaCalculada = cargoAbono.FechaCalculada;
				parms_saldo_movimiento.p_FechaHoraSistema = fechaHoraSistema;
				parms_saldo_movimiento.p_IdCargoAbonoMovimiento = cargoAbono.IdCargoAbonoMovimiento;
				parms_saldo_movimiento.p_IdEmpresa = entrada.p_IdEmpresa;
				parms_saldo_movimiento.p_IdUsuario = entrada.p_IdUsuario;
				parms_saldo_movimiento.p_Monto = cargoAbono.Monto;
				update_CargoAbonoMovimiento.add(parms_saldo_movimiento);
			
				String codigo = cargoAbono.IdTipoMovimiento.equalsIgnoreCase("MOVCAR") ? "UI_CARGO_CERTIFICADO": "UI_ABONO_CERTIFICADO";
				String descripcion = cargoAbono.IdTipoMovimiento.equalsIgnoreCase("MOVCAR") ? "Cargo N°"+ cargoAbono.IdCargoAbonoMovimiento +" certificado" : "Abono N°"+ cargoAbono.IdCargoAbonoMovimiento +" certificado";
				out.P_OUT_INFORMACIONDETALLE.add(new Detalle(cargoAbono.IdCargoAbonoMovimiento, MensajeSalida.MENSAJE_INFORMACION,codigo, descripcion));
				out.P_OUT_INFORMACIONCONTADOR++;
			}
			
			logger.debug("INICIO::Update Movimienos Certificados");
			sql.batch(con,SQL_UPDATE_MOVIMIENTOS_CERTIFICADOS,update_CargoAbonoMovimiento, ParamsCargoAbono_Update.class);
			logger.debug("FIN::Update Movimientos Certificados");	
			
			if (!listaCargoAbono.isEmpty()) {
						
				AgregarLog agregarLog = new AgregarLog();
				InAgregarLog entradaLog = new InAgregarLog();
			
				for (CargoAbonoACertificar cargoAbono : listaCargoAbono) {

					InAgregarLog.Log headLog = new InAgregarLog.Log();

					headLog.NombreProceso = "Certificación Cargo Abono";
					headLog.IdCabeceraLog = cargoAbono.IdCargoAbonoMovimiento != null ? cargoAbono.IdCargoAbonoMovimiento : 0L;
					headLog.IdTipoLog = "CERTIFICACION"; 
					headLog.IdModulo = entrada.p_IdModulo;
					headLog.IdUsuario = entrada.p_IdUsuario;
					headLog.IdEmpresa = entrada.p_IdEmpresa;
					headLog.FechaSistemaLog = entrada.p_FechaMovimiento != null ? entrada.p_FechaMovimiento : "";
					headLog.IdUsuarioAtendido = entrada.p_IdUsuario;
					headLog.IdCliente = 0L;
					headLog.Nemo = cargoAbono.NombreConcepto != null ? cargoAbono.NombreConcepto : "";
					headLog.IdPortafolio = cargoAbono.IdPortafolio != null ? cargoAbono.IdPortafolio : 0L;
					headLog.IdTipoOperacion = cargoAbono.IdTipoMovimiento != null ? cargoAbono.IdTipoMovimiento : "";
					headLog.Folio = cargoAbono.IdCargoAbonoMovimiento != null ? cargoAbono.IdCargoAbonoMovimiento : 0L;
					headLog.Monto = cargoAbono.Monto != null ? cargoAbono.Monto : BigDecimal.ZERO;
					headLog.IdMonedaOrigen = cargoAbono.IdMoneda != null ? cargoAbono.IdMoneda : "";

					if ("MOVABN".equals(cargoAbono.IdTipoMovimiento)) {
						headLog.IdTipoRegistro = "ABONO_MOVIMIENTO";
					}

					if ("MOVCAR".equals(cargoAbono.IdTipoMovimiento)) {
						headLog.IdTipoRegistro = "CARGO_MOVIMIENTO";
					}

					entradaLog.p_Log.add(headLog);
				}

				agregarLog.logica(con, entradaLog);
			}
				
						
			for(CargoAbonoACertificar cargoAbono : listaCargoAbono){
				CuentaInversion cuentaInversion = new CuentaInversion();
				cuentaInversion.IdCuentaInversion = cargoAbono.IdCuentaInversion;
						
				ActualizarSaldoCargoAbono entradaSaldoServicio = new ActualizarSaldoCargoAbono();
				ActualizarSaldoCargoAbonoEntrada entradaSaldo = new ActualizarSaldoCargoAbonoEntrada(); 
				entradaSaldo.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaSaldo.p_IdUsuario = entrada.p_IdUsuario;
				entradaSaldo.p_FechaMovimiento = entrada.p_FechaMovimiento;		
				entradaSaldo.p_CuentaInversion.add(cuentaInversion);
				entradaSaldoServicio.logica(con, entradaSaldo);
			}
			
			//Notificar
			if(out.P_OUT_ERRORESCONTADOR == 0){
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("IdEmpresa", entrada.p_IdEmpresa);
				params.put("IdUsuario", entrada.p_IdUsuario);
				List<InfoExtraNotificacion> listaInfoExtra = sql.query(con, SQL_INFO_EXTRA_NOTIFICACION, params, InfoExtraNotificacion.class);
				
				AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
				notifIn.P_IDEMPRESA = entrada.p_IdEmpresa;
				notifIn.P_IDUSUARIO = entrada.p_IdUsuario;
				notifIn.P_IDMODULO = entrada.p_IdModulo;
				notifIn.P_EVENTOS = new ArrayList<>();
				notifIn.P_VALORES = new ArrayList<>();
				notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
				notifIn.P_IDDIRECCIONES = new ArrayList<>();
					
				logger.debug("INICIO");

				String logoCorredorRutaPublica = "";
				ConsultarImagen consultarImagen = new ConsultarImagen();
				ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
				consultarImagenIn.p_IdEmpresa = entrada.p_IdEmpresa;
				consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
				ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(con, consultarImagenIn);
				if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
					logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
				}


				for(CargoAbonoACertificar cargoAbono: listaCargoAbono){
					if(cargoAbono.IdDireccionElectronicaPrincipal != null) {
						Timestamp vFechaHoraSistema = Fecha.toTimeStamp(fechaHoraSistema);

						// Evento de notificacion
						AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
						evt.CodigoTipoEvento = "MOVCAJAS";
						evt.IdSecuencia = cargoAbono.IdCargoAbonoMovimiento;
						evt.FechaCreacionSistema =  Fecha.toStringDate(vFechaHoraSistema);            
						evt.IdPersonaRemitente = listaInfoExtra.get(0).IdPersona;
						evt.IdPersonaDestinatario = cargoAbono.IdPersona;            
						evt.IdPortafolio = cargoAbono.IdPortafolio;
						notifIn.P_EVENTOS.add(evt);
		            
						AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
						ndir.IdSecuencia = cargoAbono.IdCargoAbonoMovimiento;
						ndir.TipoDestinatario = "TO";
						ndir.IdDireccion = Long.valueOf(cargoAbono.IdDireccionElectronicaPrincipal);
						notifIn.P_IDDIRECCIONES.add(ndir);
						
						String fechaIngreso = Fecha.toFormat(cargoAbono.FechaEfectivaMovimiento,"dd-MM-YYYY");
						
						// Valores generales
						notifIn.addValor(cargoAbono.IdCargoAbonoMovimiento, "CLIENTE", cargoAbono.NombreCompleto)
		            	   .addValor(cargoAbono.IdCargoAbonoMovimiento, "TIPODOCUMENTOIDENTIDAD", cargoAbono.TipoDocumento)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "CORREO", cargoAbono.Correo)  
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "DOCUMENTOIDENTIDAD", cargoAbono.DocumentoIdentidad)  
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "IDTIPOCLIENTE", cargoAbono.IdTipoCliente) 
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "TELEFONO", cargoAbono.TelCliente)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "CORREDORA", listaInfoExtra.get(0).RazonCorredora)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "DIRECCIONCORREDORA", listaInfoExtra.get(0).DireccionCorredora)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "RUTCORREDORA", listaInfoExtra.get(0).RutCorredora)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "ATENCIONCLIENTE", listaInfoExtra.get(0).TelefonoCorredora)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "MONTO", MonedaFormato.formatear(con, cargoAbono.Monto , MonedaFormato.MONTO, cargoAbono.IdMoneda).valorConFormato)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "OPERACION", cargoAbono.NombreOperacion)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "MONEDA", cargoAbono.IdMoneda)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "FECHA", cargoAbono.FechaSistema+"")
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "CONCEPTO", cargoAbono.NombreConcepto)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "IDINTERNO", cargoAbono.IdCargoAbonoMovimiento+"")
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "FECHAINGRESO", fechaIngreso)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "PORTAFOLIO", cargoAbono.CodigoPortafolio)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "AGENTE",cargoAbono.Agente)
		                   .addValor(cargoAbono.IdCargoAbonoMovimiento, "LOGO_CORREDOR", logoCorredorRutaPublica);
					}
				}
				logger.debug("FIN");
				
				if (notifIn.P_EVENTOS.size() > 0) {
					AgregarEvento notif = new AgregarEvento();
					AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
					for(ObjIDEVENTO salida : notifOut.P_OUT_IDEVENTO){
						if(salida.IdEvento == null){
							out.P_OUT_ADVERTENCIASCONTADOR++;
				            out.P_OUT_ADVERTENCIASDETALLE.add(new Detalle(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"Advertencia","No se envio la notificacion"));
						}
					}
				}
			}
			
			logger.info("<<FIN::CertificarCargoAbono>>");
			return out;
		}
		catch (Exception e){
			logger.error("ERROR",e);
			throw e;
			
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CertificarCargoAbonoEntrada in = gson.fromJson(jin.toString(), CertificarCargoAbonoEntrada.class);
		CertificarCargoAbonoSalida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master2", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9568,\r\n"
					+ "    \"p_IdModulo\": \"CTA\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_FechaMovimiento\": \"2026-07-09\",\r\n"
					+ "    \"p_IdPais\": 44,\r\n"
					+ "    \"p_CargosAbonosACertificar\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdCargoAbonoMovimiento\": 1948494\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2018-10-19\",\"p_IdPais\":44,\"p_CargosAbonosACertificar\":[{\"IdCargoAbonoMovimiento\":1437514 }]}";
			CertificarCargoAbonoEntrada Objin = gson.fromJson(json, CertificarCargoAbonoEntrada.class);
			CertificarCargoAbono demo = new CertificarCargoAbono();				
			try {
				CertificarCargoAbonoSalida out = demo.logica(con, Objin);
				System.out.println(out);
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			//con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}