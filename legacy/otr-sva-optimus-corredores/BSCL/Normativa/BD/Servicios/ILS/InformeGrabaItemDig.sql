SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.InformeGrabaItemDig";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.InformeGrabaItemDig" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
	-- Parametros entrada
	IN p_FechaContable		NVARCHAR(10),
	IN p_TabItemDigitado	TABLE ( "IdTipoInforme"	BIGINT,
									"IdItem" 		INTEGER, 
									"Monto" 		DECIMAL (20,4)) DEFAULT EMPTY,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000))DEFAULT EMPTY,	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))DEFAULT EMPTY
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);
	DECLARE SALIDA_CONTROLADA CONDITION;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1)
		  INTO p_out_ErroresContador
		  FROM :p_out_ErroresDetalle;

		SELECT COUNT(1)
		  INTO p_out_AdvertenciasContador
		  FROM :p_out_AdvertenciasDetalle;
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);

		IF ::SQL_ERROR_CODE > 10000 THEN
			-- Error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::SQL_ERROR_CODE, v_errorExt);

			p_out_ErroresDetalle = SELECT *
									 FROM :p_out_ErroresDetalle
									UNION ALL
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE
								     FROM DUMMY;
								     
			SELECT COUNT(1)
			  INTO p_out_ErroresContador
			  FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;

	BEGIN
		--------------------
		-- Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador, v_ExisteGuion INT;
	    DECLARE v_FechaContable				NVARCHAR(10);
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(520));	
		DECLARE v_TabItemDigitado TABLE (
		    "IdTipoInforme" BIGINT,
		    "IdItem"        INTEGER,
		    "Monto"         DECIMAL(20,4)
		);
		
		v_TabItemDigitado = SELECT   "IdTipoInforme",
		                             "IdItem",
		                             SUM("Monto") AS "Monto"
		                     FROM    :p_TabItemDigitado
		                     GROUP BY "IdTipoInforme", "IdItem";
		--------------------
		--Lógica de Negocio 
		--------------------
		BEGIN
			-- Prepara fecha para grabar
			SELECT INSTR(SUBSTRING(:p_FechaContable, 3, 1), '-') 
			  INTO v_ExisteGuion
			  FROM DUMMY;

			IF :v_ExisteGuion > 0 THEN
				SELECT TO_NVARCHAR(TO_DATE(:p_FechaContable, 'dd-mm-yyyy'), 'yyyy-mm-dd') 
				  INTO v_FechaContable
				from DUMMY;
			ELSE
				SELECT TO_NVARCHAR(TO_DATE(:p_FechaContable, 'yyyy-mm-dd'), 'yyyy-mm-dd') 
				  INTO v_FechaContable
				  FROM DUMMY;
			END IF;
		
			-- Elimina movimientos digitados previamente
			DELETE 
			  FROM "BSCL.Normativa.BD::ILS.InformeResultadoItemDig" AS InformeResultadoItemDig
			 WHERE EXISTS  (SELECT * 
			 				  FROM :v_TabItemDigitado
			 				 WHERE InformeResultadoItemDig."IdTipoInforme" 	= :v_TabItemDigitado."IdTipoInforme"
							   --AND InformeResultadoItemDig."IdItem" 		= :p_TabItemDigitado."IdItem"
						   	   AND InformeResultadoItemDig."FechaContable" 	= :v_FechaContable
						   	   AND InformeResultadoItemDig."IdEmpresa" 		= :p_IdEmpresa
							);

			INSERT INTO "BSCL.Normativa.BD::ILS.InformeResultadoItemDig" (
					SELECT NOR_ilsInformeTipo."IdTipoInforme" 			AS "IdTipoInforme",
						   NOR_ilsInformeTipo."Nombre" 					AS "NombreInforme",
						   NOR_ilsInformeConfiguracion."Orden" 			AS "OrdenInforme",
						   :p_IdEmpresa 								AS "IdEmpresa",
						   :v_FechaContable 							AS "FechaContable",
						   NOR_ILSINDICE."IdIndice" 					AS "IdIndice",
						   ltrim(rtrim(NOR_ILSINDICE."Nombre")) 		AS "NombreIndice",
						   NOR_ilsIndiceConfiguracion."Posicion" 		AS "PosicionIndice",
						   CASE NOR_ilsIndiceConfiguracion."Operacion"
						   		WHEN '+' THEN +1
						   		WHEN '-' THEN -1
						   		ELSE 0
						   END 											AS "OperadorIndice",
						   NOR_ilsIndiceConfiguracion."Factor" 			AS "FactorIndice",
						   NOR_ILSCONCEPTO."IdConcepto" 				AS "IdConcepto",
						   NOR_ILSCONCEPTOGRUPOCONFIGURACION."IdConceptoGrupo" 			AS "IdConceptoGrupo",
						   ltrim(rtrim(NOR_ILSCONCEPTO."Nombre")) 		AS "NombreConcepto",
						   CASE NOR_ilsConceptoConfiguracion."Operacion"
						   		WHEN '+' THEN +1
						   		WHEN '-' THEN -1
						   		ELSE 0
						   END 											AS "OperacionConcepto",
						   NOR_ilsConceptoConfiguracion."Factor" 		AS "FactorConcepto",
						   NOR_ILSITEM."IdItem" 						AS "IdItem",
						   NOR_ILSITEM."Nombre" 						AS "NombreItem",
						   NOR_ILSITEM."IdTipoItem" 					AS "IdTipoItem",
						   NOR_ilsItemConfiguracion."MayorACero" 		AS "MayorACeroItem",
						   NOR_ilsItemConfiguracion."MenorACero" 		AS "MenorACeroItem",
						   NOR_ilsItemConfiguracion."PermiteCero" 		AS "PermiteCeroItem",
						   IFNULL(:v_TabItemDigitado ."Monto", 0)*1000	AS "Monto"
					  FROM "BSCL.Normativa.BD::ILS.InformeTipo" AS NOR_ilsInformeTipo
					  			INNER JOIN "BSCL.Normativa.BD::ILS.InformeConfiguracion" AS NOR_ilsInformeConfiguracion ON
					  					NOR_ilsInformeTipo."IdTipoInforme" = NOR_ilsInformeConfiguracion."IdTipoInforme"
					  			INNER JOIN "BSCL.Normativa.BD::ILS.Indice" AS NOR_ILSINDICE ON
					  					NOR_ILSINDICE."IdIndice" = NOR_ilsInformeConfiguracion."IdIndice"
					  			INNER JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" AS NOR_ILSINDICECONFIGURACION ON
					  					NOR_ILSINDICE."IdIndice" = NOR_ILSINDICECONFIGURACION."IdIndice"
					  			INNER JOIN "BSCL.Normativa.BD::ILS.Concepto" AS NOR_ILSCONCEPTO ON
					  					NOR_ILSINDICECONFIGURACION."IdConcepto" = NOR_ILSCONCEPTO."IdConcepto"
					  					
					  			INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS NOR_ILSCONCEPTOGRUPOCONFIGURACION ON
					  					NOR_ILSCONCEPTOGRUPOCONFIGURACION."IdConcepto" = NOR_ILSCONCEPTO."IdConcepto"
					  					
					  			INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS NOR_ILSCONCEPTOCONFIGURACION ON
					  					NOR_ILSCONCEPTOGRUPOCONFIGURACION."IdConceptoGrupo" = NOR_ILSCONCEPTOCONFIGURACION."IdConceptoGrupo" 
					  					AND NOR_ILSCONCEPTOGRUPOCONFIGURACION."IdConcepto" = NOR_ILSCONCEPTOCONFIGURACION."IdConcepto" 
					  					
					  			INNER JOIN "BSCL.Normativa.BD::ILS.Item" AS NOR_ILSITEM ON
					  					NOR_ILSCONCEPTOCONFIGURACION."IdItem" = NOR_ILSITEM."IdItem"
					  				AND NOR_ILSITEM."Estado" = 'VIG' AND NOR_ILSITEM."IdTipoItem" = 'DIG'
					  			INNER JOIN (
								    SELECT "IdItem", "Estado", "TipoItem",
								           "MayorACero", "MenorACero", "PermiteCero"
								      FROM (
								            SELECT *, ROW_NUMBER() OVER (PARTITION BY "IdItem" 
								                                             ORDER BY "IdConfiguracionItem" DESC) AS rn
								              FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion"
								             WHERE "Estado" = 'VIG' AND "TipoItem" = 'D'
								           )
								     WHERE rn = 1
								) AS NOR_ILSITEMCONFIGURACION ON
								    NOR_ILSITEMCONFIGURACION."IdItem" = NOR_ILSITEM."IdItem"
					  			INNER JOIN :v_TabItemDigitado  ON
					  					NOR_ILSITEM."IdItem" = :v_TabItemDigitado ."IdItem"
					 WHERE NOR_ilsInformeTipo."IdTipoInforme" 	= :v_TabItemDigitado ."IdTipoInforme"
					   AND NOR_ilsItem."IdItem" 				= :v_TabItemDigitado ."IdItem"
					   AND NOR_ilsInformeTipo."IdEmpresa" 		= :p_IdEmpresa
				 	);
  		END;
	END;
END;