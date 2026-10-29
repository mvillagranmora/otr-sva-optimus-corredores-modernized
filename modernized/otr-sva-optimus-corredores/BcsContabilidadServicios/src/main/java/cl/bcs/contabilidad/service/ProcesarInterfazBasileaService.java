package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.model.basilea.CuentaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.CuentaPropia;
import cl.bcs.contabilidad.domain.model.basilea.DatoBusquedaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.RegistroInterfazBasilea;
import cl.bcs.contabilidad.domain.port.out.InterfazBasileaRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.basilea.InterfazBasileaException;
import cl.bcs.contabilidad.domain.service.basilea.InterfazBasileaMapper;
import cl.bcs.contabilidad.domain.service.basilea.ResolucionCuentaBasilea;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basilea interface load: legacy {@code cl.bcs.contabilidad.ProcesarInterfazBasilea.logica()}
 * (ProcesarInterfazBasilea.java:278-345), rules CONT-D-75..81.
 *
 * <p>Reads the system date, the active Basilea accounts and the broker's own account, then the positions in the order
 * acciones, RF-IF, ventas cortas, pactos, forward. Each position is resolved to one account:
 * <ul>
 *   <li>more than one account: one ERROR and the loop stops;</li>
 *   <li>none: ADVERTENCIA "No se encontró cuenta para los criterios G_P_TI_CTI" and the position is skipped;</li>
 *   <li>an account configured empty: ADVERTENCIA "No existe cuenta configurada para grupo G, producto P", and the
 *       record is still inserted with that empty account.</li>
 * </ul>
 * Messages are deduplicated by the legacy keys (G_P, G*P*TI*CTI, G-P). The records are inserted only when there is no
 * error. Messages use the legacy 4-argument MensajeSalida: codigo equals tipo and descripcion is the literal text.
 *
 * <p>Approved deviation: where the legacy crashed (no own account, IndexOutOfBounds at :291; an unparseable RUT; a null
 * filter value) the service answers one controlled ERROR and inserts nothing.
 */
@Service
public class ProcesarInterfazBasileaService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarInterfazBasileaService.class);

    private final InterfazBasileaRepository repository;
    private final PlataformaPort plataforma;

    public ProcesarInterfazBasileaService(InterfazBasileaRepository repository, PlataformaPort plataforma) {
        this.repository = repository;
        this.plataforma = plataforma;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo) {}

    record Salida(@SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    /** JavaCall entry point; legacy {@code call()} used {@code getGsonBuilder()}, which writes nulls. */
    public JsonObject execute(JsonObject params) {
        Entrada in = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), procesar(in.idEmpresa()));
    }

    Salida procesar(Long idEmpresa) {
        try {
            return cargar(idEmpresa);
        } catch (InterfazBasileaException e) {
            log.warn("ProcesarInterfazBasilea empresa {}: {}", idEmpresa, e.getMessage());
            return new Salida(1L, List.of(mensaje(MensajeSalida.ERROR, e.getMessage())));
        }
    }

    private Salida cargar(Long idEmpresa) {
        String fechaSistema = plataforma.obtenerFechaSistema(idEmpresa);
        List<CuentaBasilea> cuentas = repository.consultarCuentasBasileaActivas();
        CuentaPropia propia = repository.consultarCuentaPropia(idEmpresa).stream().findFirst()
                .orElseThrow(() -> new InterfazBasileaException(
                        "Basilea sin cuenta propia: no hay cuenta propia configurada para la empresa " + idEmpresa));

        List<DatoBusquedaBasilea> datos = new ArrayList<>();
        datos.addAll(repository.consultarAcciones(propia.idClienteCtaPropia()));
        datos.addAll(repository.consultarRfIf(propia.idClienteCtaPropia(), fechaSistema));
        datos.addAll(repository.consultarVentasCortas(propia.idClienteCtaPropia(), fechaSistema));
        datos.addAll(repository.consultarPactosVigentes(fechaSistema));
        datos.addAll(repository.consultarForward(fechaSistema));

        long errores = 0;
        Map<String, MensajeSalida> mensajes = new LinkedHashMap<>();
        List<RegistroInterfazBasilea> registros = new ArrayList<>();
        for (DatoBusquedaBasilea dato : datos) {
            ResolucionCuentaBasilea r = InterfazBasileaMapper.buscarCuenta(dato, cuentas);
            if (r.resultado() == ResolucionCuentaBasilea.Resultado.MULTIPLE) {
                errores++;
                mensajes.putIfAbsent(dato.grupo() + "_" + dato.producto(), mensaje(MensajeSalida.ERROR, String.format(
                        "Error al Procesar %s, grupo: %s, se obtuvo más de una cuenta basilea en el filtrado",
                        dato.producto(), dato.grupo())));
                break;
            }
            if (r.resultado() == ResolucionCuentaBasilea.Resultado.SIN_CUENTA) {
                mensajes.putIfAbsent(String.join("*", dato.grupo(), dato.producto(), dato.idTipoInstrumento(),
                                dato.idClasificacionTipoInstrumento()),
                        mensaje(MensajeSalida.ADVERTENCIA, String.format("No se encontró cuenta para los criterios %s_%s_%s_%s",
                                dato.grupo(), dato.producto(), dato.idTipoInstrumento(),
                                dato.idClasificacionTipoInstrumento())));
                continue;
            }
            if (r.cuentaContable().isEmpty()) {
                mensajes.putIfAbsent(dato.grupo() + "-" + dato.producto(), mensaje(MensajeSalida.ADVERTENCIA,
                        String.format("No existe cuenta configurada para grupo %s, producto %s", dato.grupo(),
                                dato.producto())));
            }
            registros.add(InterfazBasileaMapper.crearRegistro(dato, fechaSistema, r.cuentaContable(),
                    propia.documentoIdentidadCtaPropia()));
        }

        if (errores == 0) repository.insertarInterfazBasilea(registros);
        return new Salida(errores, List.copyOf(mensajes.values()));
    }

    private static MensajeSalida mensaje(String tipo, String texto) {
        return new MensajeSalida(0L, tipo, tipo, texto);
    }
}
