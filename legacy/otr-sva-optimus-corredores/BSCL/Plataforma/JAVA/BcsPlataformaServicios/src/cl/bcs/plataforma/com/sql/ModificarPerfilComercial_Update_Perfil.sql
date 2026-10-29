UPDATE "BSCL.Plataforma.BD::COM.PerfilComercialBase"
SET
	"DescripcionPerfilComercialBase" = :DescripcionPerfilComercialBase,
	"IdCategoriaInversionista" = :IdCategoriaInversionista,
	"IdPerfilProductos" = :IdPerfilProductos,
	"IdPerfilCobros" = :IdPerfilCobros,
	"IdPerfilCobrosPeru" = :IdPerfilCobrosPeru,
	"IdPerfilLimites" = :IdPerfilLimites,
	"IdPerfilCondicionesOperativas" = :IdPerfilCondicionesOperativas,
	"IdModeloFacturacion" = :IdModeloFacturacion,
	"FechaModificacion" = :FechaModificacion,
	"IdEstado" = :IdEstado,
	"PorDefecto" = :PorDefecto,
	"IdEmpresa" = :IdEmpresa,
	"TipoEnrolamiento" = :TipoEnrolamiento
WHERE "IdPerfilComercialBase" = :IdPerfilComercialBase
