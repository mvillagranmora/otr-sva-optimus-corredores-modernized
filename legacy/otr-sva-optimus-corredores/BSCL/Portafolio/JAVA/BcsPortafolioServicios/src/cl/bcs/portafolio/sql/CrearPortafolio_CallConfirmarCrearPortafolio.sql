CALL "BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolio"(
    p_IdEmpresa => :IdEmpresa,
    p_IdUsuario => :IdUsuario,
    p_IdModulo => :IdModulo,
    p_UsaReglasNegocio => :UsaReglasNegocio,
    p_IdPortafolio => :IdPortafolio,
    p_IdEstadoAutorizacion => :IdEstadoAutorizacion,
    
    --salida
    p_out_RefId => @p_out_RefId,
    p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle =>  $p_out_AdvertenciasDetalle,
    p_out_ErroresContador =>  @p_out_ErroresContador,
    p_out_ErroresDetalle =>  $p_out_ErroresDetalle
    
);