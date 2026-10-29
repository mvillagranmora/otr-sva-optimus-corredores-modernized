SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoTipo";
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 20/09/2016
--  Id Servicio = 12016
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoTipo" (
	-- Incluír Variables existentes de proceso a migrar
	IN p_IdTipoCentralizacion			BIGINT 		default 0,
	IN p_Estado							NVARCHAR(1) default 'V',
	-- Parametros salida del servicio publico
	OUT p_out_TabEventos 			TABLE ( 
	                                     "Nombre"   		NVARCHAR(60),
	                                     "IdTipoEvento"     NVARCHAR(40)
	                                     ) DEFAULT EMPTY,
		-- Parametros de Salida
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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

	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;										
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;

	DECLARE SALIDA_CONTROLADA CONDITION;
	 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
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
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								    													adv."Codigo" = msj."Codigo";
	END;
 
 	p_out_ErroresContador := 0;
 	p_out_AdvertenciasContador := 0;
 
	-- Aqui va la Query Migrada, asociada a la tabla de salida
		p_out_TabEventos = 
			 SELECT CTB_itzEventoTipo."Nombre" 			AS "Nombre",
			 		CTB_itzEventoTipo."IdTipoEvento" 	AS "IdTipoEvento"
			   FROM "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo
			   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" AS CTB_itzEventoTipoCentralizacionTipo ON
			   									CTB_itzEventoTipo."IdTipoEvento" 	= CTB_itzEventoTipoCentralizacionTipo."IdTipoEvento"
			   								AND CTB_itzEventoTipoCentralizacionTipo."Estado" 			= 'V'
			  WHERE (CTB_itzEventoTipoCentralizacionTipo."IdTipoCentralizacion" = p_IdTipoCentralizacion OR p_IdTipoCentralizacion = 0)
			    AND (CTB_itzEventoTipo."Estado" = p_Estado OR p_Estado = '')
			GROUP BY CTB_itzEventoTipo."IdTipoEvento",
					 CTB_itzEventoTipo."Nombre";
		
END;
