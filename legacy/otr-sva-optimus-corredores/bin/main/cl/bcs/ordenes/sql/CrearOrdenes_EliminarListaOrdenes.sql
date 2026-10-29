DELETE FROM "BSCL.Orden.BD::ORD.CrearOrdenes_BloqueaOrden"
    WHERE "IdBloqueaOrden" IN (:IdOrdenes);