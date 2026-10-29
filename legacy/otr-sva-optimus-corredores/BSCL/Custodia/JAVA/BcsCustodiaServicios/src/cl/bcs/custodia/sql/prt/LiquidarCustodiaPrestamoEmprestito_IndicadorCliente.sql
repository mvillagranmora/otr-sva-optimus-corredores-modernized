SELECT  ATR_CC."IdEntidad" AS "IdCliente",
		ATR_CC."ValorIndicador" AS "ContratoCustodia",
		TPO_CC."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
		ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
	   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
  AND ATR_CC."IdEntidad" in (:IdCliente)