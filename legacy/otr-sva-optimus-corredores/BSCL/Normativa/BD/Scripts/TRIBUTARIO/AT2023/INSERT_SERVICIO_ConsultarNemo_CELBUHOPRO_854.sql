SET SCHEMA "BSCL";

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 3616;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3616, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarNemos', 2, 'Q', '', '', '', 'S');

COMMIT;
