SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 198237, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarIPC', 0, 'Q', '', '', '', 'S'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=198237 AND "RutaServicio"= 'BSCL.Normativa.BD.Vistas::TRI.ConsultarIPC' AND "TipoServicio"='Q');


COMMIT;