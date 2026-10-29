package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.SQL;

public class IdentificarLlaves {
	
	private final Long ID_PAIS_DEFAULT;
	private final Long ID_EMPRESA_DEFAULT;
	
    private final String SQL_ConsultarPaises = "SELECT \"IdPais\",\"NombrePais\",\"CodigoPais\" FROM \"BSCL.Plataforma.BD::REG.Pais\"";
    private final String SQL_ConsultarCiudadDivision = "SELECT \"IdPais\", \"IdCiudad\", \"IdDivisionPais\", \"NombreDivisionPais\", \"Codigo\" FROM \"BSCL.Plataforma.BD::REG.DivisionPais\"";
    private final String SQL_ConsultarGradoInstruccion = "SELECT \"IdGradoInstruccion\", \"Codigo\", \"Nombre\" FROM \"BSCL.SocioNegocio.BD::PER.GradoInstruccion\" WHERE \"IdPais\" = :IdPais";
    private final String SQL_ConsultarProfesion = "SELECT \"IdProfesion\", \"Nombre\" FROM \"BSCL.Plataforma.BD::REG.Profesion\" WHERE \"IdPais\" = :IdPais";
    private final String SQL_ConsultarGiro = "SELECT \"IdGiro\", \"Nombre\" FROM \"BSCL.Plataforma.BD::REG.Giro\" WHERE \"IdPais\" = :IdPais";
    private final String SQL_ConsultarTipoCargo = "SELECT \"CodigoTipoCargoPersona\", \"NombreTipoCargoPersona\" FROM \"BSCL.SocioNegocio.BD::PER.TipoCargoPersona\" WHERE \"IdEmpresa\" = :IdEmpresa";
    private final String SQL_ConsultarSucursal = "SELECT \"IdSucursal\", \"CodigoSucursal\" FROM \"BSCL.Plataforma.BD::PLA.Sucursal\" WHERE \"IdEmpresa\" = :IdEmpresa";
    private final String SQL_ConsultarBanco = "SELECT \"IdBanco\", \"NombreBanco\" FROM \"BSCL.Plataforma.BD::BAN.Banco\" WHERE \"IdEmpresa\" = :IdEmpresa";
    private final String SQL_ConsultarTipoCuentaBancaria = "SELECT \"IdTipoCuentaBancaria\", UPPER(\"NombreTipoCuentaBancaria\") AS \"NombreTipoCuentaBancaria\" FROM \"BSCL.Plataforma.BD::BAN.TipoCuentaBancaria\"";
    private final String SQL_ConsultarCategoriaInversionistas = "SELECT \"IdCategoriaInversionista\", \"NombreCategoriaInversionista\" FROM \"BSCL.Plataforma.BD::COM.CategoriaInversionista\" WHERE \"IdEmpresa\" = :IdEmpresa";
    private final String SQL_ConsultarClaseCliente = "SELECT \"IdClaseCliente\", \"Nombre\" FROM \"BSCL.SocioNegocio.BD::CLI.ClaseCliente\" WHERE \"IdEmpresa\" = :IdEmpresa";
    private final String SQL_ConsultarIupEjecutivo = "SELECT \"CodigoEmpleado\", \"IdEmpleado\" FROM \"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado\" CE WHERE \"IdEmpresa\" = :IdEmpresa";
    private final String SQL_ConsultarTipoRelacion = "SELECT \"IdTipoRelacion\", \"NombreTipoRelacion\" FROM \"BSCL.SocioNegocio.BD::REL.TipoRelacion\"";  
    private final String SQL_ConsultarPlaza = "select \"IdPlaza\", \"NombrePlaza\" from \"BSCL.Plataforma.BD.Vistas::BAN.ConsultarPlaza\" where \"IdPais\" = :IdPais";
    private final String SQL_ConsultarTipoDocumentoIdentidad = "SELECT \"IdTipoDocumentoIdentidad\", \"NombreTipoDocumentoIdentidad\" FROM \"BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad\" WHERE \"IdPais\" = :IdPais;";
    private final String SQL_ConsultarInstrumentos = "SELECT \"IdInstrumento\", \"Nemotecnico\" FROM \"BSCL.Plataforma.BD::INS.Instrumento\"";
    private final String SQL_ConsultarEstadoCivil = "SELECT \"IdTipoEstadoCivil\" FROM BSCL.\"BSCL.Plataforma.BD::REG.TipoEstadoCivil\" WHERE \"IdPais\" = :IdPais;";
    
