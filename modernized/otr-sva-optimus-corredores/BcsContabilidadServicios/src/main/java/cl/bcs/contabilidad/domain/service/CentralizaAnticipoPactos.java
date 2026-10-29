package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository.AnticipoPactosDatoMovimiento;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot equivalent of legacy CentralizaAnticipoPactos.
 *
 * Ported behaviours (see characterization tests):
 *   B-04: calls consultarEmpresa -> obtenerDiaAnterior -> queryMovimientos ->
 *         queryEventos -> queryDatosMovimiento -> maps 26 dato-fields per row.
 *   B-02: when simulaOperacion="S" the persistirCentralizacion port is NOT called.
 *
 * STUB: throw UnsupportedOperationException until implementation is complete.
 */
@Service("centralizaAnticipoPactos")
public class CentralizaAnticipoPactos implements BcsCentralizacionService {

    static final String BEAN_NAME = "centralizaAnticipoPactos";

    private final PlataformaPort     plataforma;
    private final ContabilidadRepository repository;

    public CentralizaAnticipoPactos(PlataformaPort plataforma,
                                    ContabilidadRepository repository) {
        this.plataforma  = plataforma;
        this.repository  = repository;
    }

    /**
     * B-04: consultarEmpresa → obtenerDiaAnterior → query movements/events/datos →
     *       map to output.  B-02: when simulaOperacion="S" the persist call is skipped.
     */
    @Override
    public SalidaCentralizacion centralizar(EntradaCentralizacion entrada) {
        // B-04: call order is consultarEmpresa THEN obtenerDiaAnterior
        EmpresaInfo empresa = plataforma.consultarEmpresa(entrada.idEmpresa());
        String fechaAnterior = plataforma.obtenerDiaAnterior(
                empresa.idPais(), entrada.fechaContable());

        Long idEmpresa = entrada.idEmpresa();
        String fechaContable = entrada.fechaContable();
        Long idTipo = entrada.idTipoCentralizacion();

        List<TabMovimientoRef> movimientos =
                repository.queryMovimientos(idEmpresa, fechaContable, idTipo);

        List<TabEventoRef> eventos = new ArrayList<>();
        eventos.addAll(repository.queryEventosOperacion(idEmpresa, fechaContable, idTipo));
        eventos.addAll(repository.queryEventosSubyacente(
                idEmpresa, fechaContable, idTipo, fechaAnterior));

        List<AnticipoPactosDatoMovimiento> rawDatos =
                repository.queryDatosMovimientoAnticipo(idEmpresa, fechaContable);

        List<TabDatoMovimientoRef> datosMovimiento = rawDatos.stream()
                .flatMap(d -> mapDatosMovimiento(idTipo, d).stream())
                .toList();

        List<ResultadoCentralizacion> resultados = movimientos.stream()
                .map(CentralizaAnticipoPactos::toResultado)
                .toList();

        // B-02: only persist when this is a real run (not simulation)
        if ("N".equals(entrada.simulaOperacion())) {
            repository.persistirCentralizacion(
                    idTipo, fechaContable, idEmpresa, entrada.ejercicio(),
                    String.valueOf(entrada.idUsuario()),
                    movimientos, eventos, datosMovimiento, List.of());
        }

        return new SalidaCentralizacion(
                0L, List.of(), resultados, movimientos,
                eventos, datosMovimiento, List.of());
    }

    // -----------------------------------------------------------------------
    // Public static helpers — the exact field names and type conversions
    // are pinned by characterization test B-04.
    // -----------------------------------------------------------------------
    public static List<TabDatoMovimientoRef> mapDatosMovimiento(
            Long idTipoCentralizacion,
            AnticipoPactosDatoMovimiento mov) {

        List<TabDatoMovimientoRef> datos = new ArrayList<>(26);
        // Legacy CentralizaAnticipoPactos lines 111-138; field names are the
        // oracle — do not change them without a corresponding test update.
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "IdMovimiento",
                String.valueOf(mov.idMovimiento())));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "DocumentoIdentidad",
                mov.documentoIdentidad()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "NombreCliente",
                mov.nombreCliente()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "PortafolioCliente",
                mov.portafolio()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Moneda",
                mov.moneda()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Producto",
                mov.producto()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "ClaseInstrumento",
                mov.claseInstrumento()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "TipoInstrumento",
                mov.tipoInstrumento()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Nemo",
                mov.nemo()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Boveda",
                mov.boveda()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "IdCartera",
                mov.idCartera()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Emisor",
                mov.emisor()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "IdTipoReajuste",
                mov.tipoReajuste()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "TipoEmisor",
                mov.tipoEmisor()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "CodigoIFRS",
                mov.codigoIFRS()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "TipoCartera",
                mov.tipoCartera()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "ClaseEmisor",
                mov.claseEmisor()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Agente",
                mov.agente()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "NombreAgente",
                mov.nombreAgente()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "SucursalAgente",
                mov.sucursalAgente()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "NombreSucursalAgente",
                mov.nombreSucursalAgente()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "CentroCostoAgente",
                mov.centroCostoAgente()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Relacion",
                mov.relacion()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "CompraVenta",
                mov.compraVenta()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "Mesa",
                mov.mesa()));
        datos.add(dat(idTipoCentralizacion, mov.idMovimiento(), "FolioOperacion",
                String.valueOf(mov.folioOperacion())));
        return datos;
    }

    private static TabDatoMovimientoRef dat(Long tipo, Long idMov, String tipoDato,
                                             String valor) {
        return new TabDatoMovimientoRef(tipo, idMov, tipoDato,
                valor == null ? "" : valor, BigDecimal.ZERO);
    }

    /** Maps TabMovimientoRef -> ResultadoCentralizacion.
     *  Legacy: BcsCentralizacion.llenarResultado() - Ejercicio Long->String. */
    public static ResultadoCentralizacion toResultado(TabMovimientoRef m) {
        return new ResultadoCentralizacion(
                m.idTipoCentralizacion(),
                m.idMovimiento(),
                m.idTipoMovimiento(),
                m.idEmpresa(),
                m.numero(),
                m.linea(),
                m.fecha(),
                m.documentoIdentidad(),
                m.idPortafolio(),
                String.valueOf(m.ejercicio()),   // Long -> String (legacy oracle)
                m.periodo(),
                m.monto(),
                m.idMoneda(),
                m.estado()
        );
    }
}
