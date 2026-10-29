package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.devengo.DevengoCalculator;
import cl.bcs.contabilidad.domain.service.devengo.TipoDevengo;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Accrual of repos at the agreed rate, IdTipoCentralizacion 430 (CONT-C-30..37): legacy DevengoATasaPacto.java.
 * 29 data fields per row (:96-124): Portafolio, Boveda, Bolsa and Mesa are read by the SQL but not emitted;
 * IdMovimiento, IdCartera, Relacion and FolioOperacion go through String.valueOf.
 */
@Service("devengoATasaPacto")
public class DevengoATasaPacto extends CentralizaDevengoCartera {

    /** Legacy :96-124, in emission order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"), texto("DocumentoIdentidad"), texto("NombreCliente"), texto("Moneda"),
            texto("Producto"), texto("ClaseInstrumento"), texto("TipoInstrumento"), texto("Nemo"), valorDe("IdCartera"),
            texto("Emisor"), texto("TipoReajuste"), texto("CodigoIFRS"), texto("TipoCartera"), texto("ClaseEmisor"),
            texto("Agente"), texto("NombreAgente"), texto("SucursalAgente"), texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"), valorDe("Relacion"), texto("TipoPersona"), texto("Concepto"),
            fecha("FechaLiquidacion"), fecha("FechaOperacion"), texto("CompraVenta"), valorDe("FolioOperacion"),
            texto("ClienteBancario"), texto("EsVencimiento"), texto("TipoReajusteCabecera"));

    public DevengoATasaPacto(ContabilidadRepository repository, DevengoCalculator calculator) {
        super(repository, calculator, TipoDevengo.TASA_PACTO, CAMPOS);
    }
}
