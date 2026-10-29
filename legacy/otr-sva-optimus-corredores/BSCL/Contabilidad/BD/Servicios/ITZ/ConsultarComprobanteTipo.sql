SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarComprobanteTipo";

CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 20/09/2016
--  Id Servicio = 12017
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarComprobanteTipo" (
	IN p_IdPreComprobante	BIGINT 			DEFAULT 0,
	IN p_IdEmpresa			BIGINT 			DEFAULT 0,
	IN p_Nombre				NVARCHAR(60) 	DEFAULT '',
	IN p_FechaContable		NVARCHAR(10) 	DEFAULT '',
	IN p_Estado				NVARCHAR(1) 	DEFAULT 'V',
	-- Parametros salida del servicio público
	OUT p_out_TabComprobanteTipo TABLE (
	                                     "Nombre"   			NVARCHAR(114),
	                                     "IdPreComprobante"		BIGINT,
	                                     "CodigoUsuario"		NVARCHAR(40)
	                                     ) DEFAULT EMPTY,
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
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
		v_hash= SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;
        v_out_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

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
		p_out_TabComprobanteTipo = 
			 SELECT Ctb_itzContabilidadTipo."Nombre" || '  ' || 
			 		Ctb_itzPreComprobante."HoraProceso" || ' ]' || '[' || 
			 		Ctb_itzPreComprobante."CodigoUsuario" || ']' 				AS "Nombre", 
			 		Ctb_itzPreComprobante."IdPreComprobante" 					AS "IdPreComprobante", 
			 		Ctb_itzPreComprobante."CodigoUsuario" 						AS "CodigoUsuario"
			   FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS Ctb_itzPreComprobante
			   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS Ctb_itzContabilidadTipo ON 
			   									Ctb_itzContabilidadTipo."IdTipoContabilidad" = Ctb_itzPreComprobante."IdTipoContabilidad" 
			  WHERE (Ctb_itzPreComprobante."IdPreComprobante" = :p_IdPreComprobante OR :p_IdPreComprobante = 0)
			    AND (Ctb_itzPreComprobante."IdEmpresa" = :p_IdEmpresa)
			    AND (Ctb_itzContabilidadTipo."Nombre" = :p_Nombre 					OR :p_Nombre = '')
			    AND (Ctb_itzPreComprobante."FechaContable" = :p_FechaContable 		OR :p_FechaContable = '')
			    AND (Ctb_itzPreComprobante."Estado" = :p_Estado 					OR :p_Estado = '');
	END;
END;