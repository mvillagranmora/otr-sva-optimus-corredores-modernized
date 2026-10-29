SELECT "IdInstrumentoPrestamo" AS "IdInstrumento"
	, "IdTipoPrestamo"
	, "TipoPrestamoSistemaExterno"
	, "CobraPrimaAlInicio"
	, "EsDiaHabilPlazoPrestamo"
	, "IdClasificacionTipoInstrumento"
FROM "BSCL.Custodia.BD::PRT.TipoPrestamo"
WHERE "IdTipoInstrumento" = 'PRT';