/********************************************************** 
Proposito	COALESCE(: Script para la actualización de montos valorizados en la tabla Cartera
autor		COALESCE(: NRM
fecha		COALESCE(: 16/05/2017
***********************************************************/


UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET
	<<updates>>
	"INGFechaValorizacionCartera" 							= COALESCE(CURRENT_UTCTIMESTAMP,"INGFechaValorizacionCartera")
WHERE "IdCartera" = :IdCartera;
