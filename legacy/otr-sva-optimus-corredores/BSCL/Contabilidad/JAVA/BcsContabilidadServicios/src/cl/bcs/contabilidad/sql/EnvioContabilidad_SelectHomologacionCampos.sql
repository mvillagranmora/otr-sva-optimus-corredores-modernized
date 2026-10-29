SELECT "IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion","CampoSAP" || '_' || "IdTipoCentralizacion" AS "IdCampoSAP","CampoTipoCentralizacion" || '_' || "IdTipoCentralizacion" AS "IdCampoTipoCentralizacion"
FROM "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
WHERE "IdTipoCentralizacion" IN (:IdTipoCentralizacion) 
AND "CampoSAP" NOT IN ('ProductoMovimiento','RelacionCorredora','CodSecuencia','ShortName','TipoSocioNegocio','CostingCode','CostingCode2','CostingCode3','CostingCode4','CostingCode5');--Casos especiales no van en la de homologacion.