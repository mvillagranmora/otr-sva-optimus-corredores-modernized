SELECT
     --CABECERA
    CEMP."NombreCompleto" AS "Agente",
    --DETALLE
    CRT."IdCartera" AS "IdSecuencia",
    CRT."IdCartera",
    CRT."FolioCartera",
    CCL."NombreCompleto" AS "Cliente",
    CCL."TipoDocumentoIdentidad",
    CCL."DocumentoIdentidad",
    CRT."IdPortafolio",
    CRT."CodigoPortafolio",
    CRT."Nemotecnico",
    CRT."Cantidad",
    CRT."NombreTipoOperacion",
    TO_NVARCHAR(CRT."FechaVencimientoOperacion", 'DD-MM-YYYY') AS "FechaVencimiento",
    CRT."IdEstado",
    CRT."NombreEstado",
    --PIE
    CE."RazonSocial" AS "Corredora",
    CE."Telefono" AS "AtencionCliente",
    --EXTRAS
    CEMP."IdPersona" AS "IdPersonaEmpleado", 
    CEMP."IdDireccionElectronica" AS "IdDireccionElectronicaEmpleado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CRT
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON CRT."IdPortafolio" = PORT."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL ON CCL."IdCliente" = PORT."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON PORT."IdEmpleadoEjecutivo" = CEMP."IdEmpleado"

LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CCL."IdTelefonoPrincipal" = CPT."IdTelefono"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"

INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CE ON CE."IdEmpresa" = CRT."IdEmpresa"
WHERE CRT."IdTipoInstrumento" IN ('SIM','RRV','RRF') AND CRT."IdEstado" = 'VIG' AND CRT."IdEmpresa" = :idEmpresa AND CRT."FechaVencimientoOperacion" BETWEEN :fechaDesde AND :fechaHasta;
