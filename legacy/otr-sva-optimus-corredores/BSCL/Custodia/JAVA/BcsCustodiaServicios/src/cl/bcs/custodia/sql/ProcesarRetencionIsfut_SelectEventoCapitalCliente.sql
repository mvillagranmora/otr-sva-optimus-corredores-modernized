SELECT 
"IdEventoCapitalCliente",
"IdEventoCapital",
"ISFUT" AS "IsfutEventoCapitalCliente",
"RetencionISFUT",
"MontoRetencionISFUT",
"Total",
"Nemo"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 
WHERE "ISFUT" = 'S' AND "IdEventoCapital"= :IdEventoCapital