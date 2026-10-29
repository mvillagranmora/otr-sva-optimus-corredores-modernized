package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.pac.EntradaPac;
import cl.bcs.tesoreria.javacall.pac.EntradaPac.InIngresoPAC;
import cl.bcs.tesoreria.javacall.pac.SalidaPac;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.IngresarMandatoPACManejador} (TES-B-267..276): validates each PAC request for null ids and delegates the valid ones, one by
 * one, to {@link IngresarMandatoPACService}; the answer lists, per request, the folio and the messages. Plain Gson, as the legacy {@code call}.
 */
@Service
public class IngresarMandatoPACManejadorService {

    private static final Logger log = LoggerFactory.getLogger(IngresarMandatoPACManejadorService.class);
    private static final Gson GSON = new Gson();

    public static class SalidaManejador {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<EntidadMandato> MANDATO = new ArrayList<>();
    }

    public static class EntidadMandato {
        public Long IdFolioPAC;
        public List<MensajesErrores> Mensajes = new ArrayList<>();
    }

    public static class MensajesErrores {
        public Long IdInterno;
        public String Tipo;
        public String Codigo;
        public String Descripcion;
    }

    private final DiccionarioMensajes diccionario;
    private final IngresarMandatoPACService ingresar;

    public IngresarMandatoPACManejadorService(DiccionarioMensajes diccionario, IngresarMandatoPACService ingresar) {
        this.diccionario = diccionario;
        this.ingresar = ingresar;
    }

    /** JavaCall entry point: legacy {@code call(conn, json)}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaPac jin = GSON.fromJson(entrada.toString(), EntradaPac.class);
        SalidaManejador out = manejar(jin);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaManejador manejar(EntradaPac entrada) throws Exception {
        log.debug("INICIO IngresarMandatoPACManejador");
        SalidaManejador out = new SalidaManejador();
        List<SalidaPac> salidas = new ArrayList<>();

        try {
            for (InIngresoPAC mov : entrada.p_IngresoPAC) {
                if (mov.IdCuentaBancaria == null) {
                    salidas.add(conError(entrada, "CUENTA_BANCARIA_NO_ENCONTRADA"));
                } else if (mov.TipoPago == null) {
                    salidas.add(conError(entrada, "TIPO_PAGO_NO_ENCONTRADO"));
                } else if (mov.IdCliente == null) {
                    salidas.add(conError(entrada, "CLIENTE_NO_ENCONTRADO"));
                } else if (mov.IdPortafolio == null) {
                    salidas.add(conError(entrada, "PORTAFOLIO_NO_ENCONTRADO"));
                } else {
                    EntradaPac unitaria = new EntradaPac();
                    InIngresoPAC mandato = new InIngresoPAC();
                    mandato.IdCliente = mov.IdCliente;
                    mandato.IdPortafolio = mov.IdPortafolio;
                    mandato.FolioCSC = mov.FolioCSC;
                    mandato.CuentaCorriente = mov.CuentaCorriente;
                    mandato.FolioSAAPV = mov.FolioSAAPV;
                    mandato.FolioPACMandatoAgrupador = mov.FolioPACMandatoAgrupador;
                    mandato.Receptor = mov.Receptor;
                    mandato.Fecha = mov.Fecha;
                    mandato.NumeroMandato = mov.NumeroMandato;
                    mandato.Firma = mov.Firma;
                    mandato.IdentServicio = mov.IdentServicio;
                    mandato.Estado = mov.Estado;
                    mandato.EstadoCargo = mov.EstadoCargo;
                    mandato.IdEmpresa = mov.IdEmpresa;
                    mandato.IdCuentaBancaria = mov.IdCuentaBancaria;
                    mandato.Canal = mov.Canal;
                    mandato.TipoPago = mov.TipoPago;
                    mandato.DocumentoIdentidad = mov.DocumentoIdentidad;
                    unitaria.p_IngresoPAC.add(mandato);

                    unitaria.p_IdEmpresa = entrada.p_IdEmpresa;
                    unitaria.p_IdModulo = entrada.p_IdModulo;
                    unitaria.p_IdUsuario = entrada.p_IdUsuario;
                    unitaria.p_SimularOperacion = entrada.p_SimularOperacion;
                    unitaria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;

                    salidas.add(ingresar.ingresar(unitaria));
                }
            }

            for (SalidaPac salida : salidas) {
                EntidadMandato entidad = new EntidadMandato();
                for (MensajeSalida m : salida.P_OUT_MENSAJES) {
                    MensajesErrores mensaje = new MensajesErrores();
                    mensaje.IdInterno = m.id;
                    mensaje.Tipo = m.tipo;
                    mensaje.Codigo = m.codigo;
                    mensaje.Descripcion = m.descripcion;
                    entidad.Mensajes.add(mensaje);
                }

                // the folio is IdMandatoPAC of the first result, or 0 (the ternary unboxes: a null IdMandatoPAC would be a NullPointerException, as in the legacy)
                entidad.IdFolioPAC = salida.P_OUT_RESULTADO.size() > 0 ? salida.P_OUT_RESULTADO.get(0).IdMandatoPAC : 0;

                out.MANDATO.add(entidad);
                if (salida.P_OUT_ERRORESCONTADOR > 0) {
                    out.P_OUT_ERRORESCONTADOR++;
                }
            }
            return out;
        } catch (Exception error) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            throw error;
        }
    }

    private SalidaPac conError(EntradaPac entrada, String codigo) {
        SalidaPac salida = new SalidaPac();
        salida.P_OUT_ERRORESCONTADOR++;
        salida.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>()));
        return salida;
    }
}
