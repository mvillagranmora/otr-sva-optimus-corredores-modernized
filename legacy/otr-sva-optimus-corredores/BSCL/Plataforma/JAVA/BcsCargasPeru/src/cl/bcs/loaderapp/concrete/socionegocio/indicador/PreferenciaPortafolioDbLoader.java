package cl.bcs.loaderapp.concrete.socionegocio.indicador;

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
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PreferenciaPortafolioDbLoader implements DbLoader {
	public String SQL_CREAR_INDICADOR = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/indicador/CrearIndicador_Insert.sql");
	public String SQL_CONSULTAR_TIPO_INDICADOR = "SELECT \"CodigoTipoIndicador\", \"IdTipoIndicador\" FROM  BSCL.\"BSCL.Plataforma.BD::IND.TipoIndicador\"";
	public String SQL_CONSULTAR_PORTAFOLIO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio\" WHERE \"DocumentoIdentidad\" = :DocumentoIdentidad and \"CodigoPortafolio\" = :CodigoPortafolio";
	public final static String ID_TIPO_ENTIDAD = "PORTAFOLIO";
	
	public static class EntidadPortafolio{
		public Long IdPortafolio;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
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
		public String CAR_ENVCDF;
		public String CAR_ENVCEM;
		public String CAR_PERIOD;
		public String FAC_ENVFEM;
		public String FAC_FLFPPR;
		public String FAC_MODFAC;
		public String FAC_SFPINS;
		public String FAC_SLFPTR;
		public String GEN_OCCINV;
		public String GEN_SOBCTA;
		public String NOT_ENVROP;
		public String ORDMODALIDAD;
		public String ORDOCUSTRF;
		public String ORDOCUSTRV;
		public String ORDVNNOMRF;
		public String ORDVNNOMRV;
		public String TES_NIE;
		public String COD_PORTAFOLIO;
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
				indicador.CAR_ENVCDF = csvRecord.get("CAR_ENVCDF");
				indicador.CAR_ENVCEM = csvRecord.get("CAR_ENVCEM");
				indicador.CAR_PERIOD = csvRecord.get("CAR_PERIOD");
				indicador.FAC_ENVFEM = csvRecord.get("FAC_ENVFEM");
				indicador.FAC_FLFPPR = csvRecord.get("FAC_FLFPPR");
				indicador.FAC_MODFAC = csvRecord.get("FAC_MODFAC");
				indicador.FAC_SFPINS = csvRecord.get("FAC_SFPINS");
				indicador.FAC_SLFPTR = csvRecord.get("FAC_SLFPTR");
				indicador.GEN_OCCINV = csvRecord.get("GEN_OCCINV");
				indicador.GEN_SOBCTA = csvRecord.get("GEN_SOBCTA");
				indicador.NOT_ENVROP = csvRecord.get("NOT_ENVROP");
				indicador.ORDMODALIDAD = parseModalidadOrden(csvRecord.get("ORDMODALIDAD"));
				indicador.ORDOCUSTRF = csvRecord.get("ORDOCUSTRF");
				indicador.ORDOCUSTRV = csvRecord.get("ORDOCUSTRV");
				indicador.ORDVNNOMRF = csvRecord.get("ORDVNNOMRF");
				indicador.ORDVNNOMRV = csvRecord.get("ORDVNNOMRV");
				indicador.TES_NIE = csvRecord.get("TES_NIE");
				indicador.COD_PORTAFOLIO = csvRecord.get("COD_PORTAFOLIO");
				indicador.LINEA = inlinea++;
				indicadores.add(indicador);
			}

			indicadores.parallelStream().forEach((indicador) -> {

				String mensaje = "";
				int linea = indicador.LINEA;
				try (Connection conn = jsJB.getConnection()) {
					try {
						// Identificar IdCLiente mediante el documento de identidad.
						ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();
						HashMap<String,Object> paramsPor = new HashMap<String, Object>(); 
						paramsPor.put("CodigoPortafolio", indicador.COD_PORTAFOLIO);
						paramsPor.put("DocumentoIdentidad", indicador.IUP);
						List<EntidadPortafolio> portafolios = sql.query(conn, SQL_CONSULTAR_PORTAFOLIO, paramsPor, EntidadPortafolio.class);
						
						if (portafolios.size()>0 && portafolios.get(0).IdPortafolio != null) {
							List<Long> idIndicadores = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Indicador", (long) 18);
							Long idEntidad = portafolios.get(0).IdPortafolio;
							
							List<EntidadIndicador> insertIndicadores = new ArrayList<EntidadIndicador>();
							// cliente existe crear sus indicadores CAR_ENVCDF
							EntidadIndicador ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("CAR_ENVCDF");
							ind.TipoIndicador = "CAR_ENVCDF";
							ind.ValorIndicador = indicador.CAR_ENVCDF;
							ind.IdEmpresa = in.p_IdEmpresa;
							insertIndicadores.add(ind);
							// CAR_ENVCEM
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("CAR_ENVCEM");
							ind.ValorIndicador = indicador.CAR_ENVCEM;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "CAR_ENVCEM";
							insertIndicadores.add(ind);
							// CAR_PERIOD
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("CAR_PERIOD");
							ind.ValorIndicador = indicador.CAR_PERIOD;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "CAR_PERIOD";
							insertIndicadores.add(ind);
							// FAC_ENVFEM
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FAC_ENVFEM");
							ind.ValorIndicador = indicador.FAC_ENVFEM;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FAC_ENVFEM";
							insertIndicadores.add(ind);
							// FAC_FLFPPR
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FAC_FLFPPR");
							ind.ValorIndicador = indicador.FAC_FLFPPR;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FAC_FLFPPR";
							insertIndicadores.add(ind);
							// FAC_MODFAC
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FAC_MODFAC");
							ind.ValorIndicador = indicador.FAC_MODFAC;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FAC_MODFAC";
							insertIndicadores.add(ind);
							// FAC_SFPINS
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FAC_SFPINS");
							ind.ValorIndicador = indicador.FAC_SFPINS;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FAC_SFPINS";
							insertIndicadores.add(ind);
							// FAC_SLFPTR
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("FAC_SLFPTR");
							ind.ValorIndicador = indicador.FAC_SLFPTR;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "FAC_SLFPTR";
							insertIndicadores.add(ind);
							// GEN_OCCINV
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("GEN_OCCINV");
							ind.ValorIndicador = indicador.GEN_OCCINV;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "GEN_OCCINV";
							insertIndicadores.add(ind);
							// GEN_SOBCTA
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("GEN_SOBCTA");
							ind.ValorIndicador = indicador.GEN_SOBCTA;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "GEN_SOBCTA";
							insertIndicadores.add(ind);
							// NOT_ENVROP
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("NOT_ENVROP");
							ind.ValorIndicador = indicador.NOT_ENVROP;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "NOT_ENVROP";
							insertIndicadores.add(ind);
							// ORDMODALIDAD
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("ORDMODALIDAD");
							ind.ValorIndicador = indicador.ORDMODALIDAD;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "ORDMODALIDAD";
							insertIndicadores.add(ind);
							// ORDOCUSTRF
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("ORDOCUSTRF");
							ind.ValorIndicador = indicador.ORDOCUSTRF;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "ORDOCUSTRF";
							insertIndicadores.add(ind);
							// ORDOCUSTRV
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("ORDOCUSTRV");
							ind.ValorIndicador = indicador.ORDOCUSTRV;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "ORDOCUSTRV";
							insertIndicadores.add(ind);
							// ORDVNNOMRF
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("ORDVNNOMRF");
							ind.ValorIndicador = indicador.ORDVNNOMRF;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "ORDVNNOMRF";
							insertIndicadores.add(ind);
							// ORDVNNOMRV
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("ORDVNNOMRV");
							ind.ValorIndicador = indicador.ORDVNNOMRV;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "ORDVNNOMRV";
							insertIndicadores.add(ind);
							// TES_NIE
							ind = new EntidadIndicador();
							ind.IdCarga = in.IdCarga;
							ind.IdEntidad = idEntidad;
							ind.IdTipoEntidad = ID_TIPO_ENTIDAD;
							ind.IdIndicador = idIndicadores.get(insertIndicadores.size());
							ind.IdTipoIndicador = tipoIndicadorMap.get("TES_NIE");
							ind.ValorIndicador = indicador.TES_NIE;
							ind.IdEmpresa = in.p_IdEmpresa;
							ind.TipoIndicador = "TES_NIE";
							insertIndicadores.add(ind);
														
							sql.batch(conn, SQL_CREAR_INDICADOR, insertIndicadores, EntidadIndicador.class);
							mensaje = (linea + " - PREFERENCIA PORTAFOLIO " + indicador.IUP + " INGRESADO CORRECTAMENTE");
							conn.commit();
						} else {
							mensaje = (linea + " - PREFERENCIA PORTAFOLIO " + indicador.IUP + " NO EXISTE CLIENTE");
						}
					} catch (Exception e) {
						mensaje = (linea + " - PREFERENCIA PORTAFOLIO " + indicador.IUP + " ERROR GRAVE:"
								+ e.getMessage());
					}
					conn.rollback();
				} catch (SQLException e) {
					mensaje = (linea + " - - PREFERENCIA PORTAFOLIO " + indicador.IUP + " ERROR AL OBTENER CONEXION:"+ e.getMessage());
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
	
	public class ModalidadOrden{
		public boolean ESCRITAS = false;
		public boolean FAXCONFIRMACIONPOSTERIOR = false;
		public boolean TELEFONICAS = false;
		public boolean APLICACIONESWEB = false;
		public boolean MERCADOINTEGRADO = false;
		public boolean REDPRIVADA = false;
		public boolean EMAIL = false;
		public boolean DMA = false;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	private String parseModalidadOrden(String modalidadOrden) {
		ModalidadOrden modalidad = new ModalidadOrden();
		List<String> modalidadesPreferencia = Arrays.asList(((modalidadOrden!=null && modalidadOrden.length()>0) ? modalidadOrden.toUpperCase().replaceAll(" ", "") : "").split(","));
	
		if(modalidadesPreferencia!=null) {
			if(modalidadesPreferencia.contains("ESCRITA")) {
				modalidad.ESCRITAS=true;
			}
			if(modalidadesPreferencia.contains("FAX")) {
				modalidad.FAXCONFIRMACIONPOSTERIOR=true;
			}
			if(modalidadesPreferencia.contains("TELEFONO")) {
				modalidad.TELEFONICAS=true;
			}
			if(modalidadesPreferencia.contains("APLICACIONES WEB")) {
				modalidad.APLICACIONESWEB=true;
			}
			if(modalidadesPreferencia.contains("MERCADOINTEGRADO")) {
				modalidad.MERCADOINTEGRADO=true;
			}
			if(modalidadesPreferencia.contains("REDPRIVADA")) {
				modalidad.REDPRIVADA=true;
			}
			if(modalidadesPreferencia.contains("CORREOELECTRÓNICO")) {
				modalidad.EMAIL=true;
			}
			if(modalidadesPreferencia.contains("ADM")) {
				modalidad.DMA=true;
			}
		}
		return modalidad.toString();
	}
}
