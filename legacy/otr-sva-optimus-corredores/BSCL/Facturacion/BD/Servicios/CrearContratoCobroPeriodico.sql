SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearContratoCobroPeriodico";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearContratoCobroPeriodico"
  (
    -- Parametros de Validacion                                                                                            
    IN p_IdEmpresa                     BIGINT,                                                                            
    IN p_IdUsuario                     BIGINT,                                                                            
    IN p_IdModulo                      NVARCHAR(3),
                                                                                                                          
    IN p_simularOperacion              NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
    IN p_UsaReglasNegocio              NVARCHAR(1),	-- S/N se usa en caso de servicios anidados. 
    IN p_EsIngresoAutomatico           NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente                                    

    IN p_ContratoCobroPeriodicoACrear table ( 
                                              "NumeroContratoCobroPeriodico" BIGINT,
                                              "IdCliente" BIGINT,
                                              "IdPortafolio" BIGINT,
                                              "IdTipoContratoCobroPeriodico" NVARCHAR(6),
                                              "IdMoneda" NVARCHAR(3),
                                              "FormatoContratoCobroPeriodico" BIGINT
                                            ), 
                                            
    IN p_ContratoCobroPeriodicoDetalleACrear table (
                                                      "NumeroContratoCobroPeriodico" BIGINT,
                                                      "IdTipoInstrumento" NVARCHAR(3),
                                                      "IdClasificacionTipoInstrumento" NVARCHAR(3),
                                                      "IdConceptoCobroPeriodico" NVARCHAR(6)
                                                   ), 
                                            
    IN p_CobroPeriodicoACrear table (
                                      "NumeroContratoCobroPeriodico" BIGINT,
                                      "IdCobro" NVARCHAR(6),
                                      "MontoMinimoACobrar" DECIMAL(26,8),
                                      "MontoMaximoACobrar" DECIMAL(26,8),
                                      "TipoCobroPeriodico" NVARCHAR(1),
                                      "PorRango"           NVARCHAR(1) 
                                    ), 
                                    
    IN p_CobroPeriodicoDetalleACrear table (
                                             "NumeroContratoCobroPeriodico" BIGINT,
                                             "IdCobro" NVARCHAR(6),
                                             "MontoDesde" DECIMAL(26,8),
                                             "MontoHasta" DECIMAL(26,8),
                                             "MontoCobroPeriodico"  DECIMAL(26,8),
                                             "PorcentajeCobroPeriodico" DECIMAL(26,8) 
                                           ),                                                                                   
    -- Parametros de Salida    
    OUT p_out_IdContratoCobroPeriodico table ("IdContratoCobroPeriodico" BIGINT, "NumeroContratoCobroPeriodico" BIGINT) DEFAULT EMPTY,
       
    OUT p_out_InformacionContador  		INT,
    OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,   
    OUT p_out_ResultadoProceso		    TABLE ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)) DEFAULT EMPTY,
    OUT p_out_AdvertenciasContador      int,
    OUT p_out_AdvertenciasDetalle       TABLE ("NumeroContratoCobroPeriodico" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
    OUT p_out_ErroresContador           int,   
    OUT p_out_ErroresDetalle            TABLE ("NumeroContratoCobroPeriodico" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
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
	
	DECLARE v_ErroresDetalle			TABLE ("NumeroContratoCobroPeriodico" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
	        Default Select 0 As "NumeroContratoCobroPeriodico", '' As "Codigo", '' As "Descripcion" from dummy where 1=2;

    --DECLARE v_RechazadosPorValidacion	TABLE("NumeroMvtoCmbte" BIGINT);	        	
	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
        						
		Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
		
		p_out_ErroresDetalle = Select "IdInterno" As "NumeroContratoCobroPeriodico", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle;						
								
								
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT 0 As "NumeroContratoCobroPeriodico", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;	
	END;
	
	
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	
	BEGIN --LOGICA
	    --------------------
		--Declare Variables
		--------------------
        DECLARE v_out_ErroresDetalle TABLE ("IdMovimientoComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
                Default Select 0 As "IdMovimientoComprobante", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
                

        DECLARE v_out_AdvertenciasDetalle TABLE ("IdMovimientoComprobante" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000))
                Default Select 0 As "IdMovimientoComprobante", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
                
                
        DECLARE v_SecuenciaContCobPer TABLE ("NumeroContratoCobroPeriodico" BIGINT, "IdContratoCobroPeriodico" BIGINT) 
                Default Select 0 As "NumeroContratoCobroPeriodico", 0 As "IdContratoCobroPeriodico" from dummy where 1=2;
               
        
        DECLARE v_SecuenciaCobPer TABLE ("NumeroContratoCobroPeriodico" BIGINT, "IdCobro" NVARCHAR(6), "IdCobroPeriodico" BIGINT)
                Default Select 0 As "NumeroContratoCobroPeriodico", '' As "IdCobro", 0 As "IdCobroPeriodico" from dummy where 1=2;
                
                
        DECLARE v_TablaCodigosMensajes TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "CodigoDato1" NVARCHAR(100), "ValorDato1" NVARCHAR(520), "CodigoDato2" NVARCHAR(100), "ValorDato2" NVARCHAR(520))
                Default Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2", '' As "ValorDato2" from dummy where 1=2;
	            
	    
	    
		
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
            
            --Validacion Facturación
            
          END;  --End Validaciones
        End If;
        
        --Simulación
        If (:p_simularOperacion = 'S') Then
		   SIGNAL SALIDA_CONTROLADA;
	    End If;


		--------------------
		--Negocio Aqui
		--------------------	
        --Elimina contratos que no pasaron validaciones 
        --Registra secuencia de agrupacion de movimientos que no pasaron la validacion
        /*v_RechazadosPorValidacionSecuencia = Select "SecuenciaAgrupacion"
                                             From :p_MvtoCmbteAGenerar As MvtoCmbte
                                             Where MvtoCmbte."NumeroMvtoCmbte" In (Select "NumeroMvtoCmbte" 
                                                                          From :v_RechazadosPorValidacion); 
        
        
        v_MvtoCmbteAGenerar = Select *
                              From :p_MvtoCmbteAGenerar As MvtoCmbte
                              Where MvtoCmbte."NumeroMvtoCmbte" Not In (Select "NumeroMvtoCmbte" 
                                                                        From :v_RechazadosPorValidacion)
                                --Filtra movimientos que coinciden con secuencia de movimientos rechazados                                         
                                And MvtoCmbte."SecuenciaAgrupacion" Not In (Select "SecuenciaAgrupacion" 
                                                                            From :v_RechazadosPorValidacionSecuencia
                                                                            Where "SecuenciaAgrupacion" <> '' And "SecuenciaAgrupacion" <> '0');
         */    
         
                                                                        
        --Obtiene Nro de Secuencia Contrato
	    v_SecuenciaContCobPer= Select "NumeroContratoCobroPeriodico", 
		                              "BSCL.Facturacion.BD.Secuencias::ContratoCobroPeriodico".Nextval As "IdContratoCobroPeriodico"
		                        From :p_ContratoCobroPeriodicoACrear; --:v_MvtoCmbteAGenerar;

        --Obtiene Nro de Secuencia Cobro - No se incluye "IdContratoCobroPeriodico" por GroupBy
	    v_SecuenciaCobPer= Select CobPer."NumeroContratoCobroPeriodico", CobPer."IdCobro",
		                          "BSCL.Facturacion.BD.Secuencias::CobroPeriodico".Nextval As "IdCobroPeriodico"
		                        From :p_CobroPeriodicoACrear As CobPer
		                          Inner Join :v_SecuenciaContCobPer As SecConCobPer
		                             On SecConCobPer."NumeroContratoCobroPeriodico" = CobPer."NumeroContratoCobroPeriodico"
		                        Group By CobPer."NumeroContratoCobroPeriodico", CobPer."IdCobro";
		                             
        /*select 'p_ContratoCobroPeriodicoACrear', *
        from :p_ContratoCobroPeriodicoACrear;
        
        select 'p_CobroPeriodicoACrear', *
        from :p_CobroPeriodicoACrear;
        
        select 'v_SecuenciaContCobPer', *
        from :v_SecuenciaContCobPer;
        
        select 'v_SecuenciaCobPer', *
        from :v_SecuenciaCobPer;*/		                             
		 
	   	--Obtiene Fecha de Sistema
	   	--Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_p_FechaSistema);
	   	
	   	--Obtiene Moneda de Sistema
        --Call "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:p_IdEmpresa, 'MonedaSistema', v_p_IdMonedaSistema);
	    
	             
          
       --------------------------------------------   
	   -- CONTRATO COBRO PERIODICO 
	   --------------------------------------------	
	   --Insercion de registro(s)
       Insert Into "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodico" 
          (
            "IdContratoCobroPeriodico",
            "IdCliente",
            "IdPortafolio",
            "DocumentoIdentidad",
            "NombreLargoCliente",
            "IdTipoContratoCobroPeriodico",
            "IdMoneda",
            "FormatoContratoCobroPeriodico",            
            "FechaIngresoRegistro"
          )     
       Select 
            SecConCobPer."IdContratoCobroPeriodico",
            ConCobPer."IdCliente",
            ConCobPer."IdPortafolio",
            ConsCli."DocumentoIdentidad",
            ConsCli."NombreCompleto",
            ConCobPer."IdTipoContratoCobroPeriodico",
            ConCobPer."IdMoneda",
            ConCobPer."FormatoContratoCobroPeriodico",
            CURRENT_UTCTIMESTAMP
	   From :p_ContratoCobroPeriodicoACrear As ConCobPer
	     Inner Join :v_SecuenciaContCobPer As SecConCobPer
	       On SecConCobPer."NumeroContratoCobroPeriodico" = ConCobPer."NumeroContratoCobroPeriodico"	       
	     Inner Join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" As ConsCli
	       On ConsCli."IdCliente" = ConCobPer."IdCliente";	     	 
	            
       
	   --Acumula Id Contratos Creados
       p_out_IdContratoCobroPeriodico = Select "IdContratoCobroPeriodico", "NumeroContratoCobroPeriodico"
                                        From :v_SecuenciaContCobPer;
                                        
                                        
       
       -------------------------------------------   
	   --CONTRATO COBRO PERIODICO  DETALLE
	   -------------------------------------------
	   --Insercion de registro(s)
       Insert Into "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodicoDetalle"
          ( 
            "IdContratoCobroPeriodicoDetalle",
            "IdContratoCobroPeriodico",
            "IdTipoInstrumento",
            "IdClasificacionTipoInstrumento",
            "IdConceptoCobroPeriodico",
            "FechaIngresoRegistro"
          )
       Select 
            "BSCL.Facturacion.BD.Secuencias::ContratoCobroPeriodicoDetalle".Nextval,
            SecConCobPer."IdContratoCobroPeriodico",
            ConCobPerDet."IdTipoInstrumento",
            ConCobPerDet."IdClasificacionTipoInstrumento",
            ConCobPerDet."IdConceptoCobroPeriodico",
            CURRENT_UTCTIMESTAMP 
       From :p_ContratoCobroPeriodicoDetalleACrear As ConCobPerDet
         Inner Join :v_SecuenciaContCobPer As SecConCobPer
	       On SecConCobPer."NumeroContratoCobroPeriodico" = ConCobPerDet."NumeroContratoCobroPeriodico";
	       
	   
	   --------------------------------------------   
	   --COBRO PERIODICO
	   --------------------------------------------	
	   --Insercion de registro(s)
       Insert Into "BSCL.Facturacion.BD::FAC.CobroPeriodico"
          (
            "IdCobroPeriodico",
            "IdCobro",
            "IdContratoCobroPeriodico",            
            "MontoMinimoACobrar",
            "MontoMaximoACobrar",
            "TipoCobroPeriodico",
            "PorRango",
            "FechaIngresoRegistro"
          ) 
        Select
            SecCobPer."IdCobroPeriodico", 
            CobPer."IdCobro",
            SecConCobPer."IdContratoCobroPeriodico",            
            CobPer."MontoMinimoACobrar",
            CobPer."MontoMaximoACobrar",
            CobPer."TipoCobroPeriodico",
            CobPer."PorRango",
            CURRENT_UTCTIMESTAMP
        From :p_CobroPeriodicoACrear As CobPer
          Inner Join :v_SecuenciaCobPer As SecCobPer
	         On SecCobPer."NumeroContratoCobroPeriodico" = CobPer."NumeroContratoCobroPeriodico"
	        And SecCobPer."IdCobro" = CobPer."IdCobro" 
	      Inner Join :v_SecuenciaContCobPer As SecConCobPer
	       On SecConCobPer."NumeroContratoCobroPeriodico" = CobPer."NumeroContratoCobroPeriodico";
	         
      
	   --------------------------------------------   
	   --COBRO PERIODICO DETALLE
	   --------------------------------------------	
	   --Insercion de registro(s)
       Insert Into "BSCL.Facturacion.BD::FAC.CobroPeriodicoDetalle"
         (
           "IdCobroPeriodicoDetalle",
           "IdCobroPeriodico",
           "MontoDesde",
           "MontoHasta",
           "MontoCobroPeriodico",
           "PorcentajeCobroPeriodico",
           "FechaIngresoRegistro"
         )
       Select
           "BSCL.Facturacion.BD.Secuencias::CobroPeriodicoDetalle".Nextval,           
           SecCobPer."IdCobroPeriodico",
           CobPerDet."MontoDesde",
           CobPerDet."MontoHasta",
           CobPerDet."MontoCobroPeriodico",
           CobPerDet."PorcentajeCobroPeriodico",
           CURRENT_UTCTIMESTAMP
       From :p_CobroPeriodicoDetalleACrear As CobPerDet
         Inner Join :v_SecuenciaCobPer As SecCobPer
	         On SecCobPer."NumeroContratoCobroPeriodico" = CobPerDet."NumeroContratoCobroPeriodico"
	        And SecCobPer."IdCobro" = CobPerDet."IdCobro";                                                     
      
                               
       p_out_ResultadoProceso = Select 'IdContratoCobroPeriodico' As "Campo", "IdContratoCobroPeriodico" As "Id",
	   			                        'BSCL.Facturacion.BD.Servicios::CrearContratoCobroPeriodico' As "Servicio",
	   			                        'ING' As "Resultado"
	   	                         From :v_SecuenciaContCobPer Union All
		                        
		                        Select "Campo", "Id", "Servicio", "Resultado" 
		                        From :p_out_ResultadoProceso; 
		                        
       v_TablaCodigosMensajes = Select "NumeroContratoCobroPeriodico" As "IdInterno", 'UI_CONTRATO_COBRO_PERIODICO_CREADO' As "Codigo",
                                       'IdContratoCobroPeriodico' As "CodigoDato1", Cast("IdContratoCobroPeriodico" As NVARCHAR(520)) as "ValorDato1",
                                       '' As "CodigoDato2", '' As "ValorDato2" 
		                        From :v_SecuenciaContCobPer;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
		
		p_out_InformacionDetalle = Select "Codigo", "Descripcion" 
		                           From :v_out_hash;
		
		Select Count("Codigo") 
		Into p_out_InformacionContador 
		From :p_out_InformacionDetalle;
      
    End; --END LOGICA    
END;

  