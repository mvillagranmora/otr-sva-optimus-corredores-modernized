SELECT
	*
FROM "BSCL.Mesa.BD::SPT.OperacionSpot"
WHERE "FolioOrigenFW" = :FolioCartera
AND "Estado" = 'VIG'
AND "MontoMonedaPrincipal" = :MontoMonedaPrincipal
AND "TipoCambioCierre" = :TipoCambioCierre;