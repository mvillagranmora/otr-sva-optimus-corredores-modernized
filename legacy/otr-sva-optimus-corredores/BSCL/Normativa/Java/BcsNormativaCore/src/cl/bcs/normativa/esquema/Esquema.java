package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.bcs.sql.metadata.SqlTable;

/**
 * Representa el esquema de un documento (incluye la especificaci&oacute;n de
 * campos).
 *
 * @author fpino@email.com
 *
 */
public class Esquema implements Serializable {

    /**
     * Formato del nombre de la tabla/vista origen.
     */
    private static final String FORMATO_ORIGEN = "BSCL.Normativa.BD.Vistas::%s.%s.Origen";

    /**
     * Prefijo del nombre de la tabla destino.
     */
    private static final String PREFIJO_DESTINO = "BSCL.Normativa.BD::%s.Datos%s";
    
    /**
     * Prefijo del nombre de la tabla/vista datos mostrados en grilla del mantenedor.
     */
    private static final String FORMATO_VISTA_DESTINO = "BSCL.Normativa.BD.Vistas::%s.%s.Datos";


    private static final long serialVersionUID = 1L;

    /**
     * Lista de los campos del esquema.
     */
    private List<Campo> campos;

    /**
     * Lista de los campos claves del esquema.
     */
    private List<Campo> camposClaves;

    /**
     * Lista de los campos exportables del esquema.
     */
    private List<Campo> camposExportables;

    /**
     * Lista de los campos comunes del esquema.
     */
    private List<Campo> camposComunes;

    /**
     * Lista de los campos datos del esquema.
     */
    private List<Campo> camposDatos;

    /**
     * C&oacute;digo de versi&oacute;n SII.
     */
    private int codigoVersionSII;

    /**
     * Fecha l&iacute;mite de presentaci&oacute;n del documento a la entidad
     * reguladora.
     */
    private Date fechaPresentacionLimite;

    /**
     * Identificador de la entidad reguladora.
     */
    private String idEntidadReguladora;

    /**
     * M&oacute;dulo de aplicaci&oacute;n.
     */
    private String modulo;

    /**
     * Para control de concurrencia.
     */
    private final Object mutexCampos = new Object();

    /**
     * Para control de concurrencia.
     */
    private final Object mutexCamposClaves = new Object();

    /**
     * Para control de concurrencia.
     */
    private final Object mutexCamposExportables = new Object();

    /**
     * Para control de concurrencia.
     */
    private final Object mutexCamposComunes = new Object();

    /**
     * Para control de concurrencia.
     */
    private final Object mutexCamposDatos = new Object();

    /**
     * Para control de concurrencia.
     */
    private final Object mutexNombreCampoFecha = new Object();

    /**
     * Para control de concurrencia.
     */
    private final Object mutexNombreCampoRutCliente = new Object();

    /**
     * Nombre del esquema, correspondiente al primer elemento de la
     * identificaci&oacute;n del esquema.
     */
    private String nombre;

    /**
     * Nombre del campo que contiene la fecha del registro de datos de un
     * documento.
     */
    private String nombreCampoFecha;

    /**
     * Nombre del campo que contiene el RUT del Cliente del registro de datos de
     * un documento.
     */
    private String nombreCampoRutCliente;

    /**
     * Objetivo del esquema.
     */
    private String objetivo;

    /**
     * Periodicidad del esquema.
     */
    private String periodicidad;

    /**
     * Primer periodo del esquema.
     */
    private Date periodoInicio;

    /**
     * Rango de fechas del esquema.
     */
    private RangoFechas rangoFechas;

    /**
     * Versi&oacute;n del esquema, correspondiente al segundo elemento de la
     * identificaci&oacute;n del esquema.
     */
    private String version;

    /**
     * Obtiene el campo correspondiente a la carga externa en la posici&oacute;n
     * (cuenta desde 1) indicada en el registro de entrada.
     *
     * @param n
     *            La posici&oacute;n del campo en el registro de entrada.
     * @return El campo correspondiente a la carga externa en la posici&oacute;n
     *         (cuenta desde 1) indicada en el registro de entrada.
     */
    public Campo buscarCampoCargaExterna(final int n) {
        for (final Campo campo : getCampos()) {
            if (campo.getOrdenCargaExterna() == n) {
                return campo;
            }
        }

        return null;
    }

    /**
     * Busca el campo dado el c&oacute;digo SII.
     *
     * @param codigoSII
     *            El c&oacute;digo SII.
     * @return El campo dado el c&oacute;digo SII.
     */
    public Campo buscarCampoPorCodigoSII(final String codigoSII) {
        for (final Campo campo : getCampos()) {
            if (campo.getCodigoSII().equals(codigoSII)) {
                return campo;
            }
        }

        throw new EsquemaException("No se encontro campo con codigo SII: " + codigoSII);
    }

