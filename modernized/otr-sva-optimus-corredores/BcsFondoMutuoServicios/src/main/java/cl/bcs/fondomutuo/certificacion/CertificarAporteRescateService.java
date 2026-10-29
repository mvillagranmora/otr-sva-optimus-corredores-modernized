package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APORTE_REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.APV_B;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.BONIFICACION_FISCAL;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_ACEPTADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_CERTIFICADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_INGRESADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_LIQUIDADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.OK;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_SOLICITUD_APORTE;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.UF;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ComprobanteNoLiquidado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Estado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.InSolicitudes;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.IndicadorPortafolio;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.LogFila;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ParametroOperaCSN;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Persona;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ResultadoCertificacion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Traspaso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.contrato.PreciosTasasContrato;
import cl.bcs.fondomutuo.cierre.contrato.PreciosTasasContrato.PreciosTasas;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.Resultado;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.Serie;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.ConteoErrores;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Legacy {@code CertificarSolicitudAporteRescate.logica}, fronts F-A (header, requirements, parameters, reads, quota value, the eight rules, early exit) and F-B
 * (indicators, recalculation, effective date, partition, GEN_OCSNEG), bug for bug; after F-B it hands the context to the seam
 * {@link CertificarAporteRescateFasesPosteriores} (fronts C..J, T2..T5). Holds only its ports: no state. Exceptions are never caught here except where the
 * legacy does (the optional parameters).
 */
public class CertificarAporteRescateService implements CertificarAporteRescatePort {

    private static final String SECUENCIA_LOG = "BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate";

    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajesPort diccionario;
    private final RevisarRequisitosBasicosPort basicos;
    private final PreciosTasasPort precios;
    private final CalcularMontoNumeroCuota calculoCuotas;
    private final CertificarAporteRescateDatos datos;
    private final CertificarAporteRescateEscritura escritura;
    private final SecuenciaPort secuencias;
    private final CertificarAporteRescateFasesPosteriores posteriores;

    public CertificarAporteRescateService(ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario, RevisarRequisitosBasicosPort basicos, PreciosTasasPort precios,
            CalcularMontoNumeroCuota calculoCuotas, CertificarAporteRescateDatos datos, CertificarAporteRescateEscritura escritura, SecuenciaPort secuencias,
            CertificarAporteRescateFasesPosteriores posteriores) {
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.basicos = basicos;
        this.precios = precios;
        this.calculoCuotas = calculoCuotas;
        this.datos = datos;
        this.escritura = escritura;
        this.secuencias = secuencias;
        this.posteriores = posteriores;
    }

