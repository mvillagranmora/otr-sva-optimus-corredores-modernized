SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::DteConsultarEstadoComprobante";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::DteConsultarEstadoComprobante"
  (
     -- Parametros de Validacion
     IN p_IdEmpresa BIGINT,  
     IN p_IdUsuario BIGINT,   
     IN p_IdModulo  NVARCHAR(3), 
     
     IN p_simularOperacion    NVARCHAR(1) ,	-- S/N se usa para simular operacion (algunos casos)                  
     IN p_UsaReglasNegocio    NVARCHAR(1) ,	-- S/N se usa en caso de servicios anidados. 
     IN p_EsIngresoAutomatico NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente     
   
     -- Parametros de Proceso 
    IN p_ComprobanteAAct    table("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdComprobante" BIGINT, "IdTipoComprobante" NVARCHAR(3)) DEFAULT EMPTY,
     
     --Salidas para mensajes de advertencias y errores
     OUT p_out_InformacionContador  	 INT,
     OUT p_out_InformacionDetalle   	 table ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),   
     OUT p_out_ResultadoProceso		     table ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)) DEFAULT EMPTY,
     OUT p_out_AdvertenciasContador      int,
     OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
     OUT p_out_ErroresContador           int,   
     OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY                              
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	--READS SQL DATA
AS
BEGIN
    -----------------------------
	--DECLARE Manejo de Errores
	-----------------------------
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT)
            Default Select 0 As "IdComprobante" From dummy where 1=2;
	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle; 
								
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
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
       DECLARE v_out_AdvertenciasContador, v_out_ErroresContador, v_IdTareaDTEGrupo, Cant_ComprobanteConsultado,
               Cant_Cmte  BIGINT := 0;
       
       DECLARE p_TablaParametros TABLE ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100)) 
               Default Select '' As "NombreParametro", '' As "Valor" from dummy where 1=2;
               
       
       DECLARE v_CmbteDeDTE TABLE ("IdTareaDTE" BIGINT, "IdTareaDTEGrupo" BIGINT, "TipoTareaDTE" NVARCHAR(40), "IdRespuestaDTE" BIGINT, "RespuestaDTEEnvioSII" NVARCHAR(250), "RespuestaLocationDTE" NVARCHAR(40), "RespuestaPdfDTE" NVARCHAR(250),
                                  "RespuestaXmlDTE" NVARCHAR(250), "FolioComprobanteRespuestaDTE" BIGINT, "IdComprobante" BIGINT, "IdEstadoTareaDTE" NVARCHAR(3), "IdEstadoDTE" NVARCHAR(3), "IdEstadoSII" NVARCHAR(3), "IdTipoComprobante" NVARCHAR(3), 
                                  "FolioComprobante" BIGINT, "TipoComprobanteDTE" BIGINT, "AccionTipoOperacion" NVARCHAR(6), "FechaComprobante" NVARCHAR(10), "FechaVencimiento" NVARCHAR(10), "IdComprobanteMontoNoFacturable" DECIMAL(26,8), --"IdComprobanteMontoNeto" DECIMAL(26,8), 
                                  "IdComprobanteMontoExento" DECIMAL(26,8), "IdComprobanteTasaIva" DECIMAL(26,8), --"IdComprobanteMontoIva" DECIMAL(26,8), 
                                  "IdComprobanteMontoTotal" DECIMAL(26,8), "IdComprobanteValorAPagar" DECIMAL(26,8), "DocumentoIdentidadEmisor" NVARCHAR(40), "RazonSocialEmisor" NVARCHAR(100), "GiroEmisor" NVARCHAR(100), 
                                  "DireccionEmisor" NVARCHAR(100), "ComunaEmisor" NVARCHAR(40), "CiudadEmisor" NVARCHAR(40), "EmailEmisor" NVARCHAR(40), "TelefonoEmisor" NVARCHAR(40), "EmailReceptor" NVARCHAR(40), "DocumentoIdentidadReceptor" NVARCHAR(40), 
                                  "RazonSocialReceptor" NVARCHAR(100), "GiroReceptor" NVARCHAR(100), "DireccionReceptor" NVARCHAR(100), "ComunaReceptor" NVARCHAR(40), "CiudadReceptor" NVARCHAR(40), "ContactoReceptor" NVARCHAR(40), "TipoReferenciaIdComprobante" NVARCHAR(40), 
                                  "FolioReferenciaIdComprobante" BIGINT, "FechaReferenciaIdComprobante" NVARCHAR(10), "RazonReferenciaIdComprobante" NVARCHAR(40), "TipoReferenciaFolioComprobante" NVARCHAR(40), "FolioReferenciaFolioComprobante" BIGINT, "FechaReferenciaFolioComprobante" NVARCHAR(10),
                                  "RazonReferenciaFolioComprobante" NVARCHAR(40), "IdComprobanteDetalleGlosa" NVARCHAR(40), "IdComprobanteDetalleDescripcion" NVARCHAR(40), "IdComprobanteDetalleCantidad" DECIMAL(26,8), "IdComprobanteDetallePrecio" DECIMAL(26,8), "IdComprobanteDetallePorcentaje" DECIMAL(26,8), 
                                  "IdComprobanteDetalleMontoDescuento" DECIMAL(26,8), "IdComprobanteDetalleMonto" DECIMAL(26,8), "IdComprobanteDetalleEsExento" NVARCHAR(1))
               Default Select 0 As "IdTareaDTE", 0 As "IdTareaDTEGrupo", '' As "TipoTareaDTE", 0 As "IdRespuestaDTE", '' As "RespuestaDTEEnvioSII", '' As "RespuestaLocationDTE", '' As "RespuestaPdfDTE",
                              '' As "RespuestaXmlDTE", 0 As "FolioComprobanteRespuestaDTE", 0 As "IdComprobante", '' As "IdEstadoTareaDTE", '' As "IdEstadoDTE", '' As "IdEstadoSII", '' As "IdTipoComprobante", 
                              0 As "FolioComprobante", 0 As "TipoComprobanteDTE", '' As "AccionTipoOperacion", '' As "FechaComprobante", '' As "FechaVencimiento", 0 As "IdComprobanteMontoNoFacturable", --0 As "IdComprobanteMontoNeto", 
                              0 As "IdComprobanteMontoExento", 0 As "IdComprobanteTasaIva", --0 As "IdComprobanteMontoIva", 
                              0 As "IdComprobanteMontoTotal", 0 As "IdComprobanteValorAPagar", '' As "DocumentoIdentidadEmisor", '' As "RazonSocialEmisor", '' As "GiroEmisor", 
                              '' As "DireccionEmisor", '' As "ComunaEmisor", '' As "CiudadEmisor", '' As "EmailEmisor", '' As "TelefonoEmisor", '' As "EmailReceptor", '' As "DocumentoIdentidadReceptor", 
                              '' As "RazonSocialReceptor", '' As "GiroReceptor", '' As "DireccionReceptor", '' As "ComunaReceptor", '' As "CiudadReceptor", '' As "ContactoReceptor", '' As "TipoReferenciaIdComprobante", 
                              0 As "FolioReferenciaIdComprobante", '' As "FechaReferenciaIdComprobante", '' As "RazonReferenciaIdComprobante", '' As "TipoReferenciaFolioComprobante", 0 As "FolioReferenciaFolioComprobante", '' As "FechaReferenciaFolioComprobante",
                              '' As "RazonReferenciaFolioComprobante", '' As "IdComprobanteDetalleGlosa", '' As "IdComprobanteDetalleDescripcion", 0 As "IdComprobanteDetalleCantidad", 0 As "IdComprobanteDetallePrecio", 0 As "IdComprobanteDetallePorcentaje", 
                              0 As "IdComprobanteDetalleMontoDescuento", 0 As "IdComprobanteDetalleMonto", '' As "IdComprobanteDetalleEsExento" from dummy where 1=2;
      
      
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
			   
			/*Call "BSCL.SocioNegocio.BD.Reglas::CLI.ServicioCumpleRequisitosCliente"(:p_IdCliente, 0, :p_out_ErroresDetalle, :v_resultadoRegla);
			If :v_resultadoRegla <> 'OK' Then
			  SIGNAL SALIDA_CONTROLADA; 	
			End If;*/
			                  
          End If;
            
          --Validacion Facturación
          Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosDteConsultarEstadoComprobante"(:p_ComprobanteAAct,:v_RechazadosPorValidacion,:v_ErroresDetalle,:v_resultadoRegla);
		  If :v_resultadoRegla = 'RECH' Then	
		    p_out_ResultadoProceso = Select "IdComprobante" As "Campo", "IdComprobante" as "Id",
		                                    'BSCL.Facturacion.BD.Servicios::DteConsultarEstadoComprobante' as "Servicio",
		                                    'RECH' as "Resultado"
		                             From :v_RechazadosPorValidacion;
			  			
			p_out_ErroresDetalle = Select "IdComprobante", "Codigo","Descripcion" 
			                       From :v_ErroresDetalle;			                           	
                
			SIGNAL SALIDA_CONTROLADA; 	
		  Else
		    --If :v_resultadoRegla = 'ADVE' Then
			--LLamada al servicio de autorizacion si se requiere
			p_out_AdvertenciasDetalle = Select "IdComprobante", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
			                            From :v_ErroresDetalle;
			                                 
			Select count(1) 
			Into p_out_AdvertenciasContador 
			From :p_out_AdvertenciasDetalle;
		    
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
      v_ComprobanteAAct = Select "IdPortafolio", "IdCliente", "IdComprobante", "IdTipoComprobante"
                          From :p_ComprobanteAAct As CmbteAAct
                          Where CmbteAAct."IdComprobante" Not In (Select "IdComprobante" 
                                                                  From :v_RechazadosPorValidacion);
                                                                      
        /*Select 'v_ComprobanteAAct', *
        from :v_ComprobanteAAct;*/
        
        Select Count("IdComprobante")
        Into Cant_Cmte
        From :v_ComprobanteAAct;
        
        If Cant_Cmte <=0 Then
           p_out_ErroresContador := p_out_ErroresContador + 1;
		    
		   p_out_ErroresDetalle = Select 0 As "IdComprobante", '' As "Codigo", 
		                                 'Los comprobantes enviados presentan problemas' As "Descripcion" 
		                          From dummy Union All		                          
	                              Select "IdComprobante", "Codigo", "Descripcion" 
	                              From :p_out_ErroresDetalle;
	                              
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
        --Obtiene montos para job  
        v_CmbteMontoADTE = Select ConsCmbteDet."IdComprobante", ConsCmbteDet."IdTipoComprobante", 
                                  Max(ConsCmbteDet."MontoComprobante") As "MontoNoFacturable",
                                  Sum(ConsCmbteCob."MontoLocalCobro") As "MontoNeto",
                                  0 As "MontoExento",
                                  19 As "TasaIva",
                                  ((Sum(ConsCmbteCob."MontoLocalCobro") * 19) /100) As "MontoIva",
                                  (Sum(ConsCmbteCob."MontoLocalCobro") + ((Sum(ConsCmbteCob."MontoLocalCobro") * 19) /100)) As "MontoTotal",
                                  (Max(ConsCmbteDet."MontoComprobante") + Sum(ConsCmbteCob."MontoLocalCobro") + ((Sum(ConsCmbteCob."MontoLocalCobro") * 19) /100)) As "ValorAPagar"
                           From "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbteDet
                              Inner Join :v_ComprobanteAAct As CmbteAEnviar
                                 On CmbteAEnviar."IdComprobante" = ConsCmbteDet."IdComprobante"
                              Left join "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" As ConsCmbteCob
                                 On ConsCmbteCob."IdComprobante" = ConsCmbteDet."IdComprobante"
                           Group By ConsCmbteDet."IdComprobante", ConsCmbteDet."IdTipoComprobante";
                           
                           
        /*select 'v_CmbteMontoADTE', *                   
        from :v_CmbteMontoADTE;*/
        
          
        v_CmbteDeDTE = Select "BSCL.Facturacion.BD.Secuencias::TareaDTE".Nextval As "IdTareaDTE", 
                              v_IdTareaDTEGrupo As "IdTareaDTEGrupo", 
                              'DTEACT' As "TipoTareaDTE",
                              "IdRespuestaDTE",
                              '' As "RespuestaDTEEnvioSII",
                              "RespuestaLocationDTE",
                              "RespuestaPdfDTE",
                              "RespuestaXmlDTE",
                              0 As "FolioComprobanteRespuestaDTE",
                              ConsCmbte."IdComprobante",
                              'PEN' As "IdEstadoTareaDTE",
                              "IdEstadoDTE",
                              '' As "IdEstadoSII",
                              ConsCmbte."IdTipoComprobante" As "IdTipoComprobante",
                              ConsCmbte."FolioComprobante" As "FolioComprobante",
                              TipCmbte."TipoComprobanteDTE",
                              ConsCmbte."AccionTipoOperacion" As "AccionTipoOperacion",
                              LEFT("FechaComprobante",10) as "FechaComprobante",
                              LEFT("FechaComprobante",10) As "FechaVencimiento",	
                              Coalesce(CmbteMonADTE."MontoNoFacturable",0) As "IdComprobanteMontoNoFacturable",
                              --Coalesce(CmbteMonADTE."MontoNeto",0) As "IdComprobanteMontoNeto",
                              Coalesce(CmbteMonADTE."MontoExento",0) As "IdComprobanteMontoExento",
                              CmbteMonADTE."TasaIva" As "IdComprobanteTasaIva", 
                              --Coalesce(CmbteMonADTE."MontoIva",0) As "IdComprobanteMontoIva",
                              Coalesce(CmbteMonADTE."MontoTotal",0) As "IdComprobanteMontoTotal",
                              Coalesce(CmbteMonADTE."ValorAPagar",0) As "IdComprobanteValorAPagar",
                              /** Corredora **/ 
                              --ConsPer."DocumentoIdentidad" As "DocumentoIdentidadEmisor", 
                              '96535530-8' As "DocumentoIdentidadEmisor", 
                              Case
                                When Length(ConsPer."RazonSocial") > 0 Then ConsPer."RazonSocial"
                              Else ''
                              End As "RazonSocialEmisor", 
                              GiroEmisor."Nombre" As "GiroEmisor",
                              (ConsEmp."DireccionFisicaPrincipalCalle" || ' ' || "DireccionFisicaPrincipalNumero") As "DireccionEmisor",
                              ConsEmp."DireccionFisicaPrincipalNombreDivisionPais" As "ComunaEmisor",
                              ConsEmp."DireccionFisicaPrincipalNombreCiudad" As "CiudadEmisor",
                              ConsEmp."DireccionElectronicaPrincipal" As "EmailEmisor",
                              ConsEmp."TelefonoPrincipal" As "TelefonoEmisor",                             
                              /** Cliente **/
                              DirElec."Direccion" As "EmailReceptor",
                              ConsCmbte."DocumentoIdentidad" As "DocumentoIdentidadReceptor",
                              ConsCli."NombreCompleto" As "RazonSocialReceptor",
                              Case 
                                When Length(GiroReceptor."Nombre") > 0 Then GiroReceptor."Nombre"
                              Else 'En definicion'
                              End As "GiroReceptor",
                              (DirFis."Calle" || ' ' || DirFis."Numero" || ' ' || DirFis."Departamento")  As "DireccionReceptor",
                              ConsDivPais."NombreDivisionPais" As "ComunaReceptor",
                              Ciudad."NombreCiudad" As "CiudadReceptor", 
                              'PM' As "ContactoReceptor",
                              'SAP' As "TipoReferenciaIdComprobante", 
                              ConsCmbte."IdComprobante" As "FolioReferenciaIdComprobante",  
                              '' As "FechaReferenciaIdComprobante",
                              'Nro. Interno SAP' As "RazonReferenciaIdComprobante", 
                              'VIS' As "TipoReferenciaFolioComprobante", 
                              "FolioComprobante" As "FolioReferenciaFolioComprobante",  
                              '' As "FechaReferenciaFolioComprobante",
                              'Nro. Visible SAP' As "RazonReferenciaFolioComprobante", 
                              '' As "IdComprobanteDetalleGlosa", 
                              '' As "IdComprobanteDetalleDescripcion",
                              0 As "IdComprobanteDetalleCantidad",
                              0 As "IdComprobanteDetallePrecio", 
                              0 As "IdComprobanteDetallePorcentaje",
                              0 As "IdComprobanteDetalleMontoDescuento",
                              0 As "IdComprobanteDetalleMonto",
                              0 As "IdComprobanteDetalleEsExento"                               
                      From "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
                        Inner Join :v_ComprobanteAAct As CmbteAAct
                           On CmbteAAct."IdComprobante" = ConsCmbte."IdComprobante"                           
                        Inner Join :v_CmbteMontoADTE As CmbteMonADTE
                           On CmbteMonADTE."IdComprobante" = ConsCmbte."IdComprobante"
                          And CmbteMonADTE."IdTipoComprobante" = ConsCmbte."IdTipoComprobante"                                                
                        Inner Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte
                           On TipCmbte."IdTipoComprobante" = ConsCmbte."IdTipoComprobante"  
                        Left Join "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper  
	                       On TipOper."IdTipoOperacion" = ConsCmbte."IdTipoOperacion"
                        Inner Join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" As ConsCli
                           On ConsCli."IdCliente" = ConsCmbte."IdCliente"
                        Left Join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" As ConsEmp
                           On ConsEmp."IdEmpresa" = ConsCmbte."IdEmpresa"
                        Left Join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" As ConsPer
                           On ConsPer."IdPersona" = ConsEmp."IdPersona"  
                        Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" As GiroEmisor
                           On GiroEmisor."IdGiro" = ConsPer."IdGiroPrincipal"   
                        Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" As GiroReceptor
                           On GiroReceptor."IdGiro" = ConsCli."IdGiroPrincipal"
                        Left Join "BSCL.SocioNegocio.BD::PER.DireccionFisica" As DirFis
                           On DirFis."IdDireccionFisica" = ConsCli."IdDireccionFisicaPrincipal" 
                        Left Join "BSCL.Plataforma.BD::REG.Ciudad" As Ciudad
                           On Ciudad."IdCiudad" = DirFis."IdCiudad"
                        Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" As ConsDivPais
                           On ConsDivPais."IdDivisionPais" = DirFis."IdDivisionPais"
                          And ConsDivPais."IdCiudad" = Ciudad."IdCiudad"         
                        Left Join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" As DirElec
                           On DirElec."IdDireccionElectronica" = ConsCli."IdDireccionElectronicaPrincipal" 
                      Order by ConsCmbte."IdComprobante";
                      
        /*Select 'v_CmbteDeDTE', *
        from :v_CmbteDeDTE;*/
        
                              
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
             "IdComprobanteDetalleDescripcion",
             "IdComprobanteDetalleCantidad",
             "IdComprobanteDetallePrecio",
             "IdComprobanteDetallePorcentaje",
             "IdComprobanteDetalleMontoDescuento",
             "IdComprobanteDetalleMonto",
             "IdComprobanteDetalleEsExento",
             "FechaEnvioDte"
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
             '',	 
             TO_DATE("FechaComprobante"),
             "FechaVencimiento",
             '',
             
             "IdComprobanteMontoNoFacturable",
             --"IdComprobanteMontoNeto",
             "IdComprobanteMontoExento",
             "IdComprobanteTasaIva",
             --"IdComprobanteMontoIva",
             "IdComprobanteMontoTotal",
             "IdComprobanteValorAPagar",     
             
             0,
             0,
             0,
             0,
             0,
             0,
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
             "IdComprobanteDetalleDescripcion",
             "IdComprobanteDetalleCantidad",
             "IdComprobanteDetallePrecio",
             "IdComprobanteDetallePorcentaje",
             "IdComprobanteDetalleMontoDescuento",
             "IdComprobanteDetalleMonto",
             "IdComprobanteDetalleEsExento",
             CURRENT_TIMESTAMP
        From :v_CmbteDeDTE;    
        
        /*SELECT 'p_TablaParametros',*
        from :p_TablaParametros;*/
    
        /** Fin Preparacion Envio a DTE **/ 
        
        
        --*********************
		--* MENSAJE DE EXITO  *
		--*********************
        p_out_ResultadoProceso = Select "IdComprobante" As "Campo", "IdComprobante" As "Id",
				                        'BSCL.Facturacion.BD.Servicios::DteConsultarEstadoComprobante' As "Servicio",
				                        'CON' As "Resultado"
		                         From :v_CmbteDeDTE Union All
		                         Select "IdComprobante" As "Campo", "IdComprobante" as "Id",
		                                'BSCL.Facturacion.BD.Servicios::DteEnviarComprobante' as "Servicio",
		                                'RECH' as "Resultado"
		                         From :v_RechazadosPorValidacion Union All
		                         Select "Campo", "Id", "Servicio", "Resultado" 
		                         From :p_out_ResultadoProceso; 
		                        
		
		
		
		Select Count("IdComprobante")
		Into Cant_ComprobanteConsultado
		from :v_CmbteDeDTE; 

		
		/*select Cant_ComprobanteGenerado As "Cant_ComprobanteGenerado"
		from dummy;*/	
		
		If Cant_ComprobanteConsultado > 0 Then 
		   v_TablaCodigosMensajes = Select "IdComprobante" As "IdInterno", 'UI_COMPROBANTE_CONSULTADO' As "Codigo",
                                           'IdComprobante' As "CodigoDato1", Cast("IdComprobante" As NVARCHAR(520)) as "ValorDato1",
                                           '' As "CodigoDato2", '' As "ValorDato2" 
		                            From :v_CmbteDeDTE;
		  
  		   CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
		
		   p_out_InformacionDetalle = Select "Codigo", "Descripcion" 
		                              From :v_out_hash;
		
		   Select Count("Codigo") 
		   Into p_out_InformacionContador 
		   From :p_out_InformacionDetalle;
		End If;                   

    End; --Logica 

END;