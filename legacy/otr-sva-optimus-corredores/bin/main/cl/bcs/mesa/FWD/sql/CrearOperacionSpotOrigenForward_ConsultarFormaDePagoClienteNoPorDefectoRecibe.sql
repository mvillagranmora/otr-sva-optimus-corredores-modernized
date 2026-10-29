SELECT TOP 1
	 PROC."IdCartera"
	,PROC."FolioCartera"
	,PROC."IdPortafolio"
	,PROC."IdTipoOperacion"
    ,FORPAGPOR."IdFormaDePago"
    ,PROC."IdMonedaPrincipal"
    ,PROC."IdMonedaSecundaria"
    ,PROC."EGRIdTipoOperacion"
    ,PROC."EGRMontoMonedaSecundaria"
    ,PROC."EGRMontoMonedaPrincipal"
    ,PROC."INGMontoMonedaSecundaria"
    ,PROC."INGMontoMonedaPrincipal"
FROM "BSCL.Mesa.BD::FWD.CrearOperacionSpotOrigenForward_Proceso" PROC
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa" FORPAGPOR
	ON (FORPAGPOR."IdPortafolio"	= PROC."IdPortafolio" AND
		FORPAGPOR."IdTipoOperacion"	= PROC."IdTipoOperacion" AND
		FORPAGPOR."IdMoneda"		= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN PROC."IdMonedaPrincipal" ELSE PROC."IdMonedaSecundaria" END) AND
		FORPAGPOR."IdEstado"		= 'ACT' AND
		FORPAGPOR."IdEmpresa"		= :IdEmpresa)
WHERE PROC."IdCartera" IN (:IdCartera)
  AND PROC."IdPortafolio" IN (:IdPortafolio);