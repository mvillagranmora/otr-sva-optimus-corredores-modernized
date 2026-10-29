set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Invalidos";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Invalidos" (
    "CodigoValidacion",
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    "RutCliente",
    "IdCliente",
    "IdPortafolio",
    "TipoInstrumento",
    "TipoInstrumentoDescripcion",
    "TipoRegistro",
    "TipoRegistroDescripcion",
    "Nemotecnico",
    "FechaPago",
    "SecuenciaEvento",
    "RutSociedad",
    "RazonSocial",
    "TipoSociedad",
    "TipoSociedadDescripcion",
    "CantidadAcciones31Dic",
    "MontoPagado",
    "MontoRecibido",
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
    "TipoEventoCapital",
    "TipoEventoCapitalDescripcion"
) as
select b."CodigoValidacion", a.*
    from "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Datos" a
    inner join "BSCL.Normativa.BD::TRI.ResumenValidacion" b
    on a."IdDocumento"=b."IdDocumento"
    inner join "BSCL.Normativa.BD::TRI.DetalleValidacion" c
    on b."ID"=c."IdResumenValidacion"
    where a."ID"=c."IdDetalleDocumento"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Invalidos" to bcs_desarrollador with grant option;