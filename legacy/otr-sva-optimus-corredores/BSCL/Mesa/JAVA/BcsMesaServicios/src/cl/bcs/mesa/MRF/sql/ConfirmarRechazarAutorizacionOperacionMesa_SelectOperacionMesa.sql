SELECT
	OperacionMesa."IdOperacion"
	, OperacionMesa."IdTipoOperacion"
	, OperacionMesa."FechaOperacion"
	, OperacionMesa."IdEstado" AS "Estado"
	, OperacionMesa."IdEstado" AS "EstadoPrevioAutorizacion"
	, '' AS "EstadoPosteriorAutorizacion"
	, OperacionMesa."IdEmpresa"
FROM "BSCL.Mesa.BD::MES.Operacion" OperacionMesa
WHERE OperacionMesa."IdOperacion" IN (:IdOperacion) AND OperacionMesa."IdEstado" = :IdEstado;