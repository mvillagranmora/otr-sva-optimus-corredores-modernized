package cl.bcs.loaderapp.pojo.socionegocio;

public class Persona {
    private String TipoPersona;
    private String IUP;
    private String RUT;
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
    private String GradoInstruccion;
    private String CodigoGiro;
    private String Extranjero;
    private String FechaDefuncion;
    private String CodigoCargo;
    private String AnioServicio;
    private String IngresoPromedioMensual;
    private String IdRangoPatrimonio;
    private String CiudadDistrito;
    private String Calle;
    private String Numero;
    private String Complemento;
    private String Telefono;
    private String Email;
    private String NombreCortoCategoriaInversionista;
    private String TipoCliente;
    private String SegmentoEconomico;
    private String IUP_Ejecutivo;
    private String IUPIntermediario;
    private String SucursalCliente;
    private String EstadoCliente;
    private String FechaBloqueo;
    private String CodigoTipoBloqueo;
    private String ComentarioBloqueo;
    private String NombreBanco;
    private String TipoCuenta;
    private String IdMoneda;
    private String NumeroCuentaBancaria;
    private String TipoIdentificadorCuenta;
    private String IdentificadorCuenta;
    private String PlazaDistrito;
    private String CodigoPortafolio;
    private String DescripcionPortafolio;
    private String CodigoProposito;
    private String AliasPortafolio;
    private String EstadoPortafolio;
    private String FechaBloqueoPortafolio;
    private String CodigoTipoBloqueoPortafolio;
    private String ComentarioBloqueoPortafolio;
    private String MensajeValidacion;

    public String getTipoPersona() {
        return (TipoPersona!=null && TipoPersona.equals("N")) ? "N" : "J" ;
    }

    public void setTipoPersona(String tipoPersona) {
        TipoPersona = tipoPersona;
    }

    public String getIUP() {
        return IUP;
    }

