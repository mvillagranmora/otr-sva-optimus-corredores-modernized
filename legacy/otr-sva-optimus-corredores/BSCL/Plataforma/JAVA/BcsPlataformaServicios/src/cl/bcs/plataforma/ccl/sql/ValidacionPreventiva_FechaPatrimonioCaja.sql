SELECT DISTINCT "DocumentoIdentidad" AS "Valor1", "CodigoPortafolio" AS "Valor2", '' || "FechaPatrimonio" AS "Valor3"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCaja"
WHERE "FechaPatrimonio" > :p_FechaSistema;