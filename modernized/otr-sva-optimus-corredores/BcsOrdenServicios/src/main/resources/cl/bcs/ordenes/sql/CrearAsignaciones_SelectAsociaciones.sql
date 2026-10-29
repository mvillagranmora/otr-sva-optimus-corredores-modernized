SELECT "IdOrdenAsociada","IdOrdenN","IdOrdenM",CAST ("FechaIngresoRegistroCorta" AS Date) AS "FechaIngresoRegistroCorta",cast("FechaIngresoRegistro" AS Date) AS "FechaIngresoRegistro","ModalidadCalculo","Monto","Cantidad","IdUsuario","IdOrdenMOrigen" 
FROM "BSCL.Orden.BD::ORD.OrdenAsociada" O
LEFT JOIN "BSCL.Orden.BD::ORD.CrearAsignaciones_Proceso" CAP ON O."IdOrdenN" = CAP."IdOrden"
WHERE CAP."IdOrden" IS NOT NULL