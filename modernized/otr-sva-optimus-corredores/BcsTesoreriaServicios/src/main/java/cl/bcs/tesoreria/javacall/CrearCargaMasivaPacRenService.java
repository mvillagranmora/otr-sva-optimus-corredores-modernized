package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.carga.CargaMasivaPac.ClaveUniverso;
import cl.bcs.tesoreria.javacall.carga.CargaMasivaPacRen.DetalleRendicion;
import cl.bcs.tesoreria.javacall.carga.CargaMasivaPacRen.Entrada;
import cl.bcs.tesoreria.javacall.carga.CargaMasivaPacRen.Salida;
import cl.bcs.tesoreria.javacall.carga.ValidadorPreRendicion;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.AgregarPrograma;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

/**
 * Legacy {@code cl.bcs.tesoreria.CrearCargaMasivaPacRen} (TES-B-322..329): loads a PAC settlement (file type REN): the previous active REN rows become
 * INA, the detail lines (record type D) are inserted, then either the pre-settlement validation runs (p_simular S) or the settlement program is
 * scheduled (anything else).
 */
@Service
public class CrearCargaMasivaPacRenService {

    private static final Logger log = LoggerFactory.getLogger(CrearCargaMasivaPacRenService.class);

    private static final String SQL_INSERT_RENDICION_PAC = SqlNombrado.recurso("CrearCargaMasivaPacRen_InsertRendicionPac");
    private static final String SQL_UPDATE_ESTADO_ARCHIVO_PAC = SqlNombrado.recurso("CrearCargaMasivaPac_UpdateEstadoUniversoPac");

    /** The id the legacy used for every settlement line and as the base of the message ids. */
    private static final long ID_DETALLE_INICIAL = 225L;

    private final SqlNombrado sql;
    private final ConfiguracionSistema configuracion;
    private final ValidadorPreRendicion preRendicion;
    private final Colaboradores colaboradores;

    public CrearCargaMasivaPacRenService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, ValidadorPreRendicion preRendicion,
            Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.configuracion = configuracion;
        this.preRendicion = preRendicion;
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject jin) throws Exception {
        Gson gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    Salida logica(Entrada entrada) throws Exception {
        Salida salida = new Salida();
        List<DetalleRendicion> lineas = new ArrayList<>();
        ClaveUniverso rendicion = new ClaveUniverso();
        rendicion.tipo = "REN";
        long contador = ID_DETALLE_INICIAL;
        try {
            String fechaSistema = Plataforma.fechaSistema(configuracion, entrada.p_IdEmpresa);
            sql.lote(SQL_UPDATE_ESTADO_ARCHIVO_PAC, List.of(rendicion));

            for (DetalleRendicion cargaRen : entrada.p_CargaMasivaDetalle) {
                if (cargaRen.TipoRegistro.equalsIgnoreCase("D")) {
                    DetalleRendicion linea = new DetalleRendicion();
                    linea.IdCargaMasivaDetalle = ID_DETALLE_INICIAL;
                    linea.IdCargaMasivaPac = contador;
                    linea.CodigoBanco = cargaRen.CodigoBanco;
                    linea.CodigoEmpresa = cargaRen.CodigoEmpresa;
                    linea.CodigoConvenio = cargaRen.CodigoConvenio;
                    linea.TipoRegistro = cargaRen.TipoRegistro;
                    linea.IdentServicio = cargaRen.IdentServicio.replace(" ", "");
                    linea.InfAdicional = cargaRen.InfAdicional;
                    linea.MontoCargo = cargaRen.MontoCargo;
                    linea.FechaVencimiento = cargaRen.FechaVencimiento;
                    linea.FechaCargo = cargaRen.FechaCargo;
                    linea.EstadoCargo = cargaRen.EstadoCargo;
                    linea.CodigoRechazo = cargaRen.CodigoRechazo;
                    lineas.add(linea);
                    contador++;
                }
            }
            sql.lote(SQL_INSERT_RENDICION_PAC, lineas);

            // LEGACY DEFECT kept (see README TES-B-327): a null p_simular fails here, after the details were inserted
            if (entrada.p_simular.equalsIgnoreCase("S")) {
                JsonObject peticion = new JsonObject();
                peticion.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
                peticion.addProperty("p_IdModulo", entrada.p_IdModulo);
                peticion.addProperty("p_IdUsuario", entrada.p_IdUsuario);
                peticion.addProperty("p_simular", entrada.p_simular);
                JsonObject validacion = preRendicion.validaPreRendicion(peticion);

                for (JsonElement fila : validacion.getAsJsonArray("P_OUT_MENSAJES")) {
                    JsonObject m = fila.getAsJsonObject();
                    MensajeSalida msg = new MensajeSalida();
                    msg.id = contador;
                    msg.tipo = texto(m, "tipo");
                    msg.codigo = texto(m, "codigo");
                    msg.descripcion = texto(m, "descripcion");
                    salida.P_OUT_MENSAJES.add(msg);
                }
                // LEGACY DEFECT kept (see README TES-B-322): the error counter is never updated, it is always 0
                salida.P_OUT_ERRORESCONTADOR = 0L;
                salida.P_OUT_SOLICITUDES.addAll(validacion.getAsJsonArray("P_OUT_SOLICITUDES_NO_PROCESADAS"));
                salida.P_OUT_SOLICITUDES.addAll(validacion.getAsJsonArray("P_OUT_SOLICITUDES_PROCESADAS"));
            } else {
                // LEGACY DEFECT kept (see README TES-B-324): p_simular = N (or anything but S) SCHEDULES the settlement, S only validates
                AgregarPrograma.InAgregarPrograma programa = new AgregarPrograma.InAgregarPrograma();
                programa.p_IdEmpresa = entrada.p_IdEmpresa;
                programa.p_IdUsuario = entrada.p_IdUsuario;
                programa.p_IdModulo = entrada.p_IdModulo;
                programa.p_FechaSistema = fechaSistema;
                programa.p_Segundos = 0L;
                programa.p_IdTipoPrograma = "RENDICION_PAC";
                programa.p_Parametros.add(parametro("IdEmpresa", entrada.p_IdEmpresa.toString()));
                programa.p_Parametros.add(parametro("IdUsuario", entrada.p_IdUsuario.toString()));
                programa.p_Parametros.add(parametro("Simular", entrada.p_simular));

                AgregarPrograma.OutAgregarPrograma out = AgregarPrograma.invocar(colaboradores, programa);

                MensajeSalida msg = new MensajeSalida();
                msg.id = contador;
                msg.tipo = MensajeSalida.MENSAJE_INFORMACION;
                msg.codigo = MensajeSalida.MENSAJE_INFORMACION;
                msg.descripcion = "Proceso agendado OK, revisar Consulta Programas, ID " + out.P_OUT_IDPROGRAMA;
                salida.P_OUT_MENSAJES.add(msg);
            }
        } catch (Exception e) {
            log.error("CrearCargaMasivaPacRen error", e);
            throw e;
        }
        return salida;
    }

    private static AgregarPrograma.InAgregarPrograma.Parametro parametro(String nombre, String valor) {
        AgregarPrograma.InAgregarPrograma.Parametro p = new AgregarPrograma.InAgregarPrograma.Parametro();
        p.NombreParametro = nombre;
        p.Valor = valor;
        return p;
    }

    private static String texto(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }
}
