-- 2018-11-19 09:00
SET SCHEMA BSCL;
DROP PROCEDURE BSCL."BSCL.Homologacion.BD.Servicios::CargarFactura";

CREATE PROCEDURE BSCL."BSCL.Homologacion.BD.Servicios::CargarFactura" (

	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			
     IN  p_IdUsuario		   BIGINT,			
	 IN  p_IdCarga 			   BIGINT,          
	 IN  p_Modulo              NVARCHAR(3),     
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    
	 IN  p_FechaInicial        NVARCHAR(10),  
	 IN  p_FechaFinal		   NVARCHAR(10)   

) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

		-- VARIABLES VALIDACIONES BASICAS

			DECLARE v_TableSimultaneas		TABLE ("SecMovto" DECIMAL(8), "CodPer" CHAR(7), "SecMovtoCtdo" DECIMAL(8));
	
			DECLARE p_IdUsuario		 		BIGINT DEFAULT 66;				  -- ID del usuario que solicita el servicio
			DECLARE p_IdModulo         	 	NVARCHAR(5000) DEFAULT 'CUS';    -- modulo 
			DECLARE p_IdEmpresa		   	 	BIGINT DEFAULT 1;			  -- ID Empresa destino	
			DECLARE v_out_Resultado  	 	CHAR(1); 	   --RESULTADO S/N/E
		    DECLARE v_out_Mensaje	 	 	NVARCHAR(5000); --MENSAJE SALIDA
		    
		    DECLARE v_PerIni 	 			NVARCHAR(7); 
		    DECLARE V_PerFin	 	 		NVARCHAR(7); 
		    DECLARE v_FechaVigente	 	 	NVARCHAR(10); 
		    DECLARE v_RutCliente			NVARCHAR(11); 


		    DECLARE v_erroroutmsg  			NVARCHAR(5000);    
		    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
		    DECLARE v_TotalCuentasGrabadas	BIGINT;

	    	DECLARE Concepto_Egre 			NVARCHAR(5000) default '01';
	    	DECLARE Concepto_Ingre 			NVARCHAR(5000) default '01';
	    	
	    	DECLARE v_valor 				DECIMAL(10,8) default 19;
		    
		    
		    -- Para LOg
		    DECLARE v_out_ErroresContador	BIGINT; 
		    DECLARE v_out_ErroresDetalle 	table ("Codigo" NVARCHAR(5000),"Descripcion" NVARCHAR(5000));	
		    DECLARE v_MonedaSistema 		NVARCHAR(5000);
	        DECLARE v_IdContraparteGarantia bigint := 0; 
	        DECLARE	in_out_Contador 		int;
	        
	        
			--------- CONTROL DE ERRORES
			--- Controlado
			DECLARE SALIDA_CONTROLADA CONDITION;    	
			DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
			BEGIN
				IF :v_out_ErroresContador = 0 THEN	 
			 	    v_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
				END IF;		 					 	
			END;	
	
			--- Excepcion
		
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
			BEGIN
				declare v_errorExt NVARCHAR(5000);
				IF ::SQL_ERROR_CODE > 10000 THEN
					call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
					v_out_ErroresDetalle = select * from :v_out_ErroresDetalle UNION ALL  
										   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
					select count(1) into v_out_ErroresContador 
					from :v_out_ErroresDetalle;										
				ELSE
					--Error interno no manejado  (El log ser? captado por la capa de XS control)
					v_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
					--RESIGNAL; 
				END IF;			
			END;
		
			v_out_ErroresContador		:= 0;
			v_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
        

			SELECT COALESCE("Valor",'') into v_MonedaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
        	WHERE  "IdEmpresa" = p_IdEmpresa
        	AND  "CodigoParametro" = 'PLA.MonedaSistema';


			-- recuperamos 
            -- ojo esto debe homologar por el moento se debera colocar por defecto   
