SELECT 
	 MPAC."IdMandatoPAC"
	,CSC."IdFolioCSC" 
	,MPAC."Estado"
FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC"  MPAC
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"  AS  CSC ON MPAC."FolioCSC" = CSC."FolioCSC"
WHERE "IdMandatoPAC" IN (:IdMandatoPAC) 