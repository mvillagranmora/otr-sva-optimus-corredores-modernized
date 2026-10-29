UPDATE "BSCL.APV.BD::TRA.DetalleOrigenTraspaso"
SET "IdEstado"='ANU'
WHERE "IdTraspaso" IN (:IdTraspaso);