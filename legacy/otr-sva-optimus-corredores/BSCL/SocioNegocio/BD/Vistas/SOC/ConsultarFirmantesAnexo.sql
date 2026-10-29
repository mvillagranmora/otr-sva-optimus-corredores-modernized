SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarFirmantesAnexo";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarFirmantesAnexo" AS
SELECT PERF."IdPersona" AS "IdPersona"
     , CASE
           WHEN PERF."Tipo" = 'J' THEN PERF."RazonSocial"
           ELSE PERF."Nombres" || ' ' || PERF."ApellidoPaterno" || ' ' || PERF."ApellidoMaterno"
    END                 AS "NombreCompleto"
     , PERO."IdPersona" AS "IdPersonaOrigen"
     , 'S'              AS "FirmaIngresada"
     , FA."IdAnexo"     AS "IdAnexo"
FROM "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA
     LEFT JOIN "BSCL.Plataforma.BD::DOC.SolicitudFirmaAnexo" SFA ON (SFA."IdSolicitudFirmaAnexo" = FA."IdSolicitudFirmaAnexo")
     LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANE ON (ANE."IdAnexo" = FA."IdAnexo")
     LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON (DOC."IdDocumento" = ANE."IdDocumento")
     LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdCliente" = DOC."IdCliente")
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERO ON (PERO."IdPersona" = CLI."IdPersona")
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERF ON (PERF."IdPersona" = SFA."IdPersonaFirmante")
WHERE FA."IdEstado" = 'ACT'
  AND (FA."EsAutorizado" IS NULL OR FA."EsAutorizado" = 'S');

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarFirmantesAnexo" TO BCS_DESARROLLADOR WITH GRANT OPTION;