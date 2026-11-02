package cl.bcs.fondomutuo.aportereinversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.List;

import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.ConsultaRegimenReinversion;
import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.comisionnn.Pojos;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.normal.NormalDatos.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.IndicadoresPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InPortafolio;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes;

/**
 * Builds, for every request of the lot, the row of {@code FMM.SolicitudAporteRescate}, the data the requirement and equity checks need, and the output element
 * (legacy :719-1018). A null series, client, portfolio indicator or regime raises the same NullPointerException as the legacy on the same expression: the accesses are kept as written.
 */
final class AporteReinversionArmado {

    private AporteReinversionArmado() {
    }

    static void armar(AporteReinversionContexto c) throws Exception {
        AporteReinversionContrato.Entrada entrada = c.entrada;
        c.entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
        c.entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
        c.entradaValidaPatrimonioEmisor.p_IdEmpresa = entrada.p_IdEmpresa;
        c.entradaValidaPatrimonioEmisor.p_IdUsuario = entrada.p_IdUsuario;
        c.entradaValidaPatrimonioEmisor.p_Fecha = c.fechaSistema;
        int i = 0;
        for (InSolicitudesAporte solicitud : entrada.p_SolicitudesAporte) {
            SolicitudAporteRescate sol = fila(c, solicitud, i);
            c.solicitudesProcesar.add(sol);
            requisitos(c, solicitud, sol);
            c.resultadoTemp.add(salida(c, solicitud, sol));
            i++;
        }
        c.hSolicitudSalida = cl.bcs.fondomutuo.requisitos.Hashes.aHash(c.resultadoTemp, r -> r.IdInterno);
    }

