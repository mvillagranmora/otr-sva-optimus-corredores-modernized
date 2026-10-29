SELECT * 
FROM (
	SELECT OPE."CodigoOperador",row_number() over (PARTITION BY "CodigoOperador" ORDER BY LIB."Prioridad") AS "Prioridad",OPE."IdLibroOrdenes"
	FROM "BSCL.Orden.BD::ORD.LibroOrdenesOperador" OPE
	INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LIB on OPE."IdLibroOrdenes" = LIB."IdLibroOrdenes" 
	WHERE LIB."IdEstado" = 'ABT' AND OPE."IdEstado" = 'ACT' AND LIB."IdEmpresa" = :idEmpresa
)
WHERE "Prioridad" = 1
