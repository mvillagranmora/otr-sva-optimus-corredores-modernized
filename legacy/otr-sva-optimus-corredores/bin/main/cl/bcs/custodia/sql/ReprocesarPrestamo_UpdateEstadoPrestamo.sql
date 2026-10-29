UPDATE "BSCL.Custodia.BD::CUS.PrestamosPendientes" 
SET "Estado" = 'ELIMINADO'
WHERE "IdPrestamosPendientes" = :IdPrestamosPendientes;