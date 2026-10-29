SELECT PORTAFOLIO."IdPortafolio",CAST(PORTAFOLIO."ValorIndicador" AS BIGINT) AS "IdLibroOrdenes",LIBRO."NombreLibroOrdenes",LIBRO."IdTipoLibroOrden"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" PORTAFOLIO
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesUI" LIBRO ON (LIBRO."IdLibroOrdenes" = PORTAFOLIO."ValorIndicador" AND "CodigoTipoIndicador" = 'ORDLIBRO')
WHERE "IdPortafolio" IN (:IdPortafolio) AND "NombreLibroOrdenes" IN (:NombreLibroOrdenes);