set schema bscl;
DROP table #series;
create local temporary table #series(
												"IdInterno" 		BIGINT,
												"IdSerieFondo"		NVARCHAR(40),
												"IdEmisor"			NVARCHAR(40),
												"Perfil"			NVARCHAR(40),
												"EstadoAporte"		NVARCHAR(1),
												"EstadoRescate"		NVARCHAR(1),
												"EstadoReinversion"	NVARCHAR(1),
												"EstadoLiquidez"	NVARCHAR(1),
												"BloqueoAporte"		NVARCHAR(1),
												"BloqueoRescate"	NVARCHAR(1),
												"BloqueoIndefinido"	NVARCHAR(1),
												"FechaBloqueoDesde"	NVARCHAR(10),
												"FechaBloqueoHasta"	NVARCHAR(10)	
);

DELETE FROM  #series;
INSERT INTO #series VALUES(0,'','','PERFIL_COMERCIAL','A','','','','','','','','');


select * from #series;



call "BSCL.FondoMutuo.BD.Servicios::ModificarAperturaCierreBloqueoInstrumento" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'S',
	p_SerieFondo => #series,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
