SELECT TRA."IdEstado",
	COALESCE(TRA."IdTraspaso", 0) AS "IdTraspaso",
	CON."IdContratoSAAPV",
	CON."FolioContratoSAAPV",
	PAC."IdMandatoPAC",
	PAC."Estado" AS "EstadoPAC"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
LEFT JOIN "BSCL.APV.BD::TRA.Traspaso" AS TRA
	ON CON."IdContratoSAAPV" = TRA."IdContratoSAAPV" 
	AND TRA."TipoMovimiento" ='INGRESO'
	AND CON."CodigoTipoSolicitud" = 'TRA'
LEFT JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS PAC
	ON PAC."FolioSAAPV" = CON."FolioContratoSAAPV"
WHERE CON."FolioContratoSAAPV" IN (:IdFolioContratoSAAPV)
	AND CON."IdPersona" IN (:IdPersona);