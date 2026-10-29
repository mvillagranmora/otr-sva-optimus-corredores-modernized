SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarContabilidadDefinicionAsiento";

CREATE PROCEDURE
-- 
--  Proposito: Vista
--	
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 11/09/2016
--  Id Servicio = 12013
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarContabilidadDefinicionAsiento" (
	IN p_IdDefinicionAsiento	BIGINT				DEFAULT 0,
	IN p_IdTipoContabilidad		BIGINT				DEFAULT 0,
	IN p_IdCuentaContable		NVARCHAR(12)		DEFAULT '',
	IN p_Ejercicio				BIGINT				DEFAULT 0,
	IN p_Orden					BIGINT				DEFAULT 0,
	IN p_DebeHaber				NVARCHAR(1)			DEFAULT '',
	IN p_TipoEvento				NVARCHAR(1)			DEFAULT '',
	IN p_Condicionado			NVARCHAR(1)			DEFAULT '',
	IN p_Estado					NVARCHAR(1)			DEFAULT '',
	-- Parametros salida del servicio publico
	OUT p_out_TabContabilidadDefinicionAsiento TABLE ("IdDefinicionAsiento"		BIGINT,
													  "IdTipoContabilidad"		BIGINT,
													  "IdCuentaContable"		NVARCHAR(12),
													  "NombreCuenta"			NVARCHAR(70),
													  "Ejercicio"				BIGINT,
													  "Orden"					BIGINT,
													  "DebeHaber"				NVARCHAR(1),
													  "TipoEvento"				NVARCHAR(1),
													  "Condicionado"			NVARCHAR(1),
													  "OmiteCondicionCmbte"		NVARCHAR(1),
													  "Estado"					NVARCHAR(1),
													  "Formato"					NVARCHAR(1),
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
	----------------------------------
	--DECLARE Manejo de Errores
	----------------------------------
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
			--Error interno no manejado (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador  := 0;
	p_out_ErroresContador		:= 0;

	BEGIN
		p_out_TabContabilidadDefinicionAsiento = 
			 SELECT CTB_itzContabilidadDefinicionAsiento."IdDefinicionAsiento"		AS "IdDefinicionAsiento",
			 		CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad"		AS "IdTipoContabilidad",
			 		CTB_itzContabilidadDefinicionAsiento."IdCuentaContable"			AS "IdCuentaContable",
					CASE WHEN CTB_itzHomologaCuenta."NombreCuenta" = NULL THEN
						'?'
					ELSE
						CTB_itzHomologaCuenta."NombreCuenta"
					END 															AS "NombreCuenta",
			 		CTB_itzContabilidadDefinicionAsiento."Ejercicio"				AS "Ejercicio",
			 		CTB_itzContabilidadDefinicionAsiento."Orden"					AS "Orden",
			 		CTB_itzContabilidadDefinicionAsiento."DebeHaber"				AS "DebeHaber",
			 		CTB_itzContabilidadDefinicionAsiento."TipoEvento"				AS "TipoEvento",
			 		CTB_itzContabilidadDefinicionAsiento."Condicionado"				AS "Condicionado",
					CTB_itzContabilidadDefinicionAsiento."OmiteCondicionCmbte"		AS "OmiteCondicionCmbte",
					CTB_itzContabilidadDefinicionAsiento."Estado"					AS "Estado",
					CTB_itzContabilidadDefinicionAsiento."Formato"					AS "Formato",
					EstadoContabilidad."NombreEstado"								AS "NombreEstado"
			   FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento
			   					INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo ON
			   									CTB_itzContabilidadTipo."IdTipoContabilidad" = CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad"
			  					LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON
			  									CTB_itzHomologaCuenta."CuentaOptimus" = CTB_itzContabilidadDefinicionAsiento."IdCuentaContable"
			  					LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  								EstadoContabilidad."IdEstadoContabilidad" = CTB_itzContabilidadDefinicionAsiento."Estado"	 
			  WHERE (CTB_itzContabilidadDefinicionAsiento."IdDefinicionAsiento" = :p_IdDefinicionAsiento	OR :p_IdDefinicionAsiento = 0)
			    AND (CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad" = :p_IdTipoContabilidad 		OR :p_IdTipoContabilidad = 0)
			    AND (CTB_itzContabilidadDefinicionAsiento."IdCuentaContable" = :p_IdCuentaContable 			OR :p_IdCuentaContable = '')
			    AND (CTB_itzContabilidadDefinicionAsiento."Ejercicio" = :p_Ejercicio 						OR :p_Ejercicio = 0)
			    AND (CTB_itzContabilidadDefinicionAsiento."Orden" = :p_Orden 								OR :p_Orden = 0)
			    AND (CTB_itzContabilidadDefinicionAsiento."DebeHaber" = :p_DebeHaber 						OR :p_DebeHaber = '')
			    AND (CTB_itzContabilidadDefinicionAsiento."TipoEvento" = :p_TipoEvento 						OR :p_TipoEvento = '')
			    AND (CTB_itzContabilidadDefinicionAsiento."Condicionado" = :p_Condicionado 					OR :p_Condicionado = '')
			    AND (CTB_itzContabilidadDefinicionAsiento."Estado" = :p_Estado 								OR :p_Estado = '')
			    AND (CTB_itzContabilidadDefinicionAsiento."Estado" <> 'E')
		   ORDER BY CTB_itzContabilidadDefinicionAsiento."Orden";
	END;
END;
