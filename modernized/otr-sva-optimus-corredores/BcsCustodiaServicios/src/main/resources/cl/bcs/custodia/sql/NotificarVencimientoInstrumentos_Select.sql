SELECT
     --CABECERA
    CRT."IdCartera" AS "IdSecuencia",
    CCL."NombreCompleto" AS "Nombre",
    CCL."TipoDocumentoIdentidad",
    CCL."TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
    CCL."DocumentoIdentidad" AS "DocumentoIdentidad",
    TO_NVARCHAR(CRT."IdPortafolio") AS "CodigoPortafolio",
    CRT."IdPortafolio",
    COALESCE(CPT."Numero", '') AS "Telefono",
    COALESCE(CPDE."Direccion", '') AS "Correo",
    CEMP."NombreCompleto" AS "Agente",
--    TO_VARCHAR(TO_DATE(:p_FechaSistema, 'YYYY-MM-DD'), 'DD-MM-YYYY') AS "Fecha",
--    TO_VARCHAR(:p_HoraSistema) AS "Hora",
    --DETALLE
    CRT."FolioCartera",
    CRT."Nemotecnico",
    CRT."NombreTipoOperacion",
    TO_NVARCHAR(CRT."FechaVencimientoOperacion", 'DD-MM-YYYY') AS "FechaVencimiento",
    --PIE
    CCL."IdDireccionElectronicaPrincipal",
    CCL."IdPersona",
    CCDG."RazonSocial" AS "Corredora",
    CCDG."Telefono" AS "CallCenter",
    CEMP."IdPersona" AS "IdPersonaEmpleado", 
    CEMP."IdDireccionElectronica" AS "IdDireccionElectronicaEmpleado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CRT
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON CRT."IdPortafolio" = PORT."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL ON CCL."IdCliente" = PORT."IdCliente"
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CCL."IdTelefonoPrincipal" = CPT."IdTelefono"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON PORT."IdEmpleadoEjecutivo" = CEMP."IdEmpleado"
INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = CRT."IdEmpresa"
WHERE CRT."IdCartera" IN (:IdCarteras) AND CCL."IdDireccionElectronicaPrincipal" IS NOT NULL;