SELECT COUNT(1) AS "Contador" FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "IdEventoCapital" = :IdEventoCapital
and "IdEstado" != 'PRO';