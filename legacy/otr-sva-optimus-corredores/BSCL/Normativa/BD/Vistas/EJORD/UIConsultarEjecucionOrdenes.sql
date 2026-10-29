SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.UIConsultarEjecucionOrdenes";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.UIConsultarEjecucionOrdenes" AS 
 
SELECT 
 EO."IdEjecucion"		
,EO."IdEmpresa"						
,EO."IdDocumento" 					
,EO."FolioGeneracion"					
,EO."IdOrden"							
,EO."Folio" 							
,EO."IdCliente" 						
,EO."IdPortafolio"
,POR."CodigoPortafolio"
,CLI."DocumentoIdentidad"	
,CLI."NombreCompleto" 				
,EO."IdClasificacionTipoInstrumento" 	
,EO."IdTipoInstrumento"				
,EO."IdTipoOperacion"
,COALESCE(OPER."Nombre",'') AS "TipoOperacion"					
,EO."IdInstrumento"
,COALESCE(INSTR."Nemotecnico",'') AS "Instrumento"					 					
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
,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TRIM(EO."EsOD")) as "EsODUI"
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
,COALESCE(REL."DetalleIndicador",'') AS "Relacion"   
FROM "BSCL.Normativa.BD.Vistas::EJORD.ConsultarEjecucionOrdenes" AS EO
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
     	ON POR."IdPortafolio" = EO."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
	ON (CLI."IdCliente"=EO."IdCliente")     	
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER 
     	ON OPER."IdTipoOperacion" = EO."IdTipoOperacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTR 
     	ON INSTR."IdInstrumento" = EO."IdInstrumento"
LEFT OUTER JOIN  "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" REL
		ON REL."CodigoTipoIndicador"='1064PROP' AND REL."Codigo" =EO."CodigoRelacion";
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.UIConsultarEjecucionOrdenes"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
