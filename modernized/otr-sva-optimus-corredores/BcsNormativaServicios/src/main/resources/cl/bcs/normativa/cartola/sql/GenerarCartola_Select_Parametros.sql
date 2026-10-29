/********************************************************** 
Proposito	: Script para obtener los conceptos de custodia
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	
	 "IdParametro"
    ,"IdNomina"
    ,"Nombre"
    ,"Valor"
FROM "BSCL.Normativa.BD::CAR.Parametro"
WHERE "IdNomina" = :IdNomina;
