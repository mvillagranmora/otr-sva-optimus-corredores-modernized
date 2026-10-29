SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarGrupoPortafoliosCartola"; 
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarGrupoPortafoliosCartola" AS
SELECT  G."IdEmpresa",               
		G."IdGrupo",               
		G."Codigo" AS "CodigoGrupoPortafolio",               
		G."Nombre" AS "NombreGrupoPortafolio",               
		GC."IdPortafolio",               
		PORT."IdCliente" 
FROM "BSCL.SocioNegocio.BD::CLI.Grupo" G 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.GrupoClientes" GC ON GC."IdGrupo" = G."IdGrupo" 
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON PORT."IdPortafolio" = GC."IdPortafolio" 
WHERE G."IdEstado" = 'ACT'; 

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarGrupoPortafoliosCartola" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 