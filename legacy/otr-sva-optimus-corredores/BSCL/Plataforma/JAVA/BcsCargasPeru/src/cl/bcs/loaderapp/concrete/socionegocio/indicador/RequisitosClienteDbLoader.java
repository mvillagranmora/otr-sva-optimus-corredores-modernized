package cl.bcs.loaderapp.concrete.socionegocio.indicador;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RequisitosClienteDbLoader implements DbLoader {
	public String SQL_CREAR_INDICADOR = loadResourceToString(
			"cl/bcs/loaderapp/sql/socionegocio/indicador/CrearIndicador_Insert.sql");
	public String SQL_CONSULTAR_TIPO_INDICADOR = "SELECT \"CodigoTipoIndicador\", \"IdTipoIndicador\" FROM  BSCL.\"BSCL.Plataforma.BD::IND.TipoIndicador\" WHERE \"IdTipoEntidad\" = :IdTipoEntidad";

	public final static String ID_TIPO_ENTIDAD = "CLIENTE";

	public static class TipoIndicador {
		public String CodigoTipoIndicador;
		public Long IdTipoIndicador;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public class EntidadIndicador {
		public Long IdIndicador;
		public Long IdEntidad;
		public String IdTipoEntidad;
		public Long IdTipoIndicador;
		public Long IdEmpresa;
		public String ValorIndicador;
		public Long IdCarga;
		public String TipoIndicador;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public class Indicador {
		public String IUP;
		public String DOC_DEFC;
		public String DOC_DECOFON;
		public String DOC_FFATCA;
		public String DOC_PRODP;
		public String DOC_UMED;
		public String DOC_ADIREL;
		public String DOC_INSFIN;
		public String DOC_CNMILA;
		public String DOC_CNVSRI;
		public String DOC_CNVEAP;
		public String DOC_FNEMVE;
		public String DOC_CNMANA;
		public String DOC_IPEP;
		public int LINEA;

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

		try (Connection con = jsJB.getConnection()) {
			SQL sql = new SQL();
			long idCarga = in.IdCarga;
			// obtener tipos indicador
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdTipoEntidad", ID_TIPO_ENTIDAD);
			List<TipoIndicador> tipoIndicadores = sql.query(con, SQL_CONSULTAR_TIPO_INDICADOR, params,
					TipoIndicador.class);
			HashMap<String, Long> tipoIndicadorMap = new HashMap<String, Long>();
			for (TipoIndicador tipoIndicador : tipoIndicadores) {
				tipoIndicadorMap.put(tipoIndicador.CodigoTipoIndicador, tipoIndicador.IdTipoIndicador);
			}

			List<CSVRecord> recordsCSV = (List<CSVRecord>) (Object) lines;
			List<Indicador> indicadores = new ArrayList<Indicador>();
			int inlinea = 0;
			for (CSVRecord csvRecord : recordsCSV) {
				Indicador indicador = new Indicador();
				indicador.IUP = csvRecord.get("IUP");
				indicador.DOC_DEFC = csvRecord.get("DOC_DEFC");
				indicador.DOC_DECOFON = csvRecord.get("DOC_DECOFON");
				indicador.DOC_FFATCA = csvRecord.get("DOC_FFATCA");
				indicador.DOC_PRODP = csvRecord.get("DOC_PRODP");
				indicador.DOC_UMED = csvRecord.get("DOC_UMED");
				indicador.DOC_ADIREL = csvRecord.get("DOC_ADIREL");
				indicador.DOC_INSFIN = csvRecord.get("DOC_INSFIN");
				indicador.DOC_CNMILA = csvRecord.get("DOC_CNMILA");
				indicador.DOC_CNVSRI = csvRecord.get("DOC_CNVSRI");
				indicador.DOC_CNVEAP = csvRecord.get("DOC_CNVEAP");
				indicador.DOC_FNEMVE = csvRecord.get("DOC_FNEMVE");
				indicador.DOC_CNMANA = csvRecord.get("DOC_CNMANA");
				indicador.DOC_IPEP = csvRecord.get("DOC_IPEP");
				indicador.LINEA = inlinea++;
				indicadores.add(indicador);
			}

			indicadores.parallelStream().forEach((indicacador) -> {

				String mensaje = "";
				int linea = indicacador.LINEA;
				try (Connection conn = jsJB.getConnection()) {
					try {
						// Identificar IdCLiente mediante el documento de identidad.
						ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();

						SalidaSocioNegocio idsSocioNegocio = consDocCli.logica(conn, indicacador.IUP, "IUP", "",
								idEmpresaDefecto);
						if (idsSocioNegocio.IdCliente != null) {
							List<Long> idIndicadores = Utils.getSecuencia(conn,
									"BSCL.Plataforma.BD.Secuencias::Indicador", (long) 13);

							List<EntidadIndicador> insertIndicadores = new ArrayList<EntidadIndicador>();
							// cliente existe crear sus indicadores
							EntidadIndicador ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_DEFC");
							ind.TipoIndicador = "DOC_DEFC";
							ind.ValorIndicador = indicacador.DOC_DEFC;
							ind.IdEmpresa = in.p_IdEmpresa;
							insertIndicadores.add(ind);
							// DOC_DECOFON
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_DECOFON");
							ind.ValorIndicador = indicacador.DOC_DECOFON;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_DECOFON";
							insertIndicadores.add(ind);
							// DOC_FFATCA
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_FFATCA");
							ind.ValorIndicador = indicacador.DOC_FFATCA;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_FFATCA";
							insertIndicadores.add(ind);
							// DOC_PRODP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_PRODP");
							ind.ValorIndicador = indicacador.DOC_PRODP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_PRODP";
							insertIndicadores.add(ind);
							// DOC_UMED
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_UMED");
							ind.ValorIndicador = indicacador.DOC_UMED;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_UMED";
							insertIndicadores.add(ind);
							// DOC_ADIREL
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_ADIREL");
							ind.ValorIndicador = indicacador.DOC_ADIREL;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_ADIREL";
							insertIndicadores.add(ind);
							// DOC_INSFIN
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_INSFIN");
							ind.ValorIndicador = indicacador.DOC_INSFIN;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_INSFIN";
							insertIndicadores.add(ind);
							// DOC_CNMILA
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_CNMILA");
							ind.ValorIndicador = indicacador.DOC_CNMILA;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_CNMILA";
							insertIndicadores.add(ind);
							// DOC_CNVSRI
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_CNVSRI");
							ind.ValorIndicador = indicacador.DOC_CNVSRI;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_CNVSRI";
							insertIndicadores.add(ind);
							// DOC_CNVEAP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_CNVEAP");
							ind.ValorIndicador = indicacador.DOC_CNVEAP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_CNVEAP";
							insertIndicadores.add(ind);
							// DOC_FNEMVE
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_FNEMVE");
							ind.ValorIndicador = indicacador.DOC_FNEMVE;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_FNEMVE";
							insertIndicadores.add(ind);
							// DOC_CNMANA
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_CNMANA");
							ind.ValorIndicador = indicacador.DOC_CNMANA;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_CNMANA";
							insertIndicadores.add(ind);
							// DOC_IPEP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("DOC_IPEP");
							ind.ValorIndicador = indicacador.DOC_IPEP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "DOC_IPEP";
							insertIndicadores.add(ind);

							sql.batch(conn, SQL_CREAR_INDICADOR, insertIndicadores, EntidadIndicador.class);
							mensaje = (linea + " - REQUISITO CLIENTE" + indicacador.IUP + " INGRESADO CORRECTAMENTE");
							//conn.commit();
							conn.rollback();
						} else {
							mensaje = (linea + " - REQUISITO CLIENTE " + indicacador.IUP + " NO EXISTE CLIENTE");
						}
					} catch (Exception e) {
						mensaje = (linea + " - REQUISITO CLIENTE " + indicacador.IUP + " ERROR GRAVE:"
								+ e.getMessage());
					}
					conn.rollback();
				} catch (SQLException e) {
					mensaje = (linea + " - REQUISITO CLIENTE " + indicacador.IUP + " ERROR AL OBTENER CONEXION:" + e.getMessage());
				}
				System.out.println(mensaje);
				salida.add(mensaje);
			});

		} catch (Exception e) {
			salida.add(e.getMessage());
			e.printStackTrace();
		}

		return salida;
	}

}
