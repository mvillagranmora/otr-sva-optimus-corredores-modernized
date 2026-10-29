SELECT 
    "Instrumento"."IdInstrumento",
    "Instrumento"."Nemotecnico",
    "Instrumento"."ISIN",
    "Instrumento"."CorteMinimo",
    "Instrumento"."EstaInscrito",
    "Instrumento"."EsDivisible",
    "Instrumento"."RazonSocial",
    --"Instrumento"."Lista",
    "Instrumento"."FechaEmision",
    "Instrumento"."FechaVencimiento",
    "Instrumento"."IdInstrumentoRelacionado",
    "Instrumento"."IdEmisor",
    "Instrumento"."IdTipoReajuste",
    "Instrumento"."Reajustable",
    "Instrumento"."IdMoneda",
    "Instrumento"."IdEstado",
    "Instrumento"."DecimalesPrecio",
    "TipoInstrumento"."IdTipoInstrumento" AS "IdTipoInstrumento",
    "TipoInstrumento"."Codigo" AS "CodigoTipoInstrumento",
    "TipoInstrumento"."Nombre" AS "NombreTipoInstrumento",
    "ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento",
    "ClasificacionTipoInstrumento"."Codigo"  AS "CodigoClasificacionTipoInstrumento",
    "ClasificacionTipoInstrumento"."Nombre" AS "NombreClasificacionTipoInstrumento",
    "ConsultarEmisor"."DocumentoIdentidad" AS "DocumentoIdentidadEmisor", --RutEmisor
    "ConsultarEmisor"."NombreCompleto" as "NombreCompletoEmisor", --RazonSocialEmisor
    "TipoOperacionTipoInstrumento"."IdTipoOperacion",
    "TipoOperacion"."Accion",
    "TipoOperacion"."Nombre" as "NombreTipoOperacion",
    "Instrumento"."Tamano",
    cla."IdEntidadClasificadoraRiesgo",
    cla."ValorClasificacionRiesgo",
    "TipoClasificacionRiesgo"."IdTipoClasificacionRiesgo",
    "Instrumento"."TipoValorizacion",
    "Instrumento"."Tasa",
    "Instrumento"."Precio",
    "Instrumento"."ValorizaPor",
    "Instrumento"."TieneCupon",
    "Instrumento"."NumeroCupones",
    "Instrumento"."IndicadorBolson"
FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" cla ON ("Instrumento"."IdInstrumento" = cla."IdInstrumento")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "ConsultarEmisor" ON "ConsultarEmisor"."IdEmisor" = "Instrumento"."IdEmisor"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" ON "TipoInstrumento"."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento" ON "ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento" = "TipoInstrumento"."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS "TipoOperacionTipoInstrumento" ON "Instrumento"."IdInstrumento" = "TipoOperacionTipoInstrumento"."IdInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON "TipoOperacionTipoInstrumento"."IdTipoOperacion" = "TipoOperacion"."IdTipoOperacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"  AS "TipoClasificacionRiesgo" ON "TipoClasificacionRiesgo"."IdClasificacionTipoInstrumento" = "ClasificacionTipoInstrumento"."Codigo"
WHERE "Instrumento"."IdInstrumento" IN (:IdInstrumento)
