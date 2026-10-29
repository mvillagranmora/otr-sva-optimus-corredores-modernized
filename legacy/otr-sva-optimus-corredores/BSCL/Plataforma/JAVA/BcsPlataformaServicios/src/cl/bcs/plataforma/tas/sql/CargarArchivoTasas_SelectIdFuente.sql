SELECT "IdFuente" 
FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente"
WHERE UPPER("Codigo") = UPPER(:p_Fuente) OR UPPER("Descripcion") = UPPER(:p_Fuente)