SELECT DISTINCT "DocumentoIdentidad" AS "Valor1", "CodigoPortafolio" AS "Valor2", "Nemotecnico" AS "Valor3", '' || "FechaPatrimonio" AS "Valor4" 
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodia"
WHERE "FechaPatrimonio" > :p_FechaSistema;
