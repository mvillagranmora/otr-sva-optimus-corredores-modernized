SELECT DISTINCT
 "IdInstrumento"
,"NombreTipoInstrumento"
,"IdClasificacionTipoInstrumento"
,"NombreClasificacionTipoInstrumento"
,"Tamano"
,"IdEmisor"
,"ISIN"
,"FechaVencimiento"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
WHERE "IdInstrumento" IN (:IdInstrumento)