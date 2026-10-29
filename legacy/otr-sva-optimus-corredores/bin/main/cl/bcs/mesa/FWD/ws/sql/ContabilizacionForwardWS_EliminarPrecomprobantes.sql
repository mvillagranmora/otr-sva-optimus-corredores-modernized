CALL "BSCL.Contabilidad.BD.Servicios::ITZ.EliminarPreComprobante"(
    p_IdEmpresa => :IdEmpresa,
    p_IdUsuario => :IdUsuario,
    p_IdModulo => 'CON',
    p_UsaReglasNegocio => 'S',
	p_IdPreComprobante => :IdPreComprobante,
	p_IdPreComprobanteAsiento => 0,
	p_IdEvento => 0,
	p_Origen => 'PR',
	p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
	p_out_ErroresContador => @p_out_ErroresContador,
	p_out_ErroresDetalle => $p_out_ErroresDetalle
);