SET SCHEMA "BSCL";
   
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosCrearComprobanteNotaDeCredito";

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosCrearComprobanteNotaDeCredito"
  (     
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

        		                       
		/** Acumula detalle errores **/                       
		Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_ErroresDetalle"
		  ( "IdComprobante", "Codigo", "Descripcion" )
		Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
		   
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520), "CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520),
	                                           "IdComprobante" BIGINT, "Entidad" NVARCHAR(250));			
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1",'' AS "CodigoDato2", '' AS "ValorDato2", 
										  0 AS "IdComprobante",'' as "Entidad" FROM dummy WHERE 1=2;
		

		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		v_Obligatorios := 'IdComprobante, IdTipoComprobante';
		
		--Valida que existan movimientos 
        v_TablaRequisitos = Select 1 As "Prioridad", 
				       	            Case 
				       		           When Count("IdComprobante") < 1 THEN 0 
				       		        Else 1 
						            End As "Cumple", 
				       	            'NO_VIENEN_MOVIMIENTOS' AS "CodigoError", 
				       	            '' As "CodigoDato1",
				       	            '' As "ValorDato1",
				       	            '' As "CodigoDato2",
				       	            '' As "ValorDato2",
				       	            0 as "IdComprobante",
				       	            '' as "Entidad"
		                    --From :p_ComprobanteANC As CmbteNC Union All 
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_IN_Comprobante" As CmbteNC Union All 
		                    SELECT "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "CodigoDato2","ValorDato2","IdComprobante","Entidad" 
		                    From :v_TablaRequisitos;
		                    
		--Valida los campos obligatorios, no deben estar vacios
        v_TablaRequisitos = Select 2 as "Prioridad",
                                   Case 
		       		                 When Length("IdComprobante") < 1 Or
		       		                      Length("IdTipoComprobante") = 0 Then 0
		       			           Else 1
		       			           End As "Cumple",
		       			           'PARAMETROS_OBLIGATORIOS' As "CodigoError",
		       			           'CodigoParametros' As "CodigoDato1",
		       			           v_Obligatorios As "ValorDato1",
		       			           '' As "CodigoDato2",
				       	           '' As "ValorDato2",
		       			           "IdComprobante",
		       			           'AnularComprobante' As "Entidad"
		                    --From :p_ComprobanteANC As CmbteNC Union All
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_IN_Comprobante" As CmbteNC Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "CodigoDato2","ValorDato2","IdComprobante","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 0
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 0
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') in ('NCE', 'NCR', 'NEO') Then 0
                                    Else 1
                                    End As "Cumple",                                    
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 'CMBTE_FACTURACION_NO_EXISTE'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'CMBTE_TIPO_COMPROBANTE_NO_EXISTE'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') in ('NCE', 'NCR', 'NEO') Then 'CMBTE_TIPO_COMPROBANTE_NOTA_CREDITO'
                                    Else ''
                                    End As "CodigoError",                                                                      
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 'IdComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdTipoComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') in ('NCE', 'NCR', 'NEO') Then 'IdComprobante'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then Cast(CmbteNC."IdComprobante" As Nvarchar(12))
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then CmbteNC."IdTipoComprobante"
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') in ('NCE', 'NCR', 'NEO') Then Cast(CmbteNC."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then ''
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') in ('NCE', 'NCR', 'NEO') Then 'IdComprobante'
                                    Else ''
                                    End As "CodigoDato2",
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then ''
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then Cast(CmbteNC."IdComprobante" As Nvarchar(12))
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') in ('NCE', 'NCR', 'NEO') Then 'IdComprobante'
                                    Else ''
                                    End As "ValorDato2",                                                                       
		       	                    CmbteNC."IdComprobante",
		       	                    'ComprobanteNotaDeCredito' As "Entidad"		       	                          
		       	             --From :p_ComprobanteANC As CmbteNC 
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_IN_Comprobante" As CmbteNC  
		       	               Left Join "BSCL.Facturacion.BD::FAC.Comprobante" As Cmbte 
		       	                 On Cmbte."IdComprobante" = CmbteNC."IdComprobante" 
		       	                And Cmbte."IdCliente" = CmbteNC."IdCliente"
		       	                And Cmbte."IdPortafolio" = CmbteNC."IdPortafolio"
		       	                And Cmbte."IdTipoComprobante" = CmbteNC."IdTipoComprobante"
		       	               Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte  
		       	                 On TipCmbte."IdTipoComprobante" = CmbteNC."IdTipoComprobante" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2","ValorDato2","IdComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		                     
		 --Valida los campos no obligatorios, reglas de negocio 
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then 0                                      
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then 'CMBTE_DIS_ENV'                                      
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then 'IdComprobante'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",
                                    '' As "CodigoDato2",
				       	            '' As "ValorDato2",
		       	                    CmbteNC."IdComprobante",
		       	                    'ComprobanteNotaDeCredito' As "Entidad"
		       	             --From :p_ComprobanteANC As CmbteNC		       	             
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_IN_Comprobante" As CmbteNC 
		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
		       	                   On ConsCmbte."IdComprobante" = CmbteNC."IdComprobante"
		       	                  And ConsCmbte."IdPortafolio" = CmbteNC."IdPortafolio"
                                  And ConsCmbte."IdCliente" = CmbteNC."IdCliente"
                                  And ConsCmbte."IdTipoComprobante" = CmbteNC."IdTipoComprobante" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2","ValorDato2","IdComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		 
		 
		 --Valida que no exista otra nota de credito vigente para comprobante.
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When ConsCmbte."IdComprobante" > 0 Then 0                                      
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When ConsCmbte."IdComprobante" > 0 Then 'CMBTE_NOTA_CREDITO_ASOCIADA'                                      
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When ConsCmbte."IdComprobante" > 0 Then 'IdComprobante'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When ConsCmbte."IdComprobante" > 0 Then Cast(CmbteNC."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",                                    
                                    Case
                                      When ConsCmbte."IdComprobante" > 0 Then 'IdComprobante2'
                                    Else ''
                                    End As "CodigoDato2",
                                    Case 
                                      When ConsCmbte."IdComprobante" > 0 Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato2",
		       	                    CmbteNC."IdComprobante",
		       	                    'ComprobanteNotaDeCredito' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_IN_Comprobante" As CmbteNC 
		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
		       	                   On ConsCmbte."IdComprobanteReferencia" = CmbteNC."IdComprobante"
		       	                  And "IdEstadoComprobante" <> 'ANU'   Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2","ValorDato2","IdComprobante","Entidad" 
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
	   							   When :v_error_Negocio > 0 THEN 'ADVE'
	   						     Else 'OK'
	   							 End;
	   							 
	     /** Acumula comprobantes rechazados **/
	     Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_MovimientosRechazados"                           
	      ( "IdComprobante" )
	     Select Distinct "IdComprobante"
	     From :v_TablaRequisitos
	     Where "Prioridad" >= 1
	       And "Cumple" = 0;  
	      
	                               
	     v_TablaCodigosMensajes = Select "IdComprobante" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  "CodigoDato2", "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 
		 /** Acumula detalle de errores de diccionario **/
		 Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_ErroresDetalle"
		  ( "IdComprobante", "Codigo", "Descripcion" )
		 Select "IdComprobante", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		 From :v_ErroresDetalle As ErrDet
		   Inner Join :v_TablaRequisitos As TabReq 
		      On TabReq."CodigoError" = ErrDet."Codigo"
		     And TabReq."IdComprobante" = ErrDet."IdInterno"
		 Where "Cumple" = 0;
      
    END; --End Validaciones	
END;--End Principal
