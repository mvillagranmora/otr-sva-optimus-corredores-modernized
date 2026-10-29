SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPACAnalitica";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPACAnalitica" AS
SELECT PAC."IdMandatoPAC"
        ,CC."DocumentoIdentidad"
        ,CC."NombreCompleto"
        ,PAC."FolioCSC"
        ,PAC."FolioSAAPV"
        ,PAC."Receptor"
        ,PAC."Fecha"
        ,PAC."NumeroMandato" AS "NroMandato"
        ,PAC."Firma"
        ,PAC."Estado"
        ,PAC."IdPortafolio"
        ,CBP."NombrePlaza"
        ,CBP."NombreMoneda"
        ,CBP."IdCuentaBancaria"
        ,CBP."NombreTipoCuentaBancaria" AS "NombreTipoCtaBancaria"
        ,CBP."NombreBanco"
        ,CBP."NumeroCuentaBancaria" AS "NumeroCtaBancaria"
        ,CC."IdEstado" AS "EstadoCliente"
        ,PAC."Canal"
        ,PAC."EstadoCargo"
        ,COALESCE (CSC."MontoPesos",COALESCE(MONTOAPV."MontoPesos",0.0)) AS "MontoPesos" 
        ,COALESCE (CSC."MontoUF",COALESCE(MONTOAPV."MontoUF",0.0)) AS "MontoUF"
        ,PAC."IdentServicio" AS "FolioPACMandatoAgrupador" 
    FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" PAC
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC 
        ON (CC."IdCliente" = PAC."IdCliente") 
    LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SN 
        ON (SN."IdCliente" = PAC."IdCliente") 
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CBP 
        ON (CBP."IdCuentaBancaria" = PAC."IdCuentaBancaria" AND CBP."IdPersona" = SN."IdPersona") 
    LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS CSC 
        ON (PAC."FolioCSC" = CSC."FolioCSC") 
    LEFT JOIN  (
        SELECT SUM (POR."MontoPesos") AS "MontoPesos", SUM (POR."MontoUF") AS "MontoUF",SAP."FolioContratoSAAPV" 
        FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" POR 
        INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS SAP ON (POR."IdContratoSAAPV"=SAP."IdContratoSAAPV") 
        GROUP BY SAP."FolioContratoSAAPV") AS MONTOAPV 
        ON (MONTOAPV."FolioContratoSAAPV" = cast (PAC."FolioSAAPV" as varchar));  

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPACAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
