SELECT 
"IdEventoCapital"
,"FechaPago"
,"IdClasificacionTipoInstrumento"
,"IdTipoInstrumento"
,"IdInstrumento"
,"Nemo"
,"IdMoneda"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "IdEventoCapital" = :IdEventoCapital 
AND "IdTipoEventoCapital" = 'DA'
AND "IdEstado" IN ('PRO', 'NOT')