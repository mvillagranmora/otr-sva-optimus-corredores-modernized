set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Origen";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Origen" (
    "IdEmpresa",
    "RutCliente",
    "IdCliente",
    "IdPortafolio",
    "TipoInstrumento",
    "TipoRegistro",
    "Nemotecnico",
    "FechaPago",
    "SecuenciaEvento",
    "RutSociedad",
    "RazonSocial",
    "TipoSociedad",

    "MontoPagado",
    "MontoRecibido",
    "CantidadAcciones31Dic",
    "DividendosAfectos",
    "DividendosExentos",
    "DividendosNoRentaNoAcogidos",
    "DividendosNoRentaAcogidos",
    "DevolucionCapital",

    "IncrementoImpuesto",

    "CreditosAfectosConDevolucion",
    "CreditosAfectosSinDevolucion",
    "CreditosExentosConDevolucion",
    "CreditosExentosSinDevolucion",
    "CreditosImpuestosExternos",
    "CreditosImpuestoTasaAdicional",

    "NumeroCertificado",
    "CantidadDerecho",
    "IdInstrumento",
    "IdEventoCapitalCliente",
    "TipoSocioNegocio",
    "TipoCartera",
    "IdMoneda",
    "TipoEventoCapital"
) as
select 
    X."IdEmpresa",
    DIV."DocumentoIdentidad" as "RutCliente",
    B."IdCliente",
    DIV."IdPortafolio",
    DIV."IdTipoInstrumento" as "TipoInstrumento",
    'P' as "TipoRegistro",
    DIV."Nemo" as "Nemotecnico",
    DIV."FechaPago",
    DIV."Secuencia" as "SecuenciaEvento",
    COALESCE(D."DocumentoIdentidad",'') as "RutSociedad",
    D."NombreCompleto" AS "RazonSocial",
    -- A: Sociedad Anonima Abierta.
    -- C: Sociedad Anonima Cerrada o Sociedades por Acciones.
    -- E: Sociedad en Comandita por Acciones.
    'C' as "TipoSociedad",

    to_bigint(DIV."Total") as "MontoPagado",
    to_bigint(0) as "MontoRecibido",
    to_bigint(0) as "CantidadAcciones31Dic",
    to_bigint(0) as "DividendosAfectos",
    to_bigint(0) as "DividendosExentos",
    to_bigint(0) as "DividendosNoRentaNoAcogidos",
    to_bigint(0) as "DividendosNoRentaAcogidos",
    to_bigint(0) as "DevolucionCapital",

    to_bigint(0) as "IncrementoImpuesto",

    to_bigint(0) as "CreditosAfectosConDevolucion",
    to_bigint(0) as "CreditosAfectosSinDevolucion",
    to_bigint(0) as "CreditosExentosConDevolucion",
    to_bigint(0) as "CreditosExentosSinDevolucion",
    to_bigint(0) as "CreditosImpuestosExternos",
    to_bigint(0) as "CreditosImpuestoTasaAdicional",

    to_bigint(0) as "NumeroCertificado",
    to_bigint(DIV."CantidadDerecho"),
    C."IdInstrumento",
    DIV."IdEventoCapitalCliente",
    CASE DIV."TipoSocioNegocio" WHEN 'CLI' THEN 'C' ELSE 'E' END,
    DIV."TipoCartera",
    DIV."IdMoneda",
    DIV."IdTipoEventoCapital" as "TipoEventoCapital"

    --DIV."IdEventoCapital",
    --DIV."IdSocioNegocio",
    --DIV."IdTipoDocumentoIdentidad",
    --DIV."NombreCliente",
    --DIV."IdClasificacionTipoInstrumento",
    --DIV."IdInstrumento",
    --DIV."IdInstrumentoNuevo",
    --DIV."NemoNuevo",
    --DIV."FechaCierre",
    --DIV."CantidadNueva",
    --DIV."MontoCapital",
    --DIV."MontoInteres",
    --DIV."SecuenciaCupon",
    --DIV."CodigoUsuarioAgente",
    --DIV."NombreAgente",
    --DIV."CodigoSucursal",
    --DIV."FechaHoraIngresoRegistro",
    --DIV."FechaHoraUltimaActualizacion",
    --DIV."IdUsuario",
    --DIV."IdEstado",
    --DIV."IdBoveda",
    --DIV."IdBolsa",
    --DIV."IdConceptoGarantia",
    --DIV."IdTipoOperacionGarantia"
from "BSCL.Custodia.BD::CUS.EventoCapital" X
left join "BSCL.Custodia.BD::CUS.EventoCapitalCliente" DIV
    on X."IdEventoCapital"=DIV."IdEventoCapital"
--left join "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" B
    --on DIV."IdSocioNegocio"=B."IdSocioDeNegocio"
left join "BSCL.Portafolio.BD::POR.Portafolio" b 
    on b."IdPortafolio" = DIV."IdPortafolio"    
left join "BSCL.Plataforma.BD::INS.Instrumento" C
    on DIV."Nemo"=C."Nemotecnico"
left join "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" D
    on C."IdEmisor"=D."IdEmisor"
where DIV."IdTipoEventoCapital"='DV' and DIV."IdTipoInstrumento"='ACN' and DIV."IdEstado"='VIG' and B."IdCliente">0 AND B."IdMatrizAgrupadorAPV"=0
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Origen" to bcs_desarrollador with grant option;
