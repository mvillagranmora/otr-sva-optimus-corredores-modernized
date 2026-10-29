UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot"
SET "IdComprobanteFacturacion" = :IdComprobanteFacturacion
WHERE "IdOperacionSpot" IN (:IdOperacionSpot);