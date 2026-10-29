SELECT "TipoMensaje","IdMensaje1","IdMensaje2"
FROM "BSCL.Orden.BD::ORD.ControlMercado"
WHERE "TipoMensaje" || "IdMensaje1" || "IdMensaje2" IN (:llaves);