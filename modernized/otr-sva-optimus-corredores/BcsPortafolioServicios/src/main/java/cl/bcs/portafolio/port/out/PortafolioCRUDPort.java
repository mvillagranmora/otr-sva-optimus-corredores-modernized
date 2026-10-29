package cl.bcs.portafolio.port.out;

import cl.bcs.portafolio.domain.matriz.MatrizAgrupadorEntrada;
import cl.bcs.portafolio.domain.matriz.MatrizAgrupadorResultado;
import cl.bcs.portafolio.domain.portafolio.*;

import java.util.List;

public interface PortafolioCRUDPort {

    CrearPortafolioResultado crearPortafolio(CrearPortafolioEntrada entrada);
    CrearPortafolioConReplicaResultado crearPortafolioConReplica(CrearPortafolioConReplicaEntrada entrada);
    PortafolioResultado eliminarSinTransacciones(EliminarPortafolioEntrada entrada);
    PortafolioResultado modificarPortafolio(ModificarPortafolioEntrada entrada);
    PortafolioResultado modificarEstadoPortafolio(ModificarEstadoPortafolioEntrada entrada);

    void crearCuentaInversion(CuentaInversionCrear cuentaInversion);
    void crearEventoAutorizacion(Long idPortafolio, String tipo);
    void confirmarCrearPortafolio(Long idPortafolio);
    void replicaPortafolioAPV(Long idPortafolioOrigen, Long idPortafolioDestino);
    void agregarLog(Long idPortafolio, String mensaje);

    List<MatrizAgrupadorResultado> consultarMatrizAgrupador(MatrizAgrupadorEntrada entrada);
    List<CategoriaInvOperacion> consultarCategoriaInversionista(Long idPortafolio);
    String consultarConfiguracionSistema(String clave);
    String consultarPerfilComercialIdPerfilCobros(Long idPerfilComercial);
    String consultarPerfilComercialIdPlantillaProducto(Long idPerfilComercial);
    boolean verificarRequisitosBasicos(Long idPersona, String propositoPortafolio);
}
