package cl.bcs.fondomutuo.rescatereinversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.ConteoErrores;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.Entrada;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.Salida;

/**
 * Legacy {@code cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.logica} (JavaCall 1436; one method of CCN 221): the phases are the legacy ones in the
 * legacy order (FM-REI-01..39). No state between calls (everything in a {@link RreContexto}). No {@code try/catch} around the processing, as the legacy
 * (D-F2-1: an exception after the lock leaves the AUTONOMOUS lock and the session table), with ONE modern addition required by the 503 contract (plan 6.2,
 * DEV-F4/DEV-F5): a {@code ServicioNoServidoException} (gate G-CUS, a router 503 of an HTTP read or of AgregarLog / InscribirProcesoBatch) releases the
 * lock BEFORE the 503 leaves, so the repetition in TomEE is not answered as a duplicate.
 */
public class CrearSolicitudRescateReinversionService implements RescateReinversionPort {

    private final RevisarRequisitosBasicosPort basicos;
    private final ConfiguracionSistemaPort configuracion;
    private final EmpresaPort empresas;
    private final DiccionarioMensajesPort diccionario;
    private final ObtenerPerfilFondoMutuo perfil;
    private final RreLectura lectura;
    private final BloqueoReinversionPort bloqueo;
    private final RreFechas fechas;
    private final RreCuotas cuotas;
    private final RreArmado armado;
    private final RreValidacion validacion;
    private final RreEscritura escritura;

    CrearSolicitudRescateReinversionService(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            DiccionarioMensajesPort diccionario, ObtenerPerfilFondoMutuo perfil, RreLectura lectura, BloqueoReinversionPort bloqueo, RreFechas fechas,
            RreCuotas cuotas, RreArmado armado, RreValidacion validacion, RreEscritura escritura) {
        this.basicos = basicos;
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.diccionario = diccionario;
        this.perfil = perfil;
        this.lectura = lectura;
        this.bloqueo = bloqueo;
        this.fechas = fechas;
        this.cuotas = cuotas;
        this.armado = armado;
        this.validacion = validacion;
        this.escritura = escritura;
    }

