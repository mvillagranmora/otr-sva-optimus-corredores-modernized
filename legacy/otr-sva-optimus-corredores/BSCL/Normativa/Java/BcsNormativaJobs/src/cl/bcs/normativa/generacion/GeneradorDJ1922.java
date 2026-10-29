package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1922;


public final class GeneradorDJ1922 extends Generador {

    private static final int LARGO_REGISTRO_AT2025 = 946; // AT <= 2025
    private static final int LARGO_REGISTRO_AT2026   = 725; // AT >= 2026

    private int LARGO_REGISTRO;
    private RegistroDJ1922 registroResumen;
    private long totalRegistros;
    private long versionDJ;

    public GeneradorDJ1922(final ContextoGeneracion contexto)
            throws SQLException, IOException, SerializadorException {
        super(contexto);
        this.totalRegistros = 0;
        this.registroResumen = new RegistroDJ1922();
        this.versionDJ = getContexto().getAnoTributario();

        if (versionDJ >= 2025) {
            LARGO_REGISTRO = LARGO_REGISTRO_AT2026;
        } else {
            LARGO_REGISTRO = LARGO_REGISTRO_AT2025;
        }
    }

    private void actualizarAcumuladores(final RegistroDJ1922 registroDJ1922) {
        this.contarCasosInformados();
        this.totalRegistros += 1;
        registroResumen.acumular(registroDJ1922);
    }

   @Override
    public void generarDetalle(final Registro registro) throws SerializadorException {
        try {
            if (versionDJ >= 2025) {
                println(generarRegistroTipo2AT2026(registro));
            } else {
                println(generarRegistroTipo2AT2025(registro));
            }
        } catch (Exception ex) {
            throw new SerializadorException("Error: " + ex.getMessage());
            
        }
    }  
    
    @Override
    public void generarEncabezado() throws SerializadorException {
        if (getContexto().getModoPresentacion().getCodigo().equalsIgnoreCase("I")) {
            println(generarRegistroTipo0()); // el puente se encarga de elegir
        }
        if (versionDJ >= 2025) {
            println(generarRegistroTipo1AT2026());
        } else {
            println(generarRegistroTipo1AT2025());
        }
    }

     @Override
    public String generarRegistroTipo0() throws SerializadorException {
        
        if (versionDJ >= 2025) {
            return generarRegistroTipo0AT2026();
        } else {
            return generarRegistroTipo0AT2025();
        }
    }


    // =========================================================================
    // REGISTRO TIPO 0 AT2025
    // =========================================================================
   

    public String generarRegistroTipo0AT2025() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1922";

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total de registros a transmitir.
        ret += String.format("%010d", getCantidadTotalRegistros());

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

        // Relleno.
        ret += Serializador.espacios(857);

        if (ret.length() != LARGO_REGISTRO_AT2025) {
            throw new SerializadorException("Largo invalido registro tipo 0 (AT2025)");
        }

