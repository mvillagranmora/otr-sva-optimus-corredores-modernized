SELECT  
 		CAM."IdCargoAbonoMovimiento"
		,CAM."IdTipoMovimiento"
		,CAM."IdConcepto"
		,CAM."IdCuentaInversion"
		,CI."IdCliente"
		,CLI."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
		,CI."IdPortafolio"
		,POR."CodigoPortafolio"
		,POR."Proposito"
		,CI."IdMoneda"
		,CAM."IdCondicionDeLiquidacion"
		,CAM."Monto"
		,CAM."IdEstado"
		,CAM."SaldoPorFechaMovimiento"
		,CAM."FechaSistema"
		,CAM."EsCargoAbonoPorTraspaso"
		,CAM."IdModulo"
		,CAM."FechaEfectivaMovimiento"
		,CAM."FechaIngresoRegistro" 
	    
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"							 AS CAM
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" 			     AS CI
	ON CI."IdCuentaInversion" = CAM."IdCuentaInversion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" 									 AS CNF
	ON CNF."CodigoParametro" = 'PLA.FechaSistema' AND CNF."IdEmpresa" = CAM."IdEmpresa"
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" 										 AS CTO
	ON CTO."IdConcepto" = CAM."IdConcepto"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 						 AS CLI
	ON CLI."IdCliente" = CI."IdCliente"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 		         		 AS POR
	ON POR."IdPortafolio" = CI."IdPortafolio"
INNER JOIN "BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso" AS GCP
	ON GCP."IdCuentaInversion" = CAM."IdCuentaInversion" AND GCP."IdTrabajo" = (:idTrabajo)
WHERE
	CAM."FechaEfectivaMovimiento"  BETWEEN (:fechaDesde) AND (:fechaHasta)
	AND CAM."IdEstado" = 'CRT'
ORDER BY
  CAM."IdCuentaInversion",
  CAM."IdCargoAbonoMovimiento" ASC;