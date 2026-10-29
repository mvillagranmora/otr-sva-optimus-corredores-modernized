package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida.ObjResultado;
import cl.bcs.custodia.pojo.Cartera;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class FusionCarterasClientes implements JavaCall{

	static final Logger logger = LogManager.getLogger(FusionCarterasClientes.class);
	private final String SQL_SelectCarterasFusion 	  = loadResourceToString("cl/bcs/custodia/sql/FusionCarterasClientes_SelectCarterasFusion.sql");
	private final String SQL_ConsultaMesas 			  = loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_ConsultaMesas.sql");
	private final String SQL_ConsultaMesasRestriccion = loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_ConsultaMesasRestriccion.sql");
	private final String SQL_ConsultaPortafolio       = loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_ConsultaPortafolio.sql");
	private final String SQL_ConsultarModulos         = loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_ConsultarModulos.sql");
	private final String SQL_AddFusionAux     		  = loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_AddFusionAux.sql" );
	private final String SQL_AddHistorico     	      = loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_AddHistoricoConAjustes.sql" );
	// Control de ejecución EN BASE DE DATOS, por cliente, a través de la tabla ControlFusionCartera.
	private final String SQL_ConsultarUltimoControlFusion 		= loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_ConsultarUltimoControl.sql");
	private final String SQL_ConsultarSiguienteIdControlFusion  = loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_ConsultarSiguienteIdControl.sql");
	private final String SQL_InsertControlFusion 				= loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_InsertControlFusion.sql");
	private final String SQL_UpdateControlFusion 				= loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_UpdateControlFusion.sql");

	public static class ControlFusionUltimo {
		public Long IdControlFusion;
		public String IdEstado;
		public Long p_IdPortafolio;
	}
	public static class ControlFusionSiguienteId {
		public Long IdControlFusion;
	}

	public static class FusionCarterasEntrada {
		public Long p_IdEmpresa ;
		public Long p_IdUsuario ;
		public String p_IdModulo  ; 
		public Long p_IdPais;
		public Long   p_IdCliente;
		public Long p_IdPortafolio;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public String p_Nemotecnico;
		public String p_simularOperacion = "N";
		
		public List<AjusteManualFusion> p_AjustesManuales;
	
		public String toString() {
			return Utils.printJson(this, false);
		};

	};
	public static class FusionCarterasSalida{
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		public List<RegistrosFusionPreview> P_OUT_RESULTADO = new ArrayList<>();
		public List<RegistrosDetalleFusionPreview> P_OUT_DETALLE   = new ArrayList<>();
	}

	public static class RegistrosFusionPreview {
	   public Long        IdGeneracionMovimiento;   // clave para enlazar con el detalle (== SecuenciaFusionTemp del front)
	   public Long        IdPortafolio;
	   public String      IdClasificacionTipoInstrumento;
	   public String       Nemotecnico;
	   public String        NemoSVS;
	   public String       IdEmisor;
	   public Integer      CarterasFusionadas;
	   public BigDecimal   INGCantidad;
	   public BigDecimal   MontoCompraMonedaInstrumento;
	   public BigDecimal   ValorCalculadoFus;   // solo disponible si se alcanzó a valorizar
	   public Boolean       ValorizacionExitosa = false; 
	   public BigDecimal 	Precio;
	   public BigDecimal 	Tasa; // null para RV (o no poblar la columna)
	   public Boolean       AjustadoManualmente = false;
	}

	public static class RegistrosDetalleFusionPreview {
	   public Long        IdGeneracionMovimiento; 
	   public Long        FolioCartera;
	   public BigDecimal  Cantidad;
	   public BigDecimal  ValorRescate;
	   public String      FechaCompra;
	   public String      FechaLiquidacion;
	   public BigDecimal  MontoCompra;
	   public BigDecimal  TasaCompra;
	}
	public static class AjusteManualFusion {
	    public List<Long> Folios;   
	    public String TipoAjuste;   // "PRECIO" | "TASA" (RV solo admite "PRECIO")
	    public BigDecimal Valor;
	}

	public static class ObjMesas
	{

		public long IdMesa; 
		public long IdCliente;
		public long IdPortafolio;
		public String IdEstadoMesa;
	};
	public static class Modulo{
		public String CodigoModulo;
		public String IdEstado ;
	}
	public static class ConsultaPortafolioCliente
	{
		public long IdPortafolio;
		public long IdCliente;
	};
	public static class IngresoHISFusion{
		Date FFusion;
		Long IdModulo;
		Long IdE;
		Long Usuario;
		Long FOld;
		Long FolioNew;
	}
	public static class AddHISFusion{
		public String FechaFusion;
		public BigDecimal CantidadFusion;
		public BigDecimal TasaFusion;
		public BigDecimal ValorRescateFusion;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal MontoFusion;
		public BigDecimal MontoValorizado;
		public Long IdCarteraResultante;
		public String IdModulo;
		public String IdUsuario;
		public String IdEmpresa;
		public String AjustadoManualmente; // "S"/"N"
		public String TipoAjusteManual;    // "PRECIO"/"TASA"/null

		public String toString() {
			return Utils.printJson(this, false);
		};

	}; 
	public static class AddFusionAux{
		public Long IdCartera;
		public Long FolioCustodia;
		public BigDecimal Cantidad;
		public BigDecimal Tasa;
		public BigDecimal ValorRescate;
		public BigDecimal CorteMinimo;
		public String EsFungible;
		public String toString() {
			return Utils.printJson(this, false);
		};

	};
	private class Valorizaciones{
		public Integer PosValorizacion;
		public Long IdGeneracionMovimiento;
		public Boolean ValorizacionOK = false;
		public BigDecimal MontoSuma;
		public BigDecimal Tasa;
		public List<Long> Folios = new ArrayList<Long>();
		public Boolean AjustadoManualmente = false;
		public String TipoAjusteManual; // "PRECIO" | "TASA" | null
	}
	
	private class LogAjusteManual {				
				public String Nemotecnico;
				public String TipoAjuste;      // "PRECIO" | "TASA"
				public BigDecimal ValorOriginal;  // Precio o Tasa (segun TipoAjuste) ANTES del ajuste
				public BigDecimal ValorNuevo;     // Precio o Tasa (segun TipoAjuste) DESPUES del ajuste
				public BigDecimal MontoOriginal;
				public BigDecimal MontoNuevo;
				public Boolean ValorizacionResultanteOK; // si tras el ajuste el grupo quedo valorizable o no
	}
	
	static Boolean Debug = true;

	Boolean Flag = false;
	Long idProgramaControlFusion = null; 
	public List<List<Long>> ListaPortafoliosFusion = new ArrayList<>(); 
	String fechaSistema = ""; 
	FusionCarterasSalida out = new FusionCarterasSalida(); 

	List <ObjInstrumento> listasalida = new ArrayList<ObjInstrumento> () ; 
	List <List<ObjInstrumento>> CarterasRetiro = new ArrayList<> () ; 
	List <ObjInstrumento> CarterasIngreso = new ArrayList<ObjInstrumento> () ; 


	List<Valorizaciones> ValorizacionesFinal = new ArrayList<Valorizaciones>() ;
	Map<Long, LogAjusteManual> ajustesRealizadosLog = new HashMap<Long, LogAjusteManual>();
	Map<Long, Long> FolioResultantePorGrupo = new HashMap<Long, Long>();

	public FusionCarterasSalida logica(Connection con ,FusionCarterasEntrada entrada ) throws Exception { 
		logger.trace("INICIO::FusionCarterasClientes.logica");
		long inicio = System.currentTimeMillis();
		logger.info("Inicio Procedimiento de Fusion de carteras. Entrada: " + entrada.toString()) ; 

		out.P_OUT_ERRORESCONTADOR = 0L;
		ajustesRealizadosLog.clear(); // Map<Long, LogAjusteManual>, ver mas abajo
		FolioResultantePorGrupo.clear();
		SQL sql = new SQL(); 

		try {
			List <Long> IdPortafolioMesasFusionables = ValidacionesEejcucion(sql, con, entrada);
			logger.info("Portafolios fusionables encontrados para IdCliente=" + entrada.p_IdCliente + ": " + IdPortafolioMesasFusionables.size());
			List <List<Cartera>> ListaListasCarteras = new ArrayList<>();
			List <Cartera> TablaCartera; 
			HashMap <String,Object> paramsIN = new HashMap<>(7); 		

			if (out.P_OUT_ERRORESCONTADOR == 0L) {
				String mercado = entrada.p_IdClasificacionTipoInstrumento;
				logger.info("Mercado seleccionado para la fusion: " + mercado);
				paramsIN.put("Portafolio", IdPortafolioMesasFusionables );				
				paramsIN.put("FechaSistema",fechaSistema);
				paramsIN.put("Nemotecnico", (entrada.p_Nemotecnico != null && !entrada.p_Nemotecnico.trim().isEmpty())
				        ? entrada.p_Nemotecnico.trim()
				        : null);
				String msg;
								
				try 
				{			
					if ("RF".equalsIgnoreCase(mercado)) {
						paramsIN.put("Mercado", "RF");
						paramsIN.put("Orden", "\"Nemotecnico\", \"IdTipoReajuste\" ,\"IdEmisor\", \"FechaVencimientoInstrumento\", \"IdBoveda\" ,\"TasaCompra\" ");
						TablaCartera = sql.query(con, SQL_SelectCarterasFusion, paramsIN, Cartera.class);
						logger.info("Carteras candidatas RF encontradas: " + TablaCartera.size());
						if (TablaCartera.size() > 1) {
					        ListaListasCarteras.addAll(ListadoRF(TablaCartera));
					        logger.info("Grupos de fusion RF armados: " + ListaListasCarteras.size());
					    } else {
					    	logger.info("Sin carteras candidatas para fusión RF");
					    	String codigoMensaje = "UI_SIN_CARTERAS_FUSION_RF";
					    	String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
					    	out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_INFORMACION,codigoMensaje,mensaje));
					    }						
					}else if ("IF".equalsIgnoreCase(mercado)) {

					    paramsIN.put("Mercado", "IF");
						paramsIN.put("Orden","\"NemoSVS\", \"IdTipoReajuste\" ,\"IdEmisor\", \"FechaVencimientoInstrumento\" ,\"EsFungible\" ,\"ValorRescate\" ,\"IdBoveda\" , \"TasaCompra\" ");
					    TablaCartera = sql.query(con, SQL_SelectCarterasFusion, paramsIN, Cartera.class);
					    logger.info("Carteras candidatas IF encontradas: " + TablaCartera.size());

					    if (TablaCartera.size() > 1) {
					        ListaListasCarteras.addAll(ListadoIF(TablaCartera));
					        logger.info("Grupos de fusion IF armados: " + ListaListasCarteras.size());
					    } else {
					        logger.info("Sin carteras candidatas para fusión IF");
					        String codigoMensaje = "UI_SIN_CARTERAS_FUSION_IF";
					        String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
					        out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_INFORMACION,codigoMensaje,mensaje));
					    }

					}else if ("RV".equalsIgnoreCase(mercado)) {

					    paramsIN.put("Mercado", "RV");
					    paramsIN.put("Orden","\"FechaOperacion\",\"IdCartera\", \"IdPortafolio\", \"Nemotecnico\", \"IdCondicionLiquidacion\"");
					    TablaCartera = sql.query(con, SQL_SelectCarterasFusion, paramsIN, Cartera.class);
					    logger.info("Carteras candidatas RV encontradas: " + TablaCartera.size());

					    if (TablaCartera.size() > 1) {
					        ListaListasCarteras.addAll(ListadoRV(TablaCartera));
					        logger.info("Grupos de fusion RV armados: " + ListaListasCarteras.size());
					    } else {
					    	logger.info("Sin carteras candidatas para fusión RV");
					        String codigoMensaje = "UI_SIN_CARTERAS_FUSION_RV";
					        String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
					        out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_INFORMACION,codigoMensaje,mensaje));					       
					    }

					}

					if ( ListaListasCarteras.size() > 0) {
						// Construccion del instrumento fusionado es COMUN a simulacion y ejecucion real.
						// Asi el preview muestra exactamente el mismo monto/precio que resultaria de fusionar de verdad.
						logger.info("Construyendo instrumentos fusionados para " + ListaListasCarteras.size() + " grupo(s) candidato(s)");
						ConstruirCarterasIngreso(ListaListasCarteras, entrada);

						// RV su "valorizacion" corresponde al precio promedio ponderado de las carteras fusionadas (Monto/Cantidad).
						// RF e IF continuan valorizando contra el valorizador de instrumentos, como hasta ahora.
						if ("RV".equalsIgnoreCase(mercado)) {
							ValorizarPromedioPonderadoRV(con, entrada, ListaListasCarteras);
						} else {
							ValorizacionInstrumentos(con, entrada);
						}
						logger.info("Grupos que lograron valorizarse: " + CarterasIngreso.size() + " de " + ListaListasCarteras.size());
						
						AplicarAjustesManuales(con, entrada);

						if ("S".equalsIgnoreCase(entrada.p_simularOperacion)) {
							// MODO SIMULACIÓN: arma preview con lo ya valorizado, sin tocar retiro/ingreso/histórico reales
							logger.info("Modo SIMULACION: construyendo previsualizacion sin persistir cambios");
							ConstruirPreview(ListaListasCarteras, con, entrada);
						} else {
							logger.info("Modo REAL: ejecutando ingreso/retiro de titulos");
							EjecutarIngresoRetiro(ListaListasCarteras, con, entrada);	
						}
					}else {										
						logger.info("Sin fusiones para el mercado seleccionado");
				        String codigoMensaje = "UI_SIN_CARTERAS_FUSION_MERCADO";
				        String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
				        out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_INFORMACION,codigoMensaje,mensaje));
					}

					if (out.P_OUT_ERRORESCONTADOR == 0L) {														
						logger.info("Fusion Carteras Clientes finalizada sin errores. IdCliente=" + entrada.p_IdCliente + ", IdPortafolio=" + entrada.p_IdPortafolio);
						AgregarLog agregarLogFusionCarterasClientes = new AgregarLog();
						InAgregarLog entradaLog = new InAgregarLog();
						InAgregarLog.Log headLog = new InAgregarLog.Log();

					    headLog.NombreProceso = "Fusion Carteras Clientes";
					    headLog.IdCabeceraLog = idProgramaControlFusion != null ? idProgramaControlFusion : 0L;
					    headLog.IdTipoLog = "INGRESO";
					    headLog.IdModulo = entrada.p_IdModulo;
					    headLog.IdUsuario = entrada.p_IdUsuario;
					    headLog.IdEmpresa = entrada.p_IdEmpresa;
					    headLog.FechaSistemaLog = fechaSistema;
					    headLog.IdUsuarioAtendido = entrada.p_IdUsuario;
					    headLog.IdCliente = entrada.p_IdCliente;
					    headLog.IdPortafolio = entrada.p_IdPortafolio != null ? entrada.p_IdPortafolio : 0L;
					    headLog.Nemo = entrada.p_Nemotecnico != null ? entrada.p_Nemotecnico : "";
					    headLog.Folio = 0L;
					    entradaLog.p_Log.add(headLog);
					    

					    // Un detalle de log por CADA grupo efectivamente fusionado. Solo se registra en ejecucion REAL, no en cada
					    // re-simulacion del preview (ver nota mas abajo sobre esta decision).
					    if (!"S".equalsIgnoreCase(entrada.p_simularOperacion)) {
					        Long idCabecera = headLog.IdCabeceraLog;
					        for (ObjInstrumento grupo : CarterasIngreso) {					            
					            Long folioResultante = FolioResultantePorGrupo.get(grupo.IdGenerarMovimientoCustodia);
					            String subRegistro = folioResultante != null ? "IdCartera " + folioResultante : "Grupo Fusion: " + grupo.IdGenerarMovimientoCustodia;
					            LogAjusteManual ajuste = ajustesRealizadosLog.get(grupo.IdGenerarMovimientoCustodia);	
					            boolean esRV = "RV".equalsIgnoreCase(grupo.IdClasificacionTipoInstrumento);					            					           

					            if (ajuste != null) {
					                // Grupo CON ajuste manual: se deja constancia del campo ajustado (Precio o Tasa)
					                // con su valor original y el nuevo, y ademas el impacto en el Monto resultante.
					            	entradaLog.agregarDetalleLogBasico(idCabecera, idProgramaControlFusion,
					            			"Nemotecnico",
					            			grupo.Nemotecnico,
					            			grupo.Nemotecnico,
					            			subRegistro);
					            	
					                String campoAjuste = "TASA".equals(ajuste.TipoAjuste) ? "Tasa" : "Precio";
					                entradaLog.agregarDetalleLogBasico(idCabecera, idProgramaControlFusion,
					                        campoAjuste,
					                        String.valueOf(ajuste.ValorOriginal),
					                        String.valueOf(ajuste.ValorNuevo),
					                        subRegistro);					                
					                entradaLog.agregarDetalleLogBasico(idCabecera, idProgramaControlFusion,
					                        "Monto",
					                        MonedaFormato.formatear(con, ajuste.MontoOriginal, MonedaFormato.MONTO, grupo.IdMonedaInstrumento).valorConFormato
					                                + " " + grupo.IdMonedaInstrumento,
					                        MonedaFormato.formatear(con, ajuste.MontoNuevo, MonedaFormato.MONTO, grupo.IdMonedaInstrumento).valorConFormato
					                                + " " + grupo.IdMonedaInstrumento,
					                        subRegistro);
					            } else {
					                // Grupo SIN ajuste manual: se deja constancia de la valorizacion estandar, con
					                // Valor Antiguo = Valor Nuevo (no hubo cambio), para mantener el mismo formato de
					                // campos que un grupo ajustado y permitir comparar ambos casos en el mismo reporte.
					            	entradaLog.agregarDetalleLogBasico(idCabecera, idProgramaControlFusion,
					            			"Nemotecnico",
					            			grupo.Nemotecnico,
					            			grupo.Nemotecnico,
					            			subRegistro);
					            	
					                entradaLog.agregarDetalleLogBasico(idCabecera, idProgramaControlFusion,
					                        "Precio",
					                        String.valueOf(grupo.Precio),
					                        String.valueOf(grupo.Precio),
					                        subRegistro);
					                if (!esRV) {
					                    // RV no valoriza por tasa; se omite esta fila para ese mercado.
					                    entradaLog.agregarDetalleLogBasico(idCabecera, idProgramaControlFusion,
					                            "Tasa",
					                            String.valueOf(grupo.Tasa),
					                            String.valueOf(grupo.Tasa),
					                            subRegistro);
					                }					                
					                entradaLog.agregarDetalleLogBasico(idCabecera, idProgramaControlFusion,
					                        "Monto",
					                        MonedaFormato.formatear(con, grupo.Monto, MonedaFormato.MONTO, grupo.IdMonedaInstrumento).valorConFormato
					                                + " " + grupo.IdMonedaInstrumento,
					                        MonedaFormato.formatear(con, grupo.Monto, MonedaFormato.MONTO, grupo.IdMonedaInstrumento).valorConFormato
					                                + " " + grupo.IdMonedaInstrumento,
					                        subRegistro);
					           }
					        }
					    }					    
					    agregarLogFusionCarterasClientes.logica(con, entradaLog);						
					}else {
						con.rollback(); 
						String codigoErrorFusion = "UI_FUSION_ERRONEA";						
						msg = Diccionario.getMensaje(con,entrada.p_IdEmpresa,codigoErrorFusion).msg();
						out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoErrorFusion,msg));
						out.P_OUT_ERRORESCONTADOR++;
						logger.info(msg); 
						logger.info(out);												
					} 

				}
				catch (Exception e) { 
					logger.error("Excepcion no controlada durante la ejecucion de la fusion de carteras (IdCliente=" + entrada.p_IdCliente + ", IdPortafolio=" + entrada.p_IdPortafolio + ")", e);
					con.rollback(); 
					String codigoErrorFusion = "UI_FUSION_ERRONEA";
					msg = Diccionario.getMensaje(con, entrada.p_IdEmpresa,codigoErrorFusion).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoErrorFusion,msg));
					out.P_OUT_ERRORESCONTADOR++;
					logger.info(msg); 
					logger.info(out);					
				}
			}

			if (Debug) { 
				logger.debug(out.P_OUT_MENSAJES); 
				}
			long fin = System.currentTimeMillis();
			logger.trace("FIN::FusionCarterasClientes.logica", fin-inicio);
			return out;

		} finally {
			if (Flag && idProgramaControlFusion != null) {
				try {
					String estadoFinal;
					String resultadoFinal;
					if (out.P_OUT_ERRORESCONTADOR > 0L) {
						estadoFinal = "ERR";
						resultadoFinal = "Programa no ejecutado correctamente.";
					} else if (CarterasIngreso.isEmpty()) {
						estadoFinal = "TER";
						resultadoFinal = "Programa no ejecutado. Sin Carteras para fusionar.";
					} else {
						estadoFinal = "TER";
						resultadoFinal = "Programa Finalizado Correctamente.";
					}
					logger.info("Cerrando registro de control IdControlFusion=" + idProgramaControlFusion + " con IdEstado=" + estadoFinal);
					HashMap<String,Object> paramsUpdateControl = new HashMap<>();
					paramsUpdateControl.put("IdControlFusion", idProgramaControlFusion);
					paramsUpdateControl.put("IdEstado", estadoFinal);
					paramsUpdateControl.put("Resultado", resultadoFinal);
					sql.call(con, SQL_UpdateControlFusion, paramsUpdateControl, new HashMap<>());
					con.commit();
				} catch (Exception e) {					
					logger.error("No se pudo cerrar el registro de control de fusion IdControlFusion=" + idProgramaControlFusion, e);
				}
			}
		}

	}

	private List<Long> ValidacionesEejcucion(SQL sql,Connection con, FusionCarterasEntrada entrada) throws Exception { 
		//Validaciones necesarias para la ejecucion del proceso de fusion 
		// Modulo de facturacion cerrado, Modulo de custodia abierto (requerimiento del ingresoretirotitulos, y mesas que que aceptan fusion cerradas)
		logger.trace("INICIO::FusionCarterasClientes.ValidacionesEejcucion");
		long inicio = System.currentTimeMillis();
		List<ObjMesas> Mesas = new ArrayList<ObjMesas>();
		List<Modulo> Modulos = new ArrayList<Modulo>(); 
		List<ConsultaPortafolioCliente> PortafoliosFusion = new ArrayList<ConsultaPortafolioCliente>(); 
		List<Long> IdPortafolioMesasFusionables = new ArrayList<Long>();
		HashMap<String,Object> paramsRestriccion = new HashMap<>();
		paramsRestriccion.put("IdCliente", entrada.p_IdCliente);
		paramsRestriccion.put("IdPortafolio", entrada.p_IdPortafolio);
		
		try {
			Mesas = sql.query(con, SQL_ConsultaMesas, new HashMap<>(), ObjMesas.class);
			Modulos = sql.query(con, SQL_ConsultarModulos, new HashMap<>(), Modulo.class );
			logger.info("Mesas consultadas: " + Mesas.size() + " | Modulos consultados: " + Modulos.size());
			
			List<ConsultaPortafolioCliente> restriccion = sql.query(con,SQL_ConsultaMesasRestriccion,paramsRestriccion,ConsultaPortafolioCliente.class);			
			if (!restriccion.isEmpty()) {
				logger.info("Portafolio " + entrada.p_IdPortafolio + " corresponde a Cuenta Propia; se bloquea la fusion");		
			    String codigoMensaje = "UI_PORTAFOLIO_CUENTA_PROPIA";
			    String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
			    out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
			    out.P_OUT_ERRORESCONTADOR++;
			}

			if (entrada.p_IdCliente == null) {
				logger.info("No se indico IdCliente en la entrada");			
				String codigoMensaje = "UI_CLIENTE_REQUERIDO";
			    String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
			    out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
			    out.P_OUT_ERRORESCONTADOR++;
			}
			HashMap<String, Object> paramsPortafolio = new HashMap<>();
			paramsPortafolio.put("IdPortafolio", entrada.p_IdPortafolio);
			paramsPortafolio.put("IdClasificacionTipoInstrumento",entrada.p_IdClasificacionTipoInstrumento);
			paramsPortafolio.put("IdTipoInstrumento", entrada.p_IdTipoInstrumento == null || "0".equals(entrada.p_IdTipoInstrumento)
			        ? null
			        : entrada.p_IdTipoInstrumento);
			paramsPortafolio.put("Nemotecnico", (entrada.p_Nemotecnico != null && !entrada.p_Nemotecnico.trim().isEmpty())
			        ? entrada.p_Nemotecnico.trim()
			        : null);
		
			PortafoliosFusion = sql.query(con,SQL_ConsultaPortafolio,paramsPortafolio,ConsultaPortafolioCliente.class);

			if (entrada.p_IdCliente != null && !PortafoliosFusion.isEmpty()) {
				for (ConsultaPortafolioCliente portafolio : PortafoliosFusion) {
					if (portafolio.IdCliente != entrada.p_IdCliente) {
						String codigo = "UI_FUSION_PORTAFOLIO_INVALIDO";
						logger.info("Portafolio " + entrada.p_IdPortafolio + " no pertenece al cliente indicado (" + entrada.p_IdCliente + ")");
						String msg = Diccionario.getMensaje(con,entrada.p_IdEmpresa,codigo).add("IdPortafolio", entrada.p_IdPortafolio).msg();
						out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigo,msg));
						out.P_OUT_ERRORESCONTADOR++;				
						break;
					}
				}
			}

			fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa , "PLATAFORMA", "PLA.FechaSistema");

			boolean esSimulacion = "S".equalsIgnoreCase(entrada.p_simularOperacion);
			logger.info("Ejecucion en modo " + (esSimulacion ? "SIMULACION" : "REAL"));
			
			for (Modulo M : Modulos )
			{	
				if ("ABT".equalsIgnoreCase(M.IdEstado) && "FAC".equalsIgnoreCase(M.CodigoModulo)) {
					logger.info("Modulo de facturacion abierto; se bloquea la fusion");
					
					String codigoMensaje = "UI_MODULO_FACTURACION_ABIERTO";
			        String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
			        out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
			        out.P_OUT_ERRORESCONTADOR++;
				}
				if ("CER".equalsIgnoreCase(M.IdEstado) && "CUS".equalsIgnoreCase(M.CodigoModulo)) {
					logger.info("Modulo de custodia cerrado; se bloquea la fusion");					
					
					String codigoMensaje = "UI_MODULO_CUSTODIA_CERRADO";
			        String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
			        out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
			        out.P_OUT_ERRORESCONTADOR++;
				} 
			} 
			for ( ObjMesas Mesa :Mesas ) { 
				if (Mesa.IdEstadoMesa.equals("A")) { // Valida que las mesas RF que aplica fusion se encuentren cerradas
					logger.info("Mesa " + Mesa.IdMesa + " se encuentra abierta; se bloquea la fusion");
					
					String codigoMensaje = "MESA_ABIERTA_CERRAR_PARA_FUSIONAR";
					String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).add("IdMesa", Mesa.IdMesa).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
					out.P_OUT_ERRORESCONTADOR++;
				} 
			}

			if (out.P_OUT_ERRORESCONTADOR == 0L && entrada.p_IdCliente != null) {
				HashMap<String,Object> paramsUltimoControl = new HashMap<>();
				paramsUltimoControl.put("IdCliente", entrada.p_IdCliente);
				paramsUltimoControl.put("IdPortafolio", entrada.p_IdPortafolio);
				paramsUltimoControl.put("FechaOperacion", fechaSistema);
				List<ControlFusionUltimo> ultimoControl = sql.query(con, SQL_ConsultarUltimoControlFusion, paramsUltimoControl, ControlFusionUltimo.class);

				boolean hayEjecucionPendiente = !ultimoControl.isEmpty();

				if (hayEjecucionPendiente) {			
					logger.info("Existe una fusion PENDIENTE para IdCliente=" + entrada.p_IdCliente + " (Programa " + ultimoControl.get(0).IdControlFusion + "); se bloquea esta ejecucion");
					String codigo = "UI_FUSION_EJECUCION_PENDIENTE";
					String msg = Diccionario.getMensaje(con,entrada.p_IdEmpresa,codigo).add("IdControlFusion",ultimoControl.get(0).IdControlFusion).msg();
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigo,msg));				
				} else if (!esSimulacion) {
					// inserta el registro en PEN y lo deja comprometido (commit)				
					Long siguienteId = sql.query(con, SQL_ConsultarSiguienteIdControlFusion, new HashMap<>(), ControlFusionSiguienteId.class).get(0).IdControlFusion;

					HashMap<String,Object> paramsInsertControl = new HashMap<>();
					paramsInsertControl.put("IdControlFusion", siguienteId);
					paramsInsertControl.put("IdUsuario", entrada.p_IdUsuario);
					paramsInsertControl.put("IdCliente", entrada.p_IdCliente);
					paramsInsertControl.put("IdPortafolio", entrada.p_IdPortafolio);
					paramsInsertControl.put("IdEstado", "PEN");
					paramsInsertControl.put("Resultado", "Programa Encolado");
					sql.call(con, SQL_InsertControlFusion, paramsInsertControl, new HashMap<>());
					con.commit();

					idProgramaControlFusion = siguienteId;
					Flag = true; // esta instancia tomo el registro de control; es responsable de cerrarlo al finalizar (ver finally en logica())
					logger.info("Registro de control creado: IdControlFusion=" + siguienteId + " en estado PEN");
				}
			}
			
			for (ConsultaPortafolioCliente portafolio : PortafoliosFusion) {
				if (!IdPortafolioMesasFusionables.contains(portafolio.IdPortafolio)) {
					IdPortafolioMesasFusionables.add(portafolio.IdPortafolio);
				}
			}
		} catch (Exception e) {
			logger.error("Excepcion no controlada durante ValidacionesEejcucion (IdCliente=" + entrada.p_IdCliente + ", IdPortafolio=" + entrada.p_IdPortafolio + ")", e);
			String codigoMensaje = "UI_FUSION_ERRONEA";
		    String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensaje).msg();
		    out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
		    out.P_OUT_ERRORESCONTADOR++;
		    
		} 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.ValidacionesEejcucion", fin-inicio);
		return IdPortafolioMesasFusionables;
	}

	private List<List<Cartera>> ListadoRF(List<Cartera> TablaCartera ) { 
		logger.trace("INICIO::FusionCarterasClientes.ListadoRF");
		long inicio = System.currentTimeMillis();

		List<Cartera> NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
		List<List<Cartera>> ListaListaCarterasFusion = new ArrayList<>(); 
		int NCartIN = TablaCartera.size();
		List<Long> IdFolio = new ArrayList<>();
		Cartera PrimerItem = TablaCartera.remove(0);


		while (TablaCartera.size() > 0) {
			for (int i = 0 ; i<TablaCartera.size() ;i++) { 
				Cartera candidato = TablaCartera.get(i);
				if (PrimerItem.IdPortafolio.equals(candidato.IdPortafolio) && PrimerItem.IdClasificacionTipoInstrumento.equals(candidato.IdClasificacionTipoInstrumento) &&
					PrimerItem.IdEmisor.equals(candidato.IdEmisor) && PrimerItem.IdMonedaInstrumento.equals(candidato.IdMonedaInstrumento) &&
					PrimerItem.IdTipoReajuste.equals(candidato.IdTipoReajuste) &&
					PrimerItem.IdBoveda.equals(candidato.IdBoveda) && PrimerItem.Nemotecnico.equals(candidato.Nemotecnico) &&
					PrimerItem.FechaVencimientoInstrumento.equals(candidato.FechaVencimientoInstrumento)) 
				{					
					NuevaListaCarteraAFusionar.add( candidato);
					IdFolio.add(candidato.FolioCartera);
					TablaCartera.remove(i);
					i--;
				}
			}
			if (NuevaListaCarteraAFusionar.size() > 0) {

					NuevaListaCarteraAFusionar.add(PrimerItem);
					IdFolio.add(PrimerItem.FolioCartera);
					ListaPortafoliosFusion.add(IdFolio);
					IdFolio = new ArrayList<>();
					ListaListaCarterasFusion.add(NuevaListaCarteraAFusionar);
					NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
			}
			if (TablaCartera.size() > 0) { PrimerItem = TablaCartera.remove(0); }
		}
		logger.info("Carteras IN RF  = "+  NCartIN  + " ;  Fusiones :" + ListaListaCarterasFusion.size() );
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.ListadoRF", fin-inicio);
		return ListaListaCarterasFusion;
	}

	private List<List<Cartera>> ListadoIF(List<Cartera> TablaCartera ) {
			logger.trace("INICIO::FusionCarterasClientes.ListadoIF");
			long inicio = System.currentTimeMillis();

			List<Cartera> NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
			List<List<Cartera>> ListaListaCarterasFusion = new ArrayList<>(); 
			int NCartIN =  TablaCartera.size() ;
			List<Long> IdPortafolio = new ArrayList<>();
			Cartera PrimerItem = TablaCartera.remove(0);

			while (TablaCartera.size() > 0) {
				for (int i = 0 ; i<TablaCartera.size() ;i++) {
					Cartera candidato = TablaCartera.get(i);
					if (PrimerItem.IdPortafolio.equals(candidato.IdPortafolio) && PrimerItem.IdClasificacionTipoInstrumento.equals(candidato.IdClasificacionTipoInstrumento) &&
						PrimerItem.IdEmisor.equals(candidato.IdEmisor) && PrimerItem.EsFungible.equals(candidato.EsFungible)&&
						PrimerItem.IdMonedaInstrumento.equals( candidato.IdMonedaInstrumento) && PrimerItem.IdTipoReajuste.equals(candidato.IdTipoReajuste) &&
						PrimerItem.IdBoveda.equals(candidato.IdBoveda) && PrimerItem.NemoSVS.equals(candidato.NemoSVS) &&
						PrimerItem.FechaVencimientoInstrumento.equals(candidato.FechaVencimientoInstrumento)  &&
						PrimerItem.CorteMinimo.equals(candidato.CorteMinimo))  	  
					{
						NuevaListaCarteraAFusionar.add(candidato);
						IdPortafolio.add(candidato.IdPortafolio);	
						TablaCartera.remove(i);

						i--;
					}

				}
				if (NuevaListaCarteraAFusionar.size() > 0) {
							NuevaListaCarteraAFusionar.add(PrimerItem);
							IdPortafolio.add(PrimerItem.IdPortafolio);
							ListaPortafoliosFusion.add(IdPortafolio);
							IdPortafolio = new ArrayList<>();
							ListaListaCarterasFusion.add(NuevaListaCarteraAFusionar);
							NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
				}
				if (TablaCartera.size() > 0) { PrimerItem = TablaCartera.remove(0); }
			}

			logger.info("Carteras IN IF  = "+  NCartIN  + " ;  Fusiones :" + ListaListaCarterasFusion.size() );
			long fin = System.currentTimeMillis();
			logger.trace("FIN::FusionCarterasClientes.ListadoIF", fin-inicio);
			return  ListaListaCarterasFusion;

	}
	/// MERCADO RV ///
	private List<List<Cartera>> ListadoRV(List<Cartera> TablaCartera ) {
		logger.trace("INICIO::FusionCarterasClientes.ListadoRV");
		long inicio = System.currentTimeMillis();

		List<Cartera> NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
		List<List<Cartera>> ListaListaCarterasFusion = new ArrayList<>(); 
		int NCartIN =  TablaCartera.size() ;
		List<Long> IdPortafolio = new ArrayList<>();
		Cartera PrimerItem = TablaCartera.remove(0);

		while (TablaCartera.size() > 0) {
			boolean primerItemEsOSA = "OSA".equalsIgnoreCase(PrimerItem.IdTipoInstrumento);

			for (int i = 0 ; i<TablaCartera.size() ;i++) {
				Cartera candidato = TablaCartera.get(i);
				boolean candidatoEsOSA = "OSA".equalsIgnoreCase(candidato.IdTipoInstrumento);

				boolean criteriosBase =
						PrimerItem.IdPortafolio.equals(candidato.IdPortafolio) &&
						PrimerItem.IdClasificacionTipoInstrumento.equals(candidato.IdClasificacionTipoInstrumento) &&
						PrimerItem.Nemotecnico.equals(candidato.Nemotecnico) &&
						PrimerItem.IdBoveda.equals(candidato.IdBoveda) &&
						primerItemEsOSA == candidatoEsOSA; // nunca mezclar OSA con no-OSA en el mismo grupo

				// OSA exige ademas que coincida la FechaLiquidacion; para el resto de RV no aplica.
				boolean criteriosFechaLiquidacionOSA =
						!primerItemEsOSA || PrimerItem.FechaLiquidacion.equals(candidato.FechaLiquidacion);

				if (criteriosBase && criteriosFechaLiquidacionOSA)
				{
						NuevaListaCarteraAFusionar.add( candidato );
						IdPortafolio.add(candidato.IdPortafolio);
						TablaCartera.remove(i);
						i--;
				}
			}
			if (NuevaListaCarteraAFusionar.size() > 0) {
						NuevaListaCarteraAFusionar.add(PrimerItem);
						IdPortafolio.add(PrimerItem.IdPortafolio);
						ListaPortafoliosFusion.add(IdPortafolio);
						IdPortafolio = new ArrayList<>();
						ListaListaCarterasFusion.add(NuevaListaCarteraAFusionar);
						NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
			}
			if (TablaCartera.size() > 0) { PrimerItem = TablaCartera.remove(0); }
		}

		logger.info("Carteras IN RV  = "+  NCartIN  + " ;  Fusiones :" + ListaListaCarterasFusion.size() );
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.ListadoRV", fin-inicio);
		return  ListaListaCarterasFusion;

	}
