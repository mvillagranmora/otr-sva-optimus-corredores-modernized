SELECT 
	COALESCE((SELECT "ValorParametro" FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "NombreTipoParametro" = 'CARGOSABONOS' AND "IdEmpresa" = :IdEmpresa),'N') AS "CargosAbonos"
	,COALESCE((SELECT "ValorParametro" FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "NombreTipoParametro" = 'INGRESOSRETIROS' AND "IdEmpresa" = :IdEmpresa),'N') AS "IngresosRetiros"
	,COALESCE((SELECT "ValorParametro" FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "NombreTipoParametro" = 'NOTASCREDITO' AND "IdEmpresa" = :IdEmpresa),'N') AS "NotasCredito"
FROM DUMMY;