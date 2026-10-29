SELECT "IdEntidadRecaudadora",
       "DocumentoIdentidad",
       "Nombre",
       "IdPersona"
FROM "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora"
WHERE "DocumentoIdentidad" IN (:DocumentoIdentidad);