/**
* Arma, por cada grupo de carteras fusionables, el ObjInstrumento "fusionado" (suma de cantidades / montos / valor rescate)
* y el registro de control ValorizacionesFinal correspondiente. NO hace ningún llamado a BD ni valoriza; solo construye
* las estructuras en memoria. Es compartido tanto por el modo simulación como por la ejecución real, de modo que ambos
* flujos usen exactamente los mismos datos base antes de valorizar.
*
* Para RV el criterio difiere del de RF/IF en 3 puntos (ver ConstruirCarterasIngreso y ValorizarPromedioPonderadoRV):
*   - FechaOperacion del registro fusionado: la fecha de operacion de la cartera fusionable MAS ANTIGUA del grupo.
*   - FechaLiquidacion / IdCondicionLiquidacion: el registro fusionado liquida el mismo dia del proceso de fusion (T0/PH).
*   - Monto: para RV NO se suma aqui INGMontoCompraActualMonedaInstrumento (ese campo solo es valido para
*     RF/IF); el monto de RV se calcula en ValorizarPromedioPonderadoRV como Cantidad x PrecioCompra por cartera.
*/
	private void ConstruirCarterasIngreso(List<List<Cartera>> ListadoCarterasIN, FusionCarterasEntrada in) {
		logger.trace("INICIO::FusionCarterasClientes.ConstruirCarterasIngreso");
		long inicio = System.currentTimeMillis();

		if (Debug) { 
			for (List<Cartera> ListadoFusiondebug :ListadoCarterasIN) {
				logger.debug( "  --  " );
				for(Cartera A :ListadoFusiondebug) { logger.debug("|| Carteras Fusionables || Folio: "+ A.FolioCartera + " Tasa: " + A.TasaCompra +" Cantidad: " +  A.Cantidad);} 
			} 
		}

		Long IdGen = 1L; int IndexCartera = 0;
		for ( List<Cartera> Lista : ListadoCarterasIN )
		{
			BigDecimal Cantidad = BigDecimal.ZERO,Monto= BigDecimal.ZERO,valorRescate= BigDecimal.ZERO; 
			Valorizaciones CarteraIngresoIdGenMovCust = new Valorizaciones(); 
			Cartera CarteraFusionada = ListadoCarterasIN.get(IndexCartera).get(0); 
			ArrayList<ObjInstrumento> ListaTemporalCarterasRetiro = new ArrayList<ObjInstrumento>();
	
			boolean esRV = "RV".equalsIgnoreCase(CarteraFusionada.IdClasificacionTipoInstrumento);
			Cartera CarteraMasAntigua = CarteraFusionada;

			for (Cartera CarteraActual : Lista) {
				if (esRV) {
					Cantidad = Cantidad.add(CarteraActual.INGCantidad);
					if (CarteraActual.FechaOperacion != null && (CarteraMasAntigua.FechaOperacion == null || CarteraActual.FechaOperacion.compareTo(CarteraMasAntigua.FechaOperacion) < 0)) 
					{
						CarteraMasAntigua = CarteraActual;
					}
				} else if (CarteraActual.IdClasificacionTipoInstrumento.equals("RF")) {
					Cantidad = Cantidad.add(CarteraActual.INGCantidad);
				}else {
					if (CarteraActual.EsFungible.equals("N")) {
						Cantidad = Cantidad.add(CarteraActual.INGCantidad);
					}else {
						Cantidad = BigDecimal.valueOf(1); 
					}
				}
				 // Solo RF/IF utilizan este monto para la construcción inicial. Para RV el monto se
				 // calcula en ValorizarPromedioPonderadoRV como Cantidad x PrecioCompra por cartera.
			    if (!esRV) {
			        Monto = Monto.add(CarteraActual.INGMontoCompraActualMonedaInstrumento);
			    }				
				valorRescate = valorRescate.add(CarteraActual.INGValorRescate); 
				CarteraIngresoIdGenMovCust.Folios.add(CarteraActual.FolioCartera);
			}

			CarterasRetiro.add( ListaTemporalCarterasRetiro );
			//   Construccion de los Ingresos de titulo 
			ObjInstrumento Instrumentos = new ObjInstrumento();
			CarteraIngresoIdGenMovCust.IdGeneracionMovimiento = IdGen;
			CarteraIngresoIdGenMovCust.MontoSuma = Monto;
			ValorizacionesFinal.add(CarteraIngresoIdGenMovCust);

			Instrumentos.IdGenerarMovimientoCustodia = IdGen; //Registro secuencial unico proceso
			if (esRV) {
				// RV: FechaOperacion = fecha de la cartera fusionable mas antigua del grupo.
				Instrumentos.FechaOperacion = CarteraMasAntigua.FechaOperacion != null ? CarteraMasAntigua.FechaOperacion.toString() : fechaSistema;
				Instrumentos.FechaOperacionOriginal = CarteraMasAntigua.FechaOperacion != null ? CarteraMasAntigua.FechaOperacion.toString() : fechaSistema;
				Instrumentos.FechaVencimiento     = CarteraFusionada.FechaVencimientoInstrumento != null ? CarteraFusionada.FechaVencimientoInstrumento.toString() : null; // o fechaSistema??
				Instrumentos.FechaLiquidacion = fechaSistema;
				// RV: IdCondicionLiquidacion = T0 (PH, pagadero hoy), pues liquida el mismo dia del proceso de fusion
				Instrumentos.IdCondicionLiquidacion = "T0";
			} else {
				Instrumentos.FechaOperacion = fechaSistema ; //debe venir en formato AAAA-MM-DD
				Instrumentos.FechaOperacionOriginal = fechaSistema; //debe venir en formato AAAA-MM-DD
				Instrumentos.FechaVencimiento     = CarteraFusionada.FechaVencimientoInstrumento.toString(); //debe venir en formato AAAA-MM-DD 
				Instrumentos.FechaLiquidacion = CarteraFusionada.FechaLiquidacion.toString() ; //debe venir en formato AAAA-MM-DD
				Instrumentos.IdCondicionLiquidacion = CarteraFusionada.IdCondicionLiquidacion  ; //si no usara Condicion de Liq, enviar un valor <= 0
			}
			Instrumentos.IdConcepto = "INGFUS"; // Ingreso por fusion - INGFUS
			Instrumentos.IdBolsa = CarteraFusionada.IdBolsa  ;
			Instrumentos.IdTipoOperacion = "INGTIT" ; // INGRESO
			Instrumentos.IdTipoProducto = CarteraFusionada.IdTipoProducto ;
			Instrumentos.IdCliente = CarteraFusionada.IdCliente  ;
			Instrumentos.IdPortafolio = CarteraFusionada.IdPortafolio ;
			Instrumentos.IdClienteDestino = 0L;
			Instrumentos.IdPortafolioDestino = 0L;
			Instrumentos.IdBoveda =   CarteraFusionada.IdBoveda ;
			Instrumentos.CuentaBoveda = CarteraFusionada.CuentaBoveda  ; 
			Instrumentos.IdCuentaCustodioPortafolio   = CarteraFusionada.IdCuentaCustodioPortafolio  ; // Campo nuevo de peru 
			Instrumentos.IdBovedaRelacionada = "" ;
			Instrumentos.CuentaBovedaRelacionada = "" ;
			Instrumentos.IdCuentaCustodioPortafolioRelacionado = 0L; // Campo nuevo de peru
			Instrumentos.esTraspasoCliente = "N";
			Instrumentos.esTraspasoPortafolio = "N"; 
			Instrumentos.esTraspasoBoveda = "N"; 
			Instrumentos.DebeCertificar = "S";
			Instrumentos.EsGarantia = "N";
			Instrumentos.PlazoEnDias = CarteraFusionada.PlazoEnDiasOperacion; 
			//Instrumentos.IdCartera = 0L; // CarteraFusionada.IdCartera  ; //Cartera nueva no tiene idcartera
			Instrumentos.FolioCartera = CarteraFusionada.FolioCartera; 
			Instrumentos.FolioExternoReferencia = CarteraFusionada.FolioExternoReferencia; //comprobante de referencia
			Instrumentos.Glosa = CarteraFusionada.Glosa;
			Instrumentos.IndicadorNominal = "N"; 
			Instrumentos.IdAgente = CarteraFusionada.IdAgente;
			Instrumentos.IdSucursal = CarteraFusionada.IdSucursal; 
			Instrumentos.IdTipoCanalIngreso = CarteraFusionada.IdTipoCanalIngreso; 
			Instrumentos.IdClasificacionTipoInstrumento = CarteraFusionada.IdClasificacionTipoInstrumento;
			Instrumentos.IdTipoInstrumento = CarteraFusionada.IdTipoInstrumento;
			Instrumentos.IdInstrumento = CarteraFusionada.IdInstrumento ;
			Instrumentos.Nemotecnico = CarteraFusionada.Nemotecnico;
			Instrumentos.IdEmisor = CarteraFusionada.IdEmisor;
			Instrumentos.IdMonedaInstrumento = CarteraFusionada.IdMonedaInstrumento;
			Instrumentos.IdMonedaEmision = CarteraFusionada.IdMonedaEmision;
			Instrumentos.IdTipoReajuste = CarteraFusionada.IdTipoReajuste;
			Instrumentos.IdMonedaAdicional = CarteraFusionada.IdMonedaAdicional;
			Instrumentos.FechaEmisionInstrumento = CarteraFusionada.FechaEmisionInstrumento.toString();
			Instrumentos.FechaVencimientoInstrumento = CarteraFusionada.FechaVencimientoInstrumento.toString() ;
			Instrumentos.Fungible = CarteraFusionada.EsFungible;
			Instrumentos.NemoSVS = CarteraFusionada.NemoSVS; 
			Instrumentos.Precio = CarteraFusionada.PrecioCompra;
			Instrumentos.Cantidad = Cantidad; 
			Instrumentos.ValorRescate = valorRescate;
			Instrumentos.CorteMinimo = CarteraFusionada.CorteMinimo; 
			Instrumentos.Monto = Monto;
			Instrumentos.MontoEnMonedaInstrumento = CarteraFusionada.MontoCompraMonedaInstrumento;
			Instrumentos.Tasa = CarteraFusionada.TasaCompra;
			Instrumentos.TasaAgente = CarteraFusionada.TasaAgente;
			Instrumentos.CodigoIFRS = CarteraFusionada.CodigoIFRS;
			Instrumentos.IdRegimenFondoMutuo = CarteraFusionada.IdRegimenFondoMutuo;
			Instrumentos.Procesar = "S";//Por defecto: S=Procesar; N=No Procesar
			CarterasIngreso.add(Instrumentos);
			IdGen++;
			IndexCartera++;
		}

		logger.info("Instrumentos fusionados construidos: " + CarterasIngreso.size());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.ConstruirCarterasIngreso", fin-inicio);
	}

