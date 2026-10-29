package cl.bcs.loaderapp.pojo.socionegocio;

public class RelacionPersona {
	
	private String Relacion;
	private String IupCliente;
	private String IupRelacion;
	private String TipoPersona;
	private String Nombres;
	private String ApellidoPaterno;
	private String ApellidoMaterno;
	private String RazonSocial;
	private String PaisNacimientoConstitucion;
	private String PaisNacionalidadPrincipalCasaMatriz;
	private String PaisNacionalidadSecundario;
	private String PaisResidenciaPrincipal;
	private String PaisResidenciaSecundario;
	private String FechaNacimientoConstitucion;
	private String Genero;
	private String TipoEstadoCivil;
	private String ProfesionActividad;
	private String DependenciaLaboral;
	private String GradoInstruccion;
	private String CodigoGiro;
	private String Extranjero;
	private String FechaDefuncion;
	private String CodigoCargo;
	private String AniosServicio;
	private String IngresoPromedioMensual;
	private String IdRangoPatrimonio;
	private String CiudadDistrito;
	private String Calle;
	private String Numero;
	private String Complemento;
	private String Telefono;
	private String Email;
	private String DatosDeRegistrosPublicos;
	private String Asiento;
	private String FichaoPartida;

	public String getRelacion() {
		return Relacion;
	}

	public void setRelacion(String relacion) {
		Relacion = relacion;
	}

	public String getIupCliente() {
		return IupCliente;
	}

	public void setIupCliente(String iupCliente) {
		IupCliente = iupCliente;
	}

	public String getIupRelacion() {
		return IupRelacion;
	}

	public void setIupRelacion(String iupRelacion) {
		IupRelacion = iupRelacion;
	}

	public String getTipoPersona() {
		return (TipoPersona!=null && TipoPersona.equals("P")) ? "N" : "J";
	}

	public void setTipoPersona(String tipoPersona) {
		TipoPersona = tipoPersona;
	}

	public String getNombres() {
		return Nombres;
	}

	public void setNombres(String nombres) {
		Nombres = nombres;
	}

	public String getApellidoPaterno() {
		return ApellidoPaterno;
	}

	public void setApellidoPaterno(String apellidoPaterno) {
		ApellidoPaterno = apellidoPaterno;
	}

	public String getApellidoMaterno() {
		return ApellidoMaterno;
	}

	public void setApellidoMaterno(String apellidoMaterno) {
		ApellidoMaterno = apellidoMaterno;
	}

	public String getRazonSocial() {
		return RazonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		RazonSocial = razonSocial;
	}

	public String getPaisNacimientoConstitucion() {
		return PaisNacimientoConstitucion;
	}

	public void setPaisNacimientoConstitucion(String paisNacimientoConstitucion) {
		PaisNacimientoConstitucion = paisNacimientoConstitucion;
	}

	public String getPaisNacionalidadPrincipalCasaMatriz() {
		return PaisNacionalidadPrincipalCasaMatriz;
	}

	public void setPaisNacionalidadPrincipalCasaMatriz(String paisNacionalidadPrincipalCasaMatriz) {
		PaisNacionalidadPrincipalCasaMatriz = paisNacionalidadPrincipalCasaMatriz;
	}

	public String getPaisNacionalidadSecundario() {
		return PaisNacionalidadSecundario;
	}

	public void setPaisNacionalidadSecundario(String paisNacionalidadSecundario) {
		PaisNacionalidadSecundario = paisNacionalidadSecundario;
	}

	public String getPaisResidenciaPrincipal() {
		return PaisResidenciaPrincipal;
	}

	public void setPaisResidenciaPrincipal(String paisResidenciaPrincipal) {
		PaisResidenciaPrincipal = paisResidenciaPrincipal;
	}

	public String getPaisResidenciaSecundario() {
		return PaisResidenciaSecundario;
	}

	public void setPaisResidenciaSecundario(String paisResidenciaSecundario) {
		PaisResidenciaSecundario = paisResidenciaSecundario;
	}

	public String getFechaNacimientoConstitucion() {
		return (FechaNacimientoConstitucion!=null && !FechaNacimientoConstitucion.equals(""))? FechaNacimientoConstitucion : "0001-01-01";
	}

