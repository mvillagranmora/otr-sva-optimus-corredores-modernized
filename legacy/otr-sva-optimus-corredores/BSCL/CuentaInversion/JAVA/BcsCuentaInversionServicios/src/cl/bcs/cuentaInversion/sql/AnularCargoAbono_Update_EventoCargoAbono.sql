UPDATE CAM
	SET CAM."IdEvento" = EVT."IdEvento"
FROM (SELECT :IdEvento AS "IdEvento", :NumeroOperacionConcepto AS "NumeroOperacionConcepto" FROM DUMMY) as EVT
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAM
ON CAM."IdCargoAbonoMovimiento" = EVT."NumeroOperacionConcepto"
AND CAM."IdEmpresa" = :p_IdEmpresa;