SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ConsultarQuincenalDePactos";
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ConsultarQuincenalDePactos"
    ( 
	IN	p_Fecha 		DATE,
	IN  p_IdEmpresa     BIGINT,                                                                            
	IN  p_IdUsuario     BIGINT,
	
	--Salida   	--------------------------------------------------
	
	OUT p_out_FolioNemo 				NVARCHAR(1), -- Si es 'S' Utilizo Folio si es 'N' utilizo Nemo  		   
   	OUT p_out_quincenalDePactos 		table ( 
   												"IdQuincenalPactos"				BIGINT
	    										,"FechaProceso"					NVARCHAR(10)
	    										,"TipoCartera"					NVARCHAR(4)
												,"Emisor"						NVARCHAR(10)
												,"TipoReajuste"					NVARCHAR(4)
												,"FechaInicioCompromiso"		NVARCHAR(10)
												,"FechaVencimientoCompromiso" 	NVARCHAR(10)
												,"FechaVencimientoInstrumento"	NVARCHAR(10)
												,"MontoNominal"					DECIMAL(26,8)
												,"IdTipoOperacion"				NVARCHAR(6)
												,"Nemo"							NVARCHAR(40)
												,"IdMoneda"						NVARCHAR(3)
												,"FolioOperacion"				BIGINT
												,"Secuencia_Pareo"				BIGINT									
												,"CantidadResto" 				DECIMAL(26,8)
												,"IndicadorCobertura"			NVARCHAR(1)
												,"CantidadComprometida"			DECIMAL(26,8)
												,"IdRelacionCorredor"			NVARCHAR(1)
	    										,"Lin_Comprom"					BIGINT
	    										,"ClasificacionTipoInstrumento"	NVARCHAR(3)
												,"ValorPresente"				DECIMAL(26,8)
												,"ValorComprometido"			DECIMAL(26,8)
												,"NumeroCupones" 				BIGINT
												,"PlazoResidual"				BIGINT
												,"RutCliente"					NVARCHAR(11)
												,"SecuenciaRut"					BIGINT
												,"RutCorredor"					NVARCHAR(11)
												,"Sec_Comprom"					BIGINT
												,"TasaInstrumento"				DECIMAL(26,8)									
												,"TasaPacto" 					DECIMAL(26,8)
												,"TipoFamilia"					NVARCHAR(3)
												,"TipoInstrumento"				NVARCHAR(3)
												,"TipoReajustePacto"			NVARCHAR(4)
												,"TipoClasificacionSvsCliente"	NVARCHAR(2)
												,"TipoAmortizacion"				NVARCHAR(10)
												,"CategoriaRiesgo"				NVARCHAR(20)
												,"FolioCartera"					BIGINT
												,"CantidadSubyacente"			BIGINT
												,"ValorFinal"					DECIMAL(26,8)
												,"DecimalesPacto"				BIGINT
												,"DecimalesDetalle"				BIGINT
												,"MontoFinal"					DECIMAL(26,8)
												,"PrecioPacto"					DECIMAL(26,8)
												,"Grupo"						NVARCHAR(1)
										),
	OUT p_out_subyacenteFolio	 		table ( 
   												"FolioCarteraRelacionada"		BIGINT
	    										,"Cantidad"						BIGINT
	),
	OUT p_out_subyacenteNemo	 		table ( 
												"Nemotecnico"					NVARCHAR(10)
												,"IdEmisor"						NVARCHAR(40)
												,"FechaVencimiento"				DATE
												,"IdMoneda"						NVARCHAR(3)
	    										,"Cantidad"						BIGINT
	),		
	OUT p_out_countQuincenales			BIGINT,	
											
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
										
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
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
											DEFAULT SELECT  0  AS "IdInterno"
														   ,'' AS "Codigo"
														   ,'' AS "RequiereAutorizacion"
														   ,'' AS "ValorParametro"
														   ,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	DECLARE v_out_ErroresDetalle  	   	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40)) 
											DEFAULT SELECT 0  AS "IdInterno"
														   ,'' AS "Codigo"
														   ,'' AS "ValorParametro"
														   ,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	
	DECLARE v_count_operaciones				BIGINT;
	DECLARE v_count_consulta				BIGINT;
	DECLARE v_decimalMoneda				TABLE("Moneda" NVARCHAR(10),"Decimales" BIGINT)
											DEFAULT SELECT '' AS "Moneda"
														   ,0 AS "Decimales"
													FROM DUMMY
													WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									errores."Codigo",
									msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									adv."Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
	BEGIN
	
		DECLARE v_resultadoRegla 		NVARCHAR(40);
		DECLARE v_Diferencia			BIGINT;
   		DECLARE v_FechaSistema DATE;
   	
   		v_decimalMoneda= SELECT
   		Mon."IdMoneda" AS "Moneda", Mon."CantidadDecimales" AS "Decimales" FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" Mon  
				WHERE Mon."IdUsoMonedaFormato"='GEN' 
				AND Mon."IdTipoOperacion" = '' 
				AND Mon."IdTipoInstrumento" = ''; 
				
		v_MontoDecimales= SELECT
   		Mon."IdMoneda" AS "Moneda", Mon."MontoDecimales" AS "Decimales" FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" Mon  
				WHERE Mon."IdUsoMonedaFormato"='GEN' 
				AND Mon."IdTipoOperacion" = '' 
				AND Mon."IdTipoInstrumento" = '';
   		--****************************
		--*VALIDACION REGLAS BASICAS *
		--****************************
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,'MSP',:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT 
				"ValorParametro" INTO p_out_FolioNemo
		FROM "BSCL.Mesa.BD::MES.Parametro" 
		WHERE "NombreParametro" = 'FOLIONEMO'; -- Si es 'S' Utilizo Folio si es 'N' utilizo Nemo 
		
		v_QuincenalDePactos = 	SELECT
									"IdQuincenalPactos" 				AS "IdQuincenalPactos"
									,"FechaProceso" 					AS "FechaProceso"
									,"TipoCartera" 						AS "TipoCartera"
									,"Emisor" 							AS "Emisor"
									,"TipoReajuste" 					AS "TipoReajuste"
									,"FechaInicioCompromiso"			AS "FechaInicioCompromiso"
									,"FechaVencimientoCompromiso"		AS "FechaVencimientoCompromiso"
									,"FechaVencimientoInstrumento" 		AS "FechaVencimientoInstrumento"
									,"MontoNominal"						AS "MontoNominal"
									,"IdTipoOperacion"					AS "IdTipoOperacion"
									,"Nemo" 							AS "Nemo"
							
									,"IdMoneda"							AS "IdMoneda"
								-- FG	
								--	,'CLP'							AS "IdMoneda"									
									,"FolioOperacion" 					AS "FolioOperacion"
									,"Secuencia_Pareo" 					AS "Secuencia_Pareo"
									,"CantidadResto"					AS "CantidadResto"
									,"IndicadorCobertura" 				AS "IndicadorCobertura"
									,"CantidadComprometida"				AS "CantidadComprometida"
									,"IdRelacionCorredor" 				AS "IdRelacionCorredor"
									,"Lin_Comprom" 						AS "Lin_Comprom"
									,"ClasificacionTipoInstrumento" 	AS "ClasificacionTipoInstrumento"
									,"ValorPresente" 					AS "ValorPresente"
									,"ValorComprometido" 				AS "ValorComprometido"
									,"NumeroCupones" 					AS "NumeroCupones"
									,"PlazoResidual" 					AS "PlazoResidual"
               						 ,"RutCliente" 						AS "RutCliente"
									,"SecuenciaRut" 					AS "SecuenciaRut"
									,"RutCorredor" 						AS "RutCorredor"
									,"Sec_Comprom"						AS "Sec_Comprom"
									,"TasaInstrumento"					AS "TasaInstrumento"
									,"TasaPacto" 						AS "TasaPacto"
									,"TipoFamilia"						AS "TipoFamilia"
									,"TipoInstrumento"					AS "TipoInstrumento"
									,"TipoReajustePacto" 				AS "TipoReajustePacto"
									,"TipoClasificacionSvsCliente" 		AS "TipoClasificacionSvsCliente"
									,"TipoAmortizacion"					AS "TipoAmortizacion"
									,"CategoriaRiesgo" 					AS "CategoriaRiesgo"
									,0									AS "FolioCartera"
									,0									AS "CantidadSubyacente"
									--,"ValorFinal"						AS "ValorFinal"
								FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarQuincenalDePactos";
		
		
		
		
		--SELECT 'DatosTablaQuincenal',* FROM :v_QuincenalDePactos;
		
		/*IF (p_out_countQuincenales > 0) THEN
			
			p_out_quincenalDePactos = 	SELECT
											*
										FROM :v_QuincenalDePactos Where "FechaProceso" =:p_Fecha;
			
			p_out_subyacenteFolio	=	SELECT 
   											0 	AS 	"FolioCarteraRelacionada"
	    									,0 	AS  "Cantidad"
	    								FROM DUMMY;	

			p_out_subyacenteNemo	=	SELECT 
											'' 				AS "Nemotecnico"
											,'' 			AS "IdEmisor"
											,'1900-01-01' 	AS "FechaVencimiento"
											,'' 			AS "IdMoneda"
	    									,0 				AS "Cantidad"
										FROM DUMMY;
		ELSE */
		
			--*****************
			--INSERT QUINCENAL*			
			--*****************
    Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);  
    
    IF TO_DATE(:p_Fecha) >= TO_DATE(:v_FechaSistema) THEN

			p_out_quincenalDePactos = SELECT 
				"BSCL.Mesa.BD.Secuencias::MRF.IdQuincenalPactos".NEXTVAL 								AS "IdQuincenalPactos"
				,:p_Fecha 																				AS "FechaProceso"
				,'CA' 																					AS "TipoCartera"
				--,CASE WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 'VRC' ELSE 'CRV' END 					AS "Cod"
				,COALESCE(RE."IdEmisor", '') 															AS "Emisor"
				,COALESCE(RE."IdTipoReajuste", '') 														AS "TipoReajuste"
				,COALESCE(TO_VARCHAR(OP."FechaOperacion", 'DD-MM-YYYY'), '01-01-0001')					AS "FechaInicioCompromiso"
				,COALESCE(TO_VARCHAR(OP."FechaVencimiento", 'DD-MM-YYYY'), '01-01-0001')				AS "FechaVencimientoCompromiso"
				,COALESCE(TO_VARCHAR(CAR."FechaVencimientoInstrumento", 'DD-MM-YYYY'), '01-01-0001') 	AS "FechaVencimientoInstrumento"
				--,COALESCE(TO_VARCHAR(CAR."FechaLiquidacion", 'DD-MM-YYYY'), '01-01-0001') 				AS "FechaLiquidacionCompromiso"
				,CASE WHEN RE."IdClasificacionTipoInstrumento" = 'IF' THEN RE."IngValorRescate"
				 ELSE RE."IngCantidad" 
				 END   																				    AS "MontoNominal"
				,COALESCE(OP."IdTipoOperacion", '') 													AS "IdTipoOperacion"
				,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
				THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END 											AS "Nemo"
				--,CAR."Nemotecnico"																		AS "Nemo"
				,COALESCE(RE."IdMonedaEmisionInstrumento", '')											AS "IdMoneda"
				,COALESCE(OP."FolioOperacion", 0) 															AS "FolioOperacion"
				,0 																						AS "Secuencia_Pareo"
				,0.0																	 				AS "CantidadResto"
				,'N' 																					AS "IndicadorCobertura"
				,CASE WHEN RE."IdClasificacionTipoInstrumento" = 'IF' THEN RE."IngValorRescate"
				 ELSE RE."IngCantidad" 
				 END 																				    AS "CantidadComprometida"
				,COALESCE(	IND."ValorIndicador", '') 													AS "IdRelacionCorredor"
				,0 																						AS "Lin_Comprom"
				,COALESCE(RE."IdClasificacionTipoInstrumento", '') 										AS "ClasificacionTipoInstrumento"
				,COALESCE(CAR."INGMontoMercadoActualMonedaInstrumento", 0.0) 							AS "ValorPresente"
				,COALESCE(RE."MontoValorizado", 0.0)													AS "ValorComprometido"
				,COALESCE(CI."NumeroCupones", 0) 														AS "NumeroCupones"
				,COALESCE(DAYS_BETWEEN (TO_SECONDDATE(:p_Fecha, 'YYYY-MM-DD'), 
					TO_SECONDDATE(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD')), 0) 					AS "PlazoResidual"
                ,CASE WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 
					COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdClienteRelacionado"), '') 
                   	ELSE 
                   	COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdCliente"), '') END 										AS "RutCliente"
				,0 																						AS "SecuenciaRut"
				,CASE WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 
					COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdCliente"), '') 
                   	ELSE 
                   	COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdClienteRelacionado"), '') END							AS "RutCorredor"
				,COALESCE(CAR."FolioComprobante", 0)													AS "Sec_Comprom"
				,COALESCE(RE."Tasa", 0.0)																AS "TasaInstrumento"
				,OP."Tasa"																				AS "TasaPacto"
				,COALESCE(RE."IdTipoInstrumento",'') 													AS "TipoFamilia"
				,COALESCE(RE."IdTipoInstrumento", '')													AS "TipoInstrumento"
				,COALESCE(OP."IdTipoReajuste", '') 														AS "TipoReajustePacto"
				,COALESCE(	(SELECT 
						"Tipo" 
					FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
					WHERE "IdCliente" = CAR."IdClienteRelacionado"), '') 								AS "TipoClasificacionSvsCliente"
				,COALESCE(	(SELECT 
						CASE WHEN "TieneCupon" = 'S' THEN 1 ELSE 2 END
					FROM "BSCL.Plataforma.BD::INS.Instrumento" 
					WHERE "IdInstrumento" = CAR."IdInstrumento"), 2)									AS "TipoAmortizacion"
				,COALESCE(CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' 
				THEN TRIM(Emi."ValorClasificacionRiesgoIIF") ELSE ICR."ValorClasificacionRiesgo" END,'')		AS "CategoriaRiesgo"
				,COALESCE(RE."FolioCarteraCustodia", 0)													AS "FolioCartera"
				,CASE WHEN RE."IdClasificacionTipoInstrumento" = 'IF' THEN RE."IngValorRescate"
				 ELSE RE."IngCantidad" 
				 END 																					AS "CantidadSubyacente" --No van en la tabla Quincenal
				,OP."MontoOperacionFinalMoneda"													        AS "ValorFinal"
				,(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = OP."IdTipoReajuste")													AS "DecimalesPacto"
				, (SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = RE."IdTipoReajuste")													AS "DecimalesDetalle"
				,OP."MontoOperacionFinal"																AS "MontoFinal"	
				,ROUND(RE."INGMontoAPlazo", (SELECT "Decimales" 
				FROM :v_MontoDecimales WHERE "Moneda" = RE."IdMonedaEmisionInstrumento")) 				AS "PrecioPacto"
				,CASE
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 90 THEN 'A'
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 360 THEN 'B'
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 5*360 THEN 'C'
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 12*360 THEN 'D'
  					ELSE 'E' END AS "Grupo"
				FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
				INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
				INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  CAR ON (OP."IdOperacion" = RE."IdOperacion" AND RE."IdCarteraCustodia" = CAR."IdCartera")
 				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND ON "CodigoTipoIndicador"  = 'RELCORR' AND IND."IdCliente" = CAR."IdCliente"
				WHERE OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND OP."IdEstado" = 'VIG' AND CAR."IdTipoInstrumento" != 'PAC'  AND 
					((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
					(TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
					TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD')
				ORDER BY OP."FolioOperacion";
	ELSE
			p_out_quincenalDePactos = SELECT 
				"BSCL.Mesa.BD.Secuencias::MRF.IdQuincenalPactos".NEXTVAL 								AS "IdQuincenalPactos"
				,:p_Fecha 																				AS "FechaProceso"
				,'CA' 																					AS "TipoCartera"
				--,CASE WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 'VRC' ELSE 'CRV' END 					AS "Cod"
				,COALESCE(RE."IdEmisor", '') 															AS "Emisor"
				,COALESCE(RE."IdTipoReajuste", '') 														AS "TipoReajuste"
				,COALESCE(TO_VARCHAR(OP."FechaOperacion", 'DD-MM-YYYY'), '01-01-0001')					AS "FechaInicioCompromiso"
				,COALESCE(TO_VARCHAR(OP."FechaVencimiento", 'DD-MM-YYYY'), '01-01-0001')				AS "FechaVencimientoCompromiso"
				,COALESCE(TO_VARCHAR(CAR."FechaVencimientoInstrumento", 'DD-MM-YYYY'), '01-01-0001') 	AS "FechaVencimientoInstrumento"
				--,COALESCE(TO_VARCHAR(CAR."FechaLiquidacion", 'DD-MM-YYYY'), '01-01-0001') 				AS "FechaLiquidacionCompromiso"
				,CASE WHEN RE."IdClasificacionTipoInstrumento" = 'IF' THEN RE."IngValorRescate"
				 ELSE RE."IngCantidad" 
				 END   																				    AS "MontoNominal"
				,COALESCE(OP."IdTipoOperacion", '') 													AS "IdTipoOperacion"
				,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
				THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END 											AS "Nemo"
				--,CAR."Nemotecnico"																		AS "Nemo"
				,COALESCE(RE."IdMonedaEmisionInstrumento", '')											AS "IdMoneda"
				,COALESCE(OP."FolioOperacion", 0) 															AS "FolioOperacion"
				,0 																						AS "Secuencia_Pareo"
				,0.0																	 				AS "CantidadResto"
				,'N' 																					AS "IndicadorCobertura"
				,CASE WHEN RE."IdClasificacionTipoInstrumento" = 'IF' THEN RE."IngValorRescate"
				 ELSE RE."IngCantidad" 
				 END 																				    AS "CantidadComprometida"
				,COALESCE(	IND."ValorIndicador", '') 													AS "IdRelacionCorredor"
				,0 																						AS "Lin_Comprom"
				,COALESCE(RE."IdClasificacionTipoInstrumento", '') 										AS "ClasificacionTipoInstrumento"
				,COALESCE(CAR."INGMontoMercadoActualMonedaInstrumento", 0.0) 							AS "ValorPresente"
				,COALESCE(RE."MontoValorizado", 0.0)													AS "ValorComprometido"
				,COALESCE(CI."NumeroCupones", 0) 														AS "NumeroCupones"
				,COALESCE(DAYS_BETWEEN (TO_SECONDDATE(:p_Fecha, 'YYYY-MM-DD'), 
					TO_SECONDDATE(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD')), 0) 					AS "PlazoResidual"
                ,CASE WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 
					COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdClienteRelacionado"), '') 
                   	ELSE 
                   	COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdCliente"), '') END 										AS "RutCliente"
				,0 																						AS "SecuenciaRut"
				,CASE WHEN OP."IdTipoOperacion" = 'COMPAC' THEN 
					COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdCliente"), '') 
                   	ELSE 
                   	COALESCE((SELECT "DocumentoIdentidad" 
                   	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                   	WHERE "IdCliente" = CAR."IdClienteRelacionado"), '') END							AS "RutCorredor"
				,COALESCE(CAR."FolioComprobante", 0)													AS "Sec_Comprom"
				,COALESCE(RE."Tasa", 0.0)																AS "TasaInstrumento"
				,OP."Tasa"																				AS "TasaPacto"
				,COALESCE(RE."IdTipoInstrumento",'') 													AS "TipoFamilia"
				,COALESCE(RE."IdTipoInstrumento", '')													AS "TipoInstrumento"
				,COALESCE(OP."IdTipoReajuste", '') 														AS "TipoReajustePacto"
				,COALESCE(	(SELECT 
						"Tipo" 
					FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
					WHERE "IdCliente" = CAR."IdClienteRelacionado"), '') 								AS "TipoClasificacionSvsCliente"
				,COALESCE(	(SELECT 
						CASE WHEN "TieneCupon" = 'S' THEN 1 ELSE 2 END
					FROM "BSCL.Plataforma.BD::INS.Instrumento" 
					WHERE "IdInstrumento" = CAR."IdInstrumento"), 2)									AS "TipoAmortizacion"
				,COALESCE(CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' 
				THEN TRIM(Emi."ValorClasificacionRiesgoIIF") ELSE ICR."ValorClasificacionRiesgo" END,'')		AS "CategoriaRiesgo"
				,COALESCE(RE."FolioCarteraCustodia", 0)													AS "FolioCartera"
				,CASE WHEN RE."IdClasificacionTipoInstrumento" = 'IF' THEN RE."IngValorRescate"
				 ELSE RE."IngCantidad" 
				 END 																					AS "CantidadSubyacente" --No van en la tabla Quincenal
				,OP."MontoOperacionFinalMoneda"													        AS "ValorFinal"
				,(SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = OP."IdTipoReajuste")													AS "DecimalesPacto"
				, (SELECT "Decimales" FROM :v_decimalMoneda
				WHERE "Moneda" = RE."IdTipoReajuste")													AS "DecimalesDetalle"
				,OP."MontoOperacionFinal"																AS "MontoFinal"	
				,ROUND(RE."INGMontoAPlazo", (SELECT "Decimales" 
				FROM :v_MontoDecimales WHERE "Moneda" = RE."IdMonedaEmisionInstrumento")) 				AS "PrecioPacto"
				,CASE
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 90 THEN 'A'
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 360 THEN 'B'
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 5*360 THEN 'C'
  					WHEN DAYS_BETWEEN(OP."FechaVencimiento", CAR."FechaVencimientoInstrumento") <= 12*360 THEN 'D'
  					ELSE 'E' END AS "Grupo"
				FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
				INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
				INNER JOIN 	"BSCL.Custodia.BD::HIS.Cartera"  CAR ON (RE."IdCarteraCustodia" = CAR."IdCartera" AND CAR."FechaRespaldo" = :p_Fecha)
 				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND ON "CodigoTipoIndicador"  = 'RELCORR' AND IND."IdCliente" = CAR."IdCliente"
				WHERE OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND OP."IdEstado" <> 'ANU' AND CAR."IdTipoInstrumento" != 'PAC'  AND 
					((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
					(TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
					TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD')
				ORDER BY OP."FolioOperacion";
	END IF;

				SELECT COUNT(*) INTO v_count_consulta  FROM :p_out_quincenalDePactos Where "FechaProceso" = :p_Fecha;
				SELECT COUNT(*) INTO p_out_countQuincenales  FROM :v_QuincenalDePactos Where "FechaProceso" = :p_Fecha;
				
				/* Si hay nuevos registros a la fecha, se debe limpiar datos existentes y reemplazar por los nuevos */
				if v_count_consulta > p_out_countQuincenales THEN
					DELETE FROM "BSCL.Mesa.BD::MES.QuincenalPactos" WHERE "FechaProceso" = :p_Fecha;
					SELECT COUNT(*) INTO p_out_countQuincenales  FROM "BSCL.Mesa.BD::MES.QuincenalPactos" Where "FechaProceso" = :p_Fecha;
				END IF;
				
				IF p_out_FolioNemo = 'S' THEN
				
					p_out_subyacenteFolio = SELECT 
						"FolioCarteraRelacionada" 	AS "FolioCarteraRelacionada"
						,SUM("Cantidad") 		AS "Cantidad"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"
					--WHERE "IdTipoOperacion" = 'VENPAC'
					--AND "IdEstado" = 'VIG'
					GROUP BY "FolioCarteraRelacionada";	

					p_out_subyacenteNemo	=	SELECT 
											'' 				AS "Nemotecnico"
											,'' 			AS "IdEmisor"
											,'1900-01-01' 	AS "FechaVencimiento"
											,'' 			AS "IdMoneda"
	    									,0 				AS "Cantidad"
										FROM DUMMY;
			
				ELSE 
			
					p_out_subyacenteNemo =
						SELECT 
						"Nemotecnico"
						,"IdEmisor"
						, "FechaVencimientoInstrumento"		AS "FechaVencimiento"
						, "IdMonedaInstrumento"				AS "IdMoneda"
						, SUM("Cantidad") 					AS "Cantidad"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"
					--WHERE "IdTipoOperacion" = 'VENPAC'
					--AND "IdEstado" = 'VIG'
					GROUP BY "Nemotecnico","IdEmisor", "FechaVencimientoInstrumento", "IdMonedaInstrumento";
					
					p_out_subyacenteFolio	=	SELECT 
   													0 	AS 	"FolioCarteraRelacionada"
	    											,0 	AS  "Cantidad"
	    										FROM DUMMY;
				
				END IF;
		--END IF;
		
  	END;
 END;