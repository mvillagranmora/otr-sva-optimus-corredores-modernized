SELECT "IdCorredorCorresponsal","CodigoCorredor" FROM "BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal"
WHERE "CodigoCorredor" IN (:CodigoCorredor) AND "IdPais" = :IdPais;