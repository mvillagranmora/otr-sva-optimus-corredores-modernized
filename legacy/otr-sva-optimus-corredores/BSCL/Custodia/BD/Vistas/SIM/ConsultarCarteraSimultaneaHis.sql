SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaHis";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaHis" AS
 
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
		,MovNormal."IdCorredoraContraparte"
		,MovNormal."FechaLiquidacion" as "FechaLiquidacionMovtoContado"
		,IFNULL(Corred."NombreCompleto",'') as "NombreCorredora"
		,IFNULL(ejec."NombreCompleto",'') as "NombreEjecutivo"

		,IFNULL(MovNormal."FolioTransaccion",0) as "PaloNormal"
		,IFNULL(MovPlazo."FolioTransaccion",0) as "PaloPlazo"
		,Car."FolioCartera"
		,Car."FolioCarteraRelacionada"
		,Car."IdTipoInstrumento" as "IdTipoInstrumentoOperacion"
		,Car."IdInstrumento" as "IdInstrumentoOperacion"		
		,Car."IdTipoOperacion"
		,Car."IdBolsa"
		,Car."IdBoveda"
		,Car."IdCuentaCustodioPortafolio"


		,Car."IdMonedaSistema"
		,Car."IdMonedaInstrumento"
		,Car."FechaOperacion" as "FechaInversion" 
		,Car."FechaVencimientoOperacion"
		,Car."IdCondicionLiquidacion"

		,Car."FechaLiquidacion"		
		,Car."MontoCompra"
		,Car."PrecioCompra" as "PrecioInicial"
		,Car."INGMontoAPlazo" as "MontoAPlazo"
		--,Car."PrecioAPlazo"
		,coalesce(MovPlazo."Precio",0) as "PrecioAPlazo"
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

		---------------------------------------------------------------------------------------
		,Ins."CodigoTipoInstrumento"					as "suby_CodigoTipoInstrumento"			
		,Ins."NombreTipoInstrumento"					as "suby_NombreTipoInstrumento"
		,Ins."IdEmisor"									as "suby_IdEmisor"	
		
		,Sub."IdSubyacente"								as "suby_IdSubyacente"
		,Sub."FolioSubyacente"							as "suby_FolioSubyacente"
		,Sub."IdTipoInstrumento" 						as "suby_IdTipoInstrumento"
		,Sub."IdInstrumento" 							as "suby_IdInstrumento"
		,Ins."ISIN"										as "suby_ISIN"
		,Sub."Nemotecnico"								as "suby_Nemotecnico"
		,Sub."NemoSVS" 									as "suby_NemoSVS"
		,Sub."IdMonedaInstrumento" 						as "suby_IdMonedaInstrumento"
		,Sub."IdClasificacionTipoInstrumento" 			as "suby_IdClasificacionTipoInstrumento"
		,car."IdBoveda"									as "suby_IdBoveda"		
		,Sub."EsFungible"								as "suby_EsFungible"
		,Sub."IdTipoReajuste" 							as "suby_IdTipoReajusteSubyacente"
--		,Sub."MontoAPlazo"		   				 		as "suby_MontoAPlazo"		   				 	
		,car."PrecioAPlazo"	   							as "suby_PrecioAPlazo"		   				 	
		,Sub."INGFechaOperacion" 					    as "suby_INGFechaOperacion" 					
		,Sub."INGCantidad"							    as "suby_INGCantidad"							
		,Sub."INGValorRescate" 							as "suby_INGValorRescate" 					
--		,Sub."INGMontoAPlazo" 							as "suby_INGMontoAPlazo" 					
--		,Sub."INGPrecioAPlazo" 							as "suby_INGPrecioAPlazo" 					
--		,Sub."INGFechaPrecioTasaCompraActual"			as "suby_INGFechaPrecioTasaCompraActual"		 
--		,Sub."INGPrecioCompraActual"				    as "suby_INGPrecioCompraActual"				
--		,Sub."INGTasaCompraActual" 						as "suby_INGTasaCompraActual" 	
		,Sub."INGPrecioSubyacente" 						AS "suby_INGPrecioCompra"			
		,Sub."INGMontoATasaCompraActual" 			    as "suby_INGMontoATasaCompraActual" 			
