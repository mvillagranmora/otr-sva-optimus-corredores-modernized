-- Actualiza el campo S7_TipoGar
UPDATE  TIACGAR
SET
	TIACGAR."S7_TipoGar" = RC."ValorNuevo",
	TIACGAR."FecUltMod" = TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
	TIACGAR."UsrUltMod" =  USU."CodigoUsuario"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" TIACGAR
INNER JOIN "BSCL.Normativa.BD::GBL.ModificarTipoActivoGarantia_RegistroCambio" RC 
ON (
RC."RutContraparte" = TIACGAR."KRutCtraparte1"
AND RC."IdContrato" = TIACGAR."KIdContrato"
AND RC."FechaContrato" = TIACGAR."KFechaContrato"
AND RC."IdOperacion" = TIACGAR."KIdOpeEstruc"
AND RC."Producto" = TIACGAR."kProducto"
AND RC."PeriodoComercial" = TIACGAR."kPeriodoComercial"
AND RC."IdGarantia" = TIACGAR."KS7_IdGar"
)
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON USU."IdUsuario" = RC."IdUsuario"
WHERE RC."CampoModificado" = :CampoModificado; -- S7_TipoGar