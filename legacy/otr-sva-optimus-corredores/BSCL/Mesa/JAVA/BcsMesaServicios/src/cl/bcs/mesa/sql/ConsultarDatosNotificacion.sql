SELECT  OPE."FolioOperacion", 
		OPE."NombreOperacion" AS "NombreTipoOperacion", 
		OPE."RazonSocial" AS "Cliente", 
		OPE."IdAgente", 
		AGE."NombreCompleto" AS "Agente", 
		OPE."DocumentoIdentidad",
		OPE."IdPortafolio", 
		POR."CodigoPortafolio" AS "Portafolio", 
		TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
		OPE."IdOperacion", 
		OPE."NombreEstado" AS "Estado",
		PROD."Nombre" AS "Producto",
		OPE."NombreOperacion" AS "Operacion",
		OPE."MontoOperacionFinal" AS "MontoOperacionMoneda", 
		OPE."IdTipoReajuste", 
		OPE."MontoOperacionMoneda" AS "MontoOperacion",
		TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento",
		CLI."IdPersona",
		MAIL."IdDireccionElectronica" AS "IdDireccionElectronicaPrincipal",
		MAIL."IdDireccionElectronica" AS "IdDireccionElectronicaPrincipalCLI"
FROM	"BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AGE ON AGE."IdPersona" = OPE."IdAgente"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON POR."IdPortafolio" = OPE."IdPortafolio"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo" PROD ON OPE."IdTipoProducto" = PROD."IdTipoProductoMesa"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OPE."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" MAIL ON MAIL."IdPersona" = CLI."IdPersona" AND MAIL."Predeterminado" = 'UI_SI'
INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP  ON (TMP."IdOperacion" = OPE."IdOperacion");