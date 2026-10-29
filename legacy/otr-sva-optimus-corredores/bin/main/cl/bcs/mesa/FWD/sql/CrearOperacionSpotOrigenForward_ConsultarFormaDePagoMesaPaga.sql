SELECT TOP 1
	 PROC."IdCartera"
	,PROC."FolioCartera"
	,PROC."IdPortafolio"
	,PROC."IdTipoOperacion"
    ,FORPAGMES."IdFormaDePago"
    ,PROC."IdMonedaPrincipal"
    ,PROC."IdMonedaSecundaria"
    ,PROC."EGRIdTipoOperacion"
    ,PROC."EGRMontoMonedaSecundaria"
    ,PROC."EGRMontoMonedaPrincipal"
    ,PROC."INGMontoMonedaSecundaria"
    ,PROC."INGMontoMonedaPrincipal"
FROM "BSCL.Mesa.BD::FWD.CrearOperacionSpotOrigenForward_Proceso" PROC
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" FORPAGMES
	ON (FORPAGMES."IdTipoOperacion"	= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN 'VENSPT' ELSE 'COMSPT' END) AND
		FORPAGMES."IdMoneda"		= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN PROC."IdMonedaSecundaria" ELSE PROC."IdMonedaPrincipal" END) AND
		FORPAGMES."IdEstado"		= 'ACT' AND
		FORPAGMES."IdEmpresa"		= :IdEmpresa)
WHERE PROC."IdCartera" IN (:IdCartera)
  AND PROC."IdPortafolio" IN (:IdPortafolio);