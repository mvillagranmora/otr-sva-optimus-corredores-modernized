SET SCHEMA "BSCL";
  
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosGenerarInterfazAfp";

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosGenerarInterfazAfp"
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

		Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ErroresDetalle"
		Select "IdInterno" As "IdCliente", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
		
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520),
	                                           "IdCliente" BIGINT, "Entidad" NVARCHAR(250));			
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1",'' AS "CodigoDato2", '' AS "ValorDato2", 
										  0 AS "IdCliente",'' as "Entidad" FROM dummy WHERE 1=2;
		

		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		v_Obligatorios := 'IdCliente, CodigoOperadorAfp, FechaMovimientoComprobante';
		
		--Valida que existan movimientos 
        v_TablaRequisitos = Select 1 As "Prioridad", 
				       	            Case 
				       		           When Count("IdCliente") < 1 THEN 0 
				       		        Else 1 
						            End As "Cumple", 
				       	            'NO_VIENEN_MOVIMIENTOS' AS "CodigoError", 
				       	            '' As "CodigoDato1",
				       	            '' As "ValorDato1",
				       	            '' AS "CodigoDato2", 
				       	            '' AS "ValorDato2",
				       	            0 as "IdCliente",
				       	            'GenerarInterfazAfp' as "Entidad"
		                    From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_IN_Movimiento" As MvtoAGen Union All 
		                    SELECT "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "CodigoDato2","ValorDato2","IdCliente","Entidad" 
		                    From :v_TablaRequisitos;
		                    
		--Valida los campos obligatorios, no deben estar vacios
        v_TablaRequisitos = Select 2 as "Prioridad",
                                   Case 
		       		                 When Length("IdCliente") < 1 Or
		       		                      Length("CodigoOperadorAfp") < 1 Or
		       		                      Length("FechaMovimientoComprobante") < 1 Then 0
		       			           Else 1
		       			           End As "Cumple",
		       			           'PARAMETROS_OBLIGATORIOS' As "CodigoError",
		       			           'CodigoParametros' As "CodigoDato1",
		       			           v_Obligatorios As "ValorDato1",
		       			           '' AS "CodigoDato2", 
				       	           '' AS "ValorDato2",
		       			           MvtoAGen."IdCliente",
		       			           'GenerarInterfazAfp' As "Entidad"
		                    From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_IN_Movimiento" As MvtoAGen Union All 
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "CodigoDato2","ValorDato2","IdCliente","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(ConsClie."IdCliente",0) = 0 Then 0
                                      When Coalesce(ConsIndPor."CodigoTipoIndicador",'') = '' Then 0
                                    Else 1
                                    End As "Cumple",                                    
                                    Case
                                      When Coalesce(ConsClie."IdCliente",0) = 0 Then 'MVTO_FACTURACION_CLIENTE_NO_EXISTE'
                                      When Coalesce(ConsIndPor."CodigoTipoIndicador",'') = '' Then 'MVTO_FACTURACION_CODIGO_OPERADOR_AFP_NO_EXISTE'
                                    Else ''
                                    End As "CodigoError",                                                                      
                                    Case
                                      When Coalesce(ConsClie."IdCliente",0) = 0 Then 'IdCliente'
                                      When Coalesce(ConsIndPor."CodigoTipoIndicador",'') = '' Then 'CodigoOperadorAfp'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(ConsClie."IdCliente",0) = 0 Then Cast(MvtoAGen."IdCliente" As Nvarchar(12))
                                      When Coalesce(ConsIndPor."CodigoTipoIndicador",'') = '' Then Cast(MvtoAGen."CodigoOperadorAfp" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",  
                                    '' As "CodigoDato2", 
                                    '' As "ValorDato2",                                                                     
		       	                     MvtoAGen."IdCliente",
		       	                    'GenerarInterfazAfp' As "Entidad"		       	                          
		       	             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_IN_Movimiento" As MvtoAGen
		       	               Left Join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" As ConsClie
		       	                 On ConsClie."IdCliente" = MvtoAGen."IdCliente"
		       	               Left Join "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" As ConsIndPor 
		       	                 On ConsIndPor."IdCliente" = MvtoAGen."IdCliente"
		       	                And ConsIndPor."CodigoTipoIndicador" = 'FAC_COIAFP' Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2","ValorDato2","IdCliente","Entidad" 
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
	   							 
	     
	     /** Inserta en tabla rechazados **/
	     Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ClienteRechazados"
	     Select Distinct "IdCliente"
	     From :v_TablaRequisitos
	     Where "Prioridad" >= 1
	       And "Cumple" = 0;  
	       	       
	                               
	     v_TablaCodigosMensajes = Select "IdCliente" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  "CodigoDato2", "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 
		Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ErroresDetalle"
		Select "IdCliente", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		From :v_ErroresDetalle As ErrDet
		   Inner Join :v_TablaRequisitos As TabReq 
		      On TabReq."CodigoError" = ErrDet."Codigo"
		     And TabReq."IdCliente" = ErrDet."IdInterno"
		Where "Cumple" = 0;
		                         
      
    END; --End Validaciones	
END;--End Principal
