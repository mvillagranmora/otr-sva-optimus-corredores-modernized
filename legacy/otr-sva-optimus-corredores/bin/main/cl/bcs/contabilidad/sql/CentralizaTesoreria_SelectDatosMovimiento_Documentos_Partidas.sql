SELECT DISTINCT
	 ComprobanteFormaDePago."IdComprobanteFormaDePago"*-1 		AS "IdMovimiento"
	,TipoMovimiento."Nombre" 					AS "TipoDocumento"
	,Comprobante."IdComprobante"							AS "Folio"
	,YEAR(ComprobanteFormaDePago."FechaMovimiento") ||'-'|| RIGHT('0' || MONTH(ComprobanteFormaDePago."FechaMovimiento"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(ComprobanteFormaDePago."FechaMovimiento"),2) AS "FechaDocumento"
	,LEFT("NombreCuentaBancariaCorredora",60)				AS "NombreCuentaCorriente"   
	,LEFT(ComprobanteFormaDePago."NumeroCuentaBancariaCorredora",60) AS "NumeroCuentaCorriente"   
	,ComprobanteFormaDePago."IdMedioDePago"					AS "MedioDePago"                                          
	,LEFT("NumeroDocumentoCorredora",60)					AS "NumeroDocumentoPago"                                
	,ComprobanteFormaDePago."FechaLiquidacion"				AS "FechaLiquidacion"                                        
	,"FechaDocumentoPagoCorredora"							AS "FechaDocumentoPago"                                      
	,IFNULL(ComprobanteFormaDePago."NumeroSerieCorredora",'') AS "SerieDocumento"                                            
	,ComprobanteFormaDePago."IdConcepto"					AS "Concepto"    
	,LEFT(ConsultarConcepto."Nombre",60) 					AS "NombreConcepto"
	,LEFT(Comprobante."DocumentoIdentidad",60)				AS "DocumentoIdentidad"                                    
	,LEFT(Comprobante."NombrePersonaTitularPago",60)		AS "NombreCliente"                                       
	,LEFT(ConsultarPortafolio."CodigoPortafolio",60)		AS "Portafolio"
	,Comprobante."IdMoneda"									AS "Moneda"                                                      
	,caja."Codigo"								AS "CodigoCaja"
	,LEFT(Comprobante."NombrePersona",60)					AS "EmitidoA" 
	
	                                           
	,CASE IFNULL(Comprobante."EsBilateral",'N') WHEN 'N' THEN 'NO' ELSE 'SI' END AS "EsBilateral"                                                 
	,IFNULL(ConsultarPersona."Tipo",'')						AS "TipoPersona"
	,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')			AS "Agente"
	,LEFT(ConsultarEjecutivo."NombreCompleto",60) 			AS "NombreAgente"
	,ConsultarEjecutivo."CodigoSucursal" 					AS "SucursalAgente"
	,LEFT(ConsultarEjecutivo."NombreSucursal",60) 			AS "NombreSucursalAgente"
	,IFNULL(LEFT(ConsultarIndicadoresEmpleado."ValorIndicador",60),'')	AS "CentroCostoAgente"
	,:FechaSistema										AS "FechaProceso"
	,CASE WHEN ComprobanteFormaDePago."FechaLiquidacion" = Comprobante."FechaMovimiento" THEN 'TECOMP' ELSE 'TELIGA' END AS "Proceso"
	,CASE Comprobante."IdTipoMovimiento" WHEN 'MOVING' THEN CASE WHEN Comprobante."ComprobanteNeteado" = 'NO' THEN 'I' ELSE 'TI' END
		  ELSE CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'E' ELSE 'TE' END
	END														AS "TipoComprobante"
	,CASE WHEN ComprobanteFormaDePago."FechaLiquidacion" = Comprobante."FechaMovimiento" THEN 'N' ELSE 'S' END AS "Valuta"
	,CASE WHEN ComprobanteFormaDePago."IdMedioDePago" = 'EFE' THEN 'EF' ELSE 'DO' END AS "PartidaLiquidacion"
	,Comprobante."IdComprobante"							AS "NumeroComprobante"
	,COALESCE(TRIM(JAN."NombreCuentaBancaria"),'')					AS "AliasBancoCaja"
	,ComprobanteFormaDePago."Monto"							AS "Monto"
	,''	AS "TipoComprobanteAdj"
	--,0	AS "FolioComprobanteAdj"
	,ComprobanteFormaDePago."IdComprobanteFormaDePago"		AS "FolioOperacion"
	,IngresoEgreso."IdTipoProducto"							AS "CodigoProducto"
	,IngresoEgreso."Nemotecnico"							AS "Nemotecnico"
	,ComprobanteFormaDePago."NombreBancoCorredora"			AS "Emisor"
	,CASE WHEN Comprobante."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
		  WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 						THEN 'RE'
		  ELSE 'NR'
	END 													AS "Relacion"
	,CASE ConsultarComprobanteDetalle."IdMonedaOperacion" WHEN '' THEN	ComprobanteFormaDePago."Monto" ELSE ConsultarComprobanteDetalle."MontoOperacion" END AS "Valor"
	,ConsultarComprobanteDetalle."ValorParidad"				AS "TipoCambio"
	,CASE 
	   WHEN (ConsultarSocioDeNegocio."IdContraparte" > 0 AND IngresoEgreso."IdTipoPersona" = 'CNT') THEN 'SI'
	 ELSE 'NO' 
	 END 									                AS "EsContraparte"
	,IngresoEgreso."IdBolsa"								AS "Bolsa"
	,CASE Comprobante."IdTipoMovimiento" WHEN 'MOVING' 
		THEN	'ING' 
		ELSE  	'EGR' END									AS "ComprobanteTipo"			
	,Comprobante."FechaMovimiento"							AS "FechaMovimiento"
	, CASE WHEN ConsultarComprobanteDetalle."IdTipoOperacion" = 'COMSPT' AND FACT."IdMoneda" <> 'CLP' THEN FACT."Monto" ELSE ConsultarComprobanteDetalle."ValorParidad" * ConsultarComprobanteDetalle."Monto" END AS "MontoMonedaSistema"
	, DocInd."DocumentoIdentidad" 							AS "DocumentoIdentidadCAVALI"	 
	,CASE WHEN LEFT(COALESCE(ComprobanteFormaDePago."NumeroDocumentoCorredora",0),60) > 0 THEN ComprobanteFormaDePago."NumeroDocumentoCorredora" ELSE  ComprobanteFormaDePago."IdComprobanteFormaDePago" END	AS "Referencia3"
--	,Comprobante."CodigoIFRS"							AS "CodigoIFRS"
	,CASE Comprobante."IdTipoPersona" WHEN 'PRO' THEN 'SI' ELSE 'NO' END "EsProveedor"
	,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') 	AS "ClienteBancario"
	,CASE WHEN Comprobante."IdCondicionDeLiquidacion" IS NULL OR Comprobante."IdCondicionDeLiquidacion" = '' THEN 'T0' ELSE Comprobante."IdCondicionDeLiquidacion" END AS "CondicionLiquidacion"
	,COALESCE(FACT."FolioComprobante", 0) AS "FolioDTE"
	FROM "BSCL.Tesoreria.BD::TES.Comprobante" 			AS	Comprobante
	INNER JOIN "BSCL.Contabilidad.BD::GBL.CentralizaTesoreria_ComprobantesAsociados" ASOCIADO ON Comprobante."IdComprobante" = ASOCIADO."ComprobantesAsociados"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TipoMovimiento ON (TipoMovimiento."IdModulo" = 'TES' and  TipoMovimiento."IdTipoMovimiento" = Comprobante."IdTipoMovimiento")
LEFT JOIN "BSCL.Tesoreria.BD::TES.Caja" AS Caja ON Caja."IdCaja" = Comprobante."IdCaja"

LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso"			AS  IngresoEgreso
ON (IngresoEgreso."IdComprobante" = Comprobante."IdComprobante"
	AND IngresoEgreso."IdEmpresa" 	= Comprobante."IdEmpresa"
	AND IngresoEgreso."IdEstado" IN ('ANU','EMI'))
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" 	AS	ComprobanteFormaDePago 
ON (ComprobanteFormaDePago."IdEmpresa" 		= Comprobante."IdEmpresa" 		AND
	ComprobanteFormaDePago."IdComprobante" 	= Comprobante."IdComprobante" 	AND 
	ComprobanteFormaDePago."IdEstado" IN ('LIQ','GIR'))
LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalle"  ConsultarComprobanteDetalle
		ON  ConsultarComprobanteDetalle."IdComprobante" = Comprobante."IdComprobante" AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ','ING')	
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			AS	ConsultarEmpresa
ON (ConsultarEmpresa."IdEmpresa" = Comprobante."IdEmpresa")

LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" ConsultarPortafolio 
ON (ConsultarPortafolio."IdPortafolio" 	= Comprobante."IdPortafolioTitularPago")
	
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
ON (ConsultarEjecutivo."IdEmpresa" 				= Comprobante."IdEmpresa"	and
	ConsultarEjecutivo."IdEmpleadoEjecutivo" 	= ConsultarPortafolio."IdEmpleadoEjecutivo")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
ON (ConsultarIndicadoresEmpleado."IdEmpresa" = 	ConsultarEjecutivo."IdEmpresa"				AND
	ConsultarIndicadoresEmpleado."IdEmpleado"=  ConsultarEjecutivo."IdEmpleadoEjecutivo" 	AND 
	ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO')
	
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" ConsultarConcepto
ON (ConsultarConcepto."IdConcepto" = Comprobante."IdConcepto")

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" ConsultarPersona
ON (ConsultarPersona."IdPersona" = Comprobante."IdPersona")

LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 				AS DocInd 
ON DocInd."IdPersona" 					= Comprobante."IdPersona"   
AND DocInd."IdTipoDocumentoIdentidad" 	= 'RUT'

 	
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
ON (ConsultarIndicadoresCliente."IdEmpresa"	= ConsultarIndicadoresEmpleado."IdEmpresa"	AND
	ConsultarIndicadoresCliente."IdPersona" =  ConsultarPersona."IdPersona" 			AND 
	(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
	(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" BAN
ON (ComprobanteFormaDePago."IdBancoCorredora" = BAN."IdBanco" AND BAN."IdEmpresa" = Comprobante."IdEmpresa")	
--- jannis
LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco"	AS JAN
ON (BAN."IdBanco" = JAN."IdBanco" AND ComprobanteFormaDePago."IdCuentaBancariaCorredora" = JAN."IdCuentaBanco")

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" ConsultarSocioDeNegocio
ON (ConsultarSocioDeNegocio."IdEmpresa"	= Comprobante."IdEmpresa"	AND
	ConsultarSocioDeNegocio."IdPersona" =  ConsultarPersona."IdPersona")
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FACT ON (ConsultarComprobanteDetalle."IdComprobanteOrigen" = FACT."IdComprobante")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario
	ON IndClienteBancario."IdCliente" =  ConsultarSocioDeNegocio."IdCliente" AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'	
WHERE 
	Comprobante."IdEmpresa"	= :IdEmpresa			AND 
	Comprobante."IdEstado" IN ('LIQ','GIR')			AND  
	ComprobanteFormaDePago."FechaLiquidacion" = :FechaContable ;