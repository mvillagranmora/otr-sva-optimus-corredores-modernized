CALL "BSCL.Normativa.BD.Servicios.NOR::CrearSolicitudCircularAntiguo" (
    p_IdEmpresa => :p_IdEmpresa,
    p_IdUsuario => :p_IdUsuario,
    p_IdModulo => :p_IdModulo,
    p_FechaIngresoSolicitud => :p_FechaIngresoSolicitud,
    p_FechaInicio => :p_FechaInicio,
    p_FechaTermino => :p_FechaTermino,
    p_ListaRut => #GenerarNCG365Masivo_TemporalListaRut,
    p_ListaRutAut => #GenerarNCG365Masivo_TemporalListaRutAut,
    p_ListaNemo => #GenerarNCG365Masivo_TemporalListaNemo,
    p_ListaRutEmpl => #GenerarNCG365Masivo_TemporalListaRutEmpl,
    p_IdTipoDocumento => :p_IdTipoDocumento,
    p_IdEstadoDocumento => :p_IdEstadoDocumento,
    p_CreaParam => :p_CreaParam,
    p_EsPauta => :p_EsPauta,
    p_TablaParametros => #GenerarNCG365Masivo_ParametrosEntrada,
    
    p_out_resultado => @p_out_resultado,
    p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
    p_out_ErroresContador => @p_out_ErroresContador,
    p_out_ErroresDetalle => $p_out_ErroresDetalle
);