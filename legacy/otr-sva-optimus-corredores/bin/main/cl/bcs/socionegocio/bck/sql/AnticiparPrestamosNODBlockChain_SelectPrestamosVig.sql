SELECT * FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
where "IdEmpresa" = :p_IdEmpresa 
and "NombreEstado" = 'VIGENTE'
and "FolioSistemaExterno" is not null; 