SELECT 	Instru."IdInstrumento", 
		Emisor."IdEmisor", 
		Emisor."NombreCompleto" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS 	Instru
	INNER JOIN	"BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 		AS	Emisor
		ON (Instru."IdEmisor" = Emisor."IdEmisor")
WHERE Instru."IdInstrumento" IN (:IdInstrumento);