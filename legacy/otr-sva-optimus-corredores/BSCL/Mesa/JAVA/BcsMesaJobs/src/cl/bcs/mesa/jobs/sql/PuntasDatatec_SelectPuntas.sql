SELECT
	"CheckPrecioPorTramo",
	"IdPuntas",
	"IdMonedaPunta",
	"Estado",
	"AperturaPrecio",
	"IdUsuario",
	"PuntaCompra",
	"PuntaVenta",
	'Datatec' AS "Origen"
FROM "BSCL.Mesa.BD::SPT.Puntas"
WHERE "IdMonedaPunta" = :IdMoneda;