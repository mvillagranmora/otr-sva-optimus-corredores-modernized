SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarEjecucionOrdenes";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarEjecucionOrdenes" AS 
 
SELECT 
 EO."IdEjecucion"		
,EO."IdEmpresa"						
,EO."IdDocumento" 					
,EO."FolioGeneracion"					
,EO."IdOrden"							
,EO."Folio" 							
,EO."IdCliente" 						
,EO."IdPortafolio"	 				
,EO."IdClasificacionTipoInstrumento" 	
,EO."IdTipoInstrumento"				
,EO."IdTipoOperacion"					
,EO."IdInstrumento" 					
,EO."Cantidad" 						
,EO."Monto" 							
,EO."MontoValorizado"					
,EO."Precio" 							
,EO."IdMoneda" 						
,EO."IdCondicionDeLiquidacion" 		
,EO."FechaRecepcion" 					
,EO."FechaAceptacion" 				
,EO."TipoEjecucion"					
,EO."FechaEjecucion"					
,EO."IdBolsa" 						
,EO."EsOD"
,EO."PrecioEjecucion"					
,EO."MontoTransado" 					
,EO."CantidadTransada"				
,EO."ComisionTransada"				
,EO."MejorPrecioOtraBolsa"			
,EO."CantidadMejorPrecioOtraBolsa" 	
,EO."MejorPrecioMercado"				
,EO."PrecioMinimoMercado"				
,EO."HoraPrecioMinimoMercado"			
,EO."IdBolsaPrecioMinimoMercado" 		
,EO."PrecioMaximoMercado"				
,EO."HoraPrecioMaximoMercado"			
,EO."IdBolsaPrecioMaximoMercado" 		
,EO."MayorPrecioOperacionCorredor"	
,EO."HoraPrecioMayorCorredor"			
,EO."MenorPrecioOperacionCorredor"	
,EO."HoraPrecioMenorCorredor"			
,EO."IdEmpleadoDigitador"				
,EO."FolioComprobanteFacturacion"     
,EO."FechaGeneracion" 				
,EO."FolioExterno" 					
,EO."IdEstadoNotificacion"		 	
,EO."IdEstadoCarta"		 			
,EO."FechaIngreso"      				
,EO."FechaCreacion"     				
,EO."IdUsuarioCreador"
,COALESCE(REL."CodigoValorIndicador",'NOVINCULADO') AS "CodigoRelacion"
,EO."FechaReferencia"   
FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" as EO
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
		ON REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>'' AND REL."IdCliente" =EO."IdCliente";



GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.ConsultarEjecucionOrdenes"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
