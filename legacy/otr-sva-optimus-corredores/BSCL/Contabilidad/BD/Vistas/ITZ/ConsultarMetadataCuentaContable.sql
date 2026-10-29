SET SCHEMA BSCL;
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarMetadataCuentaContable";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarMetadataCuentaContable" 
(  
  "IdMetadataCuentaContable"
  ,"CodigoMetadata"
  ,"DescripcionMetadata"
) AS 
SELECT
 META."IdMetadataCuentaContable"
  ,META."CodigoMetadata"
  ,META."DescripcionMetadata"
FROM "BSCL.Contabilidad.BD::ITZ.MetadataCuentaContable" META
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarMetadataCuentaContable" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  
GO
