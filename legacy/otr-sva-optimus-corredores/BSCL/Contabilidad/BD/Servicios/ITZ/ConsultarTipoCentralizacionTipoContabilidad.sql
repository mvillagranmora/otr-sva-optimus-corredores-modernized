SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarTipoCentralizacionTipoContabilidad";

CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 12/10/2016
--  Id Servicio = 12021
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarTipoCentralizacionTipoContabilidad" (
	IN p_IdEmpresa          				BIGINT 			DEFAULT 0,
	IN p_IdTipoContabilidad					SMALLINT		DEFAULT 0,
	IN p_CodigoUsuario						NVARCHAR(40) 	DEFAULT '',
	IN p_Estado								CHAR(1)			DEFAULT '',
	-- Parametros salida del servicio publico
	OUT p_out_TabTipoCentralizacionTipoContabilidad TABLE (
												"IdTipoCentralizacion"		BIGINT,
												"NombreTipoCentralizacion" 	NVARCHAR(50),
												"IdTipoContabilidad"		NVARCHAR(20),
												"Estado" 					NVARCHAR(1)
                                     		 ),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          table ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      int,
	OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador           int,
	OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion"
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle;

		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL 
								   SELECT 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
			SELECT count(1) into p_out_ErroresContador
			FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;

	BEGIN
		p_out_TabTipoCentralizacionTipoContabilidad = 
		SELECT CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion" AS "IdTipoCentralizacion", 
			   CTB_itzCentralizacionTipo."Nombre" 								AS "NombreTipoCentralizacion", 
			   CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" 	AS "IdTipoContabilidad", 
			   CTB_itzContabilidadTipoCentralizacionTipo."Estado" 				AS "Estado" 
		  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" AS CTB_itzContabilidadTipoCentralizacionTipo 
		  						INNER JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS CTB_itzCentralizacionTipo ON 
		   									CTB_itzCentralizacionTipo."IdTipoCentralizacion" = CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion" 
		 WHERE (CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" 	= :p_IdTipoContabilidad 	OR :p_IdTipoContabilidad = 0) 
		   AND (CTB_itzContabilidadTipoCentralizacionTipo."Estado" 				= :p_Estado 				OR :p_Estado = '');
	END;
END;