SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearAutorizacionAnularComprobante";
CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearAutorizacionAnularComprobante"
  (
     IN p_IdEmpresa						BIGINT,  
     IN p_IdUsuario						BIGINT,   
     IN p_IdModulo						NVARCHAR(3),
     
     IN p_simularOperacion				NVARCHAR(1) ,	-- S/N se usa para simular operacion (algunos casos)                  
     IN p_UsaReglasNegocio				NVARCHAR(1) ,	-- S/N se usa en caso de servicios anidados. 
   
     --Salidas para mensajes de advertencias y errores
     OUT p_out_AdvertenciasContador		int,
     OUT p_out_ErroresContador			int
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
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT);	        	
	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		
		SELECT count(1) into p_out_AdvertenciasContador 
		FROM "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_AdvertenciasDetalle";
		
		IF p_out_AdvertenciasContador > 0 Then
			p_out_ErroresContador = p_out_AdvertenciasContador;
		ELSE
			SELECT count(1) into p_out_ErroresContador 
			FROM "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle";		
		END IF;
	END;
/*
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			INSERT INTO "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle" ("Codigo", "Descripcion")
				SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;				
				SELECT COUNT(1) INTO p_out_ErroresContador 
				FROM "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle";
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	*/
	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;
	v_RechazadosPorValidacion   = select 0 As "IdComprobante" from dummy where 1=2;
	
	Begin --Logica    
		Declare v_FechaSistema           NVARCHAR(10);
		DECLARE v_AdvertenciasContador, v_ErroresContador	BIGINT;       

	   -----------------------------------------------------
	   --Validaciones
	   -----------------------------------------------------
       If p_UsaReglasNegocio = 'S' Then
		 BEGIN
		    Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_IN_Comprobante"
		    Select "IdPortafolio", "IdCliente", "IdComprobante", "IdTipoComprobante"
		    From "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_IN_Comprobante";
		    
            
            --Validacion Facturación             
            --Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosAnularComprobante"(:v_ValidaComprobanteAAnular,:v_RechazadosPorValidacion, :v_ErroresDetalle, :v_resultadoRegla);
            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosAnularComprobante"(:p_IdEmpresa,:v_resultadoRegla);
            
		    If :v_resultadoRegla = 'RECH' Then
			  			
				INSERT INTO "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle" ("IdComprobante", "Codigo", "Descripcion")
				SELECT "IdComprobante", "Codigo", "Descripcion"
				FROM "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_OUT_ErroresDetalle";
                
			    SIGNAL SALIDA_CONTROLADA; 	
		    Else
		      --If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
				INSERT INTO "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle" ("IdComprobante", "Codigo", "Descripcion")
				SELECT "IdComprobante", "Codigo", "Descripcion" 
				FROM "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_OUT_AdvertenciasDetalle";
			                                 
				Select count(1) 
				Into p_out_AdvertenciasContador 
				From "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle";
				
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
        --Obtiene fecha de sistema
        Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);
        
		v_tablaEventos	= 	SELECT  'ANUCOM'				as "IdTipoEvento",
									:p_IdModulo				as "IdModuloSolicita",
									:p_IdUsuario			as "IdUsuarioSolicita",
									:v_FechaSistema			as "Fecha",
									B."IdTipoOperacion",		
									B."IdConcepto",
									B."IdComprobante"		as "NumeroOperacionConcepto",
									'' 						as "FechaOperacionConcepto",
									B."IdCliente",
									0 						as "IdFuncionario",
									0 						as "IdProveedor",
									B."IdTipoProducto",
									B."IdClasificacionTipoInstrumento",
									B."IdTipoInstrumento",
									0						as "IdInstrumento",
									''						as "Nemo",
									0						as "Cantidad",
								    0						as "Monto",
								    0						as "Precio",
								    0						as "Tasa",
									B."IdMoneda",
									'ANULACION COMPROBANTE'	as "Glosa",
									0 as "IdUnion" 
							FROM "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_IN_Comprobante" A 
								INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" B
									ON A."IdComprobante" = B."IdComprobante";
									
		----------------------------------------------------------------------------------------------------------------							
        v_tablaEventosDetalle = select  "IdEventoDetalle" as "IdUnion","NombreCampo","TipoDato","Valor","Clasificacion"
   									from "BSCL.Plataforma.BD::AUT.EventoDetalle" 
   									where "IdEventoDetalle" = 0;
   		-----------------------------------------------------------------------------------------------------------------
   									
		CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo" (
			 p_IdEmpresa				=> :p_IdEmpresa
			,p_IdUsuario				=> :p_IdUsuario
			,p_IdModulo					=> 'CTA'
			,p_simularOperacion			=> :p_simularOperacion
			,p_UsaReglasNegocio			=> :p_UsaReglasNegocio
			,p_tablaEventos				=> :v_tablaEventos
			,p_tablaDetalle			    => :v_tablaEventosDetalle
			,p_out_IdEvento				=> v_EventoAutorizacion
			,p_out_AdvertenciasContador	=> v_AdvertenciasContador
			,p_out_AdvertenciasDetalle	=> v_AdvertenciasDetalle
			,p_out_ErroresContador		=> v_ErroresContador
			,p_out_ErroresDetalle		=> v_ErroresDetalleAut
		);    

		INSERT INTO "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle"
		SELECT 0 as "IdComprobante", "Codigo", "Descripcion" FROM :v_ErroresDetalleAut;
			
		INSERT INTO "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_AdvertenciasDetalle"
		SELECT 0 as "IdComprobante", "Codigo", "RequiereAutorizacion", "Descripcion" FROM :v_AdvertenciasDetalle;	
		
		v_TablaCodigosMensajes =	Select 		"IdEvento"		 								As "IdInterno", 
												'ANULACION_COMPROBANTE_PENDIENTE_AUTORIZACION'	As "Codigo",
												'IdEvento'										As "CodigoDato1", 
												"IdEvento"										as "ValorDato1",
												'' 												As "CodigoDato2", 
												'' 												As "ValorDato2" 
										From  :v_EventoAutorizacion;
			                        
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
		
		INSERT INTO "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_InformacionDetalle"
		SELECT "Codigo", "Descripcion" FROM :v_out_hash;
		
        --Salida en caso de advertencia o error 
		Select Count(1) Into p_out_AdvertenciasContador
		From "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_AdvertenciasDetalle";
		
		Select Count(1) Into p_out_ErroresContador
		From "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle";
		
		If p_out_ErroresContador > 0 Then
			SIGNAL SALIDA_CONTROLADA;
		End if;

    END; --Logica
END;   