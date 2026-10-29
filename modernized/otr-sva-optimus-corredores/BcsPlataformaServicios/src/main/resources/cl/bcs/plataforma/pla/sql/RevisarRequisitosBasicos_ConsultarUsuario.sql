/********************************************************** 
Proposito	: consulta para validar usuario
autor		: RU
fecha		: 07/02/2017
***********************************************************/
  SELECT "IdUsuario","IdEstado"
  FROM "BSCL.Plataforma.BD::ACC.Usuario"
  WHERE "IdUsuario" = :IdUsuario AND "IdEmpresa" = :IdEmpresa;
  