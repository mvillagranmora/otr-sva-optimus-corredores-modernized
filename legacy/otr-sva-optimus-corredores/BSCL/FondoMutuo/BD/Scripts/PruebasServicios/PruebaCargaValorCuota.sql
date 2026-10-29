SET SCHEMA "BSCL";

DROP table #valores;

CREATE local temporary table #valores(
	"IDSERIEFONDO"			NVARCHAR(40),
	"IDMONEDA"				NVARCHAR(3),
	"FECHACIERRE"			NVARCHAR(10),
	"VALORCUOTA"			DECIMAL(26,8),
	"OBSERVACION"			NVARCHAR(100),
	"MENSAJEVALIDACION" 	NVARCHAR(3)
);

INSERT INTO #valores
SELECT 'FM_CUSTODIA_A','CLP','2017-12-31',5.2556,'','OK' FROM DUMMY;

call "BSCL.FondoMutuo.BD.Servicios::CargarValorCuota" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'N',
	p_SimularOperacion => 'N',
	p_ValorCuota => #valores,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
