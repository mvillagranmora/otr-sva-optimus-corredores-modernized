SELECT "Clave", "UltimoCambioDeClave" from "BSCL.Plataforma.BD::ACC.LogUsuarioClave"
WHERE "IdUsuario" = :IdUsuario  AND "Clave" = :Clave
order by "UltimoCambioDeClave" desc limit 10;
