SELECT
	EMP."RazonSocial" AS "NombreCliente",
	EMP."DocumentoIdentidad",
	EMP."IdPortafolioPrestamo" ,
	EMP."IdPortafolio" ,
	EMP."IdEstado",
	SOC."IdSocioDeNegocio" AS "IdSocioNegocio",
	SUB."FolioSubyacente" AS "FolioRegistro",
	CORRE."IdCorredora" AS "IdCorredorContraparte",
	EMP."INGCantidad" AS "Cantidad"
FROM
	"BSCL.Custodia.BD::CUS.Subyacente" SUB
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" EMP ON SUB."FolioCarteraRelacionada" = EMP."FolioPrestamo" 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" SOC ON EMP."IdCliente" = SOC."IdCliente" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" CORRE ON CORRE."DocumentoIdentidad" = SOC."DocumentoIdentidadDefecto" 
WHERE
	SUB."FolioSubyacente" IN (:FoliosRegistro)
	AND SUB."IdEstado" = 'VIG'