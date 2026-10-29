SELECT 
EvCapCli."IdEventoCapitalCliente"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdEstado" = 'PEN';