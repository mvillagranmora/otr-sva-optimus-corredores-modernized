SELECT CSC."FolioCSC", POR."IdPortafolio", POR."Proposito" 
FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" CSC
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (POR."IdPortafolio" = CSC."IdPortafolio")
WHERE CSC."FolioCSC" IN (:FolioCSC)
