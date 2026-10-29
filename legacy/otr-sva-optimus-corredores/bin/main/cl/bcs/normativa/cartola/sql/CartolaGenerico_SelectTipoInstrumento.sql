SELECT 
"IdTipoInstrumento"
,"Nombre"
FROM "BSCL.Plataforma.BD::INS.TipoInstrumento"
WHERE "IdTipoInstrumento" IN (:IdTipoInstrumento)