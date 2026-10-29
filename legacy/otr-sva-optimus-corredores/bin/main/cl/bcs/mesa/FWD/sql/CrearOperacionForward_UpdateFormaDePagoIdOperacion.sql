UPDATE CFP
SET CFP."IdOperacion" = CAR."IdCartera"
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CFP
--INNER JOIN "BSCL.Mesa.BD::FWD.CrearOperacionForward_Proceso" FWD
	--ON (CFP."IdOperacion" = FWD."IdOperacion")
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
	--ON (FWD."FolioCartera" = CAR."FolioCartera")
	ON (CFP."IdOperacion" = CAR."IdOperacion")
WHERE CAR."IdCartera" IN (:IdCartera);