/* 			SELECT coalesce("IdContraparte",0) into v_IdContraparteGarantia 
			FROM "BSCL"."BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"
				where "Bolsa" = 'S'
				and "DocumentoIdentidad" = '90249000-0';   */

 
	  	-----------------------------------------------------
		-- Master de Facturas 
		-----------------------------------------------------
       	INSERT INTO "BSCL.Homologacion.BD::HOM.Comprobante"
				   (    "IdComprobante","IdEmpresa","IdPortafolio","IdCliente","DocumentoIdentidad","TipoDocumentoIdentidad","NombreLargoCliente","DireccionFacturacion"
						,"FolioComprobante","IdTipoComprobante","IdTipoOperacion","IdTipoProducto","IdClasificacionTipoInstrumento","IdTipoInstrumento","Monto"
						,"MontoCobro","MontoFinal","MontoPagado","IdCondicionLiquidacion","FechaLiquidacion","FechaEmision","FechaEnvio","FechaRespuestaEnvio"
						,"IdEstadoComprobante","Glosa","EsExento","FechaIngresoRegistro","FechaSistema","IdCarga","FolioComprobanteFacturacionMigracion","IdMoneda"
						,"IdMonedaComprobante","IdMonedaLiquidacion","MontoComprobante","MontoLiquidacion","IdConcepto","IdBolsa","FolioComprobanteReferencia"
						,"IdEmpleadoEjecutivoPortafolio","IdTipoComprobanteReferencia","IdComprobanteReferencia","IdSucursalEmpleadoEjecutivoPortafolio","Cantidad"
						,"ModPrefFact","FechaComprobanteReferencia","FechaComprobante","IdEstadoDTE","IdEstadoSII","RespuestaDTEEnvioSII","RespuestaXmlDTE","RespuestaPdfDTE"
						,"IdRespuestaDTE","RespuestaLocationDTE","NombreEmpleadoEjecutivoPortafolio","NombreSucursalEmpleadoEjecutivoPortafolio","MontoComprobanteFinal"
						,"MontoLiquidacionFinal","MontoComprobanteCobro","MontoLiquidacionCobro","RUT_CLI","SEC_RUT_CLI","TIPO_COMP_FAC" ,"FOLIO_COMP_FAC","MERCADO"
						,"PRODUCTO","FOLIO_COMP_ASOC","FOLIO_COMP_CONT","FEC_ING_COMP"
					)
		SELECT 
					GES_TBCTMSCF."FOLIO_COMP_FAC"											as "IdComprobante"
					,p_IdEmpresa 															as "IdEmpresa"
					,POR."IdPortafolio"														as "IdPortafolio"
					,Cliente."IdCliente"														as "IdCliente"
					,TRIM(GES_TBCTMSCF."RUT_CLI") 											as "DocumentoIdentidad"
					,'R' 																	as "TipoDocumentoIdentidad" 
					,CASE Persona."Tipo" 
					   WHEN 'N' THEN Persona."Nombres" ||' '|| Persona."ApellidoPaterno" || ' ' || Persona."ApellidoMaterno"
					 ELSE Persona."RazonSocial"
		             END                                                                   as "NombreLargoCliente" 
					,COALESCE (DIRECCION_FISICA."Calle",'') 								as "DireccionFacturacion" 
					,GES_TBCTMSCF."FOLIO_COMP_FAC" 											as "FolioComprobante" 
					,TIPOCOMPROBANTE."ValorDestino" 										as "IdTipoComprobante" 
					
					,CASE 
					   WHEN GES_TBCTMSCF."TIPO_COMP_FAC" = 'FD' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMSCF."TIPO_COMP_FAC" = 'FT' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMSCF."TIPO_COMP_FAC" = 'FX' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMSCF."TIPO_COMP_FAC" = 'ND' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMSCF."TIPO_COMP_FAC" = 'NT' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMSCF."TIPO_COMP_FAC" = 'NB' THEN	TipoOperacion."IdTipoOperacionAnulacion"
					   ELSE 'XXXXX'
					 END 					   												as "IdTipoOperacion"

					 ,coalesce(TIPOPRODUCTO."ValorDestino",'')								as "IdTipoProducto"
					 
					,COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') 			as "IdClasificacionTipoInstrumento" 
					,COALESCE(INSTRUMENTO."IdTipoInstrumento",'') 							as "IdTipoInstrumento" 
					,ABS (GES_TBCTMSCF."MONTO_COM" + GES_TBCTMSCF."MONTO_VEN" ) 			as "Monto"
					  
					,GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA" as "MontoCobro" 
					,CASE WHEN substring(rtrim(TipoOperacion."IdTipoOperacion"),1,1) = 'V' THEN ABS(GES_TBCTMSCF."MONTO_TOTAL") ELSE ABS(GES_TBCTMSCF."MONTO_TOTAL") * -1 END 		as "MontoFinal"
					,GES_TBCTMSCF."MONTO_PAGADO" 											as "MontoPagado" 
					,CONDICIONLIQUIDACION."IdCondicionDeLiquidacion" 						as "IdCondicionLiquidacion" 
					, CASE WHEN GES_TBCTMSCF."FEC_PAGO"  = '2018-02-29' THEN '2018-02-28' ELSE  GES_TBCTMSCF."FEC_PAGO" END 	as "FechaLiquidacion"
					,GES_TBCTMSCF."FEC_EMI_COMP" 											as "FechaEmision" 
					,GES_TBCTMSCF."FEC_EMI_COMP" 											as "FechaEnvio" 
					,'0001-01-01' 															as "FechaRespuestaEnvio" 
					,CASE WHEN ((GES_TBCTMSCF."EST_COMP") = 'E'  AND GES_TBCTMSCF."COD_CORR_CONTRA" ='999' ) THEN 'ENV'
					 WHEN ((GES_TBCTMSCF."EST_COMP") 	  = 'E'  AND GES_TBCTMSCF."COD_CORR_CONTRA" <>'999') THEN 'FAC'
					 WHEN ((GES_TBCTMSCF."EST_COMP") 	  = 'A') THEN 'ANU'
					 END 																	as "IdEstadoComprobante"
					 
					,'' 																	as "Glosa" 
										
					,CASE WHEN GES_TBCTMSCF."TIPO_COMP_FAC" IN ('FE','FX')  then 'S' 
					 ELSE 'N' 
					 END 																	as "EsExento"
					,GES_TBCTMSCF."FEC_EMI_COMP" 											as "FechaIngresoRegistro"
					,GES_TBCTMSCF."FEC_EMI_COMP"											as "fechaSistema"
					,p_IdCarga 																as "IdCarga" 
					,GES_TBCTMSCF."FOLIO_COMP_FAC" 											as "FolioComprobanteFacturacionMigracion" 

		           ,CASE 
		            	 WHEN TRIM(GES_TBCTMSCF."MERCADO") = ''  THEN 'CLP'
		            ELSE
						CASE WHEN  TRIM(GES_TBCTMSCF."MON_MORIGEN")  = '' THEN  'CLP'
							 ELSE COALESCE(GES_TBCTMSCF."MON_MORIGEN",'CLP') 
						 END	 																
					END																		as "IdMoneda" 
		           ,CASE 
		            	 WHEN TRIM(GES_TBCTMSCF."MERCADO") = ''  THEN 'CLP'
		            ELSE
						CASE WHEN  TRIM(GES_TBCTMSCF."MON_MORIGEN")  = '' THEN  'CLP'
							 ELSE COALESCE(GES_TBCTMSCF."MON_MORIGEN",'CLP') 
						 END	 																
					END																		as "IdMonedaComprobante" 
		           /*,CASE 
		            	 WHEN TRIM(GES_TBCTMSCF."MERCADO") = ''  THEN 'CLP'
		            ELSE
						CASE WHEN  TRIM(GES_TBCTMSCF."MON_MORIGEN")  = '' THEN  'CLP'
							 ELSE COALESCE(GES_TBCTMSCF."MON_MORIGEN",'CLP') 
						 END	 																
					END																		as "IdMonedaLiquidacion" */
					,'CLP'                                                                   as "IdMonedaLiquidacion" 

					 
				    --,GES_TBCTMSCF."CAPTACION_MORIGEN"										as "MontoComprobante"
				    ,ABS(GES_TBCTMSCF."MONTO_COM" + GES_TBCTMSCF."MONTO_VEN" ) 				as "MontoComprobante"
					,ABS(GES_TBCTMSCF."MONTO_COM" + GES_TBCTMSCF."MONTO_VEN" ) 				as "MontoLiquidacion"
					
					,(CASE TipoOperacion."IdTipoOperacion" WHEN 'COMACN' THEN 'CMACNA'
				                                    WHEN 'COMACX' THEN 'CMACEX'
				                                    WHEN 'COMCFI' THEN 'CMCFIN'
				                                    WHEN 'COMETN' THEN 'CMETFN'
				                                    WHEN 'COMETX' THEN 'CMETFX'
				                                    WHEN 'COMIIF' THEN 'CMINFI'
				                                    WHEN 'COMMIL' THEN 'CMACMI'
				                                    WHEN 'COMOTC' THEN 'COMPUT'
				                                    WHEN 'COMRF'  THEN 'CMINRF'
				                                    WHEN 'COMSIM' THEN 'CMSIMU'
				                                    WHEN 'COMSPT' THEN 'CMMESP'
				                                    WHEN 'COMOSA' THEN 'CMOSAC'
				                                    WHEN 'EMPRES' THEN 'VENEMP'
				                                    WHEN 'VENACN' THEN 'VNACNA'
				                                    WHEN 'VENACX' THEN 'VNACEX'
				                                    WHEN 'VENCFI' THEN 'VNCFIN'
				                                    WHEN 'VENETN' THEN 'VNETFN'
				                                    WHEN 'VENETX' THEN 'VNETFX'
				                                    WHEN 'VENIIF' THEN 'VNINRF'
				                                    WHEN 'VENMIL' THEN 'VNACMI'
				                                    WHEN 'VENOTC' THEN 'OTROPE'
				                                    WHEN 'VENRF'  THEN 'VNINRF'
				                                    WHEN 'VENSIM' THEN 'VNSIMU'
				                                    WHEN 'VENOSA' THEN 'VNOSAC'
				                                    WHEN 'VENSPT' THEN 'VNMESP' else 'FACSIN' END) as "IdConcepto"
					,'BCS' 																	as "IdBolsa"

					,CASE 
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FD'   THEN  GES_TBCTMSCF."FOLIO_COMP_ASOC"
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FT'   THEN  GES_TBCTMSCF."FOLIO_COMP_ASOC"         
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FX'   THEN  GES_TBCTMSCF."FOLIO_COMP_ASOC"         
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FA'   THEN  GES_TBCTMSCF."FOLIO_COMP_ASOC"
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FO'   THEN  GES_TBCTMSCF."FOLIO_COMP_ASOC"
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FE'   THEN  GES_TBCTMSCF."FOLIO_COMP_ASOC"
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'ND'   THEN  GES_TBCTMSCF."FOLIO_COMP_CONT"
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NT'   THEN  GES_TBCTMSCF."FOLIO_COMP_CONT"         
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NC'   THEN  GES_TBCTMSCF."FOLIO_COMP_CONT"
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NO'   THEN  GES_TBCTMSCF."FOLIO_COMP_CONT"
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NO'   THEN  GES_TBCTMSCF."FOLIO_COMP_CONT"
									   ELSE 0
					 END 																	as "FolioComprobanteReferencia"		 


					,COALESCE(POR."IdEmpleadoEjecutivo",0) 									as "IdEmpleadoEjecutivoPortafolio"
					,CASE 
							WHEN GES_TBCTMSCF."FOLIO_COMP_ASOC" > 0 THEN
								 CASE  
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FD'   THEN  'ND'
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FT'   THEN  'NT'          
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FX'   THEN  'NT'          
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FA'   THEN  'NC'
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FO'   THEN  'NO'
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'FE'   THEN  'NO'
									   ELSE ''
								 END						   
							WHEN GES_TBCTMSCF."FOLIO_COMP_CONT" > 0 THEN
								 CASE  

									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'ND'   THEN  'FD'
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NT'   THEN  'FT'         
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NC'   THEN  'FA'
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NO'   THEN  'FO'
									   WHEN  GES_TBCTMSCF."TIPO_COMP_FAC" = 'NO'   THEN  'FE'
									   ELSE ''
								 END						  
							ELSE ''	 
					 END 																	as "IdTipoComprobanteReferencia"
					,0 																		as "IdComprobanteReferencia" 		
					,coalesce(IDAGENTE."IdSucursal",0) 										as "IdSucursalEmpleadoEjecutivoPortafolio"
					,DtcfCant."CantidaDet"													as "Cantidad" 
					,0 																		as "ModPrefFact"
					,'0001-01-01' 															as "FechaComprobanteReferencia"
					,GES_TBCTMSCF."FEC_EMI_COMP" 											as "FechaComprobante"
					
					,CASE 	WHEN (GES_TBCTMSCF."EST_COMP" = 'E' AND GES_TBCTMSCF."COD_CORR_CONTRA"  = '999') THEN 'ENV'
					  		WHEN (GES_TBCTMSCF."EST_COMP" = 'E' AND GES_TBCTMSCF."COD_CORR_CONTRA" <> '999') THEN 'PEN'
					  		WHEN (GES_TBCTMSCF."EST_COMP" = 'A') THEN 'ANU'
					 END 																	as "IdEstadoEnvioDTE"  				
					,'ENV' 																	as "IdEstadoSII"
					,'' 																	as "RespuestaDTEEnvioSII"
					,'' 																	as "RespuestaXmlDTE"
					,'' 																	as "RespuestaPdfDTE"
					,0 																		as "IdRespuestaDTE"
					,'' 																	as "RespuestaLocationDTE"
					,LEFT(coalesce(PersonaEjecutivo."Nombres"||' '||PersonaEjecutivo."ApellidoPaterno"||' '||PersonaEjecutivo."ApellidoMaterno",''),100)  as "NombreEmpleadoEjecutivoPortafolio"
					,coalesce(SUCURSAL."NombreSucursal",'') 								as "NombreSucursalEmpleadoEjecutivoPortafolio"

					,CASE WHEN substring(rtrim(TipoOperacion."IdTipoOperacion"),1,1) = 'V' THEN ABS(GES_TBCTMSCF."MONTO_TOTAL") ELSE ABS(GES_TBCTMSCF."MONTO_TOTAL") * -1 END 		as "MontoComprobanteFinal"
					,CASE WHEN substring(rtrim(TipoOperacion."IdTipoOperacion"),1,1) = 'V' THEN ABS(GES_TBCTMSCF."MONTO_TOTAL") ELSE ABS(GES_TBCTMSCF."MONTO_TOTAL") * -1 END 		as "MontoLiquidacionFinal"
					
					,(GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA")  as "MontoComprobanteCobro" 
					,(GES_TBCTMSCF."MONTO_COMISION"+GES_TBCTMSCF."MONTO_DERECHOS"+GES_TBCTMSCF."MONTO_GASTO"+GES_TBCTMSCF."MONTO_IVA") as "MontoLiquidacionCobro" 
					,GES_TBCTMSCF."RUT_CLI" 												as "RUT_CLI" 
					,GES_TBCTMSCF."SEC_RUT_CLI" 											as "SEC_RUT_CLI" 
					,GES_TBCTMSCF."TIPO_COMP_FAC" 											as "TIPO_COMP_FAC" 
					,GES_TBCTMSCF."FOLIO_COMP_FAC" 											as "FOLIO_COMP_FAC"  
					,GES_TBCTMSCF."MERCADO" 												as "MERCADO" 
					,GES_TBCTMSCF."PRODUCTO" 												as "PRODUCTO" 
					,GES_TBCTMSCF."FOLIO_COMP_ASOC" 										as "FOLIO_COMP_ASOC"
					,GES_TBCTMSCF."FOLIO_COMP_CONT" 										as "FOLIO_COMP_CONT"
					,GES_TBCTMSCF."FEC_ING_COMP" 											as "FEC_ING_COMP"
	   

			FROM "BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF 
		
				LEFT  JOIN	"BSCL_VIRTUAL"."GES_TBCTDTCF" AS DTCF 
						ON  TRIM(DTCF."TIPO_COMP_FAC")  = TRIM(GES_TBCTMSCF."TIPO_COMP_FAC")
						AND      DTCF."FOLIO_COMP_FAC"  =      GES_TBCTMSCF."FOLIO_COMP_FAC"
						AND 	 DTCF."LIN_COMP_FAC"	= 1
				
				LEFT  JOIN	( SELECT 	DETALLE."TIPO_COMP_FAC" ,
										DETALLE."FOLIO_COMP_FAC", 
										SUM(DETALLE."CANTIDAD") AS "CantidaDet"
							    FROM 	"BSCL_VIRTUAL"."GES_TBCTDTCF"  as DETALLE
							   GROUP	BY 	DETALLE."TIPO_COMP_FAC", DETALLE."FOLIO_COMP_FAC"
							) as DtcfCant
							
						ON  TRIM(DtcfCant."TIPO_COMP_FAC")  = TRIM(GES_TBCTMSCF."TIPO_COMP_FAC")
						AND      DtcfCant."FOLIO_COMP_FAC"  =      GES_TBCTMSCF."FOLIO_COMP_FAC"
							
				--LEFT  JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
						--ON 	TRIM(UPPER(CLI."DocumentoIdentidad")) = TRIM(UPPER(GES_TBCTMSCF."RUT_CLI"))
						
				LEFT JOIN 	BSCL."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocIdentidad
				       ON 	TRIM(UPPER(DocIdentidad."DocumentoIdentidad")) = TRIM(UPPER(GES_TBCTMSCF."RUT_CLI"))
				LEFT JOIN BSCL."BSCL.SocioNegocio.BD::PER.Persona" AS Persona
				       ON Persona."IdPersona" = DocIdentidad."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente 
				       ON Cliente."IdPersona" = Persona."IdPersona" 		
		
						
				LEFT  JOIN 	"BSCL.Portafolio.BD::POR.Portafolio" POR
						ON 	POR."IdCliente" = Cliente."IdCliente"
						AND POR."IdCuentaOrigen" = GES_TBCTMSCF."SEC_RUT_CLI"||''
						AND POR."CodigoPortafolio" = GES_TBCTMSCF."SEC_RUT_CLI"||''
						
				LEFT  JOIN 	"BSCL.SocioNegocio.BD::EMP.Empleado" AS IDAGENTE
						ON 	IDAGENTE."IdEmpleado" = POR."IdEmpleadoEjecutivo"

				LEFT  JOIN 	"BSCL.Plataforma.BD::PLA.Sucursal" AS SUCURSAL
						ON 	SUCURSAL."IdSucursal" = 	IDAGENTE."IdSucursal"

				LEFT  JOIN 	"BSCL.SocioNegocio.BD::PER.Persona" AS PersonaEjecutivo
						ON 	PersonaEjecutivo."IdPersona" = IDAGENTE."IdPersona"

				LEFT  JOIN 	"BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIRECCION_FISICA
						ON 	DIRECCION_FISICA."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
						
				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TIPOCOMPROBANTE
						ON  TIPOCOMPROBANTE."TipoValor" = 'TipoComprobante' 
						AND TIPOCOMPROBANTE."ValorOrigen" = TRIM(GES_TBCTMSCF."TIPO_COMP_FAC")

				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 	as Instrumento
				    	ON 	Instrumento."Nemotecnico" = 
						    	CASE WHEN (GES_TBCTMSCF."IND_SIMUL" = 'TS' OR GES_TBCTMSCF."IND_SIMUL" = 'TP') THEN
						                  CASE WHEN  DTCF."TIPO_OPERAC" = 'V' THEN 'SIMULTANEA VENTA'
						                       ELSE  'SIMULTANEA COMPRA'
						                  END
						             WHEN GES_TBCTMSCF."IND_SIMUL" 	= 'VC' THEN 'PRESTAMO VENTA CORTA'
						             WHEN TRIM(DTCF."NEMO") = ''	  THEN 'CHILE'
						             WHEN TRIM(DTCF."NEMO") = 'DOLAR' THEN 'DISTRI' 
						             ELSE IFNULL(TRIM(DTCF."NEMO"),'')    
		                        END  
						    
				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento
				    	ON 	TipoInstrumento."IdTipoInstrumento" =  
				                  CASE WHEN  TRIM(DTCF."NEMO") = '' THEN 'ORV'
				                       ELSE  Instrumento."IdTipoInstrumento"
				                  END
						
				LEFT  JOIN	"BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CONDICIONLIQUIDACION
						ON 	CONDICIONLIQUIDACION."Codigo" = CASE WHEN (IFNULL(GES_TBCTMSCF."IND_LIQUID",'')= 'CN' or trim(GES_TBCTMSCF."IND_LIQUID") = '') THEN 'CN' ELSE GES_TBCTMSCF."IND_LIQUID" END   
						

				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
						ON  TIPOPRODUCTO."TipoValor" 			= 'PlantillaProducto' 
						AND TRIM(TIPOPRODUCTO."ValorOrigen") 	= TRIM(GES_TBCTMSCF."PRODUCTO")
						
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  AS TipoOperacion
						ON	TipoOperacion."EsMovimientoComprobante" = 'S' 
						AND TipoOperacion."IdTipoOperacion" = 

							(    CASE WHEN GES_TBCTMSCF."PRODUCTO" = 'INRF' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMRF'
					       					 ELSE 'VENRF'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'INMO' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMMON'
					       					 ELSE 'VENMON'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'INEM' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMETN'
					       					 ELSE 'VENETN'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'INIF' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMIIF'
					       					 ELSE 'VENIIF'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMCFI'
					       					 ELSE 'VENCFI'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'MDOL' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMSPT'
					       					 ELSE 'VENSPT'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'INTS' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
					       		
					       		WHEN GES_TBCTMSCF."PRODUCTO" = 'INAC' and GES_TBCTMSCF."IND_SIMUL" = 'TS'  THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
					       		WHEN GES_TBCTMSCF."PRODUCTO" = 'INAC' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMACN'
					       					 ELSE 'VENACN'
					       				END
								---Jk finalmente todas las facturas con la marca ts en el ind_simul se dejan como simultaneas independiente del subproducto
								WHEN GES_TBCTMSCF."IND_SIMUL" = 'TS'  THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMCFX'
					       					 ELSE 'VENCFX'
					       				END
								WHEN GES_TBCTMSCF."PRODUCTO" = 'INVC' THEN
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'PRESTA'
					       					 ELSE 'EMPRES'
					       				END
								WHEN TRIM(GES_TBCTMSCF."PRODUCTO") = '' THEN 
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN 'COMOTC'
					       					 ELSE 'VENOTC'
					       				END
								ELSE
					       				CASE WHEN DTCF."TIPO_OPERAC" = 'C' THEN CONCAT('COM',INSTRUMENTO."IdTipoInstrumento")
					       					 ELSE CONCAT('VEN',INSTRUMENTO."IdTipoInstrumento")
					       				END
					       		END)

				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" as HOM_COMP
						ON 	HOM_COMP."FOLIO_COMP_FAC" = GES_TBCTMSCF."FOLIO_COMP_FAC"
						AND HOM_COMP."TIPO_COMP_FAC" = GES_TBCTMSCF."TIPO_COMP_FAC"
				
		WHERE 	GES_TBCTMSCF."FEC_ING_COMP" BETWEEN SUBSTRING(P_FechaInicial,1,10)  AND SUBSTRING(P_FechaFinal,1,10)
		AND 	HOM_COMP."FOLIO_COMP_FAC" IS NULL
		AND 	TRIM(GES_TBCTMSCF."RUT_CLI") <> '0-0';
--		AND 	INSTRUMENTO."IdTipoInstrumento" IS NOT NULL
--		AND 	CLI."IdCliente"	 IS NOT NULL; 
		    

	  	-----------------------------------------------------
		-- Detalle de Master de Facturas --aqui
		-----------------------------------------------------
		insert into "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle"
			(
				 "IdComprobanteDetalle","IdComprobante","NroLinea","FechaLiquidacion","IdInstrumento","NemotecnicoInstrumento","IdTipoOperacion","IdTipoProducto"
				,"Cantidad","Precio","Monto","IdBolsa","IdEstadoComprobanteDetalle","FechaIngresoRegistro","IdCarga","FolioComprobanteFacturacionMigracion"
				,"IdMoneda","IdMonedaComprobanteDetalle","IdMonedaLiquidacion","MontoComprobanteDetalle","MontoLiquidacion","IdTransaccion","IdTipoInstrumentoSubyacente"
				,"NemotecnicoSubyacente","NombreClasificacionTipoInstrumentoRelacionado","IdClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado"
				,"IdInstrumentoSubyacente","IdTipoInstrumento","NombreTipoInstrumento","IdTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumento"
				,"FechaComprobanteDetalle","IdClasificacionTipoInstrumento","IdInstrumentoRelacionado","NemotecnicoRelacionado","IdMonedaSubyacente"
				,"NombreTipoInstrumentoSubyacente","IdSucursalEmpleadoEjecutivoPortafolio","IdConcepto","IdEmpleadoEjecutivoPortafolio","ModPrefFact"
				--,"IdInstrumentoNuevo"
				,"IdMesa",
				"RUT_CLI","SEC_RUT_CLI","LINEA_COMP_FAC","FOLIO_COMP_FAC","TIPO_COMP_FAC"
				,"COD_MOVTO","MERCADO","FEC_VCTO","TIPO_OPERAC","IdMonedaEmision"
			)
		SELECT
				 "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL 		as "IdComprobanteDetalle" 
				, Comprobante."IdComprobante" 													as "IdComprobante" 
				, TBCTDTCF."LIN_COMP_FAC" 														as "NroLinea" 

				, CASE WHEN TBCTMSCF."FEC_PAGO"  = '2018-02-29' THEN '2018-02-28' ELSE  TBCTMSCF."FEC_PAGO" END 	as "FechaLiquidacion"
				
				, CASE WHEN TBCTDTCF."NEMO" = '' THEN 0 
					   ELSE COALESCE(Instrumento."IdInstrumento",0) 
				  END 																			as "IdInstrumento" 
				, CASE WHEN TBCTDTCF."NEMO" = '' THEN '' 
					   ELSE COALESCE (Instrumento."Nemotecnico",'') 
				  END 																			as "NemotecnicoInstrumento" 
				,CASE 
					   WHEN TBCTDTCF."TIPO_COMP_FAC" = 'FD' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN TBCTDTCF."TIPO_COMP_FAC" = 'FT' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN TBCTDTCF."TIPO_COMP_FAC" = 'FX' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN TBCTDTCF."TIPO_COMP_FAC" = 'ND' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN TBCTDTCF."TIPO_COMP_FAC" = 'NT' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN TBCTDTCF."TIPO_COMP_FAC" = 'NB' THEN	TipoOperacion."IdTipoOperacionAnulacion"
					   ELSE 'XXXXX'
					 END 					   												as "IdTipoOperacion"

				,coalesce(TIPOPRODUCTO."ValorDestino",'')								as "IdTipoProducto"
				,TBCTDTCF."CANTIDAD" 															as "Cantidad" 
				,TBCTDTCF."PRECIO" 																as "Precio" 
				,TBCTDTCF."MONTO" 																as "Monto" 
				,COALESCE(TBCTMSCF."IND_BOLSA",'') 												as "IdBolsa" 
				,CASE WHEN ((TBCTMSCF."EST_COMP") = 'E'  AND TBCTMSCF."COD_CORR_CONTRA" ='999' ) THEN 'ENV'
					  WHEN ((TBCTMSCF."EST_COMP") = 'E'  AND TBCTMSCF."COD_CORR_CONTRA" <>'999') THEN 'FAC'
					  WHEN ((TBCTMSCF."EST_COMP") = 'A') THEN 'ANU'
				 END 																	as "IdEstadoComprobanteDetalle"

				,COALESCE(TBCTMSCF."FEC_ING_COMP",'0001-01-01') 								as "FechaIngresoRegistro" 
				,p_IdCarga 																		as "IdCarga" 
				,COALESCE(TBCTMSCF."FOLIO_COMP_FAC",0) 											as "FolioComprobanteFacturacionMigracion" 
				,'CLP' 																			as "IdMoneda" 
				,'CLP' 																			as "IdMonedaComprobanteDetalle" 
				,'CLP' 																			as "IdMonedaLiquidacion" 
				,TBCTDTCF."MONTO" 																as "MontoComprobanteDetalle" 
				,TBCTDTCF."MONTO" 																as "MontoLiquidacion" 
				,0 -- COALESCE(TRANSACCION."IdTransaccion",0) as "IdTransaccion"
				,CASE WHEN TBCTDTCF."NEMO" = '' THEN '' 
				      ELSE COALESCE(Instrumento."IdTipoInstrumento",'') 
				 END 																			as "IdTipoInstrumentoSubyacente" 
				,CASE WHEN TBCTDTCF."NEMO" = '' THEN '' 
				      ELSE COALESCE (Instrumento."Nemotecnico",'') 
				  END 																			as "NemotecnicoSubyacente" 
				, '' 																			as "NombreClasificacionTipoInstrumentoRelacionado"
				, '' 																			as "IdClasificacionTipoInstrumentoRelacionado"
				, '' 																			as "NombreTipoInstrumentoRelacionado"
				,CASE WHEN TBCTDTCF."NEMO" = '' THEN 0 
					   ELSE COALESCE(Instrumento."IdInstrumento",0) 
				  END 																			as "IdInstrumentoSubyacente" 
				
				,CASE WHEN TBCTDTCF."NEMO" = '' THEN '' 
				      ELSE COALESCE(Instrumento."IdTipoInstrumento",'') 
				 END 																			as "IdTipoInstrumento" 
				,COALESCE(TipoInstrumento."Nombre",'')  										as "NombreTipoInstrumento" 
				, '' 																			as "IdTipoInstrumentoRelacionado"
				,COALESCE(TipoInstrumento."Nombre",'') 											as "NombreClasificacionTipoInstrumento" 
				,COALESCE(TBCTMSCF."FEC_ING_COMP",'0001-01-01')									as "FechaComprobanteDetalle" 
				,COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'') 					as "IdClasificacionTipoInstrumento" 
				, 0 																			as "IdInstrumentoRelacionado"
				,'' 																			as "NemotecnicoRelacionado"
				,CASE WHEN TBCTDTCF."NEMO" = '' THEN '' 
				      ELSE COALESCE(Instrumento."IdMoneda",'CLP') 
				 END 																			as "IdMonedaSubyacente" 
				,COALESCE(TipoInstrumento."Nombre",'')  										as "NombreTipoInstrumentoSubyacente" 
				,Comprobante."IdSucursalEmpleadoEjecutivoPortafolio"							as "IdSucursalEmpleadoEjecutivoPortafolio"
				,(CASE TipoOperacion."IdTipoOperacion" WHEN 'COMACN' THEN 'CMACNA'
				                                    WHEN 'COMACX' THEN 'CMACEX'
				                                    WHEN 'COMCFI' THEN 'CMCFIN'
				                                    WHEN 'COMETN' THEN 'CMETFN'
				                                    WHEN 'COMETX' THEN 'CMETFX'
				                                    WHEN 'COMIIF' THEN 'CMINFI'
				                                    WHEN 'COMMIL' THEN 'CMACMI'
				                                    WHEN 'COMOTC' THEN 'COMPUT'
				                                    WHEN 'COMRF'  THEN 'CMINRF'
				                                    WHEN 'COMSIM' THEN 'CMSIMU'
				                                    WHEN 'COMSPT' THEN 'CMMESP'
				                                    WHEN 'COMOSA' THEN 'CMOSAC'
				                                    WHEN 'EMPRES' THEN 'VENEMP'
				                                    WHEN 'VENACN' THEN 'VNACNA'
				                                    WHEN 'VENACX' THEN 'VNACEX'
				                                    WHEN 'VENCFI' THEN 'VNCFIN'
				                                    WHEN 'VENETN' THEN 'VNETFN'
				                                    WHEN 'VENETX' THEN 'VNETFX'
				                                    WHEN 'VENIIF' THEN 'VNINRF'
				                                    WHEN 'VENMIL' THEN 'VNACMI'
				                                    WHEN 'VENOTC' THEN 'OTROPE'
				                                    WHEN 'VENRF'  THEN 'VNINRF'
				                                    WHEN 'VENSIM' THEN 'VNSIMU'
				                                    WHEN 'VENOSA' THEN 'VNOSAC'
				                                    WHEN 'VENSPT' THEN 'VNMESP' else 'FACSIN' END) as "IdConcepto"
				,Comprobante."IdEmpleadoEjecutivoPortafolio"									as "IdEmpleadoEjecutivoPortafolio"
				,0 																				as "ModPreFact"
				--, 0 																			as "IdInstrumentoNuevo"
				, 0 																			as "IdMesa"
				,TBCTMSCF."RUT_CLI" 															as "RUT_CLI"
				,TBCTMSCF."SEC_RUT_CLI" 														as "SEC_RUT_CLI"
				,TBCTDTCF."LIN_COMP_FAC" 														as "LINEA_COMP_FAC"
				,TBCTDTCF."FOLIO_COMP_FAC" 														as "FOLIO_COMP_FAC"
				,TBCTDTCF."TIPO_COMP_FAC" 														as "TIPO_COMP_FAC"
				,TBCTDTCF."COD_MOVTO" 															as "COD_MOVTO"
				,TBCTDTCF."MERCADO" 															as "MERCADO"
				,TBCTDTCF."FEC_VCTO" 															as "FEC_VCTO"
				,TBCTDTCF."TIPO_OPERAC" 														as "TIPO_OPERAC"
				,COALESCE(Instrumento."IdMonedaEmision",'CLP')									AS "IdMonedaEmision"

				
		FROM  "BSCL_VIRTUAL"."GES_TBCTDTCF" as  TBCTDTCF

				INNER  JOIN	"BSCL.Homologacion.BD::HOM.Comprobante" as Comprobante
						ON 	Comprobante."TIPO_COMP_FAC"  = TBCTDTCF."TIPO_COMP_FAC"
						AND Comprobante."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
		
				LEFT JOIN 	"BSCL_VIRTUAL"."GES_TBCTMSCF" as TBCTMSCF
						ON 	TBCTDTCF."TIPO_COMP_FAC" 	= TBCTMSCF."TIPO_COMP_FAC"
						AND TBCTDTCF."FOLIO_COMP_FAC" 	= TBCTMSCF."FOLIO_COMP_FAC"
						
				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
						ON  TIPOPRODUCTO."TipoValor" 			= 'PlantillaProducto' 
						AND TRIM(TIPOPRODUCTO."ValorOrigen") 	= TRIM(TBCTMSCF."PRODUCTO")
						


				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 	as Instrumento
				    	ON 	Instrumento."Nemotecnico" = 
				    	CASE 
				    	/*
				    	WHEN (TBCTMSCF."IND_SIMUL" = 'TS' OR TBCTMSCF."IND_SIMUL" = 'TP') THEN
				                  CASE WHEN  TBCTDTCF."TIPO_OPERAC" = 'V' THEN 'SIMULTANEA VENTA'
				                       ELSE  'SIMULTANEA COMPRA'
				                  END
						*/
				             WHEN TBCTMSCF."IND_SIMUL" 	= 'VC' THEN 'PRESTAMO VENTA CORTA'
				             WHEN TRIM(TBCTDTCF."NEMO") = ''	  THEN 'CHILE'
				             ELSE IFNULL(TRIM(TBCTDTCF."NEMO"),'')    
                        END  
				    


				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento
				    	ON 	TipoInstrumento."IdTipoInstrumento" =  
				                  CASE WHEN  TRIM(TBCTDTCF."NEMO") = '' THEN 'ORV'
				                       ELSE  Instrumento."IdTipoInstrumento"
				                  END
	
		  	   	
				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS ComprobanteDetalle
			    		ON 	ComprobanteDetalle."FOLIO_COMP_FAC"	= TBCTDTCF."FOLIO_COMP_FAC"
						AND ComprobanteDetalle."TIPO_COMP_FAC" 	= TBCTDTCF."TIPO_COMP_FAC"
						AND ComprobanteDetalle."LINEA_COMP_FAC"	= TBCTDTCF."LIN_COMP_FAC"
				
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  AS TipoOperacion
						ON	TipoOperacion."EsMovimientoComprobante" = 'S' 
						AND TipoOperacion."IdTipoOperacion" = 

							(    CASE WHEN TBCTMSCF."PRODUCTO" = 'INRF' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMRF'
					       					 ELSE 'VENRF'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INMO' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMMON'
					       					 ELSE 'VENMON'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INEM' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMETN'
					       					 ELSE 'VENETN'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INIF' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMIIF'
					       					 ELSE 'VENIIF'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMCFI'
					       					 ELSE 'VENCFI'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'MDOL' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMSPT'
					       					 ELSE 'VENSPT'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INTS' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
					       		
					       		WHEN TBCTMSCF."PRODUCTO" = 'INAC' and TBCTMSCF."IND_SIMUL" = 'TS'  THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
								---Jk finalmente todas las facturas con la marca ts en el ind_simul se dejan como simultaneas independiente del subproducto
								WHEN TBCTMSCF."IND_SIMUL" = 'TS'  THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMCFX'
					       					 ELSE 'VENCFX'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INCF' AND Instrumento."IdTipoInstrumento" = 'OSA' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMOSA'
					       					 ELSE 'VENOSA'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INVC' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'PRESTA'
					       					 ELSE 'EMPRES'
					       				END
								WHEN TBCTMSCF."PRODUCTO" = 'INAC' AND Instrumento."IdTipoInstrumento" = 'OSA' THEN
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMOSA'
					       					 ELSE 'VENOSA'
					       				END
								WHEN TRIM(TBCTMSCF."PRODUCTO") = '' THEN 
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN 'COMOTC'
					       					 ELSE 'VENOTC'
					       				END
								ELSE
					       				CASE WHEN TBCTDTCF."TIPO_OPERAC" = 'C' THEN CONCAT('COM',INSTRUMENTO."IdTipoInstrumento")
					       					 ELSE CONCAT('VEN',INSTRUMENTO."IdTipoInstrumento")
					       				END
					       		END)
					
		WHERE 	Comprobante."FEC_ING_COMP" BETWEEN SUBSTRING(P_FechaInicial,1,10)  AND SUBSTRING(P_FechaFinal,1,10)
		  AND 	ComprobanteDetalle."FOLIO_COMP_FAC" IS NULL;


insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobante"
		("IdMovimientoComprobante"
,"IdEmpresa"
,"IdConceptoMovimiento"
,"SecuenciaAgrupacion"
,"IdCliente"
,"IdPortafolio"
,"IdInstrumento"
,"IdInstrumentoSubyacente"
,"IdTipoInstrumento"
,"IdTipoInstrumentoSubyacente"
,"IdClasificacionTipoInstrumento"
,"IdTipoProducto"
,"IdCuentaInversion"
,"IdMoneda"
,"IdTipoComprobante"
,"IdComprobante"
,"IdTipoOperacion"
,"IdBoveda"
,"IdCorredoraContraparte"
,"IdCondicionLiquidacion"
,"FechaLiquidacion"
,"IdBolsa"
,"IdTipoReajuste"
,"IdTipoConvenio"
,"IdEstadoFacturacion"
,"IdOrden"
,"IdAsignacion"
,"IdTipoCanalIngreso"
,"FechaOrden"
,"IdTransaccion"
,"FolioTransaccion"
,"FechaTransaccion"
,"FechaVencimiento"
,"FechaParidad"
,"ValorParidad"   
,"ValorCaptacionMonedaOrigen"
,"FolioComprobante"
,"Tasa"
,"Cantidad"
,"Precio"
,"Monto"
,"MontoDescuento"
,"MontoInteres"
,"Glosa"
,"ValorRescate"
,"TasaAgente"
,"Spread"
,"DiasPlazo"
,"CodigoCanalIngreso"
,"DocumentoIdentidad"
,"TipoDocumentoIdentidad"
,"NombreLargoCliente"
,"DireccionFacturacion"
,"FolioCartera"
,"CodigoTipoProducto"
,"CodigoTipoOperacion"
,"CodigoBolsa"
,"NombreTipoInstrumento"
,"NombreTipoInstrumentoSubyacente"
,"NombreClasificacionTipoInstrumento"
,"NemotecnicoInstrumento"
,"NemotecnicoSubyacente"
,"RazonSocialEmisor"
,"FechaEmision"
,"TasaEmision"
,"FechaIngresoRegistro"
,"FechaSistema"
,"EsComprobIndividual"
,"EsCuentaPropia"
,"EsPrecioPrima"
,"EsEnRueda"
,"EsRuteo"
,"EsIngresoAutomatico"
,"IdUsuario"
,"IdUsuarioBloqueo"
,"IdCarga"
,"EsAPlazo"
,"MontoAPlazo"
,"PrecioAPlazo"
,"IdMonedaSubyacente"
,"IdMonedaLiquidacion"
,"FechaTipoCambio"
,"ValorTipoCambio"
,"EsVentaCorta"
,"SecMovimientoFacturarMigracion"
,"FechaMovimientoComprobante"
,"ActualizarCustodia"
,"IdMonedaMovimientoComprobante"
,"MontoMovimientoComprobante"
,"MontoLiquidacion"
,"ActualizarCustodiaRelacionado"
,"IdEmisor"
,"EsFungible"
,"IdFormaCustodia"
,"ActualizarCuentaInversion"
,"ActualizarTesoreria"
,"IdClasificacionTipoInstrumentoRelacionado"
,"IdInstrumentoRelacionado"
,"IdAsignacionRef"
,"NombreTipoInstrumentoRelacionado"
,"NombreClasificacionTipoInstrumentoRelacionado"
,"IdTipoInstrumentoRelacionado"
,"NemotecnicoRelacionado"
,"RUT_CLI"
,"SEC_RUT_CLI"
,"TIPO_COMP_FAC"
,"FOLIO_COMP_FAC"
,"SEC_MOVTO_FAC"
,"FolioComprobanteReferencia"
,"IdComprobanteReferencia"
,"NombreSucursalEmpleadoDigitador"
,"CodigoIFRS"
,"SEC_MOVTO"
,"NombreEmpleadoDigitador"
,"NombreSucursalEmpleadoEjecutivoPortafolio"
,"ModPrefFact"
,"IdEmpleadoDigitador"
,"FechaComprobanteReferencia"
,"IdEmpleadoEjecutivoPortafolio"
,"NombreEmpleadoEjecutivoPortafolio"
,"IdMovimientoRelacionado"
,"LIN_COMP_ADJ"
,"IdSucursalEmpleadoEjecutivoPortafolio"
,"IdMovimientoOdRelacionado"
,"IdTipoComprobanteReferencia"
,"IdSucursalEmpleadoDigitador"
,"IdMesa"
,"NemoSVS"
,"IdInstrumentoAnterior"
,"IdMonedaSpot"
,"IndicadorOrdenDirecta"
,"FEC_MOVTO"
,"IdMonedaEmision") 
		select 
				"BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL  	as "IdMovimientoComprobante"
				,p_IdEmpresa 														as "IdEmpresa"
--				,GES_TBCTMFRV."CONCEPTO_MVTO"										as "IdConceptoMovimiento"
				,Comprobante."IdConcepto"											as "IdConceptoMovimiento"
--	  			,CASE WHEN TRIM(GES_TBCTMVTO."HOM_TIPO_CON")<>'' THEN  TRIM(GES_TBCTMVTO."HOM_TIPO_CON")|| TRIM(GES_TBCTMVTO."HOM_CON_CONCEPTO")  ELSE   'FACSIN' END	as "IdConceptoMovimiento"
				
				,0  																as "SecuenciaAgrupacion"
				,Cliente."IdCliente" 												as "IdCliente"
				,Portafolio."IdPortafolio" 											as "IdPortafolio"
				,coalesce(Instrumento."IdInstrumento",0) 							as "IdInstrumento"
				,coalesce(Instrumento."IdInstrumento",0)							as "IdInstrumentoSubyacente"
				,coalesce(Instrumento."IdTipoInstrumento",'') 						as "IdTipoInstrumento"
				,coalesce(Instrumento."IdTipoInstrumento",'')						as "IdTipoInstrumentoSubyacente"
				,coalesce(TipoInstrumento."IdClasificacionTipoInstrumento",'') 		as "IdClasificacionTipoInstrumento" 
				,Comprobante."IdTipoProducto" 										as "IdTipoProducto"
 				,CuentaInversion."IdCuentaInversion"  								as "IdCuentaInversion"
				,CASE WHEN  CuentaInversion."IdMoneda"   = '' THEN  'CLP'
					 ELSE COALESCE(CuentaInversion."IdMoneda" ,'CLP') 
				 END	 															as "IdMoneda" 
				,TipoComprobante."ValorDestino" 									as "IdTipoComprobante"
				,coalesce(Comprobante."IdComprobante",0) 							as "IdComprobante"
--				,coalesce(TipoOperacion."IdTipoOperacion",'')						as "IdTipoOperacion"
				,CASE 
					   WHEN GES_TBCTMFRV."TIPO_COMP_FAC" = 'FD' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFRV."TIPO_COMP_FAC" = 'FT' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFRV."TIPO_COMP_FAC" = 'FX' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFRV."TIPO_COMP_FAC" = 'ND' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMFRV."TIPO_COMP_FAC" = 'NT' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMFRV."TIPO_COMP_FAC" = 'NB' THEN	TipoOperacion."IdTipoOperacionAnulacion"
					   ELSE 'XXXXX'
				 END 					   											as "IdTipoOperacion"
				
				,coalesce(trim(GES_TBCTMFRV."COD_BOVEDA"),'DCV')					as "IdBoveda"
				,coalesce(CorredoraContraparte."IdCorredora",0) 					as "IdCorredoraContraparte"
				,coalesce(CondicionLiquidacion."ValorDestino",'PH')					as "IdCondicionLiquidacion"
				,coalesce(GES_TBCTMFRV."FEC_MVTO", '0001-01-01')					as "FechaLiquidacion"
				,coalesce(trim(GES_TBCTMFRV."IND_BOLSA"),'BCS')						as "IdBolsa"
				,coalesce(Instrumento."IdTipoReajuste",'CLP')						as "IdTipoReajuste"
				,'SCV' 																as "IdTipoConvenio" 
				,COALESCE(Comprobante."IdEstadoComprobante",'') 					as "IdEstadoFacturacion" 
				,COALESCE(Asignacion."IdOrden",0) 									as "IdOrden" 			--AQUI
				,COALESCE(Asignacion."IdAsignacion",0) 								as "IdAsignacion" 		--AQUI
				,'AGE' 																as "IdTipoCanalIngreso"
				,COALESCE(ORDEN."FechaIngresoRegistroCorta",TO_TIMESTAMP ('0001-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') ) AS "FechaOrden"
			    ,COALESCE(Asignacion."IdTransaccion",0) 							as "IdTransaccion" 		--AQUI
				,COALESCE(GES_TBCTMFRV."FOLIO_TRANS",'0') 							as "FolioTransaccion"
				,CASE 
			      	WHEN COALESCE(GES_TBCTMFRV."FEC_TRANS",'0001-01-01') = '0001-01-01' THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRV."FEC_TRANS",1,10) , 'YYYY-MM-DD')
			      	WHEN LENGTH(TRIM(GES_TBCTMFRV."FEC_TRANS")) = 10 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRV."FEC_TRANS",1,10) , 'YYYY-MM-DD')
					WHEN LENGTH(TRIM(GES_TBCTMFRV."FEC_TRANS")) = 26 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRV."FEC_TRANS",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					WHEN LENGTH(TRIM(GES_TBCTMFRV."FEC_TRANS")) = 19 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRV."FEC_TRANS",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					ELSE TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRV."FEC_TRANS",1,10) , 'YYYY-MM-DD')
				  END   															as "FechaTransaccion"
				,COALESCE(ORDEN."FechaVencimiento",'0001-01-01') 					as "FechaVencimiento"  	--AQUI
				,TO_TIMESTAMP('0001-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS')		as "FechaParidad"
				,0 																	as "ValorParidad"   
				,GES_TBCTMFRV."MONTO" 												as "ValorCaptacionMonedaOrigen"
				,GES_TBCTMFRV."FOLIO_COMP_FAC"										as "FolioComprobante"
				,GES_TBCTMFRV."TASA" 												as "Tasa"
				,GES_TBCTMFRV."CANTIDAD" 											as "Cantidad"
				,GES_TBCTMFRV."PRECIO" 												as "Precio"
				,GES_TBCTMFRV."MONTO" 												as "Monto"
				,0 																	as "MontoDescuento"
				,0 																	as "MontoInteres"
				,'' 																as "Glosa"
--AQUI			,GES_TBCTMFRV."VAL_RESC"											as "ValorRescate"
				,0																	as "ValorRescate"

				,0 																	as "TasaAgente"
				,GES_TBCTMFRV."SPREAD" 												as "Spread"
				,0 																	as "DiasPlazo" --ANALIZAR
				,'1' 																as "CodigoCanalIngreso" --- REVISAR
				,trim(GES_TBCTMFRV."RUT_DEST") 										as "DocumentoIdentidad"
				,'R' 																as "TipoDocumentoIdentidad"
				,Persona."Nombres"||' '||Persona."ApellidoPaterno"||' '||Persona."ApellidoMaterno" 	as "NombreLargoCliente"
				,coalesce(Direccion."Calle",'') 									as "DireccionFacturacion"
				,0  																as "FolioCartera"
				,TipoProducto."ValorDestino"										as "CodigoTipoProducto"
				,TipoOperacion."IdTipoOperacion"									as "CodigoTipoOperacion"
				,GES_TBCTMFRV."IND_BOLSA"  											as "CodigoBolsa"
				,coalesce(TipoInstrumento."Nombre",'') 								as "NombreTipoInstrumento"
				,coalesce(TipoInstrumento."Nombre",'') 								as "NombreTipoInstrumentoSubyacente"
				
				,'RENTA VARIABLE' 													as "NombreClasificacionTipoInstrumento" --aqui
				
				
				,coalesce(Instrumento."Nemotecnico" ,'')							as "NemotecnicoInstrumento"
				,GES_TBCTMFRV."NEMO"												as "NemotecnicoSubyacente"
				,coalesce(Instrumento."IdEmisor",'')								as "RazonSocialEmisor"
			    ,CASE 
			      	WHEN LENGTH(TRIM(Instrumento."FechaEmision")) = 26 THEN TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					WHEN LENGTH(TRIM(Instrumento."FechaEmision")) = 19 THEN TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					ELSE TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,10)                                   , 'YYYY-MM-DD')
				  END AS "FechaEmision"
				,0 																	as "TasaEmision"
				,coalesce(GES_TBCTMFRV."FEC_MVTO",'0001-01-01')					    as "FechaIngresoRegistro"
				,coalesce(GES_TBCTMFRV."FEC_MVTO",'0001-01-01')		                as "FechaSistema"

--AQUI			,COALESCE(GES_TBCTMFRV."IND_COBRO_SEP",'S')							as "EsComprobIndividual" 
				,COALESCE(GES_TBCTMFRV."IND_COMP_INDIV",'S')						as "EsComprobIndividual" 
--AQUI				,CASE WHEN GES_TBCTMFRV."RUT_CORR" = GES_TBCTMFRV."RUT_DEST" THEN 'S' ELSE 'N' END as "EsCuentaPropia" --aqui 
				,CASE WHEN GES_TBCTMFRV."IND_CTA_PRO" ='P' THEN 'S' ELSE 'N' END 	as "EsCuentaPropia"--aqui
