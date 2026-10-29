package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad.FlujoPatrimonialYRentabilidad_Entrada;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad.FlujoPatrimonialYRentabilidad_Salida;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal.Operaciones;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCartolaOnline implements JavaCall {

	private BigDecimal totalPatrimonioVMCLP = BigDecimal.ZERO;
	private BigDecimal totalPatrimonioVMUSD = BigDecimal.ZERO;
	private BigDecimal totalPatrimonioPorcCart = BigDecimal.ZERO;
	private BigDecimal totalMonedaPesos = BigDecimal.ZERO;
	private BigDecimal totalMonedaDolar = BigDecimal.ZERO;

	private List<HashMap<String, Object>> composicionCartera = new ArrayList<HashMap<String, Object>>();

	// SQLs
	private final String SQL_LOGO_CORREDORA = "select \"Binario\" from \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen\" WHERE \"NombreImagen\" = 'LOGO_CORREDOR'";

	public static class ClasificacionInstrumentos {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String Mercado;
		public String SubMercado;
		public String ClasificacionActivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MovimientosCajaSal {
		public Date FechaLiquidacion;
		public Long IdCliente;
		public BigDecimal Saldo;
		public Long IdPortafolio;
		public String TipoRegistro;
		public String Moneda;
		public String TipoMovimiento;
		public BigDecimal Ingreso;
		public BigDecimal Egreso;
		public Date FechaOperacion;
		public String NemoTecnico;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class LogoCorredora {
		public byte[] Binario;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarCartolaOnline_Entrada in = gson.fromJson(jin.toString(), ConsultarCartolaOnline_Entrada.class);
		ConsultarCartolaOnline_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ConsultarCartolaOnline_Salida logica(Connection con, ConsultarCartolaOnline_Entrada entrada)
			throws Exception {

		ConsultarCartolaOnline_Salida out = new ConsultarCartolaOnline_Salida();
		SalidaCartola salidaCartolaMovimientos = new SalidaCartola();
		EntradaCartola entradaCartola = new EntradaCartola();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
		Dato dato = new Dato();
		List<Operaciones> movimientosCajaSaldoActivo = new ArrayList<Operaciones>();
		List<Operaciones> movimientosTitulosSaldoActivo = new ArrayList<Operaciones>();
		List<Operaciones> movimientosCajaSaldoPasivo = new ArrayList<Operaciones>();
		List<Operaciones> movimientosTitulosSaldoPasivo = new ArrayList<Operaciones>();
		

		if (entrada.p_Moneda == null || entrada.p_Moneda.equals("false")) {
			entrada.p_Moneda = "CLP";
		}
		
		// Datos iniciales para obtener informacion de la cartola
		entradaCartola.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCartola.p_IdModulo = entrada.p_IdModulo;
		entradaCartola.p_IdUsuario = entrada.p_IdUsuario;
		entradaCartola.p_Moneda = entrada.p_Moneda;
		entradaCartola.p_FechaDesde = Fecha.toDate("0001-01-01");
		entradaCartola.p_FechaHasta = Fecha.toDate(entrada.p_FechaHasta);

		LocalDate fechaHasta = LocalDate.parse(entradaCartola.p_FechaHasta.toString());
		LocalDate fechaDesde = LocalDate.of(fechaHasta.getYear(), fechaHasta.getMonth(), 1);

		for (DatosCliente cliente : entrada.p_Datos) {
			dato = new Dato();
			dato.IdCliente = cliente.IdCliente;
			dato.IdPortafolio = cliente.IdPortafolio;
			entradaCartola.p_Datos.add(dato);
		}

		List<HashMap<String, Object>> datosClientesConsultados = getDatosSocioNegocio(con, entradaCartola);

		MovimientosTitulos movTitulos = new MovimientosTitulos();
		salidaCartolaMovimientos = movTitulos.procesar(con, entradaCartola);
		List<HashMap<String, Object>> movimientos = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> movimientosTitulosProcesar = new ArrayList<HashMap<String, Object>>();
		
		List<HashMap<String, Object>> movimientosTitulosFlujoPatrimonial = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> movimientosCajaFlujoPatrimonial = new ArrayList<HashMap<String, Object>>();
		
		if (salidaCartolaMovimientos != null && !salidaCartolaMovimientos.P_OUT_SALIDA.isEmpty()) {
			movimientos = (List<HashMap<String, Object>>) salidaCartolaMovimientos.P_OUT_SALIDA
					.get("movimientosTitulos");
		}
		for (HashMap<String, Object> mov : movimientos) {
			LocalDate fechaOperacion = LocalDate.parse(mov.get("FechaOperacion").toString(), formatter);
			// separa los movimientos para la cartola
			if ((!mov.get("FechaOperacion").toString().isEmpty())){
				//Se consideran las operaciones para los saldos iniciales
				Operaciones operacion = new Operaciones();
				operacion.IdCliente =Long.parseLong(mov.get("IdCliente").toString());
				operacion.FechaOperacion = new SimpleDateFormat("dd-MM-yyyy").parse((mov.get("FechaOperacion").toString()));
				operacion.Monto = new BigDecimal(mov.get("MontoMercadoMonedaLocal").toString());
				

				// Para Flujo Patrimonial SOLO debe considerar Ingresos y Retiros de titulos que no provengan de compras ni ventas(facturacion)
				if (mov.get("IdTipoOperacion").toString().equalsIgnoreCase("INGTIT") || mov.get("IdTipoOperacion").toString().equalsIgnoreCase("RETTIT"))
				{
					movimientosTitulosFlujoPatrimonial.add(mov);
						
					if (mov.get("Accion").toString().equalsIgnoreCase("Ingreso"))
						movimientosTitulosSaldoActivo.add(operacion);
					else
						movimientosTitulosSaldoPasivo.add(operacion);
				}
					

				
			
				if( !mov.get("FechaOperacion").toString().equals("Saldo Anterior")) {
					
					if ((fechaOperacion.isAfter(fechaDesde) || fechaOperacion.equals(fechaDesde))
							&& (fechaOperacion.isBefore(fechaHasta) || fechaOperacion.equals(fechaHasta))) {
						movimientosTitulosProcesar.add(mov);
					}
				}
				
			}
			
		}
		List<HashMap<String, Object>> movimientosTitulos = procesaMovimientosTitulos(movimientosTitulosProcesar,
				datosClientesConsultados);
		// Movimientos Caja Cartola
		List<HashMap<String, Object>> movimientosCaja = new ArrayList<HashMap<String, Object>>();
		// Movimientos Caja Flujo
		List<HashMap<String, Object>> movimientosCajaFlujo = getMovimientosCaja(con, entradaCartola,
				datosClientesConsultados);

		for (HashMap<String, Object> mov : movimientosCajaFlujo) {
			if("tituloCliente".equals(mov.get("tipoRegistro"))) {
				movimientosCaja.add(mov);
			}
			// separa los movimientos para la cartola
			if ((!mov.get("fechaOperacion").toString().isEmpty())) {
				
				// Para Flujo Patrimonial SOLO debe considerar mov de caja que no provengan de compras ni ventas de facturacion
				if ((!mov.get("IdModulo").toString().equalsIgnoreCase("FAC")) && (mov.get("IdComprobante").toString().equals("0") || mov.get("IdComprobante").toString().equals("")) )
				{					
					movimientosCajaFlujoPatrimonial.add(mov);
					
					//Se consideran las operaciones para los saldos iniciales
					if(!mov.get("idCliente").toString().isEmpty()) {
						Operaciones operacion = new Operaciones();
						operacion.IdCliente =Long.parseLong(mov.get("idCliente").toString());
						operacion.FechaOperacion = new SimpleDateFormat("dd-MM-yyyy").parse((mov.get("fechaOperacion").toString()));
						
						if(new BigDecimal(mov.get("Ingreso").toString()).compareTo(BigDecimal.ZERO)==1) {
							operacion.Monto = new BigDecimal(mov.get("Ingreso").toString());
							movimientosCajaSaldoActivo.add(operacion);
						}else {
							operacion.Monto = new BigDecimal(mov.get("Egreso").toString());
							movimientosCajaSaldoPasivo.add(operacion);
						}
					}
				}
				
				

				
				if(!mov.get("fechaOperacion").toString().equals("Saldo Anterior")) {
					LocalDate fechaOperacion = LocalDate.parse(mov.get("fechaOperacion").toString(), formatter);
					
					if ((fechaOperacion.isAfter(fechaDesde) || fechaOperacion.equals(fechaDesde))
							&& (fechaOperacion.isBefore(fechaHasta) || fechaOperacion.equals(fechaHasta))) {
						movimientosCaja.add(mov);
					}
				}
			}
				
		}

		BigDecimal patrimonioFinal = BigDecimal.ZERO;
		if (entrada.esPauta) {
			out.p_Datos = entrada.p_Datos;
			out.P_OUT_SALIDA.put("carteraInversiones", getCarteraInversion(con, entrada));
			
			if(entrada.p_Moneda.equalsIgnoreCase("CLP")) {
				patrimonioFinal = totalPatrimonioVMCLP.setScale(2,BigDecimal.ROUND_UP);
			}else {
				patrimonioFinal = totalPatrimonioVMUSD.setScale(2,BigDecimal.ROUND_UP);
			}
			
			out.P_OUT_SALIDA.put("flujoPatrimonial", getFlujoPatrimonial(con, entrada, movimientosTitulosFlujoPatrimonial, movimientosCajaFlujoPatrimonial,movimientosCajaSaldoActivo,movimientosTitulosSaldoActivo, movimientosCajaSaldoPasivo, movimientosTitulosSaldoPasivo, patrimonioFinal));
			
		} else {
			out.p_Datos = entrada.p_Datos;
			out.P_OUT_SALIDA.put("datosSocioNegocio", datosClientesConsultados);
			out.P_OUT_SALIDA.put("movimientosTitulos", movimientosTitulos);
			out.P_OUT_SALIDA.put("movimientosCaja", movimientosCaja);
			out.P_OUT_SALIDA.put("carteraInversiones", getCarteraInversion(con, entrada));
			out.P_OUT_SALIDA.put("composicionCartera", getComposicionCartera());
			
			if(entrada.p_Moneda.equalsIgnoreCase("CLP")) {
				patrimonioFinal = totalPatrimonioVMCLP.setScale(2,BigDecimal.ROUND_UP);
			}else {
				patrimonioFinal = totalPatrimonioVMUSD.setScale(2,BigDecimal.ROUND_UP);
			}
			
			out.P_OUT_SALIDA.put("flujoPatrimonial", getFlujoPatrimonial(con, entrada, movimientosTitulosFlujoPatrimonial, movimientosCajaFlujoPatrimonial,movimientosCajaSaldoActivo,movimientosTitulosSaldoActivo, movimientosCajaSaldoPasivo, movimientosTitulosSaldoPasivo, patrimonioFinal));
			
			
			out.P_OUT_SALIDA.put("distribucionPorClaseActivo", getDistribucionPorClaseDeActivos(composicionCartera));
			out.P_OUT_SALIDA.put("exposicionPorMoneda", getExposicionPorMoneda());
			out.P_OUT_SALIDA.put("datosConfCorredora", getConfCorredora(con));
		}

		out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CONSULTA_OK", "Ok"));
		out.P_OUT_ERRORESCONTADOR = 0L;

		//System.out.println(out);
		return out;
	};

	// Definicion de entrada y salida
	public static class ConsultarCartolaOnline_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_FiltroSoloMonedaSistema;
		public String p_FiltroUbicacionBoveda;
		public String p_Moneda;

		public List<DatosCliente> p_Datos;
		public Boolean esPauta = false;

		public ConsultarCartolaOnline_Entrada() {
			this.p_FiltroSoloMonedaSistema = "N";
			this.p_FiltroUbicacionBoveda = "";
			this.p_Datos = new ArrayList<DatosCliente>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosCliente {
		public Long IdCliente;
		public Long IdPortafolio;

		public String toString() {
			return Utils.printJson(this, false);
		}

		public Long getIdCliente() {
			return IdCliente;
		}

		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}

		public Long getIdPortafolio() {
			return IdPortafolio;
		}

		public void setIdPortafolio(Long idPortafolio) {
			IdPortafolio = idPortafolio;
		}

	}

	public static class ConsultarCartolaOnline_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public HashMap<String, List<HashMap<String, Object>>> P_OUT_SALIDA = new HashMap<>();
		public List<DatosCliente> p_Datos;

		public ConsultarCartolaOnline_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	/* extraccion de datos de movimientosTitulos */

	public List<HashMap<String, Object>> getConfCorredora(Connection con) throws SQLException {
		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		SQL sql = new SQL();
		List<LogoCorredora> logoCorredora = sql.query(con, SQL_LOGO_CORREDORA, new HashMap<String, Object>(),
				LogoCorredora.class);

		HashMap<String, Object> datosConfCorredora = new HashMap<>();
		if (!logoCorredora.isEmpty()) {
			String logo = Base64.getEncoder().encodeToString(logoCorredora.get(0).Binario);
			datosConfCorredora.put("logoCorredora", logo);
		}
		result.add(datosConfCorredora);
		return result;
	}

	public List<HashMap<String, Object>> getMovimientosTitulos(List<HashMap<String, Object>> datosClientesConsultados,
			List<HashMap<String, Object>> movimientos) throws Exception {
		movimientos = procesaMovimientosTitulos(movimientos, datosClientesConsultados);
		return movimientos;
	}

	public List<HashMap<String, Object>> procesaMovimientosTitulos(List<HashMap<String, Object>> movimientosTitulos,
			List<HashMap<String, Object>> datosClientesConsultados) {
		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		boolean cambioCliente = false;
		int cntRegistros = 0;
		HashMap<String, Object> item2 = null;

		if (!movimientosTitulos.isEmpty()) {
			movimientosTitulos = ordenaCarteraPorClasificacion(movimientosTitulos, "IdPortafolio");

			for (HashMap<String, Object> item : movimientosTitulos) {
				HashMap<String, Object> registro = new HashMap<>();
				HashMap<String, Object> registrolleno = new HashMap<>();
				registro = new HashMap<>();
				if (cntRegistros == 0 || cambioCliente) {
					Long idPortafolio = (Long) item.get("IdPortafolio");
					String textoCliente = getTextoCabeceralinea(idPortafolio, datosClientesConsultados);

					registro.put("fechaOperacion", "");
					registro.put("fechaLiquidacion", ""); // Falta fecha liquidacion
					registro.put("moneda", "");
					registro.put("tipoMovimiento", "");
					registro.put("claseActivo", "");
					registro.put("nemoTecnico", textoCliente);
					registro.put("unidades", "");
					registro.put("precio", "");
					registro.put("gastos", ""); // Falta gastos
					registro.put("montoOperacion", "");
					registro.put("idPortafolio", "");
					registro.put("idCliente", "");
					registro.put("tipoRegistro", "tituloCliente");
					registro.put("TipoConcepto", (String) item.get(""));
					registro.put("idTipoInstrumento", "");
					result.add(registro);
					cambioCliente = false;
				}
				registrolleno.put("fechaOperacion", (String) item.get("FechaOperacion"));
				registrolleno.put("fechaLiquidacion", (String) item.get("FechaOperacion")); // Falta fecha liquidacion
				registrolleno.put("moneda", (String) item.get("IdMonedaInstrumento"));
				registrolleno.put("tipoMovimiento", (String) item.get("Accion"));
				registrolleno.put("claseActivo", (String) item.get("NombreTipoOperacion"));
				registrolleno.put("nemoTecnico", (String) item.get("Nemotecnico"));
				registrolleno.put("unidades", (BigDecimal) item.get("Cantidad"));
				registrolleno.put("precio", (BigDecimal) item.get("PrecioCompra"));
				registrolleno.put("gastos", "0"); // Falta gastos
				registrolleno.put("montoOperacion", (BigDecimal) item.get("MontoCompraMonedaInstrumento"));
				registrolleno.put("idPortafolio", (Long) item.get("IdPortafolio"));
				registrolleno.put("idCliente", (Long) item.get("IdCliente"));
				registrolleno.put("tipoRegistro", "detalle");
				registrolleno.put("TipoConcepto", (String) item.get("TipoConcepto"));
				registrolleno.put("idTipoInstrumento", (String) item.get("IdTipoInstrumento"));
				result.add(registrolleno);

				if ((cntRegistros + 1) < movimientosTitulos.size()) {
					item2 = movimientosTitulos.get(cntRegistros + 1); // Siguiente registro es otro portafolio
					if (!((Long) item2.get("IdPortafolio")).equals((Long) item.get("IdPortafolio")))
						cambioCliente = true;
				}

				cntRegistros++;
			}
		}

		return result;

	}

	/* Extraccion Datos SocioNegocio */

	public List<HashMap<String, Object>> getDatosSocioNegocio(Connection con, EntradaCartola entradaCartola)
			throws Exception {

		List<HashMap<String, Object>> dusuario = new ArrayList<HashMap<String, Object>>();
		SalidaCartola salidaCartola = new SalidaCartola();
		DatosCartolaSocioNegocio dSocio = new DatosCartolaSocioNegocio();

		salidaCartola = dSocio.procesar(con, entradaCartola);
		if (salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			dusuario = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("datosSocioNegocio");
		}

		//System.out.println(dusuario);

		return dusuario;
	}

	/* extraccion de datos de movimientosCaja */

	public List<HashMap<String, Object>> getMovimientosCaja(Connection con, EntradaCartola entradaCartola,
			List<HashMap<String, Object>> datosClientesConsultados) throws Exception {
		List<HashMap<String, Object>> movimientos = new ArrayList<HashMap<String, Object>>();
		SalidaCartola salidaCartola = new SalidaCartola();
		MovimientosCaja movCaja = new MovimientosCaja();

		salidaCartola = movCaja.procesar(con, entradaCartola);
		if (salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			movimientos = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("movimientosCaja");
			movimientos = procesaMovimientosCaja(movimientos, datosClientesConsultados);
		}
		return movimientos;
	}

	public String getTextoCabeceralinea(Long idPortafolio, List<HashMap<String, Object>> datosClientesConsultados) {
		String texto = "";
		for (HashMap<String, Object> item : datosClientesConsultados) {
			Long idPortafolioCliente = (Long) item.get("IdPortafolio");
			if (idPortafolioCliente.equals(idPortafolio)) {
				texto = item.get("NombreSocioNegocio") + " - portafolio " + item.get("CodigoPortafolio");
				break;
			}
		}
		return texto;
	}

	public List<HashMap<String, Object>> procesaMovimientosCaja(List<HashMap<String, Object>> movimientosCaja,
			List<HashMap<String, Object>> datosClientesConsultados) {
		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		boolean cambioCliente = false;
		if (movimientosCaja != null) {
			int cntRegistros = 0;
			BigDecimal saldoFinal = BigDecimal.ZERO;
			BigDecimal saldoFinalAux = BigDecimal.ZERO;
			HashMap<String, Object> registro = null;
			HashMap<String, Object> item2 = null;
			for (HashMap<String, Object> item : movimientosCaja) {

				Long idPortafolio = (Long) item.get("IdPortafolio");

				if (cntRegistros == 0 || cambioCliente) {
					String textoCliente = getTextoCabeceralinea(idPortafolio, datosClientesConsultados);
					// Primera linea cliente-portafolio
					registro = new HashMap<>();
					registro.put("fechaOperacion", "");
					registro.put("fechaLiquidacion", "");
					registro.put("moneda", "");
					registro.put("tipoMovimiento", "");
					registro.put("nemoTecnico", textoCliente);
					registro.put("Ingreso", "");
					registro.put("Egreso", "");
					registro.put("Saldo", "");
					registro.put("idCliente", "");
					registro.put("idPortafolio", "");
					registro.put("tipoRegistro", "tituloCliente");
					registro.put("TipoConcepto", "");
					registro.put("AfectaRentabilidad", "");
					registro.put("Accion","");
					
					registro.put("AccionTipoOperacion", "");
					registro.put("IdModulo", "");
					registro.put("IdComprobante", "");					
									
					result.add(registro);
					cambioCliente = false;
				}
				// Solo para la fila inicial de Saldo Inicial del cliente-portafolio
				if (!item.get("SaldoInicial").equals(null)) {
					registro = new HashMap<>();
					registro.put("fechaOperacion", "Saldo Anterior");
					registro.put("fechaLiquidacion", "");
					registro.put("moneda", "CLP");
					registro.put("tipoMovimiento", "");
					registro.put("nemoTecnico", "");
					registro.put("Ingreso", "");
					registro.put("Egreso", "");
					registro.put("Saldo", (BigDecimal) item.get("SaldoInicial"));
					registro.put("idCliente", "");
					registro.put("idPortafolio", "");
					registro.put("tipoRegistro", "detalle");
					registro.put("TipoConcepto", "");
					registro.put("AfectaRentabilidad", "");
					registro.put("Accion","");
					
					registro.put("AccionTipoOperacion", "");
					registro.put("IdModulo", "");
					registro.put("IdComprobante", "");
					
					result.add(registro);

					saldoFinal = (BigDecimal) item.get("SaldoInicial");
					cambioCliente = false;
				}

				registro = new HashMap<>();
				registro.put("fechaOperacion", (String) item.get("FechaOperacion")); // cambiado de ->FechaIngreso
				registro.put("fechaLiquidacion", (String) item.get("FechaLiquidacion"));
				registro.put("moneda", (String) item.get("Moneda"));
				registro.put("tipoMovimiento", (String) item.get("NombreConcepto"));
				registro.put("nemoTecnico", (String) item.get("Nemotecnico"));
				registro.put("Ingreso", (BigDecimal) item.get("Abono"));
				registro.put("Egreso", (BigDecimal) item.get("Cargo"));
				registro.put("IdCargoAbonoMovimiento", item.get("IdCargoAbonoMovimiento"));
				BigDecimal ingreso = (BigDecimal) item.get("Abono");
				BigDecimal egreso = (BigDecimal) item.get("Cargo");

				if (ingreso == null) {
					ingreso = BigDecimal.ZERO;
				}
				if (egreso == null) {
					egreso = BigDecimal.ZERO;
				}

				if (saldoFinalAux.compareTo(BigDecimal.ZERO) != 0) {
					saldoFinal = saldoFinalAux;
				}
					 
				if ((ingreso).compareTo(BigDecimal.ZERO) == 1) {
					saldoFinal = saldoFinal.add(ingreso);
				} else {
					saldoFinal = saldoFinal.subtract(egreso);
				}

				saldoFinalAux = saldoFinal;
				registro.put("Saldo", saldoFinal);
				registro.put("idCliente", (Long) item.get("IdCliente"));
				registro.put("idPortafolio", (Long) item.get("IdPortafolio"));
				registro.put("tipoRegistro", "detalle");
				registro.put("TipoConcepto", item.get("TipoConcepto"));
				registro.put("AfectaRentabilidad", item.get("AfectaRentabilidad"));
				
				registro.put("AccionTipoOperacion", item.get("AccionTipoOperacion"));
				registro.put("IdModulo", item.get("IdModulo"));
				registro.put("IdComprobante", item.get("IdComprobante"));

				
	
				
				result.add(registro);

				if ((cntRegistros + 1) < movimientosCaja.size()) {
					item2 = movimientosCaja.get(cntRegistros + 1); // Siguiente registro es otro cliente
					if (!((Long) item2.get("IdPortafolio")).equals((Long) item.get("IdPortafolio")))
						cambioCliente = true;
				}
				cntRegistros++;
			}
		}

		return result;

	}

	/* Procesamiento de Cartera de inversiones */

	public List<HashMap<String, Object>> getCarteraInversion(Connection con, ConsultarCartolaOnline_Entrada entrada)
			throws Exception {
		List<HashMap<String, Object>> carteraInversion = new ArrayList<HashMap<String, Object>>();
		ConsultarCartolaOnline_Salida carteraInversionSalida = new ConsultarCartolaOnline_Salida();

		ConsultarCarteraInversionCartolaConsolidada carteraConsolidada = new ConsultarCarteraInversionCartolaConsolidada();

		carteraInversionSalida = carteraConsolidada.logica(con, entrada);
		carteraInversion = carteraInversionSalida.P_OUT_SALIDA.get("carteraInversiones");

		carteraInversion = preparaSalidaCartola(carteraInversion);

		return carteraInversion;
	}

	/* Procesamiento Composicion de la Cartera */

	public List<HashMap<String, Object>> getComposicionCartera() throws Exception {

		List<HashMap<String, Object>> composicion = new ArrayList<HashMap<String, Object>>();

		composicionCartera = ordenaCarteraPorClasificacion(composicionCartera, "mercado");
		//composicionCartera = ordenaCarteraPorClasificacion(composicionCartera, "titulo");
		//composicionCartera = ordenaCarteraPorClasificacion(composicionCartera, "subMercado");

		// incorporamos al final el total Patrimonio
		HashMap<String, Object> registro = crearRegistroComposicionCartera("", "", totalPatrimonioVMCLP,
				totalPatrimonioPorcCart, "totalPatrimonio", "Total Patrimonio", 0);
		composicionCartera.add(registro);

		// Ajustamos los montos de los totales de % Cartera

		// Preparamos la salida
		if (composicionCartera != null) {

			int pos = 0;
			HashMap<String, Object> item2 = null;
			String mercado2 = "";
			for (HashMap<String, Object> item : composicionCartera) {

				String mercado = item.get("mercado").toString();

				if ((pos + 1) < composicionCartera.size())
					item2 = composicionCartera.get(pos + 1); // Siguiente registro

				if (item2 != null)
					mercado2 = item2.get("mercado").toString();

				if (pos == 0) {
					registro = crearRegistroComposicionCartera("", "", BigDecimal.ZERO, BigDecimal.ZERO, "blancoN", "",
							-1);
					composicion.add(registro);

					registro = crearRegistroComposicionCartera("", "", BigDecimal.ZERO, BigDecimal.ZERO, "blanco", "",
							-1);
					composicion.add(registro);

					composicion.add(item);
				} else {
					composicion.add(item);

					if (!mercado.equals(mercado2)) {
						registro = crearRegistroComposicionCartera("", "", BigDecimal.ZERO, BigDecimal.ZERO, "blanco",
								"", -1);
						composicion.add(registro);
					}

				}
				pos++;
			}
		}

		composicionCartera = composicion;
		return composicion;
	}

	/* Flujo Patrimonial */

	public List<HashMap<String, Object>> getFlujoPatrimonial(Connection con,
			ConsultarCartolaOnline_Entrada entradaCartola, List<HashMap<String, Object>> movimientosTitulos,
			List<HashMap<String, Object>> movimientosCaja, List<Operaciones> movimientosCajaSaldoActivo,
	List<Operaciones> movimientosTitulosSaldoActivo, List<Operaciones> movimientosCajaSaldoPasivo, 	List<Operaciones> movimientosTitulosSaldoPasivo, BigDecimal totalPatrimonio) throws Exception {

		FlujoPatrimonialYRentabilidad_Entrada entrada = new FlujoPatrimonialYRentabilidad_Entrada();
		entrada.p_FechaDesde = entradaCartola.p_FechaDesde;
		entrada.p_FechaHasta = entradaCartola.p_FechaHasta;
		entrada.p_IdEmpresa = entradaCartola.p_IdEmpresa;
		entrada.p_Datos = entradaCartola.p_Datos;
		entrada.p_IdModulo = entradaCartola.p_IdModulo;
		entrada.p_IdUsuario = entradaCartola.p_IdUsuario;
		entrada.p_MovimientosCaja = movimientosCaja;
		entrada.p_MovimientosTitulos = movimientosTitulos;
		entrada.movimientosCajaActivo = movimientosCajaSaldoActivo;
		entrada.movimientosCajaPasivo = movimientosCajaSaldoPasivo;
		entrada.movimientosTitulosActivo = movimientosTitulosSaldoActivo;
		entrada.movimientosTitulosPasivo = movimientosTitulosSaldoPasivo;
		entrada.totalPatrimonio = totalPatrimonio;
		entrada.esPauta = entradaCartola.esPauta;

		FlujoPatrimonialYRentabilidad_Salida salidaFlujo = new FlujoPatrimonialYRentabilidad_Salida();
		FlujoPatrimonialYRentabilidad flujoPatrimonial = new FlujoPatrimonialYRentabilidad();
		salidaFlujo = flujoPatrimonial.logica(con, entrada);
		List<HashMap<String, Object>> flujoPatrimonialRentabilidad = new ArrayList<HashMap<String, Object>>();

		if (salidaFlujo != null && !salidaFlujo.P_OUT_SALIDA.isEmpty()) {
			flujoPatrimonialRentabilidad = (List<HashMap<String, Object>>) salidaFlujo.P_OUT_SALIDA.get("FlujoPatrimonial");
			if (entradaCartola.esPauta.equals(false)) {
				flujoPatrimonialRentabilidad = procesaFlujoPatrimonial(flujoPatrimonialRentabilidad,con,entradaCartola);
			}
		}
		return flujoPatrimonialRentabilidad;
	}

	/* Distribucion por clase de activos */

	public List<HashMap<String, Object>> getDistribucionPorClaseDeActivos(
			List<HashMap<String, Object>> composicionCartera) {
		List<HashMap<String, Object>> distribucionPorClaseDeActivo = new ArrayList<HashMap<String, Object>>();

		if (composicionCartera != null) {
			for (HashMap<String, Object> item : composicionCartera) {
				String tipoRegistro = item.get("tipoRegistro").toString();
				String mercado = item.get("mercado").toString();

				// Llenamos la informacion para la distribucion por clase de activo
				if (tipoRegistro.equalsIgnoreCase("totalMercado")) {
					String porcentaje = item.get("porcentajeTotal").toString();
					porcentaje = porcentaje.replaceAll("%", "");
					HashMap<String, Object> registro = new HashMap<>();
					registro.put("titulo", mercado);
					registro.put("porcentaje", porcentaje);
					distribucionPorClaseDeActivo.add(registro);
				}
			}
		}

		return distribucionPorClaseDeActivo;
	}

	/* Exposicion por moneda */

	public List<HashMap<String, Object>> getExposicionPorMoneda() {
		List<HashMap<String, Object>> exposicionPorMoneda = new ArrayList<HashMap<String, Object>>();

		BigDecimal exposicionMonedaPesos = obtienePorcenaje(totalMonedaPesos);
		HashMap<String, Object> registro = new HashMap<>();
		registro.put("moneda", "CLP");
		registro.put("porcentaje", exposicionMonedaPesos);
		exposicionPorMoneda.add(registro);

		BigDecimal exposicionMonedaDolar = obtienePorcenaje(totalMonedaDolar);
		registro = new HashMap<>();
		registro.put("moneda", "USD");
		registro.put("porcentaje", exposicionMonedaDolar);
		exposicionPorMoneda.add(registro);

		return exposicionPorMoneda;
	}

	public BigDecimal obtienePorcenaje(BigDecimal totalMoneda) {
		BigDecimal porcentaje = BigDecimal.ZERO;

		porcentaje = totalMoneda.multiply(new BigDecimal(100));

		if ((porcentaje).compareTo(BigDecimal.ZERO) != 0 || (totalPatrimonioVMCLP).compareTo(BigDecimal.ZERO) != 0)
			porcentaje = porcentaje.divide(totalPatrimonioVMCLP, 2, BigDecimal.ROUND_HALF_EVEN);

		return porcentaje;
	}

	public List<HashMap<String, Object>> procesaFlujoPatrimonial(List<HashMap<String, Object>> flujoPatrimonial, Connection con,ConsultarCartolaOnline_Entrada entradaCartola) throws Exception {

		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> registro = crearRegistroFlujoPatrimonial("", BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, "", "blancoN");
		result.add(registro);

		registro = crearRegistroFlujoPatrimonial("", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", "blanco");
		result.add(registro);

		if (flujoPatrimonial != null) {
			for (HashMap<String, Object> item : flujoPatrimonial) {
				
				registro = crearRegistroFlujoPatrimonial((String)
						 item.get("Titulo"),
						(BigDecimal) TransformadorMonedas(con,entradaCartola.p_FechaHasta,"CLP",entradaCartola.p_Moneda,(BigDecimal)item.get("MTD")),
						(BigDecimal) TransformadorMonedas(con,entradaCartola.p_FechaHasta,"CLP",entradaCartola.p_Moneda,(BigDecimal)item.get("YTD")),
						(BigDecimal) TransformadorMonedas(con,entradaCartola.p_FechaHasta,"CLP",entradaCartola.p_Moneda,(BigDecimal)item.get("ITD")),
						(String) item.get("Fecha"),
						"detalle");
				result.add(registro);
			}
		}
		return result;

	}

	public HashMap<String, Object> crearRegistroComposicionCartera(String mercado, String subMercado, BigDecimal total,
			BigDecimal porcentaje, String tipo, String titulo, int orden) {
		HashMap<String, Object> registro = new HashMap<>();
		registro.put("titulo", titulo);
		registro.put("mercado", mercado);
		registro.put("subMercado", subMercado);
		registro.put("montoTotal", total);
		registro.put("porcentajeTotal", porcentaje + "%");
		registro.put("tipoRegistro", tipo);
		registro.put("orden", String.valueOf(orden));
		return registro;
	}

	public HashMap<String, Object> crearRegistroFlujoPatrimonial(String titulo, BigDecimal MTD, BigDecimal YTD,
			BigDecimal ITD, String fechaInicioCuenta, String tipo) {
		HashMap<String, Object> registro = new HashMap<>();
		if (tipo != "detalle") {
			registro.put("titulo", titulo);
			registro.put("mtd", "");
			registro.put("ytd", "");
			registro.put("itd", "");
			registro.put("fechaInicioCuenta", fechaInicioCuenta);
			registro.put("tipoRegistro", tipo);
		} else {
			registro.put("titulo", titulo);
			registro.put("mtd", MTD);
			registro.put("ytd", YTD);
			registro.put("itd", ITD);
			registro.put("fechaInicioCuenta", fechaInicioCuenta);
			registro.put("tipoRegistro", tipo);
		}

		return registro;
	}

	public List<HashMap<String, Object>> procesaPorcentajeCartera(List<HashMap<String, Object>> cartera) {

		BigDecimal porCarteraTotal = BigDecimal.ZERO;

		if (cartera != null) {
			for (HashMap<String, Object> item : cartera) {
				BigDecimal valorDefault = BigDecimal.ZERO;
				String tipoRegistro = item.get("tipoFila").toString();
				String mercado = item.get("Mercado").toString();

				if (!tipoRegistro.equals("blanco") && !tipoRegistro.equals("totalPatrimonio")) {
					BigDecimal valorMercado = (BigDecimal) item.get("valorMercadoCLP");
					BigDecimal valorDiv = BigDecimal.ZERO;
					if (totalPatrimonioVMCLP.compareTo(BigDecimal.ZERO) != 0) {
						valorMercado = valorMercado.multiply(new BigDecimal(100));
						valorDiv = valorMercado.divide(totalPatrimonioVMCLP, 2, BigDecimal.ROUND_HALF_EVEN); // porcentaje
																												// =
																												// porcentaje.divide(totalPatrimonioVMCLP,2,BigDecimal.ROUND_HALF_EVEN);
						
					}
					item.replace("carteraP", valorDefault, valorDiv + "%");

					// Valor en composicion Cartera
					if (tipoRegistro.equals("totalSubMercado")) {
						String subMercado = item.get("SubMercado").toString();
						for (HashMap<String, Object> composicion : composicionCartera) {
							String subMercadoCompo = composicion.get("subMercado").toString();
							String tipoCompo = composicion.get("tipoRegistro").toString();
							if (subMercado.equals(subMercadoCompo) && tipoCompo.equals(tipoRegistro)) {
								composicion.replace("porcentajeTotal", valorDiv + "%");
								break;
							}
						}
					}

					// Valor en composicion Cartera
					if (tipoRegistro.equals("totalMercado")) {
						String subMercado = item.get("Mercado").toString();
						for (HashMap<String, Object> composicion : composicionCartera) {
							String subMercadoCompo = composicion.get("mercado").toString();
							String tipoCompo = composicion.get("tipoRegistro").toString();
							if (subMercado.equals(subMercadoCompo) && tipoCompo.equals(tipoRegistro)) {
								composicion.replace("porcentajeTotal", valorDiv + "%");
								break;
							}
						}
					}

					if (tipoRegistro.equals("detalle"))
						porCarteraTotal = porCarteraTotal.add(valorDiv);
				} else if (tipoRegistro.equals("totalPatrimonio")) {
					totalPatrimonioPorcCart = porCarteraTotal;
					item.replace("carteraP", valorDefault, porCarteraTotal + "%");
				} else if (tipoRegistro.equals("blanco")) {
					item.replace("carteraP", valorDefault, "");
				}

			}
		}

		return cartera;

	}

	public List<HashMap<String, Object>> ordenaCarteraPorClasificacion(List<HashMap<String, Object>> cartera,
			String campoOrdenar) {
		if (cartera != null) {
			Collections.sort(cartera, new Comparator<HashMap<String, Object>>() {
				@Override
				public int compare(HashMap<String, Object> cartera1, HashMap<String, Object> cartera2) {
					return (String.valueOf(cartera2.get(campoOrdenar)))
							.compareTo((String.valueOf(cartera1.get(campoOrdenar))));
				}
			});
		}
		return cartera;
	}

	public List<HashMap<String, Object>> ordenarLista(List<HashMap<String, Object>> lista, String campoOrdenar) {
		if (lista != null) {

			Collections.sort(lista, new Comparator<HashMap<String, Object>>() {
				@Override
				public int compare(HashMap<String, Object> cartera1, HashMap<String, Object> cartera2) {
					return (String.valueOf(cartera1.get(campoOrdenar)))
							.compareTo((String.valueOf(cartera2.get(campoOrdenar))));
				}
			});
		}
		return lista;
	}

	public List<HashMap<String, Object>> preparaSalidaCartola(List<HashMap<String, Object>> cartera) {

		List<HashMap<String, Object>> carteraProcesada = new ArrayList<HashMap<String, Object>>();
		ConsultarCarteraInversionCartolaConsolidada carteraConsolidada = new ConsultarCarteraInversionCartolaConsolidada();

		HashMap<String, Object> registro = null;

		if (cartera != null) {
			int pos = 0;
			boolean debeCambiar = false;
			boolean ultimoRegistro = false;

			String mercado = "";
			String subMercado = "";
			String clasificacion = "";
			String mercadoReg2 = "";
			String subMercadoReg2 = "";
			String clasificacionReg2 = "";

			BigDecimal totalClasificacionVMCLP = BigDecimal.ZERO;
			BigDecimal totalClasificacionVMUSD = BigDecimal.ZERO;
			BigDecimal totalClasificacionPorcCart = BigDecimal.ZERO;

			BigDecimal totalSubMercadoVMCLP = BigDecimal.ZERO;
			BigDecimal totalSubMercadoVMUSD = BigDecimal.ZERO;
			BigDecimal totalSubMercadoPorcCart = BigDecimal.ZERO;

			BigDecimal totalMercadoVMCLP = BigDecimal.ZERO;
			BigDecimal totalMercadoVMUSD = BigDecimal.ZERO;
			BigDecimal totalMercadoPorcCart = BigDecimal.ZERO;

			HashMap<String, Object> item2 = null;

			int n = 0;
			for (HashMap<String, Object> item : cartera) {

				if (((String) item.get("tipoFila")).equals("detalle")) {

					if ((pos + 1) < cartera.size())
						item2 = cartera.get(pos + 1); // Siguiente registro

					if (pos == cartera.size() - 1)
						ultimoRegistro = true;

					mercado = (String) item.get("Mercado");
					subMercado = (String) item.get("SubMercado");
					clasificacion = (String) item.get("Clasificacion");

					if (item2 != null) {
						mercadoReg2 = (String) item2.get("Mercado");
						subMercadoReg2 = (String) item2.get("SubMercado");
						clasificacionReg2 = (String) item2.get("Clasificacion");
					}

					if (pos == 0 || debeCambiar) {

						if (pos == 0) {
							registro = carteraConsolidada.crearRegistroCartera("", "", "", BigDecimal.ZERO, "",
									BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
									BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Long(0), "", "", "",
									"blanco", 0L, 0L, "");
							carteraProcesada.add(registro);
						}

						// Primera linea con primera Clasificacion
						if (clasificacion != "") {
							registro = carteraConsolidada.crearRegistroCartera(clasificacion, "", "", BigDecimal.ZERO,
									"", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
									BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Long(0), "",
									"", "", "clasificacion", 0L, 0L, "");
							carteraProcesada.add(registro);
							debeCambiar = false;
						}
					}

					carteraProcesada.add(item);

					if (clasificacion != "") {

						// Total por clasificacion
						totalClasificacionVMCLP = totalClasificacionVMCLP.add((BigDecimal) item.get("valorMercadoCLP"));
						totalClasificacionVMUSD = totalClasificacionVMUSD.add((BigDecimal) item.get("valorMercadoUSD"));
						totalClasificacionPorcCart = totalClasificacionPorcCart.add((BigDecimal) item.get("carteraP"));

						if (!clasificacion.equals(clasificacionReg2) || (ultimoRegistro)) {

							registro = carteraConsolidada.crearRegistroCartera("TOTAL " + clasificacion, "", "",
									BigDecimal.ZERO, "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
									totalClasificacionVMCLP, totalClasificacionVMUSD, BigDecimal.ZERO, BigDecimal.ZERO,
									totalClasificacionPorcCart, new Long(0), "", "", "", "totalClasificacion", 0L, 0L, "");
							carteraProcesada.add(registro);

							registro = carteraConsolidada.crearRegistroCartera("", "", "", BigDecimal.ZERO, "",
									BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
									BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Long(0), "", "", "",
									"blanco", 0L, 0L, "");
							carteraProcesada.add(registro);

							debeCambiar = true;

							totalClasificacionVMCLP = BigDecimal.ZERO;
							totalClasificacionVMUSD = BigDecimal.ZERO;
							totalClasificacionPorcCart = BigDecimal.ZERO;
						}
					}
					// total por subMercado
					if (subMercado != "") {
						totalSubMercadoVMCLP = totalSubMercadoVMCLP.add((BigDecimal) item.get("valorMercadoCLP"));
						totalSubMercadoVMUSD = totalSubMercadoVMUSD.add((BigDecimal) item.get("valorMercadoUSD"));
						totalSubMercadoPorcCart = totalSubMercadoPorcCart.add((BigDecimal) item.get("carteraP"));

						if (!subMercado.equals(subMercadoReg2) || (ultimoRegistro)) {

							registro = carteraConsolidada.crearRegistroCartera("TOTAL " + subMercado, "", "",
									BigDecimal.ZERO, "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
									totalSubMercadoVMCLP, totalSubMercadoVMUSD, BigDecimal.ZERO, BigDecimal.ZERO,
									totalSubMercadoPorcCart, new Long(0), mercado, subMercado, "", "totalSubMercado",
									0L, 0L, "");
							carteraProcesada.add(registro);

							registro = carteraConsolidada.crearRegistroCartera("", "", "", BigDecimal.ZERO, "",
									BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
									BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Long(0), "", "", "",
									"blanco", 0L, 0L, "");
							carteraProcesada.add(registro);

							// Llenamos el total por subMercado para la seccion de Composicion de la cartera
							registro = crearRegistroComposicionCartera(mercado, subMercado, totalSubMercadoVMCLP,
									totalSubMercadoPorcCart, "totalSubMercado", subMercado, pos);
							composicionCartera.add(registro);

							totalSubMercadoVMCLP = BigDecimal.ZERO;
							totalSubMercadoVMUSD = BigDecimal.ZERO;
							totalSubMercadoPorcCart = BigDecimal.ZERO;
						}

					}

					// total por Mercado
					totalMercadoVMCLP = totalMercadoVMCLP.add((BigDecimal) item.get("valorMercadoCLP"));
					totalMercadoVMUSD = totalMercadoVMUSD.add((BigDecimal) item.get("valorMercadoUSD"));
					totalMercadoPorcCart = totalMercadoPorcCart.add((BigDecimal) item.get("carteraP"));

					if ((pos + 1 == cartera.size()) || !mercado.equals(mercadoReg2)) {
						registro = carteraConsolidada.crearRegistroCartera("TOTAL " + mercado, "", "", BigDecimal.ZERO,
								"", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, totalMercadoVMCLP,
								totalMercadoVMUSD, BigDecimal.ZERO, BigDecimal.ZERO, totalMercadoPorcCart, new Long(0),
								mercado, subMercado, "", "totalMercado", 0L, 0L, "");
						carteraProcesada.add(registro);

						registro = carteraConsolidada.crearRegistroCartera("", "", "", BigDecimal.ZERO, "",
								BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
								BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Long(0), "", "", "", "blanco",
								0L, 0L, "");
						carteraProcesada.add(registro);

						// Llenamos el total por rercado para la seccion de Composicion de la cartera
						registro = crearRegistroComposicionCartera(mercado, mercado + " Z", totalMercadoVMCLP,
								totalMercadoPorcCart, "totalMercado", mercado, n);
						composicionCartera.add(registro);

						totalPatrimonioVMCLP = totalPatrimonioVMCLP.add(totalMercadoVMCLP);
						totalPatrimonioVMUSD = totalPatrimonioVMUSD.add(totalMercadoVMUSD);
						totalPatrimonioPorcCart = totalPatrimonioPorcCart.add(totalMercadoPorcCart);

						// exposicion por moneda
						String moneda = (String) item.get("moneda");
						if (moneda.equals("CLP"))
							totalMonedaPesos = totalMonedaPesos.add(totalMercadoVMCLP);
						else
							totalMonedaDolar = totalMonedaDolar.add(totalMercadoVMCLP);

						totalMercadoVMCLP = BigDecimal.ZERO;
						totalMercadoVMUSD = BigDecimal.ZERO;
						totalMercadoPorcCart = BigDecimal.ZERO;
						n = pos + 1;
					}

				}

				pos++;
			}
		}

		// Patrimonio
		registro = carteraConsolidada.crearRegistroCartera("TOTAL PATRIMONIO", "", "", BigDecimal.ZERO, "",
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, totalPatrimonioVMCLP, totalPatrimonioVMUSD,
				BigDecimal.ZERO, BigDecimal.ZERO, totalPatrimonioPorcCart, new Long(0), "", "", "", "totalPatrimonio",
				0L, 0L, "");
		carteraProcesada.add(registro);

		// % Cartera
		carteraProcesada = procesaPorcentajeCartera(carteraProcesada);

		return carteraProcesada;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getMBIPREConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				
				//String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":53,\"p_FechaDesde\":\"2019-12-01\",\"p_FechaHasta\":\"2019-12-09\",\"p_Datos\":[{\"IdCliente\":100059,\"IdPortafolio\":100069},{\"IdCliente\":100342,\"IdPortafolio\":100560}],\"p_Moneda\":\"CLP\"}";
				//String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":0,\"p_FechaDesde\":\"2021-03-01\",\"p_FechaHasta\":\"2021-03-22\",\"p_Datos\":[{\"IdCliente\":5477,\"IdPortafolio\":6169},{\"IdCliente\":5477,\"IdPortafolio\":12976},{\"IdCliente\":5477,\"IdPortafolio\":12977}],\"p_Moneda\":false}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":0,\"p_FechaDesde\":\"2021-03-01\",\"p_FechaHasta\":\"2021-03-22\",\"p_Datos\":[{\"IdCliente\":5477,\"IdPortafolio\":6169},{\"IdCliente\":5477,\"IdPortafolio\":12980},{\"IdCliente\":5477,\"IdPortafolio\":12976},{\"IdCliente\":5477,\"IdPortafolio\":12977},{\"IdCliente\":5477,\"IdPortafolio\":12981}],\"p_Moneda\":\"CLP\"}";
				
				ConsultarCartolaOnline_Entrada Objin_json = gson.fromJson(json, ConsultarCartolaOnline_Entrada.class);
				ConsultarCartolaOnline demo = new ConsultarCartolaOnline();
				System.out.println("ENTRADA");
				System.out.println(Objin_json.toString());
				try {
					ConsultarCartolaOnline_Salida ObjOut = demo.logica(con, Objin_json);
					System.out.println(ObjOut.toString());

				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}

			} catch (Exception e) {

				e.printStackTrace();
			} finally {
				con.close();
				System.out.println("== END ==");
			}

		}
		;
	}
	public static BigDecimal TransformadorMonedas(Connection conn, String fechaMovimiento, String monedaOrigen,
			String monedaDestino, BigDecimal monto) throws Exception {
		BigDecimal montoTransformado = BigDecimal.ZERO;
		List<CalcularParidad.CalcularParidadSalida> listCalculaParidad = new ArrayList<CalcularParidad.CalcularParidadSalida>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalculoEntrada entradaCalcularParidad = new CalculoEntrada();
		SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");
		Date date2 = parseador.parse(fechaMovimiento);
		String fechaOP = formateador.format(date2);
		List<CalcularParidad.CalculoEntrada> listparidad = new ArrayList<CalcularParidad.CalculoEntrada>();
		entradaCalcularParidad.IdTrabajo = 0L;
		entradaCalcularParidad.IdInterno = 0L;
		entradaCalcularParidad.Fecha = fechaOP; // fecha OP
		entradaCalcularParidad.IdMonedaOrigen = monedaOrigen; // MONEDA QUE VIENE EJ USD
		entradaCalcularParidad.IdMonedaDestino = monedaDestino; // MONDEA A TRANSFORMAR
		entradaCalcularParidad.ValorCambioLocalModificado = BigDecimal.ZERO;
		entradaCalcularParidad.IdUso = "";
		listparidad.add(entradaCalcularParidad);
		CalcularParidad.CalcularParidadEntrada paridadEntrada = new CalcularParidad.CalcularParidadEntrada(); // ENTRADA
																												// FINAL
		paridadEntrada.p_IdEmpresa = 1L;
		paridadEntrada.p_IdTrabajo = 0L;
		paridadEntrada.p_CalculosEntrada = listparidad;
		entradaCalcularParidad.MontoOrigen = monto;
		listCalculaParidad.add(calcularParidad.logica(conn, paridadEntrada)); // dentro tengo el monto
																		// transformado
		montoTransformado = listCalculaParidad.get(0).p_out_CalculosSalida.get(0).MontoDestino; // Dato
		// transformado
		return montoTransformado; // a pesos CLP
	}

}
