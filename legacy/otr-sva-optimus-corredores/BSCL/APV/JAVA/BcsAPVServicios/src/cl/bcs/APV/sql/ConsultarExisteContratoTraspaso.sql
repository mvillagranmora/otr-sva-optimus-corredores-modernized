SELECT Contrato."IdEstado",
	   IFNULL(Traspaso."IdTraspaso", 0) AS "IdTraspaso",
	   Contrato."IdContratoSAAPV",
	   Contrato."FolioContratoSAAPV"
FROM  "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato 
LEFT JOIN  "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
	  ON  Contrato."IdContratoSAAPV" = Traspaso."IdContratoSAAPV" 
	  AND Traspaso."TipoMovimiento"  ='INGRESO'
	  AND Contrato."CodigoTipoSolicitud"='TRA'
WHERE Contrato."FolioContratoSAAPV" IN (:IdFolioContratoSAAPV)
AND Contrato."IdPersona" IN (:IdPersona);