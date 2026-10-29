UPDATE IND
SET
IND."ValorIndicador" = DET."IdDetalleIndicador",
IND."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Plataforma.BD::IND.Indicador" IND
INNER JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DET ON UPPER(DET."DetalleIndicador") = :ValorIndicador
WHERE  IND."IdIndicador" = :IdEntidad;