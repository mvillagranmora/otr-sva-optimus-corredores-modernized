SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarClientePortafolioOperaCuentaInversion";
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarClientePortafolioOperaCuentaInversion" AS 
SELECT
CP."IdPortafolio", 
CP."CodigoPortafolio",
CP."IdEstado" AS "IdEstadoPortafolio",
CP."Estado" AS "EstadoPortafolio",
CLI."IdCliente",
CLI."DocumentoIdentidad", 
CLI."IdEmpleadoEjecutivo",
CLI."NombreCompleto",
CLI."IdEmpresa",
CLI."IdEstado" AS "IdEstadoCliente",
CLI."NombreEstado" AS "EstadoCliente",
CIP."CodigoTipoIndicador" AS "CodigoIndicadorCuentaInversion", 
CIP."ValorIndicador" AS "OperaCuentaInversion"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"  CP
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI on CP."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" CIP ON CIP."IdPortafolio" = CP."IdPortafolio" AND "CodigoTipoIndicador"  = 'GEN_OCCINV';

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarClientePortafolioOperaCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
