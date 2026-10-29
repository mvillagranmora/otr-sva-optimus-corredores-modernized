package cl.bcs.loaderapp.pojo.socionegocio;

import cl.bcs.utils.Utils;

public class Cliente {
	public String TipoPersona;
	public String TipoDocumentoIdentidad;
	public String DocumentoIdentidad;
	public String TipoDocumentoIdentidadAsociado;
	public String DocumentoIdentidadAsociado;
	public String RUT;
	public String Nombres;
	public String ApellidoPaterno;
	public String ApellidoMaterno;
	public String RazonSocial;
	public String PaisNacimientoConstitucion;
	public String PaisNacionalidadPrincipalCasaMatriz;
	public String PaisNacionalidadSecundario;
	public String PaisResidenciaPrincipal;
	public String PaisResidenciaSecundario;
	public String FechaNacimientoConstitucion;
	public String Genero;
	public String TipoEstadoCivil;
	public String ProfesionActividad;
	public String GradoInstruccion;
	public String CodigoGiro;
	public String Extranjero;
	public String FechaDefuncion;
	public String CodigoTipoCargoPersona;
	public String AnioServicio;
	public String IngresoPromedioMensual;
	public String CodigoRangoPatrimonio;
	public String CiudadDistrito;
	public String Distrito;
	public String Calle;
	public String Numero;
	public String Complemento;
	public String Telefono;
	public String Email;
	public String NombreCortoCategoriaInversionista;
	public String TipoCliente;
	public String DocumentoIdentidadAsesor;
	public String SucursalCliente;
	public String EstadoCliente;
	public String FechaBloqueo;
	public String CodigoTipoBloqueo;
	public String ComentarioBloqueo;
	public String NombreBanco;
	public String TipoCuenta;
	public String IdMoneda;
	public String NumeroCuentaBancaria;
	public String TipoIdentificadorCuenta;
	public String IdentificadorCuenta;
	public String PlazaDistrito;
	public String CodigoPortafolio;
	public String DescripcionPortafolio;
	public String AliasPortafolio;
	public String EstadoPortafolio;
	public String FechaBloqueoPortafolio;
	public String CodigoTipoBloqueoPortafolio;
	public String ComentarioBloqueoPortafolio;
	public Long IdCarga;
	public int Linea;
	
