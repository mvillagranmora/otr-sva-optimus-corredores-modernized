SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarContabilidadTipo";

CREATE PROCEDURE 
--	
--  Proposito: Vista 
--	
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 06/09/2016
--  Id Servicio = 12009
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarContabilidadTipo" (
-- Incluír Variables existentes de proceso a migrar
	IN p_IdEmpresa				BIGINT			DEFAULT 0,
	IN p_IdTipoContabilidad		BIGINT			DEFAULT 0,
	IN p_Nemo					NVARCHAR(20)	DEFAULT '',
	IN p_Tipo					NVARCHAR(1)		DEFAULT '',
	IN p_Reversa				NVARCHAR(1)		DEFAULT '',
	IN p_Agrupa					NVARCHAR(1)		DEFAULT '',
	IN p_Condicionado			NVARCHAR(1)		DEFAULT '',
	IN p_TipoGeneracion			NVARCHAR(1)		DEFAULT '',
	IN p_Estado					NVARCHAR(1)		DEFAULT '',
-- Se agrega tabla resultado donde se devuelven los resultados
-- Parametros salida del servicio publico
	OUT p_out_TabContabilidadTipo TABLE (   "Check"    				BIGINT,
                                     		"IdTipoContabilidad"	BIGINT,
                                    		"Nombre"                NVARCHAR(60),
                                     		"Nemo"               	NVARCHAR(20),
                                     		"Tipo"               	NVARCHAR(1),
                                     		"Reversa"				NVARCHAR(1),
                                     		"Agrupa"				NVARCHAR(1),
                                     		"Condicionado"			NVARCHAR(1),
                                     		"TipoGeneracion"		NVARCHAR(1),
                                     		"Formato"				NVARCHAR(1),
                                     		"Estado"				NVARCHAR(1),
                                     		"NombreEstado"			NVARCHAR(50)
                                     ),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          TABLE ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	-- FIN Sección Fija
	)
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER
    DEFAULT SCHEMA "BSCL"
    --READS SQL DATA
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(520)) DEFAULT
												SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT) DEFAULT
    											SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;
        v_out_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;

        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

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
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE, v_errorExt);
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle
								   UNION ALL
								   SELECT 0 AS "IdComprobante", v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;
			SELECT count(1) into p_out_ErroresContador
			FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;

	BEGIN
		-- Aqui va la Query Migrada, asociada a la tabla de salida
		p_out_TabContabilidadTipo = 
			SELECT 0 											AS "Check",
				   CTB_itzContabilidadTipo."IdTipoContabilidad" AS "IdTipoContabilidad",
				   CTB_itzContabilidadTipo."Nombre" 			AS "Nombre",
				   CTB_itzContabilidadTipo."Nemo" 				AS "Nemo",
				   CTB_itzContabilidadTipo."Tipo" 				AS "Tipo",
				   CTB_itzContabilidadTipo."Reversa" 			AS "Reversa",
				   CTB_itzContabilidadTipo."Agrupa" 			AS "Agrupa",
				   CTB_itzContabilidadTipo."Condicionado" 		AS "Condicionado",
				   CTB_itzContabilidadTipo."TipoGeneracion" 	AS "TipoGeneracion",
				   CTB_itzContabilidadTipo."Formato" 			AS "Formato",
				   CTB_itzContabilidadTipo."Estado" 			AS "Estado",
				   EstadoContabilidad."NombreEstado"			AS "NombreEstado"	
			  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo
			  LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  	EstadoContabilidad."IdEstadoContabilidad" = CTB_itzContabilidadTipo."Estado"
			 WHERE (CTB_itzContabilidadTipo."IdEmpresa" = :p_IdEmpresa 						OR :p_IdEmpresa = 0)
			   AND (CTB_itzContabilidadTipo."IdTipoContabilidad" = :p_IdTipoContabilidad 	OR :p_IdTipoContabilidad = 0)
			   AND (CTB_itzContabilidadTipo."Nemo" = :p_Nemo 								OR :p_Nemo = '')
			   AND (CTB_itzContabilidadTipo."Tipo" = :p_Tipo 								OR :p_Tipo = '')
			   AND (CTB_itzContabilidadTipo."Reversa" = :p_Reversa 							OR :p_Reversa = '')
			   AND (CTB_itzContabilidadTipo."Agrupa" = :p_Agrupa 							OR :p_Agrupa = '')
			   AND (CTB_itzContabilidadTipo."Condicionado" = :p_Condicionado 				OR :p_Condicionado = '')
			   AND (CTB_itzContabilidadTipo."TipoGeneracion" = :p_TipoGeneracion 			OR :p_TipoGeneracion = '')
			   AND (CTB_itzContabilidadTipo."Estado" = :p_Estado 							OR :p_Estado = '')
			   AND (CTB_itzContabilidadTipo."Estado" <> 'E')
		  ORDER BY CTB_itzContabilidadTipo."Nombre" ASC;
	END;
END;
