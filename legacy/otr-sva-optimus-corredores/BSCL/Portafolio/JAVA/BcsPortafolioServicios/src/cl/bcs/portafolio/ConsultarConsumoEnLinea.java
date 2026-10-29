package cl.bcs.portafolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.normativa.cartola.BcsCartola;
import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.ResumenInversion;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarConsumoEnLinea implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarConsumoEnLinea.class);
	
	private final String SQL_SELECT_PLANTILLASLIMITES = loadResourceToString("cl/bcs/portafolio/sql/ConsultarConsumoEnLinea_CargaDetallePlantillaLimites.sql");
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/portafolio/sql/ConsultarConsumoEnLinea_ConsultarMovimientos.sql");
	
	public static class EntradaObtieneConsumoEnLinea {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public Long p_IdInterno = 0L;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public class Datos {
		public Long IdCliente;
		public Long IdPortafolio;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public class ResumenInversionCuentaInversion {
		public Long IdCliente;
		public Long IdPortafolio;
		public String Item;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public String IdMonedaAdicional;
		public BigDecimal MontoMonedaAdicional;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimientos {
		public Long IdMovimiento;
		public String IdEstado;
		public String Mercado;
		public BigDecimal Monto;
		public String IdMoneda;
		public String Tipo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PlantillaLimites {
		public String IdPlantillaLimite;
		public Long IdEmpresa;
		public Long IdLimite;
		public String Nombre;
		public String NombreClasificacionTipoInstrumento;
		public String TipoInstrumento;
		public String TipoOperacion;
		public String CanalIngreso;
		public String Moneda;
		public String IdMoneda;
		public String IdTipoLimite;
		public BigDecimal Porcentaje;
		public BigDecimal MontoTotal;
		public BigDecimal MontoDisponible;
		public BigDecimal MontoUtilizado;
		public String NombreEstadoLimite;
		public String IdClasificacionTipoInstrumento;
		public String IdEstado;
		public String EsLeverage;
		public Long IdInterno;
		public Long IdPortafolio;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaObtieneConsumoEnLinea {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlantillaLimites> P_OUT_PLANTILLASLIMITES;

		public SalidaObtieneConsumoEnLinea() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_PLANTILLASLIMITES = new ArrayList<PlantillaLimites>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) {
		JsonObject dataout = new JsonObject();
		try {
			Gson gson = Utils.getGsonBuilder();
			EntradaObtieneConsumoEnLinea in = gson.fromJson(jin.toString(), EntradaObtieneConsumoEnLinea.class);
			SalidaObtieneConsumoEnLinea out = logica(conn, in);

			JsonParser parser = new JsonParser();
			dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataout;
	}

	public SalidaObtieneConsumoEnLinea logica(Connection con, EntradaObtieneConsumoEnLinea entrada) {
		logger.info("Inicio ConsultarConsumoEnLinea");
		SalidaObtieneConsumoEnLinea salida = new SalidaObtieneConsumoEnLinea();
		
		try {
			SQL sql = new SQL();
			//Prepara datos
			String fechaSistema = Utils.getFechaSistema(con, "BSCL", entrada.p_IdEmpresa);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");			
			LocalDate fechaDesde = LocalDate.parse(fechaSistema,formatter).minusDays(1L);
			LocalDate fechaHasta = LocalDate.parse(fechaSistema,formatter);
			
			HashMap<String, Object> paramsIn = new HashMap<String, Object>();
			paramsIn.put("IdPortafolio", entrada.p_IdPortafolio);
			paramsIn.put("IdEmpresa", entrada.p_IdEmpresa);
			
			List<PlantillaLimites> plantillasLimites = sql.query(con, SQL_SELECT_PLANTILLASLIMITES, paramsIn,PlantillaLimites.class);

			paramsIn.put("FechaOperacion", fechaHasta.toString() + " 23:59:59");
			paramsIn.put("Fecha", fechaHasta.toString());
			List<Movimientos> movimientos = sql.query(con, SQL_SELECT_MOVIMIENTOS, paramsIn, Movimientos.class);
			List<Movimientos> ordenesSobregiro = new ArrayList<>();
			List<Movimientos> ordenesRiesgoCredito = new ArrayList<>();
			List<Movimientos> aporteFFMMSobregiro = new ArrayList<>();
			List<Movimientos> aporteFFMMRiesgoCredito = new ArrayList<>();
			List<Movimientos> cuentaInversionSobregiro = new ArrayList<>();
			List<Movimientos> cuentaInversionRiesgoCredito = new ArrayList<>();		
			
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			
			CalcularParidad calcularParidades = new CalcularParidad();
			CalcularParidadEntrada entradaCalcularParidad = new CalcularParidadEntrada();
			List<CalculoEntrada> movimientosParidad = new ArrayList<>();
			HashMap<Long, Movimientos> hashMovimientos = new HashMap<>();
			
			Long cont = 0L;
			
			//prepara datos de movimientos para paridad
			for(Movimientos movimiento : movimientos) {
				movimiento.IdMovimiento = cont;
				if(!movimiento.IdMoneda.equals(monedaSistema)) {
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = fechaHasta.toString();
					calculoEntrada.IdInterno = cont;
					calculoEntrada.IdMonedaDestino = monedaSistema;
					calculoEntrada.IdMonedaOrigen = movimiento.IdMoneda;
					calculoEntrada.IdTrabajo = 1L;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = movimiento.Monto;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					movimientosParidad.add(calculoEntrada);
				}else {
					if(movimiento.IdEstado.equals("FAC")) {
						ordenesSobregiro.add(movimiento);
					}else if(movimiento.Tipo.isEmpty() && !movimiento.IdEstado.equals("FAC")) {
						ordenesRiesgoCredito.add(movimiento);
					}else if(movimiento.Mercado.equals("FM") && movimiento.IdEstado.equals("CRT")) {
						aporteFFMMSobregiro.add(movimiento);
					}else if(movimiento.Mercado.equals("FM") && !movimiento.IdEstado.equals("CRT")) {
						aporteFFMMRiesgoCredito.add(movimiento);
					}else if(movimiento.Mercado.isEmpty() && movimiento.IdEstado.equals("CRT")) {
						cuentaInversionSobregiro.add(movimiento);
					}else if(movimiento.Mercado.isEmpty() && !movimiento.IdEstado.equals("CRT")) {
						cuentaInversionRiesgoCredito.add(movimiento);
					}
				}
				hashMovimientos.put(cont, movimiento);
				cont++;
			}
			
			HashMap<String, Object> paramsInResumenInversion = new HashMap<>();

			paramsInResumenInversion.put("p_IdModulo", "SOC");
			paramsInResumenInversion.put("p_IdUsuario", entrada.p_IdUsuario);
			paramsInResumenInversion.put("p_IdEmpresa", entrada.p_IdEmpresa);
			
			List<Dato> datoParametros = new ArrayList<>();
			Dato dato = new Dato();
			dato.IdCliente = entrada.p_IdCliente;
			dato.IdPortafolio = entrada.p_IdPortafolio;
			
			datoParametros.add(dato);
			
			ResumenInversion resumenInversion = new ResumenInversion();
			BcsCartola.EntradaCartola entradaResumenInversion = new BcsCartola.EntradaCartola();
			BcsCartola.SalidaCartola salidaResumenInversion = new BcsCartola.SalidaCartola();
			entradaResumenInversion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaResumenInversion.p_IdModulo = entrada.p_IdModulo;
			entradaResumenInversion.p_IdUsuario = entrada.p_IdUsuario;
			entradaResumenInversion.p_FechaDesde = Fecha.toDate(fechaDesde.toString());
			entradaResumenInversion.p_FechaHasta = Fecha.toDate(fechaHasta.toString());
			entradaResumenInversion.p_Datos = datoParametros;
			
			salidaResumenInversion = resumenInversion.procesar(con, entradaResumenInversion);
			
			List<HashMap<String,Object>> resumenInversiones = (List<HashMap<String, Object>>) salidaResumenInversion.P_OUT_SALIDA.get("resumenInversion");
			
			//ordena resumen inversiones
			List<ResumenInversionCuentaInversion> listResumenInversion = new ArrayList<>();
			for (HashMap<String,Object> resumenItem : resumenInversiones) {
				ResumenInversionCuentaInversion resumen = new ResumenInversionCuentaInversion();
				for (String key : resumenItem.keySet()) {
					switch (key) {
						case "IdCliente":
							resumen.IdCliente = (Long)resumenItem.get(key);
							break;
						case "IdMonedaAdicional":
							resumen.IdMonedaAdicional = resumenItem.get(key).toString();
							break;
						case "IdMonedaSistema":
							resumen.IdMonedaSistema = resumenItem.get(key).toString();
							break;
						case "IdPortafolio":
							resumen.IdPortafolio = (Long) resumenItem.get(key);
							break;
						case "Item":
							resumen.Item = resumenItem.get(key).toString();
							break;
						case "MontoMonedaAdicional":
							resumen.MontoMonedaAdicional = (BigDecimal)(resumenItem.get(key));
							break;
						case "MontoMonedaSistema":
							resumen.MontoMonedaSistema = (BigDecimal)(resumenItem.get(key));
							break;
					}
				}
				listResumenInversion.add(resumen);
			}
			
			ConsultarSaldoCuentaInversion saldoCuentaInversion = new ConsultarSaldoCuentaInversion();
			EntradaConsultarSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaConsultarSaldoCuentaInversion();
			SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuentaInversion = new SalidaConsultarSaldoCuentaInversion();
			List<InCuentaInversionConsultar> listaCuentasAConsultar = new ArrayList<>();
			
			HashMap<Long, PlantillaLimites> hashLimites = new HashMap<>();
			List<PlantillaLimites> plantillaMonedaSistema = new ArrayList<>();
			
			//saca idMoneda para consultar cuenta inversion y envia limites de otras monedas a la paridad
			for (PlantillaLimites limite : plantillasLimites) {
				limite.IdInterno = entrada.p_IdInterno;
				InCuentaInversionConsultar cuentaAConsultar = new InCuentaInversionConsultar();
				cuentaAConsultar.IdPortafolio = entrada.p_IdPortafolio;
				cuentaAConsultar.IdMoneda = limite.IdMoneda;
				listaCuentasAConsultar.add(cuentaAConsultar);
				
				if(!limite.IdMoneda.equals(monedaSistema) ) {
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = fechaHasta.toString();
					calculoEntrada.IdInterno = limite.IdLimite;
					calculoEntrada.IdMonedaDestino = monedaSistema;
					calculoEntrada.IdMonedaOrigen = limite.IdMoneda;
					calculoEntrada.IdTrabajo = 2L;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = limite.MontoTotal;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					movimientosParidad.add(calculoEntrada);
					hashLimites.put(limite.IdLimite, limite);
				}else {
					plantillaMonedaSistema.add(limite);
				}	
			}
			
			entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSaldoCuentaInversion.p_IdModulo = entrada.p_IdModulo;
			entradaSaldoCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
			entradaSaldoCuentaInversion.p_UsaReglasNegocio = "S";
			entradaSaldoCuentaInversion.p_FechaDesde = fechaDesde.toString();
			entradaSaldoCuentaInversion.p_FechaHasta = fechaHasta.toString();
			entradaSaldoCuentaInversion.p_CuentaInversionAConsultar = listaCuentasAConsultar;
			entradaSaldoCuentaInversion.p_SoloSaldoDisponible = "S";
			entradaSaldoCuentaInversion.p_IdMoneda = monedaSistema;
			
			salidaConsultarSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
			
			List<SaldoPorFechaEfectiva> saldoCuentaInversionMoneda = new ArrayList<>();
			HashMap<Long, SaldoPorFechaEfectiva> hashSaldoCuentaInversion = new HashMap<>();
			
			//prepara datos de saldo cuenta inversion para paridad
			for(SaldoPorFechaEfectiva saldo : salidaConsultarSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA) {
				if(saldo.IdMoneda.equals(monedaSistema) ) {
					saldoCuentaInversionMoneda.add(saldo);
				}else {
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = fechaHasta.toString();
					calculoEntrada.IdInterno = saldo.IdCuentaInversion;
					calculoEntrada.IdMonedaDestino = monedaSistema;
					calculoEntrada.IdMonedaOrigen = saldo.IdMoneda;
					calculoEntrada.IdTrabajo = 3L;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = saldo.Saldo;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					movimientosParidad.add(calculoEntrada);
				}
				hashSaldoCuentaInversion.put(saldo.IdCuentaInversion, saldo);
			}
			
			entradaCalcularParidad.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCalcularParidad.p_IdTrabajo = 1L;
			entradaCalcularParidad.p_CalculosEntrada = movimientosParidad;
			CalcularParidadSalida salidaCalcularParidad = calcularParidades.logica(con,entradaCalcularParidad);
			
			//asigna los valores de la paridad a las listas correspondientes
			for(CalculoSalida calculoParidad : salidaCalcularParidad.p_out_CalculosSalida) {
				if(calculoParidad.IdTrabajo.equals(1L)) {
					Movimientos movimiento = hashMovimientos.get(calculoParidad.IdInterno);
					
					if(movimiento.IdEstado.equals("FAC")) {
						movimiento.Monto = calculoParidad.MontoDestino ;
						ordenesSobregiro.add(movimiento);
					}else if(movimiento.Tipo.isEmpty() && !movimiento.IdEstado.equals("FAC")) {
						movimiento.Monto = calculoParidad.MontoDestino ;
						ordenesRiesgoCredito.add(movimiento);
					}else if(movimiento.Mercado.equals("FM") && movimiento.IdEstado.equals("CRT")) {
						movimiento.Monto = calculoParidad.MontoDestino ;
						aporteFFMMSobregiro.add(movimiento);
					}else if(movimiento.Mercado.equals("FM") && !movimiento.IdEstado.equals("CRT")) {
						movimiento.Monto = calculoParidad.MontoDestino ;
						aporteFFMMRiesgoCredito.add(movimiento);
					}else if(movimiento.Mercado.isEmpty() && movimiento.IdEstado.equals("CRT")) {
						movimiento.Monto = calculoParidad.MontoDestino ;
						cuentaInversionSobregiro.add(movimiento);
					}else if(movimiento.Mercado.isEmpty() && !movimiento.IdEstado.equals("CRT")) {
						movimiento.Monto = calculoParidad.MontoDestino ;
						cuentaInversionRiesgoCredito.add(movimiento);
					}
				}else if(calculoParidad.IdTrabajo.equals(2L)) {
					PlantillaLimites limite = hashLimites.get(calculoParidad.IdInterno);
					limite.MontoTotal = calculoParidad.MontoDestino;
					plantillaMonedaSistema.add(limite);
				}else if(calculoParidad.IdTrabajo.equals(3L)) {
					SaldoPorFechaEfectiva saldo = hashSaldoCuentaInversion.get(calculoParidad.IdInterno);
					saldo.Saldo = calculoParidad.MontoDestino;
					saldoCuentaInversionMoneda.add(saldo);
				}
			}
		
			movimientosParidad = new ArrayList<CalculoEntrada>();
			entradaCalcularParidad = new CalcularParidadEntrada();
			salidaCalcularParidad = new CalcularParidadSalida();
			HashMap<Long, PlantillaLimites> hashLimite = new HashMap<>();
			
			//calculo de monto disponible y utilizado para cada limite
			for (PlantillaLimites limite : plantillaMonedaSistema) {
				BigDecimal totalPasivos = BigDecimal.ZERO;
				BigDecimal totalActivos = BigDecimal.ZERO;
				BigDecimal totalCaja = BigDecimal.ZERO;
				BigDecimal montoUtilizado = BigDecimal.ZERO;
				
				//Limites de Sobregiro
				if(limite.IdTipoLimite.equals("LIMCLILINEASOBREGIRO")) {
					if (limite.EsLeverage.equals("S")) {
						for(SaldoPorFechaEfectiva saldo : saldoCuentaInversionMoneda){
							if(limite.IdMoneda.equals(saldo.IdMoneda)) {
								totalCaja = saldo.Saldo;
							}
						}
						for(ResumenInversionCuentaInversion resumen : listResumenInversion) {
							//prestamos y pactos 
							if( (resumen.Item.trim().equalsIgnoreCase(" Prestamos") || resumen.Item.trim().equalsIgnoreCase(" Pactos")) ) {
								totalPasivos = totalPasivos.add(resumen.MontoMonedaSistema);
							//los que no sean prestamos, pactos, caja disponible y simultaneas
							}else if(!resumen.Item.trim().equalsIgnoreCase(" Prestamos") && !resumen.Item.trim().equalsIgnoreCase(" Pactos") && !resumen.Item.contains("Simul") && !resumen.Item.contains("Caja Disponible") && !resumen.Item.toUpperCase().contains(limite.IdMoneda)) {
								totalActivos = totalActivos.add(resumen.MontoMonedaSistema);
							}	
						}
						totalActivos = totalActivos.add(totalCaja);
						BigDecimal leverage = BigDecimal.ZERO;
						if(!totalActivos.equals(BigDecimal.ZERO) ) {
							leverage = totalPasivos.divide(totalActivos, 2, RoundingMode.HALF_UP);
						}
						limite.MontoTotal = totalCaja.add(leverage.multiply(totalActivos.subtract(totalPasivos)));
					}
					montoUtilizado = getMontoUtilizado(limite.IdMoneda, limite.IdClasificacionTipoInstrumento, ordenesSobregiro, aporteFFMMSobregiro, cuentaInversionSobregiro);
				}else {//Limites Riesgo de Credito
					montoUtilizado = getMontoUtilizado(limite.IdMoneda, limite.IdClasificacionTipoInstrumento, ordenesRiesgoCredito, aporteFFMMRiesgoCredito, cuentaInversionRiesgoCredito);
				}					
				if(montoUtilizado.signum() == -1) {
					limite.MontoDisponible = limite.MontoTotal.add(montoUtilizado);
				}else {
				limite.MontoDisponible = limite.MontoTotal.subtract(montoUtilizado);
				}
				limite.MontoUtilizado = montoUtilizado;
					
				if(!limite.IdMoneda.equals(monedaSistema)) {
					hashLimite.put(limite.IdLimite, limite);
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = fechaHasta.toString();
					calculoEntrada.IdInterno = 1L;
					calculoEntrada.IdMonedaDestino = limite.IdMoneda;
					calculoEntrada.IdMonedaOrigen = monedaSistema;
					calculoEntrada.IdTrabajo = limite.IdLimite;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = limite.MontoDisponible;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					movimientosParidad.add(calculoEntrada);
					
					calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = fechaHasta.toString();
					calculoEntrada.IdInterno = 2L;
					calculoEntrada.IdMonedaDestino = limite.IdMoneda;
					calculoEntrada.IdMonedaOrigen = monedaSistema;
					calculoEntrada.IdTrabajo = limite.IdLimite;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = limite.MontoUtilizado;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					movimientosParidad.add(calculoEntrada);
					
					calculoEntrada = new CalculoEntrada();
					calculoEntrada.Fecha = fechaHasta.toString();
					calculoEntrada.IdInterno = 3L;
					calculoEntrada.IdMonedaDestino = limite.IdMoneda;
					calculoEntrada.IdMonedaOrigen = monedaSistema;
					calculoEntrada.IdTrabajo = limite.IdLimite;
					calculoEntrada.IdUso = "";
					calculoEntrada.MontoOrigen = limite.MontoTotal;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					movimientosParidad.add(calculoEntrada);
				}
			}//Fin for
			
			//cambiar a la moneda del limite
			if(!movimientosParidad.isEmpty()) {
				entradaCalcularParidad.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCalcularParidad.p_IdTrabajo = 1L;
				entradaCalcularParidad.p_CalculosEntrada = movimientosParidad;
				salidaCalcularParidad = calcularParidades.logica(con, entradaCalcularParidad);
				
				for(CalculoSalida paridad : salidaCalcularParidad.p_out_CalculosSalida) {
					if(hashLimite.containsKey(paridad.IdTrabajo)) {
						PlantillaLimites plantilla = hashLimite.get(paridad.IdTrabajo);
						if(paridad.IdInterno.equals(1L)) {
							plantilla.MontoDisponible = paridad.MontoDestino;
						}else if(paridad.IdInterno.equals(2L)){
							plantilla.MontoUtilizado = paridad.MontoDestino;
						}else if(paridad.IdInterno.equals(3L)) {
							plantilla.MontoTotal = paridad.MontoDestino;
						}
					}
				}
			}
			salida.P_OUT_PLANTILLASLIMITES = plantillasLimites;
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Fin ConsultarConsumoEnLinea");
		return salida;
	}

	public BigDecimal getMontoUtilizado(String idMonedaLimite, String mercado, List<Movimientos> ordenes, List<Movimientos> aporteFFMM, List<Movimientos> cargoAbono) {
		BigDecimal montoOrdenes = BigDecimal.ZERO;
		BigDecimal montoCargoAbonos = BigDecimal.ZERO;
		BigDecimal montoFFMM = BigDecimal.ZERO;
	
		for(Movimientos orden : ordenes) {
			montoOrdenes = montoOrdenes.add(orden.Monto);
		}	
		
		for(Movimientos aporte : aporteFFMM) {
			if(aporte.Tipo.equals("APO")) {
				montoFFMM = montoFFMM.add(aporte.Monto);
			}
		}		
		
		for(Movimientos movimiento: cargoAbono) {
			/*if(movimiento.Tipo.equals("AB") && movimiento.IdMoneda.equals(idMonedaLimite)) {
				montoCargoAbonos = montoCargoAbonos.add(movimiento.Monto);
			}else*/ if (movimiento.Tipo.equals("CA") && movimiento.IdMoneda.equals(idMonedaLimite)){
				montoCargoAbonos = montoCargoAbonos.subtract(movimiento.Monto);
			}
		}
		return montoOrdenes.add(montoFFMM).add(montoCargoAbonos);
	}
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			conn = PlataformaUtils.getBolsaConnection();
			System.out.println("== INICIO ==");
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_IdCliente\":123569,\"p_IdPortafolio\":27499}";
			String jsonQA= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":50,\"p_IdModulo\":\"SOC\",\"p_IdCliente\":100059,\"p_IdPortafolio\":100069}";
			String jsonBOLSA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1006,\"p_IdModulo\":\"SOC\",\"p_IdCliente\":69201,\"p_IdPortafolio\":421459}";
			EntradaObtieneConsumoEnLinea entrada = gson.fromJson(jsonBOLSA, EntradaObtieneConsumoEnLinea.class);

			ConsultarConsumoEnLinea demo = new ConsultarConsumoEnLinea();
			SalidaObtieneConsumoEnLinea ObjOut = demo.logica(conn, entrada);

			System.out.println(ObjOut.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");
	}
}