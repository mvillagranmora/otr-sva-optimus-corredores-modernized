SELECT D."CodLenguaje",D."CodDiccionario",D."Descripcion"
FROM "BSCL.Plataforma.BD::PLA.Diccionario" D
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" C ON D."CodLenguaje" = C."Valor" AND "CodigoParametro" = :CodigoParametro
WHERE C."IdEmpresa" = :IdEmpresa;
