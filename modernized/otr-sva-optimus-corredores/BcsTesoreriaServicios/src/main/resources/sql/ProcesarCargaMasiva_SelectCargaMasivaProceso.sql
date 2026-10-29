SELECT
	PROC."IdCargaMasiva",
	PROC."CodigoTipoCargaMasiva",
	PROC."IdMoneda",
	PROC."IdConcepto",
	PROC."IdMedioDePago",
	PROC."IdCuentaBanco",
	COALESCE(CM."IdCargaMasiva",0) AS "IdCargaMasiva_Real",	
	COALESCE(TCM."CodigoTipoCargaMasiva",'') AS "CodigoTipoCargaMasiva_Real",
	CM."IdEstado"
	
FROM "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC
LEFT JOIN "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = PROC."CodigoTipoCargaMasiva")
LEFT JOIN "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM ON (CM."IdCargaMasiva" = PROC."IdCargaMasiva")		
WHERE PROC."IdCargaMasiva" IN (:IdCargaMasiva);