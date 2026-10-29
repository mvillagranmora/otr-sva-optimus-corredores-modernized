CALL "BSCL.Orden.BD.Servicios::RetirarOfertasOrdenes" (
    p_IdEmpresa => :p_IdEmpresa,
    p_IdUsuario => :p_IdUsuario,
    p_IdModulo => :p_IdModulo,
    
    p_Ordenes => #entrada_anular_ordenes_retiro_solicitud,
    
    --Salida
    p_out_InformacionContador   => @p_out_InformacionContador,
    p_out_InformacionDetalle    => $p_out_InformacionDetalle,
    p_out_AdvertenciasContador  => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle   => $p_out_AdvertenciasDetalle,
    p_out_ErroresContador       => @p_out_ErroresContador,
    p_out_ErroresDetalle        => $p_out_ErroresDetalle
);