--				,CASE WHEN TRIM(GES_TBCTMFRV."IND_OD") <> '' THEN 'S' ELSE 'N' END 	as "EsOrdenDirecta" --aqui
				,'N' 																as "EsPrecioPrima"
				,'S' 																as "EsEnRueda"
				,'N' 																as "EsRuteo"
				,'N' 																as "EsIngresoAutomatico"
				,0 																	as "IdUsuario"
				,0 																	as "IdUsuarioBloqueo"
				,p_IdCarga 															as "IdCarga"
				,CASE WHEN GES_TBCTMFRV."MONTO_APLAZO" > 0 THEN 'S' 
					  ELSE 'N' 
				 END																as "EsAPlazo"
				,GES_TBCTMFRV."MONTO_APLAZO"										as "MontoAPlazo"
				,CASE WHEN GES_TBCTMFRV."MONTO_APLAZO" > 0 THEN GES_TBCTMFRV."PRECIO" 
					  ELSE 0 
				 END																as "PrecioAPlazo"
	           ,CASE 
		            	 WHEN TRIM(GES_TBCTMFRV."MERCADO") = ''  THEN 'CLP'
		            ELSE
						CASE WHEN   TRIM(Instrumento."IdMoneda") = '' THEN  'CLP'
							 ELSE COALESCE(Instrumento."IdMoneda",'CLP') 
						 END	 																
					END																as "IdMonedaSubyacente" 
	           ,CASE 
		            	 WHEN TRIM(GES_TBCTMFRV."MERCADO") = ''  THEN 'CLP'
		            ELSE
						CASE WHEN   TRIM(Instrumento."IdMoneda")  = '' THEN  'CLP'
							 ELSE COALESCE( Instrumento."IdMoneda",'CLP') 
						 END	 																
					END																as "IdMonedaLiquidacion" 

				,TO_DATE('0001-01-01','YYYY-MM-DD')									as "FechaTipoCambio"
				,0 																	as "ValorTipoCambio"
				,CASE WHEN TRIM(GES_TBCTMFRV."IND_SIMUL") = 'VC'  THEN 'S'
				      ELSE 'N' 
				 END 																as "EsVentaCorta"
				,GES_TBCTMFRV."SEC_MOVTO" 											as "SecMovimientoFacturarMigracion" --aqui
				,TO_TIMESTAMP(GES_TBCTMFRV."FEC_MVTO", 'YYYY-MM-DD')			    as "FechaMovimientoComprobante"
				,coalesce(CASE WHEN GES_TBCTMFRV."IND_TIT_CUS" ='C' THEN 'S' 
				   WHEN GES_TBCTMFRV."IND_TIT_CUS" IN ('N','V') THEN 'N' 
				   WHEN GES_TBCTMFRV."IND_TIT_CUS" =''  THEN 'S' 
				 END,'')  															as "ActualizarCustodia"

				 ,CASE 
		            	 WHEN TRIM(GES_TBCTMFRV."MERCADO") = ''  THEN 'CLP'
		            ELSE
						CASE WHEN   TRIM(Instrumento."IdMoneda") = '' THEN  'CLP'
							 ELSE COALESCE(Instrumento."IdMoneda",'CLP') 
						 END	 																
					END																as "IdMonedaMovimientoComprobante" 
				,GES_TBCTMFRV."MONTO" 												as "MontoMovimientoComprobante"
				,GES_TBCTMFRV."MONTO" 												as "MontoLiquidacion"
				,'N' 																as "ActualizarCustodiaRelacionado"
				,coalesce(Instrumento."IdEmisor",'')  								as "IdEmisor"
--AQUI			,CASE WHEN trim(GES_TBCTMFRV."IND_FUNGIBILIDAD") = '' THEN 'N' 
--				      ELSE GES_TBCTMFRV."IND_FUNGIBILIDAD"
--				 END 																as "EsFungible"
				 ,'S' 																as "EsFungible"

				,CASE trim(GES_TBCTMFRV."IND_TIT_CUS") WHEN 'C' THEN 'CUS' 
				   WHEN 'N' THEN 'NOM' 
				   WHEN 'V' THEN 'NOM'
				   WHEN ''  THEN 'CUS'
				  END 																as "IdFormaCustodia"
				,'S' 																as "ActualizarCuentaInversion"
				,'S' 																as "ActualizarTesoreria"
				,'' 																as "IdClasificacionTipoInstrumentoRelacionado"
				,0  																as "IdInstrumentoRelacionado"
				,0  																as "IdAsignacionRef"
				,'' 																as "NombreTipoInstrumentoRelacionado"
				,'' 																as "NombreClasificacionTipoInstrumentoRelacionado"
				,'' 																as "IdTipoInstrumentoRelacionado"
				,'' 																as "NemotecnicoRelacionado" 
				
				,GES_TBCTMFRV."RUT_DEST"											as "RUT_DEST"
				,GES_TBCTMFRV."SEC_RUT_DEST" 										as "SEC_RUT_DEST"
				,GES_TBCTMFRV."TIPO_COMP_FAC" 										as "TIPO_COMP_FAC"
				,GES_TBCTMFRV."FOLIO_COMP_FAC" 										as "FOLIO_COMP_FAC"
				,GES_TBCTMFRV."SEC_MOVTO_FAC"	 									as "SEC_MOVTO_FAC"
				,0 																	as "FolioComprobanteReferencia"
				,0 																	as "IdComprobanteReferencia"
				,coalesce(EmpleadoDigitador."NombreSucursal",'') 					as "NombreSucursalEmpleadoDigitador"
				,'' 																as "CodigoIFRS"
				--,GES_TBCTMFRV."SEC_MOVTO"	 										as "SEC_MOVTO"
				,CASE WHEN GES_TBCTMFRV."SEC_MOVTO" = 0 THEN GES_TBCTMVTO."SEC_MOVTO" ELSE GES_TBCTMFRV."SEC_MOVTO" END as "SEC_MOVTO"
				,coalesce(EmpleadoDigitador."NombreCompletoEmpleado",'') 			as "NombreEmpleadoDigitador"
				,''																	as "NombreSucursalEmpleadoEjecutivoPortafolio"
				,0 																	as "ModPrefFact"
				,coalesce(EmpleadoDigitador."IdEmpleado",0) 						as "IdEmpleadoDigitador"
				,TO_DATE('0001-01-01','YYYY-MM-DD')									as "FechaComprobanteReferencia"
				,Portafolio."IdEmpleadoEjecutivo" 									as "IdEmpleadoEjecutivoPortafolio"
				,'' 																as "NombreEmpleadoEjecutivoPortafolio"
				,0 																	as "IdMovimientoRelacionado"
				,GES_TBCTMFRV."LIN_COMP_FAC" 										as "LIN_COMP_ADJ"
				,0 																	as "IdSucursalEmpleadoEjecutivoPortafolio"
				,0 																	as "IdMovimientoOdRelacionado"
				,'' 																as "IdTipoComprobanteReferencia"
				,coalesce(EmpleadoDigitador."IdSucursal",0) 						as "IdSucursalEmpleadoDigitador"
				,0 																	as "IdMesa"
				,''																	as "NemoSVS"
				,0  																as "IdInstrumentoAnterior"
				,'' 																as "IdMonedaSpot"
				,'' 																as "IndicadorOrdenDirecta"
				,Comprobante."FEC_ING_COMP"											as "FEC_MOVTO"
				,COALESCE(Instrumento."IdMonedaEmision",'CLP')						as "IdMonedaEmision"
	  																									
				
        FROM 	"BSCL_VIRTUAL"."GES_TBCTMFRV" 	as GES_TBCTMFRV
						        
                INNER JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Documento
   						ON 	UPPER(Documento."DocumentoIdentidad") =   UPPER(TRIM(GES_TBCTMFRV."RUT_DEST")) 
     																	
   				INNER JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as Persona
    				 	ON 	Persona."IdDocumentoIdentidad" = Documento."IdDocumentoIdentidad"	
  				 		
				INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as Cliente
    					ON 	Cliente."IdPersona" = Persona."IdPersona"
    					
    			INNER JOIN 	"BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
						ON 	Portafolio."IdCliente" = Cliente."IdCliente"
						AND Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRV."SEC_RUT_DEST")
						
				INNER JOIN 	"BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CuentaInversion
	  	   				ON  CuentaInversion."IdCliente"    = Cliente."IdCliente"
	  	   				AND CuentaInversion."IdPortafolio" = Portafolio."IdPortafolio"
	  	   				AND CuentaInversion."IdMoneda" 	= CASE 	WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		                             	   				WHEN GES_TBCTMFRV."IND_TIT_CCTE" = 'V' THEN 'EUR' 
	  	   		                             	   				ELSE 'CLP' 
	  	   		                                 		   END
				
	  	   		INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" AS GES_TBCTMVTO
						ON GES_TBCTMVTO.FOLIO_TRANS = GES_TBCTMFRV.FOLIO_TRANS
						AND GES_TBCTMVTO.FOLIO_COMP_ADJ = GES_TBCTMFRV.FOLIO_COMP_FAC 
						--11-04-2020
						AND GES_TBCTMFRV.COD_MOVTO 	= GES_TBCTMVTO.COD_MOVTO
						AND GES_TBCTMFRV.CANTIDAD 	= GES_TBCTMVTO.CANTIDAD
				
				INNER JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TipoComprobante2
			  			ON  TipoComprobante2."TipoValor" = 'TipoComprobante' 
			  			AND TipoComprobante2."ValorOrigen" = GES_TBCTMVTO.TIPO_COMP_ADJ                                 		   
	  	   		                                 		   
		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as Agente
		            	ON 	Agente."CodigoAgenteMigracion" =  GES_TBCTMFRV."COD_AGENTE"
		            	AND LENGTH (TRIM(Agente."CodigoAgenteMigracion")) > 0

		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PersonaAgente
		        		ON 	PersonaAgente."IdPersona" = Agente."IdPersona"
    		        		
		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS Direccion
		        		ON 	Direccion."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
	        		
		        LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::PLA.Sucursal" as Sucursal
		            	ON 	Sucursal."CodigoSucursal" =  GES_TBCTMFRV."COD_SUC"
		            	
		        LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.CorredoraCodigos" as CodigoCorredora
		            	ON  CodigoCorredora."CodigoCorredora" = trim(GES_TBCTMFRV."COD_CORR_CONTRA")
		            	AND CodigoCorredora."IdBolsa" 		  =	'BCS'
		            	
                LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Corredora" as CorredoraContraparte
                    	ON 	CorredoraContraparte."IdCorredora" = CodigoCorredora."IdCorredora"
                    	
		        		
		        LEFT  JOIN 	"BSCL.Homologacion.BD.Vistas::EmpleadosSebraAOptimus" as EmpleadoDigitador
						ON 	EmpleadoDigitador."emp_codigo" = GES_TBCTMFRV."COD_AGENTE"

	  	   		INNER JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CondicionLiquidacion
				    	ON  CondicionLiquidacion."TipoValor" 	= 'CondicionLiquidacion'         
                 		AND CondicionLiquidacion."Entidad"		= 'FACTURA'
                 		AND CondicionLiquidacion."ValorOrigen"  = trim(GES_TBCTMFRV."IND_LIQUID")
	
				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 	as Instrumento
				    	ON 	Instrumento."Nemotecnico" = 
				    	CASE WHEN (GES_TBCTMFRV."IND_SIMUL" = 'TS' OR GES_TBCTMFRV."IND_SIMUL" = 'TP') THEN
				                  CASE WHEN  GES_TBCTMFRV."TIPO_OPERAC" = 'V' THEN 'SIMULTANEA VENTA'
				                       ELSE  'SIMULTANEA COMPRA'
				                  END
				             WHEN GES_TBCTMFRV."IND_SIMUL" 	= 'VC' THEN 'PRESTAMO VENTA CORTA'
				             WHEN TRIM(GES_TBCTMFRV."NEMO") = ''	  THEN 'CHILE'
				             ELSE IFNULL(TRIM(GES_TBCTMFRV."NEMO"),'')    
                        END  
				    
				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento
				    	ON 	TipoInstrumento."IdTipoInstrumento" = 
				    					                  	CASE WHEN  TRIM(GES_TBCTMFRV."NEMO") = '' THEN 'ORV'
				                       							 ELSE  Instrumento."IdTipoInstrumento"
				                  							END

			   	INNER JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TipoComprobante
			  			ON  TipoComprobante."TipoValor" = 'TipoComprobante' 
			  			AND TipoComprobante."ValorOrigen" = GES_TBCTMFRV."TIPO_COMP_FAC"

				LEFT  JOIN 	"BSCL.Plataforma.BD::INS.TipoOperacion"  AS TipoOperacion
						ON	TipoOperacion."EsMovimientoComprobante" = 'S' 
						AND TipoOperacion."IdTipoOperacion" = 

							(    CASE WHEN GES_TBCTMFRV."PRODUCTO" = 'INRF' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMRF'
					       					 ELSE 'VENRF'
					       				END
								WHEN GES_TBCTMFRV."PRODUCTO" = 'INMO' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMMON'
					       					 ELSE 'VENMON'
					       				END
								WHEN GES_TBCTMFRV."PRODUCTO" = 'INEM' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMETN'
					       					 ELSE 'VENETN'
					       				END
								WHEN GES_TBCTMFRV."PRODUCTO" = 'INIF' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMIIF'
					       					 ELSE 'VENIIF'
					       				END
								WHEN GES_TBCTMFRV."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMCFI'
					       					 ELSE 'VENCFI'
					       				END
								WHEN GES_TBCTMFRV."PRODUCTO" = 'MDOL' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMSPT'
					       					 ELSE 'VENSPT'
					       				END
								WHEN GES_TBCTMFRV."PRODUCTO" = 'INTS' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
					       				
								WHEN GES_TBCTMFRV."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMCFX'
					       					 ELSE 'VENCFX'
					       				END
								WHEN GES_TBCTMFRV."PRODUCTO" = 'INVC' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'PRESTA'
					       					 ELSE 'EMPRES'
					       				END
					       		WHEN GES_TBCTMFRV."PRODUCTO" = 'INAC' THEN
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMACN'
					       					 ELSE 'VENACN'
					       				END
								WHEN TRIM(GES_TBCTMFRV."PRODUCTO") = '' THEN 
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN 'COMOTC'
					       					 ELSE 'VENOTC'
					       				END
								ELSE
					       				CASE WHEN GES_TBCTMFRV."TIPO_OPERAC" = 'C' THEN CONCAT('COM',Instrumento."IdTipoInstrumento")
					       					 ELSE CONCAT('VEN',Instrumento."IdTipoInstrumento")
					       				END
					       		END)

				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
						ON  TIPOPRODUCTO."TipoValor" 			= 'PlantillaProducto' 
						AND TRIM(TIPOPRODUCTO."ValorOrigen") 	= trim(GES_TBCTMFRV."PRODUCTO")
			  			
	   					
		        LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" AS Comprobante
	        			ON 	Comprobante."TIPO_COMP_FAC" 	= GES_TBCTMFRV."TIPO_COMP_FAC"
	        			AND Comprobante."FOLIO_COMP_FAC" 	= GES_TBCTMFRV."FOLIO_COMP_FAC"
			  			
			  	LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   				ON 	ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFRV."SEC_MOVTO_FAC"
	  	   				 
	  	   		LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Orden" as ORDEN
	  	   				ON 	ORDEN."IdOrden" = ASIGNACION."IdOrden"
  	   				
 	   	WHERE  	GES_TBCTMFRV."FEC_MVTO" BETWEEN SUBSTRING(p_FechaInicial,1,10)  AND SUBSTRING(p_FechaFinal,1,10)
 	   	  AND 	TRIM(GES_TBCTMFRV."EST_MOVTO_FAC") IN ('E','F');
-- 	   	  AND   GES_TBCTMFRV."MERCADO" = 'AC';


--AQUI 	   	  AND   (GES_TBCTMFRV."UNIDAD_DES"   = 'CU' or (GES_TBCTMFRV."UNIDAD_DES" = 'TE' AND GES_TBCTMFRV."MERCADO" = 'MO'));
-----------------------------------------------------------------------------------------------------------	
---ENHANCE 20-02-2019  TBCTMFRF
-----------------------------------------------------------------------------------------------------------

        insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobante"
		("IdMovimientoComprobante"
,"IdEmpresa"
,"IdConceptoMovimiento"
,"SecuenciaAgrupacion"
,"IdCliente"
,"IdPortafolio"
,"IdInstrumento"
,"IdInstrumentoSubyacente"
,"IdTipoInstrumento"
,"IdTipoInstrumentoSubyacente"
,"IdClasificacionTipoInstrumento"
,"IdTipoProducto"
,"IdCuentaInversion"
,"IdMoneda"
,"IdTipoComprobante"
,"IdComprobante"
,"IdTipoOperacion"
,"IdBoveda"
,"IdCorredoraContraparte"
,"IdCondicionLiquidacion"
,"FechaLiquidacion"
,"IdBolsa"
,"IdTipoReajuste"
,"IdTipoConvenio"
,"IdEstadoFacturacion"
,"IdOrden"
,"IdAsignacion"
,"IdTipoCanalIngreso"
,"FechaOrden"
,"IdTransaccion"
,"FolioTransaccion"
,"FechaTransaccion"
,"FechaVencimiento"
,"FechaParidad"
,"ValorParidad"   
,"ValorCaptacionMonedaOrigen"
,"FolioComprobante"
,"Tasa"
,"Cantidad"
,"Precio"
,"Monto"
,"MontoDescuento"
,"MontoInteres"
,"Glosa"
,"ValorRescate"
,"TasaAgente"
,"Spread"
,"DiasPlazo"
,"CodigoCanalIngreso"
,"DocumentoIdentidad"
,"TipoDocumentoIdentidad"
,"NombreLargoCliente"
,"DireccionFacturacion"
,"FolioCartera"
,"CodigoTipoProducto"
,"CodigoTipoOperacion"
,"CodigoBolsa"
,"NombreTipoInstrumento"
,"NombreTipoInstrumentoSubyacente"
,"NombreClasificacionTipoInstrumento"
,"NemotecnicoInstrumento"
,"NemotecnicoSubyacente"
,"RazonSocialEmisor"
,"FechaEmision"
,"TasaEmision"
,"FechaIngresoRegistro"
,"FechaSistema"
,"EsComprobIndividual"
,"EsCuentaPropia"
,"EsPrecioPrima"
,"EsEnRueda"
,"EsRuteo"
,"EsIngresoAutomatico"
,"IdUsuario"
,"IdUsuarioBloqueo"
,"IdCarga"
,"EsAPlazo"
,"MontoAPlazo"
,"PrecioAPlazo"
,"IdMonedaSubyacente"
,"IdMonedaLiquidacion"
,"FechaTipoCambio"
,"ValorTipoCambio"
,"EsVentaCorta"
,"SecMovimientoFacturarMigracion"
,"FechaMovimientoComprobante"
,"ActualizarCustodia"
,"IdMonedaMovimientoComprobante"
,"MontoMovimientoComprobante"
,"MontoLiquidacion"
,"ActualizarCustodiaRelacionado"
,"IdEmisor"
,"EsFungible"
,"IdFormaCustodia"
,"ActualizarCuentaInversion"
,"ActualizarTesoreria"
,"IdClasificacionTipoInstrumentoRelacionado"
,"IdInstrumentoRelacionado"
,"IdAsignacionRef"
,"NombreTipoInstrumentoRelacionado"
,"NombreClasificacionTipoInstrumentoRelacionado"
,"IdTipoInstrumentoRelacionado"
,"NemotecnicoRelacionado"
,"RUT_CLI"
,"SEC_RUT_CLI"
,"TIPO_COMP_FAC"
,"FOLIO_COMP_FAC"
,"SEC_MOVTO_FAC"
,"FolioComprobanteReferencia"
,"IdComprobanteReferencia"
,"NombreSucursalEmpleadoDigitador"
,"CodigoIFRS"
,"SEC_MOVTO"
,"NombreEmpleadoDigitador"
,"NombreSucursalEmpleadoEjecutivoPortafolio"
,"ModPrefFact"
,"IdEmpleadoDigitador"
,"FechaComprobanteReferencia"
,"IdEmpleadoEjecutivoPortafolio"
,"NombreEmpleadoEjecutivoPortafolio"
,"IdMovimientoRelacionado"
,"LIN_COMP_ADJ"
,"IdSucursalEmpleadoEjecutivoPortafolio"
,"IdMovimientoOdRelacionado"
,"IdTipoComprobanteReferencia"
,"IdSucursalEmpleadoDigitador"
,"IdMesa"
,"NemoSVS"
,"IdInstrumentoAnterior"
,"IdMonedaSpot"
,"IndicadorOrdenDirecta"
,"FEC_MOVTO"
,"IdMonedaEmision") 
		select 
				"BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL  	as "IdMovimientoComprobante"
				,p_IdEmpresa 														as "IdEmpresa"
