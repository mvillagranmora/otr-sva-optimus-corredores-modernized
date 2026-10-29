SELECT COUNT(1) AS "Contador" FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "IdEventoCapital" = :IdEventoCapital
AND ("IdEstado" = 'PRO' OR "IdEstado" = 'NOT' OR "IdEstado" = 'REA');