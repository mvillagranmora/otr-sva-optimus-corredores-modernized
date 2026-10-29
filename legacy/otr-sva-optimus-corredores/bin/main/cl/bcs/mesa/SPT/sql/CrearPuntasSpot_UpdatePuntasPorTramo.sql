UPDATE "PTT"
SET
	"ValorCompra" = :ValorCompra,
	"ValorVenta" = :ValorVenta,
	"IdUsuario" = :IdUsuario,
	"FechaModificacion" = :FechaModificacion
FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo" AS "PTT"
WHERE
	"PTT"."IdPuntas" = :IdPuntas
	AND "PTT"."IdEmpresa" = :IdEmpresa
	AND "PTT"."Canal" = :Canal
	AND "PTT"."IdPuntasPorTramo" = :IdPuntasPorTramo;