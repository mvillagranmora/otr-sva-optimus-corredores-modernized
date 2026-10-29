INSERT INTO "BSCL.Orden.BD::ORD.CobroRangoOrden"(
"IdCobroRangoOrden"
,"IdCobroOrden"
,"IdOrden"
,"IdGrupoCobro"
,"IdEmpresa"
,"CodigoDefinicionCobro"
,"Hasta"
,"Valor")
VALUES(
"BSCL.Orden.BD.Secuencias::CobrosRangoOrden".NEXTVAL
,:IdCobroOrden
,:IdOrden
,:IdGrupoCobro
,:IdEmpresa
,:CodigoDefinicionCobro
,:Hasta
,:Valor);



