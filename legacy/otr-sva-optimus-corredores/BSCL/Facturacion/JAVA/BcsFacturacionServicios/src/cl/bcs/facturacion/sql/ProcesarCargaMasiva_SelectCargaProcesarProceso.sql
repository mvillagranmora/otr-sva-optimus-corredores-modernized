SELECT 
	PROC."IdCargaMasiva",
	PROC."IdTipoCargaMasiva",
	PROC."CodigoArchivo",
	PROC."IdEmpleadoDigitador",
	PROC."IdBolsa",
	PROC."IdBoveda",
	COALESCE(TCM."IdTipoCargaMasiva",0) AS "IdTipoCargaMasiva_Real",
	CM."IdEstado" AS "IdEstado"
FROM "BSCL.Facturacion.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC
LEFT JOIN "BSCL.Facturacion.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."IdTipoCargaMasiva" = PROC."IdTipoCargaMasiva")
LEFT JOIN "BSCL.Facturacion.BD::CAR.CargaMasiva" AS CM ON (CM."IdCargaMasiva" = PROC."IdCargaMasiva")
WHERE PROC."IdCargaMasiva" IN (:IdCargaMasiva);