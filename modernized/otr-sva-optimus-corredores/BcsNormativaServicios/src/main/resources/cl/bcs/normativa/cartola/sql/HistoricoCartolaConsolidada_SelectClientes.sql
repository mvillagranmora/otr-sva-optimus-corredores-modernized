SELECT
CLI."NombreCompleto"
,CLI."IdCliente"
,CLI."IdPersona"
,CLI."NombreCategoriaInversionista"
,CLI."IdEmpleadoEjecutivo"
,CLI."NombreEmpleadoEjecutivo"
,POR."IdPortafolio"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON CLI."IdCliente" = POR."IdCliente"
--where CLI."IdCliente" IN (1423323);--, 123569, 249194 );
ORDER BY CLI."IdCliente"