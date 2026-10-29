WITH "ObtenerFechaActual" AS (
	SELECT TO_NVARCHAR(CURRENT_TIMESTAMP,'YYYY-MM-DD HH24:MI:SS') AS "FechaInicio"
	      ,:IdEmpresa AS "IdEmpresa"
	FROM DUMMY
),
	"Mercados"  AS (
	SELECT "Nombre", 'MERCADO_RV_CERRADO' AS "CodigoTipoControlProceso"
	FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
	WHERE "IdClasificacionTipoInstrumento" = 'RV'
	
	UNION
	SELECT "Nombre", 'MERCADO_RF_CERRADO' AS "CodigoTipoControlProceso"
	FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
	WHERE "IdClasificacionTipoInstrumento" = 'RF'
	
	UNION
	SELECT "Nombre", 'MERCADO_IF_CERRADO' AS "CodigoTipoControlProceso"
	FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
	WHERE "IdClasificacionTipoInstrumento" = 'IF'
	
	UNION
	SELECT "Nombre", 'MERCADO_DV_CERRADO' AS "CodigoTipoControlProceso"
	FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
	WHERE "IdClasificacionTipoInstrumento" = 'DV'
	
	UNION
	SELECT "Nombre", 'MERCADO_FM_CERRADO' AS "CodigoTipoControlProceso"
	FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
	WHERE "IdClasificacionTipoInstrumento" = 'FM'
	
	UNION
	SELECT "Nombre", 'MERCADO_SP_CERRADO' AS "CodigoTipoControlProceso"
	FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
	WHERE "IdClasificacionTipoInstrumento" = 'SP'
	
	UNION
	SELECT "Nombre", 'MERCADO_RE_CERRADO' AS "CodigoTipoControlProceso"
	FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
	WHERE "IdClasificacionTipoInstrumento" = 'RE'
)

SELECT MER."Nombre", CTP."CodigoControlProcesoTipo", FEC."FechaInicio"
FROM "BSCL.Plataforma.BD::CTR.ControlProcesoTipo" CTP
INNER JOIN "Mercados" MER ON MER."CodigoTipoControlProceso" = CTP."CodigoControlProcesoTipo"
INNER JOIN "ObtenerFechaActual" FEC ON FEC."IdEmpresa" = CTP."IdEmpresa"