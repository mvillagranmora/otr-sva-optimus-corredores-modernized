package cl.bcs.tesoreria.javacall;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.AnulacionMandatoPACService.InContrato;
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
import cl.bcs.tesoreria.port.out.Secuencias;

/**
 * Legacy {@code cl.bcs.tesoreria.IngresarMandatoPAC} (TES-B-97..119): registers new PAC mandates (state PAU, pending authorization ING). Per request it
 * validates the identity document and bank account, rejects folios that already exist, takes the discount day from the CSC / SAAPV contract when it is not
 * informed, annuls the active mandates of the same grouping folio / discount day / client, creates the NUEPAC authorization event and updates the bank of the
 * CSC contract. The request is decoded with a plain Gson, as the legacy {@code call} did.
 */
@Service
public class IngresarMandatoPACService {

    private static final Logger log = LoggerFactory.getLogger(IngresarMandatoPACService.class);
    private static final Gson GSON = new Gson();

    private static final String SEQ_ID_MANDATO_PAC = "BSCL.Tesoreria.BD.Secuencias::IdMandatoPAC";

    private static final String SQL_INSERT_MANDATO = SqlNombrado.recurso("IngresoPAC_InsertMandato");
    private static final String SQL_SELECT_FOLIO_CSC = SqlNombrado.recurso("IngresarMandatoPAC_SelectFolioPACCSC");
    private static final String SQL_SELECT_FOLIO_SAAPV = SqlNombrado.recurso("IngresarMandatoPAC_SelectFolioPACSAAPV");
    private static final String SQL_SELECT_DIA_DESCUENTO_CONTRATOS = SqlNombrado.recurso("IngresarMandatoPAC_SelectDiaDescuentoContratos");

    /** Discount day of a CSC / SAAPV contract, keyed by {@code Hash = <folio>_<CSC|SAAPV>}. */
    public static class DiaDescuentoContrato {
        public String FolioContrato;
        public Long DiaDescuento;
        public String TipoContrato;
        public String Hash;
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final MandatoPacComun comun;
    private final AnulacionMandatoPACService anulacion;
    private final Colaboradores colaboradores;

    public IngresarMandatoPACService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ConfiguracionSistema configuracion,
            Secuencias secuencias, MandatoPacComun comun, AnulacionMandatoPACService anulacion, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.comun = comun;
        this.anulacion = anulacion;
        this.colaboradores = colaboradores;
    }

