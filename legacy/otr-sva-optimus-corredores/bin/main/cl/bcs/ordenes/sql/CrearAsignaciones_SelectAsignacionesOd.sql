
SELECT tran."IdTransaccion",tran."IdTransaccionOd",asig."IdOrden",asig."IdAsignacion",ord."IdPortafolioCliente" AS "IdPortafolio"
	,tran."IdClasificacionTipoInstrumento"
FROM "BSCL.Orden.BD::ORD.Transaccion" tran
INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON tran."IdTransaccion" = asig."IdTransaccion"
INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON ord."IdOrden" = asig."IdOrden"
LEFT JOIN "BSCL.Orden.BD::ORD.CrearAsignaciones_Proceso" CAP ON tran."IdTransaccionOd" = CAP."IdTransaccion"
WHERE "IdTransaccionOd" != 0 AND CAP."IdTransaccion" IS NOT NULL AND asig."IdEstadoAsignacion" != 'ANU';


