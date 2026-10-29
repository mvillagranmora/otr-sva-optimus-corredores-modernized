/**
    Actualizar Cobro por monto = (Monto Actual de Cobro/ Suma del cobro de los movimientos enviados) * Monto nuevo a calcular
                     por porcentaje = (Monto movimiento comprobante * porcentaje nuevo a calcular / 100) 
**/


SET SCHEMA "BSCL"; 
GO 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CalcularMovimientoComprobanteCobrosCostos";
GO

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CalcularMovimientoComprobanteCobrosCostos"
  (                                                    
   -- Parametros de Validacion                                                                                            
   IN p_IdEmpresa                     BIGINT,                                                                            
   IN p_IdUsuario                     BIGINT,                                                                            
   IN p_IdModulo                      NVARCHAR(3),
                                                                                                                          
   IN p_simularOperacion              NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN p_UsaReglasNegocio              NVARCHAR(1),	-- S/N se usa en caso de servicios anidados. 
   IN p_EsIngresoAutomatico           NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente                                    
   IN p_TipoCalculo                   NVARCHAR(1), -- 1 = Monto, 2 = Porcentaje

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
	
		
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) 
		into p_out_ErroresContador 
		FROM "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle";
		
		SELECT count(1) 
		into p_out_AdvertenciasContador 
		FROM "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_AdvertenciasDetalle";
	END;
  
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
        						
		Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
		
		
		Insert into "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle" 
		Select "IdInterno" As "Id", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
								
								
		SELECT COUNT(1) 
		INTO p_out_ErroresContador
		FROM "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle";
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
								   
            Insert into "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
            SELECT 0 As "Id", v_errorExt,::SQL_ERROR_MESSAGE 
            FROM dummy;
             								   
								   
			SELECT count(1) 
			into p_out_ErroresContador 
			FROM "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle";
											
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
        DECLARE v_p_FechaSistema NVARCHAR(10);
        DECLARE v_CantidadCobroGenerado, v_CantidadCobroRec, v_CantidadMontoCobro  BIGINT = 0;
        DECLARE LocalSistema  NVARCHAR(40) = 'CL';
        DECLARE v_DO BIGINT;

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
            Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_IN_Cobro"
            Select "IdCobro", "MontoCobro", "MontoCobroNuevo", "PorcentajeCobroNuevo"    
            From "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_Cobro";
            
            
            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos"(:v_resultadoRegla);
            
		    If :v_resultadoRegla = 'RECH' Then			        
		        Insert into "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso"
		        Select "IdCobro" As "Campo", 0 as "Id",
		               'BSCL.Facturacion.BD.Servicios::CalcularMovimientoComprobanteCobrosCostos' as "Servicio",
		               'RECH' as "Resultado"
		        From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados";       
		                                 
		        
			    Insert into "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
			    Select "Id", "Codigo","Descripcion"
			    From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"; 
			    
                
			    --SIGNAL SALIDA_CONTROLADA; 	
		    /*Else
		     If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
			     p_out_AdvertenciasDetalle = Select "NumeroMvtoCmbte", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
			                                 From :v_ErroresDetalle;
			                                 
			     Select count(1) 
			     Into p_out_AdvertenciasContador 
			     From :p_out_AdvertenciasDetalle;
		      End If;  */ 
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
          
          
        --Obtiene Fecha de Sistema
		Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_p_FechaSistema);

    --Obtiene valor de moneda de transaccion
		WITH dolarObservado AS (
		    SELECT ROW_NUMBER() OVER (PARTITION BY PF."IdParametroFinanciero" ORDER BY PF."Fecha" DESC) AS row_num,
		        PF."Valor"
		    FROM
		        "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
		    WHERE
		        PF."Fecha" <= :v_p_FechaSistema
		        AND PF."IdParametroFinanciero" = 'DO')
					SELECT
					    COALESCE("Valor", 0)
					INTO v_DO
					FROM
					    dolarObservado
					WHERE
					    row_num = 1;

		v_tblParametrosFinancieros = WITH dolarObservado AS (
										    SELECT
										        ROW_NUMBER() OVER (
										            PARTITION BY PF."IdParametroFinanciero"
										            ORDER BY
										                PF."Fecha" DESC
										        ) AS row_num,
										        PF."IdParametroFinanciero",
										        PF."Fecha",
										        PF."Valor"
										    FROM
										        "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
										    WHERE
										        PF."Fecha" <= :v_p_FechaSistema
										)
										SELECT
											"IdParametroFinanciero",
											"Fecha",
										    CASE WHEN "IdParametroFinanciero" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP') THEN 
										    	"Valor" * :v_DO
										    ELSE
										    	"Valor"
										    END AS "Valor"
										FROM
										    dolarObservado
										WHERE
										    row_num = 1 AND "IdParametroFinanciero" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO');
		          
        v_MvtoFactCostos = Select "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro".Nextval As "IdMovimientoComprobanteCobroNuevo",
                                  "IdMovimientoComprobanteCobro", "IdMovimientoComprobante", "IdCobro", "MontoCobro"
                           From "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_MovimientoComprobanteCobro" As MvtoCmbteCobro
                           Where MvtoCmbteCobro."IdCobro" Not In (Select "IdCobro" 
                                                                  From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados");
                                                                     
        /*select 'v_MvtoFactCostos', *
        from :v_MvtoFactCostos;*/
                                                                     
        If p_TipoCalculo = 1 Then  
           If LocalSistema = 'CL' THEN
              --- Actualiza cobro segun Monto
	          v_CobroMvtoCmbte = Select "IdMovimientoComprobanteCobroNuevo",
	                                    MvtoFactCos."IdMovimientoComprobanteCobro" As "IdMovimientoComprobanteCobroAnt", 
	                                    MvtoFactCos."IdMovimientoComprobante", MvtoFactCosAgr."IdCobro", 
	                                    MvtoFactCos."MontoCobro", 
	                                    MvtoFactCosAgr."MontoCobroNuevo", MvtoFactCosAgr."PorcentajeCobroNuevo" As "Porcentaje",
                                      0 AS "MontoCobroMonedaActualizado",
	                                    "IdMonedaMovimientoComprobanteCobro", "IdMonedaCobro", "IdMonedaLiquidacionCobro",
	                                    Case
	                                      When MvtoFactCosAgr."MontoCobro" = 0 Then Round(MvtoFactCosAgr."MontoCobroNuevo",0) 
	                                    Else
	                                      Round(((MvtoFactCos."MontoCobro"/ MvtoFactCosAgr."MontoCobro") * MvtoFactCosAgr."MontoCobroNuevo"),0) 
	                                    End As "MontoCobroActualizado",  	                                                                    
	                                    "Monto", "MontoLiquidacion", "MontoMovimientoComprobante"
	                             From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" As ConsMvtoCmbteCobro
	                               Inner Join :v_MvtoFactCostos As MvtoFactCos
	                                  On MvtoFactCos."IdMovimientoComprobanteCobro" = ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro"
	                               Inner Join "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_Cobro" As MvtoFactCosAgr
	                                  On MvtoFactCosAgr."IdCobro" = MvtoFactCos."IdCobro"
	                             Where "IdEstado" = 'VIG';
	       Else 
	         v_CobroMvtoCmbte = Select "IdMovimientoComprobanteCobroNuevo",
	                                   MvtoFactCos."IdMovimientoComprobanteCobro" As "IdMovimientoComprobanteCobroAnt", 
	                                   MvtoFactCos."IdMovimientoComprobante", MvtoFactCosAgr."IdCobro", 
	                                   MvtoFactCos."MontoCobro", 
	                                   MvtoFactCosAgr."MontoCobroNuevo", MvtoFactCosAgr."PorcentajeCobroNuevo" As "Porcentaje",
                                     0 AS "MontoCobroMonedaActualizado",
	                                   "IdMonedaMovimientoComprobanteCobro", "IdMonedaCobro", "IdMonedaLiquidacionCobro",
	                                   Case
	                                     When MvtoFactCosAgr."MontoCobro" = 0 Then Round(MvtoFactCosAgr."MontoCobroNuevo",0) 
	                                   Else
	                                      Round(((MvtoFactCos."MontoCobro"/ MvtoFactCosAgr."MontoCobro") * MvtoFactCosAgr."MontoCobroNuevo"),0) 
	                                   End As "MontoCobroActualizado",  	                                                                    
	                                   "Monto", "MontoLiquidacion", "MontoMovimientoComprobante"
	                            From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru" As ConsMvtoCmbteCobro
	                              Inner Join :v_MvtoFactCostos As MvtoFactCos
	                                 On MvtoFactCos."IdMovimientoComprobanteCobro" = ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro"
	                              Inner Join "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_Cobro" As MvtoFactCosAgr
	                                 On MvtoFactCosAgr."IdCobro" = MvtoFactCos."IdCobro"
	                            Where "IdEstado" = 'VIG';
	       End If;
	    Else
	    
	      /*select 'v_MvtoFactCostos', *
	      from :v_MvtoFactCostos;*/
	      
	      --- Actualiza cobro segun Porcentaje
	      v_CobroMvtoCmbte = Select "IdMovimientoComprobanteCobroNuevo", 
	                                MvtoFactCos."IdMovimientoComprobanteCobro" As "IdMovimientoComprobanteCobroAnt", 
	                                MvtoFactCos."IdMovimientoComprobante", MvtoFactCosAgr."IdCobro", 
	                                MvtoFactCos."MontoCobro", MvtoFactCosAgr."MontoCobroNuevo", MvtoFactCosAgr."PorcentajeCobroNuevo" As "Porcentaje",
                                  0 AS "MontoCobroMonedaActualizado",
	                                "IdMonedaMovimientoComprobanteCobro", "IdMonedaCobro", "IdMonedaLiquidacionCobro",
	                                Round(((ConsMvtoCmbteCobro."MontoMovimientoComprobante" * MvtoFactCosAgr."PorcentajeCobroNuevo") / 100),0) As "MontoCobroActualizado",
	                                "Monto", "MontoLiquidacion", "MontoMovimientoComprobante"
	                         From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" As ConsMvtoCmbteCobro
	                           Inner Join :v_MvtoFactCostos As MvtoFactCos
	                              On MvtoFactCos."IdMovimientoComprobanteCobro" = ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro"
	                           Inner Join "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_Cobro" As MvtoFactCosAgr
	                              On MvtoFactCosAgr."IdCobro" = MvtoFactCos."IdCobro"
	                         Where "IdEstado" = 'VIG';
	                         
	       /*Select 'v_CobroMvtoCmbte', *
	       from :v_CobroMvtoCmbte;*/                  
	                         
	    End If;

       v_CobroMvtoCmbte =
							SELECT
								CMBTE."IdMovimientoComprobanteCobroNuevo", 
								CMBTE."IdMovimientoComprobanteCobroAnt",
								CMBTE."IdMovimientoComprobante",
								CMBTE."IdCobro",
                				CMBTE."MontoCobro",
								CMBTE."MontoCobroNuevo",
								CMBTE."Porcentaje",
								CASE 
									WHEN CMBTE."IdMonedaLiquidacionCobro" IN ('USD', 'EUR', 'DO') THEN
										ROUND(CMBTE."MontoCobroActualizado" / COALESCE(PF."Valor", 1), 2)
									ELSE
										CMBTE."MontoCobroActualizado"
								END AS "MontoCobroMonedaActualizado",
								CMBTE."IdMonedaMovimientoComprobanteCobro",
								CMBTE."IdMonedaCobro",
								CMBTE."IdMonedaLiquidacionCobro",
								CMBTE."MontoCobroActualizado",
								CMBTE."Monto",
								CMBTE."MontoLiquidacion",	
								CMBTE."MontoMovimientoComprobante"
							FROM
								:v_CobroMvtoCmbte CMBTE
							LEFT JOIN :v_tblParametrosFinancieros PF ON PF."IdParametroFinanciero" = CASE WHEN CMBTE."IdMonedaLiquidacionCobro" = 'USD' THEN 'DO' ELSE CMBTE."IdMonedaLiquidacionCobro" END;                               
	                                  
	                                     
        /*select 'v_CobroMvtoCmbte', *
        from :v_CobroMvtoCmbte;*/
        
        -- Anula movimiento comprobante cobro ya existente      
        Update MvtoCmbteCob 
        Set "IdEstado" = 'ANU'
        From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCob
          Inner Join :v_CobroMvtoCmbte AS CobMvtoCmbte
             On CobMvtoCmbte."IdMovimientoComprobanteCobroAnt" = MvtoCmbteCob."IdMovimientoComprobanteCobro";
             
             
        -- Registra movimiento comprobante cobro
        Insert into "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" 
          ( "IdMovimientoComprobanteCobro",
            "IdCobro",
            "IdMovimientoComprobante",
            "Porcentaje",
            "FechaSistema",
            "FechaIngresoRegistro",
            "IdEstado",
            "IdMonedaMovimientoComprobanteCobro",
            "MontoMovimientoComprobanteCobro",
            "MontoCobro",
            "MontoLiquidacionCobro",
            "IdMonedaCobro",
            "IdMonedaLiquidacionCobro",
            "Monto",
            "MontoLiquidacion",
            "MontoMovimientoComprobante"
         )
        Select "IdMovimientoComprobanteCobroNuevo",
               "IdCobro",
               "IdMovimientoComprobante",
               "Porcentaje",
               v_p_FechaSistema,
               CURRENT_UTCTIMESTAMP,
               'VIG',
               "IdMonedaMovimientoComprobanteCobro",
               "MontoCobroActualizado",
               "MontoCobroActualizado",
               "MontoCobroMonedaActualizado",
               "IdMonedaCobro",
               "IdMonedaLiquidacionCobro",
               "Monto",
               "MontoLiquidacion",
               "MontoMovimientoComprobante" 
        From :v_CobroMvtoCmbte;     
        
        
        
        
        
        --Actualiza Monto cuando sea necesario, para cobros recalculables
        v_MvtoFactCostosMvtoCmbte = Select Distinct "IdMovimientoComprobante"
                                    From :v_MvtoFactCostos;
        
        
        IF LocalSistema = 'CL' THEN 
           v_MvtoCmbteCobrosRecal = Select "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro".Nextval As "IdMovimientoComprobanteCobroNuevo",
                                            ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro", ConsMvtoCmbteCobro."IdMovimientoComprobante", ConsMvtoCmbteCobro."IdCobro", 
                                            ConsMvtoCmbteCobro."EsRecalculable",  ConsMvtoCmbteCobro."Porcentaje", ConsMvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro", 
                                            ConsMvtoCmbteCobro."IdMonedaCobro", ConsMvtoCmbteCobro."IdMonedaLiquidacionCobro", ConsMvtoCmbteCobro."Monto", 
                                            ConsMvtoCmbteCobro."MontoLiquidacion", ConsMvtoCmbteCobro."MontoMovimientoComprobante", ConsMvtoCmbteCobro."IdEstado", 
                                            ConsMvtoCmbteCobro."MontoCobro", ConsMvtoCmbteCobro."MontoMovimientoComprobanteCobro", ConsMvtoCmbteCobro."MontoLiquidacionCobro"
                                    From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" As ConsMvtoCmbteCobro
                                      Inner Join :v_MvtoFactCostosMvtoCmbte As MvtoFactCos
                                         On MvtoFactCos."IdMovimientoComprobante" =  ConsMvtoCmbteCobro."IdMovimientoComprobante"
                                        And ConsMvtoCmbteCobro."EsRecalculable" = 'S'
                                        And ConsMvtoCmbteCobro."IdEstado" = 'VIG';
       Else                                 
         v_MvtoCmbteCobrosRecal = Select "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro".Nextval As "IdMovimientoComprobanteCobroNuevo",
                                            ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro", ConsMvtoCmbteCobro."IdMovimientoComprobante", ConsMvtoCmbteCobro."IdCobro", 
                                            ConsMvtoCmbteCobro."EsRecalculable",  ConsMvtoCmbteCobro."Porcentaje", ConsMvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro", 
                                            ConsMvtoCmbteCobro."IdMonedaCobro", ConsMvtoCmbteCobro."IdMonedaLiquidacionCobro", ConsMvtoCmbteCobro."Monto", 
                                            ConsMvtoCmbteCobro."MontoLiquidacion", ConsMvtoCmbteCobro."MontoMovimientoComprobante", ConsMvtoCmbteCobro."IdEstado", 
                                            ConsMvtoCmbteCobro."MontoCobro", ConsMvtoCmbteCobro."MontoMovimientoComprobanteCobro", ConsMvtoCmbteCobro."MontoLiquidacionCobro"
                                    From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru" As ConsMvtoCmbteCobro
                                      Inner Join :v_MvtoFactCostosMvtoCmbte As MvtoFactCos
                                         On MvtoFactCos."IdMovimientoComprobante" =  ConsMvtoCmbteCobro."IdMovimientoComprobante"
                                        And ConsMvtoCmbteCobro."IdCobroClasificacion" = 'IMPUESTO_IGV'
                                        And ConsMvtoCmbteCobro."IdEstado" = 'VIG';      
       End If;
       
       
        /*Select 'v_MvtoCmbteCobrosRecal', *
        From :v_MvtoCmbteCobrosRecal;*/
        
        
        Select Count("IdCobro")
        Into v_CantidadCobroRec 
        From :v_MvtoCmbteCobrosRecal;                       
            
        
        If v_CantidadCobroRec > 0 THEN
          If LocalSistema = 'CL' Then 
             v_MvtoCmbteCobrosRecalAActualizar = Select MvtoCmbteCobroAAct."IdMovimientoComprobante", Sum(ConsMvtoCmbteCobros."MontoCobro") As "MontoCobro", 
                                                        Sum(ConsMvtoCmbteCobros."MontoMovimientoComprobanteCobro") As "MontoMovimientoComprobanteCobro", 
                                                        Sum(ConsMvtoCmbteCobros."MontoLiquidacionCobro") As "MontoLiquidacionCobro"
                                                 From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" As ConsMvtoCmbteCobros 
                                                   Inner Join :v_MvtoFactCostosMvtoCmbte As MvtoCmbteCobroAAct
                                                      On MvtoCmbteCobroAAct."IdMovimientoComprobante" = ConsMvtoCmbteCobros ."IdMovimientoComprobante"
                                                     And ConsMvtoCmbteCobros."EsRecalculable" <> 'S'
                                                     And ConsMvtoCmbteCobros."IdEstado" = 'VIG'
                                                 Group By MvtoCmbteCobroAAct."IdMovimientoComprobante";
          Else                                       
            v_MvtoCmbteCobrosRecalAActualizar = Select MvtoCmbteCobroAAct."IdMovimientoComprobante", Sum(ConsMvtoCmbteCobros."MontoCobro") As "MontoCobro", 
                                                        Sum(ConsMvtoCmbteCobros."MontoMovimientoComprobanteCobro") As "MontoMovimientoComprobanteCobro", 
                                                        Sum(ConsMvtoCmbteCobros."MontoLiquidacionCobro") As "MontoLiquidacionCobro"
                                                 From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru" As ConsMvtoCmbteCobros 
                                                   Inner Join :v_MvtoFactCostosMvtoCmbte As MvtoCmbteCobroAAct
                                                      On MvtoCmbteCobroAAct."IdMovimientoComprobante" = ConsMvtoCmbteCobros ."IdMovimientoComprobante"
                                                     And ConsMvtoCmbteCobros."IdCobroClasificacion" <> 'IMPUESTO_IGV'
                                                     And ConsMvtoCmbteCobros."IdEstado" = 'VIG'
                                                 Group By MvtoCmbteCobroAAct."IdMovimientoComprobante"; 
          
          
          End If;  
          /*Select 'v_MvtoCmbteCobrosRecalAActualizar', *
          from :v_MvtoCmbteCobrosRecalAActualizar;*/
            
             
          Select Count("MontoCobro")
          Into v_CantidadMontoCobro
          From :v_MvtoCmbteCobrosRecalAActualizar;
             
          If v_CantidadMontoCobro > 0 Then
            -- Anula movimiento comprobante cobro ya existente - Recalculables
            Update MvtoCmbteCob 
            Set "IdEstado" = 'ANU'
            From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCob
              Inner Join :v_MvtoCmbteCobrosRecal As CobMvtoCmbte
                 On CobMvtoCmbte."IdMovimientoComprobanteCobro" = MvtoCmbteCob."IdMovimientoComprobanteCobro";
                  
                   
            -- Registra movimiento comprobante cobro - Recalculables
            Insert into "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" 
              ( "IdMovimientoComprobanteCobro",
                "IdCobro",
                "IdMovimientoComprobante",
                "Porcentaje",
                "FechaSistema",
                "FechaIngresoRegistro",
                "IdEstado",
                "IdMonedaMovimientoComprobanteCobro",
                "MontoMovimientoComprobanteCobro",
                "MontoCobro",
                "MontoLiquidacionCobro",
                "IdMonedaCobro",
                "IdMonedaLiquidacionCobro",
                "Monto",
                "MontoLiquidacion",
                "MontoMovimientoComprobante"
              )
            Select "IdMovimientoComprobanteCobroNuevo",
                   MvtoCmbteCobRec."IdCobro",
                   MvtoCmbteCobRec."IdMovimientoComprobante",
                   MvtoCmbteCobRec."Porcentaje",
                   v_p_FechaSistema,
                   CURRENT_UTCTIMESTAMP,
                   'VIG',
                   MvtoCmbteCobRec."IdMonedaMovimientoComprobanteCobro",
                   (MvtoCmbteCobroRecalAAct."MontoCobro") * (MvtoCmbteCobRec."Porcentaje" / 100),
                   (MvtoCmbteCobroRecalAAct."MontoMovimientoComprobanteCobro") * (MvtoCmbteCobRec."Porcentaje" / 100),
                   (MvtoCmbteCobroRecalAAct."MontoLiquidacionCobro") * (MvtoCmbteCobRec."Porcentaje" / 100),                   
                   MvtoCmbteCobRec."IdMonedaCobro",
                   MvtoCmbteCobRec."IdMonedaLiquidacionCobro",
                   MvtoCmbteCobRec."Monto",
                   MvtoCmbteCobRec."MontoLiquidacion",
                   MvtoCmbteCobRec."MontoMovimientoComprobante" 
            From :v_MvtoCmbteCobrosRecal As MvtoCmbteCobRec
              Inner Join :v_MvtoCmbteCobrosRecalAActualizar As MvtoCmbteCobroRecalAAct
                 On MvtoCmbteCobroRecalAAct."IdMovimientoComprobante" = MvtoCmbteCobRec."IdMovimientoComprobante";

	      End If;
        End If;
                
        
        

        /** Acumula los movimientos cobros generados exitosos **/
        Insert into "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobanteCobro"
        Select "IdMovimientoComprobanteCobroNuevo", "IdMovimientoComprobante", "IdCobro", "MontoCobroActualizado"
        From :v_CobroMvtoCmbte;
        
        
        
        --*********************
		--* MENSAJE DE EXITO  *
		--*********************
        /** Inserta registro de resultado de proceso de servicio completo **/		                         
		Insert into "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso" 
		Select "IdCobro" As "Campo", 0 As "Id", 'BSCL.Facturacion.BD.Servicios::CalcularMovimientoComprobanteCobrosCostos' As "Servicio",
		       'ING' As "Resultado"
		From "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobanteCobro" Union All
		Select "IdCobro" As "Campo", 0 as "Id", 'BSCL.Facturacion.BD.Servicios::CalcularMovimientoComprobanteCobrosCostos' as "Servicio",
		       'RECH' as "Resultado"
		From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados";                         
		                         
		                        
		
		
		/** Acumula comprobantes generados exitosos **/
		Select Count("IdCobro")
		Into v_CantidadCobroGenerado
        From "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobanteCobro";
		
				
		If v_CantidadCobroGenerado > 0 Then 
		   v_TablaCodigosMensajes = Select Distinct "IdMovimientoComprobante" As "IdInterno", 'UI_MOVIMIENTO_COMPROBANTE_COBRO_MODIFICADO' As "Codigo",
                                           'IdMovimientoComprobante' As "CodigoDato1", Cast("IdMovimientoComprobante" As NVARCHAR(520)) as "ValorDato1",
                                           '' As "CodigoDato2", '' As "ValorDato2" 
                                    From "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobanteCobro";
		   
	    
  		   CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				   
		   
		   Insert Into "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_InformacionDetalle" 
		   Select "Codigo", "Descripcion"
		   From :v_out_hash;                           
		
		   Select Count("Codigo") 
		   Into p_out_InformacionContador 
           From "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_InformacionDetalle"; 
           
		End If;   
		
    END; --LOGICA
END;