package cl.bcs.fondomutuo.consultas.fecha;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.domain.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.InFechas;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.SalidaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.ConteoErrores;

/**
 * Legacy {@code ConsultarFechaSolicitudAporteRescate.logica} (FM-FSA-01..08, read only): the dates a contribution or redemption request would get. Checks in
 * the legacy order (basic requirements, profile, empty list), reads the configuration of the series, builds one {@code InFechas} per request with the
 * reinversion-set detection (D-F3A-01: a list of more than one request), and delegates to {@code CalcularFechaSolicitudAporteRescate}. Bug for bug: the
 * profile and the company are read twice, a repeated series makes {@code toMap} throw, a missing series in a list of two or more is a
 * NullPointerException, the counter and messages of the collaborator are not read. No state; the unit of work is the ambient read-only transaction.
 */
public class ConsultarFechaService implements ConsultarFechaPort {

    private final RevisarRequisitosBasicosPort basicos;
    private final EmpresaPort empresas;
    private final ObtenerPerfilFondoMutuo perfil;
    private final ConsultarFechaDatos datos;
    private final CalcularFechaSolicitudAporteRescate fechas;
    private final DiccionarioMensajesPort diccionario;

    public ConsultarFechaService(RevisarRequisitosBasicosPort basicos, EmpresaPort empresas, ObtenerPerfilFondoMutuo perfil, ConsultarFechaDatos datos,
            CalcularFechaSolicitudAporteRescate fechas, DiccionarioMensajesPort diccionario) {
        this.basicos = basicos;
        this.empresas = empresas;
        this.perfil = perfil;
        this.datos = datos;
        this.fechas = fechas;
        this.diccionario = diccionario;
    }

    @Override
    public ConsultarFechaContrato.Salida logica(ConsultarFechaContrato.Entrada entrada) throws Exception {
        ConsultarFechaContrato.Salida out = validacionesServicio(entrada);
        if (out.P_OUT_ERRORESCONTADOR.equals(0L)) {
            Map<String, ConsultarFechaContrato.ConfiguracionSerie> configuraciones = obtenerConfiguracionSerieFondo(entrada.p_SolicitudAporteRescate,
                    entrada.p_IdEmpresa);
            SalidaCalcularFechaSolicitudAporteRescate calculadas = obtenerFechasSolicitud(entrada.p_SolicitudAporteRescate, configuraciones,
                    entrada.p_IdEmpresa, entrada.p_IdUsuario);
            out.P_OUT_RESULTADO = generarSalida(calculadas);
        }
        return out;
    }

