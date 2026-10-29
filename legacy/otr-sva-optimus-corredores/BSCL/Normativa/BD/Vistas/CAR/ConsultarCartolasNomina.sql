SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarCartolasNomina";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarCartolasNomina" AS

SELECT
    NOM."IdEmpresa",
    NOM."Periodo",
    CAR."IdCartola",
    CAR."IdNomina",
    CAR."IdCliente",
    CAR."IdPortafolio",
    CAR."Reprocesar",
    CASE CAR."Reprocesar" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "ReprocesarUI",
    CAR."Notificar",
    CAR."EstadoProceso",
    CAR."MensajeProceso",
    DOC."DocumentoIdentidad",
    CLI."NombreCompleto" AS "NombreCliente",
    DIR."Direccion",
    POR."CodigoPortafolio",
    EST."Nombre" AS "NombreEstado"
FROM "BSCL.Normativa.BD::CAR.Cartola" CAR
LEFT JOIN "BSCL.Normativa.BD::CAR.Nomina" NOM ON CAR."IdNomina" = NOM."IdNomina"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON CAR."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CAR."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON CLI."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIR ON CLI."IdDireccionElectronicaPrincipal" = DIR."IdDireccionElectronica"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON (CAR."EstadoProceso" = EST."IdEstado")
ORDER BY NOM."Periodo" DESC;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarCartolasNomina"
    TO  BCS_DESARROLLADOR WITH GRANT OPTION;
