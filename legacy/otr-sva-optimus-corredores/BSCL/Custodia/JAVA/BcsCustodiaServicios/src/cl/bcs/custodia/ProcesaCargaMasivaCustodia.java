package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCustodia;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaEntrada;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaSalida;
import cl.bcs.custodia.evc.CrearEventoCapital;
import cl.bcs.custodia.evc.CrearEventoCapital.Evento;
import cl.bcs.custodia.evc.CrearEventoCapital.EventoCapital_OUT;
import cl.bcs.custodia.RetirarGarantiaJava.ObjGarantiasARetirar;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida.ObjResultado;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.Instrumento;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
 
public class ProcesaCargaMasivaCustodia implements JavaCall {

	static final Logger logger = LogManager.getLogger(ProcesaCargaMasivaCustodia.class);
	
	private final String Codigo 									= "%GARANTIAS_RV";
	private final String SQL_ACTUALIZA_ESTADO_CARGA_MASIVA			= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_ActualizaEstadoCargaMasiva.sql");
	private final String SQL_ACTUALIZA_ESTADO_CARGA_MASIVA_DETALLE	= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_ActualizaEstadoCargaMasivaDetalle.sql");
	private final String SQL_ACTUALIZA_ESTADO_CARGA_MASIVA_DETALLE_RECHAZADOS = loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_ActualizaEstadoCargaMasivaDetalleRechazados.sql");
	private final String SQL_SELECT_CARGAMASIVADETALLE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectCargaMasivaDetalle.sql");
	private final String SQL_SELECT_CARGAMASIVADETALLEPERU			= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectCargaMasivaDetallePeru.sql");
	private final String SQL_SELECT_CUENTASCUSTODIO					= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectCuentasCustodio.sql");
	private final String SQL_SELECT_TIPOOPERACIONTIPOINSTRUMENTO	= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectTipoOperacionTipoInstrumento.sql");
	private final String SQL_SELECT_CONCEPTOCUSTODIA				= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectConceptoCustodia.sql");
	private final String SQL_SELECT_MONEDAINSTRUMENTO				= "SELECT \"IdMoneda\" FROM \"BSCL.Plataforma.BD::INS.Instrumento\" WHERE \"Nemotecnico\" = :Nemo ;";
	private final String SQL_SELECT_GARANTIASARETIRAR				= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectGarantiasARetirar.sql");
	private final String SQL_SELECT_IDTIPOOPERACIONGARANTIARV		= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectIdTipoOperacionGarantiaRV.sql");
	private final String SQL_SELECT_PRECIOCIERRE                    = loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectPrecioCierre.sql");
	private final String SQL_SELECT_DATOS_INSTRUMENTOS_IF           = loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectDatosInstrumentoIF.sql");
	private final String SQL_INSERT_FOLIO_GARANTIA_IF               = loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_InsertFolioGarantiaIF.sql");
	private final String SQL_CREATE_TABLA_FOLIOS_GARANTIA_IF        = loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_CreateTableFolioGarantiaIF.sql");
	public  final String SQL_DROP_TABLA_FOLIOS_GARANTIA_IF          = "DROP TABLE #TablaFoliosCarteras";
	
