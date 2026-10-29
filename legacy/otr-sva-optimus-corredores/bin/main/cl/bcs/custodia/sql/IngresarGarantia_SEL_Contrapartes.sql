/********************************************************** 
Proposito	: Script para recuperar información de las contrapartes
autor		: NRM
fecha		: 31/05/2017
***********************************************************/

									
SELECT 
	  CONTR."IdContraparte"
	, CONTR."NombreEntidad" AS "RazonSocial"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia" AS CONTR
WHERE CONTR."IdContraparte" in (:IdContraparte);