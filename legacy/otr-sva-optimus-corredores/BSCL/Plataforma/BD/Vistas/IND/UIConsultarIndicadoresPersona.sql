SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPersona";
GO 
-- "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPersona" source

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPersona" AS
SELECT
    CASE PER."Tipo" WHEN 'N' 
		THEN PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
		END AS "NombrePersona",
    DOC."DocumentoIdentidad" AS "DocumentoIdentidad",
    IND."IdPersona" AS "IdPersona",
    IND."NombreTipoIndicador",
    IND."GrupoIndicador",
    IND."IdUso",
    IND."IdTipoIndicador", 
    IND."CodigoTipoIndicador",
    IND."ClaseDato", 
    IND."CantidadDecimales",
    IND."LongitudMinima",
    IND."LongitudMaxima",
    IND."IdTipoPersona",
    IND."ValorIndicador",
    IND."CodigoValorIndicador",
    IND."NombreValorIndicador",
    (CASE IND."ClaseDato" 
        WHEN 'COMBO' THEN IND."NombreValorIndicador"
        ELSE IND."ValorIndicador"
     END) AS "UINombreValorIndicador",
    IND."IdIndicador",
    IND."FechaCreacion"
FROM 
    "BSCL.SocioNegocio.BD::PER.Persona" PER
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
ON 
    PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
LEFT JOIN 
    (
    SELECT 
        PER."IdPersona",
        CAST(TIND."IdTipoEntidad" AS NVARCHAR(40)) AS "IdTipoEntidad",
        CAST(TIND."NombreTipoIndicador" AS NVARCHAR(255)) AS "NombreTipoIndicador",
        CAST(TIND."GrupoIndicador" AS NVARCHAR(40)) AS "GrupoIndicador",
        CAST(TIND."IdUso" AS NVARCHAR(3)) AS "IdUso",
        TIND."IdTipoIndicador",
        CAST(TIND."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador",
        CAST(TIND."ClaseDato" AS NVARCHAR(40)) AS "ClaseDato",
        TIND."CantidadDecimales",
        TIND."LongitudMinima",
        TIND."LongitudMaxima",
        CAST(TIND."IdTipoPersona" AS NVARCHAR(10)) AS "IdTipoPersona",
        CAST(IFNULL(IND."ValorIndicador",TIND."ValorIndicadorDefecto") AS NVARCHAR(255)) AS "ValorIndicador",
        CAST(IFNULL(DETIND."Codigo",'') AS NVARCHAR(40)) AS "CodigoValorIndicador",
        CAST(IFNULL(DETIND."DetalleIndicador",'') AS NVARCHAR(255)) AS "NombreValorIndicador",
        IFNULL(IND."IdIndicador",0) AS "IdIndicador",
        TIND."IdEmpresa",
        IND."FechaCreacion"
    FROM 
        "BSCL.SocioNegocio.BD::PER.Persona" PER
    INNER JOIN 
        "BSCL.Plataforma.BD::IND.TipoIndicador" TIND
    ON  
        TIND."IdTipoEntidad" = 'PERSONA' AND TIND."IdEstado" != 'INA'
    LEFT JOIN 
        "BSCL.Plataforma.BD::IND.Indicador" IND
    ON 
        IND."IdTipoEntidad" = TIND."IdTipoEntidad"
        AND IND."IdEntidad" = PER."IdPersona"
        AND TIND."IdTipoIndicador" = IND."IdTipoIndicador"
    LEFT JOIN 
        "BSCL.Plataforma.BD::IND.DetalleIndicador" DETIND
    ON  
        TIND."ClaseDato" = 'COMBO'
        AND CAST(DETIND."IdDetalleIndicador" AS NVARCHAR) = IND."ValorIndicador"    
        AND DETIND."IdTipoIndicador" = IND."IdTipoIndicador"
    ) AS IND
ON 
    PER."IdPersona" = IND."IdPersona"
WHERE 
    (CASE IND."ClaseDato" 
        WHEN 'COMBO' THEN IND."NombreValorIndicador"
        ELSE IND."ValorIndicador"
    END) <> '' 
ORDER BY 
    "IdPersona";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  