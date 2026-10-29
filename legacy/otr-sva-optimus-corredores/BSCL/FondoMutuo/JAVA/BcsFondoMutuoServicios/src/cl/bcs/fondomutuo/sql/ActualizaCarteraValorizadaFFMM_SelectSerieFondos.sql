SELECT 
SERIE."IdFondo"
,SERIE."IdInstrumento"
,SERIE."IdSerieFondo"
,CLASIFICACION."IdClasificacionTipoInstrumento"
,TIPO."IdTipoInstrumento"
,FONDO."IdMoneda"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE 
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = SERIE."IdInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPO ON TIPO."IdTipoInstrumento" = INS."IdTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLASIFICACION ON CLASIFICACION."IdClasificacionTipoInstrumento" = TIPO."IdClasificacionTipoInstrumento"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON FONDO."IdFondo" = SERIE."IdFondo" 
WHERE SERIE."IdSerieFondo" IN (:ListaIdSerieFondo)
