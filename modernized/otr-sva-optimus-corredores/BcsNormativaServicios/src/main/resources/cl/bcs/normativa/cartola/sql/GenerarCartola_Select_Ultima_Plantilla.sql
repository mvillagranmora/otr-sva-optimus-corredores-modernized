SELECT "IdPlantilla"
    ,"IdEmpresa"
    ,"Nombre"
    ,"VigenteDesde" 
FROM "BSCL.Normativa.BD::CAR.Plantilla" 
WHERE "IdPlantilla"=(
	SELECT MAX("IdPlantilla") FROM "BSCL.Normativa.BD::CAR.Plantilla"
	WHERE "Nombre" = 'CARTOLA_MASTER'
  	AND "IdEmpresa" = :IdEmpresa 
);