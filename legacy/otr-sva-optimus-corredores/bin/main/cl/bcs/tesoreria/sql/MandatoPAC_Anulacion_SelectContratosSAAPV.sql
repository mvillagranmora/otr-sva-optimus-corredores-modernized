SELECT 
	MPAC."IdMandatoPAC"
	,SAAPV."IdPersona"
	,SAAPV."IdCliente"
	,SAAPV."IdEmpleador"
	,SAAPV."FolioContratoSAAPV" AS "IdFolioContratoSAAPV"
	,SAAPV."IdEstadoEnvioNotificacion"
	,SAAPV."IdEstadoSolicitud"
	,SAAPV."IdEstado"
	,SAAPV."IdGlosa"
	,SAAPV."DescripcionGlosa" 
FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS MPAC 
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" SAAPV ON (MPAC."FolioSAAPV" = SAAPV."FolioContratoSAAPV")
WHERE  MPAC."IdMandatoPAC" IN (:IdMandatoPAC);