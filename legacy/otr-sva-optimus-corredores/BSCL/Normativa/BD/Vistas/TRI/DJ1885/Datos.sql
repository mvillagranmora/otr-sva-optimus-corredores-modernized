set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Datos";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Datos" (
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
select
    a."ID",
    a."IdDocumento",
    a."VboUsuario",
    a."Observaciones",
    a."FuenteDatos",
    a."RutCliente",
    a."IdCliente",
    a."IdPortafolio",
    a."TipoInstrumento",
    b."Descripcion" as "TipoInstrumentoDescripcion",
    a."TipoRegistro",
    c."Descripcion" as "TipoRegistroDescripcion",
    a."Nemotecnico",
    a."FechaPago",
    a."SecuenciaEvento",
    a."RutSociedad",
    a."RazonSocial",
    a."TipoSociedad",
    d."Descripcion" as "TipoSociedadDescripcion",
    a."CantidadAcciones31Dic",
    a."MontoPagado",
    a."MontoRecibido",
    a."DividendosAfectos",
    a."DividendosExentos",
    a."DividendosNoRentaNoAcogidos",
    a."DividendosNoRentaAcogidos",
    a."DevolucionCapital",
    a."IncrementoImpuesto",
    a."CreditosAfectosConDevolucion",
    a."CreditosAfectosSinDevolucion",
    a."CreditosExentosConDevolucion",
    a."CreditosExentosSinDevolucion",
    a."CreditosImpuestosExternos",
    a."CreditosImpuestoTasaAdicional",
    a."NumeroCertificado",
    a."CantidadDerecho",
    a."IdInstrumento",
    a."IdEventoCapitalCliente",
    a."TipoSocioNegocio",
    a."TipoCartera",
    a."IdMoneda",
    a."TipoEventoCapital",
    e."Descripcion" as "TipoEventoCapitalDescripcion"
from "BSCL.Normativa.BD::TRI.DatosDJ1885" a
left join "BSCL.Normativa.BD::DOC.Diccionario" b
    on b."Tipo"='TipoInstrumento' and a."TipoInstrumento"=b."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" c
    on c."Tipo"='TipoRegistro' and a."TipoRegistro"=c."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" d
    on d."Tipo"='TipoSociedad' and a."TipoSociedad"=d."Codigo"
left join "BSCL.Normativa.BD::DOC.Diccionario" e
    on e."Tipo"='TipoEventoCapital' and a."TipoEventoCapital"=e."Codigo"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1885.Datos" to bcs_desarrollador with grant option;