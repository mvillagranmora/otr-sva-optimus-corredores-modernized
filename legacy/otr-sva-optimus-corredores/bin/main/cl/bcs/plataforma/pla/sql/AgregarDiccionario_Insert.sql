/********************************************************** 
Proposito	: Insertar nuevo registro en el diccionario
autor		: SHECHENLEITNER - BCS
fecha		: 17/05/2017
***********************************************************/
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"(
    "CodDiccionario",
    "CodLenguaje",
    "Descripcion",
    "Abreviado"
)
VALUES(
    :CodDiccionario,
    :CodLenguaje,
    :Descripcion,
    :Abreviado
);
