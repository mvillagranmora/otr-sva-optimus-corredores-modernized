SELECT
	"IdPerfilComercialBase",
	"DescripcionPerfilComercialBase",
	"IdCategoriaInversionista",
	"IdPerfilProductos",
	"IdPerfilCobros",
	"IdPerfilCobrosPeru",
	"IdPerfilLimites",
	"IdPerfilCondicionesOperativas",
	"IdModeloFacturacion",
	"FechaModificacion",
	"IdEstado",
	"PorDefecto",
	"IdEmpresa",
	"TipoEnrolamiento"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase"
WHERE "IdPerfilComercialBase" = :IdPerfilComercialBase
