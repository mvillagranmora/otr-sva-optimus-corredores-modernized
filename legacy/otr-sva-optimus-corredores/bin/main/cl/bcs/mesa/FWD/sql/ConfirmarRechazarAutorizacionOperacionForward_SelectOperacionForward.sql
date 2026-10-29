SELECT
	OperacionForward."IdOperacion"
	, OperacionForward."IdTipoOperacion"
	, OperacionForward."FechaOperacion"
	, OperacionForward."IdEstado"
	, OperacionForward."IdEstado" AS "EstadoPrevioAutorizacion"
	, '' AS "EstadoPosteriorAutorizacion"
	, OperacionForward."IdEmpresa"
FROM "BSCL.Mesa.BD::MES.Operacion" OperacionForward
WHERE OperacionForward."IdOperacion" IN (:IdOperacion) AND OperacionForward."IdEstado" = :IdEstado;