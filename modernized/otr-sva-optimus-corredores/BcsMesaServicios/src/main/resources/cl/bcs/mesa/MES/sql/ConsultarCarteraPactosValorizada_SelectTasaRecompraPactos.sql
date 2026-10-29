SELECT TOP 1 COALESCE("Valor",'N') AS "UsaTasaRecompraPacto"
	FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
	WHERE "CodigoParametro" = 'TASARECOMPRAVENTACONPACTO'
	AND "IdEmpresa" = :IdEmpresa;