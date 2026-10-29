package cl.bcs.socionegocio.soc.pojo;

import java.util.ArrayList;
import java.util.List;

public class SocioNegocio {

	public Long							IdEntidad						= 0L; //SIRVE?
	public String						TipoEntidad						= ""; //PERSONA - CLIENTE SIRVE?
	public String 						TipoEnrolamiento				= ""; //FULL-EXPRESS
	public String						TipoDocumentoIdentidad			= ""; 
	public String						DocumentoIdentidad				= ""; 
	public String						Nombres							= "";
	public String						ApellidoPaterno					= "";
	public String						ApellidoMaterno					= "";
	public String						IdGenero						= "";
	public String						RazonSocial						= "";
	public Long							IdPaisNacimientoConstitucion	= 0L;
	public String						ResidenciaTributariaExtranjera	= "";
	public Long 						IdPaisResidenciaCasaMatriz		= 0L;
	public Long 						IdPaisNacionalidadPrincipal		= 0L;
	public String						FechaNacimientoConstitucion		= "";
	public String						FechaInicioActividad			= "";
	public String						FechaDefuncion					= "";
	public String						GlobalLegalEntity				= "";
	public String						EstadoCivil						= "";
	public Long							IdGiro							= 0L;
	public Long							IdProfesion						= 0L;
	public String						Cargo							= "";
	public Long							Empleador						= 0L;
	public Long							IdRangoPatrimonio				= 0L;
	public String						IdTipoPersona					= "";

	public List<DatosCliente>				DatosCliente;
	public List<DocumentoIdentidad>			DocumentosIdentidad;
    public List<DireccionFisica>			DireccionFisica;
    public List<Telefono>					Telefono;
    public List<DireccionElectronica>		DireccionElectronica;
    public List<IndicadorPersonaCliente>	IndicadorPersona;
    public List<CuentaBancaria>				CuentaBancaria;
    public List<TarjetasBancaria>			TarjetasBancaria;
    public List<NombreFantasia>				NombreFantasia;
    public List<Relacionados>				Relacionados;
    
	public SocioNegocio(){
		DatosCliente = new ArrayList<DatosCliente>();
		DocumentosIdentidad = new ArrayList<DocumentoIdentidad>();
        DireccionFisica = new ArrayList<DireccionFisica>();
        Telefono = new ArrayList<Telefono>();
        DireccionElectronica = new ArrayList<DireccionElectronica>();
        IndicadorPersona = new ArrayList<IndicadorPersonaCliente>();
        CuentaBancaria = new ArrayList<CuentaBancaria>();
        TarjetasBancaria = new ArrayList<TarjetasBancaria>();
        NombreFantasia = new ArrayList<NombreFantasia>();
        Relacionados = new ArrayList<Relacionados>();
	}
	
}
