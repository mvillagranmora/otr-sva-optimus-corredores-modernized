CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
    p_IdEmpresa => :IdEmpresa,
    p_IdUsuario => :IdUsuario,
    p_IdModulo => :IdModulo,
    p_UsaReglasNegocio => :UsaReglasNegocio,
    p_Portafolio => :IdPortafolioGrupoAPV, 
    p_IdEntidad => :IdPortafolioGrupoAPV, --IdEntidad
    p_TipoEntidad => :TipoEntidadReplica, 
    p_TipoOperacion => :TipoOperacion,
    
    p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle =>  $p_out_AdvertenciasDetalle,
    p_out_ErroresContador =>  @p_out_ErroresContador,
    p_out_ErroresDetalle =>  $p_out_ErroresDetalle
);