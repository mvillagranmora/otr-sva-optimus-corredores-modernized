package cl.bcs.facturacion.service;

import cl.bcs.facturacion.domain.Comprobante;
import cl.bcs.facturacion.port.out.FacturacionServiciosDataPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Pure-logic service for BcsFacturacionServicios.
 *
 * Constants and classification methods are public static — derived directly from
 * ConstantesFacturacion.java in the legacy module.
 *
 * All god-object operations (ProcesarFacturacionCustodia, CrearComprobante,
 * ProcesarGananciaCapital) are 100% HANA-driven; they are delegated to the port
 * which throws UnsupportedOperationException until a HANA-connected adapter is
 * provided.
 */
@Service
public class FacturacionServiciosService {

    // ---------------------------------------------------------
    // Constants (from ConstantesFacturacion.java)
    // ---------------------------------------------------------

    public static final List<String> TIPOS_FACTURA =
            List.of("FAC", "FAE", "BOE", "FOE", "FOC", "FEE", "FOX", "POL", "BEE");

    public static final List<String> TIPOS_NOTA_CREDITO =
            List.of("NCE", "NCR", "NCO");

    public static final List<String> TIPOS_ANTICIPO_SIMULTANEA =
            List.of("ANSIMV", "ANSIMC");

    // ---------------------------------------------------------
    // Classification helpers — pure static
    // ---------------------------------------------------------

    public static boolean esTipoFactura(String tipo) {
        return TIPOS_FACTURA.contains(tipo);
    }

    public static boolean esTipoNotaCredito(String tipo) {
        return TIPOS_NOTA_CREDITO.contains(tipo);
    }

    public static boolean esTipoAnticipoSimultanea(String tipo) {
        return TIPOS_ANTICIPO_SIMULTANEA.contains(tipo);
    }

    // ---------------------------------------------------------
    // Instance methods — delegate to port
    // ---------------------------------------------------------

    private final FacturacionServiciosDataPort dataPort;

    public FacturacionServiciosService(FacturacionServiciosDataPort dataPort) {
        this.dataPort = dataPort;
    }

    public String obtenerFechaSistema(Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        return dataPort.obtenerFechaSistema(idEmpresa);
    }

    public List<Comprobante> consultarComprobantes(Long idEmpresa, String estado) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(estado, "estado must not be null");
        return dataPort.consultarComprobantes(idEmpresa, estado);
    }

    public void procesarFacturacionCustodia(Long idEmpresa, Long idPortafolio) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(idPortafolio, "idPortafolio must not be null");
        dataPort.procesarFacturacionCustodia(idEmpresa, idPortafolio);
    }

    public void crearComprobante(Long idEmpresa, String tipoComprobante) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(tipoComprobante, "tipoComprobante must not be null");
        dataPort.crearComprobante(idEmpresa, tipoComprobante);
    }

    public void procesarGananciaCapital(Long idEmpresa, Long idComprobante) {
        Objects.requireNonNull(idEmpresa, "idEmpresa must not be null");
        Objects.requireNonNull(idComprobante, "idComprobante must not be null");
        dataPort.procesarGananciaCapital(idEmpresa, idComprobante);
    }
}
