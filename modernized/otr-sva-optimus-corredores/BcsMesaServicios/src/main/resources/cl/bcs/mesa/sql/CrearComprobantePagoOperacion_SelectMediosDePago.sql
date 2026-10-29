SELECT 
	 PRE."IdPreferenciaPago" 
	,MED."Nombre" AS "NombreFormaDePago"
	,MED."NombreTipoMedioDePago" AS "NombreMedioDePago"
FROM "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PRE
INNER JOIN  "BSCL.Plataforma.BD::PAG.MedioDePago" AS MED ON MED."IdMedioDePago" = PRE."IdMedioDePago" 
WHERE PRE."IdPreferenciaPago" IN (:IdFormaDePago)
GROUP BY PRE."IdPreferenciaPago" ,MED."Nombre",MED."NombreTipoMedioDePago" 