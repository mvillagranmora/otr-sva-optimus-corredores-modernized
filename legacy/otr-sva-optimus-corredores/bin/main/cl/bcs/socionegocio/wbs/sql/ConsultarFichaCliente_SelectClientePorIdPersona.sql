SELECT
COALESCE(Cliente."IdCliente",0) AS "IdCliente"
,COALESCE(Cliente."IdPersona",0) AS "IdPersona"
,COALESCE(Cliente."IdCategoriaInversionista",'') AS "IdCategoriaInversionista"
,COALESCE(CategoriaInversionista."NombreCategoriaInversionista",'') AS "CategoriaInversionista"
,COALESCE(Cliente."IdPersonaEjecutivo",0) AS "CodigoEjecutivo"
,COALESCE(Ejecutivo."NombreCompleto",'') AS "Ejecutivo"
,COALESCE(Cliente."IdTipoCliente",'') AS "IdTipoCliente"
,COALESCE(TipoCliente."Nombre",'') AS "TipoCliente"
,COALESCE(Cliente."IdTipoBloqueo",'') AS "IdTipoBloqueo"
,COALESCE(Bloqueo."NombreTipoBloqueo",'') AS "NombreTipoBloqueo"
,COALESCE(Cliente."IdSegmentoCliente",0) AS "IdSegmentoCliente"
,COALESCE(TipoSegmento."DescripcionSegmento",'') AS "SegmentoCliente"
,COALESCE(Cliente."TipoEnrolamiento",'') AS "TipoEnrolamiento"
,COALESCE(Cliente."IdEstado",'') AS "IdEstado"
,COALESCE(Estado."Nombre",'') AS "Estado"
,COALESCE(CAST(UltimaModificacion."FechaUltimaModificacion" as varchar),'0001-01-01') AS "UltimaModificacion"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoCliente" AS TipoCliente ON TipoCliente."IdTipoCliente" = Cliente."IdTipoCliente" AND TipoCliente."IdEmpresa" = Cliente."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" AS CategoriaInversionista ON CategoriaInversionista."IdCategoriaInversionista" = Cliente."IdCategoriaInversionista" AND  CategoriaInversionista."IdEmpresa" = Cliente."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = Cliente."IdPersonaEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" AS Ejecutivo ON Ejecutivo."IdPersonaEjecutivo" = Cliente."IdPersonaEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS TipoSegmento ON TipoSegmento."IdSegmentoCliente" = Cliente."IdSegmentoCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON Estado."IdEstado" = Cliente."IdEstado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionCliente" UltimaModificacion ON Cliente."IdCliente" = UltimaModificacion."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS Bloqueo ON
(Bloqueo."IdTipoBloqueo" = Cliente."IdTipoBloqueo" AND Bloqueo."IdTipoEntidad"='CLIENTE' AND Bloqueo."IdEmpresa" = Cliente."IdEmpresa")
WHERE Cliente."IdPersona" IN(:p_IdPersonas);