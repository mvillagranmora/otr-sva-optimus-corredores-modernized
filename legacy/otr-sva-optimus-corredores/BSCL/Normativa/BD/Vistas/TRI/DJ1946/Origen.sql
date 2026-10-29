SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Origen" (
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
)
AS 
SELECT 
	1 AS "ID",
    1 AS "IdDocumento",
    '' AS "VboUsuario",
   	'' AS "Observaciones",
   	'INICIAL' AS "FuenteDatos",
	1 AS "IdCliente",
	1 AS "IdEmpresa",
    
   	1 AS "Numero",
	1 AS "Operacion",
	1 AS "TipoRenta",
	1 AS "RentaBruta",
	1 AS "TasaDeRetencion",
	1 AS "Impuesto",
	1 AS "RentaLiquida",
	1 AS "CreditoIDPC",
	1 AS "OtrosCreditos",
	1 AS "F50Codigo",
	1 AS "F50Folio",
	TO_DATE('2020-08-17') AS "FechaPago",
	'' AS "MonedaPago",
	1 AS "TipoCambioPago",
	
	1 AS "PagadorTipoJuridico",
	'' AS "PagadorRazonSocial",
	'' AS "PagadorNombre",
	'' AS "PagadorRut",
	'' AS "PagadorDomicilio",
	'' AS "PagadorCiudad",
	1 AS "PagadorRelacion",
	'' AS "PagadorHolding",
	
	1 AS "PerceptorTipoJuridico",
	'' AS "PerceptorPaisResidencia",
	'' AS "PerceptorRazonSocial",
	'' AS "PerceptorNombre",
	1 AS "PerceptorIdentificacionTributaria",
	'' AS "PerceptorRut",
	TO_DATE('2020-08-17') AS "PerceptorFechaNacimiento",
	1 AS "PerceptorTipoDomicilio",
	'' AS "PerceptorDomicilio",
	'' AS "PerceptorCiudad",
	'' AS "PerceptorPais",
	
	1 AS "IntermediarioTipoJuridico",
	'' AS "IntermediarioPaisResidencia",
	'' AS "IntermediarioRazonSocial",
	'' AS "IntermediarioNombre",
	1 AS "IntermediarioIdentificacionTributaria",
	'1' AS "NumPerIdenTri_C51",
	'1' AS "NumInterIdenTri_C52"
FROM DUMMY
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1946.Origen" TO bcs_desarrollador WITH GRANT OPTION;