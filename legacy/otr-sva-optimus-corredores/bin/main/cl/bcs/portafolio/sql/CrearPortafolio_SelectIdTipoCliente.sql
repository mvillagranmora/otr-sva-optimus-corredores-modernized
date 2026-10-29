SELECT  PCOM."IdTipoCliente"
FROM 
    (SELECT
		    IFNULL(
		        ATR."ValorIndicador",
		        TPO."ValorIndicadorDefecto"
		    ) as "IdTipoCliente"
		FROM
		    "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
		    LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON (
		        ATR."IdTipoIndicador" = TPO."IdTipoIndicador"
		        AND ATR."IdEntidad" = 0
		        AND ATR."ModeloCliente" = (
		            SELECT
		                Cliente."IdTipoCliente"
		            FROM
		                "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
		                INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente"
		                AND P."IdPortafolio" = :IdPortafolio
		                INNER JOIN BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (
		                    TI."CodigoTipoIndicador" = 'GEN_OCCINV'
		                    AND TI."IdTipoEntidad" = 'PORTAFOLIO'
		                    AND TI."IdUso" = 'PRE'
		                    AND TI."IdEmpresa" = :IdEmpresa
		                )
		            WHERE
		                P."IdPortafolio" = :IdPortafolio
		        )
		    )
		WHERE
		    TPO."IdTipoEntidad" = 'PORTAFOLIO'
		    AND TPO."CodigoTipoIndicador" IN ('GEN_OCCINV')
		    AND TPO."IdUso" = 'PRE') PCOM;