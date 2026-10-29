SELECT "Codigo", "Descripcion" FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" 
WHERE "IdEmpresa" = :IdEmpresa
AND "IdEstado" = 'ACT'
AND "NombreEntidad" = 'MANTENEDOR'
AND UPPER("CodigoDescripcion") = UPPER(:Fuente);