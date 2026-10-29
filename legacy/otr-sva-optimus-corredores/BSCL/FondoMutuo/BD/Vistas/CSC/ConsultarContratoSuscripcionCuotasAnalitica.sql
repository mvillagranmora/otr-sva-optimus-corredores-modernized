SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasAnalitica" AS
SELECT 
	CSC."IdFolioCSC",
	COALESCE (CSC."FolioCSC", '0') AS "Folio",
	CSC."Fecha",
	CSC."MontoPesos",
	CSC."MontoUF",
	CSC."TipoPago",
	MP."Nombre" AS 	"NombreMedioPago",
	CC."DocumentoIdentidad",
	CP."CodigoPortafolio",
	CC."NombreCompleto",
	COALESCE(BA."NombreBanco",'') AS "NombreBanco",
	CSC."Estado" AS "IdEstado",
	COALESCE (ES."Nombre", CSC."Estado") AS "Estado",
	CSC."DiaDescuento",
	CSC."FolioRelacionado",
	CSC."Hora",
	CSC."IdCliente",
	CSC."IdPortafolio"
FROM  "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS CSC
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS CP ON (CP."IdPortafolio" = CSC."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON (CC."IdCliente" = CSC."IdCliente")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BA ON (BA."IdBanco" = CSC."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ES ON (ES."IdEstado" = CSC."Estado")
INNER JOIN "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarMediosDePagoCSC" AS MP ON (MP."IdCSCPago" = CSC."TipoPago")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" CANAL ON (CANAL."IdTipoCanalIngreso" = CSC."Canal");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
