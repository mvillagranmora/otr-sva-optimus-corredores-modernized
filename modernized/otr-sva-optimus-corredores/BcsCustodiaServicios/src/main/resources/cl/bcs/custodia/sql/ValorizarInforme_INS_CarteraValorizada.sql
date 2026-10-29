INSERT INTO "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF"
(
	"IdValorizacion" 
	,"IdInforme"    
	,"FolioCartera"  
	,"IdCartera"  
	,"IdEmpresa"
	/* Operacion -------------------*/ 
	,"IdCliente"  
	,"IdPortafolio"  
	,"IdTipoOperacion"
	,"IdOperacion" 
	,"FechaOperacion"  
	,"FechaVencimientoOperacion"  
	,"PlazoEnDiasOperacion"  
	,"IdCondicionLiquidacion"
	,"FechaLiquidacion"  
	,"Diferenciador"
	/* Instrumento -----------------*/ 
	,"IdClasificacionTipoInstrumento"  
	,"IdTipoInstrumento"  
	,"IdInstrumento"  
	,"Emisor"  
	,"Nemotecnico"  	
	,"NemoSVS"  
	--,"NemoOriginal"  			
	,"IdTipoReajuste"  
	,"IdMonedaInstrumento"  
	--,"IdMonedaEmision"  	
	,"IdMonedaAdicional"  	
	,"FechaEmisionInstrumento"  
	,"FechaVencimientoInstrumento"
	,"TieneCorteDeCupon"	
	,"EsFungible"  	  
	,"CodigoIFRS"
	/* Valor Actual ---------------------*/
	,"Cantidad"  
	,"ValorRescate"  
	,"TasaCompra"  
	,"PrecioCompra"  	
	,"MontoCompra" 
	,"MontoCompraMonedaInstrumento"
	--,"MontoCompraMonedaSistema" 	  
	/* Valorizacion Fecha Inicio-Consulta */
	,"FechaValorizacionActual"  
	,"PlazoOperacionActual"  
	,"FechaFactorMonedaInstrumentoActual"  	
	,"ValorFactorMonedaInstrumentoActual"  	
	,"FechaFactorMonedaAdicionalActual"  
	,"ValorFactorMonedaAdicionalActual"  
	,"TasaActual"  
	,"PrecioActual"  	
	,"MontoActual"  
	,"MontoActualMonedaInstrumento" 	
	--,"MontoaActualMonedaAdicional" 	
	,"FechaPrecioTasaMercadoActual"  
	,"FuenteTasaPrecioMercadoActual"  
	/* Valorizacion Fecha Fin-Hasta -----*/	
	,"FechaValorizacionProximo"  
	,"PlazoOperacionProximo"  	
	,"FechaFactorMonedaInstrumentoProximo"  	
	,"ValorFactorMonedaInstrumentoProximo"  
	,"FechaFactorMonedaAdicionalProximo"  
	,"ValorFactorMonedaAdicionalProximo"  
	,"TasaProximo"  
	,"PrecioProximo"  	
	,"MontoProximo"  
	,"MontoProximoMonedaInstrumento" 	
	,"MontoProximoMonedaAdicional" 	
	,"FechaPrecioTasaMercadoProximo"  
	,"FuenteTasaPrecioMercadoProximo"
	/* Intereses y Reajustes entre Valorizaciones */
	,"MontoInteres"  
	,"MontoReajuste"  
	,"MontoInteresMonedaInstrumento"  
	,"MontoReajusteMonedaInstrumento"  
	,"MontoInteresMonedaAdicional"  
	,"MontoReajusteMonedaAdicional"  
	/* Intereses y Reajustes Acumulado (F.Operacion - F.Fin) */
	,"MontoInteresCorrido"  
	,"MontoReajusteCorrido"  
	,"MontoInteresCorridoMonedaInstrumento"  
	,"MontoReajusteCorridoMonedaInstrumento"  
	,"MontoInteresCorridoMonedaAdicional"  
	,"MontoReajusteCorridoMonedaAdicional" 	
	/*MAME-------------------------------------- */
	,"MayorValorMonedaSistema"
	,"MayorValorMonedaInstrumento"
	,"MayorValorMonedaAdicional"
	,"MenorValorMonedaSistema"
	,"MenorValorMonedaInstrumento"
	,"MenorValorMonedaAdicional"
	/* Corte Cupon ----------------------------- */
	,"FechaCorteCupon"
	,"FechaPagoCorteCupon"	
	,"NumeroCupon"
	,"IdMonedaPagoCupon"
	,"MontoCorteCupon"
	,"MontoInteresCorteCupon"
	,"MontoReajusteCorteCupon"
	,"MontoCorteCuponMonedaInstrumento"
	,"MontoInteresCorteCuponMonedaInstrumento"
	,"MontoReajusteCorteCuponMonedaInstrumento"
	/* Otros ----------------------------------- */
	,"Duration"  
	,"Convexidad" 
	/* Parametros ----------------------------- */
	,"prmTipoInforme"   	
	,"prmTipoValorizacion"   
	,"prmUsuario"
	--,"FechaEmisionReporte"+
	,"IdConcepto"
	,"IdBoveda"
	,"IdAgente"
	,"MontoPactado"
	,"IdCorredoraContraparte"
	,"FolioComprobante"
	,"TasaAPlazo"
	,"PrecioAPlazo"
	,"MontoAPlazo"
	,"MontoAPlazoMonedaAdicional"
	,"MontoAPlazoMonedaInstrumento"
	,"IdMesa"
	,"IdBolsa"
	,"TipoCarteraCorredor"
	,"MontoCompraMonedaSistema"
	,"FolioTransaccionNormal"
	,"FolioTransaccionPlazo"
	,"IdTipoProducto"
	,"FolioOperacion"
	,"IdClienteSolicitante"
)
VALUES 
( 
	COALESCE(:IdValorizacion, 0)
	,COALESCE(:IdInforme, '')   
	,COALESCE(:FolioCartera, 0) 
	,COALESCE(:IdCartera, 0)
	,COALESCE(:IdEmpresa, 1)
	/* Operacion -------------------*/
	,COALESCE(:IdCliente, 0) 
	,COALESCE(:IdPortafolio, 0) 
	,COALESCE(:IdTipoOperacion, '')
	,COALESCE(:IdOperacion, 0)
	,COALESCE(:FechaOperacion,'0001-01-01') 
	,COALESCE(:FechaVencimientoOperacion,'0001-01-01') 
	,COALESCE(:PlazoEnDiasOperacion, 0) 
	,COALESCE(:IdCondicionLiquidacion, '')
	,COALESCE(:FechaLiquidacion,'0001-01-01')
	,COALESCE(:Diferenciador,'')
 	/* Instrumento -----------------*/
	,COALESCE(:IdClasificacionTipoInstrumento, '') 
	,COALESCE(:IdTipoInstrumento, '')
	,COALESCE(:IdInstrumento, 0) 
	,COALESCE(:Emisor, '') 
	,COALESCE(:Nemotecnico, '') 	
	,COALESCE(:NemoSVS, '') 
	--,COALESCE(:NemoOriginal, '') 			
	,COALESCE(:IdTipoReajuste, '') 
	,COALESCE(:IdMonedaInstrumento, '') 
	--,COALESCE(:IdMonedaEmision, '') 	
	,COALESCE(:IdMonedaAdicional, '') 	
	,COALESCE(:FechaEmisionInstrumento,'0001-01-01') 
	,COALESCE(:FechaVencimientoInstrumento,'0001-01-01')
	,COALESCE(:TieneCorteCupon, '')	
	,COALESCE(:EsFungible, '') 
	,COALESCE(:CodigoIFRS, '')
	/* Valor Actual ---------------------*/
	,COALESCE(:Cantidad, 0.0) 
	,COALESCE(:ValorRescate, 0.0) 
	,COALESCE(:TasaCompra, 0.0)
	,COALESCE(:PrecioCompra, 0.0) 	
	,COALESCE(:MontoCompra, 0.0)
	,COALESCE(:MontoCompraMonedaInstrumento, 0.0)	
	--,COALESCE(:MontoCompraMonedaSistema, 0.0)	  --??????
	/* Valorizacion Fecha Inicio-Consulta */
	,COALESCE(:FechaValorizacionActual,'0001-01-01') 
	,COALESCE(:PlazoOperacionActual, 0) 
	,COALESCE(:FechaFactorMonedaInstrumentoActual,'0001-01-01') 	
	,COALESCE(:ValorFactorMonedaInstrumentoActual, 0.0) 	
	,COALESCE(:FechaFactorMonedaAdicionalActual,'0001-01-01') 
	,COALESCE(:ValorFactorMonedaAdicionalActual , 0.0)
	,COALESCE(:TasaActual, 0.0)
	,COALESCE(:PrecioActual, 0.0)
	,COALESCE(:MontoActual, 0.0) 
	,COALESCE(:MontoActualMonedaInstrumento, 0.0)	
	--,COALESCE(:MontoaActualMonedaAdicional, 0.0)	
	,COALESCE(:FechaPrecioTasaMercadoActual,'0001-01-01') 
	,COALESCE(:FuenteTasaPrecioMercadoActual, '') 	
	/* Valorizacion Fecha Fin-Hasta -----*/
	,COALESCE(:FechaValorizacionProximo,'0001-01-01') 
	,COALESCE(:PlazoOperacionProximo, 0) 	
	,COALESCE(:FechaFactorMonedaInstrumentoProximo ,'0001-01-01')	
	,COALESCE(:ValorFactorMonedaInstrumentoProximo, 0.0) 
	,COALESCE(:FechaFactorMonedaAdicionalProximo,'0001-01-01') 
	,COALESCE(:ValorFactorMonedaAdicionalProximo, 0.0) 
	,COALESCE(:TasaProximo, 0.0) 
	,COALESCE(:PrecioProximo, 0.0) 	
	,COALESCE(:MontoProximo, 0.0) 
	,COALESCE(:MontoProximoMonedaInstrumento, 0.0)	
	,COALESCE(:MontoProximoMonedaAdicional, 0.0)	
	,COALESCE(:FechaPrecioTasaMercadoProximo,'0001-01-01') 
	,COALESCE(:FuenteTasaPrecioMercadoProximo, '')
	/* Intereses y Reajustes entre Valorizaciones */
	,COALESCE(:MontoInteres, 0.0)
	,COALESCE(:MontoReajuste, 0.0) 
	,COALESCE(:MontoInteresMonedaInstrumento, 0.0) 
	,COALESCE(:MontoReajusteMonedaInstrumento, 0.0) 
	,COALESCE(:MontoInteresMonedaAdicional, 0.0) 
	,COALESCE(:MontoReajusteMonedaAdicional, 0.0) 
	/* Intereses y Reajustes Acumulado (F.Operacion - F.Fin) */
	,COALESCE(:MontoInteresCorrido, 0.0) 
	,COALESCE(:MontoReajusteCorrido, 0.0) 
	,COALESCE(:MontoInteresCorridoMonedaInstrumento, 0.0) 
	,COALESCE(:MontoReajusteCorridoMonedaInstrumento, 0.0) 
	,COALESCE(:MontoInteresCorridoMonedaAdicional, 0.0) 
	,COALESCE(:MontoReajusteCorridoMonedaAdicional, 0.0) 	
	/*MAME-------------------------------------- */
	,COALESCE(:MayorValorMonedaSistema, 0.0)
	,COALESCE(:MayorValorMonedaInstrumento, 0.0)
	,COALESCE(:MayorValorMonedaAdicional, 0.0)
	,COALESCE(:MenorValorMonedaSistema, 0.0)
	,COALESCE(:MenorValorMonedaInstrumento, 0.0)
	,COALESCE(:MenorValorMonedaAdicional, 0.0)
	/* Corte Cupon */
	,COALESCE(:FechaCorteCupon,'0001-01-01')
	,COALESCE(:FechaPagoCorteCupon,'0001-01-01')	
	,COALESCE(:NumeroCupon, 0.0)
	,COALESCE(:IdMonedaPagoCupon, '')
	,COALESCE(:MontoCorteCupon, 0.0)
	,COALESCE(:MontoInteresCorteCupon, 0.0)
	,COALESCE(:MontoReajusteCorteCupon, 0.0)
	,COALESCE(:MontoCorteCuponMonedaInstrumento, 0.0)
	,COALESCE(:MontoInteresCorteCuponMonedaInstrumento, 0.0)
	,COALESCE(:MontoReajusteCorteCuponMonedaInstrumento, 0.0)
	/* Otros ----------------------------------- */
	,COALESCE(:Duration, 0.0)
	,COALESCE(:Convexidad, 0.0)
	/* Parametros del informe ------------------ */
	,COALESCE(:prmTipoInforme, '')  	
	,COALESCE(:prmTipoValorizacion, '')  
	,COALESCE(:prmUsuario, 0)
	--,COALESCE(:FechaEmisionReporte,'0001-01-01')
	,COALESCE(:IdConcepto, '')
	,COALESCE(:IdBoveda, '')
	,COALESCE(:IdAgente, 0)
	,COALESCE(:MontoPactado, 0.0)
	,COALESCE(:IdCorredoraContraparte, 0)
	,COALESCE(:FolioComprobante, 0)
	,COALESCE(:TasaAPlazo,0.0)
	,COALESCE(:PrecioAPlazo,0.0)
	,COALESCE(:MontoAPlazo,0.0)
	,COALESCE(:MontoAPlazoMonedaAdicional,0.0)
	,COALESCE(:MontoAPlazoMonedaInstrumento,0.0)
	,COALESCE(:IdMesa, 0)
	,COALESCE(:IdBolsa, '')
	,COALESCE(:TipoCarteraCorredor, '')
	,COALESCE(:MontoCompraMonedaSistema, 0.0)
	,COALESCE(:FolioTransaccionNormal, '')
	,COALESCE(:FolioTransaccionPlazo, '')
	,COALESCE(:IdTipoProducto, '')
	,COALESCE(:FolioOperacion, 0)
	,COALESCE(:IdClienteSolicitante, 0)
)