    public static class EstadoCivil{
    	public String IdTipoEstadoCivil;
    }
    
    public static class Instrumentos{
    	public Long IdInstrumento;
    	public String Nemotecnico;
    }
    
    public static class TipoDocumentoIdentidad{
    	public String IdTipoDocumentoIdentidad;
    	public String NombreTipoDocumentoIdentidad;
    }
    public static class Plaza{
        public Long IdPlaza;
        public String NombrePlaza;
    }
    public static class TipoRelacion {
        public String IdTipoRelacion;
        public String NombreTipoRelacion;
    }

    public static class IupEmpleado {
        public String CodigoEmpleado;
        public Long IdEmpleado;
    }

    public static class ClaseCliente {
        public String IdClaseCliente;
        public String Nombre;
    }

    public static class CategoriaInversionista {
        public String IdCategoriaInversionista;
        public String NombreCategoriaInversionista;
    }

    public static class Banco {
        public Long IdBanco;
        public String NombreBanco;

        @Override
        public String toString() {
            return String.format("Banco [IdBanco=%s, NombreBanco=%s]", IdBanco, NombreBanco);
        }

    }

    public static class TipoCuentaBancaria {
        public String IdTipoCuentaBancaria;
        public String NombreTipoCuentaBancaria;

        @Override
        public String toString() {
            return String.format("TipoCuentaBancaria [IdTipoCuentaBancaria=%s, NombreTipoCuentaBancaria=%s]", IdTipoCuentaBancaria, NombreTipoCuentaBancaria);
        }

    }

    public static class Pais {
        public Long IdPais;
        public String NombrePais;
        public String CodigoPais;

        @Override
        public String toString() {
            return "Pais [IdPais=" + IdPais + ", NombrePais=" + NombrePais + ", CodigoPais=" + CodigoPais + "]";
        }
    }

    public static class CiudadDivision {
        public Long IdPais;
        public Long IdCiudad;
        public Long IdDivisionPais;
        public String NombreDivisionPais;
        public String Codigo;

        @Override
        public String toString() {
            return "CiudadDivision [IdPais=" + IdPais + ", IdCiudad=" + IdCiudad + ", IdDivisionPais=" + IdDivisionPais + ", NombreDivisionPais=" + NombreDivisionPais + "]";
        }
    }

    public static class GradoInstruccion {
        public Long IdGradoInstruccion;
        public String Codigo;
        public String Nombre;
    }

    public static class Profesion {
        public Long IdProfesion;
        public String Nombre;
    }

    public static class Giro {
        public Long IdGiro;
        public String Nombre;
    }

    public static class TipoCargoPersona {
        public String CodigoTipoCargoPersona;
        public String NombreTipoCargoPersona;
    }

    public static class Sucursal {
        public Long IdSucursal;
        public String CodigoSucursal;
    }

    private HashMap<String, Long> paises = new HashMap<String, Long>();
    private HashMap<String, Long> ciudades = new HashMap<String, Long>();
    private HashMap<String, Long> ciudadesUbigeo = new HashMap<String, Long>();
    private HashMap<String, Long> gradosInstruccion = new HashMap<String, Long>();
    private HashMap<String, Long> profesiones = new HashMap<String, Long>();
    private HashMap<String, Long> giros = new HashMap<String, Long>();
    private HashMap<String, String> tiposCargoPersona = new HashMap<String, String>();
    private HashMap<String, Long> sucursales = new HashMap<String, Long>();
    private HashMap<String, Long> bancos = new HashMap<String, Long>();
    private HashMap<String, String> tipoCuentasBancarias = new HashMap<String, String>();
    private HashMap<String, String> categoriasInversionista = new HashMap<String, String>();
    private HashMap<String, String> clasesCliente = new HashMap<String, String>();
    private HashMap<String, Long> iupEjecutivos = new HashMap<String, Long>();
    private HashMap<String, String> tiposRelacion = new HashMap<String, String>();
    private HashMap<String, Long> plazas = new HashMap<String, Long>();
    private HashMap<String, String> tiposDocumentoIdentidad = new HashMap<String, String>();
    private HashMap<String, Long> instrumentos = new HashMap<String, Long>();
    private HashMap<String, String> estadosCivil = new HashMap<String,String>();
    
