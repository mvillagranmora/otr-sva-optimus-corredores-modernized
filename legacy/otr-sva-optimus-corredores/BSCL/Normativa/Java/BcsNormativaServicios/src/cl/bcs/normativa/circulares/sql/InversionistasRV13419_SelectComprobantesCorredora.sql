SELECT OPE.*  FROM
(
			-->CORREDORA
			SELECT 
				 CCU."MontoComprobante"
				,TOPE."Accion"
				,CCU."IdCliente"
				,CCU."FechaComprobante"
				,CCU."IdComprobante"
				,CCU."IdMoneda"
			FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SDN
			ON EMP."IdSocioDeNegocio" = SDN."IdSocioDeNegocio"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
			ON SDN."IdCliente" = CLI."IdCliente"
			INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteUI" CCU
			ON CLI."IdCliente" = CCU."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
			ON CLI."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE
			ON CCU."IdTipoOperacion" = TOPE."IdTipoOperacion"
			WHERE "IdClasificacionTipoInstrumento" = 'RV'
			AND CCU."IdEstadoDTE" = 'ENV'
			AND EMP."IdEmpresa" = :p_IdEmpresa
			AND EMP."IdEstado" = 'ACT'
			AND CCU."FechaComprobante" BETWEEN :p_FechaInicio AND :p_FechaTermino
			UNION ALL
			-->RELACIONADOS
			
			SELECT
				 CCU."MontoComprobante"
				,TOPE."Accion"
				,CCU."IdCliente"
				,CCU."FechaComprobante"
				,CCU."IdComprobante"
				,CCU."IdMoneda"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteUI" CCU
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
			ON CLI."IdCliente" = CCU."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
			ON CLI."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE
			ON CCU."IdTipoOperacion" = TOPE."IdTipoOperacion"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC
			ON CLI."IdCliente" = CIC."IdCliente"
			WHERE "IdClasificacionTipoInstrumento" = 'RV'
			AND CCU."IdEstadoDTE" = 'ENV'
			--SECCION
			AND CIC."CodigoTipoIndicador" = 'RELCORR'
			AND CIC."ValorIndicador" = 'S'
			AND CCU."FechaComprobante" BETWEEN :p_FechaInicio AND :p_FechaTermino
) AS OPE LEFT  OUTER JOIN --- Sin Notas de Credito 
	(
		SELECT NC."IdComprobanteReferencia"
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" NC
		WHERE NC."IdEstadoFacturacion"='FAC' AND NC."IdTipoComprobante"='NCE' AND NC."IdComprobanteReferencia">0
	) NC
	ON (OPE."IdComprobante"=NC."IdComprobanteReferencia") 
		WHERE NC."IdComprobanteReferencia" IS NULL
;
