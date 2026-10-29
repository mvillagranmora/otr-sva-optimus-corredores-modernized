SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOperacionFondosMutuos";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de solicitudes FFMM 
--  FFMM
--  Autor: - EAR
 
"BSCL.Homologacion.BD.Servicios::CargarOperacionFondosMutuos"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	-- Salidas para mensajes de advertencias y errores
	OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		----------------------------------------------------- 
	    -- declaracion variables 
	    DECLARE v_Total_Operaciones_Forward BIGINT:=0;
	    DECLARE v_Total_Forward_Cargados BIGINT :=0;
	    

			INSERT INTO "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
			(	"IdSolicitudAporteRescate" ,"IdCliente" ,"IdPortafolio" ,"TipoSolicitud" ,"IdSerieFondo" ,"IdTipoMovimientoFondoMutuo" ,"IdDividendo" ,"IdSerieFondoDestino" 
				,"IdEmisorDestino" ,"IdClienteDestino" ,"IdPortafolioDestino" ,"IdSerieFondoOrigen" ,"IdEmisorOrigen" ,"MontoOrigen" ,"IndicadorMLT" ,"Overnight" 
				,"IdSolicitudAporteRescateRelacionado" ,"IdTipoCartera" ,"CalculoPorMontoCuota" ,"FechaEmision" ,"FechaAceptacion" ,"FechaCertificacion" 
				,"FechaLiquidacion" ,"FechaAnulacion" ,"FechaOriginalInversion" ,"FechaIngresoRegistro" ,"FechaAnulacionRegistro" ,"FechaAceptacionRegistro" 
				,"FechaCertificacionRegistro" ,"FechaLiquidacionRegistro" ,"Monto" ,"NumeroCuotas" ,"ValorCuota" ,"CostoUF" ,"CostoMonedaLocal" ,"ValorUFFechaInversion" 
				,"ParidadFechaInversion" ,"IdMoneda" ,"MontoComision" ,"MontoPago" ,"Estado" ,"Glosa" ,"IdEmpresa" ,"IdUsuario" ,"IdModulo" ,"EstadoLiquidacion" 
				,"MueveCta" ,"FolioCartaLiquidacion" ,"IdMedioDePago" ,"MontoInversion" ,"NumeroCuotasInversion" ,"ValorCuotaInversion" ,"CostoMonedaLocalInversion" 
				,"ParidadFechaCertificacion" ,"MontoComisionLocal" ,"MontoPagoLocal" ,"FechaModificacion" ,"FechaModificacionRegistro" ,"OrigenOperacion" 
				,"SolicitudBatch" ,"ComisionInformada"
				,"FEC_MVTO","SEC_MOVTO","SEC_ID" ,"COD_MOVTO" ,"RUT_CLI" ,"SEC_RUT_CLI" ,"NEMO" 
			    ,"IdCarga","IND_BOLSA","TIPO_COMP_ADJ"
			) 
			select
			         "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate" 
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, HOM_TIPO_SOLICITUD."ValorDestino" as "TipoSolicitud"-- homologar tipo solicitud  
					, TRIM(TBPLMAIN."NEMO") as "IdSerieFondo" 
					, CASE WHEN HOM_TIPO_SOLICITUD."ValorDestino" = 'APO' THEN 'APONORM' ELSE 'RESNORM' END as "IdTipoMovimientoFondoMutuo" --DIV CUOTAS,REINVERSION,ETC
					, 0  as "IdDividendo" 
					, 0  as "IdSerieFondoDestino" -- REVISAR 
					, '' as "IdEmisorDestino" 
					, 0  as "IdClienteDestino" -- en caso de traspasos
					, 0  as "IdPortafolioDestino" -- en casos de transferencias
					, '' as "IdSerieFondoOrigen" 
					, TBCTMVTO."EMISOR" as "IdEmisorOrigen" 
					, 0 as "MontoOrigen" 
					, '' as "IndicadorMLT" 
					, '' as "Overnight" -- no se donde esta este indicador
					, 0 as "IdSolicitudAporteRescateRelacionado" -- este debe ser por los difereridos
					, 0 as "IdTipoCartera" -- no se que es esto
					, '' as "CalculoPorMontoCuota" --no se que es esto
					, TBCTMVTO."FEC_EMI" as "FechaEmision"-- para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacion" --para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacion" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacion" 
					, '0001-01-01' as "FechaAnulacion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaOriginalInversion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaIngresoRegistro" 
					, '0001-01-01' as "FechaAnulacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacionRegistro" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacionRegistro" 
					, TBCTMVTO."MONTO" as "Monto" --esto esta en moneda de fondo 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotas" 
					, TBCTMVTO."PRECIO" as "ValorCuota" 
					, 0 as "CostoUF" --- que es esto
					, 0 as "CostoMonedaLocal" -- el problema es que no tengo la conversion en la moneda calculo la paridad a fecha de certificacion?? 
					, 0 as "ValorUFFechaInversion" 
					, 0 as "ParidadFechaInversion" 
					, HOM_TIPO_MONEDA."ValorDestino" as "IdMoneda" 
					, 0 as "MontoComision" 
					, 0 as "MontoPago" 
					, 'ING' as "Estado" 
					, '' as "Glosa" 
					, p_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, p_Modulo as "IdModulo" 
					, '' as "EstadoLiquidacion" -- hay que revisar esto
					, 'S' as "MueveCta" 
					, 0 as "FolioCartaLiquidacion" 
					, 0 as "IdMedioDePago" 
					, TBCTMVTO."MONTO" as "MontoInversion" 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotasInversion" 
					, TBCTMVTO."PRECIO" as "ValorCuotaInversion" 
					, 0 as "CostoMonedaLocalInversion" 
					, 0 as "ParidadFechaCertificacion" --se puede buscar en la ruebas 
					, 0 as "MontoComisionLocal" 
					, 0 as "MontoPagoLocal" 
					, '0001-01-01' as "FechaModificacion" 
					, '0001-01-01' as "FechaModificacionRegistro" 
					, 'M' as "OrigenOperacion" --- no se lo que es 
					, 'N' as "SolicitudBatch" 
					, 'N' as "ComisionInformada"
					, TBCTMVTO."FEC_MVTO" as "FEC_MVTO"
					, TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO"
					, 0 as "SEC_ID" 
					, TBCTMVTO."COD_MOVTO" as "COD_MOVTO" 
					, TBCTMVTO."RUT_CLI" as "RUT_CLI"
					, TBCTMVTO."SEC_RUT_CLI" as "SEC_RUT_CLI" 
					, TBCTMVTO."NEMO" as "NEMO"
					, p_IdCarga
					,TBCTMVTO."IND_BOLSA" as "IND_BOLSA"
					,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
			  FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
			 	INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_SOLICITUD
					ON HOM_TIPO_SOLICITUD."TipoValor" = 'TipoSolicitudFFMM'
					AND HOM_TIPO_SOLICITUD."ValorOrigen" = TRIM(TBCTMVTO."COD_MOVTO")
			    INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS TBPLMAIN
			    	ON TRIM(TBCTMVTO."NEMO") = TRIM(TBPLMAIN."NEMO") 
			    	AND TBPLMAIN."MERCADO" ='FM'
			    	AND TBPLMAIN."IND_DIFERIDO" = 'N'
			    	AND TRIM(TBPLMAIN."NEMO_ASOC") = '' 
			    INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
					ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
					AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM")  
			   -- INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO
			   -- 	ON INSTRUMENTO."Nemotecnico" = TRIM(TBPLMAIN."NEMO") se comenta por el momento ya que estan cargados los nemo FFMM
			    INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					ON CLIENTE."DocumentoIdentidad" = TRIM(TBCTMVTO.RUT_CLI)
				INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
					ON PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO.SEC_RUT_CLI||''
					AND PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS HOM_SOLICITUD
			    	ON HOM_SOLICITUD."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
			    	AND HOM_SOLICITUD."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO" 
			  WHERE HOM_SOLICITUD."FEC_MVTO" IS NULL
			  	AND TBCTMVTO."SEC_CAR_COMPROM" =0
			  	AND TBCTMVTO."COD_MOVTO" IN ('CIFM','VIFM');-- quizas cargamso los dividendos

			 -- REINVERSIONES -- entre fondos normales
             -- aquellos reinversiones que se realizan entre fondos que no tienen asociado un fondos diferido
             -- por cuotas partiremos con los rescates

             INSERT INTO "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
			(	"IdSolicitudAporteRescate" ,"IdCliente" ,"IdPortafolio" ,"TipoSolicitud" ,"IdSerieFondo" ,"IdTipoMovimientoFondoMutuo" ,"IdDividendo" ,"IdSerieFondoDestino" 
				,"IdEmisorDestino" ,"IdClienteDestino" ,"IdPortafolioDestino" ,"IdSerieFondoOrigen" ,"IdEmisorOrigen" ,"MontoOrigen" ,"IndicadorMLT" ,"Overnight" 
				,"IdSolicitudAporteRescateRelacionado" ,"IdTipoCartera" ,"CalculoPorMontoCuota" ,"FechaEmision" ,"FechaAceptacion" ,"FechaCertificacion" 
				,"FechaLiquidacion" ,"FechaAnulacion" ,"FechaOriginalInversion" ,"FechaIngresoRegistro" ,"FechaAnulacionRegistro" ,"FechaAceptacionRegistro" 
				,"FechaCertificacionRegistro" ,"FechaLiquidacionRegistro" ,"Monto" ,"NumeroCuotas" ,"ValorCuota" ,"CostoUF" ,"CostoMonedaLocal" ,"ValorUFFechaInversion" 
				,"ParidadFechaInversion" ,"IdMoneda" ,"MontoComision" ,"MontoPago" ,"Estado" ,"Glosa" ,"IdEmpresa" ,"IdUsuario" ,"IdModulo" ,"EstadoLiquidacion" 
				,"MueveCta" ,"FolioCartaLiquidacion" ,"IdMedioDePago" ,"MontoInversion" ,"NumeroCuotasInversion" ,"ValorCuotaInversion" ,"CostoMonedaLocalInversion" 
				,"ParidadFechaCertificacion" ,"MontoComisionLocal" ,"MontoPagoLocal" ,"FechaModificacion" ,"FechaModificacionRegistro" ,"OrigenOperacion" 
				,"SolicitudBatch" ,"ComisionInformada"
				,"FEC_MVTO","SEC_MOVTO","SEC_ID" ,"COD_MOVTO" ,"RUT_CLI" ,"SEC_RUT_CLI" ,"NEMO" 
			    ,"IdCarga","IND_BOLSA","TIPO_COMP_ADJ"
			) 
			select
			         "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate" 
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, HOM_TIPO_SOLICITUD."ValorDestino" as "TipoSolicitud"-- homologar tipo solicitud  
					, TRIM(TBFMREIN."NEMO_O") as "IdSerieFondo" 
					, CASE WHEN HOM_TIPO_SOLICITUD."ValorDestino" = 'APO' THEN 'APOREINV' ELSE 'RESREINV' END  as "IdTipoMovimientoFondoMutuo" --DIV CUOTAS,REINVERSION,ETC
					, 0  as "IdDividendo" 
					, TRIM(TBFMREIN."NEMO_D")  as "IdSerieFondoDestino" -- REVISAR 
					, '' as "IdEmisorDestino" 
					, 0 as "IdClienteDestino" -- en caso de traspasos
					, 0 as "IdPortafolioDestino" -- en casos de transferencias
					, TRIM(TBFMREIN."NEMO_O") as "IdSerieFondoOrigen" 
					, TBCTMVTO."EMISOR" as "IdEmisorOrigen" 
					, 0 as "MontoOrigen" 
					, TBFMREIN."IND_MLT" as "IndicadorMLT" 
					, '' as "Overnight" -- no se donde esta este indicador
					, 0 as "IdSolicitudAporteRescateRelacionado" -- este debe ser por los difereridos
					, 0 as "IdTipoCartera" -- no se que es esto
					, ''   as "CalculoPorMontoCuota" --no se que es esto
					, TBCTMVTO."FEC_EMI" as "FechaEmision"-- para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacion" --para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacion" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacion" 
					, '0001-01-01' as "FechaAnulacion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaOriginalInversion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaIngresoRegistro" 
					, '0001-01-01' as "FechaAnulacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacionRegistro" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacionRegistro" 
					, TBCTMVTO."MONTO" as "Monto" --esto esta en moneda de fondo 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotas" 
					, TBCTMVTO."PRECIO" as "ValorCuota" 
					, 0 as "CostoUF" --- que es esto
					, 0 as "CostoMonedaLocal" -- el problema es que no tengo la conversion en la moneda calculo la paridad a fecha de certificacion?? 
					, 0 as "ValorUFFechaInversion" 
					, 0 as "ParidadFechaInversion" 
					, HOM_TIPO_MONEDA."ValorDestino" as "IdMoneda" 
					, 0 as "MontoComision" 
					, 0 as "MontoPago" 
					, 'ING' as "Estado" 
					, 'carga masiva'  as "Glosa" 
					, p_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, p_Modulo as "IdModulo" 
					, '' as "EstadoLiquidacion" -- hay que revisar esto
					, 'N' as "MueveCta" 
					, 0 as "FolioCartaLiquidacion" 
					, 0 as "IdMedioDePago" 
					, TBCTMVTO."MONTO" as "MontoInversion" 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotasInversion" 
					, TBCTMVTO."PRECIO" as "ValorCuotaInversion" 
					, 0 as "CostoMonedaLocalInversion" 
					, 0 as "ParidadFechaCertificacion" --se puede buscar en la ruebas 
					, 0 as "MontoComisionLocal" 
					, 0 as "MontoPagoLocal" 
					, '0001-01-01' as "FechaModificacion" 
					, '0001-01-01' as "FechaModificacionRegistro" 
					, 'M' as "OrigenOperacion" --- no se lo que es 
					, 'N' as "SolicitudBatch" 
					, 'N' as "ComisionInformada"
					,TBCTMVTO."FEC_MVTO" as "FEC_MVTO"
					,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO"
					,TBCTMVTO."SEC_CAR_COMPROM" as "SEC_ID" 
					,TBCTMVTO."COD_MOVTO" as "COD_MOVTO" 
					,TBCTMVTO."RUT_CLI" as "RUT_CLI" 
					,TBCTMVTO."SEC_RUT_CLI" as "SEC_RUT_CLI" 
					,TBCTMVTO."NEMO" as "NEMO"
					,p_IdCarga
					,TBCTMVTO."IND_BOLSA" as "IND_BOLSA"
					,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
	    			FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
							INNER JOIN "BSCL_VIRTUAL"."GES_TBFMREIN" AS TBFMREIN
								ON RIGHT('00000000000'|| TRIM(TBCTMVTO."RUT_CLI"),11)  =  TBFMREIN."RUT_CLI"
								AND TRIM(TBCTMVTO."SEC_RUT_CLI")  =  TBFMREIN."SEC_RUT_CLI" 
								AND TRIM(TBCTMVTO."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND TBCTMVTO."COD_MOVTO" = 'VIFM'
								AND TBCTMVTO."CANTIDAD" = TBFMREIN."CANTIDAD" 
								AND TBFMREIN."IND_MONTO" = 'C'
								AND TBCTMVTO."SEC_CAR_COMPROM" !=0
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_ORIGEN
								ON TRIM(INTRUMENTO_ORIGEN."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND INTRUMENTO_ORIGEN.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_ORIGEN."NEMO_ASOC") = ''
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_DESTINO
								ON TRIM(INTRUMENTO_DESTINO."NEMO") = TRIM(TBFMREIN."NEMO_D")
								AND INTRUMENTO_DESTINO.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_DESTINO."NEMO_ASOC") = ''
							INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
								ON CLIENTE."DocumentoIdentidad" = TRIM(TBCTMVTO.RUT_CLI)
							INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
								ON PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO.SEC_RUT_CLI||''
						  	 	AND PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							--INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO se llenan por el momento 
							--	ON INSTRUMENTO."Nemotecnico" = trim(TBCTMVTO."NEMO")                     -- solo llenan serie origen)
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_SOLICITUD
								ON HOM_TIPO_SOLICITUD."TipoValor" = 'TipoSolicitudFFMM'
								AND HOM_TIPO_SOLICITUD."ValorOrigen" = TRIM(TBCTMVTO."COD_MOVTO")
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
								ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
								AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(INTRUMENTO_ORIGEN."TIPO_FAM") 
							LEFT JOIN "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS HOM_SOLICITUD
			    				ON HOM_SOLICITUD."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
			    				AND HOM_SOLICITUD."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO" 
			  				WHERE HOM_SOLICITUD."FEC_MVTO" IS NULL;

			  -- ahora necesitamos recuperar los aportes asociados a los rescates (por cuotas)
			  INSERT INTO "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
				(	"IdSolicitudAporteRescate" ,"IdCliente" ,"IdPortafolio" ,"TipoSolicitud" ,"IdSerieFondo" ,"IdTipoMovimientoFondoMutuo" ,"IdDividendo" ,"IdSerieFondoDestino" 
				,"IdEmisorDestino" ,"IdClienteDestino" ,"IdPortafolioDestino" ,"IdSerieFondoOrigen" ,"IdEmisorOrigen" ,"MontoOrigen" ,"IndicadorMLT" ,"Overnight" 
				,"IdSolicitudAporteRescateRelacionado" ,"IdTipoCartera" ,"CalculoPorMontoCuota" ,"FechaEmision" ,"FechaAceptacion" ,"FechaCertificacion" 
				,"FechaLiquidacion" ,"FechaAnulacion" ,"FechaOriginalInversion" ,"FechaIngresoRegistro" ,"FechaAnulacionRegistro" ,"FechaAceptacionRegistro" 
				,"FechaCertificacionRegistro" ,"FechaLiquidacionRegistro" ,"Monto" ,"NumeroCuotas" ,"ValorCuota" ,"CostoUF" ,"CostoMonedaLocal" ,"ValorUFFechaInversion" 
				,"ParidadFechaInversion" ,"IdMoneda" ,"MontoComision" ,"MontoPago" ,"Estado" ,"Glosa" ,"IdEmpresa" ,"IdUsuario" ,"IdModulo" ,"EstadoLiquidacion" 
				,"MueveCta" ,"FolioCartaLiquidacion" ,"IdMedioDePago" ,"MontoInversion" ,"NumeroCuotasInversion" ,"ValorCuotaInversion" ,"CostoMonedaLocalInversion" 
				,"ParidadFechaCertificacion" ,"MontoComisionLocal" ,"MontoPagoLocal" ,"FechaModificacion" ,"FechaModificacionRegistro" ,"OrigenOperacion" 
				,"SolicitudBatch" ,"ComisionInformada"
				,"FEC_MVTO","SEC_MOVTO","SEC_ID" ,"COD_MOVTO" ,"RUT_CLI" ,"SEC_RUT_CLI" ,"NEMO" 
				,"IdCarga","IND_BOLSA","TIPO_COMP_ADJ"
				) 	
			  select
			          "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate" 
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, HOM_TIPO_SOLICITUD."ValorDestino" as "TipoSolicitud"-- homologar tipo solicitud  
					, TRIM(TBCTMVTO."NEMO") as "IdSerieFondo" 
					, CASE WHEN HOM_TIPO_SOLICITUD."ValorDestino" = 'APO' THEN 'APOREINV' ELSE 'RESREINV' END  as "IdTipoMovimientoFondoMutuo" --DIV CUOTAS,REINVERSION,ETC
					, 0  as "IdDividendo" 
					, ''  as "IdSerieFondoDestino" -- REVISAR 
					, '' as "IdEmisorDestino" 
					, 0  as "IdClienteDestino" -- en caso de traspasos
					, 0 as "IdPortafolioDestino" -- en casos de transferencias
					, '' as "IdSerieFondoOrigen" 
					, '' as "IdEmisorOrigen" 
					, 0 as "MontoOrigen" 
					, RESCATES."IND_MLT" as "IndicadorMLT" 
					, '' as "Overnight" -- no se donde esta este indicador
					, 0 as "IdSolicitudAporteRescateRelacionado" -- este debe ser por los difereridos
					, 0 as "IdTipoCartera" -- no se que es esto
					, '' as "CalculoPorMontoCuota" --no se que es esto
					, TBCTMVTO."FEC_EMI" as "FechaEmision"-- para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacion" --para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacion" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacion" 
					, '0001-01-01' as "FechaAnulacion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaOriginalInversion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaIngresoRegistro" 
					, '0001-01-01' as "FechaAnulacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacionRegistro" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacionRegistro" 
					, TBCTMVTO."MONTO" as "Monto" --esto esta en moneda de fondo 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotas" 
					, TBCTMVTO."PRECIO" as "ValorCuota" 
					, 0 as "CostoUF" --- que es esto
					, 0 as "CostoMonedaLocal" -- el problema es que no tengo la conversion en la moneda calculo la paridad a fecha de certificacion?? 
					, 0 as "ValorUFFechaInversion" 
					, 0 as "ParidadFechaInversion" 
					, HOM_TIPO_MONEDA."ValorDestino" as "IdMoneda" 
					, 0 as "MontoComision" 
					, 0 as "MontoPago" 
					, 'ING' as "Estado" 
					, '' as "Glosa" 
					, P_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, p_Modulo as "IdModulo" 
					, '' as "EstadoLiquidacion" -- hay que revisar esto
					, 'S' as "MueveCta" 
					, 0 as "FolioCartaLiquidacion" 
					, 0 as "IdMedioDePago" 
					, TBCTMVTO."MONTO" as "MontoInversion" 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotasInversion" 
					, TBCTMVTO."PRECIO" as "ValorCuotaInversion" 
					, 0 as "CostoMonedaLocalInversion" 
					, 0 as "ParidadFechaCertificacion" --se puede buscar en la ruebas 
					, 0 as "MontoComisionLocal" 
					, 0 as "MontoPagoLocal" 
					, '0001-01-01' as "FechaModificacion" 
					, '0001-01-01' as "FechaModificacionRegistro" 
					, 'M' as "OrigenOperacion" --- no se lo que es 
					, 'N' as "SolicitudBatch" 
					, 'N' as "ComisionInformada"
					,TBCTMVTO."FEC_MVTO" as "FEC_MVTO"
					,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO"
					,TBCTMVTO."SEC_CAR_COMPROM" as "SEC_ID" 
					,TBCTMVTO."COD_MOVTO" as "COD_MOVTO" 
					,TBCTMVTO."RUT_CLI" as "RUT_CLI" 
					,TBCTMVTO."SEC_RUT_CLI" as "SEC_RUT_CLI" 
					,TBCTMVTO."NEMO" as "NEMO"
              		,p_IdCarga
              		,TBCTMVTO."IND_BOLSA" as "IND_BOLSA"
              		,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
              		FROM -- por cuotas
					  (SELECT TBCTMVTO."SEC_CAR_COMPROM"
					          ,TBCTMVTO."RUT_CLI"
					          ,TBCTMVTO."SEC_RUT_CLI"
					          ,TBFMREIN."NEMO_D"
					          ,TBFMREIN."CANTIDAD"
					          ,TBFMREIN."IND_MLT"
					          ,TBCTMVTO."SEC_MOVTO"
					          ,TBCTMVTO."FEC_MVTO"
					          ,TBFMREIN."NEMO_O"
					          ,TBFMREIN."SERIE_O"
					          ,TBFMREIN."SERIE_D"
					          ,TBFMREIN."IND_MONTO"
					   FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
							INNER JOIN "BSCL_VIRTUAL"."GES_TBFMREIN" AS TBFMREIN
								ON RIGHT('00000000000'|| TRIM(TBCTMVTO."RUT_CLI"),11)  =  TBFMREIN."RUT_CLI"
								AND TRIM(TBCTMVTO."SEC_RUT_CLI")  =  TBFMREIN."SEC_RUT_CLI" 
								AND TRIM(TBCTMVTO."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND TBCTMVTO."COD_MOVTO" = 'VIFM'
								AND TBCTMVTO."CANTIDAD" = TBFMREIN."CANTIDAD" 
								AND TBFMREIN."IND_MONTO" = 'C'
								AND TBCTMVTO."SEC_CAR_COMPROM" !=0
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_ORIGEN
								ON TRIM(INTRUMENTO_ORIGEN."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND INTRUMENTO_ORIGEN.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_ORIGEN."NEMO_ASOC") = ''
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_DESTINO
								ON TRIM(INTRUMENTO_DESTINO."NEMO") = TRIM(TBFMREIN."NEMO_D")
								AND INTRUMENTO_DESTINO.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_DESTINO."NEMO_ASOC") = ''
								) AS RESCATES
							INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
								ON  TBCTMVTO."RUT_CLI" = RESCATES."RUT_CLI"
								AND TBCTMVTO."SEC_RUT_CLI" = RESCATES."SEC_RUT_CLI"
								AND TRIM(TBCTMVTO."NEMO") = TRIM(RESCATES."NEMO_D")
								AND TBCTMVTO."COD_MOVTO" = 'CIFM'
								AND TBCTMVTO."SEC_CAR_COMPROM" =  RESCATES."SEC_CAR_COMPROM"
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS TBPLMAIN
								ON TRIM(TBPLMAIN."NEMO") = TRIM(TBCTMVTO."NEMO")
								AND TBPLMAIN."MERCADO" ='FM'
							INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
								ON CLIENTE."DocumentoIdentidad" = TRIM(TBCTMVTO.RUT_CLI)
							INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
								ON PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO.SEC_RUT_CLI||''
						 	   AND PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							--INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO --los aportes por reinversion
							--	ON INSTRUMENTO."Nemotecnico" = TBCTMVTO."NEMO"                     -- solo llenan serie origen
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_SOLICITUD
								ON HOM_TIPO_SOLICITUD."TipoValor" = 'TipoSolicitudFFMM'
								AND HOM_TIPO_SOLICITUD."ValorOrigen" = TBCTMVTO."COD_MOVTO"
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
								ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
								AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM")
							LEFT JOIN  "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS HOM_SOLICITUD
								ON  HOM_SOLICITUD."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
								AND HOM_SOLICITUD."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
							WHERE HOM_SOLICITUD."FEC_MVTO" IS NULL;

			 -- REINVERSIONES -- entre fondos normales
             -- aquellos reinversiones que se realizan entre fondos que no tienen asociado un fondos diferido
             -- por montos partiremos con los rescates
             -- NOTA las reinversiones entre fondos asociados a diferidos y diferidos  quedaran para otra situacion 
             -- ya que IMTRUST no presentas casos

             INSERT INTO "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
			(	"IdSolicitudAporteRescate" ,"IdCliente" ,"IdPortafolio" ,"TipoSolicitud" ,"IdSerieFondo" ,"IdTipoMovimientoFondoMutuo" ,"IdDividendo" ,"IdSerieFondoDestino" 
				,"IdEmisorDestino" ,"IdClienteDestino" ,"IdPortafolioDestino" ,"IdSerieFondoOrigen" ,"IdEmisorOrigen" ,"MontoOrigen" ,"IndicadorMLT" ,"Overnight" 
				,"IdSolicitudAporteRescateRelacionado" ,"IdTipoCartera" ,"CalculoPorMontoCuota" ,"FechaEmision" ,"FechaAceptacion" ,"FechaCertificacion" 
				,"FechaLiquidacion" ,"FechaAnulacion" ,"FechaOriginalInversion" ,"FechaIngresoRegistro" ,"FechaAnulacionRegistro" ,"FechaAceptacionRegistro" 
				,"FechaCertificacionRegistro" ,"FechaLiquidacionRegistro" ,"Monto" ,"NumeroCuotas" ,"ValorCuota" ,"CostoUF" ,"CostoMonedaLocal" ,"ValorUFFechaInversion" 
				,"ParidadFechaInversion" ,"IdMoneda" ,"MontoComision" ,"MontoPago" ,"Estado" ,"Glosa" ,"IdEmpresa" ,"IdUsuario" ,"IdModulo" ,"EstadoLiquidacion" 
				,"MueveCta" ,"FolioCartaLiquidacion" ,"IdMedioDePago" ,"MontoInversion" ,"NumeroCuotasInversion" ,"ValorCuotaInversion" ,"CostoMonedaLocalInversion" 
				,"ParidadFechaCertificacion" ,"MontoComisionLocal" ,"MontoPagoLocal" ,"FechaModificacion" ,"FechaModificacionRegistro" ,"OrigenOperacion" 
				,"SolicitudBatch" ,"ComisionInformada"
				,"FEC_MVTO","SEC_MOVTO","SEC_ID" ,"COD_MOVTO" ,"RUT_CLI" ,"SEC_RUT_CLI" ,"NEMO" 
			    ,"IdCarga","IND_BOLSA","TIPO_COMP_ADJ"
			    
			) 
			select
			         "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate" 
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, HOM_TIPO_SOLICITUD."ValorDestino" as "TipoSolicitud"-- homologar tipo solicitud  
					, TRIM(TBFMREIN."NEMO_O") as "IdSerieFondo" 
					, CASE WHEN HOM_TIPO_SOLICITUD."ValorDestino" = 'APO' THEN 'APOREINV' ELSE 'RESREINV' END  as "IdTipoMovimientoFondoMutuo" --DIV CUOTAS,REINVERSION,ETC
					, 0  as "IdDividendo" 
					, TRIM(TBFMREIN."NEMO_D")  as "IdSerieFondoDestino" -- REVISAR 
					, '' as "IdEmisorDestino" 
					, 0 as "IdClienteDestino" -- en caso de traspasos
					, 0 as "IdPortafolioDestino" -- en casos de transferencias
					, TRIM(TBFMREIN."NEMO_O") as "IdSerieFondoOrigen" 
					, TBCTMVTO."EMISOR" as "IdEmisorOrigen" 
					, 0 as "MontoOrigen" 
					, TBFMREIN."IND_MLT" as "IndicadorMLT" 
					, '' as "Overnight" -- no se donde esta este indicador
					, 0 as "IdSolicitudAporteRescateRelacionado" -- este debe ser por los difereridos
					, 0 as "IdTipoCartera" -- no se que es esto
					, ''   as "CalculoPorMontoCuota" --no se que es esto
					, TBCTMVTO."FEC_EMI" as "FechaEmision"-- para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacion" --para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacion" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacion" 
					, '0001-01-01' as "FechaAnulacion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaOriginalInversion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaIngresoRegistro" 
					, '0001-01-01' as "FechaAnulacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacionRegistro" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacionRegistro" 
					, TBCTMVTO."MONTO" as "Monto" --esto esta en moneda de fondo 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotas" 
					, TBCTMVTO."PRECIO" as "ValorCuota" 
					, 0 as "CostoUF" --- que es esto
					, 0 as "CostoMonedaLocal" -- el problema es que no tengo la conversion en la moneda calculo la paridad a fecha de certificacion?? 
					, 0 as "ValorUFFechaInversion" 
					, 0 as "ParidadFechaInversion" 
					, HOM_TIPO_MONEDA."ValorDestino" as "IdMoneda" 
					, 0 as "MontoComision" 
					, 0 as "MontoPago" 
					, 'ING' as "Estado" 
					, 'carga masiva'  as "Glosa" 
					, p_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, p_Modulo as "IdModulo" 
					, '' as "EstadoLiquidacion" -- hay que revisar esto
					, 'N' as "MueveCta" 
					, 0 as "FolioCartaLiquidacion" 
					, 0 as "IdMedioDePago" 
					, TBCTMVTO."MONTO" as "MontoInversion" 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotasInversion" 
					, TBCTMVTO."PRECIO" as "ValorCuotaInversion" 
					, 0 as "CostoMonedaLocalInversion" 
					, 0 as "ParidadFechaCertificacion" --se puede buscar en la ruebas 
					, 0 as "MontoComisionLocal" 
					, 0 as "MontoPagoLocal" 
					, '0001-01-01' as "FechaModificacion" 
					, '0001-01-01' as "FechaModificacionRegistro" 
					, 'M' as "OrigenOperacion" --- no se lo que es 
					, 'N' as "SolicitudBatch" 
					, 'N' as "ComisionInformada"
					,TBCTMVTO."FEC_MVTO" as "FEC_MVTO"
					,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO"
					,TBCTMVTO."SEC_CAR_COMPROM" as "SEC_ID" 
					,TBCTMVTO."COD_MOVTO" as "COD_MOVTO" 
					,TBCTMVTO."RUT_CLI" as "RUT_CLI" 
					,TBCTMVTO."SEC_RUT_CLI" as "SEC_RUT_CLI" 
					,TBCTMVTO."NEMO" as "NEMO"
	    			,p_IdCarga
	    			,TBCTMVTO."IND_BOLSA" as "IND_BOLSA"
	    			,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
	    			FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
							INNER JOIN "BSCL_VIRTUAL"."GES_TBFMREIN" AS TBFMREIN
								ON RIGHT('00000000000'|| TRIM(TBCTMVTO."RUT_CLI"),11)  =  TBFMREIN."RUT_CLI"
								AND TRIM(TBCTMVTO."SEC_RUT_CLI")  =  TBFMREIN."SEC_RUT_CLI" 
								AND TRIM(TBCTMVTO."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND TBCTMVTO."COD_MOVTO" = 'VIFM'
								AND TBCTMVTO."MONTO" = TBFMREIN."MONTO" 
								AND TBFMREIN."IND_MONTO" = 'M'
								AND TBCTMVTO."SEC_CAR_COMPROM" !=0
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_ORIGEN
								ON TRIM(INTRUMENTO_ORIGEN."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND INTRUMENTO_ORIGEN.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_ORIGEN."NEMO_ASOC") = ''
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_DESTINO
								ON TRIM(INTRUMENTO_DESTINO."NEMO") = TRIM(TBFMREIN."NEMO_D")
								AND INTRUMENTO_DESTINO.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_DESTINO."NEMO_ASOC") = ''
							INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
								ON CLIENTE."DocumentoIdentidad" = TRIM(TBCTMVTO.RUT_CLI)
							INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
								ON PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO.SEC_RUT_CLI||''
						  	 	AND PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							--INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO se llenan por el momento 
							--	ON INSTRUMENTO."Nemotecnico" = trim(TBCTMVTO."NEMO")                     -- solo llenan serie origen)
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_SOLICITUD
								ON HOM_TIPO_SOLICITUD."TipoValor" = 'TipoSolicitudFFMM'
								AND HOM_TIPO_SOLICITUD."ValorOrigen" = TRIM(TBCTMVTO."COD_MOVTO")
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
								ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
								AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(INTRUMENTO_ORIGEN."TIPO_FAM")
							LEFT JOIN "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS HOM_SOLICITUD
			    				ON HOM_SOLICITUD."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
			    				AND HOM_SOLICITUD."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO" 
			  				WHERE HOM_SOLICITUD."FEC_MVTO" IS NULL;

			  -- ahora necesitamos recuperar los aportes asociados a los rescates (por montos)

			  INSERT INTO "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
				(	"IdSolicitudAporteRescate" ,"IdCliente" ,"IdPortafolio" ,"TipoSolicitud" ,"IdSerieFondo" ,"IdTipoMovimientoFondoMutuo" ,"IdDividendo" ,"IdSerieFondoDestino" 
				,"IdEmisorDestino" ,"IdClienteDestino" ,"IdPortafolioDestino" ,"IdSerieFondoOrigen" ,"IdEmisorOrigen" ,"MontoOrigen" ,"IndicadorMLT" ,"Overnight" 
				,"IdSolicitudAporteRescateRelacionado" ,"IdTipoCartera" ,"CalculoPorMontoCuota" ,"FechaEmision" ,"FechaAceptacion" ,"FechaCertificacion" 
				,"FechaLiquidacion" ,"FechaAnulacion" ,"FechaOriginalInversion" ,"FechaIngresoRegistro" ,"FechaAnulacionRegistro" ,"FechaAceptacionRegistro" 
				,"FechaCertificacionRegistro" ,"FechaLiquidacionRegistro" ,"Monto" ,"NumeroCuotas" ,"ValorCuota" ,"CostoUF" ,"CostoMonedaLocal" ,"ValorUFFechaInversion" 
				,"ParidadFechaInversion" ,"IdMoneda" ,"MontoComision" ,"MontoPago" ,"Estado" ,"Glosa" ,"IdEmpresa" ,"IdUsuario" ,"IdModulo" ,"EstadoLiquidacion" 
				,"MueveCta" ,"FolioCartaLiquidacion" ,"IdMedioDePago" ,"MontoInversion" ,"NumeroCuotasInversion" ,"ValorCuotaInversion" ,"CostoMonedaLocalInversion" 
				,"ParidadFechaCertificacion" ,"MontoComisionLocal" ,"MontoPagoLocal" ,"FechaModificacion" ,"FechaModificacionRegistro" ,"OrigenOperacion" 
				,"SolicitudBatch" ,"ComisionInformada"
				,"FEC_MVTO","SEC_MOVTO","SEC_ID" ,"COD_MOVTO" ,"RUT_CLI" ,"SEC_RUT_CLI" ,"NEMO" 
				,"IdCarga","IND_BOLSA","TIPO_COMP_ADJ"
				) 	
			  select
			          "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate" 
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, HOM_TIPO_SOLICITUD."ValorDestino" as "TipoSolicitud"-- homologar tipo solicitud  
					, TRIM(TBCTMVTO."NEMO") as "IdSerieFondo" 
					, CASE WHEN HOM_TIPO_SOLICITUD."ValorDestino" = 'APO' THEN 'APOREINV' ELSE 'RESREINV' END  as "IdTipoMovimientoFondoMutuo" --DIV CUOTAS,REINVERSION,ETC
					, 0  as "IdDividendo" 
					, ''  as "IdSerieFondoDestino" -- REVISAR 
					, '' as "IdEmisorDestino" 
					, 0  as "IdClienteDestino" -- en caso de traspasos
					, 0 as "IdPortafolioDestino" -- en casos de transferencias
					, '' as "IdSerieFondoOrigen" 
					, '' as "IdEmisorOrigen" 
					, 0 as "MontoOrigen" 
					, RESCATES."IND_MLT" as "IndicadorMLT" 
					, '' as "Overnight" -- no se donde esta este indicador
					, 0 as "IdSolicitudAporteRescateRelacionado" -- este debe ser por los difereridos
					, 0 as "IdTipoCartera" -- no se que es esto
					, '' as "CalculoPorMontoCuota" --no se que es esto
					, TBCTMVTO."FEC_EMI" as "FechaEmision"-- para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacion" --para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacion" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacion" 
					, '0001-01-01' as "FechaAnulacion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaOriginalInversion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaIngresoRegistro" 
					, '0001-01-01' as "FechaAnulacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacionRegistro" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacionRegistro" 
					, TBCTMVTO."MONTO" as "Monto" --esto esta en moneda de fondo 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotas" 
					, TBCTMVTO."PRECIO" as "ValorCuota" 
					, 0 as "CostoUF" --- que es esto
					, 0 as "CostoMonedaLocal" -- el problema es que no tengo la conversion en la moneda calculo la paridad a fecha de certificacion?? 
					, 0 as "ValorUFFechaInversion" 
					, 0 as "ParidadFechaInversion" 
					, HOM_TIPO_MONEDA."ValorDestino" as "IdMoneda" 
					, 0 as "MontoComision" 
					, 0 as "MontoPago" 
					, 'ING' as "Estado" 
					, '' as "Glosa" 
					, P_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, p_Modulo as "IdModulo" 
					, '' as "EstadoLiquidacion" -- hay que revisar esto
					, 'S' as "MueveCta" 
					, 0 as "FolioCartaLiquidacion" 
					, 0 as "IdMedioDePago" 
					, TBCTMVTO."MONTO" as "MontoInversion" 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotasInversion" 
					, TBCTMVTO."PRECIO" as "ValorCuotaInversion" 
					, 0 as "CostoMonedaLocalInversion" 
					, 0 as "ParidadFechaCertificacion" --se puede buscar en la ruebas 
					, 0 as "MontoComisionLocal" 
					, 0 as "MontoPagoLocal" 
					, '0001-01-01' as "FechaModificacion" 
					, '0001-01-01' as "FechaModificacionRegistro" 
					, 'M' as "OrigenOperacion" --- no se lo que es 
					, 'N' as "SolicitudBatch" 
					, 'N' as "ComisionInformada"
					,TBCTMVTO."FEC_MVTO" as "FEC_MVTO"
					,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO"
					,TBCTMVTO."SEC_CAR_COMPROM" as "SEC_ID" 
					,TBCTMVTO."COD_MOVTO" as "COD_MOVTO" 
					,TBCTMVTO."RUT_CLI" as "RUT_CLI" 
					,TBCTMVTO."SEC_RUT_CLI" as "SEC_RUT_CLI" 
					,TBCTMVTO."NEMO" as "NEMO"
              		,p_IdCarga
              		,TBCTMVTO."IND_BOLSA" as "IND_BOLSA"
              		,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
              		FROM -- por cuotas
					  (SELECT TBCTMVTO."SEC_CAR_COMPROM"
					          ,TBCTMVTO."RUT_CLI"
					          ,TBCTMVTO."SEC_RUT_CLI"
					          ,TBFMREIN."NEMO_D"
					          ,TBFMREIN."CANTIDAD"
					          ,TBFMREIN."IND_MLT"
					          ,TBCTMVTO."SEC_MOVTO"
					          ,TBCTMVTO."FEC_MVTO"
					          ,TBFMREIN."NEMO_O"
					          ,TBFMREIN."SERIE_O"
					          ,TBFMREIN."SERIE_D"
					          ,TBFMREIN."IND_MONTO"
					   FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
							INNER JOIN "BSCL_VIRTUAL"."GES_TBFMREIN" AS TBFMREIN
								ON RIGHT('00000000000'|| TRIM(TBCTMVTO."RUT_CLI"),11)  =  TBFMREIN."RUT_CLI"
								AND TRIM(TBCTMVTO."SEC_RUT_CLI")  =  TBFMREIN."SEC_RUT_CLI" 
								AND TRIM(TBCTMVTO."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND TBCTMVTO."COD_MOVTO" = 'VIFM'
								AND TBCTMVTO."MONTO" = TBFMREIN."MONTO" 
								AND TBFMREIN."IND_MONTO" = 'M'
								AND TBCTMVTO."SEC_CAR_COMPROM" !=0
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_ORIGEN
								ON TRIM(INTRUMENTO_ORIGEN."NEMO") = TRIM(TBFMREIN."NEMO_O")
								AND INTRUMENTO_ORIGEN.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_ORIGEN."NEMO_ASOC") = ''
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS INTRUMENTO_DESTINO
								ON TRIM(INTRUMENTO_DESTINO."NEMO") = TRIM(TBFMREIN."NEMO_D")
								AND INTRUMENTO_DESTINO.IND_DIFERIDO ='N'
								AND TRIM(INTRUMENTO_DESTINO."NEMO_ASOC") = ''
								) AS RESCATES
							INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
								ON  TBCTMVTO."RUT_CLI" = RESCATES."RUT_CLI"
								AND TBCTMVTO."SEC_RUT_CLI" = RESCATES."SEC_RUT_CLI"
								AND TRIM(TBCTMVTO."NEMO") = TRIM(RESCATES."NEMO_D")
								AND TBCTMVTO."COD_MOVTO" = 'CIFM'
								AND TBCTMVTO."SEC_CAR_COMPROM" =  RESCATES."SEC_CAR_COMPROM"
							INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS TBPLMAIN
								ON TBPLMAIN."NEMO" = TBCTMVTO."NEMO"
								AND TBPLMAIN."MERCADO" ='FM'
							INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
								ON CLIENTE."DocumentoIdentidad" = TRIM(TBCTMVTO.RUT_CLI)
							INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
								ON PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO.SEC_RUT_CLI||''
						 	   AND PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							--INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO --los aportes por reinversion
							--	ON INSTRUMENTO."Nemotecnico" = TBCTMVTO."NEMO"                     -- solo llenan serie origen
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_SOLICITUD
								ON HOM_TIPO_SOLICITUD."TipoValor" = 'TipoSolicitudFFMM'
								AND HOM_TIPO_SOLICITUD."ValorOrigen" = TBCTMVTO."COD_MOVTO"
							INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
								ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
								AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM")
							LEFT JOIN  "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS HOM_SOLICITUD
								ON  HOM_SOLICITUD."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
								AND HOM_SOLICITUD."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
							WHERE HOM_SOLICITUD."FEC_MVTO" IS NULL;

            -- insertamos los traspaso
  			-- los traspasos se conocen  a traves de fechamvto, nemo, rut, monto, codigo_comp_adj ='tt' y codigo de movimiento
            -- no obstante si existe varios movimiento al mismo rut y del mismo monto no se puede cuadrar adecuadamente
            
			 INSERT INTO "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
				(	"IdSolicitudAporteRescate" ,"IdCliente" ,"IdPortafolio" ,"TipoSolicitud" ,"IdSerieFondo" ,"IdTipoMovimientoFondoMutuo" ,"IdDividendo" ,"IdSerieFondoDestino" 
				,"IdEmisorDestino" ,"IdClienteDestino" ,"IdPortafolioDestino" ,"IdSerieFondoOrigen" ,"IdEmisorOrigen" ,"MontoOrigen" ,"IndicadorMLT" ,"Overnight" 
				,"IdSolicitudAporteRescateRelacionado" ,"IdTipoCartera" ,"CalculoPorMontoCuota" ,"FechaEmision" ,"FechaAceptacion" ,"FechaCertificacion" 
				,"FechaLiquidacion" ,"FechaAnulacion" ,"FechaOriginalInversion" ,"FechaIngresoRegistro" ,"FechaAnulacionRegistro" ,"FechaAceptacionRegistro" 
				,"FechaCertificacionRegistro" ,"FechaLiquidacionRegistro" ,"Monto" ,"NumeroCuotas" ,"ValorCuota" ,"CostoUF" ,"CostoMonedaLocal" ,"ValorUFFechaInversion" 
				,"ParidadFechaInversion" ,"IdMoneda" ,"MontoComision" ,"MontoPago" ,"Estado" ,"Glosa" ,"IdEmpresa" ,"IdUsuario" ,"IdModulo" ,"EstadoLiquidacion" 
				,"MueveCta" ,"FolioCartaLiquidacion" ,"IdMedioDePago" ,"MontoInversion" ,"NumeroCuotasInversion" ,"ValorCuotaInversion" ,"CostoMonedaLocalInversion" 
				,"ParidadFechaCertificacion" ,"MontoComisionLocal" ,"MontoPagoLocal" ,"FechaModificacion" ,"FechaModificacionRegistro" ,"OrigenOperacion" 
				,"SolicitudBatch" ,"ComisionInformada"
				,"FEC_MVTO","SEC_MOVTO","SEC_ID" ,"COD_MOVTO" ,"RUT_CLI" ,"SEC_RUT_CLI" ,"NEMO" 
				,"IdCarga","IND_BOLSA","TIPO_COMP_ADJ"
				) 	
			  select
			          "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate" 
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, HOM_TIPO_SOLICITUD."ValorDestino" as "TipoSolicitud"-- homologar tipo solicitud  
					, TRIM(TBCTMVTO."NEMO") as "IdSerieFondo" 
					, HOM_MOV_TRANFERENCIA."ValorDestino"  as "IdTipoMovimientoFondoMutuo" --DIV CUOTAS,REINVERSION,ETC
					, 0  as "IdDividendo" 
					, '' as "IdSerieFondoDestino" -- REVISAR 
					, '' as "IdEmisorDestino" 
					, 0  as "IdClienteDestino" -- en caso de traspasos
					, 0  as "IdPortafolioDestino" -- en casos de transferencias
					, '' as "IdSerieFondoOrigen" 
					, '' as "IdEmisorOrigen" 
					, 0  as "MontoOrigen" 
					, 'N' as "IndicadorMLT" 
					, '' as "Overnight" -- no se donde esta este indicador
					, 0 as "IdSolicitudAporteRescateRelacionado" -- este debe ser por los difereridos
					, 0 as "IdTipoCartera" -- no se que es esto
					, '' as "CalculoPorMontoCuota" --no se que es esto
					, TBCTMVTO."FEC_EMI" as "FechaEmision"-- para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacion" --para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacion" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacion" 
					, '0001-01-01' as "FechaAnulacion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaOriginalInversion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaIngresoRegistro" 
					, '0001-01-01' as "FechaAnulacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacionRegistro" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacionRegistro" 
					, TBCTMVTO."MONTO" as "Monto" --esto esta en moneda de fondo 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotas" 
					, TBCTMVTO."PRECIO" as "ValorCuota" 
					, 0 as "CostoUF" --- que es esto
					, 0 as "CostoMonedaLocal" -- el problema es que no tengo la conversion en la moneda calculo la paridad a fecha de certificacion?? 
					, 0 as "ValorUFFechaInversion" 
					, 0 as "ParidadFechaInversion" 
					, HOM_TIPO_MONEDA."ValorDestino" as "IdMoneda" 
					, 0 as "MontoComision" 
					, 0 as "MontoPago" 
					, 'ING' as "Estado" 
					, '' as "Glosa" 
					, P_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, p_Modulo as "IdModulo" 
					, '' as "EstadoLiquidacion" -- hay que revisar esto
					, 'N' as "MueveCta" 
					, 0 as "FolioCartaLiquidacion" 
					, 0 as "IdMedioDePago" 
					, TBCTMVTO."MONTO" as "MontoInversion" 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotasInversion" 
					, TBCTMVTO."PRECIO" as "ValorCuotaInversion" 
					, 0 as "CostoMonedaLocalInversion" 
					, 0 as "ParidadFechaCertificacion" --se puede buscar en la ruebas 
					, 0 as "MontoComisionLocal" 
					, 0 as "MontoPagoLocal" 
					, '0001-01-01' as "FechaModificacion" 
					, '0001-01-01' as "FechaModificacionRegistro" 
					, 'M' as "OrigenOperacion" --- no se lo que es 
					, 'N' as "SolicitudBatch" 
					, 'N' as "ComisionInformada"
					,TBCTMVTO."FEC_MVTO" as "FEC_MVTO"
					,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO"
					,TBCTMVTO."SEC_CAR_COMPROM" as "SEC_ID" 
					,TBCTMVTO."COD_MOVTO" as "COD_MOVTO" 
					,TBCTMVTO."RUT_CLI" as "RUT_CLI" 
					,TBCTMVTO."SEC_RUT_CLI" as "SEC_RUT_CLI" 
					,TBCTMVTO."NEMO" as "NEMO"
					,p_IdCarga
					,TBCTMVTO."IND_BOLSA" as "IND_BOLSA"
					,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
              		FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
						INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS TBPLMAIN
							ON TRIM(TBPLMAIN."NEMO") = TRIM(TBCTMVTO."NEMO")
							AND TBPLMAIN."IND_DIFERIDO" ='N'
							AND TRIM(TBPLMAIN."NEMO_ASOC") = ''
							AND TBPLMAIN."MERCADO" ='FM'
						INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
							ON CLIENTE."DocumentoIdentidad" = TRIM(TBCTMVTO.RUT_CLI)
						INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
							ON PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO.SEC_RUT_CLI||''
					   	   AND PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
						INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_MOV_TRANFERENCIA
							ON HOM_MOV_TRANFERENCIA."TipoValor" = 'TipoTraspasoFFMM'
						   AND HOM_MOV_TRANFERENCIA."ValorOrigen" = TRIM(TBCTMVTO."COD_MOVTO")
						INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
							ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
					   	   AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM")
					   	INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_SOLICITUD
							ON HOM_TIPO_SOLICITUD."TipoValor" = 'TipoSolicitudFFMM'
						   AND HOM_TIPO_SOLICITUD."ValorOrigen" = TBCTMVTO."COD_MOVTO"
						--INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO --los aportes por reinversion
						-- ON INSTRUMENTO."Nemotecnico" = TBCTMVTO."NEMO"                     -- solo llenan serie origen
						LEFT JOIN  "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS HOM_SOLICITUD
							ON HOM_SOLICITUD."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
					       AND HOM_SOLICITUD."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
						WHERE HOM_SOLICITUD."FEC_MVTO" IS NULL
						  AND TBCTMVTO."COD_MOVTO" IN ('IRFM','RIFM')
	                      AND TBCTMVTO."TIPO_COMP_ADJ" ='TT';
	                           
            -- debemos cargar los aportes y rescates por derivados 
            -- primero debemos determinar los movimientos que tienen posibilidad de ser derivados 
            -- y luego buscar si estan asociado a algun movimiento de cargo si es asi es proveniente de un derivado
            -- podria dar ese el caso que este mal asociado o que el movimiento de cargo no exista.
			
			INSERT INTO "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate"
				(	"IdSolicitudAporteRescate" ,"IdCliente" ,"IdPortafolio" ,"TipoSolicitud" ,"IdSerieFondo" ,"IdTipoMovimientoFondoMutuo" ,"IdDividendo" ,"IdSerieFondoDestino" 
				,"IdEmisorDestino" ,"IdClienteDestino" ,"IdPortafolioDestino" ,"IdSerieFondoOrigen" ,"IdEmisorOrigen" ,"MontoOrigen" ,"IndicadorMLT" ,"Overnight" 
				,"IdSolicitudAporteRescateRelacionado" ,"IdTipoCartera" ,"CalculoPorMontoCuota" ,"FechaEmision" ,"FechaAceptacion" ,"FechaCertificacion" 
				,"FechaLiquidacion" ,"FechaAnulacion" ,"FechaOriginalInversion" ,"FechaIngresoRegistro" ,"FechaAnulacionRegistro" ,"FechaAceptacionRegistro" 
				,"FechaCertificacionRegistro" ,"FechaLiquidacionRegistro" ,"Monto" ,"NumeroCuotas" ,"ValorCuota" ,"CostoUF" ,"CostoMonedaLocal" ,"ValorUFFechaInversion" 
				,"ParidadFechaInversion" ,"IdMoneda" ,"MontoComision" ,"MontoPago" ,"Estado" ,"Glosa" ,"IdEmpresa" ,"IdUsuario" ,"IdModulo" ,"EstadoLiquidacion" 
				,"MueveCta" ,"FolioCartaLiquidacion" ,"IdMedioDePago" ,"MontoInversion" ,"NumeroCuotasInversion" ,"ValorCuotaInversion" ,"CostoMonedaLocalInversion" 
				,"ParidadFechaCertificacion" ,"MontoComisionLocal" ,"MontoPagoLocal" ,"FechaModificacion" ,"FechaModificacionRegistro" ,"OrigenOperacion" 
				,"SolicitudBatch" ,"ComisionInformada"
				,"FEC_MVTO","SEC_MOVTO","SEC_ID" ,"COD_MOVTO" ,"RUT_CLI" ,"SEC_RUT_CLI" ,"NEMO" 
				,"IdCarga","IND_BOLSA","TIPO_COMP_ADJ"
				) 	
			  select
			          "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate" 
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, HOM_TIPO_SOLICITUD."ValorDestino" as "TipoSolicitud"-- homologar tipo solicitud  
					, TRIM(TBCTMVTO."NEMO") as "IdSerieFondo" 
					, CASE WHEN HOM_TIPO_SOLICITUD."ValorDestino" = 'APO' THEN 'APONORM' ELSE 'RESNORM' END as "IdTipoMovimientoFondoMutuo" --DIV CUOTAS,REINVERSION,ETC
					, 0  as "IdDividendo" 
					, '' as "IdSerieFondoDestino" -- REVISAR 
					, '' as "IdEmisorDestino" 
					, 0  as "IdClienteDestino" -- en caso de traspasos
					, 0  as "IdPortafolioDestino" -- en casos de transferencias
					, '' as "IdSerieFondoOrigen" 
					, '' as "IdEmisorOrigen" 
					, 0  as "MontoOrigen" 
					, 'N' as "IndicadorMLT" 
					, '' as "Overnight" -- no se donde esta este indicador
					, 0 as "IdSolicitudAporteRescateRelacionado" -- este debe ser por los difereridos
					, 0 as "IdTipoCartera" -- no se que es esto
					, '' as "CalculoPorMontoCuota" --no se que es esto
					, TBCTMVTO."FEC_EMI" as "FechaEmision"-- para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacion" --para que sirve esto aqui 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacion" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacion" 
					, '0001-01-01' as "FechaAnulacion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaOriginalInversion" 
					, TBCTMVTO."FEC_ING_MVTO" as "FechaIngresoRegistro" 
					, '0001-01-01' as "FechaAnulacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaAceptacionRegistro" 
					, TBCTMVTO."FEC_MVTO" as "FechaCertificacionRegistro" 
					, TBCTMVTO."FEC_LIQUID" as "FechaLiquidacionRegistro" 
					, TBCTMVTO."MONTO" as "Monto" --esto esta en moneda de fondo 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotas" 
					, TBCTMVTO."PRECIO" as "ValorCuota" 
					, 0 as "CostoUF" --- que es esto
					, 0 as "CostoMonedaLocal" -- el problema es que no tengo la conversion en la moneda calculo la paridad a fecha de certificacion?? 
					, 0 as "ValorUFFechaInversion" 
					, 0 as "ParidadFechaInversion" 
					, HOM_TIPO_MONEDA_FONDO_DESTINO."ValorDestino" as "IdMoneda" 
					, 0 as "MontoComision" 
					, 0 as "MontoPago" 
					, 'ING' as "Estado" 
					, '' as "Glosa" 
					, P_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, p_Modulo as "IdModulo" 
					, '' as "EstadoLiquidacion" -- hay que revisar esto
					, 'N' as "MueveCta" 
					, 0 as "FolioCartaLiquidacion" 
					, 0 as "IdMedioDePago" 
					, TBCTMVTO."MONTO" as "MontoInversion" 
					, TBCTMVTO."CANTIDAD" as "NumeroCuotasInversion" 
					, TBCTMVTO."PRECIO" as "ValorCuotaInversion" 
					, 0 as "CostoMonedaLocalInversion" 
					, 0 as "ParidadFechaCertificacion" --se puede buscar en la ruebas 
					, 0 as "MontoComisionLocal" 
					, 0 as "MontoPagoLocal" 
					, '0001-01-01' as "FechaModificacion" 
					, '0001-01-01' as "FechaModificacionRegistro" 
					, 'M' as "OrigenOperacion" --- no se lo que es 
					, 'N' as "SolicitudBatch" 
					, 'N' as "ComisionInformada"
					,TBCTMVTO."FEC_MVTO" as "FEC_MVTO"
					,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO"
					,TBCTMVTO."SEC_CAR_COMPROM" as "SEC_ID" 
					,TBCTMVTO."COD_MOVTO" as "COD_MOVTO" 
					,TBCTMVTO."RUT_CLI" as "RUT_CLI" 
					,TBCTMVTO."SEC_RUT_CLI" as "SEC_RUT_CLI" 
					,TBCTMVTO."NEMO" as "NEMO"
					,p_IdCarga
					,TBCTMVTO."IND_BOLSA" as "IND_BOLSA" 
					,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
					FROM  "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO
								INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS TBPLMAIN_1
								    ON TBPLMAIN_1."NEMO" = TBCTMVTO."NEMO"
								    AND TBPLMAIN_1."MERCADO" = 'FM'
								INNER JOIN "BSCL_VIRTUAL"."GES_TBPLMAIN" AS TBPLMAIN_2
									ON TBPLMAIN_2."NEMO" = TBPLMAIN_1."NEMO_ASOC"
								    AND TBPLMAIN_2."MERCADO" = 'FM'
								    AND TBPLMAIN_2."IND_DIFERIDO" ='S'
								INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" AS TBCTMVTO_2
								    ON TBCTMVTO_2."SEC_CAR_COMPROM" = TBCTMVTO."SEC_CAR_COMPROM"
								    AND TBCTMVTO_2."COD_MOVTO"  IN ('ABDO','ABMN','ABEU')
								INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA_FONDO_DESTINO
									ON HOM_TIPO_MONEDA_FONDO_DESTINO."TipoValor" = 'TipoMonedaInstrumentoFFMM'
							   	   AND HOM_TIPO_MONEDA_FONDO_DESTINO."ValorOrigen" = TRIM(TBPLMAIN_1."TIPO_FAM")
							   	INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA_FONDO_ORIGEN
									ON HOM_TIPO_MONEDA_FONDO_ORIGEN."TipoValor" = 'TipoMonedaInstrumentoFFMM'
							   	   AND HOM_TIPO_MONEDA_FONDO_ORIGEN."ValorOrigen" = TRIM(TBPLMAIN_2."TIPO_FAM")
					   			INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_SOLICITUD
									ON HOM_TIPO_SOLICITUD."TipoValor" = 'TipoSolicitudFFMM'
								   AND HOM_TIPO_SOLICITUD."ValorOrigen" = TBCTMVTO."COD_MOVTO"
								INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
									ON CLIENTE."DocumentoIdentidad" = TRIM(TBCTMVTO.RUT_CLI)
								INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO
									ON PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO.SEC_RUT_CLI||''
					   	  			AND PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
					   	  		LEFT JOIN  "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS HOM_SOLICITUD
									ON HOM_SOLICITUD."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
					       			AND HOM_SOLICITUD."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
			WHERE TBCTMVTO."COD_MOVTO" IN ('CIFM','VIFM');                
                
	        -- ahora relizaremos la actualizacion de los campos para IdTipoMovimientoFondoMutuo
            -- para actualizar los aportes por dividendos

            update A1 SET A1."IdTipoMovimientoFondoMutuo" = 'APODIV'
            from "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" AS A1     
            where "IND_BOLSA" ='PDV';
            
            -- ahora debo relacionar las reinversiones TODO rescate debe tener un aporte a un fondo de destino
            -- primero haremos la actualizacion desde rescate y lo asociaremos con su correspondiente aporte
            
            update A1 SET A1."IdSolicitudAporteRescateRelacionado" = A2."IdSolicitudAporteRescate"
            from "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A1
            , "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A2      
            where A1."COD_MOVTO" ='VIFM'
             and  A2."COD_MOVTO" ='CIFM'
             and  A1."RUT_CLI" = A2."RUT_CLI"
             and  A1."SEC_RUT_CLI" = A2."SEC_RUT_CLI"
             and  A1."SEC_ID" = A2."SEC_ID"
             and  A1."SEC_ID" > 0;
           
           
           -- ahora debo relacionar las reinversiones TODO aporte debe tener un rescate a un fondo de destino
            -- primero haremos la actualizacion desde aporte y lo asociaremos con su correspondiente rescate
            
            update A1 SET A1."IdSolicitudAporteRescateRelacionado" = A2."IdSolicitudAporteRescate"
            from "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A1
            , "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A2      
            where A1."COD_MOVTO" ='CIFM'
             and  A2."COD_MOVTO" ='VIFM'
             and  A1."RUT_CLI" = A2."RUT_CLI"
             and  A1."SEC_RUT_CLI" = A2."SEC_RUT_CLI"
             and  A1."SEC_ID" = A2."SEC_ID"
             and  A1."SEC_ID" > 0; 
            
            -- actualizamos las relaciones los traspasos de cuentas 
            -- primero los rescates los asociamos a los aportes

            update A1 SET A1."IdSolicitudAporteRescateRelacionado" = A2."IdSolicitudAporteRescate"
            from "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A1
            , "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A2      
            where A1."COD_MOVTO" ='RIFM'
             and  A2."COD_MOVTO" ='IRFM'
             and  A2."TIPO_COMP_ADJ" = 'TT'
             and  A1."TIPO_COMP_ADJ" = 'TT'
             and  A1."RUT_CLI"  = A2."RUT_CLI"
             and  A1."FEC_MVTO" = A2."FEC_MVTO"
             and  A1."NEMO" = A2."NEMO"
             and  A1."Monto" = A2."Monto";

            -- actualizamos las relaciones los traspasos de cuentas 
            -- los aportes los asociamos a los rescates
            -- pude darse el caso que haya errores ya que puede existir una operacion con los mismos parametros
            -- y los mismos montos debido a eso se busca el maximo para evitar que haya dos registros que apunten al mismo

			update A1 SET A1."IdSolicitudAporteRescateRelacionado" = A2."IdSolicitudAporteRescate"
            from "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A1
            , "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as A2      
            where A1."COD_MOVTO" ='IRFM'
             and  A2."COD_MOVTO" ='RIFM'
             and  A2."TIPO_COMP_ADJ" = 'TT'
             and  A1."TIPO_COMP_ADJ" = 'TT'
             and  A1."RUT_CLI"  = A2."RUT_CLI"
             and  A1."FEC_MVTO" = A2."FEC_MVTO"
             and  A1."NEMO"  = A2."NEMO"
             and  A1."Monto" = A2."Monto";

            -- insertar logs
   			/*
			 -- el cliente debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'SPOT' as "Entidad"
                    ,'No existe el cliente EN OPTIMUS' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'RutCliente' as "NombreCampoGenerico6"
                    , FMMOVTOSSPOT."RutCliente" as "ValorCampoGenerico6"
                     ,'FolioOperacion' as "NombreCampoGenerico7"
                    , FMMOVTOSSPOT."FolioOperacion" as "ValorCampoGenerico7"
                    ,'FolioTransaccion' as "NombreCampoGenerico8"
                    , FMMOVTOSSPOT."FolioTransaccion" as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT"	AS FMMOVTOSSPOT
				LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(FMMOVTOSSPOT."RutCliente") ELSE TRIM(p_RutCliente) end
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.OperacionSpot" AS HOM_OPERACION_SPOT
						ON HOM_OPERACION_SPOT."FolioOperacion"    = FMMOVTOSSPOT."FolioOperacion"  
						AND HOM_OPERACION_SPOT."FolioTransaccion" = FMMOVTOSSPOT."FolioTransaccion" 
			  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal
		        AND CLIENTE."DocumentoIdentidad" IS NULL
		        AND HOM_OPERACION_SPOT."FolioOperacion" IS NULL;
	*/	    

             --REVISAMOS CUANTAS OPERACIONES SERAN CARGADAS
             /*   
               SELECT COUNT(*) INTO v_Total_Operaciones_Forward
                  FROM "BSCL_VIRTUAL"."GES_FMMOVTOSSPOT" as FMMOVTOSSPOT	
                  WHERE FMMOVTOSSPOT."FechaOperacion" >= p_FechaInicial
		        	AND FMMOVTOSSPOT."FechaOperacion" <= p_FechaFinal;
          
			-- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_Forward_Cargados FROM 
                  "BSCL.Homologacion.BD::HOM.OperacionSpot" 
                  WHERE "IdCarga" = p_IdCarga;
 
             */     
             -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_ResumenDatosProcesados =  select 'CARGASOLICITUDFMM' as "CodigoProceso" 
	   			  									,'FMM' as "Modulo" 
	   			 									,0 as "TotalRegistrosSebra"
	   			  									,0 as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

                  --IF v_Total_Operaciones_Forward <> v_Total_Forward_Cargados THEN 
                  IF 0 != 0 THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_FMM' as "Codigo",'falla en la carga spot' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
