SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarAperturaCierreInstrumento";

CREATE PROCEDURE
--  Proposito: Modificar Cierre, Apertura Fondo Mutuo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 12/09/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarAperturaCierreInstrumento"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    
    IN p_Fondo							TABLE	(
    										"IdFondo"       	 NVARCHAR(40),
    										"EstadoAporte"       NVARCHAR(1),
    										"EstadoRescate"      NVARCHAR(1),
    										"EstadoReinversion"  NVARCHAR(1),
    										"EstadoLiquidez"	 NVARCHAR(1)
    									),
   	OUT p_out_Resultado					TABLE (
   											"IdFondo"			NVARCHAR(40),
   											"EstadoAporte"		NVARCHAR(1),
   											"EstadoReinversion"	NVARCHAR(1),
   											"EstadoLiquidez"	NVARCHAR(1),
   											"Resultado"			NVARCHAR(1),
   											"Codigo"			NVARCHAR(40),
   											"IdEmpresa"			BIGINT		
   										),
   	OUT p_out_AdvertenciasContador 		INT,
   	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
   	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
		DECLARE v_FechaSistema 				NVARCHAR(10);
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE	v_count_fondos				BIGINT;
		DECLARE v_count_fondos_maestro		BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR APERTURA CIERRE FM','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) AS "Count" into v_count_fondos
		FROM :p_Fondo;
		
		if (v_count_fondos = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_FONDOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_fondos_maestro = SELECT
								"P_Fondo"."IdFondo"					
						  		,"P_Fondo"."EstadoAporte"				
						  		,"P_Fondo"."EstadoRescate"
						  		,"P_Fondo"."EstadoReinversion"
						  		,"P_Fondo"."EstadoLiquidez"				
						  		,:p_IdEmpresa AS "IdEmpresa"
						  		,CASE WHEN COALESCE("Fondo"."IdFondo",'-1')!='-1' OR "P_Fondo"."IdFondo" = '' THEN 'OK' ELSE 'ERR' END AS "ResultadoFondo"
						  	FROM :p_Fondo AS "P_Fondo"
						  	INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo" AND "Fondo"."IdEmpresa" = :p_IdEmpresa);
		
		BEGIN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'FONDO_NO_EXISTE' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   		FROM :v_fondos_maestro AS "Fondo" WHERE "ResultadoFondo" = 'ERR';
			SELECT COUNT(1) as "Count" INTO v_count_fondos_maestro
			FROM  :v_fondos_maestro WHERE "ResultadoFondo" = 'OK';
			IF (v_count_fondos_maestro = 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		v_fondos_finales = SELECT
								"FondoMaestro"."IdFondo"					
						  		,"FondoMaestro"."EstadoAporte"				
						  		,"FondoMaestro"."EstadoRescate"
						  		,"FondoMaestro"."EstadoReinversion"
						  		,"FondoMaestro"."EstadoLiquidez"
						  		,"FondoMaestro"."IdEmpresa"
						  	FROM :v_fondos_maestro AS "FondoMaestro"
						  	WHERE "ResultadoFondo" = 'OK';
		
		v_fondos = SELECT DISTINCT
						 0 AS "IdInterno"
						,"Fondos"."IdFondo"
						,'TODOS' AS "Perfil"						
				  		,"Fondos"."EstadoAporte"				
				  		,"Fondos"."EstadoRescate"
				  		,"Fondos"."EstadoReinversion"
				  		,"Fondos"."EstadoLiquidez"
				  		,'' AS "BloqueoAporte"
				  		,'' AS "BloqueoRescate"
				  		,'' AS "BloqueoIndefinido"
				  		,'' AS "FechaBloqueoDesde"
				  		,'' AS "FechaBloqueoHasta"
					FROM :v_fondos_finales AS "Fondos";
		
		CALL "BSCL.FondoMutuo.BD.Servicios::CambiarEstadoAperturaCiereBloqueo"(:p_IdEmpresa,:v_fondos,v_resultado);
		
		v_out_ErroresDetalle = SELECT "IdInterno", "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM :v_resultado WHERE "Resultado" = 'N';	
		
		p_out_Resultado = SELECT
								"Fondos"."IdFondo"
								,"Fondos"."EstadoAporte"
						  		,"Fondos"."EstadoReinversion"						
						  		,"Fondos"."EstadoLiquidez"
						  		,"Resultado"."Resultado"
						  		,"Resultado"."Codigo"
						  		,"Fondos"."IdEmpresa"
						  	FROM :v_fondos_finales AS "Fondos"
						  	INNER JOIN :v_resultado AS "Resultado" ON ("Resultado"."IdFondo" = "Fondos"."IdFondo");
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_log = SELECT 
				"IdInterno" AS "IdCabeceraLog"
				,'MODIFICACION' AS "IdTipoLog"
				,'FMM' AS "IdModulo"
				,:p_IdUsuario AS "IdUsuario"
				,:p_IdEmpresa AS "IdEmpresa"
				,:v_FechaSistema AS "FechaSistemaLog"
				,:p_IdUsuario AS "IdUsuarioAtendido"
				,0 AS "IdPersona"
				,0 AS "IdCliente"
				,'' AS "EstadoCliente"
				,'' AS "NombreCliente"
				,'' AS "IdTipoDocumentoIdentidad"
				,'' AS "IdDocumentoIdentidad"
				,0 AS "IdPortafolio"
				,'' AS "EstadoPortafolio"
				,'APERTURA_CIERRE_FMM' AS "IdTipoRegistro"
				,"IdInterno" AS "IdRegistro"
				,'' AS "EstadoRegistro"
				,'' AS "IdTipoOperacion"
				,0 AS "Folio"
				,0 AS "Cantidad"
				,0 AS "PrecioTasa"
				,0 AS "Monto"
				,'' AS "IdMonedaOrigen"
				,'' AS "IdMonedaLiquidacion"
				,'' AS "TipoCambio"
				,"IdFondo" AS "Nemo"
				,0 AS "IdProceso"
				,'' AS "EstadoProceso"
				,'' AS "NombreProceso"
			FROM :v_fondos;
		
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		v_log_apertura_cierre_bloqueo = SELECT	:p_IdUsuario AS "IdUsuario"
												,:p_IdEmpresa AS "IdEmpresa"
												,"Fondo"."IdFondo"
												,:v_FechaSistema AS "FechaSistema"
												,"Fondo"."EstadoAporteComercial" AS "EstadoAporteComercialAntiguo"
												,"Fondo"."EstadoRescateComercial" AS "EstadoRescateComercialAntiguo"
												,"Fondo"."EstadoReinversionComercial" AS "EstadoReinversionComercialAntiguo"
												,"Fondo"."EstadoLiquidezComercial" AS "EstadoLiquidezComercialAntiguo"
												,"Fondo"."EstadoAporteBackoffice" AS "EstadoAporteBackofficeAntiguo"
												,"Fondo"."EstadoRescateBackoffice" AS "EstadoRescateBackofficeAntiguo"
												,"Fondo"."EstadoReinversionBackoffice" AS "EstadoReinversionBackofficeAntiguo"
												,"Fondo"."EstadoLiquidezBackoffice" AS "EstadoLiquidezBackofficeAntiguo"
												,"Fondo"."BloqueoAporte" AS "BloqueoAporteAntiguo"
												,"Fondo"."BloqueoRescate" AS "BloqueoRescateAntiguo"
												,"Fondo"."BloqueoIndefinido" AS "BloqueoIndefinidoAntiguo"
												,"Fondo"."FechaBloqueoDesde" AS "FechaBloqueoDesdeAntiguo"
												,"Fondo"."FechaBloqueoHasta" AS "FechaBloqueoHastaAntiguo"
												,"Fondos"."EstadoAporte"  AS "EstadoAporteComercialNuevo"
												,"Fondos"."EstadoRescate" AS "EstadoRescateComercialNuevo"
												,"Fondos"."EstadoReinversion" AS "EstadoReinversionComercialNuevo"
												,"Fondos"."EstadoLiquidez" AS "EstadoLiquidezComercialNuevo"
												,"Fondos"."EstadoAporte" AS "EstadoAporteBackofficeNuevo"
												,"Fondos"."EstadoRescate" AS "EstadoRescateBackofficeNuevo"
												,"Fondos"."EstadoReinversion" AS "EstadoReinversionBackofficeNuevo"
												,"Fondos"."EstadoLiquidez" AS "EstadoLiquidezBackofficeNuevo"
												,'' AS "BloqueoAporteNuevo"
												,'' AS "BloqueoRescateNuevo"
												,'' AS "BloqueoIndefinidoNuevo"
												,'' AS "FechaBloqueoDesdeNuevo"
												,'' AS "FechaBloqueoHastaNuevo"
										FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
										INNER JOIN :v_fondos_finales AS "Fondos" ON "Fondos"."IdFondo" = "Fondo"."IdFondo"
										INNER JOIN :v_resultado AS "Resultado" ON ("Resultado"."IdFondo" = "Fondos"."IdFondo")
										WHERE "Fondo"."IdEmpresa" = "Fondos"."IdEmpresa" AND "Resultado"."Resultado" = 'S';
										
		CALL "BSCL.FondoMutuo.BD.Servicios::AgregarLogAperturaCierreBloqueo"(:v_log_apertura_cierre_bloqueo);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR APERTURA CIERRE BLOQUEO FM','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
