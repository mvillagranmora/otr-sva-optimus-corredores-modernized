SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaPrestamosPendientes";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaPrestamosPendientes" AS

SELECT 
	"IdPrestamosPendientes",
	"FolioPendiente",
	"TipoRegistro",
	"Nemotecnico",
	"FechaIngreso",
	"Mercado",
	"DocumentoIdentidad",
	"TipoDocIdentidad",
	"Cantidad",
	"Tasa",
	"CondicionLiquidacion",
 	"FechaLiquidacion",
 	"CodigoCorredora",
 	"FolioSistemaExterno",
 	"Origen",
 	"Mensaje",
 	"Estado"		
FROM "BSCL.Custodia.BD::CUS.PrestamosPendientes"  
WHERE "Estado" = 'ACTIVO'

WITH READ ONLY;
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultaPrestamosPendientes" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
 
