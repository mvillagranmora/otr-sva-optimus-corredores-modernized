SELECT TOP 1 * 
FROM "BSCL.Custodia.BD::DER.OpcionesEjercicioBloqueo" EJE
WHERE EJE."IdCartera" = :p_IdCartera
AND EJE."IdEstado" = :p_IdEstado;