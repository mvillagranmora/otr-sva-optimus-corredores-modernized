SELECT "IdTipoRegistro","Valor"	
FROM "BSCL.Custodia.BD::CUS.CarteraValoresTipoRegistroAdicionales" 
WHERE "IdEmpresa" = :IdEmpresa;
