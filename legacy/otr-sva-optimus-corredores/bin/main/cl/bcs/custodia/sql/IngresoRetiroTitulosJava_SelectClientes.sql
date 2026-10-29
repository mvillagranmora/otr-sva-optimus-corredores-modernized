-- Obtiene datos de Clientes	    
SELECT
    CLI."IdCliente",
    DIDE."DocumentoIdentidad",	
    CASE 
        PER."Tipo" 
        WHEN 'N' THEN PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"		
        ELSE PER."RazonSocial"
    END AS "NombreCliente",
    COALESCE(MAX(CASE WHEN TIND."CodigoTipoIndicador" = 'BCCUM' THEN IND."ValorIndicador" END), 'N') AS "BloqueadoPorCumplimiento",
    COALESCE(MAX(CASE WHEN TIND."CodigoTipoIndicador" = 'INVCALIF' THEN IND."ValorIndicador" END), 'N') AS "InversionistaCalificado"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER 
    ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DIDE 
    ON DIDE."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
    ON TIND."IdTipoEntidad" = 'CLIENTE'
   AND TIND."CodigoTipoIndicador" IN ('BCCUM', 'INVCALIF')
   AND TIND."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND
    ON IND."IdTipoIndicador" = TIND."IdTipoIndicador" 
   AND IND."IdTipoEntidad" = TIND."IdTipoEntidad"
   AND IND."IdEntidad" = CLI."IdCliente"
WHERE CLI."IdEmpresa" = :IdEmpresa
  AND CLI."IdCliente" IN (:IdsClientes)
GROUP BY 
    CLI."IdCliente", 
    DIDE."DocumentoIdentidad", 
    PER."Tipo", 
    PER."Nombres", 
    PER."ApellidoPaterno", 
    PER."ApellidoMaterno", 
    PER."RazonSocial";
