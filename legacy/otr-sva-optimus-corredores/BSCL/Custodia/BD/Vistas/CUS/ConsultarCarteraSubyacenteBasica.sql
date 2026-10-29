SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"   AS 
SELECT 
     suby."IdSubyacente"                     	,
     suby."FolioSubyacente"						,  
	 suby."FolioCarteraRelacionada"			   	,  	     
	 suby."FechaIngresoRegistro" 				,
	 suby."FechaModificacionRegistro"			,
	 suby."IdCarteraRetirada"					,
	 origen."FolioCartera" as "FolioCarteraOrigen",
	 
	 -- OPERACION                           	,
	 suby."IdEstado" 							,
	 suby."IdTipoCanalIngreso"					,
	 CASE WHEN IFNULL(origen."IdMonedaSistema",'') <> '' THEN IFNULL(origen."IdMonedaSistema",'') ELSE IFNULL(conf."Valor",'CLP') END as "IdMonedaSistema",
	 origen."CodigoIFRS" as "CodigoIFRS",
	 
	 --INSTRUMENTO                           ,
	 suby."IdClasificacionTipoInstrumento" 		,
	 suby."IdTipoInstrumento"					,	 
	 TipoInst."Nombre" AS "NombreTipoInstrumento",
	 InstrumentoValorClasificacionRiesgo."ValorClasificacionRiesgo" as "car_ClasificacionRiesgo",
	 
	 suby."IdInstrumento" 						,
	 suby."Nemotecnico"							,
	 CASE WHEN coalesce(suby."IdEmisor",Inst."IdEmisor",'') <> '' THEN coalesce(suby."IdEmisor",Inst."IdEmisor",'') ELSE Inst."IdEmisor" END AS "IdEmisor",
	 suby."NemoSVS"	 							,
	 Inst."ISIN"								,
	 IFNULL(suby."IdTipoReajuste",'') as "IdTipoReajusteSubyacente",
	 suby."FechaEmisionInstrumento"				,
	 suby."FechaVencimientoInstrumento"			,
	 suby."EsFungible" 							,							
	 origen."IdBoveda",
	 CASE WHEN IFNULL(suby."IdMonedaInstrumento",'')<>'' THEN IFNULL(suby."IdMonedaInstrumento",'') ELSE COALESCE(Inst."IdMoneda",'') END AS "IdMonedaInstrumento",

	 --CANTIDADES Y MONTOS
	 suby."Tamano"                  ,
	 suby."Cantidad" 							,
	 suby."CorteMinimo"							,
	 suby."ValorRescate" 						,

	-- ***INGRESO***                        	,
	 suby."INGFechaOperacion" 					,
	 suby."INGCantidad"							,
	 suby."INGValorRescate" 					

	, suby."INGFechaValorizacionSubyacente"
	, suby."INGFechaValorizacionAnterior"
	, suby."INGFechaValorizacionActual"
	, suby."INGFechaValorizacionProximo"
	, suby."INGPrecioSubyacente"
	, suby."INGTasaSubyacente"
	, suby."INGMontoATasaSubyacenteAnterior"
	, suby."INGMontoInteresSubyacenteAnterior"
	, suby."INGMontoReajusteSubyacenteAnterior"
	, suby."INGMontoSubyacenteAnteriorMonedaInstrumento"
	, suby."INGMontoInteresSubyacenteAnteriorMonedaInstrumento"
	, suby."INGMontoReajusteSubyacenteAnteriorMonedaInstrumento"
	, suby."INGDurationSubyacenteAnterior"
	, suby."INGConvexidadSubyacenteAnterior"
	, suby."INGMontoSubyacenteAnteriorPibMas"
	, suby."INGMontoSubyacenteAnteriorPibMasMonedaInstrumento"
	, suby."INGMontoSubyacenteAnteriorPibMenos"
	, suby."INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento"
	, suby."INGMontoATasaSubyacenteActual"
	, suby."INGMontoSubyacenteActualMonedaInstrumento"
	, suby."INGMontoInteresCorridoSubyacenteActual"
	, suby."INGDurationSubyacenteActual"
	, suby."INGConvexidadSubyacenteActual"
	, suby."INGMontoSubyacenteActualPibMas"
	, suby."INGMontoSubyacenteActualPibMasMonedaInstrumento"
	, suby."INGMontoSubyacenteActualPibMenos"
	, suby."INGMontoSubyacenteActualPibMenosMonedaInstrumento"
	, suby."INGMontoATasaSubyacenteProximo"
	, suby."INGMontoInteresSubyacenteProximo"
	, suby."INGMontoReajusteSubyacenteProximo"
	, suby."INGMontoSubyacenteProximoMonedaInstrumento"
	, suby."INGMontoInteresSubyacenteProximoMonedaInstrumento"
	, suby."INGMontoReajusteSubyacenteProximoMonedaInstrumento"
	, suby."INGDurationSubyacenteProximo"
	, suby."INGConvexidadSubyacenteProximo"
	, suby."INGFechaPrecioTasaMercadoAnterior"
	, suby."INGFuenteTasaPrecioMercadoAnterior"
	, suby."INGPrecioMercadoAnterior"
	, suby."INGTasaMercadoAnterior"
	, suby."INGMontoATasaMercadoAnterior"
	, suby."INGMontoInteresMercadoAnterior"
	, suby."INGMontoReajusteMercadoAnterior"
	, suby."INGMontoMercadoAnteriorMonedaInstrumento"
	, suby."INGMontoInteresMercadoAnteriorMonedaInstrumento"
	, suby."INGMontoReajusteMercadoAnteriorMonedaInstrumento"
	, suby."INGDurationMercadoAnterior"
	, suby."INGConvexidadMercadoAnterior"
	, suby."INGMontoMercadoAnteriorPibMas"
	, suby."INGMontoMercadoAnteriorPibMasMonedaInstrumento"
	, suby."INGMontoMercadoAnteriorPibMenos"
	, suby."INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
	, suby."INGFechaPrecioTasaMercadoActual"
	, suby."INGFuenteTasaPrecioMercadoActual"
	, suby."INGPrecioMercadoActual"
	, suby."INGTasaMercadoActual"
	, suby."INGMontoATasaMercadoActual"
	, suby."INGMontoMercadoActualMonedaInstrumento"
	, suby."INGDurationMercadoActual"
	, suby."INGConvexidadMercadoActual"
	, suby."INGMontoMercadoActualPibMas"
	, suby."INGMontoMercadoActualPibMasMonedaInstrumento"
	, suby."INGMontoMercadoActualPibMenos"
	, suby."INGMontoMercadoActualPibMenosMonedaInstrumento"
	, suby."INGFechaPrecioTasaMercadoProximo"
	, suby."INGFuenteTasaPrecioMercadoProximo"
	, suby."INGPrecioMercadoProximo"
	, suby."INGTasaMercadoProximo"
	, suby."INGMontoATasaMercadoProximo"
	, suby."INGMontoInteresMercadoProximo"
	, suby."INGMontoReajusteMercadoProximo"
	, suby."INGMontoMercadoProximoMonedaInstrumento"
	, suby."INGMontoInteresMercadoProximoMonedaInstrumento"
	, suby."INGMontoReajusteMercadoProximoMonedaInstrumento"
	, suby."INGDurationMercadoProximo"
	, suby."INGConvexidadMercadoProximo"
	, suby."INGMontoATasaCompraAnterior"
	, suby."INGMontoInteresCompraAnterior"
	, suby."INGMontoReajusteCompraAnterior"
	, suby."INGMontoCompraAnteriorMonedaInstrumento"
	, suby."INGMontoInteresCompraAnteriorMonedaInstrumento"
	, suby."INGMontoReajusteCompraAnteriorMonedaInstrumento"
	, suby."INGDurationCompraAnterior"
	, suby."INGConvexidadCompraAnterior"
	, suby."INGMontoCompraAnteriorPibMas"
	, suby."INGMontoCompraAnteriorPibMasMonedaInstrumento"
	, suby."INGMontoCompraAnteriorPibMenos"
	, suby."INGMontoCompraAnteriorPibMenosMonedaInstrumento"
	, suby."INGMontoATasaCompraActual"
	, suby."INGMontoCompraActualMonedaInstrumento"
	, suby."INGDurationCompraActual"
	, suby."INGConvexidadCompraActual"
	, suby."INGMontoCompraActualPibMas"
	, suby."INGMontoCompraActualPibMasMonedaInstrumento"
	, suby."INGMontoCompraActualPibMenos"
	, suby."INGMontoCompraActualPibMenosMonedaInstrumento"
	, suby."INGMontoATasaCompraProximo"
	, suby."INGMontoInteresCompraProximo"
	, suby."INGMontoReajusteCompraProximo"
	, suby."INGMontoCompraProximoMonedaInstrumento"
	, suby."INGMontoInteresCompraProximoMonedaInstrumento"
	, suby."INGMontoReajusteCompraProximoMonedaInstrumento"
	, suby."INGDurationCompraProximo"
	, suby."INGConvexidadCompraProximo"
	, suby."INGTasaCorredora"
	, suby."INGFechaTasaCorredora"
	, suby."INGMontoATasaCorredora"
	, suby."INGMontoATasaCorredoraMonedaInstrumento"
	, suby."INGFechaCorteCupon"
	, suby."INGNumeroCupon"
	, suby."INGIdMonedaPagoCupon"
	, suby."INGMontoCorteCupon"
	, suby."INGFechaPagoCorteCupon"
	, suby."INGMontoCapitalCorteCupon"
	, suby."INGMontoInteresCorteCupon"
	, suby."INGMontoReajusteCorteCupon"
	, suby."INGMontoCorteCuponMonedaInstrumento"
	, suby."INGMontoCapitalCorteCuponMonedaInstrumento"
	, suby."INGMontoInteresCorteCuponMonedaInstrumento"
	, suby."INGMontoReajusteCorteCuponMonedaInstrumento"
	, suby."INGMontoPresenteTIF"
	, suby."INGMontoInteresTIF"
	, suby."INGMontoReajusteTIF"
	, suby."INGMontoInteresAcumTIF"
	, suby."INGMontoReajusteAcumTIF"
	, suby."INGMontoPresenteTIFMonedaInstrumento"
	, suby."INGMontoInteresTIFMonedaInstrumento"
	, suby."INGMontoReajusteTIFMonedaInstrumento"
	, suby."INGMontoInteresAcumTIFMonedaInstrumento"
	, suby."INGMontoReajusteAcumTIFMonedaInstrumento" 
	, suby."EGRFechaOperacion"
	, suby."EGRIdTipoCanalIngreso"
	, suby."EGRIdCondicionLiquidacion"
	, suby."EGRFechaLiquidacion"
	, suby."EGRIdTipoComprobante"
	, suby."EGRFolioComprobante"
	, suby."EGRFechaComprobante"
	, suby."EGRFolioExternoReferencia"
	, suby."EGRIdModulo"
	, suby."EGRIdConcepto"
	, suby."EGRIdUsuario"
	, suby."EGRIdSucursal"
	, suby."EGRIdAgente"
	, suby."EGRCantidad"
	, suby."EGRValorRescate"
	, suby."EGRPrecio"
	, suby."EGRTasa"
	, suby."EGRMonto"
	, suby."EGRMontoMonedaInstrumento"
	, suby."EGRSpread"
	, suby."EGRMontoPresenteTIF"
	, suby."EGRMontoPresenteTIFMonedaInstrumento"
	, suby."EGRMontoPresenteTIFMonedaAdicional"
	, suby."PrecioCompraOperacionRelacionada"
	, suby."PrecioPromedioCompraOperacionRelacionada"
	, suby."TasaCompraOperacionRelacionada"
	, suby."MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada"
	, suby."MontoPrecioPromedioCompraOperacionRelacionada"
	, suby."MontoPrecioPromedioMonedaAdicionalOperacionRelacionada"
	, CASE WHEN IFNULL(suby."IdMonedaEmision",'')<>'' THEN IFNULL(suby."IdMonedaEmision",'') ELSE COALESCE(Inst."IdMoneda",'') END AS "IdMonedaEmision"
	, IFNULL(origen2."MontoAPlazo",0) AS "MontoAPlazo"						
FROM "BSCL.Custodia.BD::CUS.Subyacente" as suby
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Inst 
	ON suby."IdInstrumento" = Inst."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInst 
	ON TipoInst."IdTipoInstrumento" = Inst."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR 
	ON TICR."IdTipoInstrumento" = Inst."IdTipoInstrumento" 
	AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS InstrumentoValorClasificacionRiesgo 
	ON InstrumentoValorClasificacionRiesgo."IdInstrumento" = Inst."IdInstrumento"
	AND InstrumentoValorClasificacionRiesgo."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as origen 
	ON (origen."IdCartera" = suby."IdCarteraRetirada")
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as origen2 
	ON (origen2."FolioCartera" = suby."FolioCarteraRelacionada" AND origen2."IdEstado" <> 'MOD'  AND origen2."IdEstado" <> 'RNV' AND origen2."IdEstado" <> 'ANT')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = origen."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
ORDER BY "IdSubyacente" ASC

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO