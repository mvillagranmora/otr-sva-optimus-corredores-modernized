/********************************************************** 
Proposito	: consulta para validar modulo
autor		: RU
fecha		: 07/02/2017
***********************************************************/
  SELECT "IdModulo"
  FROM "BSCL.Plataforma.BD::PLA.Modulo"
  WHERE "IdModulo" = :IdModulo;