    /** JavaCall entry point: legacy {@code call(conn, json)}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaPac jin = GSON.fromJson(entrada.toString(), EntradaPac.class);
        SalidaPac out = ingresar(jin);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    /** Legacy {@code logica}; also the entry of IngresarMandatoPACManejador. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaPac ingresar(EntradaPac entrada) throws Exception {
        log.debug("INICIO CrearIngresoPAC");
        SalidaPac out = new SalidaPac();

        if (entrada.p_IngresoPAC.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR,
                    "NO_SE_INGRESARON_SOLICITUDES_DE_TRASPASO", new HashMap<>()));
            return out;
        }

        RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0) {
            // LEGACY DEFECT kept (see README TES-B-101): the basic-requirement messages are not copied to the output, only the counter is set
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        String fechaSistema = Plataforma.fechaSistema(configuracion, entrada.p_IdEmpresa);

        List<String> foliosCsc = new ArrayList<>();
        List<String> foliosSaapv = new ArrayList<>();
        List<String> identServicios = new ArrayList<>();
        List<InIngresoPAC> solicitudesAProcesar = new ArrayList<>();
        // the bank of the LAST valid request is the one written to the CSC contracts of every new mandate (legacy behaviour)
        CuentaBancaria cuentaBancaria = new CuentaBancaria();

        for (InIngresoPAC mov : entrada.p_IngresoPAC) {
            boolean valido = true;

            // IdentServicio is the grouping folio: keep digits only, "0" when empty
            mov.IdentServicio = mov.IdentServicio != null && !mov.IdentServicio.isEmpty() ? mov.IdentServicio.replaceAll("[^0-9]+", "") : "0";

            foliosCsc.add(mov.FolioCSC);
            foliosSaapv.add(mov.FolioSAAPV);
            identServicios.add(mov.IdentServicio);

            List<CuentaBancaria> cuentas = comun.cuentaBancaria(mov.IdCuentaBancaria);

            // IdCliente 0 means the request comes from the web service: the client is identified by the identity document
            if (mov.IdCliente == 0L && !comun.existeDocumentoIdentidad(mov.DocumentoIdentidad)) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ADVERTENCIA,
                        "DOCUMENTO_IDENTIDAD_NO_EXISTE", new HashMap<>()));
                valido = false;
            }
            if (cuentas.isEmpty()) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ADVERTENCIA,
                        "CUENTA_CORRIENTE_NO_EXISTE", new HashMap<>()));
                valido = false;
            } else {
                cuentaBancaria = cuentas.get(0);
            }

            if (valido) {
                solicitudesAProcesar.add(mov);
            }
        }

        // LEGACY DEFECT kept (see README TES-B-114): both folio queries are SELECT TOP 1, so only the first existing mandate of the whole batch is detected
        Map<String, MandatoPac> csc = indexar(sql.consultar(SQL_SELECT_FOLIO_CSC, Map.of("FolioCSC", foliosCsc), MandatoPac.class), m -> m.FolioCSC);
        Map<String, MandatoPac> saapv = indexar(sql.consultar(SQL_SELECT_FOLIO_SAAPV, Map.of("FolioSAAPV", foliosSaapv), MandatoPac.class), m -> m.FolioSAAPV);
        List<MandatoPac> mismoIdentServicio = comun.mandatosConMismoIdentServicio(identServicios);
        Map<String, DiaDescuentoContrato> diasDescuento = indexar(sql.consultar(SQL_SELECT_DIA_DESCUENTO_CONTRATOS,
                Map.of("FolioContratoSAAPV", foliosSaapv, "FolioCSC", foliosCsc), DiaDescuentoContrato.class), c -> c.Hash);

        // one id per request (also for the rejected ones), consumed in order by the accepted requests
        List<Long> idsMandato = new ArrayList<>();
        for (int n = 0; n < entrada.p_IngresoPAC.size(); n++) {
            idsMandato.add(secuencias.siguiente(SEQ_ID_MANDATO_PAC));
        }
        int i = 0;

        List<MandatoPac> nuevos = new ArrayList<>();
        List<MandatoPac> porAnular = new ArrayList<>();

        for (InIngresoPAC ingreso : solicitudesAProcesar) {
            // a null FolioCSC / FolioSAAPV is a NullPointerException here and below (legacy, see README TES-B-115/116)
            if (csc.get(ingreso.FolioCSC.toString()) != null) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ADVERTENCIA, "FOLIO_CSC_YA_EXISTE", new HashMap<>()));
                continue;
            }
            if (saapv.get(ingreso.FolioSAAPV) != null) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ADVERTENCIA, "FOLIO_SAAPV_YA_EXISTE", new HashMap<>()));
                continue;
            }

            // LEGACY DEFECT kept (see README TES-B-112/113): with both folios informed the discount day is never looked up and stays null
            if (ingreso.DiaDescuento == null) {
                if (ingreso.FolioCSC.equalsIgnoreCase("0") && !ingreso.FolioSAAPV.equalsIgnoreCase("0")) {
                    DiaDescuentoContrato contrato = diasDescuento.get(ingreso.FolioSAAPV + "_" + "SAAPV");
                    ingreso.DiaDescuento = contrato != null ? contrato.DiaDescuento : 0L;
                }
                if (!ingreso.FolioCSC.equalsIgnoreCase("0") && ingreso.FolioSAAPV.equalsIgnoreCase("0")) {
                    DiaDescuentoContrato contrato = diasDescuento.get(ingreso.FolioCSC + "_" + "CSC");
                    ingreso.DiaDescuento = contrato != null ? contrato.DiaDescuento : 0L;
                }
            }

            // mandates of the same grouping folio, discount day and client, with a folio of the same contract kind, are replaced by the new one
            if (!ingreso.FolioSAAPV.equalsIgnoreCase("0")) {
                mismoIdentServicio.stream()
                        .filter(m -> m.IdentServicio.equalsIgnoreCase(ingreso.IdentServicio) && m.DiaDescuentoPorAutorizar.compareTo(ingreso.DiaDescuento) == 0
                                && m.IdCliente.compareTo(ingreso.IdCliente) == 0 && !m.FolioSAAPV.equalsIgnoreCase("0"))
                        .forEach(porAnular::add);
            }
            if (!ingreso.FolioCSC.equalsIgnoreCase("0")) {
                mismoIdentServicio.stream()
                        .filter(m -> m.IdentServicio.equalsIgnoreCase(ingreso.IdentServicio) && m.DiaDescuentoPorAutorizar.compareTo(ingreso.DiaDescuento) == 0
                                && m.IdCliente.compareTo(ingreso.IdCliente) == 0 && !m.FolioCSC.equalsIgnoreCase("0"))
                        .forEach(porAnular::add);
            }

            MandatoPac mandato = new MandatoPac();
            mandato.IdMandatoPAC = idsMandato.get(i);
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
            mandato.IdCuentaBancaria = ingreso.IdCuentaBancaria;
            mandato.EstadoPorAutorizar = "ING";
            mandato.DiaDescuentoPorAutorizar = ingreso.DiaDescuento;
            mandato.EstadoContratoPorAutorizar = ingreso.EstadoContrato;
            mandato.Estado = "PAU";

            if (ingreso.TipoPago.equals("N")) {
                comun.modificarCuentaBancaria(entrada, ingreso.IdCuentaBancaria);
            }

            String glosa = "Ingreso Contrato PAC" + " " + "IdMandato:" + mandato.IdMandatoPAC;
            out.P_OUT_MENSAJES.addAll(comun.crearEventoAutorizacion(entrada, fechaSistema, glosa, Long.toString(mandato.IdMandatoPAC)));

            nuevos.add(mandato);
            i++;

            ResultadoPac resultado = new ResultadoPac();
            resultado.IdMandatoPAC = mandato.IdMandatoPAC;
            out.P_OUT_RESULTADO.add(resultado);
        }

        if (!porAnular.isEmpty()) {
            anularMandatos(entrada, porAnular);
        }

        if (!nuevos.isEmpty()) {
            sql.lote(SQL_INSERT_MANDATO, nuevos);
            for (MandatoPac salida : nuevos) {
                if (!salida.FolioCSC.equals("0")) {
                    comun.actualizarBanco(cuentaBancaria.IdBanco, salida.FolioCSC);
                }
                MensajeSalida msg = new MensajeSalida();
                msg.id = salida.IdMandatoPAC;
                msg.tipo = MensajeSalida.MENSAJE_AUTORIZACION;
                msg.codigo = "CREACION_MANDATO_PAC_OK, FOLIO: " + salida.IdMandatoPAC;
                msg.descripcion = "Creacion de Mandato PAC exitosa, FOLIO: " + salida.IdMandatoPAC;
                out.P_OUT_MENSAJES.add(msg);
            }
        }

        comun.agregarLog(entrada, fechaSistema, "INGRESO MANDATO PAC", "INGRESO_MANDATO_PAC");
        return out;
    }

    private void anularMandatos(EntradaPac entrada, List<MandatoPac> mandatos) throws Exception {
        AnulacionMandatoPACService.EntradaAnulacionContrato anular = new AnulacionMandatoPACService.EntradaAnulacionContrato();
        for (MandatoPac m : mandatos) {
            InContrato contrato = new InContrato();
            contrato.IdContrato = m.IdMandatoPAC;
            contrato.Estado = "ANU";
            anular.p_Contrato.add(contrato);
        }
        anular.p_IdEmpresa = entrada.p_IdEmpresa;
        anular.p_IdUsuario = entrada.p_IdUsuario;
        anular.p_IdModulo = entrada.p_IdModulo;
        anulacion.anular(anular);
    }

    /** Indexes rows by key; a repeated key is an error (legacy {@code Utils.List2Hash}). */
    private static <T> Map<String, T> indexar(List<T> filas, Function<T, String> clave) throws Exception {
        Map<String, T> indice = new HashMap<>();
        for (T fila : filas) {
            String k = clave.apply(fila);
            if (indice.containsKey(k)) {
                throw new Exception("key field is not unique");
            }
            indice.put(k, fila);
        }
        return indice;
    }
}
