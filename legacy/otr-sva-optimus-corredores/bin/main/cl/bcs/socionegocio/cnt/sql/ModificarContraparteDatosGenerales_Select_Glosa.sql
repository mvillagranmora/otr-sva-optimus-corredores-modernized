SELECT "DocumentoIdentidad", "NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"
WHERE "IdContraparte" = :IdContraparte
