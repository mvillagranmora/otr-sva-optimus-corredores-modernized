SET SCHEMA "BSCL";
GO 
   
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosCrearMovimientoComprobante";
GO 

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosCrearMovimientoComprobante"
  (
     IN p_IdModulo          NVARCHAR(3),
     
     -- Salidas para mensajes de errores 
	 OUT p_out_resultadoRegla   NVARCHAR(40) --OK=Cumple todos los requisitos, ADVE = , RECH=Alguna de lAS reglas detienen la operacion   				                      
  )
  
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	
BEGIN --Principal
    DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_erroroutmsg  			NVARCHAR(520);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
        				
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

	
		/** Acumula detalle de errores **/                       					
		Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_OUT_ErroresDetalle"
		 ( "NumeroMvtoCmbte", "Codigo", "Descripcion" ) 
		Select "IdInterno" As "NumeroMvtoCmbte", "Codigo"  as "Codigo", "Descripcion" As "Descripcion"  
		From :v_out_hash ;
								
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   DECLARE v_countTableFormaPago	BIGINT;
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100), "ValorDato1" NVARCHAR(520), "CodigoDato2" NVARCHAR(100), "ValorDato2" NVARCHAR(520),
	                                           "NumeroMvtoCmbte" BIGINT, "Entidad" NVARCHAR(250));
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2", 
										  0 AS "NumeroMvtoCmbte",'' as "Entidad" FROM dummy WHERE 1=2;
		

		
		/** Validaciones Basicas **/
		v_Obligatorios := 'IdConcepto, IdPortafolio, FechaLiquidacion, FechaVencimiento, IdTipoConvenio, FechaMovimientoComprobante, MontoMovimientoComprobante, IdEmisor, 
		                   IdTipoReajuste, EsComprobIndividual, IndicadorOrdenDirecta, EsVentaCorta, EsEnRueda, EsRuteo, EsCuentaPropia, EsPrecioPrima, EsAPlazo, EsFungible, 
		                   ActualizarCustodia, ActualizarCuentaInversion, ActualizarTesoreria';
		
		--Valida que existan movimientos 
        v_TablaRequisitos = Select 1 As "Prioridad", 
				       	            Case 
				       		           When Count("NumeroMvtoCmbte") < 1 THEN 0 
				       		        Else 1 
						            End As "Cumple", 
				       	            'NO_VIENEN_MOVIMIENTOS' AS "CodigoError", 
				       	            '' As "CodigoDato1",
				       	            '' As "ValorDato1",
				       	            '' As "CodigoDato2",
				       	            '' As "ValorDato2",
				       	            0 as "NumeroMvtoCmbte",
				       	            '' as "Entidad"
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
		                           "NumeroMvtoCmbte","Entidad" 
		                    From :v_TablaRequisitos;
		                    
		--Valida los campos obligatorios, no deben estar vacios
        v_TablaRequisitos = Select 2 as "Prioridad",
                                   Case 
                                     When Length("IdEmpresa") = 0 Or "IdEmpresa" = 0 Then 0
		       		                 When Length("IdConcepto") = 0 Or "IdConcepto" = '' Then 0
		       		                 When Length("IdPortafolio") < 1 Or "IdPortafolio" = 0 Then 0				       			     
				       			     When Length("IdTipoComprobante") = 0 Or "IdTipoComprobante" = '' Then 0
				       			     When Length("IdCondicionLiquidacion") = 0 Or "IdCondicionLiquidacion" = '' Then 0
				       			     When Length("FechaLiquidacion") = 0 Or "FechaLiquidacion" = '' Then 0
				       			     When "IdTipoProducto" = 'INIF' And (Length("IdTipoReajuste") = 0 Or "IdTipoReajuste" = '') Then 0				       			     
				       			     When Length("IdTipoConvenio") = 0 Or "IdTipoConvenio" = '' Then 0
				       			     When (("IdTipoProducto" = 'INSI' Or "EsVentaCorta" = 'S') And (Length("FechaVencimiento") = 0 Or "FechaVencimiento" = '' Or "FechaVencimiento" = '0001-01-01')) Then 0
				       			     When Length("MontoMovimientoComprobante") = 0 Or "MontoMovimientoComprobante" <= 0 Then 0
				       			     When Length("FechaMovimientoComprobante") = 0 Or "FechaMovimientoComprobante" = '' Then 0				       			     				       			      
				       			     When Length("EsComprobIndividual") = 0 Or ("EsComprobIndividual" <> 'S' And "EsComprobIndividual" <> 'N') Then 0
				       			     When Length("EsCuentaPropia") = 0  Or ("EsCuentaPropia" <> 'S' And "EsCuentaPropia" <> 'N') Then 0
				       			     When Length("EsVentaCorta") = 0 Or ("EsVentaCorta" <> 'S' And "EsVentaCorta" <> 'N') Then 0				       			     
				       			     When Length("EsPrecioPrima") = 0  Or ("EsPrecioPrima" <> 'S' And "EsPrecioPrima" <> 'N') Then 0
				       			     When Length("EsEnRueda") = 0 Or ("EsEnRueda" <> 'S' And "EsEnRueda" <> 'N') Then 0
				       			     When Length("EsRuteo") = 0 Or ("EsRuteo" <> 'S' And "EsRuteo" <> 'N') Then 0
				       			     When Length("EsAPlazo") = 0  Or ("EsAPlazo" <> 'S' And "EsAPlazo" <> 'N') Then 0
				       			     When Length("EsFungible") = 0  Or ("EsFungible" <> 'S' And "EsFungible" <> 'N') Then 0
				       			     When Length("ActualizarCustodia") = 0  Or ("ActualizarCustodia" <> 'S' And "ActualizarCustodia" <> 'N') Then 0
				       			     When Length("ActualizarCuentaInversion") = 0  Or ("ActualizarCuentaInversion" <> 'S' And "ActualizarCuentaInversion" <> 'N') Then 0
				       			     When Length("ActualizarTesoreria") = 0 Or ("ActualizarTesoreria" <> 'S' And "ActualizarTesoreria" <> 'N') Then 0
				       			     When Length("ActualizarCustodiaRelacionado") = 0 Or ("ActualizarCustodiaRelacionado" <> 'S' And "ActualizarCustodiaRelacionado" <> 'N') Then 0 
		       			           Else 1
		       			           End As "Cumple",		       			           
		       			           Case
		       			             When Length("IdEmpresa") = 0 Or "IdEmpresa" = 0 Then 'MVTO_CMBTE_EMPRESA_INVALIDA'
		       			             When Length("IdConcepto") = 0 Or "IdConcepto" = '' Then 'MVTO_CMBTE_CONCEPTO_FACTURACION_INVALIDO'
		       		                 When Length("IdPortafolio") < 1 Or "IdPortafolio" = 0 Then 'MVTO_CMBTE_PORTAFOLIO_INVALIDO' 				       			     
				       			     When Length("IdTipoComprobante") = 0 Or "IdTipoComprobante" = '' Then 'MVTO_CMBTE_TIPO_COMPROBANTE_INVALIDO'
				       			     When Length("IdCondicionLiquidacion") = 0 Or "IdCondicionLiquidacion" = '' Then 'MVTO_CMBTE_CONDICION_DE_LIQUIDACION_INVALIDO'
				       			     When Length("FechaLiquidacion") = 0 Or "FechaLiquidacion" = '' Then 'MVTO_CMBTE_FECHA_LIQUIDACION_INVALIDA'
				       			     When "IdTipoProducto" = 'INIF' And (Length("IdTipoReajuste") = 0 Or "IdTipoReajuste" = '') Then 'MVTO_CMBTE_TIPO_REAJUSTE_INVALIDO'
				       			     When Length("IdTipoConvenio") = 0 Or "IdTipoConvenio" = '' Then 'MVTO_CMBTE_TIPO_CONVENIO_INVALIDO'				       			     
				       			     When (("IdTipoProducto" = 'INSI' Or "EsVentaCorta" = 'S') And (Length("FechaVencimiento") = 0 Or "FechaVencimiento" = '' Or "FechaVencimiento" = '0001-01-01')) Then 'MVTO_CMBTE_FECHA_VENCIMIENTO_INVALIDA'
				       			     When Length("MontoMovimientoComprobante") = 0 Or "MontoMovimientoComprobante" <= 0 Then 'MVTO_CMBTE_MONTO_MOVIMIENTO_MENOR_IGUAL_A_CERO'
				       			     When Length("FechaMovimientoComprobante") = 0 Or "FechaMovimientoComprobante" = '' Then 'MVTO_CMBTE_FECHA_MOVIMIENTO_COMPROBANTE_INVALIDO'				       			     				       			     
				       			     When Length("EsComprobIndividual") = 0 Or ("EsComprobIndividual" <> 'S' And "EsComprobIndividual" <> 'N') Then 'MVTO_CMBTE_IND_COMPROBANTE_INDIVIDUAL_INVALIDO'
				       			     When Length("EsCuentaPropia") = 0  Or ("EsCuentaPropia" <> 'S' And "EsCuentaPropia" <> 'N') Then 'MVTO_CMBTE_IND_CUENTA_PROPIA_INVALIDO'
				       			     When Length("EsVentaCorta") = 0 Or ("EsVentaCorta" <> 'S' And "EsVentaCorta" <> 'N') Then 'MVTO_CMBTE_IND_VENTA_CORTA_INVALIDO'
				       			     When Length("EsPrecioPrima") = 0  Or ("EsPrecioPrima" <> 'S' And "EsPrecioPrima" <> 'N') Then 'MVTO_CMBTE_IND_PRECIO_PRIMA_INVALIDO'
				       			     When Length("EsEnRueda") = 0 Or ("EsEnRueda" <> 'S' And "EsEnRueda" <> 'N') Then 'MVTO_CMBTE_IND_EN_RUEDA_INVALIDO'
				       			     When Length("EsRuteo") = 0 Or ("EsRuteo" <> 'S' And "EsRuteo" <> 'N') Then 'MVTO_CMBTE_IND_RUTEO_INVALIDO' 
				       			     When Length("EsAPlazo") = 0  Or ("EsAPlazo" <> 'S' And "EsAPlazo" <> 'N') Then 'MVTO_CMBTE_IND_A_PLAZO_INVALIDO'
				       			     When Length("EsFungible") = 0  Or ("EsFungible" <> 'S' And "EsFungible" <> 'N') Then 'MVTO_CMBTE_IND_FUNGIBLE_INVALIDO'
				       			     When Length("ActualizarCustodia") = 0  Or ("ActualizarCustodia" <> 'S' And "ActualizarCustodia" <> 'N') Then 'MVTO_CMBTE_IND_ACTUALIZAR_CUSTODIA_INVALIDO'
				       			     When Length("ActualizarCuentaInversion") = 0  Or ("ActualizarCuentaInversion" <> 'S' And "ActualizarCuentaInversion" <> 'N') Then 'MVTO_CMBTE_IND_ACTUALIZAR_CUENTA_INVERSION_INVALIDO'
				       			     When Length("ActualizarTesoreria") = 0 Or ("ActualizarTesoreria" <> 'S' And "ActualizarTesoreria" <> 'N') Then 'MVTO_CMBTE_IND_ACTUALIZAR_TESORERIA_INVALIDO'
				       			     When Length("ActualizarCustodiaRelacionado") = 0 Or ("ActualizarCustodiaRelacionado" <> 'S' And "ActualizarCustodiaRelacionado" <> 'N') Then 'MVTO_CMBTE_IND_ACTUALIZAR_CUSTODIA_RELACIONADO_INVALIDO'
		       			           Else ''
		       			           End As "CodigoError",		       			           
		       			           Case
		       			             When Length("IdEmpresa") = 0 Or "IdEmpresa" = 0 Then 'NumeroMvtoCmbte' 	       			           
		       			             When Length("IdConcepto") = 0 Or "IdConcepto" = '' Then 'NumeroMvtoCmbte'
		       		                 When Length("IdPortafolio") < 1 Or "IdPortafolio" = 0 Then 'NumeroMvtoCmbte' 				       			     
				       			     When Length("IdTipoComprobante") = 0 Or "IdTipoComprobante" = '' Then 'NumeroMvtoCmbte' 
				       			     When Length("IdCondicionLiquidacion") = 0 Or "IdCondicionLiquidacion" = '' Then 'NumeroMvtoCmbte'
				       			     When Length("FechaLiquidacion") = 0 Or "FechaLiquidacion" = '' Then 'NumeroMvtoCmbte'
				       			     When "IdTipoProducto" = 'INIF' And (Length("IdTipoReajuste") = 0 Or "IdTipoReajuste" = '') Then 'NumeroMvtoCmbte'
				       			     When Length("IdTipoConvenio") = 0 Or "IdTipoConvenio" = '' Then 'IdMovimientoComprobante'
				       			     When (("IdTipoProducto" = 'INSI' Or "EsVentaCorta" = 'S') And (Length("FechaVencimiento") = 0 Or "FechaVencimiento" = '' Or "FechaVencimiento" = '0001-01-01')) Then 'NumeroMvtoCmbte'
				       			     When Length("MontoMovimientoComprobante") = 0 Or "MontoMovimientoComprobante" <= 0 Then 'NumeroMvtoCmbte'
				       			     When Length("FechaMovimientoComprobante") = 0 Or "FechaMovimientoComprobante" = '' Then 'NumeroMvtoCmbte'				       			     
				       			     When Length("EsComprobIndividual") = 0 Or ("EsComprobIndividual" <> 'S' And "EsComprobIndividual" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("EsCuentaPropia") = 0  Or ("EsCuentaPropia" <> 'S' And "EsCuentaPropia" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("EsVentaCorta") = 0 Or ("EsVentaCorta" <> 'S' And "EsVentaCorta" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("EsPrecioPrima") = 0  Or ("EsPrecioPrima" <> 'S' And "EsPrecioPrima" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("EsEnRueda") = 0 Or ("EsEnRueda" <> 'S' And "EsEnRueda" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("EsRuteo") = 0 Or ("EsRuteo" <> 'S' And "EsRuteo" <> 'N') Then 'NumeroMvtoCmbte' 
				       			     When Length("EsAPlazo") = 0  Or ("EsAPlazo" <> 'S' And "EsAPlazo" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("EsFungible") = 0  Or ("EsFungible" <> 'S' And "EsFungible" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("ActualizarCustodia") = 0  Or ("ActualizarCustodia" <> 'S' And "ActualizarCustodia" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("ActualizarCuentaInversion") = 0  Or ("ActualizarCuentaInversion" <> 'S' And "ActualizarCuentaInversion" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("ActualizarTesoreria") = 0 Or ("ActualizarTesoreria" <> 'S' And "ActualizarTesoreria" <> 'N') Then 'NumeroMvtoCmbte'
				       			     When Length("ActualizarCustodiaRelacionado") = 0 Or ("ActualizarCustodiaRelacionado" <> 'S' And "ActualizarCustodiaRelacionado" <> 'N') Then 'NumeroMvtoCmbte'
		       			           Else ''
		       			           End As "CodigoDato1",		       			           
		       			           Case 
		       			             When Length("IdEmpresa") = 0 Or "IdEmpresa" = 0 Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
		       			             When Length("IdConcepto") = 0 Or "IdConcepto" = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
		       		                 When Length("IdPortafolio") < 1 Or "IdPortafolio" = 0 Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("IdTipoComprobante") = 0 Or "IdTipoComprobante" = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("IdCondicionLiquidacion") = 0 Or "IdCondicionLiquidacion" = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("FechaLiquidacion") = 0 Or "FechaLiquidacion" = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When "IdTipoProducto" = 'INIF' And (Length("IdTipoReajuste") = 0 Or "IdTipoReajuste" = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("IdTipoConvenio") = 0 Or "IdTipoConvenio" = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When (("IdTipoProducto" = 'INSI' Or "EsVentaCorta" = 'S') And (Length("FechaVencimiento") = 0 Or "FechaVencimiento" = '' Or "FechaVencimiento" = '0001-01-01')) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("MontoMovimientoComprobante") = 0 Or "MontoMovimientoComprobante" <= 0 Then Cast("MontoMovimientoComprobante" As Nvarchar(12))				       			     
				       			     When Length("FechaMovimientoComprobante") = 0 Or "FechaMovimientoComprobante" = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsComprobIndividual") = 0 Or ("EsComprobIndividual" <> 'S' And "EsComprobIndividual" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsCuentaPropia") = 0  Or ("EsCuentaPropia" <> 'S' And "EsCuentaPropia" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsVentaCorta") = 0 Or ("EsVentaCorta" <> 'S' And "EsVentaCorta" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsPrecioPrima") = 0  Or ("EsPrecioPrima" <> 'S' And "EsPrecioPrima" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsEnRueda") = 0 Or ("EsEnRueda" <> 'S' And "EsEnRueda" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsRuteo") = 0 Or ("EsRuteo" <> 'S' And "EsRuteo" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsAPlazo") = 0  Or ("EsAPlazo" <> 'S' And "EsAPlazo" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("EsFungible") = 0  Or ("EsFungible" <> 'S' And "EsFungible" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("ActualizarCustodia") = 0  Or ("ActualizarCustodia" <> 'S' And "ActualizarCustodia" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("ActualizarCuentaInversion") = 0  Or ("ActualizarCuentaInversion" <> 'S' And "ActualizarCuentaInversion" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("ActualizarTesoreria") = 0 Or ("ActualizarTesoreria" <> 'S' And "ActualizarTesoreria" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
				       			     When Length("ActualizarCustodiaRelacionado") = 0 Or ("ActualizarCustodiaRelacionado" <> 'S' And "ActualizarCustodiaRelacionado" <> 'N') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
		       			           Else ''
		       			           End As "ValorDato1",	
		       			           '' As "CodigoDato2",
				       	           '' As "ValorDato2",		       			           	       			           
		       			           MvtoCmbte."NumeroMvtoCmbte",
		       			           'IngresoMovimientoComprobante' As "Entidad"
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
		                           "NumeroMvtoCmbte","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(Concepto."IdConcepto",'') = '' Then 0                                      
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 0
                                      When Coalesce(CondLiq."IdCondicionDeLiquidacion",'') = '' Then 0
                                      When (Length(MvtoCmbte."IdTipoReajuste") <> 0 And Coalesce(TipReaj."IdTipoReajuste",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdTipoConvenio") <> 0 And Coalesce(TipConv."IdTipoConvenio",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdEmisor") <> 0 And Coalesce(ConsEmi."IdEmisor",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiqAct."IdMoneda",'') = '') Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case
                                      When Coalesce(Concepto."IdConcepto",'') = '' Then 'MVTO_CMBTE_CONCEPTO_NO_EXISTE'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'MVTO_CMBTE_TIPO_COMPROBANTE_NO_EXISTE'
                                      When Coalesce(CondLiq."IdCondicionDeLiquidacion",'') = '' Then 'MVTO_CMBTE_CONDICION_DE_LIQUIDACION_NO_EXISTE'
                                      When (Length(MvtoCmbte."IdTipoReajuste") <> 0 And Coalesce(TipReaj."IdTipoReajuste",'') = '') Then 'MVTO_CMBTE_TIPO_REAJUSTE_NO_EXISTE'
                                      When (Length(MvtoCmbte."IdTipoConvenio") <> 0 And Coalesce(TipConv."IdTipoConvenio",'') = '') Then 'MVTO_CMBTE_TIPO_CONVENIO_NO_EXISTE' 
                                      When (Length(MvtoCmbte."IdEmisor") <> 0 And Coalesce(ConsEmi."IdEmisor",'') = '') Then 'MVTO_CMBTE_EMISOR_NO_EXISTE'
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_LIQUIDACION_NO_EXISTE' 
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiqAct."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_LIQUIDACION_NO_ACTIVA'
                                    Else ''
                                    End As "CodigoError",                                    
                                    Case
                                      When Coalesce(Concepto."IdConcepto",'') = '' Then 'IdConcepto'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdTipoComprobante'
                                      When Coalesce(CondLiq."IdCondicionDeLiquidacion",'') = '' Then 'IdCondicionDeLiquidacion'
                                      When (Length(MvtoCmbte."IdTipoReajuste") <> 0 And Coalesce(TipReaj."IdTipoReajuste",'') = '') Then 'IdTipoReajuste'
                                      When (Length(MvtoCmbte."IdTipoConvenio") <> 0 And Coalesce(TipConv."IdTipoConvenio",'') = '') Then 'IdTipoConvenio'
                                      When (Length(MvtoCmbte."IdEmisor") <> 0 And Coalesce(ConsEmi."IdEmisor",'') = '') Then 'IdEmisor'
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 'IdMonedaLiquidacion' 
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiqAct."IdMoneda",'') = '') Then 'IdMonedaLiquidacion'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(Concepto."IdConcepto",'') = '' Then MvtoCmbte."IdConcepto"
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then MvtoCmbte."IdTipoComprobante"
                                      When Coalesce(CondLiq."IdCondicionDeLiquidacion",'') = '' Then MvtoCmbte."IdCondicionLiquidacion"
                                      When (Length(MvtoCmbte."IdTipoReajuste") <> 0 And Coalesce(TipReaj."IdTipoReajuste",'') = '') Then MvtoCmbte."IdTipoReajuste"
                                      When (Length(MvtoCmbte."IdTipoConvenio") <> 0 And Coalesce(TipConv."IdTipoConvenio",'') = '') Then MvtoCmbte."IdTipoConvenio"
                                      When (Length(MvtoCmbte."IdEmisor") <> 0 And Coalesce(ConsEmi."IdEmisor",'') = '') Then MvtoCmbte."IdEmisor"
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then MvtoCmbte."IdMonedaLiquidacion"
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiqAct."IdMoneda",'') = '') Then MvtoCmbte."IdMonedaLiquidacion"
                                    Else ''
                                    End As "ValorDato1", 
                                    Case
                                      When Coalesce(Concepto."IdConcepto",'') = '' Then 'NumeroMvtoCmbte'                                      
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'NumeroMvtoCmbte'
                                      When Coalesce(CondLiq."IdCondicionDeLiquidacion",'') = '' Then 'NumeroMvtoCmbte'
                                      When (Length(MvtoCmbte."IdTipoReajuste") <> 0 And Coalesce(TipReaj."IdTipoReajuste",'') = '') Then 'NumeroMvtoCmbte'
                                      When (Length(MvtoCmbte."IdTipoConvenio") <> 0 And Coalesce(TipConv."IdTipoConvenio",'') = '') Then 'NumeroMvtoCmbte'
                                      When (Length(MvtoCmbte."IdEmisor") <> 0 And Coalesce(ConsEmi."IdEmisor",'') = '') Then 'NumeroMvtoCmbte'
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 'NumeroMvtoCmbte' 
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiqAct."IdMoneda",'') = '') Then 'NumeroMvtoCmbte'
                                    Else ''
                                    End As "CodigoDato2",
				       	            Case
                                      When Coalesce(Concepto."IdConcepto",'') = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When Coalesce(CondLiq."IdCondicionDeLiquidacion",'') = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdTipoReajuste") <> 0 And Coalesce(TipReaj."IdTipoReajuste",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdTipoConvenio") <> 0 And Coalesce(TipConv."IdTipoConvenio",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdEmisor") <> 0 And Coalesce(ConsEmi."IdEmisor",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") <> 0 And Coalesce(MonLiqAct."IdMoneda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato2",                                                                      
		       	                    "NumeroMvtoCmbte",
		       	                    'IngresoMovimientoComprobante' As "Entidad"       
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte
		       	               Left Join "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" As Concepto
		       	                  On Concepto."IdConcepto" = MvtoCmbte."IdConcepto"		       	                  
		       	               Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte
		       	                  On TipCmbte."IdTipoComprobante" = MvtoCmbte."IdTipoComprobante"   
		       	               Left Join "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondLiq
		       	                  On CondLiq."IdCondicionDeLiquidacion" = MvtoCmbte."IdCondicionLiquidacion"
		       	               Left Join "BSCL.Plataforma.BD::PLA.TipoReajuste" As TipReaj
		       	                  On TipReaj."IdTipoReajuste" = MvtoCmbte."IdTipoReajuste"
		       	               Left Join "BSCL.Facturacion.BD::FAC.TipoConvenio" As TipConv
		       	                  On TipConv."IdTipoConvenio" = MvtoCmbte."IdTipoConvenio"		       	               
		       	               Left Join "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" ConsEmi
		       	                  On ConsEmi."IdEmisor" = MvtoCmbte."IdEmisor"
		       	               Left Join "BSCL.Plataforma.BD::PLA.Moneda" MonLiq
		       	                  On MonLiq."IdMoneda" = MvtoCmbte."IdMonedaLiquidacion"
		       	               Left Join "BSCL.Plataforma.BD::PLA.Moneda" MonLiqAct
		       	                  On MonLiqAct."IdMoneda" = MvtoCmbte."IdMonedaLiquidacion"
		       	                 And MonLiqAct."IdEstado" = 'ACT'  Union All   
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
		                            "NumeroMvtoCmbte","Entidad" 
		                     From :v_TablaRequisitos;
		       	                  
         --Valida los campos no obligatorios, si vienen deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When (MvtoCmbte."IdInstrumento" > 0 And Coalesce(Ins."IdInstrumento",0) = 0) Then 0
                                      When (MvtoCmbte."IdInstrumentoRelacionado" > 0 And Coalesce(RelIns."IdInstrumentoRelacionado",0) = 0) Then 0 
                                      When (MvtoCmbte."IdInstrumentoSubyacente" > 0 And Coalesce(InsSub."IdInstrumento",0) = 0) Then 0
                                      When (MvtoCmbte."IdEmpleadoDigitador" > 0 And Coalesce(ConsEmp."IdEmpleado", 0) = 0) Then 0
                                      When (Length(MvtoCmbte."IdTipoProducto") > 0 And Coalesce(TipProd."IdTipoProducto",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(MonAct."IdMoneda",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And MvtoCmbte."Monto" <= 0) Then 0                                      
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") > 0 And MvtoCmbte."MontoLiquidacion" <= 0) Then 0
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSub."IdMoneda",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSubAct."IdMoneda",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdTipoCanalIngreso") > 0 And Coalesce(TipCan."IdTipoCanalIngreso",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdTipoOperacion") > 0 And Coalesce(TipOpe."IdTipoOperacion",'') = '') Then 0
                                      When (Length(MvtoCmbte."IdBoveda") > 0 And Coalesce(Bov."IdBoveda",'') = '') Then 0
                                      When (MvtoCmbte."IdCorredoraContraparte" > 0 And Coalesce(ConsContr."IdCorredora",0) = 0) Then 0                                      
                                      When (Length(MvtoCmbte."IdBolsa") > 0 And Coalesce(Bol."IdBolsa",'') = '') Then 0
                                      When (MvtoCmbte."IdOrden" > 0 And Coalesce(Ord."IdOrden",0) = 0) Then 0                                      
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When (MvtoCmbte."IdInstrumento" > 0 And Coalesce(Ins."IdInstrumento",0) = 0) Then 'MVTO_CMBTE_INSTRUMENTO_NO_EXISTE'
                                      When (MvtoCmbte."IdInstrumentoRelacionado" > 0 And Coalesce(RelIns."IdInstrumentoRelacionado",0) = 0) Then 'MVTO_CMBTE_INSTRUMENTO_RELACIONADO_NO_EXISTE'
                                      When (MvtoCmbte."IdInstrumentoSubyacente" > 0 And Coalesce(InsSub."IdInstrumento",0) = 0) Then 'MVTO_CMBTE_INSTRUMENTO_SUBYACENTE_NO_EXISTE'
                                      When (MvtoCmbte."IdEmpleadoDigitador" > 0 And Coalesce(ConsEmp."IdEmpleado", 0) = 0) Then 'MVTO_CMBTE_EMPLEADO_DIGITADOR_INVALIDO'
                                      When (Length(MvtoCmbte."IdTipoProducto") > 0 And Coalesce(TipProd."IdTipoProducto",'') = '') Then 'MVTO_CMBTE_TIPO_PRODUCTO_NO_EXISTE' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_NO_EXISTE' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(MonAct."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_NO_ACTIVA' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And MvtoCmbte."Monto" <= 0) Then 'MVTO_CMBTE_MONTO_MENOR_IGUAL_A_CERO'                                         
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") > 0 And MvtoCmbte."MontoLiquidacion" <= 0) Then 'MVTO_CMBTE_MONTO_LIQUIDACION_MENOR_IGUAL_A_CERO'
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSub."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_SUBYACENTE_NO_EXISTE' 
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSubAct."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_SUBYACENTE_NO_ACTIVA' 
                                      When (Length(MvtoCmbte."IdTipoCanalIngreso") > 0 And Coalesce(TipCan."IdTipoCanalIngreso",'') = '') Then 'MVTO_CMBTE_TIPO_CANAL_INGRESO_NO_EXISTE'
                                      When (Length(MvtoCmbte."IdTipoOperacion") > 0 And Coalesce(TipOpe."IdTipoOperacion",'') = '') Then 'MVTO_CMBTE_TIPO_OPERACION_NO_EXISTE'  
                                      When (Length(MvtoCmbte."IdBoveda") > 0 And Coalesce(Bov."IdBoveda",'') = '') Then 'MVTO_CMBTE_BOVEDA_NO_EXISTE'
                                      When (MvtoCmbte."IdCorredoraContraparte" > 0 And Coalesce(ConsContr."IdCorredora",0) = 0) Then 'MVTO_CMBTE_CORREDOR_NO_EXISTE'  
                                      When (Length(MvtoCmbte."IdBolsa") > 0 And Coalesce(Bol."IdBolsa",'') = '') Then 'MVTO_CMBTE_BOLSA_NO_EXISTE'
                                      When (MvtoCmbte."IdOrden" > 0 And Coalesce(Ord."IdOrden",0) = 0) Then 'MVTO_CMBTE_ORDEN_NO_EXISTE' 
                                    Else ''
                                    End As "CodigoError",                                    
                                    Case
                                      When (MvtoCmbte."IdInstrumento" > 0 And Coalesce(Ins."IdInstrumento",0) = 0) Then 'IdInstrumento'
                                      When (MvtoCmbte."IdInstrumentoRelacionado" > 0 And Coalesce(RelIns."IdInstrumentoRelacionado",0) = 0) Then 'IdInstrumentoRelacionado'
                                      When (MvtoCmbte."IdInstrumentoSubyacente" > 0 And Coalesce(InsSub."IdInstrumento",0) = 0) Then 'IdInstrumentoSubyacente'
                                      When (MvtoCmbte."IdEmpleadoDigitador" > 0 And Coalesce(ConsEmp."IdEmpleado", 0) = 0) Then 'IdEmpleadoDigitador'                                      
                                      When (Length(MvtoCmbte."IdTipoProducto") > 0 And Coalesce(TipProd."IdTipoProducto",'') = '') Then 'IdTipoProducto' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'IdMoneda' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(MonAct."IdMoneda",'') = '') Then 'IdMoneda' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And MvtoCmbte."Monto" <= 0) Then 'NumeroMvtoCmbte'                                       
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") > 0 And MvtoCmbte."MontoLiquidacion" <= 0) Then 'NumeroMvtoCmbte'
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSub."IdMoneda",'') = '') Then 'IdMonedaSubyacente' 
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSubAct."IdMoneda",'') = '') Then 'IdMonedaSubyacente' 
                                      When (Length(MvtoCmbte."IdTipoCanalIngreso") > 0 And Coalesce(TipCan."IdTipoCanalIngreso",'') = '') Then 'IdTipoCanalIngreso'
                                      When (Length(MvtoCmbte."IdTipoOperacion") > 0 And Coalesce(TipOpe."IdTipoOperacion",'') = '') Then 'IdTipoOperacion'  
                                      When (Length(MvtoCmbte."IdBoveda") > 0 And Coalesce(Bov."IdBoveda",'') = '') Then 'IdBoveda'
                                      When (MvtoCmbte."IdCorredoraContraparte" > 0 And Coalesce(ConsContr."IdCorredora",0) = 0) Then 'IdCorredoraContraparte'  
                                      When (Length(MvtoCmbte."IdBolsa") > 0 And Coalesce(Bol."IdBolsa",'') = '') Then 'IdBolsa'
                                      When (MvtoCmbte."IdOrden" > 0 And Coalesce(Ord."IdOrden",0) = 0) Then 'IdOrden' 
                                    Else ''
                                    End As "CodigoDato1",                                    
                                    Case
                                      When (MvtoCmbte."IdInstrumento" > 0 And Coalesce(Ins."IdInstrumento",0) = 0) Then Cast(MvtoCmbte."IdInstrumento" as Nvarchar(6))
                                      When (MvtoCmbte."IdInstrumentoRelacionado" > 0 And Coalesce(RelIns."IdInstrumentoRelacionado",0) = 0) Then Cast(MvtoCmbte."IdInstrumentoRelacionado" as Nvarchar(6))
                                      When (MvtoCmbte."IdInstrumentoSubyacente" > 0 And Coalesce(InsSub."IdInstrumento",0) = 0) Then Cast(MvtoCmbte."IdInstrumentoSubyacente" As Nvarchar(6))
                                      When (MvtoCmbte."IdEmpleadoDigitador" > 0 And Coalesce(ConsEmp."IdEmpleado", 0) = 0) Then Cast(MvtoCmbte."IdEmpleadoDigitador" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdTipoProducto") > 0 And Coalesce(TipProd."IdTipoProducto",'') = '') Then MvtoCmbte."IdTipoProducto"
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then MvtoCmbte."IdMoneda"
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(MonAct."IdMoneda",'') = '') Then MvtoCmbte."IdMoneda"
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And MvtoCmbte."Monto" <= 0) Then Cast(MvtoCmbte."NumeroMvtoCmbte" As Nvarchar(6))
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") > 0 And MvtoCmbte."MontoLiquidacion" <= 0) Then Cast(MvtoCmbte."NumeroMvtoCmbte" As Nvarchar(6))
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSub."IdMoneda",'') = '') Then MvtoCmbte."IdMonedaSubyacente"
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSubAct."IdMoneda",'') = '') Then MvtoCmbte."IdMonedaSubyacente"
                                      When (Length(MvtoCmbte."IdTipoCanalIngreso") > 0 And Coalesce(TipCan."IdTipoCanalIngreso",'') = '') Then MvtoCmbte."IdTipoCanalIngreso"
                                      When (Length(MvtoCmbte."IdTipoOperacion") > 0 And Coalesce(TipOpe."IdTipoOperacion",'') = '') Then MvtoCmbte."IdTipoOperacion"
                                      When (Length(MvtoCmbte."IdBoveda") > 0 And Coalesce(Bov."IdBoveda",'') = '') Then MvtoCmbte."IdBoveda"
                                      When (MvtoCmbte."IdCorredoraContraparte" > 0 And Coalesce(ConsContr."IdCorredora",0) = 0) Then Cast(MvtoCmbte."IdCorredoraContraparte" As Nvarchar(6))
                                      When (Length(MvtoCmbte."IdBolsa") > 0 And Coalesce(Bol."IdBolsa",'') = '') Then MvtoCmbte."IdBolsa"
                                      When (MvtoCmbte."IdOrden" > 0 And Coalesce(Ord."IdOrden",0) = 0) Then Cast(MvtoCmbte."IdOrden" As Nvarchar(6))
                                    Else ''
                                    End  As "ValorDato1",
                                    Case
                                      When (MvtoCmbte."IdInstrumento" > 0 And Coalesce(Ins."IdInstrumento",0) = 0) Then 'NumeroMvtoCmbte'
                                      When (MvtoCmbte."IdInstrumentoRelacionado" > 0 And Coalesce(RelIns."IdInstrumentoRelacionado",0) = 0) Then 'NumeroMvtoCmbte'
                                      When (MvtoCmbte."IdInstrumentoSubyacente" > 0 And Coalesce(InsSub."IdInstrumento",0) = 0) Then 'NumeroMvtoCmbte'
                                      When (MvtoCmbte."IdEmpleadoDigitador" > 0 And Coalesce(ConsEmp."IdEmpleado", 0) = 0) Then 'NumeroMvtoCmbte'
                                      When (Length(MvtoCmbte."IdTipoProducto") > 0 And Coalesce(TipProd."IdTipoProducto",'') = '') Then 'NumeroMvtoCmbte' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'NumeroMvtoCmbte' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(MonAct."IdMoneda",'') = '') Then 'NumeroMvtoCmbte' 
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And MvtoCmbte."Monto" <= 0) Then ''                                       
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") > 0 And MvtoCmbte."MontoLiquidacion" <= 0) Then ''
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSub."IdMoneda",'') = '') Then 'NumeroMvtoCmbte' 
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSubAct."IdMoneda",'') = '') Then 'NumeroMvtoCmbte' 
                                      When (Length(MvtoCmbte."IdTipoCanalIngreso") > 0 And Coalesce(TipCan."IdTipoCanalIngreso",'') = '') Then 'NumeroMvtoCmbte'
                                      When (Length(MvtoCmbte."IdTipoOperacion") > 0 And Coalesce(TipOpe."IdTipoOperacion",'') = '') Then 'NumeroMvtoCmbte'  
                                      When (Length(MvtoCmbte."IdBoveda") > 0 And Coalesce(Bov."IdBoveda",'') = '') Then 'NumeroMvtoCmbte'
                                      When (MvtoCmbte."IdCorredoraContraparte" > 0 And Coalesce(ConsContr."IdCorredora",0) = 0) Then 'NumeroMvtoCmbte'  
                                      When (Length(MvtoCmbte."IdBolsa") > 0 And Coalesce(Bol."IdBolsa",'') = '') Then 'NumeroMvtoCmbte'
                                      When (MvtoCmbte."IdOrden" > 0 And Coalesce(Ord."IdOrden",0) = 0) Then 'NumeroMvtoCmbte' 
                                    Else ''
                                    End As "CodigoDato2",
                                    Case
                                      When (MvtoCmbte."IdInstrumento" > 0 And Coalesce(Ins."IdInstrumento",0) = 0) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (MvtoCmbte."IdInstrumentoRelacionado" > 0 And Coalesce(RelIns."IdInstrumentoRelacionado",0) = 0) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (MvtoCmbte."IdInstrumentoSubyacente" > 0 And Coalesce(InsSub."IdInstrumento",0) = 0) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (MvtoCmbte."IdEmpleadoDigitador" > 0 And Coalesce(ConsEmp."IdEmpleado", 0) = 0) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))                                      
                                      When (Length(MvtoCmbte."IdTipoProducto") > 0 And Coalesce(TipProd."IdTipoProducto",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And Coalesce(MonAct."IdMoneda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdMoneda") > 0 And MvtoCmbte."Monto" <= 0) Then ''                                      
                                      When (Length(MvtoCmbte."IdMonedaLiquidacion") > 0 And MvtoCmbte."MontoLiquidacion" <= 0) Then ''
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSub."IdMoneda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdMonedaSubyacente") > 0 And Coalesce(MonSubAct."IdMoneda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdTipoCanalIngreso") > 0 And Coalesce(TipCan."IdTipoCanalIngreso",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdTipoOperacion") > 0 And Coalesce(TipOpe."IdTipoOperacion",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdBoveda") > 0 And Coalesce(Bov."IdBoveda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (MvtoCmbte."IdCorredoraContraparte" > 0 And Coalesce(ConsContr."IdCorredora",0) = 0) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (Length(MvtoCmbte."IdBolsa") > 0 And Coalesce(Bol."IdBolsa",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                      When (MvtoCmbte."IdOrden" > 0 And Coalesce(Ord."IdOrden",0) = 0) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato2",                                    
		       	                    "NumeroMvtoCmbte",
		       	                    'IngresoMovimientoComprobante' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte
		       	               Left Join "BSCL.Plataforma.BD::INS.Instrumento" As Ins
		       	                  On Ins."IdInstrumento" = MvtoCmbte."IdInstrumento"
		       	               Left Join "BSCL.Plataforma.BD::INS.RelacionInstrumento" As RelIns
		       	                  On RelIns."IdInstrumentoRelacionado" = MvtoCmbte."IdInstrumentoRelacionado" 
		       	               Left Join "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" As ConsEmp
     	                          On ConsEmp."IdEmpleado" = MvtoCmbte."IdEmpleadoDigitador"
		       	               Left Join "BSCL.Plataforma.BD::INS.Instrumento" As InsSub
		       	                  On InsSub."IdInstrumento" = MvtoCmbte."IdInstrumentoSubyacente"
		       	               Left Join "BSCL.Plataforma.BD::INS.TipoProducto" TipProd   
		       	                  On TipProd."IdTipoProducto" = MvtoCmbte."IdTipoProducto" 		       	               
		       	               Left Join "BSCL.Plataforma.BD::PLA.Moneda" Mon
		       	                  On Mon."IdMoneda" = MvtoCmbte."IdMoneda"
		       	               Left Join "BSCL.Plataforma.BD::PLA.Moneda" MonAct
		       	                  On MonAct."IdMoneda" = MvtoCmbte."IdMoneda"
		       	                 And MonAct."IdEstado" = 'ACT'
		       	               Left Join "BSCL.Plataforma.BD::PLA.Moneda" MonSub
		       	                  On MonSub."IdMoneda" = MvtoCmbte."IdMonedaSubyacente"
		       	               Left Join "BSCL.Plataforma.BD::PLA.Moneda" MonSubAct
		       	                  On MonSubAct."IdMoneda" = MvtoCmbte."IdMonedaSubyacente"
		       	                 And MonSubAct."IdEstado" = 'ACT' 		       	               
		       	               Left Join "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TipCan
		       	                  On TipCan."IdTipoCanalIngreso" = MvtoCmbte."IdTipoCanalIngreso"		       	               
		       	               Left Join "BSCL.Plataforma.BD::INS.TipoOperacion" TipOpe
		       	                  On TipOpe."IdTipoOperacion" = MvtoCmbte."IdTipoOperacion"
		       	               Left Join "BSCL.Plataforma.BD::PLA.Boveda" Bov
		       	                  On Bov."IdBoveda" = MvtoCmbte."IdBoveda"
		       	               Left Join "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" ConsContr
		       	                  On ConsContr."IdCorredora" = MvtoCmbte."IdCorredoraContraparte"
		       	               Left Join "BSCL.Plataforma.BD::INS.Bolsa" Bol
		       	                  On Bol."IdBolsa" = MvtoCmbte."IdBolsa"		       	               		       	               
		       	               Left Join "BSCL.Orden.BD::ORD.Orden" Ord
		       	                  On Ord."IdOrden" = MvtoCmbte."IdOrden" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
		                            "NumeroMvtoCmbte","Entidad" 
		                     From :v_TablaRequisitos;
		                     
		 --Valida corte minimo de instrumento
         v_TablaRequisitos = Select 2 as "Prioridad",
				  			        Case
				  			          When Mod(MvtoCmbte."Cantidad", ConsInsTipReaj."CorteMinimo") <> 0 Then 0
							        Else 1
						            End As "Cumple",
						            Case
						              When Mod(MvtoCmbte."Cantidad", ConsInsTipReaj."CorteMinimo") <> 0 Then 'MVTO_CMBTE_CORTE_MINIMO'
							        Else ''
						            End As "CodigoError",
						            Case
						              When Mod(MvtoCmbte."Cantidad", ConsInsTipReaj."CorteMinimo") <> 0 Then 'Cantidad'
							        Else ''
						            End As "CodigoDato1",
				       	            Case
						              When Mod(MvtoCmbte."Cantidad", ConsInsTipReaj."CorteMinimo") <> 0  Then Cast("Cantidad" As Nvarchar(12))
							        Else ''
						            End As "ValorDato1",
				       	            Case
						              When Mod(MvtoCmbte."Cantidad", ConsInsTipReaj."CorteMinimo") <> 0 Then 'NumeroMvtoCmbte'
							        Else ''
						            End As "CodigoDato2",
				       	            Case
						              When Mod(MvtoCmbte."Cantidad", ConsInsTipReaj."CorteMinimo") <> 0 Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
							        Else ''
						            End As "ValorDato2",
				       	            MvtoCmbte."NumeroMvtoCmbte",
				       	            'IngresoMovimientoComprobante' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte
		                       Inner Join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" As ConsInsTipReaj
		                          On ConsInsTipReaj."IdInstrumento" = MvtoCmbte."IdInstrumento" 
		                         And ConsInsTipReaj."IdTipoReajuste" = MvtoCmbte."IdTipoReajuste"
		                       Inner Join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" As ConsIns 
		                          On ConsIns."IdInstrumento" = MvtoCmbte."IdInstrumento"
		                         And ConsIns."IdClasificacionTipoInstrumento" in ('RF') Union All		                          
		                     Select "Prioridad", "Cumple", "CodigoError", "CodigoDato1", "ValorDato1", "CodigoDato2","ValorDato2",
		                            "NumeroMvtoCmbte", "Entidad" 
		                     From :v_TablaRequisitos;
         
                     
		                     
		 --Valida los campos no obligatorios, secuencia de agrupacion no se encuentre usada anteriormente
		 v_TablaRequisitos = Select 2 as "Prioridad",
				  			        Case
				  			          When Count(MvtoCmbteCom."SecuenciaAgrupacion") > 0 Then 0
							        Else 1
						            End As "Cumple",
						            Case
						              When Count(MvtoCmbteCom."SecuenciaAgrupacion") > 0 Then 'MVTO_CMBTE_SECUENCIA_UTILIZADA'
							        Else ''
						            End As "CodigoError",
						            Case
						              When Count(MvtoCmbteCom."SecuenciaAgrupacion") > 0 Then 'IdSecuencia'
							        Else ''
						            End As "CodigoDato1",
				       	            Case
						              When Count(MvtoCmbteCom."SecuenciaAgrupacion") > 0 Then ''
							        Else ''
						            End As "ValorDato1",
				       	            Case
						              When Count(MvtoCmbteCom."SecuenciaAgrupacion") > 0 Then ''
							        Else ''
						            End As "CodigoDato2",
				       	            Case
						              When Count(MvtoCmbteCom."SecuenciaAgrupacion") > 0 Then ''
							        Else ''
						            End As "ValorDato2",
				       	            MvtoCmbte."NumeroMvtoCmbte",
				       	            'IngresoMovimientoComprobante' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte
		                       Left Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbteCom 
		                          On MvtoCmbteCom."SecuenciaAgrupacion" = MvtoCmbte."SecuenciaAgrupacion"
		                     Where MvtoCmbte."IdCliente" = MvtoCmbteCom."IdCliente"
		                       And MvtoCmbte."IdPortafolio" = MvtoCmbteCom."IdPortafolio"
  		                       And MvtoCmbteCom."IdEstadoFacturacion" = 'FAC'
  		                       And (MvtoCmbteCom."SecuenciaAgrupacion" <> '0' And MvtoCmbteCom."SecuenciaAgrupacion" <> '')
		                     Group By MvtoCmbte."NumeroMvtoCmbte" Union All
		                     Select "Prioridad", "Cumple", "CodigoError", "CodigoDato1", "ValorDato1", "CodigoDato2","ValorDato2",
		                            "NumeroMvtoCmbte", "Entidad" 
		                     From :v_TablaRequisitos;
		                    
		                     
		 --Valida moviento comprobante forma de pago
         v_TablaRequisitos = Select 2 as "Prioridad",
				                    Case
						              When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And Coalesce(MedPago."IdMedioDePago",'') = '') Then 0
							          When (Length(MvtoCmbteFPago."IdModalidadDePago") > 0 And Coalesce(ModPago."IdModalidadDePago",'') = '') Then 0
							          When (Length(MvtoCmbteFPago."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 0				 	  
						 	          When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And MvtoCmbteFPago."MontoLiquidacionFormaDePago" <= 0) Then 0
						 	          --When MvtoCmbteFPago."IdCuentaBancaria" > 0 And Coalesce(ConsPerCtaBan."IdCuentaBancaria",0) = 0 Then 0
						 	          When Length(MvtoCmbteFPago."IdTipoCuentaBancaria") > 0 And Coalesce(TipCuenBan."IdTipoCuentaBancaria",'') = '' Then 0 
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And Coalesce(ConsPerTarBan."IdTarjetaBancaria",0) = 0 Then 0
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And length(MvtoCmbteFPago."FechaVctoTarjetaBancaria") = 0 Then 0
						 	          When Length(MvtoCmbteFPago."IdTipoTarjetaBancaria") > 0 And Coalesce(TipTarBan."IdTipoTarjetaBancaria", '') = '' Then 0
						 	          When MvtoCmbteFPago."NumeroDocumento" > 0 And length(MvtoCmbteFPago."FechaDocumentoPago") = 0 Then 0
						 	        Else 1
 				                    End As "Cumple",
				                    Case 
				                      When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And Coalesce(MedPago."IdMedioDePago",'') = '') Then 'MVTO_CMBTE_MEDIO_DE_PAGO_NO_EXISTE'
							          When (Length(MvtoCmbteFPago."IdModalidadDePago") > 0 And Coalesce(ModPago."IdModalidadDePago",'') = '') Then 'MVTO_CMBTE_MODALIDAD_DE_PAGO_NO_EXISTE'
							          When (Length(MvtoCmbteFPago."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_FORMA_DE_PAGO_NO_EXISTE' 
						 	          When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And MvtoCmbteFPago."MontoLiquidacionFormaDePago" <= 0) Then 'MVTO_CMBTE_MONTO_FORMA_DE_PAGO_MENOR_IGUAL_A_CERO'
						 	          --When MvtoCmbteFPago."IdCuentaBancaria" > 0 And Coalesce(ConsPerCtaBan."IdCuentaBancaria",0) = 0 Then 'MVTO_CMBTE_CUENTA_BANCARIA_SOCIO_NO_EXISTE'
						 	          When Length(MvtoCmbteFPago."IdTipoCuentaBancaria") > 0 And Coalesce(TipCuenBan."IdTipoCuentaBancaria",'') = '' Then 'MVTO_CMBTE_TIPO_CUENTA_BANCARIA_NO_EXISTE'
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And Coalesce(ConsPerTarBan."IdTarjetaBancaria",0) = 0 Then 'MVTO_CMBTE_TARJETA_BANCARIA_NO_EXISTE'
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And length(MvtoCmbteFPago."FechaVctoTarjetaBancaria") = 0 Then 'MVTO_CMBTE_FECHA_VENCIMIENTO_TARJETA_INVALIDA'
						 	          When Length(MvtoCmbteFPago."IdTipoTarjetaBancaria") > 0 And Coalesce(TipTarBan."IdTipoTarjetaBancaria",'') = '' Then 'MVTO_CMBTE_TIPO_TARJETA_BANCARIA_NO_EXISTE'
						 	          When MvtoCmbteFPago."NumeroDocumento" > 0 And length(MvtoCmbteFPago."FechaDocumentoPago") = 0 Then 'MVTO_CMBTE_FECHA_VENCIMIENTO_DOCUMENTO_INVALIDA'
						 	        Else ''
				                    End As "CodigoError",				                    
				                    Case 
				                      When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And Coalesce(MedPago."IdMedioDePago",'') = '') Then 'IdMedioDePago'
							          When (Length(MvtoCmbteFPago."IdModalidadDePago") > 0 And Coalesce(ModPago."IdModalidadDePago",'') = '') Then 'IdModalidadDePago'
							          When (Length(MvtoCmbteFPago."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'IdMedioDePago' 
						 	          When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And MvtoCmbteFPago."MontoLiquidacionFormaDePago" <= 0) Then 'IdMedioDePago'
						 	          --When MvtoCmbteFPago."IdCuentaBancaria" > 0 And Coalesce(ConsPerCtaBan."IdCuentaBancaria",0) = 0 Then 'IdMedioDePago'
						 	          When Length(MvtoCmbteFPago."IdTipoCuentaBancaria") > 0 And Coalesce(TipCuenBan."IdTipoCuentaBancaria",'') = '' Then 'IdMedioDePago'
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And Coalesce(ConsPerTarBan."IdTarjetaBancaria",0) = 0 Then 'IdMedioDePago'
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And length(MvtoCmbteFPago."FechaVctoTarjetaBancaria") = 0 Then 'IdMedioDePago'
						 	          When Length(MvtoCmbteFPago."IdTipoTarjetaBancaria") > 0 And Coalesce(TipTarBan."IdTipoTarjetaBancaria",'') = '' Then 'IdMedioDePago'
						 	          When MvtoCmbteFPago."NumeroDocumento" > 0 And length(MvtoCmbteFPago."FechaDocumentoPago") = 0 Then 'IdMedioDePago'
						 	        Else ''
				                    End As "CodigoDato1",				                    
		       	                    Case 
				                      When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And Coalesce(MedPago."IdMedioDePago",'') = '') Then MvtoCmbteFPago."IdMedioDePago"
							          When (Length(MvtoCmbteFPago."IdModalidadDePago") > 0 And Coalesce(ModPago."IdModalidadDePago",'') = '') Then MvtoCmbteFPago."IdModalidadDePago"
							          When (Length(MvtoCmbteFPago."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then MvtoCmbteFPago."IdMedioDePago"
						 	          When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And MvtoCmbteFPago."MontoLiquidacionFormaDePago" <= 0) Then MvtoCmbteFPago."IdMedioDePago"
						 	          --When MvtoCmbteFPago."IdCuentaBancaria" > 0 And Coalesce(ConsPerCtaBan."IdCuentaBancaria",0) = 0 Then MvtoCmbteFPago."IdMedioDePago"
						 	          When Length(MvtoCmbteFPago."IdTipoCuentaBancaria") > 0 And Coalesce(TipCuenBan."IdTipoCuentaBancaria",'') = '' Then MvtoCmbteFPago."IdMedioDePago"
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And Coalesce(ConsPerTarBan."IdTarjetaBancaria",0) = 0 Then MvtoCmbteFPago."IdMedioDePago"
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And length(MvtoCmbteFPago."FechaVctoTarjetaBancaria") = 0 Then MvtoCmbteFPago."IdMedioDePago"
						 	          When Length(MvtoCmbteFPago."IdTipoTarjetaBancaria") > 0 And Coalesce(TipTarBan."IdTipoTarjetaBancaria",'') = '' Then MvtoCmbteFPago."IdMedioDePago"
						 	          When MvtoCmbteFPago."NumeroDocumento" > 0 And length(MvtoCmbteFPago."FechaDocumentoPago") = 0 Then MvtoCmbteFPago."IdMedioDePago"
						 	        Else ''
				                    End As "ValorDato1",
				                    Case 
				                      When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And Coalesce(MedPago."IdMedioDePago",'') = '') Then 'NumeroMvtoCmbte'
							          When (Length(MvtoCmbteFPago."IdModalidadDePago") > 0 And Coalesce(ModPago."IdModalidadDePago",'') = '') Then 'NumeroMvtoCmbte'
							          When (Length(MvtoCmbteFPago."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'NumeroMvtoCmbte' 
						 	          When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And MvtoCmbteFPago."MontoLiquidacionFormaDePago" <= 0) Then 'NumeroMvtoCmbte'
						 	          --When MvtoCmbteFPago."IdCuentaBancaria" > 0 And Coalesce(ConsPerCtaBan."IdCuentaBancaria",0) = 0 Then 'NumeroMvtoCmbte'
						 	          When Length(MvtoCmbteFPago."IdTipoCuentaBancaria") > 0 And Coalesce(TipCuenBan."IdTipoCuentaBancaria",'') = '' Then 'NumeroMvtoCmbte'
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And Coalesce(ConsPerTarBan."IdTarjetaBancaria",0) = 0 Then 'NumeroMvtoCmbte'
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And length(MvtoCmbteFPago."FechaVctoTarjetaBancaria") = 0 Then 'NumeroMvtoCmbte'
						 	          When Length(MvtoCmbteFPago."IdTipoTarjetaBancaria") > 0 And Coalesce(TipTarBan."IdTipoTarjetaBancaria",'') = '' Then 'NumeroMvtoCmbte'
						 	          When MvtoCmbteFPago."NumeroDocumento" > 0 And length(MvtoCmbteFPago."FechaDocumentoPago") = 0 Then 'NumeroMvtoCmbte'
						 	        Else ''
				                    End As "CodigoDato2",
				                    Case 
				                      When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And Coalesce(MedPago."IdMedioDePago",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
							          When (Length(MvtoCmbteFPago."IdModalidadDePago") > 0 And Coalesce(ModPago."IdModalidadDePago",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
							          When (Length(MvtoCmbteFPago."IdMoneda") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	          When (Length(MvtoCmbteFPago."IdMedioDePago") > 0 And MvtoCmbteFPago."MontoLiquidacionFormaDePago" <= 0) Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	          --When MvtoCmbteFPago."IdCuentaBancaria" > 0 And Coalesce(ConsPerCtaBan."IdCuentaBancaria",0) = 0 Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	          When Length(MvtoCmbteFPago."IdTipoCuentaBancaria") > 0 And Coalesce(TipCuenBan."IdTipoCuentaBancaria",'') = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And Coalesce(ConsPerTarBan."IdTarjetaBancaria",0) = 0 Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	          When MvtoCmbteFPago."IdTarjetaBancaria" > 0 And length(MvtoCmbteFPago."FechaVctoTarjetaBancaria") = 0 Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	          When Length(MvtoCmbteFPago."IdTipoTarjetaBancaria") > 0 And Coalesce(TipTarBan."IdTipoTarjetaBancaria",'') = '' Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	          When MvtoCmbteFPago."NumeroDocumento" > 0 And length(MvtoCmbteFPago."FechaDocumentoPago") = 0 Then Cast("NumeroMvtoCmbte" As Nvarchar(12))
						 	        Else ''
				                    End As "ValorDato2",
		       	                    "NumeroMvtoCmbte",
		       	                     'IngresoMovimientoComprobanteFormaDePago' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteFormaDePago" As MvtoCmbteFPago
  		                       Left Join "BSCL.Plataforma.BD::PAG.MedioDePago" As MedPago 
  		                         On MedPago."IdMedioDePago" = MvtoCmbteFPago."IdMedioDePago"
  		                       Left Join "BSCL.Plataforma.BD::PAG.ModalidadDePago" As ModPago 
  		                         On ModPago."IdModalidadDePago" = MvtoCmbteFPago."IdModalidadDePago"
  		                       Left Join "BSCL.Plataforma.BD::PLA.Moneda" As Mon 
  		                         On Mon."IdMoneda" = MvtoCmbteFPago."IdMoneda"
  		                       Left Join "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" As TipCuenBan
  		                         On TipCuenBan."IdTipoCuentaBancaria" = MvtoCmbteFPago."IdTipoCuentaBancaria"
  		                       /*Left Join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" As ConsPerCtaBan
  		                         On ConsPerCtaBan."IdCuentaBancaria" = MvtoCmbteFPago."IdCuentaBancaria"*/
  		                       Left Join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" As ConsPerTarBan
                                 On ConsPerTarBan."IdTarjetaBancaria" = MvtoCmbteFPago."IdTarjetaBancaria"
                               Left Join "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" As TipTarBan
                                 On TipTarBan."IdTipoTarjetaBancaria" = MvtoCmbteFPago."IdTipoTarjetaBancaria" Union All
                             Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
                                    "NumeroMvtoCmbte","Entidad" 
                             From :v_TablaRequisitos;

         --Valida movimiento comprobante forma de pago cuadre el monto con el movimiento comprobante
		 /*v_TablaRequisitos = Select 2 as "Prioridad",
				                   Case 
				                     When (Max(MvtoCmbte."Monto") <> Sum(MvtoCmbteFPago."MontoLiquidacionFormaDePago")) Then 0
 					               Else 1
 					               End As "Cumple",
  					               Case
  					                 When (Max(MvtoCmbte."Monto") <> Sum(MvtoCmbteFPago."MontoLiquidacionFormaDePago")) Then 'MVTO_CMBTE_MONTO_MOVIMIENTO_NO_CUADRA'
  					               Else ''
  					               End As "CodigoError",
  					               Case
  					                 When (Max(MvtoCmbte."Monto") <> Sum(MvtoCmbteFPago."MontoLiquidacionFormaDePago")) Then 'NumeroMvtoCmbte'
  					               Else ''
  					               End As "CodigoDato1",  					               
  					               Case
  					                 When (Max(MvtoCmbte."Monto") <> Sum(MvtoCmbteFPago."MontoLiquidacionFormaDePago")) Then Cast(MvtoCmbte."NumeroMvtoCmbte" As Nvarchar(12)) 
  					               Else ''
  					               End As "ValorDato1",
  					               Case
  					                 When (Max(MvtoCmbte."Monto") <> Sum(MvtoCmbteFPago."MontoLiquidacionFormaDePago")) Then ''
  					               Else ''
  					               End As "CodigoDato2",
				       	           Case
  					                 When (Max(MvtoCmbte."Monto") <> Sum(MvtoCmbteFPago."MontoLiquidacionFormaDePago")) Then ''
  					               Else ''
  					               End As "ValorDato2",
                                   MvtoCmbte."NumeroMvtoCmbte",
                                   'IngresoMovimientoComprobanteFormaDePago' as "Entidad"
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteFormaDePago" As MvtoCmbteFPago
		                      --Inner Join :p_MvtoCmbteAGenerar As MvtoCmbte
                              Inner Join "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte 
		                         On MvtoCmbte."NumeroMvtoCmbte" = MvtoCmbteFPago."NumeroMvtoCmbte"
		                        And MvtoCmbte."IdMonedaLiquidacion" = MvtoCmbteFPago."IdMoneda"
		                    Group By 
		                    		MvtoCmbte."NumeroMvtoCmbte"

		                    Union All
		                    
		                    Select "Prioridad", "Cumple", "CodigoError", "CodigoDato1", "ValorDato1", "CodigoDato2","ValorDato2",
		                           "NumeroMvtoCmbte", "Entidad" 
		                    From :v_TablaRequisitos;*/
                             
                             
         --Valida movimiento comprobante cobros
         v_TablaRequisitos = Select 2 as "Prioridad",
				                    Case
						              When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(Cobro."IdCobro",'') = '' And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then 0
						              When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then 0
							          When (Length(MvtoCmbteCob."IdMonedaCobro") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 0
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoCobro" < 0) Then 0
						 	          When (Length(MvtoCmbteCob."IdMonedaLiquidacionCobro") > 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 0
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoLiquidacionCobro" < 0) Then 0 
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoMovimientoComprobanteCobro" < 0) Then 0
						 	        Else 1
 				                    End As "Cumple",
				                    Case 
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 AND Coalesce(Cobro."IdCobro",'') = '' And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then 'MVTO_CMBTE_COBRO_NO_EXISTE'
							          When (Length(MvtoCmbteCob."IdMonedaCobro") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_COBRO_NO_EXISTE' 
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoCobro" < 0) Then 'MVTO_CMBTE_MONTO_COBRO_MENOR_A_CERO'
						 	          When (Length(MvtoCmbteCob."IdMonedaLiquidacionCobro") > 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 'MVTO_CMBTE_MONEDA_LIQUIDACION_COBRO_NO_EXISTE' 
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoLiquidacionCobro" < 0) Then 'MVTO_CMBTE_MONTO_LIQUIDACION_COBRO_MENOR_A_CERO'
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoMovimientoComprobanteCobro" < 0) Then 'MVTO_CMBTE_MONTO_LOCAL_COBRO_MENOR_A_CERO'
						 	        Else ''
				                    End As "CodigoError",
				                    Case 
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(Cobro."IdCobro",'') = '' And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then 'IdCobro'
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then 'IdCobro'
							          When (Length(MvtoCmbteCob."IdMonedaCobro") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'IdMonedaCobro' 
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoCobro" < 0) Then 'IdCobro'
						 	          When (Length(MvtoCmbteCob."IdMonedaLiquidacionCobro") > 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 'IdMonedaLiquidacionCobro' 
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoLiquidacionCobro" < 0) Then 'IdCobro'
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoMovimientoComprobanteCobro" < 0) Then 'IdCobro'
						 	        Else ''
				                    End As "CodigoDato1",				                    
		       	                    Case 
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(Cobro."IdCobro",'') = '' And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then MvtoCmbteCob."IdCobro"
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then MvtoCmbteCob."IdCobro"
							          When (Length(MvtoCmbteCob."IdMonedaCobro") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then MvtoCmbteCob."IdMonedaCobro"
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoCobro" < 0) Then MvtoCmbteCob."IdCobro"
						 	          When (Length(MvtoCmbteCob."IdMonedaLiquidacionCobro") > 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then MvtoCmbteCob."IdMonedaLiquidacionCobro"
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoLiquidacionCobro" < 0) Then MvtoCmbteCob."IdCobro"
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoMovimientoComprobanteCobro" < 0) Then MvtoCmbteCob."IdCobro"
						 	        Else ''
				                    End As "ValorDato1",
				                    Case 
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(Cobro."IdCobro",'') = '' And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then 'NumeroMvtoCmbte'
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then 'NumeroMvtoCmbte'
							          When (Length(MvtoCmbteCob."IdMonedaCobro") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then 'IdCobro'
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoCobro" < 0) Then 'NumeroMvtoCmbte'
						 	          When (Length(MvtoCmbteCob."IdMonedaLiquidacionCobro") > 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then 'IdCobro'
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoLiquidacionCobro" < 0) Then 'NumeroMvtoCmbte'
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoMovimientoComprobanteCobro" < 0) Then 'NumeroMvtoCmbte'
						 	        Else ''
				                    End As "CodigoDato2",
				                    Case 
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(Cobro."IdCobro",'') = '' And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then Cast(MvtoCmbteCob."NumeroMvtoCmbte" As Nvarchar(12)) 
				                      When (Length(MvtoCmbteCob."IdCobro") > 0 And Coalesce(DefCobro."CodigoDefinicionCobro",'') = '') Then Cast(MvtoCmbteCob."NumeroMvtoCmbte" As Nvarchar(12))
							          When (Length(MvtoCmbteCob."IdMonedaCobro") > 0 And Coalesce(Mon."IdMoneda",'') = '') Then MvtoCmbteCob."IdCobro"
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoCobro" < 0) Then Cast(MvtoCmbteCob."NumeroMvtoCmbte" As Nvarchar(12)) 
						 	          When (Length(MvtoCmbteCob."IdMonedaLiquidacionCobro") > 0 And Coalesce(MonLiq."IdMoneda",'') = '') Then MvtoCmbteCob."IdCobro"
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoLiquidacionCobro" < 0) Then Cast(MvtoCmbteCob."NumeroMvtoCmbte" As Nvarchar(12)) 
						 	          When (Length(MvtoCmbteCob."IdCobro") > 0 And MvtoCmbteCob."MontoMovimientoComprobanteCobro" < 0) Then Cast(MvtoCmbteCob."NumeroMvtoCmbte" As Nvarchar(12)) 
						 	        Else ''
				                    End As "ValorDato2",
		       	                    "NumeroMvtoCmbte",
		       	                    'IngresoMovimientoComprobanteCobros' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteCobro" As MvtoCmbteCob
  		                       Left Join "BSCL.Plataforma.BD::COM.Cobro" As Cobro
  		                         On Cobro."IdCobro" = MvtoCmbteCob."IdCobro"
  		                       Left Join "BSCL.Plataforma.BD::COM_2.DefinicionCobro" As DefCobro
  		                         On DefCobro."CodigoDefinicionCobro" = MvtoCmbteCob."IdCobro"
  		                       Left Join "BSCL.Plataforma.BD::PLA.Moneda" As Mon 
  		                         On Mon."IdMoneda" = MvtoCmbteCob."IdMonedaCobro"
  		                       Left Join "BSCL.Plataforma.BD::PLA.Moneda" As MonLiq 
  		                         On MonLiq."IdMoneda" = MvtoCmbteCob."IdMonedaLiquidacionCobro" Union All
                             Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
                                    "NumeroMvtoCmbte","Entidad" 
                             From :v_TablaRequisitos;         
	     	                  
         --Análisis de datos
         Select Count (Case 
                         When "Prioridad" = 1 Then 1 
                       End),
	   			Count(Case 
	   			         When "Prioridad" > 1 Then 1 
	   			      End)
	     Into v_error_General, v_error_Negocio
	   	 From :v_TablaRequisitos 
	   	 Where "Cumple" = 0;
	   	 
	   	 p_out_resultadoRegla := Case 
	   	                           When :v_error_General > 0 THEN 'RECH'
	   							   When :v_error_Negocio > 0 THEN 'RECH'--'ADVE'
	   						     Else 'OK'
	   							 End;
	   							 
	     	                              
	     /** Acumula movimientos rechazados por validaciones **/
	     Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_OUT_MovimientosRechazados"
	      ( "NumeroMvtoCmbte" )
	     Select "NumeroMvtoCmbte"
	     From :v_TablaRequisitos
	     Where "Prioridad" >= 1
	       And "Cumple" = 0;                              
	                             
	                               
	     v_TablaCodigosMensajes = Select "NumeroMvtoCmbte" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  "CodigoDato2", "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 		                        
		 /** Acumula detalle de errores**/                       
		 Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_OUT_ErroresDetalle"
		  ( "NumeroMvtoCmbte", "Codigo", "Descripcion" ) 
		 Select "NumeroMvtoCmbte", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		 From :v_ErroresDetalle As ErrDet
		   Inner Join :v_TablaRequisitos As TabReq 
		      On TabReq."CodigoError" = ErrDet."Codigo"
		     And TabReq."NumeroMvtoCmbte" = ErrDet."IdInterno"
		 Where "Cumple" = 0; 
      
    END; --End Validaciones	
END;--End Principal
GO