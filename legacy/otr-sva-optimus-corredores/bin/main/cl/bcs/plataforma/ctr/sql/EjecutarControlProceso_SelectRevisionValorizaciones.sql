WITH "ObtenerFechaActual" AS (
	SELECT TO_NVARCHAR(CURRENT_TIMESTAMP,'YYYY-MM-DD HH24:MI:SS') AS "FechaInicio"
	      ,:p_IdEmpresa AS "IdEmpresa"
	FROM DUMMY
),
	"RevisionCantidadVencida" AS (
	-- Valorizacion Futuro
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_FUTUROS' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
	WHERE "IdEmpresa" = :p_IdEmpresa AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" IN ('FDO','FIP','FIC','FUF','FBO','FBC')
	AND "IdClasificacionTipoInstrumento" = 'DV'
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior
	
	-- Valorizacion RF
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_CARTERA_RF' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa
	AND "IdClasificacionTipoInstrumento" = 'RF'
	AND "IdEstado" = 'VIG'
	AND "IdClasificacionTipoInstrumento" = 'RF'
	AND "IdTipoInstrumento" NOT IN ('CER','ZER','XER')
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior
	
	-- Valorizacion IF
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_CARTERA_IF' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa
	AND "IdClasificacionTipoInstrumento" = 'IF'
	AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" <> 'PAC'
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior
	
	-- Valorizacion Simultaneas
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_SIMULTANEAS' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG' 
	AND "IdClasificacionTipoInstrumento" = 'RV'
	AND "IdTipoInstrumento" = 'SIM'
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior
	
	-- Valorizacion Prestamos Emprestitos
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_PRESTAMOS_EMPRESTITOS' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdTipoInstrumento" = 'EMP'
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior 		
	
	-- Valorizacion Pactos
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_PACTOS' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" = 'PAC'
	AND "FechaVencimientoOperacion" > :p_Fecha
	AND "INGFechaOperacion" <= :p_Fecha
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior
	
	-- Valorizacion Cupones
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_CUPONES' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" IN ('CER','ZER','XER')
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior
	
	-- Valorizacion Garantias RVRFIF
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_GARANTIAS_RV_RF_IF' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::GTA.Garantia"
	WHERE "IdClasificacionTipoInstrumento" IN ('RV','RF','IF')
	AND "IdEmpresa" = :p_IdEmpresa
	AND TO_VARCHAR("INGFechaValorizacionGarantia", 'YYYY-MM-DD') = :p_FechaDiaAnterior	
	
	-- Valorizacion Forward
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZACION_FWD' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdTipoProducto" = 'MEFW'
	  AND "IdEstado" = 'VIG'
	  AND "FechaOperacion" <= :p_Fecha
	  AND "IdEmpresa" = :p_IdEmpresa
	  AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior
		
	-- Valorizacion Cartera RV Precio Mercado
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_CARTERA_RV_PRECIO_MERCADO' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG' 
	AND "IdClasificacionTipoInstrumento" = 'RV'
	AND "IdTipoInstrumento" <> 'SIM'
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior		
	
	-- Valorizacion FFMM
	UNION
	SELECT COUNT(*)AS "CantidadEjecutada", :p_FechaDiaAnterior AS "Fecha", 'VALORIZAR_FFMM' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera" CARTERA
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdInstrumento" = CARTERA."IdInstrumento"
	INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo")
	WHERE CARTERA."IdEstado" = 'VIG'
	AND TO_VARCHAR("INGFechaValorizacionCartera", 'YYYY-MM-DD') = :p_FechaDiaAnterior		
	ORDER BY "CodigoControlProcesoTipo"	
	
), 
	"RevisionTotalVencidos" AS (
	-- Valorizacion Futuro
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_FUTUROS' AS "CodigoControlProcesoTipo"
	FROM  "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" IN ('FDO','FIP','FIC','FUF','FBO','FBC')
	AND "IdClasificacionTipoInstrumento" = 'DV'
	
	-- Valorizacion RF
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_CARTERA_RF' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa
	AND "IdClasificacionTipoInstrumento" = 'RF'
	AND "IdEstado" = 'VIG'
	AND "IdClasificacionTipoInstrumento" = 'RF'
	AND "IdTipoInstrumento" NOT IN ('CER','ZER','XER')	
	
	-- Valorizacion IF
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_CARTERA_IF' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa
	AND "IdClasificacionTipoInstrumento" = 'IF'
	AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" <> 'PAC'
	
	-- Valorizacion Simultaneas
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_SIMULTANEAS' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG' 
	AND "IdClasificacionTipoInstrumento" = 'RV'
	AND "IdTipoInstrumento" = 'SIM'	
	
	-- Valorizacion Prestamos Emprestitos
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_PRESTAMOS_EMPRESTITOS' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG' 
	AND "IdClasificacionTipoInstrumento" = 'RV'
	AND "IdTipoInstrumento" IN ('PRT','EMP')
		
	-- Valorizacion Pactos
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_PACTOS' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" = 'PAC'
	AND "FechaVencimientoOperacion" > :p_Fecha
	AND "INGFechaOperacion" <= :p_Fecha	
	
	-- Valorizacion Cupones
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_CUPONES' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG'
	AND "IdTipoInstrumento" IN ('CER','ZER','XER')	
	
	-- Valorizacion Garantias RVRFIF
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_GARANTIAS_RV_RF_IF' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::GTA.Garantia"
	WHERE "IdClasificacionTipoInstrumento" IN ('RV','RF','IF')
	AND "IdEmpresa" = :p_IdEmpresa	
	
	-- Valorizacion Forward
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZACION_FWD' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdTipoProducto" = 'MEFW'
	  AND "IdEstado" = 'VIG'
	  AND "FechaOperacion" <= :p_Fecha
	  AND "IdEmpresa" = :p_IdEmpresa
	 
	-- Valorizacion Cartera RV Precio Mercado
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_CARTERA_RV_PRECIO_MERCADO' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEmpresa" = :p_IdEmpresa 
	AND "IdEstado" = 'VIG' 
	AND "IdClasificacionTipoInstrumento" = 'RV'
	AND "IdTipoInstrumento" <> 'SIM'	
	
	-- Valorizacion FFMM
	UNION
	SELECT COUNT(*) AS "CantidadTotal", 'VALORIZAR_FFMM' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera" CARTERA
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdInstrumento" = CARTERA."IdInstrumento"
	INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo")
	WHERE CARTERA."IdEstado" = 'VIG'
) 
SELECT CPT."CodigoControlProcesoTipo"
      ,CPT."DescripcionControlProcesoTipo"
      ,CANT."Fecha"
      ,CANT."CantidadEjecutada"
      ,TOT."CantidadTotal"  
      ,FEC."FechaInicio"
  FROM "BSCL.Plataforma.BD::CTR.ControlProcesoTipo" CPT
  INNER JOIN "RevisionCantidadVencida" CANT ON CANT."CodigoControlProcesoTipo" = CPT."CodigoControlProcesoTipo"
  INNER JOIN "RevisionTotalVencidos" TOT ON TOT."CodigoControlProcesoTipo" = CPT."CodigoControlProcesoTipo"
  INNER JOIN "ObtenerFechaActual" FEC ON FEC."IdEmpresa" = CPT."IdEmpresa"
  WHERE CPT."IdEstado" = 'ACT'
