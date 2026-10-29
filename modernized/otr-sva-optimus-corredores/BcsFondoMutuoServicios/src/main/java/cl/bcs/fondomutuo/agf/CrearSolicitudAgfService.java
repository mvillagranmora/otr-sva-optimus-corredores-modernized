package cl.bcs.fondomutuo.agf;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.agf.AgfDatos.InsertBeneficiario;
import cl.bcs.fondomutuo.agf.AgfDatos.InsertSolicitud;
import cl.bcs.fondomutuo.agf.AgfDatos.SelectBeneficiario;
import cl.bcs.fondomutuo.agf.AgfDatos.SelectSolicitud;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.Constantes3B;
import cl.bcs.fondomutuo.requisitos.Hashes;

/**
 * Legacy {@code cl.bcs.fondomutuo.CrearSolicitudAGF} (370 LOC), rules FM-NOR-51 / FM-NOR-52. Copies the just-created requests (and the participants not
 * yet known) to the AGF interface table {@code FMM.InterfazAgf}, only when {@code FONDOMUTUO/UsaReplicaSigaFondoMutuo} is {@code S} (default {@code N}
 * when the parameter is missing). Writes (the two inserts) join the transaction of the call. Kept as legacy: the whole body is inside the {@code S}
 * condition, a request without quotas or quota value is reported but the rest go on, the counter of the early exits is fixed at 1 and elsewhere
 * grows by one per message, and a repeated id in the input is detected with a list lookup.
 */
