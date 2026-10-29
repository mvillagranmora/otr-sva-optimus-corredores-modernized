SELECT "IdPortafolio", "IdEventoCapitalCliente" 
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 
WHERE "IdPortafolio" IN (:IdsPortafolio);