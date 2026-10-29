SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" 
AS
SELECT 
	"CDL_$0"."IdCondicionDeLiquidacion" , 
	"CDL_$0"."Codigo" , 
	"CDL_$0"."Nombre" , 
	"CDL_$0"."PlazoEnDias" , 
	"TOYDL_$1"."IdTipoOperacion" ,
	"tipoOpe"."Accion" as "AccionTipoOperacion" , 
	"TOYDL_$1"."IdEstado" AS "EstadoTipoOperacionYCondicionDeLiquidacion" ,  
	(  ( "CDL_$0"."Codigo" || ' - ' )  || "CDL_$0"."Nombre" )  AS "NombreLargo" ,
	"CDL_$0"."Orden"
FROM "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CDL_$0" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" AS "TOYDL_$1" ON  ( "TOYDL_$1"."IdCondicionDeLiquidacion" = "CDL_$0"."IdCondicionDeLiquidacion" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "tipoOpe" ON  ( "tipoOpe"."IdTipoOperacion" = "TOYDL_$1"."IdTipoOperacion" );

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
