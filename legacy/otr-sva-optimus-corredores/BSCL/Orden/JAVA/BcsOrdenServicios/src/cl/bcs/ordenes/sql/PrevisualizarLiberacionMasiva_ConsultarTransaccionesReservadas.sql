/*

Consultar transacciones reservadas para liberar

*/

SELECT "IdTransaccion", "FolioTransaccion",
		TO_VARCHAR("FechaSistemaCreacion", 'YYYY-MM-DD') AS "FechaSistemaCreacion",
		TO_VARCHAR("HoraSistemaCreacion", 'HH24:MI:SS') AS "HoraSistemaCreacion",
		"Instrumento" AS "Nemotecnico", "IdInstrumento", "IdTipoInstrumento", "IdClasificacionTipoInstrumento",
		"AccionOrden", "IdBolsa", "IdMonedaTransaccion" AS "IdMoneda", "IdCondicionDeLiquidacion",
		"NombreCondicionDeLiquidacion", "IdLibroOrdenes", "NombreLibroOrdenes", "Reservado",
		"CantidadTransaccion" AS "Cantidad", "MontoTransaccion" AS "Monto"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion"
WHERE "Reservado" = 'S'
AND TO_SECONDDATE(TO_VARCHAR("FechaSistemaCreacion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR("HoraSistemaCreacion", 'HH24:MI:SS')) >= :FechaDesde
AND TO_SECONDDATE(TO_VARCHAR("FechaSistemaCreacion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR("HoraSistemaCreacion", 'HH24:MI:SS')) <= :FechaHasta;
