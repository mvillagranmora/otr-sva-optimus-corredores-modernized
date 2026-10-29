SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaPaisResidencia";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaPaisResidencia" AS
SELECT
    FPR."IdPersona" || ':' || FPR."IdPaisResidencia" AS TABLE_KEY,
    DOC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
    FPR."IdPersona" AS "IDPERSONA",
    FPR."IdPaisResidencia" AS "IDPAISRESIDENCIA",
    PAIS."NombrePais" AS "PAISRESIDENCIA",
    FPR."TIN" AS "TIN",
    FPR."IdCausaNoTin" AS "IDCAUSANOTIN",
    CASE WHEN FPR."IdCausaNoTin" = 'OTRA' THEN FPR."Observacion" ELSE CNT."Descripcion" END AS "CAUSANOTIN",
    FPR."PaisResidenciaPrincipal" AS "PAISRESIDENCIAPRINCIPAL"
FROM "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia" AS FPR
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON PAIS."IdPais" = FPR."IdPaisResidencia"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CausaNoTin" AS CNT ON CNT."IdCausaNoTin" = FPR."IdCausaNoTin"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = FPR."IdPersona" AND DOC."Borrado" = 'N' AND DOC."Predeterminado" = 'S'WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaFatcaPaisResidencia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;