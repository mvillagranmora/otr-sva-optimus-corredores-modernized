SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoPerfilFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoPerfilFondoMutuo"
AS
SELECT
"ConfiguracionFondoMutuo"."Key" AS "IdKeyPerfil"
, "ConfiguracionFondoMutuo"."Key"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "ConfiguracionFondoMutuo"
WHERE "ConfiguracionFondoMutuo"."Key" IN ('PERFIL_COMERCIAL', 'PERFIL_BACKOFFICE');

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoPerfilFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
