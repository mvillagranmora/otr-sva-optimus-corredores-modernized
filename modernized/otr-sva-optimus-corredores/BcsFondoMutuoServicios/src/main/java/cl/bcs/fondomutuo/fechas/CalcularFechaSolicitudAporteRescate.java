package cl.bcs.fondomutuo.fechas;

import static cl.bcs.fondomutuo.cierre.ConstantesCierre.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.fechas.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.fondomutuo.fechas.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.fondomutuo.fechas.ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises;
import cl.bcs.fondomutuo.fechas.ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Port of legacy {@code cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate} (1519 LOC; rules FM-FEC-01..14), bug for bug. Plain Java: every read goes
 * through a port, the instance holds only its ports (no request state, no static state: the date offsets are local to the call, DEV-F2).
 * Dates travel as ISO text exactly as the legacy did; the calendar arithmetic is on {@link LocalDate} (the legacy used {@code java.sql.Date} and
 * {@code Calendar} in the JVM zone: identical under the UTC the container runs with, D-FEC-6). Logging of the legacy removed.
 */
public class CalcularFechaSolicitudAporteRescate {

    private final ConfiguracionSistemaPort configuracion;
    private final EmpresaPort empresas;
    private final SeriesFondoPort series;
    private final FechasPort lecturas;
    private final CalendarioPort calendario;

    public CalcularFechaSolicitudAporteRescate(ConfiguracionSistemaPort configuracion, EmpresaPort empresas, SeriesFondoPort series,
            FechasPort lecturas, CalendarioPort calendario) {
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.series = series;
        this.lecturas = lecturas;
        this.calendario = calendario;
    }

    public static class EntradaCalcularFechaSolicitudAporteRescate{
        public Long p_IdEmpresa;
        public Long p_IdPais;
        public List<InFechas> p_SolicitudAporteRescate;

        public EntradaCalcularFechaSolicitudAporteRescate() {
            this.p_SolicitudAporteRescate = new ArrayList<InFechas>();
        }

    }

    public static class InFechas{
        public Long IdInterno;
        public String IdSerieFondo;
        public String FechaEmision;
        public String EstadoFondo;
        public String EstadoLiquidacion;
        public Long NumeroDiasRetencion;
        public Long NumeroValutaValorCuota;
        public Long NumeroValutaCertificacion;
        public Long NumeroValutaCurse;
        public String SolicitudBatch;
        public String TipoSolicitud;
        public String FondoExtranjero;
        public Long IdPaisFondo;
        public Long IdPaisEmisor;
        public Long IdPaisMoneda;
        //REINVERSIONES
        public String Switch;
        public String RescateGeneraSpotCertificacion;
        public String FechaCertificacionRescateOrigen;
        public String FechaLiquidacionRescateOrigen;
        public String FechaValorCuotaRescateOrigen;
        public String FondoNacionalDolar;
        public String IdMercadoFondoMutuo;
        public InFechas() {
            this.Switch = "N";
            this.RescateGeneraSpotCertificacion = "N";
            this.FechaCertificacionRescateOrigen = "0001-01-01";
            this.FechaLiquidacionRescateOrigen = "0001-01-01";
            this.FechaValorCuotaRescateOrigen = "0001-01-01";
            this.IdMercadoFondoMutuo = "";
        }

    }


    public static class ProcesamientoFechas{
        public Long IdInterno;
        public String IdSerieFondo;
        public String FechaInicial;
        public String FechaEmision;
        public String FechaDigitacion;
        public String FechaCurse;
        public String FechaValorCuota;
        public String FechaCertificacion;
        public String FechaLiquidacion;
        public String EstadoFondo;
        public String EstadoLiquidacion;
        public Long NumeroDiasRetencion;
        public Long NumeroValutaValorCuota;
        public Long NumeroValutaCertificacion;
        public Long NumeroValutaCurse;
        public String SolicitudBatch;
        public String TipoSolicitud;
        public String FondoExtranjero;
        public Long IdPaisFondo;
        public Long IdPaisEmisor;
        public Long IdPaisMoneda;
        public Long IdPaisLocal;
        public Long IdPaisEmpresa;
        public Long IdPaisEmisorSerieFondo;
        //REINVERSIONES
        public String Switch;
        public String FechaCertificacionRescateOrigen;
        public String FechaLiquidacionRescateOrigen;
        public String FechaValorCuotaRescateOrigen;
        public String RescateGeneraSpotCertificacion;
        public Boolean PermiteDiaNoHabilFechaValorCuota;
        public Map<String, ConfiguracionFechaDias> ConfiguracionFechas;
        public String FondoNacionalDolar;
        public String IdMercadoFondoMutuo;
        public String MoneyMarket;
        public Boolean RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket;

        public ProcesamientoFechas() {
            this.ConfiguracionFechas = new HashMap<String, ConfiguracionFechaDias>();
        }

    }

    public static class CopiarProcesamientoFechas{
        public static ProcesamientoFechas copiarObjeto(ProcesamientoFechas original) {

            if(original == null) {
                return new ProcesamientoFechas();
            }

            ProcesamientoFechas copiaPFechas = new ProcesamientoFechas();

            copiaPFechas.IdInterno = (original.IdInterno != null) ? original.IdInterno : 0L;
            copiaPFechas.IdSerieFondo = (original.IdSerieFondo != null) ? original.IdSerieFondo : "";
            copiaPFechas.FechaInicial = (original.FechaInicial != null) ? original.FechaInicial : "";
            copiaPFechas.FechaEmision = (original.FechaEmision != null) ? original.FechaEmision : "";
            copiaPFechas.FechaDigitacion = (original.FechaDigitacion != null) ? original.FechaDigitacion : "";
            copiaPFechas.FechaCurse = (original.FechaCurse != null) ? original.FechaCurse : "";
            copiaPFechas.FechaValorCuota = (original.FechaValorCuota != null) ? original.FechaValorCuota : "";
            copiaPFechas.FechaCertificacion = (original.FechaCertificacion != null) ? original.FechaCertificacion : "";
            copiaPFechas.FechaLiquidacion = (original.FechaLiquidacion != null) ? original.FechaLiquidacion : "";
            copiaPFechas.EstadoFondo = (original.EstadoFondo != null) ? original.EstadoFondo : "";
            copiaPFechas.EstadoLiquidacion = (original.EstadoLiquidacion != null) ? original.EstadoLiquidacion : "";
            copiaPFechas.NumeroDiasRetencion = (original.NumeroDiasRetencion != null) ? original.NumeroDiasRetencion : 0L;
            copiaPFechas.NumeroValutaValorCuota = (original.NumeroValutaValorCuota != null) ? original.NumeroValutaValorCuota : 0L;
            copiaPFechas.NumeroValutaCertificacion = (original.NumeroValutaCertificacion != null) ? original.NumeroValutaCertificacion : 0L;
            copiaPFechas.NumeroValutaCurse = (original.NumeroValutaCurse != null) ? original.NumeroValutaCurse : 0L;
            copiaPFechas.SolicitudBatch = (original.SolicitudBatch != null) ? original.SolicitudBatch : "";
            copiaPFechas.TipoSolicitud = (original.TipoSolicitud != null) ? original.TipoSolicitud : "";
            copiaPFechas.FondoExtranjero = (original.FondoExtranjero != null) ? original.FondoExtranjero : "";
            copiaPFechas.IdPaisFondo = (original.IdPaisFondo != null) ? original.IdPaisFondo : 0L;
            copiaPFechas.IdPaisEmisor = (original.IdPaisEmisor != null) ? original.IdPaisEmisor : 0L;
            copiaPFechas.IdPaisMoneda = (original.IdPaisMoneda != null) ? original.IdPaisMoneda : 0L;
            copiaPFechas.IdPaisLocal = (original.IdPaisLocal != null) ? original.IdPaisLocal : 0L;
            copiaPFechas.IdPaisEmpresa = (original.IdPaisEmpresa != null) ? original.IdPaisEmpresa : 0L;
            copiaPFechas.IdPaisEmisorSerieFondo = (original.IdPaisEmisorSerieFondo != null) ? original.IdPaisEmisorSerieFondo : 0L;
            copiaPFechas.Switch = (original.Switch != null) ? original.Switch : "";
            copiaPFechas.FechaCertificacionRescateOrigen = (original.FechaCertificacionRescateOrigen != null) ? original.FechaCertificacionRescateOrigen : "";
            copiaPFechas.FechaLiquidacionRescateOrigen = (original.FechaLiquidacionRescateOrigen != null) ? original.FechaLiquidacionRescateOrigen : "";
            copiaPFechas.FechaValorCuotaRescateOrigen = (original.FechaValorCuotaRescateOrigen != null) ? original.FechaValorCuotaRescateOrigen : "";
            copiaPFechas.RescateGeneraSpotCertificacion = (original.RescateGeneraSpotCertificacion != null) ? original.RescateGeneraSpotCertificacion : "";
            copiaPFechas.PermiteDiaNoHabilFechaValorCuota = (original.PermiteDiaNoHabilFechaValorCuota != null) ? original.PermiteDiaNoHabilFechaValorCuota : false;
            copiaPFechas.FondoNacionalDolar = (original.FondoNacionalDolar != null) ? original.FondoNacionalDolar : "";
            copiaPFechas.IdMercadoFondoMutuo = (original.IdMercadoFondoMutuo != null) ? original.IdMercadoFondoMutuo : "";
            copiaPFechas.MoneyMarket = (original.MoneyMarket != null) ? original.MoneyMarket : "";
            copiaPFechas.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket = (original.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket != null) ? original.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket : false;

            copiaPFechas.ConfiguracionFechas = new HashMap<>();

            if(original.ConfiguracionFechas != null) {
                for (Map.Entry<String, ConfiguracionFechaDias> ConfigFechaDiasOriginal : original.ConfiguracionFechas.entrySet()) {
                    ConfiguracionFechaDias copiaConfigFechaDias = new ConfiguracionFechaDias();
                    copiaConfigFechaDias.CantidadDiasValuta = ConfigFechaDiasOriginal.getValue().CantidadDiasValuta;
                    copiaConfigFechaDias.EsHabil = (ConfigFechaDiasOriginal.getValue().EsHabil != null) ? ConfigFechaDiasOriginal.getValue().EsHabil : "";
                    copiaConfigFechaDias.IdTipoFechaOrigen = (ConfigFechaDiasOriginal.getValue().IdTipoFechaOrigen != null) ? ConfigFechaDiasOriginal.getValue().IdTipoFechaOrigen : "";
                    copiaConfigFechaDias.DebeSerHabilFechaOrigen = (ConfigFechaDiasOriginal.getValue().DebeSerHabilFechaOrigen != null) ? ConfigFechaDiasOriginal.getValue().DebeSerHabilFechaOrigen : "";
                    copiaConfigFechaDias.IdPaisFondo = ConfigFechaDiasOriginal.getValue().IdPaisFondo;
                    copiaConfigFechaDias.IdPaisEmisor = ConfigFechaDiasOriginal.getValue().IdPaisEmisor;
                    copiaConfigFechaDias.IdPaisMoneda = ConfigFechaDiasOriginal.getValue().IdPaisMoneda;
                    copiaConfigFechaDias.IdPaisLocal = ConfigFechaDiasOriginal.getValue().IdPaisLocal;
                    copiaConfigFechaDias.IdPaisEmpresa = ConfigFechaDiasOriginal.getValue().IdPaisEmpresa;
                    copiaConfigFechaDias.paisCalculo = ConfigFechaDiasOriginal.getValue().paisCalculo;
                    copiaPFechas.ConfiguracionFechas.put(ConfigFechaDiasOriginal.getKey(), copiaConfigFechaDias);
                }
            }

            return copiaPFechas;
        }
    }