    /**
     * Busca el campo por nombre.
     *
     * @param nombreCampo
     *            El nombre del campo.
     * @return El campo correspondiente al nombre o, <code>null</code> si no
     *         existe.
     */
    public Campo buscarCampoPorNombre(final String nombreCampo) {
        for (final Campo campo : getCampos()) {
            if (campo.getNombre().equals(nombreCampo)) {
                return campo;
            }
        }

        throw new EsquemaException("No esta especificado el campo: " + nombreCampo);
    }

    /**
     * Busca la lista de campos claves del esquema.
     *
     * @return La lista de campos claves del esquema.
     */
    private List<Campo> buscarCamposClaves() {
        final List<Campo> list = new ArrayList<>();
        for (final Campo campoDato : getCamposDatos()) {
            if (campoDato.isClave()) {
                list.add(campoDato);
            }
        }

        return list;
    }

    /**
     * Busca la lista de campos comunes del esquema.
     *
     * @return La lista de campos comunes del esquema.
     */
    private List<Campo> buscarCamposComunes() {
        final List<Campo> list = new ArrayList<>();
        for (final Campo campo : getCampos()) {
            if (campo.isComun()) {
                list.add(campo);
            }
        }

        return list;
    }

    /**
     * Busca la lista de campos datos del esquema.
     *
     * @return La lista de campos datos del esquema.
     */
    private List<Campo> buscarCamposDatos() {
        final List<Campo> list = new ArrayList<>();
        for (final Campo campo : getCampos()) {
            if (!campo.isComun()) {
                list.add(campo);
            }
        }

        return list;
    }

    /**
     * Busca la lista de campos exportables del esquema.
     *
     * @return La lista de campos exportables del esquema.
     */
    private List<Campo> buscarCamposExportables() {
        final List<Campo> list = new ArrayList<>();
        for (final Campo campo : getCampos()) {
            if (campo.isExportable()) {
                list.add(campo);
            }
        }

        return list;
    }

    /**
     * Busca el nombre del campo correspondiente a la fecha del registro.
     *
     * @return El nombre del campo correspondiente a la fecha del registro.
     */
    private String buscarNombreCampoFecha() {
        for (final Campo campo : getCampos()) {
            if (campo.getTipoDato().equals("DATE_FILTER")) {
                return campo.getNombre();
            }
        }

        return null;
    }

    /**
     * Busca el nombre del campo correspondiente al RUT del cliente.
     *
     * @return El nombre del campo correspondiente al RUT del cliente.
     */
    private String buscarNombreCampoRutCliente() {
        for (final Campo campo : getCampos()) {
            if (campo.getTipoDato().equals("RUT_CLIENTE")) {
                return campo.getNombre();
            }
        }

        return null;
    }

    /**
     * Chequea la compatibilidad de la tabla respecto del esquema.
     *
     * @param tabla
     *            La tabla.
     */
    public void chequearCompatibilidad(final SqlTable tabla) {
        for (final Campo campoDato : getCamposDatos()) {
            final String nombreCampo = campoDato.getNombre();
            if (!tabla.containsColumn(nombreCampo)) {
                final String msg = String.format("No existe la columna %s en la tabla/vista %s",
                        nombreCampo, tabla.getName());
                throw new EsquemaException(msg);
            }
        }
    }

    /**
     * Obtiene la lista de campos del esquema.
     *
     * @return La lista de campos del esquema.
     */
    public List<Campo> getCampos() {
        synchronized (mutexCampos) {
            if (campos.isEmpty()) {
                final String msg = String.format("No hay campos definidos para %s-%s", this.nombre,
                        this.version);
                throw new EsquemaException(msg);
            }

            return campos;
        }
    }

    /**
     * Obtiene la lista de campos claves del esquema.
     *
     * @return La lista de campos claves del esquema.
     */
    public List<Campo> getCamposClaves() {
        synchronized (mutexCamposClaves) {
            if (camposClaves == null) {
                camposClaves = buscarCamposClaves();
                if (camposClaves.isEmpty()) {
                    final String msg = String.format("No hay campos clave definidos para %s-%s",
                            this.nombre, this.version);
                    throw new EsquemaException(msg);
                }

            }

            return camposClaves;
        }
    }