    @Override
    public Salida logica(Entrada entrada) throws Exception {
        Salida out = new Salida();
        CertificarAporteRescateContexto ctx = new CertificarAporteRescateContexto();
        ctx.entrada = entrada;
        ctx.salida = out;
        Long idEmpresa = entrada.p_IdEmpresa;

        ctx.paisSistema = configuracion.entero(idEmpresa, "PLATAFORMA", "PLA.PaisSistema");

        RevisarRequisitosBasicosSalida salidaBasicos = basicos.revisar(idEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (ConteoErrores.errores(salidaBasicos.p_out_Mensajes) > 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
            return out;
        }

        if (entrada.p_Solicitudes.size() == 0) {
            return rechazoDeEntrada(out, idEmpresa, "NO_SE_INGRESARON_SOLICITUDES");
        }

        ArrayList<Long> ids = new ArrayList<Long>();
        for (InSolicitudes mov : entrada.p_Solicitudes) {
            if (ids.contains(mov.IdSolicitudAporteRescate)) {
                return rechazoDeEntrada(out, idEmpresa, "SOLICITUD_DUPLICADA");
            } else {
                ids.add(mov.IdSolicitudAporteRescate);
            }
        }

        ctx.fechaSistema = configuracion.texto(idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        ctx.tipoComprobanteFacturaNoApv = configuracion.texto(idEmpresa, "FONDOMUTUO", "TipoComprobanteFacturaNoApv");
        ctx.tipoComprobanteFacturaApv = configuracion.texto(idEmpresa, "FONDOMUTUO", "TipoComprobanteFacturaApv");
        ctx.calcularComisionCertificacion = configuracion.texto(idEmpresa, "FONDOMUTUO", "CALCULAR_COMISION_CERTIFICACION");
        ctx.validarCertificacion = configuracion.texto(idEmpresa, "FONDOMUTUO", "VALIDAR_CERTIFICACION");
        ctx.rescateGeneraVentaSpotDolar = configuracion.texto(idEmpresa, "FONDOMUTUO", "RESCATE_GENERA_VENTA_SPOT_DOLAR");
        ctx.comisionComprobanteFacturacionAutomatico = configuracion.texto(idEmpresa, "FONDOMUTUO", "RESCATE_COMISION_COMPROBANTE_AUTOMATICO");
        ctx.porcentajeRetencion = obtenerPorcentajeRetencion(idEmpresa);
        ctx.notificaOWP = configuracion.texto(idEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");

        List<Long> idSolicitudes = entrada.p_Solicitudes.stream().map(s -> s.IdSolicitudAporteRescate).distinct().collect(Collectors.toList());

        List<SolicitudProceso> solicitudes = datos.solicitudes(idEmpresa, idSolicitudes);
        List<Long> idSolicitudesParaConsultaTraspasos = obtenerIdsSolicitudesConsultaTraspasos(solicitudes);
        List<Estado> estados = datos.estados();
        List<ComprobanteNoLiquidado> comprobantes = datos.comprobantesNoLiquidados(idSolicitudes);
        Map<Long, Traspaso> traspasosApv = datos.traspasosApv(idSolicitudesParaConsultaTraspasos).stream()
                .collect(Collectors.toMap(t -> t.IdSolicitudAporteRescate, t -> t));
        Map<Long, Traspaso> traspasosNoApv = datos.traspasosNoApv(idSolicitudesParaConsultaTraspasos).stream()
                .collect(Collectors.toMap(t -> t.IdSolicitudAporteRescate, t -> t));
        Persona personaTGR = primeraPersona(datos.personasPorDocumento(obtenerDocumento(idEmpresa, "APV.DocumentoIdentidadPersonaTGR")));
        Persona personaSII = primeraPersona(datos.personasPorDocumento(obtenerDocumento(idEmpresa, "APV.DocumentoIdentidadPersonaSII")));

        Map<Long, Traspaso> traspasos = traspasosApv;
        traspasos.putAll(traspasosNoApv);
        Map<String, Estado> hestado = Hashes.aHash(estados, e -> e.IdEstado);

        ctx.solicitudes = solicitudes;
        ctx.estados = hestado;
        ctx.traspasos = traspasos;
        ctx.personaTGR = personaTGR;
        ctx.personaSII = personaSII;

        if (solicitudes.size() == 0) {
            return rechazoDeEntrada(out, idEmpresa, "UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS");
        }

        Map<Long, PreciosTasas> hashValorCuotaCargado = obtenerValorCuotaCargado(solicitudes, entrada, ctx.paisSistema);

        // F-A: the eight rules; the first that fails wins (continue)
        int solicitudesPorProcesar = 0;
        for (SolicitudProceso sol : solicitudes) {
            PreciosTasas valorCuotaCargado = hashValorCuotaCargado.get(sol.IdSolicitudAporteRescate);

            if (!ESTADO_INGRESADO.equals(sol.EstadoSolicitud) && !ESTADO_ACEPTADO.equals(sol.EstadoSolicitud)) {
                rechazar(sol, "UI_ESTADO_INCORRECTO_CERTIFICACION");
                continue;
            }

            if (!FechaLegacy.texto(sol.FechaCertificacion).equals(entrada.p_FechaCertificacion)) {
                rechazar(sol, "UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO");
                continue;
            }

            if (valorCuotaCargado == null) {
                rechazar(sol, "UI_NO_EXISTE_VALOR_CUOTA");
                continue;
            }

            if (valorCuotaCargado.FechaFuente == null) {
                rechazar(sol, "UI_NO_EXISTE_VALOR_CUOTA");
                continue;
            }

            String fechaValorCuotaSolicitud = FechaLegacy.texto(sol.FechaValorCuota);
            String fechaValorCuotaCargado = valorCuotaCargado.FechaFuente.substring(0, 10);

            if (!fechaValorCuotaSolicitud.equalsIgnoreCase(fechaValorCuotaCargado)) {
                rechazar(sol, "UI_NO_EXISTE_VALOR_CUOTA");
                continue;
            }

            if (SI.equals(sol.GeneraOmnibus) && sol.IdOmnibus == null && !(SI.equals(sol.Switch) && APORTE_REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo))) {
                rechazar(sol, "UI_ONMIBUS_NO_CREADO");
                continue;
            }

            boolean tieneComprobanteNoLiq = false;
            for (ComprobanteNoLiquidado comprobante : comprobantes) {
                if (sol.IdSolicitudAporteRescate.longValue() == comprobante.IdSolicitudAporteRescate.longValue()) {
                    tieneComprobanteNoLiq = true;
                    sol.IdComprobanteTesoreria = comprobante.IdComprobante;
                    break;
                }
            }
            if (tieneComprobanteNoLiq) {
                rechazar(sol, "UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO");
                continue;
            }
            if (!SI.equals(sol.Switch) && SI.equals(ctx.validarCertificacion) && TIPO_SOLICITUD_APORTE.equals(sol.TipoSolicitud)
                    && !ESTADO_LIQUIDADO.equals(sol.EstadoLiquidacion) && sol.FolioCartaLiquidacion.longValue() == 0) {
                rechazar(sol, "UI_VALIDACION_LIQUIDACION_FONDO_MUTUO");
                continue;
            }

            if (!(SI.equals(sol.Switch)) && (NO.equals(sol.CertificaSinAsociado))) {
                if (sol.IdSolicitudAporteRescateRelacionado.longValue() != 0
                        && (sol.IdSolicitudAporteRescate.longValue() > sol.IdSolicitudAporteRescateRelacionado.longValue()
                                && (!ESTADO_CERTIFICADO.equals(sol.EstadoRelacionado) || !ESTADO_LIQUIDADO.equals(sol.EstadoLiquidacionRelacionado)))) {
                    rechazar(sol, "UI_SOLICITUD_ASOCIADA_NO_CERTIFICADA");
                    continue;
                }
            }

            if (personaTGR == null && BONIFICACION_FISCAL.equals(sol.IdRegimenTributarioAPV)) {
                rechazar(sol, "UI_NO_EXISTE_TGR_CONFIGURADA");
                continue;
            }

            if (personaSII == null && APV_B.equals(sol.IdRegimenTributarioAPV)) {
                rechazar(sol, "UI_NO_EXISTE_SII_CONFIGURADA");
                continue;
            }

            solicitudesPorProcesar++;
        }

        // FM-CER-14: early exit, nothing valid
        if (solicitudesPorProcesar == 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_RESULTADO.addAll(procesarSalida(entrada, solicitudes, hestado));
            insertarLog(solicitudes, entrada, ctx.fechaSistema);
            return out;
        }

        // F-B
        ctx.indicadoresPortafolio = obtenerIndicadoresPortafolio(solicitudes);

        Map<Long, Resultado> hcalculo = obtenerCalculoMontoCuotas(idEmpresa, solicitudes);
        actualizarMontosSolicitudes(solicitudes, hcalculo);

        ctx.fechaCertificacion = CertificarAporteRescateReglas.fechaCertificacionEfectiva(ctx.fechaSistema, entrada.p_FechaCertificacion);

        ctx.aportes = CertificarAporteRescateReglas.porTipoOrdenadas(solicitudes, TIPO_SOLICITUD_APORTE);
        ctx.rescates = CertificarAporteRescateReglas.porTipoOrdenadas(solicitudes, "RES");

        // CELLINCECT-763: the indicator "operates with a negative balance" of the portfolio of every aporte
        List<Long> portafoliosABuscar = new ArrayList<>();
        for (SolicitudProceso aporte : ctx.aportes) {
            portafoliosABuscar.add(aporte.IdPortafolio);
        }
        List<ParametroOperaCSN> parametrosOPCSN = datos.parametrosOperaConSaldoNegativo(portafoliosABuscar);
        for (SolicitudProceso aporte : ctx.aportes) {
            ParametroOperaCSN param = parametrosOPCSN.stream().filter(p -> p.IdEntidad.equals(aporte.IdPortafolio)).findFirst().orElse(new ParametroOperaCSN());
            aporte.OperaSaldoNegativo = param.ValorIndicador;
        }

        return posteriores.continuar(ctx);
    }

    private Salida rechazoDeEntrada(Salida out, Long idEmpresa, String codigo) {
        out.P_OUT_ERRORESCONTADOR = 1L;
        out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<String, String>(), diccionario, idEmpresa));
        return out;
    }

