SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogCierreModulos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogCierreModulos" AS

SELECT DISTINCT 
	T1."IdLog", 
	T1."Tipo", 
	T1."IdUsuario", 
	"Usuario"."CodigoUsuario", 
	T1."IdEmpresa", 
	T1."FechaProceso", 
	TO_VARCHAR(T1."FechaProceso", 'HH24:MI:SS') AS "HoraProceso",
	T1."IdRegistro", 
	T1."TipoEvento", 
	T1."IdModuloCierre"
FROM
(SELECT DISTINCT
	"Log"."IdLog",
	"DetalleLog"."ValorNuevo" AS "Tipo",
	"Log"."IdUsuario" AS "IdUsuario" ,
	"Log"."IdEmpresa" AS "IdEmpresa", 
	"Log"."FechaLog" AS "FechaProceso", 
	"Log"."IdRegistro" AS "IdRegistro",
	"Evento"."IdTipoEvento" AS "TipoEvento",
	"Evento"."NumeroOperacionConcepto" AS "IdModuloCierre"
FROM "BSCL.Plataforma.BD::PLA.Log" AS "Log"
INNER JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS "DetalleLog" ON "Log"."IdLog" = "DetalleLog"."IdLog" AND "DetalleLog"."SubRegistro" = 'Evento'
INNER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS "Evento" ON "Log"."IdRegistro" = CAST("Evento"."IdEvento" AS NVARCHAR)
WHERE "Log"."IdTipoLog" = 'EDICION' AND "Log"."IdModulo" = 'AUT'
AND "Log"."IdTipoRegistro" = 'AUTORIZACION'
AND "Evento"."IdTipoEvento" IN ('CIEMOD', 'APEMOD')
UNION ALL
SELECT DISTINCT
	"Log"."IdLog",
	'SOL' AS "ValorNuevo",
	"Evento"."IdUsuario",
	"Log"."IdEmpresa", 
	"Evento"."FechaHoraRegistro", 
	"Log"."IdRegistro",
	"Evento"."IdTipoEvento",
	"Evento"."NumeroOperacionConcepto"
FROM "BSCL.Plataforma.BD::PLA.Log" AS "Log"
INNER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS "Evento" ON "Log"."IdRegistro" = CAST("Evento"."IdEvento" AS NVARCHAR)
WHERE "Log"."IdTipoLog" = 'EDICION' AND "Log"."IdModulo" = 'AUT'
AND "Log"."IdTipoRegistro" = 'AUTORIZACION'
AND "Evento"."IdTipoEvento" IN ('CIEMOD', 'APEMOD')) T1
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS "Usuario" ON T1."IdUsuario" = "Usuario"."IdUsuario"
ORDER BY T1."FechaProceso" DESC;


/* Select 
	'' AS "Tipo",
	'' AS "Evento",
	'' AS "Proceso",
	'' AS "Estado",
	'' AS "Hora"	
from DUMMY where 1 = 2 */

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogCierreModulos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
