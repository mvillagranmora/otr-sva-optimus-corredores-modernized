SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenes";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenes" AS 
 	---Ordenes Asignadas
	SELECT 
			"CO"."Folio"  						  	as "FolioOrden" 
			,"CO"."IdOrden"							as "IdOrden"
			,LEFT("TipoOperacion_$12"."Accion",1)    	as "TipoOperacion"
			,"CO"."IdTipoOperacion"					as "IdTipoOperacion"
			,"CO"."IdClasificacionTipoInstrumento" 	as "IdClasificacionTipoInstrumento"                               
			,"Instrumento_$8"."Nemotecnico"        	as "Instrumento"     
			,"CO"."IdInstrumento"      				as "IdInstrumento" 
			,"CO"."IdTipoInstrumento"				as "IdTipoInstrumento"
			,"CO"."IdEstadoOrden"					as "IdEstadoOrden"
			,"CO"."IdTipoCanalDistribucion"			as "IdTipoCanalDistribucion"
			,"CO"."MontoOrden"    				  	as "MontoOrden" 	--    o "MontoLiquidacion"                        
			,"CO"."Cantidad"          	        	as "CantidadOrden"            
			,"CO"."PrecioOrden"       			  	as "PrecioOrden"          
			,"CO"."IdCondicionDeLiquidacion"        as "IdCondicionDeLiquidacion"     
			,"CondicionDeLiquidacion_$3"."Codigo"  	as "CondicionLiquidacion"                           
			,"CO"."FechaRecepcion"                 	as "FechaRecepcion"             
			,"CO"."FechaAceptacion"                	as "FechaAceptacion" 
			,case 
			when "CEAO"."IdEstadoAsignacion" = 'ASC' then 'T'
			when "CEAO"."IdEstadoAsignacion" = 'ASP'  then 'P'
			else 'S'	
			end		
													as "TipoEjecucion"      
			,"CT"."Folio"							as "FolioPalo"
			,"CT"."FechaOperacion"   			    as "FechaEjecucion"
			,"CT"."IdBolsa"							as "BolsaEjecucion"	
			,CASE WHEN "CT"."IndicadorOrdenDirecta"= 'OD' THEN 'S' ELSE 'N' END			as "EsOD"
			,case 
			when "CT"."IndicadorOrdenDirecta" = 'OD' then 'SOD'
			else 'NOD'
			end 
													as "ModalidadEjecucion"
			,'' 									as "TipoContraparte"  --SREL O PROP
			,"CT"."Precio" 							as "PrecioEjecucion"
			,"CA"."MontoAsignado" 					as "MontoTransado"
			,"ConsultarDisponibleTransaccion"."CantidadAsignada"     as "CantidadTransada"
			,"CO"."IdMonedaOrden"					as "IdMoneda"
			,COALESCE(CCT."COMMVTOCMBTE",0) 		as "ComisionTransada"
			--,0 		as "ComisionTransada"
			--Mejor
			,0 										as "MejorPrecioOtraBolsa"				
			,0 										as "CantidadMejorPrecioOtraBolsa" 	
			,0 										as "MejorPrecioMercado"				
			--Minimo
			--,0 										as "PrecioMinimoMercado"				
			--,'' 									as "HoraPrecioMinimoMercado"			
			,'' 									as "IdBolsaPrecioMinimoMercado" 		 
			--Maximo
			,0 								 		as "PrecioMaximoMercado"				
			--,'' 									as "HoraPrecioMaximoMercado"			
			,'' 									as "IdBolsaPrecioMaximoMercado" 		 
			--Mayor corredor
			,0 										as "MayorPrecioOperacionCorredor"		
			,'' 									as "HoraPrecioMayorCorredor"			
			--Menor corredor
			,0 										as "MenorPrecioOperacionCorredor"		
			,'' 									as "HoraPrecioMenorCorredor"		
			,"CO"."DocumentoIdentidad"              as "RutCliente"  
			,"CO"."IdCliente"						as "IdCliente"
			,"CO"."IdPortafolioCliente"				as "Cuenta"        
			,0										as "IdEmpleadoDigitador" 
			,"CO"."NombreCompleto"   				as "NombreCliente"
			,'' 									as "CodigoAgente"
			,'' 									as "NombreAgente"
			,0 										as "FolioComprobanteFacturacion"
			,''										as "FolioExterno" 
			,"CO"."FechaOperacion"					AS "FechaIngreso"
			,"CA"."IdTransaccion"
			FROM  "BSCL.Orden.BD::ORD.Orden" as "CO"
			INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" as "CA" on "CO"."IdOrden"="CA"."IdOrden"
			LEFT OUTER JOIN "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacionOrden" as "CEAO" on("CO"."IdOrden" = "CEAO"."IdOrden")
			LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Transaccion" as "CT" on ("CT"."IdTransaccion" = "CA"."IdTransaccion")
			LEFT OUTER JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS "ConsultarDisponibleTransaccion" ON  ( "ConsultarDisponibleTransaccion"."IdTransaccion" = "CA"."IdTransaccion" )
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$12" ON  ( "TipoOperacion_$12"."IdTipoOperacion" = CO."IdTipoOperacion" )
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$8" ON  ( "Instrumento_$8"."IdInstrumento" = CO."IdInstrumento" )
			LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion_$3" ON "CondicionDeLiquidacion_$3"."IdCondicionDeLiquidacion" = CO."IdCondicionDeLiquidacion"
			--Comisiones
			LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" CB ON CB."IdTransaccion"="CT"."IdTransaccion" AND CB."IdCliente" = "CO"."IdCliente"
			INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" CCT ON (CB."IdMovimientoComprobante"=CCT."IdMovimientoComprobante")
			WHERE  CO."IdTipoInstrumento" = 'ACN'				   --acciones nacionales	
					AND CO."IdEstadoOrden" <> 'ANU'					   --que no esten anuladas 	
			;

GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenes"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;