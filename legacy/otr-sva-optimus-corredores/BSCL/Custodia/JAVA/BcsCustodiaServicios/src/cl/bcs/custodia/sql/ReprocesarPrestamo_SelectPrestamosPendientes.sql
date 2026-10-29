 SELECT "TipoRegistro"
 		,"FolioSistemaExterno"
 FROM "BSCL.Custodia.BD::CUS.PrestamosPendientes"
 WHERE "IdPrestamosPendientes" IN ( :p_IdPrestamosPendientes );
