SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCondiciones";

CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 21/09/2016
--  Id Servicio = 12018
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCondiciones" (
	IN p_IdDefinicionAsiento		BIGINT			DEFAULT 0,
	IN p_IdTipoContabilidad			BIGINT			DEFAULT 0,
	IN p_IdCondicion				BIGINT			DEFAULT 0,
	IN p_Estado						NVARCHAR(1)		DEFAULT '',
	-- Parametros salida del servicio publico
	OUT p_out_TabCondiciones TABLE (
									"IdCondicion"			BIGINT,
									"IdDefinicionAsiento"	BIGINT,
									"IdTipoContabilidad"	BIGINT,
									"ConectorTextual"		NVARCHAR(1),
									"ParaOrdenar"			NVARCHAR(1),
									"Conector"				NVARCHAR(10),
									"IdTipoDatoEvento"		NVARCHAR(50),
									"Condicion"				NVARCHAR(10),
									"ValorVarchar"			NVARCHAR(60),
									"ValorDecimal"			DECIMAL(18,4),
									"TipoCondicion"			NVARCHAR(1),
									"Estado"				NVARCHAR(1),
									"NombreEstado"			NVARCHAR(50),
									"Descripcion"			NVARCHAR(100)
                                     ) DEFAULT EMPTY,
	-- Sección Fija ----
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          TABLE ("Campo" NVARCHAR(40), "Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
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
    DECLARE v_RechazadosPorValidacion	TABLE ("IdComprobante" BIGINT) DEFAULT
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

		p_out_ErroresDetalle = SELECT "IdInterno" AS "IdComprobante", "Codigo" AS "Codigo", "Descripcion" AS "Descripcion"
		                         FROM :v_out_hash UNION ALL
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
			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
								   SELECT 0 AS "IdComprobante", v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;
			SELECT COUNT(1)
			  INTO p_out_ErroresContador
			  FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	

	p_out_AdvertenciasContador 	:= 0;
	p_out_ErroresContador		:= 0;

	BEGIN
		p_out_TabCondiciones = 
				 SELECT CTB_itzContabilidadTipoAsientoCondicion."IdCondicion" 				AS "IdCondicion",
						CTB_itzContabilidadTipoAsientoCondicion."IdDefinicionAsiento" 		AS "IdDefinicionAsiento",
						CTB_itzContabilidadTipoAsientoCondicion."IdTipoContabilidad" 		AS "IdTipoContabilidad",
						CASE CTB_itzContabilidadTipoAsientoCondicion."Conector"
									WHEN 'OR' THEN 'O'
									WHEN 'AND' THEN 'Y'
									ELSE ''
						END 																AS "ConectorTextual",
						CASE "Conector"
									WHEN 'WHERE' THEN 'A'
									WHEN 'AND' THEN 'B'
									WHEN 'OR' THEN 'C'
						END 																AS "ParaOrdenar", 
						CTB_itzContabilidadTipoAsientoCondicion."Conector" 					AS "Conector",
						CTB_itzContabilidadTipoAsientoCondicion."IdTipoDatoEvento" 			AS "IdTipoDatoEvento",
						CTB_itzContabilidadTipoAsientoCondicion."Condicion" 				AS "Condicion",
						CTB_itzContabilidadTipoAsientoCondicion."ValorVarchar" 				AS "ValorVarchar",
						CTB_itzContabilidadTipoAsientoCondicion."ValorDecimal" 				AS "ValorDecimal",
						CTB_itzContabilidadTipoAsientoCondicion."TipoCondicion" 			AS "TipoCondicion",
						CTB_itzContabilidadTipoAsientoCondicion."Estado" 					AS "Estado",
						EstadoContabilidad."NombreEstado"									AS "NombreEstado",
						IFNULL(CVT."Descripcion",CTB_itzContabilidadTipoAsientoCondicion."ValorVarchar")												AS "Descripcion"
				   FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" AS CTB_itzContabilidadTipoAsientoCondicion 
				    LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  		EstadoContabilidad."IdEstadoContabilidad" = CTB_itzContabilidadTipoAsientoCondicion."Estado"	 
				  	LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarValoresTipoDato" CVT ON
					CVT."IdTipoDatoEvento" = CTB_itzContabilidadTipoAsientoCondicion."IdTipoDatoEvento" AND CVT."Valor" = CTB_itzContabilidadTipoAsientoCondicion."ValorVarchar"	
				  WHERE (CTB_itzContabilidadTipoAsientoCondicion."IdTipoContabilidad" 	= :p_IdTipoContabilidad 	OR :p_IdTipoContabilidad 	= 0)
				    AND (CTB_itzContabilidadTipoAsientoCondicion."IdDefinicionAsiento" 	= :p_IdDefinicionAsiento 	OR :p_IdDefinicionAsiento 	= 0)
				    AND (CTB_itzContabilidadTipoAsientoCondicion."IdCondicion"			= :p_IdCondicion 			OR :p_IdCondicion 			= 0)
				    AND (CTB_itzContabilidadTipoAsientoCondicion."Estado" 				= :p_Estado 				OR :p_Estado 				= '')
				ORDER BY "ParaOrdenar";
	END;
END;