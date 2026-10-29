SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobanteRVGeneralSimple";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobanteRVGeneralSimple"
  ( 
      IN p_MovimientoFacturarAAgrupar  TABLE("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT),     
      
      IN v_SeparaPorOperacion          NVARCHAR(1),  
      IN v_SeparaPorProducto           NVARCHAR(1),
      IN v_SeparaPorBolsa              NVARCHAR(1),
      IN v_SeparaPorAgente             NVARCHAR(1),
      IN v_SeparaPorSucursal           NVARCHAR(1),
      IN v_SeparaPorFechaLiq           NVARCHAR(1), 
      IN v_SeparaPorMoneda             NVARCHAR(1) 
)     
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_MovimientosAgrupados table ("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdTipoOperacion" NVARCHAR(6), 
	                                      "IdTipoProducto" NVARCHAR(4), "IdBolsa" NVARCHAR(3), "IdAgente" BIGINT, 
	                                      "IdSucursal" BIGINT, "FechaLiquidacion" NVARCHAR(10), "IdMoneda" NVARCHAR(3));
	DECLARE v_MovimientosAgrupados_grupo table ("ID" BIGINT, "IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdTipoOperacion" NVARCHAR(6), 
	                                      "IdTipoProducto" NVARCHAR(4), "IdBolsa" NVARCHAR(3), "IdAgente" BIGINT, 
	                                      "IdSucursal" BIGINT, "FechaLiquidacion" NVARCHAR(10), "IdMoneda" NVARCHAR(3));
	DECLARE v_MovimientosInsert table (   "ID" BIGINT, "IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT, 
                                          "FechaLiquidacion" NVARCHAR(10), "IdTransaccion" BIGINT,
                                          "IdInstrumento" BIGINT, "IdTipoOperacion" NVARCHAR(6), "IdTipoProducto" NVARCHAR(4), 
                                          "IdBolsa" NVARCHAR(3), "IdAgente" BIGINT, "IdSucursal" BIGINT,
                                          "NemotecnicoInstrumento" NVARCHAR(100), "IdConceptoMovimiento" NVARCHAR(6), 
                                          "Cantidad" DECIMAL(26,8), "Precio" DECIMAL(26,8), "Monto" DECIMAL(26,8), 
                                          "Tasa" DECIMAL(26,8), "TasaEmision" DECIMAL(26,8), "Spread" DECIMAL(26,8), 
                                          "ValorRescate" DECIMAL(26,8), "IdMoneda" NVARCHAR(3), "ValorParidad" DECIMAL(26,8), 
                                          "ValorCaptacionMonedaOrigen" DECIMAL(26,8), "DocumentoIdentidad" NVARCHAR(40),
                                          "TipoDocumentoIdentidad" NVARCHAR(1), "NombreLargoCliente" NVARCHAR(300), 
                                          "DireccionFacturacion" NVARCHAR(100), "IdTipoInstrumento" NVARCHAR(3),
                                          "IdClasificacionTipoInstrumento" NVARCHAR(3));
                                             
     ----Agrupar Movimientos segun preferencias   
     v_MovimientosAgrupados = SELECT  MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", 
	                              (CASE WHEN v_SeparaPorOperacion = 'S' THEN "IdTipoOperacion" END) As "IdTipoOperacion",
	                              (CASE WHEN v_SeparaPorProducto  = 'S' THEN "IdTipoProducto"  END) As "IdTipoProducto",  
	                              (CASE WHEN v_SeparaPorBolsa     = 'S' THEN "IdBolsa"         END) As "IdBolsa",
	                              (CASE WHEN v_SeparaPorAgente    = 'S' THEN "IdAgente"        END) As "IdAgente",
	                              (CASE WHEN v_SeparaPorSucursal  = 'S' THEN "IdSucursal"      END) As "IdSucursal",
	                              (CASE WHEN v_SeparaPorFechaLiq  = 'S' THEN "FechaLiquidacion"END) As "FechaLiquidacion",
	                              (CASE WHEN v_SeparaPorMoneda    = 'S' THEN "IdMoneda"        END) As "IdMoneda"                         
                              From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MovimientoComprobante
                              INNER JOIN :p_MovimientoFacturarAAgrupar As MovimientoFacturar
                                            ON  MovimientoComprobante."IdMovimientoComprobante" = MovimientoFacturar."IdMovimientoComprobante"   
                                            And MovimientoComprobante."IdPortafolio"            = MovimientoFacturar."IdPortafolio"
                                            And MovimientoComprobante."IdCliente"               = MovimientoFacturar."IdCliente"
                              Group by MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", 
	                              (CASE WHEN v_SeparaPorOperacion = 'S' THEN "IdTipoOperacion" END),
	                              (CASE WHEN v_SeparaPorProducto  = 'S' THEN "IdTipoProducto"  END),  
	                              (CASE WHEN v_SeparaPorBolsa     = 'S' THEN "IdBolsa"         END),
	                              (CASE WHEN v_SeparaPorAgente = 'S' THEN "IdAgente"           END),
	                              (CASE WHEN v_SeparaPorSucursal = 'S' THEN "IdSucursal"       END),
	                              (CASE WHEN v_SeparaPorFechaLiq = 'S' THEN "FechaLiquidacion" END),
	                              (CASE WHEN v_SeparaPorMoneda = 'S' THEN "IdMoneda"           END);                                                                                                   
                             -- Order by MovimientoComprobante."IdCliente", MovimientoComprobante."IdPortafolio";
	 select * from :v_MovimientosAgrupados;
                              
      v_MovimientosAgrupados_grupo  = SELECT 
      ROW_NUMBER () OVER (order by "IdPortafolio", "IdCliente", "IdTipoOperacion", "IdTipoProducto", "IdBolsa", "IdAgente", "IdSucursal", "FechaLiquidacion", "IdMoneda") as ID    
      				 , "IdPortafolio", "IdCliente", "IdTipoOperacion", "IdTipoProducto", "IdBolsa", "IdAgente", "IdSucursal", "FechaLiquidacion", "IdMoneda"
      FROM :v_MovimientosAgrupados; 
      
      select * from :v_MovimientosAgrupados_grupo order by ID;

      ----- Movimientos a facturar enumerados por grupo           
      v_MovimientosInsert =    Select GRUPO."ID", 
      								  MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", 
                                      MovimientoComprobante."IdMovimientoComprobante",MovimientoComprobante. "FechaLiquidacion", MovimientoComprobante."IdTransaccion", 
                                      MovimientoComprobante."IdInstrumento", MovimientoComprobante."IdTipoOperacion", MovimientoComprobante."IdTipoProducto", 
                                      MovimientoComprobante."IdBolsa", MovimientoComprobante."IdAgente", MovimientoComprobante."IdSucursal", 
                                      MovimientoComprobante."NemotecnicoInstrumento", 
                                      MovimientoComprobante."IdConceptoMovimiento", MovimientoComprobante."Cantidad", MovimientoComprobante."Precio", 
                                      MovimientoComprobante."Monto", MovimientoComprobante."Tasa", MovimientoComprobante."TasaEmision", MovimientoComprobante."Spread", 
                                      MovimientoComprobante."ValorRescate", MovimientoComprobante."IdMoneda", MovimientoComprobante."ValorParidad", 
                                      MovimientoComprobante."ValorCaptacionMonedaOrigen", MovimientoComprobante."DocumentoIdentidad",
                                      MovimientoComprobante."TipoDocumentoIdentidad", MovimientoComprobante."NombreLargoCliente", MovimientoComprobante."DireccionFacturacion", 
                                      MovimientoComprobante."IdTipoInstrumento",MovimientoComprobante."IdClasificacionTipoInstrumento"                 
                               From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MovimientoComprobante -- TABLA DETALLE
                               INNER JOIN :p_MovimientoFacturarAAgrupar              As MovimientoFacturar    -- Movimientos filtrados
                                    	ON MovimientoComprobante."IdMovimientoComprobante" = MovimientoFacturar."IdMovimientoComprobante"
                                       And MovimientoComprobante."IdPortafolio"            = MovimientoFacturar."IdPortafolio"
                                       And MovimientoComprobante."IdCliente"               = MovimientoFacturar."IdCliente"                                            
                               LEFT OUTER JOIN :v_MovimientosAgrupados_grupo		 As	GRUPO		          -- Criterios de Agrupamiento para marcar
                                       ON  MovimientoComprobante."IdPortafolio"            = GRUPO."IdPortafolio" 
                                       And MovimientoComprobante."IdCliente"               = GRUPO."IdCliente"							
                               Where   MovimientoComprobante."IdTipoOperacion"   =  IFNULL(GRUPO."IdTipoOperacion" , MovimientoComprobante."IdTipoOperacion" )  
                               And 	   MovimientoComprobante."IdTipoProducto"    =  IFNULL(GRUPO."IdTipoProducto"  , MovimientoComprobante."IdTipoProducto"  )    
                               And     MovimientoComprobante."IdBolsa"           =  IFNULL(GRUPO."IdBolsa"         , MovimientoComprobante."IdBolsa"         )
                               And     MovimientoComprobante."IdAgente"		     =  IFNULL(GRUPO."IdAgente"        , MovimientoComprobante."IdAgente"        )
                               And     MovimientoComprobante."IdSucursal" 	     =  IFNULL(GRUPO."IdSucursal"      , MovimientoComprobante."IdSucursal"      )
                               And     MovimientoComprobante."FechaLiquidacion"  =  IFNULL(GRUPO."FechaLiquidacion", MovimientoComprobante."FechaLiquidacion")
                               And     MovimientoComprobante."IdMoneda"          =  IFNULL(GRUPO."IdMoneda"        , MovimientoComprobante."IdMoneda"        )
                               Order by MovimientoComprobante."IdMovimientoComprobante";
                               
       select * from :v_MovimientosInsert order by ID;
       
       /*
       ---- Recorrer Todos por grupo y enviar a Facturar
       BEGIN
       DECLARE CURSOR c_table FOR  
			SELECT    "ID" 
			        , "IdPortafolio"  				 , "IdCliente"         , "IdMovimientoComprobante"       , "FechaLiquidacion"        , "IdTipoOperacion"                                 
				    , "IdTipoProducto" 				 , "IdBolsa"   		   , "IdAgente"                      , "IdSucursal"              , "IdInstrumento"        
					, "NemotecnicoInstrumento"       , "IdTransaccion"     , "IdConceptoMovimiento"          , "Cantidad"                  
					, "Precio"                       , "Monto"             , "Tasa"                          , "TasaEmision"        
					, "Spread"                       , "ValorRescate"      , "IdMoneda"                      , "ValorParidad" 
				    , "ValorCaptacionMonedaOrigen"   , "DocumentoIdentidad", "TipoDocumentoIdentidad"        , "NombreLargoCliente"  
				    , "DireccionFacturacion" 		 , "IdTipoInstrumento" , "IdClasificacionTipoInstrumento" 
		
			from :v_MovimientosInsert
			Order by "ID" ;
			
			FOR  cur_row as c_table DO
				-- EL DETALLE
					--- Obtiene ID Detalle de IdMovimientoComprobanteDetalle y IdComprobanteDetalle
					--- Inserta datos en Agrupador de Movimientos
					---	Actualiza Estado de Movimiento
					--- Inserta Datos en Comprobante Detalle
					--- Incrementa linea 				v_NroLinea := v_NroLinea + 1;
					--- SUMA monto						v_MontoComprobante:= v_MontoComprobante + v_Arr_MontoInsert;
									
					--- Si Cambia Grupo:
						-- Obtiene secuencia asociado a Tipo Comprobante  
						-- Inserta Datos Comprobante con monto sumado
						-- Reinicia Linea Detalle
						-- Reinicia Monto v_MontoComprobante := 0;
						-- Ingresar Cobros
						
					--select cur_row."ID"
					--, cur_row."IdPortafolio"               , cur_row."IdCliente"         , cur_row."IdMovimientoComprobante", cur_row."FechaLiquidacion", cur_row."IdTipoOperacion"                               
				    --, cur_row."IdTipoProducto"	 	     , cur_row."IdBolsa"           , cur_row."IdAgente"               , cur_row."IdSucursal"      , cur_row."IdInstrumento"      
					--, cur_row."NemotecnicoInstrumento"     , cur_row."IdTransaccion"     , cur_row."IdConceptoMovimiento"   , cur_row."Cantidad"                
					--, cur_row."Precio"                     , cur_row."Monto"             , cur_row."Tasa"                   , cur_row."TasaEmision"      
					--, cur_row."Spread"                     , cur_row."ValorRescate"      , cur_row."IdMoneda"               , cur_row."ValorParidad"
				    --, cur_row."ValorCaptacionMonedaOrigen" , cur_row."DocumentoIdentidad", cur_row."TipoDocumentoIdentidad" , cur_row."NombreLargoCliente"
				    --, cur_row."DireccionFacturacion"       , cur_row."IdTipoInstrumento" , cur_row."IdClasificacionTipoInstrumento"
				    --from dummy;
				    				    
			END FOR;
	 END ;*/
                     
END;

commit;
