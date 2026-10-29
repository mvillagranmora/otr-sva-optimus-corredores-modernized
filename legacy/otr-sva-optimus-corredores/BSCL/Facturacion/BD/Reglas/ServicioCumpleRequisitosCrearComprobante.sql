SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosCrearComprobante";
GO

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosCrearComprobante"
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
        		                       
		Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_OUT_ErroresDetalle"
		Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
		                                              
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   DECLARE v_countTableFormaPago	BIGINT;
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100), 
											   "ValorDato1" NVARCHAR(520), "IdMovimientoComprobante" BIGINT, "Entidad" NVARCHAR(250));			
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		DELETE FROM "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_OUT_MovimientosRechazados";
		DELETE FROM "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_OUT_AdvertenciasDetalle";
		DELETE FROM "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_OUT_ErroresDetalle";
		
		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1", 
										  0 AS "IdMovimientoComprobante",'' as "Entidad" FROM dummy WHERE 1=2;
		

		
		/** Validaciones Básicas **/		
		v_Obligatorios := 'IdMovimientoComprobante, IdTipoComprobante, IdPortafolio';
		
		--Valida que existan movimientos 
        v_TablaRequisitos = Select 1 As "Prioridad", 
				       	            Case 
				       		           When Count("IdMovimientoComprobante") < 1 THEN 0 
				       		        Else 1 
						            End As "Cumple", 
				       	            'NO_VIENEN_MOVIMIENTOS - FACTURACION' AS "CodigoError", 
				       	            '' As "CodigoDato1",
				       	            '' As "ValorDato1",
				       	            0 as "IdMovimientoComprobante",
				       	            '' as "Entidad"
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante" As MvtoCmbteAFac Union All 
		                    SELECT "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "IdMovimientoComprobante","Entidad" 
		                    From :v_TablaRequisitos;
		                    
		--Valida los campos obligatorios, no deben estar vacios
        v_TablaRequisitos = Select 2 as "Prioridad",
                                   Case 
		       		                 When Length("IdMovimientoComprobante") = 0 Or
		       		                      Length("IdTipoComprobante") = 0 Or
				       			          Length("IdPortafolio") = 0
		       			             Then 0
		       			           Else 1
		       			           End As "Cumple",
		       			           'PARAMETROS_OBLIGATORIOS' As "CodigoError",
		       			           'CodigoParametros' As "CodigoDato1",
		       			           v_Obligatorios As "ValorDato1",
		       			           MvtoCmbteAFac."IdMovimientoComprobante",
		       			           'IngresoComprobante' As "Entidad"
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante" As MvtoCmbteAFac Union All 
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "IdMovimientoComprobante","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(MvtoCmbte."IdMovimientoComprobante",0) = 0 Then 0
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 0                                      
                                    Else 1
                                    End As "Cumple",                                    
                                    Case
                                      When Coalesce(MvtoCmbte."IdMovimientoComprobante",0) = 0 Then 'CMBTE_MOVIMIENTO_FACTURACION_NO_EXISTE'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'CMBTE_TIPO_COMPROBANTE_NO_EXISTE'
                                    Else ''
                                    End As "CodigoError",                                                                      
                                    Case
                                      When Coalesce(MvtoCmbte."IdMovimientoComprobante",0) = 0 Then 'IdMovimientoComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdTipoComprobante'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(MvtoCmbte."IdMovimientoComprobante",0) = 0 Then Cast(MvtoCmbteAFac."IdMovimientoComprobante" As Nvarchar(12))
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then MvtoCmbteAFac."IdTipoComprobante"
                                    Else ''
                                    End As "ValorDato1",                                                                       
		       	                    MvtoCmbte."IdMovimientoComprobante",
		       	                    'IngresoComprobante' As "Entidad"		       	                          
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante" As MvtoCmbteAFac
		       	               Left Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte 
		       	                 On MvtoCmbte. "IdMovimientoComprobante" = MvtoCmbteAFac."IdMovimientoComprobante" 
		       	                And MvtoCmbte. "IdTipoComprobante" = MvtoCmbteAFac."IdTipoComprobante"
		       	               Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte  
		       	                 On TipCmbte."IdTipoComprobante" = MvtoCmbteAFac."IdTipoComprobante" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		       	       
		       	                  
         --Valida los campos no obligatorios, reglas de negocio
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When MvtoCmbte."EsAPlazo" = 'S' Then 0  
                                      When MvtoCmbte."IdEstadoFacturacion" = 'BLO' Then 0
                                      When MvtoCmbte."IdEstadoFacturacion" = 'FAC' Then 0
                                      When MvtoCmbte."IdEstadoFacturacion" = 'ANU' Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When MvtoCmbte."EsAPlazo" = 'S' Then 'MOVIMIENTO_COMPROBANTE_ESAPLAZO'
                                      When MvtoCmbte."IdEstadoFacturacion" = 'BLO' Then 'MOVIMIENTO_COMPROBANTE_BLQ'
                                      When MvtoCmbte."IdEstadoFacturacion" = 'FAC' Then 'MOVIMIENTO_COMPROBANTE_FAC'
                                      When MvtoCmbte."IdEstadoFacturacion" = 'ANU' Then 'MOVIMIENTO_COMPROBANTE_ANU'
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When MvtoCmbte."EsAPlazo" = 'S' Then 'IdMovimientoComprobante'
                                      When MvtoCmbte."IdEstadoFacturacion" = 'BLO' Then 'IdMovimientoComprobante'
                                      When MvtoCmbte."IdEstadoFacturacion" = 'FAC' Then 'IdMovimientoComprobante'
                                      When MvtoCmbte."IdEstadoFacturacion" = 'ANU' Then 'IdMovimientoComprobante'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When MvtoCmbte."EsAPlazo" = 'S' Then Cast(MvtoCmbte."IdMovimientoComprobante" As Nvarchar(6))
                                      When MvtoCmbte."IdEstadoFacturacion" = 'BLO' Then Cast(MvtoCmbte."IdMovimientoComprobante" As Nvarchar(6))
                                      When MvtoCmbte."IdEstadoFacturacion" = 'FAC' Then Cast(MvtoCmbte."IdMovimientoComprobante" As Nvarchar(6))
                                      When MvtoCmbte."IdEstadoFacturacion" = 'ANU' Then Cast(MvtoCmbte."IdMovimientoComprobante" As Nvarchar(6))
                                    Else ''
                                    End  As "ValorDato1",
		       	                    MvtoCmbteAFac."IdMovimientoComprobante",
		       	                    'IngresoComprobante' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante" As MvtoCmbteAFac
		       	                Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
		       	                   On MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteAFac."IdMovimientoComprobante" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		                     
		 --Verifica movimiento venta corta habilitado para cobrar prima al principio cuando corresponda
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(ConsInsSub."IdInstrumento",0) = 0 Then 0                                        
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When Coalesce(ConsInsSub."IdInstrumento",0) = 0 Then 'CMBTE_MOVIMIENTO_COMPROBANTE_NO HABILITADO_PRT'
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When Coalesce(ConsInsSub."IdInstrumento",0) = 0 Then 'IdMovimientoComprobante' 
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When Coalesce(ConsInsSub."IdInstrumento",0) = 0 Then Cast(MvtoCmbte."IdMovimientoComprobante" As Nvarchar(6))
                                    Else ''
                                    End  As "ValorDato1",
		       	                    MvtoCmbteAFac."IdMovimientoComprobante",
		       	                    'IngresoComprobante' As "Entidad"
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante" As MvtoCmbteAFac
		       	                Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
		       	                   On MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteAFac."IdMovimientoComprobante"
		       	                  And MvtoCmbte."EsVentaCorta" = 'S'
		       	                Inner Join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" As ConsInsSub
		       	                   On ConsInsSub."IdInstrumento" = MvtoCmbte."IdInstrumento"
		       	                  And ConsInsSub."IdTipoInstrumento" = 'PRT' Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		                                         
		                     
		 --Verifica los campos no obligatorios, movimiento asociados a secuencia
         v_MvtoAFacturarSec = Select MvtoCmbteAFac."IdMovimientoComprobante", "SecuenciaAgrupacion"
                              From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante" As MvtoCmbteAFac
                                Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
                                   On MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteAFac."IdMovimientoComprobante";
                                   
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Count(MvtoCmbte."IdMovimientoComprobante") > 1 Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When Count(MvtoCmbte."IdMovimientoComprobante") > 1 Then 'CMBTE_MOVIMIENTO_COMPROBANTE_DIFERENCIASEC'
                                    Else ''
                                    End As "CodigoError",
                                    Case 
                                      When Count(MvtoCmbte."IdMovimientoComprobante") > 1 Then 'IdMovimientoComprobante'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When Count(MvtoCmbte."IdMovimientoComprobante") > 1 Then Cast(MvtoCmbteAFacSec."IdMovimientoComprobante" As Nvarchar(6))
                                    Else ''
                                    End As "ValorDato1",
		       	                    MvtoCmbteAFacSec."IdMovimientoComprobante",
		       	                    'IngresoComprobante' As "Entidad"
		       	             From :v_MvtoAFacturarSec As MvtoCmbteAFacSec
		       	               Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
		       	                  On MvtoCmbte."SecuenciaAgrupacion" = MvtoCmbteAFacSec."SecuenciaAgrupacion"
		       	                 And MvtoCmbte."IdMovimientoComprobante" <> MvtoCmbteAFacSec."IdMovimientoComprobante"
		       	             Where Length(MvtoCmbteAFacSec."SecuenciaAgrupacion") > 0 And MvtoCmbteAFacSec."SecuenciaAgrupacion" <> '0'
		       	             Group By MvtoCmbteAFacSec."IdMovimientoComprobante" Union All		       	                
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos;	                     
		 		                    
		           
		 --Verifica que se Bloquearon todos los movimientos enviados
         Update "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
         Set "IdEstadoFacturacion" = 'BLO'
         Where "IdMovimientoComprobante" in (Select "IdMovimientoComprobante"
                                             --From :p_MvtoAFacturar)
                                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante" As MvtoCmbteAFac)
           And "IdEstadoFacturacion" = 'ING';
           
        
         /*v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When "IdEstadoFacturacion" <> 'BLO' Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When "IdEstadoFacturacion" <> 'BLO' Then 'MOVIMIENTO_COMPROBANTE_A_BLOQUEAR'
                                    Else ''
                                    End As "CodigoError",
                                    Case 
                                      When "IdEstadoFacturacion" <> 'BLO' Then 'IdMovimientoComprobante'
                                    Else ''
                                    End As "CodigoDato",
                                    Case 
                                      When "IdEstadoFacturacion" <> 'BLO' Then Cast(MvtoCmbteAFac."IdMovimientoComprobante" As Nvarchar(6))
                                    Else ''
                                    End As "ValorDato",
		       	                    MvtoCmbteAFac."IdMovimientoComprobante",
		       	                    'IngresoComprobante' As "Entidad"
		       	             From :p_MvtoAFacturar As MvtoCmbteAFac
		       	               Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
		       	                  On MvtoCmbte."IdPortafolio" = MvtoCmbteAFac."IdPortafolio"
		       	                 And MvtoCmbte."IdCliente" = MvtoCmbteAFac."IdCliente"
		       	                 And MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteAFac."IdMovimientoComprobante"
		       	                 And MvtoCmbte."IdTipoComprobante" = MvtoCmbteAFac."IdTipoComprobante" Union All		       	                
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos;*/
             
                             
		 --Verifica parametros de configuracion de facturación
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Count(ConFac."CodigoParametro") < 1 Then 0                                                                             
                                    Else 1
                                    End As "Cumple",
                                    'PARAMETRO_DE_CONFIGURACION_FAC_NO_EXISTE' As "CodigoError",
                                    '' As "CodigoDato1",
                                    '' As "ValorDato1",
		       	                    0 As "IdMovimientoComprobante",
		       	                    'ParámetrosdeConfiguracionFacturación' As "Entidad"
		       	             From "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" As ConFac Union All		       	                
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
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
	   							   When :v_error_Negocio > 0 THEN 'ADVE'
	   						     Else 'OK'
	   							 End;
	   	 	   	 
	   	 
	     /** Acumula movimientos rechazados **/                          
	     Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_OUT_MovimientosRechazados"
	     Select Distinct "IdMovimientoComprobante"
	     From :v_TablaRequisitos
	     Where "Prioridad" >= 1
	       And "Cumple" = 0;    
	       
	    
	    
	                               
	     v_TablaCodigosMensajes = Select "IdMovimientoComprobante" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  '' As "CodigoDato2", '' As "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 /** Acumula detalle de errores **/		 
		 Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_OUT_ErroresDetalle"
		 Select "IdMovimientoComprobante", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		 From :v_ErroresDetalle As ErrDet
		   Inner Join :v_TablaRequisitos As TabReq 
		      On TabReq."CodigoError" = ErrDet."Codigo"
		     And TabReq."IdMovimientoComprobante" = ErrDet."IdInterno"
		 Where "Cumple" = 0
		  And "Prioridad" = 1;
		  
		  
		 /** Acumula detalle de advertencias **/		 
		 Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_OUT_AdvertenciasDetalle"
		 Select "IdMovimientoComprobante", ErrDet."Codigo" As "Codigo", 'N' As "RequiereAutorizacion", ErrDet."Descripcion"
		 From :v_ErroresDetalle As ErrDet
		   Inner Join :v_TablaRequisitos As TabReq 
		      On TabReq."CodigoError" = ErrDet."Codigo"
		     And TabReq."IdMovimientoComprobante" = ErrDet."IdInterno"
		 Where "Cumple" = 0
		  And "Prioridad" > 1; 
      	
  		DELETE FROM "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobante_IN_MovimientoComprobante";

		  
    END; --End Validaciones	
END;--End Principal
