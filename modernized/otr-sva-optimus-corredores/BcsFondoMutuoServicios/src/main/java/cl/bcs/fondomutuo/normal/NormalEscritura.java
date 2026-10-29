package cl.bcs.fondomutuo.normal;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.agf.CrearSolicitudAgfService;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.NormalDatos.TraspasoAPV;
import cl.bcs.fondomutuo.port.out.http.BatchPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.requisitos.contrato.LogContrato;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;

/** The writes of Normal (legacy :1414-1612): Tesoreria, Spot, INSERT, AGF, batch program and log. Gates G1/G2/G4 are evaluated before the first one. */
final class NormalEscritura {

    private final Compuertas compuertas;
    private final ProcesosExternos.TesoreriaPort tesoreria;
    private final ProcesosExternos.SpotPort spot;
    private final NormalDatos datos;
    private final CrearSolicitudAgfService agf;
    private final BatchPort batch;
    private final LogPort log;

    NormalEscritura(Compuertas compuertas, ProcesosExternos.TesoreriaPort tesoreria, ProcesosExternos.SpotPort spot, NormalDatos datos,
            CrearSolicitudAgfService agf, BatchPort batch, LogPort log) {
        this.compuertas = compuertas;
        this.tesoreria = tesoreria;
        this.spot = spot;
        this.datos = datos;
        this.agf = agf;
        this.batch = batch;
        this.log = log;
    }

    /** Gates of the writes (plan 6.3): nothing has been written yet, the caller releases the lock before the 503 leaves. */
    void compuertas(NormalContexto c, String notificaOwp) {
        if (c.entradaTesoreria.p_Solicitudes.size() > 0) compuertas.g1MedioDistintoDeCin();
        if (c.entradaSpot.p_DatosOperacion.size() > 0) compuertas.g2Spot();
        boolean pendientes = c.solicitudesProcesar.stream().anyMatch(s -> NormalConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(s.EstadoProceso));
        if (pendientes && "S".equalsIgnoreCase(notificaOwp) && "OWP".equalsIgnoreCase(c.entrada.p_IdTipoCanalIngreso)) compuertas.g4Owp();
    }

