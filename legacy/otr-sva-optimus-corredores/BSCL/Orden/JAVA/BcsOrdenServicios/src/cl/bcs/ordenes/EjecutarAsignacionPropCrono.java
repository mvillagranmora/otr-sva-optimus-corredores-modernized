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

import cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono.ConsultarInformacionAsignacionPropCrono_Entrada;
import cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono.ConsultarInformacionAsignacionPropCrono_Salida;
import cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono.Transaccion_Salida;
import cl.bcs.ordenes.CrearAsignaciones.Asignacion_Entrada;
import cl.bcs.ordenes.CrearAsignaciones.CrearAsignaciones_Entrada;
import cl.bcs.ordenes.CrearAsignaciones.CrearAsignaciones_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.CrearSimulacionAsignacion_Entrada;
import cl.bcs.ordenes.CrearSimulacionAsignacion.CrearSimulacionAsignacion_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Orden_Entrada;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Simulacion_Salida;
import cl.bcs.ordenes.CrearSimulacionAsignacion.Transaccion_Entrada;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class EjecutarAsignacionPropCrono implements JavaCall,JavaCallMetadata {

	private final Logger log = LogManager.getLogger(EjecutarAsignacionPropCrono.class);
	private final String SQL_UPDATE_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/EjecutarAsignacionPropCrono_UpdateTransacciones.sql") ;

	private static final String REGEX_MARCA_ASIGNACION = "^[a-zA-Z0-9_]+$";

	public static class EjecutarAsignacionPropCrono_Entrada {

		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion = "N";
		public String p_UsaReglasNegocio = "S";
		public String p_IdTipoAsignacion;
		
		public String p_HoraHasta;
		public Long p_IdLibroOrdenes;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdCondicionDeLiquidacion;
		public String p_IdTipoInstrumento;
		public String p_IdAccionOrden;
		public String p_IdBolsa;
		public String p_MarcaAsignacion;
		public Long p_IdInstrumento;
		public String p_IdMonedaNegociacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Salida {
		public Long IdTransaccion;
		public BigDecimal CantidadDisponibleTransaccion;
		public BigDecimal ValorRescateDisponibleTransaccion;
		public BigDecimal PrecioTransaccion;
		public java.sql.Timestamp FechaOperacionTransaccion;
		public Long IdLibroOrdenes;
		public String IdClasificacionTipoInstrumento;
		public String IdCondicionDeLiquidacion;
		public String IdTipoInstrumento;
		public String AccionOrden;
		public String IdBolsa;
		public String FolioTransaccion;
		public String IdMonedaInstrumento;
		public String NombreLibroOrdenes;
		public String Instrumento;
	}
	
	public static class Orden_Salida {
		public Long IdOrden;
		public java.sql.Timestamp FechaOperacionOrden;
		public Long IdLibroOrdenes;
		public String IdClasificacionTipoInstrumento;
		public String IdCondicionDeLiquidacion;
		public String IdTipoInstrumento;
		public String AccionOrden;
		public String IdBolsa;
		public String TipoPrecio;
		public BigDecimal CantidadDisponibleOrden;
		public BigDecimal PrecioOrden;
		public String NombreLibroOrdenes;
		public String IdMonedaOrden;
		public String Instrumento;
		
	}
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdOrden;
		public Long IdTransaccion;
		public String FolioTransaccion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		
	}
	
	public static class Cobro_Salida {
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdCobroAsignacion;
		public Long CodigoCobroClasificacion;
		public Long IdCobro;
		public Long IdMonedaCobro;
		public Long ValorCobroPerfil;
		public Long FormaCobro;
		public Long MontoMinimo;
		public Long ValorCobroAplicado;
		public Long MontoCobroCalculado;
		public Long MontoCobroCalculadoLocal;
		public Long MontoCobroCalculadoLiquidacion;
	}
	
	public static class EjecutarAsignacionPropCrono_Salida {
		
		public List<Transaccion_Salida> P_OUT_TRANSACCIONES;
		public List<Orden_Salida> P_OUT_ORDENES;
		public List<Cobro_Salida> P_OUT_COBROS;
		public List<Resultado_Salida> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public EjecutarAsignacionPropCrono_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_TRANSACCIONES = new ArrayList<>();
			P_OUT_ORDENES = new ArrayList<>();
			P_OUT_COBROS = new ArrayList<>();
			P_OUT_RESULTADO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateMarcaAsignacion {
		public String MarcaAsig;
		public Long IdTransaccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	@Trace
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EjecutarAsignacionPropCrono_Entrada in = gson.fromJson(jin.toString(), EjecutarAsignacionPropCrono_Entrada.class);
		EjecutarAsignacionPropCrono_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = 0L; //Commit forzado por parcialidad
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		setMetadata(in,out);
		return dataout;
	}

	@Override
	public void setMetadata(Object in,Object out) {
		EjecutarAsignacionPropCrono_Salida s = (EjecutarAsignacionPropCrono_Salida)out;
		metadata[0] = s.P_OUT_RESULTADO.size()+"";
	}
	
	@Trace
	public EjecutarAsignacionPropCrono_Salida logica(Connection con, EjecutarAsignacionPropCrono_Entrada in) throws Exception {
		log.info("INICIO");
		EjecutarAsignacionPropCrono_Salida out = new EjecutarAsignacionPropCrono_Salida();
		SQL sql = new SQL();
		
		if (!in.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_CRONOLOGICA") && !in.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_PROPORCIONAL")) {
			String codigo = "TIPO_ASIGNACION_INCORRECTA";
			String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		ConsultarInformacionAsignacionPropCrono_Entrada serviceEntrada1 = new ConsultarInformacionAsignacionPropCrono_Entrada();
		serviceEntrada1.p_IdUsuario = in.p_IdUsuario;
		serviceEntrada1.p_IdModulo = in.p_IdModulo;
		serviceEntrada1.p_HoraHasta = in.p_HoraHasta;
		serviceEntrada1.p_IdAccionOrden = in.p_IdAccionOrden;
		serviceEntrada1.p_IdBolsa = in.p_IdBolsa;
		serviceEntrada1.p_IdClasificacionTipoInstrumento = in.p_IdClasificacionTipoInstrumento;
		serviceEntrada1.p_IdCondicionDeLiquidacion = in.p_IdCondicionDeLiquidacion;
		serviceEntrada1.p_IdEmpresa = in.p_IdEmpresa;
		serviceEntrada1.p_IdLibroOrdenes = in.p_IdLibroOrdenes;
		serviceEntrada1.p_IdTipoInstrumento = in.p_IdTipoInstrumento;
		serviceEntrada1.p_MarcaAsignacion = in.p_MarcaAsignacion;
		serviceEntrada1.p_IdInstrumento = in.p_IdInstrumento;
		serviceEntrada1.p_IdMonedaNegociacion = in.p_IdMonedaNegociacion;
		ConsultarInformacionAsignacionPropCrono serviceConsultar = new ConsultarInformacionAsignacionPropCrono();
		ConsultarInformacionAsignacionPropCrono_Salida serviceSalida1 = serviceConsultar.logica(con, serviceEntrada1);
		
		if (serviceSalida1.P_OUT_TRANSACCIONES.size() == 0) {
			String codigo = "NO_SE_ENCONTRARON_TRANSACCIONES_CON_SALDO_POR_ASIGNAR";
			String desc = Diccionario.getMensaje(con, in.p_IdEmpresa,codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		if (serviceSalida1.P_OUT_ORDENES.size() == 0) {
			String codigo = "NO_SE_ENCONTRARON_ORDENES_CON_SALDO_POR_ASIGNAR";
			String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		List<Orden_Entrada> ordenesPorAsignar = new ArrayList<>();
		List<Transaccion_Entrada> transaccionesPorAsignar = new ArrayList<>();
		for (cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono.Orden_Salida ord : serviceSalida1.P_OUT_ORDENES) {
			Orden_Entrada ordEntrada = new Orden_Entrada();
			ordEntrada.IdOrden = ord.IdOrden;
			ordenesPorAsignar.add(ordEntrada);
		}
		
		for (cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono.Transaccion_Salida tran : serviceSalida1.P_OUT_TRANSACCIONES) {
			//Filtra Transacciones reserevadas 
			if("S".equalsIgnoreCase(tran.Reservado)) {
				continue;
			}
			Transaccion_Entrada tranEntrada = new Transaccion_Entrada();
			tranEntrada.Cantidad = tran.CantidadDisponibleTransaccion;
			tranEntrada.IdMovimiento = tran.IdTransaccion;
			tranEntrada.IdTransaccion = tran.IdTransaccion;
			tranEntrada.Monto = BigDecimal.ZERO;
			tranEntrada.ValorRescate = BigDecimal.ZERO;
			transaccionesPorAsignar.add(tranEntrada);
		}
		
		//SIMULAR
		CrearSimulacionAsignacion_Entrada entradaCrearSimulacion = new CrearSimulacionAsignacion_Entrada();
		entradaCrearSimulacion.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearSimulacion.p_IdModulo = "ORD";
		entradaCrearSimulacion.p_IdTipo = "ASIG";
		entradaCrearSimulacion.p_IdUsuario = in.p_IdUsuario;
		entradaCrearSimulacion.p_IdTipoAsignacion  = in.p_IdTipoAsignacion;
		entradaCrearSimulacion.p_Ordenes = ordenesPorAsignar ;
		entradaCrearSimulacion.p_Transacciones = transaccionesPorAsignar ;
		entradaCrearSimulacion.p_Origen = "";
		entradaCrearSimulacion.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		
		CrearSimulacionAsignacion serviceCrearSimulacion =  new CrearSimulacionAsignacion();
		CrearSimulacionAsignacion_Salida salidaCrearSimulacion = serviceCrearSimulacion.logica(con, entradaCrearSimulacion);
		
		if (salidaCrearSimulacion.P_OUT_SIMASIGNACION.size() == 0) {
			String codigo = "NO_SE_GENERARON_ASIGNACIONES";
			String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
			return out;
		}
		
		
		// se agrega Marca de Asignacion en caso de  tener
		
		if(in.p_MarcaAsignacion != null &&  !"".equals(in.p_MarcaAsignacion) && in.p_MarcaAsignacion.length() > 0) {
			
			if (in.p_MarcaAsignacion.length() > 25) {
				String codigo = "UI_ERROR_MARCA_ASIGNACION_LONGITUD";
				String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).msg();
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
				return out;
			}
			
			if (!in.p_MarcaAsignacion.matches(REGEX_MARCA_ASIGNACION)) {
				String codigo = "UI_ERROR_MARCA_ASIGNACION_FORMATO";
				String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).msg();
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
				return out;
			}
		
			List<Long> IdsTransacciones = transaccionesPorAsignar.stream().map(x -> x.IdTransaccion).collect(Collectors.toList());
			
			List<UpdateMarcaAsignacion> update = new ArrayList<UpdateMarcaAsignacion>();
			
			for(Long l : IdsTransacciones) {
				UpdateMarcaAsignacion trans = new UpdateMarcaAsignacion();
				trans.MarcaAsig = in.p_MarcaAsignacion;
				trans.IdTransaccion = l;
				
				update.add(trans);
				
			};
				
			sql.batch(con, SQL_UPDATE_TRANSACCIONES, update,UpdateMarcaAsignacion.class);
			

		}
		
		CrearAsignaciones_Entrada entradaCrearAsignaciones =  new CrearAsignaciones_Entrada();
		List<Asignacion_Entrada> asignaciones = new ArrayList<>();
		entradaCrearAsignaciones.p_Asignaciones = asignaciones;
		entradaCrearAsignaciones.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearAsignaciones.p_IdModulo = in.p_IdModulo;
		entradaCrearAsignaciones.p_IdUsuario = in.p_IdUsuario;
		entradaCrearAsignaciones.p_simularOperacion ="N";
		entradaCrearAsignaciones.p_UsaReglasNegocio = "S";
		
		for (Simulacion_Salida asig : salidaCrearSimulacion.P_OUT_SIMASIGNACION) {
			
			if (asig.CantidadAsignacion.doubleValue() == 0) {
				continue;
			}
			
			Asignacion_Entrada asigEnt = new Asignacion_Entrada();
			asigEnt.Cantidad = asig.CantidadAsignacion;
			asigEnt.FolioCartera = 0L;
			asigEnt.IdInterno = asig.IdInterno;
			asigEnt.IdOrden = asig.IdOrden;
			asigEnt.IdTransaccion = asig.IdTransaccion;
			asigEnt.ValorRescate = asig.ValorRescateAsignacion;
			entradaCrearAsignaciones.p_Asignaciones.add(asigEnt);
			
		}
		
		CrearAsignaciones serviceCrearAsignaciones = new CrearAsignaciones();
		CrearAsignaciones_Salida salidaCrearAsignaciones = serviceCrearAsignaciones.logica(con,entradaCrearAsignaciones);
		
		out.P_OUT_MENSAJES.addAll(salidaCrearAsignaciones.P_OUT_MENSAJES);
		
		//TRANSACCIONES
		for (cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono.Transaccion_Salida tr : serviceSalida1.P_OUT_TRANSACCIONES) {
		
			Transaccion_Salida tx =  new Transaccion_Salida();
			Utils.copyPojo(tr, tx);
			out.P_OUT_TRANSACCIONES.add(tx );
			
		}
		//ORDENES
		for (cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono.Orden_Salida or : serviceSalida1.P_OUT_ORDENES) {
			
			Orden_Salida ox =  new Orden_Salida();
			Utils.copyPojo(or, ox);
			out.P_OUT_ORDENES.add(ox);
			
		}
		//RESULTADO
		for (cl.bcs.ordenes.CrearAsignaciones.Resultado_Salida res: salidaCrearAsignaciones.p_out_Resultado) {
			
			Resultado_Salida rr = new Resultado_Salida();
			
			rr.IdInterno = res.IdInterno;
			rr.IdAsignacion = res.IdAsignacion;
			
			for (Asignacion_Entrada asig : asignaciones) {
				if (asig.IdInterno.equals(res.IdInterno)) {
					rr.IdOrden = asig.IdOrden;
					rr.IdTransaccion = asig.IdTransaccion;
					//r.FolioTransaccion = asig.FolioCarter
				}
			}
			
			out.P_OUT_RESULTADO.add(rr);
			
		}

		log.info("TERMINO");
		return out;

	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02", "connections.properties")) {
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9498,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_HoraHasta\": \"10:02:18\",\r\n"
					+ "    \"p_IdTipoAsignacion\": \"ASIGNACION_CRONOLOGICA\",\r\n"
					+ "    \"p_IdInstrumento\": 200,\r\n"
					+ "    \"p_IdMonedaTransaccion\": \"CLP\"\r\n"
					+ "}";
			
			Gson gson = Utils.getGsonBuilder();
			EjecutarAsignacionPropCrono_Entrada asdf = gson.fromJson(json, EjecutarAsignacionPropCrono_Entrada.class);
			EjecutarAsignacionPropCrono demo=new EjecutarAsignacionPropCrono();
			System.out.println(demo.logica(con, asdf).toString());
			//con.commit();
			con.rollback();
			
			
			/*JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(json).getAsJsonObject();
			EjecutarAsignacionPropCrono demo2=new EjecutarAsignacionPropCrono();
			demo2.call(con, dataJsonIn);*/
 			
    	}
	}
}
