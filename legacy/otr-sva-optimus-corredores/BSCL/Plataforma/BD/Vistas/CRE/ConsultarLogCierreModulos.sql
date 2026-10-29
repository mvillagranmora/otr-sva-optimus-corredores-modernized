SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarLogCierreModulos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarLogCierreModulos" AS

SELECT DISTINCT 
	T1."IdLog", 
	T1."Tipo", 
	T1."IdUsuario", 
	"Usuario"."CodigoUsuario", 
	T1."IdEmpresa", 
	T1."FechaProceso", 
	T1."FechaProceso" AS "HoraProceso",
	T1."FechaLog",
	T1."IdRegistro", 
	T1."TipoEvento", 
	T1."IdModuloCierre"
FROM
(
	SELECT DISTINCT
		"Log"."IdLog",
		"DetalleLog"."ValorNuevo" AS "Tipo",
		"Log"."IdUsuario" AS "IdUsuario" ,
		"Log"."IdEmpresa" AS "IdEmpresa", 
		"Log"."FechaLog" AS "FechaProceso", 
		"Log"."FechaLog",    -- Fecha de sistema en que se registró el log
		"Log"."IdRegistro" AS "IdRegistro",
		"Evento"."IdTipoEvento" AS "TipoEvento",
		"Evento"."NumeroOperacionConcepto" AS "IdModuloCierre"
	FROM "BSCL.Plataforma.BD::PLA.Log" AS "Log"
	INNER JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS "DetalleLog" ON "Log"."IdLog" = "DetalleLog"."IdLog" AND "DetalleLog"."SubRegistro" = 'Evento'
	INNER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS "Evento" ON "Log"."IdRegistro" = TO_NVARCHAR("Evento"."IdEvento")
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
		"Log"."FechaLog",   -- Fecha de sistema en que se registró el log
		"Log"."IdRegistro",
		"Evento"."IdTipoEvento",
		"Evento"."NumeroOperacionConcepto"
	FROM "BSCL.Plataforma.BD::PLA.Log" AS "Log"
	INNER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS "Evento" ON "Log"."IdRegistro" = TO_NVARCHAR("Evento"."IdEvento")
	WHERE "Log"."IdTipoLog" = 'EDICION' AND "Log"."IdModulo" = 'AUT'
	AND "Log"."IdTipoRegistro" = 'AUTORIZACION'
	AND "Evento"."IdTipoEvento" IN ('CIEMOD', 'APEMOD')

	--Solo eventos que se ejecutan sin autorizacion.
	UNION ALL
	
	SELECT DISTINCT
		L."IdLog",
		DL."ValorNuevo" AS "ValorNuevo",
		L."IdUsuario",
		L."IdEmpresa", 
		L."FechaRegistro", 
		L."FechaLog",   -- Fecha de sistema en que se registró el log
		L."IdRegistro",
		L."NombreProceso",
		'0' AS "NumeroOperacionConcepto"
	FROM "BSCL.Plataforma.BD::PLA.Log" L
	INNER JOIN "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" MFP ON MFP."NombreFuncionProceso" = L."NombreProceso"
	INNER JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS DL ON L."IdLog" = DL."IdLog"
) T1
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS "Usuario" ON T1."IdUsuario" = "Usuario"."IdUsuario"
ORDER BY T1."FechaProceso" DESC;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CRE.ConsultarLogCierreModulos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


