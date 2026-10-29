UPDATE P
SET P."IdCuentaBancariaPrincipal" = :IdCuentaBancaria
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" P
WHERE P."IdEnrolamiento" = :IdEnrolamiento;