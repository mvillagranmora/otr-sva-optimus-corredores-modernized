SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarDefinicionParametroFinanciero"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarDefinicionParametroFinanciero" AS
	SELECT 	
		"IdParametroFinanciero"
		,"Periodo"
		,"Nombre"
		,"Descripcion"
		,"IdMonedaParametro"
		,"IdMonedaValor"
	FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero";
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarDefinicionParametroFinanciero" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
