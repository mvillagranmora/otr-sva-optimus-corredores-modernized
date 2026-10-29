SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarConfiguracionAFP"; 
GO
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarConfiguracionAFP" AS
SELECT 
	conAFP."IdConfiguracionAFP" AS "IdConfiguracionAFP",
	afp."NombreAFP" AS "NombreAFP",
	multi."NombreMultifondo" AS "NombreMultifondo",
	ident."DocumentoIdentidad" AS "DocumentoCliente",
	per."NombreCompleto" AS "NombreCliente",
	por."CodigoPortafolio" AS "CodigoPortafolio"
FROM "BSCL.Facturacion.BD::FAC.ConfiguracionAFP" AS conAFP
INNER JOIN "BSCL.Facturacion.BD::FAC.AFP" AS afp
ON  conAFP."IdAFP" = afp."IdAFP"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Multifondos" AS multi
ON conAFP."IdMultifondo" = multi."IdMultifondo"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS cli
on cli."IdCliente" = conAFP."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS per
on cli."IdPersona" = per."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS ident
on ident."IdPersona" = per."IdPersona"
AND ident."Predeterminado" = 'S'
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS por
on por."IdPortafolio" = conAFP."IdPortafolio"
WHERE cli."Borrado" = 'N'
GROUP BY conAFP."IdConfiguracionAFP",
	afp."NombreAFP",
	multi."NombreMultifondo",
	ident."DocumentoIdentidad",
	per."NombreCompleto",
	por."CodigoPortafolio"
ORDER BY "NombreAFP", 
	"NombreCliente", 
	"NombreMultifondo"
	
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarConfiguracionAFP" TO BCS_DESARROLLADOR WITH GRANT OPTION;