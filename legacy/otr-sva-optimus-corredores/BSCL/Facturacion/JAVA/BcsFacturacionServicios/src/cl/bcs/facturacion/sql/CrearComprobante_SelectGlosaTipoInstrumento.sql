SELECT
GTI."IdGlosa",
GTI."IdTipoInstrumento",
GTI."Nombre",
GTI."Prioridad",
DefGlos."Descripcion"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoGlosaTipoInstrumento" GTI
Inner join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDefinicionGlosaTipoInstrumento" DefGlos ON DefGlos."IdGlosaTipoInstrumento" = GTI."IdGlosa"
