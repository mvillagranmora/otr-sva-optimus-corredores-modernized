CALL "BSCL.Custodia.BD.Servicios.CAR::CrearCargaMasivaCustodia"( 
    p_IdEmpresa => :p_IdEmpresa,
    p_IdUsuario => :p_IdUsuario,
    p_IdModulo => :p_IdModulo,
    p_UsaReglasNegocio => :p_UsaReglasNegocio,
    
    p_FechaCarga => :p_FechaCarga,
    p_CargaMasiva => #CARGA_MASIVA,
    p_DetalleCargaMasiva => #CARGA_MASIVA_DETALLE,
    
    p_out_SecuenciaCargaMasiva => $p_out_SecuenciaCargaMasiva,
    p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
    p_out_ErroresContador => @p_out_ErroresContador,
    p_out_ErroresDetalle => $p_out_ErroresDetalle
    
)