SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ReaperturaFondos";

CREATE PROCEDURE
--  Proposito: Reapertura de Fondos cambia fecha del fondo.
--  Autor: Gat-Blac 
--  Fecha Creacion: 30/12/2016
"BSCL.FondoMutuo.BD.Servicios::ReaperturaFondos"(
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    
    IN p_Fondo									TABLE(
    												"IdInterno"		BIGINT,
    												"IdFondo"		NVARCHAR(40),
    												"FechaFondo"	NVARCHAR(10)
    											),
    OUT p_out_Resultado							TABLE(
    												"IdInterno"		BIGINT,
   													"IdFondo"		NVARCHAR(40),
    												"FechaFondo"	NVARCHAR(10)
   												),
   	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
		DECLARE v_count				BIGINT;
		DECLARE v_resultadoRegla	NVARCHAR(40);
		DECLARE v_FechaSistema 		NVARCHAR(10);
		DECLARE v_errores_count		BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'REABRIR FONDO','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno",  "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		if IS_EMPTY(:p_Fondo) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_FONDOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_Fondo
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno",  'FONDOS_REPETIDOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_fondos = SELECT "P_FONDO"."IdInterno",
						  "P_FONDO"."IdFondo",
						  "P_FONDO"."FechaFondo",
						  :p_IdEmpresa AS "IdEmpresa",
						  CASE WHEN COALESCE("Fondos"."IdFondo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoFondo"
				   FROM :p_Fondo AS "P_FONDO"
				   LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondos" ON ("Fondos"."IdFondo" = "P_FONDO"."IdFondo" AND "Fondos"."IdEmpresa" = :p_IdEmpresa);
		
		v_out_ErroresDetalle = SELECT "IdInterno", 'FONDO_NO_EXISTE' AS "Codigo", "IdFondo" AS "ValorParametro", 'IdFondo' AS "NombreParametro" 
							   FROM :v_fondos
							   WHERE "ResultadoFondo" = 'ERR';
		
		v_fondos_validadas = SELECT "IdInterno","IdFondo","FechaFondo","IdEmpresa" 
							 FROM :v_fondos
							 WHERE "ResultadoFondo" = 'OK';
		
		SELECT COUNT(1) into v_errores_count
		FROM :v_fondos_validadas;
		
		IF v_errores_count = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		UPDATE FONDOS
		SET "EstadoAporteComercial" = 'A',
			"EstadoRescateComercial" = 'A',
			"EstadoReinversionComercial" = 'A',
			"EstadoLiquidezComercial" = 'A',
			"EstadoAporteBackoffice" = 'A',
			"EstadoRescateBackoffice" = 'A',
			"EstadoReinversionBackoffice" = 'A',
			"EstadoLiquidezBackoffice" = 'A',
			"FechaFondo" = VALIDADAS."FechaFondo"
		FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDOS
		INNER JOIN :v_fondos_validadas VALIDADAS ON (FONDOS."IdFondo" = VALIDADAS."IdFondo" AND FONDOS."IdEmpresa" = VALIDADAS."IdEmpresa");
		
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
					,'REABRIR_FONDO' AS "IdTipoRegistro"
					,"IdInterno" AS "IdRegistro"
					,'A' AS "EstadoRegistro"
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
			FROM :v_fondos_validadas;
		
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'REABRIR FONDO','FIN','FIN');
		
		p_out_Resultado = SELECT "IdInterno",
								 "IdFondo",
								 "FechaFondo"
						  FROM :p_Fondo;
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;		
END;
