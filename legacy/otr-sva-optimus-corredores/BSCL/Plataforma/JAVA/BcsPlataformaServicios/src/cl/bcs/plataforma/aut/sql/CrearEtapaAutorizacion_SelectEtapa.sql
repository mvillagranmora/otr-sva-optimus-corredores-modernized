SELECT "IdEtapa"
,"IdTipoEvento"
,"Codigo"
,"Nombre"
,"Orden"
,"TipoAutorizacion"
,"MinimoAutorizan"
,"IdEstado"
,"IdEmpresa"
FROM "BSCL.Plataforma.BD::AUT.Etapa"
WHERE "IdEmpresa" = :IdEmpresa
AND "IdTipoEvento" IN (:IdsTipoEvento);