UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 
SET "IdEventoCapitalClienteFormaDePago" = :IdEventoCapitalClienteFormaDePago
WHERE "IdEventoCapitalCliente" = :IdEventoCapitalCliente
AND "IdEventoCapital"=:IdEventoCapital