	public int getLinea() {
		return Linea;
	}
	public void setLinea(int linea) {
		Linea = linea;
	}
	public String getTipoPersona() {
		return TipoPersona;
	}
	public void setTipoPersona(String tipoPersona) {
		TipoPersona = tipoPersona;
	}
	public String getTipoDocumentoIdentidad() {
		return TipoDocumentoIdentidad;
	}
	public void setTipoDocumentoIdentidad(String tipoDocumentoIdentidad) {
		TipoDocumentoIdentidad = tipoDocumentoIdentidad;
	}
	public String getDocumentoIdentidad() {
		return DocumentoIdentidad;
	}
	public void setDocumentoIdentidad(String documentoIdentidad) {
		DocumentoIdentidad = documentoIdentidad;
	}
	public String getTipoDocumentoIdentidadAsociado() {
		return TipoDocumentoIdentidadAsociado;
	}
	public void setTipoDocumentoIdentidadAsociado(String tipoDocumentoIdentidadAsociado) {
		TipoDocumentoIdentidadAsociado = tipoDocumentoIdentidadAsociado;
	}
	public String getDocumentoIdentidadAsociado() {
		return DocumentoIdentidadAsociado;
	}
	public void setDocumentoIdentidadAsociado(String documentoIdentidadAsociado) {
		DocumentoIdentidadAsociado = documentoIdentidadAsociado;
	}
	public String getRUT() {
		return RUT;
	}
	public void setRUT(String rUT) {
		RUT = rUT;
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
		return FechaNacimientoConstitucion;
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
	public String getCodigoTipoCargoPersona() {
		return CodigoTipoCargoPersona;
	}
	public void setCodigoTipoCargoPersona(String codigoTipoCargoPersona) {
		CodigoTipoCargoPersona = codigoTipoCargoPersona;
	}
	public String getAnioServicio() {
		return AnioServicio;
	}
	public void setAnioServicio(String anioServicio) {
		AnioServicio = anioServicio;
	}
	public String getIngresoPromedioMensual() {
		return IngresoPromedioMensual;
	}
	public void setIngresoPromedioMensual(String ingresoPromedioMensual) {
		IngresoPromedioMensual = ingresoPromedioMensual;
	}
	public String getCodigoRangoPatrimonio() {
		return CodigoRangoPatrimonio;
	}
	public void setCodigoRangoPatrimonio(String codigoRangoPatrimonio) {
		CodigoRangoPatrimonio = codigoRangoPatrimonio;
	}
	public String getCiudadDistrito() {
		return CiudadDistrito;
	}
	public void setCiudadDistrito(String ciudadDistrito) {
		CiudadDistrito = ciudadDistrito;
	}
	public String getDistrito() {
		return Distrito;
	}
	public void setDistrito(String distrito) {
		Distrito = distrito;
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
	public String getNombreCortoCategoriaInversionista() {
		return NombreCortoCategoriaInversionista;
	}
	public void setNombreCortoCategoriaInversionista(String nombreCortoCategoriaInversionista) {
		NombreCortoCategoriaInversionista = nombreCortoCategoriaInversionista;
	}
	public String getTipoCliente() {
		return TipoCliente;
	}
	public void setTipoCliente(String tipoCliente) {
		TipoCliente = tipoCliente;
	}
	public String getDocumentoIdentidadAsesor() {
		return DocumentoIdentidadAsesor;
	}
	public void setDocumentoIdentidadAsesor(String documentoIdentidadAsesor) {
		DocumentoIdentidadAsesor = documentoIdentidadAsesor;
	}
	public String getSucursalCliente() {
		return SucursalCliente;
	}
	public void setSucursalCliente(String sucursalCliente) {
		SucursalCliente = sucursalCliente;
	}
	public String getEstadoCliente() {
		return EstadoCliente;
	}
	public void setEstadoCliente(String estadoCliente) {
		EstadoCliente = estadoCliente;
	}
	public String getFechaBloqueo() {
		return FechaBloqueo;
	}
	public void setFechaBloqueo(String fechaBloqueo) {
		FechaBloqueo = fechaBloqueo;
	}
	public String getCodigoTipoBloqueo() {
		return CodigoTipoBloqueo;
	}
	public void setCodigoTipoBloqueo(String codigoTipoBloqueo) {
		CodigoTipoBloqueo = codigoTipoBloqueo;
	}
	public String getComentarioBloqueo() {
		return ComentarioBloqueo;
	}
	public void setComentarioBloqueo(String comentarioBloqueo) {
		ComentarioBloqueo = comentarioBloqueo;
	}
	public String getNombreBanco() {
		return NombreBanco;
	}
	public void setNombreBanco(String nombreBanco) {
		NombreBanco = nombreBanco;
	}
	public String getTipoCuenta() {
		return TipoCuenta;
	}
	public void setTipoCuenta(String tipoCuenta) {
		TipoCuenta = tipoCuenta;
	}
	public String getIdMoneda() {
		return IdMoneda;
	}
	public void setIdMoneda(String idMoneda) {
		IdMoneda = idMoneda;
	}
	public String getNumeroCuentaBancaria() {
		return NumeroCuentaBancaria;
	}
	public void setNumeroCuentaBancaria(String numeroCuentaBancaria) {
		NumeroCuentaBancaria = numeroCuentaBancaria;
	}
	public String getTipoIdentificadorCuenta() {
		return TipoIdentificadorCuenta;
	}
	public void setTipoIdentificadorCuenta(String tipoIdentificadorCuenta) {
		TipoIdentificadorCuenta = tipoIdentificadorCuenta;
	}
	public String getIdentificadorCuenta() {
		return IdentificadorCuenta;
	}
	public void setIdentificadorCuenta(String identificadorCuenta) {
		IdentificadorCuenta = identificadorCuenta;
	}
	public String getPlazaDistrito() {
		return PlazaDistrito;
	}
	public void setPlazaDistrito(String plazaDistrito) {
		PlazaDistrito = plazaDistrito;
	}
	public String getCodigoPortafolio() {
		return CodigoPortafolio;
	}
	public void setCodigoPortafolio(String codigoPortafolio) {
		CodigoPortafolio = codigoPortafolio;
	}
	public String getDescripcionPortafolio() {
		return DescripcionPortafolio;
	}
	public void setDescripcionPortafolio(String descripcionPortafolio) {
		DescripcionPortafolio = descripcionPortafolio;
	}
	public String getAliasPortafolio() {
		return AliasPortafolio;
	}
	public void setAliasPortafolio(String aliasPortafolio) {
		AliasPortafolio = aliasPortafolio;
	}
	public String getEstadoPortafolio() {
		return EstadoPortafolio;
	}
	public void setEstadoPortafolio(String estadoPortafolio) {
		EstadoPortafolio = estadoPortafolio;
	}
	public String getFechaBloqueoPortafolio() {
		return FechaBloqueoPortafolio;
	}
	public void setFechaBloqueoPortafolio(String fechaBloqueoPortafolio) {
		FechaBloqueoPortafolio = fechaBloqueoPortafolio;
	}
	public String getCodigoTipoBloqueoPortafolio() {
		return CodigoTipoBloqueoPortafolio;
	}
	public void setCodigoTipoBloqueoPortafolio(String codigoTipoBloqueoPortafolio) {
		CodigoTipoBloqueoPortafolio = codigoTipoBloqueoPortafolio;
	}
	public String getComentarioBloqueoPortafolio() {
		return ComentarioBloqueoPortafolio;
	}
	public void setComentarioBloqueoPortafolio(String comentarioBloqueoPortafolio) {
		ComentarioBloqueoPortafolio = comentarioBloqueoPortafolio;
	}
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
