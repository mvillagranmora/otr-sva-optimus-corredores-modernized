package cl.bcs.fondomutuo.comisionnn;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.Entrada;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.InSolicitudesRescate;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.Moneda;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.requisitos.Constantes3B;
import cl.bcs.fondomutuo.requisitos.FechasLegacy;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InPortafolio;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes;
import cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato;
import cl.bcs.fondomutuo.requisitos.contrato.FechaContrato.ResultadoFecha;

/** Builds, for ONE rescue request of {@code CrearSolicitudRescateComisionNN}, the row to insert, the data for the requirements check and the output element. */
final class ComisionNnArmado {

    /** What the request read before the loop (legacy local maps, built with {@code Utils.List2Hash}). */
    static final class Datos {
        String fechaSistema;
        HashMap<String, SerieFondoSolicitudes> hseries;
        HashMap<Long, ClientePortafolioSolicitudes> hclientes;
        HashMap<String, TipoMovimientoFondoMutuo> htipoMovimiento;
        HashMap<String, RegimenSerieDeFondo> hseriesRegimen;
        HashMap<Long, ResultadoFecha> hfechas;
        HashMap<Long, CuotaContrato.Resultado> valoresCuota;
        HashMap<String, Moneda> hmonedas;
        List<Long> secuencias;
        cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato.Salida salidaCuotas;
        Timestamp fechaHoraUTC;
    }

    static final class Armado {
        SolicitudAporteRescate sol = new SolicitudAporteRescate();
        InSolicitudes solVal = new InSolicitudes();
        InPortafolio portVal = new InPortafolio();
        ResultadoSolicitud salida = new ResultadoSolicitud();
    }

    private ComisionNnArmado() {
    }

    static Armado armar(Entrada entrada, InSolicitudesRescate solicitud, int i, Datos d) throws Exception {
        ClientePortafolioSolicitudes cliente = d.hclientes.get(solicitud.IdPortafolio);
        TipoMovimientoFondoMutuo tipoMovimiento = d.htipoMovimiento.get(Constantes3B.RESCATE_COMISION_NN);
        SerieFondoSolicitudes serie = d.hseries.get(solicitud.IdSerieFondo);
        CuotaContrato.Resultado vc = d.valoresCuota.get(solicitud.IdInterno);
        RegimenSerieDeFondo r = d.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
        ResultadoFecha fecha = d.hfechas.get(solicitud.IdInterno);

        Armado a = new Armado();
        SolicitudAporteRescate sol = a.sol;
        completarSolicitud(entrada, solicitud, i, d, cliente, serie, vc, r, fecha, sol);
        completarValidacion(a, sol, tipoMovimiento, serie, r, cliente);
        completarSalida(a, sol, serie, cliente);
        return a;
    }

