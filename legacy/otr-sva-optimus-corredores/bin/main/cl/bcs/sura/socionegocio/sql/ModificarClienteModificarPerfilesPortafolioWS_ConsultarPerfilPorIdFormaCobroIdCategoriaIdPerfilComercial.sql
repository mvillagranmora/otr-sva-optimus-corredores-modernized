SELECT
COALESCE(PER."IdPerfilCobros",'') AS "IdPerfilCobros"
	,COALESCE(PER."IdPerfilProductos",'') AS "IdPerfilProductos"
	,COALESCE (PER."IdCategoriaInversionista", '') AS "IdCategoriaInversionista" 
	,COALESCE (PER."IdFormaCobro",'') AS "IdFormaCobro"
	,COALESCE (PER ."IdPerfilComercialBase",'') AS "IdPerfilComercial"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PER 
WHERE PER."IdCategoriaInversionista" = (:IdCategoriaInversionista) AND PER."IdFormaCobro" = (:IdFormaCobro);