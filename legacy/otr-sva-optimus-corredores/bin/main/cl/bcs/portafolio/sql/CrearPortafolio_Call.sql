CALL "BSCL.Portafolio.BD.Servicios::CrearPortafolio" (
    p_IdEmpresa => :IdEmpresa,
    p_IdUsuario => :IdUsuario,
    p_IdModulo => :IdModulo,
    p_UsaReglasNegocio => 'N',
    p_IdCliente => :IdCliente,
    p_IdPerfilComercialBase => :IdPerfilComercial,
    p_IdPersonaBrokerExtranjero => :IdPersonaBrokerExtranjero,
    p_PropositoPortafolio => :p_PropositoPortafolio,
    p_IdCuentaOrigen => :p_IdCuentaOrigen,
    p_DescripcionPortafolio => :p_DescripcionPortafolio,
    p_CodigoPortafolio => :p_CodigoPortafolio,
    p_Estado => 'ACT',
    p_AreaCliente => :p_AreaCliente,
    p_DesdeEnrolamiento => :p_DesdeEnrolamiento,
    --Salida
    p_out_RefId => @p_out_RefId,
    
    p_out_AdvertenciasContador  => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle   => $p_out_AdvertenciasDetalle,
    p_out_ErroresContador       => @p_out_ErroresContador,
    p_out_ErroresDetalle        => $p_out_ErroresDetalle
);