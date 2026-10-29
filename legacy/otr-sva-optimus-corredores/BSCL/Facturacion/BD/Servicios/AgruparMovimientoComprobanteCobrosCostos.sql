SET SCHEMA "BSCL";   
GO 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::AgruparMovimientoComprobanteCobrosCostos";
GO 

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::AgruparMovimientoComprobanteCobrosCostos"
  (                                                    
   -- Parametros de Validacion                                                                                            
   IN p_IdEmpresa                     BIGINT,                                                                            
   IN p_IdUsuario                     BIGINT,                                                                            
   IN p_IdModulo                      NVARCHAR(3),
                                                                                                                          
   IN p_simularOperacion              NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN p_UsaReglasNegocio              NVARCHAR(1),	-- S/N se usa en caso de servicios anidados. 
   IN p_EsIngresoAutomatico           NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente                                    

   
   OUT p_out_InformacionContador  	  INT,
   OUT p_out_AdvertenciasContador     INT,
   OUT p_out_ErroresContador          INT
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
	
	DECLARE v_ErroresDetalle			TABLE ("NumeroMvtoCmbte" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)) 	
	        Default Select 0 As "NumeroMvtoCmbte", '' As "Codigo", '' As "Descripcion" from dummy where 1=2;
	
    DECLARE v_RechazadosPorValidacion	TABLE("NumeroMvtoCmbte" BIGINT)
            Default Select 0  as "NumeroMvtoCmbte" From dummy Where 1=2;
	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) 
		into p_out_ErroresContador 
		FROM "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle";
		
		
		--SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
    
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
        						
		Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
		
		Insert into "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
		Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
								
								
		SELECT COUNT(1) 
		INTO p_out_ErroresContador
		FROM "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle";
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			
            Insert into "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
			SELECT 0 As "IdMovimientoComprobante", v_errorExt,::SQL_ERROR_MESSAGE 
			FROM dummy;				
			
			SELECT count(1) 
			into p_out_ErroresContador 
			FROM "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle";
			
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	BEGIN --LOGICA
	    -----------------------
		--Declare Variables
		-----------------------
        Declare Cant_MovimientoComprobanteGenerado   BIGINT = 0;
        DECLARE LocalSistema                         NVARCHAR(40) = 'CL';

        ------------------------
		--Validaciones
		------------------------
        If p_UsaReglasNegocio = 'S' Then
		  BEGIN
            If p_EsIngresoAutomatico = 'S' Then
			   Call "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalleReqBas,:v_resultadoRegla);
			   If :v_resultadoRegla = 'RECH' Then
				  SIGNAL SALIDA_CONTROLADA; 	
			   End If;
			   	                  
            End If;
            
            --Validacion Facturación
            Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante"
            Select "IdPortafolio", "IdCliente", "IdMovimientoComprobante", "IdTipoComprobante"
            From "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante";
            
            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos"(:v_resultadoRegla);
            
		    If :v_resultadoRegla = 'RECH' Then	
		        Insert into "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso" 
		        Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" as "Id",
		               'BSCL.Facturacion.BD.Servicios::AgruparMovimientoComprobanteCobrosCostos' as "Servicio",
		               'RECH' as "Resultado"
		        From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados";
		                                  
		        	                           	
			                           
                Insert into "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
                Select "IdMovimientoComprobante", "Codigo","Descripcion" 
                From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle";  
                 			                           
                
			    SIGNAL SALIDA_CONTROLADA; 	
		    /*Else
		     If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
			     p_out_AdvertenciasDetalle = Select "NumeroMvtoCmbte", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
			                                 From :v_ErroresDetalle;
			                                 
			     Select count(1) 
			     Into p_out_AdvertenciasContador 
			     From :p_out_AdvertenciasDetalle;
		      End If; */  
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
		--Obtiene localizacion
		SELECT "Valor"
		INTO LocalSistema
        FROM "BSCL.Plataforma.BD::PLA.Configuracion"
        WHERE "CodigoParametro" = 'PLA.LocalizacionSistema'
          AND "IdEmpresa" = :p_IdEmpresa;

        v_MvtoFactCostos = Select *
                           From "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante" As MvtoCmbte
                           Where MvtoCmbte."IdMovimientoComprobante" Not In (Select "IdMovimientoComprobante" 
                                                                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados");
                                                                     
        /*select 'v_MvtoFactCostos', *
        from :v_MvtoFactCostos;*/                                            
        
        IF LocalSistema = 'CL' THEN 
          Insert into "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobante"
          Select ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro", MvtoFactCos."IdMovimientoComprobante", ConsMvtoCmbteCobro."IdCobro", 
                 ConsMvtoCmbteCobro."NombreCobro", ConsMvtoCmbteCobro."IdMonedaCobro", ConsMvtoCmbteCobro."MontoCobro",
                 ConsMvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro", ConsMvtoCmbteCobro."MontoMovimientoComprobanteCobro",
                 ConsMvtoCmbteCobro."IdMonedaLiquidacionCobro", ConsMvtoCmbteCobro."MontoLiquidacionCobro", '' AS "ModificarValor", 
                 '' AS "AumentarValor", '' AS "DisminuirValor"
          From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" As ConsMvtoCmbteCobro
            Inner Join :v_MvtoFactCostos As MvtoFactCos
               On MvtoFactCos."IdMovimientoComprobante" = ConsMvtoCmbteCobro."IdMovimientoComprobante"
              And ConsMvtoCmbteCobro."IdEstado" = 'VIG' 
              And ConsMvtoCmbteCobro."EsRecalculable" = 'N'
          Order by "IdMovimientoComprobante", "IdCobro";
        ELSE  
          Insert into "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobante"
          Select ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro", MvtoFactCos."IdMovimientoComprobante", ConsMvtoCmbteCobro."IdCobro", 
                 ConsMvtoCmbteCobro."NombreCobro", ConsMvtoCmbteCobro."IdMonedaCobro", ConsMvtoCmbteCobro."MontoCobro",
                 ConsMvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro", ConsMvtoCmbteCobro."MontoMovimientoComprobanteCobro",
                 ConsMvtoCmbteCobro."IdMonedaLiquidacionCobro", ConsMvtoCmbteCobro."MontoLiquidacionCobro", ConsMvtoCmbteCobro."ModificarValor",
                 ConsMvtoCmbteCobro."AumentarValor", ConsMvtoCmbteCobro."DisminuirValor"                 
          From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru" As ConsMvtoCmbteCobro
            Inner Join :v_MvtoFactCostos As MvtoFactCos
               On MvtoFactCos."IdMovimientoComprobante" = ConsMvtoCmbteCobro."IdMovimientoComprobante"
              And ConsMvtoCmbteCobro."IdEstado" = 'VIG' 
          Order by "IdMovimientoComprobante", "IdCobro";
        END IF;   
        
                             
                               
        /*Select 'p_out_MvtoFactCostos', *
        from :p_out_MvtoFactCostos;*/
                                       

        Insert into "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_Cobro"
        Select MvtoFacCos."IdCobro", MvtoFacCos."NombreCobro", Max(MvtoFacCos."IdMonedaCobro") As "IdMonedaCobro",               
               Sum(MvtoFacCos."MontoCobro") As "MontoCobro", Max(MvtoFacCos."IdMonedaMovimientoComprobanteCobro") As "IdMonedaMovimientoComprobanteCobro", 
               Sum(MvtoFacCos."MontoMovimientoComprobanteCobro") As "MontoMovimientoComprobanteCobro",               
               Max(MvtoFacCos."IdMonedaLiquidacionCobro") As "IdMonedaLiquidacionCobro", Sum(MvtoFacCos."MontoLiquidacionCobro") As "MontoLiquidacionCobro",
               Max(MvtoFacCos."ModificarValor") AS "ModificarValor", Max(MvtoFacCos."AumentarValor") AS "AumentarValor", Max(MvtoFacCos."DisminuirValor") AS "DisminuirValor"
        From "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobante" As MvtoFacCos 
        Group By MvtoFacCos."IdCobro", MvtoFacCos."NombreCobro"
        Order By MvtoFacCos."IdCobro", MvtoFacCos."NombreCobro";                            
        
        /*Select 'p_out_MvtoFactCostosAgrup', *
        from :p_out_MvtoFactCostosAgrup;*/
        
        
    END; --LOGICA
END;
GO 