SELECT "IdTipoEvento", "IdEstado" 
FROM "BSCL.Plataforma.BD::AUT.TipoEvento" 
WHERE "IdTipoEvento" IN (:IdsTipoEvento) 
AND "IdEmpresa" = :IdEmpresa;