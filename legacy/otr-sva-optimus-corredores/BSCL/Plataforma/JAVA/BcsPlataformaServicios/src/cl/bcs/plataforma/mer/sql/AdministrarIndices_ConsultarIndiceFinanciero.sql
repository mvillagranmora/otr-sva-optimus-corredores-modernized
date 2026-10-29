SELECT "IdIndiceFinanciero",
		"Periodo",
		"Nombre",
		"Descripcion",
		"IdMonedaParametro",
		"IdMonedaValor"
FROM "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero"
WHERE "IdIndiceFinanciero" IN (:IdIndiceFinanciero)