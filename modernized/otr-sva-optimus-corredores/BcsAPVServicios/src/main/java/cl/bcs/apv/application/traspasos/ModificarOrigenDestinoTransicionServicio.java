package cl.bcs.apv.application.traspasos;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ModificarOrigenDestinoTransicion;
import cl.bcs.apv.api.dto.modificarorigendestinotransicion.ModificarOrigenDestinoTransicionEntrada;
import cl.bcs.apv.api.dto.modificarorigendestinotransicion.ModificarOrigenDestinoTransicionResultado;
import cl.bcs.apv.api.dto.modificarorigendestinotransicion.OrigenDestinoEntrada;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.traspasos.ModificarOrigenDestinoRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.ModificarOrigenDestinoRepositorio.OrigenDestino;

/** Use case {@code ModificarOrigenDestinoTransicion}: legacy {@code ModificarOrigenDestino} (updates the origin/destination account transitions). */
@Transactional(rollbackFor = Exception.class)
public class ModificarOrigenDestinoTransicionServicio implements ModificarOrigenDestinoTransicion {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ConfiguracionSistema configuracion;
    private final Reloj reloj;
    private final ModificarOrigenDestinoRepositorio repositorio;

    public ModificarOrigenDestinoTransicionServicio(ConfiguracionSistema configuracion, Reloj reloj, ModificarOrigenDestinoRepositorio repositorio) {
        this.configuracion = configuracion;
        this.reloj = reloj;
        this.repositorio = repositorio;
    }

    @Override
    public ModificarOrigenDestinoTransicionResultado ejecutar(ModificarOrigenDestinoTransicionEntrada in) {
        List<MensajeLegacy> mensajes = new ArrayList<>();
        Long contadorErrores = null;
        // LEGACY DEFECT kept (see README APV-B-03): ONE MensajeSalida instance is reused for every row, so all the messages show the LAST row's result
        MensajeLegacy msg = new MensajeLegacy();
        // the legacy reads the system date (and fails when it is not configured) although it does not use it
        Parametros.fechaSistema(configuracion, in.idEmpresa());

        for (OrigenDestinoEntrada fila : in.parametros()) {
            OrigenDestino od = new OrigenDestino();
            od.IdOrigenDestinoTransicion = fila.idOrigenDestinoTransicion();
            od.Operacion = fila.operacion();
            od.OrigenCtaBco = fila.origenCtaBco();
            od.SocioNegocioCtaBco = fila.socioNegocioCtaBco();
            od.DestinoCtaBco = fila.destinoCtaBco();
            od.FechaModificacion = FORMATO.format(reloj.ahora().atZone(ZoneId.systemDefault()));
            od.IdUsuarioModificador = in.idUsuario();
            od.IdEstado = fila.idEstado();
            int[] res = repositorio.modificar(od);
            if (res != null && res.length > 0 && res[0] == 1) {
                msg.id = 1L;
                msg.tipo = "INFORMACION";
                msg.codigo = "UI_DATOS_MODIFICADOS";
                msg.descripcion = "Datos Modificados";
                mensajes.add(msg);
                contadorErrores = 0L;
            } else {
                msg.id = 1L;
                msg.tipo = "ERROR";
                msg.codigo = "UI_ERROR_MODIFICACION";
                msg.descripcion = "Error al Modificar los Datos.";
                mensajes.add(msg);
                // LEGACY DEFECT kept (see README APV-B-05): the counter is only set per row (never when there are no rows) and holds the LAST row's value
                contadorErrores = 1L;
            }
        }
        return new ModificarOrigenDestinoTransicionResultado(MensajeLegacy.aMensajes(mensajes), contadorErrores);
    }
}
