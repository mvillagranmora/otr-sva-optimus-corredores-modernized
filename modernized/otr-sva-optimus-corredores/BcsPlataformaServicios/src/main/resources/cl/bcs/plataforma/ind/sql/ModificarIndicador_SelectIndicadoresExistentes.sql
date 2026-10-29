SELECT
REG."IdIndicador"			AS "IdIndicador"
,REG."IdEntidad"			AS "IdEntidad"
,REG."IdTipoEntidad" 		AS "IdTipoEntidad"
,REG."IdTipoIndicador"		AS "IdTipoIndicador"
,COALESCE(REG."ValorIndicador",'') AS "ValorIndicador"
,TAT."RequiereAutorizacion"	AS "RequiereAutorizacion"
,REG."FechaVencimiento" 	AS "FechaVencimiento"
,COALESCE(IND."ValorIndicador",'') AS "ValorAntiguo"
,COALESCE(IND."FechaVencimiento",TO_TIMESTAMP('0001-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS')) AS "FechaVencimientoAntiguo"
FROM "BSCL.Plataforma.BD::GBL.ModificarIndicador"  REG
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TAT ON (TAT."IdTipoIndicador" = REG."IdTipoIndicador" AND TAT."IdTipoEntidad" = REG."IdTipoEntidad")
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON (IND."IdIndicador" = REG."IdIndicador")
WHERE REG."IdIndicador" >0 AND TAT."IdTipoIndicador" IS NOT NULL AND REG."IdTrabajo" = :IdTrabajo;