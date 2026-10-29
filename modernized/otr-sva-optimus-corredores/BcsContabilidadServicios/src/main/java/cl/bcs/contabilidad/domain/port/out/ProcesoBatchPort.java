package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.MensajeSalida;

import java.util.List;

/**
 * Registration of a platform batch program, legacy {@code cl.bcs.plataforma.bat.InscribirProcesoBatch} (platform
 * module). Used by EnviarPreComprobante to schedule the ASIENTO_SAP program.
 */
public interface ProcesoBatchPort {

    /** One program parameter; {@code valorBlob} carries base64 content (legacy ValorBlob). */
    record Parametro(String nombre, String valor, String valorBlob) {}

    /** {@code idPrograma} is null when the registration reported errors. */
    record Inscripcion(long erroresContador, List<MensajeSalida> mensajes, Long idPrograma) {}

    Inscripcion inscribir(Long idEmpresa, String idModulo, Long idUsuario, String idTipoPrograma,
                          long segundosDesfase, List<Parametro> parametros);
}
