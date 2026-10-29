SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::AnulacionCargaSolicitudAporteRescate";

CREATE PROCEDURE
--  Proposito: Anulacion Carga Solicitud Aporte Rescate
--  Autor: Gat-Blac 
--  Fecha Creacion: 17/10/2016
"BSCL.FondoMutuo.BD.Servicios::AnulacionCargaSolicitudAporteRescate"(
	--VARIABLES DE ENTRADA OBLIGATORIAS 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),
    
    IN p_IdCargas 					  	  TABLE("IdCarga" BIGINT),
	
	OUT p_out_Resultado						  TABLE(
										  	"IdCarga"  	BIGINT,
										  	"Resultado"	NVARCHAR(1),
										  	"Codigo"   	NVARCHAR(100)
										  ),
	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle TABLE ("IdInterno" BIGINT,"Codigo" nvarchar(100),"Descripcion" nvarchar(5000))    
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_Resultado				TABLE ("IdCarga"   BIGINT, "Resultado" NVARCHAR(1), "Codigo" NVARCHAR(100));
	BEGIN
		v_out_Resultado	= SELECT 0 AS "IdCarga", '' AS "Resultado", '' AS "Codigo" FROM dummy WHERE 1=2;
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	--------------------
	--Manejo de Errores
	--------------------
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			DROP TABLE #RESULTADO_CargaSolicitudAporteRescateMasiva;
			RESIGNAL;
		END;
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
			
			p_out_Resultado = SELECT "IdCarga",
									 "Resultado",
									 "Codigo"
							   FROM :v_out_Resultado;
										
			DROP TABLE #RESULTADO_CargaSolicitudAporteRescateMasiva; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_count			 			BIGINT;
		DECLARE v_count_cargas_duplicadas	BIGINT;
		DECLARE v_FechaSistema				NVARCHAR(10);
		--------------------
		--LOGICA 
		--------------------
		CREATE LOCAL TEMPORARY TABLE #RESULTADO_CargaSolicitudAporteRescateMasiva (
			"IdCarga" 	BIGINT, 
			"Resultado" NVARCHAR(1), 
			"Codigo" 	NVARCHAR(40)
		);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'ANULAR CARGA SOLICITUD APORTE RESCATE','INICIO','INICIO');
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) into v_count
		FROM :p_IdCargas;
		
		IF :v_count = 0 THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_CARGAS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdCarga") as "Count" into v_count_cargas_duplicadas
		FROM :p_IdCargas
		GROUP BY "IdCarga"
		ORDER BY "Count" DESC;
		
		IF :v_count_cargas_duplicadas > 1 THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'CARGAS_DUPLICADAS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		INSERT INTO #RESULTADO_CargaSolicitudAporteRescateMasiva
		SELECT RES."IdCarga",'S','UI_EXITO' FROM :p_IdCargas AS RES
		INNER JOIN "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" AS CARGA ON (RES."IdCarga" = CARGA."IdCargaSolicitudesAporteRescate");
		
		v_cargas_solicitudes_procesadas = SELECT COUNT(1) AS "CONTADOR",
												 RES."IdCarga"
										  FROM :p_IdCargas AS RES
										  INNER JOIN "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" AS CARGA ON (RES."IdCarga" = CARGA."IdCargaSolicitudesAporteRescate")
										  WHERE CARGA."EstadoProceso" = 'PRO'
										  GROUP BY RES."IdCarga";
		
		UPDATE RES
		SET "Resultado" = 'N',
			"Codigo" = 'CARGA_PROCESADA'
		FROM #RESULTADO_CargaSolicitudAporteRescateMasiva AS RES
		INNER JOIN :v_cargas_solicitudes_procesadas AS CARGA ON (RES."IdCarga" = CARGA."IdCarga")
		WHERE CARGA."CONTADOR" > 0;
		
		
		UPDATE CARGA
		SET "Eliminado" = 'S'
		FROM "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" AS CARGA
		INNER JOIN #RESULTADO_CargaSolicitudAporteRescateMasiva AS RES ON (RES."IdCarga" = CARGA."IdCargaSolicitudesAporteRescate")
		LEFT OUTER JOIN :v_cargas_solicitudes_procesadas AS PRO ON (RES."IdCarga" = PRO."IdCarga")
		WHERE COALESCE(PRO."CONTADOR", 0) = 0;
		
		
		v_out_Resultado = SELECT DISTINCT 
								 "IdCarga",
								 "Resultado",
								 "Codigo"
						  FROM #RESULTADO_CargaSolicitudAporteRescateMasiva;
		
		v_log = SELECT "IdCarga" AS "IdCabeceraLog"
					,'ANULACION' AS "IdTipoLog"
					,'FMM' AS "IdModulo"
					,:p_IdUsuario AS "IdUsuario"
					,:p_IdEmpresa AS "IdEmpresa"
					,:v_FechaSistema AS "FechaSistemaLog"
					,:p_IdUsuario AS "IdUsuarioAtendido"
					,0  AS "IdPersona"
					,0  AS "IdCliente"
					,'' AS "EstadoCliente"
					,'' AS "NombreCliente"
					,'' AS "IdTipoDocumentoIdentidad"
					,'' AS "IdDocumentoIdentidad"
					,0  AS "IdPortafolio"
					,'' AS "EstadoPortafolio"
					,'ANULACION_CARGA_SOLICITUD_APORTE_RESCATE' AS "IdTipoRegistro"
					,"IdCarga" AS "IdRegistro"
					,'PRO' AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,'' AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
					FROM :p_IdCargas;
		
		v_DETALLELOG = SELECT "IdCarga" AS "IdCabeceraLog"
									,'ANULACION' AS "SubRegistro"
									,'IdCarga' AS "CampoRegistro"
									,'' AS "ValorAntiguo"
									,COALESCE(TO_VARCHAR("IdCarga"), '') AS "ValorNuevo"
						FROM :p_IdCargas;

		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'ANULAR CARGA SOLICITUD APORTE RESCATE','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
			
		END;
	END;
END;
		
