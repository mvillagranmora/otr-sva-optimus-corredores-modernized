package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.devengo.DevengoCalculator;
import cl.bcs.contabilidad.domain.service.devengo.TipoDevengo;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Accrual of repo portfolios at purchase rate, IdTipoCentralizacion 440 (CONT-C-20..29): legacy
 * DevengoATasaCompraCarteraPactada.java. 30 data fields per row (:94-123); Relacion and FolioOperacion go through
 * String.valueOf, so a null becomes "null".
 */
@Service("devengoATasaCompraCarteraPactada")
public class DevengoATasaCompraCarteraPactada extends CentralizaDevengoCartera {

    /** Legacy :94-123, in emission order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"), texto("NombreCliente"), texto("Moneda"), texto("Producto"),
            texto("ClaseInstrumento"), texto("TipoInstrumento"), texto("Nemo"), texto("Boveda"), texto("Emisor"),
            texto("TipoReajuste"), texto("CodigoIFRS"), texto("TipoCartera"), texto("ClaseEmisor"), texto("Agente"),
            texto("NombreAgente"), texto("SucursalAgente"), texto("NombreSucursalAgente"), texto("CentroCostoAgente"),
            valorDe("Relacion"), texto("Bolsa"), texto("TipoPersona"), texto("Concepto"), fecha("FechaLiquidacion"),
            fecha("FechaOperacion"), texto("TipoEmisor"), texto("CompraVenta"), texto("Mesa"), valorDe("FolioOperacion"),
            texto("EsVencimiento"), texto("TipoReajusteCabecera"));

    public DevengoATasaCompraCarteraPactada(ContabilidadRepository repository, DevengoCalculator calculator) {
        super(repository, calculator, TipoDevengo.TASA_COMPRA_CARTERA_PACTADA, CAMPOS);
    }
}
