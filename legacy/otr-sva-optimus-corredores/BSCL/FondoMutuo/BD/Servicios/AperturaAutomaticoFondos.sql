SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::AperturaAutomaticoFondos";

CREATE PROCEDURE
--  Proposito: Apertura Automatico de Fondos
--  Autor: Gat-Blac 
--  Fecha Creacion: 17/11/2016
"BSCL.FondoMutuo.BD.Servicios::AperturaAutomaticoFondos" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario								BIGINT,
    OUT p_out_Resultado 						TABLE(
    												"IdFondo"						NVARCHAR(40),
    												"EstadoAporteComercial"			NVARCHAR(1),
    												"EstadoRescateComercial"		NVARCHAR(1),
    												"EstadoReinversionComercial"	NVARCHAR(1),
    												"EstadoLiquidezComercial"		NVARCHAR(1),
    												"EstadoAporteBackoffice"		NVARCHAR(1),
    												"EstadoRescateBackoffice"		NVARCHAR(1),
    												"EstadoReinversionBackoffice"	NVARCHAR(1),
    												"EstadoLiquidezBackoffice"		NVARCHAR(1),
    												"BloqueoAporte"					NVARCHAR(1),
    												"BloqueoRescate"				NVARCHAR(1)
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
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
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
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_FechaSistema				NVARCHAR(10);
			DECLARE v_count_advertencias_agf	BIGINT;
			DECLARE v_count_errores_agf			BIGINT;
			DECLARE v_count_desbloqueo			BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'APERTURA AUTOMATICA FONDOS','INICIO','INICIO');
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema); 
			
			/*Se desbloquen los fondo que estan bloqueados que se deben desbloquear por fecha*/
			
			v_fondos_desbloqueos = SELECT "IdFondo"
								   FROM "BSCL.FondoMutuo.BD::FMM.Fondo"
								   WHERE ("BloqueoAporte" = 'S' OR "BloqueoRescate" = 'S') AND "FechaBloqueoHasta" <= :v_FechaSistema AND "BloqueoIndefinido" = 'N';
			
			SELECT COUNT(1) into v_count_desbloqueo
			FROM :v_fondos_desbloqueos;
			
			IF 	v_count_desbloqueo > 0 THEN
				
				UPDATE FONDOS
				SET "BloqueoAporte" = 'N',
					"BloqueoRescate" = 'N'
				FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDOS
				WHERE ("BloqueoAporte" = 'S' OR "BloqueoRescate" = 'S') AND "FechaBloqueoHasta" <= :v_FechaSistema AND "BloqueoIndefinido" = 'N';
				
				CALL "BSCL.FondoMutuo.BD.Servicios::CrearRegistroBloqueoDesbloqueoFondoAgf"(
					p_IdEmpresa => :p_IdEmpresa,
					p_IdUsuario => :p_IdUsuario,
					p_IdModulo => 'FMM',
					p_EsManual => 'N',
					p_Fondo => :v_fondos_desbloqueos,
					p_out_Resultado => :v_out_resultado_agf,
					p_out_AdvertenciasContador => :v_count_advertencias_agf,
					p_out_AdvertenciasDetalle => :v_advertencias_agf,
					p_out_ErroresContador => :v_count_errores_agf,
					p_out_ErroresDetalle => :v_errores_agf
				);
				
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", "Codigo", '' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_errores_agf
							   UNION ALL SELECT * FROM :v_out_ErroresDetalle;
							   
			END IF;
			
			/*Se abren los fondos que estan cerrados y no bloqueados*/
			
			UPDATE FONDOS
			SET "EstadoAporteComercial" = CASE "BloqueoAporte" WHEN 'N' THEN 'A' ELSE 'C' END,
				"EstadoRescateComercial" = CASE "BloqueoRescate" WHEN 'N' THEN 'A' ELSE 'C' END,
				"EstadoReinversionComercial" = 'A',
				"EstadoLiquidezComercial" = 'A',
				"EstadoAporteBackoffice" = CASE "BloqueoAporte" WHEN 'N' THEN 'A' ELSE 'C' END,
				"EstadoRescateBackoffice" = CASE "BloqueoRescate" WHEN 'N' THEN 'A' ELSE 'C' END,
				"EstadoReinversionBackoffice" = 'A',
				"EstadoLiquidezBackoffice" = 'A',
				"FechaFondo" = :v_FechaSistema
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDOS
			WHERE ("BloqueoAporte" = 'N' OR "BloqueoRescate" = 'N')  AND "AperturaAutomaticaFondo" = 'S';
			
			v_log = SELECT 
							0 AS "IdCabeceraLog"
							,'MODIFICACION' AS "IdTipoLog"
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
							,0 AS "IdPortafolio"
							,'' AS "EstadoPortafolio"
							,'APERTURA_AUTOMATICO_FMM' AS "IdTipoRegistro"
							,0 AS "IdRegistro"
							,'' AS "EstadoRegistro"
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
					FROM DUMMY;
		
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			p_out_Resultado = SELECT "IdFondo",
									 "EstadoAporteComercial",
									 "EstadoRescateComercial",
									 "EstadoReinversionComercial",
									 "EstadoLiquidezComercial",
									 "EstadoAporteBackoffice",
									 "EstadoRescateBackoffice",
									 "EstadoReinversionBackoffice",
									 "EstadoLiquidezBackoffice",
									 "BloqueoAporte",
									 "BloqueoRescate"
							  FROM "BSCL.FondoMutuo.BD::FMM.Fondo";
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'APERTURA AUTOMATICA FONDOS','FIN','FIN');
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;						 
END;
