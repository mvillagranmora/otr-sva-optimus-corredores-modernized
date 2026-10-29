SELECT 
	 CAR."FolioCartera"
	,CAR."IdCartera"
	,CAR."IdEstado"
FROM  "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
WHERE CAR."FolioCartera" IN (:ListaFolioCartera)
AND   CAR."IdEstado" ='VIG'