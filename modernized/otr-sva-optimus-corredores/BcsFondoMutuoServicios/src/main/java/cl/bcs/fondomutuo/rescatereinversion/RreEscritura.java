package cl.bcs.fondomutuo.rescatereinversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.agf.CrearSolicitudAgfService;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.http.BatchPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.contrato.LogContrato;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;

/**
 * The real pass (FM-REI-31..36, RR:1233-1409): destination rows, INSERT, AGF (in process), AgregarLog and InscribirProcesoBatch (HTTP, G5: a 503 of the
 * router is {@code ServicioNoServidoException}, rollback + 503, DEV-F5). The release of the lock is the caller's (it is the legacy's last step).
 */
final class RreEscritura {

    private final RreDatos datos;
    private final CrearSolicitudAgfService agf;
    private final LogPort log;
    private final BatchPort batch;
    private final DiccionarioMensajesPort diccionario;

    RreEscritura(RreDatos datos, CrearSolicitudAgfService agf, LogPort log, BatchPort batch, DiccionarioMensajesPort diccionario) {
        this.datos = datos;
        this.agf = agf;
        this.log = log;
        this.batch = batch;
        this.diccionario = diccionario;
    }

    /** FM-REI-30: simulation, the pending requests of {@code Final} (one per group). */
    static void simulada(RreContexto c) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesarFinal) {
            if (RreConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(sol.IdInterno));
        }
    }

    /** @return the requests inserted (empty: nothing was written; the caller releases the lock in both cases). */
    List<SolicitudAporteRescate> real(RreContexto c) throws Exception {
        CrearSolicitudAgfService.Entrada entradaAgf = new CrearSolicitudAgfService.Entrada();
        List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<>();
        for (SolicitudAporteRescate sol : c.solicitudesProcesarFinal) {
            if (RreConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                if (RreConstantes.NO.equalsIgnoreCase(sol.SolicitudBatch)) {
                    CrearSolicitudAgfService.Solicitudes s = new CrearSolicitudAgfService.Solicitudes();
                    s.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
                    entradaAgf.p_Solicitudes.add(s);
                }
                solicitudesInsertar.add(sol);
                c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(sol.IdInterno));
            }
        }
        List<RreDatos.ReinversionSerieFondoDestino> filas = destinos(c, solicitudesInsertar);
        if (filas.size() > 0) {
            datos.insertarReinversion(filas);   // before the requests: the sequence order follows the HashMap iteration (FM-REI-31)
        }
        if (solicitudesInsertar.size() > 0) {
            datos.insertarSolicitudes(solicitudesInsertar);
            if (entradaAgf.p_Solicitudes.size() > 0) {
                entradaAgf.p_IdEmpresa = c.entrada.p_IdEmpresa;
                entradaAgf.p_IdUsuario = c.entrada.p_IdUsuario;
                entradaAgf.p_IdModulo = c.entrada.p_IdModulo;
                entradaAgf.p_EsManual = RreConstantes.NO;
                CrearSolicitudAgfService.Salida sa = agf.logica(entradaAgf);
                c.out.P_OUT_ERRORESCONTADOR += sa.P_OUT_ERRORESCONTADOR;
                c.out.P_OUT_MENSAJES.addAll(sa.P_OUT_MENSAJES);
            }
            agregarLog(c, solicitudesInsertar);
            if (solicitudesInsertar.get(0).IdAgrupador != null && !RreReglas.esCanalIngresoWebServices(c.entrada.p_IdTipoCanalIngreso)) {
                RreBatch.inscribir(c, datos, batch, entradaAgf);
            }
        }
        return solicitudesInsertar;
    }

    /** FM-REI-31: one row per contribution whose input IdAgrupador equals the group of the request, in HashMap order; it also sets {@code sol.Switch}. */
    private static List<RreDatos.ReinversionSerieFondoDestino> destinos(RreContexto c, List<SolicitudAporteRescate> solicitudesInsertar) {
        List<RreDatos.ReinversionSerieFondoDestino> filas = new ArrayList<>();
        for (SolicitudAporteRescate sol : solicitudesInsertar) {
            for (Map.Entry<Long, InSolicitudesAporte> apo : c.haportes.entrySet()) {
                InSolicitudesAporte aporte = apo.getValue();
                if (aporte.IdAgrupador.equals(sol.IdAgrupadorInterno)) {
                    RreDatos.ReinversionSerieFondoDestino r = new RreDatos.ReinversionSerieFondoDestino();
                    SerieFondoSolicitudes serieAporte = c.hseries.get(aporte.IdSerieFondo);
                    SerieFondoSolicitudes serieRescate = c.hseries.get(sol.IdSerieFondo);
                    ResultadoFecha fecha = c.hfechasAporte.get(aporte.IdInterno);
                    r.IdSerieFondoDestino = aporte.IdSerieFondo;
                    r.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
                    r.Porcentaje = aporte.Porcentaje;
                    r.PorcentajeReal = aporte.PorcentajeReal;
                    r.IdRegimenTributario = aporte.IdRegimenTributario;
                    if (serieAporte != null && serieRescate != null && RreConstantes.SI.equals(serieRescate.FondoExtranjero)
                            && RreConstantes.SI.equals(serieAporte.FondoExtranjero) && serieRescate.IdEmisor.equals(serieAporte.IdEmisor)) {
                        r.Switch = RreConstantes.SI;
                        sol.Switch = RreConstantes.SI;
                    } else {
                        r.Switch = RreConstantes.NO;
                        sol.Switch = RreConstantes.NO;
                    }
                    // no date of the contribution: NullPointerException, as the legacy (fecha.FechaCertificacion)
                    r.FechaFinalReinversion = fecha.FechaCertificacion != null ? java.sql.Date.valueOf(fecha.FechaCertificacion) : null;
                    filas.add(r);
                }
            }
        }
        return filas;
    }

    /** FM-REI-33: one AgregarLog call with a line per inserted request (answer not read; null client: NullPointerException). G5 by {@link LogPort}. */
    private void agregarLog(RreContexto c, List<SolicitudAporteRescate> insertadas) {
        LogContrato.InAgregarLog entradaLog = new LogContrato.InAgregarLog();
        for (SolicitudAporteRescate sol : insertadas) {
            ClientePortafolioSolicitudes cliente = c.hclientes.get(sol.IdPortafolio);
            LogContrato.Log l = new LogContrato.Log();
            l.IdCabeceraLog = sol.IdSolicitudAporteRescate;
            l.IdTipoLog = "INGRESO";
            l.IdModulo = RreConstantes.MODULO_FONDO_MUTUO;
            l.IdUsuario = c.entrada.p_IdUsuario;
            l.IdEmpresa = c.entrada.p_IdEmpresa;
            l.FechaSistemaLog = c.fechaSistema;
            l.IdUsuarioAtendido = c.entrada.p_IdUsuario;
            l.IdCliente = sol.IdCliente;
            l.EstadoCliente = cliente.IdEstadoCliente;
            l.NombreCliente = cliente.NombreCompleto;
            l.IdTipoDocumentoIdentidad = cliente.TipoDocumentoIdentidad;
            l.IdDocumentoIdentidad = cliente.DocumentoIdentidad;
            l.IdPortafolio = sol.IdPortafolio;
            l.EstadoPortafolio = cliente.IdEstadoPortafolio;
            l.IdTipoRegistro = "SOLICITUD_RESCATE_REINVERSION";
            l.EstadoRegistro = sol.Estado;
            l.IdTipoOperacion = sol.IdTipoMovimientoFondoMutuo;
            l.Folio = sol.IdSolicitudAporteRescate;
            l.Cantidad = sol.NumeroCuotas;
            l.PrecioTasa = sol.ValorCuota;
            l.Monto = sol.Monto;
            l.IdMonedaOrigen = sol.IdMoneda;
            l.IdMonedaLiquidacion = sol.IdMoneda;
            l.TipoCambio = sol.ParidadFechaInversion.doubleValue() + "";
            l.Nemo = sol.IdSerieFondo;
            l.IdProceso = 0L;
            l.EstadoProceso = "";
            l.NombreProceso = "";
            entradaLog.p_Log.add(l);
        }
        log.agregar(JsonLegacy.conNulos().toJsonTree(entradaLog).getAsJsonObject());
    }

    /** FM-REI-36 (RR:1399-1406): after the release of the lock, a 1:N message per inserted request (only WEB omits it, D-F2-9) when the counter is 0. */
    void mensajes1N(RreContexto c, List<SolicitudAporteRescate> insertadas) {
        for (SolicitudAporteRescate sol : insertadas) {
            if (sol.IdAgrupador != null && !RreConstantes.CANAL_WEB.equals(c.entrada.p_IdTipoCanalIngreso) && !(c.out.P_OUT_ERRORESCONTADOR > 0)) {
                HashMap<String, String> m = new HashMap<>();
                m.put("secuencia", sol.IdSolicitudAporteRescate.toString());
                m.put("folioAgrupador", sol.IdAgrupador.toString());
                c.out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_OK, "UI_REINVERSION_1N_CREADA", m, diccionario, c.entrada.p_IdEmpresa));
            }
        }
    }

    static List<String> idsPorFolio(List<SolicitudAporteRescate> solicitudesProcesar) {   // D-F2-9: HashMap / HashSet order, as the legacy collectors
        return solicitudesProcesar.stream().collect(Collectors.groupingBy(s -> s.FolioContrato,
                Collectors.mapping(s -> s.IdSolicitudAporteRescate.toString(), Collectors.toSet()))).values().stream().map(s -> String.join(";", s))
                .collect(Collectors.toList());
    }
}
