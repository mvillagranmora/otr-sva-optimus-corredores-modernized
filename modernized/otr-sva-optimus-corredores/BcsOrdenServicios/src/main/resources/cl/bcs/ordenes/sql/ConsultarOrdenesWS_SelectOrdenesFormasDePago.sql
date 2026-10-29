SELECT PAGO."IdOrden",
	PAGO."IdMedioDePago",
	ORD."MontoOrden" AS "MontoPago",
	PAGO."IdBanco"
FROM "BSCL.Orden.BD::ORD.FormaDePagoOrden" PAGO
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarOrden" ORD ON (PAGO."IdOrden" = ORD."IdOrden")
WHERE PAGO."IdOrden" IN (:p_IdOrden);