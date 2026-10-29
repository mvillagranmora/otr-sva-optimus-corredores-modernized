CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
    p_IdEmpresa => :IdEmpresa,
    p_IdUsuario => :IdUsuario,
    p_IdModulo => :IdModulo,
    p_UsaReglasNegocio => 'N',                              
    p_IdTipoEvento => :IdConcepto,--'NUEDOC',
    p_IdModuloSolicita => :IdModulo,
    p_IdUsuarioSolicita => :IdUsuario,
    p_Fecha => TO_VARCHAR (:FechaIngreso, 'YYYY-MM-DD'), 
    p_IdConcepto => :IdConcepto,
    p_NumeroOperacionConcepto =>  CAST (:IdPortafolio AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
    p_FechaOperacionConcepto => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
    p_IdCliente => :IdCliente, --v_IdCliente,
    p_Glosa => :Glosa,
    
    --salida
    p_out_IdEvento => @p_out_IdEvento,
    
    p_out_AdvertenciasContador 	=> 	@p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle 	=>  $p_out_AdvertenciasDetalle,
    p_out_ErroresContador 		=>  @p_out_ErroresContador,
    p_out_ErroresDetalle 		=>  $p_out_ErroresDetalle
);