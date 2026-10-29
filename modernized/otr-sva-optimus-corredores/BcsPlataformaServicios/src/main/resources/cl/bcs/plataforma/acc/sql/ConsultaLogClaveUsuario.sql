SELECT 
COUNT(*) AS "NumClaveCoincide"
FROM 
(SELECT 
"Clave", 
"UltimoCambioDeClave" 
from "BSCL.Plataforma.BD::ACC.LogUsuarioClave" 
WHERE "IdUsuario" = :idUsuario 
order by "UltimoCambioDeClave" desc limit 10) AS LogUsuario
where LogUsuario."Clave" = :p_ClaveNueva;
