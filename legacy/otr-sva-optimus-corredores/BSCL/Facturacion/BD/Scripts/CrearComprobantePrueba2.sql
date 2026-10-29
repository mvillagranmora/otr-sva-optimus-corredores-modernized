SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba2";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba2"
  ( 
      IN p_MovimientoFacturarAAgrupar  table("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT,
                                             "IdTipoComprobante" NVARCHAR(3)),     
      IN p_IdEmpresa                   BIGINT,
      IN p_IdUsuario                   BIGINT,   
      IN p_Glosa                       NVARCHAR(260) default '',
      IN p_FechaSistema                NVARCHAR(10),      
      
      IN v_SeparaPorOperacion          NVARCHAR(1),  
      IN v_SeparaPorProducto           NVARCHAR(1),
      IN v_SeparaPorBolsa              NVARCHAR(1),
      IN v_SeparaPorAgente             NVARCHAR(1),
      IN v_SeparaPorSucursal           NVARCHAR(1),
      IN v_SeparaPorFechaLiq           NVARCHAR(1), 
      IN v_SeparaPorMoneda             NVARCHAR(1),


       --Resultados Internos
      --Facturacion
	--  OUT p_out_IdMovimientoComprobante table ("IdMovimientoComprobante" BIGINT),   
      OUT v_ComprobanteGenerado       table ("IdComprobante" BIGINT),
      
      --Cuenta Inversion
      OUT p_out_CargoAbonoGenerado    table ("IdCargoAbonoMovimiento" BIGINT),
     
      --Salida Sp Ingreso Manual de Titulo RV     
  /*    OUT p_out_FolioCartera      table ("FolioCartera" BIGINT),
      OUT p_out_FolioCarteraSub   table ("FolioCarteraSub" BIGINT), 
      OUT p_out_FolioCarteraRet   table ("FolioCarteraRet" BIGINT), 
      OUT p_out_IdSubyacente      table ("FolioSubyacente" BIGINT),
      OUT p_out_IdSubyacenteRet   table ("FolioSubyacenteRet" BIGINT),
      OUT p_out_IdGarantia        table ("FolioGarantia" BIGINT),
      OUT p_out_IdGarantiaRetiro  table ("FolioGarantiaRet" BIGINT),*/
      
      --Tesoreria
    --  OUT p_out_IngEgrGenerado        table ("IdMovIngEgr" BIGINT),
     
      --Salidas para mensajes de advertencias y errores       
      OUT p_out_AdvertenciasContador INT,   
      OUT p_out_AdvertenciasDetalle  TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)), 
      OUT p_out_ErroresContador      INT,
      OUT p_out_ErroresDetalle       TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))       
      
        
  )     
  
  LANGUAGE SQLSCRIPT
  SQL SECURITY INVOKER 
  DEFAULT SCHEMA "BSCL"
  --READS SQL DATA
