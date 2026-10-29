SELECT E."IdEtapa"
,E."IdTipoEvento"
,E."Codigo"
,E."Nombre"
,E."Orden"
,E."TipoAutorizacion"
,E."MinimoAutorizan"
,E."IdEstado"
,E."IdEmpresa"
,EA."Descripcion" AS "NombreEstado"
FROM "BSCL.Plataforma.BD::AUT.Etapa" E
LEFT JOIN "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" EA 
	ON E."IdEstado" = EA."IdEstado" AND EA."NombreEntidad" = 'AUT.ETAPA'
WHERE E."IdEmpresa" = :IdEmpresa
AND E."IdEtapa" IN (:IdsEtapas);