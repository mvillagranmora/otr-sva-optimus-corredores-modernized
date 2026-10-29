SELECT COALESCE(CC."IdDireccionElectronicaPrincipal",0) AS "IdDireccionElectronicaActual",
		CC."IdCliente"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC 
WHERE CC."IdCliente" in (:p_IdCliente);

