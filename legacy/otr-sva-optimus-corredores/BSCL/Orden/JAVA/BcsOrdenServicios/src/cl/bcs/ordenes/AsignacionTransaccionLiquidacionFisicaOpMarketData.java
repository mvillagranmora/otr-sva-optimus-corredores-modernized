package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenesPostreras.CobroModificados_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Cobro_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.CrearSimulacionAsignacion_Entrada;
import cl.bcs.ordenes.CrearSimulacionAsignacion.CrearSimulacionAsignacion_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Simulacion_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Resultado;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AsignacionTransaccionLiquidacionFisicaOpMarketData implements JavaCall {
	private static final Logger LOGGER = LogManager.getLogger(AsignacionTransaccionLiquidacionFisicaOpMarketData.class);
	private final String SQL_SELECT_CONSULTAR_TRANSACCIONES 		= loadResourceToString("cl/bcs/ordenes/sql/AsignacionTransaccionLiquidacionFisicaOpMarketData_SelectTransacciones.sql");
	private final String SQL_SELECT_PORTAFOLIOS_SORTEADOS_EJERCIDOS = loadResourceToString("cl/bcs/ordenes/sql/AsignacionTransaccionLiquidacionFisicaOpMarketData_SelectPortafoliosSorteadosEjercidos.sql");

	public static class AsignacionTransaccionLiquidacionFisicaEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaAsignacion;
		public List<DatosTransaccion> p_DatosTransaccion;

	}

	public static class DatosTransaccion {
		public String folioTransaccion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AsignacionTransaccionLiquidacionFisicaSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Resultado> P_OUT_RESULTADO;
		public List<Resultado_Salida> P_OUT_RESULTADOASIGNACION;
		public List<MensajeSalida> P_OUT_ENVIOFACTURACION;

		public AsignacionTransaccionLiquidacionFisicaSalida() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado>();
			P_OUT_ENVIOFACTURACION = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOASIGNACION = new ArrayList<Resultado_Salida>();

		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}



	public static class DatosTransaccionSalida {
		public Long IdTransaccion;
		public String FolioTransaccion;
		public BigDecimal CantidadDisponibleTransaccion;
		public BigDecimal MontoTransaccion;
		public BigDecimal ValorRescateTransaccion;
		public Long IdInstrumento;
		public String Instrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosSorteo {
		public String DocumentoIdentidad;
		public Long PortafolioDerivados;
		public String IdTipoOperacion;
		public String Serie;
		public String EsPutEsCall;
		public Long CodigoCorredora;
		public String CodigoClasificacion;
		public String IdTipoInstrumento;
		public String ModalidadLiquidacion;
		public String ModalidadEjercicio;
		public String EstiloOpcion;
		public Date Fecha;
		public Date FechaVencimiento;
		public String Subyacente;
		public BigDecimal Strike;
		public BigDecimal PrecioSubyacente;
		public BigDecimal PerdidaGanancia;
		public BigDecimal SaldoEjercicio;
		public BigDecimal SaldoNeto;
		public BigDecimal SaldoPrima;
		public BigDecimal SaldoSorteo;
		public String IdFondo;
		public String IdFondoCliente;
		public String LadoDiaAnterior;
		public String LadoDia;
		public String LadoNeto;
		public String LadoTr;
		public BigDecimal LotePadron;
		public Long ContratosAbiertosNeto;
		public Long ContratosAbiertosDiaAnterior;
		public Long ContratosAbiertosDia;
		public Long ContratosEjercidos;
		public BigDecimal ContratosSorteados;
		public Long CantidadContratosTr;
		public String Observacion;
		public Long NumeroReferencia;
		public String IdEstado;
		public String Sorteado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Trace
	public AsignacionTransaccionLiquidacionFisicaSalida logica(Connection conn, AsignacionTransaccionLiquidacionFisicaEntrada in) throws Exception {
		AsignacionTransaccionLiquidacionFisicaSalida salidaT = new AsignacionTransaccionLiquidacionFisicaSalida();
		SQL sql = new SQL();
		
		//se deja esta lista al inicio ya que se utiliza los resultados cargados en 2 partes 
		List<Asignacion_Entrada> dataEnvio = new ArrayList<>();
		
		List<String> foliosTransaccion = in.p_DatosTransaccion.stream().map(linea -> linea.folioTransaccion).distinct().collect(Collectors.toList());
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("folio", foliosTransaccion);		
		List<DatosTransaccionSalida> salidaTransaccion = sql.query(conn, SQL_SELECT_CONSULTAR_TRANSACCIONES, params,DatosTransaccionSalida.class);
		
		if (salidaTransaccion.isEmpty()) {

			String codigo = "FOLIO_TRANSACCION_VACIO";
			Long msj = in.p_IdEmpresa;
			String desc = Diccionario.getMensaje(conn, msj, codigo).msg();
			salidaT.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));

			return salidaT;

		}
	
		List<String> series = salidaTransaccion.stream().map(tran -> tran.Instrumento).distinct().collect(Collectors.toList());
		params.clear();
		params.put("series", series);
		params.put("fechaSorteo", in.p_FechaAsignacion);
		List<DatosSorteo> salidaSorteo = sql.query(conn, SQL_SELECT_PORTAFOLIOS_SORTEADOS_EJERCIDOS, params, DatosSorteo.class);
		
		if (salidaSorteo.isEmpty()) {

			String codigo = "SIN_INFORMACION_SORTEO";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salidaT.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			return salidaT;

		}
		
		List<cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada> dataAsignacionTransaccion = new ArrayList<>();
		List<cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada> dataAsignacionPortafolio = new ArrayList<>();
		//OJO validar que la suma de las cantidades de contratos sorteados sea la cantidad de la transaccion. Si no lo es hay que alertar o salir de la ejeucion
		for (DatosTransaccionSalida tran: salidaTransaccion) {
		//salidaTransaccion.stream().forEach(tran -> {
			
			dataAsignacionTransaccion = salidaSorteo.stream().filter(sor -> sor.Serie.equalsIgnoreCase(tran.Instrumento)).collect(Collectors.toList()).parallelStream().map(datSor ->{
				Transaccion_Entrada asigDataTransaccion = new Transaccion_Entrada();
				asigDataTransaccion.IdMovimiento 	= tran.IdTransaccion;
				asigDataTransaccion.IdTransaccion 	= tran.IdTransaccion;
				asigDataTransaccion.Cantidad		= datSor.ContratosSorteados;
				//asigDataTransaccion.Monto 			= tran.MontoTransaccion;
				//asigDataTransaccion.ValorRescate 	= tran.ValorRescateTransaccion;
				return asigDataTransaccion;
			}).collect(Collectors.toList());
			dataAsignacionPortafolio = salidaSorteo.stream().filter(sor -> sor.Serie.equalsIgnoreCase(tran.Instrumento)).collect(Collectors.toList()).parallelStream().map(datSor ->{
				Portafolio_Entrada asigDataPortafolio 	= new Portafolio_Entrada();
				asigDataPortafolio.IdPortafolio = datSor.PortafolioDerivados;
				asigDataPortafolio.Cantidad = BigDecimal.ZERO;
				asigDataPortafolio.Porcentaje = new BigDecimal("100");
				return asigDataPortafolio;
			}).collect(Collectors.toList());
			
		//});
		}
		
		CrearSimulacionAsignacion_Entrada asigIn = new CrearSimulacionAsignacion_Entrada();
		asigIn.p_IdEmpresa = in.p_IdEmpresa;
		asigIn.p_IdUsuario = in.p_IdUsuario;
		asigIn.p_IdModulo = "ORD";
		asigIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		asigIn.p_IdTipoAsignacion = "ASIGNACION_POSTRERA";
		asigIn.p_IdTipo = "NORMAL";
		asigIn.p_Origen = "ASIG";
		asigIn.p_Transacciones = dataAsignacionTransaccion;
		asigIn.p_Carteras = new ArrayList<>();
		asigIn.p_Ordenes = new ArrayList<>();
		asigIn.p_Portafolios = dataAsignacionPortafolio;
		
		CrearSimulacionAsignacion serviceCrearSimulacionAsignacion = new CrearSimulacionAsignacion();
		CrearSimulacionAsignacion_Salida salidaCrearSimulacionAsignacion = serviceCrearSimulacionAsignacion.logica(conn, asigIn);
		List<OrdenPostrera_Entrada> dataOrdenes = salidaCrearSimulacionAsignacion.P_OUT_SIMASIGNACION.parallelStream().map(outAsi ->{
			OrdenPostrera_Entrada ordenes = new OrdenPostrera_Entrada();
			ordenes.IdInterno = outAsi.IdInterno;
			ordenes.FechaRecepcion = outAsi.FechaIngresoRegistro.toString();
			ordenes.Glosa = outAsi.Glosa;
			ordenes.IdBoveda = outAsi.IdBoveda;
			ordenes.IdFormaCustodia = outAsi.IdFormaCustodia;
			ordenes.EsFacturaInmediata = "N";
			ordenes.IdTransaccion = outAsi.IdMovimiento;// 70290437
			ordenes.IdTransaccion = outAsi.IdTransaccion;// "502"
			ordenes.IdPortafolio = outAsi.IdPortafolio;// 1451803
			ordenes.Cantidad = outAsi.CantidadAsignadaSimulacion; // 5000
			ordenes.ValorRescate = outAsi.ValorRescateAsignacion;// 0
			ordenes.FolioCartera = outAsi.FolioCartera;
			ordenes.TasaVentaCorta = outAsi.TasaVentaCorta;
			ordenes.PlazoVentaCorta = outAsi.PlazoVentaCorta;
			ordenes.TipoCambio = outAsi.TipoCambio;
			ordenes.IdMonedaLiquidacionOrden = outAsi.IdMonedaLiquidacionOrden;
			ordenes.PagoCobroCuentaInversion = outAsi.PagoCobroCuentaInversion;
			ordenes.IdPreferenciaPagoPortafolio = outAsi.IdPreferenciaPagoPortafolio;
			ordenes.IdCuentaCustodioPortafolio = outAsi.IdCuentaCustodioPortafolio;
			ordenes.CodigoIFRS = outAsi.CodigoIFRS;
			return ordenes;
		}).collect(Collectors.toList());
		
		List<CobroModificados_Entrada> dataCobros = salidaCrearSimulacionAsignacion.P_OUT_COBROSASIGNACION.parallelStream().map(outCob ->{
			CobroModificados_Entrada cobros = new CobroModificados_Entrada();
			cobros.IdCobro = outCob.IdCobro;
			cobros.IdInterno = outCob.IdInterno;
			return cobros;
		}).collect(Collectors.toList());
			
		CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();

		CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
		entradaCrearOrdenesPostreras.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearOrdenesPostreras.p_IdUsuario = in.p_IdUsuario;
		entradaCrearOrdenesPostreras.p_IdModulo = "ORD";
		entradaCrearOrdenesPostreras.p_CobrosModificados = dataCobros;
		entradaCrearOrdenesPostreras.p_Ordenes = dataOrdenes;

		CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
		salidaT.P_OUT_RESULTADOASIGNACION.addAll(salidaCrearOrdenesPostreras.P_OUT_RESULTADO);
		salidaT.P_OUT_MENSAJES.addAll(salidaCrearOrdenesPostreras.P_OUT_MENSAJES);
		
		dataEnvio = salidaCrearOrdenesPostreras.P_OUT_RESULTADO.parallelStream().map(outPos ->{
			Asignacion_Entrada envio = new Asignacion_Entrada();
			envio.IdAsignacion = outPos.IdAsignacion;
			envio.GeneraComprobante = "N";
			return envio;
		}).collect(Collectors.toList());
		
		if (dataEnvio.isEmpty()) {
			salidaT.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ASIGNACIONES",null,conn,in.p_IdEmpresa));
			return salidaT;
		}else {
			EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();

			EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
			entradaEnviarAsignacionesFacturar.p_IdEmpresa = in.p_IdEmpresa;
			entradaEnviarAsignacionesFacturar.p_IdUsuario = in.p_IdUsuario;
			entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
			entradaEnviarAsignacionesFacturar.p_Asignaciones = dataEnvio;

			EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);			
			salidaT.P_OUT_RESULTADO.addAll(salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO);
			salidaT.P_OUT_MENSAJES.addAll(salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES);
				
		}
		return salidaT;

	}

	@Override
	public JsonObject call(Connection con, JsonObject Jin) throws Exception {
		Gson gson = getGsonBuilder();
		AsignacionTransaccionLiquidacionFisicaEntrada in = gson.fromJson(Jin.toString(), AsignacionTransaccionLiquidacionFisicaEntrada.class);
		
		
		AsignacionTransaccionLiquidacionFisicaSalida salida = logica(con, in);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();

		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

		   String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":50,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaAsignacion\":\"2020-01-01\",\"p_DatosTransaccion\":[{\"folioTransaccion\":\"1050\"},{\"folioTransaccion\":\"1051\"},{\"folioTransaccion\":\"1052\"},{\"folioTransaccion\":\"1007\"}]}";
		   AsignacionTransaccionLiquidacionFisicaEntrada Objin = gson.fromJson(json, AsignacionTransaccionLiquidacionFisicaEntrada.class);
		   AsignacionTransaccionLiquidacionFisicaOpMarketData demo = new AsignacionTransaccionLiquidacionFisicaOpMarketData();
		   System.out.println("ENTRADA:");
		   System.out.println(Objin.toString());

			try {
				
				AsignacionTransaccionLiquidacionFisicaSalida ObjOut = demo.logica(con, Objin);
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());

				con.rollback();
//				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
//				con.commit();
			}

// 			}
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
