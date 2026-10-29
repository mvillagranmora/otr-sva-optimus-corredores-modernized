INSERT INTO "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero"
(
 "IdIndiceFinanciero", 
 "Periodo", 
 "Nombre", 
 "Descripcion", 
 "IdMonedaParametro", 
 "IdMonedaValor"
 )
VALUES
(
 :p_IdIndiceFinanciero, 
 :p_Periodo, 
 :p_Nombre,
 :p_Descripcion, 
 :p_IdMonedaParametro, 
 :p_IdMonedaValor
 );
 