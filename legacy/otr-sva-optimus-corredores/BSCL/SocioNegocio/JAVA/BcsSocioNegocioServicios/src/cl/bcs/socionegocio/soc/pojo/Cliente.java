package cl.bcs.socionegocio.soc.pojo;

import cl.bcs.utils.BcsPojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cliente extends BcsPojo {
    public Long IdInterno;

    public Long IdPersona;
    public Long IdEnrolamiento;
    public DatosGenerales DatosGenerales;
    public Fatca Fatca;
    public Map<String, Indicador> IndicadoresPersonaPorCodigoTipoIndicador;
    public Map<String, Indicador> IndicadoresClientePorCodigoTipoIndicador;

    // Excepciones
    public String TieneResidenciaTributariaExtranjera;

    // Entidades de Cliente
    public List<Portafolio> Portafolios;

    // Entidades de Persona
    public Long CantidadDocumentosIdentidad;
    public Long CantidadCuentasBancarias;
    public Long CantidadTarjetasBancarias;
    public Long CantidadTelefonos;
    public Long CantidadDireccionesFisicas;
    public Long CantidadDireccionesElectronicas;
    public Long CantidadGruposDireccionesElectronicas;
    public Long CantidadRelaciones;
    public Long CantidadNombresFantasia;
    public Long CantidadFatca;

    public static class DatosGenerales extends BcsPojo {

        public Long IdCliente;
        public Long IdPersona;
        public String TipoEnrolamiento;
        public String TipoPersona;

        // Persona / Datos Generales
        public String Nombres;
        public String ApellidoPaterno;
        public String ApellidoMaterno;
        public String IdTransparenciaFiscal;
        public String Sexo;
        public java.sql.Date FechaNacimiento;
        public java.sql.Date FechaDefuncion;
        public String EstadoCivil;
        public String RegimenConyugal;
        public String Cargo;
        public String CodigoTipoCargoEmpleado;
        public String DetalleCargo;
        public String RazonSocial;
	    public String ResidenciaTributariaExtranjera;
        public BigDecimal IngresoPromedioMensual;
        public String GlobalLegalEntity;
        public java.sql.Timestamp FechaInicioActividad;
        public Long IdSegmentoCliente;
        public Long IdPaisNacimiento;
        public Long IdPaisResidencia;
        public Long IdPersonaConyuge;
        public Long IdPersonaEmpleador;
        public Long IdProfesion;
        public Long IdPaisNacionalidadPrincipal;
        public Long IdRangoPatrimonio;
        public Long IdGiroPrincipal;
        public Long IdPersonaRepresentante;
        public Long IdNombreFantasiaPrincipal;
        public String IdTipoCanalIngresoCorredor;
        public String IdSituacionLaboral;
        public String CodigoRangoRenta;
        public String CodigoRangoNivelVenta;
        
        // Cliente / Datos Generales
        public String IdTipoCliente;
        public String CodigoAdicionalHolding;
        public String CodigoEjecutivoExterno;
        public String IdCategoriaInversionista;
        public String IdClaseCliente;
        public String IdTipoBloqueo;
        public Long IdEmpleadoEjecutivo;
        public Long IdSucursalPreferencia;
        public String CLI_AreaCliente;
        public String CLI_IdTipoCliente;
        public String CLI_CodigoAdicionalHolding;
        public String CLI_CodigoEjecutivoExterno;
        public String CLI_IdCategoriaInversionista;
        public String CLI_IdClaseCliente;
        public Long CLI_IdPersonaEjecutivo;
        public Long CLI_IdEmpleadoEjecutivo;
        public Long CLI_IdSegmentoCliente;
        public Long CLI_IdSucursalPreferencia;
        public java.sql.Date FechaBloqueo;

        // Excepciones
        public transient String EsBorrador;
    }

    public static class Fatca extends BcsPojo {
        public Long IdCliente;
        public Long IdPersona;
        public String IdClasificacionFatca;
        public String IdClasificacionEmpresaFatca;
        public java.sql.Timestamp FechaFirmaFormulario;
        public String IdEstado;
    }

    public Cliente() {
        CantidadDocumentosIdentidad = 0L;
        CantidadCuentasBancarias = 0L;
        CantidadTarjetasBancarias = 0L;
        CantidadTelefonos = 0L;
        CantidadDireccionesFisicas = 0L;
        CantidadDireccionesElectronicas = 0L;
        CantidadGruposDireccionesElectronicas = 0L;
        CantidadRelaciones = 0L;
        CantidadNombresFantasia = 0L;
        CantidadFatca = 0L;
        Portafolios = new ArrayList<>();
        IndicadoresClientePorCodigoTipoIndicador = new HashMap<>();
        IndicadoresPersonaPorCodigoTipoIndicador = new HashMap<>();
    }

    public boolean tieneIdEnrolamiento() {
        return IdEnrolamiento != null;
    }

    public boolean tieneIdPersona() {
        return IdPersona != null;
    }

    public Long getIdEntidad() {
        return tieneIdEnrolamiento() ? IdEnrolamiento : IdPersona;
    }
}
