SELECT
	"Fecha",
	"IdPais"
FROM "BSCL.Plataforma.BD::REG.DiaFeriado"
WHERE "IdPais" IN (:IdPais)
	AND "IdEstado" = 'ACT'
	AND "Fecha" IN (:Fechas);