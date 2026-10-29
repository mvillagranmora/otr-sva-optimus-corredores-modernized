SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOperacionYCondicionDeLiquidacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOperacionYCondicionDeLiquidacion"  
AS
SELECT 
	"T1"."IdTipoOperacionYCondicionDeLiquidacion" , 
	"T1"."IdCondicionDeLiquidacion" , 
	"T1"."IdTipoOperacion" , 
	"T1"."SoloDiasHabiles" , 
	"T1"."IdEstado" , 
	"TipoOperacion_$2"."Nombre" AS "NombreTipoOperacion" , 
	"C1"."Codigo" AS 
	"CodigoCondicionDeLiquidacion" , 
	"C1"."Nombre" AS "NombreCondicionDeLiquidacion" , 
	"C1"."PlazoEnDias" 
	FROM  "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" AS "T1" 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "C1" ON "C1"."IdCondicionDeLiquidacion" = "T1"."IdCondicionDeLiquidacion"  
	LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$2" ON "TipoOperacion_$2"."IdTipoOperacion" = "T1"."IdTipoOperacion";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOperacionYCondicionDeLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
