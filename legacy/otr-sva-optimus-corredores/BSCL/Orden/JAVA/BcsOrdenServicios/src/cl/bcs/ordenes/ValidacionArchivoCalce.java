package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Entrada;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Salida;
import cl.bcs.ordenes.AnularAsignaciones.OrdenAnulada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CobroModificados_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Cobro_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.CrearSimulacionAsignacion_Entrada;
import cl.bcs.ordenes.CrearSimulacionAsignacion.CrearSimulacionAsignacion_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Simulacion_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Resultado;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ValidacionArchivoCalce implements JavaCall {
	private final Log log = new Log("ORD.ValidacionArchivoCalce");

	private final String SQL_SELECT_CONSULTAR_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarTransaccion_SelectTransaccion.sql");
	private final String SQL_SELECT_CONSULTAR_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarPortafolioCliente_SelectPortafolio.sql");

	public static class ArchivoEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoAsignacion;
		public String p_IdTipo;
		public String p_Origen;
		public List<AsignacionMasiva> p_AsignacionMasiva;

	}

	public static class AsignacionMasiva {
		public Long folioTransaccion;
		public String rutCliente;
		public Long codigoPortafolio;
		public Long cantidadAsignar;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}



	public static class AsignacionMasiva_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Resultado> P_OUT_RESULTADO;
		public List<Resultado_Salida> P_OUT_RESULTADOASIGNACION;
		public List<MensajeSalida> P_OUT_ENVIOFACTURACION;

		public AsignacionMasiva_Salida() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado>();
			P_OUT_ENVIOFACTURACION = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOASIGNACION = new ArrayList<Resultado_Salida>();

		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DataCliente {
		public Long idTransaccion;
		public String rut;
		public Long codPortafolio;
		public Long cantidad;
		public BigDecimal cantidadSimulacion;

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
		public String MarcaNegociacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosClienteSalida {
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}




	@Trace
	public AsignacionMasiva_Salida logica(Connection conn, ArchivoEntrada in) throws Exception {
		AsignacionMasiva_Salida salida = new AsignacionMasiva_Salida();
		SQL sql = new SQL();
		
		List<cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada> dataAnularAsignacion = new ArrayList<>();
		List<Long> IdTransaccion = new ArrayList<>();
		List<Long> IdTransaccionSimulacion = new ArrayList<>();
		List<Long> IdTransaccionCobros = new ArrayList<>();
		List<Long> IdInternoCobros = new ArrayList<>();
		List<Long> IdInternoSimulacion = new ArrayList<>();
		List<Long> IdPortafolioSimulacion = new ArrayList<>();
		List<Long> IdPortafolioCobros = new ArrayList<>();
		List<DataCliente> dataCliente = new ArrayList<>();
		List<OrdenPostrera_Entrada> dataOrdenes = new ArrayList<>();
		List<CobroModificados_Entrada> dataCobros = new ArrayList<>();
		List<cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada> dataAsignacionTransaccion = new ArrayList<>();
		List<DatosTransaccionSalida> dataTransaccion = new ArrayList<>();
		List<cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada> dataAsignacionPortafolio = new ArrayList<>();
		List<DatosClienteSalida> dataPortafolio = new ArrayList<>();
		List<Asignacion_Entrada> dataEnvioFacturacion = new ArrayList<>();
		List<Asignacion_Entrada> dataEnvio = new ArrayList<>();

		/*
		 * for que recorre el archivo de entrada y separa los datos para trabajarlos en
		 * los distintos SQL creados para el proceso masivo
		 */
		for (int i = 0; i < in.p_AsignacionMasiva.size(); i++) {
			DataCliente data = new DataCliente();
			IdTransaccion.add(in.p_AsignacionMasiva.get(i).folioTransaccion);
			data.idTransaccion = in.p_AsignacionMasiva.get(i).folioTransaccion;
			data.rut = in.p_AsignacionMasiva.get(i).rutCliente;
			data.codPortafolio = in.p_AsignacionMasiva.get(i).codigoPortafolio;
			data.cantidad = in.p_AsignacionMasiva.get(i).cantidadAsignar;
			data.cantidadSimulacion = new BigDecimal(data.cantidad);
			dataCliente.add(data);

		}

		HashMap<String, Object> consultaTransaccion = new HashMap<>();
		consultaTransaccion.put("folio", IdTransaccion);
		consultaTransaccion.put("marca", "");

		/*
		 * SQL que trabaja con los folios de las transacciones para buscar los datos
		 * asociados a cada transaccion ingresada por el archivo masivo
		 * 
		 */

		List<DatosTransaccionSalida> salidaTransaccion = sql.query(conn, SQL_SELECT_CONSULTAR_TRANSACCIONES,
				consultaTransaccion, DatosTransaccionSalida.class);

		if (salidaTransaccion.isEmpty()) {

			String codigo = "FOLIO_TRANSACCION_VACIO";
			Long msj = in.p_IdEmpresa;
			String desc = Diccionario.getMensaje(conn, msj, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));

			return salida;

		} else {

			/*
			 * for creado para asignar los datos de las transacciones rescatadas en el sql a
			 * las transacciones ingresadas en el archivo de carga masiva esto debido a que
			 * si el archivo salidaTransaccion solo viene con 1 dato pero en el archivo de
			 * carga esa transaccion viene asociada a mas de un cliente y distintos
			 * portafolios
			 * 
			 * esta se debe repetir tantas veces se requiera
			 */

			for (int i = 0; i < in.p_AsignacionMasiva.size(); i++) {
				DatosTransaccionSalida salidaTransaccionFinal = new DatosTransaccionSalida();
				for (int x = 0; x < salidaTransaccion.size(); x++) {
					if (dataCliente.get(i).idTransaccion.toString().equals(salidaTransaccion.get(x).FolioTransaccion)) {
						salidaTransaccionFinal.IdTransaccion = salidaTransaccion.get(x).IdTransaccion;
						salidaTransaccionFinal.CantidadDisponibleTransaccion = salidaTransaccion.get(x).CantidadDisponibleTransaccion;
						salidaTransaccionFinal.FolioTransaccion = salidaTransaccion.get(x).FolioTransaccion;
						salidaTransaccionFinal.MontoTransaccion = salidaTransaccion.get(x).MontoTransaccion;
						salidaTransaccionFinal.ValorRescateTransaccion = salidaTransaccion.get(x).ValorRescateTransaccion;
						dataTransaccion.add(salidaTransaccionFinal);
					}
				}
			}

			/*
			 * Este for recore el archivo salidaTransaccion y realiza la validacion del la
			 * cantidad de la transaccion
			 * 
			 * esto para ver si la suma de todas las asignaciones asociadas a una
			 * transaccion no sobrepasan la cantidad disponible de la transaccion
			 */

			for (int i = 0; i < salidaTransaccion.size(); i++) {
				long cantidad_total = 0;
				log.write(salidaTransaccion.get(i).CantidadDisponibleTransaccion.longValue());
				for (int c = 0; c < dataCliente.size(); c++) {
					if (dataCliente.get(c).idTransaccion.toString().equals(salidaTransaccion.get(i).FolioTransaccion)) {
						cantidad_total += dataCliente.get(c).cantidad;
					}

				}
				if (cantidad_total <= (salidaTransaccion.get(i).CantidadDisponibleTransaccion.longValue())) {
					log.write("OK : " + cantidad_total + "  <= "
							+ salidaTransaccion.get(i).CantidadDisponibleTransaccion.longValue());

				} else {
					log.write("FALSE : " + cantidad_total + "  > "
							+ salidaTransaccion.get(i).CantidadDisponibleTransaccion.longValue());
					String codigo = "CANTIDAD_TRAN_INSUFICIENTE";
					String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
					return salida;
				}
			}

			/*
			 * SQL creado para rescatar los datos de los portafolios segun el rut y codigo
			 * de portafolio ingresado en el archivo carga masiva
			 */


			List<String> codigoPortafolio = in.p_AsignacionMasiva.stream().map(linea -> linea.codigoPortafolio.toString()).collect(Collectors.toList());
			List<String> documentoIdentidad = in.p_AsignacionMasiva.stream().map(linea -> linea.rutCliente.toString()).collect(Collectors.toList());
			HashMap<String, Object> param = new HashMap<>();
			param.put("codigoPortafolio", codigoPortafolio);
			param.put("documentoIdentidad", documentoIdentidad);
			List<DatosClienteSalida> salidaCliente = sql.query(conn, SQL_SELECT_CONSULTAR_PORTAFOLIO, param,DatosClienteSalida.class);
			
			log.write(salidaCliente);

			if (salidaCliente.isEmpty()) {
				String codigo = "PORTAFOLIO_ASIGNACION";
				String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
				return salida;

			}

				/*
				 * for creado para recorrer el archivo salidaCliente y generar la lista de los
				 * portafolios esto por si el portafolio cliente se repite mas de una vez en el
				 * archivo de entrada de la carga masiva
				 */

				for (int i = 0; i < in.p_AsignacionMasiva.size(); i++) {
					DatosClienteSalida salidaPortafolioFinal = new DatosClienteSalida();
					for (int x = 0; x < salidaCliente.size(); x++) {
						if (dataCliente.get(i).codPortafolio.toString().equals(salidaCliente.get(x).CodigoPortafolio)) {
							if (dataCliente.get(i).rut.equals(salidaCliente.get(x).DocumentoIdentidad)) {
								salidaPortafolioFinal.CodigoPortafolio = salidaCliente.get(x).CodigoPortafolio;
								salidaPortafolioFinal.DocumentoIdentidad = salidaCliente.get(x).DocumentoIdentidad;
								salidaPortafolioFinal.IdPortafolio = salidaCliente.get(x).IdPortafolio;
								dataPortafolio.add(salidaPortafolioFinal);
							}
						}
					}
				}

				/*
				 * si el archivo salidaTransaccion trae solo 1 registro pero varios clientes o
				 * portafolios asociados se crean 2 listas una de transaccion con 1 registro y
				 * una de portafolios con todos los clientes o portafolio asociados a la
				 * transaccion que vienen en el archivo de entrada carga masiva
				 * 
				 */

				if (salidaTransaccion.size() == 1) {
					for (int i = 0; i < salidaTransaccion.size(); i++) {
						System.out.println(i);
						System.out.println(salidaTransaccion.size());
						cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada asigDataTransaccion = new cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada();
						if (String.valueOf(dataCliente.get(i).idTransaccion).equals(salidaTransaccion.get(i).FolioTransaccion)) {
							
							IdTransaccionSimulacion.add(salidaTransaccion.get(i).IdTransaccion);
							asigDataTransaccion.IdMovimiento = salidaTransaccion.get(i).IdTransaccion;
							asigDataTransaccion.IdTransaccion = salidaTransaccion.get(i).IdTransaccion;
							asigDataTransaccion.Cantidad = salidaTransaccion.get(i).CantidadDisponibleTransaccion;
							asigDataTransaccion.Monto = salidaTransaccion.get(i).MontoTransaccion;
							asigDataTransaccion.ValorRescate = salidaTransaccion.get(i).ValorRescateTransaccion;
							dataAsignacionTransaccion.add(asigDataTransaccion);
						
						}
					}
//				int x;
					for (int x = 0; x < salidaCliente.size(); x++) {
						cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada asigDataPortafolio = new cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada();
						if (dataCliente.get(x).rut.equals(dataPortafolio.get(x).DocumentoIdentidad)) {
							IdPortafolioSimulacion.add(dataPortafolio.get(x).IdPortafolio);
							System.out.println(dataPortafolio.get(x).CodigoPortafolio);
							asigDataPortafolio.IdPortafolio = dataPortafolio.get(x).IdPortafolio;
							asigDataPortafolio.Cantidad = new BigDecimal("0");
							asigDataPortafolio.Porcentaje = new BigDecimal("0");
							dataAsignacionPortafolio.add(asigDataPortafolio);
						}

						/*
						 * proceso de llamado a la simulacion de asiganacion y asignar la transaccion a
						 * los distintos portafolios
						 * 
						 */

						CrearSimulacionAsignacion serviceCrearSimulacionAsignacion = new CrearSimulacionAsignacion();

						CrearSimulacionAsignacion_Entrada entradaCrearSimulacionAsignacion = new CrearSimulacionAsignacion_Entrada();
						entradaCrearSimulacionAsignacion.p_IdEmpresa = in.p_IdEmpresa;
						entradaCrearSimulacionAsignacion.p_IdUsuario = in.p_IdUsuario;
						entradaCrearSimulacionAsignacion.p_IdModulo = in.p_IdModulo;
						entradaCrearSimulacionAsignacion.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
						entradaCrearSimulacionAsignacion.p_IdTipoAsignacion = in.p_IdTipoAsignacion;
						entradaCrearSimulacionAsignacion.p_IdTipo = in.p_IdTipo;
						entradaCrearSimulacionAsignacion.p_Origen = in.p_Origen;
						entradaCrearSimulacionAsignacion.p_Transacciones = dataAsignacionTransaccion;
						entradaCrearSimulacionAsignacion.p_Carteras = new ArrayList<>();
						entradaCrearSimulacionAsignacion.p_Ordenes = new ArrayList<>();
						entradaCrearSimulacionAsignacion.p_Portafolios = dataAsignacionPortafolio;
						CrearSimulacionAsignacion_Salida salidaCrearSimulacionAsignacion = serviceCrearSimulacionAsignacion.logica(conn, entradaCrearSimulacionAsignacion);
					
						int contador = 0;
						for (Simulacion_Salida resp : salidaCrearSimulacionAsignacion.P_OUT_SIMASIGNACION) {		
						OrdenPostrera_Entrada ordenes = new OrdenPostrera_Entrada();
						ordenes.IdInterno = resp.IdInterno;
						ordenes.FechaRecepcion = resp.FechaIngresoRegistro.toString();
						ordenes.Glosa = resp.Glosa;
						ordenes.IdBoveda = resp.IdBoveda;
						ordenes.IdFormaCustodia = resp.IdFormaCustodia;
						ordenes.EsFacturaInmediata = "N";
						ordenes.IdTransaccion = resp.IdTransaccion;// "502"
						ordenes.IdPortafolio = resp.IdPortafolio;// 1451803
						ordenes.Cantidad = new BigDecimal(dataCliente.get(0).cantidad); // 5000
						ordenes.ValorRescate = resp.ValorRescateAsignacion;// 0
						ordenes.FolioCartera = resp.FolioCartera;
						ordenes.TasaVentaCorta = resp.TasaVentaCorta;
						ordenes.PlazoVentaCorta = resp.PlazoVentaCorta;
						ordenes.TipoCambio = resp.TipoCambio;
						ordenes.IdMonedaLiquidacionOrden = resp.IdMonedaLiquidacionOrden;
						ordenes.PagoCobroCuentaInversion = resp.PagoCobroCuentaInversion;
						ordenes.IdPreferenciaPagoPortafolio = resp.IdPreferenciaPagoPortafolio;
						ordenes.IdCuentaCustodioPortafolio = resp.IdCuentaCustodioPortafolio;
						ordenes.CodigoIFRS = resp.CodigoIFRS;
						IdPortafolioCobros.add(ordenes.IdPortafolio);
						IdTransaccionCobros.add(ordenes.IdTransaccion);
						if(contador < 1) {
						if(IdTransaccionSimulacion.equals(IdTransaccionCobros)) {
							if(IdPortafolioSimulacion.equals(IdPortafolioCobros)) {
								contador++;
						IdInternoSimulacion.add(ordenes.IdInterno);
						dataOrdenes.add(ordenes);
								}
							}
						}
						IdPortafolioCobros.clear();
						IdTransaccionCobros.clear();
						}
						IdPortafolioSimulacion.clear();
						for (Cobro_Salida respuesta : salidaCrearSimulacionAsignacion.P_OUT_COBROSASIGNACION) {
							CobroModificados_Entrada cobros = new CobroModificados_Entrada();
							IdInternoCobros.add(respuesta.IdInterno);
								if(IdInternoSimulacion.equals(IdInternoCobros)) {
									cobros.IdCobro = respuesta.IdCobro;
									cobros.IdInterno = respuesta.IdInterno;
									dataCobros.add(cobros);
								}
							IdInternoCobros.clear();
					}
						IdInternoSimulacion.clear();
						salidaCrearSimulacionAsignacion.P_OUT_COBROSASIGNACION.clear();
				}
		
					
					
						CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();

						CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
						entradaCrearOrdenesPostreras.p_IdEmpresa = in.p_IdEmpresa;
						entradaCrearOrdenesPostreras.p_IdUsuario = in.p_IdUsuario;
						entradaCrearOrdenesPostreras.p_IdModulo = "ORD";
						entradaCrearOrdenesPostreras.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
						entradaCrearOrdenesPostreras.p_CobrosModificados = dataCobros;
						entradaCrearOrdenesPostreras.p_Ordenes = dataOrdenes;

						CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);

						entradaCrearOrdenesPostreras.p_CobrosModificados.clear();
						for (Resultado_Salida resp : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
							Resultado resp2 = new Resultado();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_RESULTADOASIGNACION = salidaCrearOrdenesPostreras.P_OUT_RESULTADO;
						}
						for (MensajeSalida resp : salidaCrearOrdenesPostreras.P_OUT_MENSAJES) {
							MensajeSalida resp2 = new MensajeSalida();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_MENSAJES.add(resp2);
						}

						for (Resultado_Salida resp : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
							cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada Asignacion = new cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada();
							Asignacion.IdAsignacion = resp.IdAsignacion;
							Asignacion_Entrada envio = new Asignacion_Entrada();
							envio.IdAsignacion = resp.IdAsignacion;
							envio.GeneraComprobante = "N";
							dataEnvioFacturacion.add(envio);
							dataAnularAsignacion.add(Asignacion);
							
						}			

						dataCobros.clear();
						dataOrdenes.clear();
						dataAsignacionPortafolio.clear();
						dataEnvio.addAll(dataEnvioFacturacion);
						dataEnvioFacturacion.clear();
						
						if (MensajeSalida.getNumeroErrores(salidaCrearOrdenesPostreras.P_OUT_MENSAJES) > 0) {
							salida.P_OUT_MENSAJES.addAll(salidaCrearOrdenesPostreras.P_OUT_MENSAJES);
							if (!dataAnularAsignacion.isEmpty()) {
								AnularAsignaciones serviceAnularAsignaciones = new AnularAsignaciones();

								AnularAsignaciones_Entrada entradaAnularAsignaciones = new AnularAsignaciones_Entrada();

								entradaAnularAsignaciones.p_IdEmpresa = in.p_IdEmpresa;
								entradaAnularAsignaciones.p_IdUsuario = in.p_IdUsuario;
								entradaAnularAsignaciones.p_IdModulo = in.p_IdModulo;
								entradaAnularAsignaciones.p_AnulaMovimientosFacturar = "N";
								entradaAnularAsignaciones.asignacionesParaAnular = dataAnularAsignacion;
								AnularAsignaciones_Salida salidaAnularAsignaciones = serviceAnularAsignaciones.logica(conn,
										entradaAnularAsignaciones);

								for (OrdenAnulada resp : salidaAnularAsignaciones.p_out_OrdenesAnuladas) {
									Resultado resp2 = new Resultado();
									Utils.copyPojo(resp, resp2);
									salida.P_OUT_RESULTADO.add(resp2);
								}

								for (MensajeSalida resp : salidaAnularAsignaciones.p_out_Mensajes) {
									MensajeSalida resp2 = new MensajeSalida();
									Utils.copyPojo(resp, resp2);
									salida.P_OUT_MENSAJES.add(resp2);
								}
							}
							return salida;

						}else {
				
					
					if (!dataEnvio.isEmpty()) {
						EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();

						EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
						entradaEnviarAsignacionesFacturar.p_IdEmpresa = in.p_IdEmpresa;
						entradaEnviarAsignacionesFacturar.p_IdUsuario = in.p_IdUsuario;
						entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
						entradaEnviarAsignacionesFacturar.p_Asignaciones = dataEnvio;

						EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);

						for (Resultado resp : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
							Resultado resp2 = new Resultado();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_RESULTADO.add(resp2);
						}
						for (MensajeSalida resp : salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES) {
							MensajeSalida resp2 = new MensajeSalida();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_MENSAJES.add(resp2);
						}

					}
				}
				} else {

					/*
					 * por el contrario si el archivo salida transaccion viene con mas de 1 registro
					 * se generan 2 listas una de las transacciones y otra de los portafolios pero
					 * esta vez se asocian los portafolios segun la transaccion, si esta se repite
					 * mas de una vez con distintos clientes o portafolios
					 * 
					 */
					for (int i = 0; i < in.p_AsignacionMasiva.size(); i++) {
						cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada asigDataTransaccion = new cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada();
						cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada asigDataPortafolio = new cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada();
						if (String.valueOf(dataCliente.get(i).idTransaccion).equals(dataTransaccion.get(i).FolioTransaccion)) {
							IdTransaccionSimulacion.add(dataTransaccion.get(i).IdTransaccion);
							asigDataTransaccion.IdMovimiento = dataTransaccion.get(i).IdTransaccion;
							asigDataTransaccion.IdTransaccion = dataTransaccion.get(i).IdTransaccion;
							asigDataTransaccion.Cantidad = dataTransaccion.get(i).CantidadDisponibleTransaccion;
							asigDataTransaccion.Monto = dataTransaccion.get(i).MontoTransaccion;
							asigDataTransaccion.ValorRescate = dataTransaccion.get(i).ValorRescateTransaccion;
							dataAsignacionTransaccion.add(asigDataTransaccion);

							if (dataCliente.get(i).rut.equals(dataPortafolio.get(i).DocumentoIdentidad)) {
								IdPortafolioSimulacion.add(dataPortafolio.get(i).IdPortafolio);
								asigDataPortafolio.IdPortafolio = dataPortafolio.get(i).IdPortafolio;
								asigDataPortafolio.Cantidad = dataCliente.get(i).cantidadSimulacion;
								asigDataPortafolio.Porcentaje = BigDecimal.ZERO;
								dataAsignacionPortafolio.add(asigDataPortafolio);
							}


						/*
						 * proceso de llamado a la simulacion de asiganacion y asignar la transaccion a
						 * los distintos portafolios
						 * 
						 */

						CrearSimulacionAsignacion serviceCrearSimulacionAsignacion = new CrearSimulacionAsignacion();

						CrearSimulacionAsignacion_Entrada entradaCrearSimulacionAsignacion = new CrearSimulacionAsignacion_Entrada();

						entradaCrearSimulacionAsignacion.p_IdEmpresa = in.p_IdEmpresa;
						entradaCrearSimulacionAsignacion.p_IdUsuario = in.p_IdUsuario;
						entradaCrearSimulacionAsignacion.p_IdModulo = "ORD";
						entradaCrearSimulacionAsignacion.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
						entradaCrearSimulacionAsignacion.p_IdTipoAsignacion = "ASIGNACION_POSTRERA";
						entradaCrearSimulacionAsignacion.p_IdTipo = "NORMAL";
						entradaCrearSimulacionAsignacion.p_Origen = "ASIG";
						entradaCrearSimulacionAsignacion.p_Transacciones = dataAsignacionTransaccion;
						entradaCrearSimulacionAsignacion.p_Carteras = new ArrayList<>();
						entradaCrearSimulacionAsignacion.p_Ordenes = new ArrayList<>();
						entradaCrearSimulacionAsignacion.p_Portafolios = dataAsignacionPortafolio;

						CrearSimulacionAsignacion_Salida salidaCrearSimulacionAsignacion = serviceCrearSimulacionAsignacion.logica(conn, entradaCrearSimulacionAsignacion);

						int contador = 0;
						for (Simulacion_Salida resp : salidaCrearSimulacionAsignacion.P_OUT_SIMASIGNACION) {
							
						OrdenPostrera_Entrada ordenes = new OrdenPostrera_Entrada();
						ordenes.IdInterno = resp.IdInterno;
						ordenes.FechaRecepcion = resp.FechaIngresoRegistro.toString();
						ordenes.Glosa = resp.Glosa;
						ordenes.IdBoveda = resp.IdBoveda;
						ordenes.IdFormaCustodia = resp.IdFormaCustodia;
						ordenes.EsFacturaInmediata = "N";
						ordenes.IdTransaccion = resp.IdTransaccion;// "502"
						ordenes.IdPortafolio = resp.IdPortafolio;// 1451803
						ordenes.Cantidad = new BigDecimal(dataCliente.get(0).cantidad); // 5000
						ordenes.ValorRescate = resp.ValorRescateAsignacion;// 0
						ordenes.FolioCartera = resp.FolioCartera;
						ordenes.TasaVentaCorta = resp.TasaVentaCorta;
						ordenes.PlazoVentaCorta = resp.PlazoVentaCorta;
						ordenes.TipoCambio = resp.TipoCambio;
						ordenes.IdMonedaLiquidacionOrden = resp.IdMonedaLiquidacionOrden;
						ordenes.PagoCobroCuentaInversion = resp.PagoCobroCuentaInversion;
						ordenes.IdPreferenciaPagoPortafolio = resp.IdPreferenciaPagoPortafolio;
						ordenes.IdCuentaCustodioPortafolio = resp.IdCuentaCustodioPortafolio;
						ordenes.CodigoIFRS = resp.CodigoIFRS;
						IdPortafolioCobros.add(ordenes.IdPortafolio);
						IdTransaccionCobros.add(ordenes.IdTransaccion);
							if (IdTransaccionSimulacion.equals(IdTransaccionCobros)) {
								if (IdPortafolioSimulacion.equals(IdPortafolioCobros)) {
									if (contador < 1) {
										IdInternoSimulacion.add(ordenes.IdInterno);
										contador++;
										dataOrdenes.add(ordenes);
									}
								}
							}
						IdPortafolioCobros.clear();
						IdTransaccionCobros.clear();
						}
						for (Cobro_Salida respuesta : salidaCrearSimulacionAsignacion.P_OUT_COBROSASIGNACION) {
							CobroModificados_Entrada cobros = new CobroModificados_Entrada();
							IdInternoCobros.add(respuesta.IdInterno);
								if(IdInternoSimulacion.equals(IdInternoCobros)) {
									cobros.IdCobro = respuesta.IdCobro;
									cobros.IdInterno = respuesta.IdInterno;
									dataCobros.add(cobros);
								}
							IdInternoCobros.clear();
					}
						IdTransaccionSimulacion.clear();
						
						IdPortafolioSimulacion.clear();
						IdInternoSimulacion.clear();
				}
						
			}		
						CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();

						CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
						entradaCrearOrdenesPostreras.p_IdEmpresa = in.p_IdEmpresa;
						entradaCrearOrdenesPostreras.p_IdUsuario = in.p_IdUsuario;
						entradaCrearOrdenesPostreras.p_IdModulo = "ORD";
						entradaCrearOrdenesPostreras.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
						entradaCrearOrdenesPostreras.p_CobrosModificados = dataCobros;
						entradaCrearOrdenesPostreras.p_Ordenes = dataOrdenes;

						CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
						for (Resultado_Salida resp : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
							Resultado resp2 = new Resultado();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_RESULTADO.add(resp2);
						}
						
						for (MensajeSalida resp : salidaCrearOrdenesPostreras.P_OUT_MENSAJES) {
							MensajeSalida resp2 = new MensajeSalida();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_MENSAJES.add(resp2);
						}
					
						for (Resultado_Salida resp : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
							Asignacion_Entrada envio = new Asignacion_Entrada();
							cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada Asignacion = new cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada();
							Asignacion.IdAsignacion = resp.IdAsignacion;
							envio.IdAsignacion = resp.IdAsignacion;
							envio.GeneraComprobante = "N";
							dataEnvioFacturacion.add(envio);
							dataAnularAsignacion.add(Asignacion);
						}
						dataCobros.clear();
						dataOrdenes.clear();
						dataAsignacionPortafolio.clear();
			
						
				if (MensajeSalida.getNumeroErrores(salidaCrearOrdenesPostreras.P_OUT_MENSAJES) > 0) {
					salida.P_OUT_MENSAJES.addAll(salidaCrearOrdenesPostreras.P_OUT_MENSAJES);
					if (!dataAnularAsignacion.isEmpty()) {
						AnularAsignaciones serviceAnularAsignaciones = new AnularAsignaciones();

						AnularAsignaciones_Entrada entradaAnularAsignaciones = new AnularAsignaciones_Entrada();

						entradaAnularAsignaciones.p_IdEmpresa = in.p_IdEmpresa;
						entradaAnularAsignaciones.p_IdUsuario = in.p_IdUsuario;
						entradaAnularAsignaciones.p_IdModulo = in.p_IdModulo;
						entradaAnularAsignaciones.p_AnulaMovimientosFacturar = "N";
						entradaAnularAsignaciones.asignacionesParaAnular = dataAnularAsignacion;
						AnularAsignaciones_Salida salidaAnularAsignaciones = serviceAnularAsignaciones.logica(conn,
								entradaAnularAsignaciones);

						for (OrdenAnulada resp : salidaAnularAsignaciones.p_out_OrdenesAnuladas) {
							Resultado resp2 = new Resultado();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_RESULTADO.add(resp2);
						}

						for (MensajeSalida resp : salidaAnularAsignaciones.p_out_Mensajes) {
							MensajeSalida resp2 = new MensajeSalida();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_MENSAJES.add(resp2);
						}
					}
					return salida;

				}
				dataAsignacionTransaccion.clear();
			}
				
				

				
						dataEnvio.addAll(dataEnvioFacturacion);
						dataEnvioFacturacion.clear();
						
				}
					
					if (!dataEnvio.isEmpty()) {
						EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();

						EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
						entradaEnviarAsignacionesFacturar.p_IdEmpresa = in.p_IdEmpresa;
						entradaEnviarAsignacionesFacturar.p_IdUsuario = in.p_IdUsuario;
						entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
						entradaEnviarAsignacionesFacturar.p_Asignaciones = dataEnvio;

						EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);

						for (Resultado resp : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
							Resultado resp2 = new Resultado();
							Utils.copyPojo(resp, resp2);
							salida.P_OUT_RESULTADO.add(resp2);
							salida.P_OUT_MENSAJES = salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES;
						}

					}
					
					log.write(salida);
					
					return salida;
				}


