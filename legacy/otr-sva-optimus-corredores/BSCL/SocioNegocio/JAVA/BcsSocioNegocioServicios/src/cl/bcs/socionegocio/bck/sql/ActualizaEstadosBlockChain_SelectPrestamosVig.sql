SELECT "FolioPrestamo", "EstadoAnticipo", "FolioSistemaExterno" FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
where "IdEmpresa" = :p_IdEmpresa 
and "FolioSistemaExterno" is not null;  