SELECT 
 TO_CHAR("FechaOperacionOrden" , 'YYYY-MM-DD') 	AS "FechaOperacion"
,"DocumentoIdentidad"  							AS "Rut"
,"IdCliente"           							AS "Cliente"
,"NombreCompleto"	  			 				AS "NombreCliente"
,"CodigoPortafolio"	   							AS "Portafolio"
,"IdClasificacionTipoInstrumento" 				AS "Mercado"
,"IdTipoInstrumento"   							AS "TipoInstrumento"
,"MontoOrden"									AS "Monto"
,"CantidadOrden"								AS "Cantidad" 
,"IdMonedaOrden" 								AS "IdMoneda"
,"PrecioOrden"		   							AS "Precio"
,"Tasa"
,0.0                    						AS"Paridad"		
,0.0                    						AS"PrecioPromedio"
,0.0                    						AS"ParidadPromedio"
,0.0                    						AS"TasaPromedio"
,TO_DATE("FechaVencimiento")					AS "FechaVencimiento"
,TO_DATE("FechaLiquidacion")					AS "FechaLiquidacion"
FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" 
WHERE "IdCliente" = :p_IdCliente
AND  TO_CHAR("FechaOperacionOrden" , 'YYYY-MM-DD') = :p_FechaOperacion
AND "IdEstadoOrden" <> 'ANU';
