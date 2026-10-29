package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.javacall.pac.EntradaPac;
import cl.bcs.tesoreria.javacall.pac.EntradaPac.InIngresoPAC;
import cl.bcs.tesoreria.javacall.pac.MandatoPac;
import cl.bcs.tesoreria.javacall.pac.MandatoPacComun;
import cl.bcs.tesoreria.javacall.pac.MandatoPacComun.CuentaBancaria;
import cl.bcs.tesoreria.javacall.pac.SalidaPac;
import cl.bcs.tesoreria.javacall.pac.SalidaPac.ResultadoPac;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.ModificarPAC} (TES-B-120..134, 330): modifies an existing PAC mandate (state PAU, pending authorization) after validating the
 * identity document, bank account, CSC folio and the absence of another mandate with the same grouping folio and discount day. The request is decoded with
 * a plain Gson, as the legacy {@code call} did.
 */
@Service
public class ModificarPACService {

    private static final Logger log = LoggerFactory.getLogger(ModificarPACService.class);
    private static final Gson GSON = new Gson();

    private static final String SQL_SELECT_FOLIO_CSC = SqlNombrado.recurso("ModificarPAC_SelectFolioPACCSC");
    private static final String SQL_UPDATE_MANDATO = SqlNombrado.recurso("IngresoPAC_Update");

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ConfiguracionSistema configuracion;
    private final MandatoPacComun comun;
    private final Colaboradores colaboradores;

