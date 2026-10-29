SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Invalidos" (
   	"CodigoValidacion",
	"ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
	"IdCliente",
	"IdEmpresa",
    
    "Numero",
	"Operacion",
	"TipoRenta",
	"RentaBruta",
	"TasaDeRetencion",
	"Impuesto",
	"RentaLiquida",
	"CreditoIDPC",
	"OtrosCreditos",
	"F50Codigo",
	"F50Folio",
	"FechaPago",
	"MonedaPago",
	"TipoCambioPago",
	
	"PagadorTipoJuridico",
	"PagadorRazonSocial",
	"PagadorNombre",
	"PagadorRut",
	"PagadorDomicilio",
	"PagadorCiudad",
	"PagadorRelacion",
	"PagadorHolding",
	
	"PerceptorTipoJuridico",
	"PerceptorPaisResidencia",
	"PerceptorRazonSocial",
	"PerceptorNombre",
	"PerceptorIdentificacionTributaria",
	"PerceptorRut",
	"PerceptorFechaNacimiento",
	"PerceptorTipoDomicilio",
	"PerceptorDomicilio",
	"PerceptorCiudad",
	"PerceptorPais",
	
	"IntermediarioTipoJuridico",
	"IntermediarioPaisResidencia",
	"IntermediarioRazonSocial",
	"IntermediarioNombre",
	"IntermediarioIdentificacionTributaria"
) AS
SELECT
   	b."CodigoValidacion",
	a."ID",
    a."IdDocumento",
    a."VboUsuario",
    a."Observaciones",
    a."FuenteDatos",
	a."IdCliente",
	a."IdEmpresa",
    
    a."Numero",
	a."Operacion",
	a."TipoRenta",
	a."RentaBruta",
	a."TasaDeRetencion",
	a."Impuesto",
	a."RentaLiquida",
	a."CreditoIDPC",
	a."OtrosCreditos",
	a."F50Codigo",
	a."F50Folio",
	a."FechaPago",
	a."MonedaPago",
	a."TipoCambioPago",
	
	a."PagadorTipoJuridico",
	a."PagadorRazonSocial",
	a."PagadorNombre",
	a."PagadorRut",
	a."PagadorDomicilio",
	a."PagadorCiudad",
	a."PagadorRelacion",
	a."PagadorHolding",
	
	a."PerceptorTipoJuridico",
	a."PerceptorPaisResidencia",
	a."PerceptorRazonSocial",
	a."PerceptorNombre",
	a."PerceptorIdentificacionTributaria",
	a."PerceptorRut",
	a."PerceptorFechaNacimiento",
	a."PerceptorTipoDomicilio",
	a."PerceptorDomicilio",
	a."PerceptorCiudad",
	a."PerceptorPais",
	
	a."IntermediarioTipoJuridico",
	a."IntermediarioPaisResidencia",
	a."IntermediarioRazonSocial",
	a."IntermediarioNombre",
	a."IntermediarioIdentificacionTributaria"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Datos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Invalidos" to bcs_desarrollador with grant option;
