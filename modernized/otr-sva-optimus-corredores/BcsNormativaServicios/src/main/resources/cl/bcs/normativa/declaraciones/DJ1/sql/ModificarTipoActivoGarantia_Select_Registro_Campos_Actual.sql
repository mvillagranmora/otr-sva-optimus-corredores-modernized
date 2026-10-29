SELECT
	DDJ."KIdContrato" AS "IdContrato",
	DDJ."S7_TipoGar",
	DDJ."KS7_IdGar",
	DDJ."S7_TipoActGar",
	DDJ."S7_PorcGar"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" AS DDJ
LEFT JOIN "BSCL.Normativa.BD::GBL.ModificarTipoActivoGarantia_RegistroCambio" RC 
ON (
	RC."RutContraparte" = DDJ."KRutCtraparte1"
	AND RC."IdContrato" = DDJ."KIdContrato"
	AND RC."FechaContrato" = DDJ."KFechaContrato"
	AND RC."IdOperacion" = DDJ."KIdOpeEstruc"
	AND RC."Producto" = DDJ."kProducto"
	AND RC."PeriodoComercial" = DDJ."kPeriodoComercial"
	AND RC."IdGarantia" = DDJ."KS7_IdGar"
)
WHERE DDJ."KIdContrato" IN (:KIdContrato)
AND DDJ."Seccion"=7;