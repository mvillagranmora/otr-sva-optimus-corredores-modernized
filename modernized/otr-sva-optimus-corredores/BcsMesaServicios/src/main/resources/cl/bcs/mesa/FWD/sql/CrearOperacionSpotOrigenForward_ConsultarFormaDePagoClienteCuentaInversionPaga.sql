SELECT TOP 1
	 PROC."IdCartera"
	,PROC."FolioCartera"
	,PROC."IdPortafolio"
	,PROC."IdTipoOperacion"
    ,FORPAGCIN."IdFormaDePago"
    ,PROC."IdMonedaPrincipal"
    ,PROC."IdMonedaSecundaria"
    ,PROC."EGRIdTipoOperacion"
    ,PROC."EGRMontoMonedaSecundaria"
    ,PROC."EGRMontoMonedaPrincipal"
    ,PROC."INGMontoMonedaSecundaria"
    ,PROC."INGMontoMonedaPrincipal"
FROM "BSCL.Mesa.BD::FWD.CrearOperacionSpotOrigenForward_Proceso" PROC
INNER JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarPreferenciaPortafolioMesaRF" PREF
	ON (PROC."IdPortafolio" = PREF."IdPortafolio" AND
		PREF."OperaCuentaInversion"	= 'S')
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" FORPAGCIN
    ON (FORPAGCIN."IdMedioDePago"	= 'CIN' AND
        FORPAGCIN."IdTipoOperacion"	= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN 'VENSPT' ELSE 'COMSPT' END) AND
        FORPAGCIN."IdMoneda"		= (CASE WHEN PROC."IdTipoOperacion" = 'COMSPT' THEN PROC."IdMonedaSecundaria" ELSE PROC."IdMonedaPrincipal" END) AND
        FORPAGCIN."IdEstado"		= 'ACT' AND
        FORPAGCIN."IdEmpresa"		= :IdEmpresa)
WHERE PROC."IdCartera" IN (:IdCartera)
  AND PROC."IdPortafolio" IN (:IdPortafolio);