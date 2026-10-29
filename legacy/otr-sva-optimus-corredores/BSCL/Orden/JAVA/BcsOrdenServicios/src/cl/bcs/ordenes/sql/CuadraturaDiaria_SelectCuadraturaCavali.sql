SELECT 
	CCV."NumeroOperacion",
	CAST(CCV."FechaOperacion" As Date) As "FechaOperacion",
	COALESCE(CT."FolioTransaccion",'') AS "FolioTransaccion",
	"CantidadOperacion",
	"MontoEfectivo",
	"DescripcionNegocio",
	"PrecioOperacion"
FROM "BSCL.Orden.BD::ORD.CargaCuadraturaCavali" CCV
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" CT 
		ON (CT."FolioTransaccion" = CCV."NumeroOperacion" 
		AND CT."MontoTransaccion" = CCV."MontoEfectivo"
		AND CT."CantidadTransaccion" = CCV."CantidadOperacion")
WHERE CAST(CCV."FechaOperacion" As Date) = :FechaOperacion;