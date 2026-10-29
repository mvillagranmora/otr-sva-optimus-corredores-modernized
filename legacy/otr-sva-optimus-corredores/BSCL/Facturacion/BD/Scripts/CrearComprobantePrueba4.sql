SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba4";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba4"
   (
     -- Parametros de Validacion
     IN p_IdEmpresa BIGINT,  
     IN p_IdUsuario BIGINT,   
     IN p_IdModulo  NVARCHAR(3),   
   
     IN p_simularOperacion nvarchar(1),	-- S/N se usa para simular operacion (algunos casos)
     IN p_UsaReglasNegocio nvarchar(1),	-- S/N se usa en caso de servicios anidados.   
   
     -- Parametros de Proceso 
     IN p_Glosa              NVARCHAR(260) default '',    
     IN p_MovimientoFacturar table("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT,
                                   "IdTipoComprobante" NVARCHAR(3)),        
                                   
     --Resultados Internos
     -- Validaciones
     OUT v_MovimientosBloqueados table ("IdMovimientoComprobante" BIGINT, "IdEstadoFacturacion" NVARCHAR(3)),     
     OUT v_MovimientosFacturados table ("IdMovimientoComprobante" BIGINT, "IdEstadoFacturacion" NVARCHAR(3)), 
     OUT v_MovimientoAAgruparPorSecuencias table ("IdMovimientoComprobante" BIGINT, "SecuenciaAgrupacion" BIGINT), 
     OUT v_MovimientosABloquearConProblemas table ("IdMovimientoComprobante" BIGINT, "IdEstadoFacturacion" NVARCHAR(3)),
     
     --Facturacion
     OUT v_out_IdMovimientoComprobante table ("IdMovimientoComprobante" BIGINT),
     OUT v_out_ComprobanteGenerado    table ("IdComprobante" BIGINT),   
         
     --Cuenta Inversion
     OUT p_out_CargoAbonoGenerado     table ("IdCargoAbonoMovimiento" BIGINT),      
     
     --Custodia           
     OUT p_out_FolioCartera      table ("FolioCartera" BIGINT),
     OUT p_out_FolioCarteraSub   table ("FolioCarteraSub" BIGINT), 
     OUT p_out_FolioCarteraRet   table ("FolioCarteraRet" BIGINT), 
     OUT p_out_IdSubyacente      table ("FolioSubyacente" BIGINT),
     OUT p_out_IdSubyacenteRet   table ("FolioSubyacenteRet" BIGINT),
     OUT p_out_IdGarantia        table ("FolioGarantia" BIGINT),
     OUT p_out_IdGarantiaRetiro  table ("FolioGarantiaRet" BIGINT),
     
     --Tesoreria
     OUT p_out_IngEgrGenerado        table ("IdMovIngEgr" BIGINT),
     
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
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	
    DECLARE v_out_ComprobanteGeneradoModulo table ("IdComprobante" BIGINT);
    DECLARE v_out_IdMovimientoComprobanteModulo table ("IdMovimientoComprobante" BIGINT);
    
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	/*DECLARE EXIT HANDLER FOR SQLEXCEPTION
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
	END;*/	
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	
	v_out_ComprobanteGeneradoModulo = Select 0 as "IdComprobante" from dummy where 1=2;
	v_out_IdMovimientoComprobanteModulo = Select 0 as "IdMovimientoComprobante" from dummy where 1=2;	
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	v_out_ComprobanteGenerado   = Select 0 as "IdComprobante" from dummy where 1=2;	
	
	
    v_out_IdMovimientoComprobante = Select 0 as "IdMovimientoComprobante" from dummy where 1=2;
    
    
    
    
    BEGIN
		   DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 259    BEGIN END;
		   drop table #v_out_ComprobanteGenerado;
		   drop table #p_out_AdvertenciasDetalle;
		   drop table #p_out_ErroresDetalle;
		   drop table #v_out_IdMovimientoComprobante;               
	END; 
	CREATE LOCAL TEMPORARY TABLE #v_out_ComprobanteGenerado ("IdComprobante" BIGINT);
    CREATE LOCAL TEMPORARY TABLE #p_out_AdvertenciasDetalle ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
    CREATE LOCAL TEMPORARY TABLE #p_out_ErroresDetalle ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
    CREATE LOCAL TEMPORARY TABLE #v_out_IdMovimientoComprobante ("IdMovimientoComprobante" BIGINT);  
    
    
     
	BEGIN --LOGICA
		--------------------
		--Declare Variables
		--------------------
       DECLARE v_out_AdvertenciasContadorModulo, v_out_ErroresContadorModulo, 
               v_out_AdvertenciasContadorPortafolio, v_out_ErroresContadorPortafolio  INT;
            
	   DECLARE v_Portafolios                 table("IdPortafolio" BIGINT, "IdCliente" BIGINT);
	   
	   DECLARE v_MovimientoFacturarValidar   table("IdMovimientoComprobante" BIGINT, "IdTipoComprobante" NVARCHAR(3), 
	                                               "IdPortafolio" BIGINT, "IdCliente" BIGINT);   
	         
       DECLARE p_MovimientoFacturarAAgrupar  table("IdPortafolio" BIGINT, "IdCliente" BIGINT, 
                                                "IdMovimientoComprobante" BIGINT, "IdTipoComprobante" NVARCHAR(3));                                                 
       
       DECLARE Arr_IdMovimientoComprobanteValidar, Arr_IdPortafolioValidar, Arr_IdClienteValidar  BIGINT ARRAY;
       DECLARE Arr_IdTipoComprobanteValidar       NVARCHAR(3) ARRAY;
       
       DECLARE v_Arr_IdMovimientoComprobanteValidar, v_Arr_IdPortafolioValidar, v_Arr_IdClienteValidar BIGINT;
       DECLARE v_Arr_IdTipoComprobanteValidar       NVARCHAR(3);
           
       DECLARE v_a, v_b           BIGINT;
       DECLARE v_FechaSistema     NVARCHAR(10);
      
       DECLARE Arr_IdPortafolio   BIGINT ARRAY;      
       DECLARE Arr_IdCliente      BIGINT ARRAY;
       
       DECLARE v_Arr_IdPortafolio BIGINT; 
       DECLARE v_Arr_IdCliente    BIGINT;
       
       DECLARE v_CantBloqueados, v_CantidadParConfFac, v_CantFacturados, v_CantDiferenciaSecuencia, 
               v_CantidadMovimientosABloquearConProblemas SMALLINT;
       
       DECLARE v_IdTipoFacturacion NVARCHAR(4);
       DECLARE v_FusionPorPrecio, v_SeparaPorInstrumento, v_SeparaPorTransaccion, v_SeparaPorOperacion, 
               v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, v_SeparaPorSucursal, 
               v_SeparaPorFechaLiq, v_SeparaPorMoneda, v_EsAPlazo NVARCHAR(1);
               
       DECLARE p_MovimientoFacturarVCorta table ("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT,
                                                 "IdMovimientoComprobanteNuevo" BIGINT, "IdTipoComprobante" NVARCHAR(3),     
                                                 "IdInstrumentoPR" BIGINT, "Monto" DECIMAL(26,8), "DiasPlazo" DECIMAL(26,8), 
                                                 "Tasa" DECIMAL(26,8));
               
       --Diccionario 
        DECLARE p_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(40),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    	DECLARE p_out_TablaMensajes		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));
    	
    	p_TablaCodigosMensajes = Select '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes = Select '' as "Codigo", '' as "Descripcion" from dummy where 1=2;   
		
		p_MovimientoFacturarVCorta = select 0 As "IdPortafolio", 0 As "IdCliente", 0 As "IdMovimientoComprobante",
                                            0 As "IdMovimientoComprobanteNuevo", '' As "IdTipoComprobante",     
                                            0 As "IdInstrumentoPR", 0 As "Monto", 0 As "DiasPlazo", 0 As "Tasa"
                                     from dummy where 1=2;             	   
    
	   -----------------------------------------------------
		--Validaciones básicas
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
					
			---Validaciones datos de entrada (empresa, usuario, modulo)			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_out_ErroresDetalle,:v_resultadoRegla);
			if :v_resultadoRegla = 'RECH' then
				 SIGNAL SALIDA_CONTROLADA; 	
				 
			end if;			
						
			--Datos de tabla movimiento(s)
            v_MovimientoFacturarValidar = Select "IdMovimientoComprobante", "IdTipoComprobante",
                                                 "IdPortafolio", "IdCliente"
                                          From :p_MovimientoFacturar;
            
            Arr_IdMovimientoComprobanteValidar := ARRAY_AGG(:v_MovimientoFacturarValidar."IdMovimientoComprobante");
            Arr_IdTipoComprobanteValidar := ARRAY_AGG(:v_MovimientoFacturarValidar."IdTipoComprobante");
            Arr_IdPortafolioValidar := ARRAY_AGG(:v_MovimientoFacturarValidar."IdPortafolio");
            Arr_IdClienteValidar := ARRAY_AGG(:v_MovimientoFacturarValidar."IdCliente");
            FOR v_b IN 1 .. CARDINALITY(:Arr_IdMovimientoComprobanteValidar) DO 
                v_Arr_IdMovimientoComprobanteValidar := :Arr_IdMovimientoComprobanteValidar[:v_b]; 
                v_Arr_IdTipoComprobanteValidar := :Arr_IdTipoComprobanteValidar[:v_b]; 
                v_Arr_IdPortafolioValidar := :Arr_IdPortafolioValidar[:v_b]; 
                v_Arr_IdClienteValidar := :Arr_IdClienteValidar[:v_b]; 
               
                --Id Movimiento
                If length(:v_Arr_IdMovimientoComprobanteValidar) = 0 or :v_Arr_IdMovimientoComprobanteValidar = 0 then
				   p_TablaCodigosMensajes = Select 'MOVIMIENTO_FACTURACION_INVALIDO' AS "CodigoMensaje", 'IdMovimientoComprobante' AS "CodigoDato", 
				                                   :v_Arr_IdMovimientoComprobanteValidar AS "ValorDato"
				                            From DUMMY Union All
				                            Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                            From :p_TablaCodigosMensajes;	 				  
								   
	            Else
	               CALL "BSCL.Facturacion.BD.Reglas::MovimientoFacturacionPortafolioExiste"(:v_Arr_IdMovimientoComprobanteValidar, :v_Arr_IdClienteValidar, :v_Arr_IdPortafolioValidar, 0,:v_resultadoRegla,:v_erroroutmsg); 

 			       IF v_resultadoRegla <> 'S' THEN
				      p_TablaCodigosMensajes = Select 'MOVIMIENTO_FACTURACION_NO_EXISTE' AS "CodigoMensaje", 'IdMovimientoComprobante' AS "CodigoDato", 
				                                      :v_Arr_IdMovimientoComprobanteValidar AS "ValorDato"
  				                               From DUMMY Union All
				                               Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                               From :p_TablaCodigosMensajes;					                         
			        END IF;
			    End If;                                               
                
                --Comprobante
                If length(:v_Arr_IdTipoComprobanteValidar) = 0 or :v_Arr_IdTipoComprobanteValidar = '' then
				   p_TablaCodigosMensajes = Select 'TIPO_COMPROBANTE_INVALIDO' AS "CodigoMensaje", 'IdTipoComprobante' AS "CodigoDato", 
				                                    :v_Arr_IdTipoComprobanteValidar AS "ValorDato"
  				                            From DUMMY Union All
				                            Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                            From :p_TablaCodigosMensajes;	 					  
	            Else
	               CALL "BSCL.Facturacion.BD.Reglas::TipoComprobanteExiste"(:v_Arr_IdTipoComprobanteValidar, 0,:v_resultadoRegla,:v_erroroutmsg); 
	               IF :v_resultadoRegla <> 'S' THEN			
				      p_TablaCodigosMensajes = Select 'TIPO_COMPROBANTE_NO_EXISTE' AS "CodigoMensaje", 'IdTipoComprobante' AS "CodigoDato", 
				                                    :v_Arr_IdTipoComprobanteValidar AS "ValorDato"
  				                            From DUMMY Union All
				                            Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                            From :p_TablaCodigosMensajes;				         
			       END IF;
			    End If;	
			    
			    --Llama a diccionario para evitar seguir con el proceso debido a que IdMovimiento es el campo principal
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:p_TablaCodigosMensajes, :p_out_TablaMensajes);

			    p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
			                           From :p_out_TablaMensajes Union All
					    			   Select "Codigo", "Descripcion" 
								       From :p_out_ErroresDetalle;
								       
                --Si existen errores en las validaciones debe salir
			    select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			    IF :p_out_ErroresContador > 0 THEN
				   SIGNAL SALIDA_CONTROLADA;
			    END IF;									       
			    
			    --Validar que operacion de simultaneas sea solo la contado, no la a plazo
                Select "EsAPlazo"
                Into v_EsAPlazo
                From "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
                Where "IdMovimientoComprobante" = v_Arr_IdMovimientoComprobanteValidar;
                
                If v_EsAPlazo = 'S' Then
                   p_TablaCodigosMensajes = Select 'MOVIMIENTO_COMPROBANTE_ESAPLAZO' AS "CodigoMensaje", 'IdMovimientoComprobante' AS "CodigoDato", 
				                                :v_Arr_IdMovimientoComprobanteValidar AS "ValorDato"
  				                        From DUMMY Union All
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
                End If;
			END FOR;   
			
			--Diccionario (Se hace 2 veces debido a que el llamado de las otras validaciones dependen de estos datos (ConsultarMovimientoComprobanteBloqueados, etc))
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:p_TablaCodigosMensajes, :p_out_TablaMensajes);

			p_out_ErroresDetalle =  Select "Codigo", "Descripcion" 
			                        From :p_out_TablaMensajes Union All
									Select "Codigo", "Descripcion" 
									From :p_out_ErroresDetalle;		
	        	        	        						
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;	
			 
		
			---Validaciones de negocio			
			--Validar movimiento bloqueados
            Call "BSCL.Facturacion.BD.Servicios::ConsultarMovimientoComprobanteBloqueados"(:p_MovimientoFacturar, 
                                                                                            v_MovimientosBloqueados,
                                                                                            v_CantBloqueados);
                                                                                            
            If v_CantBloqueados > 0 Then			   
			   p_TablaCodigosMensajes = Select 'MOVIMIENTO_COMPROBANTE_BLQ' AS "CodigoMensaje", 'MovCompBloqueados' AS "CodigoDato", 
				                                :v_CantBloqueados AS "ValorDato"
  				                        From DUMMY Union All
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;					
            End If;
            
            
            --Validar movimientos facturados
            Call "BSCL.Facturacion.BD.Servicios::ConsultarMovimientoComprobanteFacturados"(:p_MovimientoFacturar, 
                                                                                            v_MovimientosFacturados,
                                                                                            v_CantFacturados);
                                                                                            
            If v_CantFacturados > 0 Then		   
			   p_TablaCodigosMensajes = Select 'MOVIMIENTO_COMPROBANTE_FAC' AS "CodigoMensaje", 'MovCompFacturados' AS "CodigoDato", 
				                                :v_CantFacturados AS "ValorDato"
  				                        From DUMMY Union All
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
			   						
            End If;

            
            --Validar Movimientos con Secuencia de Agrupacion 
            Call "BSCL.Facturacion.BD.Servicios::ConsultarMovimientoComprobanteAsociadoASecuencia"(:p_MovimientoFacturar, 
                                                                                                   v_MovimientoAAgruparPorSecuencias,
                                                                                                   v_CantDiferenciaSecuencia);
                                                                                                   
            If v_CantDiferenciaSecuencia > 0 Then     		   
			   p_TablaCodigosMensajes = Select 'MOVIMIENTO_COMPROBANTE_DIFERENCIASEC' AS "CodigoMensaje", 'MovCompDiferenciaSec' AS "CodigoDato", 
				                                :v_CantDiferenciaSecuencia AS "ValorDato"
  				                        From DUMMY Union All
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;						
            End If;  
            
            --Bloquea Movimientos que seran facturados
            CALL "BSCL.Facturacion.BD.Servicios::BloquearMovimientoComprobanteAFacturar" (:p_MovimientoFacturar,
                                                                                             v_MovimientosABloquearConProblemas,
                                                                                             v_CantidadMovimientosABloquearConProblemas);              
        
            If v_CantidadMovimientosABloquearConProblemas > 0 Then	       
		       p_TablaCodigosMensajes = Select 'MOVIMIENTO_COMPROBANTE_ABLOQUEAR' AS "CodigoMensaje", 'MovCompABloquear' AS "CodigoDato", 
				                                :v_CantidadMovimientosABloquearConProblemas AS "ValorDato"
  				                        From DUMMY Union All
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;					
            End If;                                                                             
            
            --Validar Comprobante Individual            

			--Obtiene Parametros Configuracion Facturacion
            Select Count("Valor")
            Into v_CantidadParConfFac
            From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
            Where "CodigoParametro" = 'SEPARAPOROPERACION';
        
            If v_CantidadParConfFac = 0 Then 		       
  		       p_TablaCodigosMensajes = Select 'PARAMETRO_DE_CONFIGURACION_FAC_NO_EXISTE' AS "CodigoMensaje", '' AS "CodigoDato", 
				                                '' AS "ValorDato"
  				                        From DUMMY Union All
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
  		       
            End If;            
		
			--Diccionario
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:p_TablaCodigosMensajes, :p_out_TablaMensajes);

			p_out_ErroresDetalle =  Select "Codigo", "Descripcion" 
			                        From :p_out_TablaMensajes Union All
									Select "Codigo", "Descripcion" 
									From :p_out_ErroresDetalle;		
	        	        	        						
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;					
		END;            
                                                                                         
        --Obtiene fecha de sistema
        Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(p_IdEmpresa, v_FechaSistema);       
        
        --Obtiene parametros desde modulo de facturacion
        Select "Valor"
        Into v_SeparaPorOperacion
        From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
        Where "CodigoParametro" = 'SEPARAPOROPERACION';
                   
        Select "Valor"
        Into v_SeparaPorProducto
        From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
        Where "CodigoParametro" = 'SEPARAPORPRODUCTO';
          
        Select "Valor"
        Into v_SeparaPorBolsa
        From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
        Where "CodigoParametro" = 'SEPARAPORBOLSA';
          
        Select "Valor"
        Into v_SeparaPorAgente
        From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
        Where "CodigoParametro" = 'SEPARAPORAGENTE';
          
        Select "Valor" 
        Into v_SeparaPorSucursal
        From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
        Where "CodigoParametro" = 'SEPARAPORSUCURSAL';          
          
        Select "Valor" 
        Into v_SeparaPorFechaLiq
        From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
        Where "CodigoParametro" = 'SEPARAPORFECHALIQ'; 
          
        Select "Valor" 
        Into v_SeparaPorMoneda
        From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
        Where "CodigoParametro" = 'SEPARAPORMONEDA';
                                                           
        --Obtiene Portafolio de movimiento enviados a facturar 
        v_Portafolios = Select Distinct "IdPortafolio", "IdCliente" 
                        From :p_MovimientoFacturar
                        Order By "IdCliente", "IdPortafolio";                                         
                                         
        Arr_IdPortafolio := ARRAY_AGG(:v_Portafolios."IdPortafolio");
        Arr_IdCliente := ARRAY_AGG(:v_Portafolios."IdCliente");
        FOR v_a IN 1 .. CARDINALITY(:Arr_IdPortafolio) DO 
          v_Arr_IdPortafolio := :Arr_IdPortafolio[:v_a]; 
          v_Arr_IdCliente    := :Arr_IdCliente[:v_a];          
                    
          --Obtiene Parametros Configuracion Portafolio
          /*Call "BSCL.Facturacion.BD.Servicios::ProcesarPreferenciaFacturacionPortafolio" 
               (v_Arr_IdPortafolio, v_IdTipoFacturacion, v_FusionPorPrecio, v_SeparaPorInstrumento, v_SeparaPorTransaccion, 
                v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo, v_out_ErroresContadorModulo, 
                v_out_ErroresDetalleModulo);   */   
                
          --Consultar Generacion de Prima Al Inicio en caso de Mvto. Comprobante Venta Corta
          p_MovimientoFacturarVCorta = Select MovimientoComprobante. "IdPortafolio", MovimientoComprobante. "IdCliente", 
                                              MovimientoComprobante."IdMovimientoComprobante",
                                              "BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NextVal As "IdMovimientoComprobanteNuevo", 
                                              MovimientoComprobante."IdTipoComprobante", 
                                              TipoPrestamo."IdInstrumentoPrestamo" As "IdInstrumentoPR",
                                              "Monto", "DiasPlazo", "Tasa"
                                       From "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" TipoPrestamo,
                                            "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MovimientoComprobante
                                               Inner Join :p_MovimientoFacturar As MovimientoFacturarVC
                                                  On MovimientoComprobante."IdMovimientoComprobante" = MovimientoFacturarVC."IdMovimientoComprobante"   
                                                 And MovimientoComprobante."IdPortafolio" = MovimientoFacturarVC."IdPortafolio"
                                                 And MovimientoComprobante."IdCliente" = MovimientoFacturarVC."IdCliente"                                           
                                       Where MovimientoComprobante."EsVentaCorta" = 'S'
                                         And MovimientoComprobante."IdTipoProducto" <> 'INSI'
                                         And TipoPrestamo."IdTipoPrestamo" = 'PRTVTC'
                                         And TipoPrestamo."CobraPrimaAlInicio" = 'S'
                                       Order By "IdMovimientoComprobante"; 
	                                                                  
    /*     Insert Into "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
            ( "IdMovimientoComprobante", "IdEmpresa", "IdConceptoMovimiento", "SecuenciaAgrupacion", "IdCliente",
              "IdPortafolio", "IdAgente", "IdSucursal", "IdInstrumento", "IdInstrumentoSubyacente", "IdTipoInstrumento",
              "IdTipoInstrumentoSubyacente", "IdClasificacionTipoInstrumento", "IdTipoProducto", "IdMercado", "IdCuentaInversion",
              "IdMoneda", "IdTipoComprobante", "IdComprobante", "IdTipoOperacion", "IdMovimientoRef", "IdBoveda",
              "IdCorredoraContraparte", "IdCondicionLiquidacion", "FechaLiquidacion", "IdBolsa", "IdTipoReajuste",
              "IdTipoConvenio", "IdTipoTituloMovimiento", "IdTipoPagoCobroMovimiento", "IdTipoTransaccion", "IdEstadoFacturacion",
              "IdOrden", "LineaOrden", "IdAsignacion", "IdTipoCanalIngreso", "FechaOrden", "IdTransaccion", "FolioTransaccion",
              "FechaTransaccion", "FechaVencimiento", "FechaParidad", "ValorParidad", "ValorCaptacionMonedaOrigen",
              "FolioComprobante", "Tasa", "Cantidad", "Precio", "Monto", "MontoDescuento", "MontoInteres", "Glosa",
              "ValorRescate", "TasaAgente", "Spread", "DiasPlazo", "CodigoCanalIngreso", "DocumentoIdentidad", "TipoDocumentoIdentidad",
              "NombreLargoCliente", "DireccionFacturacion", "NombreAgente", "NombreSucursal", "FolioCartera", "CodigoCuentaInversion",
              "CodigoTipoProducto", "CodigoTipoOperacion", "CodigoBolsa", "NombreTipoInstrumento", "NombreTipoInstrumentoSubyacente",
              "NombreClasificacionTipoInstrumento", "NemotecnicoInstrumento", "NemotecnicoSubyacente", "RazonSocialEmisor",
              "FechaEmision", "TasaEmision", "FechaIngresoRegistro", "FechaSistema", "EsComprobIndividual", "EsCuentaPropia",
              "EsOrdenDirecta", "EsEnvio", "EsPrecioPrima", "EsEnRueda", "EsRuteo", "EsIngresoAutomatico", "IdUsuario",
              "IdUsuarioBloqueo", "IdCarga", "EsAPlazo", "MontoAPlazo", "PrecioAPlazo", "IdMovimientoOdRef", "IdMonedaSubyacente",
              "IdMonedaLiquidacion", "ValorTipoCambio", "TasaAPlazo", "EsVentaCorta", "IdComprobanteRef", "SecMovimientoFacturarMigracion",
              "ActualizarCustodia", "FechaMovimientoComprobante", "FechaTipoCambio"               
            )
          Select MovimientoFacturarVC."IdMovimientoComprobanteNuevo", "IdEmpresa", 'FAOPRI', "SecuenciaAgrupacion", MovimientoComprobante."IdCliente", 
                 MovimientoComprobante."IdPortafolio", "IdAgente", "IdSucursal", MovimientoFacturarVC."IdInstrumentoPR", "IdInstrumento", 
                 (Select "IdTipoInstrumento" 
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
                  Where "IdInstrumento" = MovimientoFacturarVC."IdInstrumentoPR") As "IdTipoInstrumento", 
                 (Select "IdTipoInstrumentoSubyacente" 
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente"
                  Where "IdInstrumento" = MovimientoComprobante."IdInstrumento") As "IdTipoInstrumentoSubyacente",                                   
                 (Select "IdClasificacionTipoInstrumento" 
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
                  Where "IdInstrumento" = MovimientoFacturarVC."IdInstrumentoPR") As "IdClasificacionTipoInstrumento", 
                 "IdTipoProducto", "IdMercado", "IdCuentaInversion", 
                 "IdMoneda", 'FAO', "IdComprobante", 'PRESTA', MovimientoComprobante."IdMovimientoComprobante", "IdBoveda", 
                  0, "IdCondicionLiquidacion", "FechaLiquidacion", "IdBolsa", "IdTipoReajuste", 
                  "IdTipoConvenio", "IdTipoTituloMovimiento", "IdTipoPagoCobroMovimiento", "IdTipoTransaccion", 'ING', 
                  "IdOrden", "LineaOrden", "IdAsignacion", "IdTipoCanalIngreso", "FechaOrden", "IdTransaccion", "FolioTransaccion", 
                  "FechaTransaccion", "FechaVencimiento", "FechaParidad", "ValorParidad", "ValorCaptacionMonedaOrigen", 
                  "FolioComprobante", MovimientoComprobante."Tasa", "Cantidad", "Precio", 
                  ((Select "BSCL.Plataforma.BD.Utilitarios::CalcularValorMontoTasa"(MovimientoComprobante."Monto",
                                                                                    MovimientoComprobante."DiasPlazo",
                                                                                    MovimientoComprobante."Tasa",
                                                                                    'I')
                    From dummy) - MovimientoComprobante."Monto") As "Monto", "MontoDescuento", "MontoInteres", 'Creación automática por mvto. venta corta', 
                  "ValorRescate", "TasaAgente", "Spread", MovimientoComprobante."DiasPlazo", "CodigoCanalIngreso", "DocumentoIdentidad", "TipoDocumentoIdentidad", 
                  "NombreLargoCliente", "DireccionFacturacion", "NombreAgente", "NombreSucursal", "FolioCartera", "CodigoCuentaInversion", 
                  "CodigoTipoProducto", 
                  (Select "Codigo" 
	               From "BSCL.Plataforma.BD::INS.TipoOperacion" 
	               Where "IdTipoOperacion" = 'PRESTA') As "CodigoTipoOperacion", "CodigoBolsa", 
	              (Select "NombreTipoInstrumento"
                   From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
                   Where "IdInstrumento" = MovimientoFacturarVC."IdInstrumentoPR") As "NombreTipoInstrumento",                 
                 (Select "Nombre" 
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente"
                  Where "IdInstrumento" = MovimientoComprobante."IdInstrumento") As "NombreTipoInstrumentoSubyacente",                 
                 (Select "NombreClasificacionTipoInstrumento"
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
                  Where "IdInstrumento" = MovimientoFacturarVC."IdInstrumentoPR") As "NombreClasificacionTipoInstrumento",
                 (Select "Nemotecnico"
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
                  Where "IdInstrumento" = MovimientoFacturarVC."IdInstrumentoPR") As "NemotecnicoInstrumento",                 
                 (Select "Nemotecnico" 
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente"
                  Where "IdInstrumento" = MovimientoComprobante."IdInstrumento") As "NemotecnicoSubyacente",                  
                 (Select "RazonSocialEmisor"
                  From "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
                  Where "IdInstrumento" = MovimientoFacturarVC."IdInstrumentoPR") As "RazonSocialEmisor",
                 "FechaEmision", "TasaEmision", "FechaIngresoRegistro", "FechaSistema", "EsComprobIndividual", "EsCuentaPropia", 
                 "EsOrdenDirecta", "EsEnvio", 'S', 'N', "EsRuteo", "EsIngresoAutomatico", "IdUsuario", 
                 "IdUsuarioBloqueo", "IdCarga", "EsAPlazo", "MontoAPlazo", "PrecioAPlazo", "IdMovimientoOdRef", "IdMonedaSubyacente", 
                 "IdMonedaLiquidacion", "ValorTipoCambio", "TasaAPlazo", 'N', "IdComprobanteRef", "SecMovimientoFacturarMigracion", 
                 'N', "FechaMovimientoComprobante", "FechaTipoCambio"
          From :p_MovimientoFacturarVCorta MovimientoFacturarVC
            Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovimientoComprobante
               On MovimientoFacturarVC."IdMovimientoComprobante" = MovimientoComprobante."IdMovimientoComprobante";
         */ 
         
          p_MovimientoFacturarAAgrupar = Select "IdPortafolio", "IdCliente", 
                                                "IdMovimientoComprobanteNuevo" As "IdMovimientoComprobante", "IdTipoComprobante" 
                                         From :p_MovimientoFacturarVCorta Union All
                                         Select "IdPortafolio", "IdCliente", "IdMovimientoComprobante", "IdTipoComprobante" 
                                         From :p_MovimientoFacturar 
                                         Where "IdPortafolio" = v_Arr_IdPortafolio
                                           And "IdCliente" = v_Arr_IdCliente
                                         Order by "IdPortafolio", "IdCliente", "IdMovimientoComprobante";                                          
         
          --borrar   
          v_IdTipoFacturacion := 'NORM';                              
          v_SeparaPorTransaccion := 'N'; 
          v_SeparaPorInstrumento := 'N';  
          v_FusionPorPrecio := 'N';
          --      
                                   
          --Construccion Query
          IF v_IdTipoFacturacion = 'NORM' Then          
            IF v_SeparaPorTransaccion = 'N' AND v_SeparaPorInstrumento = 'N' AND v_FusionPorPrecio = 'N' Then --Caso 1
				 v_out_AdvertenciasContadorModulo	:= 0;
	v_out_ErroresContadorModulo		:= 0;
	v_out_AdvertenciasDetalleModulo	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	v_out_ErroresDetalleModulo		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	 
	  p_out_CargoAbonoGenerado = select 0 AS "IdCargoAbonoMovimiento" from dummy where 1=2;
     
      --Salida Sp Ingreso Manual de Titulo RV     
      p_out_FolioCartera = select 0 AS  "FolioCartera" from dummy where 1=2;
      p_out_FolioCarteraSub= select 0 AS  "FolioCarteraSub" from dummy where 1=2;
      p_out_FolioCarteraRet = select 0 AS "FolioCarteraRet" from dummy where 1=2;
      p_out_IdSubyacente = select 0 AS   "FolioSubyacente" from dummy where 1=2;
      p_out_IdSubyacenteRet = select 0 AS  "FolioSubyacenteRet" from dummy where 1=2;
      p_out_IdGarantia  = select 0 AS "FolioGarantia" from dummy where 1=2;
      p_out_IdGarantiaRetiro = select 0 AS  "FolioGarantiaRet" from dummy where 1=2;
      
      p_out_IngEgrGenerado = select 0 AS "IdMovIngEgr" from dummy where 1=2;
      
      /*               
               Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba2"  
                     (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema,
                       v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                       v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                       v_out_IdMovimientoComprobanteModulo, v_out_ComprobanteGeneradoModulo, 
                       p_out_CargoAbonoGenerado,
                       p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                       p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                       p_out_IngEgrGenerado,
                       v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                       v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo);  */                     
                       
            /*Else
              IF v_SeparaPorTransaccion = 'N' AND v_SeparaPorInstrumento = 'N' AND v_FusionPorPrecio = 'S' Then --Caso 2
                  Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePorPrecio" 
                       (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema,
                         v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                         v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                         v_out_ComprobanteGeneradoModulo, p_out_CargoAbonoGenerado,
                         p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                         p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                         p_out_IngEgrGenerado, 
                         v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                         v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo); 
              Else
                 IF v_SeparaPorTransaccion = 'N' AND v_SeparaPorInstrumento = 'S' AND v_FusionPorPrecio = 'S' Then --Caso 3
                    Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePorInstrumentoPorPrecio" 
                         (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema, 
                           v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                           v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                           v_out_ComprobanteGeneradoModulo, p_out_CargoAbonoGenerado,
                           p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                           p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                           p_out_IngEgrGenerado,
                           v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                           v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo);   
                 Else
                   IF v_SeparaPorTransaccion = 'S' AND v_SeparaPorInstrumento = 'S' AND v_FusionPorPrecio = 'S' Then --Caso 4
                      Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePorTransaccionPorInstrumentoPorPrecio" 
                          (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema, 
                            v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                            v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                            v_out_ComprobanteGeneradoModulo, p_out_CargoAbonoGenerado,
                            p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                            p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                            p_out_IngEgrGenerado,
                            v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                            v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo); 
                   Else
                     IF v_SeparaPorTransaccion = 'S' AND v_SeparaPorInstrumento = 'N' AND v_FusionPorPrecio = 'S' Then --Caso 5
                        Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePorTransaccionPorPrecio" 
                             (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema, 
                               v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                               v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                               v_out_ComprobanteGeneradoModulo, p_out_CargoAbonoGenerado,
                               p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                               p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                               p_out_IngEgrGenerado,
                               v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                               v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo);                            
                     Else
                       IF v_SeparaPorTransaccion = 'S' AND v_SeparaPorInstrumento = 'S' AND v_FusionPorPrecio = 'N' Then --Caso 6
                          Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePorTransaccionPorInstrumento" 
                               (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema, 
                                 v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                                 v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                                 v_out_ComprobanteGeneradoModulo, p_out_CargoAbonoGenerado,
                                 p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                                 p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                                 p_out_IngEgrGenerado,
                                 v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                                 v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo);
                       Else                       
                         IF v_SeparaPorTransaccion = 'S' AND v_SeparaPorInstrumento = 'N' AND v_FusionPorPrecio = 'N' Then --Caso 7
                            Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePorTransaccion" 
                               (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema,
                                 v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                                 v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                                 v_out_ComprobanteGeneradoModulo, p_out_CargoAbonoGenerado,
                                 p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                                 p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,
                                 p_out_IngEgrGenerado,
                                 v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                                 v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo); 
                         Else
                           IF v_SeparaPorTransaccion = 'N' AND v_SeparaPorInstrumento = 'S' AND v_FusionPorPrecio = 'N' Then --Caso 8
                              Call "BSCL.Facturacion.BD.Servicios::CrearComprobantePorInstrumento" 
                                 (:p_MovimientoFacturarAAgrupar, :p_IdEmpresa, :p_IdUsuario, :p_Glosa, v_FechaSistema, 
                                   v_SeparaPorOperacion, v_SeparaPorProducto, v_SeparaPorBolsa, v_SeparaPorAgente, 
                                   v_SeparaPorSucursal, v_SeparaPorFechaLiq, v_SeparaPorMoneda, 
                                   v_out_ComprobanteGeneradoModulo, p_out_CargoAbonoGenerado,
                                   p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
                                   p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro,    
                                   p_out_IngEgrGenerado,                             
                                   v_out_AdvertenciasContadorModulo, v_out_AdvertenciasDetalleModulo,
                                   v_out_ErroresContadorModulo, v_out_ErroresDetalleModulo); 
                           End if;
                         End if;
                       End if;
                     End if;
                   End if;
                 End if;       
               End If;*/
            End If;
                
          Else
             IF v_IdTipoFacturacion = 'CONT' Then
                v_IdTipoFacturacion := 'AGRE';
             Else
               IF v_IdTipoFacturacion = 'CONV' Then
                  v_IdTipoFacturacion := 'AGRE';
               End If;
             End If;
          End If; 
         
          --Acumula Errores de Modulos y propio  
          p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContadorModulo;
	     

   
 
	      
	      
	      INSERT INTO #p_out_AdvertenciasDetalle ("Codigo", "RequiereAutorizacion", "Descripcion") 
	      Select "Codigo", "RequiereAutorizacion", "Descripcion" 
	      From :v_out_AdvertenciasDetalleModulo;
	      
	      INSERT INTO #p_out_AdvertenciasDetalle ("Codigo", "RequiereAutorizacion", "Descripcion") 
	      Select "Codigo", "RequiereAutorizacion", "Descripcion" 
	      From :p_out_AdvertenciasDetalle;
	      
	      
	      
	  /*                                        
         p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                      From :v_out_AdvertenciasDetalleModulo Union All 
                                      Select "Codigo","RequiereAutorizacion", "Descripcion"
                                      From :p_out_AdvertenciasDetalle;
                                      
         */
          p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContadorModulo;
          
          INSERT INTO #p_out_ErroresDetalle ("Codigo", "Descripcion") 
	      Select "Codigo", "Descripcion"
	      From :v_out_ErroresDetalleModulo;
	      
	      INSERT INTO #p_out_ErroresDetalle ("Codigo", "Descripcion") 
	      Select "Codigo", "Descripcion" 
	      From :p_out_ErroresDetalle;
	                             
       /*   p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                                 From :v_out_ErroresDetalleModulo Union All
                                 Select "Codigo", "Descripcion" 
                                 From :p_out_ErroresDetalle;
                                 
                                 */
                                 
                                 
         
          --Acumula Id de Comprobantes Generados 

 		 INSERT INTO #v_out_ComprobanteGenerado ("IdComprobante") 
	     Select "IdComprobante" 
	     From :v_out_ComprobanteGeneradoModulo;
	     
	     INSERT INTO #v_out_ComprobanteGenerado ("IdComprobante") 
	     Select "IdComprobante" 
	     From :v_out_ComprobanteGenerado;
	     
       /*  v_out_ComprobanteGenerado = Select "IdComprobante" 
                                      From :v_out_ComprobanteGeneradoModulo Union All                                     
                                      Select "IdComprobante"
                                      From :v_out_ComprobanteGenerado;
       */
	   
          --Acumula Id de Mov.Comprobantes Generados

		  INSERT INTO #v_out_IdMovimientoComprobante ("IdMovimientoComprobante") 
	      Select "IdMovimientoComprobante" 
	      From :v_out_IdMovimientoComprobanteModulo;
	     
	      INSERT INTO #v_out_IdMovimientoComprobante ("IdMovimientoComprobante") 
	      Select "IdMovimientoComprobante" 
	      From :v_out_IdMovimientoComprobante;				


         /* v_out_IdMovimientoComprobante = Select "IdMovimientoComprobante"
                                          From :v_out_IdMovimientoComprobanteModulo Union All
                                          Select "IdMovimientoComprobante"
                                          From :v_out_IdMovimientoComprobante; */
                                    
      END FOR;
      --Cambiamos los union all por inserts y luego asignamos!  
	  p_out_AdvertenciasDetalle = Select "Codigo", "RequiereAutorizacion", "Descripcion" from #p_out_AdvertenciasDetalle;
      p_out_ErroresDetalle = Select  "Codigo", "Descripcion" from #p_out_ErroresDetalle;
      v_out_ComprobanteGenerado = Select "IdComprobante" from #v_out_ComprobanteGenerado;
      v_out_IdMovimientoComprobante = Select "IdMovimientoComprobante" from #v_out_IdMovimientoComprobante;   
    End; --END LOGICA
	
  END;
