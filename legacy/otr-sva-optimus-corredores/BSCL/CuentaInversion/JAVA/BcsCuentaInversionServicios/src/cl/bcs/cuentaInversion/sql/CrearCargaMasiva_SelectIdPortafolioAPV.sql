SELECT
 DOC."DocumentoIdentidad"
,POR."IdPortafolio"
,POR."CodigoPortafolio"
,SUBSTRING (CPA."NombrePlanAhorro",4,LENGTH (CPA."NombrePlanAhorro")) AS "Plan"
,PA."Codigo"			AS "ProductoAgrupador"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON POR."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupador" AS CPA ON (:p_Plan IS NULL OR CPA."NombrePlanAhorro" LIKE CONCAT('%', CONCAT(:p_Plan, '%'))) AND POR."IdPortafolio" = CPA."IdPortafolio"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PA ON PA."NombreProductoAgrupador"  = CPA."Producto" AND  (:p_ProductoAgrupador IS NULL OR :p_ProductoAgrupador = PA."Codigo")
WHERE POR."CodigoPortafolio" IN (:p_CodigoPortafolio)
AND DOC."DocumentoIdentidad" IN (:p_DocumentoIdentidad);