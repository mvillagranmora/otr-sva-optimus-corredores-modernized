package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.GrabarRecaudacionTraspaso;
import cl.bcs.apv.api.casouso.IngresarRecaudacionTraspaso;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.grabarrecaudaciontraspaso.GrabarRecaudacionTraspasoEntrada;
import cl.bcs.apv.api.dto.grabarrecaudaciontraspaso.GrabarRecaudacionTraspasoResultado;
import cl.bcs.apv.api.dto.grabarrecaudaciontraspaso.RecaudacionConciliacion;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.EntradaRecaudacion;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.EntradaTraspaso;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.IngresarRecaudacionTraspasoEntrada;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.IngresarRecaudacionTraspasoResultado;
import cl.bcs.apv.api.dto.ingresarrecaudaciontraspaso.Resultado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.traspasos.IngresarRecaudacionTraspasoRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.IngresarRecaudacionTraspasoRepositorio.DetalleTraspaso;
import cl.bcs.apv.domain.port.out.traspasos.IngresarRecaudacionTraspasoRepositorio.Traspaso;

/**
 * Use case {@code IngresarRecaudacionTraspaso}: legacy {@code cl.bcs.APV.IngresoRecaudacionTraspaso}. For every traspaso of the input it builds the
 * reconciliation rows and hands them to {@code GrabarRecaudacionTraspaso}; when SEVERAL traspasos are given and one fails, the legacy rolls back the whole
 * connection (also what the ones that succeeded wrote) and answers only the error rows plus the recaudacion result of the last one.
 */
@Transactional(rollbackFor = Exception.class)
public class IngresarRecaudacionTraspasoServicio implements IngresarRecaudacionTraspaso {

    private final ConfiguracionSistema configuracion;
    private final GrabarRecaudacionTraspaso grabarRecaudacionTraspaso;
    private final IngresarRecaudacionTraspasoRepositorio repositorio;

    public IngresarRecaudacionTraspasoServicio(ConfiguracionSistema configuracion, GrabarRecaudacionTraspaso grabarRecaudacionTraspaso,
            IngresarRecaudacionTraspasoRepositorio repositorio) {
        this.configuracion = configuracion;
        this.grabarRecaudacionTraspaso = grabarRecaudacionTraspaso;
        this.repositorio = repositorio;
    }

    /** The legacy {@code SalidaIngresoRecaudacionTraspaso} while it is built (counter, messages, results). */
    private static final class Salida {
        long contadorErrores = 0L;
        List<Mensaje> mensajes = new ArrayList<>();
        List<Resultado> idResultado = new ArrayList<>();
    }

