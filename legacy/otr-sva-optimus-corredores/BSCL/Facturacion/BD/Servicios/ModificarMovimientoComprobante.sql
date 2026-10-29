SET SCHEMA "BSCL";
GO 
 
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante";
GO 

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante"
-- 
--  Proposito: Modificar un movimiento a facturar seleccionado
--  Autor: Natalia Silva Muñoz
--  Fecha Creacion: 15/03/2016 
-- 
(    
     -- Parametros de Validacion
     IN p_IdEmpresa BIGINT, 
     IN p_IdUsuario BIGINT, 
     IN p_IdModulo  NVARCHAR(3),
     
     IN p_simularOperacion    NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)
     IN p_UsaReglasNegocio 	  NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
     IN p_EsIngresoAutomatico NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente

     -- Parametros de Salida
     OUT p_out_InformacionContador  INT,
     OUT p_out_AdvertenciasContador INT,
     OUT p_out_ErroresContador      INT

)
     
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
		
AS
  Begin
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	
    	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
	    Select count(1) 
        Into p_out_ErroresContador
        From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle";
		
        Select count(1) 
        Into p_out_AdvertenciasContador
        From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_AdvertenciasDetalle";
        
	END;
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
        v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		Insert into "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle"
		Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
		
		
		Select Count(1)
		Into p_out_ErroresContador
		From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle";
		 
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
	
		Declare v_errorExt NVARCHAR(40);
		
		If ::SQL_ERROR_CODE > 10000 THEN 		
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			
			Insert into "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle"
			Select 0 As "IdMovimientoComprobante", v_errorExt,::SQL_ERROR_MESSAGE 
			From dummy;
			
			Select Count(1)
			Into p_out_ErroresContador 
			From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle";
			
		Else
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		End If;		
	END;
	
	
	p_out_InformacionContador   := 0;
	p_out_AdvertenciasContador	:= 0;	
	p_out_ErroresContador		:= 0;	
		
	
	BEGIN --LOGICA
       --------------------
	   --Declare Variables
	   -------------------- 
       Declare v_CantidadCobroRec, v_CantidadMvtoCmbte, v_CantidadMvtoCmbteCobro, v_CantidadMvtoCmbteFDePago,
               v_CantidadMontoCobro, v_CantMvtoCmbte, v_CantMvtoCmbteCobro, v_CantMvtoCmbteFDPago, v_CantidadMvtoCmbteFDePagoAAct   BIGINT := 0;
               
       Declare v_p_FechaSistema NVARCHAR(10);
       
      
       
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
            Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_IN_MovimientoComprobante"
            Select "IdMovimientoComprobante"
            From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante" Union All
            Select "IdMovimientoComprobante"
            From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteFormaDePago" Union All
            Select "IdMovimientoComprobante"
            From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteCobro";
            

            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosModificarMovimientoComprobante"(v_resultadoRegla);
		    If :v_resultadoRegla = 'RECH' Then
                Insert into "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ResultadoProceso"
                Select "IdMovimientoComprobante" As "Campo", "IdMovimientoComprobante" as "Id",
                       'BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante' as "Servicio",
		               'RECH' as "Resultado"		               
                From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados"; 
                    
                                       
                Insert into "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle"
                Select "IdMovimientoComprobante", "Codigo","Descripcion"
                From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_ErroresDetalle";
                
			    --SIGNAL SALIDA_CONTROLADA; 	
		    --Else
		      --If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
			     /*p_out_AdvertenciasDetalle = Select "IdMovimientoComprobante", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
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
        --Obtiene Fecha de Sistema
		Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_p_FechaSistema);
		
        /** Movimiento Comprobante - General **/
        v_MvtoCmbteAActualizar = Select *
                                 From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte
                                 Where MvtoCmbte."IdMovimientoComprobante" Not In (Select "IdMovimientoComprobante" 
                                                                                   From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados");
                                                                                   
        
        /*select 'v_MvtoCmbteAActualizar', *
        from :v_MvtoCmbteAActualizar;*/
        
        
        Select Count("IdMovimientoComprobante")
        Into v_CantidadMvtoCmbte
        From :v_MvtoCmbteAActualizar;
        
        If v_CantidadMvtoCmbte > 0 Then        
           -- Actualiza movimiento comprobante - General
	       Update MvtoCmbte
           Set "IdPortafolio" = COALESCE(MvtoCmbteAAct."IdPortafolio", MvtoCmbte."IdPortafolio", 0),
		       "IdFormaCustodia" = COALESCE(MvtoCmbteAAct."IdFormaCustodia", MvtoCmbte."IdFormaCustodia", ''),
		       "EsComprobIndividual" = COALESCE(MvtoCmbteAAct."EsComprobIndividual", MvtoCmbte."EsComprobIndividual", 'N'),
		       "Glosa" = COALESCE(MvtoCmbteAAct."Glosa", MvtoCmbte."Glosa", '')
           From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
             Inner Join :v_MvtoCmbteAActualizar As MvtoCmbteAAct
                On MvtoCmbteAAct."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante";
           
			--IMPMBIOPT-573 : Actualiza solo cuando es Factura Electronica Exenta y Prestamo o Anticipo Prestamo
			UPDATE MvtoCmbte
			SET "MontoMovimientoComprobante" = COALESCE(MvtoCmbteAAct."MontoMovimientoComprobante", MvtoCmbte."MontoMovimientoComprobante", 0)
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbte
			INNER JOIN :v_MvtoCmbteAActualizar AS MvtoCmbteAAct
				ON (MvtoCmbteAAct."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante")
			WHERE MvtoCmbte."IdTipoComprobante" = 'FEE'
			  AND MvtoCmbte."IdTipoOperacion" IN ('PRESTA', 'ANTPRT');
                
	    End If;
	    
	    
	    
	    
	    
	    /** Movimiento Comprobante - Cobro **/	   
	    v_MvtoCmbteCobrosAActualizar = Select "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro".Nextval As "IdMovimientoComprobanteCobroNuevo",
	                                          "IdMovimientoComprobanteCobro", "IdMovimientoComprobante", "IdCobro", "IdEstado", "Porcentaje", "IdMonedaCobro", "MontoCobro", 
	                                          "IdMonedaMovimientoComprobanteCobro", "MontoMovimientoComprobanteCobro", "IdMonedaLiquidacionCobro", "MontoLiquidacionCobro"
	                                          ,"MontoMovimientoComprobante"
                                       From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteCobro" As MvtoCmbteCobros
                                       Where MvtoCmbteCobros."IdMovimientoComprobante" Not in (Select "IdMovimientoComprobante"
                                                                                               From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados");
                                                                                               
        
        /*select 'v_MvtoCmbteCobrosAActualizar', *
        from :v_MvtoCmbteCobrosAActualizar;*/
        
        Select Count("IdMovimientoComprobanteCobro")
        Into v_CantidadMvtoCmbteCobro
        From :v_MvtoCmbteCobrosAActualizar;
        
        If v_CantidadMvtoCmbteCobro > 0 Then
			--IMPMBIOPT-573 : Actualiza sólo cuando es Factura Electronica Exenta y Prestamo o Anticipo Prestamo
			UPDATE MvtoCmbte
			SET "MontoMovimientoComprobante" = COALESCE(MvtoCmbteCobroAAct."MontoMovimientoComprobante", MvtoCmbte."MontoMovimientoComprobante", 0)
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbte
			INNER JOIN :v_MvtoCmbteCobrosAActualizar AS MvtoCmbteCobroAAct
				ON (MvtoCmbteCobroAAct."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante")
			WHERE MvtoCmbte."IdTipoComprobante" = 'FEE'
			  AND MvtoCmbte."IdTipoOperacion" IN ('PRESTA', 'ANTPRT');
		
		  v_DatosClienteLog = 
		  	SELECT 
		  		MovCom."IdMovimientoComprobante",
		  		MovCom."IdCliente",
		  		MovCom."NombreLargoCliente",
		  		MovCom."DocumentoIdentidad",
		  		MovCom."IdPortafolio"
		  	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" as MovCom
		  	INNER JOIN :v_MvtoCmbteCobrosAActualizar as MovActCli
		  	ON (MovCom."IdMovimientoComprobante" = MovActCli."IdMovimientoComprobante")
		  	LIMIT 1;
		  	
		  v_DatosAntiguosLog =
		  	SELECT
				MovComCob."IdMovimientoComprobante",
				MovComCob."IdCobro",
				MovComCob."MontoCobro",
				MovComCob."IdEstado"
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" as MovComCob
			INNER JOIN :v_MvtoCmbteCobrosAActualizar as MovActAnt
			ON (MovComCob."IdMovimientoComprobante" = MovActAnt."IdMovimientoComprobante")
      AND (MovComCob."IdMovimientoComprobanteCobro" = MovActAnt."IdMovimientoComprobanteCobro")
      AND (MovComCob."IdCobro" = MovActAnt."IdCobro")
			WHERE MovComCob."IdEstado" = 'VIG';
			
			  
          Update MvtoCmbteCobro
          Set "IdEstado" = 'ANU'
          From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCobro
            Inner Join :v_MvtoCmbteCobrosAActualizar As MvtoCmbteCobroAAct
               On MvtoCmbteCobroAAct."IdMovimientoComprobanteCobro" = MvtoCmbteCobro."IdMovimientoComprobanteCobro"
              And MvtoCmbteCobroAAct."IdMovimientoComprobante" = MvtoCmbteCobro."IdMovimientoComprobante" 
              And MvtoCmbteCobroAAct."IdCobro" = MvtoCmbteCobro."IdCobro";
              
                   
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
          Select 
                 MvtoCmbteCobroAAct."IdMovimientoComprobanteCobroNuevo",
                 MvtoCmbteCobro."IdCobro",
                 MvtoCmbteCobroAAct."IdMovimientoComprobante",
                 COALESCE(MvtoCmbteCobroAAct."Porcentaje", MvtoCmbteCobro."Porcentaje", 0),
                 v_p_FechaSistema,
                 CURRENT_UTCTIMESTAMP,                 
                 'VIG' As "IdEstado",
                 MvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro",
                 COALESCE(MvtoCmbteCobroAAct."MontoMovimientoComprobanteCobro", MvtoCmbteCobroAAct."MontoCobro", 0),
                 COALESCE(MvtoCmbteCobroAAct."MontoCobro", MvtoCmbteCobro."MontoCobro", 0),
                 COALESCE(MvtoCmbteCobroAAct."MontoLiquidacionCobro", MvtoCmbteCobroAAct."MontoCobro", 0),
                 MvtoCmbteCobro."IdMonedaCobro",
                 MvtoCmbteCobro."IdMonedaLiquidacionCobro",
                 MvtoCmbteCobro."Monto",
                 MvtoCmbteCobro."MontoLiquidacion",
                 MvtoCmbteCobro."MontoMovimientoComprobante"                
          From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCobro
            Inner Join :v_MvtoCmbteCobrosAActualizar As MvtoCmbteCobroAAct
               On MvtoCmbteCobroAAct."IdMovimientoComprobanteCobro" = MvtoCmbteCobro."IdMovimientoComprobanteCobro"
              And MvtoCmbteCobroAAct."IdMovimientoComprobante" = MvtoCmbteCobro."IdMovimientoComprobante" 
              And MvtoCmbteCobroAAct."IdCobro" = MvtoCmbteCobro."IdCobro";
              
          
          /*select 'v_MvtoCmbteCobrosAActualizar', *
          from :v_MvtoCmbteCobrosAActualizar;*/


      
	      --Actualiza Monto cuando sea necesario, para cobros recalculables
			v_IdsMvtoCmbteCobrosAActualizar =
				SELECT DISTINCT("IdMovimientoComprobante") AS "IdMovimientoComprobante"
				FROM :v_MvtoCmbteCobrosAActualizar;
				
          v_MvtoCmbteCobrosRecal = Select "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro".Nextval As "IdMovimientoComprobanteCobroNuevo",
                                          ConsMvtoCmbteCobro."IdMovimientoComprobanteCobro", ConsMvtoCmbteCobro."IdMovimientoComprobante", ConsMvtoCmbteCobro."IdCobro", 
                                          ConsMvtoCmbteCobro."EsRecalculable",  ConsMvtoCmbteCobro."Porcentaje", ConsMvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro", 
                                          ConsMvtoCmbteCobro."IdMonedaCobro", ConsMvtoCmbteCobro."IdMonedaLiquidacionCobro", ConsMvtoCmbteCobro."Monto", 
                                          ConsMvtoCmbteCobro."MontoLiquidacion", ConsMvtoCmbteCobro."MontoMovimientoComprobante", ConsMvtoCmbteCobro."IdEstado", 
                                          ConsMvtoCmbteCobro."MontoCobro", ConsMvtoCmbteCobro."MontoMovimientoComprobanteCobro", ConsMvtoCmbteCobro."MontoLiquidacionCobro"
                                   From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" As ConsMvtoCmbteCobro
                                   INNER JOIN :v_IdsMvtoCmbteCobrosAActualizar As MvtoCmbteCobro
									On MvtoCmbteCobro."IdMovimientoComprobante" =  ConsMvtoCmbteCobro."IdMovimientoComprobante"
									And ConsMvtoCmbteCobro."EsRecalculable" = 'S'
									And ConsMvtoCmbteCobro."IdEstado" = 'VIG';
                                       
                                       
           /*Select 'v_MvtoCmbteCobrosRecal', *
           from :v_MvtoCmbteCobrosRecal;*/

           Select Count("IdCobro")
           Into v_CantidadCobroRec
           From :v_MvtoCmbteCobrosRecal;             

       
          If v_CantidadCobroRec > 0 Then              
            v_MvtoCmbteCobrosRecalAActualizar = Select MvtoCmbteCobroAAct."IdMovimientoComprobante", Sum(ConsMvtoCmbteCobros."MontoCobro") As "MontoCobro", 
                                                       Sum(ConsMvtoCmbteCobros."MontoMovimientoComprobanteCobro") As "MontoMovimientoComprobanteCobro", 
                                                       Sum(ConsMvtoCmbteCobros."MontoLiquidacionCobro") As "MontoLiquidacionCobro"
                                                From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" As ConsMvtoCmbteCobros 
                                                  Inner Join :v_MvtoCmbteCobrosAActualizar As MvtoCmbteCobroAAct
                                                     On MvtoCmbteCobroAAct."IdMovimientoComprobante" = ConsMvtoCmbteCobros ."IdMovimientoComprobante"
                                                    And ConsMvtoCmbteCobros."EsRecalculable" <> 'S'
                                                    And ConsMvtoCmbteCobros."IdEstado" = 'VIG'
                                                Group By MvtoCmbteCobroAAct."IdMovimientoComprobante";
            
            /*Select 'v_MvtoCmbteCobrosRecalAActualizar', *
            from :v_MvtoCmbteCobrosRecalAActualizar;*/
           
             
            Select Count("MontoCobro")
            Into v_CantidadMontoCobro
            From :v_MvtoCmbteCobrosRecalAActualizar;
             
            If v_CantidadMontoCobro > 0 Then
              Update MvtoCmbteCobro
              Set "IdEstado" = 'ANU'
              From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCobro
                Inner Join :v_MvtoCmbteCobrosRecal As MvtoCmbteCobroRecal
                   On MvtoCmbteCobroRecal."IdMovimientoComprobanteCobro" = MvtoCmbteCobro."IdMovimientoComprobanteCobro";
              
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
                     (MvtoCmbteCobroRecalAAct."MontoCobro") * 0.19,
                     (MvtoCmbteCobroRecalAAct."MontoMovimientoComprobanteCobro") * 0.19,
                     (MvtoCmbteCobroRecalAAct."MontoLiquidacionCobro") * 0.19,                   
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
        End If;
        
        
        
        /** Movimiento Comprobante - Forma de Pago **/
        v_MvtoCmbteFDPagoAActualizar = Select *
                                       From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteFormaDePago" As MvtoCmbteFDPago
                                       Where MvtoCmbteFDPago."IdMovimientoComprobante" Not in (Select "IdMovimientoComprobante"
                                                                                               From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados");
                                                                                               
        Select Count("IdMovimientoComprobanteFormaDePago")
        Into v_CantidadMvtoCmbteFDePago
        From :v_MvtoCmbteFDPagoAActualizar;
        
        If v_CantidadMvtoCmbteFDePago > 0 Then
			--IMPMBIOPT-573 : Actualiza sólo cuando es Factura Electronica Exenta y Prestamo o Anticipo Prestamo
			UPDATE MvtoCmbte
			SET "MontoMovimientoComprobante" = COALESCE(MvtoCmbteFDPAAct."MontoMovimientoComprobante", MvtoCmbte."MontoMovimientoComprobante", 0)
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbte
			INNER JOIN :v_MvtoCmbteFDPagoAActualizar AS MvtoCmbteFDPAAct
				ON (MvtoCmbteFDPAAct."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante")
			WHERE MvtoCmbte."IdTipoComprobante" = 'FEE'
			  AND MvtoCmbte."IdTipoOperacion" IN ('PRESTA', 'ANTPRT');
		
          Select Count(ConsMvtoCmbteFDPago."IdMovimientoComprobanteFormaDePago")
          Into v_CantidadMvtoCmbteFDePagoAAct
          From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago" As ConsMvtoCmbteFDPago
            Inner Join :v_MvtoCmbteFDPagoAActualizar As MvtoCmbteFDPAAct
               On MvtoCmbteFDPAAct."IdMovimientoComprobante" = ConsMvtoCmbteFDPago."IdMovimientoComprobante";
               
          
          If v_CantidadMvtoCmbteFDePagoAAct = 0 Then
            -- Crea movimiento comprobante - Forma de Pago Cuenta Inversion
            Insert into "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"
              (
                "IdMovimientoComprobanteFormaDePago",
                "IdMovimientoComprobante",
                "IdTipoMovimiento",
                "IdMedioDePago",
                "NombreMedioDePago",
                "IdCuentaBancaria",
                "NombreCuentaBancaria",
                "IdTipoCuentaBancaria",
                "NombreTipoCuentaBancaria",
                "NumeroCuentaBancaria",
                "IdTarjetaBancaria",
                "IdTipoTarjetaBancaria",
                "NombreTipoTarjetaBancaria",
                "NumeroTarjetaBancaria",
                "IdModalidadDePago",
                "NombreModalidadDePago",
                "IdBanco",
                "CodigoBanco",
                "NombreBanco",
                "MontoLiquidacionFormaDePago",
                "IdEstado",
                "FechaIngresoRegistro",
                "IdMoneda",
                "FechaVctoTarjetaBancaria",
                "NumeroSerie",
                "NumeroDocumento",
                "FechaDocumentoPago",
                "IdTipoMovimientoSpot",
                "AfectarTesoreria"
              )
            Select "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteFormaDePago".NextVal,
                   "IdMovimientoComprobante",
                   IfNull("IdTipoMovimiento", ''),
                   "IdMedioDePago",
                   "NombreMedioDePago",
                   IfNull("IdCuentaBancaria", 0),
                   IfNull("NombreCuentaBancaria", ''),
                   IfNull("IdTipoCuentaBancaria", ''),
                   IfNull("NombreTipoCuentaBancaria", ''),
                   IfNull("NumeroCuentaBancaria", ''),
                   0,
                   IfNull("IdTipoTarjetaBancaria", ''),
                   IfNull("NombreTipoTarjetaBancaria", ''),
                   IfNull("NumeroTarjetaBancaria", ''),
                   IfNull("IdModalidadDePago", ''),
                   IfNull("NombreModalidadDePago", ''),
                   IfNull("IdBanco", 0),
                   IfNull("CodigoBanco", ''),
                   IfNull("NombreBanco", ''),
                   IfNull("MontoLiquidacionFormaDePago", 0),
                   IfNull("IdEstado", ''),
                   CURRENT_UTCTIMESTAMP,
                   IfNull("IdMoneda", ''),
                   IfNull("FechaVctoTarjetaBancaria", ''),
                   IfNull("NumeroSerie", ''),
                   IfNull("NumeroDocumento", 0),
                   IfNull("FechaDocumentoPago", '0001-01-01'),
                   IfNull("IdTipoMovimientoSpot", ''),
                   IfNull("AfectarTesoreria", 'S')
		    From :v_MvtoCmbteFDPagoAActualizar;    
          Else
            Update MvtoCmbteFDePago
            Set "IdModalidadDePago" = COALESCE(MvtoCmbteFDPagoAAct."IdModalidadDePago", MvtoCmbteFDePago."IdModalidadDePago", ''),
	            "NombreModalidadDePago" = COALESCE(MvtoCmbteFDPagoAAct."NombreModalidadDePago", MvtoCmbteFDePago."NombreModalidadDePago", ''),
			    "MontoLiquidacionFormaDePago" = COALESCE(MvtoCmbteFDPagoAAct."MontoLiquidacionFormaDePago", MvtoCmbteFDePago."MontoLiquidacionFormaDePago", 0),
			    "IdMoneda" = COALESCE(MvtoCmbteFDPagoAAct."IdMoneda", MvtoCmbteFDePago."IdMoneda", ''),
			    "IdBanco" = COALESCE(MvtoCmbteFDPagoAAct."IdBanco", MvtoCmbteFDePago."IdBanco", 0),
			    "CodigoBanco" = COALESCE(MvtoCmbteFDPagoAAct."CodigoBanco", MvtoCmbteFDePago."CodigoBanco", ''),
			    "NombreBanco" = COALESCE(MvtoCmbteFDPagoAAct."NombreBanco", MvtoCmbteFDePago."NombreBanco", ''),		    
			    "IdEstado" = COALESCE(MvtoCmbteFDPagoAAct."IdEstado", MvtoCmbteFDePago."IdEstado", ''),
			    "IdCuentaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."IdCuentaBancaria", MvtoCmbteFDePago."IdCuentaBancaria", 0),		    
			    "NombreCuentaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."NombreCuentaBancaria", MvtoCmbteFDePago."NombreCuentaBancaria", ''),
			    "NumeroCuentaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."NumeroCuentaBancaria", MvtoCmbteFDePago."NumeroCuentaBancaria", ''),		    
			    "FechaVctoTarjetaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."FechaVctoTarjetaBancaria", MvtoCmbteFDePago."FechaVctoTarjetaBancaria", ''), 
			    "IdTipoCuentaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."IdTipoCuentaBancaria", MvtoCmbteFDePago."IdTipoCuentaBancaria", ''),
			    "NombreTipoCuentaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."NombreTipoCuentaBancaria", MvtoCmbteFDePago."NombreTipoCuentaBancaria", ''),		    
			    "NumeroDocumento" = COALESCE(MvtoCmbteFDPagoAAct."NumeroDocumento", MvtoCmbteFDePago."NumeroDocumento", 0),
			    "NumeroSerie" = COALESCE(MvtoCmbteFDPagoAAct."NumeroSerie", MvtoCmbteFDePago."NumeroSerie", ''),
			    "FechaDocumentoPago" = COALESCE(MvtoCmbteFDPagoAAct."FechaDocumentoPago", MvtoCmbteFDePago."FechaDocumentoPago", ''),
			    "IdTipoTarjetaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."IdTipoTarjetaBancaria", MvtoCmbteFDePago."IdTipoTarjetaBancaria", ''),
			    "NombreTipoTarjetaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."NombreTipoTarjetaBancaria", MvtoCmbteFDePago."NombreTipoTarjetaBancaria", ''),
			    "NumeroTarjetaBancaria" = COALESCE(MvtoCmbteFDPagoAAct."NumeroTarjetaBancaria", MvtoCmbteFDePago."NumeroTarjetaBancaria", '')
            From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" As MvtoCmbteFDePago
              Inner Join :v_MvtoCmbteFDPagoAActualizar As MvtoCmbteFDPagoAAct
                 On MvtoCmbteFDPagoAAct."IdMovimientoComprobanteFormaDePago" = MvtoCmbteFDePago."IdMovimientoComprobanteFormaDePago"              
                And MvtoCmbteFDPagoAAct."IdMovimientoComprobante" = MvtoCmbteFDePago."IdMovimientoComprobante"              
                And MvtoCmbteFDPagoAAct."IdMedioDePago" = MvtoCmbteFDePago."IdMedioDePago";   
          End if;
        End if;
        
        
        
        
	   
	    --*********************
	    --* MENSAJE DE EXITO  *
	    --*********************
        /** Inserta registro de resultado de proceso de servicio completo **/
		v_out_resultadoProceso =
			Select 'IdMovimientoComprobante' As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante' As "Servicio",
				'MOD' As "Resultado"
			From :v_MvtoCmbteAActualizar Union All
			Select 'IdMovimientoComprobante' As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante' As "Servicio",
				'MOD' As "Resultado"
			From :v_MvtoCmbteCobrosAActualizar Union All
			Select 'IdMovimientoComprobante' As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante' As "Servicio",
				'MOD' As "Resultado"
			From :v_MvtoCmbteFDPagoAActualizar Union All
			Select 'IdMovimientoComprobante' As "Campo", "IdMovimientoComprobante" As "Id", 'BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante' as "Servicio",
				'RECH' as "Resultado"
			From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados";
			
		INSERT INTO "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ResultadoProceso"
		SELECT "Campo", "Id", "Servicio", "Resultado"
		FROM :v_out_resultadoProceso
		GROUP BY "Campo", "Id", "Servicio", "Resultado";
		                         
		
		
		/** Acumula Movimientos comprobantes modificados exitosos **/
		Select Count("IdMovimientoComprobante")
		Into v_CantMvtoCmbte
        From :v_MvtoCmbteAActualizar;
        
        Select Count("IdMovimientoComprobanteCobro")
		Into v_CantMvtoCmbteCobro
        From :v_MvtoCmbteCobrosAActualizar;
        
        Select Count("IdMovimientoComprobanteFormaDePago")
		Into v_CantMvtoCmbteFDPago
        From :v_MvtoCmbteFDPagoAActualizar;
		
				
		If (v_CantMvtoCmbte > 0 Or v_CantMvtoCmbteCobro > 0 Or v_CantMvtoCmbteFDPago > 0) Then 
		  v_TablaCodigosMensajes = Select "IdMovimientoComprobante" As "IdInterno", 'UI_MOVIMIENTO_COMPROBANTE_MODIFICADO' As "Codigo",
                                          'IdMovimientoComprobante' As "CodigoDato1", Cast("IdMovimientoComprobante" As NVARCHAR(520)) as "ValorDato1",
                                          '' As "CodigoDato2", '' As "ValorDato2" 
                                   From :v_MvtoCmbteAActualizar Union All
                                   Select "IdMovimientoComprobante" As "IdInterno", 'UI_MOVIMIENTO_COMPROBANTE_MODIFICADO' As "Codigo",
                                          'IdMovimientoComprobante' As "CodigoDato1", Cast("IdMovimientoComprobante" As NVARCHAR(520)) as "ValorDato1",
                                          '' As "CodigoDato2", '' As "ValorDato2" 
                                   From :v_MvtoCmbteCobrosAActualizar Union All
                                   Select "IdMovimientoComprobante" As "IdInterno", 'UI_MOVIMIENTO_COMPROBANTE_MODIFICADO' As "Codigo",
                                          'IdMovimientoComprobante' As "CodigoDato1", Cast("IdMovimientoComprobante" As NVARCHAR(520)) as "ValorDato1",
                                          '' As "CodigoDato2", '' As "ValorDato2" 
                                   From :v_MvtoCmbteFDPagoAActualizar;
			
			v_TablaCodigosMensajes =
				SELECT "IdInterno", "Codigo", "CodigoDato1", "ValorDato1", "CodigoDato2", "ValorDato2"
				FROM :v_TablaCodigosMensajes
				GROUP BY "IdInterno", "Codigo", "CodigoDato1", "ValorDato1", "CodigoDato2", "ValorDato2";
		  
  		  Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);				   
		   
  		  Insert Into "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_InformacionDetalle"
  		  Select "Codigo", "Descripcion" 
		  From :v_out_hash;                           
		
		  Select Count("Codigo") 
		  Into p_out_InformacionContador 
          From "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_InformacionDetalle";
           
		End If;
		
		IF :p_out_ErroresContador = 0 AND :v_CantMvtoCmbteCobro > 0 THEN
			--LOG DE AUDITORIA (Inicio)
			BEGIN
				DECLARE p_IdLog BIGINT;
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE CURSOR cur FOR SELECT "IdMovimientoComprobante","IdCobro","MontoCobro" FROM :v_MvtoCmbteCobrosAActualizar;			
				DECLARE v_LogDetalle TABLE (
					"IdCabeceraLog"    BIGINT,
					"SubRegistro"     NVARCHAR(40),
					"CampoRegistro"    NVARCHAR(500),
					"ValorAntiguo"    NVARCHAR(250),
					"ValorNuevo"     NVARCHAR(250)
				);
	
				SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;
	
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				v_log = SELECT  :p_IdLog AS "IdCabeceraLog"
								,'MODIFICACION' AS "IdTipoLog"
								,:p_IdModulo AS "IdModulo"
								,:p_IdUsuario AS "IdUsuario"
								,:p_IdEmpresa AS "IdEmpresa"
								,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
								,:p_IdUsuario AS "IdUsuarioAtendido"
								,0  AS "IdPersona"
								,COALESCE(cli."IdCliente", 0) AS "IdCliente"
								,'' AS "EstadoCliente"
								,COALESCE(cli."NombreLargoCliente", '') AS "NombreCliente"
								,'' AS "IdTipoDocumentoIdentidad"
								,COALESCE(cli."DocumentoIdentidad", '') AS "IdDocumentoIdentidad"
								,COALESCE(cli."IdPortafolio", 0)  AS "IdPortafolio"
								,'' AS "EstadoPortafolio"
								,'MODIFICAR_MOVIMIENTO_COMPROBANTE' AS "IdTipoRegistro"
								,COALESCE(TO_VARCHAR(cli."IdMovimientoComprobante"), '') AS "IdRegistro"
								,'' AS "EstadoRegistro"
								,'' AS "IdTipoOperacion"
								,0 AS "Folio"
								,0 AS "Cantidad"
								,0 AS "PrecioTasa"
								,0 AS "Monto"
								,'' AS "IdMonedaOrigen"
								,'' AS "IdMonedaLiquidacion"
								,'' AS "TipoCambio"
								,'' AS "Nemo"
								,0  AS "IdProceso" 
								,'' AS "EstadoProceso"
								,'ModificarMovimientoComprobante' AS "NombreProceso"
						FROM :v_DatosClienteLog as cli;
		
				FOR fila_cursor AS cur
				DO
			        DECLARE ValorAntiguoCobro NVARCHAR(250);
			        
			        SELECT datosAnt."MontoCobro" INTO ValorAntiguoCobro
			        FROM :v_DatosAntiguosLog AS datosAnt
			        WHERE datosAnt."IdMovimientoComprobante" = fila_cursor."IdMovimientoComprobante"
			        AND datosAnt."IdCobro" = fila_cursor."IdCobro";

					INSERT INTO :v_LogDetalle ("IdCabeceraLog","SubRegistro","CampoRegistro","ValorAntiguo","ValorNuevo")
					VALUES(
							:p_IdLog 
							,'IdComprobante: ' || COALESCE(TO_VARCHAR(fila_cursor."IdMovimientoComprobante"), '')
							,'TipoCobro : ' || COALESCE(fila_cursor."IdCobro", '')
							,COALESCE(TO_VARCHAR(ValorAntiguoCobro), '')
							,COALESCE(TO_VARCHAR(fila_cursor."MontoCobro"), '')
					);
				END FOR;
						
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
			END;
			--LOG DE AUDITORIA (Fin)
		END IF;
  END; --Logica
END;
GO 