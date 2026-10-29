SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Datos" (
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
	"IntermediarioIdentificacionTributaria",
	"NumPerIdenTri_C51",
	"NumInterIdenTri_C52"
) AS
SELECT
    a."ID",
    a."IdDocumento",
    a."VboUsuario",
    COALESCE(VAL."CodigoValidacion", a."Observaciones") AS "Observaciones",
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
	COALESCE(a."PerceptorIdentificacionTributaria",0),
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
	COALESCE(a."IntermediarioIdentificacionTributaria",0),
	a."NumPerIdenTri_C51",
	a."NumInterIdenTri_C52"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1946" a
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1946" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Datos" TO bcs_desarrollador WITH GRANT OPTION;