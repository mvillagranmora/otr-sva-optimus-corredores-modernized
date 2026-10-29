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

public class IndicadorPersonaDbLoader implements DbLoader {
	public String SQL_CREAR_INDICADOR = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/indicador/CrearIndicador_Insert.sql");
	public String SQL_CONSULTAR_TIPO_INDICADOR = "SELECT \"CodigoTipoIndicador\", \"IdTipoIndicador\" FROM  BSCL.\"BSCL.Plataforma.BD::IND.TipoIndicador\"";

	public final static String ID_TIPO_ENTIDAD = "PERSONA";

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
		public String FEC_INI;
		public String FUNC_PEP;
		public String ENT_PEP;
		public String REL_PEP;
		public String FEC_TER;
		public String REL_FPU;
		public String PEP;
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
				indicador.FEC_INI = csvRecord.get("FEC_INI");
				indicador.FUNC_PEP = csvRecord.get("FUNC_PEP");
				indicador.ENT_PEP = csvRecord.get("ENT_PEP");
				indicador.REL_PEP = csvRecord.get("REL_PEP");
				indicador.FEC_TER = csvRecord.get("FEC_TER");
				indicador.REL_FPU = csvRecord.get("REL_FPU");
				indicador.PEP = csvRecord.get("PEP");
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
						if (idsSocioNegocio.IdPersona != null) {
							List<Long> idIndicadores = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Indicador", (long) 7);

							List<EntidadIndicador> insertIndicadores = new ArrayList<EntidadIndicador>();
							// cliente existe crear sus indicadores FEC_INI
							EntidadIndicador ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdPersona;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FEC_INI");
							ind.TipoIndicador = "FEC_INI";
							ind.ValorIndicador = indicacador.FEC_INI;
							ind.IdEmpresa = in.p_IdEmpresa;
							insertIndicadores.add(ind);
							// FUNC_PEP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdPersona;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FUNC_PEP");
							ind.ValorIndicador = indicacador.FUNC_PEP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FUNC_PEP";
							insertIndicadores.add(ind);
							// ENT_PEP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdPersona;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("ENT_PEP");
							ind.ValorIndicador = indicacador.ENT_PEP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "ENT_PEP";
							insertIndicadores.add(ind);
							// REL_PEP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdPersona;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("REL_PEP");
							ind.ValorIndicador = indicacador.REL_PEP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "REL_PEP";
							insertIndicadores.add(ind);
							// FEC_TER
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdPersona;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FEC_TER");
							ind.ValorIndicador = indicacador.FEC_TER;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FEC_TER";
							insertIndicadores.add(ind);
							// REL_FPU
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdPersona;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("REL_FPU");
							ind.ValorIndicador = indicacador.REL_FPU;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "REL_FPU";
							insertIndicadores.add(ind);
							// PEP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idsSocioNegocio.IdPersona;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("PEP");
							ind.ValorIndicador = indicacador.PEP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "PEP";
							insertIndicadores.add(ind);
														
							sql.batch(conn, SQL_CREAR_INDICADOR, insertIndicadores, EntidadIndicador.class);
							mensaje = (linea + " - INDICADOR PERSONA" + indicacador.IUP + " INGRESADO CORRECTAMENTE");
							conn.commit();

						} else {
							mensaje = (linea + " - INDICADOR PERSONA " + indicacador.IUP + " NO EXISTE PERSONA");
						}
					} catch (Exception e) {
						mensaje = (linea + " - INDICADOR PERSONA " + indicacador.IUP + " ERROR GRAVE:" + e.getMessage());
					}
					conn.rollback();
				} catch (SQLException e) {
					mensaje = (linea + " - INDICADOR PERSONA " + indicacador.IUP + " ERROR AL OBTENER CONEXION:"+ e.getMessage());
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