    public ModificarPACService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ConfiguracionSistema configuracion,
            MandatoPacComun comun, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.comun = comun;
        this.colaboradores = colaboradores;
    }

    /** JavaCall entry point: legacy {@code call(conn, json)}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaPac jin = GSON.fromJson(entrada.toString(), EntradaPac.class);
        SalidaPac out = modificar(jin);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaPac modificar(EntradaPac entrada) throws Exception {
        log.debug("INICIO Modificar Mandato PAC");
        SalidaPac out = new SalidaPac();

        if (entrada.p_IngresoPAC.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR,
                    "NO_SE_INGRESARON_SOLICITUDES_DE_TRASPASO", new HashMap<>()));
            return out;
        }

        RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        String fechaSistema = Plataforma.fechaSistema(configuracion, entrada.p_IdEmpresa);

        List<String> foliosCsc = new ArrayList<>();
        List<String> identServicios = new ArrayList<>();
        List<InIngresoPAC> solicitudesAProcesar = new ArrayList<>();
        // the bank of the LAST valid request is the one written to the CSC contract of every modified mandate (legacy behaviour)
        CuentaBancaria cuentaBancaria = new CuentaBancaria();

        for (InIngresoPAC mov : entrada.p_IngresoPAC) {
            boolean valido = true;

            foliosCsc.add(mov.FolioCSC);
            identServicios.add(mov.IdentServicio);

            List<CuentaBancaria> cuentas = comun.cuentaBancaria(mov.IdCuentaBancaria);

            // IdCliente 0 means the request comes from the web service: the client is identified by the identity document
            if (mov.IdCliente == 0L && !comun.existeDocumentoIdentidad(mov.DocumentoIdentidad)) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, mov.IdMandatoPACBase, MensajeSalida.MENSAJE_ADVERTENCIA,
                        "DOCUMENTO_IDENTIDAD_NO_EXISTE", new HashMap<>()));
                valido = false;
            }
            if (cuentas.isEmpty()) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, mov.IdMandatoPACBase, MensajeSalida.MENSAJE_ADVERTENCIA,
                        "CUENTA_CORRIENTE_NO_EXISTE", new HashMap<>()));
                valido = false;
            } else {
                cuentaBancaria = cuentas.get(0);
            }

            if (valido) {
                solicitudesAProcesar.add(mov);
            }
        }

        List<MandatoPac> existentesCsc = sql.consultar(SQL_SELECT_FOLIO_CSC, Map.of("FolioCSC", foliosCsc), MandatoPac.class);
        List<MandatoPac> mismoIdentServicio = comun.mandatosConMismoIdentServicio(identServicios);

        List<MandatoPac> modificados = new ArrayList<>();

        for (InIngresoPAC ingreso : solicitudesAProcesar) {
            List<MandatoPac> otrosConMismoFolio = new ArrayList<>();
            if (!existentesCsc.isEmpty()) {
                existentesCsc.stream()
                        .filter(m -> m.FolioCSC.equalsIgnoreCase(ingreso.FolioCSC) && m.IdMandatoPAC.compareTo(ingreso.IdMandatoPACBase) != 0)
                        .forEach(otrosConMismoFolio::add);
            }

            if (!otrosConMismoFolio.isEmpty() && ingreso.EstadoMandato.equalsIgnoreCase("ING")) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ADVERTENCIA, "FOLIO_CSC_YA_EXISTE", new HashMap<>()));
                continue;
            }

            // mandates of the same grouping folio, discount day and client (other than the one modified); the CSC match, when there is one, wins the tipoContrato
            List<MandatoPac> iguales = new ArrayList<>();
            String tipoContrato = "";
            if (!ingreso.FolioSAAPV.equalsIgnoreCase("0")) {
                mismoIdentServicio.stream()
                        .filter(m -> m.IdentServicio.equalsIgnoreCase(ingreso.IdentServicio) && m.DiaDescuentoPorAutorizar.compareTo(ingreso.DiaDescuento) == 0
                                && m.IdCliente.compareTo(ingreso.IdCliente) == 0 && !m.FolioSAAPV.equalsIgnoreCase("0")
                                && m.IdMandatoPAC.compareTo(ingreso.IdMandatoPACBase) != 0)
                        .forEach(iguales::add);
                tipoContrato = "SAAPV";
            }
            if (!ingreso.FolioCSC.equalsIgnoreCase("0")) {
                mismoIdentServicio.stream()
                        .filter(m -> m.IdentServicio.equalsIgnoreCase(ingreso.IdentServicio) && m.DiaDescuentoPorAutorizar.compareTo(ingreso.DiaDescuento) == 0
                                && m.IdCliente.compareTo(ingreso.IdCliente) == 0 && !m.FolioCSC.equalsIgnoreCase("0")
                                && m.IdMandatoPAC.compareTo(ingreso.IdMandatoPACBase) != 0)
                        .forEach(iguales::add);
                tipoContrato = "CSC";
            }

            if (!iguales.isEmpty()) {
                Map<String, String> valores = new HashMap<>();
                valores.put("TipoContrato", tipoContrato);
                valores.put("FolioAgrupador", iguales.get(0).IdentServicio);
                valores.put("DiasDescuento", iguales.get(0).DiaDescuentoPorAutorizar.toString());
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ADVERTENCIA,
                        "MANDATOS_PAC_CON_IGUAL_FOLIO_AGRUPADOR_Y_DIAS_DESCUENTO", valores));
                continue;
            }

            MandatoPac mandato = new MandatoPac();
            mandato.IdCliente = ingreso.IdCliente;
            mandato.IdPortafolio = ingreso.IdPortafolio;
            mandato.FolioCSC = ingreso.FolioCSC;
            mandato.CuentaCorriente = ingreso.CuentaCorriente;
            mandato.FolioSAAPV = ingreso.FolioSAAPV;
            mandato.Receptor = ingreso.Receptor;
            mandato.Fecha = ingreso.Fecha;
            mandato.NumeroMandato = ingreso.NumeroMandato;
            mandato.Firma = ingreso.Firma;
            mandato.IdentServicio = ingreso.IdentServicio;
            mandato.IdEmpresa = ingreso.IdEmpresa;
            mandato.Canal = ingreso.Canal;
            mandato.IdMandatoPACBase = ingreso.IdMandatoPACBase;
            mandato.IdCuentaBancaria = ingreso.IdCuentaBancaria;
            mandato.EstadoPorAutorizar = ingreso.EstadoMandato;
            mandato.DiaDescuentoPorAutorizar = ingreso.DiaDescuento;
            mandato.EstadoContratoPorAutorizar = ingreso.EstadoContrato;
            mandato.Estado = "PAU";

            if (ingreso.TipoPago.equals("N")) {
                comun.modificarCuentaBancaria(entrada, ingreso.IdCuentaBancaria);
            }

            String glosa = "Modificacion Contrato PAC" + " " + "IdMandato:" + mandato.IdMandatoPACBase;
            out.P_OUT_MENSAJES.addAll(comun.crearEventoAutorizacion(entrada, fechaSistema, glosa, Long.toString(mandato.IdMandatoPACBase)));

            modificados.add(mandato);

            ResultadoPac resultado = new ResultadoPac();
            resultado.IdMandatoPAC = mandato.IdMandatoPACBase;
            resultado.FolioPAC = mandato.IdMandatoPACBase;
            out.P_OUT_RESULTADO.add(resultado);
        }

        if (!modificados.isEmpty()) {
            sql.lote(SQL_UPDATE_MANDATO, modificados);
            for (MandatoPac salida : modificados) {
                // LEGACY DEFECT kept (see README TES-B-330): the legacy compares the String FolioCSC with the Long 0 (equals(0L)), which is always false,
                // so the bank of the contract is updated even when the mandate has no CSC folio ("0")
                Object cero = 0L;
                if (!salida.FolioCSC.equals(cero)) {
                    comun.actualizarBanco(cuentaBancaria.IdBanco, salida.FolioCSC);
                }
                MensajeSalida msg = new MensajeSalida();
                msg.id = salida.IdMandatoPACBase;
                msg.tipo = MensajeSalida.MENSAJE_AUTORIZACION;
                msg.codigo = "MODIFICACION_MANDATO_PAC_OK, FOLIO: " + salida.IdMandatoPACBase;
                msg.descripcion = "Modificacion de Mandato PAC exitosa, FOLIO: " + salida.IdMandatoPACBase;
                out.P_OUT_MENSAJES.add(msg);
            }
        }

        comun.agregarLog(entrada, fechaSistema, "MODIFICACION MANDATO PAC", "MODIFICACION_MANDATO_PAC");
        return out;
    }
}