	public void setFechaNacimientoConstitucion(String fechaNacimientoConstitucion) {
		FechaNacimientoConstitucion = fechaNacimientoConstitucion;
	}

	public String getGenero() {
		return Genero;
	}

	public void setGenero(String genero) {
		Genero = genero;
	}

	public String getTipoEstadoCivil() {
		return TipoEstadoCivil;
	}

	public void setTipoEstadoCivil(String tipoEstadoCivil) {
		TipoEstadoCivil = tipoEstadoCivil;
	}

	public String getProfesionActividad() {
		return ProfesionActividad;
	}

	public void setProfesionActividad(String profesionActividad) {
		ProfesionActividad = profesionActividad;
	}

	public String getDependenciaLaboral() {
		return DependenciaLaboral;
	}

	public void setDependenciaLaboral(String dependenciaLaboral) {
		DependenciaLaboral = dependenciaLaboral;
	}

	public String getGradoInstruccion() {
		return GradoInstruccion;
	}

	public void setGradoInstruccion(String gradoInstruccion) {
		GradoInstruccion = gradoInstruccion;
	}

	public String getCodigoGiro() {
		return CodigoGiro;
	}

	public void setCodigoGiro(String codigoGiro) {
		CodigoGiro = codigoGiro;
	}

	public String getExtranjero() {
		return Extranjero;
	}

	public void setExtranjero(String extranjero) {
		Extranjero = extranjero;
	}

	public String getFechaDefuncion() {
		return FechaDefuncion;
	}

	public void setFechaDefuncion(String fechaDefuncion) {
		FechaDefuncion = fechaDefuncion;
	}

	public String getCodigoCargo() {
		return CodigoCargo;
	}

	public void setCodigoCargo(String codigoCargo) {
		CodigoCargo = codigoCargo;
	}

	public String getAniosServicio() {
		return AniosServicio;
	}

	public void setAniosServicio(String aniosServicio) {
		AniosServicio = aniosServicio;
	}

	public String getIngresoPromedioMensual() {
		return IngresoPromedioMensual;
	}

	public void setIngresoPromedioMensual(String ingresoPromedioMensual) {
		IngresoPromedioMensual = ingresoPromedioMensual;
	}

	public String getIdRangoPatrimonio() {
		return IdRangoPatrimonio;
	}

	public void setIdRangoPatrimonio(String idRangoPatrimonio) {
		IdRangoPatrimonio = idRangoPatrimonio;
	}

	public String getCiudadDistrito() {
		return CiudadDistrito;
	}

	public void setCiudadDistrito(String ciudadDistrito) {
		CiudadDistrito = ciudadDistrito;
	}

	public String getCalle() {
		return Calle;
	}

	public void setCalle(String calle) {
		Calle = calle;
	}

	public String getNumero() {
		return Numero;
	}

	public void setNumero(String numero) {
		Numero = numero;
	}

	public String getComplemento() {
		return Complemento;
	}

	public void setComplemento(String complemento) {
		Complemento = complemento;
	}

	public String getTelefono() {
		return Telefono;
	}