    /**
     * Obtiene la lista de campos comunes del esquema.
     *
     * @return La lista de campos comunes del esquema.
     */
    public List<Campo> getCamposComunes() {
        synchronized (mutexCamposComunes) {
            if (camposComunes == null) {
                camposComunes = buscarCamposComunes();
                if (camposComunes.isEmpty()) {
                    final String msg = String.format("No hay campos comunes definidos para %s-%s",
                            this.nombre, this.version);
                    throw new EsquemaException(msg);
                }

            }

            return camposComunes;
        }
    }

    /**
     * Obtiene la lista de campos datos del esquema.
     *
     * @return La lista de campos datos del esquema.
     */
    public List<Campo> getCamposDatos() {
        synchronized (mutexCamposDatos) {
            if (camposDatos == null) {
                camposDatos = buscarCamposDatos();
                if (camposDatos.isEmpty()) {
                    final String msg = String.format("No hay campos de datos definidos para %s-%s",
                            this.nombre, this.version);
                    throw new EsquemaException(msg);
                }
            }

            return camposDatos;
        }
    }

    /**
     * Obtiene la lista de campos exportables del esquema.
     *
     * @return La lista de campos exportables del esquema.
     */
    public List<Campo> getCamposExportables() {
        synchronized (mutexCamposExportables) {
            if (camposExportables == null) {
                camposExportables = buscarCamposExportables();
                if (camposExportables.isEmpty()) {
                    final String msg = String.format(
                            "No hay campos exportables definidos para %s-%s", this.nombre,
                            this.version);
                    throw new EsquemaException(msg);
                }

            }

            return camposExportables;
        }
    }

    /**
     * Obtiene el c&oacute;digo de versi&oacute;n SII.
     *
     * @return El c&oacute;digo de versi&oacute;n SII.
     */
    public int getCodigoVersionSII() {
        return codigoVersionSII;
    }

    /**
     * Obtiene la fecha de presentaci&oacute;n l&iacute;mite del documento ante
     * la entidad reguladora.
     *
     * @return La fecha de presentaci&oacute;n l&iacute;mite del documento ante
     *         la entidad reguladora.
     */
    public Date getFechaPresentacionLimite() {
        return fechaPresentacionLimite;
    }

    /**
     * Obtiene el id de la entidad reguladora.
     *
     * @return El id de la entidad reguladora.
     */
    public String getIdEntidadReguladora() {
        return idEntidadReguladora;
    }

    /**
     * Obtiene el m&oacute;dulo de aplicaci&oacute;n.
     *
     * @return El m&oacute;dulo de aplicaci&oacute;n.
     */
    public String getModulo() {
        return modulo;
    }

    /**
     * Obtiene el nombre del esquema.
     *
     * @return El nombre del esquema.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el nombre del campo de la fecha del registro del documento
     * asociado al esquema.
     *
     * @return El nombre del campo de la fecha del registro del documento
     *         asociado al esquema.
     */
    public String getNombreCampoFecha() {
        synchronized (mutexNombreCampoFecha) {
            if (nombreCampoFecha == null) {
                nombreCampoFecha = buscarNombreCampoFecha();
                if (nombreCampoFecha == null) {
                    final String msg = String.format("No esta definido campo Fecha para %s-%s",
                            this.nombre, this.version);
                    throw new EsquemaException(msg);
                }
            }

            return nombreCampoFecha;
        }
    }

    /**
     * Obtiene el nombre del campo RUT del Cliente del registro del documento
     * asociado al esquema.
     *
     * @return El nombre del campo RUT del Cliente del registro del documento
     *         asociado al esquema.
     */
    public String getNombreCampoRutCliente() {
        synchronized (mutexNombreCampoRutCliente) {
            if (nombreCampoRutCliente == null) {
                nombreCampoRutCliente = buscarNombreCampoRutCliente();
                if (nombreCampoRutCliente == null) {
                    final String msg = String.format(
                            "No esta definido campo RUT Cliente para %s-%s", this.nombre,
                            this.version);
                    throw new EsquemaException(msg);
                }
            }

            return nombreCampoRutCliente;
        }
    }

    /**
     * Obtiene lista de nombres de los campos clave.
     *
     * @return Lista de nombres de los campos clave.
     */
    public List<String> getNombresCamposClave() {
        final List<String> nombres = new ArrayList<>();
        for (final Campo campoClave : getCamposClaves()) {
            nombres.add(campoClave.getNombre());
        }

        return nombres;
    }

    /**
     * Obtiene el nombre de la tabla donde se registran los datos del documento.
     *
     * @return El nombre de la tabla donde se registran los datos del documento.
     */
    public String getNombreTablaDestino() {
        return String.format(PREFIJO_DESTINO, this.modulo, this.nombre);
    }

