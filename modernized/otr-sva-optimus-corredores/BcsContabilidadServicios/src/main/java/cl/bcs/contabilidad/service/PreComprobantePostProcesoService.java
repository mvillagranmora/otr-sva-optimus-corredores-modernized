package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.port.out.PreComprobantePostProcesoPort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Steps GeneraPreComprobanteMultiSeleccion runs after generating (legacy GeneraPreComprobanteMultiSeleccion.java:827-858):
 * the reversal of the previous pre-vouchers of reversible types ({@link ReversaPreComprobanteService}, always with
 * {@code IdPreComprobante = 0}) and the staging of the new ones ({@link EnvioContabilidadService}).
 */
@Service
public class PreComprobantePostProcesoService implements PreComprobantePostProcesoPort {

    private final ReversaPreComprobanteService reversa;
    private final EnvioContabilidadService envio;

    public PreComprobantePostProcesoService(ReversaPreComprobanteService reversa, EnvioContabilidadService envio) {
        this.reversa = reversa;
        this.envio = envio;
    }

    @Override
    public Resultado reversar(Long idEmpresa, String codigoUsuario, String fechaContable, List<Long> idPreComprobantesAnteriores) {
        List<ReversaPreComprobanteService.Reversion> reversiones = idPreComprobantesAnteriores.stream()
                .map(id -> new ReversaPreComprobanteService.Reversion(id, 0L)).toList();
        ReversaPreComprobanteService.Resultado r = reversa.reversar(idEmpresa, codigoUsuario, fechaContable, reversiones);
        return new Resultado(r.erroresContador(), r.mensajes());
    }

    @Override
    public Resultado enviar(Long idEmpresa, Long idUsuario, String idModulo, String usaReglasNegocio, String destino,
                            List<Long> idPreComprobantes) {
        EnvioContabilidadService.Resultado r = envio.enviar(idEmpresa, idUsuario, idModulo, destino, usaReglasNegocio, idPreComprobantes);
        return new Resultado(r.erroresContador(), r.mensajes());
    }
}
