/********************************************************** 
Proposito	: Valida registro en el diccionario
autor		: SHECHENLEITNER - BCS
fecha		: 17/05/2017
***********************************************************/
SELECT "CodLenguaje","CodDiccionario" FROM "BSCL.Plataforma.BD::PLA.Diccionario"
WHERE 	"CodDiccionario" 	IN ( :ListaCodDiccionario )
