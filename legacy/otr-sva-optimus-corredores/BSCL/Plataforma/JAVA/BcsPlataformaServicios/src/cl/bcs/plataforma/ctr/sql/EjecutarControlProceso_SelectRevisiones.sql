
SELECT "NombreClase"
	  ,"DescripcionControlProcesoRevision" AS "DescripcionProceso"
  FROM "BSCL.Plataforma.BD::CTR.ControlProcesoRevision"
WHERE "IdEmpresa" = :p_IdEmpresa   
  AND "IdEstado"  = 'ACT' 