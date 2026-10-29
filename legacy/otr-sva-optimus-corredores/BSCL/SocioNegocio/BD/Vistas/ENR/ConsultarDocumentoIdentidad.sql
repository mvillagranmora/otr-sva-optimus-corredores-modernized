SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDocumentoIdentidad";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDocumentoIdentidad" AS
    SELECT
         DI."IdDocumentoIdentidad"
        ,DI."IdEnrolamiento"
        ,DI."IdTipoDocumentoIdentidad"
        ,DI."DocumentoIdentidad"
        ,DI."NumeroSerie"
        ,DI."FechaEmision"
        ,DI."FechaVencimiento"
        ,DI."Predeterminado"
        ,DI."IdTipoDocumentoIdentidadAsociado"
        ,DI."TipoPersona"
        ,DI."DocumentoIdentidadAsociado"
    FROM "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad" DI
WITH READ ONLY;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDocumentoIdentidad" TO BCS_DESARROLLADOR WITH GRANT OPTION;
