SELECT top 1  "IdCliente"||'_'||"IdPortafolio" as "IdCompuesto", "Estado", *
FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" 
WHERE "IdCliente" in (:IdsCliente) AND "IdPortafolio" in (:IdsPortafolio) 
AND "TipoPago" = 'PAC' AND "Estado" = 'ING' ORDER BY "IdFolioCSC" DESC
