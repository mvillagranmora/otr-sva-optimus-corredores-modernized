SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.InsertQuincenalDePactos";
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.InsertQuincenalDePactos"
    ( 
	IN  p_IdEmpresa     	BIGINT,                                                                            
	IN  p_IdUsuario     	BIGINT,
	IN  p_TablaQuincenal 	table ( 
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
									,"TipoFamilia"					NVARCHAR(4)
									,"TipoInstrumento"				NVARCHAR(4)
									,"TipoReajustePacto"			NVARCHAR(4)
									,"TipoClasificacionSvsCliente"	NVARCHAR(2)
									,"TipoAmortizacion"				NVARCHAR(10)
									,"CategoriaRiesgo"				NVARCHAR(10)
									,"FolioCartera"					BIGINT
									,"CantidadSubyacente"			BIGINT
									,"ValorFinal"					DECIMAL(26,8)
									,"DecimalesPacto"				BIGINT
									,"DecimalesDetalle"				BIGINT
									,"MontoFinal"					DECIMAL(26,8)
									,"PrecioPacto"					DECIMAL(26,8)
							),
	--Salida   	--------------------------------------------------											
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
   		
   		--****************************
		--*VALIDACION REGLAS BASICAS *
		--****************************
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,'MSP',:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		
		
		
			--*****************
			--INSERT QUINCENAL*			
			--*****************
			
			INSERT INTO "BSCL.Mesa.BD::MES.QuincenalPactos"(
				"IdQuincenalPactos" 					
				,"FechaProceso" 						
				,"TipoCartera" 							
				,"Emisor" 								
				,"TipoReajuste" 						
				,"FechaInicioCompromiso"				
				,"FechaVencimientoCompromiso"			
				,"FechaVencimientoInstrumento" 			
				,"MontoNominal" 						
				,"IdTipoOperacion"						
				,"Nemo" 								
				,"IdMoneda"								
				,"FolioOperacion" 						
				,"Secuencia_Pareo" 						
				,"CantidadResto" 						
				,"IndicadorCobertura" 					
				,"CantidadComprometida" 				
				,"IdRelacionCorredor" 					
				,"Lin_Comprom" 							
				,"ClasificacionTipoInstrumento"			
				,"ValorPresente" 						
				,"ValorComprometido" 					
				,"NumeroCupones" 						
				,"PlazoResidual" 						
                ,"RutCliente" 							
				,"SecuenciaRut" 						
				,"RutCorredor" 							
				,"Sec_Comprom"							
				,"TasaInstrumento"						
				,"TasaPacto" 							
				,"TipoFamilia" 							
				,"TipoInstrumento"						
				,"TipoReajustePacto" 					
				,"TipoClasificacionSvsCliente" 			
				,"TipoAmortizacion"						
				,"CategoriaRiesgo" 						
				,"ValorFinal"							
				,"DecimalesPacto"						
				,"DecimalesDetalle"						
				,"MontoFinal"							
				,"PrecioPacto"							)
				 
			SELECT 
				"IdQuincenalPactos" 					AS "IdQuincenalPactos"
				,"FechaProceso" 						AS "FechaProceso"
				,"TipoCartera" 							AS "TipoCartera"
				,"Emisor" 								AS "Emisor"
				,"TipoReajuste" 						AS "TipoReajuste"
				,"FechaInicioCompromiso"				AS "FechaInicioCompromiso"
				,"FechaVencimientoCompromiso"			AS "FechaVencimientoCompromiso"
				,"FechaVencimientoInstrumento" 			AS "FechaVencimientoInstrumento"
				,"MontoNominal" 						AS "MontoNominal"
				,"IdTipoOperacion"						AS "IdTipoOperacion"
				,"Nemo" 								AS "Nemo"
				,"IdMoneda"								AS "IdMoneda"
				,"FolioOperacion" 						AS "FolioOperacion"
				,"Secuencia_Pareo" 						AS "Secuencia_Pareo"
				,"CantidadResto" 						AS "CantidadResto"
				,"IndicadorCobertura" 					AS "IndicadorCobertura"
				,"CantidadComprometida" 				AS "CantidadComprometida"
				,"IdRelacionCorredor" 					AS "IdRelacionCorredor"
				,"Lin_Comprom" 							AS "Lin_Comprom"
				,"ClasificacionTipoInstrumento"			AS "ClasificacionTipoInstrumento"
				,"ValorPresente" 						AS "ValorPresente"
				,"ValorComprometido" 					AS "ValorComprometido"
				,"NumeroCupones" 						AS "NumeroCupones"
				,"PlazoResidual" 						AS "PlazoResidual"
                ,"RutCliente" 							AS "RutCliente"
				,"SecuenciaRut" 						AS "SecuenciaRut"
				,"RutCorredor" 							AS "RutCorredor"
				,"Sec_Comprom"							AS "Sec_Comprom"
				,"TasaInstrumento"						AS "TasaInstrumento"
				,"TasaPacto" 							AS "TasaPacto"
				,"TipoFamilia" 							AS "TipoFamilia"
				,"TipoInstrumento"						AS "TipoInstrumento"
				,"TipoReajustePacto" 					AS "TipoReajustePacto"
				,"TipoClasificacionSvsCliente" 			AS "TipoClasificacionSvsCliente"
				,"TipoAmortizacion"						AS "TipoAmortizacion"
				,"CategoriaRiesgo" 						AS "CategoriaRiesgo"
				,"ValorFinal"							AS "ValorFinal"
				,"DecimalesPacto"						AS "DecimalesPacto"
				,"DecimalesDetalle"						AS "DecimalesDetalle"
				,"MontoFinal"							AS "MontoFinal"
				,"PrecioPacto"							AS "PrecioPacto"
			FROM :p_TablaQuincenal;
		
  	END;
 END;
