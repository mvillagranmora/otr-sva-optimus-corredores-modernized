SET SCHEMA "BSCL";
GO 
 
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar";
GO

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar"
-- 
--  Proposito: Modificar el estado de los movimientos a facturar
--  Autor: Jorge Villagrán
--  Fecha Creacion: 17/09/2015 
--  

(    -- Parametros de Validacion
     IN p_IdEmpresa BIGINT, 
     IN p_IdUsuario BIGINT, 
     IN p_IdModulo  NVARCHAR(3),
     
     IN p_simularOperacion     NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)
     IN p_UsaReglasNegocio 	   NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
     IN p_EsIngresoAutomatico  NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente

     IN p_Estado  NVARCHAR(3),
     
     --Salidas para mensajes de advertencias y errores
     OUT p_out_InformacionContador  INT,
     OUT p_out_AdvertenciasContador INT,
     OUT p_out_ErroresContador      INT
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
	
	--DECLARE v_ErroresDetalle TABLE ("IdMovimientoComprobante" BIGINT, "Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(520))
	--        Default Select 0 As "IdMovimientoComprobante", '' As "Codigo", '' As "Descripcion" from dummy where 1=2; 	
	        
    	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
	    Select count(1) 
        Into p_out_ErroresContador
        From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle";
		
        Select count(1) 
        Into p_out_AdvertenciasContador
        From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle";
        
	END;
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
        v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle"
		Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
		
		
		Select Count(1)
		Into p_out_ErroresContador
		From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle";
		 
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
	
		Declare v_errorExt NVARCHAR(40);
		
		If ::SQL_ERROR_CODE > 10000 THEN 
		
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			
			Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle"
			Select 0 As "IdMovimientoComprobante", v_errorExt,::SQL_ERROR_MESSAGE 
			From dummy;
			
			Select Count(1)
			Into p_out_ErroresContador 
			From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle";
			
		Else
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		End If;		
	END;
	
	p_out_AdvertenciasContador	:= 0;	
	p_out_ErroresContador		:= 0;	
	p_out_InformacionContador   := 0;	
	
	BEGIN --LOGICA
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_CantidadMovimientosConProblema, v_CantidadMovimientosConProblemaEs, 
	            v_CantidadMovimientosConProblemaUs, v_CantidadMvtoBlq, v_CantidadMvtoDesBlq  INT :=0;
	    
	    DECLARE v_MovimientosABlq Table("IdMovimientoComprobante" BIGINT);
	    
        DECLARE v_MovimientosABlqErr Table("IdMovimientoComprobante" BIGINT);
	    
	    DECLARE v_MovimientosADesBlq Table("IdMovimientoComprobante" BIGINT);
	    
	    DECLARE v_MovimientosADesBlqErr Table("IdMovimientoComprobante" BIGINT, "IdUsuarioBloqueo" BIGINT);
	    
	    
	    
	    
	    v_MovimientosABlq = Select 0 As "IdMovimientoComprobante" From dummy Where 1=2;
	    
	    v_MovimientosADesBlq = Select 0 As "IdMovimientoComprobante" From dummy Where 1=2;
	    
	    v_MovimientosABlqErr = Select 0 As "IdMovimientoComprobante" From dummy Where 1=2;
	    
	    v_MovimientosADesBlqErr = Select 0 As "IdMovimientoComprobante", 0 As "IdUsuarioBloqueo" From dummy Where 1=2;
	    
	 
	
	  
	    /** Validaciones **/       
        If p_UsaReglasNegocio = 'S' Then
		  BEGIN
            If p_EsIngresoAutomatico = 'S' Then
			  Call "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalleReqBas,:v_resultadoRegla);
			  If :v_resultadoRegla = 'RECH' Then
			    SIGNAL SALIDA_CONTROLADA; 	
			  End If;			                  
            End If;
            
            
            /** Validaciones en facturación **/
            Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarEstadoMovimientoAFacturar_IN_MovimientoComprobante"
            Select "IdMovimientoComprobante"
            From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_IN_MovimientoComprobante";
            
            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosModificarEstadoMovimientoAFacturar"(v_resultadoRegla);
		    If :v_resultadoRegla = 'RECH' Then
                Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ResultadoProceso"
                Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" as "Id",
                       'BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar' as "Servicio",
		               'RECH' as "Resultado"		               
                From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarEstadoMovimientoAFacturar_OUT_MovimientosRechazados"; 
                    
                                       
                Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle"
                Select "IdMovimientoComprobante", "Codigo","Descripcion"
                From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle";

                
			    --SIGNAL SALIDA_CONTROLADA; 	
		    --Else
		      --If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
			     /*p_out_AdvertenciasDetalle = Select "IdMovimientoComprobante", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
			                                   From :v_ErroresDetalle;
                                             
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
        --Selecciona los validados correctamente	
        v_MovimientoFacturarVal = Select "IdMovimientoComprobante"
                                  From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_IN_MovimientoComprobante" As MvtoCmbteAMod   
                                  Where MvtoCmbteAMod."IdMovimientoComprobante" Not In (Select "IdMovimientoComprobante" 
                                                                                        From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarEstadoMovimientoAFacturar_OUT_MovimientosRechazados"); 
                                                                                        
                                                                                       
        -- Obtiene información de los movimientos a modificar validados
        v_MovimientosAModificar = Select ConsMovCom."IdMovimientoComprobante", ConsMovCom."IdEstadoFacturacion", 
                                         ConsMovCom."IdUsuarioBloqueo", ConsMovCom."CodigoUsuarioBloqueo"
                                  From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" As ConsMovCom
                                    Inner Join :v_MovimientoFacturarVal As MovMod
                                       On MovMod."IdMovimientoComprobante" = ConsMovCom."IdMovimientoComprobante"
                                  Order by ConsMovCom."IdMovimientoComprobante";
                                  
        /*Select 'v_MovimientosAModificar', *
        from :v_MovimientosAModificar;*/
		                              
		                              
	    /** Bloqueo de Movimientos **/
	    IF (p_Estado = 'BLO') THEN
		  -- Verifica que no existan movimientos a bloquear con estado Ingresado(ING).
          v_MovimientosABlqErr = Select "IdMovimientoComprobante" 
                                 From :v_MovimientosAModificar
                                 Where "IdEstadoFacturacion" <> 'ING';          
          
          
          Select Count("IdMovimientoComprobante")
		  Into v_CantidadMovimientosConProblema                                                
		  From :v_MovimientosABlqErr;
			     
		  If v_CantidadMovimientosConProblema > 0 Then
            v_hash = Select "IdMovimientoComprobante" As "IdInterno", 'MOVIMIENTO_DISTINTO_ING' As "Codigo", 
                            'IdMovimientoComprobante' As "CodigoDato1", "IdMovimientoComprobante" As "ValorDato1", 
                            '' As "CodigoDato2", '' As "ValorDato2"
                     From :v_MovimientosABlqErr;
                     
               
            Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		    Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle"
		    Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", 'N' As "RequiereAutorizacion", "Descripcion" As "Descripcion" 
		    From :v_out_hash;
		
		
		    Select Count(1)
            Into p_out_AdvertenciasContador
            From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle";
		                                               
		  End If;
		  
		  
		  -- Marca los movimientos como 'Bloqueados' y el usuario que los bloqueó. 
		  v_MovimientosABlq = Select "IdMovimientoComprobante"
		                      From :v_MovimientosAModificar As MvtoMod
		                      Where "IdMovimientoComprobante" not In (Select "IdMovimientoComprobante"
		                                                              From :v_MovimientosABlqErr);
		    			
		  /*Select 'v_MovimientosABlq', *
		  from :v_MovimientosABlq;*/
		  
		  Update MvtoCmbte
		  Set "IdEstadoFacturacion" = 'BLO',
			  "IdUsuarioBloqueo"    = p_IdUsuario
		  From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
		  Where "IdMovimientoComprobante" in (Select "IdMovimientoComprobante"
			                                  From :v_MovimientosABlq);
		  
	    END IF;
	   
	   
	   
	    -- Des-Bloqueo de movimientos(retorna a estado ING)
	    IF (p_Estado = 'ING') THEN
	      -- Verifica que los movimientos a desbloquear pertenezcan al usuario que los bloqueó y se encuentren bloqueado.
	      v_MovimientosADesBlqErrEs = Select "IdMovimientoComprobante", "IdEstadoFacturacion"
                                      From :v_MovimientosAModificar
                                      Where "IdEstadoFacturacion" <> 'BLO';
                                      
          
          v_MovimientosADesBlqErrUs = Select "IdMovimientoComprobante", "IdUsuarioBloqueo", "CodigoUsuarioBloqueo"
                                      From :v_MovimientosAModificar
                                      Where ("IdUsuarioBloqueo" <> p_IdUsuario AND "IdUsuarioBloqueo" <> 0 );                            
                                                            
	      
          Select Count("IdMovimientoComprobante")
          Into v_CantidadMovimientosConProblemaEs
          From :v_MovimientosADesBlqErrEs;
          
          /*select 'v_MovimientosADesBlqErr', *
          from :v_MovimientosADesBlqErr;*/
          
          Select Count("IdMovimientoComprobante")
          Into v_CantidadMovimientosConProblemaUs
          From :v_MovimientosADesBlqErrUs;
          
          -- Valida movimiento distintos de bloqueados
          If v_CantidadMovimientosConProblemaEs > 0 Then
            v_hash = Select "IdMovimientoComprobante" As "IdInterno", 'MOVIMIENTO_DISTINTO_BLO' As "Codigo", 
                            'IdMovimientoComprobante' As "CodigoDato1", "IdMovimientoComprobante" As "ValorDato1", 
                            'IdEstadoFacturacion' As "CodigoDato2", "IdEstadoFacturacion" As "ValorDato2"
                     From :v_MovimientosADesBlqErrEs;		
                     
               
            Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		    Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle"
		    Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", 'N' As "RequiereAutorizacion", "Descripcion" As "Descripcion" 
		    From :v_out_hash;
		
		
		    Select Count(1)
		    Into p_out_AdvertenciasContador
		    From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle";
		  Else
		    -- Valida movimientos bloqueados por distinto usuario		  
		    If v_CantidadMovimientosConProblemaUs > 0 Then
               v_hash = Select "IdMovimientoComprobante" As "IdInterno", 'MOVIMIENTO_DISTINTO_USU' As "Codigo", 
                               'IdMovimientoComprobante' As "CodigoDato1", "IdMovimientoComprobante" As "ValorDato1", 
                               'CodigoUsuarioBloqueo' As "CodigoDato2", "CodigoUsuarioBloqueo" As "ValorDato2"
                        From :v_MovimientosADesBlqErrUs;		
                     
               
               Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		       Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle"
		       Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", 'N' As "RequiereAutorizacion", "Descripcion" As "Descripcion" 
		       From :v_out_hash;
		
		
		       Select Count(1)
		       Into p_out_AdvertenciasContador
		       From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle";
		    
		    End If;		    
		  End If;	  
		  
		  
		  
		  
		  
		  -- Marca los movimientos como 'Ingresados'(desbloqueo) y limpia el usuario que los había bloqueado. 
          v_MovimientosADesBlq = Select "IdMovimientoComprobante"
		                         From :v_MovimientosAModificar As MvtoMod
		                         Where "IdMovimientoComprobante" not In (Select "IdMovimientoComprobante"
		                                                                   From :v_MovimientosADesBlqErrEs)
		                           And "IdMovimientoComprobante" not In (Select "IdMovimientoComprobante"
		                                                                   From :v_MovimientosADesBlqErrUs);
		                                                                   
            
		  Update "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
		  Set "IdEstadoFacturacion" = 'ING',
			  "IdUsuarioBloqueo"    = 0
	      Where "IdMovimientoComprobante" in (Select "IdMovimientoComprobante"
			                                  From :v_MovimientosADesBlq);
		  
			     	   
	    End If;
	    
	    
	    
	    
	    --*********************
		--* MENSAJE DE EXITO  *
		--*********************
        /** Inserta registro de resultado de proceso de servicio completo **/		                         
		Insert into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ResultadoProceso" 
		Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar' As "Servicio",
		       'BLQ' As "Resultado"
		From :v_MovimientosABlq Union All
		Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar' As "Servicio",
		       'ING' As "Resultado"
		From :v_MovimientosADesBlq Union All
		Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar' As "Servicio",
		       'RECH' As "Resultado"
		From :v_MovimientosABlqErr Union All
		Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar' As "Servicio",
		       'RECH' As "Resultado"
		From :v_MovimientosADesBlqErr Union All		
		Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" as "Id", 'BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientoAFacturar' as "Servicio",
		       'RECH' as "Resultado"
		From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarEstadoMovimientoAFacturar_OUT_MovimientosRechazados";                         
		                         
		                        
				
		/** Acumula movimientos modificados exitosos **/
		Select Count("IdMovimientoComprobante")
		Into v_CantidadMvtoBlq
        From :v_MovimientosABlq;
        
        Select Count("IdMovimientoComprobante")
		Into v_CantidadMvtoDesBlq
        From :v_MovimientosADesBlq;
		
				
		If (v_CantidadMvtoBlq > 0 Or v_CantidadMvtoDesBlq > 0)   Then 
		   v_TablaCodigosMensajes = Select "IdMovimientoComprobante" As "IdInterno", 'UI_MOVIMIENTO_BLOQUEADO' As "Codigo",
                                           'IdMovimientoComprobante' As "CodigoDato1", Cast("IdMovimientoComprobante" As NVARCHAR(520)) as "ValorDato1",
                                           '' As "CodigoDato2", '' As "ValorDato2" 
                                    From :v_MovimientosABlq Union All
                                    Select "IdMovimientoComprobante" As "IdInterno", 'UI_MOVIMIENTO_DESBLOQUEADO' As "Codigo",
                                           'IdMovimientoComprobante' As "CodigoDato1", Cast("IdMovimientoComprobante" As NVARCHAR(520)) as "ValorDato1",
                                           '' As "CodigoDato2", '' As "ValorDato2" 
                                    From :v_MovimientosADesBlq;                                    
                                    
		  
  		   CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);				   
		   
		   Insert Into "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_InformacionDetalle"
		   Select "Codigo", "Descripcion" 
		   From :v_out_hash;                           
		
		   Select Count("Codigo") 
		   Into p_out_InformacionContador 
           From "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_InformacionDetalle"; 
           
		End If;   
	    
    End; --END LOGICA    
  END;
GO