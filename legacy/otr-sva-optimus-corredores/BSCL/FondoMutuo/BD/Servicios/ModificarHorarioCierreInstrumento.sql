SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreInstrumento";

CREATE PROCEDURE
--  Proposito: Modificar Hora de Cierre Para Instrumento Fondo Mutuo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 11/07/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreInstrumento"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    
    IN p_Fondo							TABLE	(
    												"IdInterno"					BIGINT,
    											 	"IdFondo" 		 			NVARCHAR(40),
    											 	"HoraCierreComercial"		NVARCHAR(8),
    											 	"HoraCierreBackOffice"		NVARCHAR(8),
    											 	"UsaHorarioDefault"			NVARCHAR(1),
    											 	"TipoSolicitud"				NVARCHAR(3)		
    											),
    OUT p_out_Resultado					TABLE	(
    												"IdInterno"					BIGINT,
    												"IdFondo"					NVARCHAR(40),
    												"HoraCierreComercial"		TIME,
    												"HoraCierreBackOffice"		TIME,
    												"UsaHorarioDefault"			NVARCHAR(1),
    												"TipoSolicitud"				NVARCHAR(3),
    												"IdEmpresa"					BIGINT
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
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(5000),"NombreParametro" NVARCHAR(5000));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(5000),"NombreParametro" NVARCHAR(5000));
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
		DECLARE v_FechaSistema 			NVARCHAR(10); 
		DECLARE v_resultadoRegla 		NVARCHAR(40);
		DECLARE	v_count_fondos			BIGINT;
		DECLARE v_count					BIGINT;
		DECLARE v_count_fondos_maestro	BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR HORARIO CIERRE INSTRUMENTO FM','INICIO','INICIO');
		
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
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" into v_count
		FROM :p_Fondo
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_fondos_maestro = SELECT
								"P_Fondo"."IdInterno"
								,"P_Fondo"."IdFondo"
								,"P_Fondo"."UsaHorarioDefault"
								,"P_Fondo"."HoraCierreComercial"	
								,"P_Fondo"."HoraCierreBackOffice"
								,"P_Fondo"."TipoSolicitud"						
						  		,:p_IdEmpresa AS "IdEmpresa"
						  		,CASE WHEN COALESCE("Fondo"."IdFondo",'-1')!='-1' THEN 'OK' ELSE 'ERR' END AS "ResultadoSerie"
						  		,CASE WHEN "P_Fondo"."HoraCierreComercial" != '' AND "P_Fondo"."HoraCierreBackOffice" != '' THEN 'OK' ELSE 'ERR' END AS "ResultadoHorarios"
						  	FROM :p_Fondo AS "P_Fondo"
						  	LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo" and "Fondo"."IdEmpresa" = :p_IdEmpresa);
		
		BEGIN
			v_out_ErroresDetalle = SELECT "SerieFondoMaestro"."IdInterno", 'FONDO_NO_EXISTE' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   		FROM :v_fondos_maestro AS "SerieFondoMaestro" WHERE "ResultadoSerie" = 'ERR'
								   UNION ALL 
								   SELECT "SerieFondoMaestro"."IdInterno", 'HORAS_INVALIDAS' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   		FROM :v_fondos_maestro AS "SerieFondoMaestro" WHERE "ResultadoHorarios" = 'ERR';
			
			SELECT COUNT(1) as "Count" INTO v_count_fondos_maestro
			FROM  :v_fondos_maestro WHERE "ResultadoSerie" = 'OK' AND "ResultadoHorarios" = 'OK';
			IF (v_count_fondos_maestro = 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		v_fondos_finales = SELECT
		 						"FondoMaestro"."IdInterno"
								,"FondoMaestro"."IdFondo"
								,"FondoMaestro"."UsaHorarioDefault"
						  		,"FondoMaestro"."HoraCierreComercial"						
						  		,"FondoMaestro"."HoraCierreBackOffice"				
						  		,"FondoMaestro"."TipoSolicitud"
						  		,"FondoMaestro"."IdEmpresa"
						  	FROM :v_fondos_maestro AS "FondoMaestro"
						  	WHERE "ResultadoSerie" = 'OK';
		
		UPDATE Fondo
			SET "UsaHorarioAporteDefault"     		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'APO' THEN Fondo_Finales."UsaHorarioDefault" 	   	ELSE Fondo."UsaHorarioAporteDefault"    		END,
				"HoraCierreAporteComercial"   		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'APO' THEN Fondo_Finales."HoraCierreComercial"   	ELSE Fondo."HoraCierreAporteComercial"  		END,
				"HoraCierreAporteBackoffice"  		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'APO' THEN Fondo_Finales."HoraCierreBackOffice" 	ELSE Fondo."HoraCierreAporteBackoffice" 		END,
				"UsaHorarioRescateDefault"   		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'RES' THEN Fondo_Finales."UsaHorarioDefault" 	   	ELSE Fondo."UsaHorarioRescateDefault"    		END,
				"HoraCierreRescateComercial"  		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'RES' THEN Fondo_Finales."HoraCierreComercial"  	ELSE Fondo."HoraCierreRescateComercial"  		END,
				"HoraCierreRescateBackoffice" 		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'RES' THEN Fondo_Finales."HoraCierreBackOffice" 	ELSE Fondo."HoraCierreRescateBackoffice"  		END,
				"UsaHorarioReinversionDefault" 		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'INV' THEN Fondo_Finales."UsaHorarioDefault" 	   	ELSE Fondo."UsaHorarioReinversionDefault"    	END,
				"HoraCierreReinversionComercial"  	= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'INV' THEN Fondo_Finales."HoraCierreComercial"  	ELSE Fondo."HoraCierreReinversionComercial"  	END,
				"HoraCierreReinversionBackoffice" 	= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'INV' THEN Fondo_Finales."HoraCierreBackOffice" 	ELSE Fondo."HoraCierreReinversionBackoffice" 	END,
				"UsaHorarioLiquidezDefault" 		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'LIQ' THEN Fondo_Finales."UsaHorarioDefault" 	   	ELSE Fondo."UsaHorarioLiquidezDefault"    		END,
				"HoraCierreLiquidezComercial"  		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'LIQ' THEN Fondo_Finales."HoraCierreComercial"  	ELSE Fondo."HoraCierreLiquidezComercial"  		END,
				"HoraCierreLiquidezBackoffice" 		= CASE WHEN Fondo_Finales."TipoSolicitud"  = 'LIQ' THEN Fondo_Finales."HoraCierreBackOffice" 	ELSE Fondo."HoraCierreLiquidezBackoffice" 		END
		FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo
		INNER JOIN :v_fondos_finales AS Fondo_Finales ON (Fondo."IdFondo" = Fondo_Finales."IdFondo")
		WHERE Fondo."IdEmpresa" = Fondo_Finales."IdEmpresa";
		
		p_out_Resultado = SELECT 
								 "IdInterno"
								,"IdFondo"
								,"HoraCierreComercial"
								,"HoraCierreBackOffice"
								,"UsaHorarioDefault"
								,"TipoSolicitud"
								,"IdEmpresa"
							FROM :v_fondos_finales;
		
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
				,'HORA_CIERRE_INSTRUMENTO' AS "IdTipoRegistro"
				,"IdInterno" AS "IdRegistro"
				,"UsaHorarioDefault" AS "EstadoRegistro"
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
			FROM :v_fondos_finales;
			
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR HORARIO CIERRE INSTRUMENTO FM','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
	END;
	END;
END;
	