    private MensajeSalida error(RreContexto c, Long id, String codigo) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>(), diccionario, c.entrada.p_IdEmpresa);
    }

    private Salida temprana(RreContexto c, String codigo) {
        c.out.P_OUT_ERRORESCONTADOR = 1L;
        c.out.P_OUT_MENSAJES.add(error(c, 0L, codigo));
        return c.out;
    }

    @Override
    public Salida logica(Entrada entrada) throws Exception {
        RreContexto c = new RreContexto();
        c.entrada = entrada;
        RevisarRequisitosBasicosSalida salidaBasicos = basicos.revisar(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (ConteoErrores.errores(salidaBasicos.p_out_Mensajes) > 0) {
            c.out.P_OUT_ERRORESCONTADOR = 1L;
            c.out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
            return c.out;
        }
        if (entrada.p_SolicitudesRescate.size() == 0) return temprana(c, "NO_SE_INGRESARON_SOLICITUDES_RESCATE");
        ArrayList<Long> correlativos = new ArrayList<>();
        for (InSolicitudesRescate mov : entrada.p_SolicitudesRescate) {
            if (mov.IdInterno.longValue() == 0L) return temprana(c, "ID_INTERNO_NO_CERO");
            if (correlativos.contains(mov.IdInterno)) return temprana(c, "CORRELATIVO_INCORRECTO");
            correlativos.add(mov.IdInterno);
        }
        c.fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        c.rescateGeneraVentaSpotDolar = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "RESCATE_GENERA_VENTA_SPOT_DOLAR");
        try {
            c.respetaValutasCargaMasiva = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
        } catch (Exception e) {
            // legacy: warning only, the default stays N
        }
        c.idPaisEmpresa = empresas.informacion(entrada.p_IdEmpresa).IdPais;
        RreLectura.claves(c);
        ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo ep = new ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo();
        ep.p_IdUsuario = entrada.p_IdUsuario;
        c.perfilFondoMutuo = perfil.logica(ep).PerfilFondoMutuo;
        if (c.perfilFondoMutuo == null && c.countTransaccionesBatch != entrada.p_SolicitudesRescate.size()) return temprana(c, "PERFIL_INCORRECTO");
        lectura.leer(c);
        RreLectura.tipoReinversion(c);
        EntradaCalcularFechaSolicitudAporteRescate entradaFechas = fechasYBloqueo(c);
        insertarBloqueo(c);
        if (c.out.P_OUT_ERRORESCONTADOR > 0) {
            return c.out;   // duplicate or failed lock: nothing is released (FM-REI-10)
        }
        try {
            return procesar(c, entradaFechas);
        } catch (ServicioNoServidoException e) {
            if (c.bloqueoCreado) eliminarBloqueo(c);   // plan 6.2: the lock is released BEFORE the 503 leaves (the router repeats in TomEE)
            throw e;
        }
    }

    /** FM-REI-08/09 (RR:465-537): the date request of each rescue with a series and its lock row, by POSITION. */
    private static EntradaCalcularFechaSolicitudAporteRescate fechasYBloqueo(RreContexto c) {
        EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
        entradaFechas.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entradaFechas.p_IdPais = c.idPaisEmpresa;
        int secuenciaReinversion = 0;
        for (InSolicitudesRescate solicitud : c.entrada.p_SolicitudesRescate) {
            SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
            if (serie != null) {
                entradaFechas.p_SolicitudAporteRescate.add(RreFechas.entradaRescate(c, solicitud, serie));
                InSolicitudesRescate rescate = c.entrada.p_SolicitudesRescate.get(secuenciaReinversion);
                InSolicitudesAporte aporte = c.entrada.p_SolicitudesAporte.get(secuenciaReinversion);   // D-F2-10: fewer contributions = IndexOutOfBounds
                String id = RreReglas.idBloqueo(rescate, aporte, c.fechaSistema);
                solicitud.IdBloqueoRescate = id;
                BloqueoReinversionPort.Bloqueo b = new BloqueoReinversionPort.Bloqueo();
                b.IdBloqueaReinversion = id;
                b.IdPortafolio = rescate.IdPortafolio;
                b.IdSerieFondo = aporte.IdSerieFondo;
                b.IdRegimenTributario = rescate.IdRegimenTributario != null ? rescate.IdRegimenTributario : "";
                b.Monto = rescate.Monto;
                b.NumeroCuotas = rescate.NumeroCuotas;
                b.FechaSistema = FechaLegacy.aFechaSql(c.fechaSistema);
                b.Simula = c.entrada.p_SimularOperacion != null ? c.entrada.p_SimularOperacion : "N";
                c.bloquearReinversion.add(b);
            }
            secuenciaReinversion++;
        }
        return entradaFechas;
    }

    /** FM-REI-10 (RR:1550-1592): all rows "S" (or none) = nothing; duplicate (same ids, same order) = message; any SQL failure = message, nothing dropped. */
    private void insertarBloqueo(RreContexto c) {
        try {
            List<String> ids = c.bloquearReinversion.stream().map(b -> b.IdBloqueaReinversion).collect(Collectors.toList());
            boolean simula = c.bloquearReinversion.stream().allMatch(b -> "S".equalsIgnoreCase(b.Simula));
            if (!simula) {
                List<String> idsBd = bloqueo.existentes(ids);
                if (idsBd.size() > 0 && ids.equals(idsBd)) {
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(error(c, 1L, "UI_ERROR_BLOQUEO_SOLICITUD"));
                } else {
                    bloqueo.crear(c.bloquearReinversion);
                    c.bloqueoCreado = true;
                }
            }
        } catch (AccesoDatosException e) {
            c.out.P_OUT_ERRORESCONTADOR++;
            c.out.P_OUT_MENSAJES.add(error(c, 1L, "UI_ERROR_BLOQUEO_REINVERSION"));
        }
    }

    /** FM-REI-11 (RR:1512-1548): nothing while simulating (null = N); the CALL failure is a message only (counter unchanged); the DROP always runs. */
    private void eliminarBloqueo(RreContexto c) {
        String simular = c.entrada.p_SimularOperacion != null ? c.entrada.p_SimularOperacion : "N";
        if (c.entrada.p_SolicitudesRescate.size() > 0 && !"S".equalsIgnoreCase(simular)) {
            try {
                bloqueo.llamarEliminar();
            } catch (AccesoDatosException e) {
                c.out.P_OUT_MENSAJES.add(error(c, 1L, "UI_ERROR_DELETE_BLOQUEO_REINVERSION"));
            } finally {
                bloqueo.soltarTabla();
            }
        }
    }

    private Salida procesar(RreContexto c, EntradaCalcularFechaSolicitudAporteRescate entradaFechas) throws Exception {
        c.hfechas = fechas.calcular(entradaFechas);
        // legacy Collectors.toMap: a null SolicitudBatch is a NullPointerException (HashMap.merge), FM-REI-12 / C43
        c.solicitudesBatches = c.entrada.p_SolicitudesRescate.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r.SolicitudBatch));
        c.hfechasAporte = fechas.obtenerFechaAporte(c);
        if (!cuotas.calcular(c)) {
            eliminarBloqueo(c);
            return c.out;
        }
        armado.armar(c);
        if (!validacion.validar(c)) {
            eliminarBloqueo(c);
            return c.out;
        }
        validacion.custodia(c);
        if (!RreConstantes.SI.equalsIgnoreCase(c.entrada.p_SimularOperacion)) {
            List<SolicitudAporteRescate> insertadas = escritura.real(c);
            eliminarBloqueo(c);
            if (insertadas.size() > 0) escritura.mensajes1N(c, insertadas);
        } else {
            RreEscritura.simulada(c);
        }
        return c.out;
    }
}
