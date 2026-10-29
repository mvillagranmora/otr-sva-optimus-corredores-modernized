set schema bscl;

DROP table #series;

create local temporary table #series(
									"IdSerieFondo"		NVARCHAR(40),
									"IdEmisor"			NVARCHAR(40),
									"BloqueoAporte"		NVARCHAR(1),
									"BloqueoRescate"	NVARCHAR(1),
									"BloqueoIndefinido"	NVARCHAR(1),
									"FechaBloqueoDesde"	NVARCHAR(10),
									"FechaBloqueoHasta"	NVARCHAR(10)
								);

INSERT INTO #series
SELECT 'FMLIQUIDEZ-L' AS "IdSerieFondo", '' AS "IdEmisor", 'S' AS "BloqueoAporte", 'S' AS "BloqueoRescate", '' AS "BloqueoIndefinido", '' AS "FechaBloqueoDesde", '' AS "FechaBloqueoHasta" FROM DUMMY;

CALL "BSCL.FondoMutuo.BD.Servicios::ModificarBloqueoInstrumento"(
	p_IdEmpresa => 1,
	p_IdUsuario => 21,
	p_IdModulo => 'FMM',
	p_UsaReglasNegocio => 'N',
	p_SerieFondo => #series,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);