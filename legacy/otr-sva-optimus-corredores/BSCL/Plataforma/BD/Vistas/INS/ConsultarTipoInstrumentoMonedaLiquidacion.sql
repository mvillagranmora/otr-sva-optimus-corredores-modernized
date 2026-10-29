SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion"  
AS
SELECT 
	"MonedaLiquidacion"."IdTipoInstrumentoMonedaLiquidacion"
	,"MonedaLiquidacion"."IdTipoInstrumento"
	,"MonedaLiquidacion"."IdMoneda"
	,"MonedaLiquidacion"."IdMonedaLiquidacion"
	,"Moneda"."Nombre" as "NombreMonedaLiquidacion"
	,"MonedaLiquidacion"."EsPorDefecto"
FROM  "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" AS "MonedaLiquidacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as "Moneda" on "MonedaLiquidacion"."IdMonedaLiquidacion" = "Moneda"."IdMoneda"
;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


