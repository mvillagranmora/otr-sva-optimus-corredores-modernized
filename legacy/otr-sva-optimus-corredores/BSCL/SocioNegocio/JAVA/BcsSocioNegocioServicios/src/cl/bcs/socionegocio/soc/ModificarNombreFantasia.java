package cl.bcs.socionegocio.soc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModificarNombreFantasia {
	
	static final Logger logger = LogManager.getLogger(ModificarNombreFantasia.class);

	// ************************************************
	// AGREGAR, MODIFICAR Y ELIMINAR NOMBRE DE FANTASIA
	// ************************************************
	/*if(!socioNegocio.NombreFantasia.isEmpty())
	{
		for(NombreFantasia nombreFantasia : socioNegocio.NombreFantasia)
		{
			if(nombreFantasia.Accion.equals("AGREGAR"))
			{
				HashMap<String,Object> inParametros = new HashMap<String,Object>();
 				inParametros.put("p_IdEmpresa", in.p_IdEmpresa);
 				inParametros.put("p_IdUsuario", in.p_IdUsuario);
 				inParametros.put("p_IdModulo", "SOC");
 				inParametros.put("p_UsaReglasNegocio", "N");
 				inParametros.put("p_IdPersona", IdEntidad);
 				inParametros.put("p_NombreFantasia", nombreFantasia.Nombre);
 				inParametros.put("p_IdEstado", "ACT");
				
				HashMap<String,Class>  outParametros = new HashMap<String,Class>();
				outParametros.put("p_out_RefId", Long.class);
				outParametros.put("p_out_AdvertenciasContador", Long.class);
				outParametros.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
				outParametros.put("p_out_ErroresContador", Long.class);
				outParametros.put("p_out_ErroresDetalle", ErrorDetalle.class);
				logger.debug("INICIO::SQL_CALL_CREAR_NOMBRE_FANTASIA");
				HashMap<String,Object> salidaNombreFantasia = sql.call(conn, SQL_CALL_CREAR_NOMBRE_FANTASIA, inParametros, outParametros);
				logger.debug("FIN::SQL_CALL_CREAR_NOMBRE_FANTASIA");
				if(((Long) salidaNombreFantasia.get("p_out_ErroresContador")) > 0)
				{
					entidadModificada.TipoEntidad = TipoEntidad;
					entidadModificada.IdEntidad = IdEntidad;
					
					for(ErrorDetalle errorDetalle : (List<ErrorDetalle>) salidaNombreFantasia.get("p_out_ErroresDetalle"))
					{
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "ERROR";
						mensajesErrores.Codigo = errorDetalle.Codigo;
						mensajesErrores.Mensaje = errorDetalle.Descripcion;
						
						listMensajesErrores.add(mensajesErrores);
						entidadModificada.Mensajes = listMensajesErrores;
						listEntidadModificada.add(entidadModificada);
						ContadorErrores++;
						break;
					}
					
					for(AdvertenciaDetalle advertenciaDetalle : (List<AdvertenciaDetalle>) salidaNombreFantasia.get("p_out_AdvertenciasDetalle"))
					{
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "ADVERTENCIA";
						mensajesErrores.Codigo = advertenciaDetalle.Codigo;
						mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;
						
						listMensajesErrores.add(mensajesErrores);
						entidadModificada.Mensajes = listMensajesErrores;
						listEntidadModificada.add(entidadModificada);
						ContadorErrores++;
						break;
					}
					
					break;
				}
			}
			else if(nombreFantasia.Accion.equals("MODIFICAR"))
			{
				HashMap<String,Object> inParametros = new HashMap<String,Object>();
				inParametros.put("p_IdEmpresa", in.p_IdEmpresa);
				inParametros.put("p_IdUsuario", in.p_IdUsuario);
				inParametros.put("p_IdModulo", "SOC");
				inParametros.put("p_UsaReglasNegocio", "N");
				inParametros.put("p_IdPersona", IdEntidad);
				inParametros.put("p_IdNombreFantasia", nombreFantasia.IdNombreFantasia);
				inParametros.put("p_NombreFantasia", nombreFantasia.Nombre);
				inParametros.put("p_IdEstado", nombreFantasia.IdEstado);
				
				HashMap<String,Class>  outParametros = new HashMap<String,Class>();
				outParametros.put("p_out_AdvertenciasContador", Long.class);
				outParametros.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
				outParametros.put("p_out_ErroresContador", Long.class);
				outParametros.put("p_out_ErroresDetalle", ErrorDetalle.class);
				logger.debug("INICIO::SQL_CALL_MODIFICAR_NOMBRE_FANTASIA");
				HashMap<String,Object> salidaModificarNombreFantasia = sql.call(conn, SQL_CALL_MODIFICAR_NOMBRE_FANTASIA, inParametros, outParametros);
				logger.debug("FIN::SQL_CALL_MODIFICAR_NOMBRE_FANTASIA");
				if(((Long) salidaModificarNombreFantasia.get("p_out_ErroresContador")) > 0)
				{
					entidadModificada.TipoEntidad = TipoEntidad;
					entidadModificada.IdEntidad = IdEntidad;
					
					for(ErrorDetalle errorDetalle : (List<ErrorDetalle>) salidaModificarNombreFantasia.get("p_out_ErroresDetalle"))
					{
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "ERROR";
						mensajesErrores.Codigo = errorDetalle.Codigo;
						mensajesErrores.Mensaje = errorDetalle.Descripcion;
						
						listMensajesErrores.add(mensajesErrores);
						entidadModificada.Mensajes = listMensajesErrores;
						listEntidadModificada.add(entidadModificada);
						ContadorErrores++;
						break;
					}
					
					for(AdvertenciaDetalle advertenciaDetalle : (List<AdvertenciaDetalle>) salidaModificarNombreFantasia.get("p_out_AdvertenciasDetalle"))
					{
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "ADVERTENCIA";
						mensajesErrores.Codigo = advertenciaDetalle.Codigo;
						mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;
						
						listMensajesErrores.add(mensajesErrores);
						entidadModificada.Mensajes = listMensajesErrores;
						listEntidadModificada.add(entidadModificada);
						ContadorErrores++;
						break;
					}
					
					break;
				}
			}
		}
		
		if(ContadorErrores > 0) break;
	}*/
}
