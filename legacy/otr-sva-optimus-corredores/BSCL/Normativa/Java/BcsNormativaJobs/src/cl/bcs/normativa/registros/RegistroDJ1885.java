package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;

public class RegistroDJ1885 {

    /**
     * Cantidad de acciones al 31-dic.
     */
    private long cantidadAccionesAl31Dic;

    /**
     * Cr&eacute;ditos global complementario adicional impuestos externos.
     */
    private long creditosGlobalComplementarioAdicionalImpuestosExternos;

    /**
     * Cr&eacute;ditos global complementario adicional impuestos tasa adicional.
     */
    private long creditosGlobalComplementarioAdicionalImpuestoTasaAdicional;

    /**
     * Cr&eacute;ditos global complementario afectos con derecho
     * devoluci&oacute;n.
     */
    private long creditosGlobalComplementarioAfectosConDerechoDevolucion;

    /**
     * Cr&eacute;ditos global complementario afectos sin derecho
     * devoluci&oacute;n.
     */
    private long creditosGlobalComplementarioAfectosSinDerechoDevolucion;

    /**
     * Cr&eacute;ditos global complementario exentos con derecho
     * devoluci&oacute;n.
     */
    private long creditosGlobalComplementarioExentosConDerechoDevolucion;

    /**
     * Cr&eacute;ditos global complementario exentos sin derecho
     * devoluci&oacute;n.
     */
    private long creditosGlobalComplementarioExentosSinDerechoDevolucion;

    /**
     * Dividendos reajustados afectos global complementario.
     */
    private long dividendosReajustadosAfectosGlobalComplementario;

    /**
     * Dividendos reajustados exentos global complementario.
     */
    private long dividendosReajustadosExentosGlobalComplementario;

    /**
     * Dividendos reajustados no constitutivos renta acogidos.
     */
    private long dividendosReajustadosNoConstitutivosRentaAcogidos;

    /**
     * Dividendos reajustados no constitutivos renta no acogidos.
     */
    private long dividendosReajustadosNoConstitutivosRentaNoAcogidos;
    
    /**
     * Dividendos reajustados no constitutivos renta devolucion de capital.
     */
    private long dividendosReajustadosNoConstitutivosRentaDevolucionCapital;

    /**
     * Incremento por impuesto primera categor&iacute;a.
     */
    private long incrementoPorImpuestoPrimeraCategoria;

    /**
     * N&uacute;mero de certificado.
     */
    private long numeroCertificado;

    /**
     * RUT del cliente.
     */
    private String rutCliente;

    /**
     * RUT de la sociedad.
     */
    private String rutSociedad;

    /**
     * Tipo de sociedad.
     */
    private String tipoSociedad;

    public RegistroDJ1885() {
    }

    /**
     * Constructor.
     *
     * @param registro
     *            El registro de datos.
     */
    public RegistroDJ1885(final Registro registro) {
        this.rutCliente = (String) registro.get("RutCliente");
        this.rutSociedad = (String) registro.get("RutSociedad");
        this.tipoSociedad = (String) registro.get("TipoSociedad");
        this.numeroCertificado = (Long) registro.get("NumeroCertificado");

        this.cantidadAccionesAl31Dic = (Long) registro.get("CantidadAcciones31Dic");

        this.dividendosReajustadosAfectosGlobalComplementario = (Long) registro
                .get("DividendosAfectos");
        this.dividendosReajustadosExentosGlobalComplementario = (Long) registro
                .get("DividendosExentos");
        this.dividendosReajustadosNoConstitutivosRentaNoAcogidos = (Long) registro
                .get("DividendosNoRentaNoAcogidos");
        this.dividendosReajustadosNoConstitutivosRentaAcogidos = (Long) registro
                .get("DividendosNoRentaAcogidos");
        
        this.dividendosReajustadosNoConstitutivosRentaDevolucionCapital = (Long) registro
        		.get("DevolucionCapital");
        this.incrementoPorImpuestoPrimeraCategoria = (Long) registro.get("IncrementoImpuesto");

        this.creditosGlobalComplementarioAfectosConDerechoDevolucion = (Long) registro
                .get("CreditosAfectosConDevolucion");
        this.creditosGlobalComplementarioAfectosSinDerechoDevolucion = (Long) registro
                .get("CreditosAfectosSinDevolucion");
        this.creditosGlobalComplementarioExentosConDerechoDevolucion = (Long) registro
                .get("CreditosExentosConDevolucion");
        this.creditosGlobalComplementarioExentosSinDerechoDevolucion = (Long) registro
                .get("CreditosExentosSinDevolucion");
        this.creditosGlobalComplementarioAdicionalImpuestosExternos = (Long) registro
                .get("CreditosImpuestosExternos");
        this.creditosGlobalComplementarioAdicionalImpuestoTasaAdicional = (Long) registro
                .get("CreditosImpuestoTasaAdicional");
    }

