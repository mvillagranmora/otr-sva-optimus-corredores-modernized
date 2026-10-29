SELECT "IdPortafolio","DocumentoIdentidad","IdAutorizado","IdPersona"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" 
WHERE "IdTipoAutorizado" = 'ORDEN-CREAR' AND "IdPortafolio" IN (:IdPortafolio) AND "DocumentoIdentidad" IN (:DocumentoIdentidad)