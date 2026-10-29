SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.GeneraReporteILS";

CREATE PROCEDURE
-- 
--  Proposito		: Procesa Indices generados
--  Empresa			: Ingeniería ProNet IT SpA.
--  Autor			: HMR
--  Fecha Creacion	: 20/02/2017
--  
"BSCL.Normativa.BD.Servicios::ILS.GeneraReporteILS"(
	--+ Variables de entrada obligatorias
	IN p_IdEmpresa          			BIGINT
	,IN p_IdUsuario           			BIGINT
	,IN p_IdModulo           			NVARCHAR(3)
	,IN p_UsaReglasNegocio				NVARCHAR(1)
	--+ Enumerar Variabes de entrada
	,IN p_IdTipoInforme					BIGINT
	,IN p_IdIndice						BIGINT
	,IN p_IdConcepto					BIGINT
	,IN p_IdItem						BIGINT
	,IN p_FechaInforme 					NVARCHAR(10)
	,IN p_SaldoCuentaContable			TABLE (	"IdCodigoContabilidad" 	NVARCHAR(15),
												"MontoDebe" 			DECIMAL (18,2), 
												"MontoHaber" 			DECIMAL (18,2)) DEFAULT EMPTY
	-- Parametros de Salida
	,OUT p_out_InformacionContador 		INT
	,OUT p_out_InformacionDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	,OUT p_out_AdvertenciasContador 	INT
	,OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	,OUT p_out_ErroresContador 			INT
	,OUT p_out_ErroresDetalle 			TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	

	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(1), "ValorParametro" NVARCHAR(100), "NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "ValorParametro", '' AS  "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "ValorParametro" NVARCHAR(100), "NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;

	DECLARE v_count_operaciones			BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) 
		  INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) 
		  INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj ON 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj ON 
 								    													adv."Codigo" = msj."Codigo";
		
		DROP TABLE #TabSaldoCuentasContables;
		DROP TABLE #TabIndiceCalculado;
		DROP TABLE #TabNivelItem;
		DROP TABLE #TabNivelConcepto;
		DROP TABLE #TabNivelIndice;
		DROP TABLE #TabCodigosEliminacion;
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 					NVARCHAR(40);

		-- DECLARE v_TipoAlerta 						BIGINT := 0;

		DECLARE v_ExisteInformeConfiguracion		  INT := 0;
		DECLARE v_NivelConceptoN					  INT := 0;
		DECLARE v_FechaHoraGeneracion				  NVARCHAR(40) := '';
		DECLARE v_FechaActual 						  NVARCHAR(10) := '';
		DECLARE v_HoraActual						  NVARCHAR(8) := '';
		DECLARE v_ExisteIndiceCalcular				  BIGINT := 0;
		DECLARE v_out_IndicesAdvertenciasContador     BIGINT; 
        DECLARE v_out_IndicesErroresContador          BIGINT;
        DECLARE v_resultadoSPDerechoRetroCompra       DECIMAL(20,4);
        DECLARE v_resultadoSPDerechosMonedaExtranjera DECIMAL(20,4);
        DECLARE v_resultadoSPObligacionRetroVenta     DECIMAL(20,4);
        DECLARE v_resultadoSPSimultaneasDerechos      DECIMAL(20,4);
        DECLARE v_resultadoSPSimultaneasGarantias     DECIMAL(20,4);
        DECLARE v_resultadoSPSimultaneasObligaciones  DECIMAL(20,4);
        
		DECLARE v_out_IndicesAdvertenciasDetalle TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
            DEFAULT
                SELECT
                    '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion"
                FROM DUMMY WHERE 1=2;
                
        DECLARE v_out_IndicesErroresDetalle TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
            DEFAULT
                SELECT
                    '' AS "Codigo",'' AS "Descripcion"
                FROM DUMMY WHERE 1=2;
        
        CALL "BSCL.Normativa.BD.Servicios::ILS.SPSimultaneasObligaciones" (
                p_IdEmpresa                 => :p_IdEmpresa,
                p_IdUsuario                 => :p_IdUsuario,
                p_IdModulo                  => :p_IdModulo,
                p_UsaReglasNegocio          => 'S',
                p_FechaConsulta             => :p_FechaInforme,
                p_Resultado          	    => v_resultadoSPDerechoRetroCompra,
                p_out_AdvertenciasContador  => v_out_IndicesAdvertenciasContador,
                p_out_AdvertenciasDetalle   => v_out_IndicesAdvertenciasDetalle,
                p_out_ErroresContador       => v_out_IndicesErroresContador,
                p_out_ErroresDetalle        => v_out_IndicesErroresDetalle);
        
        CALL "BSCL.Normativa.BD.Servicios::ILS.SPDerechosMonedaExtranjera" (
                p_IdEmpresa                 => :p_IdEmpresa,
                p_IdUsuario                 => :p_IdUsuario,
                p_IdModulo                  => :p_IdModulo,
                p_UsaReglasNegocio          => 'S',
                p_FechaConsulta             => :p_FechaInforme,
                p_Resultado             	=> v_resultadoSPDerechosMonedaExtranjera,
                p_out_AdvertenciasContador  => v_out_IndicesAdvertenciasContador,
                p_out_AdvertenciasDetalle   => v_out_IndicesAdvertenciasDetalle,
                p_out_ErroresContador       => v_out_IndicesErroresContador,
                p_out_ErroresDetalle        => v_out_IndicesErroresDetalle);

        CALL "BSCL.Normativa.BD.Servicios::ILS.SPObligacionRetroVenta" (
                p_IdEmpresa                 => :p_IdEmpresa,
                p_IdUsuario                 => :p_IdUsuario,
                p_IdModulo                  => :p_IdModulo,
                p_UsaReglasNegocio          => 'S',
                p_FechaConsulta             => :p_FechaInforme,
                p_Resultado             	=> v_resultadoSPObligacionRetroVenta,
                p_out_AdvertenciasContador  => v_out_IndicesAdvertenciasContador,
                p_out_AdvertenciasDetalle   => v_out_IndicesAdvertenciasDetalle,
                p_out_ErroresContador       => v_out_IndicesErroresContador,
                p_out_ErroresDetalle        => v_out_IndicesErroresDetalle);
                
        CALL "BSCL.Normativa.BD.Servicios::ILS.SPSimultaneasDerechos" (
                p_IdEmpresa                 => :p_IdEmpresa,
                p_IdUsuario                 => :p_IdUsuario,
                p_IdModulo                  => :p_IdModulo,
                p_UsaReglasNegocio          => 'S',
                p_FechaConsulta             => :p_FechaInforme,
                p_Resultado             	=> v_resultadoSPSimultaneasDerechos,
                p_out_AdvertenciasContador  => v_out_IndicesAdvertenciasContador,
                p_out_AdvertenciasDetalle   => v_out_IndicesAdvertenciasDetalle,
                p_out_ErroresContador       => v_out_IndicesErroresContador,
                p_out_ErroresDetalle        => v_out_IndicesErroresDetalle);
                
        CALL "BSCL.Normativa.BD.Servicios::ILS.SPSimultaneasGarantias" (
                p_IdEmpresa                 => :p_IdEmpresa,
                p_IdUsuario                 => :p_IdUsuario,
                p_IdModulo                  => :p_IdModulo,
                p_UsaReglasNegocio          => 'S',
                p_FechaConsulta             => :p_FechaInforme,
                p_Resultado             	=> v_resultadoSPSimultaneasGarantias,
                p_out_AdvertenciasContador  => v_out_IndicesAdvertenciasContador,
                p_out_AdvertenciasDetalle   => v_out_IndicesAdvertenciasDetalle,
                p_out_ErroresContador       => v_out_IndicesErroresContador,
                p_out_ErroresDetalle        => v_out_IndicesErroresDetalle);
        --*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
			                              '' 	AS "Codigo",
			                              '' 	AS "ValorParametro",
			                              '' 	AS "NombreParametro"
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;

		CREATE LOCAL TEMPORARY TABLE #TabSaldoCuentasContables ("IdCodigoContabilidad" 	NVARCHAR(15),
																"IdCuentaOptimus" 		NVARCHAR(15),
																"MontoDebe" 			DECIMAL (18,2), 
																"MontoHaber" 			DECIMAL (18,2)
														  		);

		CREATE LOCAL TEMPORARY TABLE #TabIndiceCalculado (	"IdEmpresa" INT NULL, 
															"IdTipoInforme" INT NULL, 
															"NombreInforme" NVARCHAR(200) NULL, 
															"OrdenInforme" INT NULL, 
															"IdIndice" INT NULL, 
															"NombreIndice" NVARCHAR(200) NULL, 
															"PosicionIndice" NVARCHAR(1) NULL, 
															"OperadorIndice" INT NULL, 
															"FactorIndice" DECIMAL(5, 3) NULL, 
															"IdConcepto" INT NULL, 
															"NombreConcepto" NVARCHAR(200) NULL, 
															"OperacionConcepto" INT NULL, 
															"FactorConcepto" DECIMAL(5, 3) NULL, 
															"IdItem" INT NULL, 
															"NombreItem" NVARCHAR(200) NULL, 
															"IdTipoItem" NVARCHAR(3) NULL, 
															"CuentaContableItem" NVARCHAR(12) NULL, 
															"SaldoDebeHaberItem" NVARCHAR(1) NULL, 
															"CondicionItem" NVARCHAR(1) NULL, 
															"MontoItem" DECIMAL(20, 4) NULL, 
															"OperacionItem" INT NULL, 
															"FactorItem" DECIMAL(5, 3) NULL, 
															"MayorACeroItem" CHAR(1) NULL, 
															"MenorACeroItem" CHAR(1) NULL, 
															"PermiteCeroItem" CHAR(1) NULL, 
															"MontoContab" DECIMAL(17, 2) NULL, 
															"ProcesoFuncion" NVARCHAR(100) NULL,
															"NombreFuncion" NVARCHAR(50) NULL
														  );

		CREATE LOCAL TEMPORARY TABLE #TabNivelItem (
													"IdTipoInforme" INT NULL, 
													"NombreInforme" NVARCHAR(200) NULL, 
													"OrdenInforme" INT NULL, 
													"IdIndice" INT NULL, 
													"NombreIndice" NVARCHAR(200) NULL, 
													"PosicionIndice" NVARCHAR(1) NULL, 
													"OperadorIndice" INT NULL, 
													"FactorIndice" DECIMAL(5, 3) NULL, 
													"IdConcepto" INT NULL, 
													"NombreConcepto" NVARCHAR(200) NULL, 
													"OperacionConcepto" INT NULL, 
													"FactorConcepto" DECIMAL(5, 3) NULL, 
													"IdItem" INT NULL, 
													"NombreItem" NVARCHAR(200) NULL, 
													"IdTipoItem" NVARCHAR(3) NULL, 
													"Monto" DECIMAL(38, 6) NULL, 
													"CuentaContable" NVARCHAR(12) NULL, 
													"SaldoDebeHaber" NVARCHAR(1) NULL, 
													"Condicion" NVARCHAR(1) NULL, 
													"MontoCondicion" DECIMAL(20, 4) NULL, 
													"OperacionItem" INT NULL, 
													"FactorItem" DECIMAL(5, 3) NULL, 
													"MontoCalculado" DECIMAL(38, 6) NULL, 
													"MayorACero" NVARCHAR(1) NULL, 
													"MenorACero" NVARCHAR(1) NULL, 
													"PermiteCero" NVARCHAR(1) NULL, 
													"ProcesoFuncion" NVARCHAR(100) NULL
													);
	
		CREATE LOCAL TEMPORARY TABLE #TabNivelConcepto (
														"IdTipoInforme" INT NULL,
														"NombreInforme" NVARCHAR(200) NULL,
														"OrdenInforme" INT NULL,
														"IdIndice" INT NULL,
														"NombreIndice" NVARCHAR(200) NULL,
														"PosicionIndice" NVARCHAR(1) NULL,
														"OperadorIndice" INT NULL,
														"FactorIndice" DECIMAL(5, 3) NULL,
														"IdConcepto" INT NULL,
														"NombreConcepto" NVARCHAR(200) NULL,
														"Monto" DECIMAL(38, 6) NULL,
														"MontoCalculado" DECIMAL(38, 6) NULL
													);
		
		CREATE LOCAL TEMPORARY TABLE #TabNivelIndice (
														"IdTipoInforme" INT NULL,
														"NombreInforme" NVARCHAR(200) NULL,
														"OrdenInforme" INT NULL,
														"IdIndice" INT NULL,
														"NombreIndice" NVARCHAR(200) NULL,
														"MontoNumerador" DECIMAL(38, 6) NULL,
														"MontoDenominador" DECIMAL(38, 6) NULL,
														"MontoIndice" DECIMAL(38, 6) NULL
													);
	
		CREATE LOCAL TEMPORARY TABLE #TabCodigosEliminacion (
														"IdTipoInforme" INT NULL,
														"FechaContable" NVARCHAR(10) NULL,
														"IdResultadoIndice" BIGINT NULL,
														"IdResultadoConcepto" BIGINT NULL,
														"IdResultadoItem" BIGINT NULL,
														"IdResultadoItemDetalle" BIGINT NULL
													);

		--Homologar Cuentas Contables con mapeo sobre modelo de ILS - Solicitado así
		INSERT INTO #TabSaldoCuentasContables 
				SELECT :p_SaldoCuentaContable."IdCodigoContabilidad" 	AS "IdCodigoContabilidad",
					   NOR_HomologaCuenta."CuentaOptimus" 				AS "IdCuentaOptimus",
					   :p_SaldoCuentaContable."MontoDebe" 				AS "MontoDebe",
					   :p_SaldoCuentaContable."MontoHaber" 				AS "MontoHaber"
				  FROM :p_SaldoCuentaContable 
				  		LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS NOR_HomologaCuenta ON
				  								NOR_HomologaCuenta."CuentaContabilidad" = :p_SaldoCuentaContable."IdCodigoContabilidad";
		
		SELECT COUNT(1)
		  INTO v_ExisteIndiceCalcular
						FROM "BSCL.Normativa.BD::ILS.InformeTipo" AS NOR_ilsInformeTipo
							INNER JOIN "BSCL.Normativa.BD::ILS.InformeConfiguracion" AS NOR_ilsInformeConfiguracion ON 
												NOR_ilsInformeTipo."IdTipoInforme" 			= NOR_ilsInformeConfiguracion."IdTipoInforme" 
							INNER JOIN "BSCL.Normativa.BD::ILS.Indice" AS NOR_ilsIndice ON 
												NOR_ilsIndice."IdIndice" 					= NOR_ilsInformeConfiguracion."IdIndice" 
										   AND (NOR_ilsIndice."IdIndice" 					= :p_IdIndice OR :p_IdIndice IS NULL OR :p_IdIndice = 0) 
							INNER JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" AS NOR_ilsIndiceConfiguracion ON 
												NOR_ilsIndice."IdIndice" 					= NOR_ilsIndiceConfiguracion."IdIndice" 
							INNER JOIN "BSCL.Normativa.BD::ILS.Concepto" AS NOR_ilsConcepto ON 
												NOR_ilsIndiceConfiguracion."IdConcepto" 	= NOR_ilsConcepto."IdConcepto" 
										   AND (NOR_ilsConcepto."IdConcepto"				= :p_IdConcepto OR :p_IdConcepto IS NULL OR :p_IdConcepto = 0) 
							INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS NOR_ilsConceptoConfiguracion ON 
												NOR_ilsConceptoConfiguracion."IdConcepto" 	= NOR_ilsIndiceConfiguracion."IdConcepto" 
							INNER JOIN "BSCL.Normativa.BD::ILS.Item" AS NOR_ilsItem ON 
												NOR_ilsConceptoConfiguracion."IdItem" 		= NOR_ilsItem."IdItem" 
											AND NOR_ilsItem."Estado" 						= 'VIG' 
							INNER JOIN "BSCL.Normativa.BD::ILS.ItemConfiguracion" AS NOR_ilsItemConfiguracion ON
												NOR_ilsItemConfiguracion."IdItem" 			= NOR_ilsItem."IdItem" 
											AND NOR_ilsItemConfiguracion."Estado" 			= 'VIG' 
											AND (NOR_ilsItem."IdItem" 						= :p_IdItem OR :p_IdItem IS NULL OR :p_IdItem = 0) 
							LEFT OUTER JOIN #TabSaldoCuentasContables ON  --TBCOPLAN
												#TabSaldoCuentasContables."IdCuentaOptimus" = NOR_ilsItemConfiguracion."CuentaContable"
							LEFT OUTER JOIN "BSCL.Normativa.BD::ILS.ProcesoFuncion" AS NOR_ilsProcesoFuncion ON 
												NOR_ilsProcesoFuncion."IdProcesoFuncion" 	= NOR_ilsItemConfiguracion."IdProcesoFuncion" 
					   WHERE NOR_ilsInformeTipo."IdTipoInforme" = :p_IdTipoInforme 
					     AND (NOR_ilsIndice."IdIndice" 			= :p_IdIndice OR :p_IdIndice IS NULL OR :p_IdIndice = 0);
	
		IF :v_ExisteIndiceCalcular > 0 THEN
			INSERT INTO #TabIndiceCalculado
					 SELECT :p_IdEmpresa 																					AS "IdEmpresa", 
							NOR_ilsInformeTipo."IdTipoInforme" 																AS "IdTipoInforme", 
							NOR_ilsInformeTipo."Nombre" 																	AS "NombreInforme", 
							NOR_ilsInformeConfiguracion."Orden" 															AS "OrdenInforme", 
							NOR_ilsIndice."IdIndice" 																		AS "IdIndice", 
							ltrim(rtrim(NOR_ilsIndice."Nombre")) 															AS "NombreIndice", 
							NOR_ilsIndiceConfiguracion."Posicion" 															AS "PosicionIndice", 
							CASE NOR_ilsIndiceConfiguracion."Operacion" 
								WHEN '+' THEN +1 
								WHEN '-' THEN -1 
								ELSE 0 
							END 																							AS "OperadorIndice", 
							NOR_ilsIndiceConfiguracion."Factor" 															AS "FactorIndice", 
							NOR_ilsConcepto."IdConcepto" 																	AS "IdConcepto", 
							ltrim(rtrim(NOR_ilsConcepto."Nombre")) 															AS "NombreConcepto", 
							CASE NOR_ilsConceptoConfiguracion."Operacion" 
								WHEN '+' THEN +1 
								WHEN '-' THEN -1 
								ELSE 0 
							END 																							AS "OperacionConcepto", 
							NOR_ilsConceptoConfiguracion."Factor" 															AS "FactorConcepto", 
							NOR_ilsItem."IdItem" 																			AS "IdItem", 
							NOR_ilsItem."Nombre" 																			AS "NombreItem", 
							NOR_ilsItem."IdTipoItem" 																		AS "IdTipoItem", 
							NOR_ilsItemConfiguracion."CuentaContable" 														AS "CuentaContableItem", 
							NOR_ilsItemConfiguracion."SaldoDebeHaber" 														AS "SaldoDebeHaberItem", 
							NOR_ilsItemConfiguracion."Condicion" 															AS "CondicionItem",
							NOR_ilsItemConfiguracion."Monto" 																AS "MontoItem",
							CASE NOR_ilsItemConfiguracion."Operacion" 
								WHEN '+' THEN +1 
								WHEN '-' THEN -1 
								ELSE 0 
							END 																							AS "OperacionItem",
							NOR_ilsItemConfiguracion."Factor" 																AS "FactorItem",
							NOR_ilsItemConfiguracion."MayorACero" 															AS "MayorACeroItem",
							NOR_ilsItemConfiguracion."MenorACero" 															AS "MenorACeroItem",
							NOR_ilsItemConfiguracion."PermiteCero" 															AS "PermiteCeroItem",
							CASE WHEN NOR_ilsItemConfiguracion."SaldoDebeHaber" = 'D' THEN 
								CASE WHEN (IFNULL(#TabSaldoCuentasContables."MontoDebe", 0) - IFNULL(#TabSaldoCuentasContables."MontoHaber", 0)) > 0 THEN 
									IFNULL(#TabSaldoCuentasContables."MontoDebe", 0) - IFNULL(#TabSaldoCuentasContables."MontoHaber", 0)
								ELSE 
									0 
								END
							ELSE
								CASE WHEN (IFNULL(#TabSaldoCuentasContables."MontoDebe", 0) - IFNULL(#TabSaldoCuentasContables."MontoHaber", 0)) < 0 THEN
									(IFNULL(#TabSaldoCuentasContables."MontoDebe", 0) - IFNULL(#TabSaldoCuentasContables."MontoHaber", 0)) * -1 
								ELSE 
									0 
								END 
							END 																							AS "MontoContab",
							IFNULL(	LTRIM(RTRIM(NOR_ilsProcesoFuncion."IdProcesoFuncion")) || ' - ' || 
									LTRIM(RTRIM(NOR_ilsProcesoFuncion."Nombre")), '') 										AS "ProcesoFuncion",
							LTRIM(RTRIM(NOR_ilsProcesoFuncion."Nombre")) 													AS "NombreFuncion"
						FROM "BSCL.Normativa.BD::ILS.InformeTipo" AS NOR_ilsInformeTipo
							INNER JOIN "BSCL.Normativa.BD::ILS.InformeConfiguracion" AS NOR_ilsInformeConfiguracion ON 
												NOR_ilsInformeTipo."IdTipoInforme" 			= NOR_ilsInformeConfiguracion."IdTipoInforme" 
							INNER JOIN "BSCL.Normativa.BD::ILS.Indice" AS NOR_ilsIndice ON 
												NOR_ilsIndice."IdIndice" 					= NOR_ilsInformeConfiguracion."IdIndice" 
										   AND (NOR_ilsIndice."IdIndice" 					= :p_IdIndice OR :p_IdIndice IS NULL OR :p_IdIndice = 0) 
							INNER JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" AS NOR_ilsIndiceConfiguracion ON 
												NOR_ilsIndice."IdIndice" 					= NOR_ilsIndiceConfiguracion."IdIndice" 
							INNER JOIN "BSCL.Normativa.BD::ILS.Concepto" AS NOR_ilsConcepto ON 
												NOR_ilsIndiceConfiguracion."IdConcepto" 	= NOR_ilsConcepto."IdConcepto" 
										   AND (NOR_ilsConcepto."IdConcepto"				= :p_IdConcepto OR :p_IdConcepto IS NULL OR :p_IdConcepto = 0) 
							INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS NOR_ilsConceptoConfiguracion ON 
												NOR_ilsConceptoConfiguracion."IdConcepto" 	= NOR_ilsIndiceConfiguracion."IdConcepto" 
							INNER JOIN "BSCL.Normativa.BD::ILS.Item" AS NOR_ilsItem ON 
												NOR_ilsConceptoConfiguracion."IdItem" 		= NOR_ilsItem."IdItem" 
											AND NOR_ilsItem."Estado" 						= 'VIG' 
							INNER JOIN "BSCL.Normativa.BD::ILS.ItemConfiguracion" AS NOR_ilsItemConfiguracion ON
												NOR_ilsItemConfiguracion."IdItem" 			= NOR_ilsItem."IdItem" 
											AND NOR_ilsItemConfiguracion."Estado" 			= 'VIG' 
											AND (NOR_ilsItem."IdItem" 						= :p_IdItem OR :p_IdItem IS NULL OR :p_IdItem = 0) 
							LEFT OUTER JOIN #TabSaldoCuentasContables ON  --TBCOPLAN
												#TabSaldoCuentasContables."IdCuentaOptimus" = NOR_ilsItemConfiguracion."CuentaContable"
							LEFT OUTER JOIN "BSCL.Normativa.BD::ILS.ProcesoFuncion" AS NOR_ilsProcesoFuncion ON 
												NOR_ilsProcesoFuncion."IdProcesoFuncion" 	= NOR_ilsItemConfiguracion."IdProcesoFuncion" 
					   WHERE NOR_ilsInformeTipo."IdTipoInforme" = :p_IdTipoInforme 
					     AND (NOR_ilsIndice."IdIndice" 			= :p_IdIndice OR :p_IdIndice IS NULL OR :p_IdIndice = 0) 
					ORDER BY NOR_ilsConcepto."IdConcepto", NOR_ilsItem."IdItem", NOR_ilsItem."IdTipoItem";
		ELSE
	          v_out_ErroresDetalle = 
	          SELECT :p_IdTipoInforme AS "IdInterno", 'NO_EXISTE_INDICE_A_PROCESAR' AS "Codigo", TRIM(CAST(:p_IdTipoInforme AS NVARCHAR(40))) AS "ValorParametro", 'IdTipoInforme' AS "NombreParametro" FROM DUMMY;
	          SIGNAL SALIDA_CONTROLADA;
		END IF;
	
		--Trae Datos de Items Digitados manualmente
		UPDATE #TabIndiceCalculado 
		   SET #TabIndiceCalculado."MontoContab" = NOR_ilsInformeResultadoItemdig."Monto" 
		  FROM #TabIndiceCalculado INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItemDig" AS NOR_ilsInformeResultadoItemdig ON 
		  				#TabIndiceCalculado."IdTipoInforme" 			= NOR_ilsInformeResultadoItemdig."IdTipoInforme" 
		  			AND #TabIndiceCalculado."IdIndice" 					= NOR_ilsInformeResultadoItemdig."IdIndice" 
		  			AND #TabIndiceCalculado."OperadorIndice" 			= NOR_ilsInformeResultadoItemdig."OperadorIndice" 
		  			AND #TabIndiceCalculado."FactorIndice" 				= NOR_ilsInformeResultadoItemdig."FactorIndice" 
		  			AND #TabIndiceCalculado."IdConcepto" 				= NOR_ilsInformeResultadoItemdig."IdConcepto" 
		  			AND #TabIndiceCalculado."OperacionConcepto" 		= NOR_ilsInformeResultadoItemdig."OperacionConcepto" 
		  			AND #TabIndiceCalculado."FactorConcepto" 			= NOR_ilsInformeResultadoItemdig."FactorConcepto" 
		  			AND #TabIndiceCalculado."IdItem" 					= NOR_ilsInformeResultadoItemdig."IdItem" 
		  			AND #TabIndiceCalculado."IdEmpresa" 				= NOR_ilsInformeResultadoItemdig."IdEmpresa" 
		  			AND NOR_ilsInformeResultadoItemdig."FechaContable" 	= :p_FechaInforme;
	
		INSERT INTO #TabNivelItem
				 SELECT "IdTipoInforme",
						"NombreInforme",
						"OrdenInforme",
						"IdIndice",
						"NombreIndice",
						"PosicionIndice",
						"OperadorIndice",
						"FactorIndice",
						"IdConcepto",
						"NombreConcepto",
						"OperacionConcepto",
						"FactorConcepto",
						"IdItem",
						"NombreItem",
						"IdTipoItem",
						"MontoContab",
						"CuentaContableItem",
						"SaldoDebeHaberItem",
						"CondicionItem",
						"MontoItem",
						"OperacionItem",
						"FactorItem",
						CASE "IdTipoItem"
							WHEN 'CTB' THEN
						   		CASE "CondicionItem"
						   			WHEN '>' THEN
							   			CASE WHEN "MontoContab" > "MontoItem" THEN
							   				"MontoContab" * "OperacionItem" * "FactorItem"
							   			ELSE
							   				0
							   			END
							   		WHEN '<' THEN
							   			CASE WHEN "MontoContab" < "MontoItem" THEN
							   				"MontoContab" * "OperacionItem" * "FactorItem"
							   			ELSE
							   				0
							   			END
							   		WHEN '=' THEN
							   			CASE WHEN "MontoContab" = "MontoItem" THEN
							   				"MontoContab" * "OperacionItem" * "FactorItem"
							   			ELSE
							   				0
							   			END
							   	ELSE
							   		0
							   	END
							WHEN 'PRG' THEN
								CASE "NombreFuncion"
									WHEN 'SPDerechoRetroCompra' THEN :v_resultadoSPDerechoRetroCompra
									WHEN 'SPDerechosMonedaExtranjera' THEN :v_resultadoSPDerechosMonedaExtranjera
									WHEN 'SPObligacionRetroVenta' THEN :v_resultadoSPObligacionRetroVenta
									WHEN 'SPSimultaneasDerechos' THEN :v_resultadoSPSimultaneasDerechos
									WHEN 'SPSimultaneasGarantias' THEN :v_resultadoSPSimultaneasGarantias
									WHEN 'SPSimultaneasObligaciones' THEN :v_resultadoSPSimultaneasObligaciones
								ELSE 0
								END 
							ELSE
								"MontoContab"
						END, 
						"MayorACeroItem",
						"MenorACeroItem",
						"PermiteCeroItem",
						"ProcesoFuncion"
				   FROM #TabIndiceCalculado;
				           
		/*
		v_TablaNivelItem = SELECT * FROM #TabNivelItem;
		
		CALL "BSCL.Normativa.BD.Servicios::ILS.GrabaNivelItem" (v_TabNivelItem => :v_TablaNivelItem);
		*/

		INSERT INTO #TabNivelConcepto
				 SELECT "IdTipoInforme", 
				 		"NombreInforme", 
				 		"OrdenInforme", 
				 		"IdIndice", 
				 		"NombreIndice", 
				 		"PosicionIndice", 
				 		"OperadorIndice", 
				 		"FactorIndice", 
				 		"IdConcepto", 
				 		"NombreConcepto", 
				 		SUM("MontoCalculado" * "FactorConcepto" * "OperacionConcepto"), 
				 		SUM("MontoCalculado" * "FactorConcepto" * "OperacionConcepto") 
				   FROM #TabNivelItem 
			   GROUP BY "IdTipoInforme", 
			   			"NombreInforme", 
			   			"OrdenInforme", 
			   			"IdIndice", 
			   			"NombreIndice", 
			   			"PosicionIndice", 
			   			"OperadorIndice", 
			   			"FactorIndice", 
			   			"IdConcepto", 
			   			"NombreConcepto";

		/*
		v_TablaNivelConcepto = SELECT * FROM #TabNivelConcepto;
		CALL "BSCL.Normativa.BD.Servicios::ILS.GrabaNivelConcepto" (v_TabNivelConcepto => :v_TablaNivelConcepto);
		*/
		
		SELECT COUNT(1)
		  INTO v_ExisteInformeConfiguracion
		  FROM "BSCL.Normativa.BD::ILS.InformeConfiguracion" 
		 WHERE "IdTipoInforme" = :p_IdTipoInforme;
	
		SELECT COUNT(1)
		  INTO v_NivelConceptoN
		  FROM #TabNivelConcepto,
		  	   "BSCL.Normativa.BD::ILS.InformeConfiguracion" AS NOR_ilsInformeConfiguracion
		 WHERE #TabNivelConcepto."PosicionIndice" = 'N' 
		   AND #TabNivelConcepto."IdTipoInforme" 	= NOR_ilsInformeConfiguracion."IdTipoInforme" 
		   AND #TabNivelConcepto."IdIndice" 		= NOR_ilsInformeConfiguracion."IdIndice";
	
		IF :v_ExisteInformeConfiguracion > 0 AND :v_NivelConceptoN = 0 THEN
	          v_out_ErroresDetalle = 
	          SELECT :p_IdTipoInforme AS "IdInterno", 'NO_EXISTE_NUMERADOR_EN_CONCEPTO' AS "Codigo", TRIM(CAST(:p_IdTipoInforme AS NVARCHAR(40))) AS "ValorParametro", 'IdTipoInforme' AS "NombreParametro" FROM DUMMY;
	          SIGNAL SALIDA_CONTROLADA;
		END IF;
	
		--Se homologan los conceptos NUMERADORES como DENOMINADORES con monto 1 para evitar division por cero
		INSERT INTO #TabNivelConcepto (
										SELECT DISTINCT "IdTipoInforme" 	AS "IdTipoInforme",
														"NombreInforme" 	AS "NombreInforme",
														"OrdenInforme" 		AS "OrdenInforme",
														"IdIndice" 			AS "IdIndice",
														"NombreIndice" 		AS "NombreIndice",
														'D' 				AS "PosicionIndice",
														1 					AS "OperadorIndice",
														1 					AS "FactorIndice",
														0 					AS "IdConcepto",
														'' 					AS "NombreConcepto",
														1 					AS "Monto",
														1 					AS "MontoCalculado"
												   FROM #TabNivelConcepto AS TMP_NivelConcepto
												  WHERE EXISTS (
												  				SELECT *
												  				  FROM "BSCL.Normativa.BD::ILS.InformeConfiguracion" AS NOR_ilsInformeConfiguracion
												  				 WHERE NOR_ilsInformeConfiguracion."IdTipoInforme" = :p_IdTipoInforme
												  				   AND NOR_ilsInformeConfiguracion."IdTipoInforme" = TMP_NivelConcepto."IdTipoInforme"
												  				   AND NOR_ilsInformeConfiguracion."IdIndice" = TMP_NivelConcepto."IdIndice"
												  				   AND NOT EXISTS  (
												  				   					SELECT 1
												  				   					  FROM #TabNivelConcepto AS TMP_NivelConcepto1
												  				   					 WHERE TMP_NivelConcepto1."PosicionIndice" 	= 'D'
												  				   					   AND TMP_NivelConcepto1."IdTipoInforme" 	= NOR_ilsInformeConfiguracion."IdTipoInforme"
												  				   					   AND TMP_NivelConcepto1."IdIndice" 		= NOR_ilsInformeConfiguracion."IdIndice"
																					)
																)
										);
	
		--Se dejan en 1 todos los conceptos de tipo DENOMINADOR que se encuentren en cero
		UPDATE #TabNivelConcepto
		   SET "Monto" = 1
		 WHERE "PosicionIndice" = 'D'
		   AND "Monto" = 0;
	
		INSERT INTO #TabNivelIndice (
										SELECT "IdTipoInforme",
											   "NombreInforme",
											   "OrdenInforme",
											   "IdIndice",
											   "NombreIndice",
											   SUM	(
											   		CASE WHEN "PosicionIndice" = 'N' THEN
											   			"MontoCalculado"
											   		ELSE
											   			0
											   		END * "FactorIndice" * "OperadorIndice"
											   		) 												AS "MontoNumerador",
											   SUM	(
											   		CASE WHEN "PosicionIndice" = 'D' THEN
											   			"MontoCalculado"
											   		ELSE
											   			1--0
											   		END * "FactorIndice" * "OperadorIndice"
											   		) 												AS "MontoDenominador",
											   0 AS "MontoIndice"
										  FROM #TabNivelConcepto
									  GROUP BY "IdTipoInforme",
									  		   "NombreInforme",
									  		   "OrdenInforme",
									  		   "IdIndice",
									  		   "NombreIndice"
									);

		/*
		v_TablaNivelIndice = SELECT * FROM #TabNivelIndice;
		CALL "BSCL.Normativa.BD.Servicios::ILS.GrabaNivelIndice" (v_TabNivelIndice => :v_TablaNivelIndice);
		*/
------------------------------------------------------------------------------------------------------------------------------------------------------
		--SELECT * FROM #TabNivelIndice;
------------------------------------------------------------------------------------------------------------------------------------------------------
		UPDATE #TabNivelIndice 
		   SET "MontoIndice" = "MontoNumerador" / "MontoDenominador";
	
		INSERT INTO #TabCodigosEliminacion (
											SELECT IFNULL(NOR_ilsInformeResultado."IdTipoInforme", 0) AS "IdTipoInforme",
												   IFNULL(NOR_ilsInformeResultado."FechaContable", '') AS "FechaContable",
												   IFNULL(NOR_ilsInformeResultadoIndice."IdResultadoIndice", 0) AS "IdResultadoIndice",
												   IFNULL(NOR_ilsInformeResultadoConcepto."IdResultadoConcepto", 0) AS "IdResultadoConcepto",
												   IFNULL(NOR_ilsInformeResultadoItem."IdResultadoItem", 0) AS "IdResultadoItem",
												   IFNULL(NOR_ilsInformeResultadoDetalleItem."IdResultadoItemDetalle", 0) AS "IdResultadoDetalleItem"
											  FROM "BSCL.Normativa.BD::ILS.InformeResultado" AS NOR_ilsInformeResultado
														LEFT OUTER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS NOR_ilsInformeResultadoIndice ON
																	NOR_ilsInformeResultado."IdTipoInforme" = NOR_ilsInformeResultadoIndice."IdTipoInforme"
																AND NOR_ilsInformeResultado."FechaContable" = NOR_ilsInformeResultadoIndice."FechaContable"
														LEFT OUTER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS NOR_ilsInformeResultadoConcepto ON
																	NOR_ilsInformeResultadoConcepto."IdResultadoIndice" = NOR_ilsInformeResultadoIndice."IdResultadoIndice"
														LEFT OUTER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItem" AS NOR_ilsInformeResultadoItem ON
																	NOR_ilsInformeResultadoItem."IdResultadoConcepto" = NOR_ilsInformeResultadoConcepto."IdResultadoConcepto"
														LEFT OUTER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItemDetalle" AS NOR_ilsInformeResultadoDetalleItem ON
																	NOR_ilsInformeResultadoDetalleItem."IdResultadoItem" = NOR_ilsInformeResultadoItem."IdResultadoItem"
											 WHERE NOR_ilsInformeResultado."IdTipoInforme" = :p_IdTipoInforme
											   AND NOR_ilsInformeResultado."FechaContable" = :p_FechaInforme
											);
	
		DELETE 
		  FROM "BSCL.Normativa.BD::ILS.InformeResultadoItemDetalle" AS NOR_ilsInformeResultadoDetalleItem 
		 WHERE EXISTS 	(
		 				SELECT * 
		 				  FROM #TabCodigosEliminacion AS TMP_CodigosEliminacion
		 				 WHERE TMP_CodigosEliminacion."IdResultadoItemDetalle" = NOR_ilsInformeResultadoDetalleItem."IdResultadoItemDetalle"
		 				);
	
		DELETE 
		  FROM "BSCL.Normativa.BD::ILS.InformeResultadoItem" AS NOR_ilsInformeResultadoItem 
		 WHERE EXISTS 	(
		 				SELECT * 
		 				  FROM #TabCodigosEliminacion AS TMP_CodigosEliminacion 
		 				 WHERE TMP_CodigosEliminacion."IdResultadoItem" = NOR_ilsInformeResultadoItem."IdResultadoItem"
		 				);
	
		DELETE 
		  FROM "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS NOR_ilsInformeResultadoConcepto 
		 WHERE EXISTS 	(
						SELECT * 
						  FROM #TabCodigosEliminacion AS TMP_CodigosEliminacion 
						 WHERE TMP_CodigosEliminacion."IdResultadoConcepto" = NOR_ilsInformeResultadoConcepto."IdResultadoConcepto"
						);
	
		DELETE 
		  FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS NOR_ilsInformeResultadoIndice 
		 WHERE EXISTS 	(
						SELECT * 
						  FROM #TabCodigosEliminacion AS TMP_CodigosEliminacion 
						 WHERE TMP_CodigosEliminacion."IdResultadoIndice" = NOR_ilsInformeResultadoIndice."IdResultadoIndice"
						);
	
		DELETE 
		FROM "BSCL.Normativa.BD::ILS.InformeResultado" AS NOR_ilsInformeResultado  
		WHERE EXISTS 	( 
						SELECT * 
						  FROM #TabCodigosEliminacion AS TMP_CodigosEliminacion 
						 WHERE TMP_CodigosEliminacion."IdTipoInforme" = NOR_ilsInformeResultado."IdTipoInforme" 
						   AND TMP_CodigosEliminacion."FechaContable" = NOR_ilsInformeResultado."FechaContable"
						);
	
		--+ Obtiene Fecha y hora de Sistema
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa 			=> :p_IdEmpresa,
			p_out_FechaHoraSistema	=> :v_FechaHoraGeneracion
		);
	
		v_FechaActual := SUBSTRING(:v_FechaHoraGeneracion, 1, 10);
		v_HoraActual  := SUBSTRING(:v_FechaHoraGeneracion, 12, 8);
	
		INSERT
		  INTO "BSCL.Normativa.BD::ILS.InformeResultado" 
		VALUES (:p_IdTipoInforme, :p_FechaInforme, :v_FechaActual, :v_HoraActual, 'VIG');
		
		INSERT
		  INTO "BSCL.Normativa.BD::ILS.InformeResultadoIndice" (SELECT "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoIndice".NEXTVAL AS "IdResultadoIndice",
		  															   "IdTipoInforme" 					AS "IdTipoInforme",
		  															   :p_FechaInforme 					AS "FechaContable",
		  															   "MontoIndice" 					AS "Monto",
		  															   "IdIndice" 						AS "IdIndice",
		  															   "NombreIndice" 					AS "NombreIndice",
		  															   'VIG'							AS "Estado"
		  														  FROM #TabNivelIndice
		  														);
	
		INSERT 
		  INTO "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" (
		  											SELECT "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoConcepto".NEXTVAL AS "IdResultadoConcepto",
		  												   NOR_ilsInformeResultadoIndice."IdResultadoIndice" 	AS "IdResultadoIndice",
		  												   TMP_NivelConcepto."Monto" 							AS "Monto",
		  												   TMP_NivelConcepto."NombreConcepto" 					AS "NombreConcepto",
		  												   TMP_NivelConcepto."IdConcepto" 						AS "IdConcepto",
		  												   TMP_NivelConcepto."PosicionIndice" 					AS "Posicion",
		  												   CASE TMP_NivelConcepto."OperadorIndice"
		  												   		WHEN 1 THEN
		  												   			'+'
		  												   		WHEN -1 THEN
		  												   			'-'
		  												   END 													AS "Operador",
		  												   TMP_NivelConcepto."FactorIndice" 					AS "Factor",
		  												   TMP_NivelConcepto."MontoCalculado" 					AS "MontoCalculado",
		  												   'VIG'												AS "Estado"
		  											  FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS NOR_ilsInformeResultadoIndice 
		  											  		INNER JOIN #TabNivelConcepto AS TMP_NivelConcepto ON 
		  											  				NOR_ilsInformeResultadoIndice."IdTipoInforme" 	= TMP_NivelConcepto."IdTipoInforme" 
		  											  			AND NOR_ilsInformeResultadoIndice."IdIndice" 		= TMP_NivelConcepto."IdIndice" 
		  											  			AND NOR_ilsInformeResultadoIndice."FechaContable" 	= :p_FechaInforme 
		  											 WHERE TMP_NivelConcepto."IdConcepto" <> 0
		  											 );
	
		INSERT INTO "BSCL.Normativa.BD::ILS.InformeResultadoItem"	(
													SELECT "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoItem".NEXTVAL AS "IdResultadoItem",
														   NOR_ilsInformeResultadoConcepto."IdResultadoConcepto", 
														   TMP_NivelItem."IdTipoItem", 
														   TMP_NivelItem."IdItem", 
														   TMP_NivelItem."NombreItem", 
														   SUM(TMP_NivelItem."MontoCalculado") AS "Monto", 
														   CASE TMP_NivelItem."OperacionConcepto" 
														   		WHEN 1 THEN 
														   			'+' 
														   		WHEN -1 THEN 
														   			'-' 
														   		ELSE 
														   			''
														   END AS "Operacion", 
														   TMP_NivelItem."FactorConcepto" AS "Factor", 
														   SUM(TMP_NivelItem."MontoCalculado") * TMP_NivelItem."FactorConcepto" * TMP_NivelItem."OperacionConcepto" AS "MontoCalculado", 
														   TMP_NivelItem."PosicionIndice" 
													  FROM "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS NOR_ilsInformeResultadoConcepto
													  		INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS NOR_ilsInformeResultadoIndice ON 
													  				NOR_ilsInformeResultadoConcepto."IdResultadoIndice" = NOR_ilsInformeResultadoIndice."IdResultadoIndice" 
													  		INNER JOIN #TabNivelItem AS TMP_NivelItem ON 
													  				NOR_ilsInformeResultadoConcepto."IdConcepto" = TMP_NivelItem."IdConcepto" 
													  			AND NOR_ilsInformeResultadoConcepto."Posicion" = TMP_NivelItem."PosicionIndice" 
													 WHERE NOR_ilsInformeResultadoIndice."FechaContable" = :p_FechaInforme 
													   AND NOR_ilsInformeResultadoIndice."IdTipoInforme" = :p_IdTipoInforme 
												  GROUP BY NOR_ilsInformeResultadoConcepto."IdResultadoConcepto", 
												  		   TMP_NivelItem."IdTipoItem", 
												  		   TMP_NivelItem."IdItem", 
												  		   TMP_NivelItem."NombreItem", 
												  		   TMP_NivelItem."OperacionConcepto", 
												  		   TMP_NivelItem."FactorConcepto", 
												  		   TMP_NivelItem."PosicionIndice"
													);
	
		INSERT INTO "BSCL.Normativa.BD::ILS.InformeResultadoItemDetalle"	(
												SELECT "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoItemDetalle".NEXTVAL AS "IdResultadoItemDetalle",
													   NOR_ilsInformeResultadoItem."IdResultadoItem",
													   TMP_NivelItem."IdTipoItem", 
													   TMP_NivelItem."Monto", 
													   TMP_NivelItem."IdItem", 
													   TMP_NivelItem."NombreItem",
													   TMP_NivelItem."CuentaContable",
													   TMP_NivelItem."SaldoDebeHaber",
													   TMP_NivelItem."Condicion",
													   TMP_NivelItem."MontoCondicion",
													   CASE TMP_NivelItem."OperacionItem" 
													   		WHEN 1 THEN 
													   			'+' 
													   		WHEN -1 THEN 
													   			'-' 
													   		ELSE 
													   			'' 
													   END AS "Operacion", 
													   TMP_NivelItem."FactorItem",
													   TMP_NivelItem."MontoCalculado",
													   TMP_NivelItem."PosicionIndice",
													   TMP_NivelItem."MayorACero",
													   TMP_NivelItem."MenorACero",
													   TMP_NivelItem."PermiteCero",
													   TMP_NivelItem."ProcesoFuncion"
												  FROM "BSCL.Normativa.BD::ILS.InformeResultadoItem" AS NOR_ilsInformeResultadoItem
												  		INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS NOR_ilsInformeResultadoConcepto ON 
												  				NOR_ilsInformeResultadoConcepto."IdResultadoConcepto" = NOR_ilsInformeResultadoItem."IdResultadoConcepto" 
												  		INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS NOR_ilsInformeResultadoIndice ON 
												  				NOR_ilsInformeResultadoConcepto."IdResultadoIndice" = NOR_ilsInformeResultadoIndice."IdResultadoIndice" 
												  		INNER JOIN #TabNivelItem AS TMP_NivelItem ON 
												  				NOR_ilsInformeResultadoItem."IdItem" = TMP_NivelItem."IdItem" 
												  			AND NOR_ilsInformeResultadoItem."Posicion" = TMP_NivelItem."PosicionIndice" 
												 WHERE NOR_ilsInformeResultadoIndice."FechaContable" = :p_FechaInforme
												   AND NOR_ilsInformeResultadoIndice."IdTipoInforme" = :p_IdTipoInforme
										      GROUP BY NOR_ilsInformeResultadoItem."IdResultadoItem", 
										      		   TMP_NivelItem."IdTipoItem",
										      		   TMP_NivelItem."Monto",
										      		   TMP_NivelItem."IdItem",
										      		   TMP_NivelItem."NombreItem",
										      		   TMP_NivelItem."CuentaContable",
										      		   TMP_NivelItem."SaldoDebeHaber", 
										      		   TMP_NivelItem."Condicion", 
										      		   TMP_NivelItem."MontoCondicion", 
										      		   TMP_NivelItem."OperacionItem",
										      		   TMP_NivelItem."FactorItem",
										      		   TMP_NivelItem."MontoCalculado",
										      		   TMP_NivelItem."PosicionIndice",
										      		   TMP_NivelItem."MayorACero",
										      		   TMP_NivelItem."MenorACero",
										      		   TMP_NivelItem."PermiteCero",
										      		   TMP_NivelItem."ProcesoFuncion"
										      	);
	END;
	
	DROP TABLE #TabSaldoCuentasContables;
	DROP TABLE #TabIndiceCalculado;
	DROP TABLE #TabNivelItem;
	DROP TABLE #TabNivelConcepto;
	DROP TABLE #TabNivelIndice;
	DROP TABLE #TabCodigosEliminacion;

END;
