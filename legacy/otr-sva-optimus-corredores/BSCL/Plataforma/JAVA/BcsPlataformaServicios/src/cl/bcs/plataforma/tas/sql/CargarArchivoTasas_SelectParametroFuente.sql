SELECT COUNT(1)
FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente"
WHERE "Codigo" = :p_Fuente OR "Descripcion" = :p_Fuente