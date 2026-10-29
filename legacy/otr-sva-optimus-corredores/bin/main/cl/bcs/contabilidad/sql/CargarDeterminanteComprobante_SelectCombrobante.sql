SELECT "IdTipoContabilidad","Nemo" FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" 
WHERE "Nemo" IN (:nemos) AND "IdEmpresa" = :IdEmpresa AND "Estado" = 'V'