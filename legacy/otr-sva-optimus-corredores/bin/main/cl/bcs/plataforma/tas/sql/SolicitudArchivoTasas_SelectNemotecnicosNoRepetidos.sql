SELECT
	"Nemotecnico" as "nemotecnico"
FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
GROUP BY "Nemotecnico"
HAVING count(*) < 2;