    private static void completarSolicitud(Entrada entrada, InSolicitudesRescate solicitud, int i, Datos d, ClientePortafolioSolicitudes cliente,
            SerieFondoSolicitudes serie, CuotaContrato.Resultado vc, RegimenSerieDeFondo r, ResultadoFecha fecha, SolicitudAporteRescate sol) {
        sol.IdInterno = solicitud.IdInterno;
        sol.IdSolicitudAporteRescate = d.secuencias.get(i);
        sol.IdCliente = solicitud.IdCliente;
        sol.IdPortafolio = solicitud.IdPortafolio;
        sol.TipoSolicitud = Constantes3B.TIPO_SOLICITUD_RESCATE;
        sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
        sol.IdTipoMovimientoFondoMutuo = Constantes3B.RESCATE_COMISION_NN;
        sol.IdDividendo = 0L;
        sol.IdSerieFondoDestino = "";
        sol.IdEmisorDestino = "";
        sol.IdClienteDestino = 0L;
        sol.IdPortafolioDestino = 0L;
        sol.IndicadorMLT = Constantes3B.NO;
        sol.Overnight = Constantes3B.NO;
        sol.IdSerieFondoOrigen = "";
        sol.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado;
        sol.TipoCarteraNormativa = "";
        sol.IdTipoCartera = "";
        sol.CalculoPorMontoCuota = Constantes3B.CALCULO_POR_CUOTA;
        sol.FechaEmision = FechasLegacy.toDate(d.fechaSistema);
        sol.FechaOriginalInversion = FechasLegacy.toDate("0001-01-01");
        sol.FechaIngresoRegistro = d.fechaHoraUTC;
        sol.IdMoneda = solicitud.IdMoneda;
        sol.Estado = Constantes3B.ESTADO_INGRESADO;
        sol.IdMedioDePago = Constantes3B.MEDIO_PAGO_TRANSFERENCIA;
        sol.Glosa = "";
        sol.IdEmpresa = entrada.p_IdEmpresa;
        sol.IdUsuario = entrada.p_IdUsuario;
        sol.IdModulo = entrada.p_IdModulo;
        sol.IdEmisorOrigen = serie.IdEmisor;   // legacy: no null check (a missing series raises NullPointerException)
        sol.MontoOrigen = BigDecimal.ZERO;
        sol.SolicitudBatch = Constantes3B.SI;
        sol.OrigenOperacion = Constantes3B.ORIGEN_MANUAL;
        sol.IdAutorizadoOrigen = 0L;
        sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
        sol.IdRegimenTributario = solicitud.IdRegimenTributario;
        sol.IngresoAutomatico = solicitud.IngresoAutomatico;
        sol.EsPorMontoLiquido = Constantes3B.NO;
        sol.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso != null ? entrada.p_IdTipoCanalIngreso : "";
        sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : 0L;
        sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
        sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
        sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
        sol.Visado = Constantes3B.SI;
        sol.IdPreferenciaPagoPortafolio = 0L;
        sol.EstadoProceso = Constantes3B.ESTADO_PENDIENTE;
        sol.Monto = BigDecimal.ZERO;
        sol.NumeroCuotas = BigDecimal.ZERO;
        sol.ValorCuota = BigDecimal.ZERO;
        sol.CostoUF = BigDecimal.ZERO;
        sol.CostoMonedaLocal = BigDecimal.ZERO;
        sol.ValorUFFechaInversion = BigDecimal.ZERO;
        sol.ParidadFechaInversion = BigDecimal.ZERO;
        sol.IdMonedaSistema = "";
        sol.MontoMonedaSistema = BigDecimal.ZERO;
        sol.MontoPagoLocal = BigDecimal.ZERO;
        sol.MontoPago = BigDecimal.ZERO;
        sol.MontoComision = BigDecimal.ZERO;
        sol.MontoComisionLocal = BigDecimal.ZERO;
        sol.IdCartera = solicitud.IdCartera;
        sol.FolioCartera = solicitud.FolioCartera;
        sol.IdMonedaPago = solicitud.IdMonedaPago;
        if (vc != null) {
            sol.Monto = vc.Monto;
            sol.NumeroCuotas = vc.NumeroCuotas;
            sol.ValorCuota = vc.ValorCuota;
            sol.CostoUF = vc.CostoEnFactorAjuste;
            sol.CostoMonedaLocal = vc.MontoDestino;
            sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
            sol.ParidadFechaInversion = vc.ValorParidad;
            sol.IdMonedaSistema = vc.IdMonedaSistema;
            sol.MontoMonedaSistema = vc.MontoMonedaSistema;
            sol.MontoComision = BigDecimal.ZERO;
            sol.MontoComisionLocal = BigDecimal.ZERO;
            sol.MontoPagoLocal = vc.MontoDestino;
            sol.MontoPago = vc.Monto;
            sol.MontoInversion = vc.Monto;
            sol.CostoMonedaLocalInversion = vc.MontoDestino;
            sol.ValorCuotaInversion = vc.ValorCuota;
            sol.ParidadFechaCertificacion = vc.ValorParidad;
            sol.NumeroCuotasInversion = vc.NumeroCuotas;
        }
        if (fecha != null) {
            sol.FechaAceptacion = fecha.FechaAceptacion;
            sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : fecha.FechaValorCuota;
            sol.FechaCertificacion = fecha.FechaCertificacion;
            sol.FechaLiquidacion = fecha.FechaLiquidacion;
        }
        if (r != null) {
            switch (r.GrupoRegimen) {
            case "APV":
                sol.Tributacion = Constantes3B.TRIBUTACION_APV;
                break;
            case "ART108":
                sol.Tributacion = Constantes3B.TRIBUTACION_ART108;
                break;
            case "ART107":
                sol.Tributacion = Constantes3B.TRIBUTACION_ART107;
                break;
            case "57BIS":
                sol.Tributacion = Constantes3B.TRIBUTACION_57BIS;
                break;
            default:
                sol.Tributacion = Constantes3B.TRIBUTACION_DEFECTO;
                break;
            }
        }
    }

