SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreDefault";

CREATE PROCEDURE
--  Proposito: Modificar Hora de Cierre Default Fondo Mutuo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 11/07/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreDefault"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    
    IN p_Horario						TABLE	(
    												"IdInterno"							BIGINT,
    											 	"HoraCierreAporteComercial"			NVARCHAR(8),
    											 	"HoraCierreAporteBackOffice"		NVARCHAR(8),
    											 	"HoraCierreRescateComercial"		NVARCHAR(8),
    											 	"HoraCierreRescateBackOffice"		NVARCHAR(8),
    											 	"HoraCierreReinversionComercial"	NVARCHAR(8),
    											 	"HoraCierreReinversionBackOffice"	NVARCHAR(8)
    											),
    OUT p_out_Resultado					TABLE	(
    												"IdInterno"							BIGINT,
    											 	"HoraCierreAporteComercial"			TIME,
    											 	"HoraCierreAporteBackOffice"		TIME,
    											 	"HoraCierreRescateComercial"		TIME,
    											 	"HoraCierreRescateBackOffice"		TIME,
    											 	"HoraCierreReinversionComercial"	TIME,
    											 	"HoraCierreReinversionBackOffice"	TIME,
    											 	"IdEmpresa"							BIGINT
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
		DECLARE v_count					BIGINT;
		DECLARE v_count_horarios		BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR HORARIO CIERRE DEFAULT FM','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) AS "Count" into v_count_horarios
		FROM :p_Horario;
		
		if (v_count_horarios = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SERIES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" into v_count
		FROM :p_Horario
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_horarios_maestro = SELECT
									 "IdInterno"
									,"HoraCierreAporteComercial"
									,"HoraCierreAporteBackOffice"
									,"HoraCierreRescateComercial"
									,"HoraCierreRescateBackOffice"
									,"HoraCierreReinversionComercial"
									,"HoraCierreReinversionBackOffice"
									,:p_IdEmpresa AS "IdEmpresa"
							 FROM :p_Horario; 					
		
		UPDATE ConfiguracionFondoMutuo
		SET "Value" = CASE "Key" WHEN  'HORA_CIERRE_APORTE_COMERCIAL'  		THEN HorariosMaestro."HoraCierreAporteComercial"
						   		 WHEN  'HORA_CIERRE_RESCATE_COMERCIAL' 		THEN HorariosMaestro."HoraCierreRescateComercial"
						   		 WHEN  'HORA_CIERRE_REINVERSION_COMERCIAL'  THEN HorariosMaestro."HoraCierreReinversionComercial"
						   		 WHEN  'HORA_CIERRE_APORTE_BACKOFFICE'		THEN HorariosMaestro."HoraCierreAporteBackOffice"
						   		 WHEN  'HORA_CIERRE_RESCATE_BACKOFFICE'		THEN HorariosMaestro."HoraCierreRescateBackOffice"
						   		 WHEN  'HORA_CIERRE_REINVERSION_BACKOFFICE' THEN HorariosMaestro."HoraCierreReinversionBackOffice"
					   ELSE		 "Value"
					   END
		FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS ConfiguracionFondoMutuo
		INNER JOIN :v_horarios_maestro AS HorariosMaestro ON (HorariosMaestro."IdEmpresa" = ConfiguracionFondoMutuo."IdEmpresa")
		WHERE ConfiguracionFondoMutuo."Key" IN ('HORA_CIERRE_APORTE_COMERCIAL'
												,'HORA_CIERRE_RESCATE_COMERCIAL'
												,'HORA_CIERRE_REINVERSION_COMERCIAL'
												,'HORA_CIERRE_LIQUIDEZ_COMERCIAL'
												,'HORA_CIERRE_APORTE_BACKOFFICE'
												,'HORA_CIERRE_RESCATE_BACKOFFICE'
												,'HORA_CIERRE_REINVERSION_BACKOFFICE'
												,'HORA_CIERRE_LIQUIDEZ_BACKOFFICE');
		
		p_out_Resultado = SELECT
								"IdInterno"
								,"HoraCierreAporteComercial"
								,"HoraCierreAporteBackOffice"
								,"HoraCierreRescateComercial"
								,"HoraCierreRescateBackOffice"
								,"HoraCierreReinversionComercial"
								,"HoraCierreReinversionBackOffice"
								,"IdEmpresa"
							FROM :v_horarios_maestro;
		
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
				,'HORA_CIERRE_FONDO_MUTUO' AS "IdTipoRegistro"
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
				,'' AS "Nemo"
				,0 AS "IdProceso"
				,'' AS "EstadoProceso"
				,'' AS "NombreProceso"
			FROM :v_horarios_maestro;
			
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR HORARIO CIERRE DEFAULT FM','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
