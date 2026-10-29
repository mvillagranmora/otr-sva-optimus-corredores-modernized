SELECT
	DDJ."KIdContrato",
	DDJ."S7_TipoGar",
	DDJ."KS7_IdGar",
	DDJ."S7_TipoActGar",
	DDJ."S7_PorcGar"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" DDJ
LEFT JOIN "BSCL.Normativa.BD::GBL.EliminarTipoActivoGarantia_RegistroElimina" RC
ON (
	RC."KRutCtraparte1" = DDJ."KRutCtraparte1"
	AND RC."KIdContrato" = DDJ."KIdContrato"
	AND RC."KFechaContrato" = DDJ."KFechaContrato"
	AND RC."KIdOpeEstruc" = DDJ."KIdOpeEstruc"
	AND RC."kProducto" = DDJ."kProducto"
	AND RC."KS7_IdGar" = DDJ."KS7_IdGar"
)
WHERE DDJ."KIdContrato" = :KIdContrato
AND DDJ."Seccion" = 7;