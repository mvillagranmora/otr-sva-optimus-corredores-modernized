
UPDATE "BSCL.APV.BD::SCS.ContratoSAAPV"
SET "IdEstado"='VIG'
WHERE "IdContratoSAAPV" IN (
SELECT Contrato."IdContratoSAAPV"
FROM "BSCL.APV.BD::TRA.Traspaso" AS Traspaso 
  				  INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
		 	    		  ON Contrato."IdContratoSAAPV" = Traspaso."IdContratoSAAPV"
		 	   			 AND Contrato."IdCliente" = Traspaso."IdCliente"
		 	   			 AND Contrato."CodigoTipoSolicitud" = 'TRA'
		 	   			 AND Traspaso."IdTraspaso" IN (:IdTraspaso));