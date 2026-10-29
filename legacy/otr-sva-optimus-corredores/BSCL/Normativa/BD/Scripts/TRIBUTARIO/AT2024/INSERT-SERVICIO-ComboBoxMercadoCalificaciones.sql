SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 11998233, 'BSCL.Normativa.BD.Vistas::TRI.ConsultaComboBoxMercadoCalificaciones', 0, 'Q', '', '', '', 'S'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=11998233 AND "RutaServicio"= 'BSCL.Normativa.BD.Vistas::TRI.ConsultaComboBoxMercadoCalificaciones' AND "TipoServicio"='Q');

COMMIT;
