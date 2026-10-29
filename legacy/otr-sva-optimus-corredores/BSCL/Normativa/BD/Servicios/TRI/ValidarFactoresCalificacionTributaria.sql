SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ValidarFactoresCalificacionTributaria";

--  Proposito: Valida factores Calificacion Tributaria
--  Autor: Gat-Blac 
--  Fecha Creacion: 24/04/2017

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ValidarFactoresCalificacionTributaria"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_Anio									BIGINT DEFAULT 0,
	OUT p_out_Resultado							TABLE(
													"IdCalificacionTributaria"		BIGINT
													,"IdClasificacionTipoInstrumento" 	NVARCHAR(3)			
													,"IdInstrumento"	 			BIGINT			
													,"ValorHistorico"				DECIMAL(26,8)
													,"FechaPago"					DATE	
													,"IdEventoCapital"				BIGINT
													,"Descripcion"					NVARCHAR(250)
													,"SecuenciaEvento"				BIGINT
													,"AnioOrigen"					BIGINT
													,"TasaCredito"					DECIMAL(26,8)
													,"PorcentajeDividendo"			DECIMAL(26,8)
													,"IdTipoUtilidades"				NVARCHAR(10)
													,"Factor06"						DECIMAL(26,8)
													,"Factor07"						DECIMAL(26,8)
													,"Factor08"						DECIMAL(26,8)
													,"Factor16"						DECIMAL(26,8)
													,"Factor17"						DECIMAL(26,8)
													,"Factor11"						DECIMAL(26,8)
													,"Factor09"						DECIMAL(26,8)
													,"Factor13"						DECIMAL(26,8)
													,"Factor12"						DECIMAL(26,8)
													,"Factor14"						DECIMAL(26,8)
													,"Factor15"						DECIMAL(26,8)
													,"Factor10"						DECIMAL(26,8)
													,"FechaIngresoSistema"			DATE
													,"FechaIngresoRegistro"			TIMESTAMP
													,"FechaModificacionSistema"		DATE
													,"FechaModificacionRegistro"	TIMESTAMP	
													,"TipoRegistro"					NVARCHAR(10)
													,"IdEstado"						NVARCHAR(3)
													,"IdEstadoResultado"			NVARCHAR(3)
													,"NombreEstado"					NVARCHAR(100)
													,"NombreEstadoResultado"		NVARCHAR(100)
													,"ResultadoSumaFactor"			DECIMAL(26,8)
												),
   	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
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
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ValidarFactoresCalificacionTributaria','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		v_calificaciones = SELECT 	CALIFICACION."IdCalificacionTributaria"	
									,CALIFICACION."IdClasificacionTipoInstrumento"			
									,CALIFICACION."IdInstrumento"				
									,CALIFICACION."ValorHistorico"			
									,CALIFICACION."FechaPago"					
									,CALIFICACION."IdEventoCapital"			
									,CALIFICACION."Descripcion"				
									,CALIFICACION."SecuenciaEvento"			
									,CALIFICACION."AnioOrigen"				
									,CALIFICACION."TasaCredito"				
									,CALIFICACION."PorcentajeDividendo"		
									,CALIFICACION."IdTipoUtilidades"			
									,CALIFICACION."Factor06"					
									,CALIFICACION."Factor07"					
									,CALIFICACION."Factor08"					
									,CALIFICACION."Factor16"					
									,CALIFICACION."Factor17"					
									,CALIFICACION."Factor11"					
									,CALIFICACION."Factor09"					
									,CALIFICACION."Factor13"					
									,CALIFICACION."Factor12"					
									,CALIFICACION."Factor14"					
									,CALIFICACION."Factor15"					
									,CALIFICACION."Factor10"					
									,CALIFICACION."FechaIngresoSistema"		
									,CALIFICACION."FechaIngresoRegistro"		
									,CALIFICACION."FechaModificacionSistema"	
									,CALIFICACION."FechaModificacionRegistro"	
									,CALIFICACION."TipoRegistro"				
									,CALIFICACION."IdEstado"
									,ESTADO."Nombre" AS "NombreEstado"
									,TO_DECIMAL(CALIFICACION."Factor06" + CALIFICACION."Factor07" + CALIFICACION."Factor08" + CALIFICACION."Factor16" + CALIFICACION."Factor17" + CALIFICACION."Factor11" + CALIFICACION."Factor09" + 
									 CALIFICACION."Factor13" + CALIFICACION."Factor12" + CALIFICACION."Factor14" + CALIFICACION."Factor15" + CALIFICACION."Factor10",26,8) AS "ResultadoSumaFactor"	
								FROM "BSCL.Normativa.BD::TRI.CalificacionTributaria" CALIFICACION
								LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ESTADO ON CALIFICACION."IdEstado" = ESTADO."IdEstado"
								WHERE :p_Anio = 0 OR :p_Anio = CALIFICACION."AnioOrigen";
		
		v_calificaciones_erroneas = SELECT 	CALIFICACION."IdCalificacionTributaria"	
									,CALIFICACION."IdClasificacionTipoInstrumento"			
									,CALIFICACION."IdInstrumento"				
									,CALIFICACION."ValorHistorico"			
									,CALIFICACION."FechaPago"					
									,CALIFICACION."IdEventoCapital"			
									,CALIFICACION."Descripcion"				
									,CALIFICACION."SecuenciaEvento"			
									,CALIFICACION."AnioOrigen"				
									,CALIFICACION."TasaCredito"				
									,CALIFICACION."PorcentajeDividendo"		
									,CALIFICACION."IdTipoUtilidades"			
									,CALIFICACION."Factor06"					
									,CALIFICACION."Factor07"					
									,CALIFICACION."Factor08"					
									,CALIFICACION."Factor16"					
									,CALIFICACION."Factor17"					
									,CALIFICACION."Factor11"					
									,CALIFICACION."Factor09"					
									,CALIFICACION."Factor13"					
									,CALIFICACION."Factor12"					
									,CALIFICACION."Factor14"					
									,CALIFICACION."Factor15"					
									,CALIFICACION."Factor10"					
									,CALIFICACION."FechaIngresoSistema"		
									,CALIFICACION."FechaIngresoRegistro"		
									,CALIFICACION."FechaModificacionSistema"	
									,CALIFICACION."FechaModificacionRegistro"	
									,CALIFICACION."TipoRegistro"				
									,CALIFICACION."IdEstado"
									,'ERR' AS "IdEstadoResultado"
									,CALIFICACION."NombreEstado"
									,ESTADO."Nombre" AS "NombreEstadoResultado"
									,CALIFICACION."ResultadoSumaFactor"	
								FROM :v_calificaciones CALIFICACION
								LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ESTADO ON 'ERR' = ESTADO."IdEstado"
								WHERE "ResultadoSumaFactor" != 100;
		
		
		p_out_Resultado = SELECT CALIFICACION."IdCalificacionTributaria"	
									,CALIFICACION."IdClasificacionTipoInstrumento"			
									,CALIFICACION."IdInstrumento"				
									,CALIFICACION."ValorHistorico"			
									,CALIFICACION."FechaPago"					
									,CALIFICACION."IdEventoCapital"			
									,CALIFICACION."Descripcion"				
									,CALIFICACION."SecuenciaEvento"			
									,CALIFICACION."AnioOrigen"				
									,CALIFICACION."TasaCredito"				
									,CALIFICACION."PorcentajeDividendo"		
									,CALIFICACION."IdTipoUtilidades"			
									,CALIFICACION."Factor06"					
									,CALIFICACION."Factor07"					
									,CALIFICACION."Factor08"					
									,CALIFICACION."Factor16"					
									,CALIFICACION."Factor17"					
									,CALIFICACION."Factor11"					
									,CALIFICACION."Factor09"					
									,CALIFICACION."Factor13"					
									,CALIFICACION."Factor12"					
									,CALIFICACION."Factor14"					
									,CALIFICACION."Factor15"					
									,CALIFICACION."Factor10"					
									,CALIFICACION."FechaIngresoSistema"		
									,CALIFICACION."FechaIngresoRegistro"		
									,CALIFICACION."FechaModificacionSistema"	
									,CALIFICACION."FechaModificacionRegistro"	
									,CALIFICACION."TipoRegistro"				
									,CALIFICACION."IdEstado"
									,CALIFICACION."IdEstadoResultado"
									,CALIFICACION."NombreEstado"
									,CALIFICACION."NombreEstadoResultado"
									,CALIFICACION."ResultadoSumaFactor"
							FROM :v_calificaciones_erroneas AS CALIFICACION;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ValidarFactoresCalificacionTributaria','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
