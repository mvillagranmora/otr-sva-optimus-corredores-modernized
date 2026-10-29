SET SCHEMA "BSCL";
   
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosProcesarPreferenciaFacturacionPortafolio";

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosProcesarPreferenciaFacturacionPortafolio"
  (
     IN p_PortAConsultar table ("IdPortafolio" BIGINT),
		                              				                      
     OUT p_out_PortafoliosRech	Table ("IdPortafolio" BIGINT),
	
	
     -- Salidas para mensajes de errores 
	 OUT p_out_ErroresDetalle  Table ("IdPortafolio" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
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
	--DECLARE v_resultadoRegla       	NVARCHAR(40);
	--DECLARE v_ErroresContador 		INT; 
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

        p_out_ErroresDetalle = Select "IdInterno" As "IdPortafolio", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle;                       
                               
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT 0 As "IdPortafolio", '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_PortafoliosRech	= SELECT 0 AS "IdPortafolio" FROM dummy WHERE 1=2;
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   --DECLARE v_Indicadores			NVARCHAR(250);
	   DECLARE v_countTableFormaPago	BIGINT;
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100), 
											   "ValorDato1" NVARCHAR(520), "IdPortafolio" BIGINT, "Entidad" NVARCHAR(250));
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1", 
										  0 AS "IdPortafolio",'' as "Entidad" FROM dummy WHERE 1=2;
		

		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		v_Obligatorios := 'IdPortafolio';
		
		--Valida que existan movimientos 
        v_TablaRequisitos = Select 1 As "Prioridad", 
				       	            Case 
				       		           When Count("IdPortafolio") < 1 THEN 0 
				       		        Else 1 
						            End As "Cumple", 
				       	            'NO_VIENEN_MOVIMIENTOS' AS "CodigoError", 
				       	            '' As "CodigoDato1",
				       	            '' As "ValorDato1",
				       	            0 as "IdPortafolio",
				       	            '' as "Entidad"
		                    From :p_PortAConsultar As PortACon Union All 
		                    SELECT "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "IdPortafolio","Entidad" 
		                    From :v_TablaRequisitos;
		                    
		--Valida los campos obligatorios, no deben estar vacios
        v_TablaRequisitos = Select 2 as "Prioridad",
                                   Case 
		       		                 When Length("IdPortafolio") < 1 Then 0
		       			           Else 1
		       			           End As "Cumple",
		       			           'PARAMETROS_OBLIGATORIOS' As "CodigoError",
		       			           'CodigoParametros' As "CodigoDato1",
		       			           v_Obligatorios As "ValorDato1",
		       			           PortACon."IdPortafolio",
		       			           'ConsultarPreferenciaPortafolio' As "Entidad"
		                    From :p_PortAConsultar As PortACon Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "IdPortafolio","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(ConsPrefPort."Valor",'') = '' Then 0
                                    Else 1
                                    End As "Cumple",                                    
                                    Case
                                      When Coalesce(ConsPrefPort."Valor",'') = '' Then 'PREFFAC_PORTAFOLIO_NO_EXISTE'
                                    Else ''
                                    End As "CodigoError",                                                                      
                                    Case
                                      When Coalesce(ConsPrefPort."Valor",'') = '' Then 'IdPortafolio'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(ConsPrefPort."Valor",'') = '' Then Cast(PortACon."IdPortafolio" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",                                                                       
		       	                    PortACon."IdPortafolio",
		       	                    'ConsultarPreferenciaPortafolio' As "Entidad"		       	                          
		       	             From :p_PortAConsultar As PortACon 
		       	               Left Join "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion" As ConsPrefPort
		       	                 On ConsPrefPort. "IdPortafolio" = PortACon."IdPortafolio" 
		       	                And ConsPrefPort."Codigo" = 'FAC_MODFAC' Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdPortafolio","Entidad" 
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
	   							 
	     p_out_PortafoliosRech = Select Distinct "IdPortafolio"
	                             From :v_TablaRequisitos
	                             Where "Prioridad" >= 1
	                               And "Cumple" = 0;
	                               
	     v_TablaCodigosMensajes = Select "IdPortafolio" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  '' As "CodigoDato2", '' As "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 p_out_ErroresDetalle = Select "IdPortafolio", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		  	                    From :v_ErroresDetalle As ErrDet
		                          Inner Join :v_TablaRequisitos As TabReq 
		                             On TabReq."CodigoError" = ErrDet."Codigo"
		                            And TabReq."IdPortafolio" = ErrDet."IdInterno"
		                        Where "Cumple" = 0;                        

      
    END; --End Validaciones	
END;--End Principal
