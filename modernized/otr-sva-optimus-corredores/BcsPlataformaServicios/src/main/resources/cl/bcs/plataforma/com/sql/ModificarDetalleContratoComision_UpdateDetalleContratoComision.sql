UPDATE "BSCL.Plataforma.BD::COM.DetalleContratoComision" 
SET
	"IdCobroClasificacion" = :IdCobroClasificacion,
	"IdCobro"				= :IdCobro,
	"EsPorRangos"			= :EsPorRangos,
	"IdConcepto"			= :IdConcepto,
	"MontoMinimo"			= :MontoMinimo,
	"MontoMaximo"			= :MontoMaximo
WHERE "IdDetalleContratoComision" = :IdDetalleContratoComision;

