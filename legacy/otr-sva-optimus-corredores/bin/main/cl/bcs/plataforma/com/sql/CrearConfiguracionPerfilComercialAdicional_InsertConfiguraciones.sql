INSERT INTO "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" (
"IdPerfilComercialAdicional","IdCategoriaInversionista", "IdFormaCobro", "IdPerfilComercialBase", "IdPerfilCobros", "IdPerfilProductos", "IdEstado", "PorDefecto", "IdEmpresa", "CodigoPortafolio") 
VALUES(:IdPerfilComercialAdicional,:IdCategoriaInversionista, INITCAP(:IdFormaCobro), :IdPerfilComercialBase, :IdPerfilCobros, :IdPerfilProductos, :IdEstado, :PorDefecto, :IdEmpresa, :CodigoPortafolio);
