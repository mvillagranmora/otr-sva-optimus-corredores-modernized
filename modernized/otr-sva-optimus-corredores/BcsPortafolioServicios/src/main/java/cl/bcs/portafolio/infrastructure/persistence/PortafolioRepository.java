package cl.bcs.portafolio.infrastructure.persistence;

import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioEntrada;
import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioResultado;
import cl.bcs.portafolio.domain.consumo.ConsumoEnLineaEntrada;
import cl.bcs.portafolio.domain.consumo.ConsumoEnLineaResultado;
import cl.bcs.portafolio.domain.consumo.ConsumoLimiteEntrada;
import cl.bcs.portafolio.domain.consumo.ConsumoLimiteResultado;
import cl.bcs.portafolio.domain.custodio.CuentaCustodioEntrada;
import cl.bcs.portafolio.domain.custodio.CuentaCustodioResultado;
import cl.bcs.portafolio.domain.matriz.MatrizAgrupadorEntrada;
import cl.bcs.portafolio.domain.matriz.MatrizAgrupadorResultado;
import cl.bcs.portafolio.domain.portafolio.*;
import cl.bcs.portafolio.domain.preferencia.PreferenciaPagoEntrada;
import cl.bcs.portafolio.domain.preferencia.PreferenciaPagoResultado;
import cl.bcs.portafolio.port.out.AutorizacionPort;
import cl.bcs.portafolio.port.out.ConsumoPort;
import cl.bcs.portafolio.port.out.CustodioPreferenciaPort;
import cl.bcs.portafolio.port.out.PortafolioCRUDPort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PortafolioRepository
        implements PortafolioCRUDPort, AutorizacionPort, ConsumoPort, CustodioPreferenciaPort {

    // ---- PortafolioCRUDPort ----

    @Override
    public CrearPortafolioResultado crearPortafolio(CrearPortafolioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: SP CrearPortafolio");
    }

    @Override
    public CrearPortafolioConReplicaResultado crearPortafolioConReplica(CrearPortafolioConReplicaEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: SP CrearPortafolioConReplica");
    }

    @Override
    public PortafolioResultado eliminarSinTransacciones(EliminarPortafolioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: SP EliminarSinTransacciones");
    }

    @Override
    public PortafolioResultado modificarPortafolio(ModificarPortafolioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: SOLICITUDCAMBIO + CAMBIOPORTAFOLIO");
    }

    @Override
    public PortafolioResultado modificarEstadoPortafolio(ModificarEstadoPortafolioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: SP ModificarEstadoPortafolio");
    }

    @Override
    public void crearCuentaInversion(CuentaInversionCrear cuentaInversion) {
        throw new UnsupportedOperationException("requires HANA: SP CrearCuentaInversion");
    }

    @Override
    public void crearEventoAutorizacion(Long idPortafolio, String tipo) {
        throw new UnsupportedOperationException("requires HANA: SP CrearEventoAutorizacion");
    }

    @Override
    public void confirmarCrearPortafolio(Long idPortafolio) {
        throw new UnsupportedOperationException("requires HANA: SP ConfirmarCrearPortafolio");
    }

    @Override
    public void replicaPortafolioAPV(Long idPortafolioOrigen, Long idPortafolioDestino) {
        throw new UnsupportedOperationException("requires HANA: SP ReplicaPortafolioAPV");
    }

    @Override
    public void agregarLog(Long idPortafolio, String mensaje) {
        throw new UnsupportedOperationException("requires HANA: LOG_PORTAFOLIO");
    }

    @Override
    public List<MatrizAgrupadorResultado> consultarMatrizAgrupador(MatrizAgrupadorEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: MATRIZAGRUPADOR");
    }

    @Override
    public List<CategoriaInvOperacion> consultarCategoriaInversionista(Long idPortafolio) {
        throw new UnsupportedOperationException("requires HANA: CATEGORIAINVERSIONISTA");
    }

    @Override
    public String consultarConfiguracionSistema(String clave) {
        throw new UnsupportedOperationException("requires HANA: CONFIGURACIONSISTEMA");
    }

    @Override
    public String consultarPerfilComercialIdPerfilCobros(Long idPerfilComercial) {
        throw new UnsupportedOperationException("requires HANA: PERFILCOMERCIAL");
    }

    @Override
    public String consultarPerfilComercialIdPlantillaProducto(Long idPerfilComercial) {
        throw new UnsupportedOperationException("requires HANA: PERFILCOMERCIAL");
    }

    @Override
    public boolean verificarRequisitosBasicos(Long idPersona, String propositoPortafolio) {
        throw new UnsupportedOperationException("requires HANA: cross-service RevisarRequisitosBasicos");
    }

    // ---- AutorizacionPort ----

    @Override
    public AutorizacionPortafolioResultado autorizarEventoPortafolio(AutorizacionPortafolioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: SP AutorizarEvento");
    }

    @Override
    public AutorizacionPortafolioResultado rechazarEventoPortafolio(AutorizacionPortafolioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: SP RechazarEvento");
    }

    // ---- ConsumoPort ----

    @Override
    public ConsumoEnLineaResultado consultarConsumoEnLinea(ConsumoEnLineaEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: cross-service BcsCuentaInversionServicios");
    }

    @Override
    public ConsumoLimiteResultado consultarLimiteConsumo(ConsumoLimiteEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: cross-service BcsMesaServicios");
    }

    // ---- CustodioPreferenciaPort ----

    @Override
    public CuentaCustodioResultado consultarCuentaCustodio(CuentaCustodioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: CUENTACUSTODIO");
    }

    @Override
    public void actualizarCuentaCustodio(CuentaCustodioEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: CUENTACUSTODIO");
    }

    @Override
    public PreferenciaPagoResultado crearPreferenciaPago(PreferenciaPagoEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: PREFERENCIAPAGO");
    }

    @Override
    public void actualizarPreferenciaPago(PreferenciaPagoEntrada entrada) {
        throw new UnsupportedOperationException("requires HANA: PREFERENCIAPAGO");
    }
}
