SELECT "IdSecuencia","NombreParametro","Valor" FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma"
WHERE "IdPrograma" = :IdPrograma
ORDER BY "IdSecuencia" ASC