SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarPerfilFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarPerfilFondoMutuo"
AS 
SELECT
 "ConfiguracionFondoMutuo"."Key" AS "NombrePerfilConfiguracion"
, "Perfil"."IdPerfil"
, "Perfil"."Nombre"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "ConfiguracionFondoMutuo"
JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles" AS "Perfil" ON ("ConfiguracionFondoMutuo"."Value" = "Perfil"."IdPerfil")
WHERE "ConfiguracionFondoMutuo"."Key" IN ('PERFIL_COMERCIAL', 'PERFIL_BACKOFFICE');

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarPerfilFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 