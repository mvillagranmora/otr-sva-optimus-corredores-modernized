SELECT cod."IdCorredora",cod."IdBolsa",cod."CodigoCorredora",cod."CodigoCorredoraFix"
FROM "BSCL.Plataforma.BD::INS.CorredoraCodigos" cod
INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" cor ON cod."IdCorredora" = cor."IdCorredora"