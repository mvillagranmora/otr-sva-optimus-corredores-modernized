package cl.bcs.normativa.carga;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;

import cl.bcs.normativa.dal.Nombres;
import cl.bcs.normativa.dal.ResumenCargaDAO;
import cl.bcs.normativa.data.DataParserException;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.data.RegistroMapper;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.esquema.ResumenCarga;
import cl.bcs.normativa.interfaces.CambiarEstadoDJCargar;
import cl.bcs.normativa.lectores.Lector;
import cl.bcs.normativa.lectores.LectorBuilder;
import cl.bcs.normativa.lectores.LectorCSV;
import cl.bcs.normativa.procesos.ContextoProceso;
import cl.bcs.normativa.procesos.ControladorProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.normativa.validacion.Validador;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.utils.PropertiesUtil;
import cl.bcs.utils.StringUtils;

/**
 * Implementaci&oacute;n de la carga de datos de un documento.
 *
 * @author fpino@email.com
 *
 */
public class ProcesoCarga implements Proceso {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();
    
    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoCarga.class.getName());

    /**
     * Obtiene el comando SELECT para obtener las claves de los registros
     * existentes en la tabla destino.
     *
     * @param contexto
     *            El contexto.
     * @param tablaDestino
     *            La tabla destino.
     * @return El comando SELECT para obtener las claves de los registros
     *         existentes en la tabla destino.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private static String armarSelectClavesDestino(
            final ContextoProceso contexto,
            final SqlTable tablaDestino)
                    throws SQLException {
        final List<String> items = new ArrayList<>();
        for (final Campo campoClave : contexto.getEsquema().getCamposClaves()) {
            final String nombre = campoClave.getNombre();
            items.add(GENERATOR.getSqlText(nombre));
        }

        final List<String> filtros = new ArrayList<>();
        filtros.add(String.format("%s=%d", GENERATOR.getSqlText("IdDocumento"),
                contexto.getDocumento().getId()));

        final String sql = String.format("SELECT DISTINCT %s FROM %s WHERE %s",
                StringUtils.join(",", items), GENERATOR.getSqlText(tablaDestino),
                StringUtils.join(" AND ", filtros));
        return sql;
    }
    
    /**
     * Obtiene el comando SELECT para obtener las claves de las operaciones
     * spot existentes en el sistema.
     *
     * @param idOperacionSpot
     *            El identificador de la operacion spot.
     * @param tablaOperacionSpot
     *            La tabla que guarda operaciones spot de sistema.
     * @return El comando SELECT para obtener las claves de las operaciones
     *         spot existentes en el sistema.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private static String armarSelectRegistroSpotSistema(
            final Long idOperacionSpot,
            final SqlTable tablaOperacionSpot)
                    throws SQLException {
        final List<String> items = new ArrayList<>();
        final String nombre = "IdOperacionSpot";
        items.add(GENERATOR.getSqlText(nombre));
        

        final List<String> filtros = new ArrayList<>();
        filtros.add(String.format("%s=%d", GENERATOR.getSqlText("IdOperacionSpot"),
        		idOperacionSpot));
        
        final String sql = String.format("SELECT DISTINCT %s FROM %s WHERE %s",
                StringUtils.join(",", items), GENERATOR.getSqlText(tablaOperacionSpot),
                StringUtils.join(" AND ", filtros));
        return sql;
    }

    /**
     * Obtiene instancia de {@link ProcesoCarga}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param map
     *            {@link Map} de par&aacute;metros proporcionado por el Job
     *            Manager.
     * @param idProceso
     *            El id del proceso.
     * @return Instancia de {@link ProcesoCarga}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static ProcesoCarga newInstance(
            final Connection conn,
            final Map<String, String> map,
            final long idProceso,
            final DataSource ds)
                    throws SQLException {
        final Properties props = PropertiesUtil.newProperties(map);
        props.setProperty("idProceso", String.format("%d", idProceso));
        return newInstance(conn, props, idProceso, ds);
    }

    /**
     * Obtiene instancia de {@link ProcesoCarga}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            Las {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @return Instancia de {@link ProcesoCarga}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static ProcesoCarga newInstance(
            final Connection conn,
            final Properties props,
            final long idProceso,
            final DataSource ds)
                    throws SQLException {
        return new ProcesoCarga(conn, props, idProceso, ds);
    }

    /**
     * Obtiene instancia de {@link ResumenCarga}.
     *
     * @param contexto
     *            El contexto de carga.
     * @return Instancia de {@link ResumenCarga}.
     */
    private static ResumenCarga newResumenCarga(
            final ContextoCarga contexto) {
        final Documento documento = contexto.getDocumento();
        final ResumenCarga resumen = new ResumenCarga();
        resumen.setFecha(new Date());
        resumen.setIdProceso(contexto.getIdProceso());
        resumen.setIdDocumento(documento.getId());
        resumen.setIdUsuario(contexto.getUsuario().getId());
        resumen.setNombreArchivo(FilenameUtils.getName(contexto.getRutaArchivo()));
        resumen.setGlosa("Iniciada");
        return resumen;
    }

    /**
     * Lista de las llaves existentes en la tabla destino.
     */
    private Set<String> clavesDestino;

    /**
     * Contexto de la carga.
     */
    private final ContextoCarga contexto;

    /**
     * Para control de concurrencia.
     */
    private final Object mutexClavesDestino = new Object();

    /**
     * Tabla de destino de la carga.
     */
    private final SqlTable tablaDestino;
    
    /**
     * Tabla para revisar folios de operaciones Spot.
     */
    private final SqlTable tablaSpotSistema;

    /**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            Las {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ProcesoCarga(
            final Connection conn,
            final Properties props,
            final long idProceso,
            final DataSource ds) throws SQLException {
        this.contexto = new ContextoCarga(conn, props, idProceso, ds);
        this.tablaDestino = getTablaDestino(contexto.getEsquema(), contexto.getSchema());
        this.tablaSpotSistema = SqlMetadata.newTable(contexto.getSchema(), Nombres.NOMBRE_TABLA_OPERACIONES_SPOT);
    }

    /**
     * Actualiza el resumen de la carga.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    private void actualizarResumen(
            final ResumenCarga resumen)
                    throws SQLException {
        contexto.getConnection().setAutoCommit(true);
        ResumenCargaDAO.actualizar(contexto.getConnection(), contexto.getSchema(), resumen);
    }

    /**
     * Borrar la lista de datos en la tabla destino.
     *
     * @param registros
     *            La lista de registros.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private void borrar(
            final List<Registro> registros)
                    throws SQLException {
        final Borrador borrador = new Borrador(contexto, tablaDestino);

        contexto.getConnection().setAutoCommit(false);
        for (final Registro registro : registros) {
            borrador.borrar(registro);
        }

        borrador.endBatch();
        LOGGER.info(String.format("%d registros eliminados", borrador.getCommited()));
    }

    /**
     * Busca las claves de los registros en la tabla destino.
     *
     * @return Las claves de los registros en la tabla destino.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public Set<String> buscarClavesDestino()
            throws SQLException,
            IOException {
        final String sql = armarSelectClavesDestino(contexto, tablaDestino);
        LOGGER.info(sql);

        final Set<String> set = new HashSet<>();
        try (final PreparedStatement statement = contexto.getConnection().prepareStatement(sql);
                final ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                final Registro registro = RegistroMapper.map(contexto.getEsquema(), rs);
                final String key = registro.getClave();
                set.add(key);
            }
        }

        return set;
    }
    
    /**
     * Busca si existe registro en tabla de operaciones spot.
     *
     * @return true si existe el registro.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public boolean buscarRegistroSpotSistemaById(Long idOperacionSpot)
            throws SQLException,
            IOException {
        final String sql = armarSelectRegistroSpotSistema(idOperacionSpot, tablaSpotSistema);
        LOGGER.info(sql);
       
        boolean existe = false;

        try (final PreparedStatement statement = contexto.getConnection().prepareStatement(sql);
                final ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
            	existe = true;
            }
        }
        LOGGER.info("Existe: " + existe);
        return existe;
    }

    /**
     * Implementa el proceso de carga.
     *
     * @param reporte
     *            El reporte.
     *
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private void cargar(
            final ReporteCarga reporte)
                    throws SQLException {
        final ResumenCarga resumen = crearResumen();
        try {
            try {
                cargarDatos(resumen, reporte);
                //Se actualiza estado de proceso de la DJ
                CambiarEstadoDJCargar cambiarEstadoDJCargar = new CambiarEstadoDJCargar();
                CambiarEstadoDJCargar.InCambiarEstadoDJ inCambiarEstadoDJ = new CambiarEstadoDJCargar.InCambiarEstadoDJ();
                inCambiarEstadoDJ.Esquema = contexto.getEsquema().getNombre();
                inCambiarEstadoDJ.Version = contexto.getEsquema().getVersion();
                cambiarEstadoDJCargar.logica(contexto.getConnection(), inCambiarEstadoDJ);
                resumen.setGlosa("Finalizada");
                if (contexto.getEsquema().getNombre().equals("DJ1") && contexto.getOpciones().getTipoCarga().toString().equals("INICIAL")) {
                	ingresaCargaDJ1Pagos(contexto.getConnection());	
                }
            } catch (final Exception ex) {
                final String msg = "Error al procesar carga";
                reporte.println(msg);
                reporte.println(ex.toString());
                resumen.setGlosa("Interrumpida");
                LOGGER.log(Level.SEVERE, msg, ex);
            }

            reporte.printResumen(resumen);
        } finally {
            actualizarResumen(resumen);
        }
    }

    /**
     * Implementa la porci&oacute;n central del proceso de carga.
     *
     * @param resumen
     *            El resumen.
     * @param reporte
     *            El reporte.
     * @throws Exception 
     */
    private void cargarDatos(
            final ResumenCarga resumen,
            final ReporteCarga reporte)
                    throws Exception {
        final List<Registro> registros = leerDatos(resumen, reporte);
        if (registros.isEmpty()) {
            LOGGER.info(String.format("No hay registros para procesar"));
        } else if (contexto.getOpciones().isGrabar()) {
            if (contexto.getOpciones().isReemplazar()) {
                borrar(registros);
            }
            grabar(registros, resumen);
        }
    }

    /**
     * Crea resumen.
     *
     * @return Nueva instancia de {@link ResumenCarga}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private ResumenCarga crearResumen()
            throws SQLException {
        final ResumenCarga resumen = newResumenCarga(contexto);
        contexto.getConnection().setAutoCommit(true);
        ResumenCargaDAO.insertar(contexto.getConnection(), contexto.getSchema(), resumen);
        return resumen;
    }

    /**
     * Obtiene el conjunto de claves de los registros de la tabla destino.
     *
     * @return El conjunto de claves de los registros de la tabla destino.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private Set<String> getClavesDestino()
            throws SQLException,
            IOException {
        synchronized (mutexClavesDestino) {
            if (clavesDestino == null) {
                clavesDestino = buscarClavesDestino();
            }

            return clavesDestino;
        }
    }
    
    private SqlTable getTablaDestino(
            final Esquema esquema,
            final String schema)
                    throws SQLException {
        return contexto.getMetadata().getTable(schema, esquema.getNombreTablaDestino());
    }

    /**
     * Obtiene la tabla origen y chequea su compatibilidad con el esquema.
     *
     * @return La tabla origen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private SqlTable getTablaOrigen()
            throws SQLException {
        final Esquema esquema = contexto.getEsquema();
        final String nombre = esquema.getNombreTablaOrigen();
        final SqlTable tabla = contexto.getMetadata().getTable(contexto.getSchema(), nombre);

        esquema.chequearCompatibilidad(tabla);
        return tabla;
    }

    /**
     * Graba la lista de datos en la tabla destino.
     *
     * @param registros
     *            La lista de registros.
     * @param resumen
     *            El resumen.
     * @throws Exception 
     */
    private void grabar(
            final List<Registro> registros,
            final ResumenCarga resumen)
                    throws Exception {
        final Grabador grabador = new Grabador(contexto, tablaDestino);
        contexto.getConnection().setAutoCommit(false);
        for (final Registro registro : registros) {
        	if (yaExiste(registro)) {
                if (contexto.getOpciones().isReemplazar()) {
                    grabador.grabar(registro);
                }
            } else {
                grabador.grabar(registro);
            }
        }

        grabador.endBatch();
        resumen.setTotalRegistrosCargados(grabador.getCommited());
        LOGGER.info(String.format("%d registros grabados", grabador.getCommited()));
    }

    /**
     * Lee los datos desde el origen.
     *
     * @param resumen
     *            El resumen.
     * @param reporte
     *            El reporte.
     * @return Lista de registro de datos desde el origen.
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     */
    private List<Registro> leerDatos(
            final ResumenCarga resumen,
            final ReporteCarga reporte)
                    throws IOException,
                    SQLException,
                    DataParserException {
        final List<Registro> registros = new ArrayList<>();

        try (final Lector lector = newLector()) {
            Registro registro = leerSiguiente(lector, resumen, reporte);
            while (registro != null) {
                registros.add(registro);
                registro = leerSiguiente(lector, resumen, reporte);
            }

            resumen.setTotalRegistrosLeidos(lector.getCantidadRegistrosLeidos());
        }

        return registros;
    }

    /**
     * Lee el siguiente registro.
     *
     * @param lector
     *            El lector.
     * @param resumen
     *            El resumen.
     * @param reporte
     *            El reporte.
     * @return El siguiente registro desde el origen, o <code>null</code> en el
     *         caso de que no haya m&aacute;s registros.
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private Registro leerSiguiente(
            final Lector lector,
            final ResumenCarga resumen,
            final ReporteCarga reporte)
                    throws IOException,
                    SQLException {
        Registro registro;
        while (true) {
            registro = lector.siguiente();
            if (registro == null) {
                return registro;
            }
            
            if(contexto.getEsquema().getNombre().equals("DJ999") && contexto.getOpciones().getTipoCarga().toString().equals("EXTERNA")) {
            	final long folioOperacion = (Long) registro.getPorNombre("IdOperacionSpot") == null ? 0: (Long) registro.getPorNombre("IdOperacionSpot");
            	if(folioOperacion != 0) {
            		if (!yaExiste(registro)) {
            			final String msg = String.format("Registro %d: %s",
  	                          lector.getCantidadRegistrosLeidos(), "No existe el registro externo que se requiere modificar con FolioOperacion " + folioOperacion);
            			reporte.println(msg);
            			resumen.contarRegistrosErroneos();
            			return null;
            		}
            	}
            	
            	if(esRegistroSistema(folioOperacion)){
	            	final String msg = String.format("Registro %d: %s",
	                          lector.getCantidadRegistrosLeidos(), "No se puede cargar de forma externa un FolioOperacion asociado a un registro de sistema");
	                reporte.println(msg);
	                resumen.contarRegistrosErroneos();
	                return null;
            	}
            }
            
			if (registro.isValido()) {
				if (Validador.validarFecha(contexto.getEsquema(), registro)) {
					return registro;
				} else {
					final String msg = String.format("Registro %d: %s", lector.getCantidadRegistrosLeidos(),
							"Fecha de registro faltante o invalida");
					reporte.println(msg);
					resumen.contarRegistrosErroneos();
				}
			} else {
				final String msg = String.format("Registro %d: %s", lector.getCantidadRegistrosLeidos(),
						registro.getRazonInvalidez());
				reporte.println(msg);
				resumen.contarRegistrosErroneos();
			}
            
        }
    }

    /**
     * Obtiene nuevo lector de los datos del origen.
     *
     * @return Nuevo lector de los datos del origen.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws ProcesoException
     *             En caso de error de proceso.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private Lector newLector()
            throws SQLException,
            ProcesoException,
            DataParserException,
            IOException {
        switch (contexto.getOpciones().getTipoCarga()) {
        case INICIAL:
            return LectorBuilder.newLectorOrigen(contexto, getTablaOrigen(), null);
        case EXTERNA:
            return new LectorCSV(contexto);
        default:
            throw new ProcesoException(
                    "Tipo de carga no soportado: " + contexto.getOpciones().getTipoCarga());
        }
    }

    @Override
    public void procesar()
            throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso de carga"));

        final String rutaReporte = contexto.buscarRutaReporte();
        try (final PrintStream out = new PrintStream(rutaReporte)) {
            final ReporteCarga reporte = new ReporteCarga(out);
            reporte.printEncabezado(contexto);

            try (final ControladorProceso controlador = new ControladorProceso(contexto)) {
                try {
                    cargar(reporte);
                    //Se actualiza estado de proceso de la DJ
                    CambiarEstadoDJCargar cambiarEstadoDJCargar = new CambiarEstadoDJCargar();
                    CambiarEstadoDJCargar.InCambiarEstadoDJ inCambiarEstadoDJ = new CambiarEstadoDJCargar.InCambiarEstadoDJ();
                    inCambiarEstadoDJ.Esquema = contexto.getEsquema().getNombre();
                    inCambiarEstadoDJ.Version = contexto.getEsquema().getVersion();
                    try {
						cambiarEstadoDJCargar.logica(contexto.getConnection(), inCambiarEstadoDJ);
					} catch (Exception e) {
						final String msg = "Error al actualizar el estado de procesos de la DJ";
			            LOGGER.log(Level.SEVERE, msg, e);
			            throw new ProcesoException(msg, e);
					}
                } catch (final SQLException ex) {
                    final String msg = "Error al procesar carga";
                    LOGGER.log(Level.SEVERE, msg, ex);
                    throw new ProcesoException(msg, ex);
                }
            }
        } catch (final FileNotFoundException ex) {
            final String msg = "Error al abrir archivo de reporte";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProcesoException(msg, ex);
        }
    }

    /**
     * Chequea si ya existe el registro en la tabla destino.
     *
     * @param registro
     *            El registro de datos.
     * @return <code>true</code> si ya existe el registro en la tabla destino,
     *         <code>false</code> en caso contrario.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private boolean yaExiste(
            final Registro registro)
                    throws SQLException,
                    IOException {
        return getClavesDestino().contains(registro.getClave());
    }
    
    private boolean esRegistroSistema(
            final Long idOperacionSpot)
                    throws SQLException,
                    IOException {
        return buscarRegistroSpotSistemaById(idOperacionSpot);
    }
    
    private boolean ingresaCargaDJ1Pagos(Connection conn) throws Exception {
    	LOGGER.info("Se ingresa tarea para cargar pagos DJ1");
    	
    	String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(conn, contexto.getEmpresa().getId(),"PLATAFORMA", "PLA.FechaSistema");
    	
    	List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
    	
    	Long IdEmpresa = contexto.getEmpresa().getId();
    	Long IdUsuario = contexto.getUsuario().getId();
    	Long IdDocumento = contexto.getDocumento().getId();
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramIdEmpresa.NombreParametro 	= "idEmpresa";
    	paramIdEmpresa.Valor			= Long.toString(IdEmpresa);
    	paramsParametros.add(paramIdEmpresa);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramIdDocumento = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramIdDocumento.NombreParametro 	= "idDocumento";
    	paramIdDocumento.Valor			= Long.toString(IdDocumento);
    	paramsParametros.add(paramIdDocumento);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramIdUsuario.NombreParametro 	= "idUsuario";
    	paramIdUsuario.Valor			= Long.toString(IdUsuario);
    	paramsParametros.add(paramIdUsuario);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramEsquema = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramEsquema.NombreParametro 	= "schema";
    	paramEsquema.Valor			= contexto.getSchema();
    	paramsParametros.add(paramEsquema);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramGrabar = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramGrabar.NombreParametro 	= "grabar";
    	paramGrabar.Valor			= contexto.getOpciones().isGrabar()?"true":"false";
    	paramsParametros.add(paramGrabar);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramReemplazar = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramReemplazar.NombreParametro 	= "reemplazar";
    	paramReemplazar.Valor			= contexto.getOpciones().isReemplazar()?"true":"false";
    	paramsParametros.add(paramReemplazar);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramAceptarFechasNull = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramAceptarFechasNull.NombreParametro 	= "aceptarFechasNull";
    	paramAceptarFechasNull.Valor			= PropertiesUtil.getRequiredString(contexto.getParametros(), "aceptarFechasNull");
    	paramsParametros.add(paramAceptarFechasNull);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramTipoCarga = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramTipoCarga.NombreParametro 	= "tipoCarga";
    	paramTipoCarga.Valor			= PropertiesUtil.getRequiredString(contexto.getParametros(), "tipoCarga");
    	paramsParametros.add(paramTipoCarga);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramFechaDesde = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramFechaDesde.NombreParametro 	= "fechaDesde";
    	paramFechaDesde.Valor			= PropertiesUtil.getOptional(contexto.getParametros(), "fechaDesde", "0001-01-01");
    	paramsParametros.add(paramFechaDesde);
    	
    	AgregarPrograma.InAgregarPrograma.Parametro	paramFechaHasta = new AgregarPrograma.InAgregarPrograma.Parametro();
    	paramFechaHasta.NombreParametro 	= "fechaHasta";
    	paramFechaHasta.Valor			= PropertiesUtil.getOptional(contexto.getParametros(), "fechaHasta", "0001-01-01");
    	paramsParametros.add(paramFechaHasta);
    	
    	AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma();
    	paramsIn.p_IdEmpresa		= IdEmpresa;
    	paramsIn.p_IdModulo			= "TRI";
    	paramsIn.p_IdUsuario		= IdUsuario;
    	paramsIn.p_IdTipoPrograma	= "CargaDJ1Pagos";
    	paramsIn.p_Segundos			= 0L;
    	paramsIn.p_FechaSistema		= FechaSistema;
    	paramsIn.p_Parametros		= paramsParametros;
    	AgregarPrograma AP = new AgregarPrograma();
    	AgregarPrograma.OutAgregarPrograma result = AP.logica(conn, paramsIn);
    	
    	if ( result.P_OUT_ERRORESCONTADOR.equals(1L) ){
    		LOGGER.info("Error al crear programa");
    		return false;
    	} else {
    		LOGGER.info(String.format("Id programa Carga DJ1 Pagos {0}",result.P_OUT_IDPROGRAMA));
    		return true;
    	}
    }
}
