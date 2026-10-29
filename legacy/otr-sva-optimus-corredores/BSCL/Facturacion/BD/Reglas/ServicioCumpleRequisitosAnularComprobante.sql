SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosAnularComprobante";
GO
CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosAnularComprobante"
  (
     IN p_IdEmpresa 		    BIGINT,
     
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

        /*p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle;	*/
		Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_OUT_ErroresDetalle"
		Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
		                        				
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	--p_out_ErroresDetalle	= SELECT 0 As "IdComprobante", '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	--p_out_MovimientosRech	= SELECT 0 AS "IdComprobante" FROM dummy WHERE 1=2;
	
	BEGIN --Validaciones
       DECLARE v_Obligatorios			NVARCHAR(1000);
	   --DECLARE v_Indicadores			NVARCHAR(250);
	   DECLARE v_countTableFormaPago	BIGINT;
	   DECLARE v_error_General			BIGINT;
	   DECLARE v_error_Negocio			BIGINT;	   
	   DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	   DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato1" NVARCHAR(100), "ValorDato1" NVARCHAR(520), "CodigoDato2" NVARCHAR(100), "ValorDato2" NVARCHAR(520), 
	                                           "IdComprobante" BIGINT, "Entidad" NVARCHAR(250));			
		
	   DECLARE v_LocalSistema           NVARCHAR(40) = 'CL';
	   
	   DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
	   BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
	   END;

		v_TablaCodigosMensajes	= SELECT 0  As "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1", '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2", 
										  0 AS "IdComprobante",'' as "Entidad" FROM dummy WHERE 1=2;
		

		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		v_Obligatorios := 'IdComprobante, IdTipoComprobante';
		
		--Obtiene localizacion
		SELECT "Valor"
		INTO v_LocalSistema
        FROM "BSCL.Plataforma.BD::PLA.Configuracion"
        WHERE "CodigoParametro" = 'PLA.LocalizacionSistema'
          AND "IdEmpresa" = :p_IdEmpresa;
          
          
          
		--Valida que existan movimientos 
        v_TablaRequisitos = Select 1 As "Prioridad", 
				       	            Case 
				       		           When Count("IdComprobante") < 1 THEN 0 
				       		        Else 1 
						            End As "Cumple", 
				       	            'FAC_NO_VIENEN_MOVIMIENTOS' AS "CodigoError", 
				       	            '' As "CodigoDato1",
				       	            '' As "ValorDato1",
				       	            '' As "CodigoDato2",
				       	            '' As "ValorDato2",
				       	            0 as "IdComprobante",
				       	            '' as "Entidad"
		                    --From :p_ComprobanteAAnular As CmbteAAnu Union All 
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_IN_Comprobante" As CmbteAAnu Union All 
		                    SELECT "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "CodigoDato2", "ValorDato2","IdComprobante","Entidad" 
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
		       			           CmbteAAnu."IdComprobante",
		       			           'AnularComprobante' As "Entidad"
		                    --From :p_ComprobanteAAnular As CmbteAAnu Union All
                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_IN_Comprobante" As CmbteAAnu Union All
		                    Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                           "CodigoDato2", "ValorDato2","IdComprobante","Entidad" 
		                    From :v_TablaRequisitos;		  
		                    
		 --Valida los campos obligatorios, deben existir
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 0
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 0
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') IN ('NCE', 'NCR', 'NEO') Then 0
                                      When Coalesce(Cmbte."EsVentaCorta",'') = 'S' AND Prestamo."CantidadAsignada" > 0 THEN 0
                                    Else 1
                                    End As "Cumple",                                    
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 'CMBTE_FACTURACION_NO_EXISTE'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'CMBTE_TIPO_COMPROBANTE_NO_EXISTE'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') IN ('NCE', 'NCR', 'NEO') THEN 'CMBTE_TIPO_COMPROBANTE_ANULAR_NOTA_CREDITO'
                                      When Coalesce(Cmbte."EsVentaCorta",'') = 'S' AND Prestamo."CantidadAsignada" > 0 THEN 'CMBTE_COMPROBANTE_ANULAR_VENTA_CORTA'
                                    Else ''
                                    End As "CodigoError",                                                                      
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then 'IdComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdTipoComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') IN ('NCE', 'NCR', 'NEO') THEN 'IdComprobante'
                                      When Coalesce(Cmbte."EsVentaCorta",'') = 'S' AND Prestamo."CantidadAsignada" > 0 THEN 'IdComprobante'
                                    Else ''
                                    End As "CodigoDato1", 
                                    Case
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then Cast(CmbteAAnu."IdComprobante" As Nvarchar(12))
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then CmbteAAnu."IdTipoComprobante"
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') IN ('NCE', 'NCR', 'NEO') Then Cast(CmbteAAnu."IdComprobante" As Nvarchar(12))
                                      When Coalesce(Cmbte."EsVentaCorta",'') = 'S' AND Prestamo."CantidadAsignada" > 0 THEN Cast(CmbteAAnu."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1", 
                                    Case 
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then ''
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then 'IdComprobante'
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') IN ('NCE', 'NCR', 'NEO') Then ''
                                      When Coalesce(Cmbte."EsVentaCorta",'') = 'S' AND Prestamo."CantidadAsignada" > 0 THEN ''
                                    Else ''
                                    End As "CodigoDato2",
                                    Case 
                                      When Coalesce(Cmbte."IdComprobante",0) = 0 Then ''
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') = '' Then Cast(CmbteAAnu."IdComprobante" As Nvarchar(12))
                                      When Coalesce(TipCmbte."IdTipoComprobante",'') IN ('NCE', 'NCR', 'NEO') Then ''
                                      When Coalesce(Cmbte."EsVentaCorta",'') = 'S' AND Prestamo."CantidadAsignada" > 0 THEN ''
                                    Else ''
                                    End As "ValorDato2",                                                                      
		       	                    Cmbte."IdComprobante",
		       	                    'AnularComprobante' As "Entidad"		       	                          
		       	             --From :p_ComprobanteAAnular As CmbteAAnu 
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_IN_Comprobante" As CmbteAAnu
		       	               Left Join "BSCL.Facturacion.BD::FAC.Comprobante" As Cmbte 
		       	                 On Cmbte."IdComprobante" = CmbteAAnu."IdComprobante" 
		       	                And Cmbte."IdCliente" = CmbteAAnu."IdCliente"
		       	                And Cmbte."IdPortafolio" = CmbteAAnu."IdPortafolio"
		       	                And Cmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante"
		       	               Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte  
		       	                 On TipCmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante"
		       	               LEFT JOIN (
		       	               			SELECT C."IdTipoComprobante",C."FolioComprobante", P."CantidadAsignada"
		       	               			FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamo" AS P
		       	               			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS C ON C."FolioCartera" = P."FolioCarteraPrestamo"
		       	               			WHERE C."IdEstado" = 'VIG'
		       	               		     ) as Prestamo
		       	               	 ON Prestamo."IdTipoComprobante" = Cmbte."IdTipoComprobante" AND Prestamo."FolioComprobante" = Cmbte."IdComprobante"
		       	             Union All
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2", "ValorDato2","IdComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		                     
		 --Valida los campos no obligatorios, reglas de negocio 
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'CL' Then 0
                                      When ConsCmbte."IdEstadoComprobante" = 'ANU' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('POL') Then 0 
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('FEE', 'FAE', 'FOE', 'FOX') Then 0
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'CL' Then 'CMBTE_DIS_FAC'
                                      When ConsCmbte."IdEstadoComprobante" = 'ANU' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('POL') Then 'CMBTE_EST_ANU'
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('FEE', 'FAE', 'FOE', 'FOX') Then 'CMBTE_DIS_FAC'
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'CL' Then 'IdComprobante'
                                      When ConsCmbte."IdEstadoComprobante" = 'ANU' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('POL') Then 'IdComprobante'
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('FEE', 'FAE', 'FOE', 'FOX') Then 'IdComprobante'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'CL' Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                      When ConsCmbte."IdEstadoComprobante" = 'ANU' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('POL') Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                      When ConsCmbte."IdEstadoComprobante" <> 'FAC' AND v_LocalSistema = 'PE' AND ConsCmbte."IdTipoComprobante" IN ('FEE', 'FAE', 'FOE', 'FOX') Then Cast(ConsCmbte."IdComprobante" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",
                                    '' As "CodigoDato2",
                                    '' As "ValorDato2",
		       	                    CmbteAAnu."IdComprobante",
		       	                    'AnularComprobante' As "Entidad"
		       	             --From :p_ComprobanteAAnular As CmbteAAnu		       	             
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_IN_Comprobante" As CmbteAAnu
		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
		       	                   On ConsCmbte."IdComprobante" = CmbteAAnu."IdComprobante"
		       	                  And ConsCmbte."IdPortafolio" = CmbteAAnu."IdPortafolio"
                                  And ConsCmbte."IdCliente" = CmbteAAnu."IdCliente"
                                  And ConsCmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante" 
                             Union ALL
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2", "ValorDato2","IdComprobante","Entidad" 
		                     From :v_TablaRequisitos;

		--Valida si ya tiene autorizaciones creadas. 
         v_TablaRequisitos = Select 2 as "Prioridad",
                                    Case 
                                      When Evento."IdEstado" = 'VIG' Then 0                                      
                                    Else 1
                                    End As "Cumple",
                                    Case 
                                      When Evento."IdEstado" = 'VIG' Then 'AUTORIZACION_PENDIENTE_APROBACION'
                                    Else ''
                                    End As "CodigoError",
                                    Case
                                      When Evento."IdEstado" = 'VIG' Then 'Id'
                                    Else ''
                                    End As "CodigoDato1",
                                    Case 
                                      When Evento."IdEstado" = 'VIG' Then Cast(Evento."IdEvento" As Nvarchar(12))
                                    Else ''
                                    End As "ValorDato1",
                                    '' As "CodigoDato2",
                                    '' As "ValorDato2",
		       	                    Evento."IdEvento" AS "IdComprobante",
		       	                    'AnularComprobante' As "Entidad"
		       	             --From :p_ComprobanteAAnular As CmbteAAnu		       	             
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_IN_Comprobante" As CmbteAAnu
/*		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
		       	                   On ConsCmbte."IdComprobante" = CmbteAAnu."IdComprobante"
		       	                  And ConsCmbte."IdPortafolio" = CmbteAAnu."IdPortafolio"
                                  And ConsCmbte."IdCliente" = CmbteAAnu."IdCliente"
                                  And ConsCmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante"*/
                                Inner JOIN "BSCL.Plataforma.BD::AUT.Evento" AS Evento
                                   ON Evento."NumeroOperacionConcepto" = Cast(CmbteAAnu."IdComprobante" As Nvarchar(40))
                              Union ALL
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2", "ValorDato2","IdComprobante","Entidad" 
		                     From :v_TablaRequisitos;
		                     
	     
		 --Valida periodo cerrado
		 If v_LocalSistema = 'PE' Then
		   v_TablaRequisitos = Select 2 as "Prioridad",
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
                                    '' As "CodigoDato2",
                                    '' As "ValorDato2",
		       	                    CmbteAAnu."IdComprobante",
		       	                    'AnularComprobante' As "Entidad"		       	             
                             From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_IN_Comprobante" As CmbteAAnu
		       	                Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
		       	                   On ConsCmbte."IdComprobante" = CmbteAAnu."IdComprobante"
		       	                  And ConsCmbte."IdPortafolio" = CmbteAAnu."IdPortafolio"
                                  And ConsCmbte."IdCliente" = CmbteAAnu."IdCliente"
                                  And ConsCmbte."IdTipoComprobante" = CmbteAAnu."IdTipoComprobante"
                                Left Join "BSCL.Facturacion.BD::FAC.PeriodoCierre" AS PeriodoCierre
                                   On Substring(PeriodoCierre."Periodo", 1, 2) || '/' || Substring(PeriodoCierre."Periodo", 4, 4) = CASE
                                                                                                                                      WHEN Length (MONTH (ConsCmbte."FechaComprobante")) < 2 THEN Concat('0', MONTH (ConsCmbte."FechaComprobante"))
                                                                                                                                    ELSE Cast(MONTH (ConsCmbte."FechaComprobante") AS nvarchar(2))
                                                                                                                                    END || '/' || Year(ConsCmbte."FechaComprobante") 
                                  And "IdEstado" = 'VIG' AND "IdEstadoPeriodo" = 'CER'
                             Union ALL
		                     Select "Prioridad","Cumple","CodigoError","CodigoDato1","ValorDato1",
		                            "CodigoDato2", "ValorDato2","IdComprobante","Entidad" 
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
	   							   When :v_error_Negocio > 0 THEN 'RECH'
	   						     Else 'OK'
	   							 End;
	   							 
	     /*p_out_MovimientosRech = Select Distinct "IdComprobante"
	                             From :v_TablaRequisitos
	                             Where "Prioridad" >= 1
	                               And "Cumple" = 0;*/
	                               
	     Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_OUT_ComprobantesRechazados"                          
	     Select Distinct "IdComprobante"
	     From :v_TablaRequisitos
	     Where "Prioridad" >= 1
	       And "Cumple" = 0;
	                               
	     v_TablaCodigosMensajes = Select 	"IdComprobante" As "IdInterno", 
	     									"CodigoError" 	As "Codigo",
	                                     	"CodigoDato1", 
	                                     	"ValorDato1", 
	                                     	"CodigoDato2", 
	                                     	"ValorDato2" 
	                              From :v_TablaRequisitos
		                          Where "Cumple" = 0
		                          Order By "Prioridad" Desc;                          


         Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_ErroresDetalle);
		 
		 /*p_out_ErroresDetalle = Select "IdComprobante", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		  	                    From :v_ErroresDetalle As ErrDet
		                          Inner Join :v_TablaRequisitos As TabReq 
		                             On TabReq."CodigoError" = ErrDet."Codigo"
		                            And TabReq."IdComprobante" = ErrDet."IdInterno"
		                        Where "Cumple" = 0;*/
		 Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAnularComprobante_OUT_ErroresDetalle"
		 Select "IdComprobante", ErrDet."Codigo" As "Codigo", ErrDet."Descripcion"
		 From :v_ErroresDetalle As ErrDet
		   Inner Join :v_TablaRequisitos As TabReq 
		      On TabReq."CodigoError" = ErrDet."Codigo"
		     And TabReq."IdComprobante" = ErrDet."IdInterno"
		 Where "Cumple" = 0;
		 
      
    END; --End Validaciones	
END;--End Principal

GO