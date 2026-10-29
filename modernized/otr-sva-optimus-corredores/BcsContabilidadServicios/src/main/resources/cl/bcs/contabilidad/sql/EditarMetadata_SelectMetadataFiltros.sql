SELECT
"IdMetadataFiltros"		  
  ,"IdMetadataCuentaContable"
  ,"CuentaOptimus"			  
  ,"IdTipoDatoEvento"		  
  ,"Condicion"				  
  ,"Valor"	
FROM "BSCL.Contabilidad.BD::ITZ.MetadataFiltros"
WHERE "IdMetadataCuentaContable" = :IdMetadataCuentaContable
