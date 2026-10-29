/********************************************************** 
Proposito	: Modifica registro en el diccionario
autor		: SHECHENLEITNER - BCS
fecha		: 17/05/2017
***********************************************************/
UPDATE "BSCL.Plataforma.BD::PLA.Diccionario"
SET	"Descripcion" 		= :Descripcion,
    "Abreviado" 		= :Abreviado
WHERE "CodDiccionario" = :CodDiccionario AND "CodLenguaje" = :CodLenguaje
