SELECT
"SerieDeFondo"."IdSerieFondo"
,"SerieDeFondo"."IdInstrumento"
,"Fondo"."IdMoneda"
,"SerieDeFondo"."DecimalesValorCuota"
,"Fondo"."IdFondo"
,"Fondo"."IdEmpresa"
,"SerieDeFondo"."IdFamiliaFondo"
,"Fondo"."IdEmisor"
,"Fondo"."FondoExtranjero"
,CLINS."IdClasificacionTipoInstrumento"
,CLINS."Nombre" AS "NombreClasificacionTipoInstrumento"
,"Instrumento"."IdTipoInstrumento"
,TINS."Nombre" AS "NombreTipoInstrumento"
,"Fondo"."NombreFondo"
,"SerieDeFondo"."CodigoSerie"
,"DctoIdentidad"."DocumentoIdentidad" AS "DocumentoIdentidadAdministradora"
,"SerieDeFondo"."RestringeRescate"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" AND "Fondo"."IdEmpresa" = "SerieDeFondo"."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "SerieDeFondo"."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLINS ON CLINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
--obtener informacion del emisor
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS "Emisor" ON "Emisor"."IdEmisor" = "Fondo"."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona" ON "Persona"."IdPersona" = "Emisor"."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DctoIdentidad" ON "DctoIdentidad"."IdDocumentoIdentidad" = "Persona"."IdDocumentoIdentidad" AND "DctoIdentidad"."Predeterminado" ='S'
WHERE "SerieDeFondo"."IdInstrumento" IN (:IdInstrumento)


