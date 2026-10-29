SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarCSCFull";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarCSCFull"
AS
SELECT CSC."IdFolioCSC",
	CSC."FolioCSC",
	CSC."IdCliente",
	CSC."IdPortafolio",
	CSC."Fecha",
	CSC."MontoPesos",
	CSC."MontoUF",
    CSC."TipoPago",
    CSC."NombreMedioPago",
    CSC."DocumentoIdentidad",
    CSC."NombreCompleto",
    CSC."IdBanco",
    CSC."NombreBanco",
    CSC."IdEstado",
    CSC."Estado",
    CSC."IdEstadoPAC",
    CSC."EstadoPAC",
    CSC."NombreFuncionario", 
    CSC."DomicilioSucursal", 
    CSC."IdEmpresa", 
    CSC."DocumentoIdentificacionFuncionario", 
    CSC."DiaDescuento", 
    CSC."CodigoSucursal",
    CSC."AceptoCondiciones", 
    CSC."FirmaFuncionario",
    CSC."CodigoPortafolio",
    CSC."Hora",
    CSC."Nombre",
    CSC."CodigoPropositoPortafolio",
    CSC."Canal",
    CSC."FolioRelacionado",
    CSC."CuentaCorriente",
    CSC."IdMatrizAgrupador",
    CSC."EsADC",
    
    CSCD."IdCSCDetalle",
	CSCD."IdSerieFondo",
	CSCD."NombreSerieFondo",
	CSCD."NombreFondo",
	CSCD."Monto",
	CSCD."Porcentaje",
	CSCD."Regimen",
	CANAL."Nombre" AS "DescripcionCanal"

FROM "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotas" CSC
LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarContratoSuscripcionCuotasDetalle" CSCD ON (CSCD."IdFolioCSC" = CSC."IdFolioCSC")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" CANAL ON (CANAL."IdTipoCanalIngreso" = CSC."Canal");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarCSCFull" TO BCS_DESARROLLADOR WITH GRANT OPTION;