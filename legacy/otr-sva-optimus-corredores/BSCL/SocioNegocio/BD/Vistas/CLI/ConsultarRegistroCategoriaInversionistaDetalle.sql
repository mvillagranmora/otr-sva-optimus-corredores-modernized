SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarRegistroCategoriaInversionistaDetalle"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarRegistroCategoriaInversionistaDetalle" AS

SELECT 
 r."IdEmpresa"
 ,r."IdCliente"
 ,r."IdRegistroEncuesta"
 ,r."IdCategoriaInversionista"
 ,r."AsignadoPorEncuesta"
 ,r."NombreEncuesta"
 ,r."Puntaje" AS "PuntajeTotal"
 ,r."FechaIngresoRegistro"
 ,r."FechaEncuesta"
 ,r."IdUsuarioResponde"
 ,c."NombreCategoriaInversionista"
 ,a."CodigoUsuario" as "CodigoUsuarioResponde"
 ,p."IdRegistroPregunta"
 ,p."TextoPregunta"
 ,rr."IdRegistroRespuesta"
 ,rr."TextoRespuesta"
 ,rr."Puntaje" AS "PuntajeRespuesta" 
 ,rr."Seleccionado"
 ,per."NombreCompleto"
 ,CASE WHEN r."IdCanal"= 'AGE' THEN 'AGENTE' ELSE  CASE WHEN r."IdCanal"='CLI'THEN 'CLIENTE'ELSE CASE WHEN r."IdCanal"='OPT'THEN 'OPTIMUS' ELSE '' END END  END  AS "IdCanal"
FROM "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta" r
LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" c on r."IdCategoriaInversionista" = c."IdCategoriaInversionista" and r."IdEmpresa" = c."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" a on r."IdUsuarioResponde" = a."IdUsuario"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.RegistroPregunta" p on r."IdRegistroEncuesta" = p."IdRegistroEncuesta"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.RegistroRespuesta" rr on p."IdRegistroPregunta" = rr."IdRegistroPregunta"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli ON cli."IdCliente" = r."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per ON per."IdPersona" = cli."IdPersona";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarRegistroCategoriaInversionistaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
