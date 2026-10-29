SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::TestServicioCarga";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::TestServicioCarga"
 (out a integer, out b integer) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	---READS SQL DATA 
AS
BEGIN
    --- 1) Leer Datos PCBCAPACITA y cargar en tabla temporal
	create local temporary table #t_MovtosFacturarOrigen
		(
			t_IdConcepto              NVARCHAR(6)
			,t_IdCliente               BIGINT
			,t_IdPortafolio            BIGINT
			    
			,t_IdTipoInstrumento       NVARCHAR(3)
			,t_IdClasificacionTipoInstrumento NVARCHAR(3)
			,t_IdTipoProducto          NVARCHAR(4)
			,t_IdMercado               NVARCHAR(4)  
			,t_IdMoneda                NVARCHAR(3)
			,t_IdComprobante           BIGINT
			,t_IdTipoOperacion         NVARCHAR(6)
			,t_IdBoveda                NVARCHAR(3)
			,t_IdCorredoraContraparte  BIGINT
			,t_IdCondicionLiquidacion  NVARCHAR(2)
			,t_IdBolsa                 NVARCHAR(3)
			,t_IdTipoConvenio          NVARCHAR(3)   
			,t_IdEstadoFacturacion     NVARCHAR(3)
			,t_IdOrden                 BIGINT   
			,t_IdTransaccion           BIGINT 
			,t_FolioTransaccion        BIGINT
			,t_MontoAPlazo             DECIMAL(26,8)
			,t_Tasa                    DECIMAL(26,8)
			,t_Cantidad                DECIMAL(26,8)
		    ,t_Precio                  DECIMAL(26,8)
		    ,t_Monto                   DECIMAL(26,8)
		    ,t_Spread                  DECIMAL(26,8)
		    ,t_Plazo                   DECIMAL(26,8)
		    ,t_FechaOperacion          TIMESTAMP
		    ,t_FechaTransaccion		   TIMESTAMP
		    ,t_FechaOrden              TIMESTAMP
		    ,t_FechaVencimiento        TIMESTAMP    
		);
	
	insert into #t_MovtosFacturarOrigen 
	select 
		---DATOS SEBRA
		 "BSCL"."GES_TBCTMFRV"."COD_MOVTO"			--as  IdConceptoMovimiento
		,"BSCL"."GES_CUENTA"."per_id"				--as  IdCliente                 	                                                        
		,"BSCL"."GES_TBCTMFRV"."SEC_RUT_DEST" 		--as  IdPortafolio
		,"BSCL"."GES_TBCTMFRV"."MERCADO"	 		--as  IdTipoInstrumento         	                                                    
		,"BSCL"."GES_TBCTMFRV"."MERCADO"	 		--as  IdClasificacionTipoInstrumento
		,CasE when "BSCL"."GES_TBCTMFRV"."MERCADO" = 'AC' then 'INAC' ELSE ' ' END --as IdTipoProducto            	                      
		,"BSCL"."GES_TBCTMFRV"."IND_BOLSA"	 		--as  IdMercado
		,"BSCL"."GES_TBCTMFRV"."MON_MORIGEN"    	--as  IdMoneda                  	                                              
		,"BSCL"."GES_TBCTMFRV"."SEC_MOVTO_FAC"  	--as  IdComprobante             	                                              
		,"BSCL"."GES_TBCTMFRV"."COD_MOVTO"			--as  IdTipoOperacion
		,"BSCL"."GES_TBCTMFRV"."COD_BOVEDA"			--as  IdBoveda                  	                                                
		,"BSCL"."GES_TBCTMFRV"."COD_CORR_CONTRA" 	--as  IdCorredoraContraparte    	                                              
		,"BSCL"."GES_TBCTMFRV"."IND_LIQUID"		 	--as  IdCondicionLiquidacion    	                                                
		,"BSCL"."GES_TBCTMFRV"."IND_BOLSA"			--as  IdBolsa
		,"BSCL"."GES_TBCTMFRV"."IND_CONV"			--as  IdTipoConvenio            	                                                                     	    
		,"BSCL"."GES_TBCTMFRV"."EST_MOVTO_FAC"		--as  IdEstadoFacturacion                                                       
		,"BSCL"."GES_TBCTMFRV"."SEC_ORDEN"			--as  IdOrden
		,CASE WHEN LENGTH("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS" )= 0 THEN 0 ELSE TO_INTEGER ("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS") END --as  IdTransaccion         	                                                
		,CASE WHEN LENGTH("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS" )= 0 THEN 0 ELSE TO_INTEGER ("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS") END --as  FolioTransaccion
		,"BSCL"."GES_TBCTMFRV"."MONTO_APLAZO"		--as  MontoAPlazo
		,"BSCL"."GES_TBCTMFRV"."TASA"				--as  Tasa                      	                                                    
		,"BSCL"."GES_TBCTMFRV"."CANTIDAD"			--as  Cantidad                  	                                                  
		,"BSCL"."GES_TBCTMFRV"."PRECIO"				--as  Precio                    	                                                  
		,"BSCL"."GES_TBCTMFRV"."MONTO"				--as  Monto
		,"BSCL"."GES_TBCTMFRV"."SPREAD"				--as  Spread                                                                        
		,"BSCL"."GES_TBCTMFRV"."PLAZO_PRIMA"		--as  Plazo
		,TO_TIMESTAMP ("BSCL"."GES_TBCTMFRV"."FEC_MVTO", 'YYYY-MM-DD') 						--as  FechaOperacion            	                                                               	                                                                            
		,TO_TIMESTAMP (concat(substring("BSCL"."GES_TBCTMFRV"."FEC_TRANS",1,10) ,
			case when length(substring("BSCL"."GES_TBCTMFRV"."FEC_TRANS",11,9)) < 9 then ':00:00:00'
			else substring("BSCL"."GES_TBCTMFRV"."FEC_TRANS",11,9)
			end),
		 'YYYY-MM-DD:HH24:MI:SS') -- as FechaTransaccion origen '2012-12-07:11:00:00.000000'	
		,TO_TIMESTAMP("BSCL"."GES_TBCTMFRV"."FEC_ORDEN", 'YYYY-MM-DD') 						--as  FechaOrden 
		,TO_TIMESTAMP("BSCL"."GES_TBCTMFRV"."FEC_VCTO_TP",'YYYY-MM-DD')						--as  FechaVencimiento
		--select "BSCL"."GES_TBCTMFRV"."Nemo"		--as  IdInstrumento             	                                                                                          
		from  "BSCL"."GES_TBCTMFRV" inner join "BSCL"."GES_CUENTA"                                                              
				on  "BSCL"."GES_TBCTMFRV"."RUT_DEST"     = "BSCL"."GES_CUENTA"."RUT_CLI"                                            
				and	"BSCL"."GES_TBCTMFRV"."SEC_RUT_DEST" = "BSCL"."GES_CUENTA"."CUENTA";
				     
   --- 2) Leer tabla temporal y 
   			-- 2.1 Cargar tabla de Movimientos
   				---Generar Folios y Cargar Movimientos, Comisiones Formas de Pago
		  		BEGIN  
		   			declare v_new_price decimal(15,2);
		   			declare v_sec_movto INTEGER;
		   			
					Declare CURSOR c_table FOR  
							SELECT t_IdConcepto              
									,t_IdCliente              
									,t_IdPortafolio           
									,t_IdTipoInstrumento      
									,t_IdClasificacionTipoInstrumento
									,t_IdTipoProducto          
									,t_IdMercado               
									,t_IdMoneda                
									,t_IdComprobante           
									,t_IdTipoOperacion         
									,t_IdBoveda                
									,t_IdCorredoraContraparte  
									,t_IdCondicionLiquidacion  
									,t_IdBolsa                 
									,t_IdTipoConvenio          
									,t_IdEstadoFacturacion     
									,t_IdOrden                 
									,t_IdTransaccion           
									,t_FolioTransaccion        
									,t_MontoAPlazo             
									,t_Tasa                    
									,t_Cantidad                
									,t_Precio                  
									,t_Monto                   
									,t_Spread                  
									,t_Plazo                   
									,t_FechaOperacion          
									,t_FechaTransaccion		  
									,t_FechaOrden              
									,t_FechaVencimiento  
							from #T_MOVTOSFACTURARORIGEN;
					FOR cur_row as c_table DO
						Select "BSCL"."BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL Into v_sec_movto from DUMMY;				
						Insert Into  "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante"  
						       (
						        "IdMovimientoComprobante"
						        
						        ,"IdConceptoMovimiento","IdCliente","IdPortafolio","IdTipoInstrumento","IdClasificacionTipoInstrumento","IdTipoProducto","IdMercado","IdMoneda","IdComprobante"
						        ,"IdTipoOperacion","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","IdBolsa","IdTipoConvenio","IdEstadoFacturacion","IdOrden","IdTransaccion","FolioTransaccion"
						        ,"MontoAPlazo","Tasa","Cantidad","Precio","Monto","Spread","Plazo","FechaOperacion","FechaTransaccion","FechaOrden","FechaVencimiento"
																
								,"IdEmpresa","SecuenciaAgrupacion","IdAgente","IdSucursal","IdCartera","IdInstrumento","IdCuentaInversion","IdMovimientoRef","IdTipoReajuste","IdTipoTituloMovimiento"
								,"IdTipoPagoCobroMovimiento","IdTipoTransaccion","IdFormaDePago","LineaOrden","IdTipoCanalIngreso","TasaAPlazo","ValorParidad","ValorCaptacionMonedaOrigen"
								,"FolioComprobante","MontoDescuento","MontoInteres","Glosa","ValorRescate","TasaAgente","CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad"
								,"NombreLargoCliente","DireccionFacturacion","NombreAgente","NombreSucursal","FolioCartera","CodigoCuentaInversion","CodigoTipoProducto","CodigoTipoOperacion"
								,"CodigoBolsa","NombreTipoInstrumento","NombreClasificacionTipoInstrumento","NemotecnicoInstrumento","RazonSocialEmisor","TasaEmision","EsComprobIndividual"
								,"EsCuentaPropia","EsOrdenDirecta","EsEnvio","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","IdUsuario","FechaIngresoRegistro","FechaSistema"
								,"FechaEmision","FechaLiquidacion","FechaParidad"
							)
						 values( :v_sec_movto
						 		,cur_row.t_IdConcepto,cur_row.t_IdCliente,cur_row.t_IdPortafolio,cur_row.t_IdTipoInstrumento,cur_row.t_IdClasificacionTipoInstrumento,cur_row.t_IdTipoProducto,cur_row.t_IdMercado,cur_row.t_IdMoneda,cur_row.t_IdComprobante           
								,cur_row.t_IdTipoOperacion,cur_row.t_IdBoveda,cur_row.t_IdCorredoraContraparte,cur_row.t_IdCondicionLiquidacion,cur_row.t_IdBolsa,cur_row.t_IdTipoConvenio,cur_row.t_IdEstadoFacturacion,cur_row.t_IdOrden,cur_row.t_IdTransaccion,cur_row.t_FolioTransaccion        
								,cur_row.t_MontoAPlazo,cur_row.t_Tasa,cur_row.t_Cantidad,cur_row.t_Precio,cur_row.t_Monto,cur_row.t_Spread,cur_row.t_Plazo,cur_row.t_FechaOperacion,cur_row.t_FechaTransaccion,cur_row.t_FechaOrden
								,cur_row.t_FechaVencimiento
								,'1'				--as  IdEmpresa                                                                                             	                                                    
								,0											--as  SecuenciaAgrupacion       	                                                                              
								,'0'								 		--as  IdAgente 				--"BSCL"."GES_TBCTMFRV"."COD_AGENTE"
								,'0'								 		--as  IdSucursal 			--"BSCL"."GES_TBCTMFRV"."COD_SUC"
								,0									 		--as  IdCartera                 	                                                                                    
								,12									 		--as  IdInstrumento  		-- HOMOLOGAR desde "BSCL"."GES_TBCTMFRV"."NEMO"           	                                                      
								,1 								 			--as  IdCuentaInversion    	--'HOM IdMoneda' 
								,0											--as  IdMovimientoRef           	                                                                            
								,0											--as  IdTipoReajuste            	                                                                            
								,'NOM'										--as  IdTipoTituloMovimiento    	                                                                          
								,0											--as  IdTipoPagoCobroMovimiento 	                                                                            
								,0											--as  IdTipoTransaccion         	                                                                                   	                                              
								,0 	 										--as  IdFormaDePago   		--- homologar desde "BSCL"."GES_TBCTMFRV"."IND_PAGO_COBRO"                   	                                                
								,0											--as  LineaOrden                	                                                                            
								,1											--as  IdTipoCanalIngreso        	                                                                                                                                   	                                                
								,0											--as  TasaAPlazo
								,0											--as  ValorParidad              	                                                                            
								,0											--as  ValorCaptacionMonedaOrigen	                                                                            
								,0											--as  FolioComprobante          	                                                                            
								,0											--as  MontoDescuento            	                                                                            
								,0											--as  MontoInteres              	                                                                            
								,'A'										--as  Glosa                     	                                                                            
								,0											--as  ValorRescate              	                                                                            
								,0											--as  TasaAgente                	                                                                            
								,0											--as  CodigoCanalIngreso        	                                                                            
								,''											--as  DocumentoIdentidad        	                                                                            
								,0											--as  TipoDocumentoIdentidad   
								,' '										--as  NombreLargoCliente        	                                                                            
								,' '										--as  DireccionFacturacion      	                                                                            
								,' '										--as  NombreAgente              	                                                                            
								,' '										--as  NombreSucursal            	                                                                            
								,0											--as  FolioCartera              	                                                                            
								,0 											--as  CodigoCuentaInversion --'HOM IdMoneda'
								,' '										--as  CodigoTipoProducto        	                                                                            
								,0 											--as  CodigoTipoOperacion  --'HOM COD_MOVTO'
								,0 											--as  CodigoBolsa --'HOM Bolsa'
								,'HOM Tipo Ins' 							--as  NombreTipoInstrumento     	                                                                      
								,'HOM CalsifIns'							--as  NombreClasificacionTipoInstrumento 	                                                              
								,'HOm Ins'									--as  NemotecnicoInstrumento    	                                                                        
								,'HOM EMISOR'								--as  RazonSocialEmisor         	                                                                        
								,'0'										--as  TasaEmision      
								,'0'										--as  EsComprobIndividual       	                                                                            
								,'0'										--as  EsCuentaPropia            	                                                                            
								,'0'										--as  EsOrdenDirecta            	                                                                            
								,'0'										--as  EsEnvio                   	                                                                            
								,'0'										--as  EsPrecioPrima             	                                                                            
								,'0'										--as  EsEnRueda                 	                                                                            
								,'0'										--as  EsRuteo                   	                                                                            
								,'0'										--as  EsIngresoAutomatico       	                                                                            
								,'0'										--as  IdUsuario
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 	--as FechaIngresoRegistro -- se calcula      	
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  Fechasistema         -- se calcula
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  FechaEmision              	                                                                            
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  FechaLiquidacion          	                                                                        
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  FechaParidad
								);
					END FOR; 
				END;	
   			-- 2.2 Cargar tabla de Comisiones
   			
   			-- 2.3 Formas de Pago
   			
   --- 3) Actualizar Con datos Homologados

   /* 
   Select 
      "BSCL"."BSCL.Facturacion.BD.Secuencias::Movimiento".NEXTVAL
   Into a    
   from DUMMY;
   */
   /*Insert Into  "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante"  
       ("IdMovimientoComprobante",
        "IdEmpresa",
        "IdConceptoMovimiento",        
		"SecuenciaAgrupacion",
        "IdCliente",
        "IdPortafolio",        
        "IdAgente",
        "IdSucursal",
        "IdCartera",
        "IdInstrumento",
        "IdTipoInstrumento",
        "IdClasificacionTipoInstrumento",
        "IdTipoProducto",
        "IdMercado",
        "IdCuentaInversion",
        "IdMoneda",
        "IdComprobante",
        "IdTipoOperacion",
        "IdMovimientoRef",
        "IdBoveda",
        "IdCorredoraContraparte",
        "IdCondicionLiquidacion",
        "IdBolsa",
        "IdTipoReajuste",
        "IdTipoConvenio",
        "IdTipoTituloMovimiento",
        "IdTipoPagoCobroMovimiento",
		"IdTipoTransaccion",
		"IdEstadoFacturacion",
		"IdFormaDePago",
		"IdOrden",
		"LineaOrden",
		"IdTipoCanalIngreso",
		"IdTransaccion",
		"FolioTransaccion",
		"TasaAPlazo",
		"MontoAPlazo",
		"ValorParidad",
		"ValorCaptacionMonedaOrigen",
		"FolioComprobante",		
		"Tasa",
		"Cantidad",
		"Precio",
		"Monto",
		"MontoDescuento",
		"MontoInteres",
		"Glosa",
		"ValorRescate",
		"TasaAgente",
		"Spread",
		"Plazo",
		"CodigoCanalIngreso",		
		"DocumentoIdentidad",
		"TipoDocumentoIdentidad",
		"NombreLargoCliente",
		"DireccionFacturacion",		
		"NombreAgente",
		"NombreSucursal",
		"FolioCartera",
		"CodigoCuentaInversion",
		"CodigoTipoProducto",
		"CodigoTipoOperacion",
		"CodigoBolsa",
		"NombreTipoInstrumento",
		"NombreClasificacionTipoInstrumento",
		"NemotecnicoInstrumento",
		"RazonSocialEmisor",		
		"TasaEmision",		
		"EsComprobIndividual",
		"EsCuentaPropia",
		"EsOrdenDirecta",
		"EsEnvio",
		"EsPrecioPrima",
		"EsEnRueda",
		"EsRuteo",
		"EsIngresoAutomatico",
		"IdUsuario",  
		"FechaOperacion",  
		"FechaIngresoRegistro",
		"FechaSistema",
		"FechaTransaccion",
		"FechaEmision",
        "FechaLiquidacion",
		"FechaOrden",
		"FechaVencimiento",
		"FechaParidad"
       )  
 */   
           
END;
