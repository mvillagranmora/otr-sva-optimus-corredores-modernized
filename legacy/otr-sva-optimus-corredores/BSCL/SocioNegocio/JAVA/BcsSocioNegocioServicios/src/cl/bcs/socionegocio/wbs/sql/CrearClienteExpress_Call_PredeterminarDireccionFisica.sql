CALL "BSCL.SocioNegocio.BD.Servicios::PER.PredeterminarPersonaDireccionFisica" (

    p_IdEmpresa  => :IdEmpresa,

    p_IdUsuario  => :IdUsuario,

    p_IdModulo      => :IdModulo,

    p_UsaReglasNegocio  => :UsaReglasNegocio,
    
    p_IdDireccionFisica => :IdDireccionFisica,

    
    p_out_RefId     =>$p_out_RefId,
    
    p_out_AdvertenciasContador => $p_out_AdvertenciasContador,
    
    p_out_AdvertenciasDetalle    => $p_out_AdvertenciasDetalle,
    
    p_out_ErroresContador     => $p_out_ErroresContador,
    
    p_out_ErroresDetalle      => $p_out_ErroresDetalle 

)