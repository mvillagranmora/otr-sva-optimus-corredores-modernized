SELECT "IdPortafolioCliente","IdInstrumento","IdTipoOperacion","Cantidad","CantidadOrdenes" ,"PrecioLimite","FechaSistema"
FROM "BSCL.Orden.BD::ORD.CrearOrdenes_BloqueaOrden"
WHERE "IdBloqueaOrden" = :p_IdBloqueaOrden
	AND "IdPortafolioCliente" = :p_IdPortafolioCliente
	AND "IdInstrumento" = :p_IdInstrumento
	AND "IdTipoOperacion" = :p_IdTipoOperacion
	AND "Cantidad" = :p_Cantidad
	AND "CantidadOrdenes" = :p_CantidadOrdenes
	AND "PrecioLimite" = :p_PrecioLimite
	AND "FechaSistema" = :p_FechaSistema
FOR UPDATE;