    private SQL sql;
    private Connection conn;

    public IdentificarLlaves(Connection connection, Long idPais, Long idEmpresa) {
        this.sql = new SQL();
        this.conn = connection;
        this.ID_EMPRESA_DEFAULT = idEmpresa;
        this.ID_PAIS_DEFAULT=  idPais;
    }
    
    public void setEstadoCivil() throws SQLException{
    	
    	System.out.println("Cargando Estados Civiles");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdPais", ID_PAIS_DEFAULT);
        List<EstadoCivil> estadoCivilRS = sql.query(conn, SQL_ConsultarEstadoCivil, params, EstadoCivil.class);
        for (EstadoCivil estadoCivil : estadoCivilRS) {
        	estadosCivil.put(estadoCivil.IdTipoEstadoCivil, estadoCivil.IdTipoEstadoCivil);
        }
    }
    
    public void setInstrumentos() throws SQLException {
    	System.out.println("Cargando instrumentos");
        HashMap<String, Object> params = new HashMap<>();
        List<Instrumentos> instrumentosRS = sql.query(conn, SQL_ConsultarInstrumentos, params, Instrumentos.class);
        for (Instrumentos instrumento : instrumentosRS) {
            instrumentos.put(instrumento.Nemotecnico, instrumento.IdInstrumento);
        }
    }

    public void setTipoRelacion() throws SQLException {
        System.out.println("Cargando tipos relacion");
        HashMap<String, Object> params = new HashMap<>();
        List<TipoRelacion> tiposRelacionRS = sql.query(conn, SQL_ConsultarTipoRelacion, params, TipoRelacion.class);
        for (TipoRelacion tipoRelacion : tiposRelacionRS) {
            tiposRelacion.put(tipoRelacion.NombreTipoRelacion, tipoRelacion.IdTipoRelacion);
        }
    }

    public void setPaises() throws SQLException {
        // obtener paises
        System.out.println("Cargando paises");
        HashMap<String, Object> params = new HashMap<>();
        List<Pais> paisesRS = sql.query(conn, SQL_ConsultarPaises, params, Pais.class);
        for (Pais pais : paisesRS) {
            paises.put(pais.CodigoPais, pais.IdPais);
        }
    }

    public void setCiudades() throws SQLException {
        // obtener ciudades
        System.out.println("Cargando ciudades");
        HashMap<String, Object> params = new HashMap<>();
        List<CiudadDivision> ciudadDivisionesRS = sql.query(conn, SQL_ConsultarCiudadDivision, params, CiudadDivision.class);
        for (CiudadDivision ciuDiv : ciudadDivisionesRS) {
            String nombre = (ciuDiv.NombreDivisionPais != null) ? ciuDiv.NombreDivisionPais.replaceAll(" ", "").toUpperCase() : "";
            ciudades.put(nombre, ciuDiv.IdCiudad);
        }
    }
    
    public void setCiudadesUbigeo() throws SQLException {
    	// obtener ciudades por ubigeo
        System.out.println("Cargando ciudades ubigeo");
        HashMap<String, Object> params = new HashMap<>();
        List<CiudadDivision> ciudadDivisionesRS = sql.query(conn, SQL_ConsultarCiudadDivision, params, CiudadDivision.class);
        for (CiudadDivision ciuDiv : ciudadDivisionesRS) {
            String nombre = (ciuDiv.Codigo != null) ? ciuDiv.Codigo.replaceAll(" ", "").toUpperCase() : "";
            ciudadesUbigeo.put(nombre, ciuDiv.IdCiudad);
        }
    }

    public void setGradosInstruccion() throws SQLException {
        // obtener grado instruccion
        System.out.println("Cargando grados intruccion");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdPais", ID_PAIS_DEFAULT);
        List<GradoInstruccion> gradoInstruccionesRS = sql.query(conn, SQL_ConsultarGradoInstruccion, params, GradoInstruccion.class);
        for (GradoInstruccion grado : gradoInstruccionesRS) {
            gradosInstruccion.put(grado.Nombre.toUpperCase(), grado.IdGradoInstruccion);
        }
    }

    public void setProfesiones() throws SQLException {
        // obtener profesiones
        System.out.println("Cargando profesiones");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdPais", ID_PAIS_DEFAULT);
        List<Profesion> profesionesRS = sql.query(conn, SQL_ConsultarProfesion, params, Profesion.class);
        for (Profesion profesion : profesionesRS) {
            profesiones.put(profesion.Nombre, profesion.IdProfesion);
        }
    }