//		}
//	}

	@Override
	public JsonObject call(Connection con, JsonObject Jin) throws Exception {
		Gson gson = getGsonBuilder();
		ArchivoEntrada in = gson.fromJson(Jin.toString(), ArchivoEntrada.class);

		AsignacionMasiva_Salida salida = logica(con, in);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();

		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1611,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoAsignacion\":\"ASIGNACION_POSTRERA\",\"p_IdTipo\":\"NORMAL\",\"p_Origen\":\"ASIG\",\"p_AsignacionMasiva\":[{\"folioTransaccion\":1001,\"rutCliente\":\"14961893-7\",\"codigoPortafolio\":1,\"cantidadAsignar\":5},{\"folioTransaccion\":1002,\"rutCliente\":\"14961893-7\",\"codigoPortafolio\":2,\"cantidadAsignar\":3},{\"folioTransaccion\":1003,\"rutCliente\":\"14340270-3\",\"codigoPortafolio\":69,\"cantidadAsignar\":9},{\"folioTransaccion\":1004,\"rutCliente\":\"4444-K\",\"codigoPortafolio\":1,\"cantidadAsignar\":10},{\"folioTransaccion\":1005,\"rutCliente\":\"17919279-9\",\"codigoPortafolio\":1,\"cantidadAsignar\":8}]}";
			ArchivoEntrada Objin = gson.fromJson(json, ArchivoEntrada.class);
			ValidacionArchivoCalce demo = new ValidacionArchivoCalce();
			System.out.println(Objin.toString());
			System.out.println("ENTRADA:");


			try {
				AsignacionMasiva_Salida ObjOut = demo.logica(con, Objin);
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				con.rollback();
//				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
//				con.commit();
			}

		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