--		,Sub."INGFechaPrecioTasaMercadoActual"			as "suby_INGFechaPrecioTasaMercadoActual"		 
--		,Sub."INGIdOrigenTasaPrecioMercadoActual" 	    as "suby_INGIdOrigenTasaPrecioMercadoActual"
		,Sub."INGMontoSubyacenteActualMonedaInstrumento"	AS "suby_INGMontoATasaCompraActualMonedaInstrumento"
		,Sub."INGMontoMercadoActualMonedaInstrumento" 		as "suby_INGMontoMercadoActualMonedaInstrumento"
		,Sub."INGPrecioMercadoActual"				    as "suby_INGPrecioMercadoActual"				
		,Sub."INGTasaMercadoActual" 				    as "suby_INGTasaMercadoActual" 				
		,Sub."INGMontoATasaMercadoActual" 			    as "suby_INGMontoATasaMercadoActual" 	
		,Sub."PrecioPromedioCompraOperacionRelacionada"		 as "suby_PrecioPromedioCompraOperacionRelacionada" 	
		,Sub."MontoPrecioPromedioCompraOperacionRelacionada" as "suby_MontoPrecioPromedioCompraOperacionRelacionada"
		,Sub."MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada" as "suby_MontoPrecioPromedioMonedaInstrumentoOperacionRelacionada"
		,Sub."IdMonedaEmision" 							as "suby_EsPagaderoEn"
		,Car."FechaRespaldo"							as "FechaRespaldo"
		,Sub."FechaRespaldo"							as "suby_FechaRespaldo"
		,Car."Cantidad"
		,MovNormal."MontoAPlazo"							AS "MontoAPlazoComprobante"
  FROM "BSCL.Custodia.BD::HIS.Cartera" as Car
 INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" as ultSub ON (ultSub."FolioCarteraRelacionada" = car."FolioCartera" and ultSub."FechaRespaldo" = Car."FechaRespaldo")
 INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" and ultSub."FechaRespaldo" = Sub."FechaRespaldo" )
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = Car."IdTipoOperacion") 
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as Ins ON (Ins."IdInstrumento" = Sub."IdInstrumento")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" as CondLiq ON (CondLiq."IdTipoOperacion" = Car."IdTipoOperacion" AND CondLiq."IdCondicionDeLiquidacion" = Car."IdCondicionLiquidacion")
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CSDN ON (CSDN."IdCliente" = Car."IdCliente")
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = CAR."IdPortafolio")
 
 LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
 	MovNormal."IdEmpresa" = Car."IdEmpresa" and 
 	MovNormal."IdComprobante" = Car."FolioComprobante" and 
 	MovNormal."NemotecnicoSubyacente" = Sub."Nemotecnico" 
 	
 LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
 	MovPlazo."IdEmpresa" = Car."IdEmpresa" and 
 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado" and 
 	MovPlazo."NemotecnicoSubyacente" = Sub."Nemotecnico"
 
 INNER JOIN MovimientoComprobanteUnico AS MCU ON (MCU."IdEmpresa" = MovNormal."IdEmpresa" AND MCU."IdMovimientoComprobante" = MovNormal."IdMovimientoComprobante" AND MCU."NemotecnicoSubyacente" = MovNormal."NemotecnicoSubyacente")
 
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" as Corred on Corred."IdCorredora" = MovNormal."IdCorredoraContraparte" AND Corred."IdEstado" = 'ACT'
  
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as ejec on CP."IdEmpleadoEjecutivo" = ejec."IdEmpleado"
 
 WHERE Car."IdTipoInstrumento"  IN ('SIM', 'RRV', 'RRF')
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaHis"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

GO