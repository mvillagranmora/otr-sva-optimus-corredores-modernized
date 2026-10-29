SELECT 
	  MPAC."IdMandatoPAC"
	 ,CSC."IdFolioCSC"
	 ,CSC."FolioCSC" 
	 ,MPAC."Estado"  
FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS MPAC 
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" CSC ON MPAC."FolioCSC" = CSC."FolioCSC"
WHERE  CSC."IdFolioCSC" IN (:IdFolioCSC) OR CSC."FolioCSC" IN (:FolioCSC)