	public static class DetalleCarga {
		public Long IdTipoCargaMasiva;
		public Long IdCargaMasiva;
		public Long IdCargaMasivaDetalle;
		public String TipoCargaMasiva;
		public String DocumentoIdentidad;
		public String IdCondicionLiquidacion;
		public Long PlazoEnDias;
		public String IdBoveda;
		public String CuentaCustodio;
		public String IdConcepto;
		public String Nemotecnico;
		public String Emisor;
		public String NemoGarantizado;
		public String IdTipoReajuste;
		public String FechaVencimiento;
		public String Moneda;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public BigDecimal Monto;
		public BigDecimal ValorRescate;
		public String Glosa;
		public Long IdCliente;
//		public Long IdPersona;
		public Long IdPortafolio;
		public String Fungible;
		public String Bolsa;
		public String CodigoPortafolio;
		public Long FolioCartera;
		public String TipoEvento;
		public Long NumeroDividendo;
		public String FechaLimite;
		public String FechaPago;
		public String Descripcion;
		public Long Factor;
		public BigDecimal Cuotas;
		public String IdEstadoPortafolio;
		public Long IdInstrumento;
		public String ObservacionValidacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleConceptoCustodia{
		public String IdConcepto;
		public String NombreEntidad;
		public String IdClasificacionTipoInstrumento;
		public Long IdContraparte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GarantiasARetirar{
		public Long IdGarantia;
		public Long FolioGarantia;
		public BigDecimal Cantidad;
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametrosActualizacion {
		public Long IdCargaMasiva;
		public String Fecha;  
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametrosActualizacionDeta {
		public Long IdCargaMasiva;  
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametrosActualizacionDetaRechazo {
		public Long IdCargaMasiva;
		public Long IdCargaMasivaDetalle;
		public String ObservacionValidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DetalleCuentasCustodio {
		public Long IdCuentaCustodioPortafolio;
		public String IdBoveda;
		public String Codigo;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaTipOpeInstrumento{
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMoneda;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaPrecioCierre{
        public String Nemotecnico;
        public BigDecimal PrecioCierre;
        public String toString() {
                return Utils.printJson(this, false);
        }
	}
	
	public static class IdTipoOperacionGarantiaRV extends BcsPojo{
		public Long IdTipoCargaMasiva;
		
		public IdTipoOperacionGarantiaRV() {
			this.IdTipoCargaMasiva = 0L;
		}
	}
	
	public static class FoliosGarantias extends BcsPojo{
		public Long FolioCartera;
	}
	
	//TODO: Entrada
	public static class ProcesarCargaMasivaCustodiaEntrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		public String 	p_FechaProceso;
		public Long 	p_CargaMasivaProcesar;
		public String 	p_UsaCarterasSistema;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//TODO :Salida
	public static class ProcesaCargaMasivaCustodiaSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public Long							P_OUT_ADVERTENCIASCONTADOR;
		
		public ProcesaCargaMasivaCustodiaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesarCargaMasivaCustodiaEntrada in = gson.fromJson(jin.toString(), ProcesarCargaMasivaCustodiaEntrada.class);
		ProcesaCargaMasivaCustodiaSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ProcesaCargaMasivaCustodiaSalida logica(Connection conn, ProcesarCargaMasivaCustodiaEntrada in) throws Exception {
		logger.info("INICIO::ProcesaCargaMasivaCustodia");
		long inicioServicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		ProcesaCargaMasivaCustodiaSalida out = new ProcesaCargaMasivaCustodiaSalida();
		String monedaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String localizacionSistema	= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		try {
			//TODO : PRINCIPAL
			HashMap<String,Object> regDetalleCarga = new HashMap<>();
			regDetalleCarga.put("IdEmpresa", in.p_IdEmpresa);
			regDetalleCarga.put("IdCargaMasiva", in.p_CargaMasivaProcesar);
			List<DetalleCarga> rsDetalleComun = new ArrayList<DetalleCarga>();
			
			switch (localizacionSistema) {
			case "PE":
				List<DetalleCarga> rsDetallePe = _sql.query(conn, SQL_SELECT_CARGAMASIVADETALLEPERU, regDetalleCarga, DetalleCarga.class);
				rsDetalleComun.addAll(rsDetallePe);
				break;

			default:
				List<DetalleCarga> rsDetalle = _sql.query(conn, SQL_SELECT_CARGAMASIVADETALLE, regDetalleCarga, DetalleCarga.class);
				rsDetalleComun.addAll(rsDetalle);
				break;
			}
			
			Long idTipoOperacion;	
			String tipoCargaMasiva;

			if(rsDetalleComun != null && !rsDetalleComun.isEmpty()) {
				idTipoOperacion = rsDetalleComun.get(0).IdTipoCargaMasiva;
				tipoCargaMasiva = rsDetalleComun.get(0).TipoCargaMasiva;
			} else {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CARGA_MASIVA_NO_EXISTE",new HashMap<>(), conn, in.p_IdEmpresa));
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;
			}
		
			List<DetalleCarga> portafoliosNoActivos = new ArrayList<DetalleCarga>();
			
			if(tipoCargaMasiva != null && !tipoCargaMasiva.isEmpty() && !"DIVIDENDO_EN_CUOTAS_FFMM".equalsIgnoreCase(tipoCargaMasiva)) {
				//Se aislan a los portafolios no activos en una lista a parte
				portafoliosNoActivos = rsDetalleComun
						.stream()
						.filter(portafolio->!"ACT".equalsIgnoreCase(portafolio.IdEstadoPortafolio))
						.collect(Collectors.toList());
	
				//Se aislan los porfolios activos en la lista principal
				rsDetalleComun = rsDetalleComun
						.stream()
						.filter(portafolio->"ACT".equalsIgnoreCase(portafolio.IdEstadoPortafolio))
						.collect(Collectors.toList());
			}

			HashMap<String, Object> parametro = new HashMap<String, Object>(1);
			parametro.put("Codigo", Codigo);
			List<IdTipoOperacionGarantiaRV> garantiasRV = _sql.query(conn, SQL_SELECT_IDTIPOOPERACIONGARANTIARV, parametro, IdTipoOperacionGarantiaRV.class);
					
			for(IdTipoOperacionGarantiaRV idGarantiaRV:garantiasRV){
				if(idGarantiaRV.IdTipoCargaMasiva == idTipoOperacion) {
					Boolean decimales= tieneDecimales(rsDetalleComun);
					if(decimales) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CANTIDAD_GARANTIA_RV",new HashMap<>(), conn, in.p_IdEmpresa));
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;
					}
				}
			}
			
			List<DetalleCarga> eventosCapital = rsDetalleComun
												.parallelStream()
												.filter(detalle -> "DIVIDENDO_EN_CUOTAS_FFMM".equals(detalle.TipoCargaMasiva))
												.collect(Collectors.toList());
			
			if(eventosCapital.size() == 0) {
				List<DetalleCarga> clientesErroneos = rsDetalleComun
						.parallelStream()
						.filter(cliente->esValorInvalido(cliente.IdCliente) || esValorInvalido(cliente.IdPortafolio))
						.collect(Collectors.toList());
				
				if(clientesErroneos.size() > 0) {
					for(DetalleCarga detalleCarga : clientesErroneos) {
						HashMap<String,String> datosMensaje = new HashMap<String,String>(4);
						datosMensaje.put("DocumentoIdentidad", detalleCarga.DocumentoIdentidad);
						datosMensaje.put("CodigoPortafolio", detalleCarga.CodigoPortafolio);
						MensajeSalida mensaje = new MensajeSalida(detalleCarga.IdCargaMasiva, MensajeSalida.MENSAJE_ERROR, "UI_CLIENTE_PORTAFOLIO_NO_EXISTE_CARGA_MASIVA_CUSTODIA", datosMensaje, conn, in.p_IdEmpresa);
						out.P_OUT_MENSAJES.add(mensaje);
					}
					out.P_OUT_ERRORESCONTADOR = Long.valueOf(clientesErroneos.size());
					return out;
				}
			}
		
			ConsultarDatosInstrumentos obtenerDatosInstrumentos = new ConsultarDatosInstrumentos();
			ConsultarDatosInstrumento_Entrada inObtenerDatosInstrumentos = new ConsultarDatosInstrumento_Entrada();
			inObtenerDatosInstrumentos.p_IdEmpresa = in.p_IdEmpresa;
			inObtenerDatosInstrumentos.p_IdModulo = in.p_IdModulo;
			inObtenerDatosInstrumentos.p_IdUsuario = in.p_IdUsuario;
			inObtenerDatosInstrumentos.p_CreaOpcionAutomatica = "N";
			inObtenerDatosInstrumentos.p_Instrumentos = new ArrayList<ConsultaInstrumento>();
			
			List<ConsultaInstrumento> instrumentosEntrada = new ArrayList<ConsultaInstrumento>();
			Long i = 0L;
			
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
			
			CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 2001L;
			
			HashMap<String,Object> regInsruGarantizado = new HashMap<>();
			ArrayList<String> rInsruGarantizado = new ArrayList<String>();
			
			HashMap<String,Object> regCuentaCustodioPortafolio = new HashMap<>();
			List<String> rCodigoCustodia = new ArrayList<String>();
			
			HashMap<String,Object> regConceptoCustodia = new HashMap<>();
			ArrayList<String> rConceptoCustodia = new ArrayList<String>();
			
			List<FechasIn> fechasEntrada = new ArrayList<FechasIn>();
			for (DetalleCarga recorreDeta: rsDetalleComun){
				CalculoEntrada c1 = new CalculoEntrada();
				FechasIn fechasIn = new FechasIn();
				ConsultaInstrumento instruIn = new ConsultaInstrumento();
				ConsultaTipOpeInstrumento instruInGar = new ConsultaTipOpeInstrumento();
				
				fechasIn.fechaInicial = in.p_FechaProceso;
				fechasIn.numeroDias = recorreDeta.PlazoEnDias;
				fechasEntrada.add(fechasIn);
				
				instruIn.Correlativo = i++;
				instruIn.IdInstrumento = null;
				instruIn.Nemotecnico = recorreDeta.Nemotecnico;
				instrumentosEntrada.add(instruIn);

				instruInGar.Nemotecnico = recorreDeta.NemoGarantizado;
				rInsruGarantizado.add(instruInGar.Nemotecnico);
				
				rCodigoCustodia.add(recorreDeta.CuentaCustodio);
				
				rConceptoCustodia.add(recorreDeta.IdConcepto);
				
				c1.Fecha = in.p_FechaProceso;
				c1.IdInterno = recorreDeta.IdCargaMasivaDetalle;
				c1.IdMonedaDestino = monedaSistema;
				if(recorreDeta.Moneda != ""){
				c1.IdMonedaOrigen = recorreDeta.Moneda;
				}else{
					HashMap<String, Object> params = new HashMap<>();
					params.put("Nemo", instruIn.Nemotecnico);
					List<ConsultaTipOpeInstrumento> monedaInstrumento = _sql.query(conn, SQL_SELECT_MONEDAINSTRUMENTO, params, ConsultaTipOpeInstrumento.class);
					c1.IdMonedaOrigen = monedaInstrumento.get(0).IdMoneda ;	
				}
				
				c1.IdTrabajo = 2001L;
				c1.IdUso = "ORDENES";								
				if(recorreDeta.Monto.compareTo(BigDecimal.ZERO) == 0){
                    if(recorreDeta.Precio.compareTo(BigDecimal.ZERO) != 0){
                            recorreDeta.Precio = recorreDeta.Precio;
                    }else{
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("Nemotecnico", recorreDeta.Nemotecnico);
                            params.put("Fecha", in.p_FechaProceso);
                            List<ConsultaPrecioCierre> precio = _sql.query(conn, SQL_SELECT_PRECIOCIERRE, params, ConsultaPrecioCierre.class);
                            recorreDeta.Precio = (!precio.isEmpty()) ? precio.get(0).PrecioCierre : BigDecimal.valueOf(0);                        
                    }
                    recorreDeta.Monto = recorreDeta.Cantidad.multiply(recorreDeta.Precio, MathContext.DECIMAL128);
				}
				c1.MontoOrigen = recorreDeta.Monto;
////			c1.ValorCambioLocalModificado = ord.TipoCambio;
				if(!c1.IdMonedaOrigen.equalsIgnoreCase(monedaSistema)){
					calcularParidadEntrada.p_CalculosEntrada.add(c1);
				}
				
			}
			CalcularParidad calcularParidadService = new CalcularParidad();
			CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
			HashMap<String, Object> hashCalcularParidad = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida,"IdInterno");
				
			inObtenerDatosInstrumentos.p_Instrumentos.addAll(instrumentosEntrada);
			inObtenerDiaSiguiente.p_fechas.addAll(fechasEntrada);
			Set<String> nemos = new HashSet<>();
			inObtenerDatosInstrumentos.p_Instrumentos.removeIf( inst -> !nemos.add(inst.Nemotecnico) );		
			
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(conn, inObtenerDiaSiguiente);
			ConsultarDatosInstrumento_Salida outDatosInstrumentos = obtenerDatosInstrumentos.logica(conn, inObtenerDatosInstrumentos);
			
			//			
			if(inObtenerDatosInstrumentos.p_Instrumentos.size()>0 && outDatosInstrumentos.P_OUT_INSTRUMENTOS.isEmpty()) {				
				String tipoCarga = rsDetalleComun.get(0).TipoCargaMasiva;
				if("RETIRO_DE_GARANTIAS_IF".equals(tipoCarga)) {									
					List <DatosInstrumento> outDatosInstrumentosIF  = this.buscarDatosInstrumentoIF(conn,rsDetalleComun);		
					if(!outDatosInstrumentosIF.isEmpty()) {
						outDatosInstrumentos.P_OUT_INSTRUMENTOS.addAll(outDatosInstrumentosIF);
					}					
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "TIPO_CARGA_CON_ERROR","Carga con error"));
				}
			}			
			
			List<DetalleCuentasCustodio> rsCuentasCustodio = new ArrayList<>();
			
			//quita los blancos y los duplicados
			rCodigoCustodia = rCodigoCustodia.stream().filter(cc -> !cc.equals("")).distinct().collect(Collectors.toList());
			
			if (rCodigoCustodia.size() > 0) {
			regCuentaCustodioPortafolio.put("CodigoCustodia", rCodigoCustodia);
				rsCuentasCustodio = _sql.query(conn, SQL_SELECT_CUENTASCUSTODIO, regCuentaCustodioPortafolio, DetalleCuentasCustodio.class);
			}
			
			Set<String> conceptos = new HashSet<>();
			rConceptoCustodia.removeIf( concepto -> !conceptos.add(concepto));
			regConceptoCustodia.put("IdConcepto", rConceptoCustodia);
			List<DetalleConceptoCustodia> rsConceptoCustodia = _sql.query(conn, SQL_SELECT_CONCEPTOCUSTODIA, regConceptoCustodia, DetalleConceptoCustodia.class);

			Set<String> instrumentosGarantizados = new HashSet<>();
			rInsruGarantizado.removeIf( instg -> !instrumentosGarantizados.add(instg));
			regInsruGarantizado.put("Nemotecnico", rInsruGarantizado);
			List<ConsultaTipOpeInstrumento> rsInsruGarantizado = _sql.query(conn, SQL_SELECT_TIPOOPERACIONTIPOINSTRUMENTO, regInsruGarantizado, ConsultaTipOpeInstrumento.class);
		
			Long Contador		= 0L;
			Long countIngTit	= 0L;
			Long countIngGar	= 0L;
			Long countRetGar	= 0L;
			Long countEveCap	= 0L;
			Long iT				= 0L;
			Long iG				= 0L;
			Long rG				= 0L;
			Long eC				= 0L;
			
			List<ObjInstrumento> detalleInstrumentos = new ArrayList<ObjInstrumento>();
			List<ObjGarantiaCustodia> detalleGarantiaCustodia = new ArrayList<ObjGarantiaCustodia>();
			List<Evento> detalleEventoCapital = new ArrayList<Evento>();

			for (DetalleCarga recDetalle: rsDetalleComun){
				ObjInstrumento registroIn = new ObjInstrumento();
				ObjGarantiaCustodia registroInGar = new ObjGarantiaCustodia();
				Evento regitroEveCap = new Evento();
				
				registroIn.IdGenerarMovimientoCustodia 	= Contador++;
				registroIn.FechaOperacion 				= in.p_FechaProceso;
				registroIn.FechaOperacionOriginal		= in.p_FechaProceso;
				registroIn.FechaVencimiento 			= recDetalle.FechaVencimiento;
				registroIn.FechaVencimientoInstrumento  = recDetalle.FechaVencimiento;
				registroIn.DebeCertificar				= "S";
				
				for (OutObtenerDiaSiguiente.FechasOut recorreFechas : outObtenerDiaSiguiente.p_out_fechas){
					if (recDetalle.PlazoEnDias.equals(recorreFechas.numeroDiasInicial)){
						registroIn.FechaLiquidacion		= recorreFechas.fechaCalculada;
						break;
					}
				}
				
				registroIn.IdConcepto					= recDetalle.IdConcepto;
				registroInGar.IdConcepto				= recDetalle.IdConcepto;
				for (DetalleConceptoCustodia recorreConceptos: rsConceptoCustodia){
					if (recDetalle.IdConcepto.equals(recorreConceptos.IdConcepto)){
						registroInGar.IdContraparteGarantia 	= recorreConceptos.IdContraparte;
						break;
					}
				}
				
				registroIn.IdBolsa						= recDetalle.Bolsa;
				registroInGar.IdBolsa					= recDetalle.Bolsa;
				
				Integer iTipo = (int) (long) recDetalle.IdTipoCargaMasiva;
				switch (iTipo) {
				case 1:
					registroIn.IdTipoOperacion = "INGTIT";
					iT++;
					countIngTit = iT;
					break;
				case 2:
					registroIn.IdTipoOperacion = "INGTIT";
					iT++;
					countIngTit = iT;
					break;
				case 3:
					registroIn.IdTipoOperacion = "INGTIT";
					iT++;
					countIngTit = iT;
					break;
				case 13:
					registroIn.IdTipoOperacion = "INGTIT";
					iT++;
					countIngTit = iT;
					break;
				case 4:
					registroIn.IdTipoOperacion = "RETTIT";
					iT++;
					countIngTit = iT;
					break;	
				case 5:
					registroIn.IdTipoOperacion = "RETTIT";
					iT++;
					countIngTit = iT;
					break;	
				case 6:
					registroIn.IdTipoOperacion = "RETTIT";
					iT++;
					countIngTit = iT;
					break;	
				case 14:
					registroIn.IdTipoOperacion = "RETTIT";
					iT++;
					countIngTit = iT;
					break;					
				case 15:
					registroInGar.IdTipoOperacion = "INGGTA";
					iG++;
					countIngGar = iG;
				case 16:
					registroInGar.IdTipoOperacion = "INGGTA";
					iG++;
					countIngGar = iG;
				case 17:
					registroInGar.IdTipoOperacion = "INGGTA";
					iG++;
					countIngGar = iG;
					
					/* Cuando el parámetro p_UsaCarterasSistema tiene valor "N" significa que no usará
					 * las carteras del sistema, por lo tanto, hay que hacer in ingreso de títulos, para 
					 * posteriormente esa cartera pasarla a Garantías. ESTO SOLO DEBIESE USARSE PARA LA
					 * CARGA INICIAL DEL SISTEMA
					 * */
					if (in.p_UsaCarterasSistema.equals("N")){
						registroIn.IdTipoOperacion = "INGTIT";
						iT++;
						countIngTit = iT;
					}
					break;					
				case 18:
					registroInGar.IdTipoOperacion = "RETGTA";
					rG++;
					countRetGar = rG;
					break;
				case 19:
					registroInGar.IdTipoOperacion = "RETGTA";
					rG++;
					countRetGar = rG;
					break;
				case 20:
					registroInGar.IdTipoOperacion = "RETGTA";
					rG++;
					countRetGar = rG;
					break;
				case 24:
					eC++;
					countEveCap = eC;
					break;
				//case 7 al 11: SIMULTANEAS, Tiene su propio Java CargaSimultaneasDesdeSebra
				//case 21:REPORTES, Tiene su propio Java ProcesaCargaMasivaCustodiaReporte
				default:
					break;
				}
				registroIn.IdCliente					= recDetalle.IdCliente;
				registroIn.IdPortafolio					= recDetalle.IdPortafolio;
				registroInGar.IdCliente					= recDetalle.IdCliente;
				registroInGar.IdPortafolio				= recDetalle.IdPortafolio;
				registroIn.IdBoveda						= recDetalle.IdBoveda;
				registroIn.CuentaBoveda					= recDetalle.CuentaCustodio; //RETIRO_DE_TITULOS_RF
				
				if("RETIRO_DE_TITULOS_RF".equals(recDetalle.TipoCargaMasiva)) {
					registroIn.FolioCartera = recDetalle.FolioCartera;
				}
				if ("DIVIDENDO_EN_CUOTAS_FFMM".equals(recDetalle.TipoCargaMasiva) == false) {
					for (DetalleCuentasCustodio recorreCtaCust : rsCuentasCustodio) {
						if (recDetalle.IdPortafolio.equals(recorreCtaCust.IdPortafolio)) {
							registroIn.IdCuentaCustodioPortafolio = recorreCtaCust.IdCuentaCustodioPortafolio;
							break;
						}
					}
				}
				registroIn.PlazoEnDias					= recDetalle.PlazoEnDias;
				registroInGar.PlazoEnDias				= recDetalle.PlazoEnDias;
				registroIn.IdCondicionLiquidacion		= recDetalle.IdCondicionLiquidacion;
				registroIn.Glosa						= recDetalle.Glosa;
				registroIn.Nemotecnico					= recDetalle.Nemotecnico;
				registroIn.Fungible						= recDetalle.Fungible;
				
				for (DatosInstrumento recorreIns : outDatosInstrumentos.P_OUT_INSTRUMENTOS){
					if (recDetalle.Nemotecnico.equals(recorreIns.Nemotecnico)
							&& ( (!"DIVIDENDO_EN_CUOTAS_FFMM".equals(recDetalle.TipoCargaMasiva) && !"FM".equals(recorreIns.IdClasificacionTipoInstrumento)) 
								|| ("DIVIDENDO_EN_CUOTAS_FFMM".equals(recDetalle.TipoCargaMasiva) && "FM".equals(recorreIns.IdClasificacionTipoInstrumento)) ) ){
						registroIn.IdEmisor							= recorreIns.IdEmisor == "" || recorreIns.IdEmisor == null ? recDetalle.Emisor : recorreIns.IdEmisor;
						registroIn.IdMonedaInstrumento				= "PAGARE R".equalsIgnoreCase(recorreIns.Nemotecnico) && "USD".equalsIgnoreCase(recDetalle.IdTipoReajuste) ? recDetalle.IdTipoReajuste : recorreIns.IdMoneda;
						registroIn.IdTipoReajuste 					= "PAGARE R".equalsIgnoreCase(recorreIns.Nemotecnico != null ? recorreIns.Nemotecnico.trim() : null) ? recDetalle.IdTipoReajuste : recorreIns.IdTipoReajuste;
						registroIn.IdTipoReajuste                   = (registroIn.IdTipoReajuste==null || registroIn.IdTipoReajuste.equalsIgnoreCase("")) ? recDetalle.IdTipoReajuste : registroIn.IdTipoReajuste; 
						registroIn.IdClasificacionTipoInstrumento	= recorreIns.IdClasificacionTipoInstrumento;
						registroIn.IdTipoInstrumento				= recorreIns.IdTipoInstrumento;
						registroIn.IdInstrumento					= recorreIns.IdInstrumento;
						registroIn.CorteMinimo						= recorreIns.CorteMinimo;
						registroIn.IdMonedaEmision 					= recorreIns.IdMonedaEmision;
						registroInGar.IdMonedaEmisionInstrumento	= recorreIns.IdMonedaEmision;
						registroInGar.IdInstrumento					= recorreIns.IdInstrumento;
						registroInGar.IdMoneda						= recorreIns.IdMoneda;
						registroInGar.IdClasificacionTipoInstrumento= recorreIns.IdClasificacionTipoInstrumento;
						registroInGar.IdTipoReajuste				= recorreIns.IdTipoReajuste;
						registroInGar.NemoSVS						= ""; // SOLO PARA IF
						registroInGar.EsFungible					= "N";
						registroInGar.FechaEmisionInstrumento		= recorreIns.FechaEmision.toString();
						registroInGar.FechaVencimientoInstrumento	= recorreIns.FechaVencimiento.toString();
						registroInGar.IdEmisor						= recorreIns.IdEmisor;
						registroInGar.IdTipoInstrumento				= recorreIns.IdTipoInstrumento;
						
						regitroEveCap.p_Nemo						= recorreIns.Nemotecnico;
						regitroEveCap.p_IdInstrumento				= recorreIns.IdInstrumento;
						regitroEveCap.p_IdEmisor					= recorreIns.IdEmisor;
						regitroEveCap.p_IdClasificacionTipoInstrumento = recorreIns.IdClasificacionTipoInstrumento;
						regitroEveCap.p_IdTipoInstrumento 			= recorreIns.IdTipoInstrumento;
									
						recDetalle.IdInstrumento					= recorreIns.IdInstrumento;
						break;
					}
				}
				for(ConsultaTipOpeInstrumento recorreInsGaran: rsInsruGarantizado){
					if (recDetalle.NemoGarantizado.equals(recorreInsGaran.Nemotecnico)){
						registroInGar.IdInstrumentoGarantizado		= recorreInsGaran.IdInstrumento;
						break;
					}
				}
				registroIn.Precio 						= recDetalle.Precio;
				registroInGar.Precio					= recDetalle.Precio;
				registroIn.Cantidad						= recDetalle.Cantidad;
				registroIn.ValorRescate					= recDetalle.ValorRescate;
				registroIn.MontoEnMonedaInstrumento		= recDetalle.Monto;	
				CalculoSalida paridad = (CalculoSalida) hashCalcularParidad.get(recDetalle.IdCargaMasivaDetalle);
				registroIn.Monto = (paridad != null) ? paridad.MontoLocal : recDetalle.Monto;
				registroIn.Tasa							= recDetalle.Tasa;
				registroInGar.Tasa						= recDetalle.Tasa;
//				registroIn.TasaAgente					= recDetalle.TasaAgente;
				
				registroInGar.Cantidad					= recDetalle.Cantidad;
				registroInGar.ValorRescate				= recDetalle.ValorRescate;
				registroInGar.MontoMonedaInstrumento	= recDetalle.Monto;		//	VALIDAR SI ESTA BIEN
				registroInGar.MontoMonedaSistema		= recDetalle.Monto;		//	VALIDAR SI ESTA BIEN
				registroInGar.FolioCarteraRetiroMasivo 	= recDetalle.FolioCartera;
				
				regitroEveCap.p_IdTipoEventoCapital = recDetalle.TipoEvento;
				regitroEveCap.p_NumeroDividendo = recDetalle.NumeroDividendo;
				regitroEveCap.p_FechaLimite = recDetalle.FechaLimite;
				regitroEveCap.p_FechaPago = recDetalle.FechaPago;
				regitroEveCap.p_Descripcion = recDetalle.Descripcion;
				regitroEveCap.p_Monto = recDetalle.Monto;
				regitroEveCap.p_Factor = BigDecimal.valueOf(recDetalle.Factor);
				regitroEveCap.p_Cuotas = recDetalle.Cuotas;
				regitroEveCap.p_EsInscrito = "N";
				regitroEveCap.p_Impuesto = BigDecimal.ZERO;
				regitroEveCap.p_Valor = BigDecimal.ZERO;
				regitroEveCap.p_IdMoneda = "";
				regitroEveCap.p_IdBoveda = "";
				regitroEveCap.p_IdMoneda = "";
				regitroEveCap.p_IdMonedaLiquidacion = "";
				regitroEveCap.p_Paridad = BigDecimal.ONE;
				regitroEveCap.p_IdInstrumentoNuevo = 0L;
				regitroEveCap.p_NemoNuevo = "";
				regitroEveCap.p_FactorNuevo	= BigDecimal.ONE;
				regitroEveCap.p_IdTipoDividendo = "";
				regitroEveCap.p_PrecioAnterior = BigDecimal.ZERO;
				regitroEveCap.p_TipoIngreso = "";
				regitroEveCap.p_IdUsuario = in.p_IdUsuario;
				regitroEveCap.p_IdEstado = "";
				regitroEveCap.p_IdEmisorNuevo = "";
				regitroEveCap.p_EsISFUT = "";
				regitroEveCap.p_TasaEfectiva = BigDecimal.ONE;
				if("DIVIDENDO_EN_CUOTAS_FFMM".equals(recDetalle.TipoCargaMasiva) && recDetalle.IdInstrumento == null){
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "INSTRUMENTO_INACTIVO").add("nemo", recDetalle.Nemotecnico).msg();
					recDetalle.ObservacionValidacion =  descripcion;

					
				}else {
					detalleEventoCapital.add(regitroEveCap);
				}
				detalleInstrumentos.add(registroIn);
				detalleGarantiaCustodia.add(registroInGar);
			}
			
			if (countIngTit.compareTo((long) 0) > 0){ // INGRESOS/RETIRO DE TITULOS
				IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada ingresoRetiroTitulos = new IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada();
				ingresoRetiroTitulos.p_IdEmpresa		= in.p_IdEmpresa;
				ingresoRetiroTitulos.p_IdUsuario		= in.p_IdUsuario;
				ingresoRetiroTitulos.p_IdModulo			= in.p_IdModulo;
				ingresoRetiroTitulos.p_simularOperacion	= "N";
				ingresoRetiroTitulos.p_UsaReglasNegocio	= "S";
				ingresoRetiroTitulos.p_Certifica 		= "N";
				ingresoRetiroTitulos.P_INSTRUMENTOS 	=  new ArrayList<ObjInstrumento>();
				ingresoRetiroTitulos.P_INSTRUMENTOS.addAll(detalleInstrumentos);
				IngresoRetiroTitulosJava ingRetTitulos = new IngresoRetiroTitulosJava();
				IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida resultIngRetTitulos = ingRetTitulos.logica(conn, ingresoRetiroTitulos);
				
				if (!resultIngRetTitulos.P_OUT_RESULTADO.isEmpty()){
					//aqui es necesario mapear el resultado.IdGenerarMovimientoCustodia == recorreErr.Id
					for (ObjResultado res : resultIngRetTitulos.P_OUT_RESULTADO){
						Optional<MensajeSalida> mensaje = resultIngRetTitulos.P_OUT_MENSAJES.stream().filter(msjs -> {
							return msjs.id!=null && msjs.id.equals(res.IdGenerarMovimientoCustodia);
						}).findFirst();
						
						if(mensaje.isPresent() && mensaje.get().tipo.equalsIgnoreCase("ERROR")) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(res.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, mensaje.get().codigo, new HashMap<>() ,conn,in.p_IdEmpresa));
						}else {
							out.P_OUT_MENSAJES.add(new MensajeSalida(res.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_INFORMACION, "UI_MOVIMIENTOS_TITULOS_OK", new HashMap<>() ,conn,in.p_IdEmpresa));
						}
					}
				}
				else if(resultIngRetTitulos.P_OUT_ERRORESCONTADOR.compareTo(0L)> 0){
					for (MensajeSalida recorreErr: resultIngRetTitulos.P_OUT_MENSAJES){
						out.P_OUT_MENSAJES.add(recorreErr);
						out.P_OUT_ERRORESCONTADOR += 1L;
					} 
					return out;
					
				}
					
			} 
			if (countIngGar.compareTo((long) 0) > 0){  // INGRESOS DE GARANTIAS
				List<ObjGarantiaCustodia> detalleGarantiaCustodiaFiltrado = new ArrayList<ObjGarantiaCustodia>();
				detalleGarantiaCustodiaFiltrado = validarCustodiaGarantia(conn, detalleGarantiaCustodia, in, rsDetalleComun);
				
				IngresarGarantiaEntrada IngresoGarantia = new IngresarGarantiaEntrada();
				IngresoGarantia.p_IdEmpresa			= in.p_IdEmpresa;
				IngresoGarantia.p_IdUsuario			= in.p_IdUsuario;
				IngresoGarantia.p_IdModulo			= in.p_IdModulo;
				IngresoGarantia.p_UsaReglasNegocio	= "S";
				IngresoGarantia.p_SimulaOperacion	= "N";
				IngresoGarantia.p_Certifica			= "N";
				IngresoGarantia.p_FechaMovimiento	= in.p_FechaProceso;
				IngresoGarantia.p_GarantiaCustodia.addAll(detalleGarantiaCustodiaFiltrado);
				IngresarGarantiaJava IngGarantia = new IngresarGarantiaJava();
				IngresarGarantiaSalida resultIngGarantia = IngGarantia.logica(conn, IngresoGarantia);
				
				if (!resultIngGarantia.P_OUT_ERRORESCONTADOR.equals(0L)){
					for (MensajeSalida recorreErr: resultIngGarantia.P_OUT_MENSAJES){
						out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_GARANTIA_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;
					}
				} else {
					if (!resultIngGarantia.P_OUT_RESULTADOPROCESO.isEmpty()){
						out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_GARANTIA_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));
						out.P_OUT_ERRORESCONTADOR = 0L;
					}
				}
			} 
			if (countRetGar.compareTo((long) 0) > 0){
				// RETIRO DE GARANTIAS
				
				List<String> Emisor = new ArrayList<String>();
				List<Long> IdCliente = new ArrayList<Long>();
				List<Long> IdPortafolio = new ArrayList<Long>();
				List<Long> IdInstrumento = new ArrayList<Long>();
				List<Long> FolioGarantia = new ArrayList<Long>();
				for(ObjGarantiaCustodia recorreObj : detalleGarantiaCustodia) {
					if(!Emisor.contains(recorreObj.IdEmisor)) Emisor.add(recorreObj.IdEmisor);
					if(!IdCliente.contains(recorreObj.IdCliente)) IdCliente.add(recorreObj.IdCliente);
					if(!IdPortafolio.contains(recorreObj.IdPortafolio))	IdPortafolio.add(recorreObj.IdPortafolio);
					if(!IdInstrumento.contains(recorreObj.IdInstrumento)) IdInstrumento.add(recorreObj.IdInstrumento);
					if(!FolioGarantia.contains(recorreObj.FolioCarteraRetiroMasivo)) FolioGarantia.add(recorreObj.FolioCarteraRetiroMasivo);
					
				}
				
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdsEmisor", Emisor);
				params.put("IdsCliente", IdCliente);
				params.put("IdsPortafolio", IdPortafolio);
				params.put("IdsInstrumento", IdInstrumento);
				params.put("FolioGarantia", FolioGarantia);
				
				List<GarantiasARetirar> garantiasARetirar = _sql.query(conn, SQL_SELECT_GARANTIASARETIRAR, params, GarantiasARetirar.class);

				RetirarGarantiaEntrada entradaRetiroGarantia = new RetirarGarantiaEntrada();
				entradaRetiroGarantia.p_IdEmpresa = in.p_IdEmpresa;
				entradaRetiroGarantia.p_IdUsuario = in.p_IdUsuario;
				entradaRetiroGarantia.p_IdModulo = in.p_IdModulo;
				entradaRetiroGarantia.p_UsaReglasNegocio = "S";
				entradaRetiroGarantia.p_SimulaOperacion = "N";
				entradaRetiroGarantia.p_IdTipoOperacion = "RETGTA";
				entradaRetiroGarantia.p_FechaMovimiento = in.p_FechaProceso;
				String observacionFolioGarantiaNoEncontrado = Diccionario.getMensaje(conn, in.p_IdEmpresa, "FOLIO_GARANTIA_NO_EXISTE", new HashMap<String,String>());
				
				for(ObjGarantiaCustodia recorreObj : detalleGarantiaCustodia) {
					boolean folioGarantiaEncontrado = false;
					for(GarantiasARetirar garantia : garantiasARetirar) {
						if(recorreObj.FolioCarteraRetiroMasivo.compareTo(garantia.FolioGarantia)==0) {
							ObjGarantiasARetirar obj = new ObjGarantiasARetirar();
							obj.IdGarantia = garantia.IdGarantia;
							obj.IdConcepto = recorreObj.IdConcepto;
							obj.Cantidad = recorreObj.Cantidad;
							obj.ValorRescate = recorreObj.Cantidad;
							obj.MontoMonedaInstrumento = (recorreObj.MontoMonedaInstrumento.multiply(recorreObj.Cantidad));
							obj.TasaRetiro = recorreObj.Tasa;
							obj.PrecioRetiro = recorreObj.Precio;
							obj.ConCertificadoAutomatico = "S";
							entradaRetiroGarantia.p_tablaRetiro.add(obj);
							folioGarantiaEncontrado = true;
							break;
						}
					}
					if(!folioGarantiaEncontrado) {
						rsDetalleComun.stream()
								.filter(det -> recorreObj.FolioCarteraRetiroMasivo.compareTo(det.FolioCartera) == 0 && recorreObj.IdPortafolio.compareTo(det.IdPortafolio) == 0)
								.forEach(det -> det.ObservacionValidacion = observacionFolioGarantiaNoEncontrado);
					}
				}
				
				RetirarGarantiaJava retirarGarantia = new RetirarGarantiaJava();
				
				if(!entradaRetiroGarantia.p_tablaRetiro.isEmpty()) {
					RetirarGarantiaSalida resultRetGarantia = retirarGarantia.logica(conn, entradaRetiroGarantia);
					
					if (!resultRetGarantia.P_OUT_RESULTADOPROCESO.isEmpty()){
						if (!resultRetGarantia.P_OUT_ERRORESCONTADOR.equals(0L)){
							for (MensajeSalida recorreErr: resultRetGarantia.P_OUT_MENSAJES){
								out.P_OUT_MENSAJES.add(recorreErr);
								if("ERROR".equalsIgnoreCase(recorreErr.tipo)) out.P_OUT_ERRORESCONTADOR += 1L;
							}
						}
						out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_RETIRO_GARANTIA_MASIVO_TERMINADO", new HashMap<>() ,conn,in.p_IdEmpresa));
					}
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_RETIRO_GARANTIA_MASIVO_TERMINADO", new HashMap<>() ,conn,in.p_IdEmpresa));
				}
				
			}
			
