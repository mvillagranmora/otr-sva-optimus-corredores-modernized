package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.SQLException;

public class IdentificarLlavesBuilder {
    private IdentificarLlaves identificarLlaves;

    public IdentificarLlavesBuilder(Connection conn, Long idPais, Long idEmpresa) {
        identificarLlaves = new IdentificarLlaves(conn, idPais, idEmpresa);
    }
    
    /**
     * Constructor DUMMY, utiliza pais 1 y empresa 1
     * utilizar contructor con parametros idPais e idEmpresa
     * @param conn
     */
    public IdentificarLlavesBuilder(Connection conn) {
        identificarLlaves = new IdentificarLlaves(conn, (long) 1, (long) 1);
    }
    
    public IdentificarLlavesBuilder setEstadoCivil() throws SQLException {
    	identificarLlaves.setEstadoCivil();
    	return this;
    }

    public IdentificarLlavesBuilder setInstrumentos() throws SQLException {
        identificarLlaves.setInstrumentos();
        return this;
    }
    
    public IdentificarLlavesBuilder setPaises() throws SQLException {
        identificarLlaves.setPaises();
        return this;
    }

    public IdentificarLlavesBuilder setCiudades() throws SQLException {
        identificarLlaves.setCiudades();
        return this;
    }
    
    public IdentificarLlavesBuilder setCiudadesUbigeo() throws SQLException {
        identificarLlaves.setCiudadesUbigeo();
        return this;
    }

    public IdentificarLlavesBuilder setGradosInstruccion() throws SQLException {
        identificarLlaves.setGradosInstruccion();
        return this;
    }

    public IdentificarLlavesBuilder setProfesiones() throws SQLException {
        identificarLlaves.setProfesiones();
        return this;
    }

    public IdentificarLlavesBuilder setGiros() throws SQLException {
        identificarLlaves.setGiros();
        return this;
    }

    public IdentificarLlavesBuilder setTiposCargoPersona() throws SQLException {
        identificarLlaves.setTiposCargoPersona();
        return this;
    }

    public IdentificarLlavesBuilder setSucursales() throws SQLException {
        identificarLlaves.setSucursales();
        return this;
    }

    public IdentificarLlavesBuilder setBancos() throws SQLException {
        identificarLlaves.setBancos();
        return this;
    }

    public IdentificarLlavesBuilder setTiposCuentaBancaria() throws SQLException {
        identificarLlaves.setTiposCuentaBancaria();
        return this;
    }

    public IdentificarLlavesBuilder setCategoriasInversionista() throws SQLException {
        identificarLlaves.setCategoriasInversionista();
        return this;
    }

    public IdentificarLlavesBuilder setClasesCliente() throws SQLException {
        identificarLlaves.setClasesCliente();
        return this;
    }

    public IdentificarLlavesBuilder setIupEjecutivo() throws SQLException {
        identificarLlaves.setIupEjecutivo();
        return this;
    }
    public IdentificarLlavesBuilder setTipoRelacion() throws SQLException{
        identificarLlaves.setTipoRelacion();
        return this;
    }
    public IdentificarLlavesBuilder setPlazas() throws SQLException{
        identificarLlaves.setPlazas();
        return this;
    }
    
    public IdentificarLlavesBuilder setTipoDocumentoIdentidad() throws SQLException{
        identificarLlaves.setTipoDocumentoIdentidad();
        return this;
    }

    public IdentificarLlaves build() {
        return identificarLlaves;
    }
}
