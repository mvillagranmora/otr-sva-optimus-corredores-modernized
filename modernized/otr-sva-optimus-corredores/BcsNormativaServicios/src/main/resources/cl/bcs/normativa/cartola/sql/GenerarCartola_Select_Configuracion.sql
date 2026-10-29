/********************************************************** 
Proposito	: Script para obtener de la configuraci�n de cartolas
autor		: NRM
fecha		: 01/09/2017
***********************************************************/


SELECT "IdConfig","IdEmpresa","Descripcion","Tipo", "Nombre","Valor" 
FROM "BSCL.Normativa.BD::CAR.Config" 
WHERE "IdEmpresa" = :IdEmpresa;
