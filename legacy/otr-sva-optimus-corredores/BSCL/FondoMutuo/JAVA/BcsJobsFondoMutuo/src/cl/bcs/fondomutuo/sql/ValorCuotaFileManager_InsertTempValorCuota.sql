INSERT INTO #p_ValorCuota (
	"IDSERIEFONDO",
	"IDMONEDA",
	"FECHACIERRE",
	"VALORCUOTA",
	"OBSERVACION",
	"MENSAJEVALIDACION"
)
VALUES (
	:IdSerieFondo,
	:IdMoneda,
	:FechaCierre,
	:ValorCuota,
	:Observacion,
	:MensajeValidacion
)