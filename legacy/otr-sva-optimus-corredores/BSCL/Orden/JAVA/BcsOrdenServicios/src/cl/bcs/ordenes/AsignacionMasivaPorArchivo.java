package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
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
import cl.bcs.ordenes.AnularAsignaciones.OrdenAnulada;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Entrada;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Salida;
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
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AsignacionMasivaPorArchivo implements JavaCall {

	private static final Logger logger = LogManager.getLogger(AsignacionMasivaPorArchivo.class);
	
	private final String SQL_INSERT_GBL_TRANSACCIONES 	= loadResourceToString("cl/bcs/ordenes/sql/AsignacionMasivaPorArchivo_InsertGBLTransaccion.sql");
	private final String SQL_SELECT_TRANSACCIONES 		= loadResourceToString("cl/bcs/ordenes/sql/AsignacionMasivaPorArchivo_SelectTransaccion.sql");
	private final String SQL_SELECT_PORTAFOLIO 			= loadResourceToString("cl/bcs/ordenes/sql/AsignacionMasivaPorArchivo_SelectPortafolio.sql");
	private final String SQL_SELECT_GBL_TRANSACCIONES 	= "SELECT CAST(\"FolioTransaccion\" AS BIGINT) AS \"FolioTransaccion\", \"FechaTransaccion\", \"Nemotecnico\", \"RutCliente\", CAST(\"CodigoPortafolio\" AS BIGINT) AS \"CodigoPortafolio\", \"CantidadAsignar\" FROM \"BSCL.Orden.BD::GBL.AsignacionMasivaPorArchivo_Entrada\""; // para reviciones
	private final String SQL_DELETE_GBL_TRANSACCIONES 	= "DELETE FROM \"BSCL.Orden.BD::GBL.AsignacionMasivaPorArchivo_Entrada\"";
	

	public static class ArchivoEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoAsignacion;
		public String p_IdTipo;
		public String p_Origen;
		public List<AsignacionMasiva> p_AsignacionMasiva;
		
		public String toString() {
			return Utils.printJson(this, false);
		};

	}

	public static class AsignacionMasiva {
		public Long FolioTransaccion;
		
		public String FechaTransaccion;
		public String Nemotecnico;
		
		
		public String RutCliente;
		public Long CodigoPortafolio;
		public BigDecimal CantidadAsignar;
		
		public Long getFolioTransaccion() {
			return FolioTransaccion;
		}


		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class AsignacionMasivaSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Resultado> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_ENVIOFACTURACION;

		public AsignacionMasivaSalida() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado>();
			P_OUT_ENVIOFACTURACION = new ArrayList<MensajeSalida>();

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

	public static class TransaccionEntrada {
		public String FolioTransaccion;
		public Long IdTransaccion;
		public BigDecimal CantidadDisponibleTransaccion;
		public BigDecimal MontoTransaccion;
		public BigDecimal ValorRescateTransaccion;
		
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
		public BigDecimal Cantidad;
		public BigDecimal Porcentaje;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}



	@Trace
	public AsignacionMasivaSalida logica(Connection conn, ArchivoEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.debug("Inicio servicio AsignacionMasivaSalida");
		
		SQL sql = new SQL();
		List<Long> IdPortafolioCobros = new ArrayList<>();
		List<Long> IdTransaccionCobros = new ArrayList<>();
		List<Long> IdPortafolioSimulacion = new ArrayList<>();
		List<Long> IdTransaccionSimulacion = new ArrayList<>();
		List<Long> IdInternoSimulacion = new ArrayList<>();
		List<Long> IdInternoCobros = new ArrayList<>();
		List<Long> IdTransaccion = new ArrayList<>();
		List<DataCliente> dataCliente = new ArrayList<>();
		List<DatosTransaccionSalida> dataTransaccion = new ArrayList<>();
		List<DatosClienteSalida> dataPortafolio = new ArrayList<>();
		List<OrdenPostrera_Entrada> dataOrdenes = new ArrayList<>();
		List<CobroModificados_Entrada> dataCobros = new ArrayList<>();
		List<Asignacion_Entrada> dataEnvio = new ArrayList<>();
		List<Transaccion_Entrada> dataAsignacionTransaccion = new ArrayList<>();
		List<cl.bcs.ordenes.CrearSimulacionAsignacion.Portafolio_Entrada> dataAsignacionPortafolio = new ArrayList<>();
		List<cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada> dataAnularAsignacion = new ArrayList<>();
		AsignacionMasivaSalida salida = new AsignacionMasivaSalida();

		// Inserta entrada en tabla global
		sql.batch(conn, SQL_INSERT_GBL_TRANSACCIONES, in.p_AsignacionMasiva, AsignacionMasiva.class);
		

		//NOTA : Habilitar Solo para ver contenido de tabla global
		//List<AsignacionMasiva> contenidoGlobalEntrada = sql.query(conn, SQL_SELECT_GBL_TRANSACCIONES, new HashMap<String, Object>(), AsignacionMasiva.class);
		
		
		
		
		
		HashMap<String, Object> params = new HashMap<>();		
		params.put("Marca", "");
		params.put("IdEmpresa", in.p_IdEmpresa);
		List<TransaccionEntrada> salidaTransaccion = sql.query(conn, SQL_SELECT_TRANSACCIONES, params, TransaccionEntrada.class);
		
		
		
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);		
		List<DatosClienteSalida> salidaPortafolio = sql.query(conn, SQL_SELECT_PORTAFOLIO, params, DatosClienteSalida.class);
		
		
		
		if (salidaTransaccion.isEmpty()) {
			
			String codigo = "FOLIO_TRANSACCION_VACIO";
			Long msj = in.p_IdEmpresa;
			String desc = Diccionario.getMensaje(conn, msj, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
		
			return salida;
		}
		
		if (salidaPortafolio.isEmpty()) {
			
			String codigo = "PORTAFOLIO_ASIGNACION_VACIO";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			return salida;
			
		}
		
		Boolean existeTransaccion;
		Boolean existeClientePortafolio;
		
		for (int i = 0; i < in.p_AsignacionMasiva.size(); i++) {
			
			DataCliente data = new DataCliente();
			IdTransaccion.add(in.p_AsignacionMasiva.get(i).FolioTransaccion);
			data.idTransaccion = in.p_AsignacionMasiva.get(i).FolioTransaccion;
			data.rut = in.p_AsignacionMasiva.get(i).RutCliente;
			data.codPortafolio = in.p_AsignacionMasiva.get(i).CodigoPortafolio;
			data.cantidad = in.p_AsignacionMasiva.get(i).CantidadAsignar.longValue();
			data.cantidadSimulacion = new BigDecimal(data.cantidad);
			dataCliente.add(data);
			
			existeTransaccion = false;
			DatosTransaccionSalida salidaTransaccionFinal = new DatosTransaccionSalida();
			for (int x = 0; x < salidaTransaccion.size(); x++) {
				
				if (dataCliente.get(i).idTransaccion.toString().equals(salidaTransaccion.get(x).FolioTransaccion)) {
					salidaTransaccionFinal.IdTransaccion = salidaTransaccion.get(x).IdTransaccion;
					salidaTransaccionFinal.CantidadDisponibleTransaccion = salidaTransaccion.get(x).CantidadDisponibleTransaccion;
					salidaTransaccionFinal.FolioTransaccion = salidaTransaccion.get(x).FolioTransaccion;
					salidaTransaccionFinal.MontoTransaccion = salidaTransaccion.get(x).MontoTransaccion;
					salidaTransaccionFinal.ValorRescateTransaccion = salidaTransaccion.get(x).ValorRescateTransaccion;
					dataTransaccion.add(salidaTransaccionFinal);
					existeTransaccion = true;
				}				
			}
			
			if (!existeTransaccion) 
			{	
				String mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "TRANSACCION_NO_EXISTE").add("IdTransaccion", in.p_AsignacionMasiva.get(i).FolioTransaccion).msg();				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_NO_EXISTE",mensaje));
				return salida;
			}
				
			existeClientePortafolio = false;
			DatosClienteSalida salidaPortafolioFinal = new DatosClienteSalida();
			for (int x = 0; x < salidaPortafolio.size(); x++) {
				if (dataCliente.get(i).codPortafolio.toString().equals(salidaPortafolio.get(x).CodigoPortafolio)) {
					if (dataCliente.get(i).rut.equals(salidaPortafolio.get(x).DocumentoIdentidad)) {
						salidaPortafolioFinal.CodigoPortafolio = salidaPortafolio.get(x).CodigoPortafolio;
						salidaPortafolioFinal.DocumentoIdentidad = salidaPortafolio.get(x).DocumentoIdentidad;
						salidaPortafolioFinal.IdPortafolio = salidaPortafolio.get(x).IdPortafolio;
						dataPortafolio.add(salidaPortafolioFinal);
						existeClientePortafolio = true;
					}
				}
			}

			if (!existeClientePortafolio)
			{			
				String mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "CLIENTE_PORTAFOLIO_NO_EXISTE").add("NombreCliente", in.p_AsignacionMasiva.get(i).RutCliente).msg();				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CLIENTE_PORTAFOLIO_NO_EXISTE",mensaje));
				return salida;
			}
			
		}

		
		for (int i = 0; i < salidaTransaccion.size(); i++) {
			
			long cantidad_total = 0;

			for (int c = 0; c < dataCliente.size(); c++) {
				if (dataCliente.get(c).idTransaccion.toString().equals(salidaTransaccion.get(i).FolioTransaccion)) {
					cantidad_total += dataCliente.get(c).cantidad;
				}

			}
			if (cantidad_total <= (salidaTransaccion.get(i).CantidadDisponibleTransaccion.longValue())) {

			} else {
				String codigo = "CANTIDAD_TRAN_INSUFICIENTE";
				String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
				return salida;
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
			
			for (TransaccionEntrada trans : salidaTransaccion) {
				
				Transaccion_Entrada asigDataTransaccion = new Transaccion_Entrada();
					IdTransaccionSimulacion.add(trans.IdTransaccion);
					asigDataTransaccion.IdMovimiento = trans.IdTransaccion;
					asigDataTransaccion.IdTransaccion = trans.IdTransaccion;
					asigDataTransaccion.Cantidad = trans.CantidadDisponibleTransaccion;
					asigDataTransaccion.Monto = trans.MontoTransaccion;
					asigDataTransaccion.ValorRescate = trans.ValorRescateTransaccion;
					dataAsignacionTransaccion.add(asigDataTransaccion);

			}
			
			for (int x = 0; x < salidaPortafolio.size(); x++) {
				
				Portafolio_Entrada asigDataPortafolio = new Portafolio_Entrada();
				IdPortafolioSimulacion.add(salidaPortafolio.get(x).IdPortafolio);
				asigDataPortafolio.IdPortafolio = salidaPortafolio.get(x).IdPortafolio;
				asigDataPortafolio.Cantidad = BigDecimal.ZERO;
				asigDataPortafolio.Porcentaje = BigDecimal.ZERO;
				dataAsignacionPortafolio.add(asigDataPortafolio);							


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

				dataAsignacionPortafolio.clear();
				int contador = 0;
				for (Simulacion_Salida resp : salidaCrearSimulacionAsignacion.P_OUT_SIMASIGNACION) {
					
					OrdenPostrera_Entrada ordenes = new OrdenPostrera_Entrada();
					List<DataCliente> result = dataCliente.stream()
						     .filter(item -> item.idTransaccion.toString().equals(resp.FolioTransaccion) && item.rut.toString().equals(resp.DocumentoIdentidad) && item.codPortafolio.toString().equals(resp.CodigoPortafolio))
						     .collect(Collectors.toList());	
					
					ordenes.IdInterno = resp.IdInterno;
					ordenes.FechaRecepcion = resp.FechaIngresoRegistro.toString();
					ordenes.Glosa = resp.Glosa;
					ordenes.IdBoveda = resp.IdBoveda;
					ordenes.IdFormaCustodia = resp.IdFormaCustodia;
					ordenes.EsFacturaInmediata = "N";
					ordenes.IdTransaccion = resp.IdTransaccion;// "502"
					ordenes.IdPortafolio = resp.IdPortafolio;// 1451803
					ordenes.Cantidad = new BigDecimal(result.get(0).cantidad);
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
				salida.P_OUT_RESULTADO.add(resp2);
				
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
				dataEnvio.add(envio);
				dataAnularAsignacion.add(Asignacion);

			}

			salidaCrearOrdenesPostreras.P_OUT_RESULTADO.clear();
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
			
			if (!dataEnvio.isEmpty()) {
				
				EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();

				EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
				entradaEnviarAsignacionesFacturar.p_IdEmpresa = in.p_IdEmpresa;
				entradaEnviarAsignacionesFacturar.p_IdUsuario = in.p_IdUsuario;
				entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
				entradaEnviarAsignacionesFacturar.p_Asignaciones = dataEnvio;

				EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar
						.logica(conn, entradaEnviarAsignacionesFacturar);

				for (Resultado resp : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
					Resultado resp2 = new Resultado();
					Utils.copyPojo(resp, resp2);
					salida.P_OUT_RESULTADO.add(resp2);
					salida.P_OUT_MENSAJES = salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES;
				}
				salida.P_OUT_RESULTADO = salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO;
				salida.P_OUT_MENSAJES = salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES;
				salida.P_OUT_ENVIOFACTURACION.addAll(salida.P_OUT_MENSAJES);
			}
			
		}
		/*
		 * por el contrario si el archivo salida transaccion viene con mas de 1 registro
		 * se generan 2 listas una de las transacciones y otra de los portafolios pero
		 * esta vez se asocian los portafolios segun la transaccion, si esta se repite
		 * mas de una vez con distintos clientes o portafolios
		 * 
		 */
		else {
			
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
					dataAsignacionPortafolio.clear();
					dataAsignacionTransaccion.clear();
					
					int contador = 0;
					for (Simulacion_Salida respuesta : salidaCrearSimulacionAsignacion.P_OUT_SIMASIGNACION) {

						OrdenPostrera_Entrada ordenes = new OrdenPostrera_Entrada();
						List<DataCliente> result = dataCliente.stream()
							     .filter(item -> item.idTransaccion.toString().equals(respuesta.FolioTransaccion) && item.rut.toString().equals(respuesta.DocumentoIdentidad) && item.codPortafolio.toString().equals(respuesta.CodigoPortafolio))
							     .collect(Collectors.toList());
						
						if (result.isEmpty())
						{
							continue;
						}
						ordenes.IdInterno = respuesta.IdInterno;
						ordenes.FechaRecepcion = respuesta.FechaIngresoRegistro.toString();
						ordenes.Glosa = respuesta.Glosa;
						ordenes.IdBoveda = respuesta.IdBoveda;
						ordenes.IdFormaCustodia = respuesta.IdFormaCustodia;
						ordenes.EsFacturaInmediata = "N";
						ordenes.IdTransaccion = respuesta.IdTransaccion;// "502"
						ordenes.IdPortafolio = respuesta.IdPortafolio;// 1451803
						ordenes.Cantidad = new BigDecimal(result.get(0).cantidad);
						ordenes.ValorRescate = respuesta.ValorRescateAsignacion;// 0
						ordenes.FolioCartera = respuesta.FolioCartera;
						ordenes.TasaVentaCorta = respuesta.TasaVentaCorta;
						ordenes.PlazoVentaCorta = respuesta.PlazoVentaCorta;
						ordenes.TipoCambio = respuesta.TipoCambio;
						ordenes.IdMonedaLiquidacionOrden = respuesta.IdMonedaLiquidacionOrden;
						ordenes.PagoCobroCuentaInversion = respuesta.PagoCobroCuentaInversion;
						ordenes.IdPreferenciaPagoPortafolio = respuesta.IdPreferenciaPagoPortafolio;
						ordenes.IdCuentaCustodioPortafolio = respuesta.IdCuentaCustodioPortafolio;
						ordenes.CodigoIFRS = respuesta.CodigoIFRS;
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
			entradaCrearOrdenesPostreras.p_CobrosModificados.clear();
			for (Resultado_Salida respuesta : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
				
				Resultado resp2 = new Resultado();
				Utils.copyPojo(respuesta, resp2);
				salida.P_OUT_RESULTADO.add(resp2);
				
			}
			
			for (MensajeSalida resp : salidaCrearOrdenesPostreras.P_OUT_MENSAJES) {
				
				MensajeSalida resp2 = new MensajeSalida();
				Utils.copyPojo(resp, resp2);
				salida.P_OUT_MENSAJES.add(resp2);
				
			}

			for (Resultado_Salida respuesta : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
				
				cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada Asignacion = new cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada();
				Asignacion_Entrada envio = new Asignacion_Entrada();
				envio.IdAsignacion = respuesta.IdAsignacion;
				envio.GeneraComprobante = "N";
				dataEnvio.add(envio);
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
			
			if (!dataEnvio.isEmpty()) {

				EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();

				EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
				entradaEnviarAsignacionesFacturar.p_IdEmpresa = in.p_IdEmpresa;
				entradaEnviarAsignacionesFacturar.p_IdUsuario = in.p_IdUsuario;
				entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
				entradaEnviarAsignacionesFacturar.p_Asignaciones = dataEnvio;

				EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = serviceEnviarAsignacionesFacturar
						.logica(conn, entradaEnviarAsignacionesFacturar);

				for (Resultado resp : salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO) {
					Resultado resp2 = new Resultado();
					Utils.copyPojo(resp, resp2);
					salida.P_OUT_RESULTADO.add(resp2);
					salida.P_OUT_MENSAJES = salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES;
				}
				salida.P_OUT_RESULTADO = salidaEnviarAsignacionesFacturar.P_OUT_RESULTADO;
				salida.P_OUT_MENSAJES = salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES;
				salida.P_OUT_ENVIOFACTURACION.addAll(salida.P_OUT_MENSAJES);
			}
				
		}
		
		
		// Limpia tablas temporales
		SQL.exec(conn, SQL_DELETE_GBL_TRANSACCIONES);
		
		
		logger.debug("Fin servicio AsignacionMasivaSalida:" + (System.currentTimeMillis() - init));
		return salida;		
		
	}



	@Override
	public JsonObject call(Connection con, JsonObject Jin) throws Exception {
		Gson gson = getGsonBuilder();
		ArchivoEntrada in = gson.fromJson(Jin.toString(), ArchivoEntrada.class);
		AsignacionMasivaSalida salida = logica(con, in);

		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();

		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoAsignacion\":\"ASIGNACION_POSTRERA\",\"p_IdTipo\":\"NORMAL\",\"p_Origen\":\"ASIG\",\"p_AsignacionMasiva\":[{\"folioTransaccion\":999456,\"rutCliente\":\"15983577-4\",\"codigoPortafolio\":1,\"cantidadAsignar\":3000},{\"folioTransaccion\":999456,\"rutCliente\":\"15298053-1\",\"codigoPortafolio\":1,\"cantidadAsignar\":2000}]}";			
			ArchivoEntrada Objin = gson.fromJson(json, ArchivoEntrada.class);
			AsignacionMasivaPorArchivo demo = new AsignacionMasivaPorArchivo();
			System.out.println(Objin.toString());
			System.out.println("ENTRADA:");
			con.rollback();

			try {
				AsignacionMasivaSalida ObjOut = demo.logica(con, Objin);
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());


				con.rollback();				
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
//				con.commit();
			}

// 			}
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
