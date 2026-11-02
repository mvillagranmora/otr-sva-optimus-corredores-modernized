package cl.bcs.fondomutuo.aportereinversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.agf.CrearSolicitudAgfService;
import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.contrato.LogContrato;
import cl.bcs.fondomutuo.rescatereinversion.RreDatos.ReinversionSerieFondoDestino;

/**
 * The writes of one lot of {@code CrearSolicitudAporteReinversion} (legacy :1238-1466): Tesoreria, Spot, INSERT, final reinversion date, AGF and log. The gates G1 and G2 are
 * evaluated BEFORE the first of them (Tesoreria writes through CrearIngresoEgreso); the legacy has no gate (approved difference: a 503 the router repeats, nothing written).
 */
final class AporteReinversionEscritura {

    private final Compuertas compuertas;
    private final ProcesosExternos.TesoreriaPort tesoreria;
    private final ProcesosExternos.SpotPort spot;
    private final AporteReinversionDatos datos;
    private final CrearSolicitudAgfService agf;
    private final LogPort log;

    AporteReinversionEscritura(Compuertas compuertas, ProcesosExternos.TesoreriaPort tesoreria, ProcesosExternos.SpotPort spot, AporteReinversionDatos datos,
            CrearSolicitudAgfService agf, LogPort log) {
        this.compuertas = compuertas;
        this.tesoreria = tesoreria;
        this.spot = spot;
        this.datos = datos;
        this.agf = agf;
        this.log = log;
    }

    /** Gates of the writes (plan 6.3): nothing has been written yet. G1: a request that goes to Tesoreria; G2: the Spot parameter is S and a pending request generates Spot. */
    void compuertas(AporteReinversionContexto c) {
        if (c.entradaTesoreria.p_Solicitudes.size() > 0) compuertas.g1MedioDistintoDeCin();
        // the legacy shape (`parametro.equals(S)`): a null parameter is a NullPointerException, as it is when the rows are built (the parameter is read before anything else)
        if (c.aporteGeneraCompraSpotDolar.equals(AporteReinversionConstantes.SI) && c.solicitudesProcesar.stream()
                .anyMatch(s -> AporteReinversionConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(s.EstadoProceso) && AporteReinversionConstantes.SI.equals(s.GeneraSpot))) {
            compuertas.g2Spot();
        }
    }

