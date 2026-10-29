SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoRetencion";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoRetencion"
AS SELECT 
TIPOR."Uso",TIPOR."IdTipoRetencion",TIPOR."Nombre",TIPOR."IdEstado"
FROM "BSCL.Facturacion.BD::FAC.TipoRetencion" TIPOR
WHERE TIPOR."IdEstado" = 'ACT';
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTipoRetencion"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

