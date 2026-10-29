SELECT CARABO."IdCargoAbonoMovimiento",DOC."DocumentoIdentidad", CTAINV."IdCliente", POR."CodigoPortafolio", CTAINV."IdPortafolio", CTAINV."IdMoneda", CTAINV."IdCuentaInversion"
	,CASE 
		WHEN CARABO."IdTipoMovimiento" = 'MOVCAR' THEN COALESCE(CARABO."Monto",0)*-1
		ELSE COALESCE(CARABO."Monto",0)
	END AS "Monto"
	,CARABO."FechaCertificacion" AS "FechaMovimiento", CARABO."IdTipoOperacion", CARABO."IdModulo", COALESCE(CARABO."IdComprobante",0) AS "FolioComprobante", CARABO."IdTipoMovimiento", CARABO."EsIngresoManual"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CARABO
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINV ON (CTAINV."IdCuentaInversion" = CARABO."IdCuentaInversion")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (CTAINV."IdPortafolio" = POR."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = CTAINV."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdPersona" = CLI."IdPersona")
WHERE CARABO."IdEstado" = 'CRT' AND DOC."Predeterminado" = 'S'
AND TO_DATE(CARABO."FechaCertificacion") BETWEEN :FechaInicio AND :FechaTermino
AND CTAINV."IdCliente" = case when :IdCliente > 0 then :IdCliente else CTAINV."IdCliente" END
ORDER BY DOC."DocumentoIdentidad", CTAINV."IdCliente", POR."CodigoPortafolio", CTAINV."IdPortafolio", CTAINV."IdMoneda", CARABO."FechaCertificacion";