    /**
     * Obtiene el nombre de la tabla desde donde se obtienen los datos del
     * documento.
     *
     * @return El nombre de la tabla desde donde se obtienen los datos del
     *         documento.
     */
    public String getNombreTablaOrigen() {
        return String.format(FORMATO_ORIGEN, this.modulo, this.nombre);
    }
    
    
    /**
     * Obtiene el nombre de la vista formato donde se registran los datos del documento.
     *
     * @return El nombre de la vista formato donde se registran los datos del documento.
     */
    public String getNombreVistaDestino() {
        return String.format(FORMATO_VISTA_DESTINO, this.modulo, this.nombre);
    }


    /**
     * Obtiene la descripci&oacute;n del objetivo del documento asociado al
     * esquema.
     *
     * @return La descripci&oacute;n del objetivo del documento asociado al
     *         esquema.
     */
    public String getObjetivo() {
        return objetivo;
    }

    /**
     * Obtiene la periodicidad de los documentos asociados al esquema.
     *
     * @return La periodicidad de los documentos asociados al esquema.
     */
    public String getPeriodicidad() {
        return periodicidad;
    }

    /**
     * Obtiene el periodo inicial.
     *
     * @return El periodo inicial.
     */
    public Date getPeriodoInicio() {
        return periodoInicio;
    }

    /**
     * Obtiene el rango de fechas.
     *
     * @return El rango de fechas.
     */
    public RangoFechas getRangoFechas() {
        return rangoFechas;
    }

    /**
     * Obtiene la versi&oacute;n del esquema.
     *
     * @return La versi&oacute;n del esquema.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Asigna la lista de campos del esquema.
     *
     * @param campos
     *            La lista de campos del esquema.
     */
    public void setCampos(final List<Campo> campos) {
        this.campos = campos;
    }

    /**
     * Asigna el c&oacute;digo de versi&oacute;n SII.
     *
     * @param codigoVersionSII
     *            El c&oacute;digo de versi&oacute;n SII.
     */
    public void setCodigoVersionSII(final int codigoVersionSII) {
        this.codigoVersionSII = codigoVersionSII;
    }

    /**
     * Asigna la fecha de presentaci&oacute;n l&iacute;mite.
     *
     * @param fechaPresentacionLimite
     *            La fecha de presentaci&oacute;n l&iacute;mite.
     */
    public void setFechaPresentacionLimite(final Date fechaPresentacionLimite) {
        this.fechaPresentacionLimite = fechaPresentacionLimite;
    }

    /**
     * Asigna el id de la entidad reguladora.
     *
     * @param idEntidadReguladora
     *            El id de la entidad reguladora.
     */
    public void setIdEntidadReguladora(final String idEntidadReguladora) {
        this.idEntidadReguladora = idEntidadReguladora;
    }

    /**
     * Asigna el m&oacute;dulo de aplicaci&oacute;n.
     *
     * @param modulo
     *            El m&oacute;dulo de aplicaci&oacute;n.
     */
    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    /**
     * Asigna el nombre del esquema.
     *
     * @param nombre
     *            El nombre del esquema.
     */
    public void setNombre(final String nombre) {
        this.nombre = nombre;
    }

    /**
     * Asigna el objetivo del esquema.
     *
     * @param objetivo
     *            El objetivo del esquema.
     */
    public void setObjetivo(final String objetivo) {
        this.objetivo = objetivo;
    }

    /**
     * Asigna la periodicidad del esquema.
     *
     * @param periodicidad
     *            La periodicidad del esquema.
     */
    public void setPeriodicidad(final String periodicidad) {
        this.periodicidad = periodicidad;
    }

    /**
     * Asigna el periodo de inicio del esquema.
     *
     * @param periodoInicio
     *            El periodo de inicio del esquema.
     */
    public void setPeriodoInicio(final Date periodoInicio) {
        this.periodoInicio = periodoInicio;
    }

    /**
     * Asigna el rango de fechas.
     *
     * @param rangoFechas
     *            El rango de fechas.
     */
    public void setRangoFechas(final RangoFechas rangoFechas) {
        this.rangoFechas = rangoFechas;
    }

    /**
     * Asigna la versi&oacute;n del esquema.
     *
     * @param version
     *            La versi&oacute;n del esquema.
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Esquema [campos=" + campos + ", fechaPresentacionLimite=" + fechaPresentacionLimite
                + ", idEntidadReguladora=" + idEntidadReguladora + ", nombre=" + nombre
                + ", objetivo=" + objetivo + ", periodicidad=" + periodicidad + ", periodoInicio="
                + periodoInicio + ", rangoFechas=" + rangoFechas + ", version=" + version + "]";
    }

}
