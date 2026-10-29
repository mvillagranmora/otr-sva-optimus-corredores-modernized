SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea";


CREATE VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS 


WITH MovimientoComprobanteUnico AS
(   -- Las comprobantes de facturas de simultaneas solo consideran 1 detalle (en sebra no, por lo que se aplica max para que no se dupliquen registros)
	SELECT "IdComprobante","NemotecnicoSubyacente",  MAX("IdEmpresa") AS "IdEmpresa", MAX("IdMovimientoComprobante") AS "IdMovimientoComprobante"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple"
	GROUP BY "IdComprobante", "NemotecnicoSubyacente"
)

SELECT
		 CSDN."DocumentoIdentidad"
		,CSDN."NombreCompleto" as "NombreLargoCliente"
		,(CSDN."DocumentoIdentidad" || ' ' || CSDN."NombreCompleto") as "Cliente"
		,CP."CodigoPortafolio"
		,CP."Proposito"
		,(CP."CodigoPortafolio" || ' ' || CP."Proposito") as "Portafolio"
		
		,TipOpe."Nombre" as "NombreOperacion"
		,TipOpe."Accion" as "AccionOperacion"
		,TipOpeContraria."Accion" || ' A PLAZO' as "AccionOperacionPlazo"
		,TipOpe."IdTipoProducto" 
		,CondLiq."NombreLargo"
		,Car."IdTipoComprobante"
		,Car."FolioComprobante"
		,Car."FechaComprobante"
		,Car."IdCartera"
		,Car."IdEmpresa"
		,Car."IdCliente"
		,Car."IdAgente"
		,Car."IdSucursal"
		,Car."IdPortafolio"
		,Car."IdBolsa"
		,MovNormal."IdCorredoraContraparte"
		,MovNormal."FechaLiquidacion" as "FechaLiquidacionMovtoContado"
		,IFNULL(Corred."NombreCompleto",'') as "NombreCorredora"
		,IFNULL(ejec."NombreCompleto",'') as "NombreEjecutivo"
		
		,REPLACE(IFNULL(MovNormal."FolioTransaccion", '0'), ' ', '') as "PaloNormal"
		,REPLACE(IFNULL(MovPlazo."FolioTransaccion", '0'), ' ', '') as "PaloPlazo"
		,Car."FolioCartera"
		,Car."FolioCarteraRelacionada"
		,Car."IdTipoInstrumento" as "IdTipoInstrumentoOperacion"
		,Car."IdInstrumento" as "IdInstrumentoOperacion"
		,Car."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoOperacion"
		,Car."Nemotecnico" AS "NemotecnicoOperacion"
		,Car."IdTipoOperacion"
		,Car."IdBoveda"
		,Car."IdCuentaCustodioPortafolio"
        ,Car."IdOperacion"

		,CASE WHEN IFNULL(Car."IdMonedaSistema",'')<>'' THEN IFNULL(Car."IdMonedaSistema",'') ELSE conf."Valor" END AS "IdMonedaSistema" 
		,Car."IdMonedaInstrumento"
		,Car."FechaOperacion" as "FechaInversion" 
		,Car."FechaVencimientoOperacion"
		,Car."IdCondicionLiquidacion"

		,Car."FechaLiquidacion"		
		,Car."INGCantidad" * Car."PrecioCompra" AS "MontoCompra"--,Car."MontoCompra"
		,Car."PrecioCompra" as "PrecioInicial"
		
		
		
	    -- Precio y Monto a Plazo de obtiene de la factura a plazo, de esta forma calza el monto de la factura con lo que se env�a a cta inversion (esta logica esta alineada con el vencimiento de simultaneas)   
	    --,COALESCE(MovPlazo."Precio",0) as "PrecioAPlazo"
		, car."PrecioAPlazo"
		/*,CASE
			WHEN Car."INGCantidad" <> MovPlazo."Cantidad" THEN  Car."INGCantidad" * coalesce(MovPlazo."Precio",0)
			ELSE MovPlazo."Monto"
		END  AS "MontoAPlazo"
		*/
		,CASE
			WHEN Car."IdEstado" ='VCD' AND Car."Cantidad" = Car."EGRCantidad" AND COALESCE(MovNormal."MontoAPlazo", 0) <> 0 THEN MovNormal."MontoAPlazo"
			WHEN Car."IdEstado" ='VCD' AND Car."Cantidad" <> Car."EGRCantidad" THEN Car."INGCantidad" * COALESCE(MovPlazo."Precio",0)
			ELSE Car."INGMontoAPlazo"
		END AS "MontoAPlazo"
		,Car."PlazoEnDiasOperacion"
		,Car."INGGlosa"		   				 		
		,Car."INGFechaOperacion" 					
		,Car."INGFechaLiquidacion" 					
		,Car."INGCantidad"							
		,Car."INGValorRescate" 				
		,Car."INGTasaAgente" 	
		,Car."INGMontoAPlazo" 	
		,Car."INGPrecioAPlazo" 	
		,Car."INGPrecioCompra"				
		,Car."INGTasaCompra"
		,Car."INGMontoATasaCompraActual"
