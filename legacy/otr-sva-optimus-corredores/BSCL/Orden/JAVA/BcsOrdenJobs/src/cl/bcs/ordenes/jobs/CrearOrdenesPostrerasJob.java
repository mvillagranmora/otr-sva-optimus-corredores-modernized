package cl.bcs.ordenes.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearOrdenesPostreras;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.PrepararOrdenesPostrerasJob.TablaPaso_Eliminar;
import cl.bcs.ordenes.jobs.RefacturarMovimientoJob.ActualizarPrograma;
import cl.bcs.plataforma.bat.AgregarLogPrograma;
import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.BarraProgresoWrapper;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class CrearOrdenesPostrerasJob extends BcsJob implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearOrdenesPostrerasJob.class);
	private final String IdTipoPrograma = "CREAR_ORDENES_POSTRERAS_JOB";
	private final String SQL_SELECT_ENTRADA_ORDENES = loadResourceToString("cl/bcs/ordenes/jobs/sql/CrearOrdenesPostrerasJob_SelectEntradaOrdenes.sql");
	private final String SQL_UPDATE_PROGRAMA = loadResourceToString("cl/bcs/ordenes/jobs/sql/CrearOrdenesPostrerasJob_UpdatePrograma.sql");
	private final String SQL_DeleteTablaPaso = loadResourceToString("cl/bcs/ordenes/jobs/sql/CrearOrdenesPostrerasJob_DeleteTablaPaso.sql");
	private final String SQL_UpdateEstadoTransaccionesJob = loadResourceToString("cl/bcs/ordenes/jobs/sql/CrearOrdenesPostrerasJob_UpdateEstadoTransaccionesJob.sql");
	private final String SQL_UPDATE_RESERVAR_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/jobs/sql/CrearOrdenesPostrerasJob_UpdateReservaTransacciones.sql");

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}

	@Override
	public void onError(Throwable t) {
		logger.info("****** ERROR  *******");
        //log.write("****** ERROR  *******");
		logger.info(t);
        //log.write(t);
        t.printStackTrace();
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static class ReservaTransaccion{
		public Long IdTransaccion;
		public Long IdUsuarioReserva;
		public String MotivoReserva;
		
		public ReservaTransaccion () {
			this.IdTransaccion = 0L;
			this.IdUsuarioReserva = 0L;
			this.MotivoReserva= "";
		}
		
		public ReservaTransaccion (Long idTransaccion, Long idUsuario, String motivoReserva) {
			this.IdTransaccion = idTransaccion;
			this.IdUsuarioReserva = idUsuario;
			this.MotivoReserva= motivoReserva;
		}
	}
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO JOB");
		SQL sql=new SQL();
        
        Long IdEmpresa = 0L;
        Long IdPrograma = 0L;
        Long IdUsuario = 0L;
        Long IdTrabajo = 0L;
        String IdModulo = "ORD";
        String simularOperacion = "";
        String UsaReglasNegocio = "";
        String AplicarComision = "";
        String EnviarFacturarAutomatico = "";
        String EsAsignacionMasivaPrecioPromedio = "";
        String UsaLogDetallado = "";
        List<OrdenPostrera_Entrada> entradaOrdenes =  new ArrayList<>();
        if (debug) {
        	IdPrograma = 801240L;
            IdEmpresa = 1L;
            IdUsuario = 97L;
            IdTrabajo = 5107400963L;
            IdModulo = "ORD";
            AplicarComision = "S";
			EnviarFacturarAutomatico = "N";
			EsAsignacionMasivaPrecioPromedio = "S";
			UsaLogDetallado = "S";
        } else {
			IdPrograma = this.getJobId();
			IdEmpresa = Long.parseLong(super.getParam("IdEmpresa"));
			IdUsuario = Long.parseLong(super.getParam("IdUsuario"));
			IdTrabajo = Long.parseLong(super.getParam("IdTrabajo"));
			simularOperacion = super.getParam("simularOperacion");
			UsaReglasNegocio = super.getParam("UsaReglasNegocio");
			AplicarComision = super.getParam("AplicarComision");
			EnviarFacturarAutomatico = super.getParam("EnviarFacturarAutomatico");
			EsAsignacionMasivaPrecioPromedio = super.getParam("EsAsignacionMasivaPrecioPromedio");
			UsaLogDetallado = super.getParam("UsaLogDetallado");
     		logger.debug("buscando el modulo 1"+super.getParam("IdModulo"));

        }
        try {
        	logger.debug("buscando el modulo 2"+super.getParam("IdModulo"));
        	 HashMap<String,Object> param = new HashMap<String,Object>();
        	 BarraProgreso p_BarraProgreso = new BarraProgresoWrapper(con, IdEmpresa, "Ingreso de Ordenes Masivo", IdPrograma, IdUsuario);
        	 {
				if(p_BarraProgreso != null) {
					p_BarraProgreso.actualizar(1L, "PROCESANDO", "Inicio creacion de ordenes");	
				}
        	 }
        	 
        	
     		param.put("IdTrabajo", IdTrabajo);
     		logger.debug("INICIO::SQL_SELECT_ENTRADA_ORDENES");
     		
      		entradaOrdenes = sql.query(con, SQL_SELECT_ENTRADA_ORDENES, param, OrdenPostrera_Entrada.class);
     		
			Map<Long,OrdenPostrera_Entrada> hashEntradaOrdenes = new HashMap<Long,OrdenPostrera_Entrada>();
			hashEntradaOrdenes = entradaOrdenes.stream().collect(Collectors.toMap(h -> h.IdInterno, h -> h));
			      		
     		if("S".equals(EsAsignacionMasivaPrecioPromedio) && !entradaOrdenes.isEmpty()) {
     			logger.info("INICIO::AGREGAR RESERVA TRANSACCIONES ASIGNACION MASIVA");
     			actualizarReservaTransacciones(con, IdEmpresa ,IdUsuario, entradaOrdenes, "UI_ASIGNACION_MASIVA_PRECIO_PROMEDIO");
     			logger.info("FIN::AGREGAR RESERVAR TRANSACCIONES ASIGNACION MASIVA");
     		}
			
     		logger.debug("FIN::SQL_SELECT_ENTRADA_ORDENES");
     		if(entradaOrdenes.size() > 0) {
     			CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
				entradaCrearOrdenesPostreras.p_IdEmpresa = IdEmpresa;
				entradaCrearOrdenesPostreras.p_IdModulo = IdModulo;
				entradaCrearOrdenesPostreras.p_IdUsuario = IdUsuario;
				entradaCrearOrdenesPostreras.p_Ordenes = entradaOrdenes;
				entradaCrearOrdenesPostreras.p_AplicarComision = AplicarComision;
				entradaCrearOrdenesPostreras.p_EnviarFacturarAutomatico = EnviarFacturarAutomatico;
 				entradaCrearOrdenesPostreras.p_EsAsignacionMasivaPrecioPromedio = EsAsignacionMasivaPrecioPromedio;
				entradaCrearOrdenesPostreras.p_UsaLogDetallado = UsaLogDetallado;
				{
					if(p_BarraProgreso != null) {
						entradaCrearOrdenesPostreras.p_BarraProgreso = p_BarraProgreso.subrango(1, 99);
					}
				}
				
  				CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
				logger.debug("INICIO::CrearOrdenesPostreras con el modulo "+IdModulo);
				CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(con, entradaCrearOrdenesPostreras);
				logger.debug("FIN::CrearOrdenesPostreras");
				
				{
					if(p_BarraProgreso != null) {
						if("S".equals(EsAsignacionMasivaPrecioPromedio) && !entradaOrdenes.isEmpty()) {
			     			logger.info("INICIO::QUITAR RESERVA TRANSACCIONES ASIGNACION MASIVA");
			     			actualizarReservaTransacciones(con, IdEmpresa ,0L, entradaOrdenes, "");
			     			logger.info("FIN::QUITAR RESERVA TRANSACCIONES ASIGNACION MASIVA");
			     		}
						p_BarraProgreso.actualizar(99L, "PROCESANDO", "Preparando archivo de salida");
					}
				}	
				
				String rutaCreacionArchivoLog = "";
				String rutaLecturaArchivoLog = "";
				
				String rutaBaseArchivos = ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
				
				SimpleDateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
		        String fechaSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, IdEmpresa);
				SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date fechaHoraDate = parser.parse(fechaSistema);
				
		        
				String nombreArchivo = "RESULTADO_ORDENES_ASIGNACION_" + fm.format(fechaHoraDate)+".txt";
				rutaCreacionArchivoLog = rutaBaseArchivos + nombreArchivo;
				rutaLecturaArchivoLog  = nombreArchivo;

				File folder = new File(rutaBaseArchivos);
				if(!folder.exists()){
					folder.mkdirs();
				}				 
				 
				 File archivoLog = new File (rutaCreacionArchivoLog);
				 BufferedWriter bw;
				 bw = new BufferedWriter(new FileWriter(archivoLog));
				 bw.write("== INICIO EJECUCION CREAR ORDENES / ASIGNACION ("+ fm.format(fechaHoraDate) +") == \n");
				
				if(salidaCrearOrdenesPostreras.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
					//EXITO
					con.commit();
					HashMap<String, String> hash = new HashMap<String, String>();
					String exito = Diccionario.getMensaje(con, IdEmpresa, "UI_ASIGNACIONES_CREADAS", hash );
					AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"INFORMACION : "+exito,true);
					bw.write("INFORMACION >>  " + exito +"\n");
					
					if("S".equals(UsaLogDetallado)) {
		     			salidaCrearOrdenesPostreras.P_OUT_MENSAJES = agruparMensajesSalida (salidaCrearOrdenesPostreras.P_OUT_MENSAJES);
		     		}
					
					for(MensajeSalida mje : salidaCrearOrdenesPostreras.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mje.tipo)) {
							//ADVERTENCIAS
							AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ADVERTENCIAS : " + mje.descripcion,true);
							bw.write("ADVERTENCIA >>  " + mje.descripcion +"\n");
						}
						if("S".equals(UsaLogDetallado)) {
							if(MensajeSalida.MENSAJE_INFORMACION.equals(mje.tipo)) {
								AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"INFORMACIONES : " + mje.descripcion,true);
								bw.write(mje.tipo + " >>  " + mje.descripcion +"\n");
							}
		     			}
					}	
				} else {
					HashMap<String,String> params = new HashMap<String,String>(6);
					
					if("S".equals(UsaLogDetallado)) {
		     			salidaCrearOrdenesPostreras.P_OUT_MENSAJES = agruparMensajesSalida (salidaCrearOrdenesPostreras.P_OUT_MENSAJES);
		     		}
					
					for(MensajeSalida mje : salidaCrearOrdenesPostreras.P_OUT_MENSAJES){ 
						if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
							if("S".equals(UsaLogDetallado)) {
								params.clear();

								OrdenPostrera_Entrada ordenPostrera = hashEntradaOrdenes.get(mje.id);
								params.put("DocumentoIdentidad", ordenPostrera.DocumentoIdentidad);
								params.put("NombreCompleto", ordenPostrera.NombreCompleto);
								params.put("Folio", ordenPostrera.FolioTransaccion);
								params.put("IdTipoOperacion", ordenPostrera.IdTipoOperacion);
								params.put("Nemotecnico", ordenPostrera.Nemotecnico);
								params.put("Cantidad", ordenPostrera.Cantidad.toString());
								params.put("Monto", ordenPostrera.Monto.toString());
								MensajeSalida msjError = new MensajeSalida(ordenPostrera.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"ASIGNACION_MASIVA_PRECIO_PROMEDIO_MENSAJE_SALIDA_BASE",params, con, IdEmpresa);
								
								//ERRORES
								AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ERRORES : "+ msjError .descripcion + " ; " + mje.descripcion,true);
								bw.write("ERROR >>  " + msjError.descripcion + " " + mje.descripcion + ";" +"\n");			
							} else {
								AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ERRORES : "+ mje.descripcion,true);
								bw.write("ERROR >>  " + mje.descripcion +"\n");
							}
							con.rollback();
						}
					}
				}
				
				bw.write("== FIN EJECUCION CREAR ORDENES / ASIGNACION ("+ fm.format(fechaHoraDate) +") == \n");
				 
				bw.close();
				actualizarEstadoTransaccionJob(entradaOrdenes,con);
					
				String rutaArchivo = rutaLecturaArchivoLog;
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(rutaArchivo, this.getJobId()));
				sql.batch(con,SQL_UPDATE_PROGRAMA , actualizar,ActualizarPrograma.class);
				TablaPaso_Eliminar tablaPaso = new TablaPaso_Eliminar();
				tablaPaso.IdTrabajo = IdTrabajo;
				List<TablaPaso_Eliminar> listTablaPaso = new ArrayList<>();
				listTablaPaso.add(tablaPaso);
				try {
					logger.debug("INICIO::SQL_DeleteTablaPaso");
						sql.batch(con,SQL_DeleteTablaPaso, listTablaPaso, TablaPaso_Eliminar.class);
					logger.debug("FIN::SQL_DeleteTablaPaso");
				}
				catch(Exception e) {
					logger.debug("ERROR::SQL_DeleteTablaPaso "+e);
				}
				
				{
					if(p_BarraProgreso != null) {
						p_BarraProgreso.actualizar(100L, "PROCESANDO", "Fin creacion de ordenes");
					}
				}
				 
     		}
        }catch(Exception e) {
        	con.rollback();
        	logger.info("error "+e);
        }
        
        con.commit();
        return true;
	}
	
	private List<MensajeSalida> agruparMensajesSalida(List<MensajeSalida> mensajes) {
		logger.info("INICIO::AGRUPAR MENSAJES SALIDA");

		List<MensajeSalida> ordenados = mensajes.stream()
				.sorted(Comparator.comparing((MensajeSalida m) -> m.codigo, Comparator.nullsLast(String::compareTo))
						.thenComparing(m -> m.id, Comparator.nullsLast(Long::compareTo)))
				.collect(Collectors.toList());

		logger.info("FIN::AGRUPAR MENSAJES SALIDA");

		return ordenados.stream().collect(Collectors.groupingBy(m -> m.id, LinkedHashMap::new, Collectors.toList()))
				.values().stream().map(grupo -> {
					MensajeSalida base = grupo.get(0);
					MensajeSalida out = new MensajeSalida();

					out.id = base.id;
					boolean tieneError = grupo.stream().anyMatch(m -> MensajeSalida.MENSAJE_ERROR.equals(m.tipo));
					boolean tieneAdvertencia = grupo.stream().anyMatch(m -> MensajeSalida.MENSAJE_ADVERTENCIA.equals(m.tipo));
					 
					out.tipo = tieneError ? MensajeSalida.MENSAJE_ERROR : (tieneAdvertencia ? MensajeSalida.MENSAJE_ADVERTENCIA : base.tipo);
					out.codigo = base.codigo;

					out.descripcion = grupo.stream().map(m -> m.descripcion).collect(Collectors.joining(" ; "));

					return out;
				}).collect(Collectors.toList());
	}

	private void actualizarReservaTransacciones(Connection con, Long idEmpresa, Long idUsuario, List<OrdenPostrera_Entrada> entradaOrdenes, String codigoMensaje) throws Exception {
		logger.info("INICIO::ACTUALIZAR RESERVA TRANSACCIONES ASIGNACION MASIVA");
		
		String mensajeReserva = codigoMensaje.isEmpty() ? "" : Diccionario.getMensaje(con,idEmpresa ,codigoMensaje, new HashMap<String,String>(0));
		List<ReservaTransaccion> transaccionesReservar = entradaOrdenes.stream().map(ent -> {return new ReservaTransaccion(ent.IdTransaccion, idUsuario,  mensajeReserva );} ).collect(Collectors.toList());
		SQL sql=new SQL();
		
		sql.batch(con,SQL_UPDATE_RESERVAR_TRANSACCIONES , transaccionesReservar,ReservaTransaccion.class);
		logger.info("FIN::ACTUALIZAR RESERVA TRANSACCIONES ASIGNACION MASIVA");
	}

	public void actualizarEstadoTransaccionJob(List<OrdenPostrera_Entrada> listaTransacciones,Connection con) {
		SQL _sql = new SQL();
		try {
			logger.debug("INICIO::SQL_UpdateEstadoTransaccionesJob");
			_sql.batch(con,SQL_UpdateEstadoTransaccionesJob , listaTransacciones,OrdenPostrera_Entrada.class);
			logger.debug("FIN::SQL_UpdateEstadoTransaccionesJob");
		}
		catch(Exception e) {
			logger.debug("FIN::SQL_UpdateEstadoTransaccionesJob "+e);
		}
	}
	
	
	
	public static void main(String[] args){
	
		
		
			CrearOrdenesPostrerasJob p = new CrearOrdenesPostrerasJob();
			try {
				Connection con = PlataformaUtils.getPropertiesConnection("qa02");
				p.logica( con,true);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	
	}

}
