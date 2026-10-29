package cl.bcs.fondomutuo.normal;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.custodia.ConsultarFlujoCustodiaFondoMutuoService;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.normal.BloqueoSolicitudPort.Bloqueo;
import cl.bcs.fondomutuo.normal.NormalContrato.InSolicitud;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.ConteoErrores;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Legacy {@code cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.logica} (JavaCall id 357). No state between requests: everything one request needs lives
 * in a {@link NormalContexto}. The phases are the legacy ones, in the legacy order; the gates G1..G5 only add a 503 BEFORE the first write, releasing the lock
 * first (the same {@code catch} that the legacy used to release it on an exception). See TRANSFORMATION_NOTES, Step 4A.
 */
public class CrearSolicitudAporteRescateNormalService implements NormalPort {

    private final RevisarRequisitosBasicosPort basicos;
    private final ConfiguracionSistemaPort configuracion;
    private final EmpresaPort empresas;
    private final DiccionarioMensajesPort diccionario;
    private final BloqueoSolicitudPort bloqueo;
    private final ObtenerPerfilFondoMutuo perfil;
    private final NormalLectura lectura;
    private final NormalCalculos calculos;
    private final NormalValidacion validacion;
    private final NormalFlujos flujos;
    private final NormalControles controles;
    private final NormalEscritura escritura;
    private final NormalCierre cierre;
    private final SecuenciaPort secuencias;
    private final CalendarioPort calendario;

    CrearSolicitudAporteRescateNormalService(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            DiccionarioMensajesPort diccionario, BloqueoSolicitudPort bloqueo, ObtenerPerfilFondoMutuo perfil, NormalLectura lectura, NormalCalculos calculos,
            NormalValidacion validacion, NormalFlujos flujos, NormalControles controles, NormalEscritura escritura, NormalCierre cierre,
            SecuenciaPort secuencias, CalendarioPort calendario) {
        this.basicos = basicos;
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.diccionario = diccionario;
        this.bloqueo = bloqueo;
        this.perfil = perfil;
        this.lectura = lectura;
        this.calculos = calculos;
        this.validacion = validacion;
        this.flujos = flujos;
        this.controles = controles;
        this.escritura = escritura;
        this.cierre = cierre;
        this.secuencias = secuencias;
        this.calendario = calendario;
    }

