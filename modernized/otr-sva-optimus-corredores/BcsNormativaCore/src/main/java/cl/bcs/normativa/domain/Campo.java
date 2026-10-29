package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Especificacion del campo del registro de un documento.
 * Migrado desde legacy cl.bcs.normativa.esquema.Campo.
 */
public final class Campo implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean clave;
    private String codigoSII;
    private boolean comun;
    private boolean editable;
    private boolean exportable;
    private String grupo;
    private int largo;
    private int cantidadDecimales;
    private boolean modoMasivo;
    private String nombre;
    private String nombreEsquema;
    private boolean obligatorio;
    private boolean ocultoEnGrilla;
    private boolean ocultoEnMantenedor;
    private int orden;
    private int ordenCargaExterna;
    private String tipoDato;
    private String tipoDiccionario;
    private String titulo;
    private String valorDefecto;
    private String versionEsquema;

    public int getCantidadDecimales() { return cantidadDecimales; }
    public String getCodigoSII() { return codigoSII; }
    public String getGrupo() { return grupo; }
    public int getLargo() { return largo; }
    public String getNombre() { return nombre; }
    public String getNombreEsquema() { return nombreEsquema; }
    public int getOrden() { return orden; }
    public int getOrdenCargaExterna() { return ordenCargaExterna; }
    public String getTipoDato() { return tipoDato; }
    public String getTipoDiccionario() { return tipoDiccionario; }
    public String getTitulo() { return titulo; }
    public String getValorDefecto() { return valorDefecto; }
    public String getVersionEsquema() { return versionEsquema; }
    public boolean isClave() { return clave; }
    public boolean isComun() { return comun; }
    public boolean isEditable() { return editable; }
    public boolean isExportable() { return exportable; }
    public boolean isModoMasivo() { return modoMasivo; }
    public boolean isObligatorio() { return obligatorio; }
    public boolean isOcultoEnGrilla() { return ocultoEnGrilla; }
    public boolean isOcultoEnMantenedor() { return ocultoEnMantenedor; }

    /** Campo es sumable si su tipo es AMOUNT, DECIMAL o QTY. */
    public boolean isSumable() {
        return "AMOUNT".equals(tipoDato) || "DECIMAL".equals(tipoDato) || "QTY".equals(tipoDato);
    }

    public void setCantidadDecimales(int cantidadDecimales) { this.cantidadDecimales = cantidadDecimales; }
    public void setClave(boolean clave) { this.clave = clave; }
    public void setCodigoSII(String codigoSII) { this.codigoSII = codigoSII; }
    public void setComun(boolean comun) { this.comun = comun; }
    public void setEditable(boolean editable) { this.editable = editable; }
    public void setExportable(boolean exportable) { this.exportable = exportable; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public void setLargo(int largo) { this.largo = largo; }
    public void setModoMasivo(boolean modoMasivo) { this.modoMasivo = modoMasivo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setNombreEsquema(String nombreEsquema) { this.nombreEsquema = nombreEsquema; }
    public void setObligatorio(boolean obligatorio) { this.obligatorio = obligatorio; }
    public void setOcultoEnGrilla(boolean ocultoEnGrilla) { this.ocultoEnGrilla = ocultoEnGrilla; }
    public void setOcultoEnMantenedor(boolean ocultoEnMantenedor) { this.ocultoEnMantenedor = ocultoEnMantenedor; }
    public void setOrden(int orden) { this.orden = orden; }
    public void setOrdenCargaExterna(int ordenCargaExterna) { this.ordenCargaExterna = ordenCargaExterna; }
    public void setTipoDato(String tipoDato) { this.tipoDato = tipoDato; }
    public void setTipoDiccionario(String tipoDiccionario) { this.tipoDiccionario = tipoDiccionario; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setValorDefecto(String valorDefecto) { this.valorDefecto = valorDefecto; }
    public void setVersionEsquema(String versionEsquema) { this.versionEsquema = versionEsquema; }

    @Override
    public String toString() {
        return "Campo[nombre=" + nombre + ", tipoDato=" + tipoDato + ", esquema=" + nombreEsquema + "-" + versionEsquema + "]";
    }
}
