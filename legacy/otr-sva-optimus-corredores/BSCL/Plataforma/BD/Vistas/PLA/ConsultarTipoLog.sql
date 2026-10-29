SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoLog"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoLog"  
AS
SELECT 	"TipoLog"."IdTipoLog" as "IdTipoLog" ,
		"TipoLog"."IdTipoLog" as "DescripcionTipoLog",
		"TipoLog"."Descripcion"	as "Descripcion"
		
FROM "BSCL.Plataforma.BD::PLA.TipoLog" AS "TipoLog";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoLog" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 