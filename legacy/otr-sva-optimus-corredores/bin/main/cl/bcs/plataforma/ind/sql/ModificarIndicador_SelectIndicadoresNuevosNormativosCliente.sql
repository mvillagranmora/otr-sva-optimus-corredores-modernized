SELECT
("BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL) AS "IdIndicador"
,CIC."IdCliente"				AS "IdEntidad"
,REG."IdTipoEntidad" 		AS "IdTipoEntidad"
,CIC."IdTipoIndicadorCliente"		AS "IdTipoIndicador"
,IFNULL(REG."ValorIndicador",'')	AS "ValorIndicador"
,REG."FechaVencimiento"		AS "FechaVencimiento"
FROM "BSCL.Plataforma.BD::GBL.ModificarIndicador" REG
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente" CIC ON CIC."IdPersona" = REG."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON IND."IdEntidad" = CIC."IdCliente" AND IND."IdTipoIndicador" = CIC."IdTipoIndicadorCliente"
WHERE REG."IdIndicador" =0 AND REG."IdTrabajo" = :IdTrabajo AND CIC."IdPersona" IS NOT NULL 
AND CIC."IdTipoIndicadorPersona" = REG."IdTipoIndicador" AND (CIC."CodigoTipoIndicadorCliente" = 'RELCORR' OR CIC."CodigoTipoIndicadorCliente" = 'NCG380') AND IND."IdIndicador" IS NULL;