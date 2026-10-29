SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarContratosComisionesConvenios"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarContratosComisionesConvenios"  
AS
SELECT CC."IdContratoComision", 
CC."Nombre" AS "NombreContratoComision",
CC."Descripcion" AS "DescripcionContratoComision",
CC."IdTipoContrato",
TC."Nombre" AS "TipoContrato",
CC."IdClasificacionTipoInstrumento",
CTI."Nombre" AS "NombreTipoInstrumento",
CC."IdPortafolio",
PORTAFOLIO."CodigoPortafolio",
DOCID."DocumentoIdentidad",
PERSONANOMBRECOMPLETO."NombreCompleto" AS "NombrePersonaCompleto",
CC."IdEstado",
ESTADO."Nombre" AS "NombreEstado"
FROM "BSCL.Plataforma.BD::COM.ContratoComision" AS CC
LEFT JOIN "BSCL.Plataforma.BD::COM.TipoContrato" AS TC ON TC."IdTipoContrato" = CC."IdTipoContrato"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTI ON CTI."IdClasificacionTipoInstrumento" = CC."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = CC."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON CLIENTE."IdCliente" = PORTAFOLIO."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON PERSONA."IdPersona" = CLIENTE."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCID ON DOCID."IdDocumentoIdentidad" = PERSONA."IdDocumentoIdentidad" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS PERSONANOMBRECOMPLETO ON PERSONANOMBRECOMPLETO."IdPersona" = CLIENTE."IdPersona" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADO ON ESTADO."IdEstado" = CC."IdEstado"
WHERE CC."IdTipoContrato" = 'CONV';
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarContratosComisionesConvenios" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
