SET SCHEMA "BSCL";
GO   
DROP PROCEDURE  "BSCL.FondoMutuo.BD.Reglas::ServicioCumpleRequisitosCrearSolicitudAporteRescate";
GO
CREATE PROCEDURE
-- 
--  Proposito: Validar datos del para ingresar solicitud aporte rescate.
--  Autor: Gat-Blac
--  Fecha Creacion: 01/07/2016  
--  
"BSCL.FondoMutuo.BD.Reglas::ServicioCumpleRequisitosCrearSolicitudAporteRescate"
(
	IN p_SolicitudesAporteRescate			TABLE (
													"IdInterno"					 BIGINT,
													"IdCliente" 				 BIGINT,
											    	"IdPortafolio" 				 BIGINT,
											    	"IdSerieFondo" 				 NVARCHAR(40),
											    	"IdTipoMovimientoFondoMutuo" NVARCHAR(10),
											    	"IdEstadoSerie"				 NVARCHAR(3),
													"Monto" 					 DECIMAL(26,8),
													"NumeroCuotas" 				 DECIMAL(26,8),
													"EstadoFondo"				 NVARCHAR(1),
													"EstadoBloqueo"				 NVARCHAR(1),
													"EsFondoValido"				 NVARCHAR(1),
													"EsFechaValidaAporte"		 NVARCHAR(1),
													"EsFechaValidaRescate"		 NVARCHAR(1),
													"EsDiferido"				 NVARCHAR(1),
													"EsMontoMaximo"				 NVARCHAR(1),
													"EsMontoMinimo"				 NVARCHAR(1),
													"ValorCuota"				 NVARCHAR(1),
													"Overnight"				 	 NVARCHAR(1),
													"EstadoLiquidez"			 NVARCHAR(1),
													"EsMoneyMarket"				 NVARCHAR(1),
													"EsFamiliaEquivalente"		 NVARCHAR(1),
													"EsPortafolioRegimenValido"	 NVARCHAR(1),
													"EsRegimenValido"			 NVARCHAR(1),
													--REINVERSIONES
													"PermiteRescateMandato"		 NVARCHAR(1),
													"PermiteCambioGrupo"		 NVARCHAR(1),
													--TRASPASOS y TRANFERENCIAS
													"EsPortafolioValido"		 NVARCHAR(1),
													--CUENTA INVERSION
													"EsCuentaInversionValido"	 NVARCHAR(1)
												  ),
	IN  p_Portafolios						TABLE (
													"IdInterno"					 BIGINT, 
													"IdPortafolio"				 BIGINT,
													"IdPortafolioReal"			 BIGINT,
													"IdEstadoPortafolio" 		 NVARCHAR(3),
													"IdCliente"					 BIGINT,
													"IdClienteReal"				 BIGINT,
													"IdEstadoCliente" 			 NVARCHAR(3),
													"IdClienteDestino"			 BIGINT,
													"IdClienteRealDestino"		 BIGINT,
													"IdPortafolioDestino"		 BIGINT,
													"IdPortafolioRealDestino"	 BIGINT,
													"IdEstadoClienteDestino"	 NVARCHAR(3),
													"IdEstadoPortafolioDestino"	 NVARCHAR(3),
													"DocumentoIdentidad" 		 NVARCHAR(40),
													"IdEmpleadoPortafolio"		 BIGINT
												  ),
	OUT p_out_SalidaDetalle     TABLE ("IdInterno" BIGINT, "Resultado" NVARCHAR(40), "Codigo" nvarchar(40),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40)),
	OUT p_out_Salida        	TABLE ("IdInterno" BIGINT, "Resultado" NVARCHAR(40))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
	BEGIN
	
		v_matriz_validaciones = SELECT sol."IdInterno" AS "IdInterno",
									1	AS	"CLIENTE_EXISTE",
									1	AS	"CLIENTE_HABILITADO",
									1	AS	"PORTAFOLIO_EXISTE",
									1	AS	"PORTAFOLIO_HABILITADO",
									CASE WHEN por."IdClienteRealDestino"    != 0 THEN 1 ELSE 0 END AS "CLIENTE_DESTINO_EXISTE",
									CASE WHEN por."IdClienteRealDestino"	!= 0 THEN 1	ELSE 0 END AS "CLIENTE_DESTINO_HABILITADO",
									CASE WHEN por."IdPortafolioRealDestino" != 0 THEN 1 ELSE 0 END AS "PORTAFOLIO_DESTINO_EXISTE",
									CASE WHEN por."IdPortafolioRealDestino" != 0 THEN 1 ELSE 0 END AS "PORTAFOLIO_DESTINO_HABILITADO",
									CASE WHEN por."IdClienteRealDestino"	!= 0 THEN 1 ELSE 0 END AS "CLIENTE_DESTINO_VALIDO",
									CASE WHEN por."IdPortafolioRealDestino" != 0 AND por."IdClienteRealDestino"	!= 0 THEN 1 ELSE 0 END AS "PORTAFOLIO_DESTINO_VALIDO",
									1	AS	"AGENTE_CONFIGURADO",
									1	AS	"INSTRUMENTO_EXISTE",
									1	AS	"INSTRUMENTO_VIGENTE",
									1	AS	"TIPO_OPERACION_VALIDA",
									1	AS	"ESTADO_FONDO",
									1	AS	"ESTADO_BLOQUEO",
									1	AS	"ES_FONDO_VALIDO",
									1	AS 	"FECHA_VALIDA_APORTE",
									1	AS  "FECHA_VALIDA_RESCATE",
									1	AS	"DIFERIDO_VALIDO",
									1	AS	"MONTO_MAXIMO",
									1 	AS	"MONTO_MINIMO",
									1	AS	"MONTO_INVALIDO",
									1	AS	"NUMERO_CUOTA_INVALIDO",
									1	AS	"VALOR_CUOTA",
									CASE sol."Overnight" WHEN 'S' THEN 1 ELSE 0 END AS "ESTADO_LIQUIDEZ",
									1	AS 	"ES_MONEY_MARKET",
									CASE WHEN sol."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN 1 ELSE 0 END AS  "FAMILIA_EQUIVALENTE",
									1	AS "PORTAFOLIO_REGIMEN_INVALIDO",
									1 	AS "REGIMEN_EXISTE",
									1   AS "RESCATE_MANDATO",
									1	AS "CAMBIO_GRUPO",
									1	AS "PORTAFOLIO_VALIDO",
									1   AS "CUENTA_INVERSION_VALIDO"
								FROM :p_SolicitudesAporteRescate sol
								INNER JOIN :p_Portafolios por ON (por."IdInterno" = sol."IdInterno");
		
		v_TablaRequisitos = 
		SELECT	sol."IdInterno",
		CASE WHEN val."CLIENTE_EXISTE" = 0 OR COALESCE(port."IdClienteReal",-1) > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'CLIENTE_NO_EXISTE' as "Codigo", CAST(port."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 'IdCliente' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."CLIENTE_HABILITADO" = 0 OR port."IdEstadoCliente" = 'ACT' THEN 1 ELSE 4 END as "IdResultado", 
		'CLIENTE_BLOQUEADO' as "Codigo", CAST(port."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 'IdCliente' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."PORTAFOLIO_EXISTE" = 0 OR COALESCE(port."IdPortafolioReal",-1) > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'PORTAFOLIO_NO_EXISTE' as "Codigo", CAST(sol."IdPortafolio" AS NVARCHAR(100)) as "ValorParametro", 'IdPortafolio' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."PORTAFOLIO_HABILITADO" = 0 OR port."IdEstadoPortafolio" = 'ACT' THEN 1 ELSE 4 END as "IdResultado", 
		'PORTAFOLIO_NO_HABILITADO' as "Codigo", CAST(sol."IdPortafolio" AS NVARCHAR(100)) as "ValorParametro", 'IdPortafolio' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."CLIENTE_DESTINO_EXISTE" = 0 OR COALESCE(port."IdClienteRealDestino",-1) > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'CLIENTE_DESTINO_NO_EXISTE' as "Codigo", CAST(port."IdClienteDestino" AS NVARCHAR(100)) as "ValorParametro", 'IdClienteDestino' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."CLIENTE_DESTINO_HABILITADO" = 0 OR port."IdEstadoClienteDestino" = 'ACT' THEN 1 ELSE 4 END as "IdResultado", 
		'CLIENTE_DESTINO_BLOQUEADO' as "Codigo", CAST(port."IdClienteDestino" AS NVARCHAR(100)) as "ValorParametro", 'IdClienteDestino' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."PORTAFOLIO_DESTINO_EXISTE" = 0 OR COALESCE(port."IdPortafolioRealDestino",-1) > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'PORTAFOLIO_DESTINO_NO_EXISTE' as "Codigo", CAST(port."IdPortafolioDestino" AS NVARCHAR(100)) as "ValorParametro", 'IdPortafolioDestino' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."PORTAFOLIO_DESTINO_HABILITADO" = 0 OR port."IdEstadoPortafolioDestino" = 'ACT' THEN 1 ELSE 4 END as "IdResultado", 
		'PORTAFOLIO_DESTINO_NO_HABILITADO' as "Codigo", CAST(port."IdPortafolioDestino" AS NVARCHAR(100)) as "ValorParametro", 'IdPortafolioDestino' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."CLIENTE_DESTINO_VALIDO" = 0 OR port."IdClienteReal" != port."IdClienteRealDestino" THEN 1 ELSE 4 END as "IdResultado", 
		'CLIENTE_DESTINO_NO_VALIDO' as "Codigo", CAST(port."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 'IdCliente' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."PORTAFOLIO_DESTINO_VALIDO" = 0 OR port."IdPortafolioReal" != port."IdPortafolioRealDestino" THEN 1 ELSE 4 END as "IdResultado", 
		'PORTAFOLIO_DESTINO_NO_VALIDO' as "Codigo", CAST(port."IdPortafolio" AS NVARCHAR(100)) as "ValorParametro", 'IdPortafolio' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."CLIENTE_DESTINO_VALIDO" = 0 OR port."IdClienteReal" != port."IdClienteRealDestino" THEN 1 ELSE 4 END as "IdResultado", 
		'CLIENTE_DESTINO_NO_VALIDO' as "Codigo", CAST(port."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 'IdCliente' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."INSTRUMENTO_EXISTE" = 0 OR COALESCE(sol."IdSerieFondo",'') != '' THEN 1 ELSE 4 END as "IdResultado", 
		'INSTRUMENTO_NO_EXISTE' as "Codigo", CAST(sol."IdSerieFondo" AS NVARCHAR(100)) as "ValorParametro", 'IdInstrumento' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."INSTRUMENTO_VIGENTE" = 0 OR sol."IdEstadoSerie" = 'ACT' THEN 1 ELSE 4 END as "IdResultado", 
		'INSTRUMENTO_NO_VIGENTE' as "Codigo", CAST(sol."IdSerieFondo" AS NVARCHAR(100)) as "ValorParametro", 'IdInstrumento' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN val."TIPO_OPERACION_VALIDA" = 0 OR COALESCE(sol."IdTipoMovimientoFondoMutuo",'') != '' THEN 1 ELSE 4 END as "IdResultado", 
		'TIPO_OPERACION_NO_VALIDA' as "Codigo", CAST(sol."IdTipoMovimientoFondoMutuo" AS NVARCHAR(100)) as "ValorParametro", 'IdTipoMovimientoFondoMutuo' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "ESTADO_FONDO" = 0 OR "EstadoFondo" = 'A' THEN 1 ELSE 2 END as "IdResultado",
		'FONDO_CERRADO' as "Codigo", CAST(sol."IdSerieFondo" AS NVARCHAR(100)) as "ValorParametro", 'IdSerieFondo' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "ESTADO_BLOQUEO" = 0 OR "EstadoBloqueo" = 'N' THEN 1 ELSE 4 END as "IdResultado",
		'FONDO_BLOQUEADO' as "Codigo", CAST(sol."EstadoBloqueo" AS NVARCHAR(100)) as "ValorParametro", 'EstadoBloqueo' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "ES_FONDO_VALIDO" = 0 OR "EsFondoValido" = 'S' THEN 1 ELSE 4 END as "IdResultado",
		'FONDO_INVALIDO' as "Codigo", CAST(sol."IdSerieFondo" AS NVARCHAR(100)) as "ValorParametro", 'IdSerieFondo' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "FECHA_VALIDA_APORTE" = 0 OR "EsFechaValidaAporte" = 'S' THEN 1 ELSE 4 END as "IdResultado",
		'FECHA_INVALIDA_APORTE' as "Codigo", CAST(sol."EsFechaValidaAporte" AS NVARCHAR(100)) as "ValorParametro", 'EsFechaValidaAporte' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "FECHA_VALIDA_RESCATE" = 0 OR "EsFechaValidaRescate" = 'S' THEN 1 ELSE 4 END as "IdResultado",
		'FECHA_INVALIDA_RESCATE' as "Codigo", CAST(sol."EsFechaValidaRescate" AS NVARCHAR(100)) as "ValorParametro", 'EsFechaValidaRescate' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "DIFERIDO_VALIDO" = 0 OR (sol."EsDiferido" = 'S') THEN 1 ELSE 4 END as "IdResultado",
		'FONDO_NO_DIFERIDO' as "Codigo", CAST(sol."IdSerieFondo" AS NVARCHAR(100)) as "ValorParametro", 'IdSerieFondo' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "MONTO_MAXIMO" = 0 OR sol."EsMontoMaximo" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'MONTO_MAXIMO' as "Codigo", CAST(sol."EsMontoMaximo" AS VARCHAR(100)) AS "ValorParametro", 'EsMontoMaximo' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "MONTO_MINIMO" = 0 OR sol."EsMontoMinimo" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'MONTO_MINIMO' as "Codigo", CAST(sol."EsMontoMinimo" AS VARCHAR(100)) AS "ValorParametro", 'EsMontoMinimo' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "VALOR_CUOTA" = 0 OR "ValorCuota" > 0 THEN 1 ELSE 4 END as "IdResultado",
		'VALOR_CUOTA_INCORRECTO' as "Codigo", CAST(sol."ValorCuota" AS NVARCHAR(100)) as "ValorParametro", 'EstadoFondo' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "ESTADO_LIQUIDEZ" = 0 OR "EstadoLiquidez" = 'A' THEN 1 ELSE 2 END as "IdResultado",
		'FECHA_LIQUIDEZ_CERRADO' as "Codigo", CAST(sol."EstadoLiquidez" AS NVARCHAR(100)) as "ValorParametro", 'EstadoLiquidez' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "ES_MONEY_MARKET" = 0 OR "EsMoneyMarket" = 'S' THEN 1 ELSE 4 END as "IdResultado",
		'NO_MONEY_MARKET' as "Codigo", CAST(sol."EsMoneyMarket" AS NVARCHAR(100)) as "ValorParametro", 'EsMoneyMarket' as "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "MONTO_INVALIDO" = 0 OR sol."Monto" > 0 THEN 1 ELSE 4 END AS "IdResultado",
		'MONTO_VALIDO' as "Codigo", CAST(sol."Monto" AS VARCHAR(100)) AS "ValorParametro", 'EsCantidadValida' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "NUMERO_CUOTA_INVALIDO" = 0 OR sol."NumeroCuotas" > 0 THEN 1 ELSE 4 END AS "IdResultado",
		'NUMERO_CUOTA_VALIDO' AS "Codigo", CAST(sol."NumeroCuotas" AS VARCHAR(100)) AS "ValorParametro", 'NumeroCuotas' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "FAMILIA_EQUIVALENTE" = 0 OR sol."EsFamiliaEquivalente" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'NO_ES_FAMILIA_EQUIVALENTE' AS "Codigo", CAST(sol."EsFamiliaEquivalente" AS VARCHAR(100)) AS "ValorParametro", 'EsFamiliaEquivalente' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "PORTAFOLIO_REGIMEN_INVALIDO" = 0 OR sol."EsPortafolioRegimenValido" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'PORTAFOLIO_REGIMEN_INVALIDO' AS "Codigo", CAST(sol."EsPortafolioRegimenValido" AS VARCHAR(100)) AS "ValorParametro", "EsPortafolioRegimenValido" AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION
		SELECT sol."IdInterno",
		CASE WHEN "REGIMEN_EXISTE" = 0 OR sol."EsRegimenValido" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'REGIMEN_NO_EXISTE' AS "Codigo", CAST(sol."EsRegimenValido" AS VARCHAR(100)) AS "ValorParametro", 'EsRegimenValido' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		WHERE sol."IdInterno" = val."IdInterno"
		UNION ALL
		SELECT sol."IdInterno",
		CASE WHEN "RESCATE_MANDATO" = 0 OR sol."PermiteRescateMandato" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'RESCATE_MANDATO_NO_PERMITIDO' AS "Codigo", CAST(sol."PermiteRescateMandato" AS VARCHAR(100)) AS "ValorParametro", 'PermiteRescateMandato' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		UNION ALL
		SELECT sol."IdInterno",
		CASE WHEN "CAMBIO_GRUPO" = 0 OR sol."PermiteCambioGrupo" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'CAMBIO_GRUPO_REGIMEN_NO_PERMITIDO' AS "Codigo", CAST(sol."PermiteCambioGrupo" AS VARCHAR(100)) AS "ValorParametro", 'PermiteCambioGrupo' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		UNION ALL
		SELECT sol."IdInterno",
		CASE WHEN "PORTAFOLIO_VALIDO" = 0 OR sol."EsPortafolioValido" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'PORTAFOLIO_DESTINO_NO_COINCIDE_PROPOSITO' AS "Codigo", CAST(sol."EsPortafolioValido" AS VARCHAR(100)) AS "ValorParametro", 'EsPortafolioValido' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val
		UNION ALL
		SELECT sol."IdInterno",
		CASE WHEN "CUENTA_INVERSION_VALIDO" = 0 OR sol."EsCuentaInversionValido" = 'S' THEN 1 ELSE 4 END AS "IdResultado",
		'PORTAFOLIO_NO_OPERA_CUENTA_INVERSION_FFMM' AS "Codigo", CAST(sol."EsCuentaInversionValido" AS VARCHAR(100)) AS "ValorParametro", 'EsCuentaInversionValido' AS "NombreParametro"
		FROM :p_SolicitudesAporteRescate sol
		INNER JOIN :p_Portafolios port on sol."IdInterno" = port."IdInterno"
		, :v_matriz_validaciones val;
		
		p_out_SalidaDetalle =	SELECT 
								tb."IdInterno", 
								CASE "IdResultado"
									WHEN 1 THEN 'OK'
									WHEN 2 THEN 'ADV'
									WHEN 3 THEN 'AUT'
									WHEN 4 THEN 'ERR'
								END as "Resultado",
								tb."Codigo" ,
								tb."ValorParametro" ,
								tb."NombreParametro" 
							FROM :v_TablaRequisitos tb
							ORDER BY "IdInterno";
		
		p_out_Salida = 	SELECT "IdInterno",CASE MAX("IdResultado")
									WHEN 1 THEN 'OK'
									WHEN 2 THEN 'ADV'
									WHEN 3 THEN 'AUT'
									WHEN 4 THEN 'ERR'
								END AS "Resultado"
								
					FROM :v_TablaRequisitos 
					GROUP BY "IdInterno"
					ORDER BY "IdInterno";
END;
