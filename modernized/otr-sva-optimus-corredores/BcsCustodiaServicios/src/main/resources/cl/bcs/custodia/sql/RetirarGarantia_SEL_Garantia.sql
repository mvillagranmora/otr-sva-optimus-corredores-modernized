/********************************************************** 
Proposito	: Script para recuperar información de las garantías a retirar
autor		: NRM
fecha		: 31/05/2017
***********************************************************/

									
SELECT 
	  GTA."IdGarantia"
	, GTA."FechaOperacion"
	, row_number() OVER (order by GTA."FolioGarantia") 				 	 as "IdGenerarMovimientoCustodia"
	, GTA."FolioGarantia"
	, GTA."FolioCarteraOrigen"
	, GTA."IdCarteraOrigen"
	, CAR."EGRIdTipoOperacion"					AS "IdTipoOperacion"
	, GTA."IdContraparteGarantia"                                        
	, GTA."RazonSocialContraparteGarantia"                               
	, GTA."IdOrigenGarantia"					as "IdOrigenGarantia"
	, GTA."FechaVencimientoOperacion" 			as "FechaVencimientoOperacion"		
	, GTA."FechaVencimientoInstrumento" 		as "FechaVencimientoInstrumento"
	, GTA."PlazoEnDiasOperacion" 				as "PlazoEnDiasOperacion"				
	, GTA."IdCliente" 							as "IdCliente"						
	, GTA."IdPortafolio" 						as "IdPortafolio"
	, COALESCE(CAR."IdTipoProducto",'') 		as "IdTipoProducto"				
	, COALESCE(CAR."IdBolsa",'')  				as "IdBolsa"						
	, GTA."IdCondicionLiquidacion" 				as "IdCondicionLiquidacion" 		
	, GTA."IdClasificacionTipoInstrumento"		as "IdClasificacionTipoInstrumento" 	
	, GTA."IdTipoInstrumento" 					as "IdTipoInstrumento"				
	, GTA."IdInstrumento" 						as "IdInstrumento" 					
	, GTA."Nemotecnico" 						as "Nemotecnico"						
	, GTA."IdEmisor" 							as "IdEmisor"							
	, GTA."NemoSVS" 							as "NemoSVS"	 						
	, GTA."IdTipoReajuste" 						as "IdTipoReajuste" 					
	, GTA."FechaEmisionInstrumento" 			as "FechaEmisionInstrumento"			
	, GTA."EsFungible" 							as "EsFungible"
	, COALESCE(GTA."IdMonedaInstrumento",'') 	as "IdMonedaInstrumento"
	, COALESCE(GTA."IdMonedaEmision",'') 		as "IdMonedaEmision"
	, COALESCE(CAR."IdBoveda",'') 				as "IdBoveda"
	
	, GTA."TasaCompraOperacionOrigen"		 	as "TasaCompraOrigen"
	, GTA."PrecioCompraOperacionOrigen" 		as "PrecioCompraOrigen"
	, GTA."INGTasaGarantia" 					as "TasaCompra" 
	, GTA."INGPrecioGarantia"		 			as "PrecioCompra"
	, GTA."INGCantidad"							AS "Cantidad"
	, GTA."INGValorRescate"						AS "ValorRescate"
	, GTA."EGRCantidad"							AS "CantidadRetirada"
	, GTA."EGRValorRescate"						AS "ValorRescateRetirado"
	, GTA."EGRTasa" 							as "TasaRetiro" 
	, GTA."EGRPrecio"		 					as "PrecioRetiro"
	, GTA."EGRMontoMonedaInstrumento"			AS "MontoRetiroAMonedaInstrumento"
	, GTA."IdInstrumentoCarteraGarantizada"
	, CLI."IdEmpleadoEjecutivo"
	, CLI."IdSucursalEjecutivo"
	, GTA."IdEstado"
	, GTA."IdConcepto"
	, conce."ConCertificadoAutomatico"
	, GTA."MontoGarantia"
FROM "BSCL.Custodia.BD::GTA.Garantia" AS GTA
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."IdCartera" = GTA."IdCarteraOrigen")
LEFT JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS conce ON conce."IdConcepto" = GTA."IdConcepto" AND conce."NombreEntidad" = 'CUS.GARANTIA' 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = GTA."IdCliente")
WHERE GTA."IdGarantia" in (:IdGarantia)
ORDER BY GTA."FolioGarantia";
