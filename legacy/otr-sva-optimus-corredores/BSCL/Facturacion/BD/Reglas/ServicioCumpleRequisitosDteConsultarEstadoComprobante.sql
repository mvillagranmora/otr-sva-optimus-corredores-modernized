SET SCHEMA "BSCL";
   
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosDteConsultarEstadoComprobante";

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosDteConsultarEstadoComprobante"
  (
     IN p_ComprobanteAAct table("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdComprobante" BIGINT, "IdTipoComprobante" NVARCHAR(3)),  
		                              				                      
     OUT p_out_MovimientosRech	Table ("IdComprobante" BIGINT),
	
	
     -- Salidas para mensajes de errores 
	 OUT p_out_ErroresDetalle  Table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
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

        p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle;					
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT 0 As "IdComprobante", '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT 0 AS "IdComprobante" FROM dummy WHERE 1=2;
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   DECLARE v_countTableFormaPago	BIGINT;
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520),
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
				       	            '' AS "CodigoDato2", 
				       	            '' AS "ValorDato2",
				       	            0 as "IdComprobante",
				       	            '' as "Entidad"
		                    From :p_ComprobanteAAct As CmbteAAnu Union All 
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
		       			           '' AS "CodigoDato2", 
				       	           '' AS "ValorDato2",
		       			           CmbteAAnu."IdComprobante",
		       			           'AnularComprobante' As "Entidad"
		                    From :p_ComprobanteAAct As CmbteAAnu Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "CodigoDato2","ValorDato2","IdComprobante","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 0
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 0
                                    Else 1
                                    End As "Cumple",                                    
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 'CMBTE_FACTURACION_NO_EXISTE'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'CMBTE_TIPO_COMPROBANTE_NO_EXISTE'
                                    Else ''
                                    End As "CodigoError",                                                                      
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 'IdComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdTipoComprobante'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then Cast(CmbteAAnu."IdComprobante" As Nvarchar(12))
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then CmbteAAnu."IdTipoComprobante"
                                    Else ''
                                    End As "ValorDato1",  
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then ''
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdComprobante'
                                    Else ''
                                    End As "CodigoDato2", 
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then ''
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then Cast(CmbteAAnu."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato2",                                                                     
		       	                    Cmbte."IdComprobante",
		       	                    'AnularComprobante' As "Entidad"		       	                          
		       	             From :p_ComprobanteAAct As CmbteAAnu 
		       	               Left Join "BSCL.Facturacion.BD::FAC.Comprobante" As Cmbte 
		       	                 On Cmbte."IdComprobante" = CmbteAAnu."IdComprobante" 
		       	                And Cmbte."IdCliente" = CmbteAAnu."IdCliente"
		       	                And Cmbte."IdPortafolio" = CmbteAAnu."IdPortafolio"
		       	                And Cmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante"
		       	               Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte  
		       	                 On TipCmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2","ValorDato2","IdComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		                     
		 --Valida los campos no obligatorios, reglas de negocio 
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then 0
                                      When ConsCmbte."IdEstadoDTE" <> 'REC' Then 0
                                      When Length(ConsCmbte."IdRespuestaDTE") = 0 Then 0
                                      When Length(ConsCmbte."RespuestaLocationDTE") = 0 Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then 'CMBTE_DIS_ENV' 
                                      When ConsCmbte."IdEstadoDTE" <> 'REC' Then 'CMBTE_DTE_DIS_REC'                                                                            
                                      When Length(ConsCmbte."IdRespuestaDTE") = 0 Then 'CMBTE_RESPUESTA_DTE'
                                      When Length(ConsCmbte."RespuestaLocationDTE") = 0 Then 'CMBTE_RESPUESTA_LOCATION_DTE'
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' Then 'IdComprobante'
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then 'IdComprobante'
                                      When ConsCmbte."IdEstadoDTE" <> 'REC' Then 'IdComprobante'
                                      When Length(ConsCmbte."IdRespuestaDTE") = 0 Then 'IdComprobante'
                                      When Length(ConsCmbte."RespuestaLocationDTE") = 0 Then 'IdComprobante'                                      
                                    Else ''
                                    End As "CodigoDato1",
                                    Case                                       
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                      When ConsCmbte."IdEstadoComprobante" <> 'ENV' Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                      When ConsCmbte."IdEstadoDTE" <> 'REC' Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                      When Length(ConsCmbte."IdRespuestaDTE") = 0 Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                      When Length(ConsCmbte."RespuestaLocationDTE") = 0 Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",
                                    '' AS "CodigoDato2", 
				       	            '' AS "ValorDato2",
		       	                    CmbteAAnu."IdComprobante",
		       	                    'AnularComprobante' As "Entidad"
		       	             From :p_ComprobanteAAct As CmbteAAnu		       	             
		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
		       	                   On ConsCmbte."IdComprobante" = CmbteAAnu."IdComprobante"
		       	                  And ConsCmbte."IdPortafolio" = CmbteAAnu."IdPortafolio"
                                  And ConsCmbte."IdCliente" = CmbteAAnu."IdCliente"
                                  And ConsCmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante" Union All
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
	   							 
	     p_out_MovimientosRech = Select Distinct "IdComprobante"
	                             From :v_TablaRequisitos
	                             Where "Prioridad" >= 1
	                               And "Cumple" = 0;
	                               
	     v_TablaCodigosMensajes = Select "IdComprobante" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  "CodigoDato2", "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 p_out_ErroresDetalle = Select "IdComprobante", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		  	                    From :v_ErroresDetalle As ErrDet
		                          Inner Join :v_TablaRequisitos As TabReq 
		                             On TabReq."CodigoError" = ErrDet."Codigo"
		                            And TabReq."IdComprobante" = ErrDet."IdInterno"
		                        Where "Cumple" = 0;
      
    END; --End Validaciones	
END;--End Principal
