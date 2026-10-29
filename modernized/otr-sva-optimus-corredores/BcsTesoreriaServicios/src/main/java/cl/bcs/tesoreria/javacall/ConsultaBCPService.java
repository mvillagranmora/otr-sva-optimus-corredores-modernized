package cl.bcs.tesoreria.javacall;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.bcp.LogMensajeTransaccionBcp;
import cl.bcs.tesoreria.javacall.bcp.LogMensajeTransaccionBcp.Registro;
import cl.bcs.tesoreria.javacall.bcp.ProcesoBcp;
import cl.bcs.tesoreria.javacall.bcp.ProcesoBcp.Cliente;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.ConsultaBCP} (TES-B-89..95): BCP asks whether the depositor ids it sends exist and are enabled in Optimus.
 * The inquiry is logged (LOG.MensajeTransaccionBCP), each id is validated and the client data or a BCP error code is answered; the answer
 * is stored in the same log row.
 */
@Service
public class ConsultaBCPService {

    private static final Long ID_EMPRESA = 1L; // legacy constant

    /** MFX_SvcRecaudaInq request. */
    public static class ConsultaBcpEntrada {
        public String IdSolicitudBcp;                                  // SvcRecaudaInqRq/RqUID
        public String MensajeEntrada;                                  // whole message sent by BCP (as json)
        public List<ConsultaEntrada> ConsultasBcp = new ArrayList<>(); // SvcEmpLst/SvcEmp
    }

    public static class ConsultaEntrada {
        public String IdServicioBcp; // SvcId
        public String IdClienteBcp;  // PagId: depositor id
    }

    /** MFX_SvcRecaudaInq answer; the field order is the order of the json stored in the log. */
    public static class ConsultaBcpSalida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_IDENTIFICADORSOLICITUD;
        public Timestamp P_OUT_FECHAPROCESO;
        public String P_OUT_NUMEROOPERACIONOPTIMUS;
        public List<ResultadoConsulta> P_OUT_RESULTADOCONSULTA = new ArrayList<>();

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ResultadoConsulta {
        public String IdServicioBcp;
        public String IdClienteBcp;
        public String NombreCliente;
        public String TipoDocumentoIdentidad; // BCP code: 1 DNI, 6 RUC
        public String DocumentoIdentidad;
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ConfiguracionSistema configuracion;
    private final LogMensajeTransaccionBcp log;

    public ConsultaBCPService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ConfiguracionSistema configuracion,
            LogMensajeTransaccionBcp log) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.log = log;
    }

    /** Legacy {@code call}: the request is the JSON of {@link ConsultaBcpEntrada}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        ConsultaBcpEntrada in = JsonLegacy.de(entrada, ConsultaBcpEntrada.class);
        ConsultaBcpSalida out = consultar(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(JsonLegacy.gson().toJson(out)).getAsJsonObject();
    }

    ConsultaBcpSalida consultar(ConsultaBcpEntrada in) throws Exception {
        ConsultaBcpSalida out = new ConsultaBcpSalida();
        String fechaSistema = Plataforma.fechaSistema(configuracion, ID_EMPRESA);

        // the inquiry is logged first and the logged row is read back (its id is the Optimus operation number)
        log.registrar(in.IdSolicitudBcp, "CONSULTA", in.MensajeEntrada, fechaSistema, ID_EMPRESA);
        Map<String, Registro> registros = log.buscar(ID_EMPRESA, "CONSULTA", in.IdSolicitudBcp);
        if (registros.isEmpty()) {
            out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, ID_EMPRESA, 0L, MensajeSalida.MENSAJE_ERROR, "ERROR_EN_REGISTRO_MENSAJE_ENTRADA_BCP", null));
            return out;
        }
        // the lookup is by the requested id while the map is keyed by the stored RqUID: a request that differs only in case finds nothing (NullPointerException)
        Registro registro = registros.get(in.IdSolicitudBcp);
        out.P_OUT_NUMEROOPERACIONOPTIMUS = registro.IdMensajeTransaccion.toString();

        for (ConsultaEntrada c : in.ConsultasBcp) {
            c.IdClienteBcp = ProcesoBcp.sinCerosIzquierda(c.IdClienteBcp);
        }
        Map<String, Cliente> clientes = ProcesoBcp.consultarClientes(sql, ID_EMPRESA, in.ConsultasBcp.stream().map(c -> c.IdClienteBcp).toList());

        List<ProcesoBcp> procesos = new ArrayList<>();
        for (ConsultaEntrada c : in.ConsultasBcp) {
            String llave = ProcesoBcp.llaveCliente(ID_EMPRESA, c.IdClienteBcp);
            ProcesoBcp p = new ProcesoBcp();
            p.IdServicioBcp = c.IdServicioBcp;
            p.IdClienteBcp = c.IdClienteBcp;
            p.asignarCliente(llave, clientes.get(llave), registro.IdMensajeTransaccion);
            procesos.add(p);
        }
        for (ProcesoBcp p : procesos) {
            // CP0010: depositor does not exist; CP0141: depositor restricted for inquiry
            List<MensajeSalida> m = out.P_OUT_MENSAJES;
            p.requisito(diccionario, ID_EMPRESA, p.LlaveCliente == null || p.LlaveCliente.isEmpty(), "TIPO_DOCUMENTO_IDENTIDAD_DESCONOCIDO", "CP0010", true, m);
            p.requisito(diccionario, ID_EMPRESA, p.IdPersona == null || p.IdPersona == 0, "PERSONA_NO_EXISTE", "CP0010", true, m);
            p.requisito(diccionario, ID_EMPRESA, (p.IdCliente == null || p.IdCliente == 0) && p.IdPersona != 0, "CLIENTE_NO_EXISTE", "CP0141", true, m);
            p.requisito(diccionario, ID_EMPRESA, !p.IdEstadoPersona.equalsIgnoreCase("ACT") && p.IdPersona != 0, "PERSONA_NO_ACTIVA", "CP0141", true, m);
            p.requisito(diccionario, ID_EMPRESA, !p.IdEstadoCliente.equalsIgnoreCase("ACT") && p.IdPersona != 0 && p.IdCliente != 0, "CLIENTE_NO_ACTIVO", "CP0141", true, m);
            p.requisito(diccionario, ID_EMPRESA, p.IdPortafolioDefecto == 0 && p.IdPersona != 0 && p.IdCliente != 0, "PORTAFOLIO_DEFECTO_NO_EXISTE", "CP0141", true, m);
        }

        // the log row keeps the answer as it is at this point (identifier, time and results are filled afterwards, as in the legacy)
        registro.MensajeSalida = out.toString();
        log.guardarSalida(List.of(registro));

        out.P_OUT_IDENTIFICADORSOLICITUD = in.IdSolicitudBcp;
        out.P_OUT_FECHAPROCESO = registro.FechaHoraRecepcion;
        for (ProcesoBcp p : procesos) {
            ResultadoConsulta r = new ResultadoConsulta();
            r.IdServicioBcp = p.IdServicioBcp;
            r.IdClienteBcp = p.IdClienteBcp;
            r.NombreCliente = p.NombreCliente;
            r.DocumentoIdentidad = p.DocumentoIdentidad;
            switch (p.IdTipoDocumentoIdentidad) { // null throws NullPointerException; another type leaves it null
                case "DNI" -> r.TipoDocumentoIdentidad = "1";
                case "RUC" -> r.TipoDocumentoIdentidad = "6";
                default -> { }
            }
            out.P_OUT_RESULTADOCONSULTA.add(r);
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return out;
    }
}
