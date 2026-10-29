SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientos";
GO

CREATE PROCEDURE
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingenier�a ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 04/09/2016
--  Id Servicio = 12003
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientos" (
	IN p_IdEmpresa							BIGINT			DEFAULT 0,
	IN p_IdCentralizacion					BIGINT			DEFAULT 0,
	IN p_IdMovimiento						BIGINT			DEFAULT 0,
	IN p_Numero								BIGINT			DEFAULT 0,
	IN p_IdTipoMovimiento					NVARCHAR(10)	DEFAULT '',
	IN p_FechaMovInicio						NVARCHAR(10)  	DEFAULT '',
	IN p_FechaMovFinal						NVARCHAR(10) 	DEFAULT '',
	IN p_FechaInicio						NVARCHAR(10) 	DEFAULT '',
	IN p_FechaFinal							NVARCHAR(10) 	DEFAULT '',
	IN p_DocumentoIdentidad					NVARCHAR(40)  	DEFAULT '',
	IN p_Cuenta								SMALLINT  		DEFAULT 0,
	IN p_Ejercicio							SMALLINT  		DEFAULT 0,
	IN p_Estado								NVARCHAR(1)   	DEFAULT '',
	--Tabla que agrega filtros adicionales
	IN p_DatosFiltro						TABLE(
												"IdTipoDatoEvento"		NVARCHAR(30),
											  	"TipoDato"				NVARCHAR(1),
											  	"ValorVarchar"			NVARCHAR(60),
											  	"ValorDecimal"			DECIMAL(18,4)
											),
	-- Se agrega tabla resultado donde se devuelven los resultados 
	-- Parametros salida del servicio publico
	OUT p_out_TabMovimientos 				TABLE ( 
												"IdMovimiento" 			BIGINT,
												"IdTipoMovimiento" 		NVARCHAR(10),
												"IdEmpresa" 			BIGINT,
												"Numero" 				BIGINT,
												"Linea" 				BIGINT,
												"FechaMov" 				NVARCHAR(10),
												"DocumentoIdentidad" 	NVARCHAR(40),
												"Cuenta" 				BIGINT,
												"CodigoPortafolio"		NVARCHAR(100),
												"Ejercicio" 			BIGINT,
												"Periodo" 				NVARCHAR(7),
												"Estado" 				NVARCHAR(3),
												"NombreEstado"			NVARCHAR(50),
												"Nombre" 				NVARCHAR(60),
												"FechaContable" 		NVARCHAR(10),
												"IdMoneda"				NVARCHAR(3),
												"Monto" 				DECIMAL(26,8),
												"CompraVenta"			NVARCHAR(60),
												"Concepto"				NVARCHAR(60),
												"CondicionLiquidacion"	NVARCHAR(60),
												"MonedaLiquidacion"		NVARCHAR(60),
												"Operacion"				NVARCHAR(60),
												"Producto"				NVARCHAR(60),
												"Relacion"				NVARCHAR(60)
											),

	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          	TABLE ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      	int,
	OUT p_out_AdvertenciasDetalle       	TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY, 
	OUT p_out_ErroresContador           	int,   
	OUT p_out_ErroresDetalle            	TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	-- FIN Secci�n Fija
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
	DECLARE v_TotalRegistros			BIGINT;
	DECLARE v_CantRegChar				BIGINT;
	DECLARE v_CantRegDeci				BIGINT;
	DECLARE v_TipoFiltro				NVARCHAR(10);
	DECLARE v_TablaDatos				TABLE (
											"IdMovimiento"			BIGINT,
											"CompraVenta"			NVARCHAR(60),
											"Concepto"				NVARCHAR(60),
											"CondicionLiquidacion"	NVARCHAR(60),
											"MonedaLiquidacion"		NVARCHAR(60),
											"Operacion"				NVARCHAR(60),
											"Producto"				NVARCHAR(60),
											"Relacion"				NVARCHAR(60)
										);
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT
												SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2; 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT) DEFAULT
    											SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;
    
    
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;
        v_out_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = SELECT "IdInterno" AS "IdComprobante", "Codigo" AS "Codigo", "Descripcion" AS "Descripcion" FROM :v_out_hash
							   UNION ALL
		                       SELECT * FROM :p_out_ErroresDetalle;

		SELECT COUNT(1)
		  INTO p_out_ErroresContador
		  FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::SQL_ERROR_CODE, v_errorExt);

			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle 
								   UNION ALL
								   SELECT 0 AS "IdComprobante", v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;
			SELECT COUNT(1)
			  INTO p_out_ErroresContador
			  FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado (El log ser� captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	BEGIN
	
		v_TablaDatos =
			SELECT
				"IdMovimiento"
			   ,MAX(MAP("IdTipoDatoEvento",'CompraVenta'			,"ValorVarchar",'')) AS "CompraVenta"
			   ,MAX(MAP("IdTipoDatoEvento",'Concepto'				,"ValorVarchar",'')) AS "Concepto"
			   ,MAX(MAP("IdTipoDatoEvento",'CondicionLiquidacion'	,"ValorVarchar",'')) AS "CondicionLiquidacion"
			   ,MAX(MAP("IdTipoDatoEvento",'MonedaLiquidacion'		,"ValorVarchar",'')) AS "MonedaLiquidacion"
			   ,MAX(MAP("IdTipoDatoEvento",'Operacion'				,"ValorVarchar",'')) AS "Operacion"
			   ,MAX(MAP("IdTipoDatoEvento",'Producto'				,"ValorVarchar",'')) AS "Producto"
			   ,MAX(MAP("IdTipoDatoEvento",'Relacion'				,"ValorVarchar",'')) AS "Relacion"
			FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato"
			WHERE
				"Estado" = 'I'
			GROUP BY
				"IdMovimiento"
			ORDER BY
				"IdMovimiento";
		
		SELECT COUNT(1) INTO v_TotalRegistros FROM :p_DatosFiltro;
		
		IF :v_TotalRegistros > 0 THEN
			v_TipoFiltro := 'COMPUESTO';
		ELSE
			v_TipoFiltro := 'SENCILLO';
		END IF;
		
		--+ Filtra con los valores por de filtro fijo 
		IF :v_TipoFiltro = 'SENCILLO' THEN
			p_out_TabMovimientos = 
				SELECT
					CTB_itzMovimiento."IdMovimiento" 												AS "IdMovimiento"
				   ,CTB_itzMovimiento."IdTipoMovimiento" 											AS "IdTipoMovimiento"
				   ,CTB_itzMovimiento."IdEmpresa" 													AS "IdEmpresa"
				   ,CTB_itzMovimiento."Numero" 														AS "Numero"
				   ,CTB_itzMovimiento."Linea" 														AS "Linea"
				   ,CAST(CAST(CTB_itzMovimiento."Fecha" AS TIMESTAMP) AS NVARCHAR(10)) 				AS "FechaMov"
				   ,CTB_itzMovimiento."DocumentoIdentidad" 											AS "DocumentoIdentidad"
				   ,CTB_itzMovimiento."Cuenta" 														AS "Cuenta"
				   ,Portafolio."CodigoPortafolio"													AS "CodigoPortafolio"
				   ,CTB_itzMovimiento."Ejercicio" 													AS "Ejercicio"
				   ,CTB_itzMovimiento."Periodo" 													AS "Periodo"
				   ,CTB_itzMovimiento."Estado" 														AS "Estado"
				   ,EstadoContabilidad."NombreEstado"												AS "NombreEstado"
				   ,CTB_itzMovimientoTipo."Nombre" 													AS "Nombre"
				   ,CAST(CAST(CTB_itzCentralizacion."FechaContable" AS TIMESTAMP) AS NVARCHAR(10)) 	AS "FechaContable"
				   ,CTB_itzMovimiento."IdMoneda"													AS "IdMoneda"
				   ,ABS(CAST(CTB_itzMovimiento."Monto" AS DECIMAL(26,8))) 							AS "Monto"
				   ,COALESCE(TablaDatos."CompraVenta",'')											AS "CompraVenta"
				   ,COALESCE(TablaDatos."Concepto",'')												AS "Concepto"
				   ,COALESCE(TablaDatos."CondicionLiquidacion",'')									AS "CondicionLiquidacion"
				   ,COALESCE(TablaDatos."MonedaLiquidacion",'')										AS "MonedaLiquidacion"
				   ,COALESCE(TablaDatos."Operacion",'')												AS "Operacion"
				   ,COALESCE(TablaDatos."Producto",'')												AS "Producto"
				   ,COALESCE(TablaDatos."Relacion",'')												AS "Relacion"
				FROM "BSCL.Contabilidad.BD::ITZ.Movimiento" as CTB_itzMovimiento
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" AS CTB_itzMovimientoTipo
						ON CTB_itzMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimiento."IdTipoMovimiento"
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" AS CTB_itzCentralizacion
						ON CTB_itzCentralizacion."IdCentralizacion" = CTB_itzMovimiento."IdCentralizacion"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad
					   ON EstadoContabilidad."IdEstadoContabilidad" = CTB_itzMovimiento."Estado"
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Portafolio
					   ON Portafolio."IdPortafolio" = CTB_itzMovimiento."Cuenta"
				LEFT JOIN :v_TablaDatos AS TablaDatos
					   ON TablaDatos."IdMovimiento" = CTB_itzMovimiento."IdMovimiento"
				WHERE
					(CTB_itzMovimiento."IdCentralizacion" = :p_IdCentralizacion OR :p_IdCentralizacion = 0)
				AND (CTB_itzMovimiento."IdEmpresa" = :p_IdEmpresa)
				AND (CTB_itzMovimiento."Numero" = :p_Numero OR :p_Numero = 0)
				AND (CTB_itzMovimiento."IdMovimiento" = :p_IdMovimiento OR :p_IdMovimiento = 0)
				AND (CTB_itzMovimiento."IdTipoMovimiento" = :p_IdTipoMovimiento OR :p_IdTipoMovimiento = '')
				AND ((CTB_itzMovimiento."Fecha" BETWEEN :p_FechaMovInicio AND :p_FechaMovFinal) OR :p_FechaMovInicio = '')
				AND (CTB_itzMovimiento."DocumentoIdentidad" = :p_DocumentoIdentidad OR :p_DocumentoIdentidad = '        0-0')
				AND (CTB_itzMovimiento."Cuenta" = :p_Cuenta OR :p_Cuenta = 0)
				AND (CTB_itzMovimiento."Ejercicio" = :p_Ejercicio OR :p_Ejercicio = 0)
				AND (CTB_itzMovimiento."Estado" = :p_Estado OR :p_Estado = '')
				AND ((CTB_itzCentralizacion."FechaContable" BETWEEN :p_FechaInicio AND :p_FechaFinal) OR :p_FechaInicio = '');
		END IF;

		IF :v_TipoFiltro = 'COMPUESTO' THEN
			--+ Cuenta los registros que son string  
			SELECT COUNT(*) 
			  INTO v_CantRegChar
			  FROM :p_DatosFiltro as p_DatosFiltro 
			 WHERE p_DatosFiltro."TipoDato" = 'V';
			 
			--+ Cuenta los registros que son numericos  
			SELECT COUNT(*) 
			  INTO v_CantRegDeci
			  FROM :p_DatosFiltro as p_DatosFiltro 
			 WHERE p_DatosFiltro."TipoDato" = 'D';

			IF :v_CantRegChar = 0 THEN 
				v_CantRegChar := -1;
			END IF;
			IF :v_CantRegDeci = 0 THEN 
				v_CantRegDeci := -1;
			END IF;

			p_out_TabMovimientos =
				SELECT
					CTB_itzMovimiento."IdMovimiento" 											AS "IdMovimiento"
				   ,CTB_itzMovimiento."IdTipoMovimiento" 										AS "IdTipoMovimiento"
				   ,CTB_itzMovimiento."IdEmpresa" 												AS "IdEmpresa"
				   ,CTB_itzMovimiento."Numero" 													AS "Numero"
				   ,CTB_itzMovimiento."Linea" 													AS "Linea"
				   ,CAST(CAST(CTB_itzMovimiento."Fecha" AS TIMESTAMP) AS CHAR(10)) 				AS "FechaMov"
				   ,CTB_itzMovimiento."DocumentoIdentidad" 										AS "DocumentoIdentidad"
				   ,CTB_itzMovimiento."Cuenta" 													AS "Cuenta"
				   ,Portafolio."CodigoPortafolio"												AS "CodigoPortafolio"
				   ,CTB_itzMovimiento."Ejercicio" 												AS "Ejercicio"
				   ,CTB_itzMovimiento."Periodo" 												AS "Periodo"
				   ,CTB_itzMovimiento."Estado" 													AS "Estado"
				   ,EstadoContabilidad."NombreEstado"											AS "NombreEstado"
				   ,CTB_itzMovimientoTipo."Nombre" 												AS "Nombre"
				   ,CAST(CAST(CTB_itzCentralizacion."FechaContable" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaContable"
				   ,CTB_itzMovimiento."IdMoneda"												AS "IdMoneda"
				   ,ABS(CAST(CTB_itzMovimiento."Monto" AS DECIMAL)) 							AS "Monto"
				   ,COALESCE(TablaDatos."CompraVenta",'')										AS "CompraVenta"
				   ,COALESCE(TablaDatos."Concepto",'')											AS "Concepto"
				   ,COALESCE(TablaDatos."CondicionLiquidacion",'')								AS "CondicionLiquidacion"
				   ,COALESCE(TablaDatos."MonedaLiquidacion",'')									AS "MonedaLiquidacion"
				   ,COALESCE(TablaDatos."Operacion",'')											AS "Operacion"
				   ,COALESCE(TablaDatos."Producto",'')											AS "Producto"
				   ,COALESCE(TablaDatos."Relacion",'')											AS "Relacion"
				FROM "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" AS CTB_itzMovimientoTipo
						ON CTB_itzMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimiento."IdTipoMovimiento"
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" AS CTB_itzCentralizacion
						ON CTB_itzCentralizacion."IdCentralizacion" = CTB_itzMovimiento."IdCentralizacion"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad
					   ON EstadoContabilidad."IdEstadoContabilidad" = CTB_itzMovimiento."Estado"
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Portafolio
					   ON Portafolio."IdPortafolio" = CTB_itzMovimiento."Cuenta"
				LEFT JOIN :v_TablaDatos AS TablaDatos
					   ON TablaDatos."IdMovimiento" = CTB_itzMovimiento."IdMovimiento"
				WHERE
					(CTB_itzMovimiento."IdCentralizacion"	= :p_IdCentralizacion 							OR :p_IdCentralizacion 	= 0)
				AND (CTB_itzMovimiento."IdEmpresa" 			= :p_IdEmpresa)
				AND (CTB_itzMovimiento."Numero" 			= :p_Numero 									OR :p_Numero 			= 0)
				AND (CTB_itzMovimiento."IdMovimiento" 		= :p_IdMovimiento 								OR :p_IdMovimiento 		= 0)
				AND (CTB_itzMovimiento."IdTipoMovimiento" 	= :p_IdTipoMovimiento 							OR :p_IdTipoMovimiento 	= '')
				AND ((CTB_itzMovimiento."Fecha" 			BETWEEN :p_FechaMovInicio AND :p_FechaMovFinal)	OR :p_FechaMovInicio 	= '')
				AND (CTB_itzMovimiento."DocumentoIdentidad" = :p_DocumentoIdentidad 						OR :p_DocumentoIdentidad 				= '        0-0')
				AND (CTB_itzMovimiento."Cuenta"				= :p_Cuenta 									OR :p_Cuenta 			= 0)
				AND (CTB_itzMovimiento."Ejercicio" 			= :p_Ejercicio 									OR :p_Ejercicio 		= 0)
				AND (CTB_itzMovimiento."Estado" 			= :p_Estado 									OR :p_Estado 			= '')
				AND ((CTB_itzCentralizacion."FechaContable" BETWEEN :p_FechaInicio AND :p_FechaFinal) 		OR :p_FechaInicio 		= '')
				AND :v_CantRegChar = 
				(
					SELECT COUNT(1)
					FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato
					INNER JOIN :p_DatosFiltro AS p_DatosFiltro
							ON p_DatosFiltro."IdTipoDatoEvento" 	= CTB_itzMovimientoDato."IdTipoDatoEvento"
						   AND p_DatosFiltro."ValorVarchar" 		= CTB_itzMovimientoDato."ValorVarchar"
						   AND p_DatosFiltro."TipoDato" 			= 'V'
						   AND p_DatosFiltro."ValorVarchar" 		<> ''
					WHERE CTB_itzMovimientoDato."IdMovimiento" = CTB_itzMovimiento."IdMovimiento"
				)	--+ Busca por los filtros de tipo char
				UNION ALL
				SELECT
					CTB_itzMovimiento."IdMovimiento" 											AS "IdMovimiento"
				   ,CTB_itzMovimiento."IdTipoMovimiento" 										AS "IdTipoMovimiento"
				   ,CTB_itzMovimiento."IdEmpresa" 												AS "IdEmpresa"
				   ,CTB_itzMovimiento."Numero" 													AS "Numero"
				   ,CTB_itzMovimiento."Linea" 													AS "Linea"
				   ,CAST(CAST(CTB_itzMovimiento."Fecha" AS TIMESTAMP) AS CHAR(10)) 				AS "FechaMov"
				   ,CTB_itzMovimiento."DocumentoIdentidad" 										AS "DocumentoIdentidad"
				   ,CTB_itzMovimiento."Cuenta" 													AS "Cuenta"
				   ,Portafolio."CodigoPortafolio"												AS "CodigoPortafolio"
				   ,CTB_itzMovimiento."Ejercicio" 												AS "Ejercicio"
				   ,CTB_itzMovimiento."Periodo" 												AS "Periodo"
				   ,CTB_itzMovimiento."Estado" 													AS "Estado"
				   ,EstadoContabilidad."NombreEstado"											AS "NombreEstado"
				   ,CTB_itzMovimientoTipo."Nombre"												AS "Nombre"
				   ,CAST(CAST(CTB_itzCentralizacion."FechaContable" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaContable"
				   ,CTB_itzMovimiento."IdMoneda"												AS "IdMoneda"
				   ,ABS(CAST(CTB_itzMovimiento."Monto" AS DECIMAL)) 							AS "Monto"
				   ,COALESCE(TablaDatos."CompraVenta",'')										AS "CompraVenta"
				   ,COALESCE(TablaDatos."Concepto",'')											AS "Concepto"
				   ,COALESCE(TablaDatos."CondicionLiquidacion",'')								AS "CondicionLiquidacion"
				   ,COALESCE(TablaDatos."MonedaLiquidacion",'')									AS "MonedaLiquidacion"
				   ,COALESCE(TablaDatos."Operacion",'')											AS "Operacion"
				   ,COALESCE(TablaDatos."Producto",'')											AS "Producto"
				   ,COALESCE(TablaDatos."Relacion",'')											AS "Relacion"
				FROM "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" AS CTB_itzMovimientoTipo
						ON CTB_itzMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimiento."IdTipoMovimiento"
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" AS CTB_itzCentralizacion
						ON CTB_itzCentralizacion."IdCentralizacion" = CTB_itzMovimiento."IdCentralizacion"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad
					   ON EstadoContabilidad."IdEstadoContabilidad" = CTB_itzMovimiento."Estado"
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Portafolio
					   ON Portafolio."IdPortafolio" = CTB_itzMovimiento."Cuenta"
				LEFT JOIN :v_TablaDatos AS TablaDatos
					   ON TablaDatos."IdMovimiento" = CTB_itzMovimiento."IdMovimiento"
				WHERE
					(CTB_itzMovimiento."IdCentralizacion" 	= :p_IdCentralizacion 							OR :p_IdCentralizacion 	= 0)
				AND (CTB_itzMovimiento."IdEmpresa" 			= :p_IdEmpresa)
				AND (CTB_itzMovimiento."Numero" 			= :p_Numero 									OR :p_Numero 			= 0)
				AND (CTB_itzMovimiento."IdMovimiento" 		= :p_IdMovimiento 								OR :p_IdMovimiento 		= 0)
				AND (CTB_itzMovimiento."IdTipoMovimiento" 	= :p_IdTipoMovimiento 							OR :p_IdTipoMovimiento 	= '')
				AND ((CTB_itzMovimiento."Fecha" 			BETWEEN :p_FechaMovInicio AND :p_FechaMovFinal) OR :p_FechaMovInicio 	= '')
				AND (CTB_itzMovimiento."DocumentoIdentidad" = :p_DocumentoIdentidad 						OR :p_DocumentoIdentidad 				= '        0-0')
				AND (CTB_itzMovimiento."Cuenta" 			= :p_Cuenta 									OR :p_Cuenta 			= 0)
				AND (CTB_itzMovimiento."Ejercicio" 			= :p_Ejercicio 									OR :p_Ejercicio 		= 0)
				AND (CTB_itzMovimiento."Estado" 			= :p_Estado 									OR :p_Estado 			= '')
				AND ((CTB_itzCentralizacion."FechaContable" BETWEEN :p_FechaInicio AND :p_FechaFinal) 		OR :p_FechaInicio 		= '')
				AND :v_CantRegDeci = 
				(
					SELECT COUNT(1)
					FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato
					INNER JOIN :p_DatosFiltro AS p_DatosFiltro
							ON p_DatosFiltro."IdTipoDatoEvento" = CTB_itzMovimientoDato."IdTipoDatoEvento"
						   AND p_DatosFiltro."ValorDecimal" = CTB_itzMovimientoDato."ValorDecimal"
						   AND p_DatosFiltro."TipoDato" = 'D'
					WHERE CTB_itzMovimientoDato."IdMovimiento" = CTB_itzMovimiento."IdMovimiento"
				);	--+ Busca por los filtros de tipo decimal
		END IF;
	END;
END;