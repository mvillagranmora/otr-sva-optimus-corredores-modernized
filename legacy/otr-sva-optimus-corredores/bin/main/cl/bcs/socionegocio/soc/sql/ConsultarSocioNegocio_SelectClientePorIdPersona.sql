SELECT 
COALESCE(Cliente."IdCliente",0) AS IdCliente
,COALESCE(Cliente."IdCategoriaInversionista",'') AS "IdCategoriaInversionista"
,COALESCE(CategoriaInversionista."NombreCategoriaInversionista",'') AS "CategoriaInversionista"
,COALESCE(Cliente."IdPersonaEjecutivo",0) AS "CodigoEjecutivo"
,COALESCE(Ejecutivo."NombreCompleto",'') AS "Ejecutivo"
,COALESCE(Cliente."IdTipoCliente",'') AS "IdTipoCliente"
,COALESCE(TipoCliente."Nombre",'') AS "TipoCliente"
,COALESCE(TipoSegmento."NombreSegmentoCliente",'') AS "SegmentoCliente"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoCliente" AS TipoCliente ON TipoCliente."IdTipoCliente" = Cliente."IdTipoCliente"
LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" AS CategoriaInversionista ON CategoriaInversionista."IdCategoriaInversionista" = Cliente."IdCategoriaInversionista"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = Cliente."IdPersonaEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS TipoSegmento ON TipoSegmento."IdSegmentoCliente" = Cliente."IdSegmentoCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" AS Ejecutivo ON Ejecutivo."IdPersonaEjecutivo" = Cliente."IdPersonaEjecutivo"
WHERE Cliente."IdPersona" = :p_IdPersona
LIMIT 1