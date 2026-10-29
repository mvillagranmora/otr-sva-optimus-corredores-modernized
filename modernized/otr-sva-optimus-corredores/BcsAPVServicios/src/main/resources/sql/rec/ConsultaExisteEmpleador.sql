SELECT "IdPersona", "PersonaBusqueda" FROM 
"BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica"
WHERE "DocumentoIdentidad" = :p_DoctoIdentidad;