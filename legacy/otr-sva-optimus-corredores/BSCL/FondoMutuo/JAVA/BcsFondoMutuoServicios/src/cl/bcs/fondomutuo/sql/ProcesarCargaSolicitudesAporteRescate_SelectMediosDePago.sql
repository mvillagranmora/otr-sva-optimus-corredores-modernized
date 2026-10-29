SELECT
	PPP."IdPortafolio",
	PP."IdTipoOperacion",
	PP."IdMedioDePago",
	TIPOP."Nombre",
	PP."IdMoneda" 
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP
INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
WHERE PPP."IdEmpresa" = :IdEmpresa
AND PPP."IdPortafolio" IN (:IdPortafolios)
AND PP."IdTipoOperacion"='RESCFM'
AND PPP."IdEstado" = 'ACT' 