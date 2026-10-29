/********************************************************** 
Proposito	: Script para obtener los conceptos de custodia
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	
	TOP 1
	 "IdPlantilla"
    ,"IdEmpresa"
    ,"Nombre"
    ,"VigenteDesde"
FROM "BSCL.Normativa.BD::CAR.Plantilla"
WHERE "Nombre" = 'CARTOLA_MASTER'
  AND "IdEmpresa" = :IdEmpresa 
  AND CAST("VigenteDesde" as date) <= :FechaDesde
ORDER BY "IdPlantilla" DESC;