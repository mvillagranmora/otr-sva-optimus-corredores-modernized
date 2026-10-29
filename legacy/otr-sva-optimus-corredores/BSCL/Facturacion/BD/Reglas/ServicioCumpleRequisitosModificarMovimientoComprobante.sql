SET SCHEMA "BSCL";
   
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosModificarMovimientoComprobante";

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosModificarMovimientoComprobante"
  (
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
		Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_ErroresDetalle"
		 ( "IdMovimientoComprobante", "Codigo", "Descripcion" ) 
		Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion"  
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
	                                           "IdMovimientoComprobante" BIGINT, "Entidad" NVARCHAR(250));
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2", 
										  0 AS "IdMovimientoComprobante",'' as "Entidad" FROM dummy WHERE 1=2;
		

		
		/** Validaciones Basicas **/
		v_Obligatorios := 'IdMovimientoComprobante';
		
		--Valida que existan movimientos 
        v_TablaRequisitos = Select 1 As "Prioridad", 
				       	            Case 
				       		           When Count("IdMovimientoComprobante") < 1 THEN 0 
				       		        Else 1 
						            End As "Cumple", 
				       	            'NO_VIENEN_MOVIMIENTOS' AS "CodigoError", 
				       	            '' As "CodigoDato1",
				       	            '' As "ValorDato1",
				       	            '' As "CodigoDato2",
				       	            '' As "ValorDato2",
				       	            0 as "IdMovimientoComprobante",
				       	            'ModificarMovimientoComprobante' as "Entidad"
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_IN_MovimientoComprobante" Union All
                            Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
		                           "IdMovimientoComprobante","Entidad" 
		                    From :v_TablaRequisitos;


		                    
		--Valida los campos obligatorios, no deben estar vacios
        v_TablaRequisitos = Select 2 as "Prioridad",
                                   Case 
                                     When Length("IdMovimientoComprobante") = 0 Or "IdMovimientoComprobante" = 0 Then 0
		       			           Else 1
		       			           End As "Cumple",		       			           
		       			           Case
		       			             When Length("IdMovimientoComprobante") = 0 Or "IdMovimientoComprobante" = 0 Then 'MVTO_CMBTE_INVALIDO'
		       			           Else ''
		       			           End As "CodigoError",		       			           
		       			           Case
		       			             When Length("IdMovimientoComprobante") = 0 Or "IdMovimientoComprobante" = 0 Then 'IdMovimientoComprobante' 	       			           
		       			           Else ''
		       			           End As "CodigoDato1",		       			           
		       			           Case 
		       			             When Length("IdMovimientoComprobante") = 0 Or "IdMovimientoComprobante" = 0 Then Cast("IdMovimientoComprobante" As Nvarchar(12))
		       			           Else ''
		       			           End As "ValorDato1",	
		       			           '' As "CodigoDato2",
				       	           '' As "ValorDato2",		       			           	       			           
		       			           MvtoCmbte."IdMovimientoComprobante",
		       			           'ModificarMovimientoComprobante' As "Entidad"
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
		                           "IdMovimientoComprobante","Entidad" 
		                    From :v_TablaRequisitos;		  


		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(ConsMvtoCmbte."IdMovimientoComprobante", 0) = 0 Then 0
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case
                                      When Coalesce(ConsMvtoCmbte."IdMovimientoComprobante", 0) = 0 Then 'MOVIMIENTO_COMPROBANTE_NO_EXISTE'
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' Then 'MOVIMIENTO_COMPROBANTE_DIS_ING' 
                                    Else ''
                                    End As "CodigoError",                                    
                                    Case
                                      When Coalesce(ConsMvtoCmbte."IdMovimientoComprobante", 0) = 0 Then 'IdMovimientoComprobante'
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' Then 'IdMovimientoComprobante'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(ConsMvtoCmbte."IdMovimientoComprobante", 0) = 0 Then Cast(MvtoCmbte."IdMovimientoComprobante" As Nvarchar(12))
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' Then Cast(MvtoCmbte."IdMovimientoComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1", 
                                    '' As "CodigoDato2",
				       	            '' As "ValorDato2",                                                                      
		       	                    MvtoCmbte."IdMovimientoComprobante",
		       	                    'ModificarMovimientoComprobante' As "Entidad"       
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_IN_MovimientoComprobante" As MvtoCmbte
		       	               Left Join "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" As ConsMvtoCmbte
		       	                 On ConsMvtoCmbte."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante" Union All   
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1", "CodigoDato2","ValorDato2",
		                            "IdMovimientoComprobante","Entidad" 
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
	     Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados"
	      ( "IdMovimientoComprobante" )
	     Select "IdMovimientoComprobante"
	     From :v_TablaRequisitos
	     Where "Prioridad" >= 1
	       And "Cumple" = 0;                              
	                             
	                               
	     v_TablaCodigosMensajes = Select "IdMovimientoComprobante" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  "CodigoDato2", "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 		                        
		 /** Acumula detalle de errores**/                       
		 Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_ErroresDetalle"
		  ( "IdMovimientoComprobante", "Codigo", "Descripcion" ) 
		 Select "IdMovimientoComprobante", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		 From :v_ErroresDetalle As ErrDet
		   Inner Join :v_TablaRequisitos As TabReq 
		      On TabReq."CodigoError" = ErrDet."Codigo"
		     And TabReq."IdMovimientoComprobante" = ErrDet."IdInterno"
		 Where "Cumple" = 0; 
      
    END; --End Validaciones	
END;--End Principal
