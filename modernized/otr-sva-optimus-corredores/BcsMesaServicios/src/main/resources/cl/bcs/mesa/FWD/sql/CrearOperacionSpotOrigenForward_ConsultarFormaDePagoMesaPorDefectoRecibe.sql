SELECT TOP 1
	 PROC."IdCartera"
	,PROC."FolioCartera"
	,PROC."IdPortafolio"
	,PROC."IdTipoOperacion"
    ,FORPAGMESDEF."IdFormaDePago"
    ,PROC."IdMonedaPrincipal"
    ,PROC."IdMonedaSecundaria"
    ,PROC."EGRIdTipoOperacion"
    ,PROC."EGRMontoMonedaSecundaria"
    ,PROC."EGRMontoMonedaPrincipal"
    ,PROC."INGMontoMonedaSecundaria"
    ,PROC."INGMontoMonedaPrincipal"
FROM "BSCL.Mesa.BD::FWD.CrearOperacionSpotOrigenForward_Proceso" PROC
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" FORPAGMESDEF
	ON (FORPAGMESDEF."IdTipoOperacion"	= PROC."IdTipoOperacion" AND
		FORPAGMESDEF."IdMoneda"			= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN PROC."IdMonedaPrincipal" ELSE PROC."IdMonedaSecundaria" END) AND
		FORPAGMESDEF."IdEstado"			= 'ACT' AND
		FORPAGMESDEF."IdEmpresa"		= :IdEmpresa AND
		FORPAGMESDEF."PorDefecto"		= 'S')
WHERE PROC."IdCartera" IN (:IdCartera)
  AND PROC."IdPortafolio" IN (:IdPortafolio);