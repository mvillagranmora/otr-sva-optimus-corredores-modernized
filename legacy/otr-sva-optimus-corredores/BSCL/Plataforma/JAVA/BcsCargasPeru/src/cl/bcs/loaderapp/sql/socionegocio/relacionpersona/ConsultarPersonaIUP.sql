SELECT
     DISTINCT DI."IdPersona" 
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI 
WHERE DI."DocumentoIdentidad" = :DocumentoIdentidad 