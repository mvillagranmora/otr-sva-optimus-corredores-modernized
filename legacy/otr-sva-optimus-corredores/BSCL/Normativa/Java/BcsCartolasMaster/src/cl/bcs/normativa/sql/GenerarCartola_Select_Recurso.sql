/********************************************************** 
Proposito	: Script para obtener los conceptos de custodia
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	
	 "IdRecurso"
    ,"IdPlantilla"
    ,"Editable"
    ,"Descripcion"
    ,"Grupo"
    ,"Nombre"
    ,"Tipo"
    ,"IdEstado"
    ,"Valor"
FROM "BSCL.Normativa.BD::CAR.Recurso"
WHERE "IdEstado" = 'ACT'
  AND "IdPlantilla" = :IdPlantilla;
