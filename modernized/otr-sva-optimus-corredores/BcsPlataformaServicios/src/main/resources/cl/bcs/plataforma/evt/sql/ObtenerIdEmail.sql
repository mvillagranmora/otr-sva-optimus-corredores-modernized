SELECT 
"BSCL.Plataforma.BD.Secuencias::EVT.Mail".nextval AS "IdEmail" 
FROM SERIES_GENERATE_INTEGER(1, 0, :cantidadEmails) 
CROSS JOIN DUMMY;
