UPDATE EVT

SET EVT."IdEstado" = (CASE WHEN EVT."IdTipoEventoCapital" = 'CC' THEN 'ANU' ELSE 'VIG' END)
FROM "BSCL.Custodia.BD::CUS.EventoCapital" EVT
WHERE "IdEventoCapital" IN (:p_IdEventoCapital);
