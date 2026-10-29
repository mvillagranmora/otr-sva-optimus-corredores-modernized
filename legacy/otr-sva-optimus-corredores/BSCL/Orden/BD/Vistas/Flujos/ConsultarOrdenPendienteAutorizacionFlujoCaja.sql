SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCaja";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCaja"
AS WITH  COBOBO AS (SELECT ord."IdOrden",SUM("MontoCobroCalculadoLiquidacion") as "TotalCobros"
						FROM "BSCL.Orden.BD::ORD.CobroOrden" cob
						INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord on cob."IdOrden" = ord."IdOrden"
						WHERE cob."IdGrupoCobro" = ord."IdGrupoCobro"
						GROUP BY ord."IdOrden") 
						SELECT 

		ord."IdOrden",ord."FechaOperacion",ord."FechaVencimiento",ord."FechaLiquidacion",ord."Cantidad",
								CASE tipoope."AccionOrden" 
									WHEN 'COMPRA' THEN "MontoLiquidacion" + "TotalCobros"
									WHEN 'VENTA' THEN "MontoLiquidacion" - "TotalCobros"
								END AS "MontoTotalOrdenMonedaLiquidacion",
								ord."IdMonedaLiquidacionOrden" as "IdMonedaLiquidacion",ord."ValorRescate",ord."IdTipoReajuste",ord."IdEmisor",ord."PrecioOrden",ord."EsVentaCorta",
								ord."IdCondicionDeLiquidacion",ord."IdTipoOperacion",tipoope."AccionOrden" as "Accion",ord."IdCliente",ord."IdPortafolioCliente" AS "IdPortafolio",
								ord."IdEmpresa",ord."IdInstrumento",ord."Nemotecnico",ord."IdTipoInstrumento",ord."IdClasificacionTipoInstrumento",ord."IdInstrumentoSubyacente",ord."IdEmpleadoPortafolio",ord."IdEstadoOrden",ord."IdFormaCustodia",
								ord."EsFungible",ord."FechaEmision",ord."IdBolsa",ord."IdMonedaOrden",ord."MontoOrden"
								
								,CAST(ord."FechaOperacion" AS DATE) AS "FechaOperacionSinHora"
								
								
						FROM "BSCL.Orden.BD::ORD.Orden" ord
						INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on ord."IdTipoOperacion" = tipoope."IdTipoOperacion"
						LEFT JOIN COBOBO cob ON ord."IdOrden" =  cob."IdOrden"
						WHERE "OrdenVigente" = 'S' AND "IdEstadoOrden" = 'PDA';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCaja" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 