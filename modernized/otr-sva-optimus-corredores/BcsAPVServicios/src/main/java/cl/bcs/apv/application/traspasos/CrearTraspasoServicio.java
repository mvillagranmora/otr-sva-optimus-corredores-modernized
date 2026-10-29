package cl.bcs.apv.application.traspasos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CrearTraspaso;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.creartraspaso.CrearTraspasoEntrada;
import cl.bcs.apv.api.dto.creartraspaso.CrearTraspasoResultado;
import cl.bcs.apv.api.dto.creartraspaso.DetalleOrigenTraspaso;
import cl.bcs.apv.api.dto.creartraspaso.Traspaso;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoRepositorio.DetalleOrigenFila;
import cl.bcs.apv.domain.port.out.traspasos.CrearTraspasoRepositorio.TraspasoFila;

/**
 * Use case {@code CrearTraspaso}: legacy {@code cl.bcs.APV.CrearTraspaso}. LEGACY DEFECT kept (see README APV-B-15): the traspaso INSERT binds a field the
 * legacy POJO lacks, so it always fails and the answer is the ERROR message carrying the SQL layer text; the success branches are ported as the legacy wrote them.
 */
@Transactional(rollbackFor = Exception.class)
public class CrearTraspasoServicio implements CrearTraspaso {

    private static final String SEQ_TRASPASO = "BSCL.APV.BD.Secuencias::TRA.IdTraspaso";
    private static final String SEQ_DETALLE = "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso";

    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final CrearTraspasoRepositorio repositorio;

    public CrearTraspasoServicio(ConfiguracionSistema configuracion, Secuencias secuencias, CrearTraspasoRepositorio repositorio) {
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.repositorio = repositorio;
    }

    @Override
    public CrearTraspasoResultado ejecutar(CrearTraspasoEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        Long idTraspasoSalida = null;
        String fechaSistema = Parametros.fechaSistema(configuracion, in.idEmpresa());
        long count = 1L;
        Traspaso traspaso = in.traspaso();
        try {
            Long idTraspaso = secuencias.siguiente(SEQ_TRASPASO);
            TraspasoFila tra = new TraspasoFila();
            tra.IdTraspaso = idTraspaso;
            tra.IdContratoSAAPV = traspaso.idContratoSAAPV();
            tra.IdEmpresa = in.idEmpresa();
            tra.TipoMovimiento = "EGRESO";
            tra.RazonRetiro = traspaso.razonRetiro();
            tra.FechaIngreso = fechaSistema;
            tra.FechaEgreso = "0001-01-01";
            tra.FechaModificacion = "0001-01-01";
            tra.CodigoTipoTraspaso = traspaso.codigoTipoTraspaso();
            tra.Descripcion = traspaso.descripcion();
            tra.IdCliente = traspaso.idCliente();
            tra.IdPersona = traspaso.idPersona();
            tra.CodigoTipoAhorro = traspaso.codigoTipoAhorro();
            tra.NombreEntidad = traspaso.nombreEntidad();
            tra.DocumentoEntidad = traspaso.documentoEntidad();
            tra.IdEstado = "PDI";
            tra.Canal = traspaso.canal();
            tra.CierreSolicitud = "0001-01-01";
            List<TraspasoFila> lstTraspaso = new ArrayList<>();
            lstTraspaso.add(tra);
            int[] resultado = repositorio.insertarTraspaso(lstTraspaso);
            if (resultado != null && resultado.length > 0 && resultado[0] == 1) {
                mensajes.add(Mensajes.de(count, "INFORMACION", "CREACION_TRASPASO_OK", "Creacion del traspaso: " + idTraspaso + " exitosa."));
                List<DetalleOrigenFila> lstOrigen = new ArrayList<>();
                for (DetalleOrigenTraspaso row : in.detalleOrigenTraspaso()) {
                    count++;
                    Long idDetalle = secuencias.siguiente(SEQ_DETALLE);
                    DetalleOrigenFila d = new DetalleOrigenFila();
                    d.IdDetalleOrigenTraspaso = idDetalle;
                    d.IdTraspaso = idTraspaso;
                    d.CodigoTipoAhorro = row.codigoTipoAhorro();
                    d.IdEntidadRecaudadora = row.idEntidadRecaudadora();
                    d.NombreEntidad = row.nombreEntidad();
                    d.DocumentoEntidad = row.documentoEntidad();
                    d.Fondo = row.fondo();
                    d.MontoTraspaso = row.montoTraspaso();
                    d.UFTraspaso = row.ufTraspaso();
                    d.PorcentajeTraspaso = row.porcentajeTraspaso();
                    d.IdEstado = "VIG";
                    d.IdInstrumento = 0L;
                    d.IdPortafolio = 0L;
                    d.CodigoRegimenTributario = "";
                    lstOrigen.add(d);
                    int[] result = repositorio.insertarDetallesOrigen(lstOrigen);
                    if (result != null && result.length > 0 && resultado[0] == 1) {
                        mensajes.add(Mensajes.de(count, "INFORMACION", "CREACION_ORIGEN_TRASPASO_OK", "Creacion del Origen traspaso: " + idDetalle + " exitosa."));
                        idTraspasoSalida = idTraspaso;
                    }
                }
            }
        } catch (Exception er) {
            try {
                repositorio.revertir();
            } catch (java.sql.SQLException e) {
                throw Sneaky.lanzar(e);
            }
            mensajes = new ArrayList<>(mensajes);
            mensajes.add(Mensajes.de(count, "ERROR", "ERROR", er.getMessage()));
        }
        return new CrearTraspasoResultado(mensajes, Mensajes.numeroErrores(mensajes), idTraspasoSalida);
    }
}
