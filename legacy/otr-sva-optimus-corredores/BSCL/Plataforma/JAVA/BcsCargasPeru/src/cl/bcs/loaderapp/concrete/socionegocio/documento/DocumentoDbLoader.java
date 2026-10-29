package cl.bcs.loaderapp.concrete.socionegocio.documento;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Documento;
import cl.bcs.utils.SQL;

public class DocumentoDbLoader implements DbLoader{
	public String SQL_INGRESAR_DOCUMENTO = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/documento/Documento_IngresarDocumento.sql");
	
	public static class EntidadDocumento{
		public String IdTipoDocumento;
		public Long IdEmpresa;
		public String NombreDocumento;
		public Long NumeroFirmantesEmpresa;
		public Long NumeroFirmantesCliente;
		public String Clasificacion;
		public String ValidaFechaVigencia;
		public String CodigoTipoIndicador;
		public String IdTipoPersona;
	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> res = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		try (Connection conn = jsJB.getConnection()){
			int linea = 1;
			List<Documento> documentosCSV = (List<Documento>) (Object) lines;
			for(Documento documento : documentosCSV){
				System.out.println(documento);
				EntidadDocumento doc = new EntidadDocumento();
				doc.IdEmpresa = idEmpresaDefecto;
				doc.IdTipoDocumento = documento.getTipoDocumento();
				doc.NombreDocumento = documento.getNombreDocumento();
				doc.NumeroFirmantesEmpresa = Long.parseLong(documento.getNumeroFirmantesEmpresa());
				doc.NumeroFirmantesCliente = Long.parseLong(documento.getNumeroFirmantesCliente());
				doc.Clasificacion = documento.getClasificacion();
				doc.ValidaFechaVigencia = documento.getValidarFechaVigencia();
				doc.CodigoTipoIndicador = documento.getCodigoTipoIndicador();
				doc.IdTipoPersona = documento.getTipoPersona();
				
				int[] resultado = sql.batch(conn, SQL_INGRESAR_DOCUMENTO, Arrays.asList(doc), EntidadDocumento.class);
				if(resultado!=null && resultado[0]==1){
					res.add(linea + " - DOCUMENTO " + documento.getNombreDocumento() + " INGRESADO CORRECTAMENTE");
				}else{
					res.add(linea + " - DOCUMENTO " + documento.getNombreDocumento() + " NO INGRESADADO");
				}
			}
		}catch (Exception e) {
			res.add("ERROR AL INGRESAR LOS DOCUMENTOS");
		}
			
		return res;
	}

}
