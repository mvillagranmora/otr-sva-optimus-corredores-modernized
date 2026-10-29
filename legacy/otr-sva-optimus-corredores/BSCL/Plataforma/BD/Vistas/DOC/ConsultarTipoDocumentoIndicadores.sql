SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarTipoDocumentoIndicadores";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarTipoDocumentoIndicadores" AS
SELECT TDI."IdTipoIndicador"    AS "IdTipoIndicador"
     , TI."IdTipoEntidad"       AS "IdTipoEntidad"
     , TDI."IdEmpresa"          AS "IdEmpresa"
     , TI."NombreTipoIndicador" AS "NombreTipoIndicador"
     , TI."CodigoTipoIndicador" AS "CodigoTipoIndicador"
     , TI."GrupoIndicador"      AS "GrupoIndicador"
     , TI."IdUso"               AS "IdUso"
     , TI."IdEstado"            AS "IdEstado"
     , TI."IdTipoPersona"       AS "IdTipoPersona"
     , TDI."IdTipoDocumento"    AS "IdTipoDocumento"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumentoIndicador" TDI
     LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (TI."IdTipoIndicador" = TDI."IdTipoIndicador");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::DOC.ConsultarTipoDocumentoIndicadores" TO BCS_DESARROLLADOR WITH GRANT OPTION;
