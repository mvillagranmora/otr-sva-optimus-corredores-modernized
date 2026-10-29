/********************************************************** 
Proposito	: Script para recuperar información de las garantías a retirar
autor		: ACC
fecha		: 10/10/2018
***********************************************************/

									
SELECT MOV."IdCustodiaOrigen" AS "IdGarantia"
	
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
WHERE MOV."IdMovimientoCarteraEnTransito" in (:IdMovimientosEnTransito)
ORDER BY MOV."IdCustodiaOrigen";
