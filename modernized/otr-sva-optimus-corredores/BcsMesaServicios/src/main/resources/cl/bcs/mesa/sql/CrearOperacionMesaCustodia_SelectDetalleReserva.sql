SELECT
	"IdReserva",
	"FolioCarteraCustodia",
	"IdCarteraCustodia"
FROM
	"BSCL.Custodia.BD::CUS.Reserva"
WHERE
	"IdReserva" IN (:IdReserva)