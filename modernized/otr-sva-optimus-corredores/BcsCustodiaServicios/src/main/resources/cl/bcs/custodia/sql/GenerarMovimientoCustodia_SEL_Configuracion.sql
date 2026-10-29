/********************************************************** 
Proposito	: Script para obtener la configración de custodia
autor		: NRM
fecha		: 04/05/2017
***********************************************************/


SELECT
	CAST(COALESCE(MAX("IngresaAGarantiaSimultaneaDeCompra"),'N') AS NVARCHAR(1)) AS "IngresaAGarantiaSimultaneaDeCompra"
FROM
(
	SELECT
		CASE WHEN "CodigoParametro"='IngresaAGarantiaSimultaneaDeCompra' THEN "Valor" ELSE NULL END AS "IngresaAGarantiaSimultaneaDeCompra"
	FROM (SELECT "CodigoParametro","Valor"
		  	FROM "BSCL.Custodia.BD::CUS.Configuracion"
		   WHERE "IdEmpresa" = :IdEmpresa
		     AND "CodigoParametro" IN ('IngresaAGarantiaSimultaneaDeCompra')
		 )
);