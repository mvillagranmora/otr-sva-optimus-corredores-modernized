package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoContratos;
import cl.bcs.apv.api.casouso.RevocarContratos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosEntrada;
import cl.bcs.apv.api.dto.revocarcontratos.EntidadContrato;
import cl.bcs.apv.api.dto.revocarcontratos.InContrato;
import cl.bcs.apv.api.dto.revocarcontratos.MensajesErrores;
import cl.bcs.apv.api.dto.revocarcontratos.RevocarContratosEntrada;
import cl.bcs.apv.api.dto.revocarcontratos.RevocarContratosResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.contratos.RevocarContratosRepositorio;
import cl.bcs.apv.domain.port.out.contratos.RevocarContratosRepositorio.ResultadoValidaciones;

/**
 * Use case {@code RevocarContratos}: legacy {@code cl.bcs.APV.CambiarEstadoRevocacionSAAPV} (validates each VIG contract, assigns a revocation id from
 * the sequence and revokes it through the typed use case {@link CambiarEstadoContratos}).
 */
@Transactional(rollbackFor = Exception.class)
public class RevocarContratosServicio implements RevocarContratos {

    private static final String SECUENCIA = "BSCL.APV.BD.Secuencias::IdSAAPVRevocacion";

    private final Colaboradores colaboradores;
    private final Secuencias secuencias;
    private final RevocarContratosRepositorio repositorio;
    private final CambiarEstadoContratos cambiarEstadoContratos;

    public RevocarContratosServicio(Colaboradores colaboradores, Secuencias secuencias, RevocarContratosRepositorio repositorio,
            CambiarEstadoContratos cambiarEstadoContratos) {
        this.colaboradores = colaboradores;
        this.secuencias = secuencias;
        this.repositorio = repositorio;
        this.cambiarEstadoContratos = cambiarEstadoContratos;
    }

    @Override
    public RevocarContratosResultado ejecutar(RevocarContratosEntrada entrada) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<EntidadContrato> contrato = new ArrayList<>();

        // the answer is not used: the legacy only calls it (a failure of the call would propagate)
        colaboradores.invocar("RevisarRequisitosBasicos", Json.objeto()
                .poner("p_IdEmpresa", entrada.idEmpresa())
                .poner("p_IdUsuario", entrada.idUsuario())
                .poner("p_IdModulo", entrada.idModulo())
                .construir());

        // the legacy asks the sequence for as many values as contracts (a null list is a NullPointerException), valid or not
        int cantidad = entrada.contratosSAAPV().size();
        List<Long> secuenciaSAAPV = new ArrayList<>();
        for (int k = 0; k < cantidad; k++) {
            secuenciaSAAPV.add(secuencias.siguiente(SECUENCIA));
        }
        int i = 0;
        long j = 0;

        // LEGACY DEFECT kept (see README APV-A-321): ONE contract object and ONE request are shared and mutated for all the contracts; the request holds the
        // same object once per valid contract, so every call sees the current values repeated
        int validos = 0;

        for (InContrato mov : entrada.contratosSAAPV()) {
            String folioSaapv = mov.idFolioContratoSAAPV();

            List<ResultadoValidaciones> contratosExistentesValidaciones = repositorio.contratoVigente(folioSaapv);

            if (mov.idCliente() == null || mov.idPersona() == null) {
                contrato.add(entidad(mov.idFolioContratoSAAPV(), 0L, "ERROR", "CLIENTE_NO_EXISTE"));
                j++;
                continue;
            } else if (contratosExistentesValidaciones.size() == 0) {
                contrato.add(entidad(mov.idFolioContratoSAAPV(), 0L, "ERROR", "CONTRATO_NO_VALIDO"));
                j++;
                continue;
            } else {
                contrato.add(entidad(mov.idFolioContratoSAAPV(), secuenciaSAAPV.get(i), "OK", "REVOCACION_CONTRATO_OK"));
            }

            ResultadoValidaciones idempleadorobjeto = contratosExistentesValidaciones.get(0);
            Long idempleador = idempleadorobjeto.IdEmpleador;

            cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV inContrato = new cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV(
                    mov.idPersona(), mov.idCliente(), idempleador, mov.idFolioContratoSAAPV(), mov.fechaRevocacion(), "", "PDS", "REV", mov.idGlosa(),
                    mov.descripcionGlosa());
            validos++;

            List<cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV> repetidos = new ArrayList<>();
            for (int k = 0; k < validos; k++) {
                repetidos.add(inContrato);
            }
            CambiarEstadoContratosEntrada contratosSAAPVEntrada = new CambiarEstadoContratosEntrada(entrada.idEmpresa(), entrada.idUsuario(), "APV", "REV",
                    repetidos, "N");

            cambiarEstadoContratos.ejecutar(contratosSAAPVEntrada);
            i++;
        }

        return new RevocarContratosResultado(0L, mensajes, contrato, j);
    }

    private static EntidadContrato entidad(String folio, Long idFolioSAAPV, String tipo, String codigo) {
        List<MensajesErrores> mensaje = new ArrayList<>();
        mensaje.add(new MensajesErrores(folio, tipo, codigo, codigo));
        return new EntidadContrato(folio, idFolioSAAPV, mensaje);
    }
}