    public void setGiros() throws SQLException {
        // obtener giros
        System.out.println("Cargando giros");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdPais", ID_PAIS_DEFAULT);
        List<Giro> girosRS = sql.query(conn, SQL_ConsultarGiro, params, Giro.class);
        for (Giro giro : girosRS) {
            giros.put(giro.Nombre, giro.IdGiro);
        }
    }

    public void setTiposCargoPersona() throws SQLException {
        // obtener Tipos cargo persona
        System.out.println("Cargando tipos cargo persona");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", ID_EMPRESA_DEFAULT);
        List<TipoCargoPersona> tipoCargoPersonaRS = sql.query(conn, SQL_ConsultarTipoCargo, params, TipoCargoPersona.class);
        for (TipoCargoPersona tipoCargo : tipoCargoPersonaRS) {
            tiposCargoPersona.put(tipoCargo.CodigoTipoCargoPersona, tipoCargo.NombreTipoCargoPersona);
        }
    }

    public void setSucursales() throws SQLException {
        // obtener sucursales
        System.out.println("Cargando sucursales");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", ID_EMPRESA_DEFAULT);
        List<Sucursal> sucursalesRS = sql.query(conn, SQL_ConsultarSucursal, params, Sucursal.class);
        for (Sucursal sucursal : sucursalesRS) {
            sucursales.put(sucursal.CodigoSucursal, sucursal.IdSucursal);
        }
    }

    public void setBancos() throws SQLException {
        // obtener bancos
        System.out.println("Cargando bancos");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", ID_EMPRESA_DEFAULT);
        List<Banco> bancosRS = sql.query(conn, SQL_ConsultarBanco, params, Banco.class);
        for (Banco banco : bancosRS) {
            bancos.put(banco.NombreBanco, banco.IdBanco);
        }
    }

    public void setTiposCuentaBancaria() throws SQLException {
        // obtener tipos cuentas bancarias
        System.out.println("Cargando tipos cuentas bancarias");
        HashMap<String, Object> params = new HashMap<>();
        List<TipoCuentaBancaria> tipoCuentaBancariaRS = sql.query(conn, SQL_ConsultarTipoCuentaBancaria, params, TipoCuentaBancaria.class);
        for (TipoCuentaBancaria tipoCuentaBancaria : tipoCuentaBancariaRS) {
            tipoCuentasBancarias.put(tipoCuentaBancaria.NombreTipoCuentaBancaria, tipoCuentaBancaria.IdTipoCuentaBancaria);
        }
    }

    public void setCategoriasInversionista() throws SQLException {
        // obtener categorias inversionistas
        System.out.println("Cargando categorias inversionistas");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", ID_EMPRESA_DEFAULT);
        List<CategoriaInversionista> categoriaInversionistaRS = sql.query(conn, SQL_ConsultarCategoriaInversionistas, params, CategoriaInversionista.class);
        for (CategoriaInversionista categoriaInversionista : categoriaInversionistaRS) {
        	//SE ESTA BUSCANDO POR ID
            categoriasInversionista.put(categoriaInversionista.IdCategoriaInversionista.toUpperCase(), categoriaInversionista.IdCategoriaInversionista);
        }
    }

    public void setClasesCliente() throws SQLException {
        // obtener clases cliente
        System.out.println("Cargando clases cliente");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", ID_EMPRESA_DEFAULT);
        List<ClaseCliente> clasesClienteRS = sql.query(conn, SQL_ConsultarClaseCliente, params, ClaseCliente.class);
        for (ClaseCliente claseCliente : clasesClienteRS) {
            clasesCliente.put(claseCliente.Nombre, claseCliente.IdClaseCliente);
        }
    }

    public void setIupEjecutivo() throws SQLException {
        // obtner iup empleados
        System.out.println("Cargando iup ejecutivos");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", ID_EMPRESA_DEFAULT);
        List<IupEmpleado> iupEjecutivosRS = sql.query(conn, SQL_ConsultarIupEjecutivo, params, IupEmpleado.class);
        for (IupEmpleado iupEjecutivo : iupEjecutivosRS) {
            iupEjecutivos.put(iupEjecutivo.CodigoEmpleado.toUpperCase(), iupEjecutivo.IdEmpleado);
        }
    }
    
