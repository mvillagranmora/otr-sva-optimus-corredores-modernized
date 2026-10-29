SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientosDatos";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 05/09/2016
--  Id Servicio = 12004
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarMovimientosDatos" (
	IN p_IdMovimiento					BIGINT			default 0,
	IN p_IdEvento                       BIGINT          default 0,
	IN p_TipoDatoEvento					NVARCHAR(30)	default '',
	IN p_ValorVarChar					NVARCHAR(60)	default '',
	IN p_ValorDecimal					DECIMAL(18,4)	default 0,
	IN p_Estado							NVARCHAR(1)		default '',
	-- Parametros salida del servicio publico
	OUT p_out_TabMovimientosDatos TABLE ( 
                                     "IdMovimiento"   	BIGINT,
                                     "IdTipoDatoEvento" NVARCHAR(30),
                                     "ValorVarChar"		NVARCHAR(60),
                                     "ValorDecimal"		DECIMAL(18,4),
                                     "Descripcion"		NVARCHAR(100),
                                     "Estado"			NVARCHAR(1),
                                     "NombreEstado"		NVARCHAR(50)
                                     ),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          table ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),
	OUT p_out_AdvertenciasContador      int,
	OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),  
	OUT p_out_ErroresContador           int,   
	OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
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
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT);	 
    DECLARE v_TipoDato 					NVARCHAR(40);
    DECLARE v_MovimientoSinEventoAsociado TABLE ( 
                                     "IdMovimiento"   	BIGINT,
                                     "IdTipoDatoEvento" NVARCHAR(30),
                                     "TipoEvento"       NVARCHAR(30),
                                     "ValorVarChar"		NVARCHAR(60),
                                     "ValorDecimal"		DECIMAL(18,4),
                                     "Descripcion"		NVARCHAR(100),
                                     "Estado"			NVARCHAR(1),
                                     "NombreEstado"		NVARCHAR(50)
                                     );
    DECLARE v_MovimientoConEventoAsociado TABLE ( 
                                     "IdMovimiento"   	BIGINT,
                                     "IdTipoDatoEvento" NVARCHAR(30),
                                     "TipoEvento"       NVARCHAR(30),
                                     "ValorVarChar"		NVARCHAR(60),
                                     "ValorDecimal"		DECIMAL(18,4),
                                     "Descripcion"		NVARCHAR(100),
                                     "Estado"			NVARCHAR(1),
                                     "NombreEstado"		NVARCHAR(50)
                                     );
    

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
	p_out_AdvertenciasDetalle	= select 0 As "IdComprobante",'' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 As "IdComprobante", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_ResultadoProceso	    = select '' as "Campo",    0 as "Id", '' as "Servicio", '' as "Resultado" From dummy where 1=2;	
	v_RechazadosPorValidacion   = select 0 As "IdComprobante" from dummy where 1=2;
	v_TipoDato 					='';
	v_MovimientoSinEventoAsociado =select 0 As "IdMovimiento",'' as "IdTipoDatoEvento",'' AS "TipoEvento",'' as "ValorVarChar",0 as "ValorDecimal",'' AS "Descripcion", '' AS "Estado",'' AS "NombreEstado"  from dummy where 1=2;
	v_MovimientoConEventoAsociado =select 0 As "IdMovimiento",'' as "IdTipoDatoEvento",'' AS "TipoEvento",'' as "ValorVarChar",0 as "ValorDecimal",'' AS "Descripcion", '' AS "Estado",'' AS "NombreEstado"  from dummy where 1=2;
	
	BEGIN
		
			IF(p_IdEvento <> 0) THEN 
				SELECT TOP 1 "IdTipoEvento" INTO v_TipoDato FROM "BSCL.Contabilidad.BD::ITZ.Evento" WHERE "IdEvento"= p_IdEvento;
	    	END IF;
	    	
	    	v_MovimientoConEventoAsociado=
	    	SELECT
				CTB_itzMovimientoDato."IdMovimiento" 													AS "IdMovimiento",
				CTB_itzMovimientoDato."IdTipoDatoEvento" 												AS "IdTipoDatoEvento",
				CTB_itzMovimientoDato."TipoEvento"														AS "TipoEvento",
				
				CASE WHEN CTB_itzEventoTipoDato."TipoDato" = 'V' THEN
					CTB_itzMovimientoDato."ValorVarchar"
				ELSE
					CAST(CAST(CTB_itzMovimientoDato."ValorDecimal" AS DECIMAL(18,4)) AS NVARCHAR(60))
				END 																					AS "ValorVarChar",
				0 																						AS "ValorDecimal",
				CASE WHEN COALESCE(CVT."Descripcion",'') = '' THEN CASE WHEN CTB_itzEventoTipoDato."TipoDato" = 'V' THEN
																CTB_itzMovimientoDato."ValorVarchar"
															  ELSE
																CAST(CAST(CTB_itzMovimientoDato."ValorDecimal" AS DECIMAL(18,4)) AS NVARCHAR(60))
															  END
				ELSE CVT."Descripcion"
				END 										  AS "Descripcion",
				CTB_itzMovimientoDato."Estado"															AS "Estado",
				EstadoContabilidad."NombreEstado"														AS "NombreEstado"
				
				FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato	
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS CTB_itzEventoTipoDato ON
				CTB_itzEventoTipoDato."IdTipoDatoEvento" = CTB_itzMovimientoDato."IdTipoDatoEvento"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  	EstadoContabilidad."IdEstadoContabilidad" = CTB_itzMovimientoDato."Estado"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarValoresTipoDato" CVT ON
					CVT."IdTipoDatoEvento" = CTB_itzMovimientoDato."IdTipoDatoEvento" AND CVT."Valor" = CTB_itzMovimientoDato."ValorVarchar"
			WHERE 
				   (CTB_itzMovimientoDato."IdMovimiento"		=	p_IdMovimiento)
				AND(CTB_itzMovimientoDato."IdTipoDatoEvento"	=	p_TipoDatoEvento OR p_TipoDatoEvento = '')
				AND(CTB_itzMovimientoDato."ValorVarchar"		=	p_ValorVarChar	 OR p_ValorVarChar	 = '')
				AND(CTB_itzMovimientoDato."ValorDecimal"		=	p_ValorDecimal	 OR p_ValorDecimal	 = 0)
				AND(CTB_itzMovimientoDato."Estado"				=	p_Estado		 OR p_Estado		 = '')
				AND(CTB_itzMovimientoDato."TipoEvento" 			=	v_TipoDato)	
		   ORDER BY CTB_itzMovimientoDato."IdTipoDatoEvento" ASC; 
		   
		   --p_out_TabMovimientosDatos=
		   v_MovimientoSinEventoAsociado=
	    	SELECT
				CTB_itzMovimientoDato."IdMovimiento" 													AS "IdMovimiento",
				CTB_itzMovimientoDato."IdTipoDatoEvento" 												AS "IdTipoDatoEvento",
				CTB_itzMovimientoDato."TipoEvento"														AS "TipoEvento",
				
				CASE WHEN CTB_itzEventoTipoDato."TipoDato" = 'V' THEN
					CTB_itzMovimientoDato."ValorVarchar"
				ELSE
					CAST(CAST(CTB_itzMovimientoDato."ValorDecimal" AS DECIMAL(18,4)) AS NVARCHAR(60))
				END 																					AS "ValorVarChar",
				0 																						AS "ValorDecimal",
				CASE WHEN COALESCE(CVT."Descripcion",'') = '' THEN CASE WHEN CTB_itzEventoTipoDato."TipoDato" = 'V' THEN
																CTB_itzMovimientoDato."ValorVarchar"
															  ELSE
																CAST(CAST(CTB_itzMovimientoDato."ValorDecimal" AS DECIMAL(18,4)) AS NVARCHAR(60))
															  END
				ELSE CVT."Descripcion"
				END 										  AS "Descripcion",
				CTB_itzMovimientoDato."Estado"															AS "Estado",
				EstadoContabilidad."NombreEstado"														AS "NombreEstado"
				
				FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato
				INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS CTB_itzEventoTipoDato ON
				CTB_itzEventoTipoDato."IdTipoDatoEvento" = CTB_itzMovimientoDato."IdTipoDatoEvento"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  	EstadoContabilidad."IdEstadoContabilidad" = CTB_itzMovimientoDato."Estado"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarValoresTipoDato" CVT ON
					CVT."IdTipoDatoEvento" = CTB_itzMovimientoDato."IdTipoDatoEvento" AND CVT."Valor" = CTB_itzMovimientoDato."ValorVarchar"
			WHERE 
				   (CTB_itzMovimientoDato."IdMovimiento"		=	p_IdMovimiento)
				AND(CTB_itzMovimientoDato."IdTipoDatoEvento"	=	p_TipoDatoEvento OR p_TipoDatoEvento = '')
				AND(CTB_itzMovimientoDato."ValorVarchar"		=	p_ValorVarChar	 OR p_ValorVarChar	 = '')
				AND(CTB_itzMovimientoDato."ValorDecimal"		=	p_ValorDecimal	 OR p_ValorDecimal	 = 0)
				AND(CTB_itzMovimientoDato."Estado"				=	p_Estado		 OR p_Estado		 = '')
				AND(CTB_itzMovimientoDato."TipoEvento" 			=	'')	-- mp
		   ORDER BY CTB_itzMovimientoDato."IdTipoDatoEvento" ASC;
		   
		   
		   p_out_TabMovimientosDatos = 
			SELECT
				a."IdMovimiento" 													AS "IdMovimiento",
				a."IdTipoDatoEvento" 												AS "IdTipoDatoEvento",
				CASE WHEN (b."ValorVarChar"<>'?') 
					THEN b."ValorVarChar"
					ELSE a."ValorVarChar" END										AS "ValorVarChar",
				0 																	AS "ValorDecimal",
				CASE WHEN (b."Descripcion"<>'?') 
					THEN b."Descripcion"
					ELSE a."Descripcion" END										AS "Descripcion",
				a."Estado"															AS "Estado",
				a."NombreEstado"													AS "NombreEstado"
			FROM :v_MovimientoSinEventoAsociado AS a
				LEFT JOIN  :v_MovimientoConEventoAsociado AS b
				ON
					a."IdMovimiento" = b."IdMovimiento" AND
					a."IdTipoDatoEvento" = b."IdTipoDatoEvento" 
			
		   ORDER BY a."IdTipoDatoEvento" ASC;
				  
	END;
END;
GO