/**
* Ejecuta el retiro/ingreso real de títulos y el registro histórico de la fusión. Asume que
* ConstruirCarterasIngreso(...) y la valorizacion correspondiente (ValorizacionInstrumentos para RF/IF, o
* ValorizarPromedioPonderadoRV para RV) ya fueron invocadas previamente (por logica()), por lo que
* CarterasIngreso/CarterasRetiro/ValorizacionesFinal ya reflejan únicamente las fusiones que sí lograron valorizarse.
*/
	private void EjecutarIngresoRetiro(List<List<Cartera>> ListadoCarterasIN , Connection con ,FusionCarterasEntrada in) throws Exception  {
		logger.trace("INICIO::FusionCarterasClientes.EjecutarIngresoRetiro");
		long inicio = System.currentTimeMillis();

		IngresoRetiroTitulosEntrada EntradaIngresoRetiro = new IngresoRetiroTitulosEntrada ()  ;
		RetiroCustodia.RetiroCustodiaEntrada EntradaRetiroCustodia = new RetiroCustodia.RetiroCustodiaEntrada();
		IngresoRetiroTitulosJava IngresoRetiro = new IngresoRetiroTitulosJava () ;

		ObjDatosEntrada CarteraRetiro = new RetiroCustodia.ObjDatosEntrada(); 

		// Continua la secuencia de IdGeneracionMovimiento asignada por ConstruirCarterasIngreso (1..N grupos)
		Long IdGen = (long) ListadoCarterasIN.size() + 1L;

		int i = 0;
		List<ObjDatosEntrada> DatosEntrada_RetiroCustodia = new ArrayList<ObjDatosEntrada>();

		for ( List<Cartera> Lista : ListadoCarterasIN ) 
		{ 
			if (ValorizacionesFinal.get(i).ValorizacionOK) 
			{
				for (Cartera CarteraActual : Lista) 
				{
				  CarteraRetiro = new RetiroCustodia.ObjDatosEntrada(); 
				  CarteraRetiro.IdGenerarMovimientoCustodia   = IdGen ;
				  CarteraRetiro.TablaAfectada   = "CARTERA" ;
				  CarteraRetiro.IdTipoOperacion   = "RETTIT" ;
				  CarteraRetiro.IdConcepto   = "RETFUS" ;
				  CarteraRetiro.AccionOperacion   = "RETIRO" ;
				  CarteraRetiro.EsRetiroPorFusion = "S" ;
				  CarteraRetiro.IdCustodia = CarteraActual.IdCartera;
				  CarteraRetiro.FechaLiquidacion   = CarteraActual.FechaLiquidacion ;  // fechaSistema?
				  CarteraRetiro.IdCarteraOrigen = CarteraActual.IdCartera;
				  CarteraRetiro.FolioComprobanteBusqueda   = CarteraActual.FolioComprobante ;
				  CarteraRetiro.FolioCustodia   = CarteraActual.FolioCartera ;
				  CarteraRetiro.IdCliente   = CarteraActual.IdCliente ;
				  CarteraRetiro.IdPortafolio   = CarteraActual.IdPortafolio ; 
				  CarteraRetiro.FechaOperacion   = CarteraActual.FechaOperacion ;
				  CarteraRetiro.FechaComprobante   = CarteraActual.FechaOperacion ;
				  CarteraRetiro.IdCondicionLiquidacion   = CarteraActual.IdCondicionLiquidacion ; 
				  CarteraRetiro.IdTipoCanalIngreso   = CarteraActual.IdTipoCanalIngreso ;
				  CarteraRetiro.IdTipoComprobante   = CarteraActual.IdTipoComprobante ;
				  CarteraRetiro.IdTipoReajusteInstrumento   = CarteraActual.IdTipoReajuste;
				  CarteraRetiro.FolioComprobante   = CarteraActual.FolioComprobante ;
				  CarteraRetiro.IdUsuario   = CarteraActual.IdUsuario ;
				  CarteraRetiro.IdSucursal   = CarteraActual.IdSucursal ;
				  CarteraRetiro.IdAgente   = CarteraActual.IdAgente ;
				  CarteraRetiro.Glosa   = CarteraActual.Glosa ;
				  CarteraRetiro.IdInstrumento   = CarteraActual.IdInstrumento;
				  CarteraRetiro.FechaVencimientoInstrumento   = CarteraActual.FechaVencimientoInstrumento ;
				  CarteraRetiro.IdBoveda   = CarteraActual.IdBoveda ;
				  CarteraRetiro.CuentaBoveda   = CarteraActual.CuentaBoveda ;
				  CarteraRetiro.IdEmisor   = CarteraActual.IdEmisor ;
				  CarteraRetiro.EsFungible   = CarteraActual.EsFungible ;
				  CarteraRetiro.IdMonedaAdicional   = CarteraActual.IdMonedaAdicional ;
				  CarteraRetiro.IdMonedaEmision   = CarteraActual.IdMonedaEmision ; 
				  CarteraRetiro.MontoCompraMonedaInstrumento   = CarteraActual.MontoCompraMonedaInstrumento ;
				  CarteraRetiro.Cantidad   = CarteraActual.Cantidad ;
				  CarteraRetiro.CorteMinimo   = CarteraActual.CorteMinimo ;
				  CarteraRetiro.ValorRescate   = CarteraActual.ValorRescate;
				  CarteraRetiro.PrecioCompra   = CarteraActual.PrecioCompra ;
				  CarteraRetiro.TasaCompra   = CarteraActual.TasaCompra ; 
				  CarteraRetiro.IdOperacion   = CarteraActual.IdOperacion ;
				  CarteraRetiro.IdClasificacionTipoInstrumento   = CarteraActual.IdClasificacionTipoInstrumento ;
				  CarteraRetiro.NemoSVS = CarteraActual.NemoSVS;
				  CarteraRetiro.Nemotecnico = CarteraActual.Nemotecnico;
	  
				  IdGen++;
				  DatosEntrada_RetiroCustodia.add(CarteraRetiro); 
				} 
			}
			i++;
		}

		logger.info("Movimientos de retiro a procesar: " + DatosEntrada_RetiroCustodia.size());



		EntradaRetiroCustodia.p_DatosEntrada = DatosEntrada_RetiroCustodia ;
		EntradaRetiroCustodia.p_IdEmpresa = in.p_IdEmpresa;
		EntradaRetiroCustodia.p_IdUsuario = in.p_IdUsuario;
		EntradaRetiroCustodia.p_IdModulo = in.p_IdModulo ;
		EntradaRetiroCustodia.p_TicketAtencion = 1L ;
		EntradaRetiroCustodia.p_SimularOperacion = "N" ;
		EntradaRetiroCustodia.p_ValorizacionAutomatica = "N" ;

		RetiroCustodia retiroCustodia = new RetiroCustodia();
		RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, EntradaRetiroCustodia); 

		if ( !(resultRetiros.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) ) {
			logger.warn("RetiroCustodia devolvio " + resultRetiros.P_OUT_ERRORESCONTADOR + " error(es)");
			String codigoMensaje = "UI_ERROR_RETIRO_TITULOS";
		    String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje).msg();
		    out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
		    out.P_OUT_ERRORESCONTADOR++;
		} else {
			logger.info("RetiroCustodia ejecutado sin errores");
		}

		out.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);

		for (List<ObjInstrumento> ListCarterasRetiro: CarterasRetiro) {
			listasalida.addAll(ListCarterasRetiro);
		}
		listasalida.addAll(CarterasIngreso);

		EntradaIngresoRetiro.p_IdUsuario = in.p_IdUsuario; 
		EntradaIngresoRetiro.p_IdModulo = in.p_IdModulo;
		EntradaIngresoRetiro.p_simularOperacion = "N" ;
		EntradaIngresoRetiro.p_UsaReglasNegocio = "S" ;
		EntradaIngresoRetiro.P_INSTRUMENTOS = listasalida;
		EntradaIngresoRetiro.p_Certifica = "N"; 
		EntradaIngresoRetiro.p_IdEmpresa = in.p_IdEmpresa;

		IngresoRetiroTitulosSalida resultadoIngresoRetiro = IngresoRetiro.logica(con,EntradaIngresoRetiro) ;

		if (resultadoIngresoRetiro.P_OUT_ERRORESCONTADOR == 0L ) {
			logger.info("IngresoRetiroTitulos ejecutado sin errores; se registra el historico de la fusion");
			LLenarHistorico(resultadoIngresoRetiro,  ListadoCarterasIN , con, in);
			logger.debug(resultadoIngresoRetiro);
		}else{ 
			logger.error("IngresoRetiroTitulos devolvio " + resultadoIngresoRetiro.P_OUT_ERRORESCONTADOR + " error(es): " + resultadoIngresoRetiro);
			String codigoMensaje = "UI_ERROR_INGRESO_TITULOS";
		    String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje).msg();
		    out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_ERROR,codigoMensaje,mensaje));
		    out.P_OUT_ERRORESCONTADOR++;
		}
		out.P_OUT_MENSAJES.addAll(resultadoIngresoRetiro.P_OUT_MENSAJES);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.EjecutarIngresoRetiro", fin-inicio);
	}

	private ValorizarInstrumentoSalida ValorizacionInstrumentos( Connection con, FusionCarterasEntrada in ) throws Exception {
		logger.trace("INICIO::FusionCarterasClientes.ValorizacionInstrumentos");
		long inicio = System.currentTimeMillis();

		List<ObjEntradaInstrumentos> ListaAValorizar1 = new ArrayList<ObjEntradaInstrumentos> () ;
		List<ObjEntradaInstrumentos> ListaAValorizar2 = new ArrayList<ObjEntradaInstrumentos> () ;
		ValorizarInstrumento valorizador = new ValorizarInstrumento();
		ValorizarInstrumentoEntrada ValIn = new ValorizarInstrumentoEntrada();
		ValIn.p_IdEmpresa = in.p_IdEmpresa; 
		ValIn.p_IdUsuario = in.p_IdUsuario;
		ValIn.p_IdModulo = in.p_IdModulo;
		ValIn.p_IdPais = in.p_IdPais;
		ValIn.p_IdMonedaAdicional = "CLP";
		ValIn.p_OrigenProcesamiento = "MESA_UI";

		Long IdValorizacion = 0L;
/*
* Construccion Primera valorizacion (de 2), utilizada para optener la tasa y el precio 
* se busca obtener precio y tasa por lo que se envian nulos
*/ 

		for (ObjInstrumento CarteraINGTIT : CarterasIngreso) {
			ObjEntradaInstrumentos TestV = LLenarObjEntradaInstrumento(CarteraINGTIT, IdValorizacion, null , 0 , "FIRST", null);
			IdValorizacion++;
			ListaAValorizar1.add(TestV);
		}ValIn.p_Instrumentos = ListaAValorizar1;

		ValorizarInstrumentoSalida ResultadoValorizador = new ValorizarInstrumentoSalida();

		logger.debug("Llamada 1 al valorizador (obtener tasa/precio) - instrumentos: " + ListaAValorizar1.size());
		ResultadoValorizador = valorizador.logica(con,ValIn); // -- Primer llamado al valorizador donde se busca obtener la tasa -- //



		List<ObjResultadoValorizacion> InstrumentosValorizados = ResultadoValorizador.P_OUT_INSTRUMENTOSVALORIZADOS;
		List<ObjResultadoValorizacion> InstrumentosCOMP = new ArrayList<ObjResultadoValorizacion>();
		Integer ContadorCOMP = 0;
		for (int icc = 0; icc<InstrumentosValorizados.size();icc++) { // se filtra el listado de valorizaciones obtenidas para obtener solo las COMP
			ObjResultadoValorizacion instrumentoActual = InstrumentosValorizados.get(icc);
			if ("COMP".equals(instrumentoActual.DimensionTasa)) {
					InstrumentosCOMP.add(instrumentoActual);
					Valorizaciones valFinalActual = ValorizacionesFinal.get(ContadorCOMP);
					if ( instrumentoActual.Tasa != null){ 
						valFinalActual.Tasa = instrumentoActual.Tasa;
						valFinalActual.PosValorizacion= icc;
						logger.debug("Tasa Valorizada: " +  instrumentoActual.Tasa  +" | Monto: "+instrumentoActual.MontoCompra);
						valFinalActual.ValorizacionOK = true;
				}else {
					logger.warn("Valorizacion (1ra pasada) sin tasa para el instrumento: " + instrumentoActual);
					valFinalActual.ValorizacionOK = false;
					// Si falla la valorizacion y este no regresa una tasa, se guarda el id de la valorizacion fallida para descartarla mas adelante					
				}
				ContadorCOMP++;
			}
		} 
/*
* Segunda valorizacion para optener el Monto deacuerdo a la tasa dada
*/
		IdValorizacion = 0L; int icv = 0;
		for (ObjInstrumento CarteraINGTIT : CarterasIngreso) { 
			if (ValorizacionesFinal.get(icv).ValorizacionOK && ValorizacionesFinal.get(icv).ValorizacionOK) {
				ObjEntradaInstrumentos TestV = LLenarObjEntradaInstrumento(CarteraINGTIT, IdValorizacion , InstrumentosCOMP , icv , "SECOND", null);
				IdValorizacion++;
				ListaAValorizar2.add(TestV); 
			}  
			icv++;  
		}
		InstrumentosCOMP.clear();
		ValIn.p_Instrumentos = null;
		ValIn.p_Instrumentos = ListaAValorizar2;
		logger.debug("Llamada 2 al valorizador (obtener monto con tasa) - instrumentos: " + ListaAValorizar2.size());
		
		ResultadoValorizador = valorizador.logica(con,ValIn); // -- Segundo llamado al valorizador donde se obtiene dada una tasa el precio y monto -- //
		
		InstrumentosValorizados = ResultadoValorizador.P_OUT_INSTRUMENTOSVALORIZADOS; 
		ContadorCOMP = 0;
		for (int icc = 0; icc<InstrumentosValorizados.size();icc++) { // se filtra el listado de valorizaciones obtenidas para obtener solo las COMP
			ObjResultadoValorizacion instrumentoActual = InstrumentosValorizados.get(icc);
			if ("COMP".equals(instrumentoActual.DimensionTasa)) {
				InstrumentosCOMP.add(instrumentoActual);
				Valorizaciones valFinalActual = ValorizacionesFinal.get(ContadorCOMP);
				if (instrumentoActual.Precio == null && instrumentoActual.MontoMonedaInstrumento == null ) {
					logger.warn("Valorizacion (2da pasada) sin precio/monto para el instrumento: " + instrumentoActual);
					valFinalActual.ValorizacionOK = false;
				}else {
					valFinalActual.Tasa = instrumentoActual.Tasa;
				}
				ContadorCOMP++;
			}
		} 
		ContadorCOMP = 0; int ContadorVal = 0;
		for ( int cc = 0 ; cc< CarterasIngreso.size() ; cc ++) {
			Valorizaciones valActual = ValorizacionesFinal.get(ContadorVal);
			if (valActual.ValorizacionOK) {
				ObjResultadoValorizacion compActual = InstrumentosCOMP.get(ContadorCOMP);
				ObjInstrumento carteraActual = CarterasIngreso.get(cc);
				carteraActual.Tasa = compActual.Tasa;
				carteraActual.Precio = compActual.Precio;
				carteraActual.Monto = compActual.MontoMonedaInstrumento;
				carteraActual.MontoEnMonedaInstrumento = compActual.MontoMonedaInstrumento;
				ContadorCOMP++;
			}else {
				List<ObjInstrumento> retirosGrupo = CarterasRetiro.get(cc);
				for (ObjInstrumento carteraNoFusionada: retirosGrupo) {
					String codigoMensaje = "UI_CARTERA_NO_FUSIONADA";
					String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje).add("FolioCartera", carteraNoFusionada.FolioCartera)
							.add("Precio", carteraNoFusionada.Precio).add("MontoMonedaInstrumento", carteraNoFusionada.MontoEnMonedaInstrumento).msg();
		            out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_INFORMACION,codigoMensaje,mensaje));		           					
				}
				CarterasIngreso.remove(cc);
				CarterasRetiro.remove(cc);
				cc--;
			}
			ContadorVal++;
		}

		logger.info("Valorizacion finalizada: " + CarterasIngreso.size() + " grupo(s) valorizado(s) correctamente");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.ValorizacionInstrumentos", fin-inicio);
		return ResultadoValorizador; 
	}

