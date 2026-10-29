SET SCHEMA "BSCL"; 
GO

--PARA IDENTIFICAR EN QUE BOLSA SE TRANSAN LOS INSTRUMENTOS
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacionBolsaInstrumento"; 
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacionBolsaInstrumento"  
AS
SELECT 
	bol."IdBolsa",
	bol."Nombre",
	bolins."IdInstrumento",
	suby."IdInstrumentoSubyacente",
	suby."IdTipoInstrumento",
	suby."EsGarantizable"
FROM "BSCL.Plataforma.BD::INS.Bolsa" AS bol
INNER JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumento" AS bolins ON bolins."IdBolsa" = bol."IdBolsa"
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = bolins."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" AS suby ON suby."IdInstrumento" = bolins."IdInstrumento" AND suby."IdBolsa" = bolins."IdBolsa"
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsaInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;