    /** Legacy :736-887: the row of one request. */
    private static SolicitudAporteRescate fila(AporteReinversionContexto c, InSolicitudesAporte solicitud, int i) {
        AporteReinversionContrato.Entrada entrada = c.entrada;
        boolean generaSpot = false;
        ClientePortafolioSolicitudes cliente = c.hclientes.get(solicitud.IdPortafolio);
        SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
        CalcularMontoNumeroCuota.Resultado vc = c.valoresCuota.get(solicitud.IdInterno);
        RegimenSerieDeFondo r = c.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
        ResultadoFecha fecha = c.hfechas.get(solicitud.IdInterno);
        SolicitudAporteRescate sol = new SolicitudAporteRescate();

        if ((solicitud.IdMoneda.equals(AporteReinversionConstantes.PESOS) && serie.IdMoneda.equals(AporteReinversionConstantes.DOLAR)
                && c.aporteGeneraCompraSpotDolar.equals(AporteReinversionConstantes.SI))
                || (solicitud.IdMoneda.equals(AporteReinversionConstantes.DOLAR) && serie.IdMoneda.equals(AporteReinversionConstantes.PESOS)
                        && c.aporteGeneraCompraSpotDolar.equals(AporteReinversionConstantes.SI))) {
            generaSpot = true;
            sol.IdMonedaPago = solicitud.IdMoneda;
        } else {
            sol.IdMonedaPago = serie.IdMoneda;
        }

        sol.IdInterno = solicitud.IdInterno;
        sol.IdSolicitudAporteRescate = c.secuencias.get(i);
        sol.IdCliente = solicitud.IdCliente;
        sol.IdPortafolio = solicitud.IdPortafolio;
        sol.TipoSolicitud = AporteReinversionConstantes.TIPO_SOLICITUD_APORTE;
        sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
        sol.IdTipoMovimientoFondoMutuo = AporteReinversionConstantes.APORTE_REINVERSION;
        sol.IdDividendo = 0L;
        sol.IdSerieFondoDestino = "";
        sol.IdEmisorDestino = "";
        sol.IdClienteDestino = 0L;
        sol.IdPortafolioDestino = 0L;
        sol.IndicadorMLT = solicitud.IndicadorMLT;
        sol.Overnight = AporteReinversionConstantes.NO;
        sol.IdSerieFondoOrigen = solicitud.IdSerieFondoOrigen;
        sol.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado == null ? 0L : solicitud.IdSolicitudAporteRescateRelacionado;
        sol.TipoCarteraNormativa = r != null ? r.TipoCarteraNormativa : "";
        sol.IdTipoCartera = "";
        sol.CalculoPorMontoCuota = AporteReinversionConstantes.CALCULO_POR_MONTO;
        sol.FechaEmision = FechaLegacy.aFechaSql(c.fechaSistema);
        sol.FechaOriginalInversion = FechaLegacy.aFechaSql(solicitud.FechaOrigenInversion);
        sol.FechaIngresoRegistro = c.fechaHoraUTC;
        sol.IdMoneda = generaSpot ? serie.IdMoneda : solicitud.IdMoneda;
        sol.Estado = AporteReinversionConstantes.ESTADO_INGRESADO;
        sol.IdMedioDePago = solicitud.IdMedioDePago;
        sol.Glosa = solicitud.Glosa;
        sol.IdEmpresa = entrada.p_IdEmpresa;
        sol.IdUsuario = entrada.p_IdUsuario;
        sol.IdModulo = entrada.p_IdModulo;
        sol.IdEmisorOrigen = solicitud.IdEmisorOrigen;
        sol.MontoOrigen = solicitud.MontoOrigen;
        sol.SolicitudBatch = solicitud.SolicitudBatch;
        sol.OrigenOperacion = solicitud.OrigenOperacion;
        sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
        sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
        sol.IdRegimenTributario = solicitud.IdRegimenTributario;
        sol.IngresoAutomatico = solicitud.IngresoAutomatico;
        sol.EsPorMontoLiquido = AporteReinversionConstantes.NO;
        sol.IdTipoCanalIngreso = solicitud.Canal != null ? solicitud.Canal : entrada.p_IdTipoCanalIngreso;
        sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : c.folioContratoSolicitudes.get(entrada.p_SolicitudesAporte.indexOf(solicitud));
        sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
        sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
        sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
        sol.Visado = AporteReinversionConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud)
                && AporteReinversionConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago) ? AporteReinversionConstantes.NO : solicitud.Visado;
        sol.IdPreferenciaPagoPortafolio = 0L;
        sol.EstadoProceso = AporteReinversionConstantes.ESTADO_PENDIENTE;
        sol.Monto = BigDecimal.ZERO;
        sol.NumeroCuotas = BigDecimal.ZERO;
        sol.ValorCuota = BigDecimal.ZERO;
        sol.CostoMonedaLocal = BigDecimal.ZERO;
        sol.ValorUFFechaInversion = BigDecimal.ZERO;
        sol.ParidadFechaInversion = BigDecimal.ZERO;
        sol.IdMonedaSistema = "";
        sol.MontoMonedaSistema = BigDecimal.ZERO;
        sol.MontoPagoLocal = BigDecimal.ZERO;
        sol.MontoPago = BigDecimal.ZERO;
        sol.MontoComision = BigDecimal.ZERO;
        sol.MontoComisionLocal = BigDecimal.ZERO;
        sol.IdAgrupador = solicitud.IdAgrupador;
        sol.Switch = solicitud.Switch;
        if (fecha != null) {
            sol.FechaAceptacion = sql(fecha.FechaAceptacion);
            sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : sql(fecha.FechaValorCuota);
            sol.FechaCertificacion = sql(fecha.FechaCertificacion);
            sol.FechaLiquidacion = sql(fecha.FechaLiquidacion);
        }
        if (vc != null) {
            sol.Monto = vc.Monto;
            sol.NumeroCuotas = vc.NumeroCuotas;
            sol.ValorCuota = vc.ValorCuota;
            sol.CostoMonedaLocal = generaSpot ? vc.Monto : vc.MontoDestino;
            sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
            sol.ParidadFechaInversion = vc.ValorParidad;
            sol.IdMonedaSistema = vc.IdMonedaSistema;
            sol.MontoMonedaSistema = vc.MontoMonedaSistema;
            sol.MontoComision = BigDecimal.ZERO;
            sol.MontoComisionLocal = BigDecimal.ZERO;
            sol.MontoPagoLocal = generaSpot ? vc.Monto : vc.MontoDestino;
            sol.MontoPago = vc.Monto;
            sol.MontoInversion = vc.Monto;
            sol.CostoMonedaLocalInversion = generaSpot ? vc.Monto : vc.MontoDestino;
            sol.ValorCuotaInversion = vc.ValorCuota;
            sol.ParidadFechaCertificacion = vc.ValorParidad;
            sol.NumeroCuotasInversion = vc.NumeroCuotas;
            // for the reinversion contributions: the cost in UF at the quota date of the contribution is either calculated or inherited from the rescue
            ClientePortafolioSolicitudes codigoPortafolio = c.hclientes.get(sol.IdPortafolio);
            if (calcularHeredarCostoUF(solicitud, codigoPortafolio.CodigoPortafolio, c.regimen, c.regimenSolicitudReinversion)) {
                sol.CostoUF = vc.CostoEnFactorAjuste;
                sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
            } else {
                sol.CostoUF = solicitud.CostoUF;
                sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
            }
        }
        if (generaSpot) {
            sol.IdMoneda = serie.IdMoneda;
            sol.GeneraSpot = "S";
        } else {
            sol.GeneraSpot = "N";
        }
        if (r != null) {
            switch (r.GrupoRegimen) {
            case "APV":
                sol.Tributacion = AporteReinversionConstantes.TRIBUTACION_APV;
                break;
            case "ART108":
                sol.Tributacion = AporteReinversionConstantes.TRIBUTACION_ART108;
                break;
            case "ART107":
                sol.Tributacion = AporteReinversionConstantes.TRIBUTACION_ART107;
                break;
            case "57BIS":
                sol.Tributacion = AporteReinversionConstantes.TRIBUTACION_57BIS;
                break;
            default:
                sol.Tributacion = AporteReinversionConstantes.TRIBUTACION_DEFECTO;
                break;
            }
        }
        sol.EstadoLiquidacion = solicitud.Switch.equalsIgnoreCase("S") ? "LIQ" : "ING";
        return sol;
    }

    /**
     * Legacy :390-413: true = the cost in UF is calculated, false = it is inherited from the rescue. Inherited for the APV-like portfolios (APV-A, BF, DC-DL, DC-AL, DC-R) and for MLT
     * requests when EVERY regime of the lot and of the rescues is 18Q. The legacy swallowed any exception of the whole block (a null regime, say) and kept "calculate": so does this.
     */
    static boolean calcularHeredarCostoUF(InSolicitudesAporte sol, String codigoPortafolio, List<String> regimenesDestino, List<ConsultaRegimenReinversion> regimenSolicitudReinversion) {
        boolean calcula = true;
        try {
            boolean todosSon18Q = regimenesDestino.stream().allMatch(regimen -> regimen.equals("18Q"))
                    && regimenSolicitudReinversion.stream().allMatch(regimen -> regimen.IdRegimenTributario.equals("18Q"));
            if ("APV-A".equalsIgnoreCase(codigoPortafolio) || "BF".equalsIgnoreCase(codigoPortafolio) || "DC-DL".equalsIgnoreCase(codigoPortafolio)
                    || "DC-AL".equalsIgnoreCase(codigoPortafolio) || "DC-R".equalsIgnoreCase(codigoPortafolio)) {
                calcula = false;
            } else if ("S".equalsIgnoreCase(sol.IndicadorMLT) && todosSon18Q) {
                calcula = false;
            }
        } catch (Exception e) {
            // legacy: printStackTrace only; the answer stays "calculate"
        }
        return calcula;
    }

    private static java.sql.Date sql(java.time.LocalDate fecha) {
        return fecha != null ? java.sql.Date.valueOf(fecha) : null;
    }

    /** Legacy :889-986: what the requirement service and the issuer-equity service receive for one request. */
    private static void requisitos(AporteReinversionContexto c, InSolicitudesAporte solicitud, SolicitudAporteRescate sol) {
        TipoMovimientoFondoMutuo tipoMovimiento = c.htipoMovimiento.get(AporteReinversionConstantes.APORTE_REINVERSION);
        SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
        RegimenSerieDeFondo r = c.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
        ClientePortafolioSolicitudes cliente = c.hclientes.get(solicitud.IdPortafolio);
        ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
        if (cliente != null) confPropositoPortafolio = c.hconfRegimenPortafolio.get(solicitud.IdRegimenTributario + "_" + cliente.Proposito);
        IndicadoresPortafolio indicadorPortafolio = c.hindicadoresPortafolio.get(solicitud.IdPortafolio);
        boolean comercial = AporteReinversionConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);
        InSolicitudes solVal = new InSolicitudes();
        InPortafolio portVal = new InPortafolio();

        solVal.IdInterno = sol.IdInterno;
        solVal.IdCliente = sol.IdCliente;
        solVal.IdPortafolio = sol.IdPortafolio;
        solVal.IdSerieFondo = sol.IdSerieFondo;
        solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
        solVal.Monto = sol.Monto;
        solVal.NumeroCuotas = sol.NumeroCuotas;
        solVal.IdEstadoSerie = "";
        solVal.EsDiferido = AporteReinversionConstantes.SI;
        solVal.EsMontoMaximo = AporteReinversionConstantes.SI;
        solVal.EsMontoMinimo = AporteReinversionConstantes.SI;
        solVal.PermiteCambioGrupo = AporteReinversionConstantes.SI;
        solVal.EsPortafolioValido = AporteReinversionConstantes.SI;
        solVal.PermiteRescateMandato = AporteReinversionConstantes.SI;
        solVal.EsCuentaInversionValido = AporteReinversionConstantes.SI;
        solVal.EsFechaValidaAporte = AporteReinversionConstantes.SI;
        solVal.EsFechaValidaRescate = AporteReinversionConstantes.SI;
        solVal.ValorCuota = sol.ValorCuota;
        solVal.Overnight = AporteReinversionConstantes.SI;
        solVal.EsMoneyMarket = AporteReinversionConstantes.SI;
        solVal.EsFamiliaEquivalente = AporteReinversionConstantes.SI;
        solVal.EsPortafolioRegimenValido = AporteReinversionConstantes.SI;
        solVal.EsRegimenValido = AporteReinversionConstantes.SI;
        solVal.PermiteAporte = AporteReinversionConstantes.SI;
        solVal.EsRegimenDestinoValido = AporteReinversionConstantes.SI;
        if (serie != null) {
            solVal.IdEstadoSerie = serie.IdEstado;
            solVal.EstadoFondo = comercial ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
            solVal.EstadoBloqueo = serie.BloqueoAporte;
            solVal.EsMontoMaximo = serie.MontoMaximo.compareTo(BigDecimal.ZERO) != 0
                    && (serie.MontoMaximo.compareTo(sol.Monto) == -1 && serie.MontoMaximo.compareTo(solicitud.MontoTotalInversion) == -1)
                            ? AporteReinversionConstantes.NO : AporteReinversionConstantes.SI;
            if (r != null && AporteReinversionConstantes.SI.equals(r.ValidaMontoMinimoInstrumento)) {
                solVal.EsMontoMinimo = serie.MontoMinimo.compareTo(BigDecimal.ZERO) != 0
                        && (serie.MontoMinimo.compareTo(sol.Monto) == 1 && serie.MontoMinimo.compareTo(solicitud.MontoTotalInversion) == 1)
                                ? AporteReinversionConstantes.NO : AporteReinversionConstantes.SI;
            }
            solVal.EstadoLiquidez = comercial ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
            solVal.EsFondoValido = AporteReinversionConstantes.SI;
            if (!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && AporteReinversionConstantes.NO.equalsIgnoreCase(serie.Diferido)) {
                solVal.EsDiferido = AporteReinversionConstantes.NO;
            }
            // high equity: the amount in system currency is accumulated per client and issuer
            BigDecimal montoPortafolioPorEmisor = c.hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + serie.IdEmisor);
            if (montoPortafolioPorEmisor == null) montoPortafolioPorEmisor = BigDecimal.ZERO;
            montoPortafolioPorEmisor = montoPortafolioPorEmisor.add(sol.MontoMonedaSistema, MathContext.DECIMAL128);
            c.hashMontosPortafolioEmisor.put(sol.IdCliente + "_" + serie.IdEmisor, montoPortafolioPorEmisor);
            if (AporteReinversionConstantes.SI.equals(serie.PatrimonioAltos)) {
                ValidaPatrimonioEmisorService.Solicitud valPatrimonio = new ValidaPatrimonioEmisorService.Solicitud();
                valPatrimonio.IdInterno = sol.IdInterno;
                valPatrimonio.IdCliente = sol.IdCliente;
                valPatrimonio.IdSerieFondo = serie.IdSerieFondo;
                valPatrimonio.IdEmisor = serie.IdEmisor;
                valPatrimonio.MontoPatrimonio = solicitud.MontoPatrimonio;
                valPatrimonio.ValorCuota = sol.ValorCuota;
                c.entradaValidaPatrimonioEmisor.p_Solicitudes.add(valPatrimonio);
            }
        }
        if (r == null) {
            solVal.EsRegimenValido = AporteReinversionConstantes.NO;
        } else {
            solVal.PermiteAporte = r.PermiteAporte;
        }
        if (confPropositoPortafolio == null) solVal.EsPortafolioRegimenValido = AporteReinversionConstantes.NO;
        if (AporteReinversionConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago) && AporteReinversionConstantes.NO.equalsIgnoreCase(indicadorPortafolio.ValorIndicador)) {
            solVal.EsCuentaInversionValido = AporteReinversionConstantes.NO;
        }
        if (cliente != null) {
            portVal.IdInterno = sol.IdInterno;
            portVal.IdPortafolio = sol.IdPortafolio;
            portVal.IdPortafolioReal = cliente.IdPortafolio;
            portVal.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
            portVal.IdCliente = sol.IdCliente;
            portVal.IdClienteReal = cliente.IdCliente;
            portVal.IdEstadoCliente = cliente.IdEstadoCliente;
            portVal.DocumentoIdentidad = cliente.DocumentoIdentidad;
            portVal.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
            portVal.IdClienteDestino = sol.IdClienteDestino;
            portVal.IdClienteRealDestino = 0L;
            portVal.IdPortafolioDestino = sol.IdPortafolio;
            portVal.IdPortafolioRealDestino = 0L;
            portVal.IdEstadoClienteDestino = "";
            portVal.IdEstadoPortafolioDestino = "";
        }
        solVal.IdTipoOperacion = AporteReinversionConstantes.TIPO_OPERACION_APORTE_FONDO_MUTUO;
        solVal.IdTipoProducto = AporteReinversionConstantes.TIPO_OPERACION;
        // portfolios of Bonificacion Fiscal are not checked against the minimum amount (the legacy reads the code without a null check)
        if (cliente.CodigoPortafolio.equalsIgnoreCase(AporteReinversionConstantes.BONIFICACION_FISCAL)) {
            solVal.EsMontoMinimo = AporteReinversionConstantes.SI;
        }
        c.entradaCumpleRequisitos.p_Portafolios.add(portVal);
        c.entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
    }

    /** Legacy :988-1015: the output element of one request (dates as text; {@code IdEmisorDestino} is the issuer of the destination series). */
    private static ResultadoSolicitud salida(AporteReinversionContexto c, InSolicitudesAporte solicitud, SolicitudAporteRescate sol) {
        SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
        ClientePortafolioSolicitudes cliente = c.hclientes.get(solicitud.IdPortafolio);
        ResultadoSolicitud salida = new ResultadoSolicitud();
        salida = Pojos.copiar(sol, salida);
        if (serie != null) {
            salida.IdFondo = serie.IdFondo;
            salida.Nemotecnico = serie.Nemotecnico;
            salida.NombreFondo = serie.NombreFondo;
            salida.NombreCorto = serie.NombreCortoTipoFondo;
            salida.NombreSerieFondo = serie.NombreSerieFondo;
            salida.IdEmisorDestino = serie.IdEmisor;
            salida.CodigoSerie = serie.CodigoSerie;
        }
        if (cliente != null) {
            salida.DocumentoIdentidad = cliente.DocumentoIdentidad;
            salida.CodigoPortafolio = cliente.CodigoPortafolio;
            salida.NombreCliente = cliente.NombreCompleto;
        }
        salida.IdSerieFondoOrigen = solicitud.IdSerieFondoOrigen;
        salida.IdEmisorOrigen = solicitud.IdEmisorOrigen;
        salida.MontoTransferencia = sol.Monto;
        salida.NumeroCuotaTransferencia = sol.NumeroCuotas;
        salida.FechaAceptacion = FechaLegacy.texto(sol.FechaAceptacion);
        salida.FechaOriginalInversion = FechaLegacy.texto(sol.FechaOriginalInversion);
        Timestamp ingreso = sol.FechaIngresoRegistro;
        salida.FechaIngresoRegistro = ingreso.toLocalDateTime().format(FechaLegacy.TS);
        salida.FechaLiquidacion = FechaLegacy.texto(sol.FechaLiquidacion);
        salida.FechaCertificacion = FechaLegacy.texto(sol.FechaCertificacion);
        salida.FechaValorCuota = FechaLegacy.texto(sol.FechaValorCuota);
        return salida;
    }
}
