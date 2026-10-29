SET SCHEMA BSCL;
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarMetadataFiltros";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarMetadataFiltros" 
(  
  "IdMetadataFiltros"		  
  ,"IdMetadataCuentaContable"
  ,"CuentaOptimus"			  
  ,"IdTipoDatoEvento"		  
  ,"Condicion"				  
  ,"Valor"			   		  

) AS 
SELECT
"IdMetadataFiltros"		  
  ,"IdMetadataCuentaContable"
  ,"CuentaOptimus"			  
  ,"IdTipoDatoEvento"		  
  ,"Condicion"				  
  ,"Valor"	
FROM "BSCL.Contabilidad.BD::ITZ.MetadataFiltros"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarMetadataFiltros" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  
GO
