SELECT COUNT(1)
FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa"
WHERE "IdTipoTasa" = :p_IdTipoTasa OR :p_IdTipoTasa = '';