    public static List<ProcesamientoFechas> CopiarListaProcesamientoFechas(List<ProcesamientoFechas> PFechasListaOriginal) {

        if (PFechasListaOriginal == null) {
            return new ArrayList<>();
        }

        List<ProcesamientoFechas> copiaPFechasLista = new ArrayList<>();

        for (ProcesamientoFechas PFechasListaOriginalElementos : PFechasListaOriginal) {
            if (PFechasListaOriginalElementos != null) {
                copiaPFechasLista.add(CopiarProcesamientoFechas.copiarObjeto(PFechasListaOriginalElementos));
            } else {
                copiaPFechasLista.add(new ProcesamientoFechas());
            }
        }

        return copiaPFechasLista;
    }

    public static class ConfiguracionFechaFFMM {
        public String IdModalidad;
        public String IdTipoFecha;
        public String TipoSolicitud;
        public String EstadoFondo;
        public String FondoExtranjero;
        public String Descripcion;
        public Long CantidadDias;
        public String EsHabil;
        public String IdTipoFechaOrigen;
        public String DebeSerHabilFechaOrigen;
        public String ValutaCurse;
        public String ValutaValorCuota;
        public String ValutaCertificacion;
        public String ValutaRetencion;
        public String CalendarioLocal;
        public String CalendarioEmisor;
        public String CalendarioFondo;
        public String CalendarioMoneda;
        public String IdEstado;

    }

    public static class ConfiguracionFechaDias {

        public long CantidadDiasValuta;
        public String EsHabil;
        public String IdTipoFechaOrigen;
        public String DebeSerHabilFechaOrigen;
        public long IdPaisFondo;
        public long IdPaisEmisor;
        public long IdPaisMoneda;
        public long IdPaisLocal;
        public long IdPaisEmpresa;
        public long paisCalculo;

    }

    public static class SalidaCalcularFechaSolicitudAporteRescate{
        public List <ResultadoFecha> P_FECHASAPORTERESCATE;

        public SalidaCalcularFechaSolicitudAporteRescate(){
            this.P_FECHASAPORTERESCATE = new ArrayList<ResultadoFecha>();
        }

    }

    public static class ResultadoFecha{
        public Long IdInterno;
        public String IdSerieFondo;
        public LocalDate FechaEmision;
        public LocalDate FechaDigitacion;
        public LocalDate FechaAceptacion;
        public LocalDate FechaValorCuota;
        public LocalDate FechaCertificacion;
        public LocalDate FechaLiquidacion;

    }

    public static class FechaCalculadaFondoMutuo{
        HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasDigitacion;
        HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasCurse;
        HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasValorCuota;
        HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasCertificacion;
        HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> FechasRetencion;

    }

    public static class DiaFeriado{
        public LocalDate Fecha;
        public Long	IdPais;

    }


    public SalidaCalcularFechaSolicitudAporteRescate logica(EntradaCalcularFechaSolicitudAporteRescate entrada) throws Exception{
        SalidaCalcularFechaSolicitudAporteRescate out = new SalidaCalcularFechaSolicitudAporteRescate();
        Long idPaisEmpresa = obtenerIdPaisEmpresa(entrada.p_IdEmpresa);
        try{
            List<ProcesamientoFechas> solicitudesAProcesar = obtenerSolicitudesAProcesar(entrada, entrada.p_SolicitudAporteRescate, idPaisEmpresa);
            List<ProcesamientoFechas> solicitudesAProcesarSwitch = obtenerSolicitudesSwitch(solicitudesAProcesar);
            List<ProcesamientoFechas> solicitudesAProcesarNoSwitch = obtenerSolicitudesNoSwitch(solicitudesAProcesar);

            if(solicitudesAProcesarSwitch.size() > 0) {
              	solicitudesAProcesarSwitch = setFechasSolicitudesSwitch(solicitudesAProcesarSwitch);
            }

            if(solicitudesAProcesarNoSwitch.size() > 0) {
                solicitudesAProcesarNoSwitch = setConfiguracionesSolicitudesNoSwitchAProcesar(entrada.p_IdEmpresa, solicitudesAProcesarNoSwitch);
                solicitudesAProcesarNoSwitch = setFechasSolicitudesNoSwitch(solicitudesAProcesarNoSwitch);
                solicitudesAProcesarNoSwitch = repasarFechaCertificacionLiquidacionSeriesExtranjeras(solicitudesAProcesarNoSwitch, idPaisEmpresa);
                solicitudesAProcesarNoSwitch = repasarFechaCertificacionRescateGeneraSpot(solicitudesAProcesarNoSwitch, idPaisEmpresa);
                solicitudesAProcesarNoSwitch = repasarFechaFondoNacionalDolar(solicitudesAProcesarNoSwitch, idPaisEmpresa);
            }

            List<ProcesamientoFechas> solicitudesFinales = new ArrayList<ProcesamientoFechas>();
            solicitudesFinales.addAll(solicitudesAProcesarSwitch);
            solicitudesFinales.addAll(solicitudesAProcesarNoSwitch);

            out.P_FECHASAPORTERESCATE = obtenerResultadoFechas(solicitudesFinales);
        }catch(Exception e){
            throw e;
        }

        return out;
    }

