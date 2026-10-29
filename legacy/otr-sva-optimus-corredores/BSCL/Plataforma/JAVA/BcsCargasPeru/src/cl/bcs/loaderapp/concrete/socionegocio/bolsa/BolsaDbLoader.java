package cl.bcs.loaderapp.concrete.socionegocio.bolsa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.socionegocio.cnt.CrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.EntradaServicioCrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.SalidaServicioCrearContraparte;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.socionegocio.utils.IUP;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BolsaDbLoader implements DbLoader{
	public String SQL_CREAR_BOLSA = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/bolsa/CrearBolsa_Insert.sql");
	public String SQL_CONSULTAR_BOLSA = "SELECT count(1) AS \"Existe\" FROM \"BSCL.Plataforma.BD::INS.Bolsa\" WHERE \"IdBolsa\" = :IdBolsa;";
	
	public static class Count{
		public long Existe;
	}
	
	public class Bolsa{
		public String IdBolsa;
		public String Codigo;
		public String Nombre;
		public String IdEstado;
		public String EsExtranjera;
		public String TipoPersona;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String Pais;
		public String Ciudad;
		public int Linea;
		public String TipoDocumentoIdentidadAsociado;
		public String DocumentoIdentidadAsociado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class EntidadBolsa{
		public String IdBolsa;
		public String Codigo;
		public String Nombre;
		public Long IdPais;
		public String IdEstado;
		public String EsExtranjera;
		public String DocumentoIdentidad;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		try (Connection con = jsJB.getConnection()){
			ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
			IdentificarLlaves llaves = new IdentificarLlavesBuilder(con)
					.setCiudadesUbigeo()
					.setPaises()
					.build();
			long idCarga = in.IdCarga;
			
			List<CSVRecord> recordsCSV = (List<CSVRecord>) (Object) lines;
			List<Bolsa> bolsas = new ArrayList<Bolsa>();
			int inlinea = 0;
			for (CSVRecord csvRecord : recordsCSV) {
				Bolsa bolsa = new Bolsa();
				bolsa.IdBolsa = csvRecord.get("IdBolsa");
				bolsa.Codigo = csvRecord.get("Codigo");
				bolsa.Nombre = csvRecord.get("Nombre");
				bolsa.IdEstado = csvRecord.get("IdEstado");
				bolsa.EsExtranjera = csvRecord.get("EsExtranjera");
				bolsa.TipoPersona = csvRecord.get("TipoPersona");
				bolsa.TipoDocumentoIdentidadAsociado = csvRecord.get("TipoDocumentoIdentidad");
				bolsa.DocumentoIdentidadAsociado = csvRecord.get("DocumentoIdentidad");

				bolsa.TipoDocumentoIdentidad = "IUP";
				bolsa.DocumentoIdentidad = IUP.generar(csvRecord.get("DocumentoIdentidad"), csvRecord.get("TipoDocumentoIdentidad"),csvRecord.get("TipoPersona"));
				
				bolsa.RazonSocial = csvRecord.get("RazonSocial");
				bolsa.Pais = csvRecord.get("Pais");
				bolsa.Ciudad = csvRecord.get("Ciudad");
				bolsa.Linea = inlinea++;
				bolsas.add(bolsa);
			}
			
			//bolsas.parallelStream().forEach((bolsa) -> {
			for (Bolsa bolsa : bolsas) {
				int linea = bolsa.Linea;
				try (Connection conn = jsJB.getConnection()){
					try {	
						SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, idEmpresaDefecto, "CONTRAPARTE", bolsa.TipoDocumentoIdentidad, bolsa.DocumentoIdentidad, bolsa.TipoPersona);
						HashMap<String,Object> params = new HashMap<String,Object>();
						params.put("IdBolsa", bolsa.IdBolsa);
						List<Count> contador = sql.query(conn, SQL_CONSULTAR_BOLSA, params, Count.class);
						if (documentoValido.Valido.equals("S")  && contador!=null && contador.get(0).Existe==0) {
							
							//Crear Persona
							CrearContraparte crearContraparte = new CrearContraparte();
							EntradaServicioCrearContraparte entradaCrearContraparte = new EntradaServicioCrearContraparte();
							entradaCrearContraparte.p_IdEmpresa = idEmpresaDefecto;
							entradaCrearContraparte.p_IdModulo = in.p_IdModulo;
							entradaCrearContraparte.p_IdUsuario = idUsuarioDefecto;
							
							Enrolamiento enr = new Enrolamiento();
							enr.IdTipoEntidad = "CONTRAPARTE";
							enr.Tipo = bolsa.TipoPersona;
							if(enr.Tipo==null) {
								throw new Exception("TipoPersona es obligatorio");
							}
							enr.IdTipoDocumentoIdentidad = bolsa.TipoDocumentoIdentidad;
							enr.DocumentoIdentidad = bolsa.DocumentoIdentidad;
							if(enr.DocumentoIdentidad==null) {
								throw new Exception("DocumentoIdentidad es obligatorio");
							}
							enr.IdTipoDocumentoIdentidadAsociado = bolsa.TipoDocumentoIdentidadAsociado;
							enr.RazonSocial = bolsa.RazonSocial;
							enr.IdPaisNacimiento = llaves.getPais(bolsa.Pais);
							enr.IdPaisNacimiento = llaves.getPais("PE");
							if(enr.IdPaisNacimiento==null) {
								throw new Exception("Pais " + bolsa.Pais + ", no Existe");
							}
							enr.IdCiudad = llaves.getCiudadUbigeo(bolsa.Ciudad);
							if(enr.IdCiudad==null) {
								throw new Exception("Ciudad " + bolsa.Ciudad + ", no Existe");
							}
							enr.CNT_Bolsa = "S";
							enr.CNT_Boveda = "N";
							enr.CNT_CamaraDeCompensacion = "N";
							enr.CNT_Corredora = "N";
							enr.CNT_Depositos = "N";
							enr.CNT_Emisor ="N";
							enr.CNT_Otros="";
							enr.CNT_Banco="N";
							enr.CNT_CodigoInscripcion = "";
							
							
							//obligatorios
							enr.IdEmpresa = idEmpresaDefecto;
							enr.IdUsuario = idUsuarioDefecto;
							enr.IdEntidadEnrolada = (long) 0;
							enr.IdTipoDocumentoIdentidadAdicional = "";
							enr.DocumentoIdentidadAdicional = "";
							enr.Nombres = "";
							enr.ApellidoMaterno="";
							enr.ApellidoPaterno="";
							enr.Sexo ="";
							enr.EstadoCivil = "";
							enr.IdTipoTelefono ="";
							enr.Telefono = "";
							enr.Email = "";
							enr.DireccionCalle = "";
							enr.DireccionNumero = "";
							enr.DireccionDepartamento = "";
							enr.CLI_IdCategoriaInversionista = "";
							enr.POR_IdPerfilComercial ="";
							enr.CLI_IdTipoCliente ="";
							enr.CLI_CodigoAdicionalHolding ="";
							enr.CLI_IdClaseCliente = "";
							enr.IdCarga = idCarga;
							
							entradaCrearContraparte.p_ConsultarEnrolamiento = Arrays.asList(enr);
							SalidaServicioCrearContraparte salidaCrearContraparte = crearContraparte.logica(conn, entradaCrearContraparte);
							//obtener advertencias de la creacion de contraparte
							for(AdvertenciaDetalle advertencia : salidaCrearContraparte.P_OUT_ADVERTENCIASDETALLE){
								System.out.println(advertencia.Descripcion);
							}
							
							if(salidaCrearContraparte.P_OUT_ERRORESCONTADOR==0 || salidaCrearContraparte.P_OUT_REFIDCONTRAPARTE != 0 ){
								EntidadBolsa bls = new EntidadBolsa();
								bls.IdBolsa = bolsa.IdBolsa;
								bls.IdBolsa = bolsa.IdBolsa;
								bls.Codigo = bolsa.Codigo;
								bls.Nombre = bolsa.Nombre;
								bls.IdPais = enr.IdPaisNacimiento;
								bls.IdEstado = bolsa.IdEstado;
								bls.EsExtranjera = bolsa.EsExtranjera;
								bls.DocumentoIdentidad = bolsa.DocumentoIdentidad;
								
								sql.batch(conn, SQL_CREAR_BOLSA, Arrays.asList(bls), EntidadBolsa.class);
																
								salida.add(linea + " - BOLSA " + bolsa.DocumentoIdentidadAsociado + " INGRESADO CORRECTAMENTE");
								conn.commit();
							}else {
								for (ErrorDetalle err : salidaCrearContraparte.P_OUT_ERRORESDETALLE) {
									System.out.println(err);
								}
								salida.add(linea + " - BOLSA " + bolsa.DocumentoIdentidadAsociado + " NO SE PUDO CREAR PERSONA ASOCIADA");	
							}
							
							salida.add(linea + " - BOLSA " + bolsa.DocumentoIdentidadAsociado + " INGRESADO CORRECTAMENTE");
							conn.commit();
						}else{
							salida.add(linea + " - BOLSA " + bolsa.DocumentoIdentidadAsociado + " YA EXISTE");
						}
						conn.rollback();
					}catch(Exception ex) {
						salida.add(linea + " - BOLSA " + bolsa.DocumentoIdentidadAsociado +" "+ ex.getMessage());
						ex.printStackTrace();
						conn.rollback();
					}
				} catch (SQLException e) {
					salida.add(linea + " - BOLSA " + bolsa.DocumentoIdentidadAsociado +" "+ e.getMessage());
				}
			//});
			}
		} catch (Exception e) {
			salida.add(e.getMessage());
			e.printStackTrace();
		}
		
		return salida;
	}

}
