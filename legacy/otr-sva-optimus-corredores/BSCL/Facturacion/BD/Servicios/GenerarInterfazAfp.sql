SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::GenerarInterfazAfp";
GO
CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::GenerarInterfazAfp"
  (
     -- Parametros de Validacion
     IN p_IdEmpresa BIGINT,  
     IN p_IdUsuario BIGINT,   
     IN p_IdModulo  NVARCHAR(3), 
     
     IN p_simularOperacion    NVARCHAR(1) ,	-- S/N se usa para simular operacion (algunos casos)                  
     IN p_UsaReglasNegocio    NVARCHAR(1) ,	-- S/N se usa en caso de servicios anidados. 
     IN p_EsIngresoAutomatico NVARCHAR(1) Default 'N',
     
     --Salidas para mensajes de advertencias y errores
     OUT p_out_InformacionContador  	 INT,
     OUT p_out_AdvertenciasContador      INT,
     OUT p_out_ErroresContador           INT   
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 

AS
BEGIN
    --------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT)
            Default Select 0 As "IdComprobante" From dummy where 1=2;
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
        Select Count(1)
        Into p_out_ErroresContador
        From "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle";
		
        Select Count(1) 
        Into p_out_AdvertenciasContador 
        From "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_AdvertenciasDetalle";
         
	END;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN			
	    v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                       '' As "ValorDato2" 
                From Dummy;	
                
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle"
		Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
							
		
		Select Count(1) 
		Into p_out_ErroresContador
		From "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle";
		
	END;
	/*
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
           
            Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle"
            Select 0 As "IdCliente", v_errorExt,::SQL_ERROR_MESSAGE 
            From dummy;
            
             								   				
			Select Count(1)
			Into p_out_ErroresContador 
			From "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle";
												
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	*/
	
	p_out_InformacionContador  := 0;
	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador	   := 0;
	
	
	Begin --Logica
       --------------------
	   --Declare Variables
	   --------------------
       DECLARE Cant_Mvto, Cant_TransaccionGenerada, Cant_Transaccion BIGINT := 0; 
                    
		
	   -----------------------------------------------------
	   --Validaciones
	   -----------------------------------------------------
       If p_UsaReglasNegocio = 'S' Then
		 BEGIN
            If p_EsIngresoAutomatico = 'S' Then
			   Call "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalleReqBas,:v_resultadoRegla);
			   If :v_resultadoRegla = 'RECH' Then
				  SIGNAL SALIDA_CONTROLADA; 	
			   End If;	   
		                  
            End If;
            
            
            /** Validacion Facturación **/
            Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_IN_Movimiento"
            Select "IdCliente", "CodigoOperadorAfp", "FechaMovimientoComprobante" 
            From "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_IN_Movimiento";
            
             
            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosGenerarInterfazAfp"(:v_resultadoRegla);
		    If :v_resultadoRegla = 'RECH' Then	
		        Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ResultadoProceso"
		        Select 'IdCliente' As "Campo", "IdCliente" as "Id",
		               'BSCL.Facturacion.BD.Servicios::GenerarInterfazAfp' as "Servicio",
		               'RECH' as "Resultado"
		        From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ClienteRechazados";
			  	
			    
			    Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle"
			    Select "IdCliente", "Codigo","Descripcion" 
			    From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ErroresDetalle";
                
			    SIGNAL SALIDA_CONTROLADA; 	
		    Else
		      --If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
			     /*p_out_AdvertenciasDetalle = Select "IdComprobante", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
			                                 From :v_ErroresDetalle;*/
			                                 
			     /*Select count(1) 
			     Into p_out_AdvertenciasContador 
			     From :p_out_AdvertenciasDetalle;*/
		      --End If;   
		    End If;  
          END;  --End Validaciones
        End If;
        
        --Simulación
        If (:p_simularOperacion = 'S') Then
		   SIGNAL SALIDA_CONTROLADA;
	    End If;
	    	    
	    --------------------
		--Negocio Aqui
		--------------------
        v_MvtoAfp = Select "IdCliente", "CodigoOperadorAfp", "FechaMovimientoComprobante"
                    From "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_IN_Movimiento" As Mvto
                    Where Mvto."IdCliente" Not In (Select "IdCliente"
                                                   From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ClienteRechazados");
        
        
        
        Select Count("IdCliente")
        Into Cant_Mvto
        From :v_MvtoAfp;
        
        If Cant_Mvto <=0 Then
           p_out_ErroresContador := p_out_ErroresContador + 1;
	                              
           SIGNAL SALIDA_CONTROLADA;
        End If;
        
                
        /** Preparacion Archivo .Csv **/    
        v_MvtoAfpAGen = Select TO_DATE("FechaTransaccion") As "FechaTransaccion",
                               ConsMvtoCmbte."FolioTransaccion",
                               Coalesce(ConsCorrEm."CodigoCorredora", 0) As "Corredora",
                               "CodigoOperadorAfp",
                               Case
                                 When ConsMvtoCmbte."AccionTipoOperacion" = 'COMPRA' Then 'C'
                                 When ConsMvtoCmbte."AccionTipoOperacion" = 'VENTA' Then 'V'
                               Else ''
                               End As "AccionTipoOperacion",
                               TO_INTEGER("Cantidad") As "Cantidad",
                               TO_DECIMAL("Precio", 26,4) As "Precio",
                               TO_TIME("FechaTransaccionHora") As "HoraTransaccion",
                               "NemotecnicoInstrumento",
                               TO_DECIMAL("Monto",26,2) As "Total",
                               Case
                                 When "IdBoveda" = 'DCV' Then 'D'
                               Else ''
                               End As "Boveda",                               
                               "IndicadorOrdenDirecta" As "OD",                               
                               Case  
                                 When ConsMvtoCmbte."IdBolsa" = 'BCS' Then '1'  
                                 When ConsMvtoCmbte."IdBolsa" = 'BEC' Then '2'  
                               Else ''
                               End As "Bolsa",
                               ConsMvtoCmbte."CodigoCondicionDeLiquidacion" As "CondicionDeLiquidacion",
                               Case
                                  When "CodigoOperadorCorredor" > '100' Then 'A'                                  
                               Else 'C'   
                               End As "TipoOperador", 
                               ('['||''''||REPLACE(TO_DATE("FechaTransaccion"),'-','') || 
                               ConsMvtoCmbte."FolioTransaccion" || 
                               Coalesce(ConsCorrEm."CodigoCorredora", 0) ||  
                               "CodigoOperadorAfp" ||
								 Case 
								   When ConsMvtoCmbte."AccionTipoOperacion" = 'COMPRA' Then 'C'
								   When ConsMvtoCmbte."AccionTipoOperacion" = 'VENTA' Then 'V'
								Else ''
								End || 
								TO_INTEGER("Cantidad") || 
								TO_DECIMAL("Precio", 26,4) || 
								REPLACE(TO_TIME("FechaTransaccionHora"),':','') || 
								Coalesce("NemotecnicoInstrumento",'') || 
								TO_DECIMAL("Monto",26,2) || 
								Case
								   When "IdBoveda" = 'DCV' Then 'D'
								Else ' '
								End || 
								"IndicadorOrdenDirecta" || 
								Case  
								   When ConsMvtoCmbte."IdBolsa" = 'BCS' Then '1'  
								   When ConsMvtoCmbte."IdBolsa" = 'BEC' Then '2'  
								Else '0'
								End  || 
								Coalesce(ConsMvtoCmbte."CodigoCondicionDeLiquidacion",'  ') || 
								Case
								   When "CodigoOperadorCorredor" > '100' Then 'A'                                  
								Else 'C'   
								End || ''''|| '],') As "Resultado"       
                         From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" As ConsMvtoCmbte
                           Inner Join :v_MvtoAfp As Mvto
                              On Mvto."IdCliente" = ConsMvtoCmbte."IdCliente"
                             And Mvto."FechaMovimientoComprobante" = ConsMvtoCmbte."FechaMovimientoComprobante"  
                           Left Join "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" As ConsCorrEm
                              On ConsCorrEm."IdBolsa" = 'BCS'
                             And ConsCorrEm."IdEmpresa" = ConsMvtoCmbte."IdEmpresa"
                           Left Join "BSCL.Orden.BD.Vistas::ConsultarTransaccion" As ConsTran
                              On ConsTran."IdTransaccion" = ConsMvtoCmbte."IdTransaccion" ;
                      
		--select  LPAD ('1', 5,'0') from dummy;
        /*Select 'v_MvtoAfpAGen', *
        from :v_MvtoAfpAGen;*/
        
        Select Count("FolioTransaccion")
        Into Cant_Transaccion
        From :v_MvtoAfpAGen;
        
        --

        If Cant_Transaccion > 0 Then
           /** Almacena registros tabla de salida **/
           Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_Movimiento"
            ( 
              "FechaTransaccion", "FolioTransaccion", "Corredora", "CodigoOperadorAfp", "AccionTipoOperacion", "Cantidad", "Precio",
              "HoraTransaccion", "NemotecnicoInstrumento", "Total", "Boveda", "OD", "Bolsa", "CondicionDeLiquidacion", "TipoOperador", 
              "Resultado"           
            )
           Select REPLACE("FechaTransaccion",'-',''), "FolioTransaccion", "Corredora", "CodigoOperadorAfp", "AccionTipoOperacion", "Cantidad", "Precio",
                  REPLACE("HoraTransaccion",':',''), "NemotecnicoInstrumento", "Total", "Boveda", "OD", "Bolsa", "CondicionDeLiquidacion", "TipoOperador", 
                  "Resultado" 
           From :v_MvtoAfpAGen;  
           
           
           Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_Concatenado"
            ( 
              "Resultado"     
            )
           Select
           "Resultado" as "Resultado"
           From :v_MvtoAfpAGen; 
        
        
           --*********************
	   	   --* MENSAJE DE EXITO  *
		   --*********************
           /** Inserta registro de resultado de proceso de servicio completo **/		                         
		   Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ResultadoProceso" 
		   Select 'IdCliente' As "Campo", "IdCliente" As "Id", 'BSCL.Facturacion.BD.Servicios::GenerarInterfazAfp' As "Servicio",
		          'GEN' As "Resultado"
		   From :v_MvtoAfp Union All
		   Select "IdCliente" As "Campo", "IdCliente" as "Id", 'BSCL.Facturacion.BD.Servicios::GenerarInterfazAfp' as "Servicio",
		          'RECH' as "Resultado"
		   From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ClienteRechazados";
		                         
		                        
		   /** Acumula transaccion generadas **/
		   Select Count("FechaTransaccion")
		   Into Cant_TransaccionGenerada
           From :v_MvtoAfpAGen;
		
		   If Cant_TransaccionGenerada > 0 Then 
		      v_TablaCodigosMensajes = Select "IdCliente" As "IdInterno", 'UI_ARCHIVO_GENERADO' As "Codigo",
                                              'IdCliente' As "CodigoDato1", Cast("IdCliente" As NVARCHAR(520)) as "ValorDato1",
                                              '' As "CodigoDato2", '' As "ValorDato2" 
                                       From :v_MvtoAfp;
		  
  		      CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				   
		      Insert Into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_InformacionDetalle"
		      Select "Codigo", "Descripcion" 
		      From :v_out_hash;                           
		
		      Select Count("Codigo") 
		      Into p_out_InformacionContador 
              From "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_InformacionDetalle"; 
           
		   End If;             
		Else
		   Insert into "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle"
	       Select 0 As "IdCliente", '' As "Codigo", 'No existen registros para cliente y fecha seleccionado' As "Descripcion" 
	       From dummy;
	       
	       SIGNAL SALIDA_CONTROLADA;
		End If;   

    END; --Logica
END;   