			if (countEveCap.compareTo((long) 0) > 0){
				List<Long> secuenciaEventoCapital = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalSecuencia", Long.valueOf(detalleEventoCapital.size()));
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

				int index = 0;
				for(Evento detalle: detalleEventoCapital) {
					detalle.p_Secuencia = secuenciaEventoCapital.get(index);
					detalle.p_FechaActualizacion = fechaSistema;
					detalle.p_FechaHoraIngresoRegistro = fechaSistema;
					detalle.p_FechaHoraUltimaActualizacion = fechaSistema;
					index++;
				}
		
				CrearEventoCapital crearEventoCapital = new CrearEventoCapital();
				CrearEventoCapital.EventoCapital_IN eventoCapitalIN = new CrearEventoCapital.EventoCapital_IN();
				eventoCapitalIN.p_IdEmpresa = in.p_IdEmpresa;
				eventoCapitalIN.p_IdUsuario = in.p_IdUsuario;
				eventoCapitalIN.p_IdModulo = in.p_IdModulo;
				eventoCapitalIN.p_UsaReglasNegocio = "S";
				eventoCapitalIN.p_EventoCapital = detalleEventoCapital;
				EventoCapital_OUT eventoCapitalOUT = crearEventoCapital.logica(conn, eventoCapitalIN);
				
				if (!eventoCapitalOUT.P_OUT_MENSAJES.isEmpty()){
					
					if (!eventoCapitalOUT.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (MensajeSalida recorreErr: eventoCapitalOUT.P_OUT_MENSAJES){
							out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR = 1L;
							return out;
						}
					}
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_EVENTO_CAPITAL_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));
					