    public void setIUP(String iUP) {
        IUP = iUP;
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
        return (FechaNacimientoConstitucion!=null && !FechaNacimientoConstitucion.equals(""))?FechaNacimientoConstitucion : "01-01-0001";
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
        if(FechaDefuncion!=null && FechaDefuncion.equals("")){
            return "01-01-0001";
        }else{
            return FechaDefuncion;
        }
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

    public String getAnioServicio() {
        return (AnioServicio != null && !AnioServicio.equals("")) ? AnioServicio : "0";
    }

    public void setAnioServicio(String anioServicio) {
        AnioServicio = anioServicio;
    }

    public String getIngresoPromedioMensual() {
        return (IngresoPromedioMensual != null && !IngresoPromedioMensual.equals("")) ? IngresoPromedioMensual.substring(0, IngresoPromedioMensual.indexOf(".")) : "0";
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

    public String getSegmentoEconomico() {
        return SegmentoEconomico;
    }

    public void setSegmentoEconomico(String segmentoEconomico) {
        SegmentoEconomico = segmentoEconomico;
    }

    public String getIUP_Ejecutivo() {
        return IUP_Ejecutivo;
    }

    public void setIUP_Ejecutivo(String iUP_Ejecutivo) {
        IUP_Ejecutivo = iUP_Ejecutivo;
    }

    public String getIUPIntermediario() {
        return IUPIntermediario;
    }

    public void setIUPIntermediario(String iUPIntermediario) {
        IUPIntermediario = iUPIntermediario;
    }

    public String getSucursalCliente() {
        return SucursalCliente;
    }

    public void setSucursalCliente(String sucursalCliente) {
        SucursalCliente = sucursalCliente;
    }

    public String getEstadoCliente() {
        return (EstadoCliente != null && EstadoCliente.equals("A")) ? "ACT" : "BLO";
    }

    public void setEstadoCliente(String estadoCliente) {
        EstadoCliente = estadoCliente;
    }

    public String getFechaBloqueo() {
        return (FechaBloqueo != null && !FechaBloqueo.equals("")) ? FechaBloqueo : "01-01-0001";
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
        return (ComentarioBloqueo!=null)? ComentarioBloqueo : "";
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

    public String getCodigoProposito() {
        return CodigoProposito;
    }

    public void setCodigoProposito(String codigoProposito) {
        CodigoProposito = codigoProposito;
    }

    public String getAliasPortafolio() {
        return AliasPortafolio;
    }

    public void setAliasPortafolio(String aliasPortafolio) {
        AliasPortafolio = aliasPortafolio;
    }

    public String getEstadoPortafolio() {
        return (EstadoPortafolio!=null && EstadoPortafolio.equals("A"))?"ACT":"BLO";
    }

    public void setEstadoPortafolio(String estadoPortafolio) {
        EstadoPortafolio = estadoPortafolio;
    }

    public String getFechaBloqueoPortafolio() {
        return (FechaBloqueoPortafolio!=null && !FechaBloqueoPortafolio.equals(""))?FechaBloqueoPortafolio : "01-01-0001";
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

    public String getMensajeValidacion() {
        return MensajeValidacion;
    }

    public void setMensajeValidacion(String mensajeValidacion) {
        MensajeValidacion = mensajeValidacion;
    }

    @Override
    public String toString() {
        return String.format(
                "Persona [TipoPersona=%s, IUP=%s, RUT=%s, Nombres=%s, ApellidoPaterno=%s, ApellidoMaterno=%s, RazonSocial=%s, PaisNacimientoConstitucion=%s, PaisNacionalidadPrincipalCasaMatriz=%s, PaisNacionalidadSecundario=%s, PaisResidenciaPrincipal=%s, PaisResidenciaSecundario=%s, FechaNacimientoConstitucion=%s, Genero=%s, TipoEstadoCivil=%s, ProfesionActividad=%s, gradoInstruccion=%s, CodigoGiro=%s, Extranjero=%s, FechaDefuncion=%s, CodigoCargo=%s, AnioServicio=%s, IngresoPromedioMensual=%s, IdRangoPatrimonio=%s, CiudadDistrito=%s, Calle=%s, Numero=%s, Complemento=%s, Telefono=%s, Email=%s, NombreCortoCategoriaInversionista=%s, TipoCliente=%s, SegmentoEconomico=%s, IUP_Ejecutivo=%s, IUPIntermediario=%s, SucursalCliente=%s, EstadoCliente=%s, FechaBloqueo=%s, CodigoTipoBloqueo=%s, ComentarioBloqueo=%s, NombreBanco=%s, TipoCuenta=%s, IdMoneda=%s, NumeroCuentaBancaria=%s, TipoIdentificadorCuenta=%s, IdentificadorCuenta=%s, PlazaDistrito=%s, CodigoPortafolio=%s, DescripcionPortafolio=%s, CodigoProposito=%s, AliasPortafolio=%s, EstadoPortafolio=%s, FechaBloqueoPortafolio=%s, CodigoTipoBloqueoPortafolio=%s, ComentarioBloqueoPortafolio=%s, MensajeValidacion=%s]",
                TipoPersona, IUP, RUT, Nombres, ApellidoPaterno, ApellidoMaterno, RazonSocial, PaisNacimientoConstitucion, PaisNacionalidadPrincipalCasaMatriz, PaisNacionalidadSecundario, PaisResidenciaPrincipal, PaisResidenciaSecundario, FechaNacimientoConstitucion, Genero, TipoEstadoCivil, ProfesionActividad, GradoInstruccion, CodigoGiro, Extranjero, FechaDefuncion, CodigoCargo, AnioServicio, IngresoPromedioMensual, IdRangoPatrimonio, CiudadDistrito, Calle, Numero, Complemento, Telefono,
                Email, NombreCortoCategoriaInversionista, TipoCliente, SegmentoEconomico, IUP_Ejecutivo, IUPIntermediario, SucursalCliente, EstadoCliente, FechaBloqueo, CodigoTipoBloqueo, ComentarioBloqueo, NombreBanco, TipoCuenta, IdMoneda, NumeroCuentaBancaria, TipoIdentificadorCuenta, IdentificadorCuenta, PlazaDistrito, CodigoPortafolio, DescripcionPortafolio, CodigoProposito, AliasPortafolio, EstadoPortafolio, FechaBloqueoPortafolio, CodigoTipoBloqueoPortafolio, ComentarioBloqueoPortafolio,
                MensajeValidacion);
    }
}
