/********************************************************** 
Proposito	: Script para la recuperar informacion en la tabla MovimientoCarteraEnTransito
autor		: NRM
fecha		: 16/05/2017
***********************************************************/


SELECT  
	"IdMovimientoCarteraEnTransito"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE "IdMovimientoCarteraEnTransitoRelacionado" IN (:IdMovimientoCarteraEnTransito)
AND "IdEstado" = 'PEN'; 