--		,Car."INGPrecioMercadoActual"				
		,Car."INGTasaMercadoActual" 				
		,Car."INGMontoATasaMercadoActual" 			
		,Car."INGMontoCorteCupon"					
		,Car."INGFechaCorteCupon"					
		,Car."IdEstado"
		,ESTADO."Nombre" AS "NombreEstado"
		,Car."INGMontoInteresCompraAnterior"
		,Car."INGMontoInteresCompraAnteriorMonedaInstrumento"
		,Car."INGMontoInteresCompraAnteriorMonedaAdicional"
		,Car."INGMontoInteresCorridoCompraActual"
		,Car."INGMontoInteresCorridoCompraActualMonedaInstrumento"
		,Car."INGMontoInteresCorridoCompraActualMonedaAdicional"
		,Car."INGMontoInteresCompraProximo"
		,Car."INGMontoInteresCompraProximoMonedaInstrumento"
		,Car."INGMontoInteresCompraProximoMonedaAdicional"
		,Car."INGMontoInteresMercadoAnterior"
		,Car."INGMontoInteresMercadoAnteriorMonedaInstrumento"
		,Car."INGMontoInteresMercadoAnteriorMonedaAdicional"
		,Car."INGMontoInteresCorridoMercadoActual"
		,Car."INGMontoInteresCorridoMercadoActualMonedaInstrumento"
		,Car."INGMontoInteresCorridoMercadoActualMonedaAdicional"
		,Car."INGMontoInteresMercadoProximo"
		,Car."INGMontoInteresMercadoProximoMonedaInstrumento"
		,Car."INGMontoInteresMercadoProximoMonedaAdicional"
		, CAST((Car."INGMontoATasaCompraActual" / Car."INGCantidad") AS DECIMAL(26,8)) as "PrecioActualCalculado"
		
		-----------------05/07/2016 - CAMPOS AGREGADOS PARA CONSULTA DE ANTICIPOS-------------------
		,Car."EGRGlosa"
		,Car."EGRFechaOperacion"
		,Car."EGRIdTipoOperacion"
		,Car."EGRIdOperacion"
		,Car."EGRIdMovimientoCarteraEnTransito"
		,Car."EGRIdTipoCanalIngreso"
		,Car."EGRIdCondicionLiquidacion"
		,Car."EGRFechaLiquidacion"
		,Car."EGRIdTipoComprobante"
		,Car."EGRFolioComprobante"
		,Car."EGRFechaComprobante"
		,Car."EGRFolioExternoReferencia"
		,Car."EGRIdModulo"
		,Car."EGRIdConcepto"
		,Car."EGRIdUsuario"
		,Car."EGRIdSucursal"
		,Car."EGRIdAgente"
		,Car."EGRFechaCurseRetiro"
		,Car."EGRCostoRetiroenFactorAjuste"
		,Car."EGRValorFactorAjusteaFechaRetiro"
		,Car."EGRCantidad"
		,Car."EGRValorRescate"
		,Car."EGRPrecio"
		,Car."EGRTasa"
		,Car."EGRMonto"
		,Car."EGRMontoMonedaInstrumento"
		,Car."EGRSpread"
		,Car."EGRPrecioAPlazo"
		,Car."EGRMontoAPlazo"
		,Car."EGRMontoAPlazoMonedaInstrumento"
		,Car."EGRMontoMonedaPrincipal"
		,Car."EGRMontoMonedaSecundaria"
		,Car."EGRIdMonedaLiquidacion"
		,Car."EGRMontoLiquidacion"
		,Car."EGRMontoLiquidacionMonedaPrincipal"
		,Car."EGRMontoARecibirAnticipo"
		
		,CASE WHEN Car."EGRIdTipoOperacion" IN ('ANSIMC','ANSIMV') AND Car."EGRIdConcepto" IN ('ANTSIC','ANTSIV') AND Car."IdEstado" IN ('ANT') THEN COALESCE(Car."EGRMontoARecibirAnticipo",0)
			  ELSE COALESCE(Car."INGMontoATasaCompraActual",0) END as "MontoLiquidacion"
		,(CASE WHEN Car."EGRIdTipoOperacion" IN ('ANSIMC','ANSIMV') AND Car."EGRIdConcepto" IN ('ANTSIC','ANTSIV') AND Car."IdEstado" IN ('ANT') THEN COALESCE(Car."EGRMontoARecibirAnticipo",0)
			  ELSE COALESCE(Car."INGMontoATasaCompraActual",0) END) - (Car."INGCantidad" * Car."PrecioCompra") as "Intereses"
		
		---------------------------------------------------------------------------------------
		,Ins."CodigoTipoInstrumento"						as "suby_CodigoTipoInstrumento"			
		,Ins."NombreTipoInstrumento"						as "suby_NombreTipoInstrumento"
		,Ins."IdEmisor"										as "suby_IdEmisor"	
		
		,Sub."IdSubyacente"									as "suby_IdSubyacente"
		,Sub."FolioSubyacente"								as "suby_FolioSubyacente"
		,Sub."IdTipoInstrumento" 							as "suby_IdTipoInstrumento"
		,Sub."IdInstrumento" 								as "suby_IdInstrumento"
		,Ins."ISIN"											as "suby_ISIN"
		,Sub."Nemotecnico"									as "suby_Nemotecnico"
		,Sub."NemoSVS" 										as "suby_NemoSVS"
		,Sub."IdMonedaInstrumento" 							as "suby_IdMonedaInstrumento"
		,Sub."IdClasificacionTipoInstrumento" 				as "suby_IdClasificacionTipoInstrumento"
		,car."IdBoveda"										as "suby_IdBoveda"		
		,Sub."EsFungible"									as "suby_EsFungible"
		,Sub."IdTipoReajuste" 								as "suby_IdTipoReajusteSubyacente"		   				 	
		,car."PrecioAPlazo"	   								as "suby_PrecioAPlazo"		   				 	
		,Sub."INGFechaOperacion" 					    	as "suby_INGFechaOperacion" 					
		,Sub."INGCantidad"							    	as "suby_INGCantidad"							
		,Sub."INGValorRescate" 								as "suby_INGValorRescate" 					
		
		,Sub."INGPrecioSubyacente"							AS "suby_INGPrecioCompra"
		,Sub."INGMontoATasaSubyacenteActual"				AS "suby_INGMontoATasaCompraActual"
		,Sub."INGMontoSubyacenteActualMonedaInstrumento"	AS "suby_INGMontoATasaCompraActualMonedaInstrumento"
		
		,Sub."INGPrecioMercadoActual"				    	as "suby_INGPrecioMercadoActual"				
		,Sub."INGTasaMercadoActual" 				   	 	as "suby_INGTasaMercadoActual" 				
		,Sub."INGMontoATasaMercadoActual" 			    	as "suby_INGMontoATasaMercadoActual" 
		,Sub."INGMontoMercadoActualMonedaInstrumento" 		as "suby_INGMontoMercadoActualMonedaInstrumento"
		
		,Sub."PrecioPromedioCompraOperacionRelacionada"		 as "suby_PrecioPromedioCompraOperacionRelacionada" 	
		,Sub."MontoPrecioPromedioCompraOperacionRelacionada" as "suby_MontoPrecioPromedioCompraOperacionRelacionada"
		,Sub."MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada" as "suby_MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada"
		,Sub."IdMonedaEmision" 								as "suby_EsPagaderoEn"
		,Sub."Tamano"										AS "suby_Tamano"
		,Car."Cantidad"
		,MovNormal."MontoAPlazo"							AS "MontoAPlazoComprobante"
  FROM "BSCL.Custodia.BD::CUS.Cartera" as Car
 INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = car."FolioCartera"
 INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" )
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = Car."IdTipoOperacion")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpeContraria ON (TipOpeContraria."IdTipoOperacion" = TipOpe."IdTipoOperacionRelacionada") 
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as Ins ON (Ins."IdInstrumento" = Sub."IdInstrumento")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" as CondLiq ON (CondLiq."IdTipoOperacion" = Car."IdTipoOperacion" AND CondLiq."IdCondicionDeLiquidacion" = Car."IdCondicionLiquidacion")
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CSDN ON (CSDN."IdCliente" = Car."IdCliente")
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = CAR."IdPortafolio")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = Car."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
 LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
 	MovNormal."IdEmpresa" = Car."IdEmpresa" and 
 	MovNormal."IdComprobante" = Car."FolioComprobante" and 
 	MovNormal."NemotecnicoSubyacente" = Sub."Nemotecnico" and MovNormal."Cantidad" = Car."Cantidad"
 	and trim(MovNormal."FolioTransaccion") = Car."FolioExternoReferencia"
 -- Las comprobantes de facturas de simultaneas solo consideran 1 detalle (en sebra no, por lo que se aplica max para que no se dupliquen registros)
-- INNER JOIN MovimientoComprobanteUnico AS MCU ON (MCU."IdEmpresa" = MovNormal."IdEmpresa" AND MCU."IdMovimientoComprobante" = MovNormal."IdMovimientoComprobante" AND MCU."NemotecnicoSubyacente" = MovNormal."NemotecnicoSubyacente")
 LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
 	MovPlazo."IdEmpresa" = Car."IdEmpresa" and 
 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado" and 
 	MovPlazo."NemotecnicoSubyacente" = Sub."Nemotecnico"
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" as Corred on Corred."IdCorredora" = MovNormal."IdCorredoraContraparte"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as ejec on CP."IdEmpleadoEjecutivo" = ejec."IdEmpleado"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTADO ON Car."IdEstado" = ESTADO."IdEstado"
 WHERE Car."IdTipoInstrumento" IN ('SIM', 'RRV', 'RRF')   
WITH READ ONLY;


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;