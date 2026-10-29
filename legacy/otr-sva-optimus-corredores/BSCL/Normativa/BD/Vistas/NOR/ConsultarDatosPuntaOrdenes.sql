
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarDatosPuntaOrdenes"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarDatosPuntaOrdenes" AS

SELECT
		"FECHA" 		AS "FECHA",
		"MERCADO" 		AS "MERCADO",
		"BOLSA" 		AS "BOLSA",
		"NEMO" 			AS "NEMO",	
		"COD_CORR" 		AS "COD_CORR",
		"CONDICION" 	AS "CONDICION",
		"COM_VEN" 		AS "COM_VEN",
		"MEJOR_BOLSA"	AS "MEJOR_BOLSA",
		"MEJOR_HORA" 	AS "MEJOR_HORA",
		"MEJOR_PRECIO" 	AS "MEJOR_PRECIO",
		"PEOR_BOLSA" 	AS "PEOR_BOLSA", 
		"PEOR_HORA" 	AS "PEOR_HORA",	
		"PEOR_PRECIO" 	AS "PEOR_PRECIO" 
FROM "BSCL.Normativa.BD::NOR.DatosPuntaOrdenes";
	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarDatosPuntaOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


