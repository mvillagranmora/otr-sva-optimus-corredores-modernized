SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarArchivosContrato";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarArchivosContrato"  
AS
SELECT
AC."IdEmpresa",
AC."CodigoArchivo",
AC."Descripcion",
AC."IdUsuario",
CU."CodigoUsuario",
AC."FechaModificacion",
AC."Binario",
AC."TipoBinario",
AC."TipoPresentacion",
AC."EsEstatico",
AC."IdTemplate",
AC."Datasource",
AC."IdTipoPersona",
CTP."Descripcion" 			AS "TipoPersona",
AC."IdTipoCliente" 			AS "TipoCliente",
TCC."DetalleIndicador"		AS "TipoClienteCMF",
TCC."IdDetalleIndicador"	AS "IdTipoClienteCMF",
AC."IdEstado",
E."Nombre" 					AS "Estado"
FROM "BSCL.Plataforma.BD::CTT.ArchivosContrato" AC
INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" CU ON AC."IdUsuario" = CU."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" E ON E."IdEstado" = AC."IdEstado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" CTP ON CTP."IdTipoPersona" = AC."IdTipoPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoClienteCmf" TCC ON TCC."IdDetalleIndicador" = AC."IdTipoClienteCmf";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CTT.ConsultarArchivosContrato" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
