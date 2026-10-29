set schema bscl;
DROP table #series;

create local temporary table #series(
	"IdSerieFondo"	NVARCHAR(40)
);

INSERT INTO #series
SELECT 'FMLIQUIDEZ-L' FROM DUMMY;

CALL "BSCL.FondoMutuo.BD.Servicios::CrearRegistroBloqueoDesbloqueoFondoAgf"(
	p_IdEmpresa => 1,
	p_IdUsuario => 21,
	p_IdModulo => 'FMM',
	p_EsManual => 'N',
	p_SerieFondo => #series,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);