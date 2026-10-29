UPDATE "BSCL.Plataforma.BD::IND.Indicador"
SET 
    "ValorIndicador" = :IdDireccionElectronica,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdIndicador" IN (
    SELECT IND."IdIndicador"
    FROM "BSCL.Plataforma.BD::IND.Indicador" IND
    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR 
        ON IND."IdEntidad" = POR."IdPortafolio"
    INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
        ON CLI."IdCliente" = POR."IdCliente"
    INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIP 
        ON IND."IdTipoIndicador" = TIP."IdTipoIndicador"
    WHERE CLI."IdPersona" = :IdPersona
        AND TIP."CodigoTipoIndicador" IN ('FAC_DIRELE', 'CAR_DIRELE', 'ORDMAILNTF')
)