--				,GES_TBCTMFRF."CONCEPTO_MVTO"										as "IdConceptoMovimiento"
				,Comprobante."IdConcepto"											as "IdConceptoMovimiento"
--	  			,CASE WHEN TRIM(GES_TBCTMVTO."HOM_TIPO_CON")<>'' THEN  TRIM(GES_TBCTMVTO."HOM_TIPO_CON")|| TRIM(GES_TBCTMVTO."HOM_CON_CONCEPTO")  ELSE   'FACSIN' END	as "IdConceptoMovimiento"
				
				,0  																as "SecuenciaAgrupacion"
				,Cliente."IdCliente" 												as "IdCliente"
				,Portafolio."IdPortafolio" 											as "IdPortafolio"
				,coalesce(Instrumento."IdInstrumento",0) 							as "IdInstrumento"
				,coalesce(Instrumento."IdInstrumento",0)							as "IdInstrumentoSubyacente"
				,coalesce(Instrumento."IdTipoInstrumento",'') 						as "IdTipoInstrumento"
				,coalesce(Instrumento."IdTipoInstrumento",'')						as "IdTipoInstrumentoSubyacente"
				,coalesce(TipoInstrumento."IdClasificacionTipoInstrumento",'') 		as "IdClasificacionTipoInstrumento" 
				,Comprobante."IdTipoProducto" 										as "IdTipoProducto"
 				,CuentaInversion."IdCuentaInversion"  								as "IdCuentaInversion"
				,CASE WHEN  CuentaInversion."IdMoneda"   = '' THEN  'CLP'
					 ELSE COALESCE(CuentaInversion."IdMoneda" ,'CLP') 
				 END	 															as "IdMoneda" 
				,TipoComprobante."ValorDestino" 									as "IdTipoComprobante"
				,coalesce(Comprobante."IdComprobante",0) 							as "IdComprobante"
--				,coalesce(TipoOperacion."IdTipoOperacion",'')						as "IdTipoOperacion"			
				,CASE 
					   WHEN GES_TBCTMFRF."TIPO_COMP_FAC" = 'FD' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFRF."TIPO_COMP_FAC" = 'FT' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFRF."TIPO_COMP_FAC" = 'FX' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFRF."TIPO_COMP_FAC" = 'ND' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMFRF."TIPO_COMP_FAC" = 'NT' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMFRF."TIPO_COMP_FAC" = 'NB' THEN	TipoOperacion."IdTipoOperacionAnulacion"
					   ELSE 'XXXXX'
				 END 					   											as "IdTipoOperacion"

				
				,coalesce(trim(GES_TBCTMFRF."COD_BOVEDA"),'DCV')					as "IdBoveda"
				,coalesce(CorredoraContraparte."IdCorredora",0) 					as "IdCorredoraContraparte"
				,coalesce(CondicionLiquidacion."ValorDestino",'PH')					as "IdCondicionLiquidacion"
				,coalesce(GES_TBCTMFRF."FEC_MVTO", '0001-01-01')					as "FechaLiquidacion"
				,coalesce(trim(GES_TBCTMFRF."IND_BOLSA"),'BCS')						as "IdBolsa"
				,coalesce(Instrumento."IdTipoReajuste",'CLP')						as "IdTipoReajuste"
				,'SCV' 																as "IdTipoConvenio" 
				,COALESCE(Comprobante."IdEstadoComprobante",'') 					as "IdEstadoFacturacion" 
				,COALESCE(Asignacion."IdOrden",0) 									as "IdOrden" 			--AQUI
				,COALESCE(Asignacion."IdAsignacion",0) 								as "IdAsignacion" 		--AQUI
				,'AGE' 																as "IdTipoCanalIngreso"
				,COALESCE(ORDEN."FechaIngresoRegistroCorta",TO_TIMESTAMP ('0001-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') ) AS "FechaOrden"
			    ,COALESCE(Asignacion."IdTransaccion",0) 							as "IdTransaccion" 		--AQUI
				,COALESCE(GES_TBCTMFRF."FOLIO_TRANS",'0') 							as "FolioTransaccion"
				,CASE 
			      	WHEN COALESCE(GES_TBCTMFRF."FEC_TRANS",'0001-01-01') = '0001-01-01' THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRF."FEC_TRANS",1,10) , 'YYYY-MM-DD')
			      	WHEN LENGTH(TRIM(GES_TBCTMFRF."FEC_TRANS")) = 10 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRF."FEC_TRANS",1,10) , 'YYYY-MM-DD')
					WHEN LENGTH(TRIM(GES_TBCTMFRF."FEC_TRANS")) = 26 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRF."FEC_TRANS",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					WHEN LENGTH(TRIM(GES_TBCTMFRF."FEC_TRANS")) = 19 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRF."FEC_TRANS",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					ELSE TO_TIMESTAMP (SUBSTRING(GES_TBCTMFRF."FEC_TRANS",1,10) , 'YYYY-MM-DD')
				  END   															as "FechaTransaccion"
				,COALESCE(ORDEN."FechaVencimiento",'0001-01-01') 					as "FechaVencimiento"  	--AQUI
				,TO_TIMESTAMP('0001-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS')		as "FechaParidad"
				,0 																	as "ValorParidad"   
				,GES_TBCTMFRF."MONTO" 												as "ValorCaptacionMonedaOrigen"
				,GES_TBCTMFRF."FOLIO_COMP_FAC"										as "FolioComprobante"
				,GES_TBCTMFRF."TIR" 												as "Tasa"
				,GES_TBCTMFRF."CANTIDAD" 											as "Cantidad"
				,GES_TBCTMFRF."PRECIO" 												as "Precio"
				,GES_TBCTMFRF."MONTO" 												as "Monto"
				,0 																	as "MontoDescuento"
				,0 																	as "MontoInteres"
				,'' 																as "Glosa"
--      		,GES_TBCTMFRF."VAL_RESC"											as "ValorRescate"
				,0																	as "ValorRescate"

				,0 																	as "TasaAgente"
--				,GES_TBCTMFRF."SPREAD" 												as "Spread"
				,0 																	as "Spread"
				,0 																	as "DiasPlazo" --ANALIZAR
				,'1' 																as "CodigoCanalIngreso" --- REVISAR
				,trim(GES_TBCTMFRF."RUT_DEST") 										as "DocumentoIdentidad"
				,'R' 																as "TipoDocumentoIdentidad"
				,Persona."Nombres"||' '||Persona."ApellidoPaterno"||' '||Persona."ApellidoMaterno" 	as "NombreLargoCliente"
				,coalesce(Direccion."Calle",'') 									as "DireccionFacturacion"
				,0  																as "FolioCartera"
				,TipoProducto."ValorDestino"										as "CodigoTipoProducto"
				,TipoOperacion."IdTipoOperacion"									as "CodigoTipoOperacion"
				,GES_TBCTMFRF."IND_BOLSA"  											as "CodigoBolsa"
				,coalesce(TipoInstrumento."Nombre",'') 								as "NombreTipoInstrumento"
				,coalesce(TipoInstrumento."Nombre",'') 								as "NombreTipoInstrumentoSubyacente"
				
				,'RENTA VARIABLE' 													as "NombreClasificacionTipoInstrumento" --aqui
				
				
				,coalesce(Instrumento."Nemotecnico" ,'')							as "NemotecnicoInstrumento"
				,GES_TBCTMFRF."NEMO"												as "NemotecnicoSubyacente"
				,coalesce(Instrumento."IdEmisor",'')								as "RazonSocialEmisor"
			    ,CASE 
			      	WHEN LENGTH(TRIM(Instrumento."FechaEmision")) = 26 THEN TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					WHEN LENGTH(TRIM(Instrumento."FechaEmision")) = 19 THEN TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					ELSE TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,10)                                   , 'YYYY-MM-DD')
				  END AS "FechaEmision"
				,0 																	as "TasaEmision"
				,coalesce(GES_TBCTMFRF."FEC_MVTO",'0001-01-01')					    as "FechaIngresoRegistro"
				,coalesce(GES_TBCTMFRF."FEC_MVTO",'0001-01-01')		                as "FechaSistema"

--AQUI			,COALESCE(GES_TBCTMFRF."IND_COBRO_SEP",'S')							as "EsComprobIndividual" 
				,COALESCE(GES_TBCTMFRF."IND_COMP_INDIV",'S')						as "EsComprobIndividual" 
--AQUI				,CASE WHEN GES_TBCTMFRF."RUT_CORR" = GES_TBCTMFRF."RUT_DEST" THEN 'S' ELSE 'N' END as "EsCuentaPropia" --aqui 
				,CASE WHEN GES_TBCTMFRF."IND_CTA_PRO" ='P' THEN 'S' ELSE 'N' END 	as "EsCuentaPropia"--aqui
--				,CASE WHEN TRIM(GES_TBCTMFRF."IND_OD") <> '' THEN 'S' ELSE 'N' END 	as "EsOrdenDirecta" --aqui
				,'N' 																as "EsPrecioPrima"
				,'S' 																as "EsEnRueda"
				,'N' 																as "EsRuteo"
				,'N' 																as "EsIngresoAutomatico"
				,0 																	as "IdUsuario"
				,0 																	as "IdUsuarioBloqueo"
				,p_IdCarga 															as "IdCarga"
				,'N'																as "EsAPlazo"
				,0																	as "MontoAPlazo"
				, 0 																as "PrecioAPlazo"
	           ,CASE WHEN   TRIM(Instrumento."IdMoneda") = '' THEN  'CLP'
					 ELSE COALESCE(Instrumento."IdMoneda",'CLP') 
					END																as "IdMonedaSubyacente" 
	           ,CASE WHEN   TRIM(Instrumento."IdMoneda")  = '' THEN  'CLP'
					 ELSE COALESCE( Instrumento."IdMoneda",'CLP') 
				END																	as "IdMonedaLiquidacion" 

				,TO_DATE('0001-01-01','YYYY-MM-DD')									as "FechaTipoCambio"
				,0 																	as "ValorTipoCambio"
				,CASE WHEN TRIM(GES_TBCTMFRF."IND_SIMUL") = 'VC'  THEN 'S'
				      ELSE 'N' 
				 END 																as "EsVentaCorta"
				,GES_TBCTMFRF."SEC_MOVTO" 											as "SecMovimientoFacturarMigracion" --aqui
				,TO_TIMESTAMP(GES_TBCTMFRF."FEC_MVTO", 'YYYY-MM-DD')			    as "FechaMovimientoComprobante"
				,coalesce(CASE WHEN GES_TBCTMFRF."IND_TIT_CUS" ='C' THEN 'S' 
				   WHEN GES_TBCTMFRF."IND_TIT_CUS" IN ('N','V') THEN 'N' 
				   WHEN GES_TBCTMFRF."IND_TIT_CUS" =''  THEN 'S' 
				 END,'')  															as "ActualizarCustodia"

				 ,CASE WHEN   TRIM(Instrumento."IdMoneda") = '' THEN  'CLP'
						ELSE COALESCE(Instrumento."IdMoneda",'CLP') 
					END																as "IdMonedaMovimientoComprobante" 
				,GES_TBCTMFRF."MONTO" 												as "MontoMovimientoComprobante"
				,GES_TBCTMFRF."MONTO" 												as "MontoLiquidacion"
				,'N' 																as "ActualizarCustodiaRelacionado"
				,coalesce(Instrumento."IdEmisor",'')  								as "IdEmisor"
--AQUI			,CASE WHEN trim(GES_TBCTMFRF."IND_FUNGIBILIDAD") = '' THEN 'N' 
--				      ELSE GES_TBCTMFRF."IND_FUNGIBILIDAD"
--				 END 																as "EsFungible"
				 ,'S' 																as "EsFungible"

				,CASE trim(GES_TBCTMFRF."IND_TIT_CUS") WHEN 'C' THEN 'CUS' 
				   WHEN 'N' THEN 'NOM' 
				   WHEN 'V' THEN 'NOM'
				   WHEN ''  THEN 'CUS'
				  END 																as "IdFormaCustodia"
				,'S' 																as "ActualizarCuentaInversion"
				,'S' 																as "ActualizarTesoreria"
				,'' 																as "IdClasificacionTipoInstrumentoRelacionado"
				,0  																as "IdInstrumentoRelacionado"
				,0  																as "IdAsignacionRef"
				,'' 																as "NombreTipoInstrumentoRelacionado"
				,'' 																as "NombreClasificacionTipoInstrumentoRelacionado"
				,'' 																as "IdTipoInstrumentoRelacionado"
				,'' 																as "NemotecnicoRelacionado" 
				
				,GES_TBCTMFRF."RUT_DEST"											as "RUT_DEST"
				,GES_TBCTMFRF."SEC_RUT_DEST" 										as "SEC_RUT_DEST"
				,GES_TBCTMFRF."TIPO_COMP_FAC" 										as "TIPO_COMP_FAC"
				,GES_TBCTMFRF."FOLIO_COMP_FAC" 										as "FOLIO_COMP_FAC"
				,GES_TBCTMFRF."SEC_MOVTO_FAC"	 									as "SEC_MOVTO_FAC"
				,0 																	as "FolioComprobanteReferencia"
				,0 																	as "IdComprobanteReferencia"
				,coalesce(EmpleadoDigitador."NombreSucursal",'') 					as "NombreSucursalEmpleadoDigitador"
				,'' 																as "CodigoIFRS"
				,GES_TBCTMFRF."SEC_MOVTO"	 										as "SEC_MOVTO"
				,coalesce(EmpleadoDigitador."NombreCompletoEmpleado",'') 			as "NombreEmpleadoDigitador"
				,''																	as "NombreSucursalEmpleadoEjecutivoPortafolio"
				,0 																	as "ModPrefFact"
				,coalesce(EmpleadoDigitador."IdEmpleado",0) 						as "IdEmpleadoDigitador"
				,TO_DATE('0001-01-01','YYYY-MM-DD')									as "FechaComprobanteReferencia"
				,Portafolio."IdEmpleadoEjecutivo" 									as "IdEmpleadoEjecutivoPortafolio"
				,'' 																as "NombreEmpleadoEjecutivoPortafolio"
				,0 																	as "IdMovimientoRelacionado"
				,GES_TBCTMFRF."LIN_COMP_FAC" 										as "LIN_COMP_ADJ"
				,0 																	as "IdSucursalEmpleadoEjecutivoPortafolio"
				,0 																	as "IdMovimientoOdRelacionado"
				,'' 																as "IdTipoComprobanteReferencia"
				,coalesce(EmpleadoDigitador."IdSucursal",0) 						as "IdSucursalEmpleadoDigitador"
				,0 																	as "IdMesa"
				,''																	as "NemoSVS"
				,0  																as "IdInstrumentoAnterior"
				,'' 																as "IdMonedaSpot"
				,'' 																as "IndicadorOrdenDirecta"
				,Comprobante."FEC_ING_COMP"											as "FEC_MOVTO"
				,COALESCE(Instrumento."IdMonedaEmision",'CLP')						as "IdMonedaEmision"
	  																									
		
        FROM 	"BSCL_VIRTUAL"."GES_TBCTMFRF" 	as GES_TBCTMFRF
						        
                INNER JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Documento
   						ON 	UPPER(Documento."DocumentoIdentidad") =   UPPER(TRIM(GES_TBCTMFRF."RUT_DEST")) 
     																	
   				INNER JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as Persona
    				 	ON 	Persona."IdDocumentoIdentidad" = Documento."IdDocumentoIdentidad"	
  				 		
				INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as Cliente
    					ON 	Cliente."IdPersona" = Persona."IdPersona"
    					
    			INNER JOIN 	"BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
						ON 	Portafolio."IdCliente" = Cliente."IdCliente"
						AND Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFRF."SEC_RUT_DEST")
						
				INNER JOIN 	"BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CuentaInversion
	  	   				ON  CuentaInversion."IdCliente"    = Cliente."IdCliente"
	  	   				AND CuentaInversion."IdPortafolio" = Portafolio."IdPortafolio"
	  	   				AND CuentaInversion."IdMoneda" 		= 'CLP'  
	  	   		/*		CASE 	WHEN GES_TBCTMFRF."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		        		WHEN GES_TBCTMFRF."IND_TIT_CCTE" = 'V' THEN 'EUR' 
	  	   		            	ELSE 'CLP' 
	  	   		        END*/

		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as Agente
		            	ON 	Agente."CodigoAgenteMigracion" =  GES_TBCTMFRF."COD_AGENTE"
		            	AND LENGTH (TRIM(Agente."CodigoAgenteMigracion")) > 0

		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PersonaAgente
		        		ON 	PersonaAgente."IdPersona" = Agente."IdPersona"
    		        		
		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS Direccion
		        		ON 	Direccion."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
	        		
		        LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::PLA.Sucursal" as Sucursal
		            	ON 	Sucursal."CodigoSucursal" =  GES_TBCTMFRF."COD_SUC"
		            	
		        LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.CorredoraCodigos" as CodigoCorredora
		            	ON  CodigoCorredora."CodigoCorredora" = trim(GES_TBCTMFRF."COD_CORR_CONTRA")
		            	AND CodigoCorredora."IdBolsa" 		  =	'BCS'
		            	
                LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Corredora" as CorredoraContraparte
                    	ON 	CorredoraContraparte."IdCorredora" = CodigoCorredora."IdCorredora"
                    	
		        		
		        LEFT  JOIN 	"BSCL.Homologacion.BD.Vistas::EmpleadosSebraAOptimus" as EmpleadoDigitador
						ON 	EmpleadoDigitador."emp_codigo" = GES_TBCTMFRF."COD_AGENTE"

	  	   		INNER JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CondicionLiquidacion
				    	ON  CondicionLiquidacion."TipoValor" 	= 'CondicionLiquidacion'         
                 		AND CondicionLiquidacion."Entidad"		= 'FACTURA'
                 		AND CondicionLiquidacion."ValorOrigen"  = trim(GES_TBCTMFRF."IND_LIQUID")
	
				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 	as Instrumento
				    	ON 	Instrumento."Nemotecnico" = 
				    	CASE WHEN (GES_TBCTMFRF."IND_SIMUL" = 'TS' OR GES_TBCTMFRF."IND_SIMUL" = 'TP') THEN
				                  CASE WHEN  GES_TBCTMFRF."TIPO_OPERAC" = 'V' THEN 'SIMULTANEA VENTA'
				                       ELSE  'SIMULTANEA COMPRA'
				                  END
				             WHEN GES_TBCTMFRF."IND_SIMUL" 	= 'VC' THEN 'PRESTAMO VENTA CORTA'
				             WHEN TRIM(GES_TBCTMFRF."NEMO") = ''	  THEN 'CHILE'
				             ELSE IFNULL(TRIM(GES_TBCTMFRF."NEMO"),'')    
                        END  
				    
				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento
				    	ON 	TipoInstrumento."IdTipoInstrumento" = 
				    					                  	CASE WHEN  TRIM(GES_TBCTMFRF."NEMO") = '' THEN 'ORV'
				                       							 ELSE  Instrumento."IdTipoInstrumento"
				                  							END

			   	INNER JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TipoComprobante
			  			ON  TipoComprobante."TipoValor" = 'TipoComprobante' 
			  			AND TipoComprobante."ValorOrigen" = GES_TBCTMFRF."TIPO_COMP_FAC"

				LEFT  JOIN 	"BSCL.Plataforma.BD::INS.TipoOperacion"  AS TipoOperacion
						ON	TipoOperacion."EsMovimientoComprobante" = 'S' 
						AND TipoOperacion."IdTipoOperacion" = 

							(    CASE WHEN GES_TBCTMFRF."PRODUCTO" = 'INRF' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMRF'
					       					 ELSE 'VENRF'
					       				END
								WHEN GES_TBCTMFRF."PRODUCTO" = 'INMO' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMMON'
					       					 ELSE 'VENMON'
					       				END
								WHEN GES_TBCTMFRF."PRODUCTO" = 'INEM' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMETN'
					       					 ELSE 'VENETN'
					       				END
								WHEN GES_TBCTMFRF."PRODUCTO" = 'INIF' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMIIF'
					       					 ELSE 'VENIIF'
					       				END
								WHEN GES_TBCTMFRF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMCFI'
					       					 ELSE 'VENCFI'
					       				END
								WHEN GES_TBCTMFRF."PRODUCTO" = 'MDOL' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMSPT'
					       					 ELSE 'VENSPT'
					       				END
								WHEN GES_TBCTMFRF."PRODUCTO" = 'INTS' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
					       				
								WHEN GES_TBCTMFRF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMCFX'
					       					 ELSE 'VENCFX'
					       				END
					       		WHEN GES_TBCTMFRF."PRODUCTO" = 'INVC' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'PRESTA'
					       					 ELSE 'EMPRES'
					       				END
					       		WHEN GES_TBCTMFRF."PRODUCTO" = 'INAC' THEN
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMACN'
					       					 ELSE 'VENACN'
					       				END
								WHEN TRIM(GES_TBCTMFRF."PRODUCTO") = '' THEN 
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN 'COMOTC'
					       					 ELSE 'VENOTC'
					       				END
								ELSE
					       				CASE WHEN GES_TBCTMFRF."TIPO_OPERAC" = 'C' THEN CONCAT('COM',Instrumento."IdTipoInstrumento")
					       					 ELSE CONCAT('VEN',Instrumento."IdTipoInstrumento")
					       				END
					       		END)

				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
						ON  TIPOPRODUCTO."TipoValor" 			= 'PlantillaProducto' 
						AND TRIM(TIPOPRODUCTO."ValorOrigen") 	= trim(GES_TBCTMFRF."PRODUCTO")
			  			
	   					
		        LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" AS Comprobante
	        			ON 	Comprobante."TIPO_COMP_FAC" 	= GES_TBCTMFRF."TIPO_COMP_FAC"
	        			AND Comprobante."FOLIO_COMP_FAC" 	= GES_TBCTMFRF."FOLIO_COMP_FAC"
			  			
			  	LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   				ON 	ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFRF."SEC_MOVTO_FAC"
	  	   				 
	  	   		LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Orden" as ORDEN
	  	   				ON 	ORDEN."IdOrden" = ASIGNACION."IdOrden"
  	   				
 	   	WHERE  	GES_TBCTMFRF."FEC_MVTO" BETWEEN SUBSTRING(p_FechaInicial,1,10)  AND SUBSTRING(p_FechaFinal,1,10)
 	   	  AND 	TRIM(GES_TBCTMFRF."EST_MOVTO_FAC") IN ('E','F');


