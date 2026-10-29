SELECT "IdRelacionClasificacion",
"IdArbolClasificacionActivo",
"IdClasificacionTipoInstrumento",
"IdTipoInstrumento",
"IdInstrumento" FROM "BSCL.Plataforma.BD::INS.RelacionClasificacionActivos"
WHERE "IdInstrumento" =:IdInstrumento