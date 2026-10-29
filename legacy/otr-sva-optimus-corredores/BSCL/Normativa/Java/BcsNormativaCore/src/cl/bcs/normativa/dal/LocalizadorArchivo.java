package cl.bcs.normativa.dal;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Esquema;

/**
 * M&eacute;todos utiles para determinar las rutas de los archivos del sistema.
 *
 * @author fpino@email.com
 *
 */

public final class LocalizadorArchivo {
    /**
     * Arma el nombre del archivo seg&uacute;n tipo.
     *
     * @param tipo
     *            El tipo de archivo.
     * @param idProceso
     *            El id del proceso.
     * @return El nombre del archivo del reporte.
     */
    private static String armarNombreArchivo(final String tipo, final long idProceso) {
        if (       tipo.equalsIgnoreCase("NCG380Cli")  || tipo.equalsIgnoreCase("NCG380Ord")
                || tipo.equalsIgnoreCase("NCG380Asig") || tipo.equalsIgnoreCase("NCG380Fac")
                || tipo.equalsIgnoreCase("NCG380Aut")  || tipo.equalsIgnoreCase("NCG380Tra")
                || tipo.equalsIgnoreCase("NCG380Ope")  || tipo.equalsIgnoreCase("NCG380Cus")
                || tipo.equalsIgnoreCase("NCG380Rep")  || tipo.equalsIgnoreCase("NCG380Ins")
                || tipo.equalsIgnoreCase("NCG380Deu") 
                || tipo.equalsIgnoreCase("FATCAR")     || tipo.equalsIgnoreCase("FATCAD")) {
            return String.format("%s-%d.csv", tipo, idProceso);
        }
        if (tipo.equalsIgnoreCase("INFINVRV")) return String.format("%s-%d.xls", tipo, idProceso);

        return String.format("%s-%d.txt", tipo, idProceso);
    }

    public static String buscarNombreArchivo(final String tipo, final long idProceso)
            throws SQLException {

        return armarNombreArchivo(tipo, idProceso);
    }

    public static String buscarRutaArchivo(final Connection conn, final String schema,
            final String tipo, final Documento documento, final long idProceso)
                    throws SQLException {
        final String raiz = buscarRutaRaiz(conn, schema, documento.getIdEmpresa());
        return raiz + File.separator + armarNombreArchivo(tipo, idProceso);
    }

    /**
     * Obtiene la ruta del archivo asociado al documento y proceso, seg&uacute;n
     * tipo.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param tipo
     *            El tipo de archivo.
     * @param documento
     *            El documento.
     * @param idProceso
     *            El id del proceso.
     * @param esquema
     *            El esquema.
     * @param parametros
     *            Los parametros.
     * @return La ruta del archivo del reporte.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String buscarRutaArchivo(final Connection conn, final String schema,
            final String tipo, final Documento documento, final long idProceso,
            final Esquema esquema, final Properties parametros) throws SQLException {

        String nombreArchivo = armarNombreArchivo(tipo, idProceso);
     
        if(documento.getIdTipoDocumento().compareTo("RMO") == 0) {
        	if(tipo.compareTo("generacion") == 0) {
	            final String extensionArchivo = parametros.getProperty("extensionArchivo");
	            String version = esquema.getVersion();
	            version = version.replace("/", "");
	            String nombre = documento.getIdTipoDocumento() + version;
	            nombreArchivo = String.format("%s-%d.", nombre, idProceso);
	            nombreArchivo =  nombreArchivo + extensionArchivo;
        	}
        	
        	if (tipo.compareTo("exportacion") == 0) {
        		String nombre = documento.getIdTipoDocumento() + "EXC";
				nombreArchivo = String.format("%s-%d.csv", nombre, idProceso);
			}
        }
        
        if (documento.getIdTipoDocumento().compareTo("DECLACORR") == 0) {
			if (tipo.compareTo("exportacion") == 0) {
				final DateFormat df = new SimpleDateFormat("yyyyMMdd");
				final String sf = df.format(new Date());
				nombreArchivo = String.format("%s-%s.csv", esquema.getNombre(), sf);
			}

            if (tipo.compareTo("generacion") == 0) {
                final String np = parametros.getProperty("numeroPresentacion");

                int presentacion = 1;
                if (np != null) {
                    presentacion = Integer.parseInt(np);
                }

                String numDJ= esquema.getNombre();
                String rutDeclarante = documento.getRutDeclarante();
        
                numDJ= numDJ.substring(3, numDJ.length());

                rutDeclarante=rutDeclarante.substring(0, rutDeclarante.length()-2);
                
                nombreArchivo = String.format("%s.%s",rutDeclarante , numDJ);

                if("DJ1".equals(esquema.getNombre())) {
                	String extensionArchivo = ConfiguracionDAO.buscarPorEmpresaMasCodigo(conn, schema, documento.getIdEmpresa(), "DJ1_pExtensionr");
                	nombreArchivo =  String.format("%s%s%s%s." + extensionArchivo, "RutEntidadLegal", "Periodicidad","TipoArchivo", "PeriodoComercial");
                }
            }
        }

        final String raiz = buscarRutaRaiz(conn, schema, documento.getIdEmpresa());
        return  raiz + File.separator + nombreArchivo;
    }

    /**
     * Obtiene la ruta del archivo de entrada para carga.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param documento
     *            El documento.
     * @param nombreArchivo
     *            El nombre del archivo (incluye extensi&oacute;n).
     * @return La ruta del archivo de entrada para carga.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String buscarRutaArchivoEntrada(final Connection conn, final String schema,
            final Documento documento, final String nombreArchivo) throws SQLException {
        final String raiz = buscarRutaRaiz(conn, schema, documento.getIdEmpresa());
        return raiz + "/" + nombreArchivo;
    }

    /**
     * Busca la ruta ra&iacute;z donde se mantienen los archivos de la empresa
     * indicada.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param schema
     *            El schema de la base de datos.
     * @param idEmpresa
     *            Id de la empresa.
     * @return La ruta ra&iacute;z donde se mantienen los archivos de la empresa
     *         indicada.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws RecordNotFoundException
     *             Si no existe el registro.
     */
    public static String buscarRutaRaiz(final Connection conn, final String schema,
            final long idEmpresa) throws SQLException, RecordNotFoundException {
        return ConfiguracionDAO.buscarPorEmpresaMasCodigo(conn, schema, idEmpresa, "RaizArchivos");
    }

    private LocalizadorArchivo() {
    }

}
