package cl.bcs.loaderapp.concrete.socionegocio.fatca;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Fatca;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FatcaDbLoader implements DbLoader {
	public String SQL_ACTUALIZAR_FATCA = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/fatca/Fatca_ActualizarFatca.sql");
	
	public static class EntidadFatca{
		public Long IdPersona;
		public String IdClasificacionFatca;
		public String IdClasificacionEmpresaFatca;
		public String NumeroGIIN;
		public String FechaFirmaFormulario;
		public String AutorizacionInformarOrganismo;
		public String TIN1;
		public Long IdPaisNacionalidad2;
		public String TIN2;
		public Long IdPaisNacionalidad3;
		public String TIN3;
		public Long IdPaisNacionalidad4;
		public String TIN4;
		public Long IdPaisNacionalidad5;
		public String TIN5;
		public String ExcepcionResidente;
		public String ExcepcionNacimiento;
		public String ExcepcionDomicilio;
		public String ExcepcionTelefono;
		public String ExcepcionOrdenPermanente;
		public String ExcepcionFirma;
		public String ParticipacionSociedadesUSA;
		public String OrdenPermanente;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> res = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		
		int linea = 1;
		List<Fatca> fatcasCSV = (List<Fatca>) (Object) lines;
		
		try (Connection conn = jsJB.getConnection()){
			IdentificarLlaves llaves = new IdentificarLlavesBuilder(conn).setPaises().build();
		
			for(Fatca fatca : fatcasCSV){
				try {
					//Identificar IdCLiente mediante el documento de identidad.
					ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();
					Optional<SalidaSocioNegocio> socio = consDocCli.consultarIUP(conn, fatca.getDocumentoIdentidad(), fatca.getTipoDocumentoIdentidad(), idEmpresaDefecto);
					if(socio.isPresent()){
						SalidaSocioNegocio idsSocioNegocio = socio.get(); 
						//crear fatca
						EntidadFatca fat = new EntidadFatca();
						fat.IdPersona = idsSocioNegocio.IdPersona;
						fat.IdClasificacionFatca = fatca.getIdClasificacionFatca();
						fat.IdClasificacionEmpresaFatca = fatca.getIdClasificacionEmpresaFatca();
						fat.NumeroGIIN = fatca.getNumeroGIIN();
						fat.FechaFirmaFormulario = fatca.getFechaFirmaFormulario();
						fat.AutorizacionInformarOrganismo = fatca.getAutorizacionInformarOrganismo();
						fat.TIN1 = fatca.getTIN1();
						fat.IdPaisNacionalidad2 = llaves.getPais(fatca.getIdPaisNacionalidad2());
						fat.TIN2 = fatca.getTIN2();
						fat.IdPaisNacionalidad3 = llaves.getPais(fatca.getIdPaisNacionalidad3());
						fat.TIN3 = fatca.getTIN3();
						fat.IdPaisNacionalidad4 = llaves.getPais(fatca.getIdPaisNacionalidad4());
						fat.TIN4 = fatca.getTIN4();
						fat.IdPaisNacionalidad5 = llaves.getPais(fatca.getIdPaisNacionalidad5());
						fat.TIN5 = fatca.getTIN5();
						fat.ExcepcionResidente = fatca.getExcepcionResidente();
						fat.ExcepcionNacimiento = fatca.getExcepcionNacimiento();
						fat.ExcepcionDomicilio = fatca.getExcepcionDomicilio();
						fat.ExcepcionTelefono = fatca.getExcepcionTelefono();
						fat.ExcepcionOrdenPermanente = fatca.getExcepcionOrdenPermanente();
						fat.ExcepcionFirma = fatca.getExcepcionFirma();
						fat.ParticipacionSociedadesUSA = fatca.getParticipacionSociedadesUSA();
						fat.OrdenPermanente = fatca.getOrdenPermanente();

						int[] resultado = sql.batch(conn, SQL_ACTUALIZAR_FATCA, Arrays.asList(fat), EntidadFatca.class);
					
						if(resultado[0]==1){
							res.add(linea + " - FATCA DOCUMENTO IDENTIDAD " + fatca.getDocumentoIdentidad() + " INGRESADA CORRECTAMENTE");
							conn.commit();
						}else{
							res.add(linea + " - FATCA DOCUMENTO IDENTIDAD " + fatca.getDocumentoIdentidad() + " NO INGRESADA");
						}
					
					}else {
						res.add(linea + " - FATCA DOCUMENTO IDENTIDAD " + fatca.getDocumentoIdentidad() + " NO EXISTE CLIENTE");
					}
				} catch (Exception e) {
					//el documento no esta asociado a una persona.
					res.add(linea + " - FATCA DOCUMENTO IDENTIDAD " + fatca.getDocumentoIdentidad() + " NO INGRESADA");
					e.printStackTrace();
				}
				linea++;
				System.out.println(res.get(res.size()-1));
			}
			conn.rollback();
		} catch (SQLException e1) {
			res.add("ERROR AL OBTENER OBJETO IDENTIFICADOR DE LLAVES");
			e1.printStackTrace();
		}
		System.out.println(lines);
		return res;
	}

}