/**
* Exclusivamente para mercado RV.
* El "monto" y la "cantidad" del registro fusionado ya vienen sumados desde ConstruirCarterasIngreso, por lo
* que el precio del instrumento fusionado corresponde directamente al PRECIO PROMEDIO PONDERADO por cantidad:
*
*     PrecioPromedioPonderado = MontoTotalGrupo / CantidadTotalGrupo
*
* Si alguna cartera del grupo no trae Cantidad o PrecioCompra, se descarta el GRUPO COMPLETO (no solo esa
* cartera): EjecutarIngresoRetiro retira todas las carteras del grupo cuando ValorizacionOK=true, por lo que
* excluir una cartera del calculo pero igual retirarla dejaria el monto fusionado descuadrado contra lo retirado.
* No se llama a ValorizarInstrumento ni se consulta Tasa, porque RV no valoriza por tasa.
*/
	private void ValorizarPromedioPonderadoRV(Connection con, FusionCarterasEntrada in, List<List<Cartera>> listadoCarterasIN) throws Exception {
		logger.trace("INICIO::FusionCarterasClientes.ValorizarPromedioPonderadoRV");
		long inicio = System.currentTimeMillis();

		for (int i = 0; i < CarterasIngreso.size(); i++) {
			ObjInstrumento instrumento = CarterasIngreso.get(i);
			Valorizaciones valGrupo = ValorizacionesFinal.get(i);
			List<Cartera> grupo = listadoCarterasIN.get(i);

			BigDecimal cantidadTotal = BigDecimal.ZERO;
			BigDecimal montoTotal = BigDecimal.ZERO;
			boolean grupoValido = true;

			for (Cartera cartera : grupo) {

				if (cartera.INGCantidad == null || cartera.PrecioCompra == null) {
					logger.warn("RV - Grupo IdGeneracionMovimiento=" + instrumento.IdGenerarMovimientoCustodia
							+ " se descarta completo: cartera Folio=" + cartera.FolioCartera + " sin cantidad o precio de compra");
					grupoValido = false;
					break; // todo-o-nada: si una cartera del grupo no valoriza, el grupo completo queda fuera
				}

				BigDecimal montoRegistro = cartera.INGCantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128);
				cantidadTotal = cantidadTotal.add(cartera.INGCantidad, MathContext.DECIMAL128);
				montoTotal = montoTotal.add(montoRegistro, MathContext.DECIMAL128);

				logger.debug("RV - Folio=" + cartera.FolioCartera
						+ " Cantidad=" + cartera.INGCantidad
						+ " PrecioCompra=" + cartera.PrecioCompra
						+ " MontoRegistro=" + montoRegistro
					);
			}

			if (grupoValido && cantidadTotal.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal precioPromedioPonderado = montoTotal.divide(cantidadTotal, MathContext.DECIMAL128);
				instrumento.Cantidad = cantidadTotal;
				instrumento.Monto = montoTotal;
				instrumento.Precio = precioPromedioPonderado;
				instrumento.MontoEnMonedaInstrumento = montoTotal;
				instrumento.Tasa = BigDecimal.ZERO; // RV no utiliza tasa; BD no permite NULL
				valGrupo.ValorizacionOK = true;
				valGrupo.MontoSuma = montoTotal;

				logger.debug("RV - Grupo IdGeneracionMovimiento=" + instrumento.IdGenerarMovimientoCustodia
						+ " CantidadTotal=" + cantidadTotal + " MontoTotal=" + montoTotal
						+ " PrecioPromedioPonderado=" + precioPromedioPonderado);
			} else {
				valGrupo.ValorizacionOK = false;

				logger.warn("RV - Grupo IdGeneracionMovimiento=" + instrumento.IdGenerarMovimientoCustodia
						+ " sin cantidad/monto valido para calcular el precio promedio ponderado");
			}
		}

		// Descarta de CarterasIngreso/CarterasRetiro los grupos que no pudieron valorizarse (mismo criterio que
		// ValorizacionInstrumentos para RF/IF); ValorizacionesFinal se conserva completo para LLenarHistorico.
		int ContadorVal = 0;
		for (int cc = 0; cc < CarterasIngreso.size(); cc++) {
			if (!ValorizacionesFinal.get(ContadorVal).ValorizacionOK) {
				ObjInstrumento carteraDescartada = CarterasIngreso.get(cc);
				String codigoMensaje = "UI_CARTERA_NO_FUSIONADA";
				String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje)
						.add("FolioCartera", carteraDescartada.FolioCartera)
						.add("Precio", carteraDescartada.Precio)
						.add("MontoMonedaInstrumento", carteraDescartada.MontoEnMonedaInstrumento).msg();		
				out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_INFORMACION,codigoMensaje,mensaje));
				CarterasIngreso.remove(cc);
				CarterasRetiro.remove(cc);
				cc--;
			}
			ContadorVal++;
		}

		logger.info("Valorizacion RV (precio promedio ponderado) finalizada: " + CarterasIngreso.size() + " grupo(s) valorizado(s) correctamente");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.ValorizarPromedioPonderadoRV", fin-inicio);
	}

	private void LLenarHistorico(IngresoRetiroTitulosSalida resultado , List<List<Cartera>> ListadoCarterasIN, Connection con, FusionCarterasEntrada in) throws Exception {
		logger.trace("INICIO::FusionCarterasClientes.LLenarHistorico");
		long inicio = System.currentTimeMillis();

		String Usuario = in.p_IdUsuario.toString();
		String Empresa = in.p_IdEmpresa.toString();
		List<AddHISFusion> QueryFusion2 = new ArrayList<AddHISFusion>();
		List<AddFusionAux> QueryFusion3 = new ArrayList<AddFusionAux>();
		
		int contadorValOK = 0;
		for (int cont1 =0 ; cont1 < CarterasIngreso.size() ; cont1++) {
			ObjInstrumento carteraIngreso = CarterasIngreso.get(cont1);			
			AddHISFusion parametros1 = new AddHISFusion();
			
			parametros1.FechaFusion = fechaSistema;
			parametros1.CantidadFusion = carteraIngreso.Cantidad;
			parametros1.TasaFusion = carteraIngreso.Tasa;
			parametros1.ValorRescateFusion = carteraIngreso.ValorRescate;
			parametros1.IdClasificacionTipoInstrumento = carteraIngreso.IdClasificacionTipoInstrumento;
			//parametros1.MontoFusion = Montos.get(cont1) ; // El monto de la fusion se agrega en el siguiente ciclo for
			parametros1.MontoValorizado = carteraIngreso.MontoEnMonedaInstrumento;
			parametros1.IdCarteraResultante = null;
			parametros1.IdModulo = in.p_IdModulo;
			parametros1.IdUsuario = Usuario;
			parametros1.IdEmpresa = Empresa;		
			QueryFusion2.add(parametros1); 
			
			contadorValOK++;
		};

		SQL sql1=new SQL();
		if (resultado.P_OUT_ERRORESCONTADOR == 0) { 
			List<ObjResultado> MensajesResultado = resultado.P_OUT_RESULTADO; 
			contadorValOK = 0; 
			for (int i = 0 ; i< ValorizacionesFinal.size(); i++  ) { 
				Valorizaciones valActual = ValorizacionesFinal.get(i);
				if (valActual.ValorizacionOK) { 
					AddHISFusion queryActual = QueryFusion2.get(contadorValOK);
					ObjResultado resultadoActual = MensajesResultado.get(contadorValOK);
					List<Cartera> listaGrupo = ListadoCarterasIN.get(i);
				
					queryActual.IdCarteraResultante  =  resultadoActual.IdCustodia; 
					queryActual.MontoFusion =  valActual.MontoSuma;
					queryActual.AjustadoManualmente = Boolean.TRUE.equals(valActual.AjustadoManualmente) ? "S" : "N";
					queryActual.TipoAjusteManual = valActual.TipoAjusteManual;
					FolioResultantePorGrupo.put(valActual.IdGeneracionMovimiento, resultadoActual.IdCustodia);
					
					for (int cci = 0 ; cci < listaGrupo.size(); cci++  ) {
						Cartera carteraDetalle = listaGrupo.get(cci);
						AddFusionAux parametros2 = new AddFusionAux();
						parametros2.IdCartera = carteraDetalle.IdCartera;
						parametros2.FolioCustodia = resultadoActual.IdCustodia;
						parametros2.Cantidad = carteraDetalle.Cantidad;
						parametros2.Tasa = carteraDetalle.TasaCompra;
						parametros2.ValorRescate = carteraDetalle.ValorRescate;
						parametros2.CorteMinimo = carteraDetalle.CorteMinimo;
						parametros2.EsFungible = carteraDetalle.EsFungible;
						QueryFusion3.add(parametros2);

						logger.debug(" ||--||  Query para Detalle: " + parametros2);
					}
					logger.debug(" ||--||--||  Query para Historico: " + queryActual);
					contadorValOK++;
				}

			} 

			logger.info("Registros a insertar en Historico: " + QueryFusion2.size() + " | Detalle Fusion Aux: " + QueryFusion3.size());

			if (QueryFusion2.size() > 0)
			{
				sql1.batch(con,SQL_AddHistorico,QueryFusion2,AddHISFusion.class);
				logger.info("Batch de Historico insertado correctamente: " + QueryFusion2.size() + " registro(s)");
			}

			if (QueryFusion3.size() > 0)
			{
				sql1.batch(con,SQL_AddFusionAux,QueryFusion3,AddFusionAux.class);
				logger.info("Batch de FusionAux insertado correctamente: " + QueryFusion3.size() + " registro(s)");
			}
		} else {
			logger.warn("No se registra Historico: el resultado de Ingreso/Retiro tiene errores (" + resultado.P_OUT_ERRORESCONTADOR + ")");
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.LLenarHistorico", fin-inicio);
	}

	private ObjEntradaInstrumentos LLenarObjEntradaInstrumento(ObjInstrumento CarteraINGTIT , Long IdVal , List<ObjResultadoValorizacion> ResVal , int nval, String Nvalorizacion, BigDecimal ValorManual){
		logger.trace("INICIO::FusionCarterasClientes.LLenarObjEntradaInstrumento");
		long inicio = System.currentTimeMillis();

		ObjEntradaInstrumentos ItemAValorizar = new ObjEntradaInstrumentos();
 
		ItemAValorizar.IdValorizacion = IdVal;
	    ItemAValorizar.OrigenProcesamiento = "MESA_UI";
	    ItemAValorizar.FechaValorizacion = fechaSistema;
	    ItemAValorizar.FechaOperacion = fechaSistema;                                 //Solo para operaciones a plazo
	    ItemAValorizar.FechaVencimientoOperacion = CarteraINGTIT.FechaVencimiento;                 //Solo para operaciones a plazo     
	    ItemAValorizar.IdClasificacionTipoInstrumento = CarteraINGTIT.IdClasificacionTipoInstrumento; 
	    ItemAValorizar.IdTipoInstrumento = CarteraINGTIT.IdTipoInstrumento; 
	    ItemAValorizar.IdInstrumento = CarteraINGTIT.IdInstrumento;
	    ItemAValorizar.Nemotecnico = CarteraINGTIT.Nemotecnico;
	    ItemAValorizar.FechaVencimiento = CarteraINGTIT.FechaVencimiento;
	    ItemAValorizar.FechaEmision = fechaSistema;
	    ItemAValorizar.IdMonedaInstrumento = CarteraINGTIT.IdMonedaInstrumento;
	    ItemAValorizar.IdMonedaEmisionInstrumento = CarteraINGTIT.IdMonedaEmision; //CarteraINGTIT.IdTipoReajuste;
	    ItemAValorizar.IdTipoReajusteInstrumento = CarteraINGTIT.IdTipoReajuste;
	    ItemAValorizar.Emisor = CarteraINGTIT.IdEmisor;                         //valorizacionCL
	    ItemAValorizar.IdCondicionDeLiquidacion = CarteraINGTIT.IdCondicionLiquidacion;           //valorizacionCL
	    ItemAValorizar.FechaDeLiquidacion = CarteraINGTIT.FechaLiquidacion; 
	    ItemAValorizar.Cantidad = CarteraINGTIT.Cantidad; 
	    ItemAValorizar.ValorRescate = CarteraINGTIT.ValorRescate; 
	    ItemAValorizar.TablaOrigen = "CARTERA";
 
	    // Dependiendo de la valorizacion Nvalorizacion sera FIRST o SECOND al llamarse.
	    if ( Nvalorizacion.equals("FIRST")) {
	    		ItemAValorizar.PrecioCompra = null;
	    		ItemAValorizar.TasaCompra = null;
	    		ItemAValorizar.MontoCompra = CarteraINGTIT.Monto;
	    } else if (Nvalorizacion.equals("SECOND")) {
	    		ItemAValorizar.PrecioCompra = null;
	    		ItemAValorizar.TasaCompra = ResVal.get(nval).Tasa;
	    		ItemAValorizar.MontoCompra = null;
	    } else if (Nvalorizacion.equals("AJUSTE_TASA")) {
	    		// Ajuste manual por Tasa: mismo patron que SECOND, pero la tasa la puso el usuario, no el valorizador.
	    		ItemAValorizar.PrecioCompra = null;
	    		ItemAValorizar.TasaCompra = ValorManual;
	    		ItemAValorizar.MontoCompra = null;
	   } else { // "AJUSTE_PRECIO"
	    		// Al dejar TasaCompra=null, el propio ValorizarInstrumento resuelve Precio=PrecioCompra y
	    		// Tasa=null (ver PROC_WS.Tasa/.Precio), sin necesitar setear ValorizarPor explicitamente.
	    		ItemAValorizar.PrecioCompra = ValorManual;
	    		ItemAValorizar.TasaCompra = null;
	    		ItemAValorizar.MontoCompra = null;
	   }
	    if (Debug)  {
	    	logger.debug("----------------DatosValorizador-.---------" + Nvalorizacion);
	    	logger.debug("Precio: "+  ItemAValorizar.PrecioCompra + " Tasa: " + ItemAValorizar.TasaCompra + " Monto:" + ItemAValorizar.MontoCompra + " Cantidad: " + ItemAValorizar.Cantidad);
	    }
	    long fin = System.currentTimeMillis();
	    logger.trace("FIN::FusionCarterasClientes.LLenarObjEntradaInstrumento", fin-inicio);
	    return ItemAValorizar;
	}

/**
* Arma el resultado de previsualización (cabecera + detalle) para TODOS los grupos candidatos,
* incluyendo aquellos cuya valorización falló (para que el usuario vea que quedarían excluidos
* de la fusión real). Usa CarterasIngreso/ValorizacionesFinal ya poblados por
* ConstruirCarterasIngreso(...) + la valorizacion correspondiente (ValorizacionInstrumentos para RF/IF,
* ValorizarPromedioPonderadoRV para RV), llamadas antes desde logica().
*/
	private void ConstruirPreview(List<List<Cartera>> ListadoCarterasIN, Connection con, FusionCarterasEntrada in) throws Exception {
		logger.trace("INICIO::FusionCarterasClientes.ConstruirPreview");
		long inicio = System.currentTimeMillis();

   // Mapa IdGeneracionMovimiento -> instrumento fusionado YA valorizado (solo contiene los que sí lograron valorizarse,
   // porque la valorizacion (RF/IF o RV) remueve de CarterasIngreso los que fallaron)
		HashMap<Long, ObjInstrumento> InstrumentosValorizadosPorGrupo = new HashMap<>();
		for (ObjInstrumento oi : CarterasIngreso) {
			InstrumentosValorizadosPorGrupo.put(oi.IdGenerarMovimientoCustodia, oi);
		}

		Long IdGen = 1L; int IndexCartera = 0;

		for ( List<Cartera> Lista : ListadoCarterasIN ) {

			Cartera CarteraFusionada = ListadoCarterasIN.get(IndexCartera).get(0);
			Valorizaciones ValGrupo = ValorizacionesFinal.get(IndexCartera);

	        RegistrosFusionPreview cabecera = new RegistrosFusionPreview();
	        cabecera.IdGeneracionMovimiento = IdGen;
	        cabecera.IdPortafolio = CarteraFusionada.IdPortafolio;
	        cabecera.IdClasificacionTipoInstrumento = CarteraFusionada.IdClasificacionTipoInstrumento;
	        cabecera.Nemotecnico = CarteraFusionada.Nemotecnico;
	        cabecera.NemoSVS = CarteraFusionada.NemoSVS;
	        cabecera.IdEmisor = CarteraFusionada.IdEmisor;
	        cabecera.CarterasFusionadas = Lista.size();

	        BigDecimal Cantidad = BigDecimal.ZERO, Monto = BigDecimal.ZERO, ValorRescate = BigDecimal.ZERO;

	        boolean esRV = "RV".equalsIgnoreCase(CarteraFusionada.IdClasificacionTipoInstrumento);

	        for (Cartera CarteraActual : Lista) {
	        	if (esRV) {
	        		// RV: la cantidad siempre se suma (mismo criterio que ConstruirCarterasIngreso). El Monto de RV
	        		// para el detalle/preview NO se arma aqui con INGMontoCompraActualMonedaInstrumento; el monto
	        		// fusionado real (Cantidad x PPP) ya viene resuelto en InstrumentoValorizado.Monto mas abajo.
	        		Cantidad = Cantidad.add(CarteraActual.INGCantidad);
	        	} else if (CarteraActual.IdClasificacionTipoInstrumento.equals("RF")) {
	        		Cantidad = Cantidad.add(CarteraActual.INGCantidad);
	        		Monto    = Monto.add(CarteraActual.INGMontoCompraActualMonedaInstrumento);
	        	} else {
	        		Cantidad = CarteraActual.EsFungible.equals("N")
	        				? Cantidad.add(CarteraActual.INGCantidad)
	        						: BigDecimal.valueOf(1);
	        		Monto    = Monto.add(CarteraActual.INGMontoCompraActualMonedaInstrumento);
	        	}
	        	ValorRescate = ValorRescate.add(CarteraActual.INGValorRescate);

	            RegistrosDetalleFusionPreview detalle = new RegistrosDetalleFusionPreview();
	            detalle.IdGeneracionMovimiento = IdGen;
	            detalle.FolioCartera     = CarteraActual.FolioCartera;
	            detalle.Cantidad         = CarteraActual.Cantidad;
	            detalle.ValorRescate     = CarteraActual.ValorRescate;
	            detalle.FechaCompra      = CarteraActual.FechaComprobante != null ? CarteraActual.FechaComprobante.toString() : null;
	            detalle.FechaLiquidacion = CarteraActual.FechaLiquidacion != null ? CarteraActual.FechaLiquidacion.toString() : null;
	            detalle.MontoCompra      = CarteraActual.MontoCompraMonedaInstrumento;
	            detalle.TasaCompra       = CarteraActual.TasaCompra;
	            out.P_OUT_DETALLE.add(detalle);
	        }

	        cabecera.INGCantidad = Cantidad;	        	        

	        ObjInstrumento InstrumentoValorizado = InstrumentosValorizadosPorGrupo.get(IdGen);
	        cabecera.MontoCompraMonedaInstrumento = esRV? (InstrumentoValorizado != null ? InstrumentoValorizado.Monto : null): Monto;
	        
	        if (ValGrupo.ValorizacionOK && InstrumentoValorizado != null) {
	        	// Para RF/IF, Monto corresponde al monto obtenido del valorizador. Para RV, corresponde
	        	// al Monto sumado del grupo (Cantidad x PrecioPromedioPonderado == Monto sumado).
	        	cabecera.ValorCalculadoFus = InstrumentoValorizado.Monto;
	        	cabecera.ValorizacionExitosa = true;
	        	cabecera.Precio = InstrumentoValorizado.Precio;
	        	cabecera.Tasa = esRV ? null : InstrumentoValorizado.Tasa;
	        	cabecera.AjustadoManualmente = Boolean.TRUE.equals(ValGrupo.AjustadoManualmente);
	        } else {
	           cabecera.ValorCalculadoFus = null;
	           cabecera.ValorizacionExitosa = false;
	           cabecera.Precio = null;
	           cabecera.Tasa = null;
	           String codigoMensaje = "UI_INSTRUMENTO_NO_VALORIZADO";
	           String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje).add("Nemotecnico", cabecera.Nemotecnico).add("IdPortafolio", cabecera.IdPortafolio).msg();
	           out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_INFORMACION,codigoMensaje,mensaje));         
	        }

	        out.P_OUT_RESULTADO.add(cabecera);

	        IdGen++; IndexCartera++;
		}
		
		String codigoMensaje = "UI_PREVIEW_FUSION_GENERADO";
		String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje).add("CantidadFusiones", out.P_OUT_RESULTADO.size()).msg();
		out.P_OUT_MENSAJES.add(new MensajeSalida(null,MensajeSalida.MENSAJE_OK,codigoMensaje,mensaje));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.ConstruirPreview", fin-inicio);
	}
	
	/**
	* Aplica los ajustes manuales de Precio/Tasa que el usuario haya hecho en el preview. Se ejecuta tanto en
	* modo simulación como en modo real, usando exactamente el mismo código, para que no puedan existir dos
	* resultados distintos (uno mostrado, otro persistido).
	*
	* El grupo se localiza por el conjunto de Folios (ValorizacionesFinal.Folios), no por IdGeneracionMovimiento,
	* porque este último es un correlativo interno de la corrida y no una clave de negocio.
	*/
	private void AplicarAjustesManuales(Connection con, FusionCarterasEntrada in) throws Exception {
		if (in.p_AjustesManuales == null || in.p_AjustesManuales.isEmpty()) return;
		logger.trace("INICIO::FusionCarterasClientes.AplicarAjustesManuales");

		for (AjusteManualFusion ajuste : in.p_AjustesManuales) {
			if (ajuste.Folios == null || ajuste.Folios.isEmpty() || ajuste.Valor == null) {
				logger.warn("Ajuste manual ignorado por datos incompletos: " + Utils.printJson(ajuste, false));
				continue;
			}
			Set<Long> foliosAjuste = new HashSet<>(ajuste.Folios);

			int idxGrupo = -1;
			for (int i = 0; i < ValorizacionesFinal.size(); i++) {
				if (new HashSet<>(ValorizacionesFinal.get(i).Folios).equals(foliosAjuste)) {
					idxGrupo = i;
					break;
				}
			}
			if (idxGrupo < 0) {
				logger.warn("Ajuste manual sin grupo correspondiente (folios ya no coinciden con ningun grupo candidato): " + foliosAjuste);
				String codigoMensaje = "UI_AJUSTE_MANUAL_GRUPO_NO_ENCONTRADO";
				String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje).msg();
				out.P_OUT_MENSAJES.add(new MensajeSalida(null, MensajeSalida.MENSAJE_INFORMACION, codigoMensaje, mensaje));
				continue;
			}

			Valorizaciones valGrupo = ValorizacionesFinal.get(idxGrupo);
			ObjInstrumento instrumento = ubicarInstrumentoPorGrupo(valGrupo.IdGeneracionMovimiento);
			if (instrumento == null) {
				// El grupo ya había quedado fuera de CarterasIngreso porque su valorización base falló;
				// un ajuste manual no puede "resucitar" un grupo que ni siquiera tiene instrumento base.
				logger.warn("Ajuste manual ignorado: el grupo IdGeneracionMovimiento=" + valGrupo.IdGeneracionMovimiento + " no tiene instrumento base (valorizacion original fallida)");
				continue;
			}

			boolean esRV = "RV".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento);
			if (esRV && !"PRECIO".equalsIgnoreCase(ajuste.TipoAjuste)) {
				logger.warn("Ajuste manual de Tasa ignorado para grupo RV (RV solo admite ajuste de Precio)");
				continue;
			}
			
			// Snapshot ANTES del ajuste: es el resultado de la valorizacion estandar (ValorizacionInstrumentos /
			// ValorizarPromedioPonderadoRV), que es exactamente lo que corrio justo antes de este metodo.
			String tipoAjusteNormalizado = esRV ? "PRECIO" : ajuste.TipoAjuste.toUpperCase();
			LogAjusteManual registroLog = new LogAjusteManual();			
			registroLog.Nemotecnico = instrumento.Nemotecnico;
			registroLog.TipoAjuste = tipoAjusteNormalizado;
			registroLog.ValorOriginal = "TASA".equals(tipoAjusteNormalizado) ? instrumento.Tasa : instrumento.Precio;
			registroLog.MontoOriginal = instrumento.Monto;

			if (esRV) {
				AjustarPrecioRV(instrumento, ajuste.Valor, valGrupo);
			} else {
				AjustarRFIF(con, in, instrumento, ajuste, valGrupo);
			}
			if (Boolean.TRUE.equals(valGrupo.ValorizacionOK)) {
				valGrupo.AjustadoManualmente = true;
				valGrupo.TipoAjusteManual = tipoAjusteNormalizado;
				} else {
				// El ajuste manual fallo: se descarta el grupo COMPLETO, igual que hace ValorizacionInstrumentos
				// cuando la valorizacion estandar falla. Si no se hace esto, el grupo queda con el instrumento
				// (con datos de la valorizacion ORIGINAL, no del ajuste) todavia en CarterasIngreso, pero
				// EjecutarIngresoRetiro ya no lo retira (revisa ValorizacionOK) -> quedaria un ingreso sin su
				// retiro correspondiente.
					logger.warn("Ajuste manual fallido: se descarta el grupo IdGeneracionMovimiento=" + instrumento.IdGenerarMovimientoCustodia + " de la fusion");
					CarterasIngreso.remove(instrumento);
				// CarterasRetiro esta indexado 1:1 por posicion con ListaListasCarteras/ValorizacionesFinal
				// (ver ConstruirCarterasIngreso), no con CarterasIngreso una vez que este ultimo se reduce. Se
				// busca la posicion correcta por IdGeneracionMovimiento en vez de asumir un indice.
					int posRetiro = -1;
					for (int p = 0; p < ValorizacionesFinal.size(); p++) {
						if (ValorizacionesFinal.get(p).IdGeneracionMovimiento.equals(instrumento.IdGenerarMovimientoCustodia)) {
							posRetiro = p; break;
						}
					}
					if (posRetiro >= 0 && posRetiro < CarterasRetiro.size()) {
						CarterasRetiro.remove(posRetiro);
					}
				}
			// Snapshot DESPUES del ajuste (independiente de si quedo OK o no, para dejar constancia del intento).
			registroLog.ValorNuevo = "TASA".equals(tipoAjusteNormalizado) ? instrumento.Tasa : instrumento.Precio;
			registroLog.MontoNuevo = instrumento.Monto;
			registroLog.ValorizacionResultanteOK = valGrupo.ValorizacionOK;
			ajustesRealizadosLog.put(instrumento.IdGenerarMovimientoCustodia, registroLog);
		}
		logger.trace("FIN::FusionCarterasClientes.AplicarAjustesManuales");
	}

	private ObjInstrumento ubicarInstrumentoPorGrupo(Long idGeneracionMovimiento) {
		for (ObjInstrumento oi : CarterasIngreso) {
			if (oi.IdGenerarMovimientoCustodia.equals(idGeneracionMovimiento)) return oi;
		}
		return null;
	}
	
	/**
	* RV no pasa por el valorizador de instrumentos: su "monto" es simplemente Cantidad x Precio (ver
	* ValorizarPromedioPonderadoRV). Reajustar el precio manualmente es, por lo tanto, solo aritmética local.
	*/
	private void AjustarPrecioRV(ObjInstrumento instrumento, BigDecimal precioAjustado, Valorizaciones valGrupo) {
		BigDecimal nuevoMonto = instrumento.Cantidad.multiply(precioAjustado, MathContext.DECIMAL128);
		instrumento.Precio = precioAjustado;
		instrumento.Monto = nuevoMonto;
		instrumento.MontoEnMonedaInstrumento = nuevoMonto;
		valGrupo.MontoSuma = nuevoMonto;
		valGrupo.ValorizacionOK = true;
		logger.info("RV - Ajuste manual de Precio aplicado. IdGeneracionMovimiento=" + instrumento.IdGenerarMovimientoCustodia
				+ " Precio=" + precioAjustado + " MontoRecalculado=" + nuevoMonto);
	}

	/**
	* RF/IF sí requieren pasar por ValorizarInstrumento, porque el monto involucra devengo/tasa y no es una
	* multiplicación simple. Se reutiliza el mismo patrón de llamada que usa ValorizacionInstrumentos en su
	* segunda pasada, mandando Precio en vez de Tasa cuando corresponde (ver LLenarObjEntradaInstrumento).
	*/
	private void AjustarRFIF(Connection con, FusionCarterasEntrada in, ObjInstrumento instrumento,
			AjusteManualFusion ajuste, Valorizaciones valGrupo) throws Exception {
		logger.info("RF/IF - Aplicando ajuste manual (" + ajuste.TipoAjuste + "=" + ajuste.Valor
				+ ") sobre IdGeneracionMovimiento=" + instrumento.IdGenerarMovimientoCustodia);

		ValorizarInstrumento valorizador = new ValorizarInstrumento();
		ValorizarInstrumentoEntrada ValIn = new ValorizarInstrumentoEntrada();
		ValIn.p_IdEmpresa = in.p_IdEmpresa;
		ValIn.p_IdUsuario = in.p_IdUsuario;
		ValIn.p_IdModulo = in.p_IdModulo;
		ValIn.p_IdPais = in.p_IdPais;
		ValIn.p_IdMonedaAdicional = "CLP";
		ValIn.p_OrigenProcesamiento = "MESA_UI";

		String variante = "TASA".equalsIgnoreCase(ajuste.TipoAjuste) ? "AJUSTE_TASA" : "AJUSTE_PRECIO";
		ObjEntradaInstrumentos item = LLenarObjEntradaInstrumento(instrumento, 0L, null, 0, variante, ajuste.Valor);

		List<ObjEntradaInstrumentos> lista = new ArrayList<>();
		lista.add(item);
		ValIn.p_Instrumentos = lista;

		ValorizarInstrumentoSalida resultado = valorizador.logica(con, ValIn);
		ObjResultadoValorizacion comp = null;
		for (ObjResultadoValorizacion r : resultado.P_OUT_INSTRUMENTOSVALORIZADOS) {
			if ("COMP".equals(r.DimensionTasa)) { comp = r; break; }
		}

		if (comp == null || (comp.Precio == null && comp.MontoMonedaInstrumento == null)) {
			logger.warn("Ajuste manual no pudo valorizarse. IdGeneracionMovimiento=" + instrumento.IdGenerarMovimientoCustodia);
			valGrupo.ValorizacionOK = false;
			String codigoMensaje = "UI_AJUSTE_MANUAL_INVALIDO";
			String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensaje)
					.add("FolioCartera", instrumento.FolioCartera).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(null, MensajeSalida.MENSAJE_ERROR, codigoMensaje, mensaje));
			return;
		}

		instrumento.Precio = comp.Precio;
		instrumento.Tasa = comp.Tasa;
		instrumento.Monto = comp.MontoMonedaInstrumento;
		instrumento.MontoEnMonedaInstrumento = comp.MontoMonedaInstrumento;
		valGrupo.Tasa = comp.Tasa;
		valGrupo.MontoSuma = comp.MontoMonedaInstrumento;
		valGrupo.ValorizacionOK = true;
	}
	
	public static void main(String[] args) { 
		Debug = true;
		FusionCarterasClientes FC = new FusionCarterasClientes();
		String json =
				"{\r\n"
				+ "    \"p_IdEmpresa\": 1,\r\n"
				+ "    \"p_IdUsuario\": 9830,\r\n"
				+ "    \"p_IdPais\": 44,\r\n"
				+ "    \"p_IdModulo\": \"CUS\",\r\n"
				+ "    \"p_IdCliente\": 1308,\r\n"
				+ "    \"p_IdPortafolio\": 6697,\r\n"
				+ "    \"p_IdClasificacionTipoInstrumento\": \"RF\",\r\n"
				+ "    \"p_IdTipoInstrumento\": null,\r\n"
				+ "    \"p_Nemotecnico\": null,\r\n"
				+ "    \"p_simularOperacion\": \"N\",\r\n"
				+ "    \"p_AjustesManuales\": []\r\n"
				+ "}";
		Gson gson = Utils.getGsonBuilder();
		FusionCarterasEntrada entrada = gson.fromJson(json, FusionCarterasEntrada.class); 

// getBOLSA02Connection()  // para pruebas en master02    getDESAConnection()   // pruebas en desa

		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties") ) 
		{
			FusionCarterasSalida salida = FC.logica(con,entrada);
			con.rollback();
		}catch (Exception e){ logger.error("Error en ejecucion de prueba (main)", e); }
	}

@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		logger.trace("INICIO::FusionCarterasClientes.call");
		long inicio = System.currentTimeMillis();
		Gson gson = Utils.getGsonBuilder(); 
		FusionCarterasEntrada in =gson.fromJson(jin.toString() , FusionCarterasEntrada.class);
		FusionCarterasClientes FC = new FusionCarterasClientes();
		FusionCarterasSalida out = FC.logica(conn,in);
		JsonParser jsonParser = new JsonParser();
		JsonObject resultado = jsonParser.parse(gson.toJson(out)).getAsJsonObject();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::FusionCarterasClientes.call", fin-inicio);
		return resultado;
	}
}