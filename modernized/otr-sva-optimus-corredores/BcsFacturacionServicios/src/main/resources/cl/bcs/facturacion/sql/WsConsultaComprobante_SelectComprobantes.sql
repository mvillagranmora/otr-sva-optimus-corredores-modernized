
SELECT 
 COMP."IdComprobante"
,COMP."FolioComprobante"
,COMP."FechaComprobante"
,COMP."FechaLiquidacion" 
,COMP."NombreLargoCliente"  AS "NombreCliente"
,COMP."NombreEmpleadoEjecutivoPortafolio"  AS "NombreEjecutivo"
,COMP."IdCondicionLiquidacion" 
,COMP."IdTipoComprobante" 
,COMP."IdTipoProducto"
,COMP."IdConcepto"
,COMP."Glosa"
,COMP."IdPortafolio" 
,COMP."IdSucursalEmpleadoEjecutivoPortafolio" 
,COMP."IdTipoOperacion"
,COMP."Monto" 
,COMP."MontoComprobante"
,COMP."MontoLiquidacionCobro"
,COMP."MontoFinal" 
,COMP."MontoPagado" 
,COMP."MontoLiquidacion" 
,COMP."MontoLiquidacionCobro" 
,COMP."MontoComprobanteFinal"
,COMP."IdEstadoComprobante"
,COMP."IdEstadoDTE" 
,COMP."IdEstadoSII" 
,COMP."CodigoPortafolio"
,COMP."IdClasificacionTipoInstrumento"
,COMP."AccionTipoOperacion"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS COMP
WHERE COMP."DocumentoIdentidad" = :rut 
AND COMP."IdEstadoComprobante" <> 'ANU'
AND COMP."FechaComprobante" BETWEEN ( CASE WHEN  :fechaInicio ='0001-01-01' THEN COMP."FechaComprobante" ELSE :fechaInicio END ) 
							AND (CASE WHEN  :fechaTermino ='0001-01-01' THEN COMP."FechaComprobante" ELSE :fechaTermino END )
ORDER  BY COMP."CodigoPortafolio" , COMP."IdTipoComprobante" 	


