UPDATE "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia"
SET "ExigibilidadEntidad"   = :ExigibilidadEntidad,
	"ExigibilidadCorredora" = :ExigibilidadCorredora
WHERE "IdExigibilidadGarantia" = :IdExigibilidadGarantia;