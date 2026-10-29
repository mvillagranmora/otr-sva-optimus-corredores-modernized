SELECT COBRAN."IdCobroRangoOrden",COBRAN."IdCobroOrden",COBRAN."IdOrden",COBRAN."IdGrupoCobro",COBRAN."IdEmpresa",COBRAN."CodigoDefinicionCobro",COBRAN."Hasta",COBRAN."Valor"
FROM "BSCL.Orden.BD::ORD.CobroRangoOrden" COBRAN
INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" ORD ON ORD."IdCobroOrden" = COBRAN."IdCobroOrden" AND ORD."IdOrden" = COBRAN."IdOrden" AND ORD."IdGrupoCobro" = COBRAN."IdGrupoCobro"
WHERE COBRAN."IdGrupoCobro" in (:idsGrupoCobro);
 