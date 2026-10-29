CREATE OR REPLACE FUNCTION "BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(Rut VARCHAR)
RETURNS VARCHAR 
LANGUAGE sql
IMMUTABLE
AS $$
    -- En Postgres SQL, la evaluación de la expresión es el retorno per se
    SELECT LPAD(TRIM(REPLACE(Rut, '.', '')), 11, ' ');
$$;