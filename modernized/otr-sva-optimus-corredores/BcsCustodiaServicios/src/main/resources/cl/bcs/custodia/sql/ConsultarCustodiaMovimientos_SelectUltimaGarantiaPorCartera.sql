SELECT "IdGarantia", "IdCarteraOrigen", "IdEstado"
FROM (
    SELECT "IdGarantia", "IdCarteraOrigen", "IdEstado",
           ROW_NUMBER() OVER (PARTITION BY "IdCarteraOrigen" ORDER BY "IdGarantia" DESC) AS "RowNum"
    FROM "BSCL.Custodia.BD::GTA.Garantia"
    WHERE "IdCarteraOrigen" IN (:idsCartera)
) AS GARANTIAS
WHERE "RowNum" = 1;