SELECT 
	EC."IdEventoCapital",
	EC."Secuencia", 
	EC."IdTipoEventoCapital",
	EC."Nemo",
	TPC."Nombre" AS "NombreEventoCapital",
	EC."IdEstado"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" EC
LEFT JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TPC ON TPC."IdTipoEventoCapital" = EC."IdTipoEventoCapital"
WHERE "IdEmpresa" = :p_IdEmpresa AND "IdEventoCapital" IN (:p_IdEventoCapital);