WITH "ObtenerFechaActual" AS (
	SELECT TO_NVARCHAR(CURRENT_TIMESTAMP,'YYYY-MM-DD HH24:MI:SS') AS "FechaInicio"
	      ,:IdEmpresa AS "IdEmpresa"
	FROM DUMMY
),
"ModulosTotales" AS (

SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_MESA_SPOT_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'MSP'

UNION 
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_MESA_RF_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'MRF'

UNION 
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_TESORERIA_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'TES'

UNION
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_FACTURACION_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'FAC'

UNION
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_FFMM_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'FMM'

UNION 
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_MESA_FORWARD_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'MFW'

UNION
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_SISTEMA_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'SIS'

UNION 
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_CONTABILIDAD_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'CON'

UNION
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_CUSTODIA_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'CUS'

UNION
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_CUENTA_INVERSION_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'CTA'

UNION
SELECT "NombreModulo", "CodigoModulo", "NombreEstado", 'MODULO_ORDENES_ABT' AS "CodigoTipoControlProceso"
FROM "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle"
WHERE "IdEmpresa" = :IdEmpresa AND "CodigoModulo" = 'ORD')

SELECT MOD."NombreModulo", MOD."NombreEstado" AS "Estado", CTP."CodigoControlProcesoTipo", FEC."FechaInicio"
FROM "BSCL.Plataforma.BD::CTR.ControlProcesoTipo" CTP
INNER JOIN "ModulosTotales" MOD ON MOD."CodigoTipoControlProceso" = CTP."CodigoControlProcesoTipo"
INNER JOIN "ObtenerFechaActual" FEC ON FEC."IdEmpresa" = CTP."IdEmpresa"