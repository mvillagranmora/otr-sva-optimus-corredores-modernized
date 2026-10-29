SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.InformeResultadoBuscar";

CREATE PROCEDURE 
-- 
--  Proposito		: Retorna Indices calculados
--  Empresa			: Ingeniería ProNet IT SpA.
--  Autor			: HMR
--  Fecha Creacion	: 20/02/2017
--  
"BSCL.Normativa.BD.Servicios::ILS.InformeResultadoBuscar" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
	-- Parametros entrada
	IN p_IdTipoInforme		BIGINT,
	IN p_FechaContable		NVARCHAR(10),
	IN p_FechaEmision		NVARCHAR(10),
	OUT p_tablaResultadoInforme TABLE(
									 "IdTipoInforme"        	 BIGINT,
									 "FechaContable" 	    	 NVARCHAR(10),
									 "FechaEmision" 	    	 NVARCHAR(10),
									 "HoraEmision" 	        	 NVARCHAR(8),
									 "EstadoInforme" 	    	 NVARCHAR(3),
									 "IdResultadoIndice"    	 BIGINT,
									 "IdIndice"        	    	 BIGINT,
									 "NombreIndice" 	    	 NVARCHAR(200),
									 "MontoIndice"   	    	 DECIMAL(20,4),
									 "EstadoIndice" 	    	 NVARCHAR(3),
									 "IdResultadoIndiceAnterior" BIGINT,
									 "FechaContableAnterior" 	 NVARCHAR(10),
									 "MontoAnterior" 	         DECIMAL(20,4),
									 "IdIndiceAnterior" 	     BIGINT,
									 "NombreIndiceAnterior" 	 NVARCHAR(200),
									 "EstadoAnterior" 	         NVARCHAR(3),
									 "Tipo"						 NVARCHAR(1)
									 )
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
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	

		--------------------
		--Validaciones
		--------------------

		--------------------
		--Reglas Negocio
		--------------------

		--------------------
		--Lógica de Negocio 
		--------------------

		BEGIN
			p_tablaResultadoInforme  = 	SELECT  TMP_ResultadoIndiceActual."IdTipoInforme",
												TMP_ResultadoIndiceActual."FechaContable",
												TMP_ResultadoIndiceActual."FechaEmision",
												TMP_ResultadoIndiceActual."HoraEmision",
												TMP_ResultadoIndiceActual."EstadoInforme",
												TMP_ResultadoIndiceActual."IdResultadoIndice",
												TMP_ResultadoIndiceActual."IdIndice",
												TMP_ResultadoIndiceActual."NombreIndice",
												TMP_ResultadoIndiceActual."MontoIndice",
												TMP_ResultadoIndiceActual."EstadoIndice",
												COALESCE(InformeResultadoIndice."IdResultadoIndice", 0) AS "IdResultadoIndiceAnterior",
												COALESCE(InformeResultadoIndice."FechaContable", '1900-01-01') AS "FechaContableAnterior",
												COALESCE(InformeResultadoIndice."Monto",0) AS "MontoAnterior",
												COALESCE(InformeResultadoIndice."IdIndice",0) AS "IdIndiceAnterior",
												COALESCE(InformeResultadoIndice."NombreIndice", '') AS "NombreIndiceAnterior",
												COALESCE(InformeResultadoIndice."Estado", '') AS "EstadoAnterior",
												Indice."Tipo"								AS "Tipo"
										   FROM (SELECT InformeResultado."IdTipoInforme",
														InformeResultado."FechaContable",
														InformeResultado."FechaEmision",
														InformeResultado."HoraEmision",
														InformeResultado."Estado" AS "EstadoInforme",
														InformeResultadoIndice."IdResultadoIndice",
														InformeResultadoIndice."Monto" AS "MontoIndice",
														InformeResultadoIndice."IdIndice",
														InformeResultadoIndice."NombreIndice",
														InformeResultadoIndice."Estado" AS "EstadoIndice"
												   FROM "BSCL.Normativa.BD::ILS.InformeTipo" AS InformeTipo
																	INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultado" AS InformeResultado ON
																					InformeTipo."IdTipoInforme" = InformeResultado."IdTipoInforme"
																	LEFT JOIN "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS InformeResultadoIndice	ON
																					InformeResultado."IdTipoInforme" = InformeResultadoIndice."IdTipoInforme"
																				AND InformeResultado."FechaContable" = InformeResultadoIndice."FechaContable"
												  WHERE InformeResultado."Estado" = 'VIG'
													AND (InformeResultado."IdTipoInforme" = :p_IdTipoInforme OR :p_IdTipoInforme = 0)
													AND (InformeResultado."FechaContable" = :p_FechaContable OR :p_FechaContable = '')
													AND (InformeResultado."FechaEmision" = :p_FechaEmision OR :p_FechaEmision = '')
													AND InformeTipo."IdEmpresa" = :p_IdEmpresa
												) AS TMP_ResultadoIndiceActual 
												LEFT JOIN "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS InformeResultadoIndice ON 
																TMP_ResultadoIndiceActual."IdTipoInforme" = InformeResultadoIndice."IdTipoInforme" 
															AND TMP_ResultadoIndiceActual."IdIndice" = InformeResultadoIndice."IdIndice"
															AND InformeResultadoIndice."FechaContable" = (SELECT MAX(NOR_ilsInformeResultadoindiceSUB."FechaContable")
																											FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS NOR_ilsInformeResultadoindiceSUB  
																										   WHERE TMP_ResultadoIndiceActual."IdTipoInforme" = NOR_ilsInformeResultadoindiceSUB."IdTipoInforme"
																										     AND TMP_ResultadoIndiceActual."FechaContable" > NOR_ilsInformeResultadoindiceSUB."FechaContable"
																											 AND TMP_ResultadoIndiceActual."IdIndice" = NOR_ilsInformeResultadoindiceSUB."IdIndice"
																										 )
										LEFT JOIN "BSCL.Normativa.BD::ILS.Indice" Indice ON
										TMP_ResultadoIndiceActual."IdIndice" = Indice."IdIndice"  
																										 
									   ORDER BY TMP_ResultadoIndiceActual."IdIndice";
		
		END;
	END;
END;
