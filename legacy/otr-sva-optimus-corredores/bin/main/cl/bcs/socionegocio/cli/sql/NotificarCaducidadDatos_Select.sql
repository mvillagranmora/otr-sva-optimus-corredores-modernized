SELECT "CLI"."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronica", 
"CLI"."IdCliente" AS "IdCliente", 
"CLI"."NombreCompleto" AS "Nombre",
"CLI"."DocumentoIdentidad" AS "DocumentoIdentidad",
"VEN"."FechaVencimiento" AS "FechaVencimiento",
"CLI"."IdPersona" AS "IdPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS "DIR" ON "CLI"."IdDireccionElectronicaPrincipal" = "DIR"."IdDireccionElectronica"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaVencimientoCliente" AS "VEN" ON "CLI"."IdCliente" = "VEN"."IdCliente"
WHERE "VEN"."FechaVencimiento" = :fechaConsulta