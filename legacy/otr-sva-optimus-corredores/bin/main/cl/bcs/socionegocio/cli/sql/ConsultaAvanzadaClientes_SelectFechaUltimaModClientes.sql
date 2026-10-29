SELECT 
fumc."FechaUltimaModificacion" AS "FechaSistema",
fumc."IdCliente"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionCliente" fumc
JOIN "BSCL.SocioDeNegocio.BD::GBL.ConsultaAvanzadaClientes_Proceso_Paso" capp ON capp."IdCliente" = fumc."IdCliente";