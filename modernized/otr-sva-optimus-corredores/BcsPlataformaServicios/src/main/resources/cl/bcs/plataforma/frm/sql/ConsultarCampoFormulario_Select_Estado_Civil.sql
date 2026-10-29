SELECT
"IdTipoEstadoCivil", 
"Nombre"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoEstadoCivil"
WHERE "IdTipoEstadoCivil" in (:IdsTipoEstadoCivil);