SET SCHEMA "BSCL"; 
GO

--PARA IDENTIFICAR EN QUE BOLSA SE TRANSAN LOS INSTRUMENTOS
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsaInstrumento"; 
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsaInstrumento"  
AS
SELECT 
	bol."IdBolsa",
	bol."Nombre",
	bolins."IdInstrumento",
	bol."IdPais",
	bol."EsExtranjera",
	bol."TieneFormaNegociacion",
	bol."UsaBovedaLocal"
FROM "BSCL.Plataforma.BD::INS.Bolsa" AS bol
INNER JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumento" AS bolins ON bolins."IdBolsa" = bol."IdBolsa"
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = bolins."IdInstrumento"
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsaInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

