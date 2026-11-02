package cl.bcs.fondomutuo.aportereinversion;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.normal.NormalContratos;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.ConteoErrores;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Legacy {@code cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.logica}: creates the {@code APOREINV} contribution requests of a rescue with reinversion, in lots of
 * 2.000 (FM-REI-41..68). No state between requests: everything one lot needs lives in an {@link AporteReinversionContexto}. The phases are the legacy ones, in the legacy order;
 * the gates G1, G2, G3 and G-CUS only add a 503 before the first write (the reads G3 and G-CUS also in simulation). It does not catch anything.
 */
public class CrearSolicitudAporteReinversionServicio implements AporteReinversionPort {

    private final RevisarRequisitosBasicosPort basicos;
    private final ConfiguracionSistemaPort configuracion;
    private final EmpresaPort empresas;
    private final DiccionarioMensajesPort diccionario;
    private final CalendarioPort calendario;
    private final ObtenerPerfilFondoMutuo perfil;
    private final AporteReinversionLectura lectura;
    private final AporteReinversionCalculos calculos;
    private final AporteReinversionControles controles;
    private final AporteReinversionEscritura escritura;
    private final SecuenciaPort secuencias;

    CrearSolicitudAporteReinversionServicio(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            DiccionarioMensajesPort diccionario, CalendarioPort calendario, ObtenerPerfilFondoMutuo perfil, AporteReinversionLectura lectura,
            AporteReinversionCalculos calculos, AporteReinversionControles controles, AporteReinversionEscritura escritura, SecuenciaPort secuencias) {
        this.basicos = basicos;
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.diccionario = diccionario;
        this.calendario = calendario;
        this.perfil = perfil;
        this.lectura = lectura;
        this.calculos = calculos;
        this.controles = controles;
        this.escritura = escritura;
        this.secuencias = secuencias;
    }

    private MensajeSalida error(AporteReinversionContexto c, Long id, String codigo) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>(), diccionario, c.entrada.p_IdEmpresa);
    }

    @Override
    public AporteReinversionContrato.Salida logica(AporteReinversionContrato.Entrada entrada) throws Exception {
        AporteReinversionContexto c = new AporteReinversionContexto();
        c.entrada = entrada;
        c.aporteGeneraCompraSpotDolar = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "APORTE_GENERA_COMPRA_SPOT_DOLAR");
        c.idMonedaLocal = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
        RevisarRequisitosBasicosSalida salidaBasicos = basicos.revisar(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (ConteoErrores.errores(salidaBasicos.p_out_Mensajes) > 0) {
            c.out.P_OUT_ERRORESCONTADOR = 1L;
            c.out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
            return c.out;
        }
        if (entrada.p_SolicitudesAporte.size() == 0) {
            return salidaTemprana(c, "NO_SE_INGRESARON_SOLICITUDES_APORTE");
        }
        ArrayList<Long> correlativos = new ArrayList<>();
        for (InSolicitudesAporte mov : entrada.p_SolicitudesAporte) {
            if (mov.IdInterno.longValue() == 0L) {
                return salidaTemprana(c, "ID_INTERNO_NO_CERO");
            }
            if (correlativos.contains(mov.IdInterno)) {
                return salidaTemprana(c, "CORRELATIVO_INCORRECTO");
            }
            correlativos.add(mov.IdInterno);
        }
        c.fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        try {
            c.respetaValutasCargaMasiva = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
        } catch (Exception e) {
            // legacy: warning only, the default stays N
        }
        c.idPaisEmpresa = empresas.informacion(entrada.p_IdEmpresa).IdPais;
        procesarEnLotes(c);
        return c.out;
    }

    private AporteReinversionContrato.Salida salidaTemprana(AporteReinversionContexto c, String codigo) {
        c.out.P_OUT_ERRORESCONTADOR = 1L;
        c.out.P_OUT_MENSAJES.add(error(c, 0L, codigo));
        return c.out;
    }

    /** Legacy :447-473: the requests go in lots of {@code MAX_REGISTROS}; the input list is REPLACED by each lot (the callers see the last one, D-F2-19). */
    private void procesarEnLotes(AporteReinversionContexto c) throws Exception {
        AporteReinversionContrato.Entrada entrada = c.entrada;
        int tamano = AporteReinversionConstantes.MAX_REGISTROS;
        List<InSolicitudesAporte> respaldoSolicitudes = new ArrayList<>(entrada.p_SolicitudesAporte);
        int segundosZona = (int) configuracion.entero(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
        Timestamp local = calendario.fechaHoraSistemaLocal(entrada.p_IdEmpresa);
        c.fechaHoraUTC = new Timestamp(local.getTime() - segundosZona * 1000L);   // local to the call (legacy: Fecha.setLocalOffSet, static)
        for (int i = 0; i < respaldoSolicitudes.size(); i += tamano) {
            int fin = Math.min(i + tamano, respaldoSolicitudes.size());
            entrada.p_SolicitudesAporte = new ArrayList<>(respaldoSolicitudes.subList(i, fin));
            if (procesarSolicitudes(c)) {
                break;
            }
        }
    }

    /** Legacy :475-1471. @return true when the whole call must stop (an error before the lot was written). */
    private boolean procesarSolicitudes(AporteReinversionContexto c) throws Exception {
        AporteReinversionContrato.Entrada entrada = c.entrada;
        c.nuevoLote();
        c.fechaMaximaCertificacion = AporteReinversionControles.fechaInicialMaxima();
        AporteReinversionLectura.Claves claves = AporteReinversionLectura.claves(c);
        ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo ep = new ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo();
        ep.p_IdUsuario = entrada.p_IdUsuario;
        c.perfilFondoMutuo = perfil.logica(ep).PerfilFondoMutuo;
        if (c.perfilFondoMutuo == null && claves.countTransaccionesBatch != entrada.p_SolicitudesAporte.size() && entrada.p_SolicitudesAporte.get(0).FechaEmision != null) {
            salidaTemprana(c, "PERFIL_INCORRECTO");   // answers through c.out (the legacy error counter is ASSIGNED 1, not incremented)
            return true;
        }
        lectura.leer(c, claves);
        calculos.paridadDeLaInversion(c);
        AporteReinversionLectura.clasificar(c);
        calculos.calcularFechas(c);
        calculos.calcularCuotas(c);
        c.folioContratoSolicitudes = secuencias.siguientes(AporteReinversionConstantes.SECUENCIA_FOLIO_CONTRATO, (long) entrada.p_SolicitudesAporte.size());
        AporteReinversionArmado.armar(c);
        if (!controles.requisitos(c)) {
            return false;
        }
        controles.montoInversion(c);
        controles.patrimonioEmisor(c);
        HashMap<String, BigDecimal> portTotalesMontos = new HashMap<>();
        NormalContratos.FlujoCajaSalida salidaFlujoCaja = controles.saldoFlujoCaja(c, portTotalesMontos);
        var salidaFlujo = controles.flujoCustodia(c);
        controles.controlar(c, salidaFlujoCaja, portTotalesMontos, salidaFlujo);
        if (!AporteReinversionConstantes.SI.equalsIgnoreCase(entrada.p_SimularOperacion)) {
            escritura.compuertas(c);
            return escritura.real(c);
        }
        escritura.simulada(c);
        return false;
    }
}
