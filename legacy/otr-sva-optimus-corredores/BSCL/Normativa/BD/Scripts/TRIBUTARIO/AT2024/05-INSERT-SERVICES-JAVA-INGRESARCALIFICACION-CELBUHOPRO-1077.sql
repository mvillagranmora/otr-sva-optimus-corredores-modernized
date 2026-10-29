-- CELBUHOPRO-1077-CAC

SET SCHEMA BSCL;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55676755, 'PLUGIN_NOR.jar@cl.bcs.normativa.IngresarCalificacion', 0, 'J', 'Ingresar calificaciones tributarias a TBSICAVC', '', '', 'S'
FROM DUMMY WHERE
NOT EXISTS(SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55676755 AND "RutaServicio"='PLUGIN_NOR.jar@cl.bcs.normativa.IngresarCalificacion' AND "TipoServicio"='J');
