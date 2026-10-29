SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1885.AsociarEventoCapital";

--  Proposito: Asociar Evento Capital.
--  Autor: Gat-Blac 
--  Fecha Creacion: 04/05/2017

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1885.AsociarEventoCapital"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_CalificacionTributaria					TABLE(
													"IdInterno"							BIGINT,
													"SecuenciaEvento"					BIGINT,
													"IdEventoCapital"					BIGINT,
													"IdCalificacionTributaria"			BIGINT,
													"FechaPago"							DATE
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
		DECLARE v_countCalificaciones		BIGINT;
		DECLARE v_count						BIGINT;
		DECLARE v_FechaSistema				NVARCHAR(10);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.AsociarEventoCapital','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) AS "Count" INTO v_countCalificaciones
		FROM :p_CalificacionTributaria;
		
		IF (v_countCalificaciones = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_CALIFICACIONES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" INTO v_count
		FROM :p_CalificacionTributaria
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_dj1885 = SELECT P_CALIFICACIONES."IdInterno"
						  ,DJ1885."SecuenciaEvento"
						  ,DJ1885."IdEventoCapitalCliente" AS "IdEventoCapital"
						  ,DJ1885."FechaPago"
					FROM "BSCL.Normativa.BD::TRI.DatosDJ1885" DJ1885
					INNER JOIN :p_CalificacionTributaria P_CALIFICACIONES ON (DJ1885."SecuenciaEvento" = P_CALIFICACIONES."SecuenciaEvento"
																			  AND DJ1885."IdEventoCapitalCliente" = P_CALIFICACIONES."IdEventoCapital"
																			  AND DJ1885."FechaPago" = P_CALIFICACIONES."FechaPago"); 
					
		v_calificaciones = SELECT P_CALIFICACIONES."IdInterno"
								  ,CALIFICACIONES."IdCalificacionTributaria"
								  ,INSTRUMENTO."Nemotecnico"
								  ,CALIFICACIONES."FechaPago"
								  ,CALIFICACIONES."SecuenciaEvento"
							FROM "BSCL.Normativa.BD::TRI.CalificacionTributaria" CALIFICACIONES
							INNER JOIN :p_CalificacionTributaria P_CALIFICACIONES ON (CALIFICACIONES."IdCalificacionTributaria" = P_CALIFICACIONES."IdCalificacionTributaria")
							INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INSTRUMENTO ON INSTRUMENTO."IdInstrumento" = CALIFICACIONES."IdInstrumento";
		
		
		v_datos_actualizar = SELECT DJ1885."IdInterno"
									,CALIFICACIONES."IdCalificacionTributaria"
									,CALIFICACIONES."Nemotecnico"
									,CALIFICACIONES."FechaPago" AS "FechaPagoCalificacion"
									,CALIFICACIONES."SecuenciaEvento" AS "SecuenciaEventoCalificacion"
									,DJ1885."SecuenciaEvento"
									,DJ1885."IdEventoCapital"
									,DJ1885."FechaPago"
							 FROM :v_dj1885 AS DJ1885
							 INNER JOIN :v_calificaciones AS CALIFICACIONES ON (DJ1885."IdInterno" = CALIFICACIONES."IdInterno");
		
		UPDATE DJ1885
		SET DJ1885."Nemotecnico" = CALIFICACION."Nemotecnico"
			,DJ1885."FechaPago" = CALIFICACION."FechaPagoCalificacion"
			,DJ1885."SecuenciaEvento" = CALIFICACION."SecuenciaEventoCalificacion"
		FROM "BSCL.Normativa.BD::TRI.DatosDJ1885" DJ1885
		INNER JOIN :v_datos_actualizar CALIFICACION ON (DJ1885."SecuenciaEvento" = CALIFICACION."SecuenciaEvento"
																			  AND DJ1885."IdEventoCapitalCliente" = CALIFICACION."IdEventoCapital"
																			  AND DJ1885."FechaPago" = CALIFICACION."FechaPago");
		
		v_log = SELECT 
						"IdCalificacionTributaria" AS "IdCabeceraLog"
						,'INGRESO' AS "IdTipoLog"
						,'NOR' AS "IdModulo"
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
						,'ASOCIAR_EVENTO_CAPITAL' AS "IdTipoRegistro"
						,"IdCalificacionTributaria" AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
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
					FROM :v_datos_actualizar;
					
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.AsociarEventoCapital','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
