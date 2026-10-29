SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes" AS 
SELECT	 

		 CSDN."DocumentoIdentidadDefecto"
		,CSDN."NombreCompleto" as "NombreLargoCliente"
		,(CSDN."DocumentoIdentidadDefecto" || ' ' || CSDN."NombreCompleto") as "Cliente"
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

		,Car."FolioCartera"
		,Car."FolioCarteraRelacionada"
		,Car."IdTipoInstrumento" as "IdTipoInstrumentoOperacion"
		,Car."IdInstrumento" as "IdInstrumentoOperacion"
		,Car."IdTipoOperacion"

		,Car."IdMonedaSistema"
		,Car."IdMonedaInstrumento"
		,Car."FechaOperacion" as "FechaInversion" 
		,Car."FechaVencimientoOperacion"
		,Car."IdCondicionLiquidacion"
		,Car."IdBolsa"

		,Car."FechaLiquidacion"		
		,Car."MontoAPlazo"
		,Car."PrecioAPlazo"
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
		,Car."INGPrecioMercadoActual"				
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

		---------------------------------------------------------------------------------------
		,Ins."CodigoTipoInstrumento"					as "suby_CodigoTipoInstrumento"			
		,Ins."NombreTipoInstrumento"					as "suby_NombreTipoInstrumento"
		,Ins."IdEmisor"									as "suby_IdEmisor"	
		
		,Sub."IdSubyacente"								as "suby_IdSubyacente"
		,Sub."FolioSubyacente"							as "suby_FolioSubyacente"
		,Sub."IdTipoInstrumento" 						as "suby_IdTipoInstrumento"
		,Sub."IdInstrumento" 							as "suby_IdInstrumento"
		,Sub."Nemotecnico"								as "suby_Nemotecnico"
		,Sub."NemoSVS" 									as "suby_NemoSVS"
		,Sub."IdMonedaInstrumento" 						as "suby_IdMonedaInstrumento"
		,Sub."IdClasificacionTipoInstrumento" 			as "suby_IdClasificacionTipoInstrumento"
		,car."IdBoveda"									as "suby_IdBoveda"		
		,Sub."IdTipoReajuste" 							as "suby_IdTipoReajusteSubyacente"
--		,Sub."MontoAPlazo"		   				 		as "suby_MontoAPlazo"		   				 	
		,car."PrecioAPlazo"	   				 			as "suby_PrecioAPlazo"		   				 	
		,Sub."INGFechaOperacion" 					    as "suby_INGFechaOperacion" 					
		,Sub."INGCantidad"							    as "suby_INGCantidad"							
		,Sub."INGValorRescate" 							as "suby_INGValorRescate" 					
--		,Sub."INGMontoAPlazo" 							as "suby_INGMontoAPlazo" 					
--		,Sub."INGPrecioAPlazo" 							as "suby_INGPrecioAPlazo" 					
--		,Sub."INGFechaPrecioTasaCompraActual"			as "suby_INGFechaPrecioTasaCompraActual"		 
		,Sub."INGPrecioSubyacente"				    	as "suby_INGPrecioSubyacente"				
		,Sub."INGTasaSubyacente" 						as "suby_INGTasaSubyacente" 				
		,Sub."INGMontoATasaSubyacenteActual"     		as "suby_INGMontoATasaSubyacente" 			
--		,Sub."INGFechaPrecioTasaMercadoActual"			as "suby_INGFechaPrecioTasaMercadoActual"		 
--		,Sub."INGIdOrigenTasaPrecioMercadoActual" 	    as "suby_INGIdOrigenTasaPrecioMercadoActual" 	
		,Sub."INGPrecioMercadoActual"				    as "suby_INGPrecioMercadoActual"				
		,Sub."INGTasaMercadoActual" 				    as "suby_INGTasaMercadoActual" 				
		,Sub."INGMontoATasaMercadoActual" 			    as "suby_INGMontoATasaMercadoActual" 			

  FROM "BSCL.Custodia.BD::CUS.Cartera" as Car
 INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."FolioCarteraRelacionada" = car."FolioCartera" and Sub."IdEstado" = 'VIG' )
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = Car."IdTipoOperacion") 
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as Ins ON (Ins."IdInstrumento" = Sub."IdInstrumento")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" as CondLiq ON (CondLiq."IdTipoOperacion" = Car."IdTipoOperacion" AND CondLiq."IdCondicionDeLiquidacion" = Car."IdCondicionLiquidacion")
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS CSDN ON (CSDN."IdCliente" = Car."IdCliente")
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = CAR."IdPortafolio")
 WHERE Car."IdTipoInstrumento" IN ('SIM', 'RRV', 'RRF') and Car."IdEstado" = 'VIG' and Car."EGRCantidad" = 0
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