-----------------------------------------------------------------------------------------------------------	
---ENHANCE 20-02-2019  TBCTMFIF
-----------------------------------------------------------------------------------------------------------

insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobante"
("IdMovimientoComprobante"
,"IdEmpresa"
,"IdConceptoMovimiento"
,"SecuenciaAgrupacion"
,"IdCliente"
,"IdPortafolio"
,"IdInstrumento"
,"IdInstrumentoSubyacente"
,"IdTipoInstrumento"
,"IdTipoInstrumentoSubyacente"
,"IdClasificacionTipoInstrumento"
,"IdTipoProducto"
,"IdCuentaInversion"
,"IdMoneda"
,"IdTipoComprobante"
,"IdComprobante"
,"IdTipoOperacion"
,"IdBoveda"
,"IdCorredoraContraparte"
,"IdCondicionLiquidacion"
,"FechaLiquidacion"
,"IdBolsa"
,"IdTipoReajuste"
,"IdTipoConvenio"
,"IdEstadoFacturacion"
,"IdOrden"
,"IdAsignacion"
,"IdTipoCanalIngreso"
,"FechaOrden"
,"IdTransaccion"
,"FolioTransaccion"
,"FechaTransaccion"
,"FechaVencimiento"
,"FechaParidad"
,"ValorParidad"   
,"ValorCaptacionMonedaOrigen"
,"FolioComprobante"
,"Tasa"
,"Cantidad"
,"Precio"
,"Monto"
,"MontoDescuento"
,"MontoInteres"
,"Glosa"
,"ValorRescate"
,"TasaAgente"
,"Spread"
,"DiasPlazo"
,"CodigoCanalIngreso"
,"DocumentoIdentidad"
,"TipoDocumentoIdentidad"
,"NombreLargoCliente"
,"DireccionFacturacion"
,"FolioCartera"
,"CodigoTipoProducto"
,"CodigoTipoOperacion"
,"CodigoBolsa"
,"NombreTipoInstrumento"
,"NombreTipoInstrumentoSubyacente"
,"NombreClasificacionTipoInstrumento"
,"NemotecnicoInstrumento"
,"NemotecnicoSubyacente"
,"RazonSocialEmisor"
,"FechaEmision"
,"TasaEmision"
,"FechaIngresoRegistro"
,"FechaSistema"
,"EsComprobIndividual"
,"EsCuentaPropia"
,"EsPrecioPrima"
,"EsEnRueda"
,"EsRuteo"
,"EsIngresoAutomatico"
,"IdUsuario"
,"IdUsuarioBloqueo"
,"IdCarga"
,"EsAPlazo"
,"MontoAPlazo"
,"PrecioAPlazo"
,"IdMonedaSubyacente"
,"IdMonedaLiquidacion"
,"FechaTipoCambio"
,"ValorTipoCambio"
,"EsVentaCorta"
,"SecMovimientoFacturarMigracion"
,"FechaMovimientoComprobante"
,"ActualizarCustodia"
,"IdMonedaMovimientoComprobante"
,"MontoMovimientoComprobante"
,"MontoLiquidacion"
,"ActualizarCustodiaRelacionado"
,"IdEmisor"
,"EsFungible"
,"IdFormaCustodia"
,"ActualizarCuentaInversion"
,"ActualizarTesoreria"
,"IdClasificacionTipoInstrumentoRelacionado"
,"IdInstrumentoRelacionado"
,"IdAsignacionRef"
,"NombreTipoInstrumentoRelacionado"
,"NombreClasificacionTipoInstrumentoRelacionado"
,"IdTipoInstrumentoRelacionado"
,"NemotecnicoRelacionado"
,"RUT_CLI"
,"SEC_RUT_CLI"
,"TIPO_COMP_FAC"
,"FOLIO_COMP_FAC"
,"SEC_MOVTO_FAC"
,"FolioComprobanteReferencia"
,"IdComprobanteReferencia"
,"NombreSucursalEmpleadoDigitador"
,"CodigoIFRS"
,"SEC_MOVTO"
,"NombreEmpleadoDigitador"
,"NombreSucursalEmpleadoEjecutivoPortafolio"
,"ModPrefFact"
,"IdEmpleadoDigitador"
,"FechaComprobanteReferencia"
,"IdEmpleadoEjecutivoPortafolio"
,"NombreEmpleadoEjecutivoPortafolio"
,"IdMovimientoRelacionado"
,"LIN_COMP_ADJ"
,"IdSucursalEmpleadoEjecutivoPortafolio"
,"IdMovimientoOdRelacionado"
,"IdTipoComprobanteReferencia"
,"IdSucursalEmpleadoDigitador"
,"IdMesa"
,"NemoSVS"
,"IdInstrumentoAnterior"
,"IdMonedaSpot"
,"IndicadorOrdenDirecta"
,"FEC_MOVTO"
,"IdMonedaEmision") 

		select 
				"BSCL.Facturacion.BD.Secuencias::MovimientoComprobante".NEXTVAL  	as "IdMovimientoComprobante"
				,p_IdEmpresa 														as "IdEmpresa"
--				,GES_TBCTMFIF."CONCEPTO_MVTO"										as "IdConceptoMovimiento"
				,Comprobante."IdConcepto"											as "IdConceptoMovimiento"
--	  			,CASE WHEN TRIM(GES_TBCTMVTO."HOM_TIPO_CON")<>'' THEN  TRIM(GES_TBCTMVTO."HOM_TIPO_CON")|| TRIM(GES_TBCTMVTO."HOM_CON_CONCEPTO")  ELSE   'FACSIN' END	as "IdConceptoMovimiento"
				
				,0  																as "SecuenciaAgrupacion"
				,Cliente."IdCliente" 												as "IdCliente"
				,Portafolio."IdPortafolio" 											as "IdPortafolio"
				,coalesce(Instrumento."IdInstrumento",0) 							as "IdInstrumento"
				,coalesce(Instrumento."IdInstrumento",0)							as "IdInstrumentoSubyacente"
				,coalesce(Instrumento."IdTipoInstrumento",'') 						as "IdTipoInstrumento"
				,coalesce(Instrumento."IdTipoInstrumento",'')						as "IdTipoInstrumentoSubyacente"
				,coalesce(TipoInstrumento."IdClasificacionTipoInstrumento",'') 		as "IdClasificacionTipoInstrumento" 
				,Comprobante."IdTipoProducto" 										as "IdTipoProducto"
 				,CuentaInversion."IdCuentaInversion"  								as "IdCuentaInversion"
				,CASE WHEN  CuentaInversion."IdMoneda"   = '' THEN  'CLP'
					 ELSE COALESCE(CuentaInversion."IdMoneda" ,'CLP') 
				 END	 															as "IdMoneda" 
				,TipoComprobante."ValorDestino" 									as "IdTipoComprobante"
				,coalesce(Comprobante."IdComprobante",0) 							as "IdComprobante"
--				,coalesce(TipoOperacion."IdTipoOperacion",'')						as "IdTipoOperacion"			
				,CASE 
					   WHEN GES_TBCTMFIF."TIPO_COMP_FAC" = 'FD' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFIF."TIPO_COMP_FAC" = 'FT' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFIF."TIPO_COMP_FAC" = 'FX' THEN	TipoOperacion."IdTipoOperacion"										
					   WHEN GES_TBCTMFIF."TIPO_COMP_FAC" = 'ND' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMFIF."TIPO_COMP_FAC" = 'NT' THEN	TipoOperacion."IdTipoOperacionAnulacion"										
					   WHEN GES_TBCTMFIF."TIPO_COMP_FAC" = 'NB' THEN	TipoOperacion."IdTipoOperacionAnulacion"
					   ELSE 'XXXXX'
				 END 					   											as "IdTipoOperacion"

				
				,coalesce(trim(GES_TBCTMFIF."COD_BOVEDA"),'DCV')					as "IdBoveda"
				,coalesce(CorredoraContraparte."IdCorredora",0) 					as "IdCorredoraContraparte"
				,coalesce(CondicionLiquidacion."ValorDestino",'PH')					as "IdCondicionLiquidacion"
				,coalesce(GES_TBCTMFIF."FEC_MVTO", '0001-01-01')					as "FechaLiquidacion"
				,coalesce(trim(GES_TBCTMFIF."IND_BOLSA"),'BCS')						as "IdBolsa"
				,coalesce(Instrumento."IdTipoReajuste",'CLP')						as "IdTipoReajuste"
				,'SCV' 																as "IdTipoConvenio" 
				,COALESCE(Comprobante."IdEstadoComprobante",'') 					as "IdEstadoFacturacion" 
				,COALESCE(Asignacion."IdOrden",0) 									as "IdOrden" 			--AQUI
				,COALESCE(Asignacion."IdAsignacion",0) 								as "IdAsignacion" 		--AQUI
				,'AGE' 																as "IdTipoCanalIngreso"
				,COALESCE(ORDEN."FechaIngresoRegistroCorta",TO_TIMESTAMP ('0001-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') ) AS "FechaOrden"
			    ,COALESCE(Asignacion."IdTransaccion",0) 							as "IdTransaccion" 		--AQUI
				,COALESCE(GES_TBCTMFIF."FOLIO_TRANS",'0') 							as "FolioTransaccion"
				,CASE 
			      	WHEN COALESCE(GES_TBCTMFIF."FEC_TRANS",'0001-01-01') = '0001-01-01' THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFIF."FEC_TRANS",1,10) , 'YYYY-MM-DD')
			      	WHEN LENGTH(TRIM(GES_TBCTMFIF."FEC_TRANS")) = 10 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFIF."FEC_TRANS",1,10) , 'YYYY-MM-DD')
					WHEN LENGTH(TRIM(GES_TBCTMFIF."FEC_TRANS")) = 26 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFIF."FEC_TRANS",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					WHEN LENGTH(TRIM(GES_TBCTMFIF."FEC_TRANS")) = 19 THEN TO_TIMESTAMP (SUBSTRING(GES_TBCTMFIF."FEC_TRANS",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					ELSE TO_TIMESTAMP (SUBSTRING(GES_TBCTMFIF."FEC_TRANS",1,10) , 'YYYY-MM-DD')
				  END   															as "FechaTransaccion"
				,COALESCE(ORDEN."FechaVencimiento",'0001-01-01') 					as "FechaVencimiento"  	--AQUI
				,TO_TIMESTAMP('0001-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS')		as "FechaParidad"
				,0 																	as "ValorParidad"   
				,GES_TBCTMFIF."MONTO" 												as "ValorCaptacionMonedaOrigen"
				,GES_TBCTMFIF."FOLIO_COMP_FAC"										as "FolioComprobante"
				,GES_TBCTMFIF."TASA" 												as "Tasa"
				,GES_TBCTMFIF."CANTIDAD" 											as "Cantidad"
				,0 																	as "Precio"
				,GES_TBCTMFIF."MONTO" 												as "Monto"
				,0 																	as "MontoDescuento"
				,0 																	as "MontoInteres"
				,'' 																as "Glosa"
      			,GES_TBCTMFIF."VAL_RESC"											as "ValorRescate"
--				,0																	as "ValorRescate"
				,0 																	as "TasaAgente"
				,0 																	as "Spread"
				,0 																	as "DiasPlazo" --ANALIZAR
				,'1' 																as "CodigoCanalIngreso" --- REVISAR
				,trim(GES_TBCTMFIF."RUT_DEST") 										as "DocumentoIdentidad"
				,'R' 																as "TipoDocumentoIdentidad"
				,Persona."Nombres"||' '||Persona."ApellidoPaterno"||' '||Persona."ApellidoMaterno" 	as "NombreLargoCliente"
				,coalesce(Direccion."Calle",'') 									as "DireccionFacturacion"
				,0  																as "FolioCartera"
				,TipoProducto."ValorDestino"										as "CodigoTipoProducto"
				,TipoOperacion."IdTipoOperacion"									as "CodigoTipoOperacion"
				,GES_TBCTMFIF."IND_BOLSA"  											as "CodigoBolsa"
				,coalesce(TipoInstrumento."Nombre",'') 								as "NombreTipoInstrumento"
				,coalesce(TipoInstrumento."Nombre",'') 								as "NombreTipoInstrumentoSubyacente"
				
				,'RENTA VARIABLE' 													as "NombreClasificacionTipoInstrumento" --aqui
				
				
				,coalesce(Instrumento."Nemotecnico" ,'')							as "NemotecnicoInstrumento"
				,GES_TBCTMFIF."NEMO"												as "NemotecnicoSubyacente"
				,coalesce(Instrumento."IdEmisor",'')								as "RazonSocialEmisor"
			    ,CASE 
			      	WHEN LENGTH(TRIM(Instrumento."FechaEmision")) = 26 THEN TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					WHEN LENGTH(TRIM(Instrumento."FechaEmision")) = 19 THEN TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,19) , 'YYYY-MM-DD:HH24:MI:SS')
					ELSE TO_TIMESTAMP (SUBSTRING(Instrumento."FechaEmision",1,10)                                   , 'YYYY-MM-DD')
				  END AS "FechaEmision"
				,0 																	as "TasaEmision"
				,coalesce(GES_TBCTMFIF."FEC_MVTO",'0001-01-01')					    as "FechaIngresoRegistro"
				,coalesce(GES_TBCTMFIF."FEC_MVTO",'0001-01-01')		                as "FechaSistema"

--AQUI			,COALESCE(GES_TBCTMFIF."IND_COBRO_SEP",'S')							as "EsComprobIndividual" 
				,COALESCE(GES_TBCTMFIF."IND_COMP_INDIV",'S')						as "EsComprobIndividual" 
--AQUI				,CASE WHEN GES_TBCTMFIF."RUT_CORR" = GES_TBCTMFIF."RUT_DEST" THEN 'S' ELSE 'N' END as "EsCuentaPropia" --aqui 
				,CASE WHEN GES_TBCTMFIF."IND_CTA_PRO" ='P' THEN 'S' ELSE 'N' END 	as "EsCuentaPropia"--aqui
