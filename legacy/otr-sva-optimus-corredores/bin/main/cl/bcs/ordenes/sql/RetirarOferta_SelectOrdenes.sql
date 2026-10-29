SELECT
ORD."IdOrden"
,TO_VARCHAR(CAST(ORD."FechaRecepcion" AS  DATE),'yyyy-mm-dd') AS "FechaRecepcion"
,TO_VARCHAR(CAST(ORD."FechaVigencia" AS  DATE),'yyyy-mm-dd') AS "FechaVigencia"
,TO_VARCHAR(CAST(ORD."FechaEmision" AS  DATE),'yyyy-mm-dd') AS "FechaEmision"
,TO_VARCHAR(CAST(ORD."FechaOperacionOrden" AS  DATE),'yyyy-mm-dd') AS "FechaOperacion"
,ORD."IdTipoVigenciaOrden" AS "IdTipoVigencia"
,ORD."GlosaOrden" AS "Glosa"
,ORD."CantidadDisponibleOrden" AS "Cantidad"
,ORD."MontoOrden" AS "MontoNeto"
,ORD."ValorRescate"
,ORD."IdBoveda"
,ORD."IdFormaCustodia"
,PV."IdPorcentajeVisibilidad"
--,PrecioLimite
--,EsFacturaInmediata
, ORD."IdTipoPrecio"
, ORD."IdPortafolio"
,ORD."IdTipoLibroOrden"
, ORD."IdLibroOrdenes"
, ORD."IdTipoCanalIngreso"
, ORD."IdTipoReajuste" 
, ORD."IdEmisor" 
, ORD."IdBolsa" 
, ORD."EspecificaBolsa"
, ORD."IdMonedaLiquidacionOrden"
, ORD."IdMonedaOrden"
, ORD."TipoCambio"
, ORD."IdInstrumento"
, ORD."IdInstrumentoSubyacente"
, ORD."TasaVentaCorta" 
, ORD."TasaSimultanea" 
, ORD."TasaRFIF"
, ORD."PlazoIF"
, ORD."PlazoSimultanea"
, ORD."PlazoVentaCorta"
, ORD."EsVentaCorta"
--,EsOD
--,IdCorredorCorresponsal
,ORD."IdPersonaAutorizadoDarOrdenes"
, ORD."IdTipoCanalDistribucion"
, ORD."TasaReporte" 
, ORD."BaseTasa"
,ORD."TipoInteres"
, ORD."IdCondicionDeLiquidacion" 
, ORD."IdTipoOperacion" 
, ORD."IdCuentaCustodioPortafolio"
--,EsPorMonto
--,EsOrdenEspecial
,CASE WHEN ORD."IdMedioDePago" = 'CIN' THEN 'S' ELSE 'N' END AS "PagoCobroCuentaInversion"
--,IdPreferenciaPagoPortafolio
,ORD."CodigoClaseOrden" AS "IdMecanismoCaptacion"
, ORD."TasaPromedioReporte"
, ORD."TasaReportanteReferencia"
--,NemotecnicoSVS
--,IdSistemaBursatil
--,InstrumentoAcreditado
--,FormadorMercado
--,EsClienteFinal
, ORD."MontoInteresContado"
, ORD."MontoInteresPlazo"
--,PorcentajeAllIn
, ORD."IdOrden" AS "IdOrdenReferencia"
FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" ORD 
LEFT JOIN "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" PV ON PV."Nombre" = ORD."PorcentajeVisibilidad"
WHERE "IdOrden" IN (:IdOrden) AND "IdEstadoOrden" = 'OFE' AND "IdEstadoAsignacion" = 'SAS'
