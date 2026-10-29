set schema bscl;
DROP table #rescates;
create local temporary table #rescates("IdSolicitudAporteRescate" BIGINT);

DELETE FROM  #rescates;
INSERT INTO #rescates VALUES(13622);
select * from #rescates;



call "BSCL.FondoMutuo.BD.Servicios::AnulacionSolicitudAporteRescate" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_IdEstado   => 'ING',
	p_solicitudes => #rescates,
	p_out_InformacionContador => ?,
	p_out_InformacionDetalle => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
