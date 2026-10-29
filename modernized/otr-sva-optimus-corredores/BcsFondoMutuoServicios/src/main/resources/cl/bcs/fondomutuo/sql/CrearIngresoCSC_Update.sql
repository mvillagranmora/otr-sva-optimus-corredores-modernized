UPDATE "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"
SET
	"FolioCSC"					=:FolioCSC
    ,"MontoPesos"				=COALESCE(:MontoPesos,"MontoPesos")     
    ,"MontoUF"				    =COALESCE(:MontoUF,"MontoUF")   
WHERE  "FolioCSC"= :FolioCSC
