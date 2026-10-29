SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotas";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotas"
AS
SELECT 
	CSC."IdFolioCSC",
	COALESCE (CSC."FolioCSC", '0') AS "FolioCSC",
	CSC."IdCliente",
	CSC."IdPortafolio",
	CSC."Fecha",
	CSC."MontoPesos",
	CSC."MontoUF",
    CSC."TipoPago",
    MP."Nombre" AS 	"NombreMedioPago",
    CC."DocumentoIdentidad",
    CC."NombreCompleto",
    CSC."IdBanco",
    COALESCE(BA."NombreBanco",'') AS "NombreBanco",
    CSC."Estado" AS "IdEstado",
    COALESCE (ES."Nombre", CSC."Estado") AS "Estado",
    MPAC."Estado" AS "IdEstadoPAC",
    EST."Nombre" AS "EstadoPAC",
    
    CSC."NombreFuncionario", 
    CSC."DomicilioSucursal", 
    CSC."IdEmpresa", 
    CSC."DocumentoIdentificacionFuncionario", 
    CSC."DiaDescuento", 
    CSC."CodigoSucursal",
    CSC."AceptoCondiciones", 
    CSC."FirmaFuncionario",
    CP."CodigoPortafolio",
    CSC."Hora",
    MP."Nombre",CP."CodigoPropositoPortafolio",
    CSC."Canal",
    CSC."FolioRelacionado",
    MPAC."CuentaCorriente",
    CP."IdMatrizAgrupador",
    CP."EsADC",
    CANAL."Nombre" 	AS "DescripcionCanal",
    CSC."MLT" 		AS "IndicadorMLT",
    CASE WHEN CSC."MontoPesos" > 0 then CSC."MontoPesos"
    	 WHEN CSC."MontoUF"    > 0 then CSC."MontoUF"
    	 WHEN CSC."MontoUSD"    > 0 then CSC."MontoUSD"
    END AS "Monto",
    CASE WHEN CSC."MontoPesos" > 0 then 'CLP'
    	 WHEN CSC."MontoUF"    > 0 then 'UF'
    	 WHEN CSC."MontoUSD"    > 0 then 'USD'
    END AS "IdMoneda"
FROM  "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS CSC
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = CSC."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON (CC."IdCliente" = CSC."IdCliente")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BA ON (BA."IdBanco" = CSC."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ES ON (ES."IdEstado" = CSC."Estado")
LEFT JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS MPAC ON (MPAC."FolioCSC" = CSC."FolioCSC")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = MPAC."Estado")
INNER JOIN "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarMediosDePagoCSC" AS MP ON (MP."IdCSCPago" = CSC."TipoPago")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" CANAL ON (CANAL."IdTipoCanalIngreso" = CSC."Canal");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;