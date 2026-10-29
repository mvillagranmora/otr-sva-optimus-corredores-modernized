WITH "ObtenerFechaActual" AS (
	SELECT TO_NVARCHAR(CURRENT_TIMESTAMP,'YYYY-MM-DD HH24:MI:SS') AS "FechaInicio"
	      ,:IdEmpresa AS "IdEmpresa"
	FROM DUMMY
),
"CUADRATURAS" AS  (

SELECT "OperacionCuadraturaDiariaOperaciones", "Resultado" AS "Mensaje", "TotalOperaciones", "OperacionesConProblemas", 'COMPROBANTES_ENVIADOS' 
AS "CodigoTipoControlProceso", "FechaEjecucion", "IdEmpresa"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" = 'CUACOMCON'

UNION
SELECT "OperacionCuadraturaDiariaOperaciones", "Resultado" AS "Mensaje", "TotalOperaciones", "OperacionesConProblemas", 'CUADRATURA_CENTRALIZACION_FACTURACION_CP' 
AS "CodigoTipoControlProceso", "FechaEjecucion", "IdEmpresa"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" = 'CUACENFACTCP'

UNION
SELECT "OperacionCuadraturaDiariaOperaciones", "Resultado" AS "Mensaje", "TotalOperaciones", "OperacionesConProblemas", 'CUADRATURA_CENTRALIZACION_FACTURACION' 
AS "CodigoTipoControlProceso", "FechaEjecucion", "IdEmpresa"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" = 'CUACENFACT'

UNION
SELECT "OperacionCuadraturaDiariaOperaciones", "Resultado" AS "Mensaje", "TotalOperaciones", "OperacionesConProblemas", 'CUADRATURA_CENTRALIZACION_INGRESO_FWD' 
AS "CodigoTipoControlProceso", "FechaEjecucion", "IdEmpresa"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" = 'CUAINGFWD'

UNION
SELECT "OperacionCuadraturaDiariaOperaciones", "Resultado" AS "Mensaje", "TotalOperaciones", "OperacionesConProblemas", 'CUADRATURA_CENTRALIZACION_VAL_FWD' 
AS "CodigoTipoControlProceso", "FechaEjecucion", "IdEmpresa"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" = 'CUAVALFWD'

UNION
SELECT "OperacionCuadraturaDiariaOperaciones", "Resultado" AS "Mensaje", "TotalOperaciones", "OperacionesConProblemas", 'CUADRATURA_CENTRALIZACION_VCT_FWD' 
AS "CodigoTipoControlProceso", "FechaEjecucion", "IdEmpresa"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" = 'CUAVCTFWD'

)

SELECT DIC."Descripcion" AS "Mensaje", CDR."TotalOperaciones", CDR."OperacionesConProblemas", CTP."CodigoControlProcesoTipo",
CASE 
	WHEN CDR."Mensaje" = 'UI_SIN_OPERACIONES' THEN 'ADV'
	WHEN CDR."Mensaje" = 'UI_DESCUADRADO' THEN 'ERR'
	WHEN CDR."Mensaje" = 'UI_CUADRADO' THEN 'COM'
END AS "Estado"
,FEC."FechaInicio"
FROM "BSCL.Plataforma.BD::CTR.ControlProcesoTipo" CTP 
INNER JOIN "CUADRATURAS" CDR ON CDR."CodigoTipoControlProceso" = CTP."CodigoControlProcesoTipo"
INNER JOIN "BSCL.Plataforma.BD::PLA.Diccionario" DIC ON DIC."CodDiccionario" = CDR."Mensaje"
INNER JOIN "ObtenerFechaActual" FEC ON FEC."IdEmpresa" = CTP."IdEmpresa"
WHERE CDR."FechaEjecucion" = :DiaAnterior 
AND CDR."IdEmpresa" = :IdEmpresa