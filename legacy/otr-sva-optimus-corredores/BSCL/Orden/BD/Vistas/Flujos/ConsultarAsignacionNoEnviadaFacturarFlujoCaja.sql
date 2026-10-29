SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCaja";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCaja"
AS WITH  COBOBO AS (SELECT asig."IdAsignacion",SUM("MontoCobroCalculadoLiquidacion") as "TotalCobros"
					FROM "BSCL.Orden.BD::ORD.CobroAsignacion" cob
					INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig on cob."IdAsignacion" = asig."IdAsignacion"
					WHERE asig."IdEstadoAsignacion" = 'NEF'
					GROUP BY asig."IdAsignacion") 
		SELECT	asig."IdAsignacion",asig."IdTransaccion",ord."IdOrden",asig."FechaAsignacion",ord."FechaVencimiento",tran."FechaLiquidacion",asig."CantidadAsignada",
								CASE tipoope."AccionOrden" 
									WHEN 'COMPRA' THEN asig."MontoAsignadoLiquidacion" + COALESCE(sumcobAsig."TotalCobros",0)
									WHEN 'VENTA' THEN asig."MontoAsignadoLiquidacion" - COALESCE(sumcobAsig."TotalCobros",0)
								END AS "MontoTotalAsignadoMonedaLiquidacion",
								ord."IdMonedaLiquidacionOrden" as "IdMonedaLiquidacion",asig."ValorRescateAsignado",
								ord."IdTipoReajuste",ord."IdEmisor",tran."Precio" as "PrecioAsignacion",ord."EsVentaCorta",
								ord."IdCondicionDeLiquidacion",ord."IdTipoOperacion",tipoope."AccionOrden" as "Accion",ord."IdCliente",ord."IdPortafolioCliente" AS "IdPortafolio",
								ord."IdEmpresa",ord."IdInstrumento",ord."Nemotecnico",ord."IdTipoInstrumento",ord."IdClasificacionTipoInstrumento",ord."IdInstrumentoSubyacente",ord."IdEmpleadoPortafolio",asig."IdEstadoAsignacion",ord."IdFormaCustodia",
								tran."EsFungible",tran."FechaEmision",tran."IdBolsa",tran."IdMoneda" as "IdMonedaAsignacion",asig."MontoAsignado"
								
								,CAST(asig."FechaAsignacion" AS DATE) AS "FechaAsignacionSinHora"
								
						FROM "BSCL.Orden.BD::ORD.Asignacion" asig
						INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord on asig."IdOrden" = ord."IdOrden"
						INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran on asig."IdTransaccion" = tran."IdTransaccion"
						INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on ord."IdTipoOperacion" = tipoope."IdTipoOperacion"
						LEFT JOIN COBOBO sumcobAsig on asig."IdAsignacion" = sumcobAsig."IdAsignacion"
						WHERE "IdEstadoAsignacion" = 'NEF';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCaja" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 