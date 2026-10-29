package cl.bcs.loaderapp.concrete.portafolio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.portafolio.Portafolio;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.utils.SQL;

public class PortafolioDbLoader implements DbLoader{
	public String SQL_CREAR_PORTAFOLIO_Call = loadResourceToString("cl/bcs/loaderapp/sql/portafolio/Portafolio_Call_Ingresar.sql");
	public String SQL_ACTUALIZAR_PORTAFOLIO = loadResourceToString("cl/bcs/loaderapp/sql/portafolio/Portafolio_Actualizar.sql");
	
	public static class EntidadPortafolio{
		public Long IdPortafolio;
		public String FechaBloqueo;
		public String ComentarioBloqueo;
		public String IdTipoBloqueo;
		public String IdPerfilProductos;
		public String IdPerfilCobros;
		public String DescripcionPortafolio;
		public String IdPerfilLimites;
		public String NumeroFicha;
		public String IdNombreFantasia;
		public String Alias;
		public Long IdPersonaBrokerExtranjero;
		
		@Override
		public String toString() {
			return String.format(
					"EntidadPortafolio [IdPortafolio=%s, FechaBloqueo=%s, ComentarioBloqueo=%s, IdTipoBloqueo=%s, IdPerfilProductos=%s, IdPerfilCobros=%s, DescripcionPortafolio=%s, IdPerfilLimites=%s, NumeroFicha=%s, IdNombreFantasia=%s, Alias=%s, IdPersonaBrokerExtranjero=%s]",
					IdPortafolio, FechaBloqueo, ComentarioBloqueo, IdTipoBloqueo, IdPerfilProductos, IdPerfilCobros,
					DescripcionPortafolio, IdPerfilLimites, NumeroFicha, IdNombreFantasia, Alias,
					IdPersonaBrokerExtranjero);
		}
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		try(Connection conn = jsJB.getConnection()){
			int linea = 1;
			List<Portafolio> portafoliosCSV = (List<Portafolio>) (Object) lines;
			for(Portafolio portafolio : portafoliosCSV){
				try {
					//Identificar IdCLiente medictae el documento de identidad.
					ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();
					SalidaSocioNegocio idsSocioNegocio = consDocCli.logica(conn, portafolio.getDocumentoIdentidadCliente(), portafolio.getTipoDocumentoIdentidadCliente(), "", idEmpresaDefecto);
	
					//ingresar portafolio del cliente
					HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
					paramsIn.put("IdEmpresa", idEmpresaDefecto);
					paramsIn.put("IdUsuario", idUsuarioDefecto);
					paramsIn.put("IdModulo", "SOC");
					
					paramsIn.put("IdCliente", idsSocioNegocio.IdCliente);
					paramsIn.put("IdPerfilComercial", portafolio.getIdPerfilComercialBaseOrigen());
					paramsIn.put("CodigoPortafolio", portafolio.getCodigoPortafolio());
					paramsIn.put("PropositoPortafolio", portafolio.getProposito());
					paramsIn.put("IdEstado", portafolio.getIdEstado());
					
					//Salida HashMap
					HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
					paramsOut.put("p_out_RefId", Long.class);
					paramsOut.put("p_out_AdvertenciasContador", Long.class);
					paramsOut.put("p_out_ErroresContador", Long.class);
					paramsOut.put("p_out_ErroresDetalle", ErrorDetalle.class);
					paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
					
					Long idPortafolio = null;
					HashMap<String,Object> resultSP = sql.call(conn, SQL_CREAR_PORTAFOLIO_Call, paramsIn, paramsOut);
					if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
						idPortafolio = (Long)resultSP.get("p_out_RefId");
					}else{
						for(ErrorDetalle errorDetalle : (List<ErrorDetalle>) resultSP.get("p_out_ErroresDetalle")){
							salida.add(linea + " - PORTAFOLIO " + portafolio.getDocumentoIdentidadCliente() + " NO INGRESADO: " + errorDetalle.Descripcion);
						}
					}
					
					if(idPortafolio!=null){
						//actualizar el portafolio con los campos faltantes
						System.out.println("IdPortafolio: " + idPortafolio);
						idsSocioNegocio = consDocCli.logica(conn, portafolio.getDocumentoIdentidadBrokerExtranjero(), portafolio.getTipoDocumentoIdentidadBrokerExtranjero(), "", idEmpresaDefecto);
						
						EntidadPortafolio por = new EntidadPortafolio();
						por.IdPortafolio = idPortafolio;
						por.FechaBloqueo = portafolio.getFechaBloqueo();
						por.ComentarioBloqueo = portafolio.getComentarioBloqueo();
						por.IdTipoBloqueo = portafolio.getIdTipoBloqueo();
						por.IdPerfilProductos = portafolio.getIdPerfilProductos();
						por.IdPerfilCobros = portafolio.getIdPerfilCobros();
						por.DescripcionPortafolio = portafolio.getDescripcionPortafolio();
						por.IdPerfilLimites = portafolio.getIdPerfilLimites();
						por.NumeroFicha = portafolio.getNumeroFicha();
						por.IdNombreFantasia = portafolio.getIdNombreFantasia();
						por.Alias = portafolio.getAlias();
						por.IdPersonaBrokerExtranjero = (idsSocioNegocio!=null) ? idsSocioNegocio.IdPersona : 0;
						
						int[] res = sql.batch(conn, SQL_ACTUALIZAR_PORTAFOLIO, Arrays.asList(por), EntidadPortafolio.class);
						for (int i : res) {
							System.out.println(i);
						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - PORTAFOLIO " + portafolio.getDocumentoIdentidadCliente() + " NO INGRESADO");
				}
				linea++;
			}
		}catch(Exception e) {
			salida.add("ERROR AL OBTENER CONEXION");
		}
		return salida;
	}

}
