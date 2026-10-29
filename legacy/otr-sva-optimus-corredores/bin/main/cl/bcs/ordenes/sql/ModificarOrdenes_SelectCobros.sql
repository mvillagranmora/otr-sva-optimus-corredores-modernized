SELECT COB."IdCobroOrden", COB."IdGrupoCobro", COB."IdOrden",COB."ValorCobroAplicado",COB."IdCobro"
FROM "BSCL.Orden.BD::ORD.CobroOrden" COB
INNER JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON ORD."IdGrupoCobro" = COB."IdGrupoCobro"
WHERE COB."IdOrden" in (:idsOrden);

