/* 

Consultar para obtener datos de configuracion cuenta inversion

*/

SELECT "CodigoDefinicionCobro" FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobro" WHERE "IdEmpresa" = :IdEmpresa AND "CodigoDefinicionCobro" IN (:CodigoDefinicionCobro);
