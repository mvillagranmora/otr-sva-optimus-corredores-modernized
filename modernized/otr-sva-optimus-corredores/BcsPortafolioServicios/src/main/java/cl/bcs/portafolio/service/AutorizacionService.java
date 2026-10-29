package cl.bcs.portafolio.service;

import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioEntrada;
import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioResultado;
import cl.bcs.portafolio.port.out.AutorizacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AutorizacionService {

    private static final Logger log = LoggerFactory.getLogger(AutorizacionService.class);

    private final AutorizacionPort port;

    public AutorizacionService(AutorizacionPort port) {
        this.port = port;
    }

    public List<AutorizacionPortafolioResultado> autorizarMasivo(List<AutorizacionPortafolioEntrada> entradas) {
        Objects.requireNonNull(entradas, "entradas");
        if (entradas.isEmpty()) {
            throw new IllegalArgumentException("entradas must not be empty");
        }
        List<AutorizacionPortafolioResultado> resultados = new ArrayList<>(entradas.size());
        for (AutorizacionPortafolioEntrada entrada : entradas) {
            try {
                resultados.add(port.autorizarEventoPortafolio(entrada));
            } catch (Exception ex) {
                log.warn("autorizarMasivo: entry failed for idEvento={}: {}", entrada.idEvento(), ex.getMessage());
                resultados.add(new AutorizacionPortafolioResultado(null, "ERROR", ex.getMessage()));
            }
        }
        return resultados;
    }

    public List<AutorizacionPortafolioResultado> rechazarMasivo(List<AutorizacionPortafolioEntrada> entradas) {
        Objects.requireNonNull(entradas, "entradas");
        if (entradas.isEmpty()) {
            throw new IllegalArgumentException("entradas must not be empty");
        }
        List<AutorizacionPortafolioResultado> resultados = new ArrayList<>(entradas.size());
        for (AutorizacionPortafolioEntrada entrada : entradas) {
            try {
                resultados.add(port.rechazarEventoPortafolio(entrada));
            } catch (Exception ex) {
                log.warn("rechazarMasivo: entry failed for idEvento={}: {}", entrada.idEvento(), ex.getMessage());
                resultados.add(new AutorizacionPortafolioResultado(null, "ERROR", ex.getMessage()));
            }
        }
        return resultados;
    }

    // -------------------------------------------------------------------------
    // Static helpers — B-POR-09/10/11 pinned by oracle tests
    // -------------------------------------------------------------------------

    /** Mutable DTO for mass authorization field routing (AutorizacionModificarPortafolioMasivo.java) */
    public static class UpdatePortafolio {
        public String CodigoPortafolio;
        public Long IdNombreFantasia;
        public Long IdPersonaBrokerExtranjero;
        public Long IdEmpleadoEjecutivo;
        public String NumeroFicha;
        public String Alias;
        public String DescripcionPortafolio;
        public String Proposito;
        public String IdPerfilProductos;
        public String IdPerfilProductosExtra;
        public String IdPerfilCobros;
        public String IdPerfilCobrosExtra;
        public String IdPerfilCobrosPeru;
        public String IdPerfilCobrosPeruExtra;
        public String IdPerfilLimites;
        public String IdPerfilLimitesExtra;
        public String IdPerfilCondicionesOperativas;
        public String IdPerfilCondicionesOperativasExtra;
        public Long IdDireccionEnvioCorrespondencia;
        public Long IdTelefonoContacto;
        public String AreaCliente;
        public Long IdEmpleadoEjecutivoComercial;
    }

    /**
     * B-POR-09: field routing switch (AutorizacionModificarPortafolioMasivo.java lines 357-441).
     * Returns false when valorNuevo is null (guard at top of legacy method).
     * IdPerfilComercialBaseOrigen (B-POR-11) is whitelisted but makes no assignment — processed separately.
     */
    public static boolean asignarValorNuevo(UpdatePortafolio up, String campoModificado, String valorNuevo) {
        if (valorNuevo == null) return false;
        switch (campoModificado) {
            case "CodigoPortafolio":
                up.CodigoPortafolio = valorNuevo; break;
            case "IdNombreFantasia":
                up.IdNombreFantasia = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo); break;
            case "IdPersonaBrokerExtranjero":
                up.IdPersonaBrokerExtranjero = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo); break;
            case "IdEmpleadoEjecutivo":
                up.IdEmpleadoEjecutivo = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo); break;
            case "NumeroFicha":
                up.NumeroFicha = valorNuevo; break;
            case "Alias":
                up.Alias = valorNuevo; break;
            case "DescripcionPortafolio":
                up.DescripcionPortafolio = valorNuevo; break;
            case "Proposito":
                up.Proposito = valorNuevo; break;
            case "IdPerfilComercialBaseOrigen":
                break;
            case "IdPerfilProductos":
                up.IdPerfilProductos = valorNuevo; break;
            case "IdPerfilProductosExtra":
                up.IdPerfilProductosExtra = valorNuevo; break;
            case "IdPerfilCobros":
                up.IdPerfilCobros = valorNuevo; break;
            case "IdPerfilCobrosExtra":
                up.IdPerfilCobrosExtra = valorNuevo; break;
            case "IdPerfilCobrosPeru":
                up.IdPerfilCobrosPeru = valorNuevo; break;
            case "IdPerfilCobrosPeruExtra":
                up.IdPerfilCobrosPeruExtra = valorNuevo; break;
            case "IdPerfilLimites":
                up.IdPerfilLimites = valorNuevo; break;
            case "IdPerfilLimitesExtra":
                up.IdPerfilLimitesExtra = valorNuevo; break;
            case "IdPerfilCondicionesOperativas":
                up.IdPerfilCondicionesOperativas = valorNuevo; break;
            case "IdPerfilCondicionesOperativasExtra":
                up.IdPerfilCondicionesOperativasExtra = valorNuevo; break;
            case "IdDireccionEnvioCorrespondencia":
                up.IdDireccionEnvioCorrespondencia = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo); break;
            case "IdTelefonoContacto":
                up.IdTelefonoContacto = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo); break;
            case "AreaCliente":
                up.AreaCliente = valorNuevo; break;
            case "IdEmpleadoEjecutivoComercial":
                up.IdEmpleadoEjecutivoComercial = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo); break;
            default:
                break;
        }
        return true;
    }

    /** B-POR-10: campo whitelist (AutorizacionModificarPortafolioMasivo.java lines 492-521) */
    public static boolean esCampoValido(String campo) {
        switch (campo) {
            case "CodigoPortafolio":
            case "IdNombreFantasia":
            case "IdPersonaBrokerExtranjero":
            case "IdEmpleadoEjecutivo":
            case "NumeroFicha":
            case "Alias":
            case "DescripcionPortafolio":
            case "Proposito":
            case "IdPerfilComercialBaseOrigen":
            case "IdPerfilProductos":
            case "IdPerfilProductosExtra":
            case "IdPerfilCobros":
            case "IdPerfilCobrosExtra":
            case "IdPerfilCobrosPeru":
            case "IdPerfilCobrosPeruExtra":
            case "IdPerfilLimites":
            case "IdPerfilLimitesExtra":
            case "IdPerfilCondicionesOperativas":
            case "IdPerfilCondicionesOperativasExtra":
            case "IdDireccionEnvioCorrespondencia":
            case "IdTelefonoContacto":
            case "AreaCliente":
            case "IdEmpleadoEjecutivoComercial":
                return true;
            default:
                return false;
        }
    }
}
