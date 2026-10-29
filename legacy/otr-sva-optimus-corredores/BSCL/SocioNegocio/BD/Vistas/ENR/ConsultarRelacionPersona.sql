-- noinspection SqlNoDataSourceInspectionForFile

SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarRelacionPersona";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarRelacionPersona" AS
SELECT "RP"."IdRelacionPersona"
     , "TREL"."IdTipoRelacion"                                                        AS "IdTipoRelacion"
     , "TREL"."NombreTipoRelacion"                                                    AS "NombreTipoRelacion"
     , "TREL"."EsIndefinida"                                                          AS "EsIndefinida"
     , "RP"."IdEnrolamientoOrigen"                                                    AS "IdEnrolamientoOrigen"
     , "ENR_OR"."RazonSocial"                                                         AS "RazonSocialOrigen"
     , "RP"."IdPersonaDestino"                                                        AS "IdPersonaDestino"
     , "TPER_DE"."Descripcion"                                                        AS "TipoPersonaDestino"
     , "PER_DE"."RazonSocial"                                                         AS "RazonSocialDestino"
     , "PER_DE"."NombreCompleto"                                                      AS "NombreCompletoDestino"
     , "DOC_DE"."IdTipoDocumentoIdentidad"                                            AS "IdTipoDocumentoIdentidadDestino"
     , "DOC_DE"."DocumentoIdentidad"                                                  AS "DocumentoIdentidadDestino"
     , "TDOC_DE"."NombreTipoDocumentoIdentidad"                                       AS "NombreTipoDocumentoIdentidadDestino"
     , "RP"."FechaInicioRelacion"                                                     AS "FechaInicioRelacion"
     , "RP"."FechaFinRelacion"                                                        AS "FechaFinRelacion"
     , "TREL"."UsaPorcentajeParticipacion"                                            AS "UsaPorcentajeParticipacion"
     , "RP"."PorcentajeParticipacion"                                                 AS "PorcentajeParticipacion"
     , "RP"."RetiraDocumentos"                                                        AS "RetiraDocumentos"
     , CASE "RP"."RetiraDocumentos" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '' END AS "TextoRetiraDocumentos"
     , "RP"."FinalidadRelacion"                                                       AS "FinalidadRelacion"
     , "RP"."CondicionesRelacion"                                                     AS "CondicionesRelacion"
     , "RP"."IdGradoVinculacionParental"                                              AS "IdGradoVinculacionParental"
     , "GVP"."Nombre"                                                                 AS "NombreGradoVinculacionParental"
     , "RP"."IdCiudad"                                                                AS "IdCiudad"
     , "RP"."Calle"                                                                   AS "Calle"
     , "RP"."Numero"                                                                  AS "Numero"
     , "RP"."Email"                                                                   AS "Email"
     , "RP"."Telefono"                                                                AS "Telefono"
     , "RP"."Complemento"                                                             AS "Complemento"
     , "RP"."Ciudad"                                                                  AS "Ciudad"
     , COALESCE("RP"."Calle", '') || COALESCE(CAST("RP"."Numero" AS VARCHAR), '') || ' - ' ||
       COALESCE("CIU"."NombreCiudad", '')                                                    AS "NombreLargo"       
FROM "BSCL.SocioNegocio.BD::ENR.RelacionPersona" AS "RP"
         LEFT OUTER JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS "TREL"
                         ON "TREL"."IdTipoRelacion" = "RP"."IdTipoRelacion"
         LEFT OUTER JOIN "BSCL.SocioNegocio.BD::ENR.Enrolamiento" AS "ENR_OR"
                         ON "ENR_OR"."IdEnrolamiento" = "RP"."IdEnrolamientoOrigen"
         LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER_DE"
                         ON "PER_DE"."IdPersona" = "RP"."IdPersonaDestino"
         LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS "TPER_DE"
                         ON "TPER_DE"."IdTipoPersona" = "PER_DE"."Tipo"
         LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC_DE"
                         ON "DOC_DE"."IdDocumentoIdentidad" = "PER_DE"."IdDocumentoIdentidad"
         LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS "TDOC_DE"
                         ON "TDOC_DE"."IdTipoDocumentoIdentidad" = "DOC_DE"."IdTipoDocumentoIdentidad"
         LEFT OUTER JOIN "BSCL.SocioNegocio.BD::REL.GradoVinculacionParental" AS "GVP"
                         ON "GVP"."IdGradoVinculacionParental" = "RP"."IdGradoVinculacionParental"
         LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS "CIU"
         				 ON "CIU"."IdCiudad" = "RP"."IdCiudad" 
WITH READ ONLY;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarRelacionPersona" TO BCS_DESARROLLADOR WITH GRANT OPTION;
