SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CargarDatosMovimientosComprobantes";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CargarDatosMovimientosComprobantes"
 (
 IN  p_IdCarga 			   BIGINT,
 IN  p_FechaCarga          VARCHAR(10),
 IN  p_IdEmpresa 		   VARCHAR(40),
 OUT p_TotalCargados       BIGINT,
 OUT p_out_ErroresContador BIGINT,
 OUT p_out_ErroresDetalle  table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))	
 ) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	---READS SQL DATA 
AS
BEGIN	
	-- VARIABLES VALIDACIONES BASICAS
	DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
    DECLARE v_out_Mensaje	 		NVARCHAR(520); --MENSAJE SALIDA
    DECLARE v_out_CodigoErrorSQL 	BIGINT;
    
    DECLARE v_erroroutmsg  			nvarchar(520);    
--    DECLARE v_Obligatorios	 		NVARCHAR(100);
    DECLARE v_TotalMovtos 			BIGINT;
    DECLARE v_SecuenciaMovto		BIGINT;
    
    -- Para LOG
    DECLARE v_out_refID				BIGINT;    
    DECLARE v_out_ErroresContador	BIGINT; 
    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	    
    

	--------------------------------------------------------- CONTROL DE ERRORES
	--- Controlado
	DECLARE SALIDA_CONTROLADA CONDITION;    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF :p_out_ErroresContador = 0 THEN	 
	 	    p_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
		END IF;		 					 	
	END;	

	--- Excepcion
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			p_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
			RESIGNAL; 
		END IF;			
	END;
	
	-- 0) Capturar N°DATOS LEIDOS DEL ORIGEN
    select Count(1) into v_TotalMovtos from  "BSCL"."GES_TBCTMFRV" inner join "BSCL"."GES_CUENTA" on  ("BSCL"."GES_TBCTMFRV"."RUT_DEST" = "BSCL"."GES_CUENTA"."RUT_CLI" and "BSCL"."GES_TBCTMFRV"."SEC_RUT_DEST" = "BSCL"."GES_CUENTA"."CUENTA");
		
	
    -- 1) Leer Datos PCBCAPACITA y cargar en tabla temporal
		create local temporary table #t_MovtosFacturarOrigen
		(			 
			 t_IdCliente               BIGINT
			,t_IdPortafolio            BIGINT
			,t_IdCorredoraContraparte  BIGINT
						 
			,t_IdComprobante           BIGINT
			,t_IdTipoConvenio          NVARCHAR(3)   
			,t_IdOrden                 BIGINT    
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
			,t_IdClasificacionTipoInstrumento NVARCHAR(3)
			,t_IdTipoProducto          NVARCHAR(4)
			,t_IdMercado               NVARCHAR(4)
			,t_IdMoneda                NVARCHAR(3)						
			,t_IdBoveda                NVARCHAR(3)
			,t_IdCondicionLiquidacion  NVARCHAR(2)			
			,t_IdBolsa                 NVARCHAR(3)		
			,t_IdEstadoFacturacion     NVARCHAR(3)
			,t_IdConcepto              NVARCHAR(6)
			,t_IdAgente				   BIGINT
			,t_IdSucursal			   BIGINT			
			,t_IdTipoOperacion         NVARCHAR(6)
			,t_CodigoTipoProducto      NVARCHAR(40)
			,t_CodigoTipoOperacion     NVARCHAR(40)
			,t_IdInstrumento		   BIGINT
			,t_IdTipoInstrumento       NVARCHAR(3) 
			,t_IdTransaccion           BIGINT
		);
	        
		insert into #t_MovtosFacturarOrigen
		 
		select
		---Datos referenciados
		 IFNULL(Cliente."IdCliente"      ,0)     											as  IdCliente   -- "GES_CUENTA"."per_id"
		,IFNULL(Portafolio."IdPortafolio", "BSCL"."GES_TBCTMFRV"."SEC_RUT_DEST") 			as  IdPortafolio	
		,IFNULL(Contraparte."IdContraparte","BSCL"."GES_TBCTMFRV"."COD_CORR_CONTRA") 		as  IdCorredoraContraparte
				       	                                                    
		,"BSCL"."GES_TBCTMFRV"."SEC_MOVTO_FAC"  	--as  IdComprobante						-- Folio Factura             	                                                
		,'FAC'										--    "BSCL"."GES_TBCTMFRV"."IND_CONV"	--as  IdTipoConvenio				 
		,"BSCL"."GES_TBCTMFRV"."SEC_ORDEN"			--as  IdOrden		
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
		
   	    ---- Homologacion Directa
   		,ClasificacionTipoInstrumento."ValorDestino" as  IdClasificacionTipoInstrumento	
		,TipoProducto."ValorDestino"				 as  IdTipoProducto 
		,Mercado."ValorDestino"                      as  IdMercado
		,Moneda."ValorDestino"                       as  IdMoneda
		,Boveda."ValorDestino" 						 as  IdBoveda		
		,CondLiq."ValorDestino"		 				 as  IdCondicionLiquidacion    	                                                
		,Bolsa."ValorDestino"						 as  IdBolsa		
		,EstadoFacturacion."ValorDestino"		     as  IdEstadoFacturacion
		,ifnull(Concepto."ValorDestino",'')			 as  IdConceptoMovimiento		
		,Agente."ValorDestino"			 			 as  IdAgente
		,Sucursal."ValorDestino" 					 as  IdSucursal
		
		----- Compuesto
		,TipoOperacion."ValorDestino"				 as  IdTipoOperacion
				
		----- Codigos Tablas Hana
		,TipoProducto_T."Codigo"					 as  CodigoTipoProducto				
		,TipoOperacion_T."Codigo"					 as  CodigoTipoOperacion
		,Instrumento_T."IdInstrumento"				 as  IdInstrumento
		,Instrumento_T."IdTipoInstrumento"	 		 as  IdTipoInstrumento  				-- es mixta, no se llena directamente desde 1 campo
		--,Transaccion_T."IdTransaccion" 				 as  IdTransaccion
		, CASE WHEN LENGTH("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS" )= 0 THEN 0 ELSE TO_INTEGER ("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS") END as  IdTransaccion

				/*,0									 		--as  IdCartera                 	                                                                                          	                                                      
								,1 								 			--as  IdCuentaInversion    	--'HOM IdMoneda' 
								,0											--as  IdMovimientoRef           	                                                                            
								,0											--as  IdTipoReajuste            	                                                                            
								,'NOM'										--as  IdTipoTituloMovimiento    	                                                                          
								,0											--as  IdTipoPagoCobroMovimiento 	                                                                            
								,0											--as  IdTipoTransaccion         	                                                                                   	                                              
								,0 	 										--as  IdFormaDePago   		--- homologar desde "BSCL"."GES_TBCTMFRV"."IND_PAGO_COBRO"
								,'0'										--as  IdUsuario
								,1											--as  IdTipoCanalIngreso

								,0											--as  CodigoCanalIngreso        	                                                                            
								,''											--as  DocumentoIdentidad        	                                                                            
								,0											--as  TipoDocumentoIdentidad   
								,' '										--as  NombreLargoCliente        	                                                                            
								,' '										--as  DireccionFacturacion      	                                                                            
								,' '										--as  NombreAgente              	                                                                            
								,' '										--as  NombreSucursal            	                                                                            
								,0											--as  FolioCartera  
								,0 											--as  CodigoCuentaInversion --'HOM IdMoneda'								
								,0 											--as  CodigoBolsa --'HOM Bolsa'
								,'HOM Tipo Ins' 							--as  NombreTipoInstrumento     	                                                                      
								,'HOM CalsifIns'							--as  NombreClasificacionTipoInstrumento 	                                                              
								,'HOm Ins'									--as  NemotecnicoInstrumento    	                                                                        
								,'HOM EMISOR'								--as  RazonSocialEmisor 		     
		     */
		     
		from  "BSCL"."GES_TBCTMFRV" 
				inner join "BSCL"."GES_CUENTA"                                                              
				  on  ("BSCL"."GES_TBCTMFRV"."RUT_DEST"     = "BSCL"."GES_CUENTA"."RUT_CLI" and "BSCL"."GES_TBCTMFRV"."SEC_RUT_DEST" = "BSCL"."GES_CUENTA"."CUENTA")
				
		        inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as ClasificacionTipoInstrumento
				  on  (ClasificacionTipoInstrumento."TipoValor" = 'ClasificacionTipoInstrumento' and  
				  	   ClasificacionTipoInstrumento."Entidad" = 'FACTURA' and
				       ClasificacionTipoInstrumento."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."MERCADO"
				      )
			      
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TipoProducto
				  on  (TipoProducto."TipoValor" = 'TipoProducto' and  
				  	   TipoProducto."Entidad" = 'FACTURA' and
				       TipoProducto."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."PRODUCTO"
				      )
									inner join "BSCL"."BSCL.Plataforma.BD::INS.TipoProducto" as TipoProducto_T
									on  (TipoProducto."ValorDestino" = TipoProducto_T."IdTipoProducto"
									    )

				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TipoOperacion
				  on  (TipoOperacion."TipoValor" = 'TipoOperacion' and  
				  	   TipoOperacion."Entidad" = 'FACTURA' and
				       trim(TipoOperacion."ValorOrigen") = trim(concat(ltrim(rtrim("BSCL"."GES_TBCTMFRV"."COD_MOVTO")),ltrim(rtrim("BSCL"."GES_TBCTMFRV"."PRODUCTO"))))
				      )
									inner join "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion_T
									on  (TipoOperacion."ValorDestino" = TipoOperacion_T."IdTipoOperacion"
									    )
				
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as Mercado
				  on  (Mercado."TipoValor"   = 'Mercado' and  
				  	   Mercado."Entidad"     = 'FACTURA' and
				       Mercado."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."IND_BOLSA"
				      )

				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as Moneda
				  on  (Moneda."TipoValor"   = 'Moneda' and  
				  	   Moneda."Entidad"     = 'FACTURA' and
				       Moneda."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."MON_MORIGEN"
				      )
				      
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as Boveda
				  on  (Boveda."TipoValor"   = 'Boveda' and  
				  	   Boveda."Entidad"     = 'FACTURA' and
				       Boveda."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."COD_BOVEDA"
				      )

			   inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as CondLiq
				  on  (CondLiq."TipoValor" = 'CondicionLiquidacion' and  
				  	   CondLiq."Entidad" = 'FACTURA' and
				       CondLiq."ValorOrigen" = case when ifnull("BSCL"."GES_TBCTMFRV"."IND_LIQUID",'') = '' then 'PH' else "BSCL"."GES_TBCTMFRV"."IND_LIQUID" END
				      )				      

				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as Bolsa
				  on  (Bolsa."TipoValor" = 'Bolsa' and  
				  	   Bolsa."Entidad" = 'FACTURA' and
				       Bolsa."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."IND_BOLSA"
				       )
				       
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as EstadoFacturacion
				  on  (EstadoFacturacion."TipoValor" = 'EstadoFacturacion' and  
				  	   EstadoFacturacion."Entidad" = 'FACTURA' and
				       EstadoFacturacion."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."EST_MOVTO_FAC"
				      )
				      
							    inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as Instrumento_T
												on  (ltrim(rtrim(Instrumento_T."Nemotecnico")) = ltrim(rtrim("BSCL"."GES_TBCTMFRV"."NEMO"))
												    )
												    
								/*inner join "BSCL"."BSCL.Orden.BD::ORD.Transaccion" as Transaccion_T
									on  (Transaccion_T."Folio" = CASE WHEN LENGTH("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS" )= 0 THEN 0 ELSE TO_INTEGER ("BSCL"."GES_TBCTMFRV"."FOLIO_TRANS") END
									    )*/
									    
												    
				left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as Concepto
				  on  (Concepto."TipoValor" = 'Concepto' and  
				  	   Concepto."Entidad" = 'FACTURA' and
				       Concepto."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."COD_MOVTO"
				      )
				
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as Agente
				  on  (Agente."TipoValor" = 'Agente' and  
				  	   Agente."Entidad" = 'FACTURA' and
				       Agente."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."COD_AGENTE"
				      )
				      
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as Sucursal
				  on  (Sucursal."TipoValor" = 'Sucursal' and  
				  	   Sucursal."Entidad" = 'FACTURA' and
				       Sucursal."ValorOrigen" = "BSCL"."GES_TBCTMFRV"."COD_SUC"
				      )
				      				      
				--Referenciados
				
					-- Cliente
					inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DocumentoIdentidad 
								ON (ltrim(rtrim(DocumentoIdentidad."DocumentoIdentidad")) =  ltrim(rtrim("BSCL"."GES_TBCTMFRV"."RUT_DEST")))
								
					inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as Persona 
								ON (Persona."IdDocumentoIdentidad" = DocumentoIdentidad."IdDocumentoIdentidad")
								
					left outer join "BSCL"."BSCL.Cliente.BD::CLI.Cliente" as Cliente 
								ON (Persona."IdPersona" = Cliente."IdPersona" and Cliente."IdEmpresa" = p_IdEmpresa)
								
					-- Portafolio
					left outer join "BSCL.Portafolio.BD::POR.Portafolio" as Portafolio 
								ON (Portafolio."IdCliente" = Cliente."IdCliente" and Portafolio."CodigoPortafolio" = "BSCL"."GES_TBCTMFRV"."SEC_RUT_DEST")

					-- Contraparte					
					left outer join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DocumentoIdentidad_CONT 
								ON ( ltrim(rtrim(DocumentoIdentidad_CONT."DocumentoIdentidad")) =  ltrim(rtrim("BSCL"."GES_TBCTMFRV"."COD_CORR_CONTRA"))
								   )
					left outer join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as Persona_CONT  
								ON (Persona_CONT."IdDocumentoIdentidad" = DocumentoIdentidad_CONT."IdDocumentoIdentidad")
					left outer join "BSCL"."BSCL.Contraparte.BD::CNT.Contraparte" as Contraparte  
								ON (Persona_CONT."IdPersona" = Contraparte."IdPersona");					
				      
   -- 2) Leer tabla temporal
   		BEGIN
   		
   			--select * from #T_MOVTOSFACTURARORIGEN;
   			
   			-- 2.1 Cargar tabla de Movimientos: Generar Folios y Cargar Movimientos, Comisiones Formas de Pago			
			DECLARE CURSOR c_table FOR  
			SELECT   			 t_IdConcepto              
								,t_IdCliente              
								,t_IdPortafolio           
								,t_IdTipoInstrumento      									                                         
								,t_IdComprobante                                    
								,t_IdCorredoraContraparte                     
								,t_IdTipoConvenio               
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
								,t_IdClasificacionTipoInstrumento
								,t_IdTipoProducto
								,t_IdMercado
								,t_IdMoneda																	
								,t_IdBoveda
								,t_IdCondicionLiquidacion		
								,t_IdBolsa
								,t_IdEstadoFacturacion
								,t_IdTipoOperacion
								,t_CodigoTipoProducto
								,t_CodigoTipoOperacion
								,t_IdInstrumento				
								,t_IdAgente		
								,t_IdSucursal		
			from #T_MOVTOSFACTURARORIGEN;
			
			FOR cur_row as c_table DO
						Select "BSCL"."BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL Into v_SecuenciaMovto from DUMMY;
						 				
						Insert Into  "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante"  
						       (
						        "IdMovimientoComprobante"
						        
						        ,"IdConceptoMovimiento","IdCliente","IdPortafolio","IdTipoInstrumento","IdClasificacionTipoInstrumento","IdTipoProducto","IdMercado","IdMoneda","IdComprobante"
						        ,"IdTipoOperacion","IdBoveda","IdCorredoraContraparte","IdCondicionLiquidacion","IdBolsa","IdTipoConvenio","IdEstadoFacturacion","IdOrden","IdTransaccion","FolioTransaccion"
						        ,"MontoAPlazo","Tasa","Cantidad","Precio","Monto","Spread","Plazo","FechaOperacion","FechaTransaccion","FechaOrden","FechaVencimiento"
						        ,"CodigoTipoProducto","CodigoTipoOperacion","IdInstrumento"
																
								,"IdEmpresa","SecuenciaAgrupacion"
								
								,"IdAgente","IdSucursal","IdCartera","IdCuentaInversion","IdMovimientoRef","IdTipoReajuste","IdTipoTituloMovimiento"
								,"IdTipoPagoCobroMovimiento","IdTipoTransaccion","IdFormaDePago","IdUsuario","IdTipoCanalIngreso"
								
								
								,"CodigoCanalIngreso","DocumentoIdentidad","TipoDocumentoIdentidad"
								,"NombreLargoCliente","DireccionFacturacion","NombreAgente","NombreSucursal","FolioCartera","CodigoCuentaInversion"								
								,"CodigoBolsa","NombreTipoInstrumento","NombreClasificacionTipoInstrumento","NemotecnicoInstrumento","RazonSocialEmisor"
								
								,"LineaOrden","TasaAPlazo","ValorParidad","ValorCaptacionMonedaOrigen","FolioComprobante","MontoDescuento","MontoInteres","Glosa","ValorRescate","TasaAgente","TasaEmision"
								,"EsComprobIndividual","EsCuentaPropia","EsOrdenDirecta","EsEnvio","EsPrecioPrima","EsEnRueda","EsRuteo","EsIngresoAutomatico","FechaIngresoRegistro","FechaSistema"
								,"FechaEmision","FechaLiquidacion","FechaParidad","IdCarga"
							)
						 values( :v_SecuenciaMovto
						 		,cur_row.t_IdConcepto,cur_row.t_IdCliente,cur_row.t_IdPortafolio,cur_row.t_IdTipoInstrumento,cur_row.t_IdClasificacionTipoInstrumento,cur_row.t_IdTipoProducto,cur_row.t_IdMercado,cur_row.t_IdMoneda,cur_row.t_IdComprobante           
								,cur_row.t_IdTipoOperacion,cur_row.t_IdBoveda,cur_row.t_IdCorredoraContraparte,cur_row.t_IdCondicionLiquidacion,cur_row.t_IdBolsa,cur_row.t_IdTipoConvenio,cur_row.t_IdEstadoFacturacion,cur_row.t_IdOrden,cur_row.t_IdTransaccion,cur_row.t_FolioTransaccion        
								,cur_row.t_MontoAPlazo,cur_row.t_Tasa,cur_row.t_Cantidad,cur_row.t_Precio,cur_row.t_Monto,cur_row.t_Spread,cur_row.t_Plazo,cur_row.t_FechaOperacion,cur_row.t_FechaTransaccion,cur_row.t_FechaOrden
								,cur_row.t_FechaVencimiento
								,cur_row.t_CodigoTipoProducto,cur_row.t_CodigoTipoOperacion,cur_row.t_IdInstrumento
								
								,:p_IdEmpresa								--as  IdEmpresa                                                                                             	                                                    
								,0											--as  SecuenciaAgrupacion       	                                                                              
								
								,cur_row.t_IdAgente				 			--as  IdAgente 				--"BSCL"."GES_TBCTMFRV"."COD_AGENTE"
								,cur_row.t_IdSucursal						--as  IdSucursal 			--"BSCL"."GES_TBCTMFRV"."COD_SUC"
								
								,0									 		--as  IdCartera
								                 	                                                                                          	                                                      
								,1 								 			--as  IdCuentaInversion    	--'HOM IdMoneda' 
								,0											--as  IdMovimientoRef           	                                                                            
								,0											--as  IdTipoReajuste            	                                                                            
								,'NOM'										--as  IdTipoTituloMovimiento    	                                                                          
								,0											--as  IdTipoPagoCobroMovimiento 	                                                                            
								,0											--as  IdTipoTransaccion         	                                                                                   	                                              
								,0 	 										--as  IdFormaDePago   		--- homologar desde "BSCL"."GES_TBCTMFRV"."IND_PAGO_COBRO"
								,'3'										--as  IdUsuario
								,1											--as  IdTipoCanalIngreso

								,0											--as  CodigoCanalIngreso        	                                                                            
								,''											--as  DocumentoIdentidad        	                                                                            
								,0											--as  TipoDocumentoIdentidad   
								,' '										--as  NombreLargoCliente        	                                                                            
								,' '										--as  DireccionFacturacion      	                                                                            
								,' '										--as  NombreAgente              	                                                                            
								,' '										--as  NombreSucursal            	                                                                            
								,0											--as  FolioCartera  
								,0 											--as  CodigoCuentaInversion --'HOM IdMoneda'								
								,0 											--as  CodigoBolsa --'HOM Bolsa'
								,'HOM Tipo Ins' 							--as  NombreTipoInstrumento     	                                                                      
								,'HOM CalsifIns'							--as  NombreClasificacionTipoInstrumento 	                                                              
								,'HOm Ins'									--as  NemotecnicoInstrumento    	                                                                        
								,'HOM EMISOR'								--as  RazonSocialEmisor 
																								
								,0											--as  LineaOrden                	                                                                            
								,0											--as  TasaAPlazo
								,0											--as  ValorParidad              	                                                                            
								,0											--as  ValorCaptacionMonedaOrigen	                                                                            
								,0											--as  FolioComprobante          	                                                                            
								,0											--as  MontoDescuento            	                                                                            
								,0											--as  MontoInteres              	                                                                            
								,'A'										--as  Glosa                     	                                                                            
								,0											--as  ValorRescate              	                                                                            
								,0											--as  TasaAgente           
								,'0'										--as  TasaEmision      
								,'0'										--as  EsComprobIndividual       	                                                                            
								,'0'										--as  EsCuentaPropia            	                                                                            
								,'0'										--as  EsOrdenDirecta            	                                                                            
								,'0'										--as  EsEnvio                   	                                                                            
								,'0'										--as  EsPrecioPrima             	                                                                            
								,'0'										--as  EsEnRueda                 	                                                                            
								,'0'										--as  EsRuteo                   	                                                                            
								,'0'										--as  EsIngresoAutomatico       	                                                                            
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 	--as FechaIngresoRegistro -- se calcula      	
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  Fechasistema         -- se calcula
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  FechaEmision              	                                                                            
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  FechaLiquidacion          	                                                                        
								,TO_TIMESTAMP('1000-01-01', 'YYYY-MM-DD') 											--as  FechaParidad
								,:p_IdCarga
								);
					END FOR; 
			END;	
   			-- 2.2 Cargar tabla de Comisiones
   			
   			-- 2.3 Formas de Pago

	-- 3) Validar Datos Leidos v/s Datos Cargados
    select Count(1) into v_TotalMovtos    from  "BSCL"."GES_TBCTMFRV" inner join "BSCL"."GES_CUENTA" on  ("BSCL"."GES_TBCTMFRV"."RUT_DEST" = "BSCL"."GES_CUENTA"."RUT_CLI" and "BSCL"."GES_TBCTMFRV"."SEC_RUT_DEST" = "BSCL"."GES_CUENTA"."CUENTA");
	--select Count(1) into p_TotalCargados  from  #t_MovtosFacturarOrigen;
	select Count(1) into p_TotalCargados from  "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" where "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante"."IdCarga" = :p_IdCarga;
	
	IF v_TotalMovtos <>  p_TotalCargados THEN		
		v_hash= select 'DatosLeidos'   as "Codigo", v_TotalMovtos   as "Valor" FROM DUMMY UNION ALL
		        select 'DatosGrabados' as "Codigo", p_TotalCargados as "Valor" FROM DUMMY;
		CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CARGA_CON_DIFERENCIAS',:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  select 'CARGA_CON_DIFERENCIAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
		p_out_ErroresContador := 1;
		Call "BSCL"."BSCL.Plataforma.BD.Servicios::CrearLog" ('ERROR','PROCESOCARGA', 'FAC',1,:p_IdEmpresa,:v_erroroutmsg,'SIS','','',0,0,0,'E',:p_IdCarga,:v_out_refID,:v_out_ErroresContador,:v_out_ErroresDetalle);
		
		SIGNAL SALIDA_CONTROLADA;
		 						
	END IF ;
			 	
   --- 4) Actualizar Con datos Homologados
           
END;
