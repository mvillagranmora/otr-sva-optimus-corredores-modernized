 SELECT DISTINCT
    inn."IdMoneda"
    FROM "BSCL.Plataforma.BD::INS.Instrumento" inn
    WHERE inn."IdInstrumentoNegociable" = :IdInstrumentoRaiz
    AND inn."IdEstado" = 'ACT'
    AND inn."EstaInscrito" = 'S';