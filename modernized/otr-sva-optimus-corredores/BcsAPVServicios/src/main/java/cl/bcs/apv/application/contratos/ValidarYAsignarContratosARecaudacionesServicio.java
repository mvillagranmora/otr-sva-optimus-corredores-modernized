package cl.bcs.apv.application.contratos;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.BuscarContratosParaRecaudacion;
import cl.bcs.apv.api.casouso.ValidarYAsignarContratosARecaudaciones;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.AsignarContratoRecaudacion;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.BuscarContratosParaRecaudacionEntrada;
import cl.bcs.apv.api.dto.validaryasignarcontratosarecaudaciones.Filtros;
import cl.bcs.apv.api.dto.validaryasignarcontratosarecaudaciones.ValidarYAsignarContratosARecaudacionesEntrada;
import cl.bcs.apv.api.dto.validaryasignarcontratosarecaudaciones.ValidarYAsignarContratosARecaudacionesResultado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio.AsignacionActualizar;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio.AsignacionInsertar;
import cl.bcs.apv.domain.port.out.contratos.RecaudacionRezagoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.RecaudacionRezagoRepositorio.DatosRecaudaciones;

/** Use case {@code ValidarYAsignarContratosARecaudaciones} (legacy {@code cl.bcs.APV.ValidarYAsignarContratosARecaudaciones}). */
@Transactional(rollbackFor = Exception.class)
public class ValidarYAsignarContratosARecaudacionesServicio implements ValidarYAsignarContratosARecaudaciones {

    /** Legacy {@code ParametrosAsignacionContratoRecaudacion} of this class. */
    private record ParametroAsignacion(Long idRecaudacionCliente, Long idContratoSAAPV, Long folioRecaudacion, String folioContratoSAAPV) {
    }

    private final RecaudacionRezagoRepositorio rezagos;
    private final AsignacionContratoRepositorio asignaciones;
    private final BuscarContratosParaRecaudacion busqueda;
    private final DiccionarioMensajes diccionario;
    private final Reloj reloj;

    public ValidarYAsignarContratosARecaudacionesServicio(RecaudacionRezagoRepositorio rezagos, AsignacionContratoRepositorio asignaciones,
            BuscarContratosParaRecaudacion busqueda, DiccionarioMensajes diccionario, Reloj reloj) {
        this.rezagos = rezagos;
        this.asignaciones = asignaciones;
        this.busqueda = busqueda;
        this.diccionario = diccionario;
        this.reloj = reloj;
    }

    @Override
    public ValidarYAsignarContratosARecaudacionesResultado ejecutar(ValidarYAsignarContratosARecaudacionesEntrada entrada) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<Mensaje> advertencias = new ArrayList<>();

        List<DatosRecaudaciones> recaudaciones = obtenerEntidadesRecaudacionesYContratos(entrada);
        List<ParametroAsignacion> parametrosAsignacion = new ArrayList<>();

        List<DatosRecaudaciones> conContratoAAsignar = recaudaciones.parallelStream().filter(r -> r.IdContratoSAAPV > 0).collect(Collectors.toList());
        if (!conContratoAAsignar.isEmpty()) {
            for (DatosRecaudaciones c : conContratoAAsignar) {
                parametrosAsignacion.add(new ParametroAsignacion(c.IdRecaudacionCliente, c.IdContratoSAAPV, c.FolioRecaudacion, c.FolioContratoSAAPV));
            }
        }
        if (!parametrosAsignacion.isEmpty()) {
            asignarContratosARecaudacion(parametrosAsignacion);
            for (ParametroAsignacion a : parametrosAsignacion) {
                HashMap<String, String> parametros = new HashMap<>();
                parametros.put("IdRecaudacionCliente", String.valueOf(a.idRecaudacionCliente()));
                parametros.put("IdContratoSAAPV", String.valueOf(a.idContratoSAAPV()));
                parametros.put("FolioRecaudacion", String.valueOf(a.folioRecaudacion()));
                // LEGACY DEFECT kept (see README APV-A, ValidarYAsignarContratosARecaudaciones): the contract folio is never filled, the message says "null"
                parametros.put("FolioContratoSAAPV", String.valueOf(a.folioContratoSAAPV()));
                mensajes.add(Mensajes.conDiccionario(diccionario, entrada.idEmpresa(), a.idRecaudacionCliente(), Mensajes.MENSAJE_OK,
                        "UI_ASIGNAR_CONTRATO_A_RECAUDACION", parametros));
            }
        }

