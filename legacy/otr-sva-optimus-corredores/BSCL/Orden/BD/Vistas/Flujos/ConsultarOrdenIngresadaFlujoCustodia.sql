SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCustodia";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCustodia"
AS SELECT ord."IdOrden",ord."FechaOperacion",ord."FechaVencimiento",ord."FechaLiquidacion",disp."CantidadDisponible",
								(disp."CantidadDisponible" / ord."Cantidad") * ord."MontoOrden" as "MontoDisponible",
								(disp."CantidadDisponible" / ord."Cantidad") * ord."ValorRescate" as "ValorRescateDisponible",
								ord."IdMonedaLiquidacionOrden" as "IdMonedaLiquidacion",ord."ValorRescate",ord."IdTipoReajuste",ord."IdEmisor",ord."PrecioOrden",ord."EsVentaCorta",
								ord."IdCondicionDeLiquidacion",ord."IdTipoOperacion",tipoope."AccionOrden" as "Accion",ord."IdCliente",ord."IdPortafolioCliente" AS "IdPortafolio",
								ord."IdEmpresa",ord."IdInstrumento",ord."Nemotecnico",ord."IdTipoInstrumento",ord."IdClasificacionTipoInstrumento",ord."IdInstrumentoSubyacente",ord."IdEmpleadoPortafolio",ord."IdEstadoOrden",ord."IdFormaCustodia",
								ord."EsFungible",ord."FechaEmision",ord."IdBolsa",ord."IdBoveda",ord."IdMonedaOrden",ord."MontoOrden",CPT."IdConcepto"
						FROM "BSCL.Orden.BD::ORD.Orden" ord
						INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on ord."IdTipoOperacion" = tipoope."IdTipoOperacion"
						INNER JOIN "BSCL.Orden.BD::ORD.ConceptoOrden" CPT ON CPT."IdTipoOperacion" = ord."IdTipoOperacion" AND CPT."Uso" = 'INGRESOORDEN'
						LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden" DISP on DISP."IdOrden" = ord."IdOrden"
						WHERE "OrdenVigente" = 'S' AND ("IdEstadoOrden" = 'ING' OR "IdEstadoOrden" = 'OFE' OR "IdEstadoOrden" = 'ENR') AND "IdFormaCustodia" = 'CUS'
						;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
