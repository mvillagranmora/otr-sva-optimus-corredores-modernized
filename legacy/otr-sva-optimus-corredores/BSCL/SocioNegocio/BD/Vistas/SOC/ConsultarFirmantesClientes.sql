SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarFirmantesClientes";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarFirmantesClientes" AS

SELECT REL."IdPersonaDestino" AS "IdPersona"
     , CASE
           WHEN PER."Tipo" = 'J' THEN PER."RazonSocial"
           ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
    END                       AS "NombreCompleto"
     , REL."IdPersonaOrigen"  AS "IdPersonaOrigen"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = REL."IdPersonaDestino")
     LEFT JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" TREL ON (TREL."IdTipoRelacion" = REL."IdTipoRelacion")
WHERE TREL."UsaFirmaDocumento" = 'S'
  AND REL."IdEstado" = 'ACT'
  AND (TREL."EsIndefinida" = 'S' OR REL."FechaFinRelacion" > CURRENT_UTCTIMESTAMP)
UNION ALL
SELECT PER."IdPersona" AS "IdPersona"
     , CASE
           WHEN PER."Tipo" = 'J' THEN PER."RazonSocial"
           ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
    END                AS "NombreCompleto"
     , PER."IdPersona" AS "IdPersonaOrigen"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarFirmantesClientes" TO BCS_DESARROLLADOR WITH GRANT OPTION;