--				,CASE WHEN TRIM(GES_TBCTMFIF."IND_OD") <> '' THEN 'S' ELSE 'N' END 	as "EsOrdenDirecta" --aqui
				,'N' 																as "EsPrecioPrima"
				,'S' 																as "EsEnRueda"
				,'N' 																as "EsRuteo"
				,'N' 																as "EsIngresoAutomatico"
				,0 																	as "IdUsuario"
				,0 																	as "IdUsuarioBloqueo"
				,925 															    as "IdCarga"
				,'N'																as "EsAPlazo"
				,0																	as "MontoAPlazo"
				, 0 																as "PrecioAPlazo"
	           ,CASE WHEN   TRIM(Instrumento."IdMoneda") = '' THEN  'CLP'
					 ELSE COALESCE(Instrumento."IdMoneda",'CLP') 
					END																as "IdMonedaSubyacente" 
	           ,CASE WHEN   TRIM(Instrumento."IdMoneda")  = '' THEN  'CLP'
					 ELSE COALESCE( Instrumento."IdMoneda",'CLP') 
				END																	as "IdMonedaLiquidacion" 

				,TO_DATE('0001-01-01','YYYY-MM-DD')									as "FechaTipoCambio"
				,0 																	as "ValorTipoCambio"
				,CASE WHEN TRIM(GES_TBCTMFIF."IND_SIMUL") = 'VC'  THEN 'S'
				      ELSE 'N' 
				 END 																as "EsVentaCorta"
				,GES_TBCTMFIF."SEC_MOVTO" 											as "SecMovimientoFacturarMigracion" --aqui
				,TO_TIMESTAMP(GES_TBCTMFIF."FEC_MVTO", 'YYYY-MM-DD')			    as "FechaMovimientoComprobante"
				,coalesce(CASE WHEN GES_TBCTMFIF."IND_TIT_CUS" ='C' THEN 'S' 
				   WHEN GES_TBCTMFIF."IND_TIT_CUS" IN ('N','V') THEN 'N' 
				   WHEN GES_TBCTMFIF."IND_TIT_CUS" =''  THEN 'S' 
				 END,'')  															as "ActualizarCustodia"

				 ,CASE WHEN   TRIM(Instrumento."IdMoneda") = '' THEN  'CLP'
						ELSE COALESCE(Instrumento."IdMoneda",'CLP') 
					END																as "IdMonedaMovimientoComprobante" 
				,GES_TBCTMFIF."MONTO" 												as "MontoMovimientoComprobante"
				,GES_TBCTMFIF."MONTO" 												as "MontoLiquidacion"
				,'N' 																as "ActualizarCustodiaRelacionado"
				,coalesce(Instrumento."IdEmisor",'')  								as "IdEmisor"
--AQUI			,CASE WHEN trim(GES_TBCTMFIF."GES_TBCTMFRF) = '' THEN 'N' 
--				      ELSE GES_TBCTMFIF."IND_FUNGIBILIDAD"
--				 END 																as "EsFungible"
				 ,'S' 																as "EsFungible"

				,CASE trim(GES_TBCTMFIF."IND_TIT_CUS") WHEN 'C' THEN 'CUS' 
				   WHEN 'N' THEN 'NOM' 
				   WHEN 'V' THEN 'NOM'
				   WHEN ''  THEN 'CUS'
				  END 																as "IdFormaCustodia"
				,'S' 																as "ActualizarCuentaInversion"
				,'S' 																as "ActualizarTesoreria"
				,'' 																as "IdClasificacionTipoInstrumentoRelacionado"
				,0  																as "IdInstrumentoRelacionado"
				,0  																as "IdAsignacionRef"
				,'' 																as "NombreTipoInstrumentoRelacionado"
				,'' 																as "NombreClasificacionTipoInstrumentoRelacionado"
				,'' 																as "IdTipoInstrumentoRelacionado"
				,'' 																as "NemotecnicoRelacionado" 
				
				,GES_TBCTMFIF."RUT_DEST"											as "RUT_DEST"
				,GES_TBCTMFIF."SEC_RUT_DEST" 										as "SEC_RUT_DEST"
				,GES_TBCTMFIF."TIPO_COMP_FAC" 										as "TIPO_COMP_FAC"
				,GES_TBCTMFIF."FOLIO_COMP_FAC" 										as "FOLIO_COMP_FAC"
				,GES_TBCTMFIF."SEC_MOVTO_FAC"	 									as "SEC_MOVTO_FAC"
				,0 																	as "FolioComprobanteReferencia"
				,0 																	as "IdComprobanteReferencia"
				,coalesce(EmpleadoDigitador."NombreSucursal",'') 					as "NombreSucursalEmpleadoDigitador"
				,'' 																as "CodigoIFRS"
				,GES_TBCTMFIF."SEC_MOVTO"	 										as "SEC_MOVTO"
				,coalesce(EmpleadoDigitador."NombreCompletoEmpleado",'') 			as "NombreEmpleadoDigitador"
				,''																	as "NombreSucursalEmpleadoEjecutivoPortafolio"
				,0 																	as "ModPrefFact"
				,coalesce(EmpleadoDigitador."IdEmpleado",0) 						as "IdEmpleadoDigitador"
				,TO_DATE('0001-01-01','YYYY-MM-DD')									as "FechaComprobanteReferencia"
				,Portafolio."IdEmpleadoEjecutivo" 									as "IdEmpleadoEjecutivoPortafolio"
				,'' 																as "NombreEmpleadoEjecutivoPortafolio"
				,0 																	as "IdMovimientoRelacionado"
				,GES_TBCTMFIF."LIN_COMP_FAC" 										as "LIN_COMP_ADJ"
				,0 																	as "IdSucursalEmpleadoEjecutivoPortafolio"
				,0 																	as "IdMovimientoOdRelacionado"
				,'' 																as "IdTipoComprobanteReferencia"
				,coalesce(EmpleadoDigitador."IdSucursal",0) 						as "IdSucursalEmpleadoDigitador"
				,0 																	as "IdMesa"
				,''																	as "NemoSVS"
				,0  																as "IdInstrumentoAnterior"
				,'' 																as "IdMonedaSpot"
				,'' 																as "IndicadorOrdenDirecta"
				,Comprobante."FEC_ING_COMP"											as "FEC_MOVTO"
				,COALESCE(Instrumento."IdMonedaEmision",'CLP')										as "IdMonedaEmision"
	  																									
	       FROM 	"BSCL_VIRTUAL"."GES_TBCTMFIF" 	as GES_TBCTMFIF
						        
                INNER JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Documento
   						ON 	UPPER(Documento."DocumentoIdentidad") =   UPPER(TRIM(GES_TBCTMFIF."RUT_DEST")) 
     																	
   				INNER JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as Persona
    				 	ON 	Persona."IdDocumentoIdentidad" = Documento."IdDocumentoIdentidad"	
  				 		
				INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as Cliente
    					ON 	Cliente."IdPersona" = Persona."IdPersona"
    					
    			INNER JOIN 	"BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
						ON 	Portafolio."IdCliente" = Cliente."IdCliente"
						AND Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTMFIF."SEC_RUT_DEST")
						
				INNER JOIN 	"BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CuentaInversion
	  	   				ON  CuentaInversion."IdCliente"    = Cliente."IdCliente"
	  	   				AND CuentaInversion."IdPortafolio" = Portafolio."IdPortafolio"
	  	   				AND CuentaInversion."IdMoneda" 		= 'CLP'  
	  	   		/*		CASE 	WHEN GES_TBCTMFIF."IND_TIT_CCTE" = 'U' THEN 'USD'
	  	   		        		WHEN GES_TBCTMFIF."IND_TIT_CCTE" = 'V' THEN 'EUR' 
	  	   		            	ELSE 'CLP' 
	  	   		        END*/

		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as Agente
		            	ON 	Agente."CodigoAgenteMigracion" =  GES_TBCTMFIF."COD_AGENTE"
		            	AND LENGTH (TRIM(Agente."CodigoAgenteMigracion")) > 0

		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PersonaAgente
		        		ON 	PersonaAgente."IdPersona" = Agente."IdPersona"
    		        		
		        LEFT  JOIN 	"BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS Direccion
		        		ON 	Direccion."IdDireccionFisica" = Persona."IdDireccionFisicaPrincipal"
	        		
		        LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::PLA.Sucursal" as Sucursal
		            	ON 	Sucursal."CodigoSucursal" =  GES_TBCTMFIF."COD_SUC"
		            	
		        LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.CorredoraCodigos" as CodigoCorredora
		            	ON  CodigoCorredora."CodigoCorredora" = trim(GES_TBCTMFIF."COD_CORR_CONTRA")
		            	AND CodigoCorredora."IdBolsa" 		  =	'BCS'
		            	
                LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Corredora" as CorredoraContraparte
                    	ON 	CorredoraContraparte."IdCorredora" = CodigoCorredora."IdCorredora"
                    	
		        		
		        LEFT  JOIN 	"BSCL.Homologacion.BD.Vistas::EmpleadosSebraAOptimus" as EmpleadoDigitador
						ON 	EmpleadoDigitador."emp_codigo" = GES_TBCTMFIF."COD_AGENTE"

	  	   		INNER JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS CondicionLiquidacion
				    	ON  CondicionLiquidacion."TipoValor" 	= 'CondicionLiquidacion'         
                 		AND CondicionLiquidacion."Entidad"		= 'FACTURA'
                 		AND CondicionLiquidacion."ValorOrigen"  = trim(GES_TBCTMFIF."IND_LIQUID")
	
				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 	as Instrumento
				    	ON 	Instrumento."Nemotecnico" = 
				    	CASE WHEN (GES_TBCTMFIF."IND_SIMUL" = 'TS' OR GES_TBCTMFIF."IND_SIMUL" = 'TP') THEN
				                  CASE WHEN  GES_TBCTMFIF."TIPO_OPERAC" = 'V' THEN 'SIMULTANEA VENTA'
				                       ELSE  'SIMULTANEA COMPRA'
				                  END
				             WHEN GES_TBCTMFIF."IND_SIMUL" 	= 'VC' THEN 'PRESTAMO VENTA CORTA'
				             WHEN TRIM(GES_TBCTMFIF."NEMO") = ''	  THEN 'CHILE'
				             ELSE IFNULL(TRIM(GES_TBCTMFIF."NEMO"),'')    
                        END  
				    
				LEFT  JOIN 	"BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento
				    	ON 	TipoInstrumento."IdTipoInstrumento" = 
				    					                  	CASE WHEN  TRIM(GES_TBCTMFIF."NEMO") = '' THEN 'ORV'
				                       							 ELSE  Instrumento."IdTipoInstrumento"
				                  							END

			   	INNER JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TipoComprobante
			  			ON  TipoComprobante."TipoValor" = 'TipoComprobante' 
			  			AND TipoComprobante."ValorOrigen" = GES_TBCTMFIF."TIPO_COMP_FAC"

				LEFT  JOIN 	"BSCL.Plataforma.BD::INS.TipoOperacion"  AS TipoOperacion
						ON	TipoOperacion."EsMovimientoComprobante" = 'S' 
						AND TipoOperacion."IdTipoOperacion" = 

							(    CASE WHEN GES_TBCTMFIF."PRODUCTO" = 'INRF' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMRF'
					       					 ELSE 'VENRF'
					       				END
								WHEN GES_TBCTMFIF."PRODUCTO" = 'INMO' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMMON'
					       					 ELSE 'VENMON'
					       				END
								WHEN GES_TBCTMFIF."PRODUCTO" = 'INEM' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMETN'
					       					 ELSE 'VENETN'
					       				END
								WHEN GES_TBCTMFIF."PRODUCTO" = 'INIF' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMIIF'
					       					 ELSE 'VENIIF'
					       				END
								WHEN GES_TBCTMFIF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMCFI'
					       					 ELSE 'VENCFI'
					       				END
								WHEN GES_TBCTMFIF."PRODUCTO" = 'MDOL' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMSPT'
					       					 ELSE 'VENSPT'
					       				END
								WHEN GES_TBCTMFIF."PRODUCTO" = 'INTS' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMSIM'
					       					 ELSE 'VENSIM'
					       				END
					       				
								WHEN GES_TBCTMFIF."PRODUCTO" = 'INCF' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMCFX'
					       					 ELSE 'VENCFX'
					       				END
					       		WHEN GES_TBCTMFIF."PRODUCTO" = 'INVC' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'PRESTA'
					       					 ELSE 'EMPRES'
					       				END
					       		WHEN GES_TBCTMFIF."PRODUCTO" = 'INAC' THEN
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMACN'
					       					 ELSE 'VENACN'
					       				END
								WHEN TRIM(GES_TBCTMFIF."PRODUCTO") = '' THEN 
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN 'COMOTC'
					       					 ELSE 'VENOTC'
					       				END
								ELSE
					       				CASE WHEN GES_TBCTMFIF."TIPO_OPERAC" = 'C' THEN CONCAT('COM',Instrumento."IdTipoInstrumento")
					       					 ELSE CONCAT('VEN',Instrumento."IdTipoInstrumento")
					       				END
					       		END)

				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Homologacion" as TIPOPRODUCTO
						ON  TIPOPRODUCTO."TipoValor" 			= 'PlantillaProducto' 
						AND TRIM(TIPOPRODUCTO."ValorOrigen") 	= trim(GES_TBCTMFIF."PRODUCTO")
			  			
	   					
		        LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" AS Comprobante
	        			ON 	Comprobante."TIPO_COMP_FAC" 	= GES_TBCTMFIF."TIPO_COMP_FAC"
	        			AND Comprobante."FOLIO_COMP_FAC" 	= GES_TBCTMFIF."FOLIO_COMP_FAC"
			  			
			  	LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as ASIGNACION
	  	   				ON 	ASIGNACION."SEC_MOVTO_FAC" = GES_TBCTMFIF."SEC_MOVTO_FAC"
	  	   				 
	  	   		LEFT  JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Orden" as ORDEN
	  	   				ON 	ORDEN."IdOrden" = ASIGNACION."IdOrden"
  	   				
 	   	WHERE  	GES_TBCTMFIF."FEC_MVTO" BETWEEN SUBSTRING(p_FechaInicial,1,10)  AND SUBSTRING(p_FechaFinal,1,10)
 	   	  AND 	TRIM(GES_TBCTMFIF."EST_MOVTO_FAC") IN ('E','F');

-----------------------------------------------------------------------------------------------------------		  
		  
				    		    		    
		update MovCom
		set MovCom."IdMovimientoRelacionado" = CarteraSim."IdMovimiento"
		from "BSCL.Homologacion.BD::HOM.MovimientoComprobante" MovCom
		join (
			select distinct A."FOLIO_COMP_ADJ", B."FOLIO_COMP_ADJ", MovCom2."IdMovimientoComprobante" as "IdMovimiento", MovCom1."IdMovimientoComprobante" 
			    from "BSCL.Homologacion.BD::HOM.MovimientoComprobante" MovCom1
			    inner join "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" Cart on (Cart."FolioComprobante" = MovCom1."FolioComprobante")
				inner join "BSCL_VIRTUAL"."GES_TBCTMVTO" A on (a."FOLIO_COMP_ADJ" = Cart."FolioComprobante")
				INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" B on (A."SEC_ORDEN" = B."SEC_ORDEN" AND A."IND_LIQUID" != B."IND_LIQUID" )
				inner join "BSCL.Homologacion.BD::HOM.MovimientoComprobante" MovCom2 on (MovCom2."FolioComprobante" = b."FOLIO_COMP_ADJ")
				and A."UNIDAD_DES" = 'CU' 
				AND B."UNIDAD_DES" = 'CU'
				and a."IND_SIMUL" = 'TS'
				and b."IND_SIMUL" = 'TS'
				AND A."IND_ANULADO"	= 'I'
				AND B."IND_ANULADO"	= 'I'
				AND A."SEC_ORDEN" != 0
				AND B."SEC_ORDEN" != 0
				and a."TIPO_COMP_ADJ" = 'FD'
				AND b."TIPO_COMP_ADJ" = 'FD'
				and b."FOLIO_COMP_CONT" = 0) CarteraSim on MovCom."IdMovimientoComprobante" = CarteraSim."IdMovimiento";
        -------------------------------------------------------
		----   Movimiento de Comprobantes (TBCTMVTO)       ----
		-------------------------------------------------------
		
	    insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle"(    
					"IdMovimientoComprobanteDetalle","IdComprobanteDetalle","IdMovimientoComprobante","IdTransaccion","IdInstrumento","NemotecnicoInstrumento"
					,"IdConceptoMovimiento","Cantidad","Precio","PrecioPromedio","Monto","Tasa","TasaEmision","ValorRescate","ValorParidad","ValorCaptacionMonedaOrigen"
					,"SecuenciaAgrupacion","IdEstadoMovimientoComprobanteDetalle","FechaIngresoRegistro","MontoMovimientoComprobanteDetalle","MontoLiquidacion"
					,"IdMoneda","IdMonedaMovimientoComprobanteDetalle","IdMonedaLiquidacion","IdTipoInstrumentoSubyacente","NemotecnicoSubyacente","NombreClasificacionTipoInstrumentoRelacionado"
					,"IdClasificacionTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","IdInstrumentoSubyacente","IdTipoInstrumento","NombreTipoInstrumento"
					,"IdTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumento","IdClasificacionTipoInstrumento","IdInstrumentoRelacionado","NemotecnicoRelacionado"
					,"NombreTipoInstrumentoSubyacente","IdMonedaSubyacente","IdTipoReajuste","FolioTransaccion","Spread","CodigoIFRS","EsEnRueda"
					--,"EsOrdenDirecta"
					,"ModPrefFact","IdSucursalEmpleadoEjecutivoPortafolio","IdCarga","SecMovimientoFacturarMigracion","IdEmpleadoEjecutivoPortafolio","IdMesa"
					,"IdInstrumentoAnterior","FOLIO_COMP_FAC","TIPO_COMP_FAC","LIN_COMP_FAC","RUT_CLI","SEC_RUT_CLI","SEC_MOVTO_FAC","IdMonedaEmision"
				) 
		SELECT 
				"BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL as "IdMovimientoComprobanteDetalle" 
				, ComprobanteDetalle."IdComprobanteDetalle" 			as "IdComprobanteDetalle" 
				, MovimientoComprobante."IdMovimientoComprobante" 		as "IdMovimientoComprobante"  
				, 0 													as "IdTransaccion" 
				, ComprobanteDetalle."IdInstrumento" 					as "IdInstrumento" 
				, ComprobanteDetalle."NemotecnicoInstrumento" 			as "NemotecnicoInstrumento" 
