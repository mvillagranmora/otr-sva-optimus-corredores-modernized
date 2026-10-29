SELECT tipoOpeTipoIns."IdTipoOperacion", tipoIns."IdClasificacionTipoInstrumento", cTipoIns."Nombre" AS "NombreClasificacionTipoInstrumento"
FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" tipoOpeTipoIns
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoIns ON tipoIns."IdTipoInstrumento" = tipoOpeTipoIns."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" cTipoIns ON tipoIns."IdClasificacionTipoInstrumento"=cTipoIns."IdClasificacionTipoInstrumento"
GROUP BY tipoOpeTipoIns."IdTipoOperacion", tipoIns."IdClasificacionTipoInstrumento", cTipoIns."Nombre"