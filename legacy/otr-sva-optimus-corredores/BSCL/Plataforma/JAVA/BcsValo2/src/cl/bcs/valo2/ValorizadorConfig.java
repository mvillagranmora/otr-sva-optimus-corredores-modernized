package cl.bcs.valo2;

import java.io.Serializable;
import java.util.Properties;

import cl.bcs.utils.PropertiesUtil;

public final class ValorizadorConfig implements Serializable {
    private static final int DEFAULT_DIAS_BUSQUEDA = 60;
    private static final int DEFAULT_ESCALA_CONVEXIDAD = 2;
    private static final int DEFAULT_ESCALA_DURATION = 2;
    private static final int DEFAULT_ESCALA_INTERESES = 6;
    private static final int DEFAULT_ESCALA_MONTOS = 2;
    private static final int DEFAULT_ESCALA_PRECIOS = 4;
    private static final int DEFAULT_ESCALA_TASAS = 6;
    private static final long serialVersionUID = 1L;

    private final boolean cacheHabilitado;
    private final int diasBusqueda;
    private final int escalaConvexidad;
    private final int escalaDuration;
    private final int escalaIntereses;
    private final int escalaMontos;
    private final int escalaPrecios;
    private final int escalaTasas;
    private final String monedaSistema;
    private final String rutaArchivoBonos;
    private final String rutaArchivoCupones;
    private final String rutaArchivoFeriados;
    private final String rutaArchivoIndicadores;
    private final String rutaArchivoParidades;

    public ValorizadorConfig(
            final Properties props) {
        this.cacheHabilitado = getCacheHabilitado(props);
        this.diasBusqueda = getDiasBusqueda(props);
        this.escalaConvexidad = getEscalaConvexidad(props);
        this.escalaDuration = getEscalaDuration(props);
        this.escalaIntereses = getEscalaIntereses(props);
        this.escalaMontos = getEscalaMontos(props);
        this.escalaPrecios = getEscalaPrecios(props);
        this.escalaTasas = getEscalaTasas(props);
        this.monedaSistema = getMonedaSistema(props);
        this.rutaArchivoBonos = getRutaArchivoBonos(props);
        this.rutaArchivoCupones = getRutaArchivoCupones(props);
        this.rutaArchivoFeriados = getRutaArchivoFeriados(props);
        this.rutaArchivoIndicadores = getRutaArchivoIndicadores(props);
        this.rutaArchivoParidades = getRutaArchivoParidades(props);
    }

    public boolean getCacheHabilitado(
            final Properties props) {
        return "S".equalsIgnoreCase(PropertiesUtil.getOptional(props, "cacheHabilitado", "N"));
    }

    public int getDiasBusqueda() {
        return diasBusqueda;
    }

    private int getDiasBusqueda(
            final Properties props) {
        return PropertiesUtil.getOptional(props, "diasBusqueda", DEFAULT_DIAS_BUSQUEDA);
    }

    public int getEscalaConvexidad() {
        return escalaConvexidad;
    }

    public int getEscalaConvexidad(
            final Properties props) {
        return PropertiesUtil.getOptional(props, "escalaConvexidad", DEFAULT_ESCALA_CONVEXIDAD);
    }

    public int getEscalaDuration() {
        return escalaDuration;
    }

    public int getEscalaDuration(
            final Properties props) {
        return PropertiesUtil.getOptional(props, "escalaDuration", DEFAULT_ESCALA_DURATION);
    }

    public int getEscalaIntereses() {
        return escalaIntereses;
    }

    public int getEscalaIntereses(
            final Properties props) {
        return PropertiesUtil.getOptional(props, "escalaIntereses", DEFAULT_ESCALA_INTERESES);
    }

    public int getEscalaMontos() {
        return escalaMontos;
    }

    public int getEscalaMontos(
            final Properties props) {
        return PropertiesUtil.getOptional(props, "escalaMontos", DEFAULT_ESCALA_MONTOS);
    }

    public int getEscalaPrecios() {
        return escalaPrecios;
    }

    public int getEscalaPrecios(
            final Properties props) {
        return PropertiesUtil.getOptional(props, "escalaPrecios", DEFAULT_ESCALA_PRECIOS);
    }

    public int getEscalaTasas() {
        return escalaTasas;
    }

    public int getEscalaTasas(
            final Properties props) {
        return PropertiesUtil.getOptional(props, "escalaTasas", DEFAULT_ESCALA_TASAS);
    }

    public String getMonedaSistema() {
        return monedaSistema;
    }

    private String getMonedaSistema(
            final Properties props) {
        return props.getProperty("monedaSistema");
    }

    public String getRutaArchivoBonos() {
        return rutaArchivoBonos;
    }

    public String getRutaArchivoBonos(
            final Properties props) {
        return props.getProperty("filepath.bonos");
    }

    public String getRutaArchivoCupones() {
        return rutaArchivoCupones;
    }

    public String getRutaArchivoCupones(
            final Properties props) {
        return props.getProperty("filepath.cupones");
    }

    public String getRutaArchivoFeriados() {
        return rutaArchivoFeriados;
    }

    public String getRutaArchivoFeriados(
            final Properties props) {
        return props.getProperty("filepath.feriados");
    }

    public String getRutaArchivoIndicadores() {
        return rutaArchivoIndicadores;
    }

    public String getRutaArchivoIndicadores(
            final Properties props) {
        return props.getProperty("filepath.indicadores");
    }

    public String getRutaArchivoParidades() {
        return rutaArchivoParidades;
    }

    public String getRutaArchivoParidades(
            final Properties props) {
        return props.getProperty("filepath.paridades");
    }

    public boolean isCacheHabilitado() {
        return cacheHabilitado;
    }

    @Override
    public String toString() {
        return "ValorizadorConfig [cacheHabilitado=" + cacheHabilitado + ", diasBusqueda="
                + diasBusqueda + ", escalaConvexidad=" + escalaConvexidad + ", escalaDuration="
                + escalaDuration + ", escalaIntereses=" + escalaIntereses + ", escalaMontos="
                + escalaMontos + ", escalaPrecios=" + escalaPrecios + ", escalaTasas=" + escalaTasas
                + ", monedaSistema=" + monedaSistema + ", rutaArchivoBonos=" + rutaArchivoBonos
                + ", rutaArchivoCupones=" + rutaArchivoCupones + ", rutaArchivoFeriados="
                + rutaArchivoFeriados + ", rutaArchivoIndicadores=" + rutaArchivoIndicadores
                + ", rutaArchivoParidades=" + rutaArchivoParidades + "]";
    }

}
