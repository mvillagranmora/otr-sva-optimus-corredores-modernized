package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Especificaci&oacute;n del campo del registro de un documento.
 *
 * @author fpino@email.com
 *
 */
public final class Campo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Indica si el campo es parte de la clave del registro de un documento.
     */
    private boolean clave;

    /**
     * C&oacute;digo SII del campo.
     */
    private String codigoSII;

    /**
     * Indica si el campo pertenece a la especificaci&oacute;n com&uacute;n a
     * todos los documentos.
     */
    private boolean comun;

    /**
     * Indica si el campo es editable.
     */
    private boolean editable;

    /**
     * Indica si el campo es exportable.
     */
    private boolean exportable;

    /**
     * Nombre del grupo al cual pertenece el campo (puede ser vacio).
     */
    private String grupo;

    /**
     * Largo del campo.
     */
    private int largo;

    /**
     * Cantidad de decimales.
     */
    private int cantidadDecimales;

    /**
     * Indica si el campo se puede editar en modo masivo.
     */
    private boolean modoMasivo;

    /**
     * Nombre del campo.
     */
    private String nombre;

    /**
     * Nombre del esquema al cual pertenece el campo.
     */
    private String nombreEsquema;

    /**
     * Indica de si el campo es obligatorio.
     */
    private boolean obligatorio;

    /**
     * Indica si el campo se oculta en la grilla de visualizaci&oacute;n.
     */
    private boolean ocultoEnGrilla;

    /**
     * Indica si el campo se oculta en el formulario de mantenci&oacute;n.
     */
    private boolean ocultoEnMantenedor;

    /**
     * Secuencia de ordenamiento del campo.
     */
    private int orden;

    /**
     * Secuencia en la cual viene el dato del campo en el archivo de carga
     * externa.
     */
    private int ordenCargaExterna;

    /**
     * Tipo de dato.
     */
    private String tipoDato;

    /**
     * Tipo de diccionario.
     */
    private String tipoDiccionario;

    /**
     * Titulo descriptivo.
     */
    private String titulo;

    /**
     * Valor por defecto.
     */
    private String valorDefecto;

    /**
     * Versi&oacute;n del esquema al cual pertenece el campo.
     */
    private String versionEsquema;

    /**
     * Obtiene la cantidad de decimales.
     *
     * @return La cantidad de decimales.
     */
    public int getCantidadDecimales() {
        return cantidadDecimales;
    }

    /**
     * Obtiene el c&oacute;digo SII del campo.
     *
     * @return El c&oacute;digo SII del campo.
     */
    public String getCodigoSII() {
        return codigoSII;
    }

    /**
     * Obtiene el grupo al cual pertenece el campo.
     *
     * @return El grupo al cual pertenece el campo.
     */
    public String getGrupo() {
        return grupo;
    }

    /**
     * Obtiene el largo del campo.
     *
     * @return El largo del campo.
     */
    public int getLargo() {
        return largo;
    }

    /**
     * Obtiene el nombre del campo.
     *
     * @return El nombre del campo.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el nombre del esquema.
     *
     * @return El nombre del esquema.
     */
    public String getNombreEsquema() {
        return nombreEsquema;
    }

    /**
     * Obtiene la secuencia de ordenamiento del campo.
     *
     * @return La secuencia de ordenamiento del campo.
     */
    public int getOrden() {
        return orden;
    }

    /**
     * Obtiene la secuencia en la cual viene el dato del campo en el archivo de
     * carga externa.
     *
     * @return La secuencia en la cual viene el dato del campo en el archivo de
     *         carga externa.
     */
    public int getOrdenCargaExterna() {
        return ordenCargaExterna;
    }

    /**
     * Obtiene el tipo de dato.
     *
     * @return El tipo de dato.
     */
    public String getTipoDato() {
        return tipoDato;
    }

    /**
     * Obtiene el tipo de diccionario.
     *
     * @return El tipo de diccionario.
     */
    public String getTipoDiccionario() {
        return tipoDiccionario;
    }

    /**
     * Obtiene el titulo descriptivo.
     *
     * @return El titulo descriptivo.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Obtiene el valor por defecto.
     *
     * @return El valor por defecto.
     */
    public String getValorDefecto() {
        return valorDefecto;
    }

    /**
     * Obtiene la versi&oacute;n del esquema.
     *
     * @return La versi&oacute;n del esquema.
     */
    public String getVersionEsquema() {
        return versionEsquema;
    }

    /**
     * Indica si el campo es parte de la clave del registro de un documento.
     *
     * @return <code>true</code> si el campo es parte de la clave del registro
     *         de un documento, <code>false</code> en caso contrario.
     */
    public boolean isClave() {
        return clave;
    }

    /**
     * Indica si el campo pertenece a la especificaci&oacute;n com&uacute;n a
     * todos los documentos.
     *
     * @return <code>true</code> si el campo pertenece a la
     *         especificaci&oacute;n com&uacute;n a todos los documentos,
     *         <code>false</code> en caso contrario.
     */
    public boolean isComun() {
        return comun;
    }

    /**
     * Indica si el campo es editable.
     *
     * @return <code>true</code> si el campo es editable, <code>false</code> en
     *         caso contrario.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Indica si el campo es exportable.
     *
     * @return <code>true</code> si el campo es exportable, <code>false</code>
     *         en caso contrario.
     */
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Indica si el campo se puede editar en modo masivo.
     *
     * @return <code>true</code> si el campo se puede editar en modo masivo,
     *         <code>false</code> en caso contrario.
     */
    public boolean isModoMasivo() {
        return modoMasivo;
    }

    /**
     * Indica si el campo es obligatorio.
     *
     * @return <code>true</code> si el campo es obligatorio, <code>false</code>
     *         en caso contrario.
     */
    public boolean isObligatorio() {
        return obligatorio;
    }

    /**
     * Indica si el campo se oculta en la grilla de visualizaci&oacute;n.
     *
     * @return <code>true</code> si el campo se oculta en la grilla de
     *         visualizaci&oacute;n, <code>false</code> en caso contrario.
     */
    public boolean isOcultoEnGrilla() {
        return ocultoEnGrilla;
    }

    /**
     * Indica si el campo se oculta en el formulario de mantenci&oacute;n.
     *
     * @return <code>true</code> si el campo se oculta en el formulario de
     *         mantenci&oacute;n, <code>false</code> en caso contrario.
     */
    public boolean isOcultoEnMantenedor() {
        return ocultoEnMantenedor;
    }

    /**
     * Determina si el campo es sumable.
     *
     * @return <code>true</code> si el campo es sumable, <code>false</code> en
     *         caso contrario.
     */
    public boolean isSumable() {
        return "AMOUNT".equals(tipoDato) || "DECIMAL".equals(tipoDato) || "QTY".equals(tipoDato);
    }

    /**
     * Asigna la cantidad de decimales.
     *
     * @param cantidadDecimales
     *            La cantidad de decimales.
     */
    public void setCantidadDecimales(final int cantidadDecimales) {
        this.cantidadDecimales = cantidadDecimales;
    }

    /**
     * Asigna si el campo es parte de la clave del registro de un documento.
     *
     * @param clave
     *            Si el campo es parte de la clave del registro de un documento.
     */
    public void setClave(final boolean clave) {
        this.clave = clave;
    }

    /**
     * Asigna el c&oacute;digo SII del campo.
     *
     * @param codigoSII
     *            El c&oacute;digo SII del campo.
     */
    public void setCodigoSII(String codigoSII) {
        this.codigoSII = codigoSII;
    }

    /**
     * Asigna si el campo pertenece a la especificaci&oacute;n com&uacute;n a
     * todos los documentos.
     *
     * @param comun
     *            Si el campo pertenece a la especificaci&oacute;n com&uacute;n
     *            a todos los documentos.
     */
    public void setComun(final boolean comun) {
        this.comun = comun;
    }

    /**
     * Asigna si el campo es editable.
     *
     * @param editable
     *            Si el campo es editable.
     */
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    /**
     * Asigna si el campo es exportable.
     *
     * @param exportable
     *            Si el campo es exportable.
     */
    public void setExportable(final boolean exportable) {
        this.exportable = exportable;
    }

    /**
     * Asigna el grupo al cual pertenece el campo.
     *
     * @param grupo
     *            El grupo al cual pertenece el campo.
     */
    public void setGrupo(final String grupo) {
        this.grupo = grupo;
    }

    /**
     * Asigna el largo del campo.
     *
     * @param largo
     *            El largo del campo.
     */
    public void setLargo(final int largo) {
        this.largo = largo;
    }

    /**
     * Asigna si el campo se puede editar en modo masivo.
     *
     * @param modoMasivo
     *            Si el campo se puede editar en modo masivo.
     */
    public void setModoMasivo(final boolean modoMasivo) {
        this.modoMasivo = modoMasivo;
    }

    /**
     * Asigna el nombre del campo.
     *
     * @param nombre
     *            El nombre del campo.
     */
    public void setNombre(final String nombre) {
        this.nombre = nombre;
    }

    /**
     * Asigna el nombre del esquema.
     *
     * @param nombreEsquema
     *            El nombre del esquema.
     */
    public void setNombreEsquema(final String nombreEsquema) {
        this.nombreEsquema = nombreEsquema;
    }

    /**
     * Asigna si el campo es obligatorio.
     *
     * @param obligatorio
     *            Si el campo es obligatorio.
     */
    public void setObligatorio(final boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    /**
     * Asigna si el campo se oculta en la grilla de visualizaci&oacute;n.
     *
     * @param ocultoEnGrilla
     *            Si el campo se oculta en la grilla de visualizaci&oacute;n.
     */
    public void setOcultoEnGrilla(final boolean ocultoEnGrilla) {
        this.ocultoEnGrilla = ocultoEnGrilla;
    }

    /**
     * Asigna si el campo se oculta en el formulario de mantenci&oacute;n.
     *
     * @param ocultoEnMantenedor
     *            Si el campo se oculta en el formulario de mantenci&oacute;n.
     */
    public void setOcultoEnMantenedor(final boolean ocultoEnMantenedor) {
        this.ocultoEnMantenedor = ocultoEnMantenedor;
    }

    /**
     * Asigna la secuencia de ordenamiento del campo.
     *
     * @param orden
     *            La secuencia de ordenamiento del campo.
     */
    public void setOrden(final int orden) {
        this.orden = orden;
    }

    /**
     * Asigna la secuencia en la cual viene el dato del campo en el archivo de
     * carga externa.
     *
     * @param ordenCargaExterna
     *            La secuencia en la cual viene el dato del campo en el archivo
     *            de carga externa.
     */
    public void setOrdenCargaExterna(final int ordenCargaExterna) {
        this.ordenCargaExterna = ordenCargaExterna;
    }

    /**
     * Asigna el tipo de dato.
     *
     * @param tipoDato
     *            El tipo de dato.
     */
    public void setTipoDato(final String tipoDato) {
        this.tipoDato = tipoDato;
    }

    /**
     * Asigna el tipo de diccionario.
     *
     * @param tipoDiccionario
     *            El tipo de diccionario.
     */
    public void setTipoDiccionario(final String tipoDiccionario) {
        this.tipoDiccionario = tipoDiccionario;
    }

    /**
     * Asigna el titulo descriptivo.
     *
     * @param titulo
     *            El titulo descriptivo.
     */
    public void setTitulo(final String titulo) {
        this.titulo = titulo;
    }

    /**
     * Asigna el valor por defecto.
     *
     * @param valorDefecto
     *            El valor por defecto.
     */
    public void setValorDefecto(final String valorDefecto) {
        this.valorDefecto = valorDefecto;
    }

    /**
     * Asigna la versi&oacute;n del esquema.
     *
     * @param versionEsquema
     *            La versi&oacute;n del esquema.
     */
    public void setVersionEsquema(final String versionEsquema) {
        this.versionEsquema = versionEsquema;
    }

    @Override
    public String toString() {
        return "Campo [clave=" + clave + ", codigoSII=" + codigoSII + ", comun=" + comun
                + ", editable=" + editable + ", exportable=" + exportable + ", grupo=" + grupo
                + ", largo=" + largo + ", cantidadDecimales=" + cantidadDecimales + ", modoMasivo="
                + modoMasivo + ", nombre=" + nombre + ", nombreEsquema=" + nombreEsquema
                + ", obligatorio=" + obligatorio + ", ocultoEnGrilla=" + ocultoEnGrilla
                + ", ocultoEnMantenedor=" + ocultoEnMantenedor + ", orden=" + orden
                + ", ordenCargaExterna=" + ordenCargaExterna + ", tipoDato=" + tipoDato
                + ", tipoDiccionario=" + tipoDiccionario + ", titulo=" + titulo + ", valorDefecto="
                + valorDefecto + ", versionEsquema=" + versionEsquema + "]";
    }

}
