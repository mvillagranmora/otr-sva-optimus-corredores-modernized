/********************************************************** 
Proposito	: Script para obtener los conceptos de custodia
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	
	 "IdPlantilla"
    ,"IdEmpresa"
    ,"Nombre"
    ,"Vigente"
    ,"VigenteDesde"
FROM "BSCL.Normativa.BD::CAR.Plantilla"
WHERE "Nombre" = 'CARTOLA_MASTER'
  AND "Vigente" = 'SI'
  AND "IdEmpresa" = :IdEmpresa 
  AND "VigenteDesde" <= :FechaDesde   
ORDER BY "VigenteDesde" DESC;