WITH "ObtenerFechaActual" AS (
	SELECT TO_NVARCHAR(CURRENT_TIMESTAMP,'YYYY-MM-DD HH24:MI:SS') AS "FechaInicio"
	      ,:p_IdEmpresa AS "IdEmpresa"
	FROM DUMMY
),
	"RevisionTotalVencidos" AS (
	-- Valida vencimiento de Simultaneas del dia anterior
	SELECT COUNT(*) AS "CantidadTotal", :p_FechaDiaAnterior AS "Fecha", 'VENC_SIM' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VIG','VCD')  
	AND "IdTipoInstrumento" = 'SIM'
	AND	"FechaVencimientoOperacion" = :p_FechaDiaAnterior 
	AND	"IdEmpresa" = :p_IdEmpresa
	
	-- Valida vencimiento de Prestamos del dia anterior
	UNION
	SELECT COUNT(*) AS "CantidadTotal", :p_FechaDiaAnterior AS "Fecha", 'VENC_PRT' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
	WHERE "IdEstado" IN ('VIG','VCD')
	AND "IdEmpresa" = :p_IdEmpresa
	AND "FechaVencimientoPrestamo" > '0001-01-01' 
	AND "FechaVencimientoPrestamo" = :p_FechaDiaAnterior
	
	-- Valida vencimiento de Pactos del dia anterior
	UNION
	SELECT COUNT(*) AS "CantidadTotal", :p_FechaDiaAnterior AS "Fecha", 'VENC_PAC' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VIG','VCD')
	AND "IdTipoInstrumento" = 'PAC' 
	AND "IdClasificacionTipoInstrumento" = 'IF' 
	AND "FechaVencimientoOperacion"=:p_FechaDiaAnterior
	AND "IdEmpresa" = :p_IdEmpresa
	
	-- Valida vencimiento de Simultaneas del dia
	UNION
	SELECT COUNT(*) AS "CantidadTotal", :p_Fecha AS "Fecha", 'VENC_SIM_DIA' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VIG','VCD')  
	AND "IdTipoInstrumento" = 'SIM'
	AND	"FechaVencimientoOperacion" = :p_Fecha 
	AND	"IdEmpresa" = :p_IdEmpresa
	
	-- Valida vencimiento de Prestamos del dia
	UNION
	SELECT COUNT(*) AS "CantidadTotal", :p_Fecha AS "Fecha", 'VENC_PRT_DIA' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
	WHERE "IdEstado" IN ('VIG','VCD')
	AND "IdEmpresa" = :p_IdEmpresa
	AND "FechaVencimientoPrestamo" > '0001-01-01' 
	AND "FechaVencimientoPrestamo" = :p_Fecha
	
	-- Valida vencimiento de Pactos del dia
	UNION
	SELECT COUNT(*) AS "CantidadTotal", :p_Fecha AS "Fecha", 'VENC_PAC_DIA' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VIG','VCD')
	AND "IdTipoInstrumento" = 'PAC' 
	AND "IdClasificacionTipoInstrumento" = 'IF' 
	AND "FechaVencimientoOperacion"=:p_Fecha
	AND "IdEmpresa" = :p_IdEmpresa	
),
	"RevisionCantidadVencida" AS (
	-- Valida vencimiento de Simultaneas del dia anterior
	SELECT COUNT(*) AS "CantidadEjecutada", 'VENC_SIM' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VCD','LIQ')   
	AND	  "IdTipoInstrumento" = 'SIM'  
	AND	  "FechaVencimientoOperacion" = :p_FechaDiaAnterior 
	AND	  "IdEmpresa" = :p_IdEmpresa
	
	-- Valida vencimiento de Prestamos del dia anterior
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", 'VENC_PRT' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
	WHERE "IdEstado" IN ('VCD','LIQ')  
	AND   "IdEmpresa" = :p_IdEmpresa
	AND   "FechaVencimientoPrestamo" = :p_FechaDiaAnterior
	
	-- Valida vencimiento de Pactos del dia anterior
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", 'VENC_PAC' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VCD','LIQ')  
	AND   "IdTipoInstrumento" = 'PAC' 
	AND   "IdClasificacionTipoInstrumento" = 'IF' 
	AND   "FechaVencimientoOperacion"=:p_FechaDiaAnterior
	AND   "IdEmpresa" = :p_IdEmpresa
	
	-- Valida vencimiento de Simultaneas del dia
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", 'VENC_SIM_DIA' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VCD','LIQ')   
	AND	  "IdTipoInstrumento" = 'SIM'  
	AND	  "FechaVencimientoOperacion" = :p_Fecha 
	AND	  "IdEmpresa" = :p_IdEmpresa
	
	-- Valida vencimiento de Prestamos del dia 
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", 'VENC_PRT_DIA' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
	WHERE "IdEstado" IN ('VCD','LIQ')  
	AND   "IdEmpresa" = :p_IdEmpresa
	AND   "FechaVencimientoPrestamo" = :p_Fecha
	
	-- Valida vencimiento de Pactos del dia 
	UNION
	SELECT COUNT(*) AS "CantidadEjecutada", 'VENC_PAC_DIA' AS "CodigoControlProcesoTipo"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdEstado" IN ('VCD','LIQ')  
	AND   "IdTipoInstrumento" = 'PAC' 
	AND   "IdClasificacionTipoInstrumento" = 'IF' 
	AND   "FechaVencimientoOperacion"=:p_Fecha
	AND   "IdEmpresa" = :p_IdEmpresa	
) 
SELECT CPT."CodigoControlProcesoTipo"
      ,CPT."DescripcionControlProcesoTipo"
      ,CANT."CantidadEjecutada"
      ,TOT."CantidadTotal"  
      ,TOT."Fecha"
      ,FEC."FechaInicio"
  FROM "BSCL.Plataforma.BD::CTR.ControlProcesoTipo" CPT
  INNER JOIN "RevisionCantidadVencida" CANT ON CANT."CodigoControlProcesoTipo" = CPT."CodigoControlProcesoTipo"
  INNER JOIN "RevisionTotalVencidos" TOT ON TOT."CodigoControlProcesoTipo" = CPT."CodigoControlProcesoTipo"
  INNER JOIN "ObtenerFechaActual" FEC ON FEC."IdEmpresa" = CPT."IdEmpresa"
  WHERE CPT."IdEstado" = 'ACT'