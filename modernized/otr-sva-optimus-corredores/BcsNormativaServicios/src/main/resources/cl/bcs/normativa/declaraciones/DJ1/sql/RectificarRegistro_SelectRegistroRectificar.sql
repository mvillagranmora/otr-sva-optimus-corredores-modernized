SELECT
	"ID",
	"KIdContrato",
	"Rectifica" AS "ValorAntiguo",
	:ValorNuevo AS "ValorNuevo",
	'Rectifica' AS "Campo"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1"
WHERE "ID" IN (:Id) AND "Rectifica"<>:ValorNuevo;