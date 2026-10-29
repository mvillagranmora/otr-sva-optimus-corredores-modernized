DELETE FROM "BSCL.Orden.BD::ORD.CobroOrden"
WHERE "IdOrden"= :IdOrden
AND "IdCobro" = :IdCobro
AND "IdCobroOrden" = :IdCobroOrden
;