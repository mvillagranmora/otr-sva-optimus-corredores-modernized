SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::DteEnviarLibro";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::DteEnviarLibro"
  (
     -- Parametros de Validacion
     IN p_IdEmpresa BIGINT,  
     IN p_IdUsuario BIGINT,   
     IN p_IdModulo  NVARCHAR(3), 
     
     IN p_simularOperacion    NVARCHAR(1) ,	-- S/N se usa para simular operacion (algunos casos)                  
     IN p_UsaReglasNegocio    NVARCHAR(1) ,	-- S/N se usa en caso de servicios anidados. 
     IN p_EsIngresoAutomatico NVARCHAR(1) Default 'N',
     IN p_PeriodoContable     NVARCHAR(10),
         
     
     --Salidas para mensajes de advertencias y errores
     OUT p_out_InformacionContador  	 INT,
     OUT p_out_AdvertenciasContador      INT,
     OUT p_out_ErroresContador           INT   
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 

AS
BEGIN
    --------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT)
            Default Select 0 As "IdComprobante" From dummy where 1=2;
	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
        Select Count(1)
        Into p_out_ErroresContador
        From "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle"; 
		
        Select Count(1) 
        Into p_out_AdvertenciasContador 
        From "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_AdvertenciasDetalle";
         
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle"
		Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo", "Descripcion" As "Descripcion" 
		From :v_out_hash;
							
		
		Select Count(1) 
		Into p_out_ErroresContador
		From "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle";
		
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
           
            Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle"
            Select 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE 
            From dummy;
            
             								   				
			Select Count(1)
			Into p_out_ErroresContador 
			From "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle";
												
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	
	
	p_out_InformacionContador  := 0;
	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador	   := 0;
	
	
	Begin --Logica
       --------------------
	   --Declare Variables
	   --------------------
       DECLARE v_IdTareaDTEGrupo, v_out_AdvertenciasContador, v_out_ErroresContador, Cant_Cmte, Cant_ComprobanteEnviado, 
               Cant_Cbmte BIGINT := 0; 
       
       DECLARE p_TablaParametros TABLE ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100)) 
               Default Select '' As "NombreParametro", '' As "Valor" from dummy where 1=2;
       
       /*DECLARE v_CmbteADTE TABLE ("IdTareaDTE" BIGINT, "IdTareaDTEGrupo" BIGINT, "TipoTareaDTE" NVARCHAR(40), "IdRespuestaDTE" BIGINT, "RespuestaDTEEnvioSII" NVARCHAR(250), "RespuestaLocationDTE" NVARCHAR(40), "RespuestaPdfDTE" NVARCHAR(40),
                                  "RespuestaXmlDTE" NVARCHAR(40), "FolioComprobanteRespuestaDTE" BIGINT, "IdComprobante" BIGINT, "IdEstadoTareaDTE" NVARCHAR(3), "IdEstadoDTE" NVARCHAR(3), "IdEstadoSII" NVARCHAR(3), "IdTipoComprobante" NVARCHAR(3), 
                                  "FolioComprobante" BIGINT, "TipoComprobanteDTE" BIGINT, "FechaComprobante" NVARCHAR(10), "FechaVencimiento" NVARCHAR(10), "MontoNoFacturable" DECIMAL(26,8), "MontoNeto" DECIMAL(26,8), "MontoExento" DECIMAL(26,8), 
                                  "TasaIva" DECIMAL(26,8), "MontoIva" DECIMAL(26,8), "MontoTotal" DECIMAL(26,8), "ValorAPagar" DECIMAL(26,8), "DocumentoIdentidadEmisor" NVARCHAR(40), "RazonSocialEmisor" NVARCHAR(100), "GiroEmisor" NVARCHAR(100), 
                                  "DireccionEmisor" NVARCHAR(100), "ComunaEmisor" NVARCHAR(40), "CiudadEmisor" NVARCHAR(40), "EmailEmisor" NVARCHAR(40), "TelefonoEmisor" NVARCHAR(40), "EmailReceptor" NVARCHAR(40), "DocumentoIdentidadReceptor" NVARCHAR(40), 
                                  "RazonSocialReceptor" NVARCHAR(100), "GiroReceptor" NVARCHAR(100), "DireccionReceptor" NVARCHAR(100), "ComunaReceptor" NVARCHAR(40), "CiudadReceptor" NVARCHAR(40), "ContactoReceptor" NVARCHAR(40), "TipoReferenciaIdComprobante" NVARCHAR(40), 
                                  "FolioReferenciaIdComprobante" BIGINT, "FechaReferenciaIdComprobante" NVARCHAR(10), "RazonReferenciaIdComprobante" NVARCHAR(40), "TipoReferenciaFolioComprobante" NVARCHAR(40), "FolioReferenciaFolioComprobante" BIGINT, "FechaReferenciaFolioComprobante" NVARCHAR(10),
                                  "RazonReferenciaFolioComprobante" NVARCHAR(40), "IdComprobanteDetalleGlosa" NVARCHAR(40), "IdComprobanteDetalleDescripcion" NVARCHAR(40), "IdComprobanteDetalleNumero" SMALLINT, "IdComprobanteDetalleCantidad" DECIMAL(26,8), "IdComprobanteDetallePrecio" DECIMAL(26,8), "IdComprobanteDetallePorcentaje" DECIMAL(26,8), 
                                  "IdComprobanteDetalleMontoDescuento" DECIMAL(26,8), "IdComprobanteDetalleMonto" DECIMAL(26,8), "IdComprobanteDetalleEsExento" NVARCHAR(1))
               Default Select 0 As "IdTareaDTE", 0 As "IdTareaDTEGrupo", '' As "TipoTareaDTE", 0 As "IdRespuestaDTE", '' As "RespuestaDTEEnvioSII", '' As "RespuestaLocationDTE", '' As "RespuestaPdfDTE",
                              '' As "RespuestaXmlDTE", 0 As "FolioComprobanteRespuestaDTE", 0 As "IdComprobante", '' As "IdEstadoTareaDTE", '' As "IdEstadoDTE", '' As "IdEstadoSII", '' As "IdTipoComprobante", 
                              0 As "FolioComprobante", 0 As "TipoComprobanteDTE", '' As "FechaComprobante", '' As "FechaVencimiento", 0 As "MontoNoFacturable", 0 As "MontoNeto", 0 As "MontoExento", 
                              0 As "TasaIva", 0 As "MontoIva", 0 As "MontoTotal", 0 As "ValorAPagar", '' As "DocumentoIdentidadEmisor", '' As "RazonSocialEmisor", '' As "GiroEmisor", 
                              '' As "DireccionEmisor", '' As "ComunaEmisor", '' As "CiudadEmisor", '' As "EmailEmisor", '' As "TelefonoEmisor", '' As "EmailReceptor", '' As "DocumentoIdentidadReceptor", 
                              '' As "RazonSocialReceptor", '' As "GiroReceptor", '' As "DireccionReceptor", '' As "ComunaReceptor", '' As "CiudadReceptor", '' As "ContactoReceptor", '' As "TipoReferenciaIdComprobante", 
                              0 As "FolioReferenciaIdComprobante", '' As "FechaReferenciaIdComprobante", '' As "RazonReferenciaIdComprobante", '' As "TipoReferenciaFolioComprobante", 0 As "FolioReferenciaFolioComprobante", '' As "FechaReferenciaFolioComprobante",
                              '' As "RazonReferenciaFolioComprobante", '' As "IdComprobanteDetalleGlosa", '' As "IdComprobanteDetalleDescripcion", 0 As "IdComprobanteDetalleNumero" , 0 As "IdComprobanteDetalleCantidad", 0 As "IdComprobanteDetallePrecio", 0 As "IdComprobanteDetallePorcentaje", 
                              0 As "IdComprobanteDetalleMontoDescuento", 0 As "IdComprobanteDetalleMonto", '' As "IdComprobanteDetalleEsExento" from dummy where 1=2;
*/

      
       		
		
	   -----------------------------------------------------
	   --Validaciones
	   -----------------------------------------------------
       If p_UsaReglasNegocio = 'S' Then
		 BEGIN
            If p_EsIngresoAutomatico = 'S' Then
			   Call "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalleReqBas,:v_resultadoRegla);
			   If :v_resultadoRegla = 'RECH' Then
				  SIGNAL SALIDA_CONTROLADA; 	
			   End If;	   
		                  
            End If;
            
            
            /** Validacion Facturación **/
            Insert into "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_IN_Comprobante"
            Select "IdPortafolio", "IdCliente", "IdComprobante", "IdTipoComprobante"
            From "BSCL.Facturacion.BD::GBL.DteEnviarLibro_IN_Comprobante";
            
             
            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosDteEnviarLibro"(:v_resultadoRegla);
		    If :v_resultadoRegla = 'RECH' Then	
		        Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ResultadoProceso"
		        Select "IdComprobante" As "Campo", "IdComprobante" as "Id",
		               'BSCL.Facturacion.BD.Servicios::DteEnviarLibro' as "Servicio",
		               'RECH' as "Resultado"
		        From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ComprobantesRechazados";
		         
			  	
			    
			    Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle"
			    Select "IdComprobante", "Codigo","Descripcion" 
			    From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ErroresDetalle";
                
			    SIGNAL SALIDA_CONTROLADA; 	
		    Else
		      --If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
			     /*p_out_AdvertenciasDetalle = Select "IdComprobante", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
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
        v_Comprobante = Select "IdPortafolio", "IdCliente", "IdComprobante", "IdTipoComprobante"
                        From "BSCL.Facturacion.BD::GBL.DteEnviarLibro_IN_Comprobante" As Cmbte
                        Where Cmbte."IdComprobante" Not In (Select "IdComprobante" 
                                                            From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ComprobantesRechazados");
        
        
        
        Select Count("IdComprobante")
        Into Cant_Cbmte
        From :v_Comprobante;
        
        If Cant_Cbmte <=0 Then
           p_out_ErroresContador := p_out_ErroresContador + 1;
		                          
	       Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle" 
	       Select 0 As "IdComprobante", '' As "Codigo",
	              'Los comprobantes enviados presentan problemas' As "Descripcion" 
	       From dummy;
	                              
           SIGNAL SALIDA_CONTROLADA;
        End If;
        
                
        /** Inicio Preparacion Envio a DTE **/    
        --Obtiene Id Grupo DTE
        Select "BSCL.Facturacion.BD.Secuencias::TareaDTEGrupo".Nextval
        Into v_IdTareaDTEGrupo 
        From dummy;
        
        --Filtros parametros para servicio agregar programa
        p_TablaParametros = Select 'IdTareaDTEGrupo' As "NombreParametro", Cast(v_IdTareaDTEGrupo As nvarchar(100)) As "Valor" 
                            From dummy; 
            
            
        --Tabla Temporal para usar en Job
        --Obtiene detalle de comprobantes
        v_CmbteDetADTE = Select ConsCmbte."IdComprobante", 
                                "FolioComprobante",
                                Max(ConsCmbte."IdTipoComprobante") As "IdTipoComprobante",
                                --Max("AccionTipoOperacion") As "AccionTipoOperacion",
                                'VENTA' As "AccionTipoOperacion",
		                        TipCmbte."TipoComprobanteDTE" As "TipoDocumento",
		                        ConsCmbte."FolioComprobante" As "FolioDocumento",
		                        Max(ConsCmbte."FechaComprobante") As "FechaDocumento",
		                        Max(ConsCmbte."DocumentoIdentidad") As "RutDocumento",
		                        Max(ConsCmbte."NombreLargoCliente") As "RazonSocial",
		                        '96535530-8' As "DocumentoIdentidadEmisor",
		                        (SubString(p_PeriodoContable,1,4) || '/' || SubString(p_PeriodoContable,6,2) || '/01')  As "PeriodoContable",
		                        Sum(ConsCmbteCob."MontoLocalCobro") As "MontoNeto",
		                        0 As "MontoExento",
		                        ((Sum(ConsCmbteCob."MontoLocalCobro") * 19) /100) As "MontoIva",
		                        (Sum(ConsCmbteCob."MontoLocalCobro") + ((Sum(ConsCmbteCob."MontoLocalCobro") * 19) /100)) As "MontoTotal",
		                        (Max(ConsCmbte."MontoComprobante") + Sum(ConsCmbteCob."MontoLocalCobro") + ((Sum(ConsCmbteCob."MontoLocalCobro") * 19) /100)) As "ValorAPagar",
		                        Max("IdRespuestaDTE") As "IdRespuestaDTE",
		                        Max("RespuestaDTEEnvioSII") As "RespuestaDTEEnvioSII",
		                        Max("RespuestaLocationDTE") As "RespuestaLocationDTE",
		                        Max("RespuestaPdfDTE") As "RespuestaPdfDTE",
		                        Max("RespuestaXmlDTE") As "RespuestaXmlDTE",
		                        Max("IdEstadoDTE") As "IdEstadoDTE",
		                        Max("IdEstadoSII") As "IdEstadoSII"
                         From "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
                           Inner Join :v_Comprobante As Cmbte 
                              On Cmbte."IdComprobante" = ConsCmbte."IdComprobante"
                           Left join "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" As ConsCmbteCob
                              On ConsCmbteCob."IdComprobante" = ConsCmbte."IdComprobante"
                           Inner Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte
                              On TipCmbte."IdTipoComprobante" = ConsCmbte."IdTipoComprobante"     
                           Group By ConsCmbte."IdComprobante", TipCmbte."TipoComprobanteDTE", ConsCmbte."FolioComprobante";
                      
                      
        /*Select 'v_CmbteDetADTE', *
        from :v_CmbteDetADTE;*/        
        
        --Obtiene totales para job  
        v_CmbteTotalesADTE = Select TipCmbte."TipoComprobanteDTE" As "TipoDocumento", 
                                    Count(ConsCmbte."IdComprobante") As "CantidadDocumentos",
                                    0 As "CantidadAnulados",
                                    Sum("MontoNeto") As "TotalMontoNeto",
                                    0 As "TotalMontoExento",
                                    Sum("MontoIva") As "TotalMontoIva",
                                    Sum("MontoTotal") As "TotalMontoTotal"
                             From "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
                               Inner Join :v_CmbteDetADTE As Cmbte 
                                  On Cmbte."IdComprobante" = ConsCmbte."IdComprobante"
                               Inner Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte
                                  On TipCmbte."IdTipoComprobante" = ConsCmbte."IdTipoComprobante" 
                             Group By TipCmbte."TipoComprobanteDTE";
                             
        /*Select 'v_CmbteTotalesADTE', *
        From :v_CmbteTotalesADTE;*/  
        
        
        

        /** Registra libro dte**/
        v_LibroDte = Select "BSCL.Facturacion.BD.Secuencias::LibroDte".Nextval As "IdLibro",
                            'VENTA' As "Tipo", 
                            "PeriodoContable" As "PeriodoContable", 
                            "DocumentoIdentidadEmisor" As "DocumentoIdentidadEmpresa"
                     From :v_CmbteDetADTE As Cmbte
                     Group By "AccionTipoOperacion", "PeriodoContable", "DocumentoIdentidadEmisor";
                          
            
        /*select 'v_LibroDte', *
        from :v_LibroDte;*/
        
        
        v_CmbteLibroDte = Select "BSCL.Facturacion.BD.Secuencias::LibroDteComprobante".Nextval As "IdLibroDteComprobante",
                                 LibDte."IdLibro" As "IdLibro",
                                 Cmbte."IdComprobante" As "IdComprobante"                                 
                          From :v_CmbteDetADTE As Cmbte
                            Inner Join :v_LibroDte As LibDte
                               On LibDte."PeriodoContable" =  Cmbte."PeriodoContable"
                              And LibDte."DocumentoIdentidadEmpresa" = Cmbte."DocumentoIdentidadEmisor"
                          Group By "IdLibro", "IdComprobante";
        
        
        /*select 'v_CmbteLibroDte', *
        from :v_CmbteLibroDte;*/ 
        
                         
        
        --Genera json a enviar a dte
        v_CmbteADTE = Select "BSCL.Facturacion.BD.Secuencias::TareaDTE".Nextval As "IdTareaDTE",
                             v_IdTareaDTEGrupo As "IdTareaDTEGrupo",
                             'DTELIB' As "TipoTareaDTE",
                             0 As "IdRespuestaDTE",
                             '' As "RespuestaDTEEnvioSII",
                             '' As "RespuestaLocationDTE",
                             '' As "RespuestaPdfDTE",
                             '' As "RespuestaXmlDTE",
                             "FolioComprobante" As "FolioComprobanteRespuestaDTE", 
                             "IdComprobante", 
                             'PEN' As "IdEstadoTareaDTE",
                             '' As "IdEstadoDTE",
                             '' As "IdEstadoSII",
                             "IdTipoComprobante",
                             Cmbte."TipoDocumento" As "TipoComprobanteDTE",
                             "AccionTipoOperacion",
                             Cmbte."PeriodoContable",
                             "FechaDocumento" As "FechaComprobante",
                             "FechaDocumento" As "FechaVencimiento",
                             '' As "CodigoVendedor",
                             
                             0 As "IdComprobanteMontoNoFacturable",
                             Coalesce("MontoNeto",0) As "IdComprobanteMontoNeto",
                             Coalesce("MontoExento",0) As "IdComprobanteMontoExento",
                             19 As "IdComprobanteTasaIva",
                             Coalesce("MontoIva",0) As "IdComprobanteMontoIva",
                             Coalesce("MontoTotal",0) As "IdComprobanteMontoTotal",
                             Coalesce("ValorAPagar",0) As "IdComprobanteValorAPagar",
                             
                             Coalesce("TotalMontoNeto",0) As "TotalIdTipoComprobanteMontoNeto",
                             Coalesce("TotalMontoExento",0) As "TotalIdTipoComprobanteMontoExento",
                             Coalesce("TotalMontoIva",0) As "TotalIdTipoComprobanteMontoIva",
                             Coalesce("TotalMontoTotal",0) As "TotalIdTipoComprobanteMontoTotal",
                             Coalesce("CantidadDocumentos",0) As "TotalIdTipoComprobanteCantidadDocumentos",
                             Coalesce("CantidadAnulados",0) As "TotalIdTipoComprobanteCantidadAnulados",
                             
                             /** Corredora **/
                             "DocumentoIdentidadEmisor", 
                             '' As "RazonSocialEmisor",
                             '' As "GiroEmisor",
                             '' As "DireccionEmisor",
                             '' As "ComunaEmisor",
                             '' As "CiudadEmisor",
                             '' As "EmailEmisor",
                             '' As "TelefonoEmisor",
                             
                             /** Cliente **/
                             '' As "EmailReceptor",
                             "RutDocumento" As "DocumentoIdentidadReceptor",
                             "RazonSocial" As "RazonSocialReceptor",
                             '' As "GiroReceptor",
                             '' As "DireccionReceptor",
                             '' As "ComunaReceptor",
                             '' As "CiudadReceptor",
                             '' As "ContactoReceptor",
                             
                             'LIBRO' As "TipoReferenciaIdComprobante",
                             "IdLibro" As "FolioReferenciaIdComprobante",  
                             '' As "FechaReferenciaIdComprobante",
                             '' As "RazonReferenciaIdComprobante",
                             '' As "TipoReferenciaFolioComprobante",
                             0 As "FolioReferenciaFolioComprobante",
                             '' As "FechaReferenciaFolioComprobante",
                             '' As "RazonReferenciaFolioComprobante",
                             '' As "IdComprobanteDetalleGlosa", 
                             '' As "IdComprobanteDetalleDescripcion", 
                             0 As "IdComprobanteDetalleCantidad",
                             0 As "IdComprobanteDetallePrecio", 
                             0 As "IdComprobanteDetallePorcentaje",
                             0 As "IdComprobanteDetalleMontoDescuento",
                             0 As "IdComprobanteDetalleMonto",
                             0 As "IdComprobanteDetalleEsExento"
                      From :v_CmbteDetADTE As Cmbte
                        Inner Join :v_CmbteTotalesADTE As CmbteTot
                           On CmbteTot."TipoDocumento" = Cmbte."TipoDocumento"
                        Inner Join :v_LibroDte As LibDte
                           On LibDte."Tipo" = Cmbte."AccionTipoOperacion"
                          And LibDte."PeriodoContable"= Cmbte."PeriodoContable"
                          And LibDte."DocumentoIdentidadEmpresa" = Cmbte."DocumentoIdentidadEmisor";
                           

        /*Select 'v_CmbteADTE', *
        From :v_CmbteADTE;*/
                
                        
                        
        -- Inserta registro tarea dte              
        Insert into "BSCL.Facturacion.BD::FAC.TareaDTE"
           (
             "IdTareaDTE",
             "IdTareaDTEGrupo",
             "TipoTareaDTE",
             "IdRespuestaDTE",
             "RespuestaDTEEnvioSII",             
             "RespuestaLocationDTE",
             "RespuestaPdfDTE",
             "RespuestaXmlDTE",
             "FolioComprobanteRespuestaDTE",             
             "IdComprobante",
             "IdEstadoTareaDTE",             
             "IdEstadoDTE",
             "IdEstadoSII",             
             "IdTipoComprobante",
             "TipoComprobanteDTE",
             "AccionTipoOperacion",
             "PeriodoContable",
             "FechaComprobante",
             "FechaVencimiento",             
             "CodigoVendedor",             
             "IdComprobanteMontoNoFacturable",
             --"IdComprobanteMontoNeto",
             "IdComprobanteMontoExento",
             "IdComprobanteTasaIva",
             --"IdComprobanteMontoIva",
             "IdComprobanteMontoTotal",
             "IdComprobanteValorAPagar",             
             "TotalIdTipoComprobanteMontoNeto",
             "TotalIdTipoComprobanteMontoExento",
             "TotalIdTipoComprobanteMontoIva",
             "TotalIdTipoComprobanteMontoTotal",
             "TotalIdTipoComprobanteCantidadDocumentos",
             "TotalIdTipoComprobanteCantidadAnulados",                                      
             "DocumentoIdentidadEmisor",
             "RazonSocialEmisor",
             "GiroEmisor",
             "DireccionEmisor",
             "ComunaEmisor",
             "CiudadEmisor",
             "EmailEmisor",
             "TelefonoEmisor",
             "EmailReceptor",
             "DocumentoIdentidadReceptor",
             "RazonSocialReceptor",
             "GiroReceptor",
             "DireccionReceptor",
             "ComunaReceptor",
             "CiudadReceptor",
             "ContactoReceptor",
             "TipoReferenciaIdComprobante",
             "FolioReferenciaIdComprobante",
             "FechaReferenciaIdComprobante",
             "RazonReferenciaIdComprobante",
             "TipoReferenciaFolioComprobante",
             "FolioReferenciaFolioComprobante",
             "FechaReferenciaFolioComprobante",
             "RazonReferenciaFolioComprobante",
             "IdComprobanteDetalleGlosa",
             "IdComprobanteDetalleCantidad",
             "IdComprobanteDetallePrecio",
             "IdComprobanteDetallePorcentaje",
             "IdComprobanteDetalleMontoDescuento",
             "IdComprobanteDetalleMonto",
             "IdComprobanteDetalleEsExento"
           )
        Select
             "IdTareaDTE",
             "IdTareaDTEGrupo",
             "TipoTareaDTE",
             "IdRespuestaDTE",
             "RespuestaDTEEnvioSII",             
             "RespuestaLocationDTE",
             "RespuestaPdfDTE",
             "RespuestaXmlDTE",
             "FolioComprobanteRespuestaDTE",             
             "IdComprobante",
             "IdEstadoTareaDTE",             
             "IdEstadoDTE",
             "IdEstadoSII",             
             "IdTipoComprobante",
             "TipoComprobanteDTE",
             "AccionTipoOperacion",
             "PeriodoContable",
             "FechaComprobante",
             "FechaVencimiento",             
             "CodigoVendedor",             
             "IdComprobanteMontoNoFacturable",
             --"IdComprobanteMontoNeto",
             "IdComprobanteMontoExento",
             "IdComprobanteTasaIva",
             --"IdComprobanteMontoIva",
             "IdComprobanteMontoTotal",
             "IdComprobanteValorAPagar",             
             "TotalIdTipoComprobanteMontoNeto",
             "TotalIdTipoComprobanteMontoExento",
             "TotalIdTipoComprobanteMontoIva",
             "TotalIdTipoComprobanteMontoTotal",
             "TotalIdTipoComprobanteCantidadDocumentos",
             "TotalIdTipoComprobanteCantidadAnulados",                                      
             "DocumentoIdentidadEmisor",
             "RazonSocialEmisor",
             "GiroEmisor",
             "DireccionEmisor",
             "ComunaEmisor",
             "CiudadEmisor",
             "EmailEmisor",
             "TelefonoEmisor",
             "EmailReceptor",
             "DocumentoIdentidadReceptor",
             "RazonSocialReceptor",
             "GiroReceptor",
             "DireccionReceptor",
             "ComunaReceptor",
             "CiudadReceptor",
             "ContactoReceptor",
             "TipoReferenciaIdComprobante",
             "FolioReferenciaIdComprobante",
             "FechaReferenciaIdComprobante",
             "RazonReferenciaIdComprobante",
             "TipoReferenciaFolioComprobante",
             "FolioReferenciaFolioComprobante",
             "FechaReferenciaFolioComprobante",
             "RazonReferenciaFolioComprobante",
             "IdComprobanteDetalleGlosa",
             "IdComprobanteDetalleCantidad",
             "IdComprobanteDetallePrecio",
             "IdComprobanteDetallePorcentaje",
             "IdComprobanteDetalleMontoDescuento",
             "IdComprobanteDetalleMonto",
             "IdComprobanteDetalleEsExento"
        From :v_CmbteADTE;
        
        /*SELECT 'p_TablaParametros',*
        from :p_TablaParametros;*/
        
        
        --Anula libro previamente enviado
        Update LibDte 
        Set "IdEstado" = 'ANU'
        From "BSCL.Facturacion.BD::FAC.LibroDte" As LibDte
          Inner Join :v_LibroDte As LibDteNvo  
             On LibDteNvo."PeriodoContable" = LibDte."PeriodoContable"
            And LibDteNvo."Tipo" = LibDte."Tipo" 
            And LibDteNvo."DocumentoIdentidadEmpresa" = LibDte."DocumentoIdentidadEmpresa";
          
          
                         
        
        --Inserta datos en tabla libro y detalle libro
        Insert Into "BSCL.Facturacion.BD::FAC.LibroDte"
           ( "IdLibro",
             "Tipo",
             "PeriodoContable",
             "DocumentoIdentidadEmpresa",
             "IdEstado",
             "FechaIngresoRegistro"
           )
        Select "IdLibro",
               "Tipo",
               "PeriodoContable",
               "DocumentoIdentidadEmpresa",
               'PEN',
               CURRENT_UTCTIMESTAMP
        From :v_LibroDte;
                
         
        Insert Into "BSCL.Facturacion.BD::FAC.LibroDteComprobante"
           ( "IdLibroDteComprobante",
             "IdLibro",
             "IdComprobante",
             "FechaIngresoRegistro"
           ) 
        Select "IdLibroDteComprobante",
               "IdLibro",
               "IdComprobante",
               CURRENT_UTCTIMESTAMP  
         From :v_CmbteLibroDte;      
             
           

                          
        /** Llama a DTE **/
        Call "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma" 
          (			
			 /*p_IdEmpresa BIGINT*/ p_IdEmpresa => :p_IdEmpresa,
			 /*p_IdUsuario BIGINT*/ p_IdUsuario => :p_IdUsuario,
			 /*p_IdModulo NVARCHAR(3)*/ p_IdModulo => 'FAC',
			 /*p_IdAgenda BIGINT*/ p_IdAgenda => 0,
			 /*p_IdTipoTarea NVARCHAR(40)*/ p_IdTipoTarea => 'DTELIB',
			 /*p_Segundos BIGINT*/ p_Segundos => 0,
			 /*p_TablaParametros table*/ p_TablaParametros => :p_TablaParametros,
			 /*p_CreaParam NVARCHAR(1)*/ p_CreaParam => 'S',
			 /*p_out_IdPrograma table*/ p_out_IdPrograma => v_out_IdPrograma,
			 /*p_out_AdvertenciasContador int*/ p_out_AdvertenciasContador => v_out_AdvertenciasContador,
			 /*p_out_AdvertenciasDetalle table*/ p_out_AdvertenciasDetalle => v_out_AdvertenciasDetalle,
			 /*p_out_ErroresContador int*/ p_out_ErroresContador => v_out_ErroresContador,
			 /*p_out_ErroresDetalle table*/ p_out_ErroresDetalle => v_out_ErroresDetalle
		  );	          
        /** Fin Preparacion Envio a DTE **/ 
        
        
        --*********************
		--* MENSAJE DE EXITO  *
		--*********************		                         
		-- Registra resultado proceso
		Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ResultadoProceso"                          
		Select "IdComprobante" As "Campo", "IdComprobante" As "Id",
		       'BSCL.Facturacion.BD.Servicios::DteEnviarLibro' As "Servicio",
		       'ENV' As "Resultado"
		From :v_CmbteADTE Union All
		Select "IdComprobante" As "Campo", "IdComprobante" as "Id",
		       'BSCL.Facturacion.BD.Servicios::DteEnviarLibro' as "Servicio",
		       'RECH' as "Resultado"
		From "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ComprobantesRechazados";
		                         
		
		-- Registra advertencias
        p_out_AdvertenciasContador = p_out_AdvertenciasContador + v_out_AdvertenciasContador;
		Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_AdvertenciasDetalle"
		Select 0 As "IdComprobante", "Codigo", "RequiereAutorizacion", "Descripcion" 
		From :v_out_AdvertenciasDetalle;
		
		-- Registra errores
        p_out_ErroresContador = p_out_ErroresContador + v_out_ErroresContador;
       /* Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle"
        Select 0 As "IdComprobante", "Codigo", "Descripcion" 
        From :v_out_ErroresDetalle;*/

		-- Registra salida UI - informacion contador
		Select Count("IdComprobante")
		Into Cant_ComprobanteEnviado
		from :v_CmbteADTE;
		
		
		If Cant_ComprobanteEnviado > 0 Then 
		   v_TablaCodigosMensajes = Select Distinct "IdComprobante" As "IdInterno", 'UI_COMPROBANTE_ENVIADO' As "Codigo",
                                           'IdComprobante' As "CodigoDato1", Cast("IdComprobante" As NVARCHAR(520)) as "ValorDato1",
                                           '' As "CodigoDato2", '' As "ValorDato2" 
		                            From :v_CmbteADTE;
		  
  		   CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
		
		   
		   Insert into "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_InformacionDetalle"
		   Select "Codigo", "Descripcion" 
		   From :v_out_hash;                           
		
		
		   Select Count("Codigo") 
		   Into p_out_InformacionContador 
		   From "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_InformacionDetalle";
		   
		End If;             

    END; --Logica
END;   
