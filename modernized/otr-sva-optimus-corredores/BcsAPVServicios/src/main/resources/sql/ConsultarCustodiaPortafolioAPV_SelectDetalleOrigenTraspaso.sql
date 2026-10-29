SELECT
"IdInstrumento"
,"IdTraspaso"
,"IdPortafolio"
,"CodigoRegimenTributario"
FROM "BSCL.APV.BD::TRA.DetalleOrigenTraspaso"
WHERE "IdInstrumento" IN (:IdInstrumento)
AND "IdTraspaso" = :IdTraspaso;