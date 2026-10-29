SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarGrupoClientes"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarGrupoClientes" AS 
SELECT  GRU."IdGrupo",   
		GRU."IdEmpresa",   
		GRU."Nombre",   
		GRU."IdEstado",   
		CRGP."NombreEstado" AS "EstadoGrupo",   
		GRU."Codigo" 
FROM "BSCL.SocioNegocio.BD::CLI.Grupo" GRU INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoGrupoClientes" CRGP ON GRU."IdEstado" = CRGP."IdEstado"; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarGrupoClientes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 