    public void setPlazas() throws SQLException{
    	// obtener plazas
        System.out.println("Cargando plazas");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdPais", ID_PAIS_DEFAULT);
        List<Plaza> plazaRs = sql.query(conn, SQL_ConsultarPlaza, params, Plaza.class);
        for (Plaza plaza : plazaRs) {
            plazas.put(plaza.NombrePlaza, plaza.IdPlaza);
        }
    }
    
    public void setTipoDocumentoIdentidad() throws SQLException{
    	// obtener plazas
        System.out.println("Cargando tipo documento identidad");
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdPais", ID_PAIS_DEFAULT);
        List<TipoDocumentoIdentidad> tiposDocumentiIdentidadRS = sql.query(conn, SQL_ConsultarTipoDocumentoIdentidad, params, TipoDocumentoIdentidad.class);
        for (TipoDocumentoIdentidad tipoDocumento : tiposDocumentiIdentidadRS) {
            tiposDocumentoIdentidad.put(tipoDocumento.NombreTipoDocumentoIdentidad.toUpperCase(), tipoDocumento.IdTipoDocumentoIdentidad);
        }
    }
    
    public String getTipoDocumentoIdentidad(String tipoDocumentoIdentidad){
    	return tiposDocumentoIdentidad.get(tipoDocumentoIdentidad);
    }
    
    public Long getPlaza(String plaza) {
        return plazas.get(plaza.toUpperCase());
    }

    public Long getPais(String codigoPais) {
        return paises.get(codigoPais);
    }
    
    public String getEstadoCivil(String estadoCivil) {
    	return estadosCivil.get(estadoCivil);
    }

    public Long getCiudad(String nombreCiudad) {
        // evaluar si es ciudad/division
        String ciudad = "";
        if (nombreCiudad != null && nombreCiudad.contains("/")) {
            // se asume que la ciudad va primero
            ciudad = nombreCiudad.split("/")[0];
        } else if (nombreCiudad != null && nombreCiudad.contains("-")) {
            // se asume que la ciudad va primero
            ciudad = nombreCiudad.split("-")[0];
        } else {
            ciudad = (nombreCiudad!=null)?nombreCiudad : "";
        }
        return ciudades.get(ciudad.replaceAll(" ", "").toUpperCase());
    }
    
    public Long getCiudadUbigeo(String ubigeo) {
        // buscar ciudad por ubigeo
        return ciudadesUbigeo.get(ubigeo.replaceAll(" ", "").toUpperCase());
    }
    
    

    public Long getGradoIntruccion(String gradoIns) {
        return gradosInstruccion.get(gradoIns.toUpperCase());
    }

    public Long getProfesion(String profesion) {
        return profesiones.get(profesion);
    }

    public Long getGiro(String giro) {
        return giros.get(giro);
    }

    public String getTipoCargoPersona(String tipoCargo) {
        return tiposCargoPersona.get(tipoCargo);
    }

    public Long getRangoPatrimonio(String rangoPatrimonio) {
        return (rangoPatrimonio!=null && !rangoPatrimonio.equals(""))?Long.parseLong(rangoPatrimonio) + 166000 : 0;// fix archivo peru es:
                                                        // 1,2,...,5
    }

    public Long getSucursal(String sucursal) {
        return sucursales.get(sucursal);
    }

    public Long getBanco(String banco) {
        return bancos.get(banco);
    }

    public String getTipoCuentaBancaria(String tipoCuentaBancaria) {
        return tipoCuentasBancarias.get(tipoCuentaBancaria);
    }

    public String getIdCategoriaInversionista(String categoriaInversionista) {
        String catInv = categoriasInversionista.get(categoriaInversionista.toUpperCase());
        return (catInv != null) ? catInv : "";
    }

    public String getClaseCliente(String claseCliente) {
        return clasesCliente.get(claseCliente);
    }

    public Long getIUPEjecutivo(String iupEjecutivo) {
        return iupEjecutivos.get((iupEjecutivo!=null)?iupEjecutivo.toUpperCase() : "");
    }

    public String getTipoRelacion(String tipoRelacion) {
        return tiposRelacion.get(tipoRelacion);
    }
    
    public Long getInstrumentos(String nemo) {
    	return instrumentos.get(nemo);
    }

}
