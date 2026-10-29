package cl.bcs.apv.application.contratos;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.AsignarContratoARecaudacion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.AsignarContratoARecaudacionEntrada;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.AsignarContratoARecaudacionResultado;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.ParametrosAsignacionContratoRecaudacion;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio.AsignacionActualizar;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio.AsignacionFila;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio.AsignacionInsertar;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio.ContratoRatificar;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio.DetalleTraspasoFila;

/** Use case {@code AsignarContratoARecaudacion} (legacy {@code cl.bcs.APV.AsignarContratoARecaudacion}). */
@Transactional(rollbackFor = Exception.class)
public class AsignarContratoARecaudacionServicio implements AsignarContratoARecaudacion {

    private final AsignacionContratoRepositorio repositorio;
    private final DiccionarioMensajes diccionario;
    private final Reloj reloj;

    public AsignarContratoARecaudacionServicio(AsignacionContratoRepositorio repositorio, DiccionarioMensajes diccionario, Reloj reloj) {
        this.repositorio = repositorio;
        this.diccionario = diccionario;
        this.reloj = reloj;
    }

    @Override
    public AsignarContratoARecaudacionResultado ejecutar(AsignarContratoARecaudacionEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        logica(in, mensajes);
        // legacy call(): the counter is set after logica
        return new AsignarContratoARecaudacionResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }

    private void logica(AsignarContratoARecaudacionEntrada in, List<Mensaje> out) {
        // the legacy read the REAL wall clock (user decision 4: the Reloj port)
        Timestamp fechaHora = Timestamp.from(reloj.ahora());
        LocalDate fechaLocal = fechaHora.toLocalDateTime().toLocalDate();
        int mesSistema = fechaLocal.getMonthValue();
        int anoSistema = fechaLocal.getYear();
        String tipoMensajesSalida = "";
        boolean tieneDetalleTraspaso = false;

        List<AsignacionInsertar> insertar = new ArrayList<>();
        List<AsignacionActualizar> actualizar = new ArrayList<>();
        List<Long> idsContrato = new ArrayList<>();
        // the legacy wrote 0 into the (mutable) input row when its contract was null: the records are immutable, the normalised rows are kept here
        List<ParametrosAsignacionContratoRecaudacion> parametros = new ArrayList<>();

        for (ParametrosAsignacionContratoRecaudacion original : in.parametros()) {
            ParametrosAsignacionContratoRecaudacion fila = original.idContratoSAAPV() == null
                    ? new ParametrosAsignacionContratoRecaudacion(original.idRecaudacionCliente(), 0L, original.idRecaudacion()) : original;
            parametros.add(fila);

            List<AsignacionFila> asignaciones = repositorio.asignacionesDe(fila.idRecaudacionCliente());
            if ("S".equals(in.simulacion()) && asignaciones.size() > 0) {
                for (AsignacionFila actual : asignaciones) {
                    LocalDate actualFechaModificacion = actual.FechaModificacion().toLocalDateTime().toLocalDate();
                    int mesActual = actualFechaModificacion.getMonthValue();
                    int anoActual = actualFechaModificacion.getYear();
                    if (actual.IdContratoSAAPV().equals(fila.idContratoSAAPV()) && mesActual == mesSistema && anoActual == anoSistema) {
                        tipoMensajesSalida = "ContratoAsignado";
                    }
                }
            }
            if (asignaciones.size() > 0) {
                actualizar.add(new AsignacionActualizar(fila.idRecaudacionCliente(), fila.idContratoSAAPV(), fechaHora));
            } else {
                insertar.add(new AsignacionInsertar(fila.idRecaudacionCliente(), fila.idContratoSAAPV(), fechaHora, fechaHora));
            }
            idsContrato.add(fila.idContratoSAAPV());
        }

        List<Long> idsSinRepeticiones = idsContrato.stream().distinct().collect(Collectors.toList());

        List<DetalleTraspasoFila> detalleTraspaso = new ArrayList<>();
        if ("TRASPA".equalsIgnoreCase(in.origen())) {
            detalleTraspaso = repositorio.detalleTraspaso(idsSinRepeticiones);
        }

        List<ContratoRatificar> contratos = repositorio.contratosARatificar(idsContrato);

        if (!"S".equals(in.simulacion()) || "".equals(tipoMensajesSalida)) {
            // LEGACY DEFECT kept (see README APV-A, AsignarContratoARecaudacion): when there are updates the inserts are dropped (else if)
            if (actualizar.size() > 0) {
                repositorio.actualizarAsignaciones(actualizar);
            } else if (insertar.size() > 0) {
                repositorio.insertarAsignaciones(insertar);
            }

            for (ContratoRatificar contrato : contratos) {
                if (detalleTraspaso.size() > 0) {
                    List<DetalleTraspasoFila> detalleContrato = detalleTraspaso.stream()
                            .filter(d -> d.IdContratoSAAPV().compareTo(contrato.IdContratoSAAPV) == 0).collect(Collectors.toList());
                    if (detalleContrato.size() > 0) {
                        tieneDetalleTraspaso = "S".equalsIgnoreCase(detalleContrato.get(0).TieneDetalle()) ? true : false;
                    }
                }
                if (!"TRASPA".equalsIgnoreCase(in.origen()) && !tieneDetalleTraspaso) {
                    List<ParametrosAsignacionContratoRecaudacion> filtro = parametros.stream()
                            .filter(p -> p.idContratoSAAPV().equals(contrato.IdContratoSAAPV)).collect(Collectors.toList());
                    contrato.IdRecaudacionCliente = filtro.get(0).idRecaudacionCliente();
                    contrato.IdRecaudacion = filtro.get(0).idRecaudacion();
                }
            }

            if (contratos.size() > 0) {
                Long datosPorActualizar = contratos.stream().filter(c -> c.IdRecaudacionCliente.compareTo(0L) != 0).count();
                if (datosPorActualizar > 0) {
                    contratos = contratos.stream().filter(c -> c.IdRecaudacionCliente.compareTo(0L) != 0).collect(Collectors.toList());
                    repositorio.actualizarRegimen(contratos);
                }
            }
        }

        for (ContratoRatificar contrato : contratos) {
            String codigo = "ContratoAsignado".equals(tipoMensajesSalida) ? "UI_CONTRATO_YA_ASIGNADO" : "UI_ASIGNACION_EXITOSA";
            out.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), contrato.IdRecaudacionCliente, Mensajes.MENSAJE_INFORMACION, codigo, new HashMap<String, String>(0)));
        }
    }
}
