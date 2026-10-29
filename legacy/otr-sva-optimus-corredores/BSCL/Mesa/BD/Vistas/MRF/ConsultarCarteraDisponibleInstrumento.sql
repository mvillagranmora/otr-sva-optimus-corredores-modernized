SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraDisponibleInstrumento";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraDisponibleInstrumento" AS

SELECT
	CAR."IdEmpresa"
	,CAR."IdPortafolio"
	,CAR."IdCliente"
	,CAR."IdMesa"
	,CAR."FolioCartera"
	,CAR."IdCartera"
	,CAR."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"
	,CAR."ValorClasificacionRiesgo"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdEmisor"
	,CAR."IdBoveda"
	,CAR."IdBolsa"
	,CAR."EsFungible"
	--,CAR."CodigoIFRS"
	,COALESCE(CatIFRS."CodigoCarteraIFRS" ,'')  as "CodigoIFRS"
    ,COALESCE(CatIFRS."CarteraIFRS",'')         as "CodigoDescIFRS"
	,CAR."IdMonedaSistema"
	,CAR."IdTipoReajuste"
	,CAR."IdMonedaEmision"         AS "Moneda"
    ,CAR."IdMonedaInstrumento"     AS "MonedaInstrumento"-- Equivale a la moneda de liquidacion de esa cartera
	,CAR."INGMontoCompraActualMonedaInstrumento"	
	,CAR."IdMonedaInstrumento"
--	,CAR."MontoCompra" AS "INGMontoATasaCompraActual"
	,CAR."INGMontoATasaCompraActual"
	,CAR."Cantidad"
	,CAR."INGCantidad"
	,CASE WHEN CAR."IdClasificacionTipoInstrumento"='IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad_UI"		
	--* ,INS."CorteMinimo"	--CAR."CorteMinimo"
	,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' 
		  THEN CAR."CorteMinimo"
		  ELSE INS."CorteMinimo"	
	 END AS "CorteMinimo"
	,CAR."INGTasaCompra"
	--,"INGMontoATasaMercadoProximo"
	--,"INGValorRescate"
	,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' THEN 0
		   ELSE CAR."INGValorRescate" 
	 END AS "INGValorRescate"
	,CAR."INGTasaMercadoActual"
	,CAR."FechaVencimientoInstrumento"
	,CAR."INGPrecioMercadoActual"
	,CAR."INGPrecioCompra" AS "INGPrecioCompraActual"
	,CASE WHEN CAR."TipoCarteraCorredor" = 'CC' THEN CAR."FechaVencimientoOperacion"
		  ELSE CAR."FechaLiquidacion" 
	 	  END AS "FechaLiquidacion"
	,0 as "CantidadSimulacion"
	,0 as "PrecioSimulacion"
	,0 as "TasaSimulacion"
	,0 as "MontoSimulacion"
	,0 as "ValorRescateSimulacion"
	,CAR."FechaVencimientoInstrumento" as "FechaVencimientoSimulacion"
	,CASE WHEN CAR."TipoCarteraCorredor" = 'CC' THEN 'S' 
		  ELSE 'N' 
	      END AS "CarteraPacto"
	,CAR."ClasificacionTipoInstrumento"
	,CAR."TipoValorizacion"
	,CAR."NombreBoveda"
	--,INS."IdMoneda" AS "IdMonedaInstrumento"
	,INS."FechaEmision" AS "FechaEmisionInstrumento"
	,CAR."INGMontoATasaCompraActual" AS "MontoCLP_Org"
	,CAR."IdUsuario"
	,CAR."TipoCarteraCorredor"
FROM 	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" INS
		ON (CAR."IdInstrumento" = INS."IdInstrumento" AND CAR."IdTipoReajuste" = INS."IdTipoReajuste")
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" AS CatIFRS
              ON (CAR."CodigoIFRS" = CatIFRS."CodigoCarteraIFRS")
WHERE 	(CAR."IdClasificacionTipoInstrumento" = 'IF' OR CAR."IdClasificacionTipoInstrumento" = 'RF') 
		AND CAR."IdTipoInstrumento" != 'PAC' AND (CASE WHEN CAR."EsFungible" = 'S' AND CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate" ELSE CAR."INGCantidad" END) >= 0

WITH READ ONLY;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraDisponibleInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


