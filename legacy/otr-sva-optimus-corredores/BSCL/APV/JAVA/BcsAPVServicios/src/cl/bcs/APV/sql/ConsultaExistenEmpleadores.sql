SELECT "DocumentoIdentidad","IdPersona"  FROM 
"BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica"
WHERE "DocumentoIdentidad" IN (:p_DoctosIdentidad);