    private ConsultarFechaContrato.Salida validacionesServicio(ConsultarFechaContrato.Entrada entrada) throws Exception {
        ConsultarFechaContrato.Salida out = new ConsultarFechaContrato.Salida();
        RevisarRequisitosBasicosSalida requisitos = basicos.revisar(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (ConteoErrores.errores(requisitos.p_out_Mensajes) > 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.addAll(requisitos.p_out_Mensajes);
            return out;
        }
        if (obtenerPerfil(entrada.p_IdUsuario) == null) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_ERROR, "PERFIL_INCORRECTO", new HashMap<>(), diccionario, entrada.p_IdEmpresa));
            return out;
        }
        Long cantidad = Long.valueOf(entrada.p_SolicitudAporteRescate.size());   // a null list is a NullPointerException, as the legacy
        if (cantidad.equals(0L)) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_INGRESARON_SOLICITUDES", new HashMap<>(), diccionario,
                    entrada.p_IdEmpresa));
            return out;
        }
        return out;
    }

    /** One read of the profile: the legacy reads it twice (here and in {@link #obtenerFechasEntrada}); the second read is kept (D-F3A-02). */
    private String obtenerPerfil(Long idUsuario) throws Exception {
        EntradaObtenerPerfilFondoMutuo e = new EntradaObtenerPerfilFondoMutuo();
        e.p_IdUsuario = idUsuario;
        return perfil.logica(e).PerfilFondoMutuo;
    }

    private Map<String, ConsultarFechaContrato.ConfiguracionSerie> obtenerConfiguracionSerieFondo(List<ConsultarFechaContrato.Solicitud> solicitudes,
            Long idEmpresa) {
        List<String> serieFondos = solicitudes.parallelStream().map(s -> s.IdSerieFondo).collect(Collectors.toList());
        List<ConsultarFechaContrato.ConfiguracionSerie> lista = datos.configuracionSeries(serieFondos, idEmpresa);
        return lista.parallelStream().collect(Collectors.toMap(c -> c.IdSerieFondo, c -> c));   // a repeated series: IllegalStateException (D-NOR-9)
    }

    private SalidaCalcularFechaSolicitudAporteRescate obtenerFechasSolicitud(List<ConsultarFechaContrato.Solicitud> solicitudes,
            Map<String, ConsultarFechaContrato.ConfiguracionSerie> configuraciones, Long idEmpresa, Long idUsuario) throws Exception {
        EntradaCalcularFechaSolicitudAporteRescate entrada = new EntradaCalcularFechaSolicitudAporteRescate();
        Long idPais = empresas.informacion(idEmpresa).IdPais;
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdPais = idPais;
        entrada.p_SolicitudAporteRescate = obtenerFechasEntrada(solicitudes, configuraciones, idUsuario);
        return fechas.logica(entrada);
    }

    private List<InFechas> obtenerFechasEntrada(List<ConsultarFechaContrato.Solicitud> solicitudes,
            Map<String, ConsultarFechaContrato.ConfiguracionSerie> configuraciones, Long idUsuario) throws Exception {
        String perfilActual = obtenerPerfil(idUsuario);
        boolean comercial = ConstantesFondoMutuo.PERFIL_COMERCIAL.equals(perfilActual);
        return solicitudes.parallelStream().map(solicitud -> {
            InFechas inFecha = new InFechas();
            ConsultarFechaContrato.ConfiguracionSerie configuracion = configuraciones.get(solicitud.IdSerieFondo);
            inFecha.IdInterno = solicitud.IdInterno;
            inFecha.IdSerieFondo = solicitud.IdSerieFondo;
            inFecha.FechaEmision = solicitud.FechaEmision;
            inFecha.SolicitudBatch = ConstantesFondoMutuo.NO;
            inFecha.TipoSolicitud = solicitud.TipoSolicitud;

            // a set of more than one request is read as a reinversion (D-F3A-01): foreign funds, same issuer, different currency
            String fondosExtranjeros = ConstantesFondoMutuo.NO;
            String mismoEmisor = ConstantesFondoMutuo.NO;
            String idMonedaReinversionDiferentes = "";
            Integer solicitudesSize = solicitudes.size();
            if (solicitudes.size() > 1) {
                String emisor = configuraciones.get(solicitudes.get(0).IdSerieFondo).IdEmisor;
                mismoEmisor = ConstantesFondoMutuo.SI;
                fondosExtranjeros = ConstantesFondoMutuo.SI;
                idMonedaReinversionDiferentes = ConstantesFondoMutuo.NO;
                String idMoneda = solicitudes.get(0).IdMoneda;
                for (ConsultarFechaContrato.Solicitud s : solicitudes) {
                    if (ConstantesFondoMutuo.NO.equalsIgnoreCase(configuraciones.get(s.IdSerieFondo).FondoExtranjero)) fondosExtranjeros = ConstantesFondoMutuo.NO;
                    if (!emisor.equalsIgnoreCase(configuraciones.get(s.IdSerieFondo).IdEmisor)) mismoEmisor = ConstantesFondoMutuo.NO;
                    if (!idMoneda.equalsIgnoreCase(s.IdMoneda)) idMonedaReinversionDiferentes = ConstantesFondoMutuo.SI;
                }
            }

            if (configuracion != null) {
                inFecha.EstadoLiquidacion = comercial ? configuracion.EstadoLiquidezComercial : configuracion.EstadoLiquidezBackoffice;
                if (ConstantesFondoMutuo.TIPO_SOLICITUD_APORTE.equals(solicitud.TipoSolicitud)) {
                    inFecha.EstadoFondo = comercial ? configuracion.EstadoAporteComercial : configuracion.EstadoAporteBackoffice;
                    inFecha.NumeroDiasRetencion = configuracion.DiasRetencionesAporte;
                    inFecha.NumeroValutaValorCuota = configuracion.DiasValutaValorCuotaAporte;
                    inFecha.NumeroValutaCertificacion = configuracion.DiasValutaCertificacionAporte;
                    inFecha.NumeroValutaCurse = configuracion.DiasValutaCurseAporte;
                } else {
                    inFecha.EstadoFondo = comercial ? configuracion.EstadoRescateComercial : configuracion.EstadoRescateBackoffice;
                    inFecha.NumeroDiasRetencion = fondosExtranjeros.equalsIgnoreCase(ConstantesFondoMutuo.SI)
                            ? (mismoEmisor.equalsIgnoreCase(ConstantesFondoMutuo.NO) ? 4 : 2) : configuracion.DiasRetencionesRescate;
                    inFecha.NumeroValutaValorCuota = fondosExtranjeros.equalsIgnoreCase(ConstantesFondoMutuo.SI) ? 1 : configuracion.DiasValutaValorCuotaRescate;
                    inFecha.NumeroValutaCertificacion = fondosExtranjeros.equalsIgnoreCase(ConstantesFondoMutuo.SI) ? 2 : configuracion.DiasValutaCertificacionRescate;
                    inFecha.NumeroValutaCurse = configuracion.DiasValutaCurseRescate;
                }
                inFecha.FondoExtranjero = configuracion.FondoExtranjero;
                inFecha.IdPaisFondo = configuracion.IdPaisFondo;
                inFecha.IdPaisEmisor = configuracion.IdPaisEmisor;
                inFecha.IdPaisMoneda = configuracion.IdPaisMoneda;
                inFecha.FondoNacionalDolar = configuracion.FondoNacionalDolar;
                inFecha.IdMercadoFondoMutuo = configuracion.IdMercadoFondoMutuo;
                if (solicitudesSize.compareTo(1) == 0) {
                    inFecha.RescateGeneraSpotCertificacion = (!solicitud.IdMoneda.equalsIgnoreCase(solicitud.IdMonedaPago) && !"".equalsIgnoreCase(solicitud.IdMonedaPago)
                            && !"".equalsIgnoreCase(solicitud.IdMoneda)) ? ConstantesFondoMutuo.SI : ConstantesFondoMutuo.NO;
                } else {
                    inFecha.RescateGeneraSpotCertificacion = ConstantesFondoMutuo.SI.equalsIgnoreCase(idMonedaReinversionDiferentes)
                            ? ConstantesFondoMutuo.SI : ConstantesFondoMutuo.NO;
                }
            }
            return inFecha;
        }).collect(Collectors.toList());
    }

    private static List<ConsultarFechaContrato.Resultado> generarSalida(SalidaCalcularFechaSolicitudAporteRescate calculadas) {
        return calculadas.P_FECHASAPORTERESCATE.parallelStream().map(fecha -> {
            ConsultarFechaContrato.Resultado r = new ConsultarFechaContrato.Resultado();
            r.IdInterno = fecha.IdInterno;
            r.IdSerieFondo = fecha.IdSerieFondo;
            r.FechaEmision = sql(fecha.FechaEmision);
            r.FechaDigitacion = sql(fecha.FechaDigitacion);
            r.FechaAceptacion = sql(fecha.FechaAceptacion);
            r.FechaValorCuota = sql(fecha.FechaValorCuota);
            r.FechaCertificacion = sql(fecha.FechaCertificacion);
            r.FechaLiquidacion = sql(fecha.FechaLiquidacion);
            return r;
        }).collect(Collectors.toList());
    }

    private static Date sql(LocalDate d) {
        return d == null ? null : Date.valueOf(d);
    }
}
