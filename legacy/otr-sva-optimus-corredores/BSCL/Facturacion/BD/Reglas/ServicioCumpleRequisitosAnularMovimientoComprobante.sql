SET SCHEMA "BSCL";
GO   
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosAnularMovimientoComprobante";
GO
CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosAnularMovimientoComprobante"
  (
     IN p_IdEmpresa BIGINT,
     IN p_MovimientoAAnular table("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT),  
		                              				                      
     OUT p_out_MovimientosRech	Table ("IdMovimientoComprobante" BIGINT),
	
     -- Salidas para mensajes de errores 
	 OUT p_out_ErroresDetalle  Table ("IdMovimientoComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
	 
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

        p_out_ErroresDetalle = Select "IdInterno" As "IdMovimientoComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle;
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT 0 As "IdMovimientoComprobante", '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT 0 AS "IdMovimientoComprobante" FROM dummy WHERE 1=2;
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   --DECLARE v_Indicadores			NVARCHAR(250);
	   DECLARE v_countTableFormaPago	BIGINT;
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;
	   DECLARE v_LocalSistema           NVARCHAR(40) = 'CL';
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100), 
											   "ValorDato1" NVARCHAR(520), "IdMovimientoComprobante" BIGINT, "Entidad" NVARCHAR(250));			
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;
		
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1", 
										  0 AS "IdMovimientoComprobante",'' as "Entidad" FROM dummy WHERE 1=2;
		
										  		--Obtiene localizacion
		SELECT "Valor"
		INTO v_LocalSistema
        FROM "BSCL.Plataforma.BD::PLA.Configuracion"
        WHERE "CodigoParametro" = 'PLA.LocalizacionSistema'
          AND "IdEmpresa" = :p_IdEmpresa;
          

		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
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
				       	            0 as "IdMovimientoComprobante",
				       	            '' as "Entidad"
		                    From :p_MovimientoAAnular As MvtoCmbteAAnu Union All 
		                    SELECT "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "IdMovimientoComprobante","Entidad" 
		                    From :v_TablaRequisitos;
		                    
		--Valida los campos obligatorios, no deben estar vacios
        v_TablaRequisitos = Select 2 as "Prioridad",
                                   Case 
		       		                 When Length("IdMovimientoComprobante") < 1 Then 0
		       			           Else 1
		       			           End As "Cumple",
		       			           'PARAMETROS_OBLIGATORIOS' As "CodigoError",
		       			           'CodigoParametros' As "CodigoDato1",
		       			           v_Obligatorios As "ValorDato1",
		       			           MvtoCmbteAAnu."IdMovimientoComprobante",
		       			           'AnularMovimientoComprobante' As "Entidad"
		                    From :p_MovimientoAAnular As MvtoCmbteAAnu Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "IdMovimientoComprobante","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(MvtoCmbteAAnu."IdMovimientoComprobante",0) = 0 Then 0
                                    Else 1
                                    End As "Cumple",                                    
                                    Case
                                      When Coalesce(MvtoCmbteAAnu."IdMovimientoComprobante",0) = 0 Then 'CMBTE_MOVIMIENTO_FACTURACION_NO_EXISTE'
                                    Else ''
                                    End As "CodigoError",                                                                      
                                    Case
                                      When Coalesce(MvtoCmbteAAnu."IdMovimientoComprobante",0) = 0 Then 'IdMovimientoComprobante'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(MvtoCmbteAAnu."IdMovimientoComprobante",0) = 0 Then Cast(MvtoCmbteAAnu."IdMovimientoComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",                                                                       
		       	                    MvtoCmbteAAnu."IdMovimientoComprobante",
		       	                    'AnularMovimientoComprobante' As "Entidad"		       	                          
		       	             From :p_MovimientoAAnular As MvtoCmbteAAnu 
		       	               Left Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte 
		       	                 On MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteAAnu."IdMovimientoComprobante" 
		       	                And MvtoCmbte."IdCliente" = MvtoCmbteAAnu."IdCliente"
		       	                And MvtoCmbte."IdPortafolio" = MvtoCmbteAAnu."IdPortafolio" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		                     
		 --Valida los campos no obligatorios, reglas de negocio 
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' AND ConsMvtoCmbte."IdEstadoFacturacion" <> 'ANU' Then 0                                      
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' AND ConsMvtoCmbte."IdEstadoFacturacion" <> 'ANU' Then 'MVTO_CMBTE_DIS_ING'                                      
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' AND ConsMvtoCmbte."IdEstadoFacturacion" <> 'ANU' Then 'IdMovimientoComprobante'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When ConsMvtoCmbte."IdEstadoFacturacion" <> 'ING' AND ConsMvtoCmbte."IdEstadoFacturacion" <> 'ANU' Then Cast(ConsMvtoCmbte."IdMovimientoComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",
		       	                    MvtoCmbteAAnu."IdMovimientoComprobante",
		       	                    'AnularMovimientoComprobante' As "Entidad"
		       	             From :p_MovimientoAAnular As MvtoCmbteAAnu 		       	             
		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" As ConsMvtoCmbte
		       	                   On ConsMvtoCmbte."IdMovimientoComprobante" = MvtoCmbteAAnu."IdMovimientoComprobante"
		       	                  And ConsMvtoCmbte."IdPortafolio" = MvtoCmbteAAnu."IdPortafolio"
                                  And ConsMvtoCmbte."IdCliente" = MvtoCmbteAAnu."IdCliente" Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos; 		       	                  
                           	 
		 --Valida periodo cerrado
		 If v_LocalSistema = 'PE' Then
		   v_TablaRequisitos = Select 1 as "Prioridad",
                                    Case 
                                      When Coalesce(PeriodoCierre."Periodo",'') <> ''  Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When Coalesce(PeriodoCierre."Periodo",'') <> '' Then 'CMBTE_PERIODO_CERRADO'
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When Coalesce(PeriodoCierre."Periodo",'') <> '' Then 'Periodo'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When Coalesce(PeriodoCierre."Periodo",'') <> '' Then Cast(PeriodoCierre."Periodo" As  Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",
		       	                    MovtoCmbteAAnu."IdMovimientoComprobante",
		       	                    'AnularComprobante' As "Entidad"		       	             
                             From :p_MovimientoAAnular As MovtoCmbteAAnu
		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" As ConsMovCmbte
		       	                   On ConsMovCmbte."IdMovimientoComprobante" = MovtoCmbteAAnu."IdMovimientoComprobante"
		       	                  And ConsMovCmbte."IdPortafolio" = MovtoCmbteAAnu."IdPortafolio"
                                  And ConsMovCmbte."IdCliente" = MovtoCmbteAAnu."IdCliente"
                                Left Join "BSCL.Facturacion.BD::FAC.PeriodoCierre" AS PeriodoCierre
                                   On Substring(PeriodoCierre."Periodo", 1, 2) || '/' || Substring(PeriodoCierre."Periodo", 4, 4) = CASE
                                                                                                                                      WHEN Length (MONTH (ConsMovCmbte."FechaMovimientoComprobante")) < 2 THEN Concat('0', MONTH (ConsMovCmbte."FechaMovimientoComprobante"))
                                                                                                                                    ELSE Cast(MONTH (ConsMovCmbte."FechaMovimientoComprobante") AS nvarchar(2))
                                                                                                                                    END || '/' || Year(ConsMovCmbte."FechaMovimientoComprobante") 
                                  And "IdEstado" = 'VIG' AND "IdEstadoPeriodo" = 'CER'
                             Union ALL
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "IdMovimientoComprobante","Entidad" 
		                     From :v_TablaRequisitos;                   
	     End If;
              	     	                  
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
	   							 
	     p_out_MovimientosRech = Select Distinct "IdMovimientoComprobante"
	                             From :v_TablaRequisitos
	                             Where "Prioridad" >= 1
	                               And "Cumple" = 0;
	                               
	     v_TablaCodigosMensajes = Select "IdMovimientoComprobante" As "IdInterno", "CodigoError" As "Codigo",
	                                     "CodigoDato1", "ValorDato1",  '' As "CodigoDato2", '' As "ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                 

		 Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 p_out_ErroresDetalle = Select "IdMovimientoComprobante", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		  	                    From :v_ErroresDetalle As ErrDet
		                          Inner Join :v_TablaRequisitos As TabReq 
		                             On TabReq."CodigoError" = ErrDet."Codigo"
		                            And TabReq."IdMovimientoComprobante" = ErrDet."IdInterno"
		                        Where "Cumple" = 0;
      
    END; --End Validaciones	
END;--End Principal
