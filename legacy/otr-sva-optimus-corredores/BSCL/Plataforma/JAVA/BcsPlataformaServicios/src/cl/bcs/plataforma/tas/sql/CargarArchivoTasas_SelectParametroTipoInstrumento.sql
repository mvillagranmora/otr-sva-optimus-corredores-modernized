SELECT COUNT(1)
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento"
WHERE "IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '';