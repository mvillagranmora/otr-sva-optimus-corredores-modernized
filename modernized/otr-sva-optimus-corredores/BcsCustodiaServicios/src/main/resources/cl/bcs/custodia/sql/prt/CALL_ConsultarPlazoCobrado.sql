CALL "BSCL.Plataforma.BD.Servicios::ConsultarPlazo"(
    p_IdEmpresa => :p_IdEmpresa,
    p_IdUsuario => :p_IdUsuario,
    p_IdModulo => :p_IdModulo,
    p_UsaReglasNegocio => 'S',
    
    p_FechaDesde => :p_FechaDesde,
    p_FechaHasta => :p_FechaHasta,
    p_DiasHabiles => :p_DiasHabiles,
    p_IdCondicionDeLiquidacion => :p_IdCondicionDeLiquidacion,
    p_IdPais => :p_IdPais,
    
    p_out_PlazoDias=>@p_out_PlazoDias,
    p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
    p_out_ErroresContador=>@p_out_ErroresContador,
    p_out_ErroresDetalle => $p_out_ErroresDetalle
);