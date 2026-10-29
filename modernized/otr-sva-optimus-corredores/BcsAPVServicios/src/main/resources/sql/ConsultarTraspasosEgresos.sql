SELECT
	Traspaso."IdTraspaso",
	Traspaso."FechaModificacion",
	DetalleTraspaso."IdDetalleOrigenTraspaso",
	DetalleTraspaso."MontoTraspaso",
	DetalleTraspaso."PorcentajeTraspaso",
	DetalleTraspaso."NombreEntidad",
	DetalleTraspaso."DocumentoEntidad"
FROM
	"BSCL.APV.BD::TRA.Traspaso" AS Traspaso
LEFT OUTER JOIN "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" AS DetalleTraspaso
	ON DetalleTraspaso."IdTraspaso" = Traspaso."IdTraspaso"
WHERE Traspaso."IdTraspaso" = :IdTraspaso
