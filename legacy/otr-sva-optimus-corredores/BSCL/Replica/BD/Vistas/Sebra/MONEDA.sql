SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.MONEDA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.MONEDA" AS

	SELECT 
		 left(M."IdMoneda",10) AS MON_ID
	  ,  substring(M."Nombre",0,30) AS MON_NOMBRE --- JK  aplicar left al nombre 
	  ,  substring(M."Simbolo",0,2) AS MON_SIMBOLO
	  , M."IdMoneda" AS MON_NEMO
	  , 'N' AS MON_USO_OPERATIVO
	  , '' as MON_CTA_CLI
	  , 0 AS MON_CODIGO
	  , '' AS MON_GLOSA_CORTA
	  , '' AS MON_GLOSA_PLURAL
	  , '' AS MON_IND_TIT_CTE
	FROM "BSCL.Plataforma.BD::PLA.Moneda" M
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.MONEDA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