--				, GES_TBCTMVTO."CONCEPTO_MVTO" 							as "IdConceptoMovimiento" 
				, COALESCE(ComprobanteDetalle."IdConcepto",'FACSIN') as "IdConceptoMovimiento"
	
				, GES_TBCTMVTO."CANTIDAD" 								as "Cantidad" 
				, GES_TBCTMVTO."PRECIO" 								as "Precio" 
				, 0 													as "PrecioPromedio" 
				, ABS(GES_TBCTMVTO."MONTO")								as "Monto" 
				, GES_TBCTMVTO."TASA" 									as "Tasa" 
				, 0 													as "TasaEmision" 
				, GES_TBCTMVTO."VAL_RESC"								as "ValorRescate" 
				, 0							as "ValorParidad" 				--TBCTMFRF."PARIDAD_MORIGEN" 
				, ABS(GES_TBCTMVTO."MONTO") as "ValorCaptacionMonedaOrigen"  --TBCTMFRF."CAPTACION_MORIGEN" 
				, 0 													as "SecuenciaAgrupacion" 
				, ComprobanteDetalle."IdEstadoComprobanteDetalle" 		as "IdEstadoMovimientoComprobanteDetalle" 
				, ComprobanteDetalle."FechaIngresoRegistro" 			as "FechaIngresoRegistro" 
				, MovimientoComprobante."MontoMovimientoComprobante" 	as "MontoMovimientoComprobanteDetalle" 
				, ABS(GES_TBCTMVTO."MONTO")								as "MontoLiquidacion" 
				
				
				,CASE WHEN  TRIM(ComprobanteDetalle."IdMoneda")  = '' THEN  'CLP'
					 ELSE COALESCE(ComprobanteDetalle."IdMoneda" ,'CLP') 
				 END	 												as "IdMoneda"
				,CASE WHEN  TRIM(ComprobanteDetalle."IdMoneda")  = '' THEN  'CLP'
					 ELSE COALESCE(ComprobanteDetalle."IdMoneda" ,'CLP') 
				 END	 												as "IdMonedaMovimientoComprobanteDetalle"
				,CASE WHEN  TRIM(ComprobanteDetalle."IdMoneda")  = '' THEN  'CLP'
					 ELSE COALESCE(ComprobanteDetalle."IdMoneda" ,'CLP') 
				 END	 												as "IdMoneda"

				
				
				, ComprobanteDetalle."IdTipoInstrumentoSubyacente" 		as "IdTipoInstrumentoSubyacente" 
				, ComprobanteDetalle."NemotecnicoSubyacente"  			as "NemotecnicoSubyacente" 
				, '' 													as "NombreClasificacionTipoInstrumentoRelacionado" 
				, '' 													as "IdClasificacionTipoInstrumentoRelacionado" 
				, '' 													as "NombreTipoInstrumentoRelacionado" 
				, ComprobanteDetalle."IdInstrumentoSubyacente" 			as "IdInstrumentoSubyacente" 
				, CASE ComprobanteDetalle."IdTipoInstrumento" WHEN 'MON' THEN 'SPT' ELSE ComprobanteDetalle."IdTipoInstrumento" END
																		as "IdTipoInstrumento" 
				
				, ComprobanteDetalle."NombreTipoInstrumento" 			as "NombreTipoInstrumento" 
				, '' 													as "IdTipoInstrumentoRelacionado" 
				, '' 													as "NombreClasificacionTipoInstrumento" 
				, ComprobanteDetalle."IdClasificacionTipoInstrumento"	as "IdClasificacionTipoInstrumento" 
				, 0  													as "IdInstrumentoRelacionado" 
				, '' 													as "NemotecnicoRelacionado" 
				, ComprobanteDetalle."NombreTipoInstrumentoSubyacente" 	as "NombreTipoInstrumentoSubyacente" 
				,'CLP' 													as "IdMonedaSubyacente"
				,'' 													as "IdTipoReajuste" 
				,0 														as "FolioTransaccion"
				,0 														as "Spread"
				,'' 													as "Codigo"
				, '' 													as "EsEnRueda"
				--, '' 													as "EsOrdenDirecta"
				,0 														as "ModPrefFact"
				,0 														as "IdSucursalEmpleadoEjecutivoPortafolio"
				,p_IdCarga 												as "IdCarga"
				,GES_TBCTMVTO."SEC_MOVTO" 								as "SecMovimientoFacturarMigracion"
				,0 														as "IdEmpleadoEjecutivoPortafolio"
				,0 														as "IdMesa"
				,0 														as "IdInstrumentoAnterior"
				,GES_TBCTMVTO."FOLIO_COMP_ADJ" 							as "FOLIO_COMP_FAC"
				,GES_TBCTMVTO."TIPO_COMP_ADJ" 							as "TIPO_COMP_FAC"
				,GES_TBCTMVTO."LIN_COMP_ADJ"							as "LIN_COMP_FAC"
				,GES_TBCTMVTO."RUT_CLI" 								as "RUT_CLI"
				,GES_TBCTMVTO."SEC_RUT_CLI" 							as "SEC_RUT_CLI"
				,GES_TBCTMVTO."SEC_MOVTO" 								as  "SEC_MOVTO_FAC"
				,COALESCE(ComprobanteDetalle."IdMonedaEmision",'CLP')	AS "IdMonedaEmision"
				
		FROM 	BSCL_VIRTUAL."GES_TBCTMVTO" AS GES_TBCTMVTO
				INNER JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS MovimientoComprobante 
						ON 	MovimientoComprobante."SEC_MOVTO" = GES_TBCTMVTO."SEC_MOVTO"

				INNER JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS Comprobante
						ON 	Comprobante."TIPO_COMP_FAC" 	=  CASE 
						                                         WHEN GES_TBCTMVTO."TIPO_COMP_ADJ" = 'IT' THEN 'FD'
						                                         WHEN GES_TBCTMVTO."TIPO_COMP_ADJ" = 'ET' THEN 'FD'
						                                       ELSE GES_TBCTMVTO."TIPO_COMP_ADJ"
						                                       END
						AND Comprobante."FOLIO_COMP_FAC" 	=  GES_TBCTMVTO."FOLIO_COMP_ADJ"
						
				INNER JOIN 	"BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS ComprobanteDetalle
						ON  ComprobanteDetalle."TIPO_COMP_FAC"  = CASE 
						                                            WHEN GES_TBCTMVTO."TIPO_COMP_ADJ" = 'IT' THEN 'FD'
						                                            WHEN GES_TBCTMVTO."TIPO_COMP_ADJ" = 'ET' THEN 'FD'
						                                          ELSE GES_TBCTMVTO."TIPO_COMP_ADJ"
						                                          END
						AND ComprobanteDetalle."FOLIO_COMP_FAC" = GES_TBCTMVTO."FOLIO_COMP_ADJ" 
						AND ComprobanteDetalle."LINEA_COMP_FAC" = GES_TBCTMVTO."LIN_COMP_ADJ" 
						AND GES_TBCTMVTO."RUT_CLI" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then GES_TBCTMVTO."RUT_CLI" else p_RutCliente end
				
				LEFT JOIN  "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS Concepto
                		ON  Concepto."TipoValor" = 'CONCEPTO'
                		AND Concepto."Entidad" = 'MOVIMIENTO'
                		AND Concepto."ValorOrigen" = GES_TBCTMVTO."CONCEPTO_MVTO"
						
--				LEFT  JOIN 	"BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MovtoCompDetalle
--						ON 	MovtoCompDetalle."TIPO_COMP_FAC" 	=  ComprobanteDetalle."TIPO_COMP_FAC"
--						AND MovtoCompDetalle."FOLIO_COMP_FAC"	=  ComprobanteDetalle."FOLIO_COMP_FAC"
--						AND MovtoCompDetalle."LIN_COMP_FAC"  	=  ComprobanteDetalle."LINEA_COMP_FAC"
				
			WHERE 	GES_TBCTMVTO."FEC_MVTO"  BETWEEN  SUBSTRING(p_FechaInicial,1,10) AND  SUBSTRING(p_FechaFinal,1,10)
	   	  	AND 	GES_TBCTMVTO."IND_ANULADO"	= 'I'
 	   	  	AND 	GES_TBCTMVTO."EST_MOVTO"	= 'T';
 	   	  	
			
        -------------------------------------------------------
		----   Movimiento de cobro iva                     ----
		-------------------------------------------------------
		SELECT 	Cobro."ValorCobro" INTO v_valor 
		  FROM 	"BSCL.Plataforma.BD::COM.Cobro" As Cobro
		 WHERE 	Cobro."IdCobro" = 'IVA';
		 
		 
		INSERT INTO "BSCL.Homologacion.BD::HOM.ComprobanteCobro"
				(
					 "IdComprobanteCobro","IdCobro","IdComprobante","IdMoneda"
					,"Monto","Porcentaje","FechaSistema","FechaIngresoRegistro"
					,"IdEstado","IdMonedaComprobanteCobro","IdMonedaLiquidacion"
					,"MontoComprobanteCobro","MontoLiquidacion","IdCarga"
					,"FOLIO_COMP_FAC","TIPO_COMP_FAC"  		     
				)  	
		SELECT     "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro".NEXTVAL 	as "IdComprobanteCobro"
				  ,'IVA'								as "IdCobro"
				  ,	Comprobante."IdComprobante"			as "IdComprobante"
				  ,'CLP'								as "IdMoneda"
			  	  ,GES_TBCTMSCF."MONTO_IVA"				as "Monto"
				  ,v_valor								as "Porcentaje"
				  ,Comprobante."FechaSistema"			as "FechaSistema"
				  ,Comprobante."FechaIngresoRegistro"	as "FechaIngresoRegistro"
				  ,CASE WHEN Comprobante."IdEstadoComprobante" = 'ANU' THEN Comprobante."IdEstadoComprobante" 
				  	    ELSE 'VIG'
				   END									as "IdEstado"
				  ,'CLP'								as "IdMonedaComprobanteCobro" 
				  ,'CLP'								as "IdMonedaLiquidacion"
				  ,GES_TBCTMSCF."MONTO_IVA"				as "MontoComprobanteCobro"
				  ,GES_TBCTMSCF."MONTO_IVA"				as "MontoLiquidacion"
				  ,p_IdCarga 							as "IdCarga"
				  ,GES_TBCTMSCF."FOLIO_COMP_FAC"		as "FOLIO_COMP_FAC"
				  ,GES_TBCTMSCF."TIPO_COMP_FAC"			as "TIPO_COMP_FAC"
				  
		FROM	"BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF
				INNER JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" as Comprobante
						ON	Comprobante."TIPO_COMP_FAC" 	= 	GES_TBCTMSCF."TIPO_COMP_FAC"
						AND	Comprobante."FOLIO_COMP_FAC"	=	GES_TBCTMSCF."FOLIO_COMP_FAC"
				
		WHERE 	GES_TBCTMSCF."FEC_ING_COMP"  BETWEEN  SUBSTRING(p_FechaInicial,1,10) AND  SUBSTRING(p_FechaFinal,1,10)
		  AND 	GES_TBCTMSCF."MONTO_IVA" > 0 ;
		  
--			AND GES_TBCTMSCF."TIPO_COMP_FAC" = 'FD'
--			AND GES_TBCTMSCF."FOLIO_COMP_FAC" =674404;
			
        -------------------------------------------------------
		----   Movimiento de cobro Comision                ----
		-------------------------------------------------------
		SELECT 	Cobro."ValorCobro" INTO v_valor 
		  FROM 	"BSCL.Plataforma.BD::COM.Cobro" As Cobro
		 WHERE 	Cobro."IdCobro" = 'COVA';
		 
		 
		INSERT INTO "BSCL.Homologacion.BD::HOM.ComprobanteCobro"
				(
					 "IdComprobanteCobro","IdCobro","IdComprobante","IdMoneda"
					,"Monto","Porcentaje","FechaSistema","FechaIngresoRegistro"
					,"IdEstado","IdMonedaComprobanteCobro","IdMonedaLiquidacion"
					,"MontoComprobanteCobro","MontoLiquidacion","IdCarga"
					,"FOLIO_COMP_FAC","TIPO_COMP_FAC"  		     
				)  	
		SELECT     "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro".NEXTVAL 	as "IdComprobanteCobro"
				  ,'COVA'								as "IdCobro"
				  ,	Comprobante."IdComprobante"			as "IdComprobante"
				  ,'CLP'								as "IdMoneda"
			  	  ,GES_TBCTMSCF."MONTO_COMISION"		as "Monto"
				  ,v_valor								as "Porcentaje"
				  ,Comprobante."FechaSistema"			as "FechaSistema"
				  ,Comprobante."FechaIngresoRegistro"	as "FechaIngresoRegistro"
				  ,CASE WHEN Comprobante."IdEstadoComprobante" = 'ANU' THEN Comprobante."IdEstadoComprobante" 
				  	    ELSE 'VIG'
				   END									as "IdEstado"
				  ,'CLP'								as "IdMonedaComprobanteCobro" 
				  ,'CLP'								as "IdMonedaLiquidacion"
				  ,GES_TBCTMSCF."MONTO_COMISION"		as "MontoComprobanteCobro"
				  ,GES_TBCTMSCF."MONTO_COMISION"		as "MontoLiquidacion"
				  ,p_IdCarga 							as "IdCarga"
				  ,GES_TBCTMSCF."FOLIO_COMP_FAC"		as "FOLIO_COMP_FAC"
				  ,GES_TBCTMSCF."TIPO_COMP_FAC"			as "TIPO_COMP_FAC"
				  
		FROM	"BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF
				INNER  JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" as Comprobante
						ON	Comprobante."TIPO_COMP_FAC" 	= 	GES_TBCTMSCF."TIPO_COMP_FAC"
						AND	Comprobante."FOLIO_COMP_FAC"	=	GES_TBCTMSCF."FOLIO_COMP_FAC"
				
		WHERE 	GES_TBCTMSCF."FEC_ING_COMP"  BETWEEN  SUBSTRING(p_FechaInicial,1,10) AND  SUBSTRING(p_FechaFinal,1,10)
		  AND 	GES_TBCTMSCF."MONTO_COMISION" > 0 ;

--			AND GES_TBCTMSCF."TIPO_COMP_FAC" = 'FD'
--			AND GES_TBCTMSCF."FOLIO_COMP_FAC" =674404;
		  

        -------------------------------------------------------
		----   Movimiento de cobro Derechos                ----
		-------------------------------------------------------
		SELECT 	Cobro."ValorCobro" INTO v_valor 
		  FROM 	"BSCL.Plataforma.BD::COM.Cobro" As Cobro
		 WHERE 	Cobro."IdCobro" = 'DEVA';
		 
		 
		INSERT INTO "BSCL.Homologacion.BD::HOM.ComprobanteCobro"
				(
					 "IdComprobanteCobro","IdCobro","IdComprobante","IdMoneda"
					,"Monto","Porcentaje","FechaSistema","FechaIngresoRegistro"
					,"IdEstado","IdMonedaComprobanteCobro","IdMonedaLiquidacion"
					,"MontoComprobanteCobro","MontoLiquidacion","IdCarga"
					,"FOLIO_COMP_FAC","TIPO_COMP_FAC"  		     
				)  	
		SELECT     "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro".NEXTVAL 	as "IdComprobanteCobro"
				  ,'DEVA'								as "IdCobro"
				  ,	Comprobante."IdComprobante"			as "IdComprobante"
				  ,'CLP'								as "IdMoneda"
			  	  ,GES_TBCTMSCF."MONTO_DERECHOS"		as "Monto"
				  ,v_valor								as "Porcentaje"
				  ,Comprobante."FechaSistema"			as "FechaSistema"
				  ,Comprobante."FechaIngresoRegistro"	as "FechaIngresoRegistro"
				  ,CASE WHEN Comprobante."IdEstadoComprobante" = 'ANU' THEN Comprobante."IdEstadoComprobante" 
				  	    ELSE 'VIG'
				   END									as "IdEstado"
				  ,'CLP'								as "IdMonedaComprobanteCobro" 
				  ,'CLP'								as "IdMonedaLiquidacion"
				  ,GES_TBCTMSCF."MONTO_DERECHOS"		as "MontoComprobanteCobro"
				  ,GES_TBCTMSCF."MONTO_DERECHOS"		as "MontoLiquidacion"
				  ,p_IdCarga 							as "IdCarga"
				  ,GES_TBCTMSCF."FOLIO_COMP_FAC"		as "FOLIO_COMP_FAC"
				  ,GES_TBCTMSCF."TIPO_COMP_FAC"			as "TIPO_COMP_FAC"
				  
		FROM	"BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF
				INNER JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" as Comprobante
						ON	Comprobante."TIPO_COMP_FAC" 	= 	GES_TBCTMSCF."TIPO_COMP_FAC"
						AND	Comprobante."FOLIO_COMP_FAC"	=	GES_TBCTMSCF."FOLIO_COMP_FAC"
				
		WHERE 	GES_TBCTMSCF."FEC_ING_COMP"  BETWEEN  SUBSTRING(p_FechaInicial,1,10) AND  SUBSTRING(p_FechaFinal,1,10)
		  AND 	GES_TBCTMSCF."MONTO_DERECHOS" > 0 ;
		  
--			AND GES_TBCTMSCF."TIPO_COMP_FAC" = 'FD'
--			AND GES_TBCTMSCF."FOLIO_COMP_FAC" =674404;

        -------------------------------------------------------
		----   Movimiento de cobro GASTO                   ----
		-------------------------------------------------------
		SELECT 	Cobro."ValorCobro" INTO v_valor 
		  FROM 	"BSCL.Plataforma.BD::COM.Cobro" As Cobro
		 WHERE 	Cobro."IdCobro" = 'GFFA';
		 
		 
		INSERT INTO "BSCL.Homologacion.BD::HOM.ComprobanteCobro"
				(
					 "IdComprobanteCobro","IdCobro","IdComprobante","IdMoneda"
					,"Monto","Porcentaje","FechaSistema","FechaIngresoRegistro"
					,"IdEstado","IdMonedaComprobanteCobro","IdMonedaLiquidacion"
					,"MontoComprobanteCobro","MontoLiquidacion","IdCarga"
					,"FOLIO_COMP_FAC","TIPO_COMP_FAC"  		     
				)  	
		SELECT     "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro".NEXTVAL 	as "IdComprobanteCobro"
				  ,'GFFA'								as "IdCobro"
				  ,	Comprobante."IdComprobante"			as "IdComprobante"
				  ,'CLP'								as "IdMoneda"
			  	  ,GES_TBCTMSCF."MONTO_GASTO"			as "Monto"
				  ,0									as "Porcentaje"
				  ,Comprobante."FechaSistema"			as "FechaSistema"
				  ,Comprobante."FechaIngresoRegistro"	as "FechaIngresoRegistro"
				  ,CASE WHEN Comprobante."IdEstadoComprobante" = 'ANU' THEN Comprobante."IdEstadoComprobante" 
				  	    ELSE 'VIG'
				   END									as "IdEstado"
				  ,'CLP'								as "IdMonedaComprobanteCobro" 
				  ,'CLP'								as "IdMonedaLiquidacion"
				  ,GES_TBCTMSCF."MONTO_GASTO"			as "MontoComprobanteCobro"
				  ,GES_TBCTMSCF."MONTO_GASTO"			as "MontoLiquidacion"
				  ,p_IdCarga 							as "IdCarga"
				  ,GES_TBCTMSCF."FOLIO_COMP_FAC"		as "FOLIO_COMP_FAC"
				  ,GES_TBCTMSCF."TIPO_COMP_FAC"			as "TIPO_COMP_FAC"
				  
		FROM	"BSCL_VIRTUAL"."GES_TBCTMSCF" AS GES_TBCTMSCF
				INNER JOIN 	"BSCL.Homologacion.BD::HOM.Comprobante" as Comprobante
						ON	Comprobante."TIPO_COMP_FAC" 	= 	GES_TBCTMSCF."TIPO_COMP_FAC"
						AND	Comprobante."FOLIO_COMP_FAC"	=	GES_TBCTMSCF."FOLIO_COMP_FAC"
				
		WHERE 	GES_TBCTMSCF."FEC_ING_COMP"  BETWEEN  SUBSTRING(p_FechaInicial,1,10) AND  SUBSTRING(p_FechaFinal,1,10)
		  AND 	GES_TBCTMSCF."MONTO_GASTO" > 0 ;

--			AND GES_TBCTMSCF."TIPO_COMP_FAC" = 'FD'
--			AND GES_TBCTMSCF."FOLIO_COMP_FAC" =674404;
		  

END;
