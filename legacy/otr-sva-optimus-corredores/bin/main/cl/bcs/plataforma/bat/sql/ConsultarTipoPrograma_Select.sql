select "IdTipoPrograma",
"Descripcion",
"MaximoIntentos",
"Modulo",
"NombreClasePrograma",
"TipoClasePrograma",
"Nodo",
"EsPauta" 
from "BSCL.Plataforma.BD::BAT.TipoPrograma" 
where "IdTipoPrograma" IN (:IdTipoProgramas);