AS
BEGIN
  DECLARE v_out_AdvertenciasContadorCInversion, v_out_ErroresContadorCInversion, v_out_AdvertenciasContadorCustodia,
            v_out_ErroresContadorCustodia INT; -- v_out_AdvertenciasContadorTesoreria, v_out_ErroresContadorTesoreria INT;
            
  DECLARE v_FolioTipoComprobante table("IdTipoComprobante" NVARCHAR(3), "FolioComprobante" BIGINT); 
  DECLARE v_MovimientoUpdate table ("IdComprobante" BIGINT, "FolioComprobante" BIGINT, "IdMovimientoComprobante" BIGINT);
   	

  DECLARE v_MovimientosAgrupados table ("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdTipoComprobante" NVARCHAR(3), "IdComprobante" BIGINT, "IdTipoInstrumento" NVARCHAR(3),  
                                        "IdTipoOperacion" NVARCHAR(6), "IdTipoProducto" NVARCHAR(4), "IdBolsa" NVARCHAR(3), 
                                        "IdAgente" BIGINT, "IdSucursal" BIGINT, "FechaLiquidacion" NVARCHAR(10), 
                                        "IdMoneda" NVARCHAR(3));


  DECLARE v_Comprobante Table ("IdComprobante" BIGINT, "ID" BIGINT,  "IdEmpresa" BIGINT, "IdPortafolio" BIGINT, "IdCliente" BIGINT, "DocumentoIdentidad" NVARCHAR(40),
                  "TipoDocumentoIdentidad" NVARCHAR(1), "NombreLargoCliente"  NVARCHAR(300), "DireccionFacturacion"  NVARCHAR(100), "FolioComprobante" BIGINT, 
                  "IdTipoComprobante" NVARCHAR(3), "IdMonedaOrigen" NVARCHAR(3), "IdTipoOperacion" NVARCHAR(6), "IdTipoProducto" NVARCHAR(4), "IdClasificacionTipoInstrumento" NVARCHAR(3),
                  "IdTipoInstrumento" NVARCHAR(3), "Monto" DECIMAL(28,8), "MontoCobro" DECIMAL(26,8), "MontoFinal" DECIMAL(26,8),
                  "IdAgente" BIGINT, "IdSucursal" BIGINT, "IdCondicionLiquidacion" NVARCHAR(2), "FechaLiquidacion" DATE , "FechaEmision" DATE, "FechaEnvio" SECONDDATE,
                    "IdEstadoEnvio" NVARCHAR(3), "FechaRespuestaEnvio" SECONDDATE, "IdEstadoComprobante"    NVARCHAR(3), "IdTipoComprobanteRef" NVARCHAR(3),
                  "IdComprobanteRef" BIGINT, "Glosa" NVARCHAR(260), "EsExento" NVARCHAR(1), "FechaIngresoRegistro" TIMESTAMP,"FechaSistema" NVARCHAR(10));

  

  DECLARE v_MovimientosAgrupados_grupo table ("ID" BIGINT, "IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdTipoComprobante" NVARCHAR(3), "IdComprobante" BIGINT, "IdTipoInstrumento" NVARCHAR(3),  "IdTipoOperacion" NVARCHAR(6), 
                                        "IdTipoProducto" NVARCHAR(4), "IdBolsa" NVARCHAR(3), "IdAgente" BIGINT, 
                                        "IdSucursal" BIGINT, "FechaLiquidacion" NVARCHAR(10), "IdMoneda" NVARCHAR(3));
                                             
  /*DECLARE v_MovimientosInsert table ("IdComprobanteDetalle" BIGINT, "IdMovimientoComprobanteDetalle" BIGINT, "ID" BIGINT, "IdPortafolio" BIGINT, "IdCliente" BIGINT,"IdTipoComprobante" NVARCHAR(3), "IdMovimientoComprobante" BIGINT, 
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
                                                    "IdClasificacionTipoInstrumento" NVARCHAR(3), "IdCondicionLiquidacion" NVARCHAR(2));*/
                                                    
  DECLARE v_MovimientosInsert table ("IdComprobante" BIGINT, "NroLinea" BIGINT, "IdComprobanteDetalle" BIGINT, "IdMovimientoComprobanteDetalle" BIGINT, "ID" BIGINT, "IdPortafolio" BIGINT, "IdCliente" BIGINT,"IdTipoComprobante" NVARCHAR(3), "IdMovimientoComprobante" BIGINT, 
  /*v_MovimientosDetalleInsert*/                                                  "FechaLiquidacion" NVARCHAR(10), "IdTransaccion" BIGINT,
                                                    "IdInstrumento" BIGINT, "IdTipoOperacion" NVARCHAR(6), "IdTipoProducto" NVARCHAR(4), 
                                                    "IdBolsa" NVARCHAR(3), "IdAgente" BIGINT, "IdSucursal" BIGINT,
                                                    "NemotecnicoInstrumento" NVARCHAR(100), "IdConceptoMovimiento" NVARCHAR(6), 
                                                    "Cantidad" DECIMAL(26,8), "Precio" DECIMAL(26,8), "Monto" DECIMAL(26,8), 
                                                    "Tasa" DECIMAL(26,8), "TasaEmision" DECIMAL(26,8), "Spread" DECIMAL(26,8), 
                                                    "ValorRescate" DECIMAL(26,8), "IdMoneda" NVARCHAR(3), "ValorParidad" DECIMAL(26,8), 
                                                    "ValorCaptacionMonedaOrigen" DECIMAL(26,8), "DocumentoIdentidad" NVARCHAR(40),
                                                    "TipoDocumentoIdentidad" NVARCHAR(1), "NombreLargoCliente" NVARCHAR(300), 
                                                    "DireccionFacturacion" NVARCHAR(100), "IdTipoInstrumento" NVARCHAR(3),
                                                    "IdClasificacionTipoInstrumento" NVARCHAR(3), "IdCondicionLiquidacion" NVARCHAR(2));
                                                       
  DECLARE v_Arr_IdMoneda NVARCHAR(3);
  DECLARE Arr_IdMoneda   NVARCHAR(3)  ARRAY;
  
  DECLARE SALIDA_CONTROLADA CONDITION; 
  DECLARE v_errorcuenta 							INT DEFAULT 0;  
  DECLARE v_errorhashmsg 							table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
  DECLARE v_erroroutmsg  							nvarchar(520);	        	
  DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
  BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
  END;
	
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	--v_out_CargoAbonoGenerado    = select 0 as "IdCargoAbonoMovimiento" from dummy where 1=2;  ---borrar OJO PIOJO
    --v_out_IngEgrGenerado        = select 0 As "IdMovIngEgr" from dummy Where 1=2;
    
    v_out_AdvertenciasDetalleCInversion	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
   -- v_out_AdvertenciasDetalleTesoreria	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;

    v_out_ErroresDetalleCInversion	= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2; 
  --  v_out_ErroresDetalleTesoreria	= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	--v_ComprobanteGenerado = select 0 as "IdComprobante" from dummy where 1=2;
	
    -- Bloque para el drop de la tabla temporal
	/*BEGIN
		   DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 259 BEGIN END;    
		   drop table #v_ComprobanteGenerado;	
		 		                 
	END;  

	CREATE LOCAL TEMPORARY TABLE #v_ComprobanteGenerado ("IdComprobante" BIGINT, "ID" BIGINT);
                      */
     ----Se puede mejorar, en algun futuro.
     ----Agrupar Movimientos segun preferencias, se agregó inmediatamente la idComprobante para generar el comprobante.
	 ----Se agrego TipoInstrumento para agrupación   
     v_MovimientosAgrupados = Select MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente",
                                     MovimientoComprobante."IdTipoComprobante", "BSCL.Facturacion.BD.Secuencias::Comprobante".NEXTVAL as "IdComprobante", MovimientoComprobante."IdTipoInstrumento",  
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
                                      END) As "IdSucursal",
                                     (CASE 
                                        WHEN v_SeparaPorFechaLiq = 'S' THEN "FechaLiquidacion"
                                      END) As "FechaLiquidacion",
                                     (CASE 
                                        WHEN v_SeparaPorMoneda = 'S' THEN "IdMoneda"
                                      END) As "IdMoneda"                         
                              From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MovimientoComprobante
                                INNER JOIN :p_MovimientoFacturarAAgrupar As MovimientoFacturar
                                   ON MovimientoComprobante."IdMovimientoComprobante" = MovimientoFacturar."IdMovimientoComprobante"   
                                  And MovimientoComprobante."IdPortafolio" = MovimientoFacturar."IdPortafolio"
                                  And MovimientoComprobante."IdCliente" = MovimientoFacturar."IdCliente"
                              --Where MovimientoComprobante."IdEstadoFacturacion" = 'ING'     
                              Group by MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", 
                                       MovimientoComprobante."IdTipoComprobante", MovimientoComprobante."IdTipoInstrumento",
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
                                      END),
                                     (CASE 
                                        WHEN v_SeparaPorFechaLiq = 'S' THEN "FechaLiquidacion"
                                      END),
                                     (CASE 
                                        WHEN v_SeparaPorMoneda = 'S' THEN "IdMoneda"
                                      END)                                                                                                 
                              Order by MovimientoComprobante."IdCliente", MovimientoComprobante."IdPortafolio";
                       -- select * from :v_MovimientosAgrupados;

                        v_MovimientosAgrupados_grupo  = SELECT 
      ROW_NUMBER () OVER (order by "IdPortafolio", "IdCliente","IdTipoComprobante", "IdTipoOperacion", "IdTipoProducto", "IdBolsa", "IdAgente", "IdSucursal", "FechaLiquidacion", "IdMoneda") as ID    
               , "IdPortafolio", "IdCliente","IdTipoComprobante", "IdComprobante",  "IdTipoInstrumento",  "IdTipoOperacion", "IdTipoProducto", "IdBolsa", "IdAgente", "IdSucursal", "FechaLiquidacion", "IdMoneda"
      FROM :v_MovimientosAgrupados; 

      --select * from :v_MovimientosAgrupados_grupo order by ID;
		
	
        ----- Movimientos a facturar con detalles.           
     v_MovimientosDetalleInsert  =    Select GRUPO."IdComprobante", ROW_NUMBER () OVER (partition by MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", MovimientoComprobante."IdTipoComprobante", 
	 									  MovimientoComprobante."IdTipoOperacion", MovimientoComprobante."IdTipoProducto", MovimientoComprobante."IdBolsa", MovimientoComprobante."IdAgente", 
	 									  MovimientoComprobante."IdSucursal", MovimientoComprobante."FechaLiquidacion", MovimientoComprobante."IdMoneda") as "NroLinea" , "BSCL.Facturacion.BD.Secuencias::ComprobanteDetalle".NEXTVAL as "IdComprobanteDetalle" , "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL as "IdMovimientoComprobanteDetalle", 
                                      GRUPO."ID", 
                                      MovimientoComprobante."IdPortafolio", MovimientoComprobante."IdCliente", MovimientoComprobante."IdTipoComprobante", 
                                      MovimientoComprobante."IdMovimientoComprobante",MovimientoComprobante. "FechaLiquidacion", MovimientoComprobante."IdTransaccion", 
                                      MovimientoComprobante."IdInstrumento", MovimientoComprobante."IdTipoOperacion", MovimientoComprobante."IdTipoProducto", 
                                      MovimientoComprobante."IdBolsa", MovimientoComprobante."IdAgente", MovimientoComprobante."IdSucursal", 
                                      MovimientoComprobante."NemotecnicoInstrumento", 
                                      MovimientoComprobante."IdConceptoMovimiento", MovimientoComprobante."Cantidad", MovimientoComprobante."Precio", 
                                      MovimientoComprobante."Monto", MovimientoComprobante."Tasa", MovimientoComprobante."TasaEmision", MovimientoComprobante."Spread", 
                                      MovimientoComprobante."ValorRescate", MovimientoComprobante."IdMoneda", MovimientoComprobante."ValorParidad", 
                                      MovimientoComprobante."ValorCaptacionMonedaOrigen", MovimientoComprobante."DocumentoIdentidad",
                                      MovimientoComprobante."TipoDocumentoIdentidad", MovimientoComprobante."NombreLargoCliente", MovimientoComprobante."DireccionFacturacion", 
                                      MovimientoComprobante."IdTipoInstrumento",MovimientoComprobante."IdClasificacionTipoInstrumento", MovimientoComprobante."IdCondicionLiquidacion"           
                               From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MovimientoComprobante -- TABLA DETALLE
                               INNER JOIN :p_MovimientoFacturarAAgrupar              As MovimientoFacturar    -- Movimientos filtrados
                                      ON MovimientoComprobante."IdMovimientoComprobante" = MovimientoFacturar."IdMovimientoComprobante"
                                       And MovimientoComprobante."IdPortafolio"            = MovimientoFacturar."IdPortafolio"
                                       And MovimientoComprobante."IdCliente"               = MovimientoFacturar."IdCliente"
                                       And MovimientoComprobante."IdTipoComprobante"       = MovimientoFacturar."IdTipoComprobante"                                          
                               LEFT OUTER JOIN :v_MovimientosAgrupados_grupo     As GRUPO             -- Criterios de Agrupamiento para marcar
                                       ON  MovimientoComprobante."IdPortafolio"            = GRUPO."IdPortafolio" 
                                       And MovimientoComprobante."IdCliente"               = GRUPO."IdCliente"
                                       AND MovimientoComprobante."IdTipoInstrumento"        = GRUPO."IdTipoInstrumento"
                                       --And MovimientoComprobante."IdTipoComprobante" =  GRUPO."idTIpoComprobante"             
                               Where   MovimientoComprobante."IdTipoOperacion"   =  IFNULL(GRUPO."IdTipoOperacion" , MovimientoComprobante."IdTipoOperacion" )  
                               And     MovimientoComprobante."IdTipoProducto"    =  IFNULL(GRUPO."IdTipoProducto"  , MovimientoComprobante."IdTipoProducto"  )    
                               And     MovimientoComprobante."IdBolsa"           =  IFNULL(GRUPO."IdBolsa"         , MovimientoComprobante."IdBolsa"         )
                               And     MovimientoComprobante."IdAgente"        =  IFNULL(GRUPO."IdAgente"        , MovimientoComprobante."IdAgente"        )
                               And     MovimientoComprobante."IdSucursal"        =  IFNULL(GRUPO."IdSucursal"      , MovimientoComprobante."IdSucursal"      )
                               And     MovimientoComprobante."FechaLiquidacion"  =  IFNULL(GRUPO."FechaLiquidacion", MovimientoComprobante."FechaLiquidacion")
                               And     MovimientoComprobante."IdMoneda"          =  IFNULL(GRUPO."IdMoneda"        , MovimientoComprobante."IdMoneda"        )
                               Order by GRUPO."ID";

			
	--select * from :v_MovimientosDetalleInsert;	
            				 			   

   -- Datos a insertar en comprobante Aun se puede mejorar.
     v_Comprobante = select  GRUPO."IdComprobante", Movimiento."ID", :p_IdEmpresa as "IdEmpresa", Movimiento."IdPortafolio", Movimiento."IdCliente", Movimiento."DocumentoIdentidad", 
					          Movimiento."TipoDocumentoIdentidad", Movimiento."NombreLargoCliente", Movimiento."DireccionFacturacion",
					          (case when Movimiento."IdTipoComprobante" = 'FAC' then "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFactura".NEXTVAL
                                      when Movimiento."IdTipoComprobante" = 'FAE' then "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronica".NEXTVAL
                                      when Movimiento."IdTipoComprobante" = 'FAO' then "BSCL.Facturacion.BD.Secuencias::TipoComprobanteFacturaElectronicaOtrosConceptos".NEXTVAL
                                      when Movimiento."IdTipoComprobante" = 'NCE' then "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCreditoElectronica".NEXTVAL
                                      when Movimiento."IdTipoComprobante" = 'NCR' then "BSCL.Facturacion.BD.Secuencias::TipoComprobanteNotaDeCredito".NEXTVAL  
                                      else -1 end) as "FolioComprobante", Movimiento."IdTipoComprobante", Movimiento."IdMoneda" as "IdMonedaOrigen", Movimiento."IdTipoOperacion", Movimiento."IdTipoProducto",
            				  Movimiento."IdClasificacionTipoInstrumento", Movimiento."IdTipoInstrumento", SUM(Movimiento."Monto") as "Monto", 0 as "MontoCobro", 0 as "MontoFinal",
            				  Movimiento."IdAgente", Movimiento."IdSucursal", 
            				  '' as "IdCondicionLiquidacion", --Acá deberia ir condicion de liquidacion si fuese necesario 
            				  Movimiento."FechaLiquidacion", '0001-01-01' as "FechaEmision", '0001-01-01' as "FechaEnvio", 'PEN' as "IdEstadoEnvio",
            				  '0001-01-01' as "FechaRespuestaEnvio", 'FAC' as "IdEstadoComprobante", '' as "IdTipoComprobanteRef", 0 as "IdComprobanteRef", :p_Glosa as "Glosa", 'N' as "EsExento",
            				  CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro", :p_FechaSistema as  "FechaSistema"           
			          From    :v_MovimientosDetalleInsert as Movimiento, 
			                  :v_MovimientosAgrupados_grupo as GRUPO
			          WHere
			            	Movimiento."IdPortafolio" = GRUPO."IdPortafolio" 
			            And Movimiento."IdCliente" = GRUPO."IdCliente"
			            And Movimiento."ID" = GRUPO."ID"
			             Group By Movimiento."IdPortafolio", Movimiento."ID", Movimiento."IdCliente", Movimiento."DocumentoIdentidad", Movimiento."TipoDocumentoIdentidad",
			            	   Movimiento."DireccionFacturacion", Movimiento."FechaLiquidacion", Movimiento."IdTipoOperacion", Movimiento."IdTipoProducto", Movimiento."IdMoneda", 
			                   Movimiento."NombreLargoCliente", Movimiento."IdTipoInstrumento", Movimiento."IdClasificacionTipoInstrumento", GRUPO."IdComprobante", 
			                   "IdCondicionLiquidacion", Movimiento."IdTipoComprobante", :p_IdEmpresa, Movimiento."IdSucursal", Movimiento."IdAgente", Movimiento."IdBolsa"
			          Order By "ID"; 

     --select * from :v_Comprobante;
     
     

    -- Tabla update para MovimientoComprobante
   	v_MovimientoUpdate = select Comprobante."IdComprobante", Comprobante."FolioComprobante", "IdMovimientoComprobante"
   						 From :v_Comprobante as Comprobante
   						 INNER JOIN :v_MovimientosDetalleInsert as Movimiento
   						 ON Comprobante."ID" = Movimiento."ID"
   						 and Comprobante."IdComprobante" = Movimiento."IdComprobante"
   						 Group By Comprobante."IdComprobante", Comprobante."FolioComprobante", "IdMovimientoComprobante";
   						 
   						 
   						 
   	--select * from :v_MovimientoUpdate;
   	 
					  	

                                                 /*========================================================================*/
                                                 /*Inserción MovimientoComprobanteDetalle, ComprobanteDetalle, Comprobante*/
                                                /* ======================================================================*/
   
                              
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
			  Select
				     
				      "IdMovimientoComprobanteDetalle",
				      "IdComprobanteDetalle",
				      "IdMovimientoComprobante",
				      "IdTransaccion",
                      "IdInstrumento",
                      "NemotecnicoInstrumento",
                      "IdConceptoMovimiento",
                      "Cantidad",
                      "Precio",
				      0,
				      "Monto",
                      "Tasa",
                      "TasaEmision",
                      "Spread",
                      "ValorRescate",
                      "IdMoneda",
                      "IdMoneda",  --Ojo con esto, estoy copiando lo que saque de CrearComprobante.sql
                      "ValorParidad",
                      "ValorCaptacionMonedaOrigen",    
					  0,
					  'ING',
					  CURRENT_UTCTIMESTAMP   
				From 
					  :v_MovimientosDetalleInsert;
				
													
					
				---Inserta Datos en Comprobante Detalle				 				            
		        Insert into "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
		           ( 
		             "IdComprobanteDetalle",
                     "IdComprobante", 
                     "NroLinea",                     
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
                Select
			       
			          Movimiento."IdComprobanteDetalle",
                      Movimiento."IdComprobante", 
                      Movimiento."NroLinea",                     
                      Movimiento."FechaLiquidacion",
                      Movimiento."IdInstrumento",
                      Movimiento."NemotecnicoInstrumento",
                      Movimiento."IdTipoOperacion",
                      Movimiento."IdTipoProducto",
                      Movimiento."Cantidad",
                      Movimiento."Precio",
                      Movimiento."Monto",
                      Movimiento."IdBolsa",
                      Movimiento."IdAgente",
                      Movimiento."IdSucursal",
				      'ING',
				      CURRENT_UTCTIMESTAMP
				 From 
				 	  :v_MovimientosDetalleInsert as Movimiento;
				 

		   ---Actualiza Estado de Movimiento
					  Update "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
					  SET "IdEstadoFacturacion" = 'FAC',
					      "IdComprobante" = Actualiza."IdComprobante",
					      "FolioComprobante" = Actualiza."FolioComprobante" 
					  From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" as MovComprobante,
					  	   :v_MovimientoUpdate as Actualiza
					  Where MovComprobante."IdMovimientoComprobante" = Actualiza."IdMovimientoComprobante";
		
	
		  -- Sad Mr.Mime :(	  		  	  
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
                      "IdTipoOperacion",
                      "IdTipoProducto",
                      "IdClasificacionTipoInstrumento",
                      "IdTipoInstrumento",
                      "Monto",
                      "MontoCobro",
                      "MontoFinal",
                      "IdAgente",
                      "IdSucursal",
                      "IdCondicionLiquidacion",
                      "FechaLiquidacion",
                      "FechaEmision",
                      "FechaEnvio",
                      "IdEstadoEnvio",
                      "FechaRespuestaEnvio",                      
                      "IdEstadoComprobante",
                      "IdTipoComprobanteRef",
                      "IdComprobanteRef",
                      "Glosa",
                      "EsExento",
                      "FechaIngresoRegistro",
                      "FechaSistema"
                    )
         Select
                    
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
                      "IdTipoOperacion",
                      "IdTipoProducto",
                      "IdClasificacionTipoInstrumento",
                      "IdTipoInstrumento",
                      "Monto",
                      0,
                      0,
                      "IdAgente",
                      "IdSucursal",
                      "IdCondicionLiquidacion",
                      "FechaLiquidacion",
                      '0001-01-01',
                      '0001-01-01',
                      'PEN',
                      '0001-01-01',                      
                      'FAC',
                      '',
                      0,
                      :p_Glosa,
                      'N',
                      CURRENT_UTCTIMESTAMP,
                      :p_FechaSistema            
		 From    
		 			  :v_Comprobante;                   
           
		     
	
	v_ComprobanteGenerado = select "IdComprobante" from :v_Comprobante; 
	
	--select * from :v_ComprobanteGenerado;
	
	Arr_IdMoneda := ARRAY_AGG(:v_MovimientosDetalleInsert."IdMoneda");
	v_Arr_IdMoneda := :Arr_IdMoneda[1]; --Esto es parche!
	
	
		 --Ingresar Cobros  		  		  
  	 CALL "BSCL.Facturacion.BD.Servicios::CrearComprobanteCobro"(:v_ComprobanteGenerado, :p_FechaSistema, :v_Arr_IdMoneda);
  	 
  	 
  	 --Ingresar Forma de Pago
   /*  Call "BSCL.Facturacion.BD.Servicios::CrearComprobanteFormaDePago" (:v_ComprobanteGenerado);       */  	          	               
                                                   
     --Envia a Cuenta Inversion
    Call "BSCL.Facturacion.BD.Servicios::GenerarMovimientoCuentaInversion"(:v_ComprobanteGenerado, :p_IdEmpresa, :p_IdUsuario, :p_FechaSistema,
                                                                            v_out_CargoAbonoGenerado, v_out_AdvertenciasContadorCInversion, 
                                                                            v_out_AdvertenciasDetalleCInversion, v_out_ErroresContadorCInversion,                                                                              
                                                                            v_out_ErroresDetalleCInversion);
                                                                             
     --Envia a Custodia                                                                     
   /*  Call "BSCL.Facturacion.BD.Servicios::GenerarMovimientoCustodia"(:v_ComprobanteGenerado, :p_IdEmpresa, :p_FechaSistema,
                                                                      p_out_IdMovimientoComprobante,
                                                                      p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                                                                      p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                                                                      v_out_AdvertenciasContadorCustodia, v_out_AdvertenciasDetalleCustodia, 
                                                                      v_out_ErroresContadorCustodia, v_out_ErroresDetalleCustodia);*/
    
    


     --Envia a Tesoreria
    /* Call "BSCL.Facturacion.BD.Servicios::GenerarMovimientoTesoreria"(:v_ComprobanteGenerado, :p_IdEmpresa, :p_IdUsuario,
                                                                       v_out_IngEgrGenerado,
                                                                       v_out_AdvertenciasContadorTesoreria, v_out_AdvertenciasDetalleTesoreria, 
                                                                       v_out_ErroresContadorTesoreria, v_out_ErroresDetalleTesoreria);   */                                                                 
              
     
     										--Acumula Id de Comprobantes + Comprobantes Automatico
   												  /*v_ComprobanteGenerado = Select "IdComprobante" 
                    						         From :p_out_ComprobanteGeneradoCmbteAut Union All
                        								     Select "IdComprobante" 
                         								    From :v_ComprobanteGenerado;*/
     
     --Acumula Id de Cuenta Inversion
    p_out_CargoAbonoGenerado = Select "IdCargoAbonoMovimiento"
                                From :v_out_CargoAbonoGenerado Union All                                
                                Select "IdCargoAbonoMovimiento"
                                From :p_out_CargoAbonoGenerado; 
                                
     --Acumula Id de Tesoreria
   /*  p_out_IngEgrGenerado = Select "IdMovIngEgr"
                            From :v_out_IngEgrGenerado Union All                            
                            Select "IdMovIngEgr"                            
                            From :p_out_IngEgrGenerado;  */  
                                
     
     p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContadorCInversion;-- + v_out_AdvertenciasContadorCustodia;-- + v_out_AdvertenciasContadorTesoreria; 
                                              
     p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                 From :v_out_AdvertenciasDetalleCInversion Union All 
                                /* Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                 From :v_out_AdvertenciasDetalleCustodia Union All */
                                 /*Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                 From :v_out_AdvertenciasDetalleTesoreria Union All*/
                                 Select "Codigo","RequiereAutorizacion", "Descripcion"
                                 From :p_out_AdvertenciasDetalle;

     p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContadorCInversion;-- + v_out_ErroresContadorCustodia;-- + v_out_ErroresContadorTesoreria;
                                     
     p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                            From :v_out_ErroresDetalleCInversion Union All 
                            /*Select "Codigo", "Descripcion" 
                            From :v_out_ErroresDetalleCustodia Union All*/
                            /*Select "Codigo", "Descripcion" 
                            From :v_out_ErroresDetalleTesoreria Union All*/
                            Select "Codigo", "Descripcion" 
                            From :p_out_ErroresDetalle; 
                                                       							
END;
