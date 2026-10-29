package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.MensajeSalida;

import java.util.List;

/**
 * Steps that run after pre-vouchers are generated (legacy GeneraPreComprobanteMultiSeleccion L827-858): the reversal of
 * the previous pre-voucher of reversible types (legacy {@code ReversaPreComprobante}) and the sending of the new
 * pre-vouchers (legacy {@code EnvioContabilidad}, {@code p_Destino = B}, {@code p_UsaReglasNegocio = S}). Implemented by
 * the envio services (WP-B).
 */
public interface PreComprobantePostProcesoPort {

    record Resultado(long erroresContador, List<MensajeSalida> mensajes) {
        public static Resultado vacio() { return new Resultado(0L, List.of()); }
    }

    /** Legacy {@code ReversaPreComprobante.logica} with {@code IdPreComprobante = 0} per previous pre-voucher. */
    Resultado reversar(Long idEmpresa, String codigoUsuario, String fechaContable, List<Long> idPreComprobantesAnteriores);

    /** Legacy {@code EnvioContabilidad.logica}. */
    Resultado enviar(Long idEmpresa, Long idUsuario, String idModulo, String usaReglasNegocio, String destino,
                     List<Long> idPreComprobantes);
}