    @Override
    public IngresarRecaudacionTraspasoResultado ejecutar(IngresarRecaudacionTraspasoEntrada in) {
        try {
            Salida out = logica(in);
            // the legacy call() sets the counter AFTER logica, from the messages
            return new IngresarRecaudacionTraspasoResultado(Mensajes.numeroErrores(out.mensajes), out.mensajes, out.idResultado);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private Salida logica(IngresarRecaudacionTraspasoEntrada entrada) throws Exception {
        Salida result = new Salida();
        Salida out = new Salida();
        String fechaSistema = Parametros.fechaSistema(configuracion, entrada.idEmpresa());
        Long idResultado = 0L;

        for (EntradaTraspaso fila : entrada.traspasos()) {
            Map<Long, Traspaso> traspasos = obtenerTraspasos(fila, entrada.tipoTraspaso());

            if (traspasos != null) {
                EntradaRecaudacion recaudacion = entrada.recaudacion();
                // the legacy copies the recaudacion fields first (a null recaudacion is the NullPointerException of obtenerDatosRecaudacion), then computes the deposit amount
                recaudacion.folioCartola();
                BigDecimal montoDeposito = obtenerMontoDeposito(traspasos, entrada.tipoTraspaso(), entrada.traspasos());
                List<DetalleTraspaso> detalleTraspasos = obtenerDetalleTraspasos(fila, entrada.tipoTraspaso());
                GrabarRecaudacionTraspasoResultado resultadoGrabar = grabarRecaudacionTraspaso(entrada, recaudacion, montoDeposito, traspasos, detalleTraspasos);
                Salida resultIngreso = obtenerSalida(resultadoGrabar);

                Long cantOperaciones = repositorio.contarOperacionesSpotEnProceso();

                if (cantOperaciones != null && cantOperaciones > 0L) {
                    repositorio.eliminarSaldoFlujoCajaProceso();
                    repositorio.eliminarOperacionSpotProceso();
                }

                if (entrada.traspasos().size() > 1 && resultIngreso.contadorErrores > 0L) {
                    for (Mensaje mge : resultIngreso.mensajes) {
                        if (mge.tipo().compareToIgnoreCase("ERROR") == 0) {
                            Mensaje m = mge;
                            if (mge.codigo().compareToIgnoreCase("ERROR_RECAUDACION_CLIENTE") == 0) {
                                m = new Mensaje(mge.id(), mge.tipo(), mge.codigo(), mge.descripcion().concat(" Folio Traspaso: ".concat(fila.idTraspaso().toString())));
                            }
                            result.mensajes.add(m);
                        }
                    }
                    idResultado = resultIngreso.idResultado.get(0).idResultado();
                } else {
                    result.mensajes.addAll(resultIngreso.mensajes);
                }

                result.contadorErrores = result.contadorErrores + resultIngreso.contadorErrores;
                result.idResultado.addAll(resultIngreso.idResultado);

            } else {
                result.mensajes.add(new Mensaje(6L, "ERROR", "ERROR_TRASPASO_RECAUDACION", "Problemas al validar traspaso."));
                result.contadorErrores++;
            }
        }

        if (result.contadorErrores > 0L && entrada.traspasos().size() > 1L) {
            for (Mensaje mge : result.mensajes) {
                if (mge.tipo().compareToIgnoreCase("ERROR") == 0) {
                    out.mensajes.add(mge);
                    out.contadorErrores++;
                }
            }
            out.idResultado.add(new Resultado("REC", "Recaudacion", idResultado));
            // LEGACY DEFECT kept (see README APV-B-326): the rollback reverts the whole connection, also the traspasos that were recorded correctly
            repositorio.revertir();
        } else {
            out = result;
        }
        return out;
    }

    private Map<Long, Traspaso> obtenerTraspasos(EntradaTraspaso entradaTraspaso, String tipoTraspaso) throws Exception {
        List<EntradaTraspaso> lstEntradaTraspasos = new ArrayList<>();
        lstEntradaTraspasos.add(entradaTraspaso);
        List<Long> idsTraspasos = lstEntradaTraspasos.stream().map(traspaso -> traspaso.idTraspaso()).distinct().collect(Collectors.toList());
        List<Traspaso> traspasosSinAgrupar = repositorio.traspasos(idsTraspasos);
        // LEGACY DEFECT kept (see README APV-B-239): two rows of the view for the same traspaso make Collectors.toMap throw IllegalStateException
        Map<Long, Traspaso> traspasos = traspasosSinAgrupar.stream().collect(Collectors.toMap(traspaso -> traspaso.IdTraspaso, traspaso -> traspaso));
        if (validarTraspasosEntrada(traspasosSinAgrupar, tipoTraspaso) == false) {
            return null;
        }
        return traspasos;
    }

    @SuppressWarnings("StringEquality")
    private boolean validarTraspasosEntrada(List<Traspaso> entradaTraspasos, String tipoTraspaso) {
        // LEGACY DEFECT kept (see README APV-B-238): String compared by reference ("I", "COM"): for a value that came from JSON the validation never rejects anything
        if (tipoTraspaso == "I") {
            for (Traspaso traspaso : entradaTraspasos) {
                if (traspaso.IdEstado != "COM" || traspaso.NombreEntidad.compareTo(entradaTraspasos.get(0).NombreEntidad) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private BigDecimal obtenerMontoDeposito(Map<Long, Traspaso> traspasos, String tipoTraspaso, List<EntradaTraspaso> entradaTraspasos) {
        BigDecimal montoDeposito;
        // LEGACY DEFECT kept (see README APV-B-322): for type I the deposit is the SUM of MontoPesoDetalle of the view, not the MontoTraspaso of the input
        if (tipoTraspaso.compareTo("I") == 0) {
            montoDeposito = traspasos.entrySet().stream().map(traspaso -> traspaso.getValue().MontoPesoDetalle)
                    .reduce(BigDecimal.ZERO, (subtotal, montoDetalle) -> subtotal.add(montoDetalle, MathContext.DECIMAL128));
        } else {
            montoDeposito = entradaTraspasos.get(0).montoTraspaso();
        }
        return montoDeposito;
    }

    private List<DetalleTraspaso> obtenerDetalleTraspasos(EntradaTraspaso entradaTraspaso, String tipoTraspaso) throws Exception {
        List<EntradaTraspaso> lstEntradaTraspasos = new ArrayList<>();
        lstEntradaTraspasos.add(entradaTraspaso);
        List<Long> idsTraspasos = lstEntradaTraspasos.stream().map(traspaso -> traspaso.idTraspaso()).distinct().collect(Collectors.toList());
        List<DetalleTraspaso> detalleTraspasos = new ArrayList<>();

        if (tipoTraspaso.compareTo("I") == 0) {
            detalleTraspasos = repositorio.detalleTraspasos(idsTraspasos);
        } else {
            if (tipoTraspaso.compareTo("R") == 0) {
                for (Long fila : entradaTraspaso.idDetalleTraspaso()) {
                    List<DetalleTraspaso> detalleTraspasoReapertura = repositorio.detalleTraspasoReapertura(idsTraspasos.get(0), fila);
                    detalleTraspasos.add(detalleTraspasoReapertura.get(0));
                }
            }
        }
        return detalleTraspasos;
    }

    private GrabarRecaudacionTraspasoResultado grabarRecaudacionTraspaso(IngresarRecaudacionTraspasoEntrada entrada, EntradaRecaudacion recaudacion,
            BigDecimal montoDeposito, Map<Long, Traspaso> traspasos, List<DetalleTraspaso> detalleTraspasos) {
        // the details of a reapertura traspaso
        List<Long> lstIdDetalleTraspaso = new ArrayList<>();
        for (DetalleTraspaso fila : detalleTraspasos) {
            lstIdDetalleTraspaso.add(fila.IdDetalleTraspaso);
        }

        String fecha = Parametros.fechaSistema(configuracion, entrada.idEmpresa());
        List<RecaudacionConciliacion> conciliacion = detalleTraspasos.stream().filter(detalleTraspaso -> traspasos.get(detalleTraspaso.IdTraspaso) != null).map(detalleTraspaso -> {
            Traspaso traspaso = traspasos.get(detalleTraspaso.IdTraspaso);
            return new RecaudacionConciliacion(
                    detalleTraspaso.IdTraspaso,
                    detalleTraspaso.IdDetalleTraspaso,
                    detalleTraspaso.IdContratoSAAPV,
                    recaudacion.folioCartola(),
                    recaudacion.lineaCartola(),
                    recaudacion.idSucursal(),
                    fecha,
                    recaudacion.cuentaCorriente(),
                    recaudacion.idTipoDeposito(),
                    recaudacion.idBanco(),
                    detalleTraspaso.IdRegimenTributario,
                    detalleTraspaso.Regimen,
                    "1",
                    montoDeposito.longValue(),
                    obtenerMontoDetalle(detalleTraspaso),
                    detalleTraspaso.IdCliente,
                    "TRASPA",
                    "DEPENDIENTE".equals(traspaso.Trabajador) ? "DEP" : "IND",
                    traspaso.IdPersona,
                    traspaso.IdEmpleador,
                    traspaso.FolioContratoSAAPV);
        }).collect(Collectors.toList());

        GrabarRecaudacionTraspasoEntrada entradaGrabar = new GrabarRecaudacionTraspasoEntrada(entrada.idEmpresa(), entrada.idUsuario(), entrada.idModulo(),
                "TRASPA", fecha, conciliacion, new ArrayList<>(lstIdDetalleTraspaso));
        return grabarRecaudacionTraspaso.ejecutar(entradaGrabar);
    }

    private static Long obtenerMontoDetalle(DetalleTraspaso detalleTraspaso) {
        if (detalleTraspaso.MontoPeso.compareTo(BigDecimal.ZERO) != 0) {
            return detalleTraspaso.MontoPeso.longValue();
        }
        if (detalleTraspaso.MontoUF.compareTo(BigDecimal.ZERO) != 0) {
            return detalleTraspaso.MontoUF.longValue();
        }
        if (detalleTraspaso.Porcentaje.compareTo(BigDecimal.ZERO) != 0) {
            return detalleTraspaso.Porcentaje.longValue();
        }
        return 0L;
    }

    private static Salida obtenerSalida(GrabarRecaudacionTraspasoResultado resultadoGrabar) {
        Salida salida = new Salida();
        for (int i = 0; i < resultadoGrabar.mensajes().size(); i++) {
            if (resultadoGrabar.mensajes().get(i).tipo().equals("ERROR")) {
                salida.contadorErrores++;
            }
        }
        salida.mensajes = resultadoGrabar.mensajes();
        salida.idResultado = resultadoGrabar.idResultado().stream()
                .map(r -> new Resultado(r.codigoResultado(), r.nombreResultado(), r.idResultado())).collect(Collectors.toList());
        return salida;
    }
}
