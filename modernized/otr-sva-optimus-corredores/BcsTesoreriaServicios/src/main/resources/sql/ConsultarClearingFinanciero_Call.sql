CALL "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinanciero"
(
    -- VARIABLES DE ENTRADA OBLIGATORIAS
    p_IdEmpresa                         => :p_IdEmpresa,
    p_IdUsuario                         => :p_IdUsuario,
    p_IdModulo                          => :p_IdModulo,
    p_UsaReglasNegocio                  => 'N', 
    p_FechaLiquidacion                  => :p_FechaContable,
    p_Camaras                           => #v_Camaras,
    --SALIDAS
    p_out_ClearingFinancieroResumen     => $p_out_ClearingFinancieroResumen,
    p_out_ClearingFinancieroDetalle     => $p_out_ClearingFinancieroDetalle,
    p_out_ClearingFinancieroDividendos =>  $p_out_ClearingFinancieroDividendos,
    p_out_AdvertenciasContador          => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle           => $p_out_AdvertenciasDetalle,  
    p_out_ErroresContador               => @p_out_ErroresContador,
    p_out_ErroresDetalle                => $p_out_ErroresDetalle
);