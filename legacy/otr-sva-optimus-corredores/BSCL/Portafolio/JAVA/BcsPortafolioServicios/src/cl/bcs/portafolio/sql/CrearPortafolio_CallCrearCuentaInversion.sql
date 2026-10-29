CALL "BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion" (  
     p_IdEmpresa                => :p_IdEmpresa
    ,p_IdUsuario                => :p_IdUsuario
    ,p_IdModulo                 => :p_IdModulo
    ,p_UsaReglasNegocio         => 'S'
    ,p_CuentaInversion          => "BSCL.Portafolio.BD::GBL.CrearPortafolio_AgregarCuentaInversion"   
    
    ,p_out_ResultadoProceso     => $p_out_ResultadoProceso
    
    ,p_out_InformacionContador  => @p_out_InformacionContador
    ,p_out_InformacionDetalle   => $p_out_InformacionDetalle
    ,p_out_AdvertenciasContador => @p_out_AdvertenciasContador
    ,p_out_AdvertenciasDetalle  => $p_out_AdvertenciasDetalle
    ,p_out_ErroresContador      => @p_out_ErroresContador
    ,p_out_ErroresDetalle       => $p_out_ErroresDetalle
);