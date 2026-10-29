package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import org.springframework.stereotype.Service;

import java.util.List;

import static cl.bcs.contabilidad.domain.service.soporte.Campo.*;

/**
 * Treasury centralization CentralizaTesoreria (legacy CentralizaTesoreria.java). Characterization: CONT-F-177..CONT-F-193.
 * Flow and CLP conversion: {@link CentralizacionTesoreria}.
 */
@Service("centralizaTesoreria")
public class CentralizaTesoreria extends CentralizacionTesoreria {

    /** Legacy CentralizaTesoreria.java:306-361, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("NombreCuentaCorriente"),
            texto("TipoDocumento"),
            valorDe("Folio"),
            texto("FechaDocumento"),
            texto("NumeroCuentaCorriente"),
            texto("MedioDePago"),
            texto("NumeroDocumentoPago"),
            fecha("FechaLiquidacion"),
            timestampOPorDefecto("FechaDocumentoPago"),
            texto("DocumentoIdentidadCAVALI"),
            texto("SerieDocumento"),
            texto("Concepto"),
            texto("NombreConcepto"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Moneda"),
            texto("CodigoCaja"),
            texto("EmitidoA"),
            texto("EsBilateral"),
            texto("TipoPersona"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("FechaProceso"),
            texto("Proceso"),
            texto("TipoComprobante"),
            texto("Valuta"),
            texto("PartidaLiquidacion"),
            valorDe("NumeroComprobante"),
            texto("AliasBancoCaja"),
            decimal("Monto"),
            texto("TipoComprobanteAdj"),
            valorDe("FolioComprobanteAdj"),
            valorDe("FolioOperacion"),
            texto("CodigoProducto"),
            texto("Nemotecnico"),
            texto("Emisor"),
            texto("Relacion"),
            decimal("Valor"),
            decimal("TipoCambio"),
            texto("EsContraparte"),
            texto("Bolsa"),
            texto("ComprobanteTipo"),
            fecha("FechaMovimiento"),
            decimal("MontoMonedaSistema"),
            valorDe("Referencia3"),
            valorDe("EsProveedor"),
            texto("ClienteBancario"),
            texto("CondicionLiquidacion"),
            valorDe("FolioDTE"),
            valorDe("OperacionNoFacturable"));

    public CentralizaTesoreria(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository, "cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectMovimientos.sql", "cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectEventos.sql",
                "cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectDatosMovimiento.sql", CAMPOS);
    }
}