    private static void rechazar(SolicitudProceso sol, String codigo) {
        sol.EstadoProceso = ESTADO_RECHAZADO;
        sol.Codigo = codigo;
    }

    /** Legacy {@code obtenerPorcentajeRetencion}: optional, 0 when absent or not numeric (D-F3B-09). */
    private Long obtenerPorcentajeRetencion(Long idEmpresa) {
        Long porcentajeRetencion = 0L;
        try {
            porcentajeRetencion = configuracion.entero(idEmpresa, "PLATAFORMA", "APV.PorcentajeRetencion");
        } catch (Exception e) {
            // legacy: logged, the value stays 0
        }
        return porcentajeRetencion;
    }

    private String obtenerDocumento(Long idEmpresa, String parametro) {
        String documento = "";
        try {
            documento = configuracion.texto(idEmpresa, "PLATAFORMA", parametro);
        } catch (Exception e) {
            // legacy: warning, the document stays empty (the query still runs)
        }
        return documento;
    }

    private static Persona primeraPersona(List<Persona> personas) {
        return personas.size() > 0 ? personas.get(0) : null;
    }

    private static List<Long> obtenerIdsSolicitudesConsultaTraspasos(List<SolicitudProceso> solicitudes) {
        List<Long> ids = new ArrayList<Long>();
        ids.addAll(solicitudes.stream().map(s -> s.IdSolicitudAporteRescate).collect(Collectors.toList()));
        ids.addAll(solicitudes.stream().filter(s -> !s.IdSolicitudAporteRescateRelacionado.equals(0L)).map(s -> s.IdSolicitudAporteRescateRelacionado)
                .collect(Collectors.toList()));
        return ids;
    }

