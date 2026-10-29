SELECT "IdEventoCapitalCliente" as "IdEventoCapitalCliente" 
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
WHERE "IdEventoCapitalCliente" in (:ListaIdEventoCapitalCliente)
  AND "IdTipoEventoCapital" = 'SU'
  AND "IdEstado" = :IdEstado