    public void acumular(final RegistroDJ1885 registroDJ1885) {
        this.dividendosReajustadosAfectosGlobalComplementario += registroDJ1885
                .getDividendosReajustadosAfectosGlobalComplementario();
        this.dividendosReajustadosExentosGlobalComplementario += registroDJ1885
                .getDividendosReajustadosExentosGlobalComplementario();
        this.dividendosReajustadosNoConstitutivosRentaNoAcogidos += registroDJ1885
                .getDividendosReajustadosNoConstitutivosRentaNoAcogidos();
        this.dividendosReajustadosNoConstitutivosRentaAcogidos += registroDJ1885
                .getDividendosReajustadosNoConstitutivosRentaAcogidos();
        
        this.dividendosReajustadosNoConstitutivosRentaDevolucionCapital =
				this.getDividendosReajustadosNoConstitutivosRentaDevolucionCapital() + registroDJ1885
				        .getDividendosReajustadosNoConstitutivosRentaAcogidos();

        this.incrementoPorImpuestoPrimeraCategoria += registroDJ1885
                .getIncrementoPorImpuestoPrimeraCategoria();

        this.creditosGlobalComplementarioAfectosConDerechoDevolucion += registroDJ1885
                .getCreditosGlobalComplementarioAfectosConDerechoDevolucion();
        this.creditosGlobalComplementarioAfectosSinDerechoDevolucion += registroDJ1885
                .getCreditosGlobalComplementarioAfectosSinDerechoDevolucion();
        this.creditosGlobalComplementarioExentosConDerechoDevolucion += registroDJ1885
                .getCreditosGlobalComplementarioExentosConDerechoDevolucion();
        this.creditosGlobalComplementarioExentosSinDerechoDevolucion += registroDJ1885
                .getCreditosGlobalComplementarioExentosSinDerechoDevolucion();
        this.creditosGlobalComplementarioAdicionalImpuestosExternos += registroDJ1885
                .getCreditosGlobalComplementarioAdicionalCreditosImpuestosExternos();
        this.creditosGlobalComplementarioAdicionalImpuestoTasaAdicional += registroDJ1885
                .getCreditosGlobalComplementarioAdicionalImpuestoTasaAdicional();
    }

    /**
     * Obtiene la cantidad de acciones al 31-dic.
     *
     * @return La cantidad de acciones al 31-dic.
     */
    public long getCantidadAccionesAl31Dic() {
        return cantidadAccionesAl31Dic;
    }

    /**
     * Obtiene los cr&eacute;ditos global complementario adicional impuestos
     * externos.
     *
     * @return Los cr&eacute;ditos global complementario adicional impuestos
     *         externos.
     */
    public long getCreditosGlobalComplementarioAdicionalCreditosImpuestosExternos() {
        return creditosGlobalComplementarioAdicionalImpuestosExternos;
    }

    /**
     * Obtiene los cr&eacute;ditos global complementario adicional impuestos
     * tasa adicional.
     *
     * @return Los cr&eacute;ditos global complementario adicional impuestos
     *         tasa adicional.
     */
    public long getCreditosGlobalComplementarioAdicionalImpuestoTasaAdicional() {
        return creditosGlobalComplementarioAdicionalImpuestoTasaAdicional;
    }

    /**
     * Obtiene los cr&eacute;ditos global complementario afectos con derecho
     * devoluci&oacute;n.
     *
     * @return Los cr&eacute;ditos global complementario afectos con derecho
     *         devoluci&oacute;n.
     */
    public long getCreditosGlobalComplementarioAfectosConDerechoDevolucion() {
        return creditosGlobalComplementarioAfectosConDerechoDevolucion;
    }

    /**
     * Obtiene los cr&eacute;ditos global complementario afectos sin derecho
     * devoluci&oacute;n.
     *
     * @return Los cr&eacute;ditos global complementario afectos sin derecho
     *         devoluci&oacute;n.
     */
    public long getCreditosGlobalComplementarioAfectosSinDerechoDevolucion() {
        return creditosGlobalComplementarioAfectosSinDerechoDevolucion;
    }

    /**
     * Obtiene los cr&eacute;ditos global complementario exentos con derecho
     * devoluci&oacute;n.
     *
     * @return Los cr&eacute;ditos global complementario exentos con derecho
     *         devoluci&oacute;n.
     */
    public long getCreditosGlobalComplementarioExentosConDerechoDevolucion() {
        return creditosGlobalComplementarioExentosConDerechoDevolucion;
    }

    /**
     * Obtiene los cr&eacute;ditos global complementario exentos sin derecho
     * devoluci&oacute;n.
     *
     * @return Los cr&eacute;ditos global complementario exentos sin derecho
     *         devoluci&oacute;n.
     */
    public long getCreditosGlobalComplementarioExentosSinDerechoDevolucion() {
        return creditosGlobalComplementarioExentosSinDerechoDevolucion;
    }

