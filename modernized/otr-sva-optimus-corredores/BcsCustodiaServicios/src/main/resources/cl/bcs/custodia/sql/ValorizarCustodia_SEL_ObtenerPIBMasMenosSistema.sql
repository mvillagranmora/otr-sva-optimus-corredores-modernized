/********************************************************** 
Proposito	: Script para la lectura de configuracion de plataforma
autor		: NRM
fecha		: 17/05/2017
***********************************************************/


SELECT 	 cast(ifnull(MAX(t."PLA.PibMas"),'0') as decimal) AS "PibMas"
		,cast(ifnull(MAX(t."PLA.PibMenos"),'0') as decimal) AS "PibMenos"
		,ifnull(MAX(t."PLA.PibTipoTasa"),'') AS "PibTipoTasa"
FROM (
	SELECT
		  "CodigoParametro",
		  "Descripcion",
		  CASE WHEN "CodigoParametro"='PLA.PibMas'    THEN "Valor" ELSE NULL END AS "PLA.PibMas",
		  CASE WHEN "CodigoParametro"='PLA.PibMenos'  THEN "Valor" ELSE NULL END AS "PLA.PibMenos",
		  CASE WHEN "CodigoParametro"='PLA.PibTipoTasa'  THEN "Valor" ELSE NULL END AS "PLA.PibTipoTasa"
	FROM (SELECT "IdEmpresa","CodigoParametro","Descripcion","Valor"
	    	FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
	       WHERE "CodigoParametro" IN ('PLA.PibMas','PLA.PibMenos','PLA.PibTipoTasa') AND "IdEmpresa" = :IdEmpresa
	     )
) t;
