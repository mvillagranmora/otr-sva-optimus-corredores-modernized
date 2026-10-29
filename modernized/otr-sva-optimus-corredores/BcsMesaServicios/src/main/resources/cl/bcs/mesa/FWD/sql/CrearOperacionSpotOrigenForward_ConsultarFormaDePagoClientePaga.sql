SELECT TOP 1
	 PROC."IdCartera"
	,PROC."FolioCartera"
	,PROC."IdPortafolio"
	,PROC."IdTipoOperacion"
    ,FORPAGPORDEF."IdFormaDePago"
    ,PROC."IdMonedaPrincipal"
    ,PROC."IdMonedaSecundaria"
    ,PROC."EGRIdTipoOperacion"
    ,PROC."EGRMontoMonedaSecundaria"
    ,PROC."EGRMontoMonedaPrincipal"
    ,PROC."INGMontoMonedaSecundaria"
    ,PROC."INGMontoMonedaPrincipal"
FROM "BSCL.Mesa.BD::FWD.CrearOperacionSpotOrigenForward_Proceso" PROC
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa" FORPAGPORDEF
	ON (FORPAGPORDEF."IdPortafolio"		= PROC."IdPortafolio" AND
		FORPAGPORDEF."IdTipoOperacion"	= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN 'VENSPT' ELSE 'COMSPT' END) AND
		FORPAGPORDEF."IdMoneda"			= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN PROC."IdMonedaSecundaria" ELSE PROC."IdMonedaPrincipal" END) AND
		FORPAGPORDEF."IdEstado"			= 'ACT' AND
		FORPAGPORDEF."IdEmpresa"		= :IdEmpresa AND
		FORPAGPORDEF."PorDefecto"		= 'S')
WHERE PROC."IdCartera" IN (:IdCartera)
  AND PROC."IdPortafolio" IN (:IdPortafolio);