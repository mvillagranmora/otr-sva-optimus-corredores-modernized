UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
SET 
    "IdEstado" = :IdEstado, 
    "IdTipoBloqueo" = :IdTipoBloqueo, 
    "ComentarioBloqueo" = :ComentarioBloqueo
WHERE "IdEstado" = 'ACT' AND "FechaVigencia" BETWEEN '0001-01-02' AND :FechaSistema;