    /** The quota value of EVERY solicitud read, by {@code IdRegistro} (rows without {@code IdInstrumento} dropped; a repeated key is IllegalStateException). */
    private Map<Long, PreciosTasas> obtenerValorCuotaCargado(List<SolicitudProceso> solicitudes, Entrada entrada, Long paisSistema) {
        PreciosTasasContrato.Entrada entradaPrecioTasa = new PreciosTasasContrato.Entrada();
        entradaPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaPrecioTasa.p_IdModulo = entrada.p_IdModulo;
        entradaPrecioTasa.p_IdPais = paisSistema;
        entradaPrecioTasa.p_IdUsuario = entrada.p_IdUsuario;
        entradaPrecioTasa.p_DatosInstrumentos = solicitudes.stream().map(sol -> {
            PreciosTasasContrato.DatosInstrumentos d = new PreciosTasasContrato.DatosInstrumentos();
            d.FechaConsulta = PreciosTasasContrato.fechaConsulta(FechaLegacy.texto(sol.FechaValorCuota));
            d.IdClasificacionTipoInstrumento = "FM";
            d.IdInstrumento = sol.IdInstrumento;
            d.IdMoneda = sol.IdMonedaFondo;
            d.IdRegistro = sol.IdSolicitudAporteRescate;
            d.IdTipoCriterioTasa = "0";
            d.IdTipoInstrumento = sol.IdTipoInstrumento;
            d.IdUso = "CARTERA";
            d.Nemotecnico = sol.IdSerieFondo;
            d.PlazoResidual = 0L;
            d.TipoCartera = "0";
            return d;
        }).collect(Collectors.toList());

        PreciosTasasContrato.Salida salida = PreciosTasasContrato.entregar(precios, entradaPrecioTasa);
        return salida.P_OUT_PRECIOSTASAS.stream().filter(p -> p.IdInstrumento != null).collect(Collectors.toMap(p -> p.IdRegistro, p -> p));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // F-B
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    private Map<Long, IndicadorPortafolio> obtenerIndicadoresPortafolio(List<SolicitudProceso> solicitudes) throws Exception {
        List<Long> portafolios = solicitudes.stream().filter(CertificarAporteRescateReglas::pendienteOAdvertencia).map(s -> s.IdPortafolio).distinct()
                .collect(Collectors.toList());
        return Hashes.aHash(datos.indicadoresPortafolio(portafolios), i -> i.IdPortafolio);
    }

    private Map<Long, Resultado> obtenerCalculoMontoCuotas(Long idEmpresa, List<SolicitudProceso> solicitudes) throws Exception {
        EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = idEmpresa;
        entradaCalculo.p_Series = solicitudes.stream().filter(CertificarAporteRescateReglas::pendienteOAdvertencia).map(sol -> {
            Serie serie = new Serie();
            serie.IdInterno = sol.IdSolicitudAporteRescate;
            serie.Fecha = FechaLegacy.texto(sol.FechaValorCuota);
            serie.IdSerieFondo = sol.IdSerieFondo;
            serie.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
            serie.Monto = sol.CostoMonedaLocal;
            serie.NumeroCuotas = sol.NumeroCuotas;
            serie.IdMonedaFondo = sol.IdMonedaFondo;
            serie.IdMonedaInversion = sol.IdMoneda;
            serie.TipoSolicitud = sol.TipoSolicitud;
            serie.TipoFactorAjuste = UF;
            serie.IdEmpresa = idEmpresa;
            serie.IdTipoMovimientoFondoMutuo = (sol.IdTipoMovimientoFondoMutuo == null) ? "" : sol.IdTipoMovimientoFondoMutuo;
            return serie;
        }).collect(Collectors.toList());

        SalidaCalcularMontoNumeroCuota salidaCalculo = new SalidaCalcularMontoNumeroCuota();
        if (entradaCalculo.p_Series.size() > 0) {
            salidaCalculo = calculoCuotas.logica(entradaCalculo);
        }
        return Hashes.aHash(salidaCalculo.P_OUT_RESULTADO, r -> r.IdInterno);
    }

    /** FM-CER-17: the amounts of the calculation over the PEN / ADV solicitudes; a failed or missing result rejects the solicitud. */
    private static void actualizarMontosSolicitudes(List<SolicitudProceso> solicitudes, Map<Long, Resultado> hcalculo) {
        solicitudes.stream().filter(CertificarAporteRescateReglas::pendienteOAdvertencia).forEach(sol -> {
            Resultado r = hcalculo.get(sol.IdSolicitudAporteRescate);
            if (r == null || !OK.equals(r.Resultado)) {
                rechazar(sol, "ERROR_CALCULO_MONTO_CUOTA");
            } else if (NO.equals(sol.TieneComisionNNRelacionada)) {
                sol.CostoMonedaLocal = r.MontoDestino;
                sol.Monto = r.Monto;
                sol.ValorCuota = r.ValorCuota;
                sol.NumeroCuotas = r.NumeroCuotas;
                sol.ParidadFechaCertificacion = r.ValorParidad;
                sol.CostoUF = APORTE_REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) ? sol.CostoUF : r.CostoEnFactorAjuste;
                sol.ValorUFFechaInversion = APORTE_REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) ? sol.ValorUFFechaInversion : r.ValorFactorAjuste;
                sol.IdMonedaSistema = r.IdMonedaSistema;
                // with an aporte that has a Spot the amount of the Spot is kept
                sol.MontoMonedaSistema = TIPO_SOLICITUD_APORTE.equals(sol.TipoSolicitud) && sol.IdOperacionSpot != null && !sol.IdOperacionSpot.equals(0L)
                        ? sol.MontoMonedaSistema
                        : r.MontoMonedaSistema;
                sol.MontoPago = r.Monto;
                sol.MontoPagoLocal = r.MontoDestino;
            }
        });
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // output and log (reused by the later fronts)
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    List<ResultadoCertificacion> procesarSalida(Entrada entrada, List<SolicitudProceso> solicitudes, Map<String, Estado> hestado) {
        List<ResultadoCertificacion> resultados = new ArrayList<>();
        for (SolicitudProceso solicitud : solicitudes) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("IdComprobante", String.valueOf(solicitud.IdComprobanteTesoreria));
            params.put("IdSolicitudAporteRescateRelacionado", String.valueOf(solicitud.IdSolicitudAporteRescateRelacionado));
            params.put("IdSolicitudAporteRescate", String.valueOf(solicitud.IdSolicitudAporteRescate));
            params.put("DocumentoIdentidad", solicitud.DocumentoIdentidad);
            params.put("CodigoPortafolio", solicitud.CodigoPortafolio);

            Estado estado = hestado.get(solicitud.EstadoProceso);
            ResultadoCertificacion r = new ResultadoCertificacion();
            r.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
            r.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado;
            r.IdSerieFondo = solicitud.IdSerieFondo;
            r.Codigo = solicitud.Codigo;
            r.Estado = solicitud.EstadoProceso;
            r.NombreEstado = estado.Nombre;
            r.DocumentoIdentidad = solicitud.DocumentoIdentidad;
            r.CodigoPortafolio = solicitud.CodigoPortafolio;
            r.DescripcionCodigo = solicitud.DescripcionCodigo.isEmpty() ? obtenerDescripcionMensajeSalida(entrada.p_IdEmpresa, solicitud.Codigo, params) : solicitud.DescripcionCodigo;

            if (SI.equals(entrada.p_CertificacionAporteSwitchSimulado)) {
                r.IdSolicitudAporteRescate = 0L;
            }
            resultados.add(r);
        }
        return resultados;
    }

    private String obtenerDescripcionMensajeSalida(Long idEmpresa, String codigo, Map<String, String> parametrosMensaje) {
        String descripcion = "";
        try {
            descripcion = diccionario.mensaje(idEmpresa, codigo, parametrosMensaje);
        } catch (Exception e) {
            // legacy: the text stays empty
        }
        return descripcion;
    }

    void insertarLog(List<SolicitudProceso> sol, Entrada entrada, String fechaSistema) {
        List<LogFila> filas = new ArrayList<>();
        for (SolicitudProceso s : sol) {
            LogFila l = new LogFila();
            l.IdSolicitudAporteRescate = s.IdSolicitudAporteRescate;
            l.Codigo = s.Codigo;
            l.IdEstado = s.EstadoProceso;
            l.TipoSolicitud = s.TipoSolicitud;
            l.IdTipoMovimientoFondoMutuo = s.IdTipoMovimientoFondoMutuo;
            l.IdUsuario = entrada.p_IdUsuario;
            l.IdEmpresa = entrada.p_IdEmpresa;
            l.FechaSistema = fechaSistema;
            l.IdSerieFondo = s.IdSerieFondo;
            l.IdEmisor = s.IdEmisor;
            l.Monto = s.Monto;
            l.IdMoneda = s.IdMonedaFondo;
            l.MontoOriginal = s.CostoMonedaLocal;
            l.TipoCambio = s.ParidadFechaCertificacion;
            l.IdMonedaOriginal = s.IdMoneda;
            l.ValorCuota = s.ValorCuota != null ? s.ValorCuota : BigDecimal.ZERO;
            l.NumeroCuotas = s.NumeroCuotas != null ? s.NumeroCuotas : BigDecimal.ZERO;
            l.IdCliente = s.IdCliente;
            l.IdPortafolio = s.IdPortafolio;
            l.Simulacion = entrada.p_SimularOperacion;
            filas.add(l);
        }
        List<Long> idsLog = secuencias.siguientes(SECUENCIA_LOG, (long) filas.size());
        for (int i = 0; i < filas.size(); i++) {
            filas.get(i).IdLogCertificacionAporteRescate = idsLog.get(i);
        }
        escritura.insertarLog(filas);
    }
}
