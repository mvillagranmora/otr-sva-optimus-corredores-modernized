DELETE FROM "BSCL.APV.BD::TRA.DetalleOrigenTraspaso"
WHERE "IdTraspaso" IN (SELECT "IdTraspaso" 
					   FROM "BSCL.APV.BD::TRA.Traspaso" 
					   WHERE "IdContratoSAAPV" = :IdContratoSAAPV);