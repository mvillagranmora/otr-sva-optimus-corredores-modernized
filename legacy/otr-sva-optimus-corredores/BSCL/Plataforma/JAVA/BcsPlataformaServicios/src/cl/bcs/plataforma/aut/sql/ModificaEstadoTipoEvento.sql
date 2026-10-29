UPDATE "BSCL.Plataforma.BD::AUT.TipoEvento"
SET "IdEstado"= :IdEstado
WHERE "IdTipoEvento"= :IdTipoEvento AND "IdEmpresa"= :IdEmpresa;
