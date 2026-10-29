SELECT	"IdMedioDePago",
		"Nombre"
FROM "BSCL.Custodia.BD::CUS.Configuracion" CONF
	INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" MED 
		ON (CONF."Valor" = MED."IdMedioDePago")
WHERE "CodigoParametro" = 'CUS.MedioPagoDefectoEVCAP' 
	AND "IdEmpresa" = :IdEmpresa;