SELECT PPP."IdPreferenciaPagoPortafolio"
	 , PPP."IdEnrolamiento"
	 , PPP."IdPreferenciaPago"
	 , PPP."IdPersonaRelacionada"
	 , PPP."IdCuentaBancaria"
	 , PPP."IdEmpresa"
	 , PPP."IdTarjetaBancaria"
	 , PPP."PorDefecto"
	 , TIPOP."RecibePagaPortafolio"
FROM "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio" PPP
	 LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
	 LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOP ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);
