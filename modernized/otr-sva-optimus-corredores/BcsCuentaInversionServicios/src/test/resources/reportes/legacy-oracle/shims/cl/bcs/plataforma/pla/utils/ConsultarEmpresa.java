package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.oracle.Colaboradores;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.plataforma.pla.utils.ConsultarEmpresa (BcsPlataformaServicios/.../ConsultarEmpresa.java).
 * InformacionEmpresa is copied verbatim (ConsultarEmpresa.java:48-80). The real logica() joins 17 platform tables
 * (ConsultarEmpresa_Select.sql) and throws "Empresa no existe" when the company has no row (ConsultarEmpresa.java:41-42).
 * The shim answers from case.json colaboradores.empresa (fields of InformacionEmpresa) and throws the same exception
 * when colaboradores.empresa is absent. The reporting classes only read IdPais / Codigo / Nombre.
 */
public class ConsultarEmpresa implements JavaCall {

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public InformacionEmpresa logica(Connection conn, Long idEmpresa) throws Exception {
        Colaboradores.registrar("ConsultarEmpresa", java.util.Map.of("IdEmpresa", String.valueOf(idEmpresa)));
        if (!Colaboradores.CONFIG.has("empresa")) throw new Exception("Empresa no existe");
        return Utils.getGsonBuilder().fromJson(Colaboradores.CONFIG.get("empresa"), InformacionEmpresa.class);
    }

    public static class InformacionEmpresa {
        public Long IdEmpresa;
        public String Codigo;
        public String Nombre;
        public String IdEstado;
        public Long IdPais;
        public Long IdSocioDeNegocio;
        public Long IdContraparte;
        public Long IdPersona;
        public Long IdCliente;
        public Long IdCorredora;
        public java.sql.Timestamp FechaIngreso;
        public java.sql.Timestamp FechaCreacion;
        public java.sql.Timestamp FechaModificacion;
        public Long IdDocumentoIdentidad;
        public String DocumentoIdentidad;
        public String NombreCompleto;
        public String DocumentoIdentidadRepresentante;
        public String DireccionElectronicaPrincipal;
        public String DireccionFisicaPrincipalCalle;
        public String DireccionFisicaPrincipalNumero;
        public String DireccionFisicaPrincipalDepartamento;
        public String DireccionFisicaPrincipalSector;
        public String DireccionFisicaPrincipalNombreCiudad;
        public String DireccionFisicaPrincipalNombreDivisionPais;
        public String TelefonoPrincipal;
        public String CodigoPais;
        public String NombrePais;
        public String CodigoInscripcion;
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
}