    /**
     * Obtiene los dividendos reajustados afectos global complementario.
     *
     * @return Los dividendos reajustados afectos global complementario.
     */
    public long getDividendosReajustadosAfectosGlobalComplementario() {
        return dividendosReajustadosAfectosGlobalComplementario;
    }

    /**
     * Obtiene los dividendos reajustados exentos global complementario.
     *
     * @return Los dividendos reajustados exentos global complementario.
     */
    public long getDividendosReajustadosExentosGlobalComplementario() {
        return dividendosReajustadosExentosGlobalComplementario;
    }

    /**
     * Obtiene los dividendos reajustados no constitutivos renta acogidos.
     *
     * @return Los dividendos reajustados no constitutivos renta acogidos.
     */
    public long getDividendosReajustadosNoConstitutivosRentaAcogidos() {
        return dividendosReajustadosNoConstitutivosRentaAcogidos;
    }

    /**
     * Obtiene los dividendos reajustados no constitutivos renta no acogidos.
     *
     * @return Los dividendos reajustados no constitutivos renta no acogidos.
     */
    public long getDividendosReajustadosNoConstitutivosRentaNoAcogidos() {
        return dividendosReajustadosNoConstitutivosRentaNoAcogidos;
    }
    
    /**
     * Obtiene los dividendos reajustados no constitutivos renta devolucion de capital.
     *
     * @return Los dividendos reajustados no constitutivos renta devolucion de capital.
     */
    public long getDividendosReajustadosNoConstitutivosRentaDevolucionCapital() {
		return dividendosReajustadosNoConstitutivosRentaDevolucionCapital;
	}

    /**
     * Obtiene el incremento por impuesto primera categor&iacute;a.
     *
     * @return El incremento por impuesto primera categor&iacute;a.
     */
    public long getIncrementoPorImpuestoPrimeraCategoria() {
        return incrementoPorImpuestoPrimeraCategoria;
    }

    /**
     * Obtiene el n&uacute;mero de certificado.
     *
     * @return El n&uacute;mero de certificado.
     */
    public long getNumeroCertificado() {
        return numeroCertificado;
    }

    /**
     * Obtiene el RUT del cliente.
     *
     * @return El RUT del cliente.
     */
    public String getRutCliente() {
        return rutCliente;
    }

    /**
     * Obtiene el RUT de la sociedad.
     *
     * @return El RUT de la sociedad.
     */
    public String getRutSociedad() {
        return rutSociedad;
    }

    /**
     * Obtiene el tipo de sociedad.
     *
     * @return El tipo de sociedad.
     */
    public String getTipoSociedad() {
        return tipoSociedad;
    }

    @Override
    public String toString() {
        return "RegistroDJ1885 [cantidadAccionesAl31Dic=" + cantidadAccionesAl31Dic
                + ", creditosGlobalComplementarioAdicionalImpuestosExternos="
                + creditosGlobalComplementarioAdicionalImpuestosExternos
                + ", creditosGlobalComplementarioAdicionalImpuestoTasaAdicional="
                + creditosGlobalComplementarioAdicionalImpuestoTasaAdicional
                + ", creditosGlobalComplementarioAfectosConDerechoDevolucion="
                + creditosGlobalComplementarioAfectosConDerechoDevolucion
                + ", creditosGlobalComplementarioAfectosSinDerechoDevolucion="
                + creditosGlobalComplementarioAfectosSinDerechoDevolucion
                + ", creditosGlobalComplementarioExentosConDerechoDevolucion="
                + creditosGlobalComplementarioExentosConDerechoDevolucion
                + ", creditosGlobalComplementarioExentosSinDerechoDevolucion="
                + creditosGlobalComplementarioExentosSinDerechoDevolucion
                + ", dividendosReajustadosAfectosGlobalComplementario="
                + dividendosReajustadosAfectosGlobalComplementario
                + ", dividendosReajustadosExentosGlobalComplementario="
                + dividendosReajustadosExentosGlobalComplementario
                + ", dividendosReajustadosNoConstitutivosRentaAcogidos="
                + dividendosReajustadosNoConstitutivosRentaAcogidos
                + ", dividendosReajustadosNoConstitutivosRentaNoAcogidos="
                + dividendosReajustadosNoConstitutivosRentaNoAcogidos
                + ", incrementoPorImpuestoPrimeraCategoria=" + incrementoPorImpuestoPrimeraCategoria
                + ", numeroCertificado=" + numeroCertificado + ", rutCliente=" + rutCliente
                + ", rutSociedad=" + rutSociedad + ", tipoSociedad=" + tipoSociedad + "]";
    }

}
