SELECT
	 "IdMonedaPrincipal"
	,"IdInstrumento"
	,"Nemotecnico"
FROM (
	SELECT
		 "IdMonedaPrincipal"
		,COALESCE("IdInstrumento",0) AS "IdInstrumento"
		,COALESCE("Nemotecnico",'') AS "Nemotecnico"
		,ROW_NUMBER() OVER (PARTITION BY "IdMonedaPrincipal" ORDER BY "IdInstrumento" DESC) AS "POS"
	FROM "BSCL.Mesa.BD::MES.InstrumentoMoneda"
	WHERE "IdMonedaPrincipal" IN (:IdMonedaPrincipal)
	  AND "IdEmpresa" = :IdEmpresa
	  AND "IdMonedaSecundaria" = 'CLP'
	  AND "IdTipoInstrumento" = 'SPT'
	  AND "Nemotecnico" = 'INTERB'
) INS
WHERE INS."POS" = 1;
