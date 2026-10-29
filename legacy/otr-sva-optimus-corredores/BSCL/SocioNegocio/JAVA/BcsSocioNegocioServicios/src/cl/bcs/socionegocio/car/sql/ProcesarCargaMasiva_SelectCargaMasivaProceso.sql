SELECT
	CD."IdCargaMasiva",
	TCM."CodigoArchivo" AS "CodigoTipoCargaMasiva",
	COALESCE(CD."IdCargaMasiva",0) AS "IdCargaMasiva_Real",	
	COALESCE(TCM."CodigoArchivo",'') AS "CodigoTipoCargaMasiva_Real",
	CD."IdEstado"
FROM "BSCL.SocioNegocio.BD::GBL.ProcesarCargaMasiva_Proceso" PROC
LEFT JOIN "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" CD ON (PROC."IdCargaMasiva"=CD."IdCargaMasiva")
LEFT JOIN "BSCL.SocioNegocio.BD::CAR.CargaMasiva" AS CM ON (CM."IdCargaMasiva" = PROC."IdCargaMasiva")
LEFT JOIN "BSCL.SocioNegocio.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."IdTipoCargaMasiva" = CM."IdTipoCargaMasiva")
WHERE CD."IdCargaMasiva" IN (:IdCargaMasiva);