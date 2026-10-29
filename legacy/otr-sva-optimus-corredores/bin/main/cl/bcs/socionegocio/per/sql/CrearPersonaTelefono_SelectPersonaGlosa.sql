SELECT "DocumentoIdentidad", "NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "IdPersona" = :p_IdPersona;
