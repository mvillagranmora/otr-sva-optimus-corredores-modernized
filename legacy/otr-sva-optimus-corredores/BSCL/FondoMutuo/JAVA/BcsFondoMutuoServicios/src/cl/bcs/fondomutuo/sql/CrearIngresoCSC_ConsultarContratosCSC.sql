SELECT * FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"
WHERE "IdCliente" in (:inIdCliente) AND "IdPortafolio" in (:inIdPortafolio) AND "TipoPago" = 'PAC';