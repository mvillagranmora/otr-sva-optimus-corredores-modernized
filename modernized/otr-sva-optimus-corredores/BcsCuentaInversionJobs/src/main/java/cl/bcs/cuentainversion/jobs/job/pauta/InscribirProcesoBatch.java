package cl.bcs.cuentainversion.jobs.job.pauta;

import cl.bcs.cuentainversion.jobs.runtime.SalidaPauta;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON contract of the platform collaborator {@code cl.bcs.plataforma.bat.InscribirProcesoBatch} (enrolls batch programs), as the legacy PAUTA
 * steps used it: the Entrada / Salida classes of the legacy service, with the same field names and constructor defaults. It is called through the
 * {@link Colaboradores} port (platform JavaCall router with the internal token), not in process.
 */
final class InscribirProcesoBatch {

    static final String CLASE = "cl.bcs.plataforma.bat.InscribirProcesoBatch";
    private static final String CADENA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LONGITUD = 30;

    private InscribirProcesoBatch() {
    }

    static class Parametro {
        public Long IdInterno;
        public String NombreParametro;
        public String Valor;
        public String ValorBlob;

        Parametro(Long idInterno, String nombreParametro, String valor, String valorBlob) {
            this.IdInterno = idInterno;
            this.NombreParametro = nombreParametro;
            this.Valor = valor;
            this.ValorBlob = valorBlob;
        }
    }

    static class InTipoPrograma {
        public Long IdInterno;
        public String IdTipoPrograma;
        public Long SegundosDesfase;
        public String Comentario = "";
    }

    static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<InTipoPrograma> p_TipoPrograma;
        public List<Parametro> p_Parametro;
    }

    /** Legacy {@code InscribirProcesoBatchSalida}: no field defaults (CIJ-DEF-12). */
    static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public List<Object> P_OUT_PROGRAMA;
        public List<MensajeSalida> P_OUT_MENSAJES;
    }

    /** The enrollment of one report program (the common part of the two report PAUTA steps, legacy lines 27-45). */
    static Entrada entrada(Long idEmpresa, String idTipoPrograma, List<Parametro> parametros) {
        Entrada entrada = new Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = 0L;
        entrada.p_IdModulo = "CTA";
        entrada.p_TipoPrograma = new ArrayList<>();
        InTipoPrograma tipoPrograma = new InTipoPrograma();
        tipoPrograma.IdInterno = 0L;
        tipoPrograma.IdTipoPrograma = idTipoPrograma;
        tipoPrograma.SegundosDesfase = 0L;
        entrada.p_TipoPrograma.add(tipoPrograma);
        entrada.p_Parametro = parametros;
        return entrada;
    }

    /** Legacy the legacy message concatenation (platform utils, lines 644-650): a line break before each description. */
    static String concatenarMensajes(List<MensajeSalida> mensajes) {
        String mensajeSalida = "";
        for (MensajeSalida mensaje : mensajes) {
            mensajeSalida += "\n" + mensaje.descripcion;
        }
        return mensajeSalida;
    }

    /** The same for the messages of the reporting family (record {@code reportes.MensajeSalida}). */
    static String concatenarMensajesReporte(List<cl.bcs.cuentainversion.reportes.MensajeSalida> mensajes) {
        String mensajeSalida = "";
        for (cl.bcs.cuentainversion.reportes.MensajeSalida mensaje : mensajes) {
            mensajeSalida += "\n" + mensaje.descripcion();
        }
        return mensajeSalida;
    }

    /** Legacy {@code generarNombreRandom}: 30 DIFFERENT characters of letters and digits. */
    static String nombreRandom() {
        String result = "";
        int tamanoCadena = CADENA.length();
        while (result.length() < LONGITUD) {
            int posicion = (int) (Math.random() * tamanoCadena);
            char letra = CADENA.charAt(posicion);
            if (!result.contains(String.valueOf(letra))) {
                result = result + letra;
            }
        }
        return result;
    }

    /** The shared body of the two report PAUTA steps: enroll and translate the answer (legacy lines 47-53). */
    static SalidaPauta inscribir(Colaboradores colaboradores, Entrada entrada) {
        Salida respuesta = colaboradores.invocar(CLASE, entrada, Salida.class);
        SalidaPauta salida = new SalidaPauta();
        salida.P_OUT_ERRORESCONTADOR = respuesta.P_OUT_ERRORESCONTADOR;
        salida.P_OUT_MENSAJE = concatenarMensajes(respuesta.P_OUT_MENSAJES);
        return salida;
    }
}
