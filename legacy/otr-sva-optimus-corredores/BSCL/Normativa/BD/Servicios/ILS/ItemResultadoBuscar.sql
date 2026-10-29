SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ItemResultadoBuscar";

CREATE PROCEDURE
--
--  Proposito		: Retorna Items calculados
--  Empresa			: Ingeniería ProNet IT SpA.
--  Autor			: HMR
--  Fecha Creacion	: 20/02/2017
--
"BSCL.Normativa.BD.Servicios::ILS.ItemResultadoBuscar" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
	-- Parametros entrada
	IN p_IdTipoInforme		BIGINT,
	IN p_FechaContable		NVARCHAR(10),
	OUT p_tablaResultadoGC TABLE(
									 "IdResultadoIndice"    	 BIGINT,
									 "IdResultadoConcepto"    	 BIGINT,
									 "IdIndice"         	   	 BIGINT,
									 "NombreIndice" 	    	 NVARCHAR(50),
									 "IdNombreIndice" 	    	 NVARCHAR(60),
									 "IdConcepto"        	   	 BIGINT,
									 "PositivoNegativo"			 NVARCHAR(1),
									 "NombreConcepto"        	 NVARCHAR(200),
									 "IdNombreConcepto"        	 NVARCHAR(200), 
									 "IdConceptoGrupo"         	 BIGINT,
									 "NombreConceptoGrupo"    	 NVARCHAR(1000),
									 "IdNombreConceptoGrupo"     NVARCHAR(1000),
									 "Monto"        	         DECIMAL(20,4),
									 "Operacion"      	    	 NVARCHAR(50),
									 "Factor"        	    	 NVARCHAR(50),
									 "MontoCalculado"         	 DECIMAL(20,4),
 									 "Posicion"                  NVARCHAR(50),
									 "MontoContable"             DECIMAL(20,4),
									 "MontoDigitado"             DECIMAL(20,4),
									 "MontoProgramado"           DECIMAL(20,4)
									 ),
	OUT p_tablaResultadoItem TABLE(
									 "IdResultadoIndice"    	 BIGINT,
									 "IdResultadoConcepto"    	 BIGINT,
									 "IdIndice"         	   	 BIGINT,
									 "NombreIndice" 	    	 NVARCHAR(50),
									 "IdNombreIndice" 	    	 NVARCHAR(60),
									 "IdConcepto"        	   	 BIGINT,
									 "PositivoNegativo"			 NVARCHAR(1),
									 "NombreConcepto"        	 NVARCHAR(200),
									 "IdNombreConcepto"        	 NVARCHAR(200), 
									 "IdConceptoGrupo"         	 BIGINT,
									 "NombreConceptoGrupo"    	 NVARCHAR(1000),
									 "IdNombreConceptoGrupo"     NVARCHAR(1000),
									 "IdItem"	         	   	 BIGINT,
									 "NombreItem"		    	 NVARCHAR(200),
									 "IdNombreItem"			     NVARCHAR(222),
									 "Operacion"      	    	 NVARCHAR(50),
									 "Factor"        	    	 NVARCHAR(50),
									 "MontoCalculado"         	 DECIMAL(20,4),
 									 "Posicion"                  NVARCHAR(50)
									 ),
	-- Parametros de Salida
	OUT p_out_InformacionContador 		INT,
	OUT p_out_InformacionDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
 --L�gica de Negocio
		--------------------
		BEGIN
			p_tablaResultadoGC = SELECT InformeResultadoIndice."IdResultadoIndice"           AS "IdResultadoIndice"
			                             ,InformeResultadoConcepto."IdResultadoConcepto"	   AS "IdResultadoConcepto"
			                             ,InformeResultadoIndice."IdIndice"					   AS "IdIndice"
			                             ,InformeResultadoIndice."NombreIndice"			 	   AS "NombreIndice"
			                             ,InformeResultadoIndice."IdIndice" || '-' || InformeResultadoIndice."NombreIndice" AS "IdNombreIndice"
			                             ,InformeResultadoConcepto."IdConcepto"		 		   AS "IdConcepto"
			                             ,InformeResultadoItem."PositivoNegativo" AS "PositivoNegativo"
			                             ,InformeResultadoConcepto."NombreConcepto"	 		   AS "NombreConcepto"
			                             ,InformeResultadoConcepto."IdConcepto" || '-' || InformeResultadoConcepto."NombreConcepto" AS "IdNombreConcepto"
			                             ,ConcGrupConf."IdConceptoGrupo"                       AS "IdConceptoGrupo"
			                             ,ConcGrupConf."Nombre"            					   AS "NombreConceptoGrupo"
			                             ,ConcGrupConf."IdConceptoGrupo" || '-' || ConcGrupConf."Nombre" AS "IdNombreConceptoGrupo"
			                             ,Sum(InformeResultadoItem."Monto")                    AS "Monto"
			                             ,Max(ConcGrup."Operacion")                AS "Operacion"
			                             ,Max(ConcGrupConf."Factor")                   AS "Factor"
			                             ,Sum(
			                             CASE InformeResultadoItem."Operacion"
			                             	WHEN '-' THEN (InformeResultadoItem."MontoCalculado" *(-1) * ConcGrupConf."Factor")/1000
			                             	ELSE (InformeResultadoItem."MontoCalculado" * ConcGrupConf."Factor")/1000
			                             END)												AS "MontoCalculado"
			                             ,CASE InformeResultadoItem."Posicion"
			                                WHEN 'N' THEN	'Numerador'
			                                WHEN 'D' THEN	'Denominador'
			                              ELSE	''
			                              END                                                  AS "Posicion" 
										 ,Sum(InformeResultadoItem."MontoContable")           AS "MontoContable"
										 ,Sum(InformeResultadoItem."MontoDigitado")           AS "MontoDigitado"
										 ,Sum(InformeResultadoItem."MontoProgramado")           AS "MontoProgramado"
			                       FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS InformeResultadoIndice
			                         INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS InformeResultadoConcepto
			                            ON InformeResultadoIndice."IdResultadoIndice" = InformeResultadoConcepto."IdResultadoIndice"
			                           AND InformeResultadoConcepto."IdResultadoIndice" = InformeResultadoIndice."IdResultadoIndice" 
			                           AND InformeResultadoIndice."IdTipoInforme" = :p_IdTipoInforme
			                           AND InformeResultadoIndice."FechaContable" = :p_FechaContable
			                         INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItem" as InformeResultadoItem
			                            ON InformeResultadoItem."IdResultadoConcepto" = InformeResultadoConcepto."IdResultadoConcepto"
			                           AND InformeResultadoItem."IdTipoInforme" = InformeResultadoIndice."IdTipoInforme"
			                           AND InformeResultadoItem."IdConcepto" = InformeResultadoConcepto."IdConcepto"
			                           AND InformeResultadoItem."IdTipoInforme" = InformeResultadoConcepto."IdTipoInforme"
			                         INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConcGrupConf
			                            ON ConcGrupConf."IdConcepto" = InformeResultadoConcepto."IdConcepto"	
			                           AND ConcGrupConf."IdConceptoGrupo" = InformeResultadoItem."IdConceptoGrupo"
			                         LEFT JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS ConcGrup
			                         	ON ConcGrup."IdConcepto" = InformeResultadoConcepto."IdConcepto"	
			                           AND ConcGrup."IdConceptoGrupo" = InformeResultadoItem."IdConceptoGrupo"
			                           AND ConcGrup."IdItem" = InformeResultadoItem."IdItem"
			                       WHERE InformeResultadoConcepto."Estado" = 'VIG'
			                       GROUP BY InformeResultadoIndice."IdResultadoIndice", InformeResultadoConcepto."IdResultadoConcepto", InformeResultadoIndice."IdIndice", InformeResultadoIndice."NombreIndice", 
			                                InformeResultadoConcepto."IdConcepto", InformeResultadoItem."PositivoNegativo", InformeResultadoConcepto."NombreConcepto", ConcGrupConf."IdConceptoGrupo",ConcGrupConf."Nombre", InformeResultadoItem."Posicion"
			                       ORDER BY InformeResultadoIndice."IdIndice", InformeResultadoConcepto."IdConcepto", ConcGrupConf."IdConceptoGrupo" ASC;
			                       
			p_tablaResultadoItem = SELECT InformeResultadoIndice."IdResultadoIndice"           AS "IdResultadoIndice"
			                             ,InformeResultadoConcepto."IdResultadoConcepto"	   AS "IdResultadoConcepto"
			                             ,InformeResultadoIndice."IdIndice"					   AS "IdIndice"
			                             ,InformeResultadoIndice."NombreIndice"			 	   AS "NombreIndice"
			                             ,InformeResultadoIndice."IdIndice" || '-' || InformeResultadoIndice."NombreIndice" AS "IdNombreIndice"
			                             ,InformeResultadoConcepto."IdConcepto"		 		   AS "IdConcepto"
			                             ,InformeResultadoItem."PositivoNegativo" 				AS "PositivoNegativo"
			                             ,InformeResultadoConcepto."NombreConcepto"	 		   AS "NombreConcepto"
			                             ,InformeResultadoConcepto."IdConcepto" || '-' || InformeResultadoConcepto."NombreConcepto" AS "IdNombreConcepto"
			                             ,ConcGrupConf."IdConceptoGrupo"                       AS "IdConceptoGrupo"
			                             ,ConcGrupConf."Nombre"            					   AS "NombreConceptoGrupo"
			                             ,ConcGrupConf."IdConceptoGrupo" || '-' || ConcGrupConf."Nombre" AS "IdNombreConceptoGrupo"
			                             ,InformeResultadoItem."IdItem"							AS "IdItem"
			                             ,InformeResultadoItem."NombreItem" AS "NombreItem"
			                             ,InformeResultadoItem."IdItem" || '-' || InformeResultadoItem."NombreItem" AS "IdNombreItem"
			                             ,ConcGrup."Operacion"                AS "Operacion"
			                             ,ConcGrupConf."Factor"                   AS "Factor"
			                             ,(InformeResultadoItem."MontoCalculado" * ConcGrupConf."Factor")/1000           AS "MontoCalculado"
			                             ,CASE InformeResultadoItem."Posicion"
			                                WHEN 'N' THEN	'Numerador'
			                                WHEN 'D' THEN	'Denominador'
			                              ELSE	''
			                              END                                                  AS "Posicion"
			                       FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS InformeResultadoIndice
			                         INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS InformeResultadoConcepto
			                            ON InformeResultadoIndice."IdResultadoIndice" = InformeResultadoConcepto."IdResultadoIndice"
			                           AND InformeResultadoConcepto."IdResultadoIndice" = InformeResultadoIndice."IdResultadoIndice" 
			                           AND InformeResultadoIndice."IdTipoInforme" = :p_IdTipoInforme
			                           AND InformeResultadoIndice."FechaContable" = :p_FechaContable
			                         INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItem" as InformeResultadoItem
			                            ON InformeResultadoItem."IdResultadoConcepto" = InformeResultadoConcepto."IdResultadoConcepto"
			                           AND InformeResultadoItem."IdTipoInforme" = InformeResultadoIndice."IdTipoInforme"
			                           AND InformeResultadoItem."IdConcepto" = InformeResultadoConcepto."IdConcepto"
			                           AND InformeResultadoItem."IdTipoInforme" = InformeResultadoConcepto."IdTipoInforme"
			                         INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConcGrupConf
			                            ON ConcGrupConf."IdConcepto" = InformeResultadoConcepto."IdConcepto"	
			                           AND ConcGrupConf."IdConceptoGrupo" = InformeResultadoItem."IdConceptoGrupo"
			                         LEFT JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS ConcGrup
			                         	ON ConcGrup."IdConcepto" = InformeResultadoConcepto."IdConcepto"	
			                           AND ConcGrup."IdConceptoGrupo" = InformeResultadoItem."IdConceptoGrupo"
			                           AND ConcGrup."IdItem" = InformeResultadoItem."IdItem"
			                       WHERE InformeResultadoConcepto."Estado" = 'VIG'
			                       ORDER BY InformeResultadoIndice."IdIndice", InformeResultadoConcepto."IdConcepto", ConcGrupConf."IdConceptoGrupo", InformeResultadoItem."IdItem"  ASC;
		END;		
	END;	
END;