    private MensajeSalida error(NormalContexto c, Long id, String codigo) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>(), diccionario, c.entrada.p_IdEmpresa);
    }

    @Override
    public NormalContrato.Salida logica(NormalContrato.Entrada entrada) throws Exception {
        NormalContexto c = new NormalContexto();
        c.entrada = entrada;
        RevisarRequisitosBasicosSalida salidaBasicos = basicos.revisar(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (ConteoErrores.errores(salidaBasicos.p_out_Mensajes) > 0) {
            c.out.P_OUT_ERRORESCONTADOR = 1L;
            c.out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
            return c.out;
        }
        if (entrada.p_SolicitudesAporteRescate.size() == 0) {
            c.out.P_OUT_ERRORESCONTADOR = 1L;
            c.out.P_OUT_MENSAJES.add(error(c, 0L, "NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE"));
            return c.out;
        }
        c.fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        if (!NormalConstantes.SI.equalsIgnoreCase(entrada.p_SimularOperacion) && aplicaBloqueoSolicitud(entrada)) {
            if (!bloquear(c)) {
                return c.out;
            }
        }
        try {
            return procesar(c);
        } catch (Exception e) {
            eliminarBloqueo(c);
            throw e;   // DEV-F3 / F7: no printStackTrace; the controller answers the generic 500
        }
    }

    /** Legacy :326-371. @return false when the request is a duplicate or the lock could not be taken (answer already in {@code c.out}). */
    private boolean bloquear(NormalContexto c) {
        for (InSolicitud mov : c.entrada.p_SolicitudesAporteRescate) {
            Bloqueo b = new Bloqueo();
            b.IdBloqueoSolicitudAporteRescate = String.valueOf(mov.IdPortafolio) + '_' + mov.IdSerieFondo + '_' + mov.IdRegimenTributario + '_'
                    + mov.TipoSolicitud + '_' + String.valueOf(mov.Monto) + '_' + String.valueOf(mov.NumeroCuotas) + '_' + c.fechaSistema;
            b.IdPortafolio = mov.IdPortafolio;
            b.IdSerieFondo = mov.IdSerieFondo;
            b.IdRegimenTributario = mov.IdRegimenTributario;
            b.TipoSolicitud = mov.TipoSolicitud;
            b.Monto = mov.Monto;
            b.NumeroCuotas = mov.NumeroCuotas;
            b.FechaSistema = c.fechaSistema;
            c.solicitudesBloqueadas.add(b);
        }
        try {
            List<String> ids = c.solicitudesBloqueadas.stream().map(s -> s.IdBloqueoSolicitudAporteRescate).collect(Collectors.toList());
            List<String> idsBd = bloqueo.existentes(ids);
            if (idsBd.size() > 0 && c.solicitudesBloqueadas.size() == idsBd.size() && ids.equals(idsBd)) {
                c.out.P_OUT_ERRORESCONTADOR++;
                c.out.P_OUT_MENSAJES.add(error(c, 1L, "UI_ERROR_BLOQUEO_SOLICITUD"));
                return false;
            }
            bloqueo.crear(c.solicitudesBloqueadas);
        } catch (AccesoDatosException e) {
            c.out.P_OUT_ERRORESCONTADOR++;
            c.out.P_OUT_MENSAJES.add(error(c, 1L, "UI_ERROR_BLOQUEO_SOLICITUD"));
            return false;
        }
        return true;
    }

    /** Legacy :1749-1766: the lock is released on every exit; a failure of the CALL only adds a message (D-NOR-3), the DROP always runs. */
    private void eliminarBloqueo(NormalContexto c) {
        if (!NormalConstantes.SI.equalsIgnoreCase(c.entrada.p_SimularOperacion) && c.solicitudesBloqueadas.size() > 0) {
            try {
                bloqueo.llamarEliminar(c.solicitudesBloqueadas);
            } catch (AccesoDatosException e) {
                c.out.P_OUT_MENSAJES.add(error(c, 1L, "UI_ERROR_DELETE_BLOQUEO_SOLICITUD"));
            } finally {
                bloqueo.soltarTabla();
            }
        }
    }

    static boolean aplicaBloqueoSolicitud(NormalContrato.Entrada entrada) {
        String origen = entrada.p_IdOrigenProceso;
        return NormalValidacion.esOrigenProcesosManuales(entrada) && !NormalConstantes.ORIGEN_CARGA_MASIVA_FM.equalsIgnoreCase(origen)
                && !NormalConstantes.CSC.equalsIgnoreCase(origen);
    }

    private NormalContrato.Salida procesar(NormalContexto c) throws Exception {
        NormalContrato.Entrada entrada = c.entrada;
        ArrayList<Long> correlativos = new ArrayList<>();
        for (InSolicitud mov : entrada.p_SolicitudesAporteRescate) {
            if (mov.IdInterno.longValue() == 0L) {
                return salidaTemprana(c, "ID_INTERNO_NO_CERO");
            }
            if (correlativos.contains(mov.IdInterno)) {
                return salidaTemprana(c, "CORRELATIVO_INCORRECTO");
            }
            correlativos.add(mov.IdInterno);
        }
        c.idPaisEmpresa = empresas.informacion(entrada.p_IdEmpresa).IdPais;
        c.calcularComisionCertificacion = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "CALCULAR_COMISION_CERTIFICACION");
        try {
            c.respetaValutasCargaMasiva = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
        } catch (Exception e) {
            // legacy: warning only, the default stays N
        }
        NormalLectura.Claves claves = NormalLectura.claves(c);
        ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo ep = new ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo();
        ep.p_IdUsuario = entrada.p_IdUsuario;
        c.perfilFondoMutuo = perfil.logica(ep).PerfilFondoMutuo;
        if (c.perfilFondoMutuo == null && claves.countTransaccionesBatch != entrada.p_SolicitudesAporteRescate.size()) {
            return salidaTemprana(c, "PERFIL_INCORRECTO");
        }
        lectura.leer(c, claves);
        c.rescateGeneraSpotDolar = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "RESCATE_GENERA_VENTA_SPOT_DOLAR");
        calculos.calcular(c);
        armar(c);
        if (!validacion.requisitos(c)) {
            eliminarBloqueo(c);
            return c.out;
        }
        validacion.montoInversion(c);
        c.fechaMaximaCertificacion = NormalValidacion.fechaMaximaCertificacion(c);
        validacion.patrimonioEmisor(c);
        HashMap<String, java.math.BigDecimal> portTotalesMontos = new HashMap<>();
        NormalContratos.FlujoCajaSalida salidaFlujoCaja = flujos.saldoFlujoCaja(c, portTotalesMontos);
        ConsultarFlujoCustodiaFondoMutuoService.Salida salidaFlujo = flujos.flujoCustodia(c);
        controles.controlar(c, salidaFlujoCaja, portTotalesMontos, salidaFlujo);
        return escribir(c);
    }

    private NormalContrato.Salida salidaTemprana(NormalContexto c, String codigo) {
        c.out.P_OUT_ERRORESCONTADOR = 1L;
        c.out.P_OUT_MENSAJES.add(error(c, 0L, codigo));
        eliminarBloqueo(c);
        return c.out;
    }

    private NormalContrato.Salida escribir(NormalContexto c) throws Exception {
        String notifica = null;
        if (!NormalConstantes.SI.equalsIgnoreCase(c.entrada.p_SimularOperacion)) {
            notifica = configuracion.texto(c.entrada.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");   // read before the gates (DEV-4A-2)
            escritura.compuertas(c, notifica);
            escritura.real(c);
        } else {
            escritura.simulada(c);
        }
        if (!cierre.comisionNn(c)) {
            eliminarBloqueo(c);
            return c.out;
        }
        if (notifica == null) {
            notifica = configuracion.texto(c.entrada.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");
        }
        cierre.notificarOwp(c, notifica);
        eliminarBloqueo(c);
        return c.out;
    }

    /** Legacy :740-1078: the secuences of the folios, the UTC stamp, and the row / checks / output of each request. */
    private void armar(NormalContexto c) throws Exception {
        NormalContrato.Entrada entrada = c.entrada;
        c.entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
        c.entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
        c.entradaCumpleRequisitos.p_PermiteOperarProspectos = entrada.p_PermiteOperarProspectos;
        c.entradaValidaPatrimonioEmisor.p_IdEmpresa = entrada.p_IdEmpresa;
        c.entradaValidaPatrimonioEmisor.p_IdUsuario = entrada.p_IdUsuario;
        c.entradaValidaPatrimonioEmisor.p_Fecha = c.fechaSistema;
        c.folioContratoSolicitudes = secuencias.siguientes(NormalConstantes.SECUENCIA_FOLIO_CONTRATO, (long) entrada.p_SolicitudesAporteRescate.size());
        int segundosZona = (int) configuracion.entero(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
        Timestamp local = calendario.fechaHoraSistemaLocal(entrada.p_IdEmpresa);
        c.fechaHoraUTC = new Timestamp(local.getTime() - segundosZona * 1000L);   // DEV-F2: local to the call (legacy: Fecha.setLocalOffSet, static)
        NormalArmado armado = new NormalArmado(new FormatoMoneda(calendario));
        int i = 0;
        for (InSolicitud solicitud : entrada.p_SolicitudesAporteRescate) {
            String tm = NormalArmado.tipoMovimiento(solicitud);
            String procedencia;
            if ("PAC".equalsIgnoreCase(entrada.p_IdOrigenProceso)) {
                procedencia = "PAC";
            } else {
                procedencia = solicitud.Canal != null && !solicitud.Canal.isEmpty() ? solicitud.Canal : entrada.p_IdTipoCanalIngreso != null ? entrada.p_IdTipoCanalIngreso : "";
            }
            SolicitudAporteRescate sol = armado.solicitud(c, solicitud, i, tm, procedencia);
            cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes solVal = new cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes();
            cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InPortafolio portVal = new cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InPortafolio();
            c.solicitudesProcesar.add(sol);
            NormalRequisitos.completar(c, solicitud, sol, procedencia, solVal, portVal);
            c.entradaCumpleRequisitos.p_Portafolios.add(portVal);
            c.entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
            SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
            ResultadoSolicitud salida = NormalSalida.armar(sol, serie, c.hclientes.get(solicitud.IdPortafolio));
            c.resultadoTemp.add(salida);
            i++;
        }
        c.hSolicitudSalida = Hashes.aHash(c.resultadoTemp, r -> r.IdInterno);
    }
}