	public void setTelefono(String telefono) {
		Telefono = telefono;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public String getDatosDeRegistrosPublicos() {
		return DatosDeRegistrosPublicos;
	}

	public void setDatosDeRegistrosPublicos(String datosDeRegistrosPublicos) {
		DatosDeRegistrosPublicos = datosDeRegistrosPublicos;
	}

	public String getAsiento() {
		return Asiento;
	}

	public void setAsiento(String asiento) {
		Asiento = asiento;
	}

	public String getFichaoPartida() {
		return FichaoPartida;
	}

	public void setFichaoPartida(String fichaoPartida) {
		FichaoPartida = fichaoPartida;
	}

	@Override
	public String toString() {
		return "RelacionPersona [Relacion=" + Relacion + ", IupCliente=" + IupCliente + ", IupRelacion=" + IupRelacion
				+ ", TipoPersona=" + TipoPersona + ", Nombres=" + Nombres + ", ApellidoPaterno=" + ApellidoPaterno
				+ ", ApellidoMaterno=" + ApellidoMaterno + ", RazonSocial=" + RazonSocial
				+ ", PaisNacimientoConstitucion=" + PaisNacimientoConstitucion
				+ ", PaisNacionalidadPrincipalCasaMatriz=" + PaisNacionalidadPrincipalCasaMatriz
				+ ", PaisNacionalidadSecundario=" + PaisNacionalidadSecundario + ", PaisResidenciaPrincipal="
				+ PaisResidenciaPrincipal + ", PaisResidenciaSecundario=" + PaisResidenciaSecundario
				+ ", FechaNacimientoConstitucion=" + FechaNacimientoConstitucion + ", Genero=" + Genero
				+ ", TipoEstadoCivil=" + TipoEstadoCivil + ", ProfesionActividad=" + ProfesionActividad
				+ ", DependenciaLaboral=" + DependenciaLaboral + ", GradoInstruccion=" + GradoInstruccion
				+ ", CodigoGiro=" + CodigoGiro + ", Extranjero=" + Extranjero + ", FechaDefuncion=" + FechaDefuncion
				+ ", CodigoCargo=" + CodigoCargo + ", AniosServicio=" + AniosServicio + ", IngresoPromedioMensual="
				+ IngresoPromedioMensual + ", IdRangoPatrimonio=" + IdRangoPatrimonio + ", CiudadDistrito="
				+ CiudadDistrito + ", Calle=" + Calle + ", Numero=" + Numero + ", Complemento=" + Complemento
				+ ", Telefono=" + Telefono + ", Email=" + Email + ", DatosDeRegistrosPublicos="
				+ DatosDeRegistrosPublicos + ", Asiento=" + Asiento + ", FichaoPartida=" + FichaoPartida
				+ ", getRelacion()=" + getRelacion() + ", getIupCliente()=" + getIupCliente() + ", getIupRelacion()="
				+ getIupRelacion() + ", getTipoPersona()=" + getTipoPersona() + ", getNombres()=" + getNombres()
				+ ", getApellidoPaterno()=" + getApellidoPaterno() + ", getApellidoMaterno()=" + getApellidoMaterno()
				+ ", getRazonSocial()=" + getRazonSocial() + ", getPaisNacimientoConstitucion()="
				+ getPaisNacimientoConstitucion() + ", getPaisNacionalidadPrincipalCasaMatriz()="
				+ getPaisNacionalidadPrincipalCasaMatriz() + ", getPaisNacionalidadSecundario()="
				+ getPaisNacionalidadSecundario() + ", getPaisResidenciaPrincipal()=" + getPaisResidenciaPrincipal()
				+ ", getPaisResidenciaSecundario()=" + getPaisResidenciaSecundario()
				+ ", getFechaNacimientoConstitucion()=" + getFechaNacimientoConstitucion() + ", getGenero()="
				+ getGenero() + ", getTipoEstadoCivil()=" + getTipoEstadoCivil() + ", getProfesionActividad()="
				+ getProfesionActividad() + ", getDependenciaLaboral()=" + getDependenciaLaboral()
				+ ", getGradoInstruccion()=" + getGradoInstruccion() + ", getCodigoGiro()=" + getCodigoGiro()
				+ ", getExtranjero()=" + getExtranjero() + ", getFechaDefuncion()=" + getFechaDefuncion()
				+ ", getCodigoCargo()=" + getCodigoCargo() + ", getAniosServicio()=" + getAniosServicio()
				+ ", getIngresoPromedioMensual()=" + getIngresoPromedioMensual() + ", getIdRangoPatrimonio()="
				+ getIdRangoPatrimonio() + ", getCiudadDistrito()=" + getCiudadDistrito() + ", getCalle()=" + getCalle()
				+ ", getNumero()=" + getNumero() + ", getComplemento()=" + getComplemento() + ", getTelefono()="
				+ getTelefono() + ", getEmail()=" + getEmail() + ", getDatosDeRegistrosPublicos()="
				+ getDatosDeRegistrosPublicos() + ", getAsiento()=" + getAsiento() + ", getFichaoPartida()="
				+ getFichaoPartida() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
}
