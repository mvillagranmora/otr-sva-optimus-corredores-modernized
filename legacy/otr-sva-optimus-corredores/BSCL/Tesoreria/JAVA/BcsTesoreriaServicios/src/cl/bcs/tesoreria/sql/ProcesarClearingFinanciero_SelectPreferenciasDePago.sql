SELECT
	"IdTipoMovimiento",
	"IdMedioDePago",
	"IdModalidadDePago",
	"IdCuentaBanco",
	"IdMoneda",
	"IdConcepto"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor"
WHERE  ("IdConcepto" 		IN ('CLFIPM','CFPHSM','CLFIRV','CLFIDV','CLFIHS') OR         -- para clearing financiero CHILE
		"IdConcepto" 		IN ('CLFIRV','CLFIRF','CLFIRP','CLFIRM'))  -- para clearing financiero PERU
AND "IdTipoMovimiento" 	IN ('MOVING','MOVEGR')		
AND "NombreEntidad" 	= 'TES.ClearingFinanciero'
AND "IdEstado" 			= 'ACT'
AND "IdEmpresa"			= :IdEmpresa;	