    /** Simulation (legacy :1460-1466): the pending requests are the answer, nothing is written. */
    void simulada(AporteReinversionContexto c) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (AporteReinversionConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(sol.IdInterno));
            }
        }
    }

    /** @return true when the lot raised errors in the Spot step and the call must stop BEFORE the INSERT (legacy :1399-1401). */
    boolean real(AporteReinversionContexto c) throws Exception {
        AporteReinversionContrato.Entrada entrada = c.entrada;
        if (c.entradaTesoreria.p_Solicitudes.size() > 0) {
            c.entradaTesoreria.p_IdEmpresa = entrada.p_IdEmpresa;
            c.entradaTesoreria.p_IdUsuario = entrada.p_IdUsuario;
            c.entradaTesoreria.p_IdModulo = entrada.p_IdModulo;
            c.entradaTesoreria.p_SimularOperacion = entrada.p_SimularOperacion;
            c.entradaTesoreria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
            c.entradaTesoreria.p_EsIngresoAutomatico = AporteReinversionConstantes.NO;
            ProcesosExternos.SalidaTesoreria st = tesoreria.procesar(c.entradaTesoreria);
            for (ProcesosExternos.ResultadoProceso rs : st.P_OUT_RESULTADOPROCESO) {
                if (AporteReinversionConstantes.ESTADO_RECHAZADO_TESORERIA.equalsIgnoreCase(rs.Resultado)) {
                    for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
                        if (rs.Id.longValue() == sol.IdInterno.longValue() && rs.Campo.equalsIgnoreCase(AporteReinversionConstantes.NUMERO_REFERENCIA_TESORERIA)) {
                            sol.EstadoProceso = AporteReinversionConstantes.ESTADO_RECHAZADO;
                            break;
                        }
                    }
                }
            }
        }
        CrearSolicitudAgfService.Entrada entradaAgf = new CrearSolicitudAgfService.Entrada();
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (AporteReinversionConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                if (AporteReinversionConstantes.NO.equalsIgnoreCase(sol.SolicitudBatch)) {
                    CrearSolicitudAgfService.Solicitudes s = new CrearSolicitudAgfService.Solicitudes();
                    s.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
                    entradaAgf.p_Solicitudes.add(s);
                }
                c.solicitudesInsertar.add(sol);
                c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(sol.IdInterno));
            }
        }
        if (AporteReinversionConstantes.SI.equals(c.aporteGeneraCompraSpotDolar) && spot(c)) {
            return true;
        }
        if (c.solicitudesInsertar.size() > 0) {
            datos.insertarSolicitudes(c.solicitudesInsertar);
            actualizarFechaFinalReinversion(c);
            if (entradaAgf.p_Solicitudes.size() > 0) {
                entradaAgf.p_IdEmpresa = entrada.p_IdEmpresa;
                entradaAgf.p_IdUsuario = entrada.p_IdUsuario;
                entradaAgf.p_IdModulo = entrada.p_IdModulo;
                entradaAgf.p_EsManual = AporteReinversionConstantes.NO;
                CrearSolicitudAgfService.Salida sa = agf.logica(entradaAgf);
                c.out.P_OUT_ERRORESCONTADOR += sa.P_OUT_ERRORESCONTADOR;
                c.out.P_OUT_MENSAJES.addAll(sa.P_OUT_MENSAJES);
            }
            agregarLog(c);
        }
        return false;
    }

    /** Legacy :1273-1405: the Spot operations of the requests to insert that generate one, with the sale and the purchase forms of payment. */
    private boolean spot(AporteReinversionContexto c) throws Exception {
        AporteReinversionContrato.Entrada entrada = c.entrada;
        List<SolicitudAporteRescate> generanSpot = c.solicitudesInsertar.stream().filter(p -> p.GeneraSpot != null).filter(p -> p.GeneraSpot.equals(AporteReinversionConstantes.SI))
                .collect(Collectors.toList());
        Map<Long, InSolicitudesAporte> solicitudesEntradaAgrupadasPorIdInterno = entrada.p_SolicitudesAporte.stream()
                .collect(Collectors.toMap(solicitud -> solicitud.IdInterno, solicitud -> solicitud));
        if (generanSpot.size() == 0) return false;
        ProcesosExternos.EntradaSpot entradaSpot = new ProcesosExternos.EntradaSpot();
        Long contador = 0L;
        for (SolicitudAporteRescate sol : generanSpot) {
            sol.Estado = AporteReinversionConstantes.ESTADO_PENDIENTE;
            InSolicitudesAporte datosEntradaSolicitud = solicitudesEntradaAgrupadasPorIdInterno.get(sol.IdInterno);
            String fechaCertificaSpotCompra = null;
            String fechaLiquidaSpotCompra = null;
            if (datosEntradaSolicitud != null) {
                fechaCertificaSpotCompra = datosEntradaSolicitud.FechaCertificaSpotCompra;
                fechaLiquidaSpotCompra = datosEntradaSolicitud.FechaLiquidaSpotCompra;
            }
            // a Spot purchase that comes from a rescue with reinversion takes the certification date of the rescue
            if (AporteReinversionConstantes.APORTE_REINVERSION.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && fechaCertificaSpotCompra != null) {
                SolicitudAporteRescate solSpot = copiar(sol);
                solSpot.FechaCertificacion = FechaLegacy.aFechaSql(fechaCertificaSpotCompra);
                entradaSpot.p_DatosOperacion.add(solSpot);
            } else {
                entradaSpot.p_DatosOperacion.add(sol);
            }
            InSolicitudesAporte inSolicitud = c.listaEntrada.get(sol.IdInterno);
            entradaSpot.p_DatosOrigenInversion.add(inSolicitud);
            SerieFondoSolicitudes serie = c.hseries.get(sol.IdSerieFondo);
            ProcesosExternos.FormaPagoSpot venta = new ProcesosExternos.FormaPagoSpot();
            ProcesosExternos.FormaPagoSpot compra = new ProcesosExternos.FormaPagoSpot();
            boolean reinversionConFechaDeLiquidacion = AporteReinversionConstantes.APORTE_REINVERSION.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && fechaLiquidaSpotCompra != null;
            if (inSolicitud.IdMoneda.equals(AporteReinversionConstantes.PESOS) && serie.IdMoneda.equals(AporteReinversionConstantes.DOLAR)) {
                venta.AfectarTesoreria = "N";
                venta.IdComprobantePagoOperacion = 0L;
                venta.IdFormaDePago = "COMSPT_CIN_CLP_0";
                venta.IdInternoPagoOperacion = contador++;
                venta.IdMoneda = AporteReinversionConstantes.PESOS;
                venta.IdMonedaLiquidacion = AporteReinversionConstantes.PESOS;
                venta.IdOperacion = sol.IdInterno;
                venta.IdTipoMovimiento = "MOVING";
                venta.MontoFormaDePago = c.valoresCuota.get(sol.IdInterno).MontoDestino;
                venta.RecibePagaPortafolio = "R";
                venta.FechaLiquidacion = sol.FechaAceptacion;
                entradaSpot.p_DatosFormaPago.add(venta);

                compra.AfectarTesoreria = "N";
                compra.IdComprobantePagoOperacion = 0L;
                compra.IdFormaDePago = "VENSPT_CIN_USD_0";
                compra.IdInternoPagoOperacion = contador++;
                compra.IdMoneda = AporteReinversionConstantes.DOLAR;
                compra.IdMonedaLiquidacion = AporteReinversionConstantes.DOLAR;
                compra.IdOperacion = sol.IdInterno;
                compra.IdTipoMovimiento = "MOVEGR";
                compra.MontoFormaDePago = sol.Monto;
                compra.RecibePagaPortafolio = "P";
                compra.FechaLiquidacion = sol.FechaAceptacion;
                entradaSpot.p_DatosFormaPago.add(compra);
            } else if (inSolicitud.IdMoneda.equals(AporteReinversionConstantes.DOLAR) && serie.IdMoneda.equals(AporteReinversionConstantes.PESOS)) {
                venta.AfectarTesoreria = "N";
                venta.IdComprobantePagoOperacion = 0L;
                venta.IdFormaDePago = "COMSPT_CIN_USD_0";
                venta.IdInternoPagoOperacion = contador++;
                venta.IdMoneda = AporteReinversionConstantes.DOLAR;
                venta.IdMonedaLiquidacion = AporteReinversionConstantes.DOLAR;
                venta.IdOperacion = sol.IdInterno;
                venta.IdTipoMovimiento = "MOVEGR";
                venta.MontoFormaDePago = c.valoresCuota.get(sol.IdInterno).MontoDestino;
                venta.RecibePagaPortafolio = "R";
                venta.FechaLiquidacion = reinversionConFechaDeLiquidacion ? FechaLegacy.aFechaSql(fechaLiquidaSpotCompra) : sol.FechaAceptacion;
                entradaSpot.p_DatosFormaPago.add(venta);

                compra.AfectarTesoreria = "N";
                compra.IdComprobantePagoOperacion = 0L;
                compra.IdFormaDePago = "VENSPT_CIN_CLP_0";
                compra.IdInternoPagoOperacion = contador++;
                compra.IdMoneda = AporteReinversionConstantes.PESOS;
                compra.IdMonedaLiquidacion = AporteReinversionConstantes.PESOS;
                compra.IdOperacion = sol.IdInterno;
                compra.IdTipoMovimiento = "MOVING";
                compra.MontoFormaDePago = sol.Monto;
                compra.RecibePagaPortafolio = "P";
                compra.FechaLiquidacion = reinversionConFechaDeLiquidacion ? FechaLegacy.aFechaSql(fechaLiquidaSpotCompra) : sol.FechaAceptacion;
                entradaSpot.p_DatosFormaPago.add(compra);
            }
        }
        if (entradaSpot.p_DatosOperacion.size() > 0 && entradaSpot.p_DatosFormaPago.size() > 0) {
            entradaSpot.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaSpot.p_IdUsuario = entrada.p_IdUsuario;
            entradaSpot.p_IdModulo = entrada.p_IdModulo;
            entradaSpot.p_CalculaMargen = AporteReinversionConstantes.SPOT_CALCULA_MARGEN;
            entradaSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
            ProcesosExternos.SalidaSpot ss = spot.procesar(entradaSpot);
            for (ProcesosExternos.OperacionSpot rs : ss.P_OUT_IDOPERACIONSPOT) {
                for (SolicitudAporteRescate sol : c.solicitudesInsertar) {
                    if (Long.parseLong(rs.IdInterno) == sol.IdInterno) {
                        if (AporteReinversionConstantes.SPOT_ESTADO_RECHAZADO.equalsIgnoreCase(rs.Estado)) {
                            sol.EstadoProceso = AporteReinversionConstantes.ESTADO_RECHAZADO;
                            break;
                        } else {
                            sol.IdOperacionSpot = rs.IdOperacionSpot;
                            break;
                        }
                    }
                }
            }
            for (MensajeSalida rs : ss.P_OUT_MENSAJES) {
                if (AporteReinversionConstantes.SPOT_ERROR.equalsIgnoreCase(rs.tipo)) {
                    c.out.P_OUT_ERRORESCONTADOR += 1L;
                    c.out.P_OUT_MENSAJES.add(new MensajeSalida(0L, rs.tipo, rs.codigo, AporteReinversionConstantes.MODULO_SPOT + " : " + rs.descripcion));
                    // D-CSR-10: the legacy rejects only the FIRST request of the list, whichever failed
                    for (SolicitudAporteRescate sol : c.solicitudesInsertar) {
                        sol.EstadoProceso = AporteReinversionConstantes.ESTADO_RECHAZADO;
                        break;
                    }
                }
            }
            if (c.out.P_OUT_ERRORESCONTADOR > 0) {
                return true;
            }
        }
        return false;
    }

    /** Legacy :415-445: the final reinversion date of the destination rows moves to the certification date of the new contributions (in groups of the lot size). */
    private void actualizarFechaFinalReinversion(AporteReinversionContexto c) {
        List<Long> idsRescates = c.solicitudesInsertar.stream()
                .map(solicitud -> solicitud.IdSolicitudAporteRescateRelacionado != null ? solicitud.IdSolicitudAporteRescateRelacionado : 0L).distinct().collect(Collectors.toList());
        int tamano = AporteReinversionConstantes.MAX_REGISTROS;
        List<ReinversionSerieFondoDestino> actualizar = new ArrayList<>();
        for (int i = 0; i < idsRescates.size(); i += tamano) {
            int fin = Math.min(i + tamano, idsRescates.size());
            actualizar.addAll(datos.reinversionesAActualizar(new ArrayList<>(idsRescates.subList(i, fin))));
        }
        if (!actualizar.isEmpty()) {
            datos.actualizarFechaFinalReinversion(actualizar);
        }
    }

    private void agregarLog(AporteReinversionContexto c) {
        LogContrato.InAgregarLog entradaLog = new LogContrato.InAgregarLog();
        for (SolicitudAporteRescate sol : c.solicitudesInsertar) {
            ClientePortafolioSolicitudes cliente = c.hclientes.get(sol.IdPortafolio);
            LogContrato.Log l = new LogContrato.Log();
            l.IdCabeceraLog = sol.IdSolicitudAporteRescate;
            l.IdTipoLog = "INGRESO";
            l.IdModulo = AporteReinversionConstantes.MODULO_FONDO_MUTUO;
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
            l.IdTipoRegistro = "SOLICITUD_APORTE_REINVERSION";
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

    /** Legacy {@code copiarSolicitudAporteRescate} (:269-388): the legacy copies these fields and leaves the others at the constructor defaults. */
    static SolicitudAporteRescate copiar(SolicitudAporteRescate s) {
        SolicitudAporteRescate c = new SolicitudAporteRescate();
        c.IdInterno = s.IdInterno;
        c.IdSolicitudAporteRescate = s.IdSolicitudAporteRescate;
        c.IdTipoCanalIngreso = s.IdTipoCanalIngreso;
        c.FolioContrato = s.FolioContrato;
        c.IdCliente = s.IdCliente;
        c.IdPortafolio = s.IdPortafolio;
        c.TipoSolicitud = s.TipoSolicitud;
        c.IdSerieFondo = s.IdSerieFondo;
        c.IdTipoMovimientoFondoMutuo = s.IdTipoMovimientoFondoMutuo;
        c.IdDividendo = s.IdDividendo;
        c.IdSerieFondoDestino = s.IdSerieFondoDestino;
        c.IdEmisorDestino = s.IdEmisorDestino;
        c.IdClienteDestino = s.IdClienteDestino;
        c.IdPortafolioDestino = s.IdPortafolioDestino;
        c.IdSerieFondoOrigen = s.IdSerieFondoOrigen;
        c.IdEmisorOrigen = s.IdEmisorOrigen;
        c.MontoOrigen = s.MontoOrigen;
        c.IndicadorMLT = s.IndicadorMLT;
        c.Overnight = s.Overnight;
        c.IdSolicitudAporteRescateRelacionado = s.IdSolicitudAporteRescateRelacionado;
        c.IdTipoCartera = s.IdTipoCartera;
        c.CalculoPorMontoCuota = s.CalculoPorMontoCuota;
        c.FechaIngresoRegistro = s.FechaIngresoRegistro;
        c.FechaEmision = s.FechaEmision;
        c.FechaAceptacion = s.FechaAceptacion;
        c.FechaValorCuota = s.FechaValorCuota;
        c.FechaCertificacion = s.FechaCertificacion;
        c.FechaLiquidacion = s.FechaLiquidacion;
        c.FechaAnulacion = s.FechaAnulacion;
        c.FechaModificacion = s.FechaModificacion;
        c.FechaOriginalInversion = s.FechaOriginalInversion;
        c.EsPorMontoLiquido = s.EsPorMontoLiquido;
        c.Monto = s.Monto;
        c.NumeroCuotas = s.NumeroCuotas;
        c.Porcentaje = s.Porcentaje;
        c.ValorCuota = s.ValorCuota;
        c.CostoUF = s.CostoUF;
        c.CostoMonedaLocal = s.CostoMonedaLocal;
        c.ParidadFechaCertificacion = s.ParidadFechaCertificacion;
        c.ValorUFFechaInversion = s.ValorUFFechaInversion;
        c.ParidadFechaInversion = s.ParidadFechaInversion;
        c.IdMoneda = s.IdMoneda;
        c.IdMonedaPago = s.IdMonedaPago;
        c.MontoMonedaPago = s.MontoMonedaPago;
        c.ValidaSpot = s.ValidaSpot;
        c.GeneraSpot = s.GeneraSpot;
        c.MontoComision = s.MontoComision;
        c.ComisionInformada = s.ComisionInformada;
        c.MontoComisionLocal = s.MontoComisionLocal;
        c.MontoLiquido = s.MontoLiquido;
        c.MontoLiquidoLocal = s.MontoLiquidoLocal;
        c.MontoPago = s.MontoPago;
        c.MontoPagoLocal = s.MontoPagoLocal;
        c.MontoInversion = s.MontoInversion;
        c.NumeroCuotasInversion = s.NumeroCuotasInversion;
        c.ValorCuotaInversion = s.ValorCuotaInversion;
        c.CostoMonedaLocalInversion = s.CostoMonedaLocalInversion;
        c.Spread = s.Spread;
        c.Estado = s.Estado;
        c.EstadoLiquidacion = s.EstadoLiquidacion;
        c.MueveCta = s.MueveCta;
        c.FolioCartaLiquidacion = s.FolioCartaLiquidacion;
        c.IdMedioDePago = s.IdMedioDePago;
        c.Glosa = s.Glosa;
        c.IdEmpresa = s.IdEmpresa;
        c.IdUsuario = s.IdUsuario;
        c.IdModulo = s.IdModulo;
        c.OrigenOperacion = s.OrigenOperacion;
        c.SolicitudBatch = s.SolicitudBatch;
        c.IdRegimenTributario = s.IdRegimenTributario;
        c.TipoCarteraNormativa = s.TipoCarteraNormativa;
        c.Tributacion = s.Tributacion;
        c.IdRegimenTributarioDestino = s.IdRegimenTributarioDestino;
        c.TipoCarteraNormativaDestino = s.TipoCarteraNormativaDestino;
        c.AdministracionLiquidez = s.AdministracionLiquidez;
        c.Visado = s.Visado;
        c.IdPreferenciaPagoPortafolio = s.IdPreferenciaPagoPortafolio;
        c.IdAutorizadoOrigen = s.IdAutorizadoOrigen;
        c.IdAutorizadoDestino = s.IdAutorizadoDestino;
        c.IdMonedaSistema = s.IdMonedaSistema;
        c.MontoMonedaSistema = s.MontoMonedaSistema;
        c.Switch = s.Switch;
        c.EstadoAnulacion = s.EstadoAnulacion;
        c.FolioOmnibus = s.FolioOmnibus;
        c.FolioAgfOmnibus = s.FolioAgfOmnibus;
        c.IdEmpleadoEjecutivo = s.IdEmpleadoEjecutivo;
        c.IdEmpleadoEjecutivoDestino = s.IdEmpleadoEjecutivoDestino;
        c.IdPersona = s.IdPersona;
        c.IdInstrumento = s.IdInstrumento;
        c.IdTipoInstrumento = s.IdTipoInstrumento;
        c.EstadoProceso = s.EstadoProceso;
        c.IdIngresoEgreso = s.IdIngresoEgreso;
        c.IdOperacionSpot = s.IdOperacionSpot;
        c.IdCartera = s.IdCartera;
        c.IdAgrupador = s.IdAgrupador;
        c.IdAgrupadorInterno = s.IdAgrupadorInterno;
        c.RelacionTraspaso = s.RelacionTraspaso;
        c.IdPersonaEmisorDestino = s.IdPersonaEmisorDestino;
        c.RentabilidadReal = s.RentabilidadReal;
        c.IdMatrizAgrupador = s.IdMatrizAgrupador;
        c.IdMotivoRetiro = s.IdMotivoRetiro;
        c.IngresoAutomatico = s.IngresoAutomatico;
        c.ForzarSpot = s.ForzarSpot;
        c.RazonRetiro = s.RazonRetiro;
        c.TipoContratoOperacion = s.TipoContratoOperacion;
        c.FolioContratoOperacion = s.FolioContratoOperacion;
        c.IdPersonaCobro = s.IdPersonaCobro;
        c.IdCuentaBancaria = s.IdCuentaBancaria;
        c.IdBanco = s.IdBanco;
        c.IdTipoCuentaBancaria = s.IdTipoCuentaBancaria;
        c.NumeroCuentaBancaria = s.NumeroCuentaBancaria;
        c.NombreTipoCuentaBancaria = s.NombreTipoCuentaBancaria;
        c.IdPersonaTraspaso = s.IdPersonaTraspaso;
        c.SecuenciaRelacionada = s.SecuenciaRelacionada;
        return c;
    }
}