        return ret;
    }

    // =========================================================================
    // REGISTRO TIPO 0 AT2026
    // =========================================================================

    public String generarRegistroTipo0AT2026() throws SerializadorException {

        // Tipo de registro.
        String ret = "0";

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L);

        // Numero de formulario.
        ret += "1922";

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total de registros a transmitir.
        ret += String.format("%010d", getCantidadTotalRegistros());

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

        // Relleno.
        ret += Serializador.espacios(636);

        if (ret.length() != LARGO_REGISTRO_AT2026) {
            throw new SerializadorException("Largo invalido registro tipo 0 (AT2026)");
        }

        return ret;
    }

    // =========================================================================
    // REGISTRO TIPO 1 AT2025
    // =========================================================================


    public String generarRegistroTipo1AT2025() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L) + "00";

        // Numero de formulario.
        ret += "1922";

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
        String RazonSocial = getContexto().getRazonSocial();
        RazonSocial = Serializador.removerCaracteresEspeciales(RazonSocial);
        ret += String.format("%-30s", (RazonSocial).length() > 30 ? (RazonSocial).substring(0,30) : (RazonSocial));

        // Direccion postal.
        String DireccionDeclaracion = getContexto().getDireccionDeclaracion();
        DireccionDeclaracion = Serializador.removerCaracteresEspeciales(DireccionDeclaracion);
        ret += String.format("%-35s", (DireccionDeclaracion).length() > 35 ? (DireccionDeclaracion).substring(0,35) : (DireccionDeclaracion));

        // Comuna.
        String ComunaDeclaracion = getContexto().getComunaDeclaracion();
        ComunaDeclaracion = Serializador.removerCaracteresEspeciales(ComunaDeclaracion);
        ret += String.format("%-15s", (ComunaDeclaracion).length() > 15 ? (ComunaDeclaracion).substring(0,15) : (ComunaDeclaracion));

        // Correo electronico.
        String EmailDeclaracion = getContexto().getEmailDeclaracion();
        ret += String.format("%-30s", (EmailDeclaracion).length() > 30 ? (EmailDeclaracion).substring(0,30) : (EmailDeclaracion));

        // Fax.
        ret += "56020000000";

        // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(742);

        if (ret.length() != LARGO_REGISTRO_AT2025) {
            throw new SerializadorException("Largo invalido registro tipo 1 (AT2025)");
        }

        return ret;
    }

    // =========================================================================
    // REGISTRO TIPO 1 AT2026
    // =========================================================================

    public String generarRegistroTipo1AT2026() throws SerializadorException {

        // Tipo de registro y precedencia.
        String ret = "11";

        // Ano tributario.
        ret += String.format("%04d", getContexto().getAnoTributario()+1L) + "00";

        // Numero de formulario.
        ret += "1922";

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
        String RazonSocial = getContexto().getRazonSocial();
        RazonSocial = Serializador.removerCaracteresEspeciales(RazonSocial);
        ret += String.format("%-30s", (RazonSocial).length() > 30 ? (RazonSocial).substring(0,30) : (RazonSocial));

        // Direccion postal.
        String DireccionDeclaracion = getContexto().getDireccionDeclaracion();
        DireccionDeclaracion = Serializador.removerCaracteresEspeciales(DireccionDeclaracion);
        ret += String.format("%-35s", (DireccionDeclaracion).length() > 35 ? (DireccionDeclaracion).substring(0,35) : (DireccionDeclaracion));

        // Comuna.
        String ComunaDeclaracion = getContexto().getComunaDeclaracion();
        ComunaDeclaracion = Serializador.removerCaracteresEspeciales(ComunaDeclaracion);
        ret += String.format("%-15s", (ComunaDeclaracion).length() > 15 ? (ComunaDeclaracion).substring(0,15) : (ComunaDeclaracion));

        // Correo electronico.
        String EmailDeclaracion = getContexto().getEmailDeclaracion();  
        ret += String.format("%-30s", (EmailDeclaracion).length() > 30 ? (EmailDeclaracion).substring(0,30) : (EmailDeclaracion));
    
        // Fax.
        ret += "56020000000";

        // Fono.
        ret += String.format("5602%07d", getContexto().getTelefonoDeclaracion() % 10000000);

        // Codigo de empresa.
        ret += String.format("%02d", getContexto().getCodigoEmpresa());

        // Numero de cliente.
        ret += String.format("%04d", getContexto().getNumeroCliente());

        // Unidad, caja y paquete.
        ret += Serializador.ceros(13);

        // Relleno.
        ret += Serializador.espacios(521);

        if (ret.length() != LARGO_REGISTRO_AT2026) {
            throw new SerializadorException("Largo invalido registro tipo 1 (AT2026)");
        }

        return ret;
    }

    // =========================================================================
    // REGISTRO TIPO 2 AT2025
    // =========================================================================

    public String generarRegistroTipo2AT2025(final Registro registro) throws SerializadorException {

        final RegistroDJ1922 registroDJ1922 = new RegistroDJ1922(registro);
        actualizarAcumuladores(registroDJ1922);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21922";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        ret += "1";

        // RUT Administradora.
        String RutAdmin = Serializador.serializarRut(registroDJ1922.getRutAdministradora());
        ret += String.format("%-9s", (RutAdmin).length() > 9 ? (RutAdmin).substring(0,9) : (RutAdmin));

        // RUT Fondo.
        String RutFondo = Serializador.serializarRut(registroDJ1922.getRutFondo());
        ret += String.format("%-9s", (RutFondo).length() > 9 ? (RutFondo).substring(0,9) : (RutFondo));

        // RUN Fondo.
        String RunFondo = Serializador.serializarRut(registroDJ1922.getRunFondo());
        ret += String.format("%-9s", (RunFondo).length() > 9 ? (RunFondo).substring(0,9) : (RunFondo));

        // Tipo de fondo.
        ret += String.format("%1s", registroDJ1922.getTipoFondo());

        // Tributa.
        String TributaSa = registroDJ1922.getTributaSa();
        ret += String.format("%-1s", (TributaSa).length() > 1 ? (TributaSa).substring(1) : (TributaSa));

        // Nombre o Razon Social del Participe.
        String NombreParticipe = registroDJ1922.getNombreParticipe();
        NombreParticipe = Serializador.removerCaracteresEspeciales(NombreParticipe);
        ret += String.format("%-30s", (NombreParticipe).length() > 30 ? (NombreParticipe).substring(0,30) : (NombreParticipe));

        // RUT Participe.
        String RutCliente = Serializador.serializarRut(registroDJ1922.getRutCliente());
        ret += String.format("%-9s", (RutCliente).length() > 9 ? (RutCliente).substring(9) : (RutCliente));

        // Tax ID.
        String TaxId = registroDJ1922.getTaxId();
        ret += String.format("%-15s", (TaxId).length() > 15 ? (TaxId).substring(15) : (TaxId));

        // Codigo Pais Residencia.
        String CodigoPais = registroDJ1922.getCodigoPais();
        ret += String.format("%-2s", (CodigoPais).length() > 2 ? (CodigoPais).substring(2) : (CodigoPais));

        // Tipo Operacion.
        String TipoOperacion = registroDJ1922.getTipoOperacion();
        ret += String.format("%-1s", (TipoOperacion).length() > 1 ? (TipoOperacion).substring(1) : (TipoOperacion));

        // Fecha Inversion.
        ret += Serializador.serializar(registroDJ1922.getFechaInversion());

        // Moneda Reajuste.
        String MonedaReajuste = registroDJ1922.getMonedaReajuste();
        ret += String.format("%-1s", (MonedaReajuste).length() > 1 ? (MonedaReajuste).substring(1) : (MonedaReajuste));

        // Monto nominal de aportes.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getMontoNominalAportesActualizado()).replace(".", ""))).replace(" ","0");

        // Numero de Serie de Cuotas.
        ret += String.format("%21s", registroDJ1922.getNumSerieCuotasParticipe_C138());

        // Numero de Cuotas de la Serie.
        ret += String.format("%10s", (String.format("%010.0f", registroDJ1922.getCantidadCuotasParticipe()).replace(".", ""))).replace(" ","0");

        // Participe Cumple Requisitos.
        ret += String.format("%1d", registroDJ1922.getParticipeCumpleRequisitos());

        // Cuotas de Fondos que Cumplen Requisitos.
        ret += String.format("%1d", registroDJ1922.getCuotasCumplenRequisitos());

        // Valor de la Cuota.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getValorCuotaPesos()).replace(".", ""))).replace(" ","0");

        // Valor mercado.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getNumCuotasSerieParticipe()).replace(".", ""))).replace(" ","0");

        // Porcentaje de participacion en el Patrimonio del Fondo al 31-12.
        ret += String.format("%5s", (String.format("%03.2f", registroDJ1922.getPorcentajeParticipacion31Dic()).replace(".", ""))).replace(" ","0");

        // Valor Nominal Cuotas Rescatadas.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getValNominalCuotasRescatadas()).replace(".", ""))).replace(" ","0");

        // Cantidad de Cuotas Rescatadas.
        ret += String.format("%10s", (String.format("%010.0f", registroDJ1922.getCantidadCuotasRescatadas()).replace(".", ""))).replace(" ","0");

        // Diferencia Rescate Enajenacion.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDiferenciaPorRescateOEnajenacionActualizado()).replace(".", ""))).replace(" ","0");

        // Diferencia Rescate Enajenacion 107.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDifPorRescateOEnajenacionCFIAcogido107Actualizado()).replace(".", ""))).replace(" ","0");

        // Valor Nominal Inversion a la Fecha de Liquidacion.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getValNominalInversionaFechaLiquidacion()).replace(".", ""))).replace(" ","0");

        // Cantidad de Cuotas Cedidas.
        ret += String.format("%10s", (String.format("%010.0f", registroDJ1922.getCantCuotasCedidasPorLiquidacion()).replace(".", ""))).replace(" ","0");

        // Diferencia Valor Cuotas Fecha Liquidacion.
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDiferenciaValorCuotasFechaLiquidacionActualizado()).replace(".", ""))).replace(" ","0");

        //c66
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCConIDPC()).replace(".", ""))).replace(" ","0");

        //c67
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCConIDPCal31122016()).replace(".", ""))).replace(" ","0");

        //c68
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCConIDPCVOL()).replace(".", ""))).replace(" ","0");

        //c69
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCSINDC()).replace(".", ""))).replace(" ","0");

        //c96
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCProvRap_C96()).replace(".", ""))).replace(" ","0");

        //c97
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCORP_C97()).replace(".", ""))).replace(" ","0");

        //c98
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCExDD_C98()).replace(".", ""))).replace(" ","0");

        //c99
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCUtAFFut_C99()).replace(".", ""))).replace(" ","0");

        //c70
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistNoRentaFUT()).replace(".", ""))).replace(" ","0");

        //c100
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasExIGCAfecImptoAdi_C100()).replace(".", ""))).replace(" ","0");

        //c28
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosExentosIGCoAdicionalActualizado()).replace(".", ""))).replace(" ","0");

        //c29
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosNoConstitutivosDeRentaActualizado()).replace(".", ""))).replace(" ","0");

        //c31
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistNoRentaFE()).replace(".", ""))).replace(" ","0");

        //c30
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosAfectosImptoUnicoTasa10Actualizado()).replace(".", ""))).replace(" ","0");

        //c32
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosAfectosImptoUnicoTasa4Actualizado()).replace(".", ""))).replace(" ","0");

        //c33
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDistribucionXDiminucionNoImputadaCapital()).replace(".", ""))).replace(" ","0");

        //c34
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDevolucionesDeCapitalActualizado()).replace(".", ""))).replace(" ","0");

        //c71
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompNSRSD()).replace(".", ""))).replace(" ","0");

        //c72
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompNSRCD()).replace(".", ""))).replace(" ","0");

        //c101
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RANSRD01012020SDD_C101()).replace(".", ""))).replace(" ","0");

        //c102
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RANSRD01012020CDD_C102()).replace(".", ""))).replace(" ","0");

        //c73
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompSRSD()).replace(".", ""))).replace(" ","0");

        //c74
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompSRCD()).replace(".", ""))).replace(" ","0");

        //c103
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RXSRSDD_C103()).replace(".", ""))).replace(" ","0");

        //c104
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RXSRCDD_C104()).replace(".", ""))).replace(" ","0");

        //c75
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCTDCIF()).replace(".", ""))).replace(" ","0");

        //c77
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompSDD31122016()).replace(".", ""))).replace(" ","0");

        //c76
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCDD31122016()).replace(".", ""))).replace(" ","0");

        //c105
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumH31122016RXSDD_C105()).replace(".", ""))).replace(" ","0");

        //c106
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumH31122016RXCDD_C106()).replace(".", ""))).replace(" ","0");

        //c78
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCTDCIF31122016()).replace(".", ""))).replace(" ","0");

        //c79
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCITAExArt21()).replace(".", ""))).replace(" ","0");

        //c42
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getMontoInversionenCuotasCFIAnterior93()).replace(".", ""))).replace(" ","0");

        //c43
        //ret += String.format( "%7s" , (String.format("%07.0f", registroDJ1922.getNumeroCertificado()).replace(".", ""))).replace(" ","0");
        ret += String.format("%07d", registroDJ1922.getNumeroCertificado());

        //c107
        String PerRutFdo = Serializador.serializarRut(registroDJ1922.getDetBenNetoPerRutFdo_C107());
        ret += String.format("%-9s", (PerRutFdo).length() > 9 ? (PerRutFdo).substring(0,9) : (PerRutFdo));

        try {
            //c113
            ret += String.format("%18s", (String.format("%018.0f", registroDJ1922.getDetBenNetoPerPOS_C113()).replace(".", ""))).replace(" ","0");

            //c114
            ret += String.format("%20s", (String.format("%020.0f", registroDJ1922.getDetBenNetoPerNEG_C114()).replace(".", ""))).replace(" ","0");

            //c133
            ret += String.format("%20s", (String.format("%020.0f", registroDJ1922.getDetBenNetoUtilPer_C133()).replace(".", ""))).replace(" ","0");

            //c134
            ret += String.format("%20s", (String.format("%020.0f", registroDJ1922.getDetBenNetoIntPer_C134()).replace(".", ""))).replace(" ","0");

            //c135
            ret += String.format("%20s", (String.format("%020.0f", registroDJ1922.getDetBenNetoDivPer_C135()).replace(".", ""))).replace(" ","0");

            //c136
            ret += String.format("%20s", (String.format("%020.0f", registroDJ1922.getDetBenNetoGanCapPer_C136()).replace(".", ""))).replace(" ","0");

            //c137
            ret += String.format("%20s", (String.format("%020.0f", registroDJ1922.getDetBenNetoPerGastDev_C137()).replace(".", ""))).replace(" ","0");
        } catch (Exception ex) {
            throw new SerializadorException("error en campos detbenneto: " + ex.getMessage());
        }

        if (ret.length() != LARGO_REGISTRO_AT2025) {
            throw new SerializadorException("Largo invalido registro tipo 2 (AT2025)");
        }

        return ret;
    }

    // =========================================================================
    // REGISTRO TIPO 2 AT2026
    // =========================================================================

    public String generarRegistroTipo2AT2026(final Registro registro) 
        throws SerializadorException {

        final RegistroDJ1922 registroDJ1922 = new RegistroDJ1922(registro);
        actualizarAcumuladores(registroDJ1922);

        // Tipo de Registro y Numero de Formulario.
        String ret = "21922";

        // Codigo de Presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de Declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // Tipo de Declaracion.
        ret += getContexto().getTipoDeclaracion().getCodigo();

        // RUT Declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        //Tipo de contribuyente C138
        String TipoContribuyente = registroDJ1922.getTipoContribuyente();
        ret += String.format("%-1s", (TipoContribuyente).length() > 1 ? (TipoContribuyente).substring(1) : (TipoContribuyente));

        // RUT Administradora
        String RutAdmin = Serializador.serializarRut(registroDJ1922.getRutAdministradora());
        ret += String.format("%-9s", (RutAdmin).length() > 9 ? (RutAdmin).substring(0,9) : (RutAdmin));

        // RUT Fondo
        String RutFondo = Serializador.serializarRut(registroDJ1922.getRutFondo());
        ret += String.format("%-9s", (RutFondo).length() > 9 ? (RutFondo).substring(0,9) : (RutFondo));

        //RUN o Nemotecnico C139
        String RunNemotecnico = registroDJ1922.getRunNemotecnico();
        ret += String.format("%-21s", (RunNemotecnico).length() > 21 ? (RunNemotecnico).substring(0,21) : (RunNemotecnico));

        // Nombre o Razon Social del Participe
        String NombreParticipe = registroDJ1922.getNombreParticipe();
        NombreParticipe = Serializador.removerCaracteresEspeciales(NombreParticipe);
        ret += String.format("%-30s", (NombreParticipe).length() > 30 ? (NombreParticipe).substring(0,30) : (NombreParticipe));

        // RUT Participe
        String RutCliente = Serializador.serializarRut(registroDJ1922.getRutCliente());
        ret += String.format("%-9s", (RutCliente).length() > 9 ? (RutCliente).substring(9) : (RutCliente));

        // Tax ID
        String TaxId = registroDJ1922.getTaxId();
        ret += String.format("%-15s", (TaxId).length() > 15 ? (TaxId).substring(15) : (TaxId));

        // Codigo Pais Residencia
        String CodigoPais = registroDJ1922.getCodigoPais();
        ret += String.format("%-2s", (CodigoPais).length() > 2 ? (CodigoPais).substring(2) : (CodigoPais));

        // Tipo Operacion
        String TipoOperacion = registroDJ1922.getTipoOperacion();
        ret += String.format("%-1s", (TipoOperacion).length() > 1 ? (TipoOperacion).substring(1) : (TipoOperacion));

        // Fecha Inversion
        ret += Serializador.serializar(registroDJ1922.getFechaInversion());

        // Valor Nominal Cuotas Rescatadas
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getValNominalCuotasRescatadas()).replace(".", ""))).replace(" ","0");

        // Cantidad de Cuotas Rescatadas
        ret += String.format("%10s", (String.format("%010.0f", registroDJ1922.getCantidadCuotasRescatadas()).replace(".", ""))).replace(" ","0");

        // Diferencia Rescate Enajenacion
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDiferenciaPorRescateOEnajenacionActualizado()).replace(".", ""))).replace(" ","0");

        // Diferencia Rescate Enajenacion 107
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDifPorRescateOEnajenacionCFIAcogido107Actualizado()).replace(".", ""))).replace(" ","0");

        // Valor Nominal Inversion a la Fecha de Liquidacion
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getValNominalInversionaFechaLiquidacion()).replace(".", ""))).replace(" ","0");

        // Cantidad de Cuotas Cedidas
        ret += String.format("%10s", (String.format("%010.0f", registroDJ1922.getCantCuotasCedidasPorLiquidacion()).replace(".", ""))).replace(" ","0");

        // Diferencia Valor Cuotas Fecha Liquidacion
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDiferenciaValorCuotasFechaLiquidacionActualizado()).replace(".", ""))).replace(" ","0");

        //c66
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCConIDPC()).replace(".", ""))).replace(" ","0");

        //c67
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCConIDPCal31122016()).replace(".", ""))).replace(" ","0");

        //c68
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCConIDPCVOL()).replace(".", ""))).replace(" ","0");

        //c69
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistAfectGCSINDC()).replace(".", ""))).replace(" ","0");

        //c96
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCProvRap_C96()).replace(".", ""))).replace(" ","0");

        //c97
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCORP_C97()).replace(".", ""))).replace(" ","0");

        //c98
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCExDD_C98()).replace(".", ""))).replace(" ","0");

        //c99
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasTribCUtAFFut_C99()).replace(".", ""))).replace(" ","0");

        //c70
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistNoRentaFUT()).replace(".", ""))).replace(" ","0");

        //c100
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDisRemRentasExIGCAfecImptoAdi_C100()).replace(".", ""))).replace(" ","0");

        //c28
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosExentosIGCoAdicionalActualizado()).replace(".", ""))).replace(" ","0");

        //c29
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosNoConstitutivosDeRentaActualizado()).replace(".", ""))).replace(" ","0");

        //c31
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDivRemDistNoRentaFE()).replace(".", ""))).replace(" ","0");

        //c30
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosAfectosImptoUnicoTasa10Actualizado()).replace(".", ""))).replace(" ","0");

        //c32
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDividendosAfectosImptoUnicoTasa4Actualizado()).replace(".", ""))).replace(" ","0");

        //c33
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDistribucionXDiminucionNoImputadaCapital()).replace(".", ""))).replace(" ","0");

        //c34
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getDevolucionesDeCapitalActualizado()).replace(".", ""))).replace(" ","0");

        //c71
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompNSRSD()).replace(".", ""))).replace(" ","0");

        //c72
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompNSRCD()).replace(".", ""))).replace(" ","0");

        //c101
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RANSRD01012020SDD_C101()).replace(".", ""))).replace(" ","0");

        //c102
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RANSRD01012020CDD_C102()).replace(".", ""))).replace(" ","0");

        //c73
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompSRSD()).replace(".", ""))).replace(" ","0");

        //c74
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompSRCD()).replace(".", ""))).replace(" ","0");

        //c103
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RXSRSDD_C103()).replace(".", ""))).replace(" ","0");

        //c104
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumD01012017RXSRCDD_C104()).replace(".", ""))).replace(" ","0");

        //c75
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCTDCIF()).replace(".", ""))).replace(" ","0");

        //c77
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompSDD31122016()).replace(".", ""))).replace(" ","0");

        //c76
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCDD31122016()).replace(".", ""))).replace(" ","0");

        //c105
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumH31122016RXSDD_C105()).replace(".", ""))).replace(" ","0");

        //c106
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredIGCAcumH31122016RXCDD_C106()).replace(".", ""))).replace(" ","0");

        //c78
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCTDCIF31122016()).replace(".", ""))).replace(" ","0");

        //c79
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getCredImpGloCompCITAExArt21()).replace(".", ""))).replace(" ","0");

        //c42
        ret += String.format("%15s", (String.format("%015.0f", registroDJ1922.getMontoInversionenCuotasCFIAnterior93()).replace(".", ""))).replace(" ","0");

        //c43
        ret += String.format("%07d", registroDJ1922.getNumeroCertificado());

        if (ret.length() != LARGO_REGISTRO_AT2026) {
            throw new SerializadorException("Largo invalido registro tipo 2 (AT2026)");
        }

        return ret;
    }

    // =========================================================================
    // REGISTRO TIPO 3 — AT2025
    // =========================================================================

    public String generarRegistroTipo3AT2025() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311922";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());

        // Suma por Diferencia por Rescate o Enajenacion
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDiferenciaPorRescateOEnajenacionActualizado()).replace(".", ""))).replace(" ","0");

        //C45 + c23
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDifPorRescateOEnajenacionCFIAcogido107Actualizado()).replace(".", ""))).replace(" ","0");

        //c46 + c26
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDiferenciaValorCuotasFechaLiquidacionActualizado()).replace(".", ""))).replace(" ","0");

        //c81 + c66
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCConIDPC()).replace(".", ""))).replace(" ","0");

        //c82 + c67
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCConIDPCal31122016()).replace(".", ""))).replace(" ","0");

        //c83 + c68
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCConIDPCVOL()).replace(".", ""))).replace(" ","0");

        //c84 + c69
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCSINDC()).replace(".", ""))).replace(" ","0");

        //c115 + c96
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCProvRap_C96()).replace(".", ""))).replace(" ","0");

        //c116 + c97
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCORP_C97()).replace(".", ""))).replace(" ","0");

        //c117 + c98
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCExDD_C98()).replace(".", ""))).replace(" ","0");

        //c118 + c99
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCUtAFFut_C99()).replace(".", ""))).replace(" ","0");

        //c85 + c70
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistNoRentaFUT()).replace(".", ""))).replace(" ","0");

        //c119 + c100
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasExIGCAfecImptoAdi_C100()).replace(".", ""))).replace(" ","0");

        //c48 + c28
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosExentosIGCoAdicionalActualizado()).replace(".", ""))).replace(" ","0");

        //c49 + c29
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosNoConstitutivosDeRentaActualizado()).replace(".", ""))).replace(" ","0");

        //c86 + c31
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistNoRentaFE()).replace(".", ""))).replace(" ","0");

        //c50 + c30
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosAfectosImptoUnicoTasa10Actualizado()).replace(".", ""))).replace(" ","0");

        //c52 + c32
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosAfectosImptoUnicoTasa4Actualizado()).replace(".", ""))).replace(" ","0");

        //c53 + c33
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDistribucionXDiminucionNoImputadaCapital()).replace(".", ""))).replace(" ","0");

        //c54 + c34
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDevolucionesDeCapitalActualizado()).replace(".", ""))).replace(" ","0");

        //c87 + c71
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompNSRSD()).replace(".", ""))).replace(" ","0");

        //c88 + c72
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompNSRCD()).replace(".", ""))).replace(" ","0");

        //c120 + c101
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RANSRD01012020SDD_C101()).replace(".", ""))).replace(" ","0");

        //c121 + c102
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RANSRD01012020CDD_C102()).replace(".", ""))).replace(" ","0");

        //c89 + c73
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompSRSD()).replace(".", ""))).replace(" ","0");

        //c90 + c74
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompSRCD()).replace(".", ""))).replace(" ","0");

        //c122 + c103
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RXSRSDD_C103()).replace(".", ""))).replace(" ","0");

        //c123 + c104
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RXSRCDD_C104()).replace(".", ""))).replace(" ","0");

        //c91 + c75
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCTDCIF()).replace(".", ""))).replace(" ","0");

        //c93 + c77
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompSDD31122016()).replace(".", ""))).replace(" ","0");

        //c92 + c76
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCDD31122016()).replace(".", ""))).replace(" ","0");

        //c124 + c105
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumH31122016RXSDD_C105()).replace(".", ""))).replace(" ","0");

        //c125 + c106
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumH31122016RXCDD_C106()).replace(".", ""))).replace(" ","0");

        //c94 + c78
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCTDCIF31122016()).replace(".", ""))).replace(" ","0");

        //c95 + c79
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCITAExArt21()).replace(".", ""))).replace(" ","0");

        //c62 + c42
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaMontoInversionenCuotasCFIAnterior93()).replace(".", ""))).replace(" ","0");

        //c131 + c113
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDetBenNetoPerPOS_C113()).replace(".", ""))).replace(" ","0");

        //c132 + c114
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDetBenNetoPerNEG_C114()).replace(".", ""))).replace(" ","0");

        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(211);

        if (ret.length() != LARGO_REGISTRO_AT2025) {
            throw new SerializadorException("Largo invalido registro tipo 3 (AT2025)");
        }

        return ret;
    }

    // =========================================================================
    // REGISTRO TIPO 3 — AT2026
    // =========================================================================

    public String generarRegistroTipo3AT2026() throws SerializadorException {

        // Tipo de registro y numero de formulario.
        String ret = "311922";

        // Codigo de presentacion.
        ret += getContexto().getModoPresentacion().getCodigo();

        // Folio de declaracion.
        ret += String.format("%07d", getContexto().getFolioDeclaracion());

        // RUT declarante.
        ret += Serializador.serializarRut(getContexto().getRutDeclarante());

        // Total de casos informados.
        ret += String.format("%010d", getCantidadCasosInformados());

        // Suma por Diferencia por Rescate o Enajenacion.
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDiferenciaPorRescateOEnajenacionActualizado()).replace(".", ""))).replace(" ","0");

        //C45 + c23
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDifPorRescateOEnajenacionCFIAcogido107Actualizado()).replace(".", ""))).replace(" ","0");

        //c46 + c26
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDiferenciaValorCuotasFechaLiquidacionActualizado()).replace(".", ""))).replace(" ","0");

        //c81 + c66
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCConIDPC()).replace(".", ""))).replace(" ","0");

        //c82 + c67
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCConIDPCal31122016()).replace(".", ""))).replace(" ","0");

        //c83 + c68
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCConIDPCVOL()).replace(".", ""))).replace(" ","0");

        //c84 + c69
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistAfectGCSINDC()).replace(".", ""))).replace(" ","0");

        //c115 + c96
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCProvRap_C96()).replace(".", ""))).replace(" ","0");

        //c116 + c97
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCORP_C97()).replace(".", ""))).replace(" ","0");

        //c117 + c98
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCExDD_C98()).replace(".", ""))).replace(" ","0");

        //c118 + c99
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasTribCUtAFFut_C99()).replace(".", ""))).replace(" ","0");

        //c85 + c70
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistNoRentaFUT()).replace(".", ""))).replace(" ","0");

        //c119 + c100
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDisRemRentasExIGCAfecImptoAdi_C100()).replace(".", ""))).replace(" ","0");

        //c48 + c28
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosExentosIGCoAdicionalActualizado()).replace(".", ""))).replace(" ","0");

        //c49 + c29
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosNoConstitutivosDeRentaActualizado()).replace(".", ""))).replace(" ","0");

        //c86 + c31
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDivRemDistNoRentaFE()).replace(".", ""))).replace(" ","0");

        //c50 + c30
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosAfectosImptoUnicoTasa10Actualizado()).replace(".", ""))).replace(" ","0");

        //c52 + c32
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDividendosAfectosImptoUnicoTasa4Actualizado()).replace(".", ""))).replace(" ","0");

        //c53 + c33
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDistribucionXDiminucionNoImputadaCapital()).replace(".", ""))).replace(" ","0");

        //c54 + c34
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaDevolucionesDeCapitalActualizado()).replace(".", ""))).replace(" ","0");

        //c87 + c71
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompNSRSD()).replace(".", ""))).replace(" ","0");

        //c88 + c72
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompNSRCD()).replace(".", ""))).replace(" ","0");

        //c120 + c101
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RANSRD01012020SDD_C101()).replace(".", ""))).replace(" ","0");

        //c121 + c102
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RANSRD01012020CDD_C102()).replace(".", ""))).replace(" ","0");

        //c89 + c73
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompSRSD()).replace(".", ""))).replace(" ","0");

        //c90 + c74
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompSRCD()).replace(".", ""))).replace(" ","0");

        //c122 + c103
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RXSRSDD_C103()).replace(".", ""))).replace(" ","0");

        //c123 + c104
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumD01012017RXSRCDD_C104()).replace(".", ""))).replace(" ","0");

        //c91 + c75
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCTDCIF()).replace(".", ""))).replace(" ","0");

        //c93 + c77
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompSDD31122016()).replace(".", ""))).replace(" ","0");

        //c92 + c76
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCDD31122016()).replace(".", ""))).replace(" ","0");

        //c124 + c105
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumH31122016RXSDD_C105()).replace(".", ""))).replace(" ","0");

        //c125 + c106
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredIGCAcumH31122016RXCDD_C106()).replace(".", ""))).replace(" ","0");

        //c94 + c78
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCTDCIF31122016()).replace(".", ""))).replace(" ","0");

        //c95 + c79
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaCredImpGloCompCITAExArt21()).replace(".", ""))).replace(" ","0");

        //c62 + c42
        ret += String.format("%18s", (String.format("%018.0f", registroResumen.getsumaMontoInversionenCuotasCFIAnterior93()).replace(".", ""))).replace(" ","0");

        // Fecha de presentacion.
        ret += getContexto().getFechaPresentacion();

        // RUT del representante legal.
        ret += Serializador.serializarRut(getContexto().getRutRepresentante());

        // Relleno.
        ret += Serializador.espacios(26);

        if (ret.length() != LARGO_REGISTRO_AT2026) {
            throw new SerializadorException("Largo invalido registro tipo 3 (AT2026)");
        }

        return ret;
    }

    
   @Override
    public void generarResumen() throws SerializadorException {
        if (versionDJ >= 2025) {
            print(generarRegistroTipo3AT2026());
        } else {
            print(generarRegistroTipo3AT2025());
        }
        println("");
    }

    private long getCantidadTotalRegistros() {
        return getCantidadCasosInformados() + 3;
    }
}
