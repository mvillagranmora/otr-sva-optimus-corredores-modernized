SELECT
	DDJ."KIdContrato" AS "IdContrato",
	DDJ."S6_TipoGar",
	DDJ."S6_MonVal",
	DDJ."KS6_IdGar",
	DDJ."S6_MontoUmb",
	DDJ."S6_ValGtia"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" AS DDJ
LEFT JOIN "BSCL.Normativa.BD::GBL.ModificarGarantia_RegistroCambio" RC 
ON (
	RC."RutContraparte" = DDJ."KRutCtraparte1"
	AND RC."IdContrato" = DDJ."KIdContrato"
	AND RC."FechaContrato" = DDJ."KFechaContrato"
	AND RC."IdOperacion" = DDJ."KIdOpeEstruc"
	AND RC."Producto" = DDJ."kProducto"
	AND RC."IdGarantia" = DDJ."KS6_IdGar"
)
WHERE DDJ."KIdContrato" IN (:KIdContrato)
AND DDJ."Seccion"=6;