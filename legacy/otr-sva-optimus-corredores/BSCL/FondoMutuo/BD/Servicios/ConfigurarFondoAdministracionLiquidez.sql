SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConfigurarFondoAdministracionLiquidez";

CREATE PROCEDURE
--  Proposito: Configurar Fondo Administracion Liquidez .
--  Autor: Gat-Blac 
--  Fecha Creacion: 12/12/2016
"BSCL.FondoMutuo.BD.Servicios::ConfigurarFondoAdministracionLiquidez" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    
    IN p_FondoAdministracionLiquidez			TABLE(
    												"IdMoneda"		NVARCHAR(3),
    												"IdSerieFondo"	NVARCHAR(40),
    												"MontoMinimo"	DECIMAL(28,6)
    											),
   	OUT p_out_Resultado							TABLE(
   													"IdMoneda"		NVARCHAR(3),
   													"IdSerieFondo"	NVARCHAR(40),
   													"MontoMinimo"	DECIMAL(28,6)
   												),
   	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdMoneda"	NVARCHAR(3),"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdMoneda"	NVARCHAR(3),"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdMoneda" NVARCHAR(3), "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdMoneda" NVARCHAR(3), "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	
	BEGIN
		v_out_AdvertenciasDetalle = SELECT '' as "IdMoneda" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT '' as "IdMoneda" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdMoneda",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdMoneda",
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONFIGURAR FONDO ADMINISTRACION LIQUIDEZ','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT '' as "IdMoneda",  "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		
		if IS_EMPTY(:p_FondoAdministracionLiquidez) THEN
			v_out_ErroresDetalle = SELECT '' as "IdMoneda", 'NO_SE_INGRESARON_FONDOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdMoneda") as "Count" into v_count
		FROM :p_FondoAdministracionLiquidez
		GROUP BY "IdMoneda"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT '' as "IdMoneda",  'MONEDA_REPETIDAS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_fondos_entrada = SELECT "P_FONDO"."IdMoneda",
						  "P_FONDO"."IdSerieFondo",
						  "P_FONDO"."MontoMinimo",
						  :p_IdEmpresa AS "IdEmpresa",
						  CASE WHEN COALESCE("SERIEFONDO"."IdSerieFondo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoSerieFondo",
						  CASE WHEN COALESCE("FONDO"."MoneyMarket",'N') = 'N' THEN 'ERR' ELSE 'OK' END AS "ResultadoMoneyMarket",
						  CASE WHEN COALESCE("FONDO"."IdMoneda",'') = "P_FONDO"."IdMoneda" THEN 'OK' ELSE 'ERR' END AS "ResultadoMonedaFondo"
					FROM :p_FondoAdministracionLiquidez AS "P_FONDO"
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SERIEFONDO" ON ("SERIEFONDO"."IdSerieFondo" = "P_FONDO"."IdSerieFondo")
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "FONDO" ON ("FONDO"."IdFondo" = "SERIEFONDO"."IdFondo");
		
		v_validados = SELECT "P_FONDO"."IdMoneda",
							  "P_FONDO"."IdSerieFondo",
							  "P_FONDO"."MontoMinimo",
							  "P_FONDO"."IdEmpresa"
					  FROM :v_fondos_entrada AS "P_FONDO"
					  WHERE 'ERR' NOT IN ("P_FONDO"."ResultadoSerieFondo","P_FONDO"."ResultadoMoneyMarket","P_FONDO"."ResultadoMonedaFondo");
		
		v_out_ErroresDetalle = SELECT "IdMoneda", 'SERIE_NO_EXISTE' AS "Codigo","IdSerieFondo" AS "ValorParametro", 'IdSerieFondo' AS "NombreParametro" 
							   FROM :v_fondos_entrada
							   WHERE  "ResultadoSerieFondo" = 'ERR'
							   UNION ALL
							   SELECT "IdMoneda", 'FONDO_NO_MONEY_MARKEY' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro" 
							   FROM :v_fondos_entrada
							   WHERE  "ResultadoMoneyMarket" = 'ERR'
							   UNION ALL
							   SELECT "IdMoneda", 'MONEDA_INCORRECTA' AS "Codigo","IdMoneda" AS "ValorParametro", 'IdMoneda' AS "NombreParametro" 
							   FROM :v_fondos_entrada
							   WHERE  "ResultadoMonedaFondo" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		IF IS_EMPTY(:v_validados) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_insertar = SELECT "P_FONDO"."IdMoneda",
							 "P_FONDO"."IdSerieFondo",
							 "P_FONDO"."MontoMinimo",
							 "P_FONDO"."IdEmpresa"
					 FROM :v_validados AS "P_FONDO"
					 LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez" AS "Config" ON ("Config"."IdMoneda" = "P_FONDO"."IdMoneda")
					 WHERE COALESCE("Config"."IdMoneda",'') = '';
		
		v_actualizar = SELECT "P_FONDO"."IdMoneda",
							 "P_FONDO"."IdSerieFondo",
							 "P_FONDO"."MontoMinimo",
							 "P_FONDO"."IdEmpresa"
					   FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez" AS "Config"
					   INNER JOIN :v_validados AS "P_FONDO" ON ("Config"."IdMoneda" = "P_FONDO"."IdMoneda");
					   
		INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"("IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa")
		SELECT "IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa" FROM :v_insertar;
		
		UPDATE CONFIGURACION
		SET "IdSerieFondo" = "P_FONDO"."IdSerieFondo",
			"MontoMinimo"  = "P_FONDO"."MontoMinimo"
		FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez" AS CONFIGURACION
		INNER JOIN :v_actualizar AS "P_FONDO" ON (CONFIGURACION."IdMoneda" = "P_FONDO"."IdMoneda");
		
		p_out_Resultado = SELECT "IdMoneda",
							 	 "IdSerieFondo",
							 	 "MontoMinimo"
						  FROM :v_validados;
		
		v_log = SELECT 
						0 AS "IdCabeceraLog"
						,'CONFIGURAR FONDO ADMINISTRACION LIQUIDEZ' AS "IdTipoLog"
						,'FMM' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,TO_NVARCHAR(:v_FechaSistema, 'YYYY-MM-DD') AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0  AS "IdPersona"
						,0  AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente"
						,'' AS "IdTipoDocumentoIdentidad"
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'CONFIGURAR FONDO ADMINISTRACION LIQUIDEZ' AS "IdTipoRegistro"
						,0 AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,0 AS "Folio"
						,0 AS "Cantidad"
						,0 AS "PrecioTasa"
						,0 AS "Monto"
						,"IdMoneda" AS "IdMonedaOrigen"
						,'' AS "IdMonedaLiquidacion"
						,'' AS "TipoCambio"
						,'' AS "Nemo"
						,0 AS "IdProceso"
						,'' AS "EstadoProceso"
						,'' AS "NombreProceso"
					FROM :v_validados;
				
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONFIGURAR FONDO ADMINISTRACION LIQUIDEZ','FIN','FIN');
			
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;		
END;