public class CrearSolicitudAgfService {

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_EsManual;
        public List<Solicitudes> p_Solicitudes = new ArrayList<>();
    }

    public static class Solicitudes {
        public Long IdSolicitudAporteRescate;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<Resultado> P_OUT_RESULTADO = new ArrayList<>();
    }

    public static class Resultado {
        public Long IdSolicitudAporteRescate;
        public Long IdSolicitudAporteRescateRelacionado;
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public Date FechaLiquidacion;
        public Date FechaAceptacion;
        public String TipoSolicitud;
        public String IdEstadoSolicitud;
        public String IdSerieFondo;
        public Long IdUsuario;
        public BigDecimal NumeroCuotas;
        public BigDecimal ValorCuota;
        public BigDecimal Monto;
        public String CalculoPorMontoCuota;
        public String IdMoneda;
        public String PropositoSerie;
        public String IdSerieFondoDestino;
        public String IdEstadoSerieFondo;
        public String IndicadorMLT;
        public String IdEstado;
        public Long IdEmpresa;
    }

    private final AgfDatos datos;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajesPort diccionario;

    public CrearSolicitudAgfService(AgfDatos datos, ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario) {
        this.datos = datos;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
    }

    public Salida logica(Entrada entrada) throws Exception {
        Salida out = new Salida();
        String fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String usaReplica = Constantes3B.NO;
        try {
            usaReplica = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "UsaReplicaSigaFondoMutuo");
        } catch (Exception e) {
            // legacy: warning only, the default stays N
        }

        if (Constantes3B.SI.equals(usaReplica)) {
            if (entrada.p_Solicitudes.size() == 0) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(msg(0L, "NO_SE_INGRESARON_SOLICITUDES", new HashMap<>(), entrada.p_IdEmpresa));
                return out;
            }

            List<Long> ids = new ArrayList<>();
            for (Solicitudes s : entrada.p_Solicitudes) {
                if (ids.contains(s.IdSolicitudAporteRescate)) {
                    out.P_OUT_ERRORESCONTADOR = 1L;
                    out.P_OUT_MENSAJES.add(msg(0L, "SOLICITUDES_REPETIDAS", new HashMap<>(), entrada.p_IdEmpresa));
                    return out;
                }
                ids.add(s.IdSolicitudAporteRescate);
            }

            List<SelectBeneficiario> beneficiarios = datos.beneficiariosNuevos(ids);
            List<SelectSolicitud> solicitudes = datos.solicitudes(ids);
            List<InsertBeneficiario> insertBeneficiarios = new ArrayList<>();
            for (SelectBeneficiario b : beneficiarios) {
                InsertBeneficiario in = new InsertBeneficiario();
                in.DocumentoIdentidad = b.DocumentoIdentidad;
                in.CodigoPortafolio = b.CodigoPortafolio;
                in.NombreCompleto = b.NombreCompleto;
                in.IdCliente = b.IdCliente;
                in.IdPortafolio = b.IdPortafolio;
                in.FechaIngreso = fechaSistema;
                in.IdEstado = Constantes3B.ESTADO_PENDIENTE;
                in.TipoRegistro = Constantes3B.TIPO_REGISTRO_PARTICIPE;
                in.IdEmpresa = entrada.p_IdEmpresa;
                insertBeneficiarios.add(in);
            }

            java.util.HashMap<Long, SelectSolicitud> porId = Hashes.aHash(solicitudes, s -> s.IdSolicitudAporteRescate);

            List<InsertSolicitud> insertSolicitudes = new ArrayList<>();
            for (Solicitudes sol : entrada.p_Solicitudes) {
                SelectSolicitud ss = porId.get(sol.IdSolicitudAporteRescate);
                if (ss == null) {
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_MENSAJES.add(msg(sol.IdSolicitudAporteRescate, "NO_SE_ENCONTRO_SOLICITUD", new HashMap<>(), entrada.p_IdEmpresa));
                    continue;
                }
                if ((ss.NumeroCuotas == null || BigDecimal.ZERO.compareTo(ss.NumeroCuotas) == 0)
                        || (ss.ValorCuota == null || BigDecimal.ZERO.compareTo(ss.ValorCuota) == 0)) {
                    HashMap<String, String> valores = new HashMap<>();
                    valores.put("IdSerieFondo", ss.IdSerieFondo);
                    valores.put("DocumentoIdentidad", ss.DocumentoIdentidad);
                    valores.put("TipoContrato", ss.TipoContratoOperacion);
                    valores.put("FolioOperacion", ss.FolioContratoOperacion);
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_MENSAJES.add(msg(sol.IdSolicitudAporteRescate, "VALOR_CUOTA_VACIO", valores, entrada.p_IdEmpresa));
                    continue;
                }
                InsertSolicitud in = copiar(ss);
                in.FechaIngreso = fechaSistema;
                in.IdEstado = Constantes3B.ESTADO_PENDIENTE;
                in.TipoRegistro = Constantes3B.TRANSFERENCIA.equalsIgnoreCase(ss.IdTipoMovimientoFondoMutuo)
                        || Constantes3B.TRASPASO_PORTAFOLIO.equalsIgnoreCase(ss.IdTipoMovimientoFondoMutuo) ? Constantes3B.TIPO_REGISTRO_TRASPASO
                                : Constantes3B.TIPO_REGISTRO_APORTE_RESCATE;
                in.IdEmpresa = entrada.p_IdEmpresa;
                insertSolicitudes.add(in);
            }

            if (insertBeneficiarios.size() > 0) {
                datos.insertarBeneficiarios(insertBeneficiarios);
            }
            if (insertSolicitudes.size() > 0) {
                datos.insertarSolicitudes(insertSolicitudes);
            }
            for (InsertSolicitud sol : insertSolicitudes) {
                out.P_OUT_RESULTADO.add(resultado(sol));
            }
        }
        return out;
    }

    private MensajeSalida msg(Long id, String codigo, HashMap<String, String> valores, Long idEmpresa) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, valores, diccionario, idEmpresa);
    }

    private static InsertSolicitud copiar(SelectSolicitud ss) {
        InsertSolicitud is = new InsertSolicitud();
        is.IdSolicitudAporteRescate = ss.IdSolicitudAporteRescate;
        is.IdSolicitudAporteRescateRelacionado = ss.IdSolicitudAporteRescateRelacionado;
        is.DocumentoIdentidad = ss.DocumentoIdentidad;
        is.CodigoPortafolio = ss.CodigoPortafolio;
        is.FechaLiquidacion = ss.FechaLiquidacion;
        is.FechaAceptacion = ss.FechaAceptacion;
        is.TipoSolicitud = ss.TipoSolicitud;
        is.IdEstadoSolicitud = ss.IdEstadoSolicitud;
        is.IdSerieFondo = ss.IdSerieFondo;
        is.IdUsuario = ss.IdUsuario;
        is.NumeroCuotas = ss.NumeroCuotas;
        is.ValorCuota = ss.ValorCuota;
        is.Monto = ss.Monto;
        is.CalculoPorMontoCuota = ss.CalculoPorMontoCuota;
        is.IdMoneda = ss.IdMoneda;
        is.PropositoSerie = ss.PropositoSerie;
        is.IdSerieFondoDestino = ss.IdSerieFondoDestino;
        is.IdEstadoSerieFondo = ss.IdEstadoSerieFondo;
        is.IndicadorMLT = ss.IndicadorMLT;
        is.NombreCompleto = ss.NombreCompleto;
        return is;
    }

    private static Resultado resultado(InsertSolicitud is) {
        Resultado r = new Resultado();
        r.IdSolicitudAporteRescate = is.IdSolicitudAporteRescate;
        r.IdSolicitudAporteRescateRelacionado = is.IdSolicitudAporteRescateRelacionado;
        r.DocumentoIdentidad = is.DocumentoIdentidad;
        r.CodigoPortafolio = is.CodigoPortafolio;
        r.FechaLiquidacion = is.FechaLiquidacion;
        r.FechaAceptacion = is.FechaAceptacion;
        r.TipoSolicitud = is.TipoSolicitud;
        r.IdEstadoSolicitud = is.IdEstadoSolicitud;
        r.IdSerieFondo = is.IdSerieFondo;
        r.IdUsuario = is.IdUsuario;
        r.NumeroCuotas = is.NumeroCuotas;
        r.ValorCuota = is.ValorCuota;
        r.Monto = is.Monto;
        r.CalculoPorMontoCuota = is.CalculoPorMontoCuota;
        r.IdMoneda = is.IdMoneda;
        r.PropositoSerie = is.PropositoSerie;
        r.IdSerieFondoDestino = is.IdSerieFondoDestino;
        r.IdEstadoSerieFondo = is.IdEstadoSerieFondo;
        r.IndicadorMLT = is.IndicadorMLT;
        r.IdEstado = is.IdEstado;
        r.IdEmpresa = is.IdEmpresa;
        return r;
    }
}