    private List<ProcesamientoFechas> repasarFechaFondoNacionalDolar(List<ProcesamientoFechas> solicitudesAProcesarNoSwitch, Long idPaisEmpresa) throws Exception {

        List<ProcesamientoFechas> solicitudesAConsultar = solicitudesAProcesarNoSwitch.stream().filter(solicitud-> "S".equals(solicitud.FondoNacionalDolar)).collect(Collectors.toList());

        if(solicitudesAConsultar.size() > 0) {

            List<ProcesamientoFechas> fechasSinProcesar = CopiarListaProcesamientoFechas(solicitudesAProcesarNoSwitch);

            //para fecha curse
            Map<String,DiaFeriado> feriadosCurse = obtenerFeriadosFondosNacionalesUSD(solicitudesAConsultar, idPaisEmpresa);
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCurse = obtenerFechaAConsultarCurse(solicitudesAProcesarNoSwitch,feriadosCurse,idPaisEmpresa);
            if(fechasAConsultarCurse.size() > 0) {
                List<Long> idsInternos = solicitudesAConsultar.stream().map(solicitud -> solicitud.IdInterno).collect(Collectors.toList());
                HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(fechasAConsultarCurse, idPaisEmpresa);
                solicitudesAProcesarNoSwitch.stream().filter(solicitud-> idsInternos.contains(solicitud.IdInterno)).forEach(solicitud->{
                    if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
                        solicitud.FechaCurse = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
                    }
                });
            }
            //para fecha certificacion
            Map<String,DiaFeriado> feriadosCertificacion = obtenerFeriadosFondosNacionalesUSD(solicitudesAConsultar, idPaisEmpresa);
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = obtenerFechaAConsultarCertificacion(solicitudesAProcesarNoSwitch,feriadosCertificacion,feriadosCurse,idPaisEmpresa,fechasSinProcesar);
            if(fechasAConsultarCertificacion.size() > 0) {
                List<Long> idsInternos = solicitudesAConsultar.stream().map(solicitud -> solicitud.IdInterno).collect(Collectors.toList());
                HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(fechasAConsultarCertificacion, idPaisEmpresa);
                solicitudesAProcesarNoSwitch.stream().filter(solicitud-> idsInternos.contains(solicitud.IdInterno)).forEach(solicitud->{
                    if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
                        solicitud.FechaCertificacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
                    }
                });
            }
            //para fecha pago/liquidacion
            Map<String,DiaFeriado> feriadosPago = obtenerFeriadosFondosNacionalesUSD(solicitudesAConsultar, idPaisEmpresa);
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarPago = obtenerFechaAConsultarLiquidacion(solicitudesAProcesarNoSwitch,feriadosPago,feriadosCurse,idPaisEmpresa,fechasSinProcesar);
            if(fechasAConsultarPago.size() > 0) {
                List<Long> idsInternos = solicitudesAConsultar.stream().map(solicitud -> solicitud.IdInterno).collect(Collectors.toList());
                HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(fechasAConsultarPago, idPaisEmpresa);
                solicitudesAProcesarNoSwitch.stream().filter(solicitud-> idsInternos.contains(solicitud.IdInterno)).forEach(solicitud->{
                    if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
                        solicitud.FechaLiquidacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
                    }
                });
            }

            solicitudesAProcesarNoSwitch = obtenerFechaValorCuotaRepaso(solicitudesAProcesarNoSwitch);

        }

        return solicitudesAProcesarNoSwitch;
    }

    private Long obtenerIdPaisEmpresa(Long idEmpresa) throws Exception {
        InformacionEmpresa empresa = empresas.informacion(idEmpresa);
        Long idPaisEmpresa = empresa.IdPais;
        return idPaisEmpresa;
    }

    private List<ProcesamientoFechas> obtenerSolicitudesAProcesar(EntradaCalcularFechaSolicitudAporteRescate entrada, List<InFechas> solicitudes, Long idPaisEmpresa) throws Exception {
        Map<String,SerieFondoSolicitudes> mapSeriesFondo = obtenerSeriesFondos(entrada);
        String permiteDiaNoHabilFechaValorCuotaMoneyMarket = obtenerPermiteDiaNoHabilFechaValorCuotaMoneyMarket(entrada.p_IdEmpresa);
        List<ProcesamientoFechas> solicitudesAProcesar = new ArrayList<ProcesamientoFechas>();
        for (InFechas solicitud : solicitudes) {
            ProcesamientoFechas fechaProceso = new ProcesamientoFechas();
            SerieFondoSolicitudes serieFondo = mapSeriesFondo.get(solicitud.IdSerieFondo);
            fechaProceso.IdInterno = solicitud.IdInterno;
            fechaProceso.IdSerieFondo = solicitud.IdSerieFondo;
            fechaProceso.FechaEmision = solicitud.FechaEmision;
            fechaProceso.EstadoFondo = solicitud.EstadoFondo;
            fechaProceso.EstadoLiquidacion = solicitud.EstadoLiquidacion;
            fechaProceso.NumeroDiasRetencion = (solicitud.NumeroDiasRetencion  != null ? solicitud.NumeroDiasRetencion : 0L);
            fechaProceso.NumeroValutaValorCuota = (solicitud.NumeroValutaValorCuota  != null ? solicitud.NumeroValutaValorCuota : 0L);
            fechaProceso.NumeroValutaCertificacion = (solicitud.NumeroValutaCertificacion  != null ? solicitud.NumeroValutaCertificacion : 0L);
            fechaProceso.NumeroValutaCurse = (solicitud.NumeroValutaCurse != null ? solicitud.NumeroValutaCurse : 0L);
            fechaProceso.SolicitudBatch = solicitud.SolicitudBatch;
            fechaProceso.TipoSolicitud = solicitud.TipoSolicitud;
            fechaProceso.FondoExtranjero = solicitud.FondoExtranjero;
            fechaProceso.IdPaisFondo = (solicitud.IdPaisFondo  != null ? solicitud.IdPaisFondo : 0L);
            fechaProceso.IdPaisEmisor = (solicitud.IdPaisEmisor != null ? solicitud.IdPaisEmisor : 0L);
            fechaProceso.IdPaisMoneda = (solicitud.IdPaisMoneda != null ? solicitud.IdPaisMoneda : 0L);
            fechaProceso.IdPaisLocal = (solicitud.IdPaisEmisor != null ? solicitud.IdPaisEmisor : 0L);
            fechaProceso.IdPaisEmpresa = idPaisEmpresa;
            fechaProceso.IdPaisEmisorSerieFondo = serieFondo != null ? serieFondo.IdPaisEmisor : 0L;
            fechaProceso.PermiteDiaNoHabilFechaValorCuota = permiteDiaNoHabilFechaValorCuota(serieFondo, fechaProceso.TipoSolicitud, permiteDiaNoHabilFechaValorCuotaMoneyMarket);
            fechaProceso.RescateGeneraSpotCertificacion = solicitud.RescateGeneraSpotCertificacion;
            fechaProceso.Switch = solicitud.Switch;
            fechaProceso.FechaCertificacionRescateOrigen = solicitud.FechaCertificacionRescateOrigen;
            fechaProceso.FechaLiquidacionRescateOrigen = solicitud.FechaLiquidacionRescateOrigen;
            fechaProceso.FechaValorCuotaRescateOrigen = solicitud.FechaValorCuotaRescateOrigen;
            fechaProceso.FondoNacionalDolar = solicitud.FondoNacionalDolar;
            fechaProceso.IdMercadoFondoMutuo = solicitud.IdMercadoFondoMutuo;
            fechaProceso.MoneyMarket = serieFondo != null ? serieFondo.IndicadorMoneyMarket : "N";
            fechaProceso.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket = repasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket(serieFondo, permiteDiaNoHabilFechaValorCuotaMoneyMarket);
            solicitudesAProcesar.add(fechaProceso);
        }
        return solicitudesAProcesar;
    }

    private List<ProcesamientoFechas> obtenerSolicitudesSwitch(List<ProcesamientoFechas> solicitudesAProcesar){
        List<ProcesamientoFechas> solicitudesAProcesarSwitch = solicitudesAProcesar.stream().filter(solicitud->SI.equals(solicitud.Switch)).collect(Collectors.toList());
        return solicitudesAProcesarSwitch;
    }

    private List<ProcesamientoFechas> obtenerSolicitudesNoSwitch(List<ProcesamientoFechas> solicitudesAProcesar){
        List<ProcesamientoFechas> solicitudesAProcesarSwitch = solicitudesAProcesar.stream().filter(solicitud->NO.equals(solicitud.Switch)).collect(Collectors.toList());
        return solicitudesAProcesarSwitch;

    }

    private List<ProcesamientoFechas> setFechasSolicitudesSwitch(List<ProcesamientoFechas> solicitudesAProcesar){
        solicitudesAProcesar.stream().forEach(solicitud->{
            solicitud.FechaDigitacion = solicitud.FechaCertificacionRescateOrigen;
            solicitud.FechaEmision = solicitud.FechaCertificacionRescateOrigen;
            solicitud.FechaCurse = solicitud.FechaCertificacionRescateOrigen;
            solicitud.FechaValorCuota = solicitud.FechaValorCuotaRescateOrigen;
            solicitud.FechaCertificacion = solicitud.FechaCertificacionRescateOrigen;
            solicitud.FechaLiquidacion = solicitud.FechaLiquidacionRescateOrigen;
        });
        return solicitudesAProcesar;
    }

    private Map<String,SerieFondoSolicitudes> obtenerSeriesFondos(EntradaCalcularFechaSolicitudAporteRescate entrada){
        List<String> idSeriesFondo = entrada.p_SolicitudAporteRescate.stream().map(solicitud->solicitud.IdSerieFondo).distinct().collect(Collectors.toList());
        List<SerieFondoSolicitudes> seriesFondo = series.series(entrada.p_IdEmpresa, idSeriesFondo);
        Map<String,SerieFondoSolicitudes> mapSeriesFondo = seriesFondo.stream().collect(Collectors.toMap(serie->serie.IdSerieFondo, serie->serie));
        return mapSeriesFondo;
    }

    private String obtenerPermiteDiaNoHabilFechaValorCuotaMoneyMarket(Long idEmpresa) throws Exception {
        String permiteDiaNoHabilFechaValorCuotaMoneyMarket = NO;
        try {
            permiteDiaNoHabilFechaValorCuotaMoneyMarket = configuracion.texto(idEmpresa, "FONDOMUTUO", "PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET");
        }catch(Exception e) {
        }

        return permiteDiaNoHabilFechaValorCuotaMoneyMarket;
    }

    private Boolean permiteDiaNoHabilFechaValorCuota(SerieFondoSolicitudes serieFondo, String tipoSolicitud, String permiteDiaNoHabilFechaValorCuotaMoneyMarket) throws Exception {
        Boolean permiteDiaNoHabilFechaValorCuota = SI.equals(permiteDiaNoHabilFechaValorCuotaMoneyMarket);
        Boolean esFechaMoneyMarket = false;
        try {
            if(TIPO_SOLICITUD_APORTE.equals(tipoSolicitud)) {
                esFechaMoneyMarket = esFechaMoneyMarketAporte(serieFondo);
            }else if(TIPO_SOLICITUD_RESCATE.equals(tipoSolicitud)) {
                esFechaMoneyMarket = esFechaMoneyMarketRescate(serieFondo);
            }else {
                throw new Exception("Tipo Solicitud no valida.");
            }
        }catch(Exception e) {
            throw e;
        }
        Boolean resultado = esFechaMoneyMarket && permiteDiaNoHabilFechaValorCuota;
        return resultado;
    }

    private Boolean esFechaMoneyMarketAporte(SerieFondoSolicitudes serieFondo) {
        Boolean esFechaMoneyMarketAporte = SI.equals(serieFondo.MoneyMarket);
        return esFechaMoneyMarketAporte;
    }

    private Boolean esFechaMoneyMarketRescate(SerieFondoSolicitudes serieFondo) {
        Boolean esFechaMoneyMarketRescate = SI.equals(serieFondo.MoneyMarket) || MERCADO_RF.equals(serieFondo.IdMercadoFondoMutuo);
        return esFechaMoneyMarketRescate;
    }

    private List<ProcesamientoFechas> setConfiguracionesSolicitudesNoSwitchAProcesar(Long idEmpresa, List<ProcesamientoFechas> solicitudesAProcesar) throws Exception{
        String modalidadSolicitud = obtenerModalidadSolicitudFechaAporteRescate(idEmpresa);
        List<ConfiguracionFechaFFMM> configuracionFechaFFMM = obtenerConfiguracionFechaFondoMutuo(idEmpresa);
        solicitudesAProcesar.stream().forEach(solicitud->{
            for (ConfiguracionFechaFFMM configuracionFechaFondoMutuo : configuracionFechaFFMM){
                if (esConfiguracionValida(configuracionFechaFondoMutuo, solicitud, modalidadSolicitud)){
                    ConfiguracionFechaDias configuracionFechaDias = new ConfiguracionFechaDias();
                    configuracionFechaDias.CantidadDiasValuta = obtenerValuta(configuracionFechaFondoMutuo, solicitud);
                    configuracionFechaDias.EsHabil = configuracionFechaFondoMutuo.EsHabil;
                    configuracionFechaDias.IdTipoFechaOrigen = configuracionFechaFondoMutuo.IdTipoFechaOrigen;
                    configuracionFechaDias.DebeSerHabilFechaOrigen = configuracionFechaFondoMutuo.DebeSerHabilFechaOrigen;
                    configuracionFechaDias.IdPaisEmpresa = solicitud.IdPaisEmpresa;
                    configuracionFechaDias.IdPaisFondo = obtenerIdPaisFondo(configuracionFechaFondoMutuo, solicitud);
                    configuracionFechaDias.IdPaisEmisor = obtenerIdPaisEmisor(configuracionFechaFondoMutuo, solicitud);
                    configuracionFechaDias.IdPaisMoneda = obtenerIdPaisMoneda(configuracionFechaFondoMutuo, solicitud);
                    configuracionFechaDias.IdPaisLocal = obtenerIdPaisLocal(configuracionFechaFondoMutuo, solicitud);
                    solicitud.ConfiguracionFechas.put(configuracionFechaFondoMutuo.IdTipoFecha, configuracionFechaDias);
                }
            }
        });
        return solicitudesAProcesar;
    }

    private String obtenerModalidadSolicitudFechaAporteRescate(Long idEmpresa) throws Exception {
        String modalidadSolicitud = "";
        try {
            modalidadSolicitud = configuracion.texto(idEmpresa, "FONDOMUTUO", "MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE");
        }catch(Exception e) {
        }

        return modalidadSolicitud;
    }

    private List<ConfiguracionFechaFFMM> obtenerConfiguracionFechaFondoMutuo(Long idEmpresa){
        List<ConfiguracionFechaFFMM> configuracionFechaFondoMutuo = lecturas.configuracionFechas(idEmpresa);
        return configuracionFechaFondoMutuo;
    }

    private Boolean esConfiguracionValida(ConfiguracionFechaFFMM configuracionFechaFondoMutuo, ProcesamientoFechas solicitud, String modalidadSolicitud) {
        Boolean esConfiguracionValida = configuracionCumpleRequisitosSolicitud(configuracionFechaFondoMutuo, solicitud) && configuracionCumpleRequisitosModalidad(configuracionFechaFondoMutuo, solicitud, modalidadSolicitud);
        return esConfiguracionValida;
    }

    private Boolean configuracionCumpleRequisitosSolicitud(ConfiguracionFechaFFMM configuracionFechaFondoMutuo, ProcesamientoFechas solicitud) {
        Boolean configuracionCumpleRequisitosSolicitud = esTipoSolicitudValida(configuracionFechaFondoMutuo, solicitud) && esEstadoFondoValido(configuracionFechaFondoMutuo, solicitud) && esFondoExtranjeroValido(configuracionFechaFondoMutuo, solicitud);
        return configuracionCumpleRequisitosSolicitud;
    }

    private Boolean esTipoSolicitudValida(ConfiguracionFechaFFMM configuracionFechaFondoMutuo, ProcesamientoFechas solicitud) {
        Boolean esTipoSolicitudValida = configuracionFechaFondoMutuo.TipoSolicitud == null || configuracionFechaFondoMutuo.TipoSolicitud.equalsIgnoreCase(solicitud.TipoSolicitud);
        return esTipoSolicitudValida;
    }

    private Boolean esEstadoFondoValido(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Boolean esEstadoFondoValido = configuracionFondoMutuo.EstadoFondo == null || configuracionFondoMutuo.EstadoFondo.equalsIgnoreCase(solicitud.EstadoFondo);
        return esEstadoFondoValido;
    }

    private Boolean esFondoExtranjeroValido(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Boolean esFondoExtranjeroValido = configuracionFondoMutuo.FondoExtranjero == null || configuracionFondoMutuo.FondoExtranjero.equalsIgnoreCase(solicitud.FondoExtranjero);
        return esFondoExtranjeroValido;
    }

    private Boolean configuracionCumpleRequisitosModalidad(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud, String modalidadSolicitud) {
        Boolean configuracionCumpleRequisitosModalidad = esModalidadValidaSolicitud(configuracionFondoMutuo, solicitud, modalidadSolicitud) || esModalidadConfiguradaEnParametro(configuracionFondoMutuo, modalidadSolicitud);
        return configuracionCumpleRequisitosModalidad;
    }

    private Boolean esModalidadValidaSolicitud(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud, String modalidadSolicitud) {
        Boolean esModalidadValidaSolicitud = esModalidadNula(modalidadSolicitud) && (esModalidadUnitario(configuracionFondoMutuo, solicitud) || esModalidadBatch(configuracionFondoMutuo, solicitud));
        return esModalidadValidaSolicitud;
    }

    private Boolean esModalidadNula(String modalidadSolicitud) {
        Boolean esModalidadNula = modalidadSolicitud == null || modalidadSolicitud.equalsIgnoreCase("");
        return esModalidadNula;
    }

    private Boolean esModalidadUnitario(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Boolean esModalidadUnitario = configuracionFondoMutuo.IdModalidad.equalsIgnoreCase("UNITARIO") && solicitud.SolicitudBatch.equalsIgnoreCase("N");
        return esModalidadUnitario;
    }

    private Boolean esModalidadBatch(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Boolean esModalidadBatch = configuracionFondoMutuo.IdModalidad.equalsIgnoreCase("BATCH") && solicitud.SolicitudBatch.equalsIgnoreCase("S");
        return esModalidadBatch;
    }

    private Boolean esModalidadConfiguradaEnParametro(ConfiguracionFechaFFMM configuracionFondoMutuo, String modalidadSolicitud) {
        Boolean esModalidadConfiguradaEnParametro = configuracionFondoMutuo.IdModalidad.equalsIgnoreCase(modalidadSolicitud);
        return esModalidadConfiguradaEnParametro;
    }

    private Long obtenerValuta(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long valuta = configuracionFondoMutuo.CantidadDias + obtenerValutaFechaCurse(configuracionFondoMutuo, solicitud) + obtenerValutaFechaCertificacion(configuracionFondoMutuo, solicitud) + obtenerValutaFechaRetencion(configuracionFondoMutuo, solicitud) + obtenerValutaFechaValorCuota(configuracionFondoMutuo, solicitud);
        return valuta;
    }

    private Long obtenerValutaFechaCurse(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long valuta = SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaCurse) ? solicitud.NumeroValutaCurse : 0L;
        return valuta;
    }

    private Long obtenerValutaFechaCertificacion(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long valuta = SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaCertificacion) ? solicitud.NumeroValutaCertificacion : 0L;
        return valuta;
    }

    private Long obtenerValutaFechaRetencion(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long valuta = SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaRetencion) ? solicitud.NumeroDiasRetencion : 0L;
        return valuta;
    }

    private Long obtenerValutaFechaValorCuota(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long valuta = SI.equalsIgnoreCase(configuracionFondoMutuo.ValutaValorCuota) ? solicitud.NumeroValutaValorCuota : 0L;
        return valuta;
    }

    private Long obtenerIdPaisFondo(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long idPaisFondo = configuracionFondoMutuo.CalendarioFondo.equalsIgnoreCase("S") ? solicitud.IdPaisFondo : solicitud.IdPaisEmpresa;
        return idPaisFondo;
    }

    private Long obtenerIdPaisEmisor(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long idPaisEmisor = configuracionFondoMutuo.CalendarioEmisor.equalsIgnoreCase("S") ? solicitud.IdPaisEmisor : solicitud.IdPaisEmpresa;
        return idPaisEmisor;
    }

    private Long obtenerIdPaisMoneda(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long idPaisMoneda = "S".equalsIgnoreCase(configuracionFondoMutuo.CalendarioMoneda) || "S".equals(solicitud.FondoNacionalDolar) ? solicitud.IdPaisMoneda : solicitud.IdPaisEmpresa;
        return idPaisMoneda;
    }

    private Long obtenerIdPaisLocal(ConfiguracionFechaFFMM configuracionFondoMutuo, ProcesamientoFechas solicitud) {
        Long idPaisLocal = configuracionFondoMutuo.CalendarioLocal.equalsIgnoreCase("S") ? solicitud.IdPaisLocal : solicitud.IdPaisEmpresa;
        return idPaisLocal;
    }

    private List<ProcesamientoFechas> setFechasSolicitudesNoSwitch(List<ProcesamientoFechas> solicitudesAProcesar) throws Exception {
        FechaCalculadaFondoMutuo fechaCalculadaFondoMutuo = new FechaCalculadaFondoMutuo();
        fechaCalculadaFondoMutuo.FechasDigitacion = obtenerFechaOperacion(solicitudesAProcesar, TIPO_FECHA_DIGITACION);
        solicitudesAProcesar = setFechaDigitacion(solicitudesAProcesar,fechaCalculadaFondoMutuo.FechasDigitacion);

        fechaCalculadaFondoMutuo.FechasCurse = obtenerFechaOperacion(solicitudesAProcesar, TIPO_FECHA_CURSE);
        solicitudesAProcesar = setFechaCurse(solicitudesAProcesar,fechaCalculadaFondoMutuo.FechasCurse);

        fechaCalculadaFondoMutuo.FechasValorCuota = obtenerFechaOperacion(solicitudesAProcesar, TIPO_FECHA_VALOR_CUOTA);
        fechaCalculadaFondoMutuo.FechasCertificacion = obtenerFechaOperacion(solicitudesAProcesar, TIPO_FECHA_CERTIFICACION);
        fechaCalculadaFondoMutuo.FechasRetencion = obtenerFechaOperacion(solicitudesAProcesar, TIPO_FECHA_RETENCION);
        solicitudesAProcesar = setFechaDependientesFechaCurse(solicitudesAProcesar,fechaCalculadaFondoMutuo);
        return solicitudesAProcesar;
    }

    private HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> obtenerFechaOperacion(List<ProcesamientoFechas> solicitudesAProcesar, String tipoFecha) throws Exception {
        ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente(calendario);
        ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
        inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
        //Obtiene la fecha con el IdPaisEmisor como preferencia
        inObtenerDiaSiguiente.p_diaHabil = SI;
        inObtenerDiaSiguiente.p_soloCaeDiaHabil = SI;
        inObtenerDiaSiguiente.p_fechas = solicitudesAProcesar.stream().map(solicitud->{
            ConfiguracionFechaDias configuracionFechaDias = solicitud.ConfiguracionFechas.get(tipoFecha);
            configuracionFechaDias.paisCalculo = (configuracionFechaDias.IdPaisEmisor > 0 ? configuracionFechaDias.IdPaisEmisor : (configuracionFechaDias.IdPaisFondo > 0 ? configuracionFechaDias.IdPaisFondo : (configuracionFechaDias.IdPaisLocal > 0 ? configuracionFechaDias.IdPaisLocal : (configuracionFechaDias.IdPaisMoneda > 0 ? configuracionFechaDias.IdPaisMoneda : configuracionFechaDias.IdPaisEmpresa))));

            ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
            fecha.fechaInicial = obtenerFechaInicial(solicitud,tipoFecha);
            fecha.numeroDias = configuracionFechaDias.CantidadDiasValuta;
            fecha.pais = configuracionFechaDias.paisCalculo;

            return fecha;
        }).collect(Collectors.toList());

        //Obtiene la fecha con el IdPaisFondo como preferencia
        inObtenerDiaSiguiente.p_fechas.addAll(solicitudesAProcesar.stream().map(solicitud->{
            ConfiguracionFechaDias configuracionFechaDias = solicitud.ConfiguracionFechas.get(tipoFecha);
            if("S".equals(solicitud.FondoNacionalDolar) && TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud)) {
                configuracionFechaDias.paisCalculo = solicitud.IdPaisMoneda;
            }else {
                configuracionFechaDias.paisCalculo = (configuracionFechaDias.IdPaisFondo > 0 ? configuracionFechaDias.IdPaisFondo : (configuracionFechaDias.IdPaisEmisor > 0 ? configuracionFechaDias.IdPaisEmisor : (configuracionFechaDias.IdPaisLocal > 0 ? configuracionFechaDias.IdPaisLocal : (configuracionFechaDias.IdPaisMoneda > 0 ? configuracionFechaDias.IdPaisMoneda : configuracionFechaDias.IdPaisEmpresa))));
            }

            ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
            fecha.fechaInicial = obtenerFechaInicial(solicitud,tipoFecha);
            fecha.numeroDias = configuracionFechaDias.CantidadDiasValuta;
            fecha.pais = configuracionFechaDias.paisCalculo;

            return fecha;
        }).collect(Collectors.toList()));

        ObtenerDiaSiguiente.OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSiguiente.logica(inObtenerDiaSiguiente);
        inObtenerDiaSiguiente.p_fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
        HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> hashFechasSalida = new HashMap<>();

        for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas)
            hashFechasSalida.put(fechaSalida.fechaInicial + fechaSalida.numeroDiasInicial + fechaSalida.idPais, fechaSalida);
        return hashFechasSalida;
    }

    private String obtenerFechaInicial(ProcesamientoFechas solicitud, String tipoFecha) {
        String fecha = "";
        switch(tipoFecha) {
            case "DIGITA" : {
                fecha = solicitud.FechaEmision;
                break;
            }
            case "CURSE": {
                fecha = solicitud.FechaDigitacion;
                break;
            }
            default: {
                fecha = solicitud.FechaCurse;
                break;
            }
        }
        return fecha;
    }

    private List<ProcesamientoFechas> setFechaDigitacion(List<ProcesamientoFechas> solicitudesAProcesar, HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechas){
        solicitudesAProcesar.stream().forEach(solicitud->{
            solicitud.FechaDigitacion = obtenerFecha(solicitud, fechas, TIPO_FECHA_DIGITACION);
        });
        return solicitudesAProcesar;
    }

    private List<ProcesamientoFechas> setFechaCurse(List<ProcesamientoFechas> solicitudesAProcesar, HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechas){
        solicitudesAProcesar.stream().forEach(solicitud->{
            solicitud.FechaCurse = obtenerFecha(solicitud, fechas, TIPO_FECHA_CURSE);
        });
        return solicitudesAProcesar;
    }

    private List<ProcesamientoFechas> setFechaDependientesFechaCurse(List<ProcesamientoFechas> solicitudesAProcesar, FechaCalculadaFondoMutuo fechaCalculadaFondoMutuo){
        solicitudesAProcesar.stream().forEach(solicitud->{
            solicitud.FechaValorCuota = obtenerFechaValorCuota(solicitud, fechaCalculadaFondoMutuo);
            solicitud.FechaCertificacion = obtenerFecha(solicitud, fechaCalculadaFondoMutuo.FechasCertificacion, TIPO_FECHA_CERTIFICACION);
            solicitud.FechaLiquidacion = obtenerFecha(solicitud, fechaCalculadaFondoMutuo.FechasRetencion, TIPO_FECHA_RETENCION);
        });
        return solicitudesAProcesar;
    }

    private String obtenerFecha(ProcesamientoFechas solicitud, HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechas, String tipoFecha) {
        ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get(tipoFecha);
        if((tipoFecha.equalsIgnoreCase("CERTIF") || tipoFecha.equalsIgnoreCase("LIQUID")) && configuracion.IdPaisEmisor != configuracion.paisCalculo) {
            String fechaEmisor ="";
            String fechaFondo = "";
            if( ("A".equalsIgnoreCase(solicitud.EstadoFondo))&&("A".equalsIgnoreCase(solicitud.EstadoLiquidacion))) {
                fechaEmisor = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?solicitud.NumeroValutaCertificacion:solicitud.NumeroDiasRetencion)+solicitud.IdPaisEmisor).fechaCalculada;
                fechaFondo = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?solicitud.NumeroValutaCertificacion:solicitud.NumeroDiasRetencion)+solicitud.IdPaisFondo).fechaCalculada;
              }else{
                 	fechaEmisor = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?(solicitud.ConfiguracionFechas.get("CERTIF").CantidadDiasValuta):(solicitud.ConfiguracionFechas.get("LIQUID").CantidadDiasValuta))+solicitud.IdPaisEmisor).fechaCalculada;
                    fechaFondo = fechas.get(solicitud.FechaCurse+(tipoFecha.equalsIgnoreCase("CERTIF")?(solicitud.ConfiguracionFechas.get("CERTIF").CantidadDiasValuta):(solicitud.ConfiguracionFechas.get("LIQUID").CantidadDiasValuta))+solicitud.IdPaisFondo).fechaCalculada;
                 	}
            if(fechaEmisor.compareTo(fechaFondo) > 0)
               configuracion.paisCalculo = solicitud.IdPaisEmisor;
        }
        FechasOut fecha = fechas.get(obtenerKeyFecha(solicitud, configuracion, tipoFecha));
        return fecha.fechaCalculada;
    }

    private String obtenerFechaValorCuota(ProcesamientoFechas solicitud, FechaCalculadaFondoMutuo fechaCalculadaFondoMutuo) {

        String fechaValorCuota = null;
        if(solicitud.PermiteDiaNoHabilFechaValorCuota) {
            FechasOut fechaCalculada = null;
            if(TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud)) {
                ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get( TIPO_FECHA_CERTIFICACION);
                HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasCertificacion = fechaCalculadaFondoMutuo.FechasCertificacion;
                fechaCalculada = fechasCertificacion.get(obtenerKeyFecha(solicitud, configuracion,  TIPO_FECHA_CERTIFICACION));
            }else {
                ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get( TIPO_FECHA_CURSE);
                HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasCertificacion = fechaCalculadaFondoMutuo.FechasCurse;
                fechaCalculada = fechasCertificacion.get(obtenerKeyFecha(solicitud, configuracion,  TIPO_FECHA_CURSE));
            }
            LocalDate fechaCalculadaConFormato = LocalDate.parse(fechaCalculada.fechaCalculada, DateTimeFormatter.ISO_LOCAL_DATE);
            fechaValorCuota = fechaCalculadaConFormato.minusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }else {
            ConfiguracionFechaDias configuracion = solicitud.ConfiguracionFechas.get( TIPO_FECHA_VALOR_CUOTA);
            if(configuracion.IdPaisEmisor != configuracion.IdPaisFondo) {
                String fechaEmisor = fechaCalculadaFondoMutuo.FechasValorCuota.get(solicitud.FechaCurse+solicitud.NumeroValutaValorCuota+solicitud.IdPaisEmisor).fechaCalculada;
                String fechaFondo = fechaCalculadaFondoMutuo.FechasValorCuota.get(solicitud.FechaCurse+solicitud.NumeroValutaValorCuota+solicitud.IdPaisFondo).fechaCalculada;
                if(fechaEmisor.compareTo(fechaFondo) > 0)
                    configuracion.paisCalculo = solicitud.IdPaisEmisor;
            }
            HashMap<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasValorCuota = fechaCalculadaFondoMutuo.FechasValorCuota;
            FechasOut fechaCalculada = fechasValorCuota.get(obtenerKeyFecha(solicitud, configuracion,  TIPO_FECHA_VALOR_CUOTA));
            fechaValorCuota = fechaCalculada.fechaCalculada;
        }

        return fechaValorCuota;
    }

    private List<ProcesamientoFechas> obtenerFechaValorCuotaRepaso(List<ProcesamientoFechas> solicitudes) throws Exception {

        for(ProcesamientoFechas solicitud : solicitudes) {
            if("S".equals(solicitud.FondoNacionalDolar)) {

                if(TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud) && !solicitud.IdMercadoFondoMutuo.equals(MERCADO_RF)) {
                    solicitud.FechaValorCuota = LocalDate.parse(solicitud.FechaCurse, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ISO_LOCAL_DATE);
                    continue;
                }

                String fechaValorCuota = solicitud.FechaValorCuota;
                String fecha = TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud) ? solicitud.FechaCertificacion : solicitud.FechaCurse;

                ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior(calendario);
                InObtenerDiaAnterior inObtenerDiaAnteriorUSA = new InObtenerDiaAnterior();
                InObtenerDiaAnterior.FechasIn fechasInUSA = new InObtenerDiaAnterior.FechasIn();

                if(solicitud.FondoNacionalDolar.equals("S") && solicitud.IdMercadoFondoMutuo.equals("RF") && solicitud.TipoSolicitud.equals("RES")) {
                    inObtenerDiaAnteriorUSA.excluirConsultarDiaFeriado = true;
                }

                fechasInUSA.fechaInicial = fecha;
                fechasInUSA.numeroDias = 1L;
                inObtenerDiaAnteriorUSA.fechas.add(fechasInUSA);
                inObtenerDiaAnteriorUSA.idPais = solicitud.IdPaisMoneda;
                inObtenerDiaAnteriorUSA.diaHabil = calculoValorDiaHabil(solicitud,fecha);
                inObtenerDiaAnteriorUSA.soloCaeDiaHabil = calculoValorSoloCaeDiaHabil(solicitud,fecha);

                OutObtenerDiaAnterior outObtenerDiaAnteriorUSA = obtenerDiaAnterior.logica(inObtenerDiaAnteriorUSA);

                InObtenerDiaAnterior inObtenerDiaAnteriorChile = new InObtenerDiaAnterior();
                InObtenerDiaAnterior.FechasIn fechasInChile = new InObtenerDiaAnterior.FechasIn();

                if(solicitud.FondoNacionalDolar.equals("S") && solicitud.IdMercadoFondoMutuo.equals("RF") && solicitud.TipoSolicitud.equals("RES")) {
                    inObtenerDiaAnteriorChile.excluirConsultarDiaFeriado = true;
                }

                fechasInChile.fechaInicial = fecha;
                fechasInChile.numeroDias = 1L;
                inObtenerDiaAnteriorChile.fechas.add(fechasInChile);
                inObtenerDiaAnteriorChile.idPais = solicitud.IdPaisFondo;
                inObtenerDiaAnteriorChile.diaHabil = calculoValorDiaHabil(solicitud,fecha);//
                inObtenerDiaAnteriorChile.soloCaeDiaHabil = calculoValorSoloCaeDiaHabil(solicitud,fecha);

                OutObtenerDiaAnterior outObtenerDiaAnteriorChile = obtenerDiaAnterior.logica(inObtenerDiaAnteriorChile);

                if(solicitud.PermiteDiaNoHabilFechaValorCuota) {
                    LocalDate fUSA = FechaLegacy.aFecha(outObtenerDiaAnteriorUSA.fechas.get(0).fechaCalculada);
                    LocalDate fChile = FechaLegacy.aFecha(outObtenerDiaAnteriorChile.fechas.get(0).fechaCalculada);
                    fechaValorCuota =  fUSA.compareTo(fChile) > 0 ? FechaLegacy.aTexto(fUSA) : FechaLegacy.aTexto(fChile);
                }
                if(solicitud.RepasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket && TIPO_SOLICITUD_APORTE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
                    fechaValorCuota = solicitud.FechaCurse;
                }

                solicitud.FechaValorCuota = fechaValorCuota;
            }
        }

        return solicitudes;
    }

    private String calculoValorDiaHabil(ProcesamientoFechas solicitud, String fecha) {

        String diaHabil = "N";

        Long day = FechaLegacy.diaDeSemanaCalendar(FechaLegacy.aFecha(fecha));

        if(!day.equals(2L)) {
            diaHabil = TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud) || fechaCaeDiaLunes(fecha, solicitud.TipoSolicitud, solicitud.MoneyMarket) ? "S" : "N";

        } else {
            diaHabil = (TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud) && !MERCADO_RF.equals(solicitud.IdMercadoFondoMutuo)) || fechaCaeDiaLunes(fecha, solicitud.TipoSolicitud, solicitud.MoneyMarket) ? "S" : "N";
        }

        return diaHabil;
    }

    private String calculoValorSoloCaeDiaHabil(ProcesamientoFechas solicitud, String fecha) {
        String soloCaeDiaHabil = "N";

        if(TIPO_SOLICITUD_APORTE.equals(solicitud.TipoSolicitud)) {
            soloCaeDiaHabil = fechaCaeDiaLunes(fecha, solicitud.TipoSolicitud, solicitud.MoneyMarket) ? "S" : "N";
            return soloCaeDiaHabil;
        }

        if(TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud)) {
            Long day = FechaLegacy.diaDeSemanaCalendar(FechaLegacy.aFecha(fecha));
            soloCaeDiaHabil = day.equals(2L) && (SI.equals(solicitud.MoneyMarket) || MERCADO_RF.equals(solicitud.IdMercadoFondoMutuo)) ? "N" : "S";
        }

        return soloCaeDiaHabil;
    }

    private Boolean fechaCaeDiaLunes(String fecha, String tipoSolicitud, String moneyMarket) {
        Boolean caeDiaLunes = false;

        Long day = FechaLegacy.diaDeSemanaCalendar(FechaLegacy.aFecha(fecha));
        caeDiaLunes = day.equals(2L) && TIPO_SOLICITUD_APORTE.equals(tipoSolicitud) && "N".equals(moneyMarket);

        return caeDiaLunes;
    }

    private String obtenerKeyFecha(ProcesamientoFechas solicitud, ConfiguracionFechaDias configuracion, String tipoFecha) {
        String key = obtenerFechaInicial(solicitud, tipoFecha) + configuracion.CantidadDiasValuta + configuracion.paisCalculo;
        return key;
    }

    private List<ProcesamientoFechas> repasarFechaCertificacionRescateGeneraSpot(List<ProcesamientoFechas> solicitudesAProcesarNoSwitch, Long idPaisEmpresa) throws Exception{
        List<ProcesamientoFechas> solicitudesAConsultar = solicitudesAProcesarNoSwitch.stream().filter(solicitud->!solicitud.IdPaisEmpresa.equals(solicitud.IdPaisEmisorSerieFondo) || !solicitud.IdPaisEmpresa.equals(solicitud.IdPaisFondo)).collect(Collectors.toList());

        //Se calcula el dia limite de retroceso de la fecha de certificacion (t+2)
        ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente(calendario);
        InObtenerDiaSiguiente diaSiguienteIn = new InObtenerDiaSiguiente();
        diaSiguienteIn.p_diaHabil = SI;
        diaSiguienteIn.p_idPais = idPaisEmpresa;

        //Se calcula dia anterior si corresponde
        ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior(calendario);
        InObtenerDiaAnterior diaAnteriorIn = new InObtenerDiaAnterior();
        diaAnteriorIn.idPais = idPaisEmpresa;
        diaAnteriorIn.diaHabil = SI;

        HashMap<String,OutObtenerDiaAnterior.FechasOut> hashDiaAnteriorLiquidacion = new HashMap<String,OutObtenerDiaAnterior.FechasOut>();
        HashMap<String,OutObtenerDiaSiguiente.FechasOut> hashDosDiasSiguientesEmision = new HashMap<String,OutObtenerDiaSiguiente.FechasOut>();

        List<ProcesamientoFechas> solicitudesFiltradas = new ArrayList<ProcesamientoFechas>();

        for(ProcesamientoFechas sol : solicitudesAConsultar) {
            if(SI.equalsIgnoreCase(sol.RescateGeneraSpotCertificacion)) {
                InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
                fecha.fechaInicial = sol.FechaLiquidacion;
                fecha.numeroDias = 1L;
                diaAnteriorIn.fechas.add(fecha);
                hashDiaAnteriorLiquidacion.put(fecha.fechaInicial, new OutObtenerDiaAnterior.FechasOut());

                InObtenerDiaSiguiente.FechasIn fechaSig = new InObtenerDiaSiguiente.FechasIn();
                fechaSig.fechaInicial = sol.FechaEmision;
                fechaSig.numeroDias = 2L;
                fechaSig.pais = idPaisEmpresa;
                diaSiguienteIn.p_fechas.add(fechaSig);
                hashDosDiasSiguientesEmision.put(fechaSig.fechaInicial, new OutObtenerDiaSiguiente.FechasOut());
                solicitudesFiltradas.add(sol);
            }
        }
        if(diaAnteriorIn.fechas.size() > 0) {
            OutObtenerDiaAnterior diaAnteriorOut = obtenerDiaAnterior.logica(diaAnteriorIn);

            for(OutObtenerDiaAnterior.FechasOut fecha : diaAnteriorOut.fechas) {
                hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).fechaCalculada = fecha.fechaCalculada;
                hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).fechaInicial = fecha.fechaInicial;
                hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).numeroDias = fecha.numeroDias;
                hashDiaAnteriorLiquidacion.get(fecha.fechaInicial).numeroDiasInicial = fecha.numeroDiasInicial;
            }

            OutObtenerDiaSiguiente diaSiguienteOut2 = obtenerDiaSiguiente.logica(diaSiguienteIn);

            for(OutObtenerDiaSiguiente.FechasOut fecha : diaSiguienteOut2.p_out_fechas) {
                hashDosDiasSiguientesEmision.get(fecha.fechaInicial).fechaCalculada = fecha.fechaCalculada;
                hashDosDiasSiguientesEmision.get(fecha.fechaInicial).fechaInicial = fecha.fechaInicial;
                hashDosDiasSiguientesEmision.get(fecha.fechaInicial).numeroDias = fecha.numeroDias;
                hashDosDiasSiguientesEmision.get(fecha.fechaInicial).numeroDiasInicial = fecha.numeroDiasInicial;
            }

            for(ProcesamientoFechas sol : solicitudesFiltradas) {
                if(hashDiaAnteriorLiquidacion.containsKey(sol.FechaLiquidacion)
                    && sol.FechaLiquidacion.equalsIgnoreCase(hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaInicial)
                    && ((sol.FechaCertificacion.compareTo(hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaCalculada) < 0)
                    || sol.FechaCertificacion.equalsIgnoreCase(sol.FechaLiquidacion))) {
                    solicitudesAConsultar.get(solicitudesAConsultar.indexOf(sol)).FechaCertificacion = (hashDosDiasSiguientesEmision.get(sol.FechaEmision).fechaCalculada.compareTo(hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaCalculada) <= 0) ? hashDiaAnteriorLiquidacion.get(sol.FechaLiquidacion).fechaCalculada : sol.FechaLiquidacion;
                }
            }
        }

        return solicitudesAProcesarNoSwitch;
    }

    private List<ProcesamientoFechas> repasarFechaCertificacionLiquidacionSeriesExtranjeras(List<ProcesamientoFechas> solicitudesAProcesarNoSwitch, Long idPaisEmpresa) throws Exception{
        List<ProcesamientoFechas> solicitudesAConsultar = solicitudesAProcesarNoSwitch.stream().filter(solicitud->esSerieExtranjera(solicitud.IdPaisEmpresa, solicitud.IdPaisEmisorSerieFondo, solicitud.IdPaisFondo, solicitud.IdPaisMoneda)).collect(Collectors.toList());
        if(solicitudesAConsultar.size() > 0) {
            Map<String,DiaFeriado> feriados = obtenerFeriados(solicitudesAConsultar, idPaisEmpresa);
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = obtenerFechaAConsultar(solicitudesAProcesarNoSwitch,feriados,idPaisEmpresa);
            if(fechasAConsultar.size() > 0) {
                HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> fechasCalculadas = obtenerFechasCalculadasRepaso(fechasAConsultar, idPaisEmpresa);
                solicitudesAProcesarNoSwitch.stream().filter(solicitud->!solicitud.IdPaisEmpresa.equals(solicitud.IdPaisEmisorSerieFondo)).forEach(solicitud->{
                    if(fechasCalculadas.get((solicitud.IdInterno * 3) + 1) != null) {
                        solicitud.FechaCertificacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 1).fechaCalculada;
                    }
                    if(fechasCalculadas.get((solicitud.IdInterno * 3) + 2) != null) {
                        solicitud.FechaLiquidacion = fechasCalculadas.get((solicitud.IdInterno * 3) + 2).fechaCalculada;
                    }
                });
            }

        }
        return solicitudesAProcesarNoSwitch;
    }

    private Boolean esSerieExtranjera(Long idPaisEmpresa, Long idPaisEmisorSerieFondo, Long idPaisFondo, Long idPaisMoneda) {
        Boolean esSerieExtranjera = !idPaisEmpresa.equals(idPaisEmisorSerieFondo) || !idPaisEmpresa.equals(idPaisFondo) || !idPaisEmpresa.equals(idPaisMoneda);
        return esSerieExtranjera;
    }

    private Map<String,DiaFeriado> obtenerFeriados(List<ProcesamientoFechas> solicitudesAConsultar, Long idPaisEmpresa) throws Exception{

        List<String> fechas = solicitudesAConsultar.stream()
                .filter(solicitud->(!solicitud.IdPaisEmisor.equals(idPaisEmpresa) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
                .map(solicitud->solicitud.FechaCertificacion)
                .collect(Collectors.toList());
        fechas.addAll(solicitudesAConsultar.stream()
                .filter(solicitud->(!solicitud.IdPaisEmisor.equals(idPaisEmpresa) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
                .map(solicitud->solicitud.FechaLiquidacion)
                .collect(Collectors.toList()));
        List<LocalDate> fechasFiltro = fechas.stream().distinct().map(fecha->FechaLegacy.aFecha(fecha)).collect(Collectors.toList());

        List<Long> idsPaisFiltro = solicitudesAConsultar.stream()
                .filter(solicitud->!solicitud.IdPaisFondo.equals(idPaisEmpresa))
                .map(solicitud->solicitud.IdPaisFondo)
                .collect(Collectors.toList());
        idsPaisFiltro.addAll(solicitudesAConsultar.stream()
                .filter(solicitud->(!solicitud.IdPaisFondo.equals(solicitud.IdPaisEmisor) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
                .map(solicitud->solicitud.IdPaisEmisor)
                .collect(Collectors.toList()));
        idsPaisFiltro.addAll(solicitudesAConsultar.stream()
                .filter(solicitud->(!solicitud.IdPaisMoneda.equals(idPaisEmpresa)))
                .map(solicitud->solicitud.IdPaisMoneda)
                .collect(Collectors.toList()));
        idsPaisFiltro.add(idPaisEmpresa);
        idsPaisFiltro = idsPaisFiltro.stream().distinct().collect(Collectors.toList());



        List<DiaFeriado> feriados = lecturas.feriados(fechasFiltro, idsPaisFiltro);
        List<DiaFeriado> feriadosUnicos = new ArrayList<DiaFeriado>();
        boolean[] agregarFeriadoUnico = {true};
        feriados.forEach(feriado -> {
            agregarFeriadoUnico[0] = true;
            feriadosUnicos.forEach(feriadoUnico -> {
                if(feriadoUnico.IdPais == feriado.IdPais && feriadoUnico.Fecha.equals(feriado.Fecha)) {
                    agregarFeriadoUnico[0] = false;
                }
            });
            if(agregarFeriadoUnico[0] == true) {
                feriadosUnicos.add(feriado);
            }
        });
        Map<String,DiaFeriado> hashFeriados = feriadosUnicos.stream().collect(Collectors.toMap(feriado->(FechaLegacy.aTexto(feriado.Fecha)+feriado.IdPais.toString()), feriado->feriado));


        return hashFeriados;
    }

    private Map<String,DiaFeriado> obtenerFeriadosFondosNacionalesUSD(List<ProcesamientoFechas> solicitudesAConsultar, Long idPaisEmpresa) throws Exception{

        List<String> fechas = new ArrayList<String>();
        solicitudesAConsultar.stream().forEach(solicitud-> {
            fechas.add(solicitud.FechaCurse);
            fechas.add(solicitud.FechaCertificacion);
            fechas.add(solicitud.FechaLiquidacion);
        });

        fechas.addAll(solicitudesAConsultar.stream()
                .map(solicitud-> solicitud.FechaCertificacion)
                .collect(Collectors.toList()));

        List<LocalDate> fechasFiltro = fechas.stream().distinct().map(fecha->FechaLegacy.aFecha(fecha)).collect(Collectors.toList());

        List<Long> idsPaisFiltro = solicitudesAConsultar.stream()
                .filter(solicitud->!solicitud.IdPaisFondo.equals(idPaisEmpresa))
                .map(solicitud->solicitud.IdPaisFondo)
                .collect(Collectors.toList());
        idsPaisFiltro.addAll(solicitudesAConsultar.stream()
                .filter(solicitud->(!solicitud.IdPaisFondo.equals(solicitud.IdPaisEmisor) || !solicitud.IdPaisFondo.equals(idPaisEmpresa)))
                .map(solicitud->solicitud.IdPaisEmisor)
                .collect(Collectors.toList()));
        idsPaisFiltro.addAll(solicitudesAConsultar.stream()
                .filter(solicitud->(!solicitud.IdPaisMoneda.equals(idPaisEmpresa)))
                .map(solicitud->solicitud.IdPaisMoneda)
                .collect(Collectors.toList()));
        idsPaisFiltro.add(idPaisEmpresa);
        idsPaisFiltro = idsPaisFiltro.stream().distinct().collect(Collectors.toList());



        List<DiaFeriado> feriados = lecturas.feriados(fechasFiltro, idsPaisFiltro);
        Map<String,DiaFeriado> hashFeriados = feriados.stream().collect(Collectors.toMap(feriado->(FechaLegacy.aTexto(feriado.Fecha)+feriado.IdPais.toString()), feriado->feriado));


        return hashFeriados;
    }

    private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultar(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriados, Long idPaisEmpresa){
        List<ProcesamientoFechas> solicitudesFechaCertificacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaCertificacion(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());
        List<ProcesamientoFechas> solicitudesFechaLiquidacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaLiquidacion(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());
        List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();
        List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = solicitudesFechaCertificacion.stream().distinct().map(solicitud->{
            ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
            fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
            fechaEntrada.numeroDias = 1L;
            fechaEntrada.paises.add(idPaisEmpresa);
            fechaEntrada.paises.add(solicitud.IdPaisFondo);
            fechaEntrada.paises.add(solicitud.IdPaisEmisor);
            fechaEntrada.fechaInicial = solicitud.FechaCertificacion;
            return fechaEntrada;
        }).collect(Collectors.toList());

        List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarLiquidacion = solicitudesFechaLiquidacion.stream().distinct().map(solicitud->{
            ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
            fechaEntrada.idInterno = ((solicitud.IdInterno * 3) + 2);
            fechaEntrada.numeroDias = 1L;
            fechaEntrada.paises.add(idPaisEmpresa);
            fechaEntrada.paises.add(solicitud.IdPaisFondo);
            fechaEntrada.paises.add(solicitud.IdPaisEmisor);
            fechaEntrada.paises.add(solicitud.IdPaisMoneda);
            fechaEntrada.fechaInicial = solicitud.FechaLiquidacion;
            return fechaEntrada;
        }).collect(Collectors.toList());
        fechasAConsultar.addAll(fechasAConsultarCertificacion);
        fechasAConsultar.addAll(fechasAConsultarLiquidacion);
        return fechasAConsultar;
    }

    private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultarCurse(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriados, Long idPaisEmpresa){
        List<ProcesamientoFechas> solicitudesFechaCurse = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaCurse(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());
        List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();
        List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCurse = solicitudesFechaCurse.stream().distinct().map(solicitud->{
            ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
            fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
            fechaEntrada.numeroDias = 1L;
            fechaEntrada.paises.add(idPaisEmpresa);
            fechaEntrada.paises.add(solicitud.IdPaisFondo);
            fechaEntrada.paises.add(solicitud.IdPaisEmisor);
            fechaEntrada.paises.add(solicitud.IdPaisMoneda);
            fechaEntrada.fechaInicial = solicitud.FechaCurse;
            return fechaEntrada;
        }).collect(Collectors.toList());

        fechasAConsultar.addAll(fechasAConsultarCurse);
        return fechasAConsultar;
    }

    private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultarCertificacion(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriadosCertificacion, Map<String,DiaFeriado> feriadosCurse, Long idPaisEmpresa, List<ProcesamientoFechas> fechasSinProcesar){
        List<ProcesamientoFechas> solicitudesFechaCurse = fechasSinProcesar.stream().filter(solicitud->esFeriadoFechaCurse(feriadosCurse, solicitud, idPaisEmpresa)).collect(Collectors.toList());
        List<ProcesamientoFechas> solicitudesFechaCertificacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaCertificacion(feriadosCertificacion, solicitud, idPaisEmpresa)).collect(Collectors.toList());

        List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();

        if(solicitudesFechaCurse.isEmpty() == false) {
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = solicitudesFechaCurse.stream().distinct().map(solicitud->{
                ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
                fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
                fechaEntrada.numeroDias = 1L;
                fechaEntrada.paises.add(idPaisEmpresa);
                fechaEntrada.paises.add(solicitud.IdPaisFondo);
                fechaEntrada.paises.add(solicitud.IdPaisEmisor);
                fechaEntrada.paises.add(solicitud.IdPaisMoneda);
                fechaEntrada.fechaInicial = solicitud.FechaCertificacion;
                return fechaEntrada;
            }).collect(Collectors.toList());
            fechasAConsultar.addAll(fechasAConsultarCertificacion);

        }else {
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarCertificacion = solicitudesFechaCertificacion.stream().distinct().map(solicitud->{
                ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
                fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
                fechaEntrada.numeroDias = 1L;
                fechaEntrada.paises.add(idPaisEmpresa);
                fechaEntrada.paises.add(solicitud.IdPaisFondo);
                fechaEntrada.paises.add(solicitud.IdPaisEmisor);
                fechaEntrada.paises.add(solicitud.IdPaisMoneda);
                fechaEntrada.fechaInicial = solicitud.FechaCertificacion;
                return fechaEntrada;
            }).collect(Collectors.toList());
            fechasAConsultar.addAll(fechasAConsultarCertificacion);
        }

        return fechasAConsultar;
    }

    private List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> obtenerFechaAConsultarLiquidacion(List<ProcesamientoFechas> solicitudesAConsultar, Map<String,DiaFeriado> feriados, Map<String,DiaFeriado> feriadosCurse, Long idPaisEmpresa, List<ProcesamientoFechas> fechasSinProcesar){
        List<ProcesamientoFechas> solicitudesFechaCurse = fechasSinProcesar.stream().filter(solicitud->esFeriadoFechaCurse(feriadosCurse, solicitud, idPaisEmpresa)).collect(Collectors.toList());
        List<ProcesamientoFechas> solicitudesFechaLiquidacion = solicitudesAConsultar.stream().filter(solicitud->esFeriadoFechaLiquidacion(feriados, solicitud, idPaisEmpresa)).collect(Collectors.toList());

        List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar = new ArrayList<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn>();
        if(solicitudesFechaCurse.isEmpty() == false) {
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarLiquidacion = solicitudesFechaCurse.stream().distinct().map(solicitud->{
                ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
                fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
                fechaEntrada.numeroDias = 1L;
                fechaEntrada.paises.add(idPaisEmpresa);
                fechaEntrada.paises.add(solicitud.IdPaisFondo);
                fechaEntrada.paises.add(solicitud.IdPaisEmisor);
                fechaEntrada.paises.add(solicitud.IdPaisMoneda);
                fechaEntrada.fechaInicial = solicitud.FechaLiquidacion;
                return fechaEntrada;
            }).collect(Collectors.toList());

            fechasAConsultar.addAll(fechasAConsultarLiquidacion);
        }else {
            List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultarLiquidacion = solicitudesFechaLiquidacion.stream().distinct().map(solicitud->{
                ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn fechaEntrada = new ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn();
                fechaEntrada.idInterno = (solicitud.IdInterno * 3) + 1;
                fechaEntrada.numeroDias = 1L;
                fechaEntrada.paises.add(idPaisEmpresa);
                fechaEntrada.paises.add(solicitud.IdPaisFondo);
                fechaEntrada.paises.add(solicitud.IdPaisEmisor);
                fechaEntrada.paises.add(solicitud.IdPaisMoneda);
                fechaEntrada.fechaInicial = solicitud.FechaLiquidacion;
                return fechaEntrada;
            }).collect(Collectors.toList());

            fechasAConsultar.addAll(fechasAConsultarLiquidacion);
        }

        return fechasAConsultar;
    }

    private Boolean esFeriadoFechaCertificacion(Map<String,DiaFeriado> feriados, ProcesamientoFechas solicitud, Long idPaisEmpresa) {
        Boolean esFeriadoFechaCertificacion = feriados.get(solicitud.FechaCertificacion+idPaisEmpresa.toString()) != null || (feriados.get(solicitud.FechaCertificacion+solicitud.IdPaisFondo.toString()) != null) || (feriados.get(solicitud.FechaCertificacion+solicitud.IdPaisEmisor.toString()) != null || (feriados.get(solicitud.FechaCertificacion+solicitud.IdPaisMoneda.toString()) != null));
        return esFeriadoFechaCertificacion;
    }

    private Boolean esFeriadoFechaLiquidacion(Map<String,DiaFeriado> feriados, ProcesamientoFechas solicitud, Long idPaisEmpresa) {
        Boolean esFeriadoFechaCertificacion = feriados.get(solicitud.FechaLiquidacion+idPaisEmpresa.toString()) != null || (feriados.get(solicitud.FechaLiquidacion+solicitud.IdPaisFondo.toString()) != null) || (feriados.get(solicitud.FechaLiquidacion+solicitud.IdPaisEmisor.toString()) != null || (feriados.get(solicitud.FechaLiquidacion+solicitud.IdPaisMoneda.toString()) != null));
        return esFeriadoFechaCertificacion;
    }

    private Boolean esFeriadoFechaCurse(Map<String,DiaFeriado> feriados, ProcesamientoFechas solicitud, Long idPaisEmpresa) {
        Boolean esFeriadoFechaCertificacion = feriados.get(solicitud.FechaCurse+idPaisEmpresa.toString()) != null || (feriados.get(solicitud.FechaCurse+solicitud.IdPaisFondo.toString()) != null) || (feriados.get(solicitud.FechaCurse+solicitud.IdPaisEmisor.toString()) != null || (feriados.get(solicitud.FechaCurse+solicitud.IdPaisMoneda.toString()) != null) || (feriados.get(solicitud.FechaDigitacion+solicitud.IdPaisMoneda.toString()) != null) || (feriados.get(solicitud.FechaDigitacion+solicitud.IdPaisFondo.toString()) != null));
        return esFeriadoFechaCertificacion;
    }

    private HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> obtenerFechasCalculadasRepaso(List<ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.FechasIn> fechasAConsultar, Long idPaisEmpresa) throws Exception{
        ObtenerDiaHabilMultiplesPaises obtenerDiaSiguiente = new ObtenerDiaHabilMultiplesPaises(calendario);
        EntradaObtenerDiaHabilMultiplesPaises inObtenerDiaSiguiente = new EntradaObtenerDiaHabilMultiplesPaises();
        inObtenerDiaSiguiente.p_diaHabil = SI;
        inObtenerDiaSiguiente.p_soloCaeDiaHabil = SI;
        inObtenerDiaSiguiente.p_fechas = fechasAConsultar;
        SalidaObtenerDiaHabilMultiplesPaises outDiaSiguiente = obtenerDiaSiguiente.logica(inObtenerDiaSiguiente);
        HashMap<Long,ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut> hashFechasSalida = new HashMap<>();
        for (ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises.FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
            hashFechasSalida.put(fechaSalida.idInterno, fechaSalida);
        }
        return hashFechasSalida;
    }


    public List<ResultadoFecha> obtenerResultadoFechas(List<ProcesamientoFechas> solicitudesAProcesar){
        List<ResultadoFecha> resultados = solicitudesAProcesar.stream().map(solicitud->{
            ResultadoFecha resultadoFecha = new ResultadoFecha();
            resultadoFecha.IdInterno = solicitud.IdInterno;
            resultadoFecha.IdSerieFondo = solicitud.IdSerieFondo;
            resultadoFecha.FechaEmision = FechaLegacy.aFecha(solicitud.FechaEmision);
            resultadoFecha.FechaAceptacion = FechaLegacy.aFecha(solicitud.FechaCurse);
            resultadoFecha.FechaDigitacion = FechaLegacy.aFecha(solicitud.FechaDigitacion);
            resultadoFecha.FechaValorCuota = FechaLegacy.aFecha(solicitud.FechaValorCuota);
            resultadoFecha.FechaCertificacion = FechaLegacy.aFecha(solicitud.FechaCertificacion);
            resultadoFecha.FechaLiquidacion = FechaLegacy.aFecha(solicitud.FechaLiquidacion);
            return resultadoFecha;
        }).collect(Collectors.toList());
        return resultados;
    }

    private Boolean repasarFechaValorCuotaFondoNacionalUSDNoMoneyMarket(SerieFondoSolicitudes serieFondo, String permiteDiaNoHabilFechaValorCuotaMoneyMarket) {

        Boolean result = false;
        result = NO.equalsIgnoreCase(serieFondo.MoneyMarket) && SI.equalsIgnoreCase(serieFondo.FondoNacionalDolar) && SI.equalsIgnoreCase(permiteDiaNoHabilFechaValorCuotaMoneyMarket);
        return result;
    }
}
