SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMoneda"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMoneda"  
AS
SELECT
	TMO."IdTipoMoneda",
	TMO."Nombre" AS "NombreTipoMoneda",
	TMO."MultiplicaODivide",
	TMO."Descripcion"	
FROM "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TMO;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMoneda" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
