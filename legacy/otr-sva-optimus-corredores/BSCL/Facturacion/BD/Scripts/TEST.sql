PROCEDURE 
"BSCL"."BSCL.Facturacion.BD.Servicios::TEST" (
      IN p_MovimientoFacturarAAgrupar  TABLE("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT),     
      IN p_IdEmpresa                   BIGINT,
      IN p_IdTipoComprobante           NVARCHAR(3),
      IN p_Glosa                       NVARCHAR(260),
      IN p_FechaHoraSistema            NVARCHAR(40),
      
      IN v_SeparaPorOperacion          NVARCHAR(1),  
      IN v_SeparaPorProducto           NVARCHAR(1),
      IN v_SeparaPorBolsa              NVARCHAR(1),
      IN v_SeparaPorAgente             NVARCHAR(1),
      IN v_SeparaPorSucursal           NVARCHAR(1) 
)     
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_MovimientosAgrupados table ("IdPortafolio" BIGINT, "IdCliente" BIGINT, "FechaLiquidacion" NVARCHAR(10), 
                                            "IdTipoOperacion" NVARCHAR(6), "IdTipoProducto" NVARCHAR(4), "IdBolsa" NVARCHAR(3), 
                                            "IdAgente" BIGINT, "IdSucursal" BIGINT);
                                             
	DECLARE v_MovimientosInsert table ("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT, 
                                                    "FechaLiquidacion" NVARCHAR(10), "IdTransaccion" BIGINT,
                                                    "IdInstrumento" BIGINT, "IdTipoOperacion" NVARCHAR(6), "IdTipoProducto" NVARCHAR(4), 
                                                    "IdBolsa" NVARCHAR(3), "IdAgente" BIGINT, "IdSucursal" BIGINT,
                                                    "NemotecnicoInstrumento" NVARCHAR(100), "IdConceptoMovimiento" NVARCHAR(6), 
                                                    "Cantidad" DECIMAL(26,8), "Precio" DECIMAL(26,8), "Monto" DECIMAL(26,8), 
                                                    "Tasa" DECIMAL(26,8), "TasaEmision" DECIMAL(26,8), "Spread" DECIMAL(26,8), 
                                                    "ValorRescate" DECIMAL(26,8), "IdMoneda" NVARCHAR(3), "ValorParidad" DECIMAL(26,8), 
                                                    "ValorCaptacionMonedaOrigen" DECIMAL(26,8), "DocumentoIdentidad" NVARCHAR(40),
                                                    "TipoDocumentoIdentidad" BIGINT, "NombreLargoCliente" NVARCHAR(300), 
                                                    "DireccionFacturacion" NVARCHAR(100), "IdTipoInstrumento" NVARCHAR(3),
                                                    "IdClasificacionTipoInstrumento" NVARCHAR(3));                                    
                                             
	DECLARE v_Ref_IdMovimientoComprobanteDetalle        BIGINT;
	DECLARE v_Ref_IdComprobanteDetalle                  BIGINT;
	DECLARE v_Ref_IdComprobante                         BIGINT;
	DECLARE v_Ref_TipoComprobante                       BIGINT;
                                                      
	DECLARE v_a                                         BIGINT;
	DECLARE v_b                                         BIGINT; 
	DECLARE v_NroLinea                                  SMALLINT;   
	DECLARE v_MontoComprobante                          DECIMAL(26,8); 
                                                    
	DECLARE Arr_IdPortafolioAgrupador                   BIGINT ARRAY;
	DECLARE Arr_IdClienteAgrupador                      BIGINT ARRAY;        
	DECLARE Arr_FechaLiquidacionAgrupador               NVARCHAR(10) ARRAY;
	DECLARE Arr_IdTipoOperacionAgrupador                NVARCHAR(6) ARRAY;
	DECLARE Arr_IdTipoProductoAgrupador                 NVARCHAR(4) ARRAY;
	DECLARE Arr_IdBolsaAgrupador                        NVARCHAR(3) ARRAY;
	DECLARE Arr_IdAgenteAgrupador                       BIGINT ARRAY;
	DECLARE Arr_IdSucursalAgrupador                     BIGINT ARRAY;
                                                      
	DECLARE v_Arr_IdPortafolioAgrupador                 BIGINT;
	DECLARE v_Arr_IdClienteAgrupador                    BIGINT;   
	DECLARE v_Arr_FechaLiquidacionAgrupador             NVARCHAR(10);
	DECLARE v_Arr_IdTipoOperacionAgrupador              NVARCHAR(6);
	DECLARE v_Arr_IdTipoProductoAgrupador               NVARCHAR(4);
	DECLARE v_Arr_IdBolsaAgrupador                      NVARCHAR(3);
	DECLARE v_Arr_IdAgenteAgrupador                     BIGINT;
	DECLARE v_Arr_IdSucursalAgrupador                   BIGINT;
        
	DECLARE Arr_IdPortafolioInsert                      BIGINT ARRAY;
	DECLARE Arr_IdClienteInsert                         BIGINT ARRAY;
	DECLARE Arr_FechaLiquidacionInsert                  NVARCHAR(10) ARRAY;
	DECLARE Arr_IdMovimientoComprobanteInsert           BIGINT ARRAY;
	DECLARE Arr_IdTipoOperacionInsert                   NVARCHAR(6) ARRAY;
	DECLARE Arr_IdTipoProductoInsert                    NVARCHAR(4) ARRAY;
	DECLARE Arr_IdBolsaInsert                           NVARCHAR(3) ARRAY;
	DECLARE Arr_IdAgenteInsert                          BIGINT ARRAY;
	DECLARE Arr_IdSucursalInsert                        BIGINT ARRAY;              
	DECLARE Arr_IdInstrumentoInsert                     BIGINT ARRAY;
	DECLARE Arr_NemotecnicoInstrumentoInsert            NVARCHAR(100) ARRAY;
	DECLARE Arr_IdTransaccionInsert                     BIGINT ARRAY;
	DECLARE Arr_IdConceptoMovimientoInsert              NVARCHAR(6) ARRAY;
	DECLARE Arr_CantidadInsert                          DECIMAL(26,8) ARRAY;
	DECLARE Arr_PrecioInsert                            DECIMAL(26,8) ARRAY;
	DECLARE Arr_MontoInsert                             DECIMAL(26,8) ARRAY;   
	DECLARE Arr_TasaInsert                              DECIMAL(26,8) ARRAY;
	DECLARE Arr_TasaEmisionInsert                       DECIMAL(26,8) ARRAY;
	DECLARE Arr_SpreadInsert                            DECIMAL(26,8) ARRAY;
	DECLARE Arr_ValorRescateInsert                      DECIMAL(26,8) ARRAY;
	DECLARE Arr_IdMonedaInsert                          NVARCHAR(3) ARRAY;
	DECLARE Arr_ValorParidadInsert                      DECIMAL(26,8) ARRAY;
	DECLARE Arr_ValorCaptacionMonedaOrigenInsert        DECIMAL(26,8) ARRAY;
	DECLARE Arr_DocumentoIdentidadInsert                NVARCHAR(40) ARRAY;
	DECLARE Arr_TipoDocumentoIdentidadInsert            BIGINT ARRAY;
	DECLARE Arr_NombreLargoClienteInsert                NVARCHAR(300) ARRAY;
	DECLARE Arr_DireccionFacturacionInsert              NVARCHAR(100) ARRAY;
	DECLARE Arr_IdClasificacionTipoInstrumentoInsert    NVARCHAR(3) ARRAY;
	DECLARE Arr_IdTipoInstrumentoInsert                 NVARCHAR(3) ARRAY;
      
	DECLARE v_Arr_IdPortafolioInsert                    BIGINT;
	DECLARE v_Arr_IdClienteInsert                       BIGINT;
	DECLARE v_Arr_IdMovimientoComprobanteInsert         BIGINT;
	DECLARE v_Arr_FechaLiquidacionInsert                NVARCHAR(10);
	DECLARE v_Arr_IdTipoOperacionInsert                 NVARCHAR(6);
	DECLARE v_Arr_IdTipoProductoInsert                  NVARCHAR(4);
	DECLARE v_Arr_IdBolsaInsert                         NVARCHAR(3);
	DECLARE v_Arr_IdAgenteInsert                        BIGINT;
	DECLARE v_Arr_IdSucursalInsert                      BIGINT;
	DECLARE v_Arr_IdInstrumentoInsert                   BIGINT;
	DECLARE v_Arr_NemotecnicoInstrumentoInsert          NVARCHAR(100);
	DECLARE v_Arr_IdTransaccionInsert                   BIGINT;
	DECLARE v_Arr_IdConceptoMovimientoInsert            NVARCHAR(6);
	DECLARE v_Arr_CantidadInsert                        DECIMAL(26,8);
	DECLARE v_Arr_PrecioInsert                          DECIMAL(26,8);
	DECLARE v_Arr_MontoInsert                           DECIMAL(26,8);   
	DECLARE v_Arr_TasaInsert                            DECIMAL(26,8);
	DECLARE v_Arr_TasaEmisionInsert                     DECIMAL(26,8);
	DECLARE v_Arr_SpreadInsert                          DECIMAL(26,8);
	DECLARE v_Arr_ValorRescateInsert                    DECIMAL(26,8);
	DECLARE v_Arr_IdMonedaInsert                        NVARCHAR(3);
	DECLARE v_Arr_ValorParidadInsert                    DECIMAL(26,8);
	DECLARE v_Arr_ValorCaptacionMonedaOrigenInsert      DECIMAL(26,8); 
	DECLARE v_Arr_DocumentoIdentidadInsert              NVARCHAR(40);
	DECLARE v_Arr_TipoDocumentoIdentidadInsert          BIGINT;
	DECLARE v_Arr_NombreLargoClienteInsert              NVARCHAR(300);
	DECLARE v_Arr_DireccionFacturacionInsert            NVARCHAR(100); 
	DECLARE v_Arr_IdClasificacionTipoInstrumentoInsert  NVARCHAR(3);
	DECLARE v_Arr_IdTipoInstrumentoInsert               NVARCHAR(3);
      
      
         
	----Agrupar Movimientos segun preferencias   
	v_MovimientosAgrupados = Select MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", "FechaLiquidacion", 
                                  	(CASE 
                                     WHEN v_SeparaPorOperacion = 'S' THEN "IdTipoOperacion"
                                  	END) As "IdTipoOperacion",
                                  	(CASE 
                                     WHEN v_SeparaPorProducto = 'S' THEN "IdTipoProducto"
                                  	END) As "IdTipoProducto",  
                                  	(CASE 
                                     WHEN v_SeparaPorBolsa = 'S' THEN "IdBolsa"
                                  	END) As "IdBolsa",
                                  	(CASE 
                                     WHEN v_SeparaPorAgente = 'S' THEN "IdAgente"
                                  	END) As "IdAgente",
                                  	(CASE 
                                     WHEN v_SeparaPorSucursal = 'S' THEN "IdSucursal"
                                  	END) As "IdSucursal"                                                    
                               From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MovimientoComprobante
                              INNER JOIN :p_MovimientoFacturarAAgrupar As MovimientoFacturar
                                        ON MovimientoComprobante."IdMovimientoComprobante" = MovimientoFacturar."IdMovimientoComprobante"   
                                        And MovimientoComprobante."IdPortafolio" = MovimientoFacturar."IdPortafolio"
                                        And MovimientoComprobante."IdCliente" = MovimientoFacturar."IdCliente"
                              Group by MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", "FechaLiquidacion",
                                        (CASE 
                                           WHEN v_SeparaPorOperacion = 'S' THEN "IdTipoOperacion"
                                         END),
                                        (CASE 
                                           WHEN v_SeparaPorProducto = 'S' THEN "IdTipoProducto"
                                         END),  
                                        (CASE 
                                           WHEN v_SeparaPorBolsa = 'S' THEN "IdBolsa"
                                         END),
                                        (CASE 
                                           WHEN v_SeparaPorAgente = 'S' THEN "IdAgente"
                                         END),
                                        (CASE 
                                           WHEN v_SeparaPorSucursal = 'S' THEN "IdSucursal"
                                         END)                                                            
                             Order by MovimientoComprobante."IdCliente", MovimientoComprobante."IdPortafolio", "FechaLiquidacion";
                      
	
	 SELECT * FROM :v_MovimientosAgrupados;
	
	 Arr_IdPortafolioAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."IdPortafolio");          
     Arr_IdClienteAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."IdCliente");
     Arr_FechaLiquidacionAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."FechaLiquidacion");
     Arr_IdTipoOperacionAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."IdTipoOperacion");
     Arr_IdTipoProductoAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."IdTipoProducto");
     Arr_IdBolsaAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."IdBolsa");
     Arr_IdAgenteAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."IdAgente");
     Arr_IdSucursalAgrupador := ARRAY_AGG(:v_MovimientosAgrupados."IdSucursal");
     
     v_NroLinea := 1;
     v_MontoComprobante := 0;
     
     FOR v_a IN 1 .. CARDINALITY(:Arr_IdPortafolioAgrupador) DO
         v_Arr_IdPortafolioAgrupador := :Arr_IdPortafolioAgrupador[:v_a];
         v_Arr_IdClienteAgrupador    := :Arr_IdClienteAgrupador[:v_a];
         v_Arr_FechaLiquidacionAgrupador := :Arr_FechaLiquidacionAgrupador[:v_a];
         v_Arr_IdTipoOperacionAgrupador := :Arr_IdTipoOperacionAgrupador[:v_a];
         v_Arr_IdTipoProductoAgrupador  := :Arr_IdTipoProductoAgrupador[:v_a];
         v_Arr_IdBolsaAgrupador := :Arr_IdBolsaAgrupador[:v_a];
         v_Arr_IdAgenteAgrupador := :Arr_IdAgenteAgrupador[:v_a];
         v_Arr_IdSucursalAgrupador := :Arr_IdSucursalAgrupador[:v_a];
         
         ---Obtener datos de movimientos asociados al grupo         
         v_MovimientosInsert = Select MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", 
                                      MovimientoComprobante."IdMovimientoComprobante", "FechaLiquidacion", "IdTransaccion", 
                                      "IdInstrumento", "IdTipoOperacion", "IdTipoProducto", "IdBolsa", "IdAgente", "IdSucursal", 
                                      "NemotecnicoInstrumento", 
                                      "IdConceptoMovimiento", "Cantidad", "Precio", "Monto", "Tasa", "TasaEmision", "Spread", 
                                      "ValorRescate", "IdMoneda", "ValorParidad", "ValorCaptacionMonedaOrigen", "DocumentoIdentidad",
                                      "TipoDocumentoIdentidad", "NombreLargoCliente", "DireccionFacturacion", "IdTipoInstrumento",
                                      "IdClasificacionTipoInstrumento"                 
                               From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MovimientoComprobante
                                 INNER JOIN :p_MovimientoFacturarAAgrupar As MovimientoFacturar
                                    ON MovimientoComprobante."IdMovimientoComprobante" = MovimientoFacturar."IdMovimientoComprobante"
                               Where MovimientoComprobante."IdPortafolio" = v_Arr_IdPortafolioAgrupador 
                                   And MovimientoComprobante."IdCliente" = v_Arr_IdClienteAgrupador 
                                   And "FechaLiquidacion" = v_Arr_FechaLiquidacionAgrupador
                                   And "IdTipoOperacion" =  IFNULL(v_Arr_IdTipoOperacionAgrupador, "IdTipoOperacion")  
                                   And "IdTipoProducto" = IFNULL(v_Arr_IdTipoProductoAgrupador, "IdTipoProducto")    
                                   And "IdBolsa" = IFNULL(v_Arr_IdBolsaAgrupador, "IdBolsa")
                                   And "IdAgente" = IFNULL(v_Arr_IdAgenteAgrupador, "IdAgente")
                                   And "IdSucursal" = IFNULL(v_Arr_IdSucursalAgrupador, "IdSucursal")
                               Order by MovimientoComprobante."IdMovimientoComprobante"; 
          
         ---Obtiene Nro de Secuencia Comprobante
         SELECT "BSCL.Facturacion.BD.Secuencias::Comprobante".NEXTVAL
	     INTO v_Ref_IdComprobante
	     FROM DUMMY; 
	       
          v_MontoComprobante := 0; 
 
          Arr_IdPortafolioInsert := ARRAY_AGG(:v_MovimientosInsert."IdPortafolio");
          Arr_IdClienteInsert := ARRAY_AGG(:v_MovimientosInsert."IdCliente");
          Arr_IdMovimientoComprobanteInsert := ARRAY_AGG(:v_MovimientosInsert."IdMovimientoComprobante");
          Arr_FechaLiquidacionInsert := ARRAY_AGG(:v_MovimientosInsert."FechaLiquidacion"); 
          Arr_IdTipoOperacionInsert := ARRAY_AGG(:v_MovimientosInsert."IdTipoOperacion");
          Arr_IdTipoProductoInsert := ARRAY_AGG(:v_MovimientosInsert."IdTipoProducto");
          Arr_IdBolsaInsert := ARRAY_AGG(:v_MovimientosInsert."IdBolsa");
          Arr_IdAgenteInsert := ARRAY_AGG(:v_MovimientosInsert."IdAgente");
          Arr_IdSucursalInsert := ARRAY_AGG(:v_MovimientosInsert."IdSucursal");                   
          Arr_IdInstrumentoInsert := ARRAY_AGG(:v_MovimientosInsert."IdInstrumento");
          Arr_NemotecnicoInstrumentoInsert := ARRAY_AGG(:v_MovimientosInsert."NemotecnicoInstrumento");
          Arr_IdTransaccionInsert := ARRAY_AGG(:v_MovimientosInsert."IdTransaccion");
          Arr_IdConceptoMovimientoInsert := ARRAY_AGG(:v_MovimientosInsert."IdConceptoMovimiento");
          Arr_CantidadInsert := ARRAY_AGG(:v_MovimientosInsert."Cantidad");
          Arr_PrecioInsert := ARRAY_AGG(:v_MovimientosInsert."Precio");
          Arr_MontoInsert := ARRAY_AGG(:v_MovimientosInsert."Monto");
          Arr_TasaInsert := ARRAY_AGG(:v_MovimientosInsert."Tasa");
          Arr_TasaEmisionInsert := ARRAY_AGG(:v_MovimientosInsert."TasaEmision");
          Arr_SpreadInsert := ARRAY_AGG(:v_MovimientosInsert."Spread");
          Arr_ValorRescateInsert := ARRAY_AGG(:v_MovimientosInsert."ValorRescate");
          Arr_IdMonedaInsert := ARRAY_AGG(:v_MovimientosInsert."IdMoneda");
          Arr_ValorParidadInsert := ARRAY_AGG(:v_MovimientosInsert."ValorParidad");
          Arr_ValorCaptacionMonedaOrigenInsert := ARRAY_AGG(:v_MovimientosInsert."ValorCaptacionMonedaOrigen");  
          Arr_DocumentoIdentidadInsert := ARRAY_AGG(:v_MovimientosInsert."DocumentoIdentidad"); 
          Arr_TipoDocumentoIdentidadInsert := ARRAY_AGG(:v_MovimientosInsert."TipoDocumentoIdentidad"); 
          Arr_NombreLargoClienteInsert := ARRAY_AGG(:v_MovimientosInsert."NombreLargoCliente");
          Arr_DireccionFacturacionInsert := ARRAY_AGG(:v_MovimientosInsert."DireccionFacturacion");
          Arr_IdTipoInstrumentoInsert := ARRAY_AGG(:v_MovimientosInsert."IdTipoInstrumento"); 
          Arr_IdClasificacionTipoInstrumentoInsert := ARRAY_AGG(:v_MovimientosInsert."IdClasificacionTipoInstrumento");                   
          FOR v_b IN 1 .. CARDINALITY(:Arr_IdPortafolioInsert) DO 
              v_Arr_IdMovimientoComprobanteInsert := :Arr_IdMovimientoComprobanteInsert[:v_b];
              v_Arr_IdTipoOperacionInsert := :Arr_IdTipoOperacionInsert[:v_b];
              v_Arr_IdPortafolioInsert := :Arr_IdPortafolioInsert[:v_b];
              v_Arr_IdClienteInsert := :Arr_IdClienteInsert[:v_b];
              v_Arr_FechaLiquidacionInsert := :Arr_FechaLiquidacionInsert[:v_b];
              v_Arr_IdTipoProductoInsert := :Arr_IdTipoProductoInsert[:v_b];
              v_Arr_IdBolsaInsert := :Arr_IdBolsaInsert[:v_b];
              v_Arr_IdAgenteInsert := :Arr_IdAgenteInsert[:v_b];
              v_Arr_IdSucursalInsert := :Arr_IdSucursalInsert[:v_b];
              v_Arr_IdInstrumentoInsert := :Arr_IdInstrumentoInsert[:v_b];
              v_Arr_NemotecnicoInstrumentoInsert := :Arr_NemotecnicoInstrumentoInsert[:v_b];
              v_Arr_IdTransaccionInsert := :Arr_IdTransaccionInsert[:v_b];
              v_Arr_IdConceptoMovimientoInsert := :Arr_IdConceptoMovimientoInsert[:v_b];
              v_Arr_CantidadInsert := :Arr_CantidadInsert[:v_b];
              v_Arr_PrecioInsert := :Arr_PrecioInsert[:v_b];
              v_Arr_MontoInsert := :Arr_MontoInsert[:v_b];
              v_Arr_TasaInsert := :Arr_TasaInsert[:v_b];
              v_Arr_TasaEmisionInsert := :Arr_TasaEmisionInsert[:v_b];
              v_Arr_SpreadInsert := :Arr_SpreadInsert[:v_b];
              v_Arr_ValorRescateInsert := :Arr_ValorRescateInsert[:v_b];
              v_Arr_IdMonedaInsert := :Arr_IdMonedaInsert[:v_b];
              v_Arr_ValorParidadInsert := :Arr_ValorParidadInsert[:v_b];
              v_Arr_ValorCaptacionMonedaOrigenInsert := :Arr_ValorCaptacionMonedaOrigenInsert[:v_b];
              v_Arr_DocumentoIdentidadInsert := :Arr_DocumentoIdentidadInsert[:v_b]; 
              v_Arr_TipoDocumentoIdentidadInsert := :Arr_TipoDocumentoIdentidadInsert[:v_b];
              v_Arr_NombreLargoClienteInsert := :Arr_NombreLargoClienteInsert[:v_b];
              v_Arr_DireccionFacturacionInsert := :Arr_DireccionFacturacionInsert[:v_b];
              v_Arr_IdTipoInstrumentoInsert := :Arr_IdTipoInstrumentoInsert[:v_b];
              v_Arr_IdClasificacionTipoInstrumentoInsert := :Arr_IdClasificacionTipoInstrumentoInsert[:v_b];                  
              
              ---Obtiene Nro de Secuencia Agrupador
	          SELECT "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL
		      INTO v_Ref_IdMovimientoComprobanteDetalle
		      FROM DUMMY;
		      
		      ---Obtiene Nro de Secuencia Comprobante de Detalle
	          SELECT "BSCL.Facturacion.BD.Secuencias::ComprobanteDetalle".NEXTVAL
	          INTO v_Ref_IdComprobanteDetalle
	          FROM DUMMY;		                   	                     
	                       
	          ---Inserta datos en Agrupador de Movimientos 
			  Insert into "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
				     (  
				       "IdMovimientoComprobanteDetalle",
				       "IdComprobanteDetalle",
				       "IdMovimientoComprobante",
				       "IdTransaccion",
                       "IdInstrumento",
                       "NemotecnicoInstrumento",
                       "IdConceptoMovimiento",
                       "Cantidad",
                       "Precio",
                       "PrecioPromedio",
                       "Monto",
                       "Tasa",
                       "TasaEmision",
                       "SpreadTasaEmision",
                       "ValorRescate",
                       "IdMonedaOrigen",
                       "IdMonedaDestino",
                       "ValorParidad",
                       "ValorCaptacionMonedaOrigen",
				       "SecuenciaAgrupacion",
				       "IdEstadoMovimientoComprobanteDetalle",
				       "FechaIngresoRegistro"
				     )
			  Values
				    ( 
				      v_Ref_IdMovimientoComprobanteDetalle,
				      v_Ref_IdComprobanteDetalle,
					  v_Arr_IdMovimientoComprobanteInsert,
					  v_Arr_IdTransaccionInsert,
					  v_Arr_IdInstrumentoInsert,
					  v_Arr_NemotecnicoInstrumentoInsert,
					  v_Arr_IdConceptoMovimientoInsert,
					  v_Arr_CantidadInsert,
				      v_Arr_PrecioInsert,
				      0,
				      v_Arr_MontoInsert,
				      v_Arr_TasaInsert,
				      v_Arr_TasaEmisionInsert,
				      v_Arr_SpreadInsert,
				      v_Arr_ValorRescateInsert,
                      v_Arr_IdMonedaInsert,
				      'CLP',
				      v_Arr_ValorParidadInsert,
                      v_Arr_ValorCaptacionMonedaOrigenInsert,    
					  0,
					  'ING',
					  CURRENT_UTCTIMESTAMP 
					);
							
					
				---	Actualiza Estado de Movimiento
				Update "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
				Set "IdEstadoFacturacion" = 'FAC'
				Where "IdMovimientoComprobante" = v_Arr_IdMovimientoComprobanteInsert;
					
				---Inserta Datos en Comprobante Detalle				 				            
		        Insert into "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
		           ( 
		             "IdComprobanteDetalle",
                     "IdComprobante", 
                     "NroLinea",
                     "Glosa",
                     "FechaLiquidacion",
                     "IdInstrumento",
                     "NemotecnicoInstrumento",
                     "IdTipoOperacion",
                     "IdTipoProducto",
                     "Cantidad",
                     "Precio",
                     "Monto",
                     "IdBolsa",
                     "IdAgente",
                     "IdSucursal",                               
                     "IdEstadoComprobanteDetalle",
                     "FechaIngresoRegistro"                             
			       )
                Values
			       ( 
			          v_Ref_IdComprobanteDetalle,
				      v_Ref_IdComprobante,
 	                  v_NroLinea,
				      :p_Glosa,				      
				      v_Arr_FechaLiquidacionInsert,
				      v_Arr_IdInstrumentoInsert,	
				      v_Arr_NemotecnicoInstrumentoInsert,			               				               
				      v_Arr_IdTipoOperacionInsert,
				      v_Arr_IdTipoProductoInsert,
				      v_Arr_CantidadInsert,
				      v_Arr_PrecioInsert,
				      v_Arr_MontoInsert,
				      v_Arr_IdBolsaInsert,
                      v_Arr_IdAgenteInsert,
                      v_Arr_IdSucursalInsert,
				      'ING',
				      CURRENT_UTCTIMESTAMP
				   );
				   
				   v_NroLinea := v_NroLinea + 1;
				   v_MontoComprobante:= v_MontoComprobante + v_Arr_MontoInsert;
          END FOR;     
		
		  v_NroLinea := 1;
		  
		  ---Obtiene secuencia asociado a Tipo Comprobante 
		  If p_IdTipoComprobante = 'FAC' Then
		      SELECT "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFactura".NEXTVAL
	          INTO v_Ref_TipoComprobante
	          FROM DUMMY;
		  Else
		      If p_IdTipoComprobante = 'FAE' Then
		         SELECT "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronica".NEXTVAL
	             INTO v_Ref_TipoComprobante
	             FROM DUMMY;
		      End if;
		  End if;  		  
		  ---
		  		  	             
		  --- Inserta Datos Comprobante
          Insert into "BSCL.Facturacion.BD::FAC.Comprobante" 
                    (
                      "IdComprobante",
                      "IdEmpresa",
                      "IdPortafolio",
                      "IdCliente",
                      "DocumentoIdentidad",
                      "TipoDocumentoIdentidad",
                      "NombreLargoCliente", 
                      "DireccionFacturacion",
                      "FolioComprobante",
                      "IdTipoComprobante",
                      "IdMonedaOrigen",
                      "IdTipoProducto",
                      "IdClasificacionTipoInstrumento",
                      "IdTipoInstrumento",
                      "Monto",
                      "IdAgente",
                      "IdSucursal",
                      "FechaLiquidacion",
                      "FechaEmision",
                      "FechaEnvio",
                      "IdEstadoEnvio",
                      "FechaRespuestaEnvio",                      
                      "IdEstadoComprobante",
                      "IdTipoComprobanteRef",
                      "IdComprobanteRef",
                      "FechaIngresoRegistro"
                    )
          Values
                    (
                      v_Ref_IdComprobante,                      
                      :p_IdEmpresa,
                      v_Arr_IdPortafolioInsert,
				      v_Arr_IdClienteInsert,
				      v_Arr_DocumentoIdentidadInsert,
				      v_Arr_TipoDocumentoIdentidadInsert,
				      v_Arr_NombreLargoClienteInsert,
				      v_Arr_DireccionFacturacionInsert,
                      v_Ref_TipoComprobante,
                      :p_IdTipoComprobante,
                      'CLP',
                      v_Arr_IdTipoProductoInsert,
                      v_Arr_IdClasificacionTipoInstrumentoInsert,
                      v_Arr_IdTipoInstrumentoInsert,
                      v_MontoComprobante,
                      v_Arr_IdAgenteInsert,
                      v_Arr_IdSucursalInsert,
                      v_Arr_FechaLiquidacionInsert,
                      '0001-01-01',
                      '0001-01-01',
                      'PEN',
                      '0001-01-01',                      
                      'FAC',
                      '',
                      0,
                      CURRENT_UTCTIMESTAMP                
                    );  
          
     END FOR ; 
	                      
  	--Ingresar Cobros  
  	--CALL "BSCL"."BSCL.Facturacion.BD.Servicios::CrearComprobanteCobro" (:v_Ref_IdComprobante, p_FechaHoraSistema); 	                                       
END;