SELECT 
    CLI."IdCliente",
    DI."DocumentoIdentidad" AS "DocumentoIdentidadCliente", 
    CASE 
        WHEN PER."Tipo" = 'N' THEN 
            PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
        ELSE 
            PER."RazonSocial"
    END AS "NombreCliente",
    TO_CHAR(OS."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
    OS."IdMonedaPrincipal",
    SUM(OS."MontoMonedaPrincipal") AS "TotalMontoMonedaPrincipal",
    OS."IdMonedaSecundaria",
    SUM(OS."MontoMonedaSecundaria") AS "TotalMontoMonedaSecundaria" 
FROM 
    BSCL."BSCL.Plataforma.BD::IND.Indicador" IND
JOIN 
    BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TIPO_IND ON IND."IdTipoIndicador" = TIPO_IND."IdTipoIndicador" 
JOIN 
    BSCL."BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON IND."IdEntidad" = CLI."IdCliente"
LEFT JOIN 
    BSCL."BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN 
    BSCL."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
JOIN 
    BSCL."BSCL.Mesa.BD::SPT.OperacionSpot" OS ON OS."IdCliente" = CLI."IdCliente"
WHERE 
    TIPO_IND."CodigoTipoIndicador" = 'LIMSPOCOR'
    AND OS."IdMonedaPrincipal" = 'USD'
    AND OS."IdMonedaSecundaria" = 'CLP'
    AND TO_DATE(OS."FechaOperacion") = TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD')
    AND (:p_IdCliente = 0 OR CLI."IdCliente" = :p_IdCliente)
    AND IND."ValorIndicador" = 'S'
    AND OS."Estado" = 'VIG'
GROUP BY 
    DI."DocumentoIdentidad",
    CASE 
        WHEN PER."Tipo" = 'N' THEN 
            PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
        ELSE 
            PER."RazonSocial"
    END,
    TO_CHAR(OS."FechaOperacion", 'YYYY-MM-DD'),
    OS."IdMonedaPrincipal",
    OS."IdMonedaSecundaria",
    CLI."IdCliente";
