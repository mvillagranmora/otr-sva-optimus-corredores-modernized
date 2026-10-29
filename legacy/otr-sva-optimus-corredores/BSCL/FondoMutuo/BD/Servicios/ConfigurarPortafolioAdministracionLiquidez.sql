SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConfigurarPortafolioAdministracionLiquidez";

CREATE PROCEDURE
--  Proposito: Configurar Portafolios Administracion Liquidez .
--  Autor: Gat-Blac 
--  Fecha Creacion: 12/12/2016
"BSCL.FondoMutuo.BD.Servicios::ConfigurarPortafolioAdministracionLiquidez"(
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    
    IN p_PortafolioFondoAdministracionLiquidez	TABLE(
    												"IdMoneda"		NVARCHAR(3),
    												"IdSerieFondo"	NVARCHAR(40),
    												"MontoMinimo"	DECIMAL(28,6),
    												"IdCliente"		BIGINT,
    												"IdPortafolio"	BIGINT,
    												"IdEstado"		NVARCHAR(3)
    											),
   	OUT p_out_Resultado							TABLE(
   													"IdMoneda"		NVARCHAR(3),
    												"IdSerieFondo"	NVARCHAR(40),
    												"MontoMinimo"	DECIMAL(28,6),
    												"IdCliente"		BIGINT,
    												"IdPortafolio"	BIGINT,
    												"IdEstado"		NVARCHAR(3),
    												"Codigo"		NVARCHAR(100)
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
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONFIGURAR PORTAFOLIO FONDO ADMINISTRACION LIQUIDEZ','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT '' as "IdMoneda",  "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		
		if IS_EMPTY(:p_PortafolioFondoAdministracionLiquidez) THEN
			v_out_ErroresDetalle = SELECT '' as "IdMoneda", 'NO_SE_INGRESARON_PORTAFOLIOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdPortafolio") as "Count" into v_count
		FROM :p_PortafolioFondoAdministracionLiquidez
		GROUP BY "IdPortafolio"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT '' as "IdMoneda",  'PORTAFOLIOS_REPETIDOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_fondos_entrada = SELECT "P_PORTAFOLIO"."IdMoneda",
						  "P_PORTAFOLIO"."IdSerieFondo",
						  "P_PORTAFOLIO"."MontoMinimo",
						  "P_PORTAFOLIO"."IdCliente",
						  "P_PORTAFOLIO"."IdPortafolio",
						  :p_IdEmpresa AS "IdEmpresa",
						  "P_PORTAFOLIO"."IdEstado",
						  COALESCE("Config"."IdConfiguracionPortafolioAdministracionLiquidez",0) AS "IdConfiguracionPortafolioAdministracionLiquidez",
						  CASE WHEN COALESCE("Config"."IdConfiguracionPortafolioAdministracionLiquidez",0) = 0 AND "P_PORTAFOLIO"."IdEstado" = 'INA' THEN 'ERR' ELSE 'OK' END AS "ResultadoClienteConfiguracion",
						  CASE WHEN COALESCE("SERIEFONDO"."IdSerieFondo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoSerieFondo",
						  CASE WHEN COALESCE("FONDO"."MoneyMarket",'N') = 'N' THEN 'ERR' ELSE 'OK' END AS "ResultadoMoneyMarket",
						  CASE WHEN COALESCE("FONDO"."IdMoneda",'') = "P_PORTAFOLIO"."IdMoneda" THEN 'OK' ELSE 'ERR' END AS "ResultadoMonedaFondo",
						  CASE WHEN COALESCE("PORTAFOLIO"."IdCliente",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoCliente",
						  CASE WHEN COALESCE("PORTAFOLIO"."IdPortafolio",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoPortafolio",
						  CASE WHEN COALESCE("INDICADOR"."IdCliente",0) = 0 AND "P_PORTAFOLIO"."IdEstado" = 'ACT' THEN 'ERR' ELSE 'OK' END AS "ResultadoIndicador"
					FROM :p_PortafolioFondoAdministracionLiquidez AS "P_PORTAFOLIO"
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SERIEFONDO" ON ("SERIEFONDO"."IdSerieFondo" = "P_PORTAFOLIO"."IdSerieFondo")
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "FONDO" ON ("FONDO"."IdFondo" = "SERIEFONDO"."IdFondo")
					LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "PORTAFOLIO" ON  ( "PORTAFOLIO"."IdPortafolio" = "P_PORTAFOLIO"."IdPortafolio" AND "PORTAFOLIO"."IdCliente"  = "P_PORTAFOLIO"."IdCliente")
					LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS "INDICADOR" ON ("INDICADOR"."IdCliente" = "PORTAFOLIO"."IdCliente" AND "INDICADOR"."CodigoTipoIndicador" = 'CNTADMLIQ' AND "INDICADOR"."ValorIndicador" = 'S')
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez" AS "Config" ON ("Config"."IdPortafolio" = "P_PORTAFOLIO"."IdPortafolio" AND "Config"."IdMoneda" = "P_PORTAFOLIO"."IdMoneda");
		
		v_validados = SELECT "P_PORTAFOLIO"."IdMoneda",
							  "P_PORTAFOLIO"."IdSerieFondo",
							  "P_PORTAFOLIO"."MontoMinimo",
							  "P_PORTAFOLIO"."IdCliente",
							  "P_PORTAFOLIO"."IdPortafolio",
							  "P_PORTAFOLIO"."IdEmpresa",
							  "P_PORTAFOLIO"."IdConfiguracionPortafolioAdministracionLiquidez",
							  "P_PORTAFOLIO"."IdEstado"
					  FROM :v_fondos_entrada AS "P_PORTAFOLIO"
					  WHERE 'ERR' NOT IN ("P_PORTAFOLIO"."ResultadoClienteConfiguracion","P_PORTAFOLIO"."ResultadoSerieFondo","P_PORTAFOLIO"."ResultadoMoneyMarket","P_PORTAFOLIO"."ResultadoMonedaFondo",
					  					  "P_PORTAFOLIO"."ResultadoCliente","P_PORTAFOLIO"."ResultadoPortafolio","P_PORTAFOLIO"."ResultadoIndicador");
		
		v_errores = SELECT "P_PORTAFOLIO"."IdMoneda",
							  "P_PORTAFOLIO"."IdSerieFondo",
							  "P_PORTAFOLIO"."MontoMinimo",
							  "P_PORTAFOLIO"."IdCliente",
							  "P_PORTAFOLIO"."IdPortafolio",
							  "P_PORTAFOLIO"."IdEmpresa",
							  "P_PORTAFOLIO"."IdEstado",
							  "P_PORTAFOLIO"."ResultadoSerieFondo",
							  "P_PORTAFOLIO"."ResultadoMoneyMarket",
							  "P_PORTAFOLIO"."ResultadoMonedaFondo",
					  		  "P_PORTAFOLIO"."ResultadoCliente",
					  		  "P_PORTAFOLIO"."ResultadoPortafolio",
					  		  "P_PORTAFOLIO"."ResultadoIndicador",
					  		  "P_PORTAFOLIO"."ResultadoClienteConfiguracion"
					  FROM :v_fondos_entrada AS "P_PORTAFOLIO"
					  WHERE 'ERR' IN ("P_PORTAFOLIO"."ResultadoClienteConfiguracion","P_PORTAFOLIO"."ResultadoSerieFondo","P_PORTAFOLIO"."ResultadoMoneyMarket","P_PORTAFOLIO"."ResultadoMonedaFondo",
					  					  "P_PORTAFOLIO"."ResultadoCliente","P_PORTAFOLIO"."ResultadoPortafolio","P_PORTAFOLIO"."ResultadoIndicador");
		
		v_out_ErroresDetalle = SELECT "IdMoneda", 'PORTAFOLIO_NO_TIENE_CONFIGURACION' AS "Codigo",CAST("IdPortafolio" AS NVARCHAR(100)) AS "ValorParametro", 'IdPortafolio' AS "NombreParametro" 
							   FROM :v_fondos_entrada
							   WHERE  "ResultadoSerieFondo" = 'ERR'
							   UNION ALL
							   SELECT "IdMoneda", 'SERIE_NO_EXISTE' AS "Codigo","IdSerieFondo" AS "ValorParametro", 'IdSerieFondo' AS "NombreParametro" 
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
							   SELECT "IdMoneda", 'CLIENTE_INCORRECTO' AS "Codigo",CAST("IdCliente" AS NVARCHAR(100)) AS "ValorParametro", 'IdCliente' AS "NombreParametro" 
							   FROM :v_fondos_entrada
							   WHERE  "ResultadoCliente" = 'ERR'
							   UNION ALL
							   SELECT "IdMoneda", 'PORTAFOLIO_INCORRECTO' AS "Codigo",CAST("IdPortafolio" AS NVARCHAR(100)) AS "ValorParametro", 'IdPortafolio' AS "NombreParametro" 
							   FROM :v_fondos_entrada
							   WHERE  "ResultadoPortafolio" = 'ERR'
							   UNION ALL
							   SELECT "IdMoneda", 'SIN_CONTRATO_ADMINISTRACION_LIQUIDEZ' AS "Codigo",'' AS "ValorParametro", '' AS "NombreParametro" 
							   FROM :v_fondos_entrada
							   WHERE  "ResultadoIndicador" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		
		v_insertar = SELECT  "BSCL.FondoMutuo.BD.Secuencias::ConfiguracionPortafolioAdministracionLiquidez".NEXTVAL AS "IdConfiguracionPortafolioAdministracionLiquidez", 
							 "P_PORTAFOLIO"."IdPortafolio",
							 "P_PORTAFOLIO"."IdCliente",
							 "P_PORTAFOLIO"."IdMoneda",
							 "P_PORTAFOLIO"."IdSerieFondo",
							 "P_PORTAFOLIO"."MontoMinimo",
							 "P_PORTAFOLIO"."IdEmpresa",
							 :v_FechaSistema AS "FechaIngreso",
							 CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",
							 "P_PORTAFOLIO"."IdEstado"
					 FROM :v_validados AS "P_PORTAFOLIO"
					 WHERE COALESCE("P_PORTAFOLIO"."IdConfiguracionPortafolioAdministracionLiquidez",0) = 0;
		
		v_actualizar = SELECT "Config"."IdConfiguracionPortafolioAdministracionLiquidez",
							 "P_PORTAFOLIO"."IdPortafolio",
							 "P_PORTAFOLIO"."IdCliente",
							 "P_PORTAFOLIO"."IdMoneda",
							 "P_PORTAFOLIO"."IdSerieFondo",
							 "P_PORTAFOLIO"."MontoMinimo",
							 "P_PORTAFOLIO"."IdEmpresa",
							 :v_FechaSistema AS "FechaModificacion",
							 CURRENT_UTCTIMESTAMP AS "FechaModificacionRegistro",
							 "P_PORTAFOLIO"."IdEstado"
						FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez" AS "Config"
						INNER JOIN :v_validados AS "P_PORTAFOLIO" ON ("Config"."IdConfiguracionPortafolioAdministracionLiquidez" = "P_PORTAFOLIO"."IdConfiguracionPortafolioAdministracionLiquidez");
		
		INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez"("IdConfiguracionPortafolioAdministracionLiquidez","IdMoneda",
		"MontoMinimo","IdSerieFondo","IdCliente","IdPortafolio","IdEstado","FechaIngreso","FechaIngresoRegistro","IdEmpresa")
		SELECT "IdConfiguracionPortafolioAdministracionLiquidez","IdMoneda","MontoMinimo","IdSerieFondo","IdCliente","IdPortafolio",'ACT',"FechaIngreso",
		"FechaIngresoRegistro","IdEmpresa" FROM :v_insertar;
		
		UPDATE CONFIGURACION
		SET "IdEstado" = "P_PORTAFOLIO"."IdEstado",
			"IdSerieFondo" = "P_PORTAFOLIO"."IdSerieFondo",
			"MontoMinimo" = "P_PORTAFOLIO"."MontoMinimo",
			"FechaModificacion" = "P_PORTAFOLIO"."FechaModificacion",
			"FechaModificacionRegistro" = "P_PORTAFOLIO"."FechaModificacionRegistro"
		FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez" AS CONFIGURACION
		INNER JOIN :v_actualizar AS "P_PORTAFOLIO" ON (CONFIGURACION."IdConfiguracionPortafolioAdministracionLiquidez" = "P_PORTAFOLIO"."IdConfiguracionPortafolioAdministracionLiquidez");
		
		p_out_Resultado = SELECT "IdMoneda",
    							 "IdSerieFondo",
    							 "MontoMinimo",
    							 "IdCliente",
    							 "IdPortafolio",
    							 "IdEstado",
    							 'UI_EXITO' AS "Codigo"
						  FROM :v_validados
						  UNION ALL
						  SELECT "IdMoneda",
    							 "IdSerieFondo",
    							 "MontoMinimo",
    							 "IdCliente",
    							 "IdPortafolio",
    							 "IdEstado",
    							 CASE WHEN "ResultadoClienteConfiguracion" = 'ERR' THEN 'PORTAFOLIO_NO_TIENE_CONFIGURACION'
    							 	  WHEN "ResultadoSerieFondo" = 'ERR' THEN 'SERIE_NO_EXISTE' 
    							 	  WHEN "ResultadoMoneyMarket" = 'ERR' THEN 'FONDO_NO_MONEY_MARKEY' 
    							 	  WHEN "ResultadoMonedaFondo" = 'ERR' THEN 'MONEDA_INCORRECTA'
    							 	  WHEN "ResultadoCliente" = 'ERR' THEN 'CLIENTE_INCORRECTO'
    							 	  WHEN "ResultadoPortafolio" = 'ERR' THEN 'PORTAFOLIO_INCORRECTO'
    							 	  WHEN "ResultadoIndicador" = 'ERR' THEN 'SIN_CONTRATO_ADMINISTRACION_LIQUIDEZ' 
    							 END AS "Codigo"
						  FROM :v_errores;
		
		v_log = SELECT 
						0 AS "IdCabeceraLog"
						,'PORTAFOLIO FONDO ADMINISTRACION LIQUIDEZ' AS "IdTipoLog"
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
						,'PORTAFOLIO FONDO ADMINISTRACION LIQUIDEZ' AS "IdTipoRegistro"
						,0 AS "IdRegistro"
						,"IdEstado" AS "EstadoRegistro"
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
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONFIGURAR PORTAFOLIO FONDO ADMINISTRACION LIQUIDEZ','FIN','FIN');
			
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;		
END;
