/********************************************************** 
Proposito	: Script para obtener todos los portafolios de clientes
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	
	 "IdCliente"
	,"IdPortafolio"
	,"CodigoPortafolio"
	,"Proposito"
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdCliente" IN (:IdCliente);