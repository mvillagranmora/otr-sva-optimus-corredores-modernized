UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "FolioSistemaExterno" = :p_FolioSistemaExterno 
WHERE "IdEmpresa" = :p_IdEmpresa AND "FolioCartera" = :p_FolioPrestamo;  
 