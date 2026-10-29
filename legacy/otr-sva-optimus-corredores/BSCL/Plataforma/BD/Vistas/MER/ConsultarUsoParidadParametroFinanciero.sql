SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarUsoParidadParametroFinanciero"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarUsoParidadParametroFinanciero" AS
	SELECT 	
		USO."IdUso"
		,USO."IdParametroFinanciero"
		,USO."Prioridad"
		,COALESCE(DEFINICION."Nombre",'') AS "Nombre"
	FROM "BSCL.Plataforma.BD::MER.UsoParidadParametroFinanciero" USO
	LEFT JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DEFINICION ON (USO."IdParametroFinanciero" = DEFINICION."IdParametroFinanciero");
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarUsoParidadParametroFinanciero" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
