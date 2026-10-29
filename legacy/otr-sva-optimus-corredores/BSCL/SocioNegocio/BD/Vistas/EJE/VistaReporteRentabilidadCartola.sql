SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.VistaReporteRentabilidadCartola";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.VistaReporteRentabilidadCartola" AS
SELECT 
	CARHIS."NombreCliente" AS "Cliente",
	CARHIS."CategoriaInversionista" AS "PerfilRiesgo",
	CARHIS."RentabilidadYTD" AS "YTD",
	CARHIS."RentabilidadMTD" AS "MTD",
	CARHIS."TotalPatrimonio" AS "Pesos",
	CARHIS."RentabilidadITD" AS "ITD",
	CARHIS."NombreAgente" AS "NombreAgente",
	CARHIS."Fecha"   AS  "FechaVencimiento",
	CARHIS."IdAgente" AS "IdAgente"
	
FROM "BSCL.Normativa.BD::CAR.CartolaHistorica" AS CARHIS 
