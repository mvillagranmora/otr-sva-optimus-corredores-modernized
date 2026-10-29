
SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoReferencia";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoReferencia" AS
SELECT "TipoReferencia", "Razon", "Descripcion" FROM "BSCL.Facturacion.BD::FAC.ReferenciaDocumento";

GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTipoReferencia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

