SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogAnalitica" AS
SELECT
"Log"."IdLog",
"Log"."IdModulo",
"Log"."IdTipoLog",
"Log"."IdTipoRegistro",
"Log"."NombreProceso",
"Log"."FechaRegistro",
'UTC' AS "TimeZone",
COALESCE("Log"."Nemo",'') as "Nemo",
COALESCE("DetalleLog"."IdDetalleLog",0) AS "IdDetalleLog",
COALESCE("DetalleLog"."SubRegistro",'') AS "SubRegistro",
COALESCE("DetalleLog"."CampoRegistro",'') AS "CampoRegistro",
LEFT(COALESCE("DetalleLog"."ValorAntiguo",''),2000) AS "ValorAntiguo",
LEFT(COALESCE("DetalleLog"."ValorNuevo",''),2000) AS "ValorNuevo",
"Log"."IdUsuario",
COALESCE("Usuario"."CodigoUsuario",'') AS "CodigoUsuario",
"Log"."IdUsuarioAtendido",
COALESCE("Log"."CodigoAutenticacion",'') AS "CodigoAutenticacionLog",
COALESCE("DetalleLog"."CodigoAutenticacion",'') AS "CodigoAutenticacionDetalleLog"
FROM "BSCL.Plataforma.BD::PLA.Log" AS "Log"
LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS "DetalleLog" ON "Log"."IdLog" = "DetalleLog"."IdLog"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS "Usuario" ON "Usuario"."IdUsuario" = "Log"."IdUsuario";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;