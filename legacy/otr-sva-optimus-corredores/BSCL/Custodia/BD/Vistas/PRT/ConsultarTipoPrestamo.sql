SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo";

CREATE VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo"
AS 
SELECT
	prt."IdInstrumentoPrestamo",
	prt."IdTipoPrestamo",
	ins."Nemotecnico",
	prt."IdEstado",
	prt."MinimoDiasAnticipo",
	prt."MaximoDiasPrestamo",
	prt."EsDiaHabilPlazoPrestamo",
	prt."EsDiaHabilParaCalculoInteres",
	prt."CobraPrimaAlInicio",
	prt."EsFueraRueda",
	prt."MinimoDiasPrestamo", 
	prt."IdBovedaDefecto",
	prt."SoloCuentaPropia",
	prt."PlazoSugerido",
	ins."IdEmisor",
	prt."TipoPrestamoSistemaExterno",
	prt."EsRenovable",
	prt."IdClasificacionTipoInstrumento"
FROM "BSCL.Custodia.BD::PRT.TipoPrestamo" as prt--
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" as ins on ins."IdInstrumento" = prt."IdInstrumentoPrestamo"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
