SELECT 
    CC."IdCliente",
    CC."IdPortafolio",
    CC."NombreLargoCliente",
    CC."DocumentoIdentidad",
    CC."CodigoPortafolio",
    CC."AccionTipoOperacion",
    CC."IdClasificacionTipoInstrumento",
    CC."NombreTipoInstrumento",
    CCD."NemotecnicoInstrumento",
    CDL."Codigo",
    CCDG."RazonSocial" AS "Nombre",
    CCDG."Telefono" AS "TelefonoPrincipal",
    CE."NombreCompleto",
    CE."IdPersona",
    CC."IdMoneda",
    CC."IdMonedaComprobante",
    CC."IdMonedaLiquidacion",
    COALESCE(CPDE."Direccion", '') AS "Direccion",
    COALESCE(CPT."Numero", '') AS "Numero",
    COUNT(CCD."Monto") AS "CantidadTotal",
    MEDIAN(CCD."Monto") AS "PrecioPromedio",
    SUM(CCD."Monto") AS "Total",
    CE."IdPersona" AS "IdPersonaRemitente",
    CSN."IdPersona" AS "IdPersonaDestinatario"  
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" CC 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON CSN."IdCliente" = CC."IdCliente"  AND CC."FechaComprobante" = :FechaSistema AND  CC."IdEmpresa" = :IdEmpresa AND  (CC."AccionTipoOperacion" = 'COMPRA' OR CC."AccionTipoOperacion" = 'VENTA')
INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CCD ON CCD."IdComprobante" = CC."IdComprobante" 
INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CDL ON CDL."IdCondicionDeLiquidacion" = CC."IdCondicionLiquidacion"  
INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :IdEmpresa 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = CC."IdEmpleadoEjecutivoPortafolio" 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCLI ON CC."IdCliente" = CCLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CCLI."IdDireccionElectronicaPrincipal" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdTelefono" = CCLI."IdTelefonoPrincipal" AND CPT."CodigoProposito" = 'COME'
GROUP BY 
CC."IdCliente",
CC."IdPortafolio",
CC."NombreLargoCliente",
CC."DocumentoIdentidad",
CC."CodigoPortafolio",
CC."AccionTipoOperacion",
CC."IdClasificacionTipoInstrumento",
CC."NombreTipoInstrumento",
CCD."NemotecnicoInstrumento",
CDL."Codigo",
CCDG."RazonSocial",
CCDG."Telefono",
CE."NombreCompleto",
CE."IdPersona",
CPDE."Direccion",
CPT."Numero",
CE."IdPersona",
CSN."IdPersona",
CC."IdMoneda",
CC."IdMonedaComprobante",
CC."IdMonedaLiquidacion"
ORDER BY "IdCliente";