					out.P_OUT_ERRORESCONTADOR = 0L;
				}

			}

			
			if ((countIngTit.compareTo((long) 0) > 0) || (countIngGar.compareTo((long) 0) > 0) || (countRetGar.compareTo((long) 0) > 0) || (countEveCap.compareTo((long) 0) > 0)){
				List<ParametrosActualizacion> paramUpdate = new ArrayList<>();
				ParametrosActualizacion ii = new ParametrosActualizacion();
				ii.IdCargaMasiva 	= in.p_CargaMasivaProcesar;
				ii.Fecha			= in.p_FechaProceso;
				paramUpdate.add(ii);
				_sql.batch(conn, SQL_ACTUALIZA_ESTADO_CARGA_MASIVA, paramUpdate, ParametrosActualizacion.class);
				
				List<ParametrosActualizacionDeta> paramUpdateDet = new ArrayList<>();
				ParametrosActualizacionDeta iii = new ParametrosActualizacionDeta();
				iii.IdCargaMasiva 	= in.p_CargaMasivaProcesar;
				paramUpdateDet.add(iii);
				_sql.batch(conn, SQL_ACTUALIZA_ESTADO_CARGA_MASIVA_DETALLE, paramUpdateDet, ParametrosActualizacionDeta.class);				
			}
			
			Long cantidadDetalleRechazado = rsDetalleComun.stream().filter(det -> det.ObservacionValidacion != null).count();
			List<ParametrosActualizacionDetaRechazo> paramUpdateDetRechazo = new ArrayList<>();
			
			//Si existen porfolios no activos se actualiza la tabla CargaMasivaDetalle
			if(portafoliosNoActivos != null && portafoliosNoActivos.size() > 0) {
				for(DetalleCarga detalleCarga : portafoliosNoActivos) {
					ParametrosActualizacionDetaRechazo idsProcesarUpdate = new ParametrosActualizacionDetaRechazo();
					idsProcesarUpdate.IdCargaMasiva 	= detalleCarga.IdCargaMasiva;
					idsProcesarUpdate.IdCargaMasivaDetalle = detalleCarga.IdCargaMasivaDetalle;
					idsProcesarUpdate.ObservacionValidacion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "OPERACION_TERMINO_PORTAFOLIO_NO_ACTIVO").add("IdCargaMasivaDetalle", detalleCarga.IdCargaMasivaDetalle).msg();
					paramUpdateDetRechazo.add(idsProcesarUpdate);		
				}
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_ADVERTENCIA, "CARGA_MASIVA_CON_ERRORES", new HashMap<>() ,conn,in.p_IdEmpresa));
				out.P_OUT_ADVERTENCIASCONTADOR += 1L;
				
			}
			
			if(cantidadDetalleRechazado > 0) {
				for(DetalleCarga detalleCarga : rsDetalleComun) {
					if(detalleCarga.ObservacionValidacion != null) {
						ParametrosActualizacionDetaRechazo idsProcesarUpdate = new ParametrosActualizacionDetaRechazo();
						idsProcesarUpdate.IdCargaMasiva 	= detalleCarga.IdCargaMasiva;
						idsProcesarUpdate.IdCargaMasivaDetalle = detalleCarga.IdCargaMasivaDetalle; 
						idsProcesarUpdate.ObservacionValidacion = detalleCarga.ObservacionValidacion; 
						paramUpdateDetRechazo.add(idsProcesarUpdate);			
					}	
				}	
			}
			
			if(paramUpdateDetRechazo.size() > 0) {
				_sql.batch(conn, SQL_ACTUALIZA_ESTADO_CARGA_MASIVA_DETALLE_RECHAZADOS, paramUpdateDetRechazo, ParametrosActualizacionDetaRechazo.class);
			}
				
		}
		catch(Exception e) {
			logger.error(e);
			throw e;
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesaCargaMasivaCustodia", finServicio - inicioServicio);
		return out;
	}
	
	private List<DatosInstrumento> buscarDatosInstrumentoIF(Connection conn, List<DetalleCarga> rsDetalleComun) throws SQLException {
		logger.debug("INICIO::ProcesaCargaMasivaCustodia.esValorInvalido");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();	
		
		List<FoliosGarantias> listaFoliosGarantias = rsDetalleComun.stream().map(det ->{
			FoliosGarantias folio = new FoliosGarantias();  
			folio.FolioCartera =det.FolioCartera;					
			return folio;
			}).collect(Collectors.toList());
		
		SQL.exec(conn, SQL_CREATE_TABLA_FOLIOS_GARANTIA_IF);	
		sql.batch(conn, SQL_INSERT_FOLIO_GARANTIA_IF, listaFoliosGarantias, FoliosGarantias.class);	
		
		List<DatosInstrumento> datosInstrumentoIF = sql.query(conn, SQL_SELECT_DATOS_INSTRUMENTOS_IF, new HashMap<String,Object>(), DatosInstrumento.class);
		
		SQL.exec(conn, SQL_DROP_TABLA_FOLIOS_GARANTIA_IF);	
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesaCargaMasivaCustodia.esValorInvalido", fin - inicio);
		
		return datosInstrumentoIF;
	}

	private Boolean esValorInvalido(Long valor) {
		logger.trace("INICIO::ProcesaCargaMasivaCustodia.esValorInvalido");
		long inicio = System.currentTimeMillis();
		Boolean esValorInvalido = valor == null || valor.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesaCargaMasivaCustodia.esValorInvalido", fin - inicio);
		return esValorInvalido;
	}
	
	private Boolean tieneDecimales(List<DetalleCarga> rsDetalleComun) {
		logger.trace("INICIO::ProcesaCargaMasivaCustodia.tieneDecimales");
		long inicio = System.currentTimeMillis();
		
		Boolean decimales=false;
		
		BigDecimal entero= new BigDecimal("0");
		BigDecimal decimal= new BigDecimal("0"); 
		
		for(DetalleCarga detalle:rsDetalleComun) {
			entero= detalle.Cantidad;
			decimal = entero.subtract(entero.divideToIntegralValue(new BigDecimal("1")));
			if( decimal.compareTo(new BigDecimal("0")) != 0 ) {
				decimales = true;
				return decimales;
			}
		} 
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesaCargaMasivaCustodia.tieneDecimales", fin - inicio);
		return decimales;
	}
	
	private List<ObjGarantiaCustodia> validarCustodiaGarantia(Connection conn, List<ObjGarantiaCustodia> detalleGarantiaCustodia, ProcesarCargaMasivaCustodiaEntrada in, List<DetalleCarga> detalleCarga) throws Exception {
		List<ConsultarFlujoCustodia.FlujoUso> flujoUso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();
		List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> flujoFiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		
		for (ObjGarantiaCustodia gar : detalleGarantiaCustodia) {
			ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
			datosFlujoUso.UsoTipoOperacion				= "INGGPR";
			datosFlujoUso.IdCliente						= gar.IdCliente;
			datosFlujoUso.IdPortafolio					= gar.IdPortafolio;
			datosFlujoUso.BusquedaEspecifica			= "SOLO_GARANTIZABLE";
			flujoUso.add(datosFlujoUso);
			
			ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
			datosFlujoFiltrosBusqueda.IdInstrumento						= gar.IdInstrumento;
			flujoFiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
		}
		
		List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> flujoEsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
		ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
		datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
		datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
		datosFlujoEsAgrupadoPor.Instrumento						= "S";
		datosFlujoEsAgrupadoPor.Emisor							= "S";
		datosFlujoEsAgrupadoPor.Nemotecnico						= "S";
		datosFlujoEsAgrupadoPor.NemoSVS							= "S";
		datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "S";
		datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "S";
		datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "S";
		datosFlujoEsAgrupadoPor.TipoReajuste					= "S";
		datosFlujoEsAgrupadoPor.EsFungible						= "S";
		datosFlujoEsAgrupadoPor.Bolsa							= "S";
		flujoEsAgrupadoPor.add(datosFlujoEsAgrupadoPor);
		
		ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
		ConsultarFlujoCustodia.FlujoCustodiaEntrada flujoCustodiaEntrada = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
		flujoCustodiaEntrada.p_IdEmpresa			= in.p_IdEmpresa;
		flujoCustodiaEntrada.p_IdUsuario			= in.p_IdUsuario;
		flujoCustodiaEntrada.p_IdModulo				= in.p_IdModulo;
		flujoCustodiaEntrada.p_FechaSaldo			= fechaSistema;
		flujoCustodiaEntrada.p_IdPais				= idPaisEmpresa;
		flujoCustodiaEntrada.p_DiasProyeccion		= 0;
		flujoCustodiaEntrada.p_DiaHabil				= "S";
		flujoCustodiaEntrada.p_ValorizarFlujo		= "N";
		flujoCustodiaEntrada.p_Uso					= flujoUso;
		flujoCustodiaEntrada.p_FiltrosBusqueda		= flujoFiltrosBusqueda;
		flujoCustodiaEntrada.p_EsAgrupadoPor		= flujoEsAgrupadoPor;

		ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(conn, flujoCustodiaEntrada);
		
		List<ObjGarantiaCustodia> detalleGarantiaFiltrado = new ArrayList<ObjGarantiaCustodia>();
		Set<String> hashDetalleGarantiaSinCustodia = new HashSet<String>();
		for (ObjGarantiaCustodia gar : detalleGarantiaCustodia) {
			for (FlujoCustodia flujo : salidaFlujo.p_out_FlujoTotalizado) {
				if (flujo.IdInstrumento != null && gar.IdInstrumento != null && gar.IdCliente.compareTo(flujo.IdCliente) == 0 && gar.IdPortafolio.compareTo(flujo.IdPortafolio) == 0 &&
						( (gar.IdInstrumento.compareTo(flujo.IdInstrumento) == 0 && !"IF".equalsIgnoreCase(gar.IdClasificacionTipoInstrumento) && gar.Cantidad.compareTo(flujo.Cantidad) <= 0) 
								|| ("IF".equalsIgnoreCase(gar.IdClasificacionTipoInstrumento) && gar.NemoSVS.equals(flujo.NemoSVS) && gar.ValorRescate.compareTo(flujo.ValorRescate) <= 0) )) {
					detalleGarantiaFiltrado.add(gar);
				}
				
				if (flujo.IdInstrumento != null && gar.IdInstrumento != null && gar.IdCliente.compareTo(flujo.IdCliente) == 0 && gar.IdPortafolio.compareTo(flujo.IdPortafolio) == 0 &&
						( (gar.IdInstrumento.compareTo(flujo.IdInstrumento) == 0 && !"IF".equalsIgnoreCase(gar.IdClasificacionTipoInstrumento) && gar.Cantidad.compareTo(flujo.Cantidad) > 0) 
								|| ("IF".equalsIgnoreCase(gar.IdClasificacionTipoInstrumento) && gar.NemoSVS.equals(flujo.NemoSVS) && gar.ValorRescate.compareTo(flujo.ValorRescate) > 0) )) {
					String claveDetalleGarantiaSinCustodia = gar.IdPortafolio + "_" + gar.IdInstrumento + "_" + gar.Cantidad + "_" + gar.ValorRescate;
					hashDetalleGarantiaSinCustodia.add(claveDetalleGarantiaSinCustodia);
				}
			}
		}
		
		String observacionValidacionCustodia = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_SALDO_INSUFICIENTE_FLUJO_CUSTODIA", new HashMap<String,String>());
		String observacionValidacionInstrumento = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_INSTRUMENTO_INACTIVO", new HashMap<String,String>());
		
		for(DetalleCarga det : detalleCarga) {
			String llaveDetalleCarga = det.IdPortafolio + "_" + det.IdInstrumento + "_" + det.Cantidad + "_" + det.ValorRescate;
			if(hashDetalleGarantiaSinCustodia.contains(llaveDetalleCarga))
				det.ObservacionValidacion = observacionValidacionCustodia;
			if(det.IdInstrumento == null)
				det.ObservacionValidacion = observacionValidacionInstrumento;
		}
		
		return detalleGarantiaFiltrado;
	};
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonIF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaProceso\":\"2025-09-17\",\"p_CargaMasivaProcesar\":952,\"p_UsaCarterasSistema\":\"S\"}";
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaProceso\":\"2025-12-15\",\"p_CargaMasivaProcesar\":3083,\"p_UsaCarterasSistema\":\"N\"}";
			ProcesarCargaMasivaCustodiaEntrada Objin = gson.fromJson(jsonRF, ProcesarCargaMasivaCustodiaEntrada.class);
			
			ProcesaCargaMasivaCustodia demo=new ProcesaCargaMasivaCustodia();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ProcesaCargaMasivaCustodiaSalida ObjOut=demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
	    	}
		System.out.println("== END ==");
	}
}