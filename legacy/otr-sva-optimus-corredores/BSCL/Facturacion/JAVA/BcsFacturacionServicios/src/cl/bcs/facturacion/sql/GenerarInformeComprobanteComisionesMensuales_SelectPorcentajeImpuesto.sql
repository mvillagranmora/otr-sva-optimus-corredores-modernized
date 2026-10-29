SELECT "IdCobro"
		,"ValorCobro" 
FROM "BSCL.Plataforma.BD::COM.Cobro"
WHERE "IdCobro" = :IdCobro AND "IdEmpresa" = :IdEmpresa;