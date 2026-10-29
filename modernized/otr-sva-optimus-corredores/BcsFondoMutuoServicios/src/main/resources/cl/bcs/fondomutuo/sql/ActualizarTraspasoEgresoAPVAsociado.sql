UPDATE "BSCL.APV.BD::TRA.Traspaso" 
SET "IdEstado" = :IdEstado
WHERE "IdEmpresa" = :IdEmpresa 
AND "IdTraspaso" = :IdTraspaso;