SELECT 
    NOM."IdNomina",
    NOM."IdPersona",
    NOM."IdCliente",
    NOM."IdPortafolio",
    DIRE."IdDireccionElectronica",
    CCDG."RazonSocial" AS "Corredora",
    CCDG."Telefono" AS "AtencionCliente",
    PER."IdDireccionElectronicaPrincipal", 
    PER."NombreCompleto",
    PER."DocumentoIdentidad",
    PER."TipoDocumentoIdentidad",
    EMP."NombreCompleto" AS "Agente",
    EMP."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronicaPrincipalAgente",
    EMP."IdDireccionElectronica" AS "IdDireccionElectronicaAgente",
    EMP."IdPersona" AS "IdAgente",
    NOM."CodigoPortafolio"
FROM "BSCL.Normativa.BD::CIR.Nomina" NOM
INNER JOIN "BSCL.Normativa.BD::DOC.Documento" AS DOC ON (NOM."IdDocumento" = DOC."IdDocumento")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS PER ON (PER."IdPersona"= NOM."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP ON (PER."IdEmpleadoEjecutivo"=EMP."IdEmpleado")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS DIRE ON
    (PER."IdDireccionElectronicaPrincipal" = DIRE."IdDireccionElectronica" AND NOM."IdPersona" = DIRE."IdPersona" AND DIRE."CodigoProposito" = 'CIRC')
LEFT JOIN "BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente" CER ON (NOM."IdCliente" = CER."IdCliente" AND NOM."IdPortafolio" = CER."IdPortafolio" AND DOC."AnoMes" = CER."Periodo" AND CER."IdTipoDocumento" = 'CIRCCarta')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :IdEmpresa
WHERE "IdNomina" = :IdNomina AND NOM."IdCliente" = :IdCliente;