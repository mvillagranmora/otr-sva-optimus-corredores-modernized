SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarRegistroCategoriaInversionista"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarRegistroCategoriaInversionista" AS

SELECT 
  r."IdEmpresa"
 , r."IdCliente"
 , r."IdRegistroEncuesta"
 , r."IdCategoriaInversionista"
 , "AsignadoPorEncuesta"
 , "NombreEncuesta"
 , r."Puntaje" AS "PuntajeTotal"
 , r."FechaIngresoRegistro"
 , "FechaEncuesta" as "FechaRegistroCategoria"
 , "IdUsuarioResponde"
 ,"NombreCategoriaInversionista"
 ,"CodigoUsuario" as "CodigoUsuarioResponde"
 ,p."IdPortafolio"
 ,COALESCE(p."CodigoPortafolio",'')  AS "CodigoPortafolio"
 ,COALESCE(r."IdPerfilProductos",'')  AS "IdPerfilProductos"
 ,CASE WHEN r."IdCanal"= 'AGE' THEN 'AGENTE' ELSE  CASE WHEN r."IdCanal"='CLI'THEN 'CLIENTE'ELSE CASE WHEN r."IdCanal"='OPT'THEN 'OPTIMUS' ELSE  CASE WHEN r."IdCanal"='WS'THEN 'WS' ELSE '' END END END END  AS "IdCanal"
 ,CASE WHEN r."AsignadoPorEncuesta"='N' THEN 'Manual' ELSE CASE WHEN r."AsignadoPorEncuesta"='S' THEN 'Encuesta' ELSE CASE WHEN r."AsignadoPorEncuesta"='P'THEN 'Planilla Portafolio' ELSE 'Producto Extra' END  END  END AS "TipoIngreso"
FROM "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta" r
LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" c on r."IdCategoriaInversionista" = c."IdCategoriaInversionista" and r."IdEmpresa" = c."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" a on r."IdUsuarioResponde" = a."IdUsuario"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON p."IdPortafolio"= r."IdPortafolio"

UNION

SELECT DISTINCT 		
 "CLI"."IdEmpresa"
,"CLI"."IdCliente"
,"LOG"."IdLog" AS "IdRegistroEncuesta"
,"ValorNuevo" AS"IdCategoriaInversionista"
,'N' AS "AsignadoPorEncuesta"
,"NombreProceso" AS "NombreEncuesta"
, 0 AS "PuntajeTotal"
,"FechaLog" AS "FechaIngresoRegistro"
,CAST("FechaRegistro" AS SECONDDATE) AS "FechaRegistroCategoria"
,"USER"."IdUsuario"AS "IdUsuarioResponde"
,"ValorNuevo" AS "NombreCategoriaInversionista"
,"CodigoUsuario" as "CodigoUsuarioResponde"
,"PORT"."IdPortafolio"
,"PORT"."CodigoPortafolio" AS "CodigoPortafolio"
,"ValorNuevo" AS "IdPerfilProductos"
 ,CASE WHEN "IdModulo"= 'AGE' THEN 'AGENTE' ELSE  CASE WHEN "IdModulo"='CLI'THEN 'CLIENTE'ELSE CASE WHEN "IdModulo"='OPT'THEN 'OPTIMUS' ELSE "IdModulo" END END  END  AS "IdCanal"
,"NombreProceso" AS "TipoIngreso" 		
FROM "BSCL.Plataforma.BD::PLA.Log"             AS "LOG"
LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS "DET"  ON ("DET"."IdLog" = "LOG"."IdLog")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS "CLI"  ON ("CLI"."IdCliente" = "LOG"."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" AS "CLOG" ON ("CLOG"."CodigoCampo" = "DET"."CampoRegistro")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario"    AS "USER" ON ("USER"."IdUsuario" = "LOG"."IdUsuario")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" ON ("PORT"."IdPortafolio" = "CLI"."IdPortafolioDefecto")
INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista" AS "CAT" ON ("CAT"."IdCategoriaInversionista" = "ValorNuevo")
WHERE "CLOG"."AfectaFechaModificacion" = 'N' 
AND NOT "LOG"."IdCliente" = 0
GROUP BY  "CLI"."IdEmpresa","CLI"."IdCliente","LOG"."IdLog" , "ValorNuevo","NombreProceso","FechaLog","USER"."IdUsuario"
,"ValorNuevo","CodigoUsuario" ,"PORT"."IdPortafolio","PORT"."CodigoPortafolio" ,"ValorNuevo" ,"IdModulo","NombreProceso","FechaRegistro" 
;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarRegistroCategoriaInversionista" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
