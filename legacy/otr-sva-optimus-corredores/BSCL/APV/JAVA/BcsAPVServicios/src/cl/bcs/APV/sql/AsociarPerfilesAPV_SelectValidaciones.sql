SELECT
	 'PLAN_ABIERTO_ASOCIADO_CATEGORIA_INVERSIONISTA_EXISTE' AS "Codigo",
	 PRFAPV."IdPerfilComercialAbierto"||'_'||PRFAPV."IdPerfilComercialActivo" AS "Descripcion"
FROM "BSCL.APV.BD::APV.PerfilComercialAPV" PRFAPV
WHERE PRFAPV."IdEmpresa" = :IdEmpresa
  AND PRFAPV."IdEstado" = :IdEstado
  AND PRFAPV."IdCategoriaInversionista" IN (:IdCategoriaInversionista)
  AND PRFAPV."IdPerfilComercialAbierto" IN (:IdPerfilComercialAbierto)
UNION ALL
SELECT
	 'PLAN_ABIERTO_ASOCIADO_PLAN_ACTIVO_EXISTE' AS "Codigo",
	 PRFAPV."IdPerfilComercialAbierto"||'_'||PRFAPV."IdPerfilComercialActivo" AS "Descripcion"
FROM "BSCL.APV.BD::APV.PerfilComercialAPV" PRFAPV
WHERE PRFAPV."IdEmpresa" = :IdEmpresa
  AND PRFAPV."IdEstado" = :IdEstado
  AND PRFAPV."IdCategoriaInversionista" IN (:IdCategoriaInversionista)
  AND PRFAPV."IdPerfilComercialAbierto" IN (:IdPerfilComercialAbierto)
  AND PRFAPV."IdPerfilComercialActivo" IN (:IdPerfilComercialActivo)
UNION ALL
SELECT
	 'PLAN_ACTIVO_ASOCIADO_PLAN_ABIERTO_EXISTE' AS "Codigo",
	 PRFAPV."IdPerfilComercialActivo"||'_'||PRFAPV."IdPerfilComercialAbierto" AS "Descripcion"
FROM "BSCL.APV.BD::APV.PerfilComercialAPV" PRFAPV
WHERE PRFAPV."IdEmpresa" = :IdEmpresa
  AND PRFAPV."IdEstado" = :IdEstado
  AND PRFAPV."IdCategoriaInversionista" IN (:IdCategoriaInversionista)
  AND PRFAPV."IdPerfilComercialAbierto" IN (:IdPerfilComercialActivo)
  AND PRFAPV."IdPerfilComercialActivo" IN (:IdPerfilComercialAbierto)
;