    void simulada(NormalContexto c) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (NormalConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(sol.IdInterno));
            }
        }
    }

    void real(NormalContexto c) throws Exception {
        NormalContrato.Entrada entrada = c.entrada;
        if (c.entradaTesoreria.p_Solicitudes.size() > 0) {
            c.entradaTesoreria.p_IdEmpresa = entrada.p_IdEmpresa;
            c.entradaTesoreria.p_IdUsuario = entrada.p_IdUsuario;
            c.entradaTesoreria.p_IdModulo = entrada.p_IdModulo;
            c.entradaTesoreria.p_SimularOperacion = entrada.p_SimularOperacion;
            c.entradaTesoreria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
            c.entradaTesoreria.p_EsIngresoAutomatico = NormalConstantes.NO;
            ProcesosExternos.SalidaTesoreria st = tesoreria.procesar(c.entradaTesoreria);
            for (MensajeSalida rs : st.P_OUT_MENSAJES) {
                if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(rs.tipo)) {
                    c.out.P_OUT_ERRORESCONTADOR += 1L;
                    c.out.P_OUT_MENSAJES.add(new MensajeSalida(0L, rs.tipo, rs.codigo, NormalConstantes.MODULO_TESORERIA + " : " + rs.descripcion));
                    rechazarPrimera(c);   // D-NOR-5: the FIRST request of the list, not the one that failed
                }
            }
            for (ProcesosExternos.ResultadoProceso rs : st.P_OUT_RESULTADOPROCESO) {
                if (NormalConstantes.ESTADO_RECHAZADO_TESORERIA.equalsIgnoreCase(rs.Resultado)) {
                    for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
                        if (rs.Id.longValue() == sol.IdInterno.longValue() && rs.Campo.equalsIgnoreCase(NormalConstantes.NUMERO_REFERENCIA_TESORERIA)) {
                            sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
                            break;
                        }
                    }
                }
            }
            for (ProcesosExternos.IngresoEgresoGenerado rs : st.P_OUT_INGRESOEGRESOGENERADO) {
                for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
                    if (rs.NumeroReferencia.longValue() == sol.IdInterno.longValue()) {
                        sol.IdIngresoEgreso = rs.IdIngresoEgreso;
                        break;
                    }
                }
            }
        }
        if (c.entradaSpot.p_DatosOperacion.size() > 0) {
            c.entradaSpot.p_IdEmpresa = entrada.p_IdEmpresa;
            c.entradaSpot.p_IdUsuario = entrada.p_IdUsuario;
            c.entradaSpot.p_IdModulo = entrada.p_IdModulo;
            c.entradaSpot.p_CalculaMargen = NormalConstantes.SPOT_CALCULA_MARGEN;
            c.entradaSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
            c.entradaSpot.p_PermiteOperarProspectos = entrada.p_PermiteOperarProspectos;
            ProcesosExternos.SalidaSpot ss = spot.procesar(c.entradaSpot);
            for (ProcesosExternos.OperacionSpot rs : ss.P_OUT_IDOPERACIONSPOT) {
                for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
                    if (Long.parseLong(rs.IdInterno) == sol.IdInterno) {
                        if (NormalConstantes.SPOT_ESTADO_RECHAZADO.equalsIgnoreCase(rs.Estado)) {
                            sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
                        } else {
                            sol.IdOperacionSpot = rs.IdOperacionSpot;
                        }
                        break;
                    }
                }
            }
            for (MensajeSalida rs : ss.P_OUT_MENSAJES) {
                if (NormalConstantes.SPOT_ERROR.equalsIgnoreCase(rs.tipo)) {
                    c.out.P_OUT_ERRORESCONTADOR += 1L;
                    c.out.P_OUT_MENSAJES.add(new MensajeSalida(rs.id, rs.tipo, rs.codigo, NormalConstantes.MODULO_SPOT + " : " + rs.descripcion));
                    rechazarPrimera(c);   // D-NOR-5
                }
            }
        }
        insertar(c);
    }

    private static void rechazarPrimera(NormalContexto c) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
            break;
        }
    }

    private void insertar(NormalContexto c) throws Exception {
        NormalContrato.Entrada entrada = c.entrada;
        CrearSolicitudAgfService.Entrada entradaAgf = new CrearSolicitudAgfService.Entrada();
        List<NormalContratos.Parametro> bonificacion = new ArrayList<>();
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (NormalConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                if (NormalConstantes.NO.equalsIgnoreCase(sol.SolicitudBatch)) {
                    CrearSolicitudAgfService.Solicitudes s = new CrearSolicitudAgfService.Solicitudes();
                    s.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
                    entradaAgf.p_Solicitudes.add(s);
                }
                c.solicitudesInsertar.add(sol);
                c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(sol.IdInterno));
                TraspasoAPV traspaso = c.hTraspasoAPV.get(sol.FolioContrato);
                if (NormalConstantes.TIPO_SOLICITUD_RESCATE.equals(sol.TipoSolicitud) && c.hportafolioAPV.get(sol.IdPortafolio) != null && traspaso == null) {
                    bonificacion.add(new NormalContratos.Parametro(0L, "IdSolicitudAporteRescate", sol.IdSolicitudAporteRescate.toString(), null));
                }
            }
        }
        if (c.solicitudesInsertar.size() > 0) {
            datos.insertarSolicitudes(c.solicitudesInsertar);
            if (entradaAgf.p_Solicitudes.size() > 0) {
                entradaAgf.p_IdEmpresa = entrada.p_IdEmpresa;
                entradaAgf.p_IdUsuario = entrada.p_IdUsuario;
                entradaAgf.p_IdModulo = entrada.p_IdModulo;
                entradaAgf.p_EsManual = NormalConstantes.NO;
                CrearSolicitudAgfService.Salida sa = agf.logica(entradaAgf);
                c.out.P_OUT_ERRORESCONTADOR += sa.P_OUT_ERRORESCONTADOR;
                c.out.P_OUT_MENSAJES.addAll(sa.P_OUT_MENSAJES);
            }
            if (bonificacion.size() > 0 && !entrada.p_IdOrigenProceso.equalsIgnoreCase("PAC")) {
                inscribirBatch(c, bonificacion);
            }
            agregarLog(c);
        }
    }

    private void inscribirBatch(NormalContexto c, List<NormalContratos.Parametro> bonificacion) {
        NormalContratos.BatchEntrada e = new NormalContratos.BatchEntrada();
        e.p_Parametro = new ArrayList<>();
        NormalContratos.TipoPrograma tp = new NormalContratos.TipoPrograma();
        e.p_TipoPrograma = new ArrayList<>();
        tp.IdInterno = 0L;
        tp.IdTipoPrograma = "INGRESAR_ORDEN_FFMM_BONIFICACION_FISCAL";
        tp.SegundosDesfase = 0L;
        e.p_TipoPrograma.add(tp);
        e.p_Parametro.add(new NormalContratos.Parametro(0L, "IdEmpresa", c.entrada.p_IdEmpresa.toString(), null));
        e.p_Parametro.add(new NormalContratos.Parametro(0L, "IdUsuario", c.entrada.p_IdUsuario.toString(), null));
        for (NormalContratos.Parametro p : bonificacion) {
            e.p_Parametro.add(new NormalContratos.Parametro(0L, p.NombreParametro, p.Valor, null));
        }
        e.p_IdEmpresa = c.entrada.p_IdEmpresa;
        e.p_IdUsuario = c.entrada.p_IdUsuario;
        e.p_IdModulo = c.entrada.p_IdModulo;
        NormalContratos.inscribir(batch, e);
    }

    private void agregarLog(NormalContexto c) {
        LogContrato.InAgregarLog entradaLog = new LogContrato.InAgregarLog();
        for (SolicitudAporteRescate sol : c.solicitudesInsertar) {
            ClientePortafolioSolicitudes cliente = c.hclientes.get(sol.IdPortafolio);
            LogContrato.Log l = new LogContrato.Log();
            l.IdCabeceraLog = sol.IdSolicitudAporteRescate;
            l.IdTipoLog = "INGRESO";
            l.IdModulo = NormalConstantes.MODULO_FONDO_MUTUO;
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
            l.IdTipoRegistro = "SOLICITUD_APORTE_RESCATE";
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
}
