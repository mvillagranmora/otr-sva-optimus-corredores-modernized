SET SCHEMA "BSCL";

DROP table #valores;

CREATE local temporary table #valores(
	"IDSERIEFONDO"			NVARCHAR(40),
	"FECHACIERRE"			NVARCHAR(10),
	"VALORDOLARAPORTE"		DECIMAL(26,8),
	"VALORDOLARRESCATE"		DECIMAL(26,8),
	"OBSERVACION"			NVARCHAR(100),
	"MENSAJEVALIDACION" 	NVARCHAR(3)
);

INSERT INTO #valores
SELECT 'FMLIQUIDEZ_A','2016-12-31',568,568,'','OK' FROM DUMMY;
INSERT INTO #valores
SELECT 'FMLIQUIDEZ_B','2016-12-31',568,568,'','OK' FROM DUMMY;
INSERT INTO #valores
SELECT 'CFICCLAT-B','2016-12-31',568,568,'','OK' FROM DUMMY;
INSERT INTO #valores
SELECT 'CFICCLA-A','2016-12-31',568,568,'','OK' FROM DUMMY;
INSERT INTO #valores
SELECT 'FMLIQUIDEZ_D','2016-12-31',568,568,'','OK' FROM DUMMY;
INSERT INTO #valores
SELECT 'FMLIQUIDEZ_C','2016-12-31',568,568,'','OK' FROM DUMMY;

call "BSCL.FondoMutuo.BD.Servicios::CargarTipoCambio" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'N',
	p_TipoCambio => #valores,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
