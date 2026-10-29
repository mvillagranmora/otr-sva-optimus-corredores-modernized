INSERT INTO #PRECIOSING (
	"Nemotecnico",
	"Entidad",
	"Fecha",
	"Valor"
)
VALUES (
	:Nemotecnico,
	:Entidad,
	TO_DATE(:Fecha, 'YYYY-MM-DD'),
	:Valor
	)