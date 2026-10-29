SET SCHEMA BSCL;
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasMetadata";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasMetadata" 
AS 
SELECT
   DISTINCT META."IdMetadataCuentaContable"
   ,META."CuentaOptimus",HOM."CuentaContabilidad",HOM."NombreCuenta",HOM."DetalleCuenta"
FROM "BSCL.Contabilidad.BD::ITZ.MetadataFiltros" META
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" HOM ON (HOM."CuentaOptimus" = META."CuentaOptimus")
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasMetadata" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  
GO
