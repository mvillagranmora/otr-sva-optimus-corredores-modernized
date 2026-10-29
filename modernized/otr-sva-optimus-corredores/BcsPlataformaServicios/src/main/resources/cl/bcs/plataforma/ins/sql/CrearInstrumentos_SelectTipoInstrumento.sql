SELECT "IdTipoInstrumento", "Codigo", "Nombre", "Descripcion", "IdEstado", "IdClasificacionTipoInstrumento" 
FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" 
WHERE "IdTipoInstrumento" IN (:IdTipoInstrumento);