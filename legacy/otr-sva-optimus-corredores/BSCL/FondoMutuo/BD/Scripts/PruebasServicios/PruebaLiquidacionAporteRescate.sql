set schema bscl;
DROP table #filtros;

create local temporary table #filtros(
													"IdInterno"			BIGINT,
    												"IdEmisor"			NVARCHAR(40),
    												"TipoSolicitud"		NVARCHAR(3),
    												"IdEstado"			NVARCHAR(3),
    												"FechaEmision"		NVARCHAR(10),
    												"FechaLiquidacion"	NVARCHAR(10),
    												"IdSerieFondo"		NVARCHAR(40)
												);

INSERT INTO #filtros VALUES(0,'','APO','ING','2016-10-25','2016-10-25','');


call "BSCL.FondoMutuo.BD.Servicios::LiquidacionSolicitudAporteRescate" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'S',
	p_UsaReglasNegocio => 'S',
	p_Filtros => #filtros,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);



