package cl.bcs.loaderapp.concrete.socionegocio.anotacion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Anotacion;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnotacionDbLoader implements DbLoader{
	public String SQL_INGRESAR_ANOTACION = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/anotacion/Anotacion_IngresarAnotacion.sql");
	
	public static class EntidadAnotacion{
		public Long IdAnotacion;
		public Long IdCliente;
		public String FechaAnotacion;
		public String IdMotivoAnotacion;
		public String Titulo;
		public String Detalle;
		public Long IdUsuarioCreador;
		public String IdRelevanciaAnotacion;
		public String IdAmbitoAnotacion;
		public String FechaDesde;
		public String FechaHasta;
		@Override
		public String toString() {
			return String.format(
					"EntidadAnotacion [IdAnotacion=%s, IdCliente=%s, FechaAnotacion=%s, IdMotivoAnotacion=%s, Titulo=%s, Detalle=%s, IdUsuarioCreador=%s, IdRelevanciaAnotacion=%s, IdAmbitoAnotacion=%s, FechaDesde=%s, FechaHasta=%s]",
					IdAnotacion, IdCliente, FechaAnotacion, IdMotivoAnotacion, Titulo, Detalle, IdUsuarioCreador,
					IdRelevanciaAnotacion, IdAmbitoAnotacion, FechaDesde, FechaHasta);
		}
	}
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> res = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		try(Connection conn = jsJB.getConnection()){
			int linea = 1;
			List<Anotacion> anotacionesCSV = (List<Anotacion>) (Object) lines;
			for(Anotacion anotacion : anotacionesCSV){
				try {
					//Identificar IdCLiente mediante el documento de identidad.
					ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();
					SalidaSocioNegocio idsSocioNegocio = consDocCli.logica(conn, anotacion.getDocumentoIdentidad(), anotacion.getTipoDocumentoIdentidad(), anotacion.getTipoPersona(), idEmpresaDefecto);
					
					//crear anotacion
					EntidadAnotacion ant = new EntidadAnotacion();
					ant.IdAnotacion = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Anotacion", (long) 1).get(0);
					ant.IdCliente = idsSocioNegocio.IdCliente;
					ant.FechaAnotacion = anotacion.getFechaAnotacion();
					ant.IdMotivoAnotacion = anotacion.getIdMotivo();
					ant.Titulo = anotacion.getTitulo();
					ant.Detalle = anotacion.getDetalle();
					ant.IdUsuarioCreador = idUsuarioDefecto;
					ant.IdRelevanciaAnotacion = anotacion.getIdRelevancia();
					ant.IdAmbitoAnotacion = anotacion.getIdAmbito();
					ant.FechaDesde = anotacion.getFechaDesde();
					ant.FechaHasta = anotacion.getFechaHasta();
					
					System.out.println(ant);
					
					int[] resultado = sql.batch(conn, SQL_INGRESAR_ANOTACION, Arrays.asList(ant), EntidadAnotacion.class);
				
					if(resultado[0]==1){
						res.add(linea + " - ANOTACION " + anotacion.getDocumentoIdentidad() + " INGRESADA CORRECTAMENTE");
					}else{
						res.add(linea + " - ANOTACION " + anotacion.getDocumentoIdentidad() + " NO INGRESADA");
					}
				} catch (Exception e) {
					res.add(linea + " - ANOTACION " + anotacion.getDocumentoIdentidad() + " NO INGRESADA");
				}
				linea++;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return res;
	}
	
}
