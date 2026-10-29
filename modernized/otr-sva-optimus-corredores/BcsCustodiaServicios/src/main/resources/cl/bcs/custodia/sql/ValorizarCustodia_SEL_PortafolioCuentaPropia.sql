/********************************************************** 
Proposito	: Script para la lectura de portafolios de cuenta propia
autor		: NRM
fecha		: 17/05/2017
***********************************************************/



SELECT "IdPortafolio"
  FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
 WHERE "IdEmpresa" = :IdEmpresa;