        List<DatosRecaudaciones> sinContratoAAsignar = recaudaciones.stream().filter(r -> r.IdContratoSAAPV == 0).collect(Collectors.toList());
        if (!sinContratoAAsignar.isEmpty()) {
            for (DatosRecaudaciones s : sinContratoAAsignar) {
                HashMap<String, String> parametros = new HashMap<>();
                parametros.put("IdRecaudacionCliente", String.valueOf(s.IdRecaudacionCliente));
                parametros.put("FolioRecaudacion", String.valueOf(s.FolioRecaudacion));
                advertencias.add(Mensajes.conDiccionario(diccionario, entrada.idEmpresa(), s.IdRecaudacionCliente, Mensajes.MENSAJE_ADVERTENCIA,
                        "UI_ERROR_ASIGNAR_CONTRATO_RECAUDACION", parametros));
            }
        }
        long contadorAdvertencias = advertencias.stream().count();
        // the legacy call() does not recount the errors: the counter keeps its constructor value, 0
        return new ValidarYAsignarContratosARecaudacionesResultado(mensajes, 0L, advertencias, contadorAdvertencias);
    }

    private void asignarContratosARecaudacion(List<ParametroAsignacion> parametros) {
        // the legacy read the real wall clock (user decision 4: the Reloj port)
        Timestamp fechaHora = Timestamp.from(reloj.ahora());
        List<AsignacionInsertar> inserts = new ArrayList<>();
        List<AsignacionActualizar> updates = new ArrayList<>();
        for (ParametroAsignacion fila : parametros) {
            if (asignaciones.asignacionesDe(fila.idRecaudacionCliente()).size() > 0) {
                updates.add(new AsignacionActualizar(fila.idRecaudacionCliente(), fila.idContratoSAAPV(), fechaHora));
            } else {
                inserts.add(new AsignacionInsertar(fila.idRecaudacionCliente(), fila.idContratoSAAPV(), fechaHora, fechaHora));
            }
        }
        if (updates.size() > 0) {
            asignaciones.actualizarAsignaciones(updates);
        }
        if (inserts.size() > 0) {
            asignaciones.insertarAsignaciones(inserts);
        }
    }

    private List<DatosRecaudaciones> obtenerEntidadesRecaudacionesYContratos(ValidarYAsignarContratosARecaudacionesEntrada entrada) {
        List<DatosRecaudaciones> todas = obtenerRecaudacionesRezago(entrada.filtrosRecaudacion());
        rezagos.crearTablaTemporal();
        List<AsignarContratoRecaudacion> contratos;
        try {
            rezagos.borrarTablaTemporal(todas);
            rezagos.insertarATablaTemporal(todas);
            contratos = obtenerIdsContratos(todas, entrada);
        } catch (RuntimeException e) {
            // the session-scoped temporary table must not outlive a failed call (the pooled connection is reused): one best-effort DROP
            if (!todas.isEmpty()) {
                try {
                    rezagos.dropTablaTemporal(todas.subList(0, 1));
                } catch (RuntimeException ignorada) {
                    // the table may already be gone (the legacy drops once per row); the original failure is the one that matters
                }
            }
            throw e;
        }
        for (DatosRecaudaciones recaudacion : todas) {
            AsignarContratoRecaudacion encontrado = contratos.parallelStream().filter(con -> recaudacion.IdRecaudacionCliente.equals(con.idRecaudacionCliente()))
                    .findFirst().orElse(new AsignarContratoRecaudacion(null, null, null, null));
            recaudacion.IdContratoSAAPV = encontrado.idContratoSAAPV();
            recaudacion.FolioContratoSAAPV = encontrado.folioContratoSAAPV();
        }
        return todas;
    }

    private List<DatosRecaudaciones> obtenerRecaudacionesRezago(Filtros filtros) {
        List<DatosRecaudaciones> lista = rezagos.recaudacionesEnRezago(filtros.fechaDesde(), filtros.fechaHasta());
        if (filtros.folioRecaudacion() > 0) {
            lista = lista.parallelStream().filter(rec -> rec.FolioRecaudacion.equals(filtros.folioRecaudacion())).collect(Collectors.toList());
        }
        if (!filtros.rutCliente().equals("")) {
            lista = lista.parallelStream().filter(rec -> rec.RutCliente.equals(filtros.rutCliente())).collect(Collectors.toList());
        }
        if (!filtros.rutEmpleador().equals("")) {
            lista = lista.parallelStream().filter(rec -> rec.RutEmpleador.equals(filtros.rutEmpleador())).collect(Collectors.toList());
        }
        return lista;
    }

    private List<AsignarContratoRecaudacion> obtenerIdsContratos(List<DatosRecaudaciones> recaudaciones, ValidarYAsignarContratosARecaudacionesEntrada entrada) {
        List<Long> ids = recaudaciones.stream().map(rec -> rec.FolioRecaudacion).collect(Collectors.toList());
        List<AsignarContratoRecaudacion> resultado = busqueda
                .ejecutar(new BuscarContratosParaRecaudacionEntrada(entrada.idEmpresa(), entrada.idUsuario(), entrada.idModulo(), ids)).resultado();
        // LEGACY DEFECT kept (see README APV-A, ValidarYAsignarContratosARecaudaciones): one DROP of the temporary table per row, two or more rows give SQLException
        rezagos.dropTablaTemporal(recaudaciones);
        return resultado;
    }
}