    private static void completarValidacion(Armado a, SolicitudAporteRescate sol, TipoMovimientoFondoMutuo tipoMovimiento, SerieFondoSolicitudes serie,
            RegimenSerieDeFondo r, ClientePortafolioSolicitudes cliente) {
        InSolicitudes solVal = a.solVal;
        InPortafolio portVal = a.portVal;
        solVal.IdInterno = sol.IdInterno;
        solVal.IdCliente = sol.IdCliente;
        solVal.IdPortafolio = sol.IdPortafolio;
        solVal.IdSerieFondo = sol.IdSerieFondo;
        solVal.FolioCartera = sol.FolioCartera;
        solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
        solVal.Monto = sol.Monto;
        solVal.NumeroCuotas = sol.NumeroCuotas;
        solVal.IdEstadoSerie = "";
        solVal.EsDiferido = Constantes3B.SI;
        solVal.EsMontoMaximo = Constantes3B.SI;
        solVal.EsMontoMinimo = Constantes3B.SI;
        solVal.PermiteCambioGrupo = Constantes3B.SI;
        solVal.EsPortafolioValido = Constantes3B.SI;
        solVal.PermiteRescateMandato = Constantes3B.SI;
        solVal.EsCuentaInversionValido = Constantes3B.SI;
        solVal.EsFechaValidaAporte = Constantes3B.SI;
        solVal.EsFechaValidaRescate = Constantes3B.SI;
        solVal.ValorCuota = sol.ValorCuota;
        solVal.Overnight = Constantes3B.SI;
        solVal.EsMoneyMarket = Constantes3B.SI;
        solVal.EsFamiliaEquivalente = Constantes3B.SI;
        solVal.EsPortafolioRegimenValido = Constantes3B.SI;
        solVal.EsRegimenValido = Constantes3B.SI;
        solVal.EsCuentaInversionValido = Constantes3B.SI;
        solVal.PermiteAporte = Constantes3B.SI;
        solVal.EsRegimenDestinoValido = Constantes3B.SI;
        if (serie != null) {
            solVal.IdEstadoSerie = serie.IdEstado;
            solVal.EstadoFondo = Constantes3B.ABIERTO;
            solVal.EstadoBloqueo = serie.BloqueoAporte;
            solVal.EstadoLiquidez = Constantes3B.ABIERTO;
            solVal.EsFondoValido = Constantes3B.SI;
            if (!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && Constantes3B.NO.equalsIgnoreCase(serie.Diferido)) {
                solVal.EsDiferido = Constantes3B.NO;
            }
        }
        if (r == null) {
            solVal.EsRegimenValido = Constantes3B.NO;
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
        solVal.IdTipoOperacion = Constantes3B.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? Constantes3B.TIPO_OPERACION_APORTE_FONDO_MUTUO
                : Constantes3B.TIPO_OPERACION_RESCATE_FONDO_MUTUO;
        solVal.IdTipoProducto = Constantes3B.TIPO_OPERACION;
    }

    private static void completarSalida(Armado a, SolicitudAporteRescate sol, SerieFondoSolicitudes serie, ClientePortafolioSolicitudes cliente) {
        ResultadoSolicitud salida = a.salida;
        Pojos.copiar(sol, salida);
        if (serie != null) {
            salida.IdFondo = serie.IdFondo;
            salida.Nemotecnico = serie.Nemotecnico;
            salida.NombreFondo = serie.NombreFondo;
            salida.NombreCorto = serie.NombreCortoTipoFondo;
            salida.NombreSerieFondo = serie.NombreSerieFondo;
            salida.CodigoSerie = serie.CodigoSerie;
            salida.IdEmisorDestino = serie.IdEmisor;
        }
        salida.IdSerieFondoOrigen = "";
        salida.IdEmisorOrigen = "";
        if (cliente != null) {
            salida.DocumentoIdentidad = cliente.DocumentoIdentidad;
            salida.CodigoPortafolio = cliente.CodigoPortafolio;
            salida.NombreCliente = cliente.NombreCompleto;
        }
        salida.MontoTransferencia = sol.Monto;
        salida.NumeroCuotaTransferencia = sol.NumeroCuotas;
        salida.FechaAceptacion = texto(sol.FechaAceptacion);
        salida.FechaOriginalInversion = texto(sol.FechaOriginalInversion);
        salida.FechaIngresoRegistro = sol.FechaIngresoRegistro.toLocalDateTime().format(FechasLegacy.TS);
        salida.FechaLiquidacion = texto(sol.FechaLiquidacion);
        salida.FechaCertificacion = texto(sol.FechaCertificacion);
        salida.FechaValorCuota = texto(sol.FechaValorCuota);
    }

    /** Legacy {@code Fecha.stringToSqlDate(java.util.Date)}: yyyy-MM-dd in the JVM zone; a null date raises NullPointerException. */
    static String texto(java.util.Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
