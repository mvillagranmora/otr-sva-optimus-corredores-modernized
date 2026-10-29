SELECT "IdEstado","NombreEstado","IdEstadoSiguiente","NombreEstadoSiguiente","IdTipoProducto","NombreEntidad"
FROM "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa"
WHERE "IdEstado" IN (:IdEstado) and "IdTipoProducto" IN (:IdTipoProducto) and "NombreEntidad" IN (:NombreEntidad);