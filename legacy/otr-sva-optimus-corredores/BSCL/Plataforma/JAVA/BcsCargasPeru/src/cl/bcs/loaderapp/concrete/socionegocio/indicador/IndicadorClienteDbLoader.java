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

public class IndicadorClienteDbLoader implements DbLoader {
	public String SQL_CREAR_INDICADOR = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/indicador/CrearIndicador_Insert.sql");
	public String SQL_CONSULTAR_TIPO_INDICADOR = "SELECT \"CodigoTipoIndicador\", \"IdTipoIndicador\" FROM  BSCL.\"BSCL.Plataforma.BD::IND.TipoIndicador\"";

	public final static String ID_TIPO_ENTIDAD = "CLIENTE";
	public final static String ID_TIPO_ENTIDAD_POR = "PORTAFOLIO";

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
		public String INVCALIF;
		public String TIPINST;
		public String RELCORR;
		public String GENPRIMREL;
		public String GENTIPPRIRJUR;
		public String GENSEGUREL;
		public String GENPROTDAT;
		public String CAR_DIRELE;
		public String CAR_DIRFIS;
		public String FAC_DIRELE;
		public String FAC_DIRFIS;
		public String NOT_EMAIL;
		public String ORDDEFECTOESPORD;
		public String GENESTADOLABORAL;
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
			//params.put("IdTipoEntidad", ID_TIPO_ENTIDAD);
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
				indicador.INVCALIF = csvRecord.get("INVCALIF");
				indicador.TIPINST = csvRecord.get("TIPINST");
				indicador.RELCORR = csvRecord.get("RELCORR");
				indicador.GENPRIMREL = csvRecord.get("GENPRIMREL");
				indicador.GENTIPPRIRJUR = csvRecord.get("GENTIPPRIRJUR");
				indicador.GENSEGUREL = csvRecord.get("GENSEGUREL");
				indicador.GENPROTDAT = csvRecord.get("GENPROTDAT");
				indicador.CAR_DIRELE = csvRecord.get("CAR_DIRELE");
				indicador.CAR_DIRFIS = csvRecord.get("CAR_DIRFIS");
				indicador.FAC_DIRELE = csvRecord.get("FAC_DIRELE");
				indicador.FAC_DIRFIS = csvRecord.get("FAC_DIRFIS");
				indicador.NOT_EMAIL = csvRecord.get("NOT_EMAIL");
				indicador.ORDDEFECTOESPORD = csvRecord.get("ORDDEFECTOESPORD");
				indicador.GENESTADOLABORAL = csvRecord.get("GENESTADOLABORAL");
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
									"BSCL.Plataforma.BD.Secuencias::Indicador", (long) 14);

							List<EntidadIndicador> insertIndicadores = new ArrayList<EntidadIndicador>();
							// cliente existe crear sus indicadores INVCALIF
							EntidadIndicador ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("INVCALIF");
							ind.TipoIndicador = "INVCALIF";
							ind.ValorIndicador = indicacador.INVCALIF;
							ind.IdEmpresa = in.p_IdEmpresa;
							insertIndicadores.add(ind);
							// TIPINST
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("TIPINST");
							ind.ValorIndicador = indicacador.TIPINST;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "TIPINST";
							insertIndicadores.add(ind);
							// RELCORR
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("RELCORR");
							ind.ValorIndicador = indicacador.RELCORR;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "RELCORR";
							insertIndicadores.add(ind);
							// GENPRIMREL
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("GENPRIMREL");
							ind.ValorIndicador = indicacador.GENPRIMREL;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "GENPRIMREL";
							insertIndicadores.add(ind);
							// GENTIPPRIRJUR
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("GENTIPPRIRJUR");
							ind.ValorIndicador = indicacador.GENTIPPRIRJUR;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "GENTIPPRIRJUR";
							insertIndicadores.add(ind);
							// GENSEGUREL
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("GENSEGUREL");
							ind.ValorIndicador = indicacador.GENSEGUREL;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "GENSEGUREL";
							insertIndicadores.add(ind);
							// GENPROTDAT
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD_POR;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("GENPROTDAT");
							ind.ValorIndicador = indicacador.GENPROTDAT;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "GENPROTDAT";
							insertIndicadores.add(ind);
							// CAR_DIRELE
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD_POR;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("CAR_DIRELE");
							ind.ValorIndicador = indicacador.CAR_DIRELE;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "CAR_DIRELE";
							insertIndicadores.add(ind);
							// CAR_DIRFIS
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD_POR;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("CAR_DIRFIS");
							ind.ValorIndicador = indicacador.CAR_DIRFIS;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "CAR_DIRFIS";
							insertIndicadores.add(ind);
							// FAC_DIRELE
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD_POR;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FAC_DIRELE");
							ind.ValorIndicador = indicacador.FAC_DIRELE;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FAC_DIRELE";
							insertIndicadores.add(ind);
							// FAC_DIRFIS
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD_POR;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FAC_DIRFIS");
							ind.ValorIndicador = indicacador.FAC_DIRFIS;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FAC_DIRFIS";
							insertIndicadores.add(ind);
							// NOT_EMAIL
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD_POR;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("NOT_EMAIL");
							ind.ValorIndicador = indicacador.NOT_EMAIL;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "NOT_EMAIL";
							insertIndicadores.add(ind);
							// ORDDEFECTOESPORD
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD_POR;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("ORDDEFECTOESPORD");
							ind.ValorIndicador = indicacador.ORDDEFECTOESPORD;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "ORDDEFECTOESPORD";
							insertIndicadores.add(ind);
							// GENESTADOLABORAL
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdCliente;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("GENESTADOLABORAL");
							ind.ValorIndicador = indicacador.GENESTADOLABORAL;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "GENESTADOLABORAL";
							insertIndicadores.add(ind);
							
							sql.batch(conn, SQL_CREAR_INDICADOR, insertIndicadores, EntidadIndicador.class);
							mensaje = (linea + " - INDICADOR CLIENTE" + indicacador.IUP + " INGRESADO CORRECTAMENTE");
							conn.commit();

						} else {
							mensaje = (linea + " - INDICADOR CLIENTE " + indicacador.IUP + " NO EXISTE CLIENTE");
						}
					} catch (Exception e) {
						mensaje = (linea + " - INDICADOR CLIENTE " + indicacador.IUP + " ERROR GRAVE:"
								+ e.getMessage());
					}
					conn.rollback();
				} catch (SQLException e) {
					mensaje = (linea + " - INDICADOR CLIENTE " + indicacador.IUP + " ERROR AL OBTENER CONEXION:"+ e.getMessage());
				}
				salida.add(mensaje);
			});

		} catch (Exception e) {
			salida.add(e.getMessage());
			e.printStackTrace();
		}

		return salida;
	}

}
