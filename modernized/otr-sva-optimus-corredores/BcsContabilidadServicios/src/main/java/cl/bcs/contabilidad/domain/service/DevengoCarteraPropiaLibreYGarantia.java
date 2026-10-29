package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.devengo.DevengoCalculator;
import cl.bcs.contabilidad.domain.service.devengo.TipoDevengo;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Accrual of the own free and pledged fixed-income portfolio at purchase rate, IdTipoCentralizacion 450
 * (CONT-C-40..47): legacy DevengoCarteraPropiaLibreYGarantia.java. Movements and events are keyed by IdCartera;
 * 30 data fields per row (:96-125), only IdMovimiento through String.valueOf (IdCartera and Relacion are plain Strings).
 */
@Service("devengoCarteraPropiaLibreYGarantia")
public class DevengoCarteraPropiaLibreYGarantia extends CentralizaDevengoCartera {

    /** Legacy :96-125, in emission order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"), texto("DocumentoIdentidad"), texto("NombreCliente"), texto("Portafolio"),
            texto("Moneda"), texto("Producto"), texto("ClaseInstrumento"), texto("TipoInstrumento"), texto("Nemo"),
            texto("Boveda"), texto("IdCartera"), texto("Emisor"), texto("TipoReajuste"), texto("CodigoIFRS"),
            texto("TipoCartera"), texto("ClaseEmisor"), texto("Agente"), texto("NombreAgente"), texto("SucursalAgente"),
            texto("NombreSucursalAgente"), texto("CentroCostoAgente"), texto("Relacion"), texto("Bolsa"),
            fecha("FechaLiquidacion"), texto("TipoPersona"), fecha("FechaOperacion"), texto("IdConcepto"),
            texto("DocumentoIdentidadCAVALI"), texto("TipoEmisor"), texto("Mesa"));

    public DevengoCarteraPropiaLibreYGarantia(ContabilidadRepository repository, DevengoCalculator calculator) {
        super(repository, calculator, TipoDevengo.CARTERA_PROPIA_LIBRE_Y_GARANTIA, CAMPOS);
    }
}
