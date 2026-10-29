SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::MES.ConsultarDocumentos";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MES.ConsultarDocumentos" AS
SELECT "Codigo" AS "Codigo", "Nombre" AS "Nombre" , "IdTipoOperacion" AS "IdTipoOperacion", "TipoOperacionMesa" AS "TipoOperacionMesa"
FROM "BSCL.Mesa.BD::MES.TipoDocumento";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MES.ConsultarDocumentos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
