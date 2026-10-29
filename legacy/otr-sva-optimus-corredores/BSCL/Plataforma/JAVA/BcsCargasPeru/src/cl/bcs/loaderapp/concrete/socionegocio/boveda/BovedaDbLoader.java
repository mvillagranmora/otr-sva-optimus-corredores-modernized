package cl.bcs.loaderapp.concrete.socionegocio.boveda;

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

public class BovedaDbLoader implements DbLoader{
	public String SQL_CREAR_BOVEDA = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/boveda/CrearBoveda_Insert.sql");
	public String SQL_CONSULTAR_BOVEDA = "SELECT count(1) AS \"Existe\" FROM \"BSCL.Plataforma.BD::PLA.Boveda\" WHERE \"IdBoveda\" = :IdBoveda;";
	
	public static class Count{
		public long Existe;
	}
	
	public class Boveda{
		public String IDBOVEDA;
		public String CODIGO;
		public String NOMBRE;
		public String IDESTADO;
		public String TipoPersona;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String IDPERSONA;
		public String UBICACION;
		public int Linea;
		public String TipoDocumentoIdentidadAsociado;
		public String DocumentoIdentidadAsociado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class EntidadBoveda{
		public String IdBoveda;
		public String Codigo;
		public String Nombre;
		public String IdEstado;
		public long IdPersona;
		public String Ubicacion;
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
			List<Boveda> bovedas = new ArrayList<Boveda>();
			int inlinea = 0;
			for (CSVRecord csvRecord : recordsCSV) {
				Boveda boveda = new Boveda();
				boveda.IDBOVEDA = csvRecord.get("IDBOVEDA");
				boveda.CODIGO= csvRecord.get("CODIGO");
				boveda.NOMBRE = csvRecord.get("NOMBRE");
				boveda.IDESTADO = csvRecord.get("IDESTADO");
				boveda.TipoPersona = csvRecord.get("TipoPersona");
				boveda.TipoDocumentoIdentidadAsociado = csvRecord.get("TipoDocumentoIdentidad");
				boveda.DocumentoIdentidadAsociado = csvRecord.get("DocumentoIdentidad");

				boveda.TipoDocumentoIdentidad = "IUP";
				boveda.DocumentoIdentidad = IUP.generar(csvRecord.get("DocumentoIdentidad"), csvRecord.get("TipoDocumentoIdentidad"),csvRecord.get("TipoPersona"));
				boveda.UBICACION = csvRecord.get("UBICACION");
				boveda.Linea = inlinea++;
				bovedas.add(boveda);
			}
			
			//bovedas.parallelStream().forEach((boveda) -> {
			for (Boveda boveda : bovedas) {
				int linea = boveda.Linea;
				try (Connection conn = jsJB.getConnection()){
					try {	
						SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, idEmpresaDefecto, "CONTRAPARTE", boveda.TipoDocumentoIdentidad, boveda.DocumentoIdentidad, boveda.TipoPersona);
						HashMap<String,Object> params = new HashMap<String,Object>();
						params.put("IdBoveda", boveda.IDBOVEDA);
						List<Count> contador = sql.query(conn, SQL_CONSULTAR_BOVEDA, params, Count.class);
						if (documentoValido.Valido.equals("S")  && contador!=null && contador.get(0).Existe==0) {
							
							//Crear Persona
							CrearContraparte crearContraparte = new CrearContraparte();
							EntradaServicioCrearContraparte entradaCrearContraparte = new EntradaServicioCrearContraparte();
							entradaCrearContraparte.p_IdEmpresa = idEmpresaDefecto;
							entradaCrearContraparte.p_IdModulo = in.p_IdModulo;
							entradaCrearContraparte.p_IdUsuario = idUsuarioDefecto;
							
							Enrolamiento enr = new Enrolamiento();
							enr.IdTipoEntidad = "CONTRAPARTE";
							enr.Tipo = boveda.TipoPersona;
							if(enr.Tipo==null) {
								throw new Exception("TipoPersona es obligatorio");
							}
							enr.IdTipoDocumentoIdentidad = boveda.TipoDocumentoIdentidad;
							enr.DocumentoIdentidad = boveda.DocumentoIdentidad;
							if(enr.DocumentoIdentidad==null) {
								throw new Exception("DocumentoIdentidad es obligatorio");
							}
							enr.IdTipoDocumentoIdentidadAsociado = boveda.TipoDocumentoIdentidadAsociado;
							enr.RazonSocial = boveda.NOMBRE;
							
							enr.IdPaisNacimiento = llaves.getPais("PE");
							/*
							enr.IdPaisNacimiento = llaves.getPais(boveda.Pais);
							if(enr.IdPaisNacimiento==null) {
								throw new Exception("Pais " + boveda.Pais + ", no Existe");
							}
							*/
							
							enr.IdCiudad = llaves.getCiudad("LIMA");
							/*
							enr.IdCiudad = llaves.getCiudad(boveda.Ciudad);
							if(enr.IdCiudad==null) {
								throw new Exception("Ciudad " + boveda.Ciudad + ", no Existe");
							}
							*/
							enr.CNT_Bolsa = "N";
							enr.CNT_Boveda = "S";
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
								EntidadBoveda bvd = new EntidadBoveda();
								bvd.IdBoveda = boveda.IDBOVEDA;
								bvd.Codigo = boveda.CODIGO;
								bvd.Nombre = boveda.NOMBRE;
								bvd.IdEstado = boveda.IDESTADO;
								bvd.IdPersona = salidaCrearContraparte.P_OUT_REFIDPERSONA;
								bvd.Ubicacion = boveda.UBICACION;
								
								sql.batch(conn, SQL_CREAR_BOVEDA, Arrays.asList(bvd), EntidadBoveda.class);
																
								salida.add(linea + " - BOVEDA " + boveda.DocumentoIdentidadAsociado + " INGRESADO CORRECTAMENTE");
								conn.commit();
							}else {
								for (ErrorDetalle err : salidaCrearContraparte.P_OUT_ERRORESDETALLE) {
									System.out.println(err);
								}
								salida.add(linea + " - BOVEDA " + boveda.DocumentoIdentidadAsociado + " NO SE PUDO CREAR PERSONA ASOCIADA");	
							}
							
							salida.add(linea + " - BOVEDA " + boveda.DocumentoIdentidadAsociado + " INGRESADO CORRECTAMENTE");
							conn.commit();
						}else{
							salida.add(linea + " - BOVEDA " + boveda.DocumentoIdentidadAsociado + " YA EXISTE");
						}
						conn.rollback();
					}catch(Exception ex) {
						salida.add(linea + " - BOVEDA " + boveda.DocumentoIdentidadAsociado +" "+ ex.getMessage());
						ex.printStackTrace();
						conn.rollback();
					}
				} catch (SQLException e) {
					salida.add(linea + " - BOVEDA " + boveda.DocumentoIdentidadAsociado +" "+ e.getMessage());
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
