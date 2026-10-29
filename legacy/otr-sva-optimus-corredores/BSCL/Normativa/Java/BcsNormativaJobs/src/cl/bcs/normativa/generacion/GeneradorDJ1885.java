package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1885;

/**
 * Implementaci&oacute;n del generador de la DJ1885.
 *
 * @author fpino@email.com
 *
 */
public final class GeneradorDJ1885 extends Generador {

    private static final int LARGO_REGISTRO = 263;

    /**
     * Registro para agrupar dividendos de un mismo cliente.
     */
    private RegistroDJ1885 registroAgrupador;

    /**
     * Registro resumen de la DJ.
     */
    private RegistroDJ1885 registroResumen;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de generaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public GeneradorDJ1885(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);

        this.registroResumen = new RegistroDJ1885();
    }

    /**
     * Actualiza los acumuladores.
     *
     * @param registroDJ1885
     *            Registro DJ1885.
     */
    private void actualizarAcumuladores(final RegistroDJ1885 registroDJ1885) {
        this.contarCasosInformados();

        registroResumen.acumular(registroDJ1885);
    }

    private void agrupar(final RegistroDJ1885 registroDJ1885) {
        registroAgrupador.acumular(registroDJ1885);
    }

    private boolean corte(final RegistroDJ1885 registroDJ1885) {
        if (registroAgrupador.getRutCliente().compareTo(registroDJ1885.getRutCliente()) != 0) {
            return true;
        }

        if (registroAgrupador.getRutSociedad() != null && registroAgrupador.getRutSociedad().compareTo(registroDJ1885.getRutSociedad()) != 0) {
            return true;
        }

        return false;
    }

    @Override
    public void generarDetalle(final Registro registro) throws SerializadorException {
        final RegistroDJ1885 registroDJ1885 = new RegistroDJ1885(registro);
        if (registroAgrupador == null) {
            registroAgrupador = registroDJ1885;
        } else if (corte(registroDJ1885)) {
            println(generarRegistroTipo2(registroAgrupador));
            registroAgrupador = registroDJ1885;
        } else {
            agrupar(registroDJ1885);
        }
    }

    @Override
    public void generarEncabezado() throws SerializadorException {
        println(generarRegistroTipo0());
        println(generarRegistroTipo1());
    }

    /**
     * Genera registro tipo 0.
     *
     * @return Registro tipo 0.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    @Override
    public String generarRegistroTipo0() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario());

        // Numero de formulario.
        ret += "1885";

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total de registros a transmitir.
        ret += String.format("%07d", getCantidadTotalRegistros());

        // Codigo empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Checksum declarante.
        ret += Serializador.ceros(10);

        // Checksum SII.
        ret += Serializador.ceros(10);

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Codigo del tipo de declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Fecha/hora envio (ddMMyyyyhhmmss).
        ret += Serializador.ceros(16);

        // Numero de version.
        ret += String.format("%02d", getContexto().getNumeroVersion());

        // Numero de atencion.
        ret += Serializador.ceros(8);

        // Periodo tributario.
        ret += String.format("%04d00", getContexto().getAnoTributario());

        // Relleno.
        ret += Serializador.espacios(171);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 0");
        }

        return ret;
    }

    /**
     * Genera registro tipo 1.
     *
     * @return Registro tipo 1.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo1() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";

        // Ano tributario.
        ret += String.format("%04d00", getContexto().getAnoTributario());

        // Numero de formulario.
        ret += "1885";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT declarante anterior.
        ret += getContexto().getRutDeclaranteAnterior();

        // Codigo de presentacion anterior.
        ret += getContexto().getCodigoModoPresentacionAnterior();

        // Folio de declaracion anterior.
        ret += String.format("%07d", getContexto().getFolioDeclaracionAnterior());

        // Relleno.
        ret += Serializador.espacios(6);
        
        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());
        
        // Razon social.
        String RazonSocial=getContexto().getRazonSocial();
        RazonSocial = Serializador.removerCaracteresEspeciales(RazonSocial);
        ret += String.format("%-30s",  (RazonSocial).length() > 30 ?  (RazonSocial).substring(0,30) :  (RazonSocial));
        
        // Direccion postal.
        String DireccionDeclaracion=getContexto().getDireccionDeclaracion();
        DireccionDeclaracion = Serializador.removerCaracteresEspeciales(DireccionDeclaracion);
        ret += String.format("%-35s",  (DireccionDeclaracion).length() > 35 ?  (DireccionDeclaracion).substring(0,35) :  (DireccionDeclaracion));

        // Comuna.
        String ComunaDeclaracion=getContexto().getComunaDeclaracion();
        ComunaDeclaracion = Serializador.removerCaracteresEspeciales(ComunaDeclaracion);
        ret += String.format("%-15s",  (ComunaDeclaracion).length() > 15 ?  (ComunaDeclaracion).substring(0,15) :  (ComunaDeclaracion));
     
        // Correo electronico.
        String EmailDeclaracion=getContexto().getEmailDeclaracion();
        ret += String.format("%30s",  (EmailDeclaracion).length() > 30 ?  (EmailDeclaracion).substring(0,30) :  (EmailDeclaracion));
        
        // Fax.
        ret += "56020000000";

        // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 1000000);
        
        // Relleno.
        ret += Serializador.espacios(1);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(58);

        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 1");
        }

        return ret;
    }

    /**
     * Genera registro tipo 2.
     *
     * @param registro
     *            El registro de datos.
     * @return Registro tipo 2.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo2(final RegistroDJ1885 registroDJ1885)
            throws SerializadorException {

        actualizarAcumuladores(registroDJ1885);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21885";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Numero de Registro.
        ret += String.format("%07d", getCantidadCasosInformados());

        // RUT de la Sociedad.
        ret += Serializador.serializarRut(registroDJ1885.getRutSociedad());

        // Tipo de Sociedad.
        ret += registroDJ1885.getTipoSociedad();

        // RUT del Cliente.
        ret += Serializador.serializarRut(registroDJ1885.getRutCliente());

        // Cantidad de Acciones al 31/12.
        ret += String.format("%012d", registroDJ1885.getCantidadAccionesAl31Dic());

        // Monto Dividendos Reajustados Afectos a Global Complementario.
        ret += String.format("%015d",
                registroDJ1885.getDividendosReajustadosAfectosGlobalComplementario());

        // Monto Dividendos Reajustados Exentos de Global Complementario.
        ret += String.format("%015d",
                registroDJ1885.getDividendosReajustadosExentosGlobalComplementario());

        // Montos Dividendos Reajustados No Constitutivos de Renta No Acogidos a
        // Impuesto Sustitutivo.
        ret += String.format("%015d",
                registroDJ1885.getDividendosReajustadosNoConstitutivosRentaNoAcogidos());

        // Montos Dividendos Reajustados No Constitutivos de Renta Acogidos a
        // Impuesto Sustitutivo.
        ret += String.format("%015d",
                registroDJ1885.getDividendosReajustadosNoConstitutivosRentaAcogidos());
        
        // Montos Dividendos Reajustados No Constitutivos de Renta Devolucion de Capital
        ret += String.format("%015d",
                registroDJ1885.getDividendosReajustadosNoConstitutivosRentaDevolucionCapital());

        // Incremento por Impuesto Primera Categoria.
        ret += String.format("%015d", registroDJ1885.getIncrementoPorImpuestoPrimeraCategoria());

        // Creditos para Global Complementario Adicional Impuesto de Primera
        // Categoria Afectos Con Derecho a Devolucion.
        ret += String.format("%015d",
                registroDJ1885.getCreditosGlobalComplementarioAfectosConDerechoDevolucion());

        // Creditos para Global Complementario Adicional Impuesto de Primera
        // Categoria Afectos Sin Derecho a Devolucion.
        ret += String.format("%015d",
                registroDJ1885.getCreditosGlobalComplementarioAfectosSinDerechoDevolucion());

        // Creditos para Global Complementario Adicional Impuesto de Primera
        // Categoria Exentos Con Derecho a Devolucion.
        ret += String.format("%015d",
                registroDJ1885.getCreditosGlobalComplementarioExentosConDerechoDevolucion());

        // Creditos para Global Complementario Adicional Impuesto de Primera
        // Categoria Exentos Sin Derecho a Devolucion.
        ret += String.format("%015d",
                registroDJ1885.getCreditosGlobalComplementarioExentosSinDerechoDevolucion());

        // Creditos para Global Complementario Adicional Creditos por Impuestos
        // Externos.
        ret += String.format("%015d",
                registroDJ1885.getCreditosGlobalComplementarioAdicionalCreditosImpuestosExternos());

        // Creditos para Global Complementario Adicional Impuesto Tasa
        // Adicional.
        ret += String.format("%015d",
                registroDJ1885.getCreditosGlobalComplementarioAdicionalImpuestoTasaAdicional());

        // Numero de certificado.
        ret += String.format("%07d", registroDJ1885.getNumeroCertificado());

        // Relleno.
        ret += Serializador.espacios(15);
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 2");
        }

        return ret;
    }

    /**
     * Genera registro tipo 3.
     *
     * @return Registro tipo 3.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String generarRegistroTipo3() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "31885";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getDividendosReajustadosAfectosGlobalComplementario());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getDividendosReajustadosExentosGlobalComplementario());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getDividendosReajustadosNoConstitutivosRentaNoAcogidos());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getDividendosReajustadosNoConstitutivosRentaAcogidos());
        
        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getDividendosReajustadosNoConstitutivosRentaDevolucionCapital());

        // Total monto ...
        ret += String.format("%018d", registroResumen.getIncrementoPorImpuestoPrimeraCategoria());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getCreditosGlobalComplementarioAfectosConDerechoDevolucion());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getCreditosGlobalComplementarioAfectosSinDerechoDevolucion());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getCreditosGlobalComplementarioExentosConDerechoDevolucion());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getCreditosGlobalComplementarioExentosSinDerechoDevolucion());

        // Total monto ...
        ret += String.format("%018d", registroResumen
                .getCreditosGlobalComplementarioAdicionalCreditosImpuestosExternos());

        // Total monto ...
        ret += String.format("%018d",
                registroResumen.getCreditosGlobalComplementarioAdicionalImpuestoTasaAdicional());

        // Total de casos informados.
        ret += String.format("%07d", getCantidadCasosInformados());

        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());
        
        if (ret.length() != LARGO_REGISTRO) {
            throw new SerializadorException("Largo invalido registro tipo 3");
        }

        return ret;
    }

    @Override
    public void generarResumen() throws SerializadorException {
        if (registroAgrupador != null) {
            println(generarRegistroTipo2(registroAgrupador));
        }
        print(generarRegistroTipo3());
    }

    /**
     * Obtiene la cantidad total de registros que contiene el archivo de
     * resultado.
     *
     * @return La cantidad total de registros que contiene el archivo de
     *         resultado.
     */
    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }

}
