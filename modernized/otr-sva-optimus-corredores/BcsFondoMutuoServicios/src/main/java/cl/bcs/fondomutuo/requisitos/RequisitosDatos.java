package cl.bcs.fondomutuo.requisitos;

import java.util.List;

/** Reads of legacy {@code ServicioCumpleRequisitosCrearSolicitudAporteRescate} (verbatim SQL: {@code ComunesSolicitudes_SelectPortafolio.sql}, {@code ..._SelectEmpleadoUsuario.sql}). */
public interface RequisitosDatos {

    /** Legacy {@code pojo.ClientePortafolioSolicitudes} (row of {@code ComunesSolicitudes_SelectPortafolio.sql}). */
    class ClientePortafolioSolicitudes {
        public String NombreCompleto;
        public String CodigoPortafolio;
        public Long IdPortafolio;
        public Long IdCliente;
        public String IdEstadoCliente;
        public String IdEstadoPortafolio;
        public String DocumentoIdentidad;
        public Long IdEmpleadoEjecutivo;
        public String TipoDocumentoIdentidad;
        public String Proposito;
        public Long IdPersona;
        public String EsClienteNuevoProspecto;
        public String BloqueadoPorCumplimiento = "N";
    }

    /** Row of {@code ..._SelectEmpleadoUsuario.sql} (the query also returns {@code IdEntidad}, not mapped). */
    class ObjValidaProducto {
        public String ValidaProducto;
        public String IdTipoOperacion;
        public String IdTipoOperacionTodos;
    }

    List<ClientePortafolioSolicitudes> clientesPortafolio(List<Long> idsPortafolio);

    List<ObjValidaProducto> empleadoUsuario(Long idUsuario);
}
