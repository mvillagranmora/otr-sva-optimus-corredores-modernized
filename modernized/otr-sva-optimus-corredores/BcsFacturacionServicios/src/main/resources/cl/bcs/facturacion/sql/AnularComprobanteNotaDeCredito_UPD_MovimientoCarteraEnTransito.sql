UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
SET
	"FechaAnulacionRegistro" 		= '0001-01-01 00:00:00.000',
	"IdEstado"						=  COALESCE(:IdEstado, "IdEstado"),
	"IdUsuarioCertificacion" 		= COALESCE(:IdUsuarioCertificacion, "IdUsuarioCertificacion"),
	"IdUsuarioAnulacion" 			= COALESCE(:IdUsuarioAnulacion, "IdUsuarioAnulacion")
WHERE "IdMovimientoCarteraEnTransito" = :IdMovimientoCarteraEnTransito
  -- OR "IdMovimientoCarteraEnTransitoRelacionado